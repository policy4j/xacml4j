# Full-reactor blockers observed after approved POM repair

## Current status after conductor-approved baseline restoration

The conductor approved required-core-fix.patch and
required-xml-content-fix.patch. Both are now applied, after focused tests
were written and recorded red. The earlier descriptions below are retained
as historical diagnosis, not current unresolved status.

- Run 28: 12 ISO8601DateTime operation tests; positive and negative
  day-time subtraction fail. The other ten checks pass. XML is skipped
  because the failed core module blocks its dependent tests in this run.
- Run 29: XML alone reaches the new five-test class; whitespace before
  the DOM element fails, the other four checks pass.
- Run 30: all 12 duration tests and all 5 XML tests pass after the fixes.
- Run 31: full reactor compiles; 677 tests, 2 failures, 0 errors, 9 existing
  skips. IIC104 in both suites, IIIF001 and both IIIG006 invocations now pass.
  The only failures are IIC106 in OASIS 2.0 and OASIS 3.0.

Every checked ISO8601DateTime operation, with positive/negative/zero operands:

| Operation | Before | After | Production change |
| --- | --- | --- | --- |
| plus(ISO8601DayTimeDuration) | 3 pass | 3 pass | None |
| minus(ISO8601DayTimeDuration) | Positive/negative fail; zero passes | 3 pass | Negate the sign-branch condition only |
| plus(ISO8601YearMonthDuration) | 3 pass | 3 pass | None |
| minus(ISO8601YearMonthDuration) | 3 pass | 3 pass | None |

### New blocker: IIC106, both OASIS suites

Classification: **pre-existing core defect**, not an adaptation regression,
XML defect or harness failure. The unchanged fixtures ask
date-add-yearMonthDuration(2002-03-22, -P1Y2M) to produce 2001-01-22.
The production function returns 2002-03-22, so the condition is false and
the decision is NotApplicable instead of Permit.

ISO8601Date.java:145–150 calls duration.toDuration() in both plus/minus.
ISO8601Duration.java:36–37 converts only day/time fields, so this year/month
operand becomes PT0S. ISO8601Date.java matches a8a90d46 exactly. The current
authorization says not to touch other core files, so it remains unchanged.

32-date-period-diagnostics.txt records six fresh production checks across
both date operations with positive, negative and zero operands: four
nonzero-duration checks fail, both zero checks pass. The existing toPeriod()
API retains the signed calendar fields; all six independent candidate
calculations match the expected dates. This is candidate evidence, not a
passing gate after applying the proposal.

required-date-period-fix.patch is a checked, **unapplied** two-line proposal
to use toPeriod() in ISO8601Date.plus/minus. A new conductor scope decision
was requested. Focused tests must precede its application if authorized.

31-conformance-testcases.json retains the latest per-method IDs and traces;
31-Xacml20ConformanceTest-testIICTests-tail.txt and the corresponding 3.0
tail show the unchanged operand, unchanged output and false date equality.

| Suite | Case executions | Failed invocations / IDs |
| --- | ---: | --- |
| OASIS 2.0 | 221 | 1: IIC106 |
| OASIS 3.0 | 221 | 1: IIC106 |
| RSA 2008 | 17 | 0 |
| Examples | 2 | 0 |

OASIS/RSA total: 459 executions, 2 failed invocations, 0 errors. Existing
exclusions and loop bounds remain. Later IIC cases are still blocked by
the first assertion failure at IIC106.
Each OASIS suite retains 30 explicit case exclusions; see
31-existing-case-exclusions.json. These are separate from Surefire's counts.

## Historical run 22 diagnosis

The run `22-reactor-verify-with-conformance.txt` compiled every module, then
failed conformance: 41 Surefire tests, 4 failures, 6 errors, no Surefire skips.
`22-conformance-testcases.json` retains the case IDs and stack traces from that
run before later runs overwrite the live Surefire reports.

## Out-of-scope production defects

