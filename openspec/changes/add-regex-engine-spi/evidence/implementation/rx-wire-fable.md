=== BEGIN REPORT RX-WIRE-fable ===

FINAL REPORT (policy4j_implementer_claude_fable, Claude Fable 5.1)

Worktree `.ntm/worktrees/xacml4j--batch2/cc_2`, branch `ntm/xacml4j--batch2/cc_2`, HEAD `87133237`,
on the main tree as the conductor reset it (the SPI of tasks 2.1, 3.1, 3.2 merged; cycles AQ, AR,
AS; FIX-CYCLE2 and FIX-CYCLE3). Change `add-regex-engine-spi`, groups 4 (D3, decision 1) and 5
(D6, decision 4). Beads: `br update --claim` refused all four of `policy4j-eufz.4.1`, `4.2`,
`5.1`, `5.2` ("cannot claim blocked issue", naming 3.3, 4.1, 4.3 and 5.1 as the blockers), so the
conductor claims or unblocks them. No file under `openspec/`, `.beads/`, the vendored fixtures or
the XSDs was edited.

## Summary

Both groups are done and green. The regexp functions a provider registers are now an instance,
`RegExpFunctions.Matching`: the six functions on one engine, profile and limits, with a cache of
compiled patterns (design D3). `FunctionProviderBuilder.withRegexEngine` and
`PolicyRepository.Builder.withRegexEngine` select the engine and profile; without a selection the
system properties `org.xacml4j.regex.engine` and `org.xacml4j.regex.profile` are read when the
provider is built, and `jdk`/`legacy` is the default, one shared instance. An unknown engine, an
unknown profile, a profile the engine does not offer, and two providers of one engine name fail
when the provider is built. `PolicyImportTool` unmarshals with the functions and algorithms of its
repository (two new accessors on `PolicyRepository`), so the conformance runner's repositories and
`PdpFixture` load their policies over the selected engine. The instance methods run on cycle AS's
trusted dispatch: a method handle bound to the instance, the same `FIXED` shape as the static
methods had.

Compile once (design D6): a `Match` or `Apply` whose first argument is a string literal binds the
pattern at its call site through a new `FunctionSpec.bind` and a `BoundCall`, compiled on the first
evaluation, never at load, and kept with its deterministic failure; every other call goes through
a Guava cache per `Matching` (256 entries by default, the `cacheSize` property, no lock while a
pattern compiles); a malformed, unsupported or structural-limit failure is kept, a deadline or
resource failure is not; `cacheRegExps` logs one warning and is ignored. The reactor with
`legacy` is green (core 7,194, PDP 716, conformance 1,818, JMH 225), the OASIS and RSA suites also
pass with `jdk`/`xf`, and the one-fork smoke cuts the regex requests of `rules:100` by 54 to 93
percent in time and by 75 to 90 percent in allocation, under a load average of 7 to 13 from
another lane's build (the numbers are smokes, not the 5.3 pair).

Two pre-existing defects surfaced, neither in this packet's files: the status of a failing target
match is reported as a processing-error without a message (finding 1), and a first compilation on
a cold, loaded JVM can exceed the one-second deadline of the call (finding 2).

## Changes

### Group 4: provider wiring and selection (D3)

- `RegExpFunctions` (`xacml-core/.../function/impl/RegExpFunctions.java`): `Matching` is public,
  carries `@XacmlFunctionProvider`, and holds the six `*-regexp-match` functions as instance
  methods (the same annotations and identifiers as the static ones); it implements
  `LiteralBinder` (group 5). Factories `matching()` (the properties, read now; `DEFAULT` when they
  name `jdk` and `legacy` or are not set), `matching(String engine, String profile)` (resolves
  `RegexEngines.engine(name)` once, here, and the profile by `RegexProfile.of`, each failure an
  `IllegalArgumentException` that names the engine or the profile and lists the known ones;
  duplicate providers stay the `IllegalStateException` of `RegexEngines`), and
  `matching(RegexEngine, RegexProfile)` with the limits of the properties. The constructor
  `Matching(RegexEngine, RegexProfile, RegexLimits)` rejects a profile the engine does not offer
  ("engine X does not offer profile Y"). The six static methods of design D7 stay and delegate to
  `DEFAULT`, as do `matches(String, String)` and `covertXacmlToJavaSyntax`.
