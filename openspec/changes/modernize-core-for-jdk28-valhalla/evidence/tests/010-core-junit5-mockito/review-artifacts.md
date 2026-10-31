## Conductor review follow-up: recipe artifacts / oracle strength

Historical result: run **47** on JDK 21 is GREEN: **760 / 0 failures / 0 errors / 2 skipped**. Run 45 also passes with the same counts. At that point, literal 762/four-skips acceptance remained pending. The rv019 follow-up packet specifies the 760/zero/zero/two gate; its fresh result and stronger catch proof are recorded below.

| Conductor item | Evidence / disposition |
| --- | --- |
| 1. Plain RECORD-phase calls on Mockito mocks | Java statement and method-invocation AST scans find zero bare mock calls outside stubbing in current sources. The task-start positive control contains ten void recording sites; all ten map to explicit post-stimulus checks (the higher-order context setup maps to assertInvalid plus AfterEach). |
| 2. Verification inside assertThrows | Zero verify calls inside an assertThrows invocation. AttributeDesignatorTest's pre-existing try/catch that could fall through was strengthened to assertThrows; every original status/reference assertion remains after it. |
| 3. Dropped strict / ordered checks | Every expectation remains in the existing ledger; all ten recorded void calls have checks. InOrder sequences remain. Cache mutation initially survived (run 41) because strict-stub handling marked the extra stubbed call verified. Added 470 explicit cumulative total checks at 202 boundaries in 32 files, alongside ordered checks, to restore exact frequency enforcement. Totals come from expected calls, never actual invocation history. |
| 4. Duplicate consecutive verification | Zero identical consecutive verification statements. Ordered checks establish sequence; companion global times(N) checks establish total frequency, including interleaved extra stubbed calls. These have distinct purposes. |
| 5. Unasserted captors | Initial review confirmed **40** unasserted captors (pre-existing), across seven files / 34 methods. Added key, lazy status, Rule identity, rule-list and context checks. All 65 current captor declarations now have asserted value reads. Initial run 34: 61 / 2 / 0 / 0, due to two new assertions incorrectly assuming a direct context; corrected those new assertions to check extended mode and policy metadata on the existing Indeterminate wrapper. Run 35: **61 / 0 / 0 / 0**. No original assertion was removed. |
| 6. Lost times(N) / once checks | All 17 explicit count sites are accounted for: 15 remain exact mock-count checks; two Condition object-method sites now use real BooleanVal values with explicit type assertions. Implicit once calls keep explicit checks. Companion cumulative counts also enforce earlier expectations during later cached/scenario calls. |
| 7. control.verify-only or empty controls | All 24 methods found without an assertion call in the old body retain explicit interaction and/or no-more checks; expected-exception annotations are now explicit assertThrows. The empty-control mutation fails at verifyNoMoreInteractions. Real-value-only controls disappear with their model/value assertions preserved. |

SUPPORTED: ast-grep represents Java syntax. Receiver identity inference and count correspondence were also reviewed in source; this is not an IDE reference inventory. [Before review](32-review-artifacts-before.json), [final artifact audit](32-review-artifacts-audit.json), [new capture checks](33-captor-oracle-fixes.json), [exact-count checks](44-exact-count-checks.json). No Mockito strictness was relaxed, no assertion/count was adjusted to absorb a test's own normal invocation, and no reset/clearInvocations was added.

### Temporary mutation proof

Mutations changed owned test stimuli/collaborator answers only, to simulate wrong order, extra or missing calls, and wrong captured data. This proves the test oracles reject those faults; it is **not** a mutation-coverage claim for production code. None was an assertion or verification mutation. Every red below executed its test and failed at the listed oracle, not during compilation. The cache check was repeated after repair; the other original oracle checks were retained while the total checks were added.

