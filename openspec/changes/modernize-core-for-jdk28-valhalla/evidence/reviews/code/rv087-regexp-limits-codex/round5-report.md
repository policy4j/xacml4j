Both round-4 findings are closed. The sentinel weight is rejected independently of `maxRepetition`, and the property tests now exercise the configured time limit at its boundary and one nanosecond beyond.

1. **MEDIUM — Maximum cache size disables eviction.**  
   File: `xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/RegExpFunctions.java:251` (accepted configuration at line 166).

   With `.cacheRegExps=true` and `.cacheSize=2147483647`, `size() > maxSize` can never be true: `size()` returns an `int`. Given sufficient heap, more than 2,147,483,647 distinct patterns inserted on one thread remain cached, including after the backing map’s size counter overflows.

   **Fix:** Reject `Integer.MAX_VALUE` in both property validation and the cache constructor, or evict before insertion when the current size equals the configured bound. Add a boundary validation test.

2. **LOW — Quoting normalization overflows its initial capacity.**  
   File: `xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/RegExpFunctions.java:433`.

   Set `.maxPatternLength=2147483647` and supply:

   ```java
   "\\Q" + "a".repeat(1_073_741_820) + "\\E"
   ```

   Its length is 1,073,741,824, so it passes the length check. Given sufficient heap for the supplied string and rewriting, `2 * length` becomes `Integer.MIN_VALUE`. `StringBuilder` consequently throws `NegativeArraySizeException` before compilation or the next deadline check.

   **Fix:** Use an overflow-safe initial capacity. If normalization would exceed a representable size, reject it through the fixed limit error. Test the capacity calculation without allocating the enormous string.

No comparable arithmetic disablement appears in the input-length or time checks. String lengths cannot exceed `Integer.MAX_VALUE`, and the configured millisecond ceiling converts safely to nanoseconds. The repetition sentinel fix remains effective.

This is a static review of the supplied diff; reactor results are supplied evidence. The accepted `policy4j-the.4.58` exposure remains open.

VERDICT: REQUEST_CHANGES
codex exit=0