- `XacmlDefaultFunctions`: `PROVIDERS` lists the static provider classes without
  `RegExpFunctions`; the regexp functions come from a `Matching`: the one given to the new
  constructor `XacmlDefaultFunctions(FunctionInvocationFactory, RegExpFunctions.Matching)`, or
  `RegExpFunctions.matching()` for the other constructors (also the `ServiceLoader` one).
  `isBuiltIn(Class)` (package-private) names the engine's own providers: `PROVIDERS`,
  `RegExpFunctions` and `RegExpFunctions.Matching`; `JavaMethodToFunctionSpecConverter`
  (`writesNoResponses`, design D36) and `DefaultFunctionInvocation` (trust) use it instead of
  `PROVIDERS.contains`.
- `DefaultFunctionInvocation.create`: an instance method of a public class of this loader is
  trusted like a static one (`isBindable(Method, Object)`), and gets a method handle with the
  instance bound into it (`handle(Method, Object)`: `unreflect(m).bindTo(instance)`), so the
  regexp instance methods take the `FIXED` fast shape of cycle AS (`invokeFast`, `invokeFixed`) as
  the static methods did; the lambda bindings of one- and two-parameter methods stay static only.
  `callShape`, `isTrustedBuiltIn`, `usesBuiltInInvocation` and the higher-order fast paths are
  unchanged, so IIC168's `any-of-all` keeps `anyOfPair` and `invokeVariadicPair`.
- `FunctionProviderBuilder`: `withRegexEngine(String engine, String profile)`,
  `withRegexEngine(RegexEngine, RegexProfile)` and `withRegexEngine(RegexEngine, RegexProfile,
  RegexLimits)`. The standard functions this builder has added are built again with the regexp
  functions of the selection (`builder()` adds them before any call), the ones it adds later use
  it, and a discovered `XacmlDefaultFunctions` is skipped when an engine was selected.
- `PolicyRepository`: `getFunctionProvider()` and `getAlgorithmProvider()`, default methods that
  return the marshalling providers' system providers, which every import used before;
  `AbstractPolicyRepository` overrides them with its fields. `Builder.withRegexEngine(String,
  String)` builds the standard functions with the engine (replacing a provider given before), and
  the default function provider is built when the repository is, not when the builder is.
- `PolicyImportTool.importPolicy`: `newPolicyUnmarshaller(repository.getFunctionProvider(),
  repository.getAlgorithmProvider())` instead of the static `newPolicyUnmarshaller()`; the class
  comment says so. The conformance runner (`ConformanceCaseRunner`) is unchanged: it builds its
  repositories with its own `FUNCTIONS` (the standard functions, so the properties select the
  engine) and imports through the tool, which now uses them.
- `xacml-jmh`: `Marshalling.policies(MediaType, FunctionProvider,
  DecisionCombiningAlgorithmProvider)`; `PdpFixture.parse` parses over the fixture's `FUNCTIONS`
  and `ALGORITHMS`, the ones its store repositories get, with the Javadoc naming the properties;
  the test double `ForwardingPolicyRepository` forwards the two accessors.

### Group 5: compile once (D6)

- `Matching` keeps a `LoadingCache<String, Compiled>` of `limits.cacheSize()` entries, keyed by
  pattern (the profile is fixed per instance, so two instances over one engine keep their patterns
  apart). `Compiled` is a private record: the `CompiledRegex`, or the kept `RegexCompileException`.
  The loader keeps a deterministic failure as an outcome and rethrows a transient one, which Guava
  wraps and `compile()` unwraps (`UncheckedExecutionException`, `ExecutionError`). Guava loads
  outside its segment lock, and one load serves every thread that misses the same key.
