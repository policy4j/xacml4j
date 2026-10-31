# Conformance manifest draft

The CSV contains one row per request fixture in the scoped suites, plus each
RSA request and each example. Fixture paths are repository-relative. The
disposition distinguishes current harness coverage from the suite's full
request-file universe; the two latest-run columns record run 46 evidence.
No fixture or source file was modified, and no Maven command was run.

## Fixture universe and run 46 disposition counts

| Suite | Request rows | Executed and passed | Skipped by disposition | Not reached by current loop | Special harness | Missing files |
| --- | ---: | ---: | ---: | ---: | ---: | ---: |
| OASIS 2.0 | 374 | 346 | 20 | 6 | 2 | 0 |
| OASIS 3.0 | 487 | 346 | 20 | 119 | 2 | 0 |
| RSA 2008 | 17 | 17 | 0 | 0 | 0 | 0 |
| Examples | 2 | 2 | 0 | 0 | 0 | 0 |
| **Total** | **880** | **711** | **40** | **125** | **4** | **0** |

The OASIS cells count distinct case IDs because the CSV has one row per case.
Run 46's `case_ids` arrays contain 347 invocation events in each OASIS suite,
but only 346 unique IDs: `IIIG006` runs once in `testIIIGTests` and again in
`testExecuteIIIG006` (`Xacml20ConformanceTest.java:191-205`, likewise the
3.0 harness). Run 46 reports both OASIS suites with no failed methods. Its
summary reports 17 RSA cases and both examples passing. The RSA and example
JSON recorder entries have no case IDs, so their executed/pass flags are
based on the recorded Surefire summary rather than those arrays.

The expected fixture counts match: 374 OASIS 2.0 request files, 487
non-deprecated OASIS 3.0 request files, 17 RSA request files, and two
examples. Recursive enumeration under the 3.0 directory finds 559 request
files because it also contains 72 deprecated `*d` fixtures; those are outside
the 487-case task universe and are not rows in this draft. The manifest has
880 unique `(suite, case_id)` rows and all request, policy, and response paths
referenced in it exist.

## Skip-list reconciliation

Each Java conformance harness has 30 `skipTests.add(...)` entries:
IIA=2, IIIA=5, IIC=17, IID=1, and IIIG=5. This agrees with the run 46 gate
log and `46-existing-case-exclusions.json`. Only 21 entries per suite match
an actual request fixture in the scoped universe: IIA=2, IIIA=5, IIC=8,
IID=1, IIIG=5. Nine IIC entries in the skip list have no corresponding
request file: `IIC023`, `IIC054`, `IIC055`, `IIC088`, `IIC089`, `IIC092`,
`IIC093`, `IIC098`, and `IIC099`. This explains task 3.4's 21
fixture-backed inherited skips versus the gate's 30 raw exclusion-list
entries. Skip additions have no inline explanatory comments. In the CSV,
`IID029` is assigned `special-harness` per task 3.4, leaving 20
`skipped-exclusion` rows in each suite.

The two IID requests are genuinely multi-root cases. Both suites have
`IID029Policy1.xml`, `IID029Policy2.xml`, `IID030Policy1.xml`, and
`IID030Policy2.xml`, plus their request/response fixtures and special notes.
The current test harness imports a single root policy for an executed case;
it does not load this two-policy arrangement. `IID029` is also in the legacy
skip list. `IID030` is the last case named by a count of 30 but is omitted by
the exclusive loop condition.

## Hard-coded loops and off-by-one

Both harnesses have the same group counts and loop implementation. Group
invocations are at `Xacml20ConformanceTest.java:125,137,143,167,175,181,187,199`
and the matching lines in `Xacml30ConformanceTest.java`. The common loop is
`for (int i = 1; i < testCount; i++)` at line 210 in both files. Its maximum
case number is `testCount - 1`; a fixture equal to the passed count is
omitted. The policy preload loop at line 247 has the same exclusive upper
bound.

