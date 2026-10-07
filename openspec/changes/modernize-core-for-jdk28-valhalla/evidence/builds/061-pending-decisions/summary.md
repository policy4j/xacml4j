# Run 061: maintainer decisions blocking the group 4 gate (conductor)

Date: 2026-10-02. Source: decision memos run 032 (Grok) and run 033 (Agy),
run 037 Table C (Grok), run 036 (Agy, adjudicated), reviews rv037-rv065.
Each item blocks a bead or a disabled test; none is implemented until the
maintainer chooses. "Rec." is the swarm's recommendation.

| # | Decision | Options | Rec. | Blocks |
| --- | --- | --- | --- | --- |
| D1 | integer-divide result and rounding (C2) | (a) xs:integer, quotient toward zero, remainder sign of dividend (5/2=2, 7/-3=-2, -5 mod 3=-2); (b) floor division | (a) (memo 032; matches Java operators and current mod) | task 4.6, bead the.4.6, conformance IIC020 |
| D2 | double-equal(NaN, NaN) (C1) | (a) true, a recorded deviation (IIC350/IIC358 expect Permit); (b) false per IEEE 754 and retag those cases | (a) (memo 032); -0.0 == 0.0 true either way | bead the.4.29, task 4.6 |
| D3 | PIP resolver failure (fail open) | (a) keep: failure = missing attribute (empty bag or missing-attribute); (b) Indeterminate processing-error regardless of MustBePresent (fail closed); (c) configurable per resolver | (b) | bead the.4.34; two disabled PDP tests |
| D4 | integer overflow (C3) | (a) Indeterminate processing-error outside 64 bits; (b) arbitrary precision (BigInteger) | (a) (keeps Int64 a value-class candidate; run 045) | task 5.4 |
| D5 | round/floor on NaN, INF, halfway (C4) | (a) IEEE: NaN stays NaN, INF stays, ties to even; (b) keep Math.round (NaN->0, +INF->Long.MAX) | (a) | row 60 of the 4.3 safety net |
| D6 | double-to-integer of NaN/INF (C5) | (a) Indeterminate processing-error; (b) defined saturation or zero | (a) | row 12 of the 4.3 safety net |
| D7 | integer-mod by zero (C6) | (a) Indeterminate processing-error (current behavior); (b) leave unspecified | (a) | row 43 |
| D8 | ipAddress set functions registered at 1.0 (C7) | (a) keep as extensions; (b) re-version; (c) remove | (a) | extension test |
| D9 | x500Name-match with a multi-valued terminal RDN (C8) | (a) compare terminal RDN sequences with x500Name-equal; (b) keep the string suffix | (a) | bead the.4.44 |
| D10 | on-permit-apply-second (CS01 profile) | (a) implement the profile's combine() and register; (b) leave unregistered | (a) after combine() matches CS01 section 2 | task 4.6; registry test disabled |
| D11 | or/and/n-of with Indeterminate (bead 4.12) | (a) a later deciding value wins over an earlier error (A.3.5); (b) keep first-error | (a) (memo 032) | bead the.4.12 |
| D12 | request syntax errors as responses (bead 4.17, IIA005) | (a) keep PDP.decide(RequestContext); map unmarshal SyntaxException to an Indeterminate syntax-error response in the adapter/harness; (b) add a raw-request PDP entry point | (a) (memo 033) | bead the.4.17; IIA005 skipped |
| D13 | hierarchical resources (bead 4.18) and legacy 2.0 XPath (bead 4.20) | (a) document as unsupported; (b) implement | (a) | beads the.4.18, 4.20 |
| D14 | IIIG300 fixture vs Plus Errata 01 5.48 | (a) keep the engine (lists all fully applicable policies) and skip IIIG300 with the adjudicated reason; (b) match the fixture | (a) | bead the.3.9 |
| D15 | IIA023 (request timezone -14:30 beyond the XML Schema range) and IID029 (fixture defect) | keep skipped with adjudicated reasons | keep | bead the.3.9 |
| D16 | Bound on multiple-resources XPath expansion (bead 4.50, security) | (a) configurable limit with a safe default (e.g. 100 expanded requests, 100 paths per category), exceeding it -> Indeterminate; (b) no limit, documented | (a) | bead the.4.50 |
| D17 | Regex hardening (bead 4.51, security) | (a) bound input length and evaluation time, bounded LRU pattern cache; (b) linear-time regex engine; (c) document a trusted-policy assumption | (a) | bead the.4.51 |
| D18 | XPath failure status in AttributeReference (found in rv074, 2026-10-02) | (a) a non-syntax XPath evaluation error is Indeterminate processing-error whatever MustBePresent is; missing-attribute only for an empty selection with MustBePresent=true; (b) keep missing-attribute (true) and empty bag (false) | (a) (XACML 3.0 §5.30, §7.3.7) | bead the.11 |

Also needed later (not gate blockers): task 10.1, how the integration
branch lands (replace or merge into feature_xacml_20); commit approval.
