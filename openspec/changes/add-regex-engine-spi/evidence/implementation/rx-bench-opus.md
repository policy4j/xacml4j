=== BEGIN REPORT RX-BENCH-opus ===
FINAL REPORT (policy4j_implementer, Claude Opus 5.5): change `add-regex-engine-spi`, task 2.2 (regex benchmarks)

## Summary

- **`RegexFunctionBenchmark`**: new class in `xacml-jmh`, package `org.xacml4j.jmh.regex`. It measures today's functions in four separate methods:
  - `compile`: translation and compilation;
  - `match`: a search with a compiled pattern;
  - `call`: a whole `RegExpFunctions.matches` call, repeating the set's patterns (cache hits once a cache exists);
  - `callMiss`: the same call over at least 1,024 distinct patterns (cache misses).
- **Entry points:** it uses only the ones that D7 keeps, `RegExpFunctions.matches(String, String)` and `covertXacmlToJavaSyntax`, plus `java.util.regex`. So it runs unchanged before and after the SPI.
- **Parameters:** only those the plan asks for: `patterns` (`conformance`, `adversarial`) and `inputLength` (16, 256, 4096).
- **Regex corpus:** `rules:<n>` gains four requests: `regex-targets`, `regex-conditions`, `regex-any-of-all` (IIC168), and `regex-malformed`. With the existing `repeated-conditions` they form the regex corpus. Every case has an exact expected result, checked when the fixture opens.
- **Tests:**
  - `RegexBenchmarkTest`: new, 11 tests.
  - `BenchmarkSmokeTest`: new entry for the regex benchmark.
  - `RulesCorpusTest`: request list updated.
  - The JMH module passes 225 of 225 on the final sources.
  - The one-fork smoke ran all 20 function combinations and all 5 corpus requests. Numbers are below.
- **Finding outside the regex code:** for every rule that ends Indeterminate, `RootEvaluationContext.setEvaluationStatus` re-hashes the root policy set with an uncached deep `hashCode`. That is 92% of the bytes of a decision with one malformed pattern, and grows with tree size × failing rules. Details are under Findings, item 1.
- **Beads:** the claim was refused, because `policy4j-eufz.2.2` is blocked by `policy4j-eufz.2.1`. I did not force it.

## Changes (all in `xacml-jmh`)

The worktree's baseline holds the main tree's uncommitted work. For example, `RulesCorpus.java` is untracked there. So `git diff` shows more than my edits; the list below is complete.

| File | Change |
| --- | --- |
| `src/main/java/org/xacml4j/jmh/regex/RegexFunctionBenchmark.java` | new benchmark (see Design) |
| `src/test/java/org/xacml4j/jmh/regex/RegexBenchmarkTest.java` | new, 4 tests (11 with their parameters) |
| `src/main/java/org/xacml4j/jmh/corpus/RulesCorpus.java` | the class javadoc, new constants (`:80-83`, `:100-109`), the 4 cases (`:196-207`), 4 generators (`:347-390`), appended after `responses` in `root` (`:257-258`) |
| `src/test/java/org/xacml4j/jmh/corpus/RulesCorpusTest.java` | the 11 request ids, in order |
| `src/test/java/org/xacml4j/jmh/pdp/BenchmarkSmokeTest.java` | `regexFunctionBenchmarkRunsEveryMethodWithEveryDefaultParameterSet` (`:136`): 20 parameter sets, each with a finite score |
| `README.md` | rows in the benchmark and parameter tables, 4 rows in the rules table with a paragraph on the regex corpus, and a section "Regular-expression functions" (`:220`) |

Not touched: `xacml-core` (Fable), `TargetIndex` (Terra), the temporal functions (Astra), `openspec/`, `.beads/`. The failed claim wrote nothing. Nothing is staged or committed.

## Design

### `RegexFunctionBenchmark`

| Method | One operation |
| --- | --- |
| `compile` (`:95`) | `Pattern.compile(RegExpFunctions.covertXacmlToJavaSyntax(p))`. The repetition scan is package-private, so it is measured only inside `call`. |
| `match` (`:101`) | `compiled.matcher(input).find()`, without the deadline wrapper, which is private |
| `call` (`:107`) | `RegExpFunctions.matches(p, input)`: translation, limits, compilation, and the search with its deadline |
| `callMiss` (`:113`) | the same call over at least `MISSED_PATTERNS` = 1,024 distinct patterns, four times the default cache of 256 (`RegExpFunctions.java:154`, design D6) |

