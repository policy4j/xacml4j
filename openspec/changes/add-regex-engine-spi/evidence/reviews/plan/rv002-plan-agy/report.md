### Findings

#### P1-1: Engine selection via `PolicyRepository.Builder` (D2) is disconnected from policy unmarshalling in live code, conformance tests, and JMH benchmarks
- **Where**:
  - [design.md](openspec/changes/add-regex-engine-spi/design.md#L90-L96), [design.md](openspec/changes/add-regex-engine-spi/design.md#L169-L170)
  - [tasks.md](openspec/changes/add-regex-engine-spi/tasks.md#L20-L34)
  - [AbstractPolicyRepository.java](xacml-pdp/src/main/java/org/xacml4j/v30/spi/repository/AbstractPolicyRepository.java#L62-L80), [AbstractPolicyRepository.java](xacml-pdp/src/main/java/org/xacml4j/v30/spi/repository/AbstractPolicyRepository.java#L217)
  - [PolicyImportTool.java](xacml-pdp/src/main/java/org/xacml4j/v30/spi/repository/PolicyImportTool.java#L75-L78)
  - [MarshallingProvider.java](xacml-core/src/main/java/org/xacml4j/v30/marshal/MarshallingProvider.java#L47-L56)
  - [ConformanceCaseRunner.java](xacml-conformance-tests/src/test/java/org/xacml4j/v20/ConformanceCaseRunner.java#L66-L105)
  - [PdpFixture.java](xacml-jmh/src/main/java/org/xacml4j/jmh/harness/PdpFixture.java#L56-L146), [Marshalling.java](xacml-jmh/src/main/java/org/xacml4j/jmh/harness/Marshalling.java#L42-L45)
- **Problem**:
  1. [design.md](openspec/changes/add-regex-engine-spi/design.md#L91-L96) states that `PolicyRepository.Builder` selects the engine so the engine is known when a policy is loaded and literal patterns compile once (D4). However, in [AbstractPolicyRepository.java](xacml-pdp/src/main/java/org/xacml4j/v30/spi/repository/AbstractPolicyRepository.java#L62-L80), the `functions` field is `private final` and never read (`r.accept(new FunctionValidatingVisitor())` at line 217 is commented out).
  2. [PolicyImportTool.importPolicy](xacml-pdp/src/main/java/org/xacml4j/v30/spi/repository/PolicyImportTool.java#L75-L78) calls the zero-argument `MarshallingProvider.newPolicyUnmarshaller()`, which uses `MarshallingProvider.funcSystemProvider` ([MarshallingProvider.java](xacml-core/src/main/java/org/xacml4j/v30/marshal/MarshallingProvider.java#L47-L56)) — a `static final` interface constant initialized once at class load time. That directly violates [design.md](openspec/changes/add-regex-engine-spi/design.md#L93-L94) (*"read when the provider is built (not at class load)"*).
  3. Both [ConformanceCaseRunner.java](xacml-conformance-tests/src/test/java/org/xacml4j/v20/ConformanceCaseRunner.java#L89-L96) (via `repository.newImportTool()`) and [PdpFixture.java](xacml-jmh/src/main/java/org/xacml4j/jmh/harness/PdpFixture.java#L105) (which unmarshals policies via [Marshalling.java](xacml-jmh/src/main/java/org/xacml4j/jmh/harness/Marshalling.java#L42-L45) before opening the `PolicyRepository`) bypass `PolicyRepository.Builder`'s `FunctionProvider`. Setting an engine on `PolicyRepository.Builder` will have no effect on policy loading in production `PolicyImportTool` callers, conformance tests (task 5.3), or JMH benchmarks (tasks 4.3, 5.4).
- **Smallest Fix**:
  1. In [design.md](openspec/changes/add-regex-engine-spi/design.md#L90-L96) (D2) and [tasks.md](openspec/changes/add-regex-engine-spi/tasks.md#L20), specify that [PolicyImportTool.java](xacml-pdp/src/main/java/org/xacml4j/v30/spi/repository/PolicyImportTool.java#L75-L78) passes the repository's `FunctionProvider` and `DecisionCombiningAlgorithmProvider` into `MarshallingProvider.newPolicyUnmarshaller(functions, algorithms)`, and make `MarshallingProvider.newPolicyUnmarshaller()` build or resolve the default `FunctionProvider` dynamically instead of freezing `funcSystemProvider` at interface initialization if system-property overrides are supported.
  2. In [tasks.md](openspec/changes/add-regex-engine-spi/tasks.md#L27-L34) (tasks 4.3, 5.3, 5.4), specify how [ConformanceCaseRunner.java](xacml-conformance-tests/src/test/java/org/xacml4j/v20/ConformanceCaseRunner.java#L66) and [PdpFixture.java](xacml-jmh/src/main/java/org/xacml4j/jmh/harness/PdpFixture.java#L56) accept a `FunctionProvider` / `engine` parameter and pass it to `Marshalling.policies(...)`.

---

#### P1-2: Contradiction between shared `XacmlPattern` strict XF 1.0 validation / D5 status changes and `Requirement: The jdk engine keeps today's results`
- **Where**:
  - [design.md](openspec/changes/add-regex-engine-spi/design.md#L48-L56), [design.md](openspec/changes/add-regex-engine-spi/design.md#L84-L86), [design.md](openspec/changes/add-regex-engine-spi/design.md#L103-L105), [design.md](openspec/changes/add-regex-engine-spi/design.md#L135-L142)
  - [spec.md](openspec/changes/add-regex-engine-spi/specs/regex-engine-spi/spec.md#L21-L30)
  - [tasks.md](openspec/changes/add-regex-engine-spi/tasks.md#L13-L20)
  - [RegExpLimitsTest.java](xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/RegExpLimitsTest.java#L141-L181), [RegExpLimitsTest.java](xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/RegExpLimitsTest.java#L341-L441)
  - [spec.md (request-limits)](openspec/changes/modernize-core-for-jdk28-valhalla/specs/request-limits/spec.md#L159-L170)
- **Problem**:
  1. [spec.md](openspec/changes/add-regex-engine-spi/specs/regex-engine-spi/spec.md#L21-L30) requires: *"The `jdk` engine SHALL give the result, status code, and status message that `RegExpFunctions` gives before this change for every pattern and input of the characterization corpus, including the limit cases of bead 4.58."*
  2. [tasks.md](openspec/changes/add-regex-engine-spi/tasks.md#L13) (`2.1`) puts RX1 table rows, drift cases D1–D8, and `RegExpLimitsTest` cases into `RegExpCharacterizationTest`, and task `3.2` ([tasks.md](openspec/changes/add-regex-engine-spi/tasks.md#L20)) requires `2.1` to pass unchanged.
  3. However, [design.md](openspec/changes/add-regex-engine-spi/design.md#L56) and [design.md](openspec/changes/add-regex-engine-spi/design.md#L84-L86) put a shared `XacmlPattern` validating XF 1.0 §7.6.1 parser in `xacml-core` (task `3.1`) and D5 ([design.md](openspec/changes/add-regex-engine-spi/design.md#L135-L142)) changes malformed-pattern failures from `processing-error` (with `null` message) to `syntax-error` (with a fixed message).
  4. If `jdk` uses `XacmlPattern` or D5's `syntax-error`:
     - Java-only patterns tested in [RegExpLimitsTest.java](xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/RegExpLimitsTest.java#L141-L441) and required by [spec.md (request-limits)](openspec/changes/modernize-core-for-jdk28-valhalla/specs/request-limits/spec.md#L159-L170) — including `\Q...\E`, `[]a{1000001}]`, `\c[`, `\x{41}`, `(?<year>...)`, `(?=a)`, `(?!b)`, and RX1 drift case D8 `(?i)a` — are invalid under XF 1.0 §7.6.1 and would start failing on `jdk`.
     - `(?x)` ([RegExpLimitsTest.java](xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/RegExpLimitsTest.java#L341)) is currently rejected as a **limit** (`processing-error`, `"Regular expression evaluation limit exceeded"`); under `XacmlPattern` + S2 it would become `syntax-error`.
     - Any malformed pattern (e.g., `(`) in `2.1` pinned to today's `processing-error` + `null` message will fail in `3.2` if `jdk` switches to S2 (`syntax-error` + fixed message).
- **Smallest Fix**:
  1. State in [design.md](openspec/changes/add-regex-engine-spi/design.md#L84-L86) (D1/D3) that `jdk` retains `covertXacmlToJavaSyntax` + `RepetitionScanner` + `java.util.regex.Pattern` (preserving `\Q...\E`, `[]a...]`, and all [RegExpLimitsTest.java](xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/RegExpLimitsTest.java) behaviors), while `XacmlPattern` validates XF 7.6.1 for `re2j`.
  2. In [spec.md](openspec/changes/add-regex-engine-spi/specs/regex-engine-spi/spec.md#L21-L30) and [tasks.md](openspec/changes/add-regex-engine-spi/tasks.md#L13-L20), explicitly scope `RegExpCharacterizationTest` so that if the maintainer selects S2 in task `1.3`, only the malformed-pattern status/message assertion changes in `3.2` while all valid and limit cases remain identical.

---

#### P1-3: Silent-misreading and translation defects in D3 (`^`/`$` inside `[...]`, nested `[...]` emission from character-class escapes, and unbounded RE2 range expansion)
- **Where**:
  - [design.md](openspec/changes/add-regex-engine-spi/design.md#L103-L112)
  - [spec.md](openspec/changes/add-regex-engine-spi/specs/regex-engine-spi/spec.md#L47-L69)
  - [rx1-opus-xsd-to-re2.md](openspec/changes/add-regex-engine-spi/research/rx1-opus-xsd-to-re2.md#L58-L76), [rx1-opus-xsd-to-re2.md](openspec/changes/add-regex-engine-spi/research/rx1-opus-xsd-to-re2.md#L98-L108)
  - [RegularExpressionFunctionsTest.java](xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/RegularExpressionFunctionsTest.java#L89-L96)
- **Problem**:
  1. **`^` and `$` inside `[...]`**: [design.md](openspec/changes/add-regex-engine-spi/design.md#L106) says *"`^` and `$` become `\A` and `\z`"*. Inside a character class (such as `[$]` in [RegularExpressionFunctionsTest.java](xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/RegularExpressionFunctionsTest.java#L89-L96) or `[a^]`), `^` (non-initial) and `$` are literals, whereas `\A` and `\z` inside `[...]` are rejected by RE2/J (`invalid escape sequence`).
  2. **Multi-character escapes inside `[...]`**: [design.md](openspec/changes/add-regex-engine-spi/design.md#L107-L109) says *"`\s \d \w \i \c`, category and block escapes, and class subtraction become explicit code-point ranges"*. When `\d`, `\w`, `\i`, `\c`, `\p{...}`, or `\P{...}` appear *inside* a character class (e.g., `[\d_]` or `[\i-[:]]`), emitting a bracketed `[...]` inside the outer `[...]` creates nested `[` and `]`, which RE2/J **silently misreads** as a literal `[` and premature class close ([rx1-opus-xsd-to-re2.md](openspec/changes/add-regex-engine-spi/research/rx1-opus-xsd-to-re2.md#L71-L72), rows 17–18).
  3. **Unbounded RE2 string expansion before `programSize()` check**: Expanding `\w`, `\c`, or `\p{L}` to explicit Unicode 16.0 code-point ranges turns a 2-character escape into thousands of characters of `\x{...}-\x{...}` ranges. Because `Matcher.programSize()` is only available *after* `Pattern.compile` finishes ([rx1-opus-xsd-to-re2.md](openspec/changes/add-regex-engine-spi/research/rx1-opus-xsd-to-re2.md#L98)), a 10,000-character input pattern full of `\c` or `\w` can expand to tens of megabytes and exhaust memory during `RE2.compile` before `programSize()` is ever checked.
  4. **Unnecessary reluctant-quantifier rewrite**: [design.md](openspec/changes/add-regex-engine-spi/design.md#L110-L111) says *"reluctant quantifiers drop the `?`"*, contradicting RX1 ([rx1-opus-xsd-to-re2.md](openspec/changes/add-regex-engine-spi/research/rx1-opus-xsd-to-re2.md#L61), row 7), which showed RE2/J supports `??`, `*?`, `+?`, `{n,m}?` natively.
- **Smallest Fix**:
  1. In [design.md](openspec/changes/add-regex-engine-spi/design.md#L106-L112) (D3) and [spec.md](openspec/changes/add-regex-engine-spi/specs/regex-engine-spi/spec.md#L47-L69):
     - Specify that `^` → `\A` and `$` → `\z` apply only to unescaped anchors **outside** `[...]`.
     - Specify that every character class `[...]` (including nested `\s \S \d \D \w \W \i \I \c \C \p{...} \P{...}`, negations, and nested subtractions) is flattened via code-point range-set algebra into a **single flat `[...]`** with no inner unescaped `[` or `]`, and add a scenario in `spec.md` for `[$]` and `[\i-[:]]`.
     - Keep reluctant `?` as-is in RE2 emission (or document why it is preserved).
     - Enforce a pre-compile cap on the emitted RE2 pattern length / total emitted range count before calling `com.google.re2j.Pattern.compile`.

---

#### P2-1: Benchmark claims in `design.md` and `tasks.md` cannot be measured as written
- **Where**:
  - [design.md](openspec/changes/add-regex-engine-spi/design.md#L130-L131), [design.md](openspec/changes/add-regex-engine-spi/design.md#L166-L172)
  - [spec.md](openspec/changes/add-regex-engine-spi/specs/regex-engine-spi/spec.md#L81-L89)
  - [tasks.md](openspec/changes/add-regex-engine-spi/tasks.md#L14-L15), [tasks.md](openspec/changes/add-regex-engine-spi/tasks.md#L27), [tasks.md](openspec/changes/add-regex-engine-spi/tasks.md#L34)
  - [PdpFixture.java](xacml-jmh/src/main/java/org/xacml4j/jmh/harness/PdpFixture.java#L104-L125), [PdpDecideBenchmark.java](xacml-jmh/src/main/java/org/xacml4j/jmh/pdp/PdpDecideBenchmark.java#L53-L82)
- **Problem**:
  1. **Task ordering of `RegexFunctionBenchmark` (`2.2` vs `3.2` vs `5.2`)**: [tasks.md](openspec/changes/add-regex-engine-spi/tasks.md#L14) creates `RegexFunctionBenchmark` in task `2.2` (before the `RegexEngine` SPI exists in `3.2` and before `xacml-regex-re2j` exists in `5.2`), whereas [spec.md](openspec/changes/add-regex-engine-spi/specs/regex-engine-spi/spec.md#L86-L89) requires `@Param` values `engine` (`jdk`, `re2j`), `mode` (`compileAndMatch`, `compiledMatch`), and `caseId`.
  2. **Deadline fast-path cost (`design.md:130-131` and `tasks.md:34`)**: [design.md](openspec/changes/add-regex-engine-spi/design.md#L130-L131) states *"the deadline costs RE2/J its literal-prefix fast path; measured in task 6.x"* (and [tasks.md](openspec/changes/add-regex-engine-spi/tasks.md#L34) puts this in `5.4`), because `DeadlineCharSequence` defeats RE2/J's `input instanceof String` check in `Matcher.find()` ([rx5-terra-re2j-performance.md](openspec/changes/add-regex-engine-spi/research/rx5-terra-re2j-performance.md#L21-L23)). However, `RegexFunctionBenchmark` in [design.md](openspec/changes/add-regex-engine-spi/design.md#L168) and [spec.md](openspec/changes/add-regex-engine-spi/specs/regex-engine-spi/spec.md#L86-L89) has no `@Param` or benchmark mode that toggles the deadline (`maxMatchMillis > 0` vs `0` / raw `String`), so it cannot measure the `DeadlineCharSequence` penalty on `re2j`!
  3. **`PdpDecideBenchmark` cache-warming (`4.3` vs `2.3`)**: Today's `RegExpFunctions` already caches compiled patterns by default (`DEFAULT_CACHE_SIZE = 256`, [RegExpFunctions.java](xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/RegExpFunctions.java#L150-L157)), and [PdpFixture.java](xacml-jmh/src/main/java/org/xacml4j/jmh/harness/PdpFixture.java#L123) runs `fixture.check()` during `@Setup(Level.Trial)` before 5 warmup iterations ([rx4-astra-spi-design.md](openspec/changes/add-regex-engine-spi/research/rx4-astra-spi-design.md#L131)). Thus in `2.3` vs `4.3`, `PdpDecideBenchmark` only measures replacing a warmed `ConcurrentHashMap.get(pattern)` + `covertXacmlToJavaSyntax` lookup with a direct `CompiledRegex` reference on `Apply`/`Match` — not cold compilation.
- **Smallest Fix**:
  1. Add a deadline toggle (or `maxMatchMillis` parameter: default `250` vs `0` disabled) to `RegexFunctionBenchmark` in [design.md](openspec/changes/add-regex-engine-spi/design.md#L168) and [spec.md](openspec/changes/add-regex-engine-spi/specs/regex-engine-spi/spec.md#L86-L89) so task `5.4` can actually measure the `DeadlineCharSequence` vs `String` fast-path delta on `re2j`.
  2. Clarify in [tasks.md](openspec/changes/add-regex-engine-spi/tasks.md#L14) (`2.2`) that `RegexFunctionBenchmark` runs with `engine=jdk` in `2.2`/`2.3` and adds `re2j` to the `@Param` in `5.2`/`5.4`, and state in `4.3` that `PdpDecideBenchmark` measures the warmed steady-state gain of D4 (eliminating per-call `covertXacmlToJavaSyntax` + cache lookup).

---

#### P2-2: Missing observable scenarios in `specs/regex-engine-spi/spec.md` and inconsistent status for `a{1001}` vs repetition limits
- **Where**:
  - [spec.md](openspec/changes/add-regex-engine-spi/specs/regex-engine-spi/spec.md#L3-L80)
  - [design.md](openspec/changes/add-regex-engine-spi/design.md#L111-L112), [design.md](openspec/changes/add-regex-engine-spi/design.md#L133-L142)
  - [rx2-fable-limits-selection.md](openspec/changes/add-regex-engine-spi/research/rx2-fable-limits-selection.md#L23-L35)
- **Problem**:
  1. **Limit vs. unsupported-construct conflict on `a{1001}`**: [design.md](openspec/changes/add-regex-engine-spi/design.md#L111-L112) (D3) classifies *"counts above 1000 are rejected before RE2/J sees the pattern (unsupported)"*, whereas on `jdk` (and in [rx2-fable-limits-selection.md](openspec/changes/add-regex-engine-spi/research/rx2-fable-limits-selection.md#L27)) `a{1001}` when `maxRepetition=1000` is a **repetition limit** (`processing-error` with message `"Regular expression evaluation limit exceeded"`). Under D5 S2 ([design.md](openspec/changes/add-regex-engine-spi/design.md#L135-L136)), classifying `a{1001}` as "unsupported" in `re2j` would return `syntax-error` (`"Unsupported regular expression construct"`) while `jdk` returns `processing-error` (`"Regular expression evaluation limit exceeded"`). Furthermore, if a caller configures `maxRepetition=500`, `re2j` must enforce `min(maxRepetition, 1000)` as a limit, and only treat `{1001}` when `maxRepetition > 1000` as an engine cap / limit.
  2. **Missing scenarios in `spec.md`**:
     - [spec.md](openspec/changes/add-regex-engine-spi/specs/regex-engine-spi/spec.md#L8-L9) states *"two engines with one name SHALL fail when the engine is resolved"*, with no `#### Scenario:` for duplicate engine names.
     - [spec.md](openspec/changes/add-regex-engine-spi/specs/regex-engine-spi/spec.md#L3-L4) requires all six `*-regexp-match` functions to route through the SPI, but only tests `string-regexp-match` in scenarios (omitting `anyURI`, `ipAddress`, `dnsName`, `rfc822Name`, `x500Name`, and the 1.0 legacy aliases `urn:oasis:names:tc:xacml:1.0:function:rfc822Name-regexp-match` and `x500Name-regexp-match` noted in [rx3-sol-conformance-inventory.md](openspec/changes/add-regex-engine-spi/research/rx3-sol-conformance-inventory.md#L98-L112)).
     - [spec.md](openspec/changes/add-regex-engine-spi/specs/regex-engine-spi/spec.md#L49-L69) has no scenarios for:
       - Malformed pattern status and message (D5).
       - `re2j` silent-misreading rejection of octal escapes (`\12`), `x{,n}`, inline flags (`(?i)`), and script-as-block (`\p{Greek}`).
       - `re2j` compile/runtime limits (`maxRepetition`, `maxProgramSize`, `maxInputLength`, `maxMatchMillis`).
- **Smallest Fix**:
  1. Treat `quantifier > maxRepetition` (and `> 1000` on `re2j`) consistently as a **repetition limit** (`processing-error`, `"Regular expression evaluation limit exceeded"`) across both engines.
  2. Add observable `#### Scenario:` blocks in [spec.md](openspec/changes/add-regex-engine-spi/specs/regex-engine-spi/spec.md) for duplicate engine names, the five typed `*-regexp-match` functions (plus 1.0 aliases), malformed-pattern status (D5), the four remaining silent-misreading constructs (`\12`, `x{,3}`, `(?i)`, `\p{Greek}`), and `re2j` limits (`maxRepetition`, `maxProgramSize`).

---

#### P2-3: Unmarked public API compatibility and system-property compatibility risks
- **Where**:
  - [proposal.md](openspec/changes/add-regex-engine-spi/proposal.md#L31-L32)
  - [design.md](openspec/changes/add-regex-engine-spi/design.md#L92-L94), [design.md](openspec/changes/add-regex-engine-spi/design.md#L125-L126), [design.md](openspec/changes/add-regex-engine-spi/design.md#L176)
  - [RegExpFunctions.java](xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/RegExpFunctions.java#L169-L180), [RegExpFunctions.java](xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/RegExpFunctions.java#L362), [RegExpFunctions.java](xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/RegExpFunctions.java#L722)
- **Problem**:
  1. [RegExpFunctions.java](xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/RegExpFunctions.java#L362) and [RegExpFunctions.java](xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/RegExpFunctions.java#L722) expose `public static boolean matches(String, String)` and `public static String covertXacmlToJavaSyntax(String)`, plus the six `public static BooleanValue regexpMatch(...)` entry points. Removing or changing their signatures would be an unmarked `BREAKING` change.
  2. Bead 4.58 ([spec.md (request-limits)](openspec/changes/modernize-core-for-jdk28-valhalla/specs/request-limits/spec.md#L175-L178)) specifies the existing system properties under `org.xacml4j.v30.policy.function.impl.RegExpFunctions.*` (`maxPatternLength`, `maxInputLength`, `maxMatchMillis`, `maxRepetition`, `cacheSize`, `cacheRegExps`). [design.md](openspec/changes/add-regex-engine-spi/design.md#L125-L126) introduces `RegexLimits` but does not state that `RegexLimits` still defaults from those existing system properties when built.
- **Smallest Fix**:
  State in [design.md](openspec/changes/add-regex-engine-spi/design.md#L80-L96) (D1/D2/D5) that `RegExpFunctions.matches(String, String)`, `RegExpFunctions.covertXacmlToJavaSyntax(String)`, and the existing `org.xacml4j.v30.policy.function.impl.RegExpFunctions.*` system properties remain supported and delegate to the default `jdk` engine / `RegexLimits` defaults.

---

#### P3-1: Unstated XF 1.0 §7.6.1 grammar extensions in `XacmlPattern` (`.` and `(?:...)`)
- **Where**:
  - [design.md](openspec/changes/add-regex-engine-spi/design.md#L19), [design.md](openspec/changes/add-regex-engine-spi/design.md#L84), [design.md](openspec/changes/add-regex-engine-spi/design.md#L106-L110)
  - [spec.md](openspec/changes/add-regex-engine-spi/specs/regex-engine-spi/spec.md#L5-L6)
  - [rx1-opus-xsd-to-re2.md](openspec/changes/add-regex-engine-spi/research/rx1-opus-xsd-to-re2.md#L59-L62), [rx1-opus-xsd-to-re2.md](openspec/changes/add-regex-engine-spi/research/rx1-opus-xsd-to-re2.md#L142)
- **Problem**:
  As RX1 proved from the W3C specifications ([rx1-opus-xsd-to-re2.md](openspec/changes/add-regex-engine-spi/research/rx1-opus-xsd-to-re2.md#L59-L62)), strict F&O 1.0 §7.6.1 defines `.` as `[^\n]` (excluding `\r` only in XSD Part 2 and F&O 3.1 §5.6.2) and does not include `(?:...)` (added in F&O 3.0). [design.md](openspec/changes/add-regex-engine-spi/design.md#L84) says `XacmlPattern` parses "the XF 1.0 section 7.6.1 grammar", while [design.md](openspec/changes/add-regex-engine-spi/design.md#L106-L110) translates `.` to `[^\n\r]` and accepts `(?:...)`.
- **Smallest Fix**:
  Note in [design.md](openspec/changes/add-regex-engine-spi/design.md#L84) and [spec.md](openspec/changes/add-regex-engine-spi/specs/regex-engine-spi/spec.md#L5-L6) that `XacmlPattern` parses XF 1.0 §7.6.1 / XSD Part 2 Appendix F with `.` = `[^\n\r]` and non-capturing groups `(?:...)` (F&O 3.0+).

---

#### P3-2: Stale JDK 21 reference in `proposal.md` and task-number typo in `design.md`
- **Where**:
  - [proposal.md](openspec/changes/add-regex-engine-spi/proposal.md#L53)
  - [design.md](openspec/changes/add-regex-engine-spi/design.md#L131)
- **Problem**:
  1. [proposal.md](openspec/changes/add-regex-engine-spi/proposal.md#L53) says *"JDK 21 support"*, whereas the repository baseline is JDK 25 ([AGENTS.md](AGENTS.md#L62), [design.md](openspec/changes/add-regex-engine-spi/design.md#L26)).
  2. [design.md](openspec/changes/add-regex-engine-spi/design.md#L131) says *"measured in task 6.x"*, whereas the `re2j` benchmark task in [tasks.md](openspec/changes/add-regex-engine-spi/tasks.md#L34) is `5.4`.
- **Smallest Fix**:
  Change `JDK 21` to `JDK 25` in [proposal.md](openspec/changes/add-regex-engine-spi/proposal.md#L53) and `task 6.x` to `task 5.4` in [design.md](openspec/changes/add-regex-engine-spi/design.md#L131).

---

### Verdict

**APPROVE WITH CHANGES**

The research synthesis, SPI boundary, and five open questions for the maintainer are well-structured. Before starting implementation (`2.1`), update `design.md`, `specs/regex-engine-spi/spec.md`, and `tasks.md` to resolve **P1-1** (wire `PolicyRepository`'s `FunctionProvider` into `PolicyImportTool`, `ConformanceCaseRunner`, and `PdpFixture`), **P1-2** (keep `jdk` on its existing syntax/limit path so `RegExpLimitsTest` and `RegExpCharacterizationTest` do not conflict with `XacmlPattern`/D5), **P1-3** (restrict `^`/`$` rewriting to outside `[...]`, flatten character-class escapes into a single `[...]` without nested brackets, and bound emitted RE2 pattern length before `RE2.compile`), and **P2-1** through **P2-3**.
