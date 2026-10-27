# 009: cycle E tests before and after (tasks 8.1, 8.2)

Date: 2026-10-27. Oracle GraalVM 25.0.4, Maven 3.9.16 offline, release 25.

Before-tree: the cycle D snapshot (cycles A, B, D) with the three new test files, so only cycle E
differs.

- `RootEvaluationContextCharacterizationTest` (5): empty results when nothing was recorded; policy ids in
  id order with one entry per id (the later policy of an id wins) and `clear()`; advice and obligations
  per effect, merged by id, Not-applicable and Indeterminate ignored; rule statuses per rule and the
  decision status for a null rule; 200 rounds of 8 threads writing first at the same time lose nothing.
- `RequestFixedCostCharacterizationTest` (4): repeated categories; attribute-value checks by id, type, and
  issuer; include-in-result keeps category order and only marked attributes; the `AttributeIndex`
  searches (`single`, `findByIssuer`, both `find` forms, `findFirst`) with their order, and their results
  are new modifiable lists, also when empty.
- `DecisionFixedCostPdpTest` (2, real PDP): resolved attributes list only the information point's bag (none
  without it); 4 threads x 250 decisions each get their own decision.

Before D9: 11 of 11 pass (characterization). After D9: the same; the existing `RootEvaluationContextTest`
(TTL minimum, applicable policies only), `AttributeLookupCharacterizationTest`,
`DefaultEvaluationContextHandlerTest`, `CorrelationIdTest`, and `DefaultPolicyDecisionPointTest` pass too.
The first draft of the `AttributeIndex` loops returned immutable or shared empty lists; cycle B's
characterization (`Entity.find(id, issuer)` is modifiable) showed the public behavior, and the loops now
return new lists as the streams did.

Output: `output.txt` (test lines of the before and after runs). The reactor and conformance run of the gate is separate.
