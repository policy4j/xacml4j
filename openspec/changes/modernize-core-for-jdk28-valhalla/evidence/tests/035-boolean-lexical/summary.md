# Boolean lexical forms and manifest follow-up

PASS. The final full JDK 21 reactor `clean verify` completed with 5,594 tests,
0 failures, 0 errors and 32 skips. Conformance executed 857 cases, all passed;
23 cases remain disabled. Six OASIS 3.0 cases changed from skipped to passed.

Java version: 21.0.6, vendor: Oracle Corporation. Maven 3.9.16.
See `jdk-version.txt`, `command.txt`, `10-full-reactor-clean-verify.log` and
`10-full-reactor-results.json`. Every Maven run used
`-Dlicense.skipUpdateLicense=true`; the final gate did not ignore failures.

## Changes

- `xacml-core/src/main/java/org/xacml4j/v30/types/BooleanVal.java:47`: String
  and StringVal inputs use one parser for `true`, `false`, `1`, `0` with XML
  whitespace collapse. Invalid text throws SyntaxException with syntax-error.
  Native Boolean values and canonical serialization are preserved.
- `xacml-core/src/test/java/org/xacml4j/v30/types/BooleanLexicalTest.java`: 27
  new invocations cover three parse routes, all four forms, XML whitespace,
  invalid forms, native values, value/hash equality and lexical round trips.
- `xacml-core/src/test/java/org/xacml4j/v30/types/BooleanValTest.java`: four
  old assertions accepted True/TRUE/FALSE/False. They now require SyntaxException
  and the exact syntax-error status. All five original test methods remain.
  This is the requested correction of an invalid lexical expectation.
- `xacml-json/src/main/java/org/xacml4j/v30/marshal/json/GsonUtil.java:44`:
  textual request flags use the same parser. JVM configuration flags remain
  Java booleans. The inventory and reasons are in `parser-audit.md`.
- `xacml-json/src/test/java/org/xacml4j/v30/marshal/json/BooleanRequestLexicalTest.java`:
  12 request-level invocations cover 1/0, whitespace, yes/TRUE rejection, all
  three text flags, exact syntax-error status, native booleans and flag defaults.
- `xacml-xml/src/test/java/org/xacml4j/v30/xml/BooleanRequestLexicalTest.java`:
  8 request-level invocations cover 1/0, whitespace and yes/TRUE rejection
  through both 2.0 and 3.0 unmarshallers, with exact syntax-error status.
- `xacml-test/src/main/java/org/xacml4j/v20/Xacml30TestUtility.java:144`:
  removed the temporary boolean-only parser. Typed comparison now delegates
  directly to the type factory. The existing comparator tests were retained.
- `xacml-conformance-tests/src/test/manifest/conformance-manifest.csv`: nine
  rows changed initially, then two more after the conductor's XML merge,
  with request/policy/response paths and case order unchanged.
  `manifest-changes.json` records every changed field.
- `../030-harness-comparators/conformance-blockers.md`: only the two
  IIIA030/IIIA330 placeholders changed to `policy4j-the.4.24`.

`task-only.patch` is relative to task-start working-tree bytes, not the branch
base. Existing uncommitted lane work is preserved. This lane did not edit XML
mappers, RequestContext, TypeToString or DoubleVal. The conductor merged XML
mapper/RequestContext changes after run 06; `08-conductor-xml-merge-changes.json`
records their separate hashes and other concurrent additions/removals.
TypeToString, DoubleVal, every POM and vendored fixture retain task-start bytes.
Our existing Java headers are unchanged; new headers match adjacent tests.
The audit covers 3,547 task-start files: 3,533 unchanged, six changes by this
lane, six external modifications and two external .orig removals. There are
three new Java tests by this lane and six external new tests. Source bytes were
stable in each gate pair (05/06 and 09/10). The task-only patch excludes all
external edits. See `scope-and-header-audit.json` and `07-scoped-git-status.txt`.

## Tests first: red, then green

Run 02 executed before any production parser or harness changes. The hashes in
`02-red-production-hashes.json` match the task-start snapshot. All tests compiled.
`maven.test.failure.ignore=true` allowed all three modules to run: its Maven
exit 0 does not mean the test result was green.

| Focus | Run 02 tests | Failures | Errors | Run 03 tests | Failures | Errors |
| --- | ---: | ---: | ---: | ---: | ---: | ---: |
| Core factory/capability and existing BooleanValTest | 32 | 22 | 0 | 32 | 0 | 0 |
| XML 2.0/3.0 requests | 8 | 8 | 0 | 8 | 0 | 0 |
| JSON requests and text flags | 12 | 11 | 0 | 12 | 0 | 0 |
| Total | 52 | 41 | 0 | 52 | 0 | 0 |

