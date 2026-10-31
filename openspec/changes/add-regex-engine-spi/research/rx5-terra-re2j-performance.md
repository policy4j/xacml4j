FINAL REPORT (RX5 research lane, Codex GPT-6)

**Answer:** Use RE2/J 1.8 as an explicit alternative, while retaining a bounded compiled-pattern cache for the default JDK engine. The cache can remove repeated compilation, but it does not remove backtracking or the existing deadline gap. Published short-input measurements often favor precompiled JDK patterns; they do not establish the result for this PDP on JDK 21 or 25.

External sources below were retrieved **2026-10-27 UTC**.

### Current implementation and releases

- The existing implementation translates and scans the pattern on every call. Its per-thread compiled-pattern cache is disabled by default and holds up to 256 entries per thread when enabled. Matching uses `find()` and checks a deadline through the input sequence. [RegExpFunctions.java:150](xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/RegExpFunctions.java:150), [RegExpFunctions.java:388](xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/RegExpFunctions.java:388)

- The deadline cannot interrupt all work that occurs without an input read. The existing test source records `(.*a){20}$` with 40 `a` characters and a failing suffix as taking more than three seconds on JDK 21.0.6 without its limit. That is a **recorded claim**, not a run by this lane. [RegExpLimitsTest.java:58](xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/RegExpLimitsTest.java:58), [RegExpFunctions.java:407](xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/RegExpFunctions.java:407)

