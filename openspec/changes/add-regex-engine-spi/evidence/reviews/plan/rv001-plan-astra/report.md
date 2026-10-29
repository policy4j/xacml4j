FINAL REPORT (RXP1 plan reviewer, GPT-6)

1. **P1 — The compatibility requirements cannot all hold.**  
   [design.md:103](openspec/changes/add-regex-engine-spi/design.md:103) preserves today’s JDK translation while giving RE2/J corrected character classes. Meanwhile, [spec.md:47](openspec/changes/add-regex-engine-spi/specs/regex-engine-spi/spec.md:47) requires equal results whenever both engines accept a contract-corpus pattern.

   For example, `\d` on U+0663 is accepted by both: today’s unflagged JDK pattern means ASCII digits, while the proposed RE2/J translation means Unicode `Nd`. RX1 explicitly identifies this disagreement. A shared strict XF parser also rejects legacy patterns such as `(?i)a`, contradicting the preservation requirement. [RX1:106](openspec/changes/add-regex-engine-spi/research/rx1-opus-xsd-to-re2.md:106), [RX1:115](openspec/changes/add-regex-engine-spi/research/rx1-opus-xsd-to-re2.md:115), [Java SE 25 Pattern](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/regex/Pattern.html).

   **Smallest fix:** define separate legacy-JDK and strict-XF semantic profiles, with an explicit difference matrix; alternatively obtain approval to change JDK semantics. State whether the JDK path bypasses strict validation. Replace unconditional cross-engine equality with the chosen contract.  
   **Test:** table-driven outcomes for `\d`/U+0663, `\w`/`_`, negative subtraction, and `(?i)a`/`A`, including registry-level statuses.

