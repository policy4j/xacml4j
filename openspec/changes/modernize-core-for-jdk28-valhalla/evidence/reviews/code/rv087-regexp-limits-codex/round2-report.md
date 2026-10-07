1. **HIGH — Escape handling can hide the repetitions that bypass the deadline.**  
   File: `xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/RegExpFunctions.java`, lines **424–430**.

   Two concrete cases:

   - Pattern `\c[(?:(?:){2147483647}){2147483647}`, input U+001B. Java consumes `\c[` as one control-character escape. The scanner consumes only `\c`, then treats `[` as an unclosed character class and skips the remaining pattern. It returns weight **1**. After matching the prefix, Java reaches the known empty-repetition stall without further input reads.
   - Pattern `(?:(?:){1000})\Q\E{2147483647}`, input `""`. Java removes the empty quoted section, so the outer quantifier applies to the preceding group. The scanner skips the quoting after it has already finished that group’s quantifier check, returning weight **1000**. The enormous nested repetition is accepted.

   **Fix:** Consume complete Java escapes, including `\cX`, and account for empty quoting before determining quantifier attachment. Use a validated parser or reject syntax the scanner cannot faithfully interpret. Add regressions for both cases.

2. **HIGH — The largest nested repetition product does not bound empty-input backtracking.**  
   File: `xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/RegExpFunctions.java`, lines **363–387**, **318**.

   Construct the pattern as:

   ```java
   "(?:(?=)|(?=))".repeat(40) + "(?!)"
   ```

   Match it against `""`. Its repetition weight is **1**. Both alternatives in every group succeed without consuming input; the final empty negative lookahead always fails. Backtracking explores approximately **2^40 combinations**, without calling `charAt`. The deadline therefore cannot interrupt the work, and the eventual result is returned without an elapsed-time check.

   **Fix:** Bound zero-width branching as well as counted repetitions, rejecting combinations whose work cannot be safely bounded, or use an engine with an execution budget. Check the deadline after `find()` too; that corrects late-result classification but cannot interrupt these stalls.

3. **MEDIUM — A leading literal `]` causes rejection of harmless character classes.**  
   File: `xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/RegExpFunctions.java`, lines **456–457**.

   Pattern `[]a{1000001}]`, input `"a"`, should match immediately. Java treats the first `]` as a literal class member; the braces and digits are also class members. The scanner closes the class at that first `]`, interprets `a{1000001}` as a repetition, and rejects it under the default limit.

   **Fix:** Track leading-literal `]` rules for ordinary and negated classes, including nested classes. Add a test asserting this match succeeds.

The deadline arithmetic is otherwise sound for normal `nanoTime` wraparound: subtraction avoids an absolute-clock comparison, conversion saturates rather than becoming negative, and positive validation excludes zero and negative limits. The counter resets at 1,024 and cannot overflow. However, 1,024 reads bound neither elapsed time between reads nor work after the last read.

Compilation remains uninterruptible. Its post-compilation check detects an overrun; the pattern-length cap does not establish a wall-clock ceiling. No particular slow-compilation measurement is established by this packet.

Character and length delegation preserve surrogate handling and `find()` semantics. No additional result change or `subSequence`/`toString` bypass is demonstrated here. Cache eviction and access order are correct; retained memory still scales with the number of live caching threads. The limit exception’s fixed message does not expose the pattern or input.

Missing tests include the failures above and a fresh-JVM test proving the **public entry point** honors overridden properties. The current default test checks constants.

This was a static review only. No files were edited; the 7,775-test reactor result is supplied evidence.

VERDICT: REQUEST_CHANGES
codex exit=0