| Group | `testCount` passed | Current loop reaches | Case left at exclusive bound in available fixtures |
| --- | ---: | --- | --- |
| IIA | 22 | 001–021 | 3.0: IIA022; IIA023–024 are also beyond this bound |
| IIB | 54 | 001–053 | No IIB054/055 request fixture; 3.0 IIB300–301 are beyond this bound |
| IIC | 233 | 001–232 | No IIC233 request fixture; 3.0 IIC300+ and IIC340–359 are beyond it |
| IID | 30 | 001–029 | IID030; IID029 is explicitly skipped and needs the multi-root harness |
| IIE | 3 | 001–002 | IIE003 |
| IIIA | 28 | 001–027 | IIIA028; later 3.0 IIIA cases are beyond this bound |
| IIIF | 7 | 001–006 | IIIF007 |
| IIIG | 7 | 001–006 | 001–005 are skipped; 006 is invoked by both the loop and a separate test |

OASIS 2.0 also has `IIIC001`–`IIIC003` fixtures but no `IIIC` execution
method, so the six `not-reached-loop-bound` rows for that suite are IIE003,
IIIA028, IIIC001–003, and IIIF007. The 3.0 loop omits 119 fixture IDs in
total, including those kinds of upper-bound cases and the larger-numbered
cases. All `IIC340`–`IIC349` policy/request/response triples exist, but the
current IIC bound reaches only case numbers through 232. They are therefore
`not-reached-loop-bound`, not missing fixtures; they were not executed in
run 46.

## Higher-order and duration cases

The active, non-deprecated XACML 3.0 policies using the requested 3.0
variadic function identifiers are:

| Case | Policy | Function identifier(s) | Run 46 |
| --- | --- | --- | --- |
| IIC164 | `IIC164Policy.xml` | `3.0:function:any-of` | executed, passed |
| IIC165 | `IIC165Policy.xml` | `3.0:function:all-of` | executed, passed |
| IIC166 | `IIC166Policy.xml` | `3.0:function:any-of-any` | executed, passed |
| IIC170 | `IIC170Policy.xml` | `3.0:function:any-of`, nested `3.0:function:map` | executed, passed |

Adjacent policies IIC167–IIC169 use the XACML 1.0 identifiers
`all-of-any`, `any-of-all`, and `all-of-all`, respectively; they are also
within the current loop and passed run 46. Deprecated `IIC164d`, `IIC165d`,
`IIC166d`, and `IIC170d` use corresponding 1.0 identifiers and are outside
the manifest's non-deprecated 487-case universe.

## Policy import failures that do not fail setup

In both OASIS harnesses, `addPolicy` logs a null imported policy at lines
258–260 and catches/logs any exception at lines 262–265, then returns
normally. Thus a missing or unimportable policy does not fail repository
setup at the point of import. The preload at `Xacml20ConformanceTest.java:104`
also requests `IIE002PolicyId2.xml`, which is absent from the fixture
directory; this auxiliary import is caught by that helper. It is not a
missing request/policy/response triple for the IIE002 manifest row, which
executes and passes.

The earlier green-gate investigation recorded policy import failures while
IIC150 and IIC164 were under test (run 36/42 summaries identify the previous
function-registration defects; run 37/43 directly reproduced the import
diagnostics). Those identifiers were repaired before run 46, whose OASIS
case manifests are green. This is historical evidence, not a current run 46
failure.

## Evidence inputs and limits

The fixture counts come from request filenames; suite membership and pass
state come from `46-conformance-testcases.json`,
`46-existing-case-exclusions.json`, and the run 46 `summary.md`. RSA and
example pass state comes from that summary and Surefire totals. `command.txt`
records the scoped searches, structural loop query, and Python generator.
The CSV is a task 3.1 draft for the inherited harness; it does not claim that
the unexecuted cases pass or provide reasons to retain any inherited skip.