The other red-run invocations are passing controls; the native JSON/defaults
test passed before and after. Run 03 did not ignore failures. Run 04 passed all
77 comparator invocations after removing the harness workaround. The initial
manifest guard ran 13 tests with 0 failures/errors/skips after the .4.24 id
updates. The final clean gate reran the guard with the restored cases enabled.

See `02-boolean-lexical-red-results.json`, `03-boolean-lexical-green-results.json`,
`04-harness-comparator-results.json`, `01-manifest-guard-results.json` and the
raw logs for exact classes, failed invocations and status assertions.

## Final reactor counts

Counts below are fresh Surefire reports from run 10, the final full `clean verify`.
Tests run includes skipped invocations. Module and per-class receipts are in
`10-full-reactor-results.json`; Maven Results sections are in
`surefire-summary.txt`. Run 05 selected conformance plus upstream modules;
xacml-json was not in that selected reactor.

| Module | Tests run | Failures | Errors | Skipped |
| --- | ---: | ---: | ---: | ---: |
| xacml-core | 4459 | 0 | 0 | 5 |
| xacml-xml | 53 | 0 | 0 | 0 |
| xacml-json | 21 | 0 | 0 | 1 |
| xacml-pdp | 77 | 0 | 0 | 3 |
| xacml-test | 83 | 0 | 0 | 0 |
| xacml-conformance-tests | 901 | 0 | 0 | 23 |
| Total | 5594 | 0 | 0 | 32 |

These counts include the reviewed lanes merged by the conductor. This packet
adds 47 invocations (27 core, 8 XML, 12 JSON); no original test method is dropped.

Run 06 was green at 5495/0/0/31. A final source check detected concurrent
conductor writes after that gate; no Maven build failed during the merge.
The reviewed XML lane was then tested again in run 09 and the full reactor in
run 10. Additional concurrent changes were three core registry/service-loader
test classes and removal of two .orig backups. This lane did not author them.
They are captured in the external-change receipt and included in run 10.

Three new core skips come from those external safety-net tests, not this
packet's boolean tests: the on-permit-apply-second profile case is disabled
pending task 4.6 semantics; duplicate type and function registration cases are
disabled for `policy4j-the.4.26`. Exact reasons and all nine non-conformance
skips are in `10-full-reactor-results.json`. All 47 new boolean invocations ran.

## Conformance changes and dispositions

Runs 05/06 passed 855 enabled cases. Final runs 09/10 pass 857. The manifest still covers
all 880 requests. The conformance module's 901 invocations include those 880
cases plus 21 guard tests. `10-final-case-results.csv` lists each outcome;
`case-result-changes.json` compares it with run 030. No formerly passing case
regressed and no new case was disabled.

| Suite | Manifest executed | Enabled special harness | Cases passed | Cases failed | Runtime skipped |
| --- | ---: | ---: | ---: | ---: | ---: |
| OASIS 2.0 | 360 | 2 | 362 | 0 | 12 |
| OASIS 3.0 | 475 | 1 | 476 | 0 | 11 |
| RSA 2008 | 17 | 0 | 17 | 0 | 0 |
| Examples | 2 | 0 | 2 | 0 | 0 |
| Total | 854 | 3 | 857 | 0 | 23 |

OASIS 3.0 retains two special-harness rows; IID029 is disabled, so only one of
them contributes to executed cases. OASIS 2.0 has 12 skipped-disposition rows;
OASIS 3.0 has 10. Its disabled special-harness row accounts for the eleventh skip.

- OASIS 3.0 IIA022, IIA024 and IIIA340 passed with merged .4.16 plus the .3.11
  comparator. They are executed, with their bug fields cleared.
- OASIS 3.0 IIIG301 passed with merged .4.22. It is executed, with its bug
  field cleared.
- OASIS 3.0 IIIE301 and IIIE303 passed after the reviewed XML merge and are
  executed, with their bug fields cleared. IIIC001 was already executed and
  passed again. The three requested probes are in `09-requested-xml-cases.json`.
- IIIA030 and IIIA330 remain skipped with .4.24, now recorded in both the
  manifest and run 030 blockers.
- IIIG300 remains skipped with .3.9 under the conductor's fixture/spec
  adjudication: section 5.48 includes all fully applicable policies; the
  fixture expects two ids while the engine returns three. It was not rerun.
- IIA005 stays skipped in both suites with .4.17. The updated reason records
  SyntaxException and the pending request-error response-mapping decision.

Specification sources and the supplied adjudications are in
`spec-references.md`. `conformance-blockers.md` lists every remaining skip and
its bug/reason. No fixture, expected result, policy response mapper or tracking
state was changed. No commit, br command, install, task tick or issue closure.

Residual risk: the 23 disabled conformance cases and nine disabled unit
invocations are not verified by these gates. There is no unfinished
implementation inside this packet's scope.