| Oracle / test | Temporary mutation | Outcome | Restoration |
| --- | --- | --- | --- |
| `TargetTest.testAllMatch` | Reverse two matching children; the result remains MATCH but the original strict order must fail. | InOrder rejects reversed matching children; **1 / 1 / 0 / 0** ([receipt](37-mutant-order-red-receipt.json)) | Exact bytes restored |
| `HigherOrderFunctionsTest.testMapWithValidArguments` | Add a fourth input without removing any expected member; times(4) must reject the fifth flag call. | Exact count rejects five calls where four were recorded; **1 / 1 / 0 / 0** ([receipt](38-mutant-count-red-receipt.json)) | Exact bytes restored |
| `AttributeDesignatorTest.testMustBePresentTrueAttributeDoesNotExistAndContextHandlerReturnsEmptyBag` | Throw the expected exception without invoking the designator; assertThrows succeeds and the following resolve verification must fail. | assertThrows succeeds, then verification rejects missing resolve; **1 / 1 / 0 / 0** ([receipt](39-mutant-exception-verify-red-receipt.json)) | Exact bytes restored |
| `ConditionTest.testExpressionThrowsRuntimeException` | Return a different current Rule; result/status/count checks remain satisfied but the captured Rule identity assertion must fail. | New captured Rule identity assertion rejects a different Rule; **1 / 1 / 0 / 0** ([receipt](40-mutant-captured-rule-red-receipt.json)) | Exact bytes restored |
| `FunctionSpecBuilderTest.testParameterVarArgMinAndMaxEquals` | Insert an unexpected invocation before the expected builder exception; the replacement for the empty control verification must reject it. | No-more-interactions rejects an unexpected invocation with no expectations; **1 / 1 / 0 / 0** ([receipt](42-mutant-empty-control-red-receipt.json)) | Exact bytes restored |
| `PolicyIDReferenceTest.testEvaluateFailedToResolveReferenceAndReturnNull` | Simulate an unnecessary second resolver call after cached evaluation; the result remains Indeterminate but the one-call resolver budget must reject it. | Explicit total rejects a second resolver call after cached evaluation; **1 / 1 / 0 / 0** ([receipt](46-mutant-cache-exact-count-red-receipt.json)) | Exact bytes restored |

Run 41's initially surviving cache mutation is retained as [receipt](41-mutant-cache-red-receipt.json) and [initial patch](41-cache-mutation.patch). Run 46 repeats it against the strengthened source and fails with TooManyActualInvocations: wanted one resolver call, observed two. The code returns to exact pre-mutation bytes, verified by SHA-256 restoration receipts; run 47 then executes every core case successfully.

### Follow-up scope / gate

This follow-up changes seven files for capture/exception checks and adds total-count checks in 32 existing core test files (overlapping sets); the full migration still changes 105 existing sources plus ModelFixtures.java. All 550 original declared test methods and all 760 named invocations remain, including the same two actual disabled tests. License blocks remain byte-identical. Root/core POMs, core main code, vendored inputs and runs 008/009 match task-start hashes.

The shared-tree audit now observes 30 changed paths outside this lane (other module migration sources, one module POM and conductor tracking/evidence files). Authorship is UNATTRIBUTED; this lane's write commands/patches were restricted to core test sources and this run. Those external changes were preserved, not restored. The global preservation audit's original strict mode correctly rejected those differences; its explicit --allow-shared-changes mode records them while still rejecting any change to the protected core/POM/vendored/prior-run paths. [Shared-tree differences](shared-tree-changes.json).

Full reactor/conformance and JDK 28 remain UNRUN in this lane. No commit, br command, task tick, issue closure or out-of-scope fix was performed here.

## rv019 fixes

Result: **PASS**. Run **54** on JDK 21.0.6 executes **760 tests, 0 failures,
0 errors, 2 skipped**. All three repaired methods fail against the scratch
catch mutant and pass against production. No test, assertion or exact count
was removed or weakened.

1. **HIGH — RuleTest.testPermitRuleEvaluateIfApplicableWithTargetEvaluationException**
   (`RuleTest.java:495-513`). A spy of the existing real Target throws the
   original EvaluationException directly from `match`. The stub does not
   set a context status. The real Rule exercises BaseDecisionRule.isMatch's
   EvaluationException catch; the Indeterminate_P decision and both complete
   missing-attribute/Bad status assertions remain. Exact one-call, InOrder
   and no-more-interactions checks now verify Target.match, matching the
   baseline expectation.
2. **MEDIUM — PolicyTest/PolicySetTest.testIsApplicableTargetException**
   (`PolicyTest.java:177-190`, `PolicySetTest.java:174-187`). The same limited
   Target spy throws NullPointerException directly from match. Real Policy
   and PolicySet instances now require isMatch's general Exception catch.
   The original Indeterminate results and PolicySet status check remain;
   Policy also checks the processing-error status. Builders retain all
   original conditions, children, advice, obligations and combiners.

