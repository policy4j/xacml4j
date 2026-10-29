# Evidence

Layout: `tests/`, `reviews/<lane>/`, `benchmarks/jmh/`, `builds/`, `final/`.
One directory per run: `<NNN>-<scope>` (reviews: `rvNNN-<scope>-<lane>`).
Each run holds `summary.md`, `command.txt`, and its raw output. Every JMH run
is also a row of `benchmarks/jmh/LEDGER.md`. See docs/ai/OPENSPEC.md
"Evidence".

## Requirements to proof

| Capability / requirement / scenario | Test or conformance case | Run |
| --- | --- | --- |
| parse-diagnostics / Positioned diagnostics / Unknown function in a policy | `PolicyDiagnosticsTest` | pending |
| parse-diagnostics / Positioned diagnostics / Malformed XML | `RequestDiagnosticsTest` | pending |
| parse-diagnostics / Positioned diagnostics / Invalid JSON value | `JsonRequestDiagnosticsTest` | pending |
| parse-diagnostics / Positioned diagnostics / Unknown root element | `PolicyDiagnosticsTest` | pending |
| parse-diagnostics / Several errors per parse / Three bad rules | `PolicyDiagnosticsTest` | pending |
| parse-diagnostics / Several errors per parse / Error cap | `PolicyDiagnosticsTest` | pending |
| parse-diagnostics / Compiler-like text format / Golden output | `DiagnosticGoldenTest` | pending |
| parse-diagnostics / Compiler-like text format / Did you mean | `DiagnosticFormatterTest` | pending |
| parse-diagnostics / Machine-readable format / Same content as the text form | `DiagnosticFormatterTest` | pending |
| parse-diagnostics / Compatible exception / Old constructor | `SyntaxExceptionTest` | pending |
| parse-diagnostics / Compatible exception / Conformance unchanged | reactor gate | pending |
| parse-diagnostics / No cost on valid input / Same model | conformance gate; JMH pair of group 4 | pending |
| parse-security / Hardened XML factories / External entity | `XmlAttackCorpusTest` | pending |
| parse-security / Hardened XML factories / Entity expansion | `XmlAttackCorpusTest` | pending |
| parse-security / Hardened XML factories / Factory setup fails | `SecureXmlFactoriesTest` | pending |
| parse-security / Hardened XML factories / Transformer | `SecureXmlFactoriesTest` | pending |
| parse-security / Parse limits / Deep nesting | `ParseLimitsTest` | pending |
| parse-security / Parse limits / Limits on a caller's reader | `ParseLimitsTest` | pending |
| parse-security / Parse limits / Raised limit | `ParseLimitsTest` | pending |
| parse-security / Parse limits / JSON limits and duplicates | `JsonParseLimitsTest` | pending |
| parse-security / Caller-supplied parsers / Strict option | `ParseLimitsTest` | pending |
| parse-security / Optional XSD validation / Off by default | `XsdValidationTest` | pending |
| parse-security / Optional XSD validation / On | `XsdValidationTest` | pending |
| parse-security / Optional XSD validation / No external schema access | `XsdValidationTest` | pending |
| parse-security / Optional XSD validation / Conformance fixtures | `XsdConformanceCharacterizationTest` | pending |
| parse-benchmarks / Parse benchmarks / Every input checked | `ParseBenchmarkTest` | pending |
| parse-benchmarks / Parse benchmarks / Smoke run | `BenchmarkSmokeTest` | pending |
| parse-benchmarks / JMH pair for every optimization cycle / Ledger audit | task 10.2 audit | pending |

## Gate tasks to proof