- **Why `matches` and not the six function methods:** the full call is the same engine path. The function methods add only `TypeToString` and `BooleanVal`, and they need an `EvaluationContext`. The corpus covers a typed function through the PDP: `anyURI-regexp-match` in `regex-targets`.
- **Pattern sets** (`:71-92`):
  - `conformance`: the 12 distinct patterns of the OASIS cases that use `string-regexp-match` (IIB008, IIB009, IIC056, IIC057, IIC165 to IIC169), copied exactly from `xacml3.0-ct-v.0.4/*Policy.xml`, spaces included.
  - `adversarial`: `\s+$`, `.*\.pdf$`, and `[a-z0-9._]+@example\.com`. On a near-miss input, a backtracking search takes time quadratic in its length.
  - No exponential or stack-overflowing pattern is included. Without a deadline, `match` would hang on one at 4,096 characters, and `call` would end on the 1 s deadline.
- **Inputs and oracle:**
  - Each pattern has a subject: one of its shortest inputs.
  - Found input: the filler, then the subject, `inputLength` characters in all. Expected: true.
  - Missed input: the same with `~` as its last character. Expected: false.
  - A subject longer than the input loses its start, so the expected result is false. Because the subject is a shortest match, no shorter string can match.
  - The expected results follow from how the inputs are built, not from any engine.
  - Both profiles and both engines should agree: the patterns and inputs are ASCII, and none of them uses the `.`, `\s`, `\d`, or `\w` drift cases of design D2.
- **Setup checks:**
  - `Patterns` (`:128`) checks every pattern on its subject and on its missed subject.
  - `Inputs` (`:152`) first runs every search without the deadline, which warms the JIT, then every full call (`:160`).
  - `Misses` (`:178`) checks every variant on its case's shortest inputs.
  - A wrong result throws `IllegalStateException` and fails the trial.
- **Variants for `callMiss`** (`:236`): the pattern, plus one CJK ideograph from U+4E00 upward, plus `{0}`. The suffix matches only the empty string, so it changes no result. It is valid in the legacy and XF syntaxes and in RE2, and it adds no counted repetition above 0 (D4).
- **States** are split so that `compile` takes only `patterns`. The smoke keys confirm that `inputLength` applies only to `match`, `call`, and `callMiss`.
- **Counting hooks (design Benchmark Plan):** they need an engine to count, and today's functions have none, so they come with the engine parameter (tasks 3.2 and 5.2). Until then, `RegexBenchmarkTest` checks the structure instead: `call` uses 12 (or 3) distinct patterns, and `callMiss` uses at least 1,024 with none repeated.

### Regex corpus: extended `rules:<n>`

**Why extend `rules:<n>` instead of adding a corpus:**
- `repeated-conditions` is already in it.
- Its XML builders are already in it.
- Its 4 new policies come after `responses` under the `first-applicable` root, so every existing request still stops at its own policy. Per-request work is unchanged; with `targetIndex=true` the root index also selects the same policies.
- One cost: `all` now cycles over 11 requests, not 7. No `rules:` `all` result is recorded in the evidence (`rtk rg -c 'rules:[0-9]+' openspec/changes/optimize-pdp-evaluation/evidence` finds one review mention only).

| Request | Policies | Expected result |
| --- | --- | --- |
| `regex-targets` | `n` rules, `first-applicable`. Each target is an `anyURI-regexp-match` `Match` with a literal pattern of its own, `^https://records\.example\.com/<i>/[a-z]+\.pdf$`. Only rule `n-1` matches. | effect of rule `n-1` (even rules permit, odd rules deny) |
| `regex-conditions` | `n` rules, `first-applicable`. Each condition is `string-regexp-match` with a literal pattern of its own, `^(user\|admin)-<i>@[a-z]{2,16}\.example\.(com\|org)$`. | effect of rule `n-1` |
| `regex-any-of-all` | `n` Permit rules, `deny-overrides`, so all are evaluated. Each has IIC168's condition exactly: its two patterns, its two values with their spaces, and `any-of-all`. | Permit |
| `regex-malformed` | one Permit rule with the pattern `^[a-z0-9._+@example\.com$`, whose character class is not closed | `INDETERMINATE_P`, processing-error without a message |

