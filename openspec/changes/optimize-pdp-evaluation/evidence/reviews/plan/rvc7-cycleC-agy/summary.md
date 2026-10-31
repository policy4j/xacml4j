# RVC7: plan review of D7 (cycle C, type-created bag storage)

Reviewer: Agy, Gemini 4 Argon, NTM pane, read-only. Verdict: APPROVE WITH CHANGES.

| Finding | Disposition |
| --- | --- |
| P1: `boolean` storage as two counts loses which value came first | Applied to D7: two counts and the first value |
| P1: hot callers (`HigherOrderFunctions`, `DecidingOperands`, `BagFunctions.doubleIsIn`, `SetFunctions.double*`, `BagOfValues.single()`, `BaseDecisionRuleResponseExpression`, `Quantified*`) call `values()` | Applied: task 6.5 moves them to indexed or storage methods |
| P1: two equalities on `double` bags (bit equality for storage and Java methods, IEEE `==` for the XACML `double-*` functions) | Applied to D7 and the tests of 6.3 |
| P2: primitive storage would re-box on `value()` and `one-and-only` | Applied: the storage keeps its first value object; `IntegerVal.of` small-value cache |
| P2: `hashCode` must equal Guava's `Multiset.hashCode` formula; the cached field is serialized | Applied to D7 and 6.3 |
| Eclipse Collections | Reviewer agrees with the recorded recommendation |

Task order taken from the review: 6.3 tests (golden streams first), 6.4 storage, 6.5 callers, 6.6 gate.
