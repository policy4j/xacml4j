1. **HIGH — The deadline does not bound matcher execution.**  
   `RegExpFunctions.java:242–248, 290–291`

   Matching `(?:(?:){2147483647}){2147483647}` against `""` meets the default length limits. Java’s minimum-repetition loops can execute these empty groups without calling `charAt`. The clock is never checked, so the match can hold the decision thread far beyond `maxMatchMillis`.

   The 1,024-call interval also bounds character accesses between checks, not elapsed time.

   **Fix:** enforce the budget through execution that can actually be stopped, such as a terminable isolated process, or use an engine with verified resource bounds for the supported syntax. Timing out a `Future` or interrupting its thread does not reliably stop Java regex execution.

2. **MEDIUM — Compilation receives no time budget.**  
   `RegExpFunctions.java:286–291`

   On a cache miss, rewriting and `Pattern.compile` finish before the deadline is created. With a valid 1 ms limit, a cold, long literal pattern can consume that budget compiling and still receive a fresh matching budget. Matching it against `""` can then return `false` without checking the clock.

   The pattern-length cap bounds source length; it does not enforce compilation time.

   **Fix:** include rewriting, compilation and matching in the enforced evaluation budget. Starting the clock earlier and checking after compilation would detect an overrun, but would not stop compilation while it happens.

3. **LOW — The timing test accepts substantial violations.**  
   `RegExpLimitsTest.java:87–91`

   The configured limit is 50 ms, but the assertion permits 20 seconds. An implementation that throws the expected exception after 10 seconds passes.

   **Fix:** assert elapsed time against a documented scheduling tolerance. Add a test using the empty-input repetition above, with a subprocess timeout that can terminate the runaway execution. Fresh-JVM tests for system-property overrides are also missing.

The deadline subtraction correctly tolerates `nanoTime()` wraparound for the positive, saturated durations used here. Zero and negative limits are rejected, and the resetting counter does not overflow.

The wrapper preserves UTF-16 length and code units; no surrogate-pair, region or `find()` result regression is demonstrated. Although `toString()` exposes the underlying string and subsequences get fresh counters, no reachable bypass through those methods is shown for this boolean matching path.

The cache correctly enforces its per-thread entry bound and access-order LRU eviction. Aggregate retention still scales with live threads × cache capacity × pattern size; the change provides no process-wide memory bound. The limit-exceeded exception and status disclose neither pattern nor input.

The supplied reactor result does not establish the missing deadline cases.

VERDICT: REQUEST_CHANGES
codex exit=0