- **Status of `regex-malformed` today:**
  - `PatternSyntaxException` is not an `EvaluationException`, so `FunctionInvocationException.statusFor` gives processing-error with a null message (`FunctionInvocationException.java:61-74`).
  - The Permit rule makes Indeterminate{P}, which `first-applicable` returns unchanged (`FirstApplicable.java:67-68`).
  - `DefaultPolicyDecisionPoint.createResult` then builds `Result.builder(decision, status)` without responses (`:296-304`).
  - The test confirms all of this with counts 1, 2, 3, 8, and 100.
- **Why one rule, not `n`:** each failing rule re-hashes the root policy set (finding 1). My first version used `n` rules, and cost 5.5 ms and 5.35 MB per decision at `n` = 100, quadratic in `n`. At `rules:10000` that is an estimated 55 s per decision (UNVERIFIED), and every fixture open decides every request, so it would slow every trial of that size.
- **Task 3.2 must change one line:** the approved status change (D5, maintainer decision 3) gives syntax-error with a fixed message. The expected result at `RulesCorpus.java:205-207` must then change, and the README says so. Until it does, the fixture fails at setup, which is the intended alarm.

## Smoke results (indicative)

Run on 2026-10-28: one fork, 5 warmup and 5 measurement iterations of 1 s, `-XX:-UseJVMCICompiler` (C2), `-prof gc`. Oracle GraalVM 25.0.4, Apple M2 Pro, a shared machine with the JMH lock free. Task 2.3 makes the real baseline.

**`RegexFunctionBenchmark`** (µs/op ± error; B/op)

| Method | conformance 16 | conformance 256 | conformance 4096 | adversarial 16 | adversarial 256 | adversarial 4096 |
| --- | ---: | ---: | ---: | ---: | ---: | ---: |
| `compile` | 0.195 ± 0.036; 1,177 | (same) | (same) | 0.158 ± 0.019; 936 | (same) | (same) |
| `match` | 0.071 ± 0.007; 136 | 0.801 ± 0.137; 136 | 11.4 ± 3.5; 136 | 0.261 ± 0.043; 136 | 63.1 ± 6.6; 136 | 17,315 ± 3,233; 255 |
| `call` | 0.351 ± 0.018; 1,345 | 1.175 ± 0.179; 1,345 | 12.5 ± 1.0; 1,345 | 0.565 ± 0.130; 1,128 | 91.8 ± 13.2; 1,129 | 20,525 ± 927; 1,468 |
| `callMiss` | 0.456 ± 0.030; 1,645 | 1.222 ± 0.045; 1,645 | 12.6 ± 0.4; 1,645 | 0.669 ± 0.054; 1,395 | 108.3 ± 10.9; 1,363 | 22,167 ± 1,774; 1,610 |

`compile` does not take `inputLength`, so its value is one per set.

**`PdpDecideBenchmark`** on `rules:100`, in-memory store, target index off

| Request | µs/op | B/op |
| --- | ---: | ---: |
| `repeated-conditions` | 67.4 ± 8.0 | 192,403 |
| `regex-targets` | 73.6 ± 8.3 | 209,847 |
| `regex-conditions` | 83.5 ± 9.6 | 252,234 |
| `regex-any-of-all` | 165.4 ± 139.0 (first run: 129.5 ± 10.3) | 504,451 |
| `regex-malformed` | 56.3 ± 10.0 | 54,034 |

Readings:
- **Cost of a full call at 16 characters:** about `compile` + `match` + 0.085 µs (repetition scan, deadline wrapper, clock reads). Of its 1,345 B, 1,177 B is translation and compilation, the part that compile-once can remove.
- **Corpus decisions:** they spend about 2 KB and 0.7 µs per regex evaluation (100 per decision, 200 in `regex-any-of-all`). So the expected compile-once gain is large on 4 of the 5 requests. This is an estimate only, UNVERIFIED.
- **The jdk deadline wrapper costs** `call` − `compile` − `match`:
  - 28.5 µs (+45%) at adversarial 256;
  - 3.2 ms (+19%) at adversarial 4,096;
  - 0.84 µs (+7%) at conformance 4,096.
  The plan measures the wrapper for `re2j` later; the jdk figure exists already.
- **`callMiss` costs more than `call` even today:** +0.105 µs and +300 B at conformance 16, because its patterns are longer by the suffix and the ideograph makes them UTF-16 strings. The README says to compare `callMiss` across runs only. The cost of a miss is then the change of (`callMiss` − `call`) between runs.
- **Cold first calls at 4,096 characters** (adversarial near-miss): 70–380 ms, against the 1 s deadline. Warm calls take 24–106 ms. Measured with `Cold.java` under Graal and C2. This is why setup runs the deadline-free searches first.

