FINAL REPORT (xacml4j JMH benchmark engineer, GPT-6)

**Answer: recommend an engine-neutral compiled-pattern SPI, with policy-owned constant bindings and a bounded cache for other calls. Share compiled patterns; create matching state for each evaluation.** Keep `jdk` as the default. The design is ready to develop further; implementation correctness and performance remain **UNVERIFIED**.

All external sources below were retrieved on **2026-10-27**.

1. **SPI shape and matching contract**

   Proposed API sketch—not existing code:

   ```java
   interface RegexEngine {
       String name(); // "jdk" or "re2j"

       CompiledRegex compile(
           String xacmlPattern,
           RegexCompileOptions options,
           RegexBudget budget) throws RegexCompileException;
   }

   interface CompiledRegex {
       boolean find(
           String input,
           RegexBudget budget) throws RegexEvaluationException;
   }
   ```

   `RegexCompileOptions` should be immutable and identify the syntax contract, translation options, and structural limits. `RegexBudget` belongs to one operation; it must never be stored in a compiled pattern.

   The matcher contract should require a **fresh search from the beginning of the input on every `find` call**, including correct empty-pattern and anchor behavior. Keep native `Pattern` and `Matcher` objects behind the implementation. A separate public matcher interface adds little value while the only operation is a boolean substring search.

   This matches XACML 3.0 plus Errata 01 §A.3.13: the string function uses XPath matching with reversed arguments, and the other five functions convert their second argument to a string first. The referenced XPath edition specifies substring matching unless anchors constrain it. [OASIS, 12 July 2017, §A.3.13](https://docs.oasis-open.org/xacml/3.0/errata01/os/xacml-3.0-core-spec-errata01-os-complete.html); [XPath Functions and Operators, 23 January 2007, §7.6.2](https://www.w3.org/TR/2007/REC-xpath-functions-20070123/#func-matches).

   Today, all six functions already converge on one helper, which uses `TypeToString`; the actual JDK search calls `Matcher.find()`. Preserve that conversion path and the existing function identifiers and legacy aliases. [RegExpFunctions.java:64](xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/RegExpFunctions.java:64), [RegExpFunctions.java:135](xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/RegExpFunctions.java:135), [RegExpFunctions.java:403](xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/RegExpFunctions.java:403).

2. **Where a compiled constant can live**

   `FunctionSpecBuilder` currently stores one `FunctionInvocation` in each function specification. It evaluates arguments before passing them to that invocation. The annotation converter builds this specification from a Java method, without receiving a particular policy expression’s arguments. Therefore, putting one mutable “compiled constant” field on the shared specification would mix different call sites. [FunctionSpecBuilder.java:267](xacml-core/src/main/java/org/xacml4j/v30/policy/function/FunctionSpecBuilder.java:267), [FunctionSpecBuilder.java:415](xacml-core/src/main/java/org/xacml4j/v30/policy/function/FunctionSpecBuilder.java:415), [JavaMethodToFunctionSpecConverter.java:241](xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/JavaMethodToFunctionSpecConverter.java:241).

   Add an optional preparation/binding hook whose **result belongs to the policy call site**. `FunctionSpecBuilder` can hold the binding factory, while `Apply` and `Match` hold the resulting bound invocation:

   - `Apply` already owns its specification and argument list.
   - `Match` owns a literal first argument and invokes its predicate repeatedly over an attribute bag.
   - Higher-order calls need the cache fallback: fixture IIC168 passes regex patterns through a bag to `any-of-all`, so direct `Apply` specialization alone misses a real conformance path.

   Evidence: [Apply.java:57](xacml-core/src/main/java/org/xacml4j/v30/policy/Apply.java:57), [Match.java:44](xacml-core/src/main/java/org/xacml4j/v30/policy/Match.java:44), [Match.java:121](xacml-core/src/main/java/org/xacml4j/v30/policy/Match.java:121), [IIC168Policy.xml:9](xacml-conformance-tests/src/test/resources/xacml3.0-ct-v.0.4/IIC168Policy.xml:9).

   Only recognize proven constants initially, such as literal `StringVal` arguments. Do not evaluate arbitrary expressions during policy loading to discover whether they are constant.

**Reuse options**

| Option | Cost and risk | What a test would prove |
|---|---|---|
| Compile literals during policy loading | Moves work off the decision path; increases loading cost. Immediate failures can change when an error becomes observable. Dynamic and higher-order calls still need another path. | Loading and evaluation preserve the chosen failure semantics; unvisited branches do not unexpectedly affect decisions. |
| Bounded cache on first call | Smallest integration change. Every call performs a lookup; eviction can cause recompilation. | Compilation is reused while resident; capacity is enforced; eviction and concurrent misses behave correctly. |
| Policy-owned constant binding plus bounded fallback cache | More integration work, but constants avoid repeated lookup after initialization. Bound patterns live as long as their policies. | Distinct expressions and engines never share the wrong pattern; literal reuse, dynamic calls, higher-order calls, and policy retirement all behave correctly. |

**Recommendation:** the third option, with constant bindings created at policy load and initialized on their **first actual evaluation**. Optional eager preparation can follow, provided it records failures without changing evaluation semantics. A cache-only implementation is a reasonable first increment, but it should not be described as permanent compile-once behavior across eviction.

Use a thread-safe cache owned by a stable regex configuration/service, keyed by the original pattern and relevant compilation options. Engine identity is implicit if each service owns exactly one engine; otherwise it must be part of the key. Bound entries and concurrent compilation work. Avoid holding a global cache lock while compiling.

Cache successful compilation and, if useful, bounded immutable descriptors of deterministic syntax/unsupported-feature failures. Do not retain request input, evaluation contexts, or transient deadline failures. Policy-owned references are outside cache eviction and need an explicit policy-lifetime memory budget. These are design recommendations; their implementation is **UNVERIFIED**.

3. **Thread safety and engine selection**

   Require engines and compiled wrappers to support concurrent calls. Matching state, input references, and deadlines remain invocation-local. Java SE 25 explicitly documents `Pattern` as immutable and thread-safe, and `Matcher` as unsafe for concurrent sharing. [Java SE 25 Pattern](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/regex/Pattern.html), [Java SE 25 Matcher](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/regex/Matcher.html).

   RE2/J 1.8’s matcher is stateful. Its engine source uses an atomic machine pool and documents retention up to peak simultaneous matching demand. Consequently, a bounded number of compiled patterns is **not a fixed heap-byte bound**, especially under concurrency. [RE2/J 1.8 Matcher source](https://raw.githubusercontent.com/google/re2j/re2j-1.8/java/com/google/re2j/Matcher.java), [RE2/J 1.8 RE2 source](https://raw.githubusercontent.com/google/re2j/re2j-1.8/java/com/google/re2j/RE2.java).

   Freeze engine selection and limits for the lifetime of an evaluation environment. A bound invocation must capture that environment or be recreated for another engine. If the same parsed policy serves differently configured PDPs, use separate bound evaluation plans. A request must not select an engine, and unsupported RE2/J syntax must not silently fall back to JDK.

   Keep the six annotated functions as thin adapters into one regex service. Context-based service access is possible because their signatures already receive `EvaluationContext`, but the required accessor and binding lifecycle would be new design work. [RegExpFunctions.java:67](xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/RegExpFunctions.java:67).

4. **Preserve limits and distinguish failures**

   Today’s defaults are 10,000 pattern characters, 100,000 input characters, 1,000 milliseconds, repetition weight 1,000,000, and an optional 256-entry cache **per thread**. Translation and repetition scanning occur before the cache lookup. [RegExpFunctions.java:153](xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/RegExpFunctions.java:153), [RegExpFunctions.java:175](xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/RegExpFunctions.java:175), [RegExpFunctions.java:389](xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/RegExpFunctions.java:389).

   Recommended contract:

   - Check input length and start a fresh deadline on every call, including cache hits.
   - On a miss, use one budget for translation, compilation, and matching. Separately budget optional policy-load preparation.
   - Retain JDK translation and repetition checks for compatibility. Do not feed JDK-specific translated syntax into RE2/J.
   - Bound translation expansion and compilation resources as well as source length.
   - Distinguish invalid pattern, unsupported feature, structural limit, and deadline failures internally.

   The existing JDK deadline is cooperative: work without input reads may continue, although a late result is rejected. The SPI must not promise hard cancellation that the adapter cannot enforce. RE2/J’s linear-input matching claim also does not establish a wall-clock bound on compilation or translation. [RegExpFunctions.java:42](xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/RegExpFunctions.java:42), [RegExpFunctions.java:378](xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/RegExpFunctions.java:378), [RE2/J 1.8 README](https://raw.githubusercontent.com/google/re2j/re2j-1.8/README.md).

   Preserve the current limit-to-`processing-error` mapping. Current invocation wrapping also maps other ordinary failures to `processing-error` unless an `EvaluationException` supplies a status. Do not silently change that while extracting the SPI. Policy-admission errors and unsupported-pattern status need an explicit semantic decision; §7.19 distinguishes unsupported functionality from invalid policy syntax. [RegExpFunctions.java:683](xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/RegExpFunctions.java:683), [FunctionInvocationException.java:59](xacml-core/src/main/java/org/xacml4j/v30/policy/FunctionInvocationException.java:59), [OASIS §7.19](https://docs.oasis-open.org/xacml/3.0/errata01/os/xacml-3.0-core-spec-errata01-os-complete.html).

   Keep existing limit properties as compatibility defaults. Make cache enablement and scope changes explicit: changing from an opt-in per-thread cache to default shared reuse changes both resource use and deadline outcomes.

5. **Modules and registration**

   Proposed layout:

   ```text
   xacml-core
     org.xacml4j.v30.spi.regex
       RegexEngine, CompiledRegex, options, budgets, typed failures
     org.xacml4j.v30.spi.regex.jdk
       JdkRegexEngine
     shared selection/cache/function adapters

   xacml-regex-re2j
     org.xacml4j.v30.spi.regex.re2j
       Re2jRegexEngine
     dependencies: xacml-core, com.google.re2j:re2j:1.8
   ```

   Register each engine with `@AutoService(RegexEngine.class)`. Discover engines once, validate unique names, and select explicitly. Unknown names, duplicate names, or an unavailable requested engine should fail configuration; adding the RE2/J JAR must not change the default.

   This follows the repository’s existing `@AutoService(FunctionProvider.class)` convention and compiler annotation-processor configuration. AutoService generates the corresponding `META-INF/services` descriptor. [XacmlDefaultFunctions.java:30](xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/XacmlDefaultFunctions.java:30), [pom.xml:363](pom.xml:363), [AutoService 1.1.1 documentation](https://raw.githubusercontent.com/google/auto/auto-service-1.1.1/service/README.md).

   Package-level discovery tests should prove that core works alone, both engines appear when the adapter is present, duplicate names fail, and packaged/shaded artifacts retain service descriptors.

6. **JMH design**

   Measure three variants: today’s implementation with its cache disabled, the SPI using `jdk`, and the SPI using `re2j`. Include today’s cache-enabled configuration as a separate diagnostic comparison.

   The function benchmark should separate:

   - Translation and compilation.
   - Matching with a precompiled pattern.
   - Complete function invocation, including typed string conversion, limits, cache lookup, and result creation.
   - Literal binding, cache hit, miss, eviction, and concurrent use.

   Cover all six functions, matching and nonmatching inputs, early/late matches, anchors, empty patterns, Unicode, pattern complexity, input size, and working sets below/at/above cache capacity. Use identical supported cases for engine comparisons. Report unsupported and limit-rejected cases separately; fast rejection is not successful matching throughput.

   Add a `RegexCorpus` through the existing `PolicyCorpusProvider` mechanism. Include target `Match`, condition `Apply`, and higher-order bag evaluation. Existing seeds include IIB008’s `read|write` target and IIC168’s bag of patterns; supplement them with typed functions and controlled scaling cases. [SyntheticCorpus.java:47](xacml-jmh/src/main/java/org/xacml4j/jmh/corpus/SyntheticCorpus.java:47), [IIB008Policy.xml:31](xacml-conformance-tests/src/test/resources/xacml3.0-ct-v.0.4/IIB008Policy.xml:31).

   **Important measurement boundary:** `PdpFixture.open()` parses policies and checks decisions before measurement. Thus its normal `PdpDecideBenchmark` measures warmed decisions and cannot establish first-use compilation cost. Use separate loading/first-decision measurements with fresh state. Engine configuration must reach parsing and binding, not merely appear as a benchmark parameter. [PdpFixture.java:104](xacml-jmh/src/main/java/org/xacml4j/jmh/harness/PdpFixture.java:104), [PdpDecideBenchmark.java:69](xacml-jmh/src/main/java/org/xacml4j/jmh/pdp/PdpDecideBenchmark.java:69).

   Use independent expected results, checked outside timed methods. Keep potentially nonterminating JDK patterns in isolated safety tests. Future performance runs require a conductor-granted quiet window, at least three forks, five warmup and measurement iterations, returned results or `Blackhole`, and `-prof gc`. Compare the same machine, exact JDK/JIT, flags, and inputs; report score ± error and no improvement when intervals overlap. [BENCHMARKS.md](docs/ai/BENCHMARKS.md).

| Planned result | Current baseline | SPI `jdk` | SPI `re2j` |
|---|---|---|---|
| Function latency ± error | NOT RUN | NOT RUN | NOT RUN |
| PDP decision latency ± error | NOT RUN | NOT RUN | NOT RUN |
| Compilation/first-use cost | NOT RUN | NOT RUN | NOT RUN |
| Allocation, bytes/operation | NOT RUN | NOT RUN | NOT RUN |

**Confidence and disagreement / drift risk**

High confidence in the inspected integration boundaries and substring-search contract. The hybrid binding design is a recommendation, not implemented evidence.

The packet’s annotation names differ from this checkout: the actual types are `XacmlFunctionProvider` and `XacmlFuncSpec`. The latter controls argument evaluation but supplies no constant-binding metadata. [XacmlFunctionProvider.java:32](xacml-core/src/main/java/org/xacml4j/v30/policy/function/XacmlFunctionProvider.java:32), [XacmlFuncSpec.java:32](xacml-core/src/main/java/org/xacml4j/v30/policy/function/XacmlFuncSpec.java:32).

The inspected build targets **JDK 25**, despite the older JDK 28 migration context. Benchmark preview flags should follow the actual build under test. [pom.xml:362](pom.xml:362). RE2/J evidence was pinned to **1.8**; whether it is the latest release was not checked. The shared tree already contained modifications, including `FunctionSpecBuilder`; these observations describe that working tree.

Tavily extraction failed with an upstream timeout on 2026-10-27. DuckDuckGo successfully fetched the official OASIS source. Context7 resolution returned unrelated or C++ results for RE2/J and AutoService; those results were not used as API evidence. Versioned upstream sources supplied the fallback.

**Verification and not checked**

Read source and documentation using `rtk proxy sed`, searched with `rtk rg`, inspected `rtk git status --short`, and fetched the cited official sources. **No files were written or edited; no benchmark code was added.** No Maven, tests, benchmarks, profilers, Git writes, or beads writes were run.

Not checked: exhaustive conformance inventory; full XML Schema-to-RE2 translation coverage; executable status parity; actual service descriptors; concurrency correctness; heap retention; or performance. Those remain **UNVERIFIED**. The main residual risks are changed error timing, engine/configuration leakage between shared policies, incomplete syntax translation, and overstated deadline or memory guarantees.
