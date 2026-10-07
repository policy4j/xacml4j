# Run 052: donor pre-audit for task 6.5, Entity/Record, designators, adapters (Agy)

Date: 2026-10-02. Lane: `policy4j_code_review_agy` (NTM persona), Antigravity;
pane shows Gemini 3.8 Flash (High) (report self-label unreliable). Session
`policy4j--g1`, pane 3, 125 tool calls. Read-only: donor `b9f114a3`, worktree
model, PDP and marshalling code. Raw report: [agy-report.txt](agy-report.txt),
organized by the three 6.5 lanes.

Reported:
- Core model (8): Record keyed so attributes collide and lose issuer and
  include-in-result (the D8 rationale); Entity equals/hashCode mismatch;
  EntityType stub; EntityId ordering; case-insensitive Issuer and inverted
  Issuer.match (run 041); MustBePresent status suppression; XPath picks the
  first of several nodes (4.9 regression); absent category (4.33).
- PDP runtime (5): resolver failures swallowed (pending decision 4.34; the
  report calls this a violation, but no decision has been made); cyclic
  resolution treated as missing; references to classes and builders that do
  not exist in the donor; MultiRequestContext cache keys.
- Marshalling (6): the donor predates 4.17, 4.19, 4.24, 4.25 and would undo
  them; JSON Category attributes serialized as an object instead of an
  array (JSON Profile 1.1: Attribute is an array); no JAXB boolean adapter
  (4.47).

## Conductor notes (2026-10-02)

- The "reversion" rows restate that the donor predates the group 4 fixes
  (consistent with runs 040 and 043); the merged safety-net and fix tests are
  the guard.
- Not spot-checked this round; earlier spot checks of this reviewer's donor
  claims (runs 041, 044, 045, 046, 048, 050) held, with rejections where the
  reviewer misread the spec. The JSON-array claim is new and unverified;
  check it against JSON Profile 1.1 section 4.2 before the marshalling lane.

Use: plan 6.5 as three lanes (core model, PDP runtime, marshalling) after 6.1
and 5.x; D8 drives the model; the 4.x tests and conformance are the oracle.
