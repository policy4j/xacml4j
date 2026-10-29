FINAL REPORT (RX3 research lane, Codex GPT-6)

**Answer:** Nine cases in each OASIS suite use regexp matching. Two additional deprecated 3.0 fixtures do so. All call `string-regexp-match`; no reference to any of the six regexp functions was found in the RSA 2008 fixtures. None of the inventoried patterns requires unsupported RE2 syntax. **Actual RE2/J conformance results are UNVERIFIED: no engine or test was executed.**

The conformance cases provide little compatibility coverage: their patterns are ASCII literals, alternation, and repetition. They do not cover the other five functions, Unicode classes, subtraction, invalid patterns, caching, or limits.

All external sources cited below were retrieved **2026-10-27**. RE2/J findings use the **`re2j-1.8` tag**, rather than assuming that current C++ RE2 documentation describes every Java detail.

For repository citations:

- `F20` = `xacml-conformance-tests/src/test/resources/oasis-xacml20-compat-test`
- `F30` = `xacml-conformance-tests/src/test/resources/xacml3.0-ct-v.0.4`
- `RF` = `xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/RegExpFunctions.java`
- `RT` = `xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/RegularExpressionFunctionsTest.java`
- `LT` = `xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/RegExpLimitsTest.java`

**Conformance inventory**

`␠` below denotes exactly one U+0020 space. Leading, trailing, and repeated spaces are part of the patterns.

| Case, present in both suites | Pattern arguments | Source locations |
| --- | --- | --- |
| IIB008 | `read\|write`¹ | `F20/IIB008Policy.xml:49`; `F30/IIB008Policy.xml:32` |
| IIB009 | `read\|write`¹ | `F20/IIB009Policy.xml:49`; `F30/IIB009Policy.xml:32` |
| IIC056 | `J.*␠Hibbert`; `B.*␠Simpson` | `F20/IIC056Policy.xml:24,33`; `F30/IIC056Policy.xml:15,21` |
| IIC057 | `J.*␠K.*␠Hibbert`; `B.*␠O.*␠Simpson` | `F20/IIC057Policy.xml:24,33`; `F30/IIC057Policy.xml:15,21` |
| IIC165 | `␠*This␠␠is.*␠IT!␠␠` | `F20/IIC165Policy.xml:20`; `F30/IIC165Policy.xml:11` |
| IIC166 | `␠*This␠.*is␠not␠IT!␠*`; `␠␠␠␠␠This␠.*is␠IT!␠␠` | `F20/IIC166Policy.xml:21,23`; `F30/IIC166Policy.xml:12,13` |
| IIC167 | `␠␠␠This␠␠is␠n*o*t*␠*IT!␠␠`; `␠␠␠This␠␠is␠IT!␠␠` | `F20/IIC167Policy.xml:21,23`; `F30/IIC167Policy.xml:12,13` |
| IIC168 | `␠␠␠This␠␠is␠n*o*t*␠*IT!␠␠`; `␠␠␠This␠is␠not␠a␠match␠to␠IT!␠␠` | `F20/IIC168Policy.xml:21,23`; `F30/IIC168Policy.xml:12,13` |
| IIC169 | `␠␠␠This␠␠is␠n*o*t*␠*IT!␠␠`; `␠␠␠This␠␠is␠.*IT!␠␠` | `F20/IIC169Policy.xml:21,23`; `F30/IIC169Policy.xml:12,13` |

¹ The actual pattern is **`read|write`**, with no backslash; the table escape protects its column separator.

IIB008–009 use target matches; IIC056–057 use direct Apply expressions; IIC165–169 pass the predicate to higher-order functions. All eighteen cases are enabled in `xacml-conformance-tests/src/test/manifest/conformance-manifest.csv:30,128,231,407,507,610`.

The deprecated fixtures are:

- `F30/xacml3.0-deprecated/IIC165dPolicy.xml:17`: the IIC165 pattern.
- `F30/xacml3.0-deprecated/IIC166dPolicy.xml:18,19`: the two IIC166 patterns.

The harness excludes that deprecated directory (`ConformanceManifest.java:176`).

Inventory verification used both `rtk rg` and a read-only XML parser. The parser inspected all **2,869 XML files**, including all **43 RSA files**, with zero parse errors, and inspected `FunctionId`/`MatchId` references containing regex terminology. This supports the RSA inventory result beyond an empty text search.

**RE2 compatibility**