| Case | Expected / observed | Cause and classification |
| --- | --- | --- |
| OASIS 2.0 and 3.0 IIC104 | Permit / NotApplicable | Pre-existing a8a90d46 core behavior. `ISO8601DateTime.java:190`–`198` uses the wrong sign branch in `minus(ISO8601DayTimeDuration)`. The production function returns `2002-03-27T10:23:47-05:00` for `2002-03-22T08:23:47-05:00 - P5DT2H0M0S`; the unchanged case expects `2002-03-17T06:23:47-05:00`. `design.md:26`–`27`, tasks 4.3/4.5 already record this defect. |
| OASIS 3.0 IIIF001 | Permit / Indeterminate | Pre-existing a8a90d46 XML mapper behavior. `Xacml30RequestContextFromJaxbToObjectModelMapper.java:482` selects the first mixed-content object, which is leading whitespace in the unchanged request, instead of the following DOM element. Resource content is absent after production unmarshalling, so required selectors cannot resolve. |
| OASIS 3.0 IIIG006, two invocations | Permit / NotApplicable | Same pre-existing XML content loss. The request has a whitespace/DOM/whitespace mixed-content list, but the resulting resource entity has no content. XPath node functions therefore cannot match it. |

`23-conformance-diagnostics.txt` records fresh JDK 11 calls to production
date-time subtraction, JAXB unmarshalling and the production request
unmarshaller, using the unmodified conformance requests. The helper is
`DiagnoseConformance.java`, run by `run-conformance-diagnostics.py` against the
reactor's Surefire classpath.

`required-core-fix.patch` and `required-xml-content-fix.patch` are **unapplied
proposals**, each checked with `rtk git apply --check`. Their behavior has not
been verified after application. Core and XML writes are outside this lane's
current continuation scope; an explicit conductor decision was requested.
No fixture or expected result was changed to hide a failure.

## Test-support comparison errors in run 22

The first XACML 3.0 cases in six groups failed with a null dereference at
`Xacml30TestUtility.java:122`: IIA001, IIB001, IIC001, IID001, IIE001 and
IIIA006. Expected responses omit the optional policy-ID list, whereas the
production marshaller emits an empty list. The fresh IIA001 diagnostic also
confirms absent expected versus empty actual obligations.

The response comparison is being repaired within owned `xacml-test/src/**`.
The original policy-ID count, value and version assertions, and obligation
count, ID and assignment assertions remain. Absent optional collections are
normalized to empty collections. A pre-existing obligation bug that compared
the expected list to itself is corrected to read the actual response.
Focused tests written first reject missing/unexpected IDs and obligations,
version differences and differing obligation assignments, and traverse the
production response marshaller for the empty-response case.
`24-response-comparison-before.txt`: 14 tests, 2 failures, 6 errors.
`25-response-comparison-after.txt`: the same 14 tests, no failures/errors/skips.

## Historical run 26 after the owned comparison repair

`26-reactor-verify-response-comparison.txt`: every module compiles; the full
reactor still fails at conformance. Totals are 660 Surefire tests, 5 failures,
0 errors and 9 existing skips. The six comparison errors from run 22 are gone.
`26-conformance-testcases.json` preserves the fresh case-level results:

| Suite | Surefire tests | Case executions | Failed executions / IDs |
| --- | ---: | ---: | --- |
| OASIS 2.0 | 10 | 219 | 1: IIC104 |
| OASIS 3.0 | 10 | 214 | 4: IIC104, IIIF001, IIIG006 twice |
| RSA 2008 interop | 17 | 17 | 0 |
| Examples | 2 | 2 | 0 |
| Resolver characterization | 2 | Not a conformance case | 0 |

OASIS and RSA execute 450 cases with 5 failed invocations. OASIS case counts
come from each testcase's actual `Executing test=` output, not loop bounds.
RSA case counts come from its 17 test methods. The OASIS `testName` methods
execute no cases. Both suites invoke IIIG006 twice. Existing exclusion sets,
loop bounds and early aborts are unchanged; these counts do not prove that
all vendored cases execute. Later IIC cases remain unverified because each
IIC group stops at IIC104.