- `RegexCompileException.isDeterministic()` (new public method of the SPI): malformed, unsupported
  and a structural limit are deterministic; a limit from an `Error` of the engine (a resource
  failure) is not. `RegexLimitException` (the deadline) is never kept.
- Call-site binding: `org.xacml4j.v30.policy.BoundCall` (new interface, one method
  `<T extends ValueExp> T invoke(EvaluationContext, Expression argument)`) and
  `org.xacml4j.v30.policy.function.LiteralBinder` (new interface, `Optional<BoundCall>
  bind(String functionId, Value<?> literal)`), implemented by the provider instance.
  `FunctionSpec.bind(Value<?>)` is a new default method (empty); `FunctionSpecBuilder.FunctionSpecImpl`
  overrides it for an eager pair whose invocation is a `DefaultFunctionInvocation` with a binder:
  the bound call evaluates the second argument, takes the validated list path when validation is
  on at runtime, and maps and reports a failure through `failed` exactly as `invokeDirect` does;
  `DefaultFunctionInvocation.bind(FunctionSpec, Value<?>)` wraps the provider's call with the
  `FunctionInvocationException(spec, invocationCause(e))` mapping of the direct path.
  `Match` binds its literal when it is built (`bound`, null for a function that binds none) and
  invokes it per bag value; `Apply` binds the literal first argument of a type-correct
  two-argument expression and invokes it with `invocationArguments.get(1)` (the type assertion of
  a dynamic argument stays). Nothing is evaluated at load: `Matching.bind` only wraps the pattern
  string in a `BoundPattern`, which compiles through the cache on its first evaluation and keeps
  the outcome in a volatile field; a call site therefore survives the eviction of its pattern.
- `cacheRegExps`: the property is read once; when it is set to anything, `RegExpFunctions` logs
  one warning naming the property and the cache size, and compiled patterns are reused
  regardless. The per-thread `PatternCache` and `REGEXP_CACHE` are gone.
- `xacml-jmh/README.md`: the paragraph that said `call` and `callMiss` do the same work until D6
  now says what each measures after it.

### New or changed public and package-private API (every new SPI method)

- New types: `org.xacml4j.v30.policy.BoundCall`; `org.xacml4j.v30.policy.function.LiteralBinder`.
- `FunctionSpec`: `default Optional<BoundCall> bind(Value<?> literal)`.
- `DefaultFunctionInvocation`: `public Optional<BoundCall> bind(FunctionSpec, Value<?>)`.
- `FunctionProviderBuilder`: the three `withRegexEngine` overloads above.
- `XacmlDefaultFunctions`: constructor `(FunctionInvocationFactory, RegExpFunctions.Matching)`;
  package-private `static boolean isBuiltIn(Class<?>)`; `PROVIDERS` without `RegExpFunctions`.
- `RegExpFunctions`: `ENGINE_PROPERTY`, `PROFILE_PROPERTY`; `static Matching matching()`,
  `matching(String, String)`, `matching(RegexEngine, RegexProfile)`; `Matching` public with
  `Matching(RegexEngine, RegexProfile, RegexLimits)`, `engine()`, `profile()`, `limits()`, the six
  function methods, `bind(String, Value<?>)`, `boolean matches(String, String)`. Removed:
  `PatternCache` (package-private) and the `.cacheRegExps` switch.
- `RegexCompileException`: `boolean isDeterministic()`.
- `PolicyRepository`: `default FunctionProvider getFunctionProvider()`,
  `default DecisionCombiningAlgorithmProvider getAlgorithmProvider()`,
  `Builder.withRegexEngine(String, String)`.
- `xacml-jmh` `Marshalling`: `policies(MediaType, FunctionProvider, DecisionCombiningAlgorithmProvider)`.

### Tests

