# rv018: tasks 2.2 and 2.3 review, xacml-core JUnit 5 + Mockito (Agy)

Date: 2026-10-01. Lane: `policy4j_code_review_agy` (NTM persona), Antigravity,
Gemini 3.8 Flash (High) (report header names "gemini-2.5-pro"; recorded as
reported). Session `policy4j--g1`, pane 3. Read-only; full-patch review.

Scope: xacml-core/src/test migration, 106 files (baseline: post-2.1
snapshot), evidence run `tests/010-core-junit5-mockito`.

Verdict: APPROVE. Findings: none.

Reviewer statements:
- JDK 21: 760 run, 0 failures, 0 errors, 2 skipped. Declared @Test methods
  550 before and after; 760 named invocations before and after. The 762 -> 760
  and 4 -> 2 Surefire deltas are the class-level @Ignore helpers TestFunctions
  and TestInstanceFunctions (no test methods), which Vintage reported and
  Jupiter does not (conductor verified the source counts independently).
- No JUnit 4 / EasyMock references in the 107 test sources; license headers
  byte-identical on all 106 pre-existing files; new ModelFixtures.java has the
  LGPL header.
- Anti-patterns (AST and regex scans): 0 verify inside assertThrows lambdas,
  0 test-side calls on mocks, 0 unasserted captors (all 65 ArgumentCaptor
  declarations asserted); strict mocks converted to InOrder; 470 cumulative
  verify(mock, times(N)) checks across 32 files; 5 obsolete reset(context)
  calls removed in LogicalFunctionsTest.
- Model and value classes are real instances; mocks are restricted to
  interfaces (Expression, EvaluationContextHandler, ...); RuleTest,
  PolicyIDReferenceTest and PolicySetIDReferenceTest keep the original
  semantics with real metadata.
- The six mutation receipts (runs 37-42, 46) show the restored oracles fail on
  reversed order, wrong counts, exception verification, captured rules, empty
  controls and cache misses.
- Noted: PolicyTest and PolicySetTest (~330-335) have captures of rules that
  the baseline did not assert either (pre-existing).
