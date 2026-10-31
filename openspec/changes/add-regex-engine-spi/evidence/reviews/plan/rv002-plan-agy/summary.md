# rv002: plan review of add-regex-engine-spi (Agy, Gemini 4 Argon)

Date: 2026-10-27. Read-only NTM lane, other model family than the author. Packet `packet.md`; report `report.md`.

Verdict: REJECT.

## Disposition (conductor, 2026-10-27)

| Finding | Disposition |
| --- | --- |
| Today's semantics and strict XF cannot both hold with cross-engine equality (rv001 P1, rv002 P1-2) | two named profiles: `legacy` (today, `jdk` only, default) and `xf` (strict XF 1.0, both engines); equality only inside `xf`; difference matrix `RegexProfileDifferenceTest` (design D2, spec "The xf profile is strict XF 1.0") |
| `programSize()` acts after compilation; range expansion unbounded (rv001 P1, rv002 P1-3) | overflow-safe size estimate on the `XacmlPattern` tree and an emitted-length bound before native compilation; `programSize()` stays as a second check; test with a counting compiler (design D4, spec "Compilation is bounded before it allocates") |
| Transient compile failures kept forever (rv001 P1) | only deterministic outcomes are kept; deadline and resource failures are retried (design D6, spec scenario "Transient failure is retried") |
| Selection via the repository is not wired to unmarshalling (rv002 P1-1) | the wiring is part of the design (D3) and its own task group 4: the repository's `FunctionProvider` reaches `PolicyImportTool`, the conformance runner, and `PdpFixture`; spec "Selection reaches policy loading" |
| `^`/`$` in classes, nested brackets from class escapes (rv002 P1-3) | classes are computed as one code-point set and emitted flat; anchors map only outside classes; contract cases `[$]`, `[a^]`, `[\d_]`, `[\i-[:]]` (D4) |
| Grammar edition mixed (rv001 P2, rv002 P3-1) | `xf` pins XF 1.0 (2007): `.` excludes only LF, no `(?:...)`; later-edition behavior is Open Question 5 |
| Status change contradicts the characterization gate (rv001 P2, rv002 P1-2, P2-2) | the history test records today; the acceptance test asserts the D5 matrix and lists the changed cases; spec "Statuses and messages"; count above 1000 is a structural limit in the `xf` profile only; `legacy` keeps today's weight (corrected after re-check rv003) |
| Benchmarks cannot isolate the wrapper cost or first use (rv001 P2, rv002 P2-1) | wrapped and unwrapped `re2j` cases with the same compiled patterns; counting hooks; PDP results labelled warmed; first use only with `SingleShotTime` (Benchmark Plan, task 6.4) |
| Public entry points and system properties (rv002 P2-3) | kept, delegating to `jdk`/`legacy`; the properties stay the `jdk` defaults (design D7) |
| Missing scenarios (rv002 P2-2) | added: duplicate engine name, two repositories, matrix, transient retry |
| JDK 21 in the proposal; "task 6.x" (rv002 P3-2) | fixed |
| rv002 claim that today's cache is on by default | not adopted: `CACHE_REGXP` defaults to `false` and line 396 compiles without the cache unless it is set (RX5 is right) |