- `xacml-pdp/src/test/java/org/xacml4j/v30/spi/repository/RegexEngineSelectionTest.java` (9): an
  unknown engine (`none`), an unknown profile (`nope`), a profile the engine does not offer (an
  engine of `xf` only, standing for `re2j`/`legacy`), the repository builder with an unknown
  engine, two providers of one name (`twin`, registered in
  `xacml-pdp/src/test/resources/META-INF/services/org.xacml4j.v30.policy.function.regex.RegexEngineProvider`
  as two nested classes of the test; the message names both), the properties (set to `xf`, then
  `nope`, then cleared, restored in `finally`), two repositories (`jdk`/`legacy` and `jdk`/`xf`,
  one policy text with `\d` imported into each through its import tool, U+0663: NotApplicable and
  Permit; ASCII `3`: Permit), the import tool using the repository's functions (an engine whose
  every pattern finds every input permits "no digit here"; the default repository does not), and
  `getFunctionProvider()` identity. A `@BeforeAll` compiles one `xf` pattern under a 60-second
  deadline, for finding 2.
- `xacml-pdp/src/test/java/org/xacml4j/v30/pdp/RegexCompileOnceTest.java` (7), outcomes only,
  with two minimal engine doubles over the `jdk` engine: `CompilesOnce` (a repeated compilation of
  a pattern is malformed) and `FailsFirst` (the first compilation of each pattern fails as given).
  Literal in a target: 1,000 Permits, then three distinct dynamic patterns through a second PDP
  over the same provider and a cache of two, then Permit again (the call site kept its pattern
  through eviction) and NotApplicable for a non-matching subject. The same for an `Apply` in a
  condition. IIC168 (the policy and request inline, imported through the tool): Permit twice, the
  second decision equal to the first. Deterministic failure kept: the target decides
  Indeterminate{P} twice, the condition and the dynamic path twice syntax-error with the fixed
  message (a second compilation would have permitted). Transient failure retried, for a
  `RegexLimitException` and for `RegexCompileException.limit(StackOverflowError)`:
  Indeterminate{P} then Permit on the target; processing-error with the limit message then Permit
  on the condition and the dynamic path. Concurrent misses: eight threads behind a latch, every
  decision Permit, on the target and on the dynamic path. Eviction with a cache of two: the third
  pattern evicts the first, the second and third stay hits, the first is compiled again and the
  double rejects it. The fixture's deadline is 60 seconds (finding 2).
- `RegExpLimitsTest`: the two `PatternCache` bound assertions became `RegexLimits.builder()
  .cacheSize(Integer.MAX_VALUE)`, and `patternCacheKeepsTheMostRecentlyUsedPatterns` became
  `compiledPatternsAreReusedPerInstance` (a `Matching` over an engine that compiles once; another
  instance over the same engine compiles again).
- Failing first: the new tests use `withRegexEngine`, `getFunctionProvider`, `FunctionSpec.bind`
  and the public `Matching`, so against the main tree they do not compile; I wrote them before the
  code and ran them after it. The first run (14:43:48) had three failures that led to findings 1
  and 2 and to the two test adjustments above; I did not run them against the old code.

### Files

Changed against the main checkout (`git diff --no-index --stat`, insertions/deletions):
`xacml-core/src/main/java/org/xacml4j/v30/policy/Apply.java` (+14), `FunctionSpec.java` (+12),
`Match.java` (+5/-2), `function/FunctionProviderBuilder.java` (+68/-6), `function/FunctionSpecBuilder.java`
(+30), `function/impl/DefaultFunctionInvocation.java` (+58/-21),
`function/impl/JavaMethodToFunctionSpecConverter.java` (+1/-1), `function/impl/RegExpFunctions.java`
(+395/-152), `function/impl/XacmlDefaultFunctions.java` (+50/-8),
`function/regex/RegexCompileException.java` (+9); `xacml-core/src/test/.../impl/RegExpLimitsTest.java`
(+47/-17); `xacml-pdp/src/main/java/org/xacml4j/v30/spi/repository/PolicyRepository.java` (+52/-9),
`AbstractPolicyRepository.java` (+10), `PolicyImportTool.java` (+6/-1);
`xacml-jmh/src/main/java/org/xacml4j/jmh/harness/Marshalling.java` (+13), `PdpFixture.java` (+5/-2),
`xacml-jmh/src/test/java/org/xacml4j/jmh/testing/ForwardingPolicyRepository.java` (+12),
`xacml-jmh/README.md` (+8/-6). New: `xacml-core/src/main/java/org/xacml4j/v30/policy/BoundCall.java`,
`xacml-core/src/main/java/org/xacml4j/v30/policy/function/LiteralBinder.java`,
`xacml-pdp/src/test/java/org/xacml4j/v30/spi/repository/RegexEngineSelectionTest.java`,
`xacml-pdp/src/test/java/org/xacml4j/v30/pdp/RegexCompileOnceTest.java`,
`xacml-pdp/src/test/resources/META-INF/services/org.xacml4j.v30.policy.function.regex.RegexEngineProvider`.
`git diff --check` and `ast-grep scan` of the changed production files are clean; no trailing
whitespace in the new files.

