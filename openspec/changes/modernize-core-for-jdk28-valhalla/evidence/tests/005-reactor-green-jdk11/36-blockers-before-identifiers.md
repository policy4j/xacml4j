# Task 1.3 remaining blocker after approved arithmetic repair

Current gate: `36-reactor-verify-date-arithmetic.txt`, **BUILD FAILURE**.
727 Surefire tests / 0 failures / 1 error / 9 existing skips. All modules
and their tests compile. Only OASIS 3.0 IIC150 fails, during policy import.

## IIC150: pre-existing core function-registration defect

The required identifier is
`urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-one-and-only`.
BagFunctions.java:491 registers its 1.0 identifier, with no 3.0 alias.
XacmlDefaultFunctions includes this class but live lookup for the 3.0 name
returns empty, so policy import throws PolicySyntaxException before evaluation.

`37-duration-bag-function-diagnostics.txt` reproduces the error by importing
the unchanged `xacml3.0-ct-v.0.4/IIC150Policy.xml` through the production
repository/import tool. It independently checks both duration bag families:
all eight 1.0 names are present, and all eight 3.0 names are absent.
BagFunctions, XacmlDefaultFunctions, the suite and IIC150Policy all match
a8a90d46 (`scope-check.txt`). The OASIS normative function table requires
the fixture's 3.0 identifier (see `arithmetic-reference.md`).

This is **outside the approved date/time arithmetic and XML content mapping
scope**. It is a pre-existing core registration defect, not an adaptation
regression, an arithmetic error, a harness mismatch or an invalid fixture.
No production fix was applied for it. Fixtures and expected results are intact.

The previous case marker is IIC149, which succeeds. The harness prints a
marker after evaluation, so the failed IIC150 import emits no marker of its
own. Run 36's JSON now records IIC150 and the import phase explicitly;
run 37 supplies the independent identification proof.

`required-duration-bag-alias-fix.patch` is a checked, **unapplied proposal**
for BagFunctions only: register the eight standard 3.0 duration bag IDs and
retain their current 1.0 IDs with @XacmlLegacyFunc. The converter already
supports that annotation (JavaMethodToFunctionSpecConverter.java:98–105).
Conductor authorization for this class and focused bag tests is required.
Proposed tests must first show missing 3.0 registration, then prove all
sixteen old/new identifiers resolve, cardinality checks remain and the
unchanged IIC150 response matches through production import/evaluation.
Rerun the full gate afterward. Proposal behavior remains UNVERIFIED.

## Current case totals

| Suite | Attempted | Evaluated | Failed invocations / IDs |
| --- | ---: | ---: | --- |
| OASIS 2.0 | 347 | 347 | 0 |
| OASIS 3.0 | 265 | 264 | 1: IIC150, import error |
| RSA 2008 | 17 | 17 | 0 |
| Examples | 2 | 2 | 0 |

OASIS/RSA: 629 attempted, 628 evaluated, 1 import error. Every evaluated
case passes. Both examples pass. Each OASIS suite retains 30 explicit
exclusions and inherited loop bounds; neither has changed. Their IIIG006
duplicate invocations remain. Later 3.0 IIC cases are unexecuted after IIC150.
These are distinct from the 9 Surefire skips elsewhere in the reactor.

## Resolved failures and focused evidence

| Case / check | Cause | Disposition |
| --- | --- | --- |
| IIC104, both OASIS suites | Pre-existing core day-time subtraction sign error | Resolved by the prior conductor-approved sign condition; run 36 passes. |
| IIC106, both suites | Pre-existing core date year/month conversion to day/time PT0S | Resolved by the approved Period patch; runs 35 and 36 pass. |
| IIC107, both suites | Sibling date subtraction check | Passes with the Period patch. |
| Combined P1Y1M dateTime boundaries | Pre-existing sequential year/month clipping of February 29 | Four focused checks red in run 34, green after signed combined Period replacement in run 35. Not a separate failed conformance case. |
| IIIF001, both IIIG006 invocations in OASIS 3.0 | Pre-existing XML whitespace/DOM content selection | Resolved by the earlier approved content filter; run 36 passes. |
| IIA001/IIB001/IIC001/IID001/IIE001/IIIA006 comparison errors from run 22 | Pre-existing harness/test-support absent/empty collection handling | Resolved by the owned helper repair; original assertions retained, run 36 passes. |

Run 34: new 50-case arithmetic matrix plus 12 prior type checks and three
provider tests; 65 tests, 20 failures, no errors/skips. Tests precede changes.
Run 35: same 65 tests, all pass. `arithmetic-operations.md` and
`36-arithmetic-operation-tests.json` list every operation, test and operands.
No new function, weakened assertion, fixture edit or expected-result change.

Earlier blocker diagnosis and proposal states are preserved in
`31-blockers-before-date-arithmetic.md`; those are historical, not current
unresolved approvals. The JUnit POM, core sign, XML content and date Period
patches have all been conductor-approved and applied. Only the new duration
bag alias proposal remains unapplied and outside scope.
