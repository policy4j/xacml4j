# rv058: task 4.4 follow-up (rv048) and bugs 4.33, 4.27 review, lane g44f (Agy)

Date: 2026-10-02. Lane: `policy4j_code_review_agy` (NTM persona), Antigravity;
pane shows Gemini 3.8 Flash (High). Session `policy4j--g1`, pane 3, 13 tool
calls. Read-only: lane g44f patch (author Luna/Codex), sources, evidence 049.

Verdict: APPROVE (2 LOW).

Verified: expected lexical strings for all types (no self-reference); alias
resolution parses through the resolved type; combining checks are behavioral
(no class names); 236 section 10.2.8 ids resolved through the provider, with
disabled rows only for 4.36, 4.37 and the AnyURI rows (4.15); 4.33 absent
category returns an empty bag or missing-attribute; 4.27 `@AutoService` on
the default combining provider, ServiceLoader and `withDefaultAlgorithms()`
tested. The conductor's `-0` change (round trip of the sign bit instead of a
fixed spelling) is correct under XML Schema 3.2.5.

1. LOW, `TypeRegistryTest.java:152`: the sample "YQ" is outside the
   xs:base64Binary lexical space (XML Schema 3.2.16 requires padding); the
   engine accepts it. Filed as policy4j-the.4.48; the test should use valid
   input and a separate test should pin rejection once fixed.
2. LOW, `DecisionCombiningAlgorithmRegistryTest.java:55-121`: the
   fingerprint inputs contain no Indeterminate (or extended Indeterminate)
   results, so 1.0 and 3.0 deny-overrides/permit-overrides look the same;
   ordered vs unordered is not observable on fixed lists (should be stated).
   Fix: add Indeterminate{P}/{D}/{DP} vectors where Appendix C differs.

## Merge (2026-10-02 03:01)

Lane g44f applied to the main worktree. AttributeDesignatorFunctionTest had
also changed through the g41 merge; the conductor replaced its two disabled
4.33 tests with the lane tests (same cases plus interaction checks) and
removed the unused import. In AttributeResolutionSafetyNetTest the
absent-category optional test is enabled (passes with 4.33 fixed); the
required-attribute absent-category test is relabelled policy4j-the.4.43 (the
invocation wrapper still reports processing-error). Check lane: the two
classes run 6 and 21 tests, 0 failures, 4 skipped.