| Task | Run(s) | Status |
| --- | --- | --- |
| 1.2 Plan review | `reviews/plan/` | pending |
| 2.3 Parse baseline | `benchmarks/jmh/001-parse-baseline` | done: 6 results, completeness checked, ledger rows |
| 2.2 Position-capture prototypes (D3) | `benchmarks/jmh/002-capture-off` to `006-woodstox-nocapture` | measured: every capture costs at most about 8% and 1 KB; the Woodstox gain mixes parser and factory reuse, so JDK reuse variants are measured next before D3 and D9 are decided |
| 2.4 Benchmarks gate | `benchmarks/jmh/001-parse-baseline`; `tests/002-parse-benchmarks-green` | review pending |
| 3.4 Diagnostics core gate | `reviews/code/`, `tests/` | pending |
| 4.5 XML diagnostics gate | `reviews/code/`, `tests/`, `benchmarks/jmh/` | pending |
| 5.4 Security gate | `reviews/security/`, `tests/`, `benchmarks/jmh/` | pending |
| 6.3 XSD gate | `reviews/code/`, `tests/`, `benchmarks/jmh/` | pending |
| 7.4 JSON gate | `reviews/code/`, `tests/`, `benchmarks/jmh/` | pending |
| 8.2 Optimization cycles | `benchmarks/jmh/`, `reviews/plan/` | pending |

## Runs

| Run | Kind | Command | Result |
| --- | --- | --- | --- |
| `tests/001-parse-benchmarks-red` | test (task 2.1) | `ParseBenchmarkTest` before the benchmarks | RED as intended |
| `tests/002-parse-benchmarks-green` | test (task 2.2) | `ParseBenchmarkTest`, `BenchmarkSmokeTest` | GREEN |
| `benchmarks/jmh/001-parse-baseline` | JMH, JDK 25 C2 (task 2.3) | `PolicyParseBenchmark`, `RequestParseBenchmark`, default parameters | XML request 78.7 us and 112 KB; the same request as JSON 10.4 us and 48 KB; rsa2008 policy 75 us and 114 KB |
| `benchmarks/jmh/002-capture-off` | JMH, session 3 (task 2.2) | parse, XML only | base of the prototypes; agrees with 001 |
| `benchmarks/jmh/003-capture-xlocator` | JMH prototype (a) | same | no claim on time; about +1 KB per parse |
| `benchmarks/jmh/004-capture-sidetable` | JMH prototype (b) | same | +6.6% to +8.5% time (one set disjoint); about +1 KB |
| `benchmarks/jmh/005-capture-woodstox` | JMH prototype (c) | same | -69% to -87% time, -70% to -86% bytes against 002 (static factory against a factory per parse; see 006) |
| `benchmarks/jmh/006-woodstox-nocapture` | JMH, Woodstox without capture | same | capture on Woodstox costs about 4% (no claim) and 0.8 KB |
| `benchmarks/jmh/007-capture-off-s4` | JMH, session 4 (task 2.2, D9 cycle 1) | parse, XML only | session base |
| `benchmarks/jmh/008-jdk-sax-factory-reuse` | JMH, session 4 (task 2.2, D9 cycle 1) | parse, XML only | -26% to -42% time, -54% to -64% bytes |
| `benchmarks/jmh/009-jdk-sax-parser-per-thread` | JMH, session 4 (task 2.2, D9 cycle 1) | parse, XML only | -38% to -65% time, -78% to -90% bytes (fewest bytes) |
| `benchmarks/jmh/010-jdk-stax-factory-reuse` | JMH, session 4 (task 2.2, D9 cycle 1) | parse, XML only | -34% to -58% time |
| `benchmarks/jmh/011-woodstox-nocapture-s4` | JMH, session 4 (task 2.2, D9 cycle 1) | parse, XML only | -69% to -88% time (fastest) |
| `benchmarks/jmh/012-woodstox-unmarshaller-per-thread` | JMH, session 4 (task 2.2, D9 cycle 1) | parse, XML only | at most -9% more than 011 |

## Not applicable

- 3.4 JMH: NOT APPLICABLE: group 3 adds types and formatters without changing the parse path; group 4 measures the parse-path change.
- 9.3 JMH: NOT APPLICABLE: the CLI calls the parsers without changing them.