## Findings

1. MEDIUM, pre-existing, outside this packet's files: the status of a failing target match is not
   reported. `Match.match` records the status per rule
   (`context.setEvaluationStatus(context.getCurrentRule(), e.getStatus())`, `Match.java`), then
   `Rule.evaluateRule` sets a processing-error without a message if the general status is absent
   (`Rule.java:122`), and `DefaultPolicyDecisionPoint.createResult` reads the general status
   (`:318`). A condition keeps its status (`Condition.java:94`). Probe on the before-change jar
   (`scratchpad/rxwire/probe-main/StatusProbe.java`): a target with the malformed literal `(`
   decides Indeterminate{P} with `processing-error, message=null` three times; the same literal in
   a condition decides Indeterminate{P} with `syntax-error, "Regular expression pattern is
   invalid"`. So the D5 statuses reach the result through conditions and higher-order calls, not
   through targets. The new tests check the decision only for targets. A fix belongs to the
   rule/match status handling (design D34 territory), not to this packet.
2. MEDIUM, pre-existing by design (bead 4.58, "the time for compiling the pattern counts too"):
   on a cold JVM the first compilation of a pattern can exceed the default deadline of one
   second, and the decision is Indeterminate with the limit message. Observed: the first `xf`
   compilation in a Surefire fork took 1.7 s at load average 10 (another lane's `clean test`
   running) and failed with "Regular expression evaluation limit exceeded"
   (`scratchpad/rxwire/reactor-2.log` of the 15:05 run); on an idle JVM the same first
   compilation costs 18 ms for the category table plus 17 ms for the block table, and the first
   `legacy` compilation 136 ms of class loading (`scratchpad/rxwire/probe-main/WarmUpProbe.java`).
   The `xf` profile is exposed more than `legacy` because no other code loads its classes. A
   cheap mitigation for the maintainer: compile one probe pattern when a `Matching` is built
   (outside any deadline), or do not count a first compilation that the cache will keep. The new
   tests work around it with a 60-second deadline; the conformance suites did not hit it in six
   runs.
