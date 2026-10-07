# Run 072: ruled function semantics (bead the.4.44; decisions C4, C5, C7)

Date: 2026-10-07. Rulings: run 061 D5, D6, D8, D9
(`builds/070-maintainer-rulings`). JDK: Oracle GraalVM 21.0.6+8.1
(`java version "21.0.6" 2025-01-21 LTS`). Reviews:
`reviews/code/rv079-rulings-codex`, `reviews/code/rv080-rulings-grok`.

## Changes

| Ruling | Function | Before | After |
| --- | --- | --- | --- |
| D9 (C8), bead the.4.44 | `x500Name-match` | string suffix of the canonical names | RDNs of the first name are a terminal sequence of the RDNs of the second; each RDN compared as `x500Name-equal` compares names. The RDNs are the canonical name (`X500Principal.CANONICAL`) split at unescaped commas |
| D5 (C4) | `round` | `Math.round`: NaN gives 0, an infinity saturates, ties go up | `Math.rint`: NaN stays NaN, an infinity stays, ties go to the even integer |
| D6 (C5) | `double-to-integer` | NaN and the infinities saturate through `(int)` | Indeterminate `processing-error` |
| D8 (C7) | `ipAddress-*` set functions (1.0 namespace) | registered | kept as extensions; a test pins the five ids |
| D7 (C6) | `integer-mod` by zero | Indeterminate | unchanged; row 43 was already active |

`floor` already kept NaN; a test now also pins the infinities.

Not done here: D4 (integer overflow, decision C3) stays with task 5.4. Its
two rows are the only skips left in `FunctionSafetyNet43Test`.

## Tests

- Red first: `red-surefire-summary.txt` (1 failure for x500Name-match, then
  5 failures for round and double-to-integer).
- Focused green: `FunctionSafetyNet43Test` 118 tests, 0 failures, 2 skipped.
  After the round-2 review fixes (canonical RDN split, more `x500Name-match`
  and `round` rows): 122 tests, 0 failures, 2 skipped.
- Gate: full reactor, 7,638 tests, 0 failures, 0 errors, 24 skipped;
  conformance 927 tests with 14 skipped, the same as run 069
  (`tests/071-resolver-failure-fail-closed/surefire-summary.txt`, gate 5).
- Three placeholder tests that only called `fail(...)` behind `@Disabled`
  (C8, C7, C4) are replaced by real tests. The C5 test is enabled.

## Spec

`specs/function-semantics/spec.md` gains three requirements: rounding and
conversion of special double values, x500Name-match compares RDN sequences,
and ipAddress set functions are extensions. `openspec validate --strict`
passes.

## JMH

NOT APPLICABLE for `round` and `double-to-integer` (one call replaced, one
branch changed). `x500Name-match` now parses both names per call; no
benchmark covers it, and it is not on the conformance hot path. Recorded
as unmeasured, not as accepted performance.

## Reviews

Same packets as run 071: `reviews/code/rv079-rulings-codex` and
`reviews/code/rv080-rulings-grok`. Round 2 found that rebuilding RDNs from
RFC 2253 text could change equality or fail to parse; the split of the
canonical form replaced it. Rounds 3 to 6 had no finding on the functions.
Final verdicts: Codex APPROVE (round 6), Grok APPROVE (rounds 4 and 5).

## Limits

The expected values come from the rulings, not from a new reading of XACML
3.0 Appendix A in this run.
