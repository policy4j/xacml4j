# rv032: rv028 fix verification, advice/obligation comparison (Agy)

Date: 2026-10-01. Lane: `policy4j_code_review_agy` (NTM persona), Antigravity,
Gemini 3.8 Flash (High). Session `policy4j--g1`, pane 3. Read-only.

Scope: the advice lane after the rv028 fix (393-line patch): multiset
comparison of attribute assignments, advice and obligations in
Xacml30TestUtility; assertAdvice public; new tests.

Verdict: APPROVE.

- Finding 1 (multiset assignment comparison): FIXED
  (`Xacml30TestUtility.java:161, 178, 185, 192, 205-248`).
- Finding 2 (assertAdvice visibility): FIXED (`:182-187`).
- Finding 3 (tests for repeated AttributeId and duplicate ids): FIXED
  (`Xacml30TestUtilityTest.java:146-181, 219-279`:
  testObligationAssignmentsWithSameAttributeIdAreUnordered,
  testDuplicateObligationIdsAreComparedAsMultiset,
  testAdviceAssignmentsWithSameAttributeIdAreUnordered,
  testDuplicateAdviceIdsAreComparedAsMultiset).

Edge cases (reviewer): a differing value, a differing multiplicity
({X,X,Y} vs {X,Y,Y}), and an assignment moved between two advices with the
same AdviceId all fail; obligations use the same sameMultiset helper.
Conformance in the lane: OASIS 2.0 347, 3.0 347, RSA 17, examples 2;
xacml-test 30 tests and conformance 42 Surefire tests, 0 failures.
