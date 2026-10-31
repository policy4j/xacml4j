<!-- Cedar error model research BR6, Codex GPT-6 Astra, NTM pane, read-only (independent cross-check of BR5); report from the pane, paths made repository-relative. -->

FINAL REPORT (xacml4j JMH benchmark engineer, GPT-6)

**Recommendation: use exact Cedar semantics by default for Cedar policies. Offer an explicitly named enforcement option that denies when a forbid policy errors, while retaining the original Cedar result and diagnostics. Keep existing XACML evaluation unchanged.**

Mapping Cedar errors to XACML Indeterminate is a defensible alternative, but it changes the language’s authorization behavior. It should be advertised as an xacml4j execution mode, not ordinary Cedar.

BR6, read-only research, 2026-10-27. No files changed; no builds, tests, benchmarks, Git writes, or `br` writes ran.

Research tools: Tavily search **SUPPORTED**; DuckDuckGo search and public-page fetch **SUPPORTED**; web/PDF fetch **SUPPORTED**; arXiv metadata **SUPPORTED**. GitHub connector **UNAVAILABLE by instruction**, never called; no sign-in. Several guessed source URLs returned 404 and were corrected using public repository trees. Conclusions below use opened sources, not those failed lookups.

All external sources were fetched on 2026-10-27. “Fetched fact,” “repository fact,” and “inference/proposal” distinguish the evidence.

1. **Cedar’s actual contract**

