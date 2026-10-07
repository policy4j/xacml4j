REQUEST_CHANGES: the current implementation is not CS01-correct. Do not enable it through the default registry until semantics and propagation are proved. D10 remains the maintainer's choice (`openspec/changes/modernize-core-for-jdk28-valhalla/evidence/builds/061-pending-decisions/summary.md:19`).

Requested identity: OpenAI / `gpt-6-astra`; lane `codex_astra`. Actual identity: OpenAI / `GPT-6.1-sol`, as exposed to this session. This is not an Astra execution receipt.

Reviewed the requested instructions, change artifacts, summaries, and scoped source at HEAD `befdd116bd11385ac8d007732fa8e4d347af3143` plus existing dirty work. SUPPORTED: literal searches and opened source through `rtk rg`/`rtk proxy sed`. Behavioral conclusions below are static traces, not executed reproductions.

For compact references, C = `xacml-core/src/main/java/org/xacml4j/v30`; T = `xacml-core/src/test/java/org/xacml4j/v30`; P = `xacml-pdp/src/main/java/org/xacml4j/v30`. All resolve from the repository root.

1. **HIGH — valid three-child policies are rejected.** `C/policy/combine/impl/OnPermitApplySecondPolicyCombiningAlgorithm.java:62-64` accepts only two children. Every three-child input returns processing-error without evaluating anything. Fix the arity guard according to the table below; test 0, 1, 2, 3, 4, and a larger count, including zero child/context interactions for invalid counts.

2. **HIGH — wrong deciding child, decision subtype, and status.** The same file at `:66-94` evaluates child 1 after every first result except NotApplicable. For two children, first Deny followed by Permit produces Indeterminate{P}; first Indeterminate{P} followed by NotApplicable becomes NotApplicable. With a first missing-attribute error and second Deny, `:78-80` replaces the error with processing-error and Indeterminate{D}. Fix by selecting the branch before creating its context; retain the deciding error status. The helper `C/policy/combine/DecisionCombiningAlgorithms.java:50-52` performs real child evaluation, so these extra calls can invoke resolvers and emit metadata.

3. **HIGH — obligations/advice can escape a non-applicable enclosing policy.** `C/policy/DescendantEvaluationContext.java:130-136` forwards them immediately to root storage (`C/policy/RootEvaluationContext.java:218-250`); `C/policy/BaseCompositeDecisionRule.java:137-155` does not retract them. Counterexample: an outer first-applicable set evaluates an on-permit set with child 0 Permit plus obligation/advice G and child 1 NotApplicable, then an unrelated Permit policy. The inner set returns NotApplicable, but `P/pdp/DefaultPolicyDecisionPoint.java:194-200` returns G with the outer Permit. This violates the enclosing-result filter in [XACML 3.0 OASIS Standard, 22 January 2013, §7.18](https://docs.oasis-open.org/xacml/3.0/xacml-3.0-core-spec-os-en.html). Buffer responses at composite boundaries; add this real-PDP regression and its Deny analogue.

4. **MEDIUM — default discovery cannot reach the implementation, and existing tests cannot certify it.** `C/policy/combine/impl/DefaultXacml30DecisionCombiningAlgorithms.java:47-70` omits registration despite its existing `@AutoService` at `:29`. `T/policy/combine/DecisionCombiningAlgorithmRegistryTest.java:393-408` disables this URN test and checks only provider presence/unrelated lookup. `T/policy/combine/impl/OnPermitApplySecondPolicyTest.java:60-156` covers only empty input and three first-Permit pairs. After D10 approval and fixes, register the instance with `addCompositeRuleCombineAlgorithm`; no new service descriptor or annotation is needed.

The following oracle comes from the **normative pseudocode**, not the introductory prose, in [Additional Combining Algorithms Profile 1.0, CS01, 18 August 2014, §2.1](https://docs.oasis-open.org/xacml/xacml-3.0-combalgs/v1.0/cs01/xacml-3.0-combalgs-v1.0-cs01.html), retrieved through Tavily and independently fetched through DuckDuckGo. Both retrieve the same primary document; this is not independent standards corroboration.

Let S = {P, D, N, I_D, I_P, I_DP}; N means NotApplicable, X ranges over S, and child indexes are zero-based. Status sources apply to Indeterminate outputs.

| Count | Child 0 | Selected result | Output | Evaluation trace | Error status source |
| --- | --- | --- | --- | --- | --- |
| <2 or >3 | irrelevant | none | I_DP | empty | processing-error |
| 2 or 3 | P | child 1 = X | X | 0 → 1 | child 1 |
| 2 | D, N, I_D | none | N | 0 | no response error |
| 3 | D, N, I_D | child 2 = X | X | 0 → 2 | child 2 |
| 2 or 3 | I_P, I_DP | none | I_DP | 0 | child 0 |

Generate all **36 pairs and 216 triples** from this independently encoded oracle, varying unselected results too. Assert exact call counts/order, distinct child contexts, and no creation/evaluation of unselected contexts. A selected NotApplicable must never trigger fallback. Use interface doubles for this table, then real Policy/PolicySet objects through PDP.decide for propagation.

Propagation tests must cover:

- Distinct status codes, nested codes, messages, and details in each child; preserve the deciding Status and reject stale gate/sibling errors. CS01 explicitly requires the first error's code; whole-Status preservation is an additional regression assertion.
- Matching gate and selected-child obligations/advice: P/P includes both; D/third-D includes both; opposite effects exclude the gate. N or Indeterminate exports neither. Include enclosing policy responses, nested discarded branches, same identifiers with distinguishable assignments, and repeated requests.
- Matching obligation/advice assignment failure in gate, selected child, and enclosing set; verify Indeterminate and no leaked responses. Nonmatching expressions and unselected resolvers must remain untouched.

Minimal implementation design: preserve the public class/URN and implement the table directly. Stage responses until each composite's final decision, including its own obligations/advice, is known; commit only matching-effect contributions and discard failures. Isolate deciding status from discarded evaluations. This shared-context correction needs explicit task ownership; changing only the combiner's branches is insufficient. Then enable dedicated CS01 tests through direct default lookup, `withDefaultAlgorithms()`, `withDiscoveredAlgorithms()`, and ServiceLoader; assert correct policy lookup, absent rule lookup, identifier, and behavior. Keep CS01 expectations separate from the registry test's Appendix-C oracle.

Recommendations and limits: add programmatic profile tests in core/PDP; scoped fixture/manifest searches found no profile URN. Preserve vendored fixtures and compare executed conformance case identities, not merely totals. Reconcile the registry spec's identical-baseline-set scenario (`specs/combining-algorithm-registry/spec.md:24`, under this change) with the approved added URN. Legacy plain INDETERMINATE interoperability remains UNVERIFIED; document/test its boundary mapping rather than inventing profile semantics.

UNRUN: builds, tests, conformance, strict OpenSpec validation, JDK 28, JMH, and runtime discovery. Run 068's 7,137 tests/39 skips is supplied historical evidence, not this review's execution. Gate 4.7 and other unresolved task-4.6 choices remain open. No tool approval blocked completed reads. Only this report was written; no code, tracking, fixtures, or other lane reports changed.

DONE_CODEX_ASTRA_20261004