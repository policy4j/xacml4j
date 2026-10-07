# Conformance blocker status: configured gate GREEN; additional IIIE3xx proposals OPEN

Final run 71: BUILD SUCCESS on JDK 11; 934 tests / 0 failures / 0 errors /
9 existing skips. Configured OASIS 2.0 347 pass, OASIS 3.0 347 pass, RSA 17
pass, examples 2 pass. Case evidence: 71-conformance-testcases.json. The
30 explicit exclusions per OASIS suite and all loop bounds are unchanged;
71-existing-case-exclusions.json equals runs 36/46/53/63. Fixtures are unchanged.

## Round 2 verification follow-up

Integral lexical scale and valid sub-nanosecond arithmetic: RESOLVED. Run 69
117/1/18/0 red with production unchanged; run 70 117/0/0/0 green; run 71 full
934/0/0/9 green. Scale-zero integral seconds restore the prior lexical form;
RoundingMode.DOWN truncates nanos toward zero under the conductor's decision.
Tests and decision: review-fixes.md, Round 2 verification follow-up. No new
configured failed case IDs; emitted case order/exclusions are unchanged.
Additional IIIE301-303 proposals below were not rerun in this follow-up.

## Round 3 review fixes (retained)

HIGH variadic higher-order binding (policy4j-the.4.8): RESOLVED; run 59 red,
run 61 mixed-array packing red, runs 62/63 green. MEDIUM identifier lookup-only
coverage: RESOLVED; 36 duration and 11 higher-order evaluation rows, each
comparing the canonical and retained alias with checks off/on. Full mapping:
round3-review-fixes.md. No new configured conformance failures were observed.
The record-only time-in-range URN and duplicate-id merge issues remain unchanged.
Additional IIIE301-303 diagnostics below are retained round 2 observations;
they were not rerun or added to the configured round 3 gate.

## Review fixes (round 2, retained)

Both MEDIUM findings: RESOLVED with tests first (runs 48/49 red, 51/52 green,
53 full gate green). The conditional P14M double-count defect is also fixed
after its red test showed 26 instead of 14 months. All six standard arithmetic
operations, signed fractions, round trips/equality/ordering, existing multiple
XPath expansion and the no-ID/explicit-ID selector cases pass. Mapping:
review-fixes.md; red/green test receipts: 49-review-red-tests.json and
53-review-green-tests.json.

## Additional targeted coverage, pending proposals

Runs 50 and 54: IIIE301–303 executed=3, failed=3, passed=0 in each. These
cases are outside the configured loops; the direct diagnostic invokes the
unchanged production comparator and exits nonzero on failures.

| Case | Cause / classification | Status |
| --- | --- | --- |
| IIIE301 | Draft fixture selector URN omits profile; the canonical handler ID matches OASIS CS02. Fixture compatibility, unchanged from a8a90d46. | OPEN proposal: decide draft attribute alias support; no fixture changes. |
| IIIE302 | Pre-existing harness compares Attributes categories by position; decisions match but category order differs. | OPEN proposal: preserve every assertion while matching categories by category/id and multiplicity. Later assertions UNVERIFIED. |
| IIIE303 | Pre-existing XML request IDREF cast assumes String, but JAXB supplies AttributesType. | OPEN proposal: unwrap IDREF target's id with a failing request-unmarshaller test first. |

All three observed failures are identical before/after; no adaptation
regression is shown. 55-review-baseline-snippets.json confirms the relevant
base source. review-proposals.md gives each file:line, exact observed failure,
proposed repair and test. These are outside current semantic/Content scope
and were not applied.

## Retained gaps and record-only notes

Zero-node context selection falls back to the document and two-node selection
uses the first node, instead of Indeterminate syntax-error. The requested
characterization tests pass before/after; XmlContent stays unchanged. Integer
getSeconds/isZero and Java-to-XML duration construction still truncate;
55-fractional-duration-gaps.txt records them. AssociatedAdvice comparison is
missing (pre-existing), and comment/PI-first mixed Content is UNVERIFIED.
No fixtures, expected results, other POMs, tracking or commits were changed.

## Earlier configured-suite blockers

| Case / earlier blocker | Cause and classification | Approval / repair | Final status |
| --- | --- | --- | --- |
| IIA001/IIB001/IIC001/IID001/IIE001/IIIA006, run 22 | Pre-existing harness/test-support absent optional collection comparison | Owned test utility repair; original assertions preserved | RESOLVED |
| IIC104, both suites | Pre-existing core day-time subtraction sign error | Conductor-approved sign restoration; focused red/green tests | RESOLVED |
| IIIF001/IIIG006, XACML 3.0 | Pre-existing XML mapper chose leading whitespace rather than DOM content | Conductor-approved content selection; focused red/green tests | RESOLVED; both IIIG006 invocations pass |
| IIC106/IIC107, both suites | Pre-existing date/year-month conversion, plus characterized combined-period calendar clipping | Conductor-approved date patch and arithmetic-class scope; runs 34 red / 35 green | RESOLVED |
| IIC150, run 36 | Pre-existing core missing 3.0 duration bag identifiers; import stops before evaluation | required-duration-bag-alias-fix.patch APPROVED/APPLIED; run 39 red / 41 green | RESOLVED |
| IIC164, run 42 | Pre-existing core missing 3.0 any-of and sibling higher-order identifiers; import stops before evaluation | Conductor-approved identifier-class scope; run 44 red / 45 green | RESOLVED |

All earlier configured blockers remain resolved in run 71. Prior approval records and
red/green receipts are retained. Duration provider bodies/aliases, accepted XML
repairs and the exact conductor-approved JUnit compile-scope patch are unchanged.