## Findings

1. **High, for `optimize-pdp-evaluation` and not regex-specific.**
   - **What happens:** for every rule that ends Indeterminate, the context chain calls `RootEvaluationContext.setEvaluationStatus(DecisionRule, Status)` once per level. That method puts the status into a `ConcurrentHashMap` keyed by the decision rule (`RootEvaluationContext.java:298-304`). `PolicySet.hashCode` is an uncached deep content hash (`PolicySet.java:315-326`). So each put walks the root policy set, through every `Match.hashCode`, `AttributeDesignator.hashCode`, and `ISO8601Date.hashCode`/`instant()`, allocating `Objects.hash` arrays and `Instant`s.
   - **Measured** with async-profiler allocation profiles: with one failing rule, 49,552 of 54,048 B/op (92%). With 100 failing rules, 4.97 of 5.35 MB/op (93%) and 5.5 ms/op. The regex failure path itself is about 3.3 KB: stack trace 2.0 KB, `Pattern.compile` 0.8 KB, translation 0.5 KB.
   - **Impact:** any decision with Indeterminate rules pays tree size × failing rules. This also hides most of what compile-once gains on a malformed pattern.
   - **Owners:** `RootEvaluationContext` (Fable) and `PolicySet`.
   - **Options:** cache the hash code in the immutable `Policy` and `PolicySet`, as `Apply` caches its own (`Apply.java:291-293`); or key the status map by identity. That every field of `Policy` and `PolicySet` is immutable is UNVERIFIED. I changed neither; both are outside my files.
2. **Beads:** `br update policy4j-eufz.2.2 --claim --actor policy4j_implementer_claude_opus` was refused: "cannot claim blocked issue: policy4j-eufz.2.1". `br show policy4j-eufz.2.2` shows that dependency.
3. **Later engine parameters need another call path.** `call` and `callMiss` reach jdk/legacy only, because the static entry points stay bound to it (D7). An `engine` or `profile` parameter needs a full call through the instance-based provider of D3 (tasks 4.2, 6.x). The counting hooks belong there too.
4. **Typed-function coverage:** the conformance suites use only `string-regexp-match` (research RX3). `regex-targets` now covers `anyURI-regexp-match` through the PDP, with its exact expected result.

## Verification

| Run | Result | Output (worktree `target/rx-bench/`) |
| --- | --- | --- |
| `RegexBenchmarkTest` + `RulesCorpusTest` | 11 + 7 pass (also after the last edits: `focused-2.txt`, `focused-3.txt`) | `focused-1.txt` |
| `BenchmarkSmokeTest` | 4 of 4 pass, including the 20 regex parameter sets | `smoke-1.txt` |
| JMH module tests, final sources | 225 run, 0 failures (213 before, plus 11 + 1) | `jmh-module-tests-final.txt` |
| One-fork smoke, functions | 20 of 20, exit 0 | `regex-functions.{json,txt}` |
| One-fork smoke, corpus, final jar | 5 of 5, exit 0 | `regex-corpus-2.{json,txt}`; first jar: `regex-corpus.*` |
| Allocation profiles of `regex-malformed` | as in finding 1 | `prof-malformed/`, `prof-malformed-2/` (`alloc.collapsed`) |

Exact commands are in `target/rx-bench/command.txt`, and the JVM and machine in `env.txt`. Scripts are in `<scratchpad>/rx/`: `smoke.sh`, `corpus.sh`, `Cold.java`, `table.py`, `us.py`, `sites.py`, `callers.py`.

## Residual risk

- **Deadline on a slow machine:** an adversarial call at 4,096 characters takes up to 380 ms cold. Setup warms the JIT first, but a much slower or heavily loaded machine could still pass the 1 s deadline on a first call. Setup would then fail with the limit exception: a loud failure, never wrong numbers.
- **Variants checked on short inputs only:** `Misses` checks each variant on its case's shortest inputs. Their results on long inputs rest on the base patterns, which are checked at full length, and on the suffix matching only the empty string.
- **Noise:** the smoke ran one fork on a shared machine. `regex-any-of-all` varied between runs (±139 µs against ±10 µs), so the numbers are indicative only.
- **Cache semantics:** `callMiss` assumes a cache keyed by pattern text that holds about 256 entries. A frequency-based cache could still hit some of its 1,024 patterns. The counting hooks will show the real hit rate.
=== END REPORT RX-BENCH-opus ===