3. LOW, trade-off of D6: the cache miss path is slower and allocates more than today's direct
   compilation: `callMiss` (1,024 distinct patterns over a cache of 256, every call a miss and an
   eviction) at 16 characters went from 448 to 606 ns/op (+35 percent, outside the error) and from
   1,661 to 1,825 B/op, the other `callMiss` rows within error; a cache hit allocates 208 B/op
   (the budget, the matcher and Guava's recency-queue node) against 1,144 to 1,361 B/op before.
   The call-site path of a `Match` or `Apply` reads no cache at all.
4. INFO: `RegexEngines` is resolved once per `Matching` (`matching(String, String)`), closing the
   residual of RX-SPI; `DEFAULT` uses `new JdkRegexEngine()` directly.
5. INFO: the `twin` providers of the selection test are on the PDP module's test class path, so
   `RegexEngines.names()` lists `twin` in PDP tests; resolving it fails by design, nothing else
   changes.

## Verification

```
JAVA_HOME="$(jenv prefix 25)" rtk mvn -o -Dlicense.skipUpdateLicense=true -pl xacml-core,xacml-pdp,xacml-jmh -am -DskipTests test-compile
JAVA_HOME="$(jenv prefix 25)" rtk mvn -o -Dlicense.skipUpdateLicense=true -pl xacml-core,xacml-pdp -am -Dtest='RegexEngineSelectionTest,RegexCompileOnceTest,RegExpCharacterizationTest,RegExpLimitsTest,RegexStatusMatrixTest,RegularExpressionFunctionsTest,DirectPairCallTest,HigherOrderFunctionsTest,FunctionProviderServiceLoaderTest,AnnotationBasedFunctionFactoryTest,FunctionInvocationCharacterizationTest,DefaultFunctionSpecTest,FunctionInvocationStatusTest,HigherOrderVariadicFunctionsTest,MatchTest,ApplyTest,ApplyArgumentValidationTest,FunctionSpecBuilderTest,MatchStorageFastPathTest,LaterDecidingOperandTest' -Dsurefire.failIfNoSpecifiedTests=false test
JAVA_HOME="$(jenv prefix 25)" rtk mvn -o -Dlicense.skipUpdateLicense=true -pl xacml-pdp -am -Dtest='RegexEngineSelectionTest,RegexCompileOnceTest' -Dsurefire.failIfNoSpecifiedTests=false test
JAVA_HOME="$(jenv prefix 25)" rtk mvn -o -Dlicense.skipUpdateLicense=true -pl xacml-conformance-tests,xacml-jmh -am test
JAVA_HOME="$(jenv prefix 25)" rtk mvn -o -Dlicense.skipUpdateLicense=true -Dorg.xacml4j.regex.profile=nope -pl xacml-pdp -am -Dtest='RegexEngineSelectionTest' -Dsurefire.failIfNoSpecifiedTests=false test
JAVA_HOME="$(jenv prefix 25)" rtk mvn -o -Dlicense.skipUpdateLicense=true -Dorg.xacml4j.regex.profile=xf -pl xacml-conformance-tests -am -Dtest='Xacml20ConformanceTest,Xacml30ConformanceTest,RSA2008InteropTest,XacmlExampleTest' -Dsurefire.failIfNoSpecifiedTests=false test
JAVA_HOME="$(jenv prefix 25)" rtk mvn -o -q -Dlicense.skipUpdateLicense=true -pl xacml-jmh -am -DskipTests package
java -jar benchmarks.jar PdpDecideBenchmark.decide -p store=in-memory -p corpus=rules:100 -p request=repeated-conditions,regex-conditions,regex-targets,regex-any-of-all,regex-malformed -f 1 -jvmArgs -XX:-UseJVMCICompiler -prof gc -foe true -rf json
java -jar benchmarks.jar 'RegexFunctionBenchmark\.(call|callMiss)$' -f 1 -jvmArgs -XX:-UseJVMCICompiler -prof gc -foe true -rf json
```

Maven 3.9.16 (IntelliJ's), offline. Logs, JSON and scripts in this session's scratchpad under
`rxwire/` (`compile.log`, `focused-1.log`, `focused-2.log`, `probe.log`, `reactor-1.log`,
`reactor-2.log`, `propagation-nope.log`, `conformance-xf.log`, `jmh/{before,after}/{decide,regex}.{json,log}`,
`jmh/env.txt`, `jmh/decide-table.md`, `jmh/regex-table.md`).

| Run | Result |
| --- | --- |
| before-change benchmarks jar, from the untouched reset tree (14:17:26 to 14:18:16) | `package` exit 0, 10,831,970 bytes, kept as `rxwire/before/benchmarks.jar` |
| test-compile of core, PDP, JMH (14:42:33 to 14:43:15) | exit 0 |
| focused core and PDP tests, first run (14:43:48 to 14:44:12) | core 699 tests, 0 failures (characterization 116, limits 18, matrix 5, the function and match tests); PDP 16 tests, 3 failures: the two statuses of finding 1 and the cold `xf` compilation of finding 2 |
| focused PDP tests after the adjustments (14:57:30 to 14:58:05) | `RegexEngineSelectionTest` 9, `RegexCompileOnceTest` 7, 0 failures |
| reactor, conformance and JMH module, code before the last two small edits (15:01:54 to 15:04:42) | BUILD SUCCESS: core 7,194 (2 skipped), xml 254, json 24 (1 skipped), PDP 716 (3 skipped), test support 99, conformance 1,818 (30 skipped), JMH 225 |
| reactor on the final code, first attempt (15:05:23 to 15:06:36) | BUILD FAILURE: `RegexEngineSelectionTest.initializationError`, "Regular expression evaluation limit exceeded" after 1.694 s, load average 10 (finding 2); the warm-up got a 60-second deadline |
| reactor on the final code (15:09:55 to 15:12:32, Total time 02:31 min) | BUILD SUCCESS: core 7,194 (2 skipped), xml 254, json 24 (1 skipped), PDP 716 (3 skipped; `RegexEngineSelectionTest` 9, `RegexCompileOnceTest` 7), test support 99, conformance 1,818 (30 skipped: `Xacml20ConformanceTest` 375/7, `ObservedConformanceTest` 880/18, `Xacml30ConformanceTest` 488/5), JMH 225 |
| property propagation, `-Dorg.xacml4j.regex.profile=nope` (17:40:45) | `RegexEngineSelectionTest` 9 run, 3 errors, each `Unknown regular expression profile "nope"; the known profiles are [legacy, xf]`, one as `ExceptionInInitializerError` of the marshalling providers' system provider: the `-D` reaches the forked JVM and an unknown profile fails every provider build |
| conformance with `-Dorg.xacml4j.regex.profile=xf` (17:41:25 to 17:42:30) | BUILD SUCCESS: `RSA2008InteropTest` 17, `Xacml20ConformanceTest` 375 (7 skipped), `Xacml30ConformanceTest` 488 (5 skipped), `XacmlExampleTest` 2; 882 run, 0 failures |
| after-change benchmarks jar (15:12:38) | `package` exit 0, 10,840,917 bytes |
| JMH smoke, before then after (15:44:27 to 17:40:44) | four runs, exit 0, `-foe true`; the machine never went quiet: load average 7 to 13 throughout (another lane's `clean test` and a Gradle daemon), each wait capped at 30 minutes |

JMH environment: Oracle GraalVM 25.0.4+7.1 with C2 (`-XX:-UseJVMCICompiler`), JMH 1.37, Apple M2
Pro, macOS 26.6.1; 1 fork, 5 warmup and 5 measurement iterations of 1 s; `-prof gc`
(`gc.alloc.rate.norm`). Smokes under load: use them for direction, not as the 5.3 pair.

`PdpDecideBenchmark.decide`, `store=in-memory`, `corpus=rules:100`, `targetIndex=false`:

| Request | Before | After | Change | Before B/op | After B/op |
| --- | --- | --- | --- | --- | --- |
| regex-conditions | 79.944 ± 5.498 us/op | 13.648 ± 1.817 us/op | -82.9% | 247,262 | 24,273 |
| regex-targets | 66.395 ± 4.630 us/op | 16.192 ± 3.157 us/op | -75.6% | 212,937 | 21,914 |
| regex-any-of-all | 124.712 ± 10.060 us/op | 57.848 ± 3.130 us/op | -53.6% | 489,118 | 93,029 |
| regex-malformed | 385.508 ± 27.908 us/op | 27.659 ± 2.653 us/op | -92.8% | 483,130 | 69,290 |
| repeated-conditions | 63.519 ± 5.242 us/op | 28.965 ± 3.065 us/op | -54.4% | 194,697 | 39,475 |

`RegexFunctionBenchmark` (`RegExpFunctions.matches(String, String)`, the shared default instance):

| Benchmark | patterns, inputLength | Before | After | Change | Before B/op | After B/op |
| --- | --- | --- | --- | --- | --- | --- |
| call | conformance, 16 | 343.5 ± 41.7 ns/op | 140.9 ± 28.2 ns/op | -59.0% | 1,361 | 208 |
| call | adversarial, 16 | 535.9 ± 50.8 ns/op | 327.0 ± 28.0 ns/op | -39.0% | 1,144 | 208 |
| call | conformance, 256 | 1,124.5 ± 158.7 ns/op | 960.0 ± 170.8 ns/op | -14.6% (within error) | 1,361 | 208 |
| call | adversarial, 256 | 88,153 ± 8,937 ns/op | 83,562 ± 4,369 ns/op | -5.2% (within error) | 1,145 | 209 |
| call | conformance, 4096 | 12,958 ± 882 ns/op | 12,817 ± 2,271 ns/op | -1.1% (within error) | 1,361 | 208 |
| call | adversarial, 4096 | 22,418,219 ± 1,935,378 ns/op | 23,533,738 ± 3,878,004 ns/op | +5.0% (within error) | 1,517 | 369 |
| callMiss | conformance, 16 | 448.3 ± 39.4 ns/op | 606.0 ± 109.3 ns/op | +35.2% | 1,661 | 1,825 |
| callMiss | adversarial, 16 | 628.8 ± 41.6 ns/op | 751.8 ± 120.1 ns/op | +19.6% (within error) | 1,411 | 1,599 |
| callMiss | conformance, 256 | 1,239.3 ± 89.9 ns/op | 1,463.2 ± 143.2 ns/op | +18.1% (within error) | 1,661 | 1,881 |
| callMiss | adversarial, 256 | 100,775 ± 13,870 ns/op | 85,531 ± 7,553 ns/op | -15.1% (within error) | 1,379 | 1,631 |
| callMiss | conformance, 4096 | 13,386 ± 1,400 ns/op | 13,192 ± 777 ns/op | -1.4% (within error) | 1,661 | 1,881 |
| callMiss | adversarial, 4096 | 24,502,147 ± 4,252,248 ns/op | 22,771,678 ± 3,363,020 ns/op | -7.1% (within error) | 1,691 | 1,925 |

The `call` rows are cache hits after D6 (the set's patterns repeat); `callMiss` rows are misses
with an eviction on every call (finding 3). The long inputs are dominated by the search itself,
as the design expects: compilation cost shows at 16 characters only.

## Residual risk

- Finding 1 hides the D5 statuses of targets in results; the corpus and the conformance cases
  that use regexp functions evaluate through conditions, so nothing pinned changes, but a client
  that reads the status of a target failure gets processing-error without a message, as before.
- Finding 2: the first regexp evaluation of a JVM under heavy load can be Indeterminate with the
  limit message; it was so before this change for `legacy`, and `xf` adds its class loading.
- Finding 3: a workload whose dynamic patterns exceed the cache (more than 256 distinct patterns
  cycling) pays about 160 ns and 200 B per call more than today's direct compilation; raising
  `cacheSize` is the remedy. The call-site path has no such cost.
- A provider instance of another implementation that implements `LiteralBinder` is bound for
  every two-argument eager call with a literal first argument; the binder decides per function
  identifier, and a static provider class is never bound.
- `FunctionProviderBuilder.withRegexEngine` builds the standard functions again when they were
  added before the call (`builder()` adds them first): one extra reflection pass per such
  builder, only when an engine is selected.
- `PolicyRepository.getFunctionProvider()` has a default for other implementations; one that does
  not override it imports with the static system functions, as before, with no warning.
- The JMH numbers are one fork under load 7 to 13; the 5.3 gate pair against the 2.3 baseline is
  the measurement. The before jar is the reset tree before any edit of this packet; both jars were
  built from this worktree.
- `MarshallingProvider.funcSystemProvider` is built at class initialization; an unknown engine or
  profile in the properties now fails that initialization with `ExceptionInInitializerError` (the
  propagation run shows it), which is the "fails when the provider is built" of D3 at its earliest
  point.

=== END REPORT RX-WIRE-fable ===