The literal, alternation, concatenation, and star constructs above are supported by RE2. With substring matching and unchanged whitespace, no changed result is predicted for the supplied requests; their inspected attribute values contained no non-ASCII characters or CR/LF. This is a static inference, not a passing-engine result. [RE2 syntax](https://github.com/google/re2/wiki/Syntax), [RE2/J 1.8 parser](https://github.com/google/re2j/blob/re2j-1.8/java/com/google/re2j/Parser.java).

There is nevertheless a semantic difference in the **dot-star patterns**: IIC056, IIC057, IIC165, IIC166, IIC169, and their deprecated counterparts can behave differently on other inputs. Java’s default dot excludes CR, LF, U+0085, U+2028, and U+2029; RE2’s default dot excludes LF. For example, a CR between `J` and ` Hibbert` can distinguish engines. None of the current requests exposes that difference. [Java SE 25 Pattern](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/regex/Pattern.html), [RE2/J 1.8 parser](https://github.com/google/re2j/blob/re2j-1.8/java/com/google/re2j/Parser.java).

Outside this fixture inventory, RE2/J rejects counted bounds above **1,000** and does not support backreferences or lookaround. The current repetition limit is a different rule: a product of nested **minimum** counts, defaulting to 1,000,000 (`RF:392`; `LT:409`). These require additional engine-contract cases. [RE2/J 1.8 parser](https://github.com/google/re2j/blob/re2j-1.8/java/com/google/re2j/Parser.java), [RE2 syntax](https://github.com/google/re2/wiki/Syntax).

**Behavior that characterization must preserve**

1. **Substring matching, whitespace, and translation.**  
   Matching uses `Matcher.find()`, not whole-input `matches()` (`RF:403`). Empty patterns remain empty; patterns are neither trimmed nor padded with `.*` (`RF:722`). Pin empty input, empty pattern against newline, prefix/suffix substrings, branch-local anchors, and significant spaces.

   Translation currently performs these operations (`RF:722`):

   - `\p{Is…}` → `\p{In…}`, and the corresponding negative escape.
   - Nested class subtraction `-[` → `&&[^`.
   - Unescaped `$` outside character classes → strict-end `\z`.

   Preserve its actual escape/class/quoted-text handling during the move; this is not evidence of a complete XML Schema regex translator. Existing tests cover subtraction, empty translation, anchors, escaped dollars, and spaces (`RT:69,79,89,100,126`). Add Unicode-block and quoted/escaped translation cases.

2. **Cache activation and lifetime.**  
   The property is exactly  
   `org.xacml4j.v30.policy.function.impl.RegExpFunctions.cacheRegExps`.

   It is parsed once during class initialization with `Boolean.parseBoolean`; absent means false (`RF:178`). Limits and cache capacity are also initialized once (`RF:171`). Changing properties afterward does not reconfigure that loaded class.

   When enabled, the cache is **per thread**, access-order LRU, default capacity **256**, keyed by the **translated Java pattern** (`RF:175,262`). Translation and repetition checks still occur on every call; only compiled Pattern reuse changes (`RF:389–396`). Compile failures are not inserted.

   `LT:492` tests a standalone PatternCache’s eviction, not property activation or thread isolation. Add isolated-classloader/fork characterization for activation and initialization, plus same-thread reuse, cross-thread separation, eviction, and repeated compile failure.

3. **Length and configuration limits.**  
   Defaults are pattern length **10,000**, input length **100,000**, deadline **1,000 ms**, repetition weight **1,000,000**, cache size **256** (`RF:150`). Length checks use Java String length and permit equality (`RF:378`; `LT:77`).

   Numeric properties are trimmed positive integers. Maximum configurable values differ: pattern length `Integer.MAX_VALUE / 8`, cache size `Integer.MAX_VALUE - 1`, input length and milliseconds `Integer.MAX_VALUE`, repetition `Long.MAX_VALUE` (`RF:163,234`; `LT:295,357,381`). Preserve rejection of malformed, zero, negative, and excessive values.

4. **Deadline scope and its accepted gap.**  
   Timing starts before translation/compilation. It is checked after compilation, every **1,024 input-character reads**, and after matching; an exact-deadline result is accepted (`RF:331,388,397,409`; `LT:219`).

   Compilation cannot be interrupted. Work that does not read input is not interrupted either; its late result is discarded afterward. This limitation is explicit in `RF:43` and `RF:416`. Conversion of typed input to text precedes the timed helper (`RF:129`). Preserve these boundaries unless the design explicitly changes them.

5. **Repetition scanning.**  
   Preserve nested-minimum products, alternatives, saturation, quoted text, escapes, class contents, and adjacent quantifiers. A `Long.MAX_VALUE` result is rejected even when configured as the limit; comments mode is conservatively rejected through this limit path (`RF:393`; `LT:136,190,255,337,409,433`).

6. **Errors and status codes.**  
   Exceeded limits throw `EvaluationException` with **processing-error** and exactly **“Regular expression evaluation limit exceeded”**, without pattern/input text (`RF:683`; `LT:525`).

   Malformed patterns directly reach `Pattern.compile`, so direct helper calls expose `java.util.regex.PatternSyntaxException` (`RF:396`). Through registered invocation, ordinary failures become `FunctionInvocationException` with **processing-error**; nested EvaluationException statuses are retained (`DefaultFunctionInvocation.java:150`; `FunctionInvocationException.java:61`). The defensive missing-TypeToString branch instead raises `SyntaxException`, hence **syntax-error** (`RF:129`; `SyntaxException.java:35`).

   Add direct and registered malformed-pattern tests. Also pin when an invalid constant pattern fails: moving compilation earlier must not silently replace evaluation-time failure with policy-load rejection.

**Six typed functions**

All take a string pattern first and return Boolean; the EvaluationContext parameter is injected rather than an XACML operand.

| Function | Second operand | Source |
| --- | --- | --- |
| string-regexp-match | XML Schema string | `RF:66` |
| anyURI-regexp-match | XML Schema anyURI | `RF:77` |
| ipAddress-regexp-match | XACML 2.0 ipAddress | `RF:88` |
| dnsName-regexp-match | XACML 2.0 dnsName | `RF:99` |
| rfc822Name-regexp-match | XACML 1.0 rfc822Name | `RF:110` |
| x500Name-regexp-match | XACML 1.0 x500Name | `RF:121` |

The string function has its 1.0 URN; the other five expose canonical 2.0 URNs and legacy 1.0 aliases. All use TypeToString (`RF:129`), so tests must preserve lexical conversion rather than substitute object `toString()`. Particularly relevant representations include URI text, IPv6 brackets/mask/port, DNS port ranges, RFC822 names, and X.500 formatting (`AnyURI.java:127`; `IPAddress.java:228`; `DNSName.java:173`; `RFC822Name.java:258`; `X500Name.java:74`).

`RT:110` exercises five typed methods only with the broadly matching `.*$`. Add discriminating positive/negative cases through the registry for every type and alias. Also fix `RT:56`: checking that `getFunction()` is non-null does not establish registration because its return type is Optional (`FunctionProvider.java:64`).

**Options and recommendation**

| Dot-semantics choice | Cost and risk | What tests prove |
| --- | --- | --- |
| Preserve current JDK behavior in RE2 translation | Requires syntax-aware translation; incorrect handling inside classes/escapes is a risk | Differential cases for CR, LF, U+0085, U+2028, U+2029, dotall flags, and affected fixture patterns |
| Allow documented engine differences | Less translation work; selecting an engine can change decisions despite green conformance | An explicit matrix records each differing tuple and expected engine result |

Recommend preserving the JDK implementation’s behavior during extraction and defining RE2 differences explicitly before claiming interchangeability. Conformance alone is insufficient acceptance evidence.

**Confidence and drift risk**

Fixture inventory and source behavior are **SUPPORTED** by current reads and independent text/XML inventories. Engine outcomes remain **UNVERIFIED**.

Both **XACML 2.0 OS, 1 February 2005**, and **XACML 3.0 OS, 22 January 2013**, define these functions in §A.3.13. Their XF references differ: 2.0 cites §6.3.15; 3.0 cites §7.6.2. The proposal’s XPath F&O 3.1 reference is a later document; distinguish that reference from the standards’ original wording. [XACML 2.0](https://docs.oasis-open.org/xacml/2.0/access_control-xacml-2.0-core-spec-os.pdf), [XACML 3.0](https://docs.oasis-open.org/xacml/3.0/xacml-3.0-core-spec-os-en.html).

Context7 returned C++ RE2 coverage rather than RE2/J-specific coverage; Java conclusions therefore use pinned 1.8 sources. No quota or research-service failure occurred.

**Not checked**

No files were edited. No Maven, tests, benchmarks, profilers, Git writes, or beads writes were run. Neither engine was executed. Unicode-version equivalence, exhaustive XML Schema translation, runtime class-initialization behavior, and actual RE2/J conformance passes remain unverified.