2. **P1 — The program-size check occurs too late to bound compilation.**  
   [design.md:133](openspec/changes/add-regex-engine-spi/design.md:133) uses `Matcher.programSize()` for the 10,000-instruction cap. That API reports an already compiled program. RE2/J 1.8 expands counted repetitions during simplification, and its compiler allocates instructions without a size guard. [RE2/J 1.8 Simplify](https://raw.githubusercontent.com/google/re2j/re2j-1.8/java/com/google/re2j/Simplify.java), [Compiler](https://raw.githubusercontent.com/google/re2j/re2j-1.8/java/com/google/re2j/Compiler.java), [Matcher](https://raw.githubusercontent.com/google/re2j/re2j-1.8/java/com/google/re2j/Matcher.java).

   Many sequential `a{1000}` terms fit the source-length limit and each satisfy the nested-product bound, yet exceed the proposed program cap during compilation. A post-compilation rejection cannot prevent those allocations. Expanded Unicode range storage also needs a bound independent of instruction count. RX1 already warns that `programSize()` is available only after compilation. [RX1:98](openspec/changes/add-regex-engine-spi/research/rx1-opus-xsd-to-re2.md:98).

   **Smallest fix:** require an overflow-safe, conservative expansion estimate before emission and native compilation, covering concatenation, alternation, repetition, nesting, and range storage. Keep the post-compilation check as a second check.  
   **Test:** an oversized concatenation of individually permitted repetitions must be rejected before a counting native-compiler delegate is invoked.

3. **P1 — Literal bindings permanently retain transient compilation failures.**  
   [spec.md:34](openspec/changes/add-regex-engine-spi/specs/regex-engine-spi/spec.md:34) requires every compilation failure to produce the same status forever; [design.md:119](openspec/changes/add-regex-engine-spi/design.md:119) repeats this without limiting it to deterministic failures.

   Compilation belongs to the deadline budget. Consequently, an initial compilation delayed by scheduling or GC can poison an otherwise valid literal for the policy’s lifetime. Current code checks elapsed time after compilation, and RX4 explicitly excludes transient deadline failures from retained failures. [RegExpFunctions.java:388](xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/RegExpFunctions.java:388), [RX4:62](openspec/changes/add-regex-engine-spi/research/rx4-astra-spi-design.md:62).

   **Smallest fix:** retain successful compilation and deterministic malformed/unsupported/structural-limit results only. Permit retry after transient deadline or resource failures, and qualify “at most once” accordingly.  
   **Test:** an injected first-attempt compilation timeout followed by an on-time attempt must allow the second request to succeed; a malformed literal must remain consistently rejected.

4. **P2 — The plan silently mixes grammar editions.**  
   [design.md:84](openspec/changes/add-regex-engine-spi/design.md:84) declares XF 1.0, but D3 translates dot to a class excluding both LF and CR. The cited 2007 XF §7.6.1.1 describes unflagged dot as excluding LF only. RX1 explicitly asks the design to record its proposed later-edition deviations, including CR handling and acceptance of noncapturing groups. That decision is missing from the maintainer questions. [RX1:142](openspec/changes/add-regex-engine-spi/research/rx1-opus-xsd-to-re2.md:142), [XPath F&O 1.0, 23 January 2007](https://www.w3.org/TR/2007/REC-xpath-functions-20070123/).

   **Smallest fix:** pin one grammar edition and explicitly obtain approval for deviations. Record their expected outcomes rather than presenting later-edition behavior as unqualified XF 1.0 compliance.  
   **Test:** distinguish LF, CR, U+0085, U+2028, and U+2029 for dot; explicitly test the chosen acceptance/status for `(?:a)`.

5. **P2 — The status migration contradicts the required characterization gate.**  
   [spec.md:21](openspec/changes/add-regex-engine-spi/specs/regex-engine-spi/spec.md:21) requires every recorded JDK result, status, and message to remain identical. [tasks.md:13](openspec/changes/add-regex-engine-spi/tasks.md:13) includes malformed patterns and a stack-overflow case in that corpus. D5 proposes changing malformed-pattern status and changes engine-error messages to the fixed limit message. [design.md:135](openspec/changes/add-regex-engine-spi/design.md:135).

   Thus selecting S2 makes the mandatory preservation gate fail. Even selecting S1 leaves the engine-error message change unresolved. The unsupported-pattern scenario also refers to “the status of an unsupported pattern” without specifying that status normatively.

   **Smallest fix:** after the maintainer decision, add an explicit status/message matrix to the delta spec, identify approved compatibility exceptions, and distinguish historical characterization from post-change acceptance tests. Include message changes in the breaking-change description.  
   **Test:** assert exact codes and messages for malformed syntax, unsupported constructs, structural limits, deadlines, and an injected `StackOverflowError`. Injection makes the mapping test independent of JVM stack thresholds.

6. **P2 — The benchmark plan cannot isolate its promised deadline-wrapper cost.**  
   [tasks.md:34](openspec/changes/add-regex-engine-spi/tasks.md:34) requires measuring RE2/J wrapper cost, but [design.md:166](openspec/changes/add-regex-engine-spi/design.md:166) specifies engine comparisons and separate compile/match measurements without a within-engine wrapper control. Comparing JDK with RE2/J confounds wrapper overhead with engine differences.

   The PDP harness also evaluates corpus requests during setup, so its decision measurements cannot establish first-use compilation cost. RX4 explicitly documents this boundary. [PdpFixture.java:124](xacml-jmh/src/main/java/org/xacml4j/jmh/harness/PdpFixture.java:124), [RX4:131](openspec/changes/add-regex-engine-spi/research/rx4-astra-spi-design.md:131).

   **Smallest fix:** add benchmark-only wrapped/unwrapped RE2/J cases using identical compiled patterns and inputs. Label PDP results as warmed decisions; add fresh-state first-use measurements if those costs will be claimed. Include complete function invocation for cache-hit/miss measurements.  
   **Test:** setup verifies equal results and the selected engine; counting hooks prove whether each measured case includes compilation or cache lookup.

Verification: read the requested plan artifacts, all six research reports, the bead 4.58 requirement, current function wiring/tests, and relevant JMH sources. Inspected pinned RE2/J 1.8 sources; cited external evidence was retrieved on 2026-10-27. No files were changed. No builds, tests, benchmarks, OpenSpec validation, Git writes, or beads writes ran. Runtime reproductions and performance remain **UNVERIFIED**.

**Verdict: REJECT.**
