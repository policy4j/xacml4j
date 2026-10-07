# rv035: group 3 review, conformance manifest and harness (Grok, adversarial)

Date: 2026-10-01. Lane: adversarial code reviewer, Grok Build grok-4.7
(xhigh), role `.ntm/roles/policy4j_code_review.md`, session cleared first.
Session `policy4j--g1`, pane 0. Read-only (sources, before-source runners,
manifest via csv parse, fixtures and engine lines; ast-grep scan exit 0).
Maven not run.

Scope: same as rv034.

Verdict: APPROVE (two LOW findings).

Findings:
1. LOW, manifest rows oasis20/IIC012 and oasis30/IIC012: tagged
   fixture/spec, but IIC012Policy.xml:12-20 is a schema-valid integer
   Condition and IIC012Response.xml:8-11 expects Indeterminate
   processing-error (no Special.txt). Condition.java:59 rejects the expression
   at construction, so the PDP never returns the oracle. The skip is right;
   the label should be engine defect. Resolution: new engine bug; relabel the
   rows in the next manifest edit.
2. LOW, IIA004: IIA004Special.txt:12-18 lets a PDP that never evaluates the
   policy demonstrate rejection; the rows are skipped, so an importer that
   later accepts the invalid policy stays green. Fix: special-harness that
   asserts the import rejection (like IIE003). Resolution: added to bug
   policy4j-the.3.9 as the recommended disposition.

Checks that held (reviewer): 880 rows match the fixtures (374/487/17/2);
all 692 previously executed OASIS ids (346 per suite) still enabled and
executed; RSA 17 and examples 2 executed; IIIG006 once per suite; 44 disabled
rows each with reason and policy4j-the.* id; boundary ids the old exclusive
loop skipped now present (IIE003, IID030, IIIA028, IIIF007); IIE003 method 1
faithful (`ConformanceCaseRunner.java:102-114`, "is not compatible" only from
Match.java:63-67); IID029/IID030 emulation compares the vendored response; 3.0
IID029 is a real fixture bug; import failures become AssertionError with
suite/case/resource/cause; the 3.0 media type is used (the old 3.0 runner
imported with XACML20_XML); the manifest guard rejects malformed rows,
duplicates, path escapes, missing/extra requests and missing files and cannot
pass with an empty manifest; each case is an independent parameterized test.
Sampled classifications IIA004, IIA023, IID029, IIC300, IID300, IIIG300,
IIC020, IIIE301, IIIC002, IIC302/IIC330 are right.

Residual (reviewer): Xacml20TestUtility compares obligations to themselves
(`Xacml20TestUtility.java:87`, pre-existing), so 2.0 passes compare decision,
resource id and status only (new bug); RSA/example obligation assignments are
matched by id only (XacmlPolicyTestSupport); the IID029/030 PolicySet emulates
repository target selection; conformance-blockers.md still showed BUG-TBD
while the id replacement was in progress.

## Correction (conductor, 2026-10-02)

Finding 1 was wrong: IIC012Special.txt exists in both suites and allows a PDP that never evaluates statically ill-typed policies to demonstrate rejection instead of returning the runtime Indeterminate. The reviewer's own decision memo (builds/032-semantic-decisions-grok, question 3) reached this. Bug policy4j-the.4.23 was closed as not a defect; IIC012 joins IIA004 as a static-rejection special-harness case (policy4j-the.3.9).
