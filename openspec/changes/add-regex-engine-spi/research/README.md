# Research

Status: REQUIRED. The translation of XML Schema patterns to RE2 syntax, the
status codes, the engine selection, and the release facts of RE2/J depend on
standards, library, and engine facts that the source cannot answer.

## Questions and answers

### Q1. XML Schema regular-expression features in RE2 syntax

- Answer: see `rx1-opus-xsd-to-re2.md` (translation table, rejections R1/R2, Unicode skew, today's `jdk` drifts D1 to D8); adopted in design D1, D3.
- Sources: in the report, each with URL or file:line and retrieval date 2026-10-27.
- Confidence: per the report's confidence section; every engine-behavior claim is from source reading until task 2.1 runs.
- Disagreement / drift risk: XACML 3.0 A.3.13 cites the XPath functions, whose
  flags and anchoring differ from XML Schema `pattern` facets.

### Q2. Conformance cases that use the regexp-match functions

- Answer: see `rx3-sol-conformance-inventory.md`: 9 `string-regexp-match` cases per OASIS suite (IIB008, IIB009, IIC056, IIC057, IIC165 to IIC169) plus 2 deprecated fixtures; RSA 2008 has none; all patterns are ASCII literals, `.`, `*`, `|`.
- Sources: local fixtures (read only), file:line in the report.
- Confidence: per the report's confidence section; every engine-behavior claim is from source reading until task 2.1 runs.
- Disagreement / drift risk: none known.

### Q3. RE2/J releases, license, JDK support, Unicode classes

- Answer: see `rx5-terra-re2j-performance.md`: 1.8 is the latest release (Maven Central, updated 2025-01-09), BSD-3, Java 6 bytecode, no runtime dependencies; Unicode 6.0 tables; issues 168 and 198. Local note: the local Maven repository holds 1.7; 1.8 needs one online build.
- Sources: in the report, each with URL or file:line and retrieval date 2026-10-27.
- Confidence: per the report's confidence section; every engine-behavior claim is from source reading until task 2.1 runs.
- Disagreement / drift risk: record the version per claim.

### Q4. Engine selection; what other XACML engines do

- Answer: see `rx2-fable-limits-selection.md` (options A to F; recommends a provider-level engine), `rx4-astra-spi-design.md` (SPI shape, call-site binding, cache), `rx6-agy-engines.md` (AuthzForce, AT&T/OpenAZ/ONAP, Balana, SNE-XACML: no SPI, no limits; AuthzForce compiles constants at load). Design D2 (Open Question 1).
- Sources: in the report, each with URL or file:line and retrieval date 2026-10-27.
- Confidence: per the report's confidence section; every engine-behavior claim is from source reading until task 2.1 runs.
- Disagreement / drift risk: engine docs can lag their code; cite code.

### Q5. Limits and status codes per engine

- Answer: see `rx2-fable-limits-selection.md`: all four limits stay; RE2/J adds nested-repetition and program-size bounds; recommended statuses S2 (syntax-error for malformed, processing-error for unsupported and limits). Design D5 (Open Question 3).
- Sources: in the report, each with URL or file:line and retrieval date 2026-10-27.
- Confidence: per the report's confidence section; every engine-behavior claim is from source reading until task 2.1 runs.
- Disagreement / drift risk: none known.

## Lanes

| Lane | Profile / model | Report | Status |
| --- | --- | --- | --- |
| conductor | Claude Code, Claude Opus 5.5 | this file | started 2026-10-27 |
| RX1 | Claude Opus 5.5 | `rx1-opus-xsd-to-re2.md` (Q1) | done |
| RX2 | Claude Fable 5.1 | `rx2-fable-limits-selection.md` (Q4, Q5) | done |
| RX3 | Codex GPT-6.1 Sol | `rx3-sol-conformance-inventory.md` (Q2) | done after one follow-up |
| RX4 | Codex GPT-6 Astra | `rx4-astra-spi-design.md` (SPI design) | done after one follow-up |
| RX5 | Codex GPT-6.1 Terra | `rx5-terra-re2j-performance.md` (Q3) | done |
| RX6 | Agy, Gemini 4 Argon | `rx6-agy-engines.md` (Q4 in other engines) | done |

## Synthesis

`synthesis.md`; the design, the spec `regex-engine-spi`, and `tasks.md` follow it.
