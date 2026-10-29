# Research

Status: REQUIRED. Parser limits, factory settings, JAXB position support, and
the JSON framework choice depend on JDK, library, and standard facts that the
live source cannot answer.

## Questions and answers

### Q1. JDK 21 XML processing limits and factory properties

- Answer: see `dq2-fable-xml-security.md` §Q1 (JDK 21 and 25 limits, defaults, properties per factory, the JDK XML configuration file); adopted in design D13 (D6).
- Sources: pending (Java SE 21 "Java API for XML Processing (JAXP) Security Guide").
- Confidence: pending.
- Disagreement / drift risk: JDK versions after 21 changed XML defaults; record the version per claim.

### Q2. Factory guidance per type; what a caller's own parser keeps

- Answer: see `dq2-fable-xml-security.md` §Q2; adopted in design D13 (D6).
- Sources: pending (OWASP XML External Entity Prevention Cheat Sheet; JDK docs).
- Confidence: pending.
- Disagreement / drift risk: none known.

### Q3. Source positions for JAXB objects (`-Xlocator`) and their cost

- Answer: see `dq4-astra-jaxb-positions.md`. `-Xlocator` (XJC 2.3.7 through plugin 0.15.3 `<args>`) adds a `Locator` field, `@XmlLocation`, and the RI interface `com.sun.xml.bind.Locatable` to each generated class; RI 2.3.8 copies a `LocatorImpl` per unmarshalled bean (no switch to turn it off per parse); the generated imports need `jaxb-impl` at compile scope. SAX and JDK StAX positions are event-end positions, optional, and not the opening `<` or an attribute; Woodstox 7.3.0 has event-start positions. Adopted in design D13 (D3, D4, D9). Cost per bean is unmeasured (prototype and JMH in task 2.2).
- Sources: XJC 2.3.7 `SourceLocationAddOn`, RI 2.3.8 `ClassBeanInfoImpl` and `StructureLoader`, plugin 0.15.3 POM and `AbstractXJC2Mojo`, Java 25 `Locator` and `Location` API docs, Woodstox 7.3.0 `StreamScanner` and `ReaderConfig` (all retrieved 2026-10-27; URLs in the report).
- Confidence: high for the generated members, the snapshot per bean, and the position contracts; medium for the cost ranking of the candidates; none for time and allocation until task 2.2 runs.
- Disagreement / drift risk: JAXB RI 2.3.x is the last `javax` line; Context7 served Jakarta and Woodstox 7.2 docs, so the report uses the exact tagged sources instead.

### Q4. JSON limits and positions per framework; open-source Jackson techniques

- Answer: see `dq3-sol-json.md` (limits, positions, profile gaps, provider selection) and `dq2-fable-xml-security.md` recommendation 11; adopted in design D13 (D8). Versions (Maven Central `maven-metadata.xml`, retrieved
  2026-10-27): Gson 2.15.0 (repository updated 2026-10-24), Jackson
  `com.fasterxml.jackson.core:jackson-databind` 2.22.4 (2026-10-17), Jackson
  `tools.jackson.core:jackson-databind` 3.2.4 (2026-10-18), DSL-JSON 2.0.2
  (last update 2023-08-29), simdjson-java 0.4.0 (2025-09-15).
- Sources: Maven Central metadata as above; Gson: `Strictness` API since 2.11.0 and `JsonReader#setNestingLimit` on the main branch (https://github.com/google/gson/discussions/2677, retrieved 2026-10-27); other framework docs pending (Context7).
- Confidence: high for the versions; pending for the features.
- Disagreement / drift risk: Jackson 3 changes packages (`tools.jackson`) and the configuration model.

### Q5. Diagnostic formats (GCC/Clang, rustc, SARIF 2.1.0)

- Answer: see `dq1-opus-diagnostics.md` §Q5 (GNU form, code at the end for tool regexes, SARIF 2.1.0 for code scanning); adopted in design D13 (D5).
- Sources: pending (GCC diagnostic format docs; OASIS SARIF 2.1.0).
- Confidence: pending.
- Disagreement / drift risk: none known.

### Q6. Schema validity of the conformance fixtures

- Answer: pending (characterization, task 6.1; fixtures are read only).
- Sources: local run.
- Confidence: pending.
- Disagreement / drift risk: some OASIS fixtures are known to be hand-written; expect failures.

### Q7. Limits and diagnostics in other XACML engines; parsing techniques in the literature

- Answer: see `dq6-agy-engines.md` (AuthzForce, AT&T XACML, Balana from fetched sources; arXiv techniques) and `dq5-agy-parse-performance.md`; adopted in design D13.
- Sources: pending (project repositories and docs; arXiv).
- Confidence: pending.
- Disagreement / drift risk: engine docs can lag their code; cite code where possible.

## Lanes

| Lane | Profile / model | Report | Status |
| --- | --- | --- | --- |
| conductor | Claude Code, Claude Opus 5.5 | this file (versions in Q4) | started 2026-10-27 |
| DQ1 | `policy4j_jmh` base, Claude Opus 5.5 | `dq1-opus-diagnostics.md` (Q5; D1, D2, D4, D5) | done |
| DQ2 | Claude Fable 5.1 | `dq2-fable-xml-security.md` (Q1, Q2; D6) | done |
| DQ3 | Codex GPT-6.1 Sol | `dq3-sol-json.md` (Q4; D8) | done after one follow-up |
| DQ4 | Codex GPT-6 Astra | `dq4-astra-jaxb-positions.md` (Q3; D3, D4, D9) | done after two follow-ups |
| DQ5 | Agy, Gemini 4 Argon (assigned instead of Terra, who was reviewing) | `dq5-agy-parse-performance.md` (parse performance; D7, D9) | done |
| DQ6 | Agy, Gemini 4 Argon | `dq6-agy-engines.md` (Q7; D3, D6 to D9) | done |

## Synthesis

`synthesis.md`: what each lane changed in the design (D13) and the questions left for the maintainer.
