# rv098: review of the 5.6 generic bounds (Codex Astra)

Date: 2026-10-24 and 2026-10-25. Lane R4: Codex CLI 0.164.0, GPT-6-Astra
(max), persona `policy4j_code_review_codex_astra`, read-only, in NTM session
`policy4j--landing-review`. Authors of the reviewed code: the maintainer and
the conductor (task 5.6, bead `policy4j-the.5.6`). Also reviewed:
`RFC822Name.java` (a separate maintainer edit) and design D13.

Final verdict: APPROVE (round 3).

| Round | Verdict | Findings | Result |
| --- | --- | --- | --- |
| 1 | REQUEST_CHANGES | MEDIUM R4-1: `TypeToString.forType` used `Types.capabilities::get`; the receiver of a method reference is evaluated at once, so a null type loaded the capability registry and its providers. LOW R4-2: `RFC822Name.java:283` uses spaces in a tab-indented file; license-block whitespace changed. Missing: a contract test of the new signatures | R4-1 fixed (the lambda of the committed code); R4-2 left to the maintainer (their edit); the contract test is remaining work on the bead |
| 2 | REQUEST_CHANGES | R4-1 closed. MEDIUM R4-3: D13 said raw-type callers still compile, but `Function<Object, Value> f = XacmlTypes.STRING;` does not (invariant generic arguments). LOW R4-4: `<T extends Value<T>>` is met by self-valued types such as `RFC822Name` | D13 corrected: erasures unchanged, some source callers need `Function<Object, Value<?>>`; the bound rejects payload-wrapping values such as `BooleanVal` |
| 3 | APPROVE | both corrections true for the code; no new finding | - |

After the fix of R4-1 the gate ran again (run 087): 8,009 tests, 0
failures, 18 skipped; conformance 937 with 12 skipped.

Limits: read-only; no compiler run of the counterexample and no test of
binary callers (UNVERIFIED in the lane reports).
