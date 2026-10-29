## Context

The six XACML `*-regexp-match` functions (XACML 3.0 Plus Errata 01, A.3.13)
live in `xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/RegExpFunctions.java`.
Every call translates the pattern to `java.util.regex` syntax, scans its
repetition weight, compiles it, and runs `Matcher.find()` behind a deadline
wrapper. A per-thread pattern cache exists but runs only when the property
`...cacheRegExps` is `true`; it is `false` by default (`RegExpFunctions.java:178`,
`:396`). The bead 4.58 limits (pattern length, input length, deadline,
repetition weight) are JVM-global, read from system properties at class load;
every limit yields processing-error with one fixed message. A malformed
pattern yields processing-error without a message. The class also exposes
public static entry points: `matches(String, String)`,
`covertXacmlToJavaSyntax(String)`, and the six function methods.

The maintainer asked on 2026-10-26 for a regular-expression SPI with a JDK
engine and an RE2/J engine (linear time, no backtracking).

The discovery swarm (research RX1 to RX6, 2026-10-27) found:

- XACML A.3.13 uses `fn:matches` of XF 1.0 (23 January 2007): substring search,
  `^` and `$` anchor the string, no flags; not the XML Schema `pattern` facet (RX1).
- Today's translation drifts from XF in `.`, `\s`, `\d`, `\w`, `\c`,
  negative-group subtraction, and `&&`, and accepts Java-only syntax such as
  `(?i)` (RX1, by source reading).
- RE2/J needs translation for `.`, `\s \d \w \i \c`, block escapes, `\p{Cn}`,
  `\p{C}`, subtraction; five constructs are misread silently; a validating
  parser is needed. Its tables are Unicode 6.0; JDK 25 is 16.0 (RX1, RX5).
- RE2/J expands counted repetition at compile time with no program-size guard;
  `Matcher.programSize()` reports an already compiled program (RX1, RX2).
- Conformance uses `string-regexp-match` in 9 cases per OASIS suite with ASCII
  patterns only; RSA 2008 has none (RX3).
- No other open-source XACML engine has an SPI, a linear-time engine, or
  ReDoS limits; AuthzForce compiles constant patterns at policy load (RX6).
- Compiled patterns are shared, matching state is per call; a literal's
  compiled form belongs to its call site (RX4).
- The plan review (rv001 Astra, rv002 Agy) showed that today's semantics and
  strict XF semantics cannot both hold for one engine pair, and that the
  repository's `FunctionProvider` never reaches policy unmarshalling: the
  field of `AbstractPolicyRepository` is not read, and `PolicyImportTool`
  calls `MarshallingProvider.newPolicyUnmarshaller()`, which uses the static
  `funcSystemProvider`.

## Goals / Non-Goals

**Goals:**

- One SPI for compiling an XACML pattern once and testing whether any substring
  of an input matches, with today's limits.
