# Research

Status: REQUIRED. Brainstorm research for a future enhancement, by a four-lane NTM swarm on 2026-10-27
(session `xacml4j--pdp-profile-2`, read-only packets).

## Lanes

| Lane | Model | Scope | Report |
| --- | --- | --- | --- |
| BR1 | Claude Opus 5.5 | ALFA and the Axiomatics-style domain model | `br1-opus-alfa-domain-model.md` |
| BR2 | Claude Fable 5.1 | AWS Cedar policies and schema | `br2-fable-cedar.md` |
| BR3 | Codex GPT-6 Astra | default XACML 2.0, 3.0, profile, and 4.0 models | `br3-astra-default-models.md` |
| BR4 | Agy, Gemini 4 Argon | type system: ValueBox (module and design), Cedar, CEL; typed functions | `br4-agy-type-system.md` |
| BR5 | Claude Fable 5.1 | Cedar error model against XACML Indeterminate (decision 4) | `br5-fable-cedar-errors.md` |
| BR6 | Codex GPT-6 Astra | the same question, independent cross-check of BR5 | `br6-astra-cedar-errors.md` |

## Findings (sources in the reports)

1. **ALFA has no standard** (BR1): the OASIS text is a 2015 working draft never voted to a committee
   draft; "ALFA 2.0" was an individual IETF draft (2024), now expired. In practice ALFA is Axiomatics'
   tooling. The only open compiler (`a2x`, Rust) is GPL-3.0; no permissive full grammar exists, so a
   grammar would be written clean-room (ANTLR 4, BSD-3). The Axiomatics dictionary is plain data: short
   name in a namespace, XACML id, category, data type, optional constraint.
2. **XACML 4.0 is published as drafts** (BR1, BR3): ACAL v1.0, XACML v4.0 (XML), and JACAL (JSON), all
   CSD01 of 18 February 2026. Material changes: representation-independent semantics, Policy and PolicySet
   merged, boolean-expression targets, notices for obligations and advice, entities and quantified
   expressions, short identifiers (a machine-readable `ShortIdSet` of 321 names), new namespaces, changed
   argument order of some functions, XPath moved to a profile. Automatic 3.0-to-4.0 translation would be
   lossy.
3. **Cedar** (BR2): version 4.6 (language) as of 2026-10-17, Apache-2.0, formally specified in Lean;
   cedar-java lags the language and is a JNI binding. Almost every Cedar construct maps to xacml4j: scopes
   to targets, `when`/`unless` to conditions, sets to bags, records to `entity` values, `.getTag` to
   `attribute-designator`; gaps: the entity hierarchy `in`, polymorphic `==`, decimal, skip-on-error
   (needs a Cedar combining algorithm: forbid overrides permit, errors skip the policy), and obligations
   (absent in Cedar).
4. **Default models** (BR3): keep "mandatory to implement", "required in a request", and "supplied by a
   resolver" apart (one `required` flag cannot express them); `MustBePresent` is not schema requiredness.
   Profiles have their own attribute bundles (RBAC, SoD, IPC, Export Compliance, Related and Nested
   Entities, Time Extensions, DLP/NAC); catalog entries must not claim behavior a profile needs.
5. **Type system** (BR4): xacml4j has primitive value types, bags, and the `entity` type; ValueBox adds
   sized numerics, struct and entity boxes with schemas, sets and maps, options and sums, refinement types,
   and method-handle operator dispatch, but its module does not compile today. Cedar and CEL show typed
   records, entity types, overloads, and a static type checker. Valhalla value classes later favor value
   carriers without a stored type reference.

## Consensus of the lanes

- Separate layers: standards catalogs (versioned, generated from xacml4j's registries and checked against
  the specification tables), application models (entities, attributes, types, actions, relations,
  constraints), the type system and typed functions, and language front ends.
- Everything opt-in, in new modules where possible; validation starts as diagnostics; decisions unchanged
  with every new feature off.
- Cedar policies compile to the xacml4j policy model (one evaluator) with a Cedar combining algorithm;
  cedar-java only as a test oracle.
