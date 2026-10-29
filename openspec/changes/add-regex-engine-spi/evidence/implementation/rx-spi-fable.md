=== BEGIN REPORT RX-SPI-fable ===

FINAL REPORT (policy4j_implementer_claude_fable, Claude Fable 5.1)

Worktree `.ntm/worktrees/xacml4j--batch2/cc_2`, branch `ntm/xacml4j--batch2/cc_2`, HEAD `87133237`,
on the main working tree as reset by the conductor (cycles AM, AN, AP and FIX-AI2 merged), not reset
since; the tree also holds FIX-AN-fable, merged into main by the conductor (files disjoint from this
packet's). Change `add-regex-engine-spi`, tasks 2.1, 3.1 and 3.2, resumed after the FIX-AN pause
with the conductor's note on the `rules:<n>` corpus of task 2.2. Main's `RegExpFunctions.java` and
`RegExpLimitsTest.java` are unchanged against HEAD (`rtk git -C <main> status`), and its
`DirectPairCallTest.java` is identical to this tree's, so cycles AQ, AR and FIX-CYCLE2 touched none
of this packet's files. Beads: `policy4j-eufz.2.1` claimed (in_progress); `br` refused the claims of
3.1 ("cannot claim blocked issue: policy4j-eufz.2.3", the baseline JMH run) and of 3.2 (blocked by
3.1), on 2026-10-28 and again when I resumed, so the conductor claims or unblocks them.

## Summary

The three tasks are done and green. `RegExpCharacterizationTest` records today's outcomes for 116
rows and passed on the code before any production change (12:17:09); it passes after the change
with the 15 rows that design D5 changes carrying their approved outcome, and `RegexStatusMatrixTest`
lists exactly those rows. The SPI of D1 is in `org.xacml4j.v30.policy.function.regex`, with
`XacmlPattern` (the strict XF 1.0 grammar of D2, flat classes and the size estimate of D4), the
`jdk` engine in `regex.jdk` with the `legacy` profile (today's translation and limits, moved) and the
`xf` profile (count bound 1000, program-size cap), the D5 statuses in `RegExpFunctions`, and the
compatible entry points and properties of D7. Compile-once (D6) and the provider wiring (D3) are
not built; the SPI fits them: a `CompiledRegex` is immutable and shareable, its matcher state is
per call, and `RegexLimits` carries the cache size. The `rules:<n>` corpus of task 2.2 (Claude Opus,
in main, not in this tree before) pinned today's status of its `regex-malformed` request; with D5
its expected result is syntax-error with the fixed message, the one line Opus named plus the two
README lines that described it, and the PDP returns exactly that at every rule count the corpus
tests use. Reactor with `legacy` and that corpus: BUILD SUCCESS, core 7,188 (2 skipped, 429 more
than before), xml 254, json 24 (1 skipped), PDP 506 (3 skipped), test support 99, conformance 1,818
(30 skipped: RSA 17, XACML 2.0 suite 375 with 7 skipped, XACML 3.0 suite 488 with 5 skipped,
examples 2), JMH 225 (task 2.2's 12 included). No conformance case changes its status under D5; the
existing tests whose exercised status changes are a row of `DirectPairCallTest`, whose assertion
compares two invocation paths and holds, and the `regex-malformed` request of the JMH corpus,
changed as the conductor asked.

## Changes

### Task 2.1, `RegExpCharacterizationTest` (impl package, one parameterized table, 116 rows)

Through the registered `string-regexp-match` function: the nine conformance patterns of RX3 with
inputs of their requests, the 23 rows of the RX1 table, the drifts D1 to D8, the limit cases of
`RegExpLimitsTest` with the default limits (10,001-character pattern, 100,001-character input, the
weight cases, the hidden repetitions, comments mode, the catastrophic deadline case), and an
`Error` of the engine: `(a|b)*` on 100,000 characters, which overflows the stack of
`java.util.regex` in milliseconds on this JDK (JDK-5050507; probed on the main thread and on a
256 KiB thread). Every row passed on today's code at the first run, so the predictions of RX1 held,
including D1 (`\c+` matches `k`, not `a`), D5, D6 and D8. A row D5 changes keeps today's outcome
(`today`) and carries the approved one (`approved`), which is asserted; `changedByD5()` gives them
to the matrix test.

### Task 3.1, the failing tests (all green after 3.2)

- `RegexEngineContractTest` (regex package): 69 rows, each with its outcome in `legacy` and in `xf`,
  run against every engine on the class path that offers the profile (138 cases on `jdk`); the
  substring, anchor, class edge (`[$]`, `[a^]`, `[\d_]`, `[\i-[:]]`, `[]a]`, `[--z]`), subtraction,
  Unicode-after-6.0 (`\p{Lo}` on U+0860), count-bound and Java-only-syntax rows of the spec and the
  design. A `re2j` engine runs the same table once task 6.2 registers it.
- `RegexProfileDifferenceTest`: the D2 matrix on `jdk`, 17 rows (`\d` on U+0663, `\w` on `_` and
  `+`, `.` on CR, U+0085, U+2028, U+2029, negated subtraction, `&&`, `(?i)a`, `\c+`, `(?:a)`, `\s`
  on VT, `\i`, `\p{IsPrivateUse}`, `a{1001}`).
- `RegexStatusMatrixTest` (impl package): the four rows of D5 through `RegExpFunctions.Matching`,
  the unsupported row with an injected engine, the engine `Error` row with an injected engine whose
  `find` throws `StackOverflowError`, the `xf`-only limits, and the list of the 15 characterization
  rows the matrix changes.
- `XacmlPatternTest`: 147 cases: 46 accepted and 63 rejected patterns of the grammar, the tree,
  the back-reference digit rule, flat classes (subtraction after negation, `\w` with `+` and
  without `_`, `C` without Cs, `.` without LF only, blocks as one range), the size estimate and
  its saturation, the largest count, and the Appendix F block names.
- `LegacySyntaxTest` (regex.jdk): the scanner and quoting assertions of rv087, moved from
  `RegExpLimitsTest` with the code (9 tests); `RegExpLimitsTest` keeps the function-level limit
  tests on `RegexLimits` (18 tests).

### Task 3.2, the SPI, `XacmlPattern`, the `jdk` engine, the entry points

New types and methods (`org.xacml4j.v30.policy.function.regex` unless said):

- `RegexEngine`: `name()`, `profiles()`, `offers(RegexProfile)` (default), `compile(String pattern,
  RegexProfile, RegexLimits)`.
- `CompiledRegex`: `find(CharSequence input, RegexBudget)`: a fresh search from the start, the
  budget read while the input is read and after the search.
- `RegexEngineProvider`: `name()`, `create()`; found with `ServiceLoader`; `JdkRegexEngineProvider`
  is registered with `@AutoService`.
- `RegexEngines`: `engine(String name)` (unknown name: `IllegalArgumentException` listing the
  names; two providers of one name: `IllegalStateException` naming both), `names()`.
- `RegexProfile`: `LEGACY`, `XF`; `id()`, `of(String)`.
- `RegexLimits`: `defaults()`, `builder()`, `toBuilder()`; `maxPatternLength()`,
  `maxInputLength()`, `maxMatchMillis()`, `maxMatchNanos()`, `maxRepetition()` (legacy weight),
  `maxRepeatCount()` (xf, 1000), `maxProgramSize()` (xf, 100,000), `cacheSize()` (256); the
  constants `DEFAULT_*`, `LARGEST_MAX_PATTERN_LENGTH`, `LARGEST_CACHE_SIZE` and the validation
  messages of today's `Limits`.
- `RegexBudget`: `start(RegexLimits)`, `start(long nanos, LongSupplier clock)`, `expired()`,
  `check()`; `LIMIT_EXCEEDED_MESSAGE`.
- `RegexException` (`kind()`, `Kind {MALFORMED, UNSUPPORTED, LIMIT}`), `RegexCompileException`
  (`malformed(String)`, `malformed(String, Throwable)`, `unsupported(String construct, String
  engine)`, `limit(String)`, `limit(Error)`), `RegexLimitException`.
- `XacmlPattern`: `parse(String)`, `source()`, `root()`, `groups()`, `largestCount()`,
  `sizeEstimate()`, `hasBackReferences()`, `codePoints(ClassGroup)`, `codePoints(ClassItem)`,
  `UNBOUNDED`; the sealed `Node` records `Alternation`, `Sequence`, `Repeat`, `Group`,
  `BackReference`, `Literal`, `Anchor`, `CharClass(ClassGroup, CodePointSet)`; `ClassGroup(negated,
  items, subtracted)` and the sealed `ClassItem` records `CodePointRange`, `CategoryEscape`,
  `BlockEscape`, `MultiCharEscape`, `Wildcard`.
- `CodePointSet`: `of(int)`, `of(int, int)`, `builder()`, `contains`, `isEmpty`, `rangeCount`,
  `lo(i)`, `hi(i)`, `size`, `union`, `complement`, `intersect`, `minus`; `EMPTY`, `ALL`.
- `UnicodeSets` (package-private): categories and blocks scanned once from `Character.getType`
  and `Character.UnicodeBlock.of`, the XML 1.0 name characters, `\s`, `\d`, `\w`.
- `regex.jdk.JdkRegexEngine`: `NAME`, `compile` (:68), `toLegacyJavaSyntax(String)` (:85);
  package-private `LegacySyntax` (today's translation, quoting and repetition scanner, moved
  unchanged with the Sun notice), `XfSyntax` (the emitter), `DeadlineInput` (the clock every 1,024
  reads, as today).
- `RegExpFunctions`: the six functions unchanged; `matches(String, String)` and
  `covertXacmlToJavaSyntax(String)` public as before (:291, :316); package-private `limitsFrom`
  (:194, today's property names and validation), `cacheSize`, `matches(..., RegexLimits[,
  LongSupplier])`, `Matching` (:328: engine, profile, limits, and the D5 statuses), `PatternCache`
  of `CompiledRegex` (:242; the per-thread cache behind `.cacheRegExps`, kept as today until D6);
  public `INVALID_PATTERN_MESSAGE` (:160, "Regular expression pattern is invalid"), the one new
  public member of the class, so that the JMH corpus and a client that reads the status pin the
  same text (the limit message is public as `RegexBudget.LIMIT_EXCEEDED_MESSAGE`).

### The `rules:<n>` regex corpus of task 2.2 under D5 (the conductor's note)

Task 2.2 (Claude Opus) is in main's `xacml-jmh` and was not in this tree: `RulesCorpus.java`,
`RulesCorpusTest.java`, `BenchmarkSmokeTest.java`, `README.md`, and the new
`regex/RegexFunctionBenchmark.java` and `regex/RegexBenchmarkTest.java`. I copied the six files from
main (`diff -rq` of the two `xacml-jmh/src` trees: identical before my edit) and changed:

- `RulesCorpus.java`: the expected result of `regex-malformed` (:206-209), from
  `Status.processingError().build()` to
  `Status.syntaxError().message(RegExpFunctions.INVALID_PATTERN_MESSAGE).build()`, with the import
  (:52). The fixture compares the whole `Result`, status message included
  (`CorpusRequest.mismatch`, `Status.equals`), so the pinned message is the fixed one, not a
  literal copy. Against main the file differs by those 3 lines only (`git diff --no-index --stat`).
- `README.md`: the `regex-malformed` row of the rules table (:173, "status syntax-error with the
  message `Regular expression pattern is invalid`") and the sentence that said the expected status
  was today's until D5 (:177-179).
- The other four files are byte-identical to main's; they are in this tree so that the reactor
  runs task 2.2's tests on the D5 code.

Why the PDP returns that status: `RegExpFunctions.status` throws an `EvaluationException` with the
syntax-error status, `FunctionInvocationException.statusFor` keeps the first `EvaluationException`
status of the cause chain (`FunctionInvocationException.java:61-65`), each of the `n` Permit rules
ends Indeterminate{P} with it, `deny-overrides` keeps Indeterminate{P}, and
`DefaultPolicyDecisionPoint.createResult` takes `context.getEvaluationStatus()` (:297).
`RulesCorpusTest` (counts 1, 2, 3, 8 and 100, every request checked when the fixture opens) and
`PdpFixtureTest` pass with the new expectation. I did not run the old expectation on the D5 code;
it cannot pass, because `Status.equals` compares the code and the message (`Status.java:183-193`)
and the fixture throws on any mismatch (Opus's report: "the fixture fails at setup, which is the
intended alarm").

What the `xf` profile is (D2, strict XF 1.0 of 23 January 2007, fetched): the XML Schema Part 2
Appendix F grammar with `^` and `$` as metacharacters, `\$` as an escape, reluctant quantifiers,
back-references, no flags, no `(?:`; `.` excludes the line feed only; braces are metacharacters;
`Char` excludes `^` and `$`. Interpretations, each a parser decision to review: `\i` and `\c` are
the name characters of XML 1.0 Fifth Edition (XACML's [XML]), not the XML 1.0 Second Edition
tables XSD 1.0 cites; `C` is Cc, Cf, Co and Cn without Cs, as Appendix F says; a block name is one
`Character.UnicodeBlock.forName` knows (case-insensitive, so lenient to XSD) plus `PrivateUse`
(E000 to F8FF, as Appendix F lists it), an unknown block is malformed; a back-reference inside a
class is malformed (the 2007 production allows the token, gives it no meaning, and later editions
forbid it); a back-reference takes further digits while the number stays within the groups opened
so far and must name a closed group; a hyphen stands for itself first or last only; a range must
be in order. The `jdk` engine emits `xf` in Java syntax: categories by their Java names (the data
is the data of the flat sets; `C` as `[\p{Cc}\p{Cf}\p{Co}\p{Cn}]`), blocks and name characters as
code-point ranges, `\s` as the four spaces, `\w` as `[^\p{P}\p{Z}\p{Cc}\p{Cf}\p{Co}\p{Cn}]`, `.` as
`[^\n]`, `[G-[S]]` as `[G&&[^S]]` and `[^G-[S]]` as `[[^G]&&[^S]]` (JDK 25 negates a whole class),
groups as `(?<gN>...)` with `\k<gN>` back-references, `$` as `\z`.

Limits (D4): in `xf`, a count above `maxRepeatCount` (1000) and an estimate or an emitted pattern
above `maxProgramSize` are structural limits before `Pattern.compile`; the estimate adds pieces and
branches, multiplies by the largest count (the smallest plus one without one), and counts a class as
its ranges. 100,000 is my default for the cap (the proposal fixed none): `\p{L}{2,30}` (about
21,000) compiles, `(a{1000}){1000}` does not. `legacy` keeps the repetition weight and no count
bound (`(?:){1000}{1001}` matches the empty input, `a{1001}` matches).

Statuses (D5, `RegExpFunctions.status`, :373): a `MALFORMED` compile failure is syntax-error with
"Regular expression pattern is invalid"; `UNSUPPORTED` is processing-error with "Regular expression
construct X is not supported by engine Y"; `LIMIT` and `RegexLimitException` are processing-error
with the fixed limit message; an `Error` is caught at the engine boundary (`JdkRegexEngine`, compile
and find) and again in `Matching` for engines of other implementations, as a limit. The JDK reports
a stack overflow while compiling as a `PatternSyntaxException`, so it is malformed, not a limit. The
detail of a malformed failure is `PatternSyntaxException.getDescription()`, never the pattern.

Status changes under D5 (the list the packet asks for):
- Conformance: none. The 9 regexp cases per OASIS suite and the two deprecated fixtures use valid
  patterns; both suites pass with `legacy` as before, same counts.
- `RegExpCharacterizationTest`: 15 rows, each from processing-error without a message: the malformed
  patterns `a{3,2}`, `a{x}`, `a**`, `*a`, `(`, `\i`, `\I`, `\C`, `\p{Greek}`, `\p{IsPrivateUse}`,
  `\p{IsNoSuchBlock}`, `[^]`, `[a`, `(?` to syntax-error with the fixed message, and the engine
  `Error` row `(a|b)*` on 100,000 characters to processing-error with the limit message.
- `DirectPairCallTest` (unchanged file): its `(` row invokes `string-regexp-match` on two paths and
  asserts equal outcomes; both are syntax-error now.
- `xacml-jmh` `rules:<n>` request `regex-malformed` (task 2.2, pattern `^[a-z0-9._+@example\.com$`):
  Indeterminate{P} as before, status from processing-error without a message to syntax-error with
  the fixed message; pinned in `RulesCorpus.java:206-209`, checked by `RulesCorpusTest`,
  `PdpFixtureTest` and every fixture open of the `rules` corpus. Changed in this tree, as asked.
- No other existing test pinned the old status (`rtk rg` over the test trees and `xacml-jmh` for
  the regexp functions and the status codes: the files above only).

Files. New: `xacml-core/src/main/java/org/xacml4j/v30/policy/function/regex/{RegexEngine,
CompiledRegex, RegexEngineProvider, RegexEngines, RegexProfile, RegexLimits, RegexBudget,
RegexException, RegexCompileException, RegexLimitException, XacmlPattern, CodePointSet,
UnicodeSets}.java`, `.../regex/jdk/{JdkRegexEngine, JdkRegexEngineProvider, LegacySyntax, XfSyntax,
DeadlineInput}.java`; tests `.../function/impl/RegExpCharacterizationTest.java`,
`.../function/impl/RegexStatusMatrixTest.java`, `.../function/regex/{XacmlPatternTest,
RegexEngineContractTest, RegexProfileDifferenceTest}.java`, `.../function/regex/jdk/LegacySyntaxTest.java`.
Changed: `.../function/impl/RegExpFunctions.java` (the function layer on the SPI; the translation
and scanner moved to `LegacySyntax`; `INVALID_PATTERN_MESSAGE` public), `.../function/impl/RegExpLimitsTest.java`
(on `RegexLimits`; the scanner assertions moved to `LegacySyntaxTest`). In `xacml-jmh`, against
main: `src/main/java/org/xacml4j/jmh/corpus/RulesCorpus.java` (3 lines: the import and the expected
result of `regex-malformed`) and `README.md` (the row and the sentence); the other four task 2.2
files are copies of main's. `git diff --check`, a trailing-whitespace search of the edited files,
and `ast-grep scan` clean. No `openspec/` or `.beads/` file edited.

## Verification

```
JAVA_HOME="$(jenv prefix 25)" rtk mvn -o -Dlicense.skipUpdateLicense=true -pl xacml-core -am -Dtest=RegExpCharacterizationTest -Dsurefire.failIfNoSpecifiedTests=false test
JAVA_HOME="$(jenv prefix 25)" rtk mvn -o -Dlicense.skipUpdateLicense=true -pl xacml-core -am -Dtest='RegExpCharacterizationTest,RegExpLimitsTest,LegacySyntaxTest,XacmlPatternTest,RegexEngineContractTest,RegexProfileDifferenceTest,RegexStatusMatrixTest,RegularExpressionFunctionsTest,FunctionSafetyNet43Test,DirectPairCallTest,HigherOrderFunctionsTest,FunctionProviderServiceLoaderTest' -Dsurefire.failIfNoSpecifiedTests=false test
JAVA_HOME="$(jenv prefix 25)" rtk mvn -o -Dlicense.skipUpdateLicense=true -pl xacml-conformance-tests,xacml-jmh -am test
JAVA_HOME="$(jenv prefix 25)" rtk mvn -o -Dlicense.skipUpdateLicense=true -pl xacml-jmh -am -Dtest='RulesCorpusTest,PdpFixtureTest,RegexBenchmarkTest,BenchmarkSmokeTest' -Dsurefire.failIfNoSpecifiedTests=false test
```

The JMH module's tests take the JMH lock, so each run waited until no other lane's JMH process was
alive (`pgrep -f 'org.openjdk.jm[h]|benchmarks.ja[r]'`). Logs: `rxspi-reactor.log`,
`rxspi-jmh-focused.log`, `rxspi-reactor-2.log` in this session's scratchpad.

| Run | Result |
| --- | --- |
| 2.1 on today's code, before any production change (12:17:09) | `RegExpCharacterizationTest` 116 tests, 0 failures |
| 3.1 and 3.2, regexp tests after the change (12:24:40) | 858 tests, 0 failures: characterization 116, limits 18, legacy syntax 9, pattern 147, contract 138, difference 17, matrix 5, and the five existing regexp tests |
| reactor, SPI code without task 2.2's files (12:33) | BUILD SUCCESS: core 7,188 (2 skipped), xml 254, json 24 (1 skipped), PDP 506 (3 skipped), test support 99, conformance 1,818 (30 skipped; RSA2008InteropTest 17, Xacml20ConformanceTest 375 with 7 skipped, Xacml30ConformanceTest 488 with 5 skipped, XacmlExampleTest 2), JMH 213 |
| JMH module, task 2.2's files with the D5 expectation (12:56:18 to 12:57:03) | BUILD SUCCESS: 51 tests, 0 failures (RulesCorpusTest 7, RegexBenchmarkTest 11, PdpFixtureTest 29, BenchmarkSmokeTest 4) |
| reactor, final tree (12:58:08 to 12:59:51) | BUILD SUCCESS: core 7,188 (2 skipped), xml 254, json 24 (1 skipped), PDP 506 (3 skipped), test support 99, conformance 1,818 (30 skipped; the same classes and counts), JMH 225 (0 failures) |

Before that last run the regexp tests failed twice on test harness details only (a CSV delimiter
inside a pattern, blank display names, a vertical tab that `@CsvSource` turns into null, one block
alias the JDK spells otherwise) and once on a wrong row of mine (`[a-\]]` is an inverted range and
the grammar rejects it, as it should).

## Residual risk

- `jdk/xf` and the flat sets agree by construction of the categories (both the JDK's data), but the
  emitter's Java syntax is checked against the flat sets only through the contract and pattern
  tests; a sampled differential of class membership (Java acceptance against `CodePointSet`) would
  be a cheap addition in 6.1, when `re2j` consumes the flat sets.
- The `xf` cap of 100,000 and the count bound are configurable through `RegexLimits` only; D7's
  properties cover today's five limits. The `.cacheRegExps` property still switches the per-thread
  cache on, as today; D6 (group 5) ignores it with the warning.
- `Matching` catches every `Error` of an engine, `VirtualMachineError` included, as a limit, as the
  matrix says; before, the invocation layer turned the same errors into processing-error without a
  message.
- `RegexEngines` consults `ServiceLoader` on every call; the provider wiring of D3 (group 4) decides
  where to cache the resolution.
- Block names are matched as `Character.UnicodeBlock.forName` matches them, case-insensitively;
  strict XSD is case-sensitive.
- The JMH corpus pins the malformed status through `RegExpFunctions.INVALID_PATTERN_MESSAGE`, now
  public API; a later change of the text moves the corpus with it, a change of the status code
  fails every `rules` fixture open, as it should. The `rules:<n>` smoke of task 2.2 was not re-run
  after the one-line change (the fixture check in `RulesCorpusTest` is the same check the benchmark
  runs at setup); task 2.3's baseline and task 3.3's pair run on this tree.
- Merge note: this tree's `xacml-jmh` holds copies of main's six task 2.2 files; only
  `RulesCorpus.java` and `README.md` differ from main, by the lines named above.

=== END REPORT RX-SPI-fable ===