**Fetched fact — versions.** The current released SDK is `cedar-policy` **4.14.0**, released **2026-10-17**, implementing language **4.6**. The Lean source inspected is independently pinned to commit **`5d94a040fcc80fcc55465f57514b21b92a9a0cc2`**, dated **2026-10-26**. This is a current specification snapshot, not an assertion that every theorem in that snapshot shipped with SDK 4.14.0. Sources: [release history](https://raw.githubusercontent.com/cedar-policy/cedar/main/cedar-policy/CHANGELOG.md), [Lean commit](https://api.github.com/repos/cedar-policy/cedar-spec/commits/5d94a040fcc80fcc55465f57514b21b92a9a0cc2).

For a concrete request, linked policies, and an entity store, the Lean authorizer computes:

- `P`: IDs of permit policies whose complete policy expression evaluates successfully to `true`.
- `F`: IDs of forbid policies whose complete expression evaluates successfully to `true`.
- `E`: IDs of policies whose evaluation returns an error.
- **Allow exactly when `P` is nonempty and `F` is empty; otherwise Deny.**

`E` does not enter the decision expression. This is symmetric: an erroring permit and an erroring forbid are both excluded from satisfaction. Sources: pinned [Authorizer.lean](https://raw.githubusercontent.com/cedar-policy/cedar-spec/5d94a040fcc80fcc55465f57514b21b92a9a0cc2/cedar-lean/Cedar/Spec/Authorizer.lean), [Rust 4.14.0 authorizer](https://raw.githubusercontent.com/cedar-policy/cedar/v4.14.0/cedar-policy-core/src/authorizer.rs).

The response has more information than its binary decision:

| Information | Lean response | Current documented SDK behavior |
|---|---|---|
| Decision | Allow or Deny | Allow or Deny |
| Determining policies | Satisfied permits for Allow; satisfied forbids for Deny | Same |
| Default denial | Deny with no determining policies | Same |
| Errors | Set of erroring policy IDs | IDs plus error particulars |

The Lean `Response` does **not** contain detailed error messages or error kinds. Those belong to implementation diagnostics. Sources: pinned [Response.lean](https://raw.githubusercontent.com/cedar-policy/cedar-spec/5d94a040fcc80fcc55465f57514b21b92a9a0cc2/cedar-lean/Cedar/Spec/Response.lean), [authorization documentation, current](https://docs.cedarpolicy.com/auth/authorization.html).

**An erroring forbid can therefore permit access indirectly:** if another permit succeeds and no forbid succeeds, the result is Allow. An error alone never grants access; at least one permit must succeed.

For example, assume the schema permits the request and declares `context.n: Long`:

```cedar
permit(principal, action == Action::"read", resource);

forbid(principal, action == Action::"read", resource)
when { context.n + 1 > 0 };
```

With `context.n = 9223372036854775807`, the addition overflows. The forbid errors; the permit succeeds; Cedar returns **Allow**, with the forbid’s ID in the errors.

This is a **derived example, not an executed test**. Its premises follow from Cedar’s checked integer arithmetic and authorization definition. Schema validation does not rule out this overflow. Sources: [validation documentation](https://docs.cedarpolicy.com/policies/validation.html), pinned [Evaluator.lean](https://raw.githubusercontent.com/cedar-policy/cedar-spec/5d94a040fcc80fcc55465f57514b21b92a9a0cc2/cedar-lean/Cedar/Spec/Evaluator.lean).

**Fetched fact — rationale.** The authors chose skip-on-error partly to contain operational disruption. With blanket deny-on-error, adding one broadly applicable faulty policy can disrupt requests previously authorized by an otherwise working policy collection. The official documentation presents diagnostics as the mechanism through which applications can implement stricter behavior. The designer’s explanation explicitly acknowledges the opposing risk: skipping a faulty forbid can remove an intended restriction. It describes availability and confidentiality/integrity as competing concerns, and explicitly permits applications to reject requests when forbid errors appear. Sources: [official authorization discussion](https://docs.cedarpolicy.com/auth/authorization.html), [designer’s explanation, undated](https://cedarland.blog/design/why-ignore-errors/content.html).

The OOPSLA paper specifies the satisfied-policy decision rule and diagnostics in §3.3, and the principal proof claims in §3.5. It is **PACMPL 8, OOPSLA1, Article 118, April 2024**, DOI `10.1145/3649835`; the accessible extended version is **arXiv v2, 8 March 2024**. Its simplified presentation must not replace the current implementation’s treatment of bounded arithmetic and extensions. Sources: [published paper](https://cdn.amazon.science/96/a8/1b427993481cbdf0ef2c8ca6db85/cedar-a-new-language-for-expressive-fast-safe-and-analyzable-authorization.pdf), [extended version](https://arxiv.org/html/2403.04651v2).

**Fetched facts — validation does not eliminate all errors.**

Policy validation and authorization are separate APIs. Validating policies alone does not establish that actual requests and entity data conform to the schema. The current Lean soundness theorem requires a well-formed schema, valid policies, a valid request, and valid entity data. Under those assumptions, policy evaluation produces a Boolean or one of three modeled error classes: `entityDoesNotExist`, `extensionError`, or `arithBoundsError`. Sources: [validation documentation](https://docs.cedarpolicy.com/policies/validation.html), pinned [Validation.lean](https://raw.githubusercontent.com/cedar-policy/cedar-spec/5d94a040fcc80fcc55465f57514b21b92a9a0cc2/cedar-lean/Cedar/Thm/Validation.lean).

| Error or condition | What validation establishes |
|---|---|
| Integer overflow | Still possible for checked addition, subtraction, multiplication and negation. |
| Missing entity | Still possible when dereferencing an absent principal, resource, literal entity, or referenced entity. Ordinary request/entity schema validation does not establish existence of every referenced entity. |
| Invalid extension constructor input | Strict validation requires acceptable literals for constructors. Non-strict validation can admit computed strings that fail at runtime. |
| Datetime arithmetic overflow | Still possible even when constructor literals validate. Runtime `.offset()` and `.durationSince()` use checked arithmetic. |
| Missing required attributes, wrong attribute types | Excluded under the full validation assumptions; possible if runtime data violates the validated schema. |
| Unguarded optional attributes or tags | Rejected under the corresponding validation rules; absent optional data must be handled by appropriate guards. |
| Implementation resource limits | Outside the mathematical error inventory. Rust 4.14.0 checks remaining stack space and can return a recursion-limit evaluation error. |
| Parse errors, invalid requests, unlinked templates, partial-evaluation unknowns | Distinct boundaries or API modes. They must not all be conflated with ordinary, concrete policy-evaluation errors. |

Sources for the additional distinctions:

- [RFC 19, strict validation](https://raw.githubusercontent.com/cedar-policy/rfcs/7fe9dd452effc02da3f0c4b112b43c3602cc645f/text/0019-stricter-validation.md): accepted **2023-09-05**, released in SDK **3.0.0**.
- [RFC 80, datetime](https://raw.githubusercontent.com/cedar-policy/rfcs/7fe9dd452effc02da3f0c4b112b43c3602cc645f/text/0080-datetime-extension.md): accepted **2024-09-11**, initially experimental in SDK **4.3.0**; checked extension operations also appear in pinned [ExtFun.lean](https://raw.githubusercontent.com/cedar-policy/cedar-spec/5d94a040fcc80fcc55465f57514b21b92a9a0cc2/cedar-lean/Cedar/Spec/ExtFun.lean).
- [Rust 4.14.0 evaluation errors](https://raw.githubusercontent.com/cedar-policy/cedar/v4.14.0/cedar-policy-core/src/evaluator/err.rs), [stack checks](https://raw.githubusercontent.com/cedar-policy/cedar/v4.14.0/cedar-policy-core/src/evaluator.rs).

**Important source qualification:** the validation documentation describes malformed extension values primarily in connection with non-strict mode. That should not be read as “strict validation eliminates every extension error”: datetime arithmetic and the current Lean theorem show the broader residual category.

Entity-slice validation can strengthen the guarantee. The current Lean `validate_with_level_no_dne` theorem additionally requires that the entity store pass `closedAtLevel` for the request and validated level. Merely enabling level validation is insufficient. The closure check is a separate runtime premise. Sources: pinned [Validation.lean](https://raw.githubusercontent.com/cedar-policy/cedar-spec/5d94a040fcc80fcc55465f57514b21b92a9a0cc2/cedar-lean/Cedar/Thm/Validation.lean), [RFC 76](https://raw.githubusercontent.com/cedar-policy/rfcs/7fe9dd452effc02da3f0c4b112b43c3602cc645f/text/0076-entity-slice-validation.md), accepted **2024-10-03**.

**Inference:** error monitoring is not a completeness check. Cedar `in` uses available ancestry, with absent entities contributing no ancestors; `has` and `hasTag` can return false without throwing. Incomplete data can therefore make a forbid false rather than erroneous. Denying on forbid errors does not detect every missing restriction. Source semantics: pinned [Evaluator.lean](https://raw.githubusercontent.com/cedar-policy/cedar-spec/5d94a040fcc80fcc55465f57514b21b92a9a0cc2/cedar-lean/Cedar/Spec/Evaluator.lean) and [Entities.lean](https://raw.githubusercontent.com/cedar-policy/cedar-spec/5d94a040fcc80fcc55465f57514b21b92a9a0cc2/cedar-lean/Cedar/Spec/Entities.lean).

**Fetched proof statements; dependency assessment is my inference.**

| Property established in the inspected Lean source | Does the property force skip-on-error? |
|---|---|
| A **satisfied** forbid implies Deny | No. A stricter binary error-denial rule can preserve this. |
| No satisfied permit implies Deny | No. A stricter binary rule can preserve this too. |
| Allow requires a satisfied permit | No. |
| Allow **iff** some permit succeeds and no forbid succeeds | Yes, its sufficiency direction excludes an additional error-based veto. |
| Adding a permit cannot revoke an existing Allow | Blanket deny-on-any-error violates this when the added permit errors. A forbid-only error guard can preserve it. |
| Adding a forbid cannot turn Deny into Allow | Compatible with stricter error handling. |
| Policy order and duplicate independence | Compatible with an order-independent error guard; not guaranteed by an arbitrary alternative implementation. |
| Determining/erroring ID sets are disjoint | The inspected theorem requires unique policy IDs. |
| Validation and slicing soundness | Their stated assumptions matter. They do not prove that a translated Java evaluator or a modified authorizer is equivalent. |

Source: pinned [Authorization.lean](https://raw.githubusercontent.com/cedar-policy/cedar-spec/5d94a040fcc80fcc55465f57514b21b92a9a0cc2/cedar-lean/Cedar/Thm/Authorization.lean), dated by the specification commit above.

Consequently, “forbid trumps permit” does **not** mean “every intended prohibition survives an error.” It means a forbid that actually evaluates successfully to true wins. Changing error handling requires new equivalence/property arguments; existing Lean proofs do not transfer automatically.

2. **XACML 3.0 preserves uncertainty and delegates its treatment**

**Fetched fact.** The normative source is **XACML 3.0 Plus Errata 01, OASIS Standard incorporating Approved Errata, 12 July 2017**. Sections 7.10–7.14 distinguish:

- `Indeterminate{P}`: an error with potential Permit effect.
- `Indeterminate{D}`: an error with potential Deny effect.
- `Indeterminate{DP}`: either effect remains possible.

An error in a Permit rule’s target or condition produces `{P}`; the equivalent Deny rule produces `{D}`. Policies and policy sets combine their children using their selected algorithms. A nonmatching target produces NotApplicable; an indeterminate policy/set target is handled by §7.14’s table, not simply ignored. Extended Indeterminate values are internal; the standard response exposes plain **Indeterminate**. Source: [core specification, §§7.10–7.17](https://docs.oasis-open.org/xacml/3.0/errata01/os/xacml-3.0-core-spec-errata01-os-complete.html).

The following results are **derived from Appendix C**, not executed. Assume matching targets, no obligations/advice, and these child outcomes:

- `P`: successful Permit.
- `D`: successful Deny.
- `Ep`: erroring Permit rule.
- `Ed`: erroring Deny rule.

| Combining algorithm | `Ep` | `Ed + P` | `Ep + Ed` | `D + Ep` |
|---|---:|---:|---:|---:|
| 3.0 deny-overrides; ordered equivalent, C.2–C.3 | `I{P}` | `I{DP}` | `I{DP}` | Deny |
| 3.0 permit-overrides; ordered equivalent, C.4–C.5 | `I{P}` | Permit | `I{DP}` | `I{DP}` |
| Deny-unless-permit, C.6 | Deny | Permit | Deny | Deny |
| Permit-unless-deny, C.7 | Permit | Permit | Permit | Deny |
| First-applicable, C.8, inputs in displayed order | Indeterminate | Indeterminate | Indeterminate | Deny |
| Only-one-applicable, C.9, each child wrapped in a matching policy | Indeterminate | Indeterminate | Indeterminate | Indeterminate |

Only-one-applicable tests **target applicability**: two matching policy targets cause Indeterminate regardless of whether their conditions would succeed.

Legacy algorithms must also be kept distinct. C.10/C.11 legacy **policy** deny-overrides returns Deny upon an Indeterminate child, whereas its **rule** variant preserves an error unless a decisive Deny exists. C.12/C.13 legacy permit-overrides likewise has different rule/policy treatment: with `D + Ep`, the rule variant yields Indeterminate, while the policy variant yields Deny. Appendix C.1 specifies conversion between extended and unextended Indeterminate values. Source for these rules and the table: [core specification, Appendix C](https://docs.oasis-open.org/xacml/3.0/errata01/os/xacml-3.0-core-spec-errata01-os-complete.html).

**Implication:** neither “XACML skips errors” nor “XACML denies on errors” accurately describes the language. The selected algorithm matters. In particular, 3.0 deny-overrides preserves Permit for `Ep + P`; it is not blanket deny-on-any-error.

**Fetched fact — enforcement.** Section 7.2 defines three PEP behaviors:

| PEP | Handling relevant here |
|---|---|
| Base | Handling of Indeterminate and NotApplicable is undefined. |
| Deny-biased | Only Permit can authorize, subject to discharging its obligations. |
| Permit-biased | Decisions other than Deny authorize, subject to the section’s obligation rules. |

Therefore, **returning Indeterminate is fail-closed only with the appropriate enforcement contract**. Source: [core specification, §7.2](https://docs.oasis-open.org/xacml/3.0/errata01/os/xacml-3.0-core-spec-errata01-os-complete.html).

**Inference about user expectations:** XACML consumers can reasonably expect the declared combining algorithm, the four standard response decisions, and useful error status. A consumer may distinguish explicit Deny from inability to decide, retry a missing-attribute request, or apply a documented PEP bias. The standard expressly permits some request refinement in §7.19.3. There is no evidence here supporting a universal claim that every XACML deployment fails closed.

**Status is not an unrestricted diagnostics envelope.** Section 5.57 prohibits `StatusDetail` alongside `ok`, `syntax-error`, or `processing-error`; missing-attribute details have prescribed content. `StatusMessage` is free-form text. Thus, “put the whole Cedar error collection in ordinary StatusDetail” is not a portable standard mapping. Source: [core specification, §§5.54–5.58](https://docs.oasis-open.org/xacml/3.0/errata01/os/xacml-3.0-core-spec-errata01-os-complete.html).

3. **Consequences for xacml4j and the four options**

**Repository facts.** The proposal requires opt-in behavior and unchanged existing decisions; implementation is not approved. The research consensus proposes one evaluator and cedar-java as an oracle. The maintainer has left the error model open. Evidence: [proposal.md:3](openspec/changes/add-authorization-model-and-policy-languages/proposal.md:3), [proposal.md:32](openspec/changes/add-authorization-model-and-policy-languages/proposal.md:32), [research/README.md:51](openspec/changes/add-authorization-model-and-policy-languages/research/README.md:51), [research/README.md:79](openspec/changes/add-authorization-model-and-policy-languages/research/README.md:79).

Four implementation constraints materially qualify BR2’s suggested direct mappings:

- **Rule errors are already effect-aware.** `Condition.evaluate` records exceptions and returns Indeterminate; `Rule.evaluate` converts that to `{P}` or `{D}`. The existing deny-overrides implementation combines them. Evidence: [Condition.java:87](xacml-core/src/main/java/org/xacml4j/v30/policy/Condition.java:87), [Rule.java:108](xacml-core/src/main/java/org/xacml4j/v30/policy/Rule.java:108), [DenyOverrides.java:67](xacml-core/src/main/java/org/xacml4j/v30/policy/combine/impl/DenyOverrides.java:67).
- **A new combiner alone cannot provide Cedar compatibility.** xacml4j’s XACML Boolean functions retain an earlier error while looking for a later deciding operand. Thus `error || true` becomes true and `error && false` becomes false. Cedar’s left-to-right evaluation propagates that earlier error. Evidence: [DecidingOperands.java:74](xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/DecidingOperands.java:74), pinned [Cedar evaluator](https://raw.githubusercontent.com/cedar-policy/cedar-spec/5d94a040fcc80fcc55465f57514b21b92a9a0cc2/cedar-lean/Cedar/Spec/Evaluator.lean). This qualifies BR2’s `when`/`unless` mapping at [br2-fable-cedar.md:74](openspec/changes/add-authorization-model-and-policy-languages/research/br2-fable-cedar.md:74).
- **Recorded errors currently disappear from successful decision results.** `DefaultPolicyDecisionPoint.createResult` consults evaluation status for Indeterminate, but constructs Permit/Deny results with `Result.ok`. Merely recording skipped Cedar errors in the context will not deliver them to callers. Evidence: [DefaultPolicyDecisionPoint.java:186](xacml-pdp/src/main/java/org/xacml4j/v30/pdp/DefaultPolicyDecisionPoint.java:186).
- **Cedar’s final default Deny must remain Deny.** Exposing NotApplicable instead changes composition and permit-biased enforcement. Likewise, Cedar policy IDs mapped to individual XACML rules are not automatically represented by the existing collection of composite policy references. Evidence: [BR2’s proposed mappings:70](openspec/changes/add-authorization-model-and-policy-languages/research/br2-fable-cedar.md:70), [Result.java:45](xacml-core/src/main/java/org/xacml4j/v30/Result.java:45).

**Derived decision matrix.** Here `P` means a matching Cedar permit and `D` a matching forbid. Option B maps their effects to XACML Permit/Deny rules. The table assumes ordinary policy-evaluation errors and no infrastructure failure.

| Policy set | A: exact Cedar | B: 3.0 deny-overrides, raw result | C: configured default A | D: deny on forbid error |
|---|---:|---:|---:|---:|
| Only `Ep` | Deny | `I{P}` | Deny | Deny |
| `Ep + P` | Allow | Permit | Allow | Allow |
| `Ed + P` | **Allow** | **`I{DP}`** | **Allow** | **Deny** |
| Only `Ed` | Deny | `I{D}` | Deny | Deny |
| Only `Ep + Ed` | Deny | `I{DP}` | Deny | Deny |
| `D + Ep` | Deny | Deny | Deny | Deny |
| `P + D` | Deny | Deny | Deny | Deny |
| Empty set, or all policies false | Deny | NotApplicable | Deny | Deny |

For C, selecting B or D produces the respective column; configuration is not a fifth semantic rule. Extended values in B become plain Indeterminate on a conforming XACML wire response.

**A — exact Cedar, with diagnostics.**

- Use a Cedar-specific combining algorithm, limited to compiler-produced Cedar policies. Preserve checked arithmetic, expression order, entity behavior, and whole-policy error propagation.
- Collect determining IDs and policy errors independently of the decision. Continue sufficiently to reproduce the complete diagnostics; the inspected Rust authorizer evaluates the policies and accumulates errors rather than returning immediately upon a satisfied forbid.
- A `StatusMessage` can provide a human-readable summary. Use a typed Cedar result/diagnostics API for the complete data. A custom Status extension would require an explicit interoperability contract.
- Differential testing can demand exact decisions, determining-ID sets and error-ID sets against matching versions of Cedar. Compare detailed errors by normalized kind where available, not unstable message wording.
- Security: a failed forbid can cease to restrict a successful permit. Availability follows Cedar’s intended behavior.
- Mixing: keep a Cedar bundle as a semantic boundary. Its default Deny can override an otherwise successful native XACML Permit under an outer deny-overrides policy. Route authorization domains explicitly.

**B — XACML-native uncertainty with 3.0 deny-overrides.**

- Preserve Cedar expression evaluation, but convert a policy error to the effect-appropriate Indeterminate and use the existing 3.0 algorithm.
- This exposes uncertainty naturally to XACML consumers and composes through standard XACML algorithms.
- Require a deny-biased PEP if the desired operational result is denial on uncertainty. An outer permit-overrides policy can otherwise defeat the intended restriction.
- Full Cedar differential equivalence is intentionally lost. Test Cedar expression outcomes against Cedar, then test the changed composition against explicit XACML expectations.
- This remains tolerant of an erroring permit beside a successful permit. It is stricter principally where potential Deny matters.
- Label it something such as `CEDAR_EXPRESSIONS_XACML_COMBINING`; calling it ordinary Cedar would conceal a behavioral difference.

**C — selectable behavior.**

Two scopes are possible:

| Configuration scope | Benefit | Cost |
|---|---|---|
| Per PDP | Simple operational setting | Affects every Cedar bundle in that PDP; moving a bundle can silently change its meaning. |
| Per compiled policy bundle/set | Meaning travels with the artifact and can be audited | Requires validation of mixed modes and explicit nesting rules. |

My proposed default is **A**, selected in a versioned, trusted bundle manifest. Allow an explicitly selected D guard. B can exist as a separately named interoperability mode if needed.

The request must not choose or weaken the mode. Include the mode and semantic version in compiled-artifact identity, cache identity, audit records and differential-test fixtures. A silent global flag is too easy to overlook during policy review.

**D — exact Cedar result plus an enforcement guard.**

Compute A first. Preserve its raw decision and diagnostics. Then apply:

```text
effectiveAllow =
    cedarDecision == Allow
    AND no evaluated forbid policy errored
```

This produces the D column while retaining a trustworthy Cedar oracle result. Report the guard’s denial reason separately; do not claim the failing forbid was a satisfied Cedar determining policy.

This is directly consistent with the application-level possibility discussed in the [Cedar designer’s explanation](https://cedarland.blog/design/why-ignore-errors/content.html). The wrapper itself is an xacml4j proposal, not a Cedar standard.

For a flat set with only effect-typed policy errors, **B with a deny-biased PEP and D authorize the same requests**. They differ in the result contract and subsequent composition: B exposes uncertainty; D retains the exact Cedar evaluation and applies an explicit enforcement decision.

D still permits denial-of-service through a broadly applicable faulty forbid. It also does not detect a forbid that becomes false because ancestry or other data is incomplete. The guard must sit at an enforcement boundary where a later Permit cannot override it.

**All four options can leave XACML conformance unchanged** if they are isolated to the new Cedar front end and new identifiers. None justifies modifying standard logical functions, the existing Rule/Condition contract, or standard combining algorithms.

4. **Recommended design, verification, risks and remaining decisions**

**Proposal.** Keep the product default for Cedar as A; expose D as a named, optional enforcement profile. Retain existing XACML behavior regardless of whether the Cedar module is installed. Do not make B the implicit interpretation of `.cedar` files.

Concrete candidate types, all new rather than changes to `Decision`:

- `org.xacml4j.cedar.compile.CompiledCedarBundle`: language version, policy-ID mapping, schema identity and execution profile.
- `org.xacml4j.cedar.eval.CedarPolicyOutcome`: satisfied, unsatisfied, or a typed policy error, retaining the policy’s effect.
- `org.xacml4j.cedar.eval.CedarDiagnostics`: determining IDs and policy errors.
- `org.xacml4j.cedar.api.CedarAuthorizationResult`: raw Cedar decision, diagnostics and optional enforcement outcome.
- `org.xacml4j.cedar.combine.CedarCombiningAlgorithm`: exact Cedar combination.
- `org.xacml4j.cedar.enforcement.ForbidErrorGuard`: the explicit D behavior.

These are design suggestions, not existing classes or an implementation plan approved by the maintainer.

Reuse the evaluator infrastructure, but provide Cedar-specific expression operations where semantics differ. In particular, preserve left-to-right error propagation, checked Long arithmetic, and Cedar entity lookup. A compiler may initially keep the entire Cedar policy predicate in a Cedar-specific condition expression rather than forcing its logic through XACML target truth tables.

Do **not** translate every Java exception or every Indeterminate into a skippable Cedar policy error. Classify compiler defects, resolver/service failures, unsupported features and resource limits explicitly. Rust’s own recursion-limit errors show that implementation-limit compatibility needs a documented boundary; the Lean mathematical result alone does not settle it.

Keep diagnostics request-local and immutable once returned. Avoid storing mutable error lists on shared compiled policies. Errors can contain operand values, so expose detail according to the caller’s access, rather than automatically including raw inputs in Status messages. The SDK error structures demonstrate that such payloads can include values. Source: [Rust 4.14.0 error definitions](https://raw.githubusercontent.com/cedar-policy/cedar/v4.14.0/cedar-policy-core/src/evaluator/err.rs).

**Oracle version risk.** Maven Central currently lists cedar-java **4.10.0**, metadata last updated **2026-05-14**. Its inspected release branch uses Cedar `release/4.10.x`, whereas the current language release is SDK 4.14.0/language 4.6. Sources: [Maven metadata](https://repo.maven.apache.org/maven2/com/cedarpolicy/cedar-java/maven-metadata.xml), [pinned Java FFI manifest](https://raw.githubusercontent.com/cedar-policy/cedar-java/7e3e1bd4838ff2c11db937dfeb0366d8838efe23/CedarJavaFFI/Cargo.toml).

Therefore, pin and record **three** versions independently: compiler target, Java/native oracle, and Lean commit. Use cedar-java for their supported common subset; do not claim language-4.6 coverage merely because Java differential tests pass. The Java wrapper also distinguishes operation-level failure from a successful authorization response that contains policy errors. Sources: [AuthorizationResponse](https://raw.githubusercontent.com/cedar-policy/cedar-java/4f8766dcfe5aeb01876b4dcd71edd1a69f8e77dd/CedarJava/src/main/java/com/cedarpolicy/model/AuthorizationResponse.java), [AuthorizationSuccessResponse](https://raw.githubusercontent.com/cedar-policy/cedar-java/4f8766dcfe5aeb01876b4dcd71edd1a69f8e77dd/CedarJava/src/main/java/com/cedarpolicy/model/AuthorizationSuccessResponse.java), development snapshot inspected 2026-10-27.

**Future verification required; none executed in this lane:**

1. Turn every decision-matrix row into an end-to-end policy/request test, including raw decision, effective decision and diagnostics.
2. Exercise overflow in a schema-valid forbid, absent entities, malformed non-strict extension input, datetime overflow, and runtime data violating the schema.
3. Test `error || true`, `true || error`, `error && false`, and `false && error`, including ordered `when`/`unless` clauses.
4. Test policy permutation, duplicate handling, determining/erroring IDs, and complete diagnostics after a decisive forbid.
5. Differentially compare A with pinned Cedar and Lean; compare D’s **raw** result with A and its enforcement result with the explicit guard.
6. Test mixed Cedar/XACML roots under deny-overrides, permit-overrides and domain routing. Include empty/nonmatching Cedar bundles.
7. Verify standard XML/JSON decision serialization, legal Status content, request isolation and cached results.
8. Run the existing conformance suites without fixture or expectation changes. The current runner constructs policies and PDPs through the standard providers; preserve that path. Evidence: [ConformanceCaseRunner.java:98](xacml-conformance-tests/src/test/java/org/xacml4j/v20/ConformanceCaseRunner.java:98).

**BREAKING if introduced globally or changed after release:**

- Changing standard `Rule`, `Condition`, Boolean-function or combining semantics.
- Reinterpreting existing Indeterminate decisions as skipped policies.
- Changing a Cedar bundle from A to B/D without an explicit artifact/version change.
- Replacing default Cedar Deny with NotApplicable at a shared composition boundary.
- Making schema or entity-completeness validation mandatory for existing XACML users.
- Changing public result/enum contracts to carry Cedar-specific state.
- Treating existing obligation failures as skippable Cedar errors. Cedar’s base semantics do not supply an obligation model.

**Open questions for the maintainer:**

1. Does “Cedar support” promise equivalent authorization decisions and diagnostics, or only Cedar syntax with XACML execution? This determines whether A is a compatibility requirement.
2. Should D be a built-in enforcement profile, and which trusted deployment artifact selects it?
3. When both languages apply, should authorization require both domains to permit, use an explicitly chosen outer algorithm, or route to exactly one domain?
4. Are incomplete entity data and PIP outages rejected before Cedar evaluation, or admitted under Cedar’s missing-data semantics?
5. Which implementation/resource errors follow Cedar skip behavior, and which are operation failures outside the policy decision?
6. Must complete Cedar diagnostics cross the existing XACML transport, or is a typed Cedar API plus audit output sufficient?
7. Which released Cedar/cedar-java pair and Lean commit define the initial compatibility target?

**Verification and residual risk:** the report is supported by public standards, tagged Rust source, pinned Lean source, RFCs, and read-only repository inspection. The decision tables and proposed architecture are reasoned derivations, not runtime measurements. The main unresolved risks are translation fidelity, incomplete entity data, mixed-policy composition and oracle version drift. No implementation or conformance acceptance is claimed.
