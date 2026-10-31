**REJECT** — one P2 finding: `D52-ASTRA-1`.

`xacml-core/src/main/java/org/xacml4j/v30/policy/function/FunctionSpecBuilder.java:328`–`329` accepts caller-owned mutable type metadata inside a standard parameter wrapper. If an operand changes that metadata after preparation, the matching-value path skips the signature check. With an empty bag, `all-of` returns true where the ordinary route produces syntax-error/Indeterminate, suppressing the outer failure observation. Restrict preparation to proven immutable type metadata, or retain the checked route for custom metadata. The exact trigger, correction and regression assertions are in `report.md`.

Reviewer: `cod_3`, actual model `gpt-6-astra`, effort `max`; bead `policy4j-cwtu.42.19.1`, actor `ntm-astra-g1`.

Candidate base `cd443805c3044e7412c25aae71dd02127994d418`; reviewer/current main `759d90a6c75510d55127d8e22870f9edd985c991`. All 8 frozen and 8 protected source hashes match the manifest at initial and closing inspection. No source drift. The candidate was preserved.

`Apply` keeps five references, one int and one boolean by replacing `bound` with `callSite`: field reuse supports 40 bytes under the supplied layout assumptions. The size measurement was not repeated.

Evidence: static source trace and hash/Git checks; no runtime counterexample executed. Builds, tests, conformance, fresh size measurement and controlled performance pair are **UNRUN**. Performance is not approved.

Changed files: only this directory's `report.md`, `summary.md` and `command.txt`. Beads and integration remain with the conductor.