- Two semantic profiles, named and tested: `legacy` (today's results) and `xf`
  (strict XF 1.0, the edition A.3.13 cites).
- Engine `jdk` with both profiles, `legacy` by default; engine `re2j` in
  `xacml-regex-re2j` with the `xf` profile only.
- Compiled patterns reused across calls; compilation bounded before it
  allocates.
- JMH evidence per engine, with controls that isolate each claimed cost.

**Non-Goals:**

- Changing the default profile in this change (Open Question 2).
- Selecting an engine per request or per policy.
- Other string functions.

## Research Basis

Research: REQUIRED. `research/README.md` indexes Q1 to Q5 and lanes RX1 to
RX6; `research/synthesis.md` records adoption and disagreements; the plan
reviews are in `evidence/reviews/plan/`.

## Decisions

### D1. SPI in `xacml-core`

Package `org.xacml4j.v30.policy.function.regex`:

- `RegexEngine`: `name()`, `profiles()`, and
  `compile(String pattern, RegexProfile profile, RegexLimits limits)`
  returning a `CompiledRegex` or throwing `RegexCompileException` with a kind
  from the status matrix (D5).
- `CompiledRegex`: `boolean find(CharSequence input, RegexBudget budget)`,
  a fresh search from the start of the input on every call; immutable, safe to
  share; matching state stays inside the call.
- `RegexEngineProvider` (ServiceLoader, `@AutoService`): name and factory; two
  providers with one name fail when the engine is resolved.
- `RegexLimits` (immutable, per engine instance; the `jdk` defaults come from
  today's system properties, D7) and `RegexBudget` (one operation; deadline).
- `XacmlPattern`: a validating parser of the `xf` profile's grammar to a
  syntax tree with a size estimate (D4). The `legacy` profile does not use it.

### D2. Semantic profiles

- `legacy`: today's translation and `java.util.regex` behavior, including the
  drifts and the Java-only syntax it accepts. Only the `jdk` engine offers it.
  Its results, statuses, and messages are the characterization corpus,
  except the approved changes of the status matrix (D5).
- `xf`: XF 1.0 section 7.6.1 as published on 23 January 2007, which A.3.13
  cites: `.` matches every character except LF; no non-capturing groups; no
  flags; classes with subtraction; `\p{IsX}` blocks; categories of the JDK's
  Unicode data (16.0 on JDK 25). Later-edition behavior (`.` also excluding
  CR, `(?:...)`) is a deviation the maintainer approves or rejects (Open
  Question 5). Both engines offer `xf`, and give equal results on every
  pattern and input of the contract corpus.
- The difference matrix between `legacy` and `xf` is a table of test cases
  (`RegexProfileDifferenceTest`): `\d` on U+0663, `\w` on `_` and `+`, `.` on
  CR, U+0085, U+2028, U+2029, negative-group subtraction, `&&`, `(?i)a` on
  `A`, `\c`, and `(?:a)`.

### D3. Engine selection and wiring (Open Question 1)

Proposed (RX2 option C with D): the engine and profile are constructor
arguments of an instance-based regexp function provider, chosen on
`FunctionProviderBuilder` and `PolicyRepository.Builder`; the default is
`jdk`/`legacy`, or the values of `org.xacml4j.regex.engine` and
`org.xacml4j.regex.profile` read when the provider is built. This needs
wiring that does not exist: `AbstractPolicyRepository` and `PolicyImportTool`
pass the repository's `FunctionProvider` to
`MarshallingProvider.newPolicyUnmarshaller(FunctionProvider, ...)`, and the
conformance runner and `PdpFixture` use that path. An unknown name, a profile
the engine does not offer, or `re2j` without its module fails when the
provider is built. Alternative (RX4): per PDP through an `EvaluationContext`
accessor; it changes a public interface and the mock contexts of the function
tests, and the engine is unknown at policy load. Rejected: selection by the
request, separate function identifiers per engine, silent fallback from
`re2j` to `jdk`.

### D4. Translation and compile-size bound (`re2j`, `xf` profile)

- Every character class is computed as one code-point set (ranges, escapes,
  categories, blocks, subtraction, negation) and emitted as one flat class;
  no nested brackets reach RE2/J. `^` and `$` map to `\A` and `\z` only
  outside classes; inside a class they are escaped literals.
- Back-references and constructs outside the profile are rejected before
  RE2/J sees the pattern.
- Before emission, `XacmlPattern` computes an overflow-safe estimate of the
  expanded program: concatenation and alternation add, a count `{n,m}`
  multiplies by `m` (by `n` plus one for `{n,}`), nesting multiplies, and each
  class adds its range count. A pattern whose estimate or emitted length
  exceeds the cap is rejected as a structural limit before native compilation
  allocates. In the `xf` profile, a count above 1000 is the same structural
  limit (as RE2/J and Go bound it). The `legacy` profile keeps today's
  repetition weight and no count bound: `(?:){1000}{1001}` still matches the
  empty input there (`RegExpLimitsTest`). `Matcher.programSize()` after
  compilation is a second check.

### D5. Statuses and messages (Open Question 3)

| Case | `legacy` today | Proposed `legacy` and `xf` |
| --- | --- | --- |
| Pattern invalid in its profile's grammar | processing-error, no message | syntax-error, fixed message without the pattern (XACML 3.0 7.19.2, B.8) |
| Valid in the profile, but the engine cannot express it | not applicable | processing-error, message names the construct and the engine, never the input |
| Limit: length, weight, deadline (both profiles); count above 1000 and compile size (`xf` only, new) | processing-error, fixed message | unchanged; the `xf`-only limits are new and change no `legacy` result |
| `Error` from an engine (`StackOverflowError`, JDK-5050507) | processing-error, no message | processing-error, the fixed limit message |

The two rows that change (malformed, engine `Error`) are **BREAKING** for a
client that reads status codes or messages. `RegExpCharacterizationTest`
records today's values as history; the acceptance test
`RegexStatusMatrixTest` asserts the matrix as approved, and lists every
recorded case it changes. The engine-`Error` case uses an injected engine, so
the test does not depend on the JVM's stack size.

### D6. Compile once

- A `Match` or `Apply` whose pattern argument is a literal string binds the
  compiled pattern at its call site on its first evaluation (not on the shared
  `FunctionSpec`; no expression is evaluated at load).
- Only deterministic outcomes are kept at the call site: a compiled pattern,
  or a malformed, unsupported, or structural-limit failure. A deadline or
  resource failure is not kept; the next evaluation compiles again.
- Other calls (dynamic patterns, higher-order functions such as IIC168's
  `any-of-all`) use a bounded cache per engine instance, keyed by profile and
  pattern: 256 entries by default, no global lock while compiling, the same
  rule for what is kept.
- The `...cacheRegExps` property is accepted and ignored with a warning; the
  cache is on for every call (**BREAKING** only for code that relied on
  recompilation).

### D7. Compatibility of the public entry points

`RegExpFunctions.matches(String, String)`, `covertXacmlToJavaSyntax(String)`,
and the six function methods stay, delegating to `jdk`/`legacy`. Today's
system properties (`maxPatternLength`, `maxInputLength`, `maxMatchMillis`,
`maxRepetition`, `cacheSize`) stay the defaults of the `jdk` engine's
`RegexLimits`, read when the provider is built.

### D8. Module and dependency

`xacml-regex-re2j` with `com.google.re2j:re2j` 1.8 (BSD-3, no runtime
dependencies, Java 6 bytecode; runs on the JDK 25 baseline). The offline
repository holds 1.7; 1.8 needs one online build. Upstream issues 168
(compile loop with case-insensitive Unicode) and 198 do not apply to the
`xf` profile, which has no flags; tests pin that `(?i)` is rejected there.

## Test Strategy

- History: `RegExpCharacterizationTest` records today's results, statuses, and
  messages for a corpus (conformance patterns, the RX1 table rows, the drift
  cases, the limit cases of `RegExpLimitsTest`, an injected engine `Error`).
- Acceptance: `jdk`/`legacy` equals the history except the approved rows of
  D5; `RegexProfileDifferenceTest` (D2); `RegexEngineContractTest` runs one
  table against `jdk`/`xf` and `re2j`/`xf` with equal outcomes, including
  class edge cases (`[$]`, `[a^]`, `[\d_]`, `[\i-[:]]`) and Unicode cases
  assigned after 6.0.
- Bounds: an oversized concatenation of individually allowed repetitions is
  rejected before a counting native-compiler delegate runs (D4).
- Reuse: a transient compile failure followed by a timely one succeeds; a
  malformed literal stays rejected (D6).
- Conformance: the OASIS and RSA suites with `jdk`/`legacy`, `jdk`/`xf`, and
  `re2j`/`xf`.

## Benchmark Plan

- `RegexFunctionBenchmark` (task 2.2 for today's functions; engine and profile
  parameters added with the engines): compile and match measured separately;
  for `re2j`, wrapped and unwrapped deadline cases with the same compiled
  patterns and inputs, to isolate the wrapper cost; full function calls for
  cache hits and misses, with counting hooks that prove what each case
  includes; inputs of 16, 256, and 4,096 characters; conformance and
  adversarial patterns.
- `PdpDecideBenchmark` with a regex corpus: results are warmed decisions,
  because `PdpFixture.open()` evaluates every request before it returns.
  No first-use cost is claimed from the PDP benchmark; compilation cost comes
  from the compile mode of `RegexFunctionBenchmark`.
- Every group records a before/after pair; lanes stay idle during JMH runs.

## Code Intelligence Plan

ripwire `--impact` on `RegExpFunctions.matches` and on the function provider
registration (`XacmlDefaultFunctions`), Serena references for
`FunctionProviderBuilder`, `PolicyRepository.Builder`, and
`MarshallingProvider.newPolicyUnmarshaller`, and an ast-grep rule that keeps
`Pattern.compile` and RE2/J compilation inside the engines.

## Execution Plan

Groups in `tasks.md`: plan review and approval; characterization and baseline;
SPI, profiles, and the `jdk` engine; provider wiring and selection; compile
once; the `re2j` engine; close out. Writers get disjoint files; reviewers come
from other model families than the author.

## Risks / Trade-offs

- Translation gaps silently change a decision: mitigated by the validating
  parser, flat classes, differential tests, and rejection of every construct
  without a tested translation.
- Unicode version skew: mitigated by generating `xf` ranges from the JDK's data.
- Compile-size estimate too strict or too loose: mitigated by the second check
  after compilation and by tests at the bound.
- Cache memory: bounded in entries, not bytes; RE2/J keeps matcher machines up
  to peak concurrency (RX4).
- Wiring the repository's function provider into unmarshalling changes which
  provider a repository's policies use; today they all use the static default.

## Migration and Rollback

The default stays `jdk`/`legacy`; `xf` and `re2j` are opt-in. Rollback is to
select `jdk`/`legacy`. Changes visible without opting in: the D5 rows and the
D6 cache property, both waiting for the maintainer.

## Open Questions

1. Engine selection: per function provider and repository (D3) or per PDP?
2. Default profile: keep `legacy` (this change), or make `xf` the default
   later in its own change with the decision changes listed?
3. Statuses: adopt the D5 matrix, or keep today's statuses?
4. Defaults: compile-size cap (estimate and emitted length) and count bound
   1000, both for the `xf` profile on both engines only (`legacy` keeps
   today's repetition weight); cache 256 entries per engine instance (all
   engines and profiles).
5. `xf` grammar edition: strict XF 1.0 (2007), or accept later-edition
   behavior (`.` excluding CR, `(?:...)`) as approved deviations?