Choice: **spy**, limited to these three exception-injection methods. Normal
real target composition catches evaluation errors in Match or MatchAllOf
before isMatch can see them. The spy keeps the real target's other behavior
and replaces only match at the baseline exception boundary. Every site has
a one-line rv019 comment. No production model or other test mock was changed.

### Catch mutation proof

The helper creates a scratch reactor with byte-identical POMs and core
sources/tests, then removes only both catches surrounding Target.match in
BaseDecisionRule.isMatch. The main worktree's production source is never
edited. The same three named tests run before and after the test repairs.

| Run | Source / tests | Tests / Failures / Errors / Skipped | Result |
| --- | --- | --- | --- |
| 51 | Scratch catch deleted, previous tests | 3 / 0 / 0 / 0 | Mutant survives: confirms the lost catch oracle. |
| 52 | Same scratch mutant, repaired tests | 3 / 0 / 3 / 0 | RED: Rule has uncaught EvaluationException("Bad"); Policy and PolicySet have uncaught NullPointerException. All failures occur during evaluation, not compilation or verification setup. |
| 53 | Production, repaired tests | 3 / 0 / 0 / 0 | GREEN. |
| 54 | Production, every core test | 760 / 0 / 0 / 2 | GREEN: BUILD SUCCESS. |

Receipts: [51](51-rv019-before-mutant-receipt.json),
[52](52-rv019-catch-mutant-red-receipt.json),
[53](53-rv019-focused-green-receipt.json),
[54](54-rv019-core-gate-receipt.json).
Mutation: [catch deletion patch](50-rv019-catch-mutant.patch).
[Restoration proof](50-rv019-production-restoration.json) confirms scratch
source restoration to the exact original bytes. Task-start, final worktree
and restored scratch source all have SHA-256
`eb6af6c7524623816b888db79efc1f73fa7a31449d6b6d2e91e6ba39901eddbe`.

### Exception / status replacement audit

The [complete mapping](55-rv019-exception-boundaries.md) lists every candidate:
**26** original exception expectations map to **26** current throw sites.
Three Target sites are fixed. The real mustBePresent AttributeReference
replacement rethrows the same resolver exception at the original Match
caller boundary (AttributeReference.java:125-129; Match.java:131-137).
The other **22** interface exception sites keep the same collaborator method
and production caller.

The other **22** throw sites replace a baseline model's returned
Indeterminate result. Their inner fixture errors are caught within the real
model; its caller receives the same result before combining it. These are
result/short-circuit tests and do not claim to exercise the caller's exception
catch. Original outer decisions, captures, order and counts remain.

Both `rtk rg` and Java ast-grep find no thenAnswer/doAnswer site after the
sole status-setting Rule answer is removed. The ModelFixtures default
answers only reject unexpected resolver/handler/visitor calls; they never
set a context status. The remaining status setter sites are stubbing or
verification of calls made by production. Coverage: **SUPPORTED** for the
source/AST inventories and the opened exception paths; this is not an IDE
reference inventory or whole-engine mutation coverage.

### Method / header / write scope

| Class | Declared test methods before | After | Run 54 Tests / Failures / Errors / Skipped |
| --- | ---: | ---: | --- |
| RuleTest | 23 | 23 | 23 / 0 / 0 / 0 |
| PolicyTest | 14 | 14 | 14 / 0 / 0 / 0 |
| PolicySetTest | 14 | 14 | 14 / 0 / 0 / 0 |

All three license headers are byte-identical. Of **397** scoped source/POM/doc
paths, only these three test files changed in core. All core main files,
all other core test/resource files and every POM retain task-start hashes.
AGENTS.md also retains its task-start hash.

The strict hash audit detected one shared-tree change outside the three
files: docs/ai/TESTING.md gained the vendored-fixture integrity instructions.
Authorship is **UNATTRIBUTED**; this lane did not edit or restore that file.
The initial rejection is retained in
[the strict receipt](56-rv019-strict-scope-audit.json). The explicit
shared-doc recording pass preserves that change while still rejecting any
core/POM/header/method change outside this repair:
[scope receipt](56-rv019-scope-audit.json).

Maven commands remain plain JDK 21 invocations with -pl xacml-core (no -am)
and -Dlicense.skipUpdateLicense=true; [command.txt](command.txt) and
[surefire-summary.txt](surefire-summary.txt) include all four runs.
No fixture, production, POM, docs, task, issue, branch or commit was changed
by this follow-up. Full reactor/conformance and JDK 28 were UNRUN in this
core-test-only lane. The required core gate and rv019 proof are complete.