- XACML 4.0 / ACAL as an experimental catalog pinned to CSD01, no claim of 4.0 execution.

## BREAKING if done carelessly

Adding constants to the `MediaType` or `XacmlTypes` enums (exhaustive switches in client code); replacing
those enums or the public identifier classes; making model validation mandatory; treating `MustBePresent`
as requiredness; replacing bags by sets; letting bags nest; changing `Decision`, rule, or condition error
semantics globally; automatic 3.0-to-4.0 translation; renaming function short ids to ALFA camelCase.

## Maintainer decisions (2026-10-27)

Answers to questions 1 to 5 below: "1 porting, 2 explain deeper 3 current axiomatic behavior and xacml4j
extensions 4 research 5 adopt".

1. **ValueBox: port.** The type descriptors and schema model of `artagon-valuebox` are ported into
   xacml4j (JDK 25, no artagon dependency); its known module defects (as-built chapter) are fixed in the
   port, not copied.
2. **Model language:** the maintainer asked for a deeper explanation of the options before deciding, then
   asked whether to adopt GraphQL schema (SDL) for entities. Facts (fetched 2026-10-27): the latest GraphQL
   specification is the September 2025 edition (https://spec.graphql.org, OWFa 1.0); graphql-java is MIT
   licensed (https://github.com/graphql-java/graphql-java, docs v25) and parses SDL with ANTLR 4; SDL
   directives are typed and declared with their locations. Options under discussion: Cedar schema syntax
   with annotations, ALFA declarations, JSON with a JSON Schema, GraphQL SDL with xacml4j directives, or a
   canonical JSON model with several authoring syntaxes.
3. **ALFA dialect: current Axiomatics behavior, plus xacml4j extensions** (for example variables,
   entities, quantified expressions); extensions are marked as such so that plain ALFA stays compatible.
4. **Cedar error model: research further** (lanes BR5 and BR6) before deciding between a skip-on-error
   Cedar algorithm, XACML Indeterminate, or a configurable mode.
5. **XACML 4.0: adopt** the ACAL v1.0 / XACML 4.0 / JACAL CSD01 drafts (18 February 2026) as the 4.0
   target, pinned to CSD01 and marked experimental until a committee specification.

## Cedar error model (BR5, BR6)

Facts (both lanes, primary sources fetched 2026-10-27: the Cedar Lean definitions, the Rust 4.14.0 source,
the Cedar designers' rationale, XACML 3.0 Plus Errata 01 sections 7.2, 7.10 to 7.19, and Appendix C):

- Cedar skips a policy that errors and reports it: the decision is Deny if a forbid is satisfied, else
  Allow if a permit is satisfied, else Deny; erroring policies are listed as diagnostics and never decide.
  The Rust engine has one error mode. Cedar's authors chose this for availability (one faulty policy must
  not deny everything) and left the erroring-forbid case to diagnostics and validation. Schema validation
  removes most errors, but not overflow, missing entities, or some extension values.
- XACML propagates errors as Indeterminate{P}, {D}, {DP}; the combining algorithm resolves them, and a
  deny-biased PEP denies on Indeterminate. 66 conformance responses depend on this.
- The risk: an erroring forbid next to a satisfied permit gives Allow under Cedar. In xacml4j this is larger
  than in Cedar, because attributes come from information points at evaluation time, so a resolver outage
  can silently disable every Cedar forbid that reads it (BR5).

Options: A, Cedar exact (skip and report); B, XACML native (compiled rules under deny-overrides); C,
configurable per policy set; D, A plus a fail-closed rule for erroring forbids.

| Set (S2: a satisfied permit and an erroring forbid) | Cedar | A | B | D |
| --- | --- | --- | --- | --- |
| decision | Allow | Permit | Indeterminate | Indeterminate{D} (BR5 D1) or Deny with the raw Cedar result kept (BR6) |

Agreement of the two lanes:

1. **Default A**: exact Cedar semantics in a dedicated Cedar combining algorithm with new identifiers, with
   determining and erroring policy ids as diagnostics. It is the only option that can be differentially
   tested against cedar-java, the Lean CLI, and the Cedar integration corpus, and the Lean proofs transfer.
2. **One opt-in fail-closed variant** for erroring forbids, documented as an xacml4j mode, not Cedar.
3. **No PDP-wide error mode**: `Rule`, `Condition`, the standard algorithms, `Decision`, and the 66
   Indeterminate conformance expectations stay unchanged; everything is additive.
4. **Diagnostics in a typed Java result plus audit and metrics**, not `StatusDetail` on an `ok` status
   (XACML 5.57 forbids it); erroring Cedar policies do not enter `PolicyIdentifierList` (5.48).
5. **Do not map every exception to a skippable Cedar error**: resolver and service failures, unsupported
   features, and resource limits are classified apart from Cedar data errors.

Where they differ, the shape of the opt-in variant:

- BR5 (D1): an erroring forbid makes the Cedar policy set Indeterminate{D} with `missing-attribute` or
  `processing-error`, which keeps XACML's vocabulary, the 7.19.3 resubmission protocol, and correct
  composition under deny-overrides.
- BR6 (D as a guard): compute A, keep the raw Cedar decision and diagnostics, then deny when a forbid
  errored; the mode is fixed in a versioned bundle manifest, never chosen by the request, and is part of
  the compiled-artifact, cache, and audit identity.

Recommendation (conductor, from both reports): A as the default; one opt-in variant that returns
Indeterminate{D} inside an XACML policy tree (BR5 D1) and also records the raw Cedar result in the
diagnostics (BR6); the mode is chosen per compiled policy set by trusted configuration, never per request;
resolver failures are classified separately from Cedar data errors. Not implemented; a decision of the
maintainer.

Open questions from BR5 and BR6 for the maintainer:

- A as the default, or the fail-closed variant as the default because xacml4j users are XACML operators?
- Fail closed only on infrastructure failures (information point exceptions) and skip data errors as Cedar
  does? Not Cedar; it needs its own oracle rule.
- Diagnostics: typed Java result, audit, and metrics only, or also advice on Permit and Deny?
- A Cedar-style facade (`isAuthorized` returning decision, determining and erroring policies) next to
  `PolicyDecisionPoint.decide`?
- Which Cedar release, cedar-java release (4.10.0 trails language 4.6 of SDK 4.14.0), and Lean commit form
  the first compatibility target?

## Open questions for the maintainer

1. **ValueBox**: port its type descriptors and schema model into `xacml-core` (BR4), publish
   `artagon-valuebox` as a library that xacml4j depends on, or use it as design input only (BR3)?
2. **Model language**: Cedar schema syntax with `@xacml.*` annotations (files stay valid Cedar; BR2),
   ALFA declarations (BR1), JSON with a JSON Schema (BR1), or several with converters?
3. **"Default model"**: the standards vocabulary, an application entity schema, or both (BR3)?
4. **ALFA dialect**: the OASIS 2015 draft, current Axiomatics behavior, or the expired ALFA 2.0 draft;
   xacml4j extensions (variables, entities, quantified expressions) allowed? Generated id prefix neutral or
   Axiomatics-compatible (BR1)?
5. **Cedar scope**: language version and features (templates, tags, enumerated entity types, partial
   evaluation); skip-on-error confined to a Cedar algorithm or surfaced as Indeterminate; obligations by
   annotation; entities embedded per request or resolved through PIPs (BR2)?
6. **XACML 4.0**: ship an experimental ACAL/4.0 catalog now from CSD01, or wait for a committee
   specification (BR1, BR3)?
7. **Enforcement**: lint and diagnostics only first, or opt-in request and PIP contract checks (BR3)?
8. **Dependencies**: an ANTLR 4 runtime (BSD-3) in the language modules, or hand-written parsers (BR1,
   BR2)?
9. **Serialization**: must new composite values (entity, struct, set, map) be Java-serializable, or only
   marshalled through XML, JSON, and binary codecs (BR4)?
