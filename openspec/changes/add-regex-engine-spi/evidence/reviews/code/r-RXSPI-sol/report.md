
FINAL REPORT (Codex Sol, GPT-6)

**Verdict: REJECT.** Six P2 findings and one P3. This is a source review; the counterexamples below were not executed.

Paths abbreviated below are under `xacml-core/src/main/java/org/xacml4j/v30/policy/function/`.

1. **P2 — Recursive Unicode-cache initialization can reject a valid pattern.**  
   `regex/UnicodeSets.java:163` calls `category()` inside `COMPOSED.computeIfAbsent`; `category()` updates that same map at line 115. With a cold cache, resolving `Lu,Ll,Lt,Lm,Lo,Mn,Mc,Me,Nd,Nl,No` before the first `\\w` makes the nested insertion trigger resizing while the outer computation holds a reservation. JDK 25 throws `IllegalStateException("Recursive update")`. This violates the [ConcurrentHashMap computation contract](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/concurrent/ConcurrentHashMap.html#computeIfAbsent(K,java.util.function.Function)).  
   **Fix:** Put the composed word set in a lazy holder, or resolve its dependencies before entering the map callback.  
   **Test:** In a fresh class loader, compile a class containing those category escapes followed by `\\w`, then assert that it matches `A`.

2. **P2 — Empty groups bypass the structural estimate and its cap.**  
   `regex/XacmlPattern.java:278–290` assigns zero cost to an empty sequence and no overhead to a capturing group. Thus `(((){1000}){1000}){1000}` has estimate **zero**, count 1000, and a short emitted pattern; both XF caps pass. JDK 25’s `Pattern.GroupCurly.match` performs the minimum repetitions even without consuming input. The deadline is checked only after that work (`regex/jdk/JdkRegexEngine.java:144–150`). The same estimate also omits capture instructions relevant to the forthcoming expanding engine.  
   **Fix:** Conservatively charge group/repetition overhead, including empty bodies, before multiplication. Keep overflow saturation.  
   **Test:** Assert that this pattern’s compilation fails with `LIMIT`; do not run its expensive match.

3. **P2 — Java back-references silently change XF results.**  
   `regex/jdk/XfSyntax.java:91` emits an ordinary Java named back-reference. For `^(a)?\\1$` on the empty string, XF requires **true**: an unmatched capture supplies an empty string. Java’s `Pattern.BackRef.match` instead fails when the capture did not participate. Sources: [XF 1.0 §7.6.1, 2007](https://www.w3.org/TR/2007/REC-xpath-functions-20070123/#regex-syntax), [OpenJDK 25 Pattern source](https://github.com/openjdk/jdk/blob/jdk-25%2B36/src/java.base/share/classes/java/util/regex/Pattern.java).  
   **Fix:** Implement the XF behavior; until supported, explicitly reject potentially unmatched references as `UNSUPPORTED`, rather than returning a wrong boolean. Making every back-reference optional would also be wrong.  
   **Test:** Cover empty input, `a`, and `aa` for this pattern.

4. **P2 — Legacy input-limit precedence changed.**  
   `impl/RegExpFunctions.java:352` compiles before checking input length; the check now resides in `regex/jdk/JdkRegexEngine.java:138`. Previously both lengths were checked first (`PATCH-RXSPI.diff:397`). With `maxInputLength=1`, pattern `(` and input `xx` previously produced the fixed **limit processing-error**; now they produce **syntax-error**. D5 does not require removing this existing early rejection.  
   **Fix:** Restore the length checks before compilation in `Matching.matches`; retain the engine check for direct SPI callers.  
   **Test:** Assert the limit status and message when oversized input accompanies a malformed pattern.

5. **P2 — JDK compilation stack exhaustion is misclassified as malformed or unsupported.**  
   `regex/jdk/JdkRegexEngine.java:99` maps every `PatternSyntaxException` to `MALFORMED`; line 118 maps it to `UNSUPPORTED` for XF. JDK 25 converts compilation `StackOverflowError` into a `PatternSyntaxException` with the diagnostic `Stack overflow during pattern compilation`. Consequently a valid deeply nested legacy pattern receives syntax-error instead of D5’s limit status. It would also become eligible for incorrect permanent failure caching in D6. This conversion is confirmed in the [OpenJDK source](https://github.com/openjdk/jdk/blob/jdk-25%2B36/src/java.base/share/classes/java/util/regex/Pattern.java).  
   **Fix:** Recognize that resource diagnostic and report a **transient limit**, distinguishable from deterministic structural limits for D6.  
   **Test:** Pin the diagnostic’s failure kind, status and message without relying on the test JVM’s stack size.

6. **P2 — The advertised strict profile contains unapproved Unicode deviations.**  
   `regex/UnicodeSets.java:231` uses Fifth Edition name ranges. For example, `^\\i$` and `^\\c$` accept U+200C, which the XML Second Edition productions incorporated by XF 2007 do not. XACML’s separate XML reference does not amend XF’s incorporated function definition. Also, line 134 accepts case-folded block names such as `\\p{Isbasiclatin}` through `UnicodeBlock.forName`, whereas Appendix F identifies `BasicLatin`. Sources: [XF §7.6.1](https://www.w3.org/TR/2007/REC-xpath-functions-20070123/#regex-syntax), [XSD Appendix F](https://www.w3.org/TR/2004/REC-xmlschema-2-20041028/#regexs), [XML Second Edition name productions](https://www.w3.org/TR/2000/REC-xml-20001006/#sec-common-syn).  
   **Fix:** Use the incorporated name productions and validate canonical block spelling. Fifth Edition names or case-insensitive aliases require an expressly approved deviation.  
   **Tests:** Pin U+200C and correctly/mis-capitalized block names.

7. **P3 — Some tests assert duplicated fixture data instead of additional behavior.**  
   `xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/RegexStatusMatrixTest.java:127` compares characterization metadata with more constants; it never invokes production code. Most rows at `.../regex/RegexProfileDifferenceTest.java:43` duplicate contract rows.  
   **Fix:** Keep change-history metadata as documentation and derive the difference matrix from one shared case table. The characterization, actual status-mapping tests, compiled-pattern reuse test, and public parser tests otherwise pin useful outcomes.

Other requested checks:

- Excluding `Cs` from `C`, the hyphen restrictions, and dot excluding LF only agree with the cited editions. Rejecting back-references inside classes is defensible because their class semantics are unclear, but the **2007 grammar admits them**. `XacmlPattern.java:601` should report unsupported use or document an approved deviation, rather than claim malformed grammar.
- Catching engine `Error`, including `VirtualMachineError`, follows the maintainer-approved D5 matrix. Finding 5 concerns a resource error hidden by the JDK’s wrapping.
- `CompiledRegex` has the right sharing shape: final compiled state and a fresh matcher/deadline wrapper per call (`JdkRegexEngine.java:126–144`). `RegexEngines.engine()` explicitly creates an engine; repeated discovery is acceptable at provider construction. D3 should retain that instance.
- The malformed corpus expectation is correct (`RulesCorpus.java:209`). Public `INVALID_PATTERN_MESSAGE` is a reasonable additive API for D5’s fixed message; the characterization independently pins its literal text.
- D3 wiring and D6 reuse remain outside this packet.

**Verification:** Scoped `git diff --check` passed. `ast-grep scan` passed for 25 files and three Java rules. Scoped Ripwire quality comparison was unavailable because that directory has neither a baseline nor a local Git HEAD. No files changed; no builds, tests, or JVMs ran. Fable’s green reactor is supplied evidence, recorded at `REPORT-RX-SPI-fable.md:234`, not fresh verification.
