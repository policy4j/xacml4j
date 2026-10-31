## ADDED Requirements

### Requirement: Engine SPI for the regexp-match functions
The six XACML `*-regexp-match` functions SHALL evaluate through a
regular-expression engine that compiles a pattern once, in a named semantic
profile, into an immutable compiled pattern and reports whether any substring
of the input matches it. Engines SHALL be found by name through
`ServiceLoader`. The default SHALL be engine `jdk` with profile `legacy`.

#### Scenario: Substring match without anchors
- **GIVEN** the pattern `read|write` and the input `may write`
- **WHEN** `string-regexp-match` is evaluated with `jdk`/`legacy`, `jdk`/`xf`, and `re2j`/`xf`
- **THEN** each result is true (`RegexEngineContractTest`)

#### Scenario: Unknown engine or profile
- **GIVEN** a function provider built with engine `none`, or with engine `re2j` and profile `legacy`
- **WHEN** the provider is built
- **THEN** it fails with an error naming the engine or profile; no function is created (`RegexEngineSelectionTest`)

#### Scenario: Duplicate engine name
- **GIVEN** two engine providers named `jdk` on the class path
- **WHEN** the engine `jdk` is resolved
- **THEN** resolution fails with an error naming both providers (`RegexEngineSelectionTest`)

### Requirement: Selection reaches policy loading
The engine and profile chosen for a policy repository SHALL be the ones that
evaluate the regexp-match functions of every policy that repository loads,
including policies imported through `PolicyImportTool`.

#### Scenario: Two repositories
- **GIVEN** one repository with `jdk`/`legacy` and one with `jdk`/`xf`, each holding a policy that matches `\d` against U+0663
- **WHEN** a request is evaluated against each
- **THEN** the first policy does not match and the second does (`RegexEngineSelectionTest`)

### Requirement: The legacy profile keeps today's results
`jdk`/`legacy` SHALL give the result, status code, and status message that
`RegExpFunctions` gives before this change for every case of the
characterization corpus, except the cases the status matrix changes. The
public static entry points of `RegExpFunctions` and its system properties
SHALL keep working.

#### Scenario: Characterization corpus
- **GIVEN** the corpus recorded by `RegExpCharacterizationTest` before the change
- **WHEN** each case is evaluated with `jdk`/`legacy` after the change
- **THEN** every result, status code, and message equals the recorded one, except the cases `RegexStatusMatrixTest` lists as changed (`RegExpCharacterizationTest`, `RegexStatusMatrixTest`)

### Requirement: The xf profile is strict XF 1.0
The `xf` profile SHALL accept exactly the regular-expression grammar of XF 1.0
section 7.6.1 (23 January 2007) plus the deviations the maintainer approves,
SHALL use the Unicode data of the running JDK for categories and blocks, and
SHALL give equal results on `jdk` and `re2j` for every case of the contract
corpus.

#### Scenario: Profile differences
- **GIVEN** `\d` with U+0663, `\w` with `_`, and `(?i)a` with `A`
- **WHEN** each is evaluated with `jdk`/`legacy` and with `jdk`/`xf`
- **THEN** `legacy` gives today's results (false, true, true) and `xf` gives true, false, and a syntax failure (`RegexProfileDifferenceTest`)

#### Scenario: Classes are not misread
- **GIVEN** the patterns `[a-z-[aeiou]]`, `[$]`, `[a^]`, `[\d_]`, and `[\i-[:]]`
- **WHEN** each is evaluated with `jdk`/`xf` and `re2j`/`xf` on the contract inputs
- **THEN** both engines give the same results, which the XF class rules define (`RegexEngineContractTest`)

#### Scenario: Unicode after 6.0
- **GIVEN** a code point assigned after Unicode 6.0 in category `Lo`
- **WHEN** `\p{Lo}` is matched with `jdk`/`xf` and `re2j`/`xf`
- **THEN** both return true (`RegexEngineContractTest`)

### Requirement: Compilation is bounded before it allocates
For the `xf` profile, a pattern whose estimated expanded program, emitted
length, or any repetition count exceeds its configured bound SHALL be rejected
as a structural limit before the native engine compiles it.

#### Scenario: Count bound only in xf
- **GIVEN** the patterns `a{1000}` and `a{1001}`, and `(?:){1000}{1001}` with the empty input
- **WHEN** each is evaluated with `jdk`/`xf` and with `jdk`/`legacy`
- **THEN** in `xf` `a{1000}` compiles and `a{1001}` fails as a structural limit; in `legacy` all three keep today's results (`RegexEngineContractTest`, `RegExpLimitsTest`)

#### Scenario: Many allowed repetitions
- **GIVEN** a concatenation of `a{1000}` terms, each within the count bound, whose total estimate exceeds the cap
- **WHEN** it is compiled by `re2j`
- **THEN** compilation fails as a structural limit and the native compiler is not called (`Re2jEngineTest`, counting compiler)

### Requirement: Statuses and messages
Each failure SHALL give the status code and message of the status matrix:
syntax-error with a fixed message for a pattern invalid in its profile;
processing-error naming the construct and engine for a valid pattern the
engine cannot express; processing-error with the fixed limit message for every
limit and for an `Error` thrown by an engine. No message SHALL contain the
pattern or the input.

#### Scenario: Matrix
- **GIVEN** one case per row: `(` (malformed), `(a)\1` on `re2j` (unsupported), a pattern over the length limit, an injected engine `StackOverflowError`
- **WHEN** each is evaluated
- **THEN** the codes are syntax-error, processing-error, processing-error, processing-error, and the messages are the matrix messages (`RegexStatusMatrixTest`)

### Requirement: Compiled patterns are reused
A literal pattern argument of a `Match` or an `Apply` SHALL be compiled once
per call site, engine, and profile after it compiles successfully; other calls
SHALL reuse compiled patterns from a bounded cache per engine instance. Only
deterministic failures (malformed, unsupported, structural limit) SHALL be
kept; a deadline or resource failure SHALL NOT be kept.

#### Scenario: Literal pattern in a target
- **GIVEN** a policy whose target matches `string-regexp-match` with a literal pattern
- **WHEN** 1,000 requests are evaluated
- **THEN** the engine compiles the pattern once (`RegexCompileOnceTest`, counting engine)

#### Scenario: Transient failure is retried
- **GIVEN** a literal pattern whose first compilation fails on the deadline and whose second does not
- **WHEN** two requests are evaluated
- **THEN** the first result is Indeterminate with the limit status and the second is the match result (`RegexCompileOnceTest`)

#### Scenario: Higher-order call
- **GIVEN** conformance case IIC168, where patterns reach `any-of-all` through a bag
- **WHEN** it is evaluated twice
- **THEN** the second evaluation compiles nothing and the decision is unchanged (`RegexCompileOnceTest`)

### Requirement: Benchmarks per engine
`xacml-jmh` SHALL provide `RegexFunctionBenchmark` (compile and match per
engine and profile, wrapped and unwrapped deadline cases for `re2j`, full
function calls with cache hits and misses) and a regex corpus for
`PdpDecideBenchmark`; every implementation group SHALL record a JMH
before/after pair.

#### Scenario: Benchmark setup
- **GIVEN** `RegexFunctionBenchmark` with every engine and profile parameter
- **WHEN** its setup runs
- **THEN** every pattern gives its expected result on every engine and profile, and the counting hooks confirm what each case measures (`RegexBenchmarkTest`)