- Maven Central metadata lists releases **1.0 through 1.8**, with 1.8 as latest and metadata last updated on 2025-01-09. GitHub also lists 1.8 as latest; 1.7 preceded it in June 2022. Version 1.8 includes named-group and case-folding changes. [Maven Central metadata](https://repo.maven.apache.org/maven2/com/google/re2j/re2j/maven-metadata.xml), [RE2/J releases](https://github.com/google/re2j/releases), [maintainer announcements](https://groups.google.com/g/re2j-discuss)

- The 1.8 source declares Java 6 source and target compatibility; its published POM lists only test-scoped dependencies. Its license is the Go-derived BSD three-clause license, listed as “Go License” in the POM. This is useful compatibility evidence, but **JDK 21 and JDK 25 runtime support is UNVERIFIED**: neither the published artifact nor its tests ran in this lane. [1.8 build configuration](https://raw.githubusercontent.com/google/re2j/re2j-1.8/build.gradle), [Maven Central POM](https://central.sonatype.com/artifact/com.google.re2j/re2j), [1.8 license](https://raw.githubusercontent.com/google/re2j/re2j-1.8/LICENSE)

### Unicode and known compatibility risks

RE2/J 1.8 looks up Unicode general categories and scripts such as `\p{L}` and `\p{Han}` in generated tables. Those tables identify themselves as **Unicode 6.0**. By comparison, JDK 21’s `Character` data is Unicode 15.0 and JDK 25’s is Unicode 16.0. Case folding also uses runtime character operations, so the table version alone does not describe every Unicode operation. Differential tests are needed for newly assigned characters, scripts, supplementary characters and case folding. [RE2/J 1.8 parser](https://raw.githubusercontent.com/google/re2j/re2j-1.8/java/com/google/re2j/Parser.java), [generated tables](https://raw.githubusercontent.com/google/re2j/re2j-1.8/java/com/google/re2j/UnicodeTables.java), [JDK 21 Character](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Character.html), [JDK 25 Character](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/lang/Character.html)

The parser’s named-class lookup is limited to its category and script maps plus `Any`; XML Schema block syntax such as `\p{IsBasicLatin}` therefore needs translation or a defined rejection. RE2/J also rejects counted bounds over 1,000. For example, `a{1001}` is below the current project’s default repetition-weight limit but is rejected by the 1.8 parser unless translated. These are **static source observations**; engine parity and status mapping are UNVERIFIED. [RE2/J parser](https://raw.githubusercontent.com/google/re2j/re2j-1.8/java/com/google/re2j/Parser.java), [project limits](xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/RegExpFunctions.java:150)

Two upstream reports matter to the safety claim:

- Issue 168 reports an infinite **compilation** loop for a case-insensitive Unicode pattern on JDK 11 and 17. The fix included in 1.8 says it reduces the incidence but does not eliminate the possibility. Whether the reported case affects 1.8 on JDK 21 or 25 is **UNVERIFIED**. [Issue 168](https://github.com/google/re2j/issues/168), [merged change 160](https://github.com/google/re2j/pull/160)
- Issue 198 reports asymmetric case-insensitive matching for `\p{Ll}`. Its applicability to the proposed XACML translation and release artifact is **UNVERIFIED**. [Issue 198](https://github.com/google/re2j/issues/198)

RE2/J describes matching as linear in input length for a fixed accepted pattern; that is not a guarantee of bounded compilation time or memory for arbitrary patterns. Retain admission and resource limits. [RE2/J README](https://github.com/google/re2j), [USENIX Security 2022 counting study](https://www.usenix.org/conference/usenixsecurity22/presentation/turonova)

### Measurements and literature

- An upstream 2022 JMH issue used precompiled patterns, `find()`, and 32-character numeric inputs. It reported **176 ns/op for JDK versus 1,068 ns/op for RE2/J**, about 6.1 times as much time for RE2/J. The report used one fork and two measurement iterations and does not establish the JVM or RE2/J version for this project. [RE2/J issue 162](https://github.com/google/re2j/issues/162)

- A competing engine’s 2026 benchmark on JDK 26.0.2 reports 30–65-character `find()` cases: a successful literal search at **81.7 ns JDK versus 215.6 ns RE2/J**, while a literal no-match was approximately **45 ns for each**. Its separate anchored adversarial example, `(a+)+b` against 20 `a` characters followed by `c`, reports **1,862.5 ns JDK versus 883.6 ns RE2/J**. The author reports machine drift and an invalid earlier baseline; these are narrow, author-supplied measurements, not JDK 21/25 or PDP results. [Benchmark methodology and tables](https://github.com/jemmix/tdfa-jvm/blob/main/BENCHMARKS.md)

- For adversarial Java matching, a 2014 arXiv paper models Java-style backtracking and analyzes families with exponential behavior. A 2017 arXiv extended paper reports 41 exploitable ReDoS vulnerabilities in Java web applications. The 2025 peer-reviewed ReDoS survey cautions that modern engines have partial defenses and that exploitability depends on the surrounding system. None is a measurement of this proposed SPI. [Berglund et al.](https://arxiv.org/abs/1405.5599), [Wüstholz et al.](https://arxiv.org/abs/1701.04045), [ReDoS survey](https://arxiv.org/abs/2406.11618)

The repository does contain short examples, such as `J.* Hibbert` against `Julius Hibbert`; that fixture does not establish the distribution of deployed policy patterns or request lengths. [IIC056 policy](xacml-conformance-tests/src/test/resources/xacml3.0-ct-v.0.4/IIC056Policy.xml:14), [request](xacml-conformance-tests/src/test/resources/xacml3.0-ct-v.0.4/IIC056Request.xml:5)

### Options

1. **Cached JDK engine.** Low dependency and translation risk; likely avoids repeated compilation for stable patterns, but retains backtracking and deadline gaps. A cache-hit/miss benchmark with the existing guards enabled would establish its actual benefit. Compiled `Pattern` objects can be shared between threads; `Matcher` objects cannot. [JDK 25 Pattern documentation](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/regex/Pattern.html)
2. **Explicit RE2/J engine.** Provides the non-backtracking matching strategy for accepted patterns, at the cost of a module, dependency, syntax translation and possible Unicode or status differences. Differential engine tests must establish decisions, statuses and `find()` behavior, including the known issues above.
3. **Automatic fallback to JDK for unsupported RE2/J patterns.** This improves acceptance but reintroduces the JDK engine’s worst-case behavior exactly where users may expect the RE2/J bound. Prefer explicit rejection with a defined status over silent fallback.

**Recommendation:** Implement the planned explicit engine choice, retain JDK as default, and cache only within a bounded policy or engine scope. Preserve dynamic first arguments and current guard behavior. Benchmark cold compilation, warm hits, misses, match and no-match cases, Unicode, varied input lengths and complete PDP decisions on both JDK 21 and 25. Run potentially hanging compilation cases in a separately terminable process.

### Confidence, drift and not checked

Confidence is **high** in the cited release metadata and version-tagged source observations; **moderate** in the general performance direction; and **low** in any predicted PDP speedup. The benchmark sources disagree across patterns and JVMs, and their results exclude this project’s complete translation, scanning and decision path.

No tests, builds, benchmarks or profilers ran. JDK 21/25 execution, RE2/J issue reproduction, all six function variants, conformance results, status mapping, cache allocation and deployed workload frequency remain **UNVERIFIED**.

The native GitHub MCP returned HTTP 401; public GitHub pages and version-tagged source were the successful fallback. The direct web fetch of Maven metadata was inaccessible; the DuckDuckGo MCP retrieved the Maven Central metadata successfully. Context7 resolved C++ RE2 rather than RE2/J, so the Java-specific claims use the tagged upstream Java source.
