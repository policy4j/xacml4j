## Why

Policies and requests are parsed on every policy load and on every decision
request, but a parse error today says what went wrong without saying where:
the JAXB mappers build the model from objects that carry no source position,
so 114 `SyntaxException` sites in main code report a message only, and the
first error stops the parse. A bad unknown root element even escapes as
`IllegalArgumentException` (`XacmlPolicyUnmarshaller.create`). The XML
hardening is uneven across entry points (`BaseJAXBUnmarshaller` allows an
internal DTD subset; `DOMUtil` builds an unhardened `TransformerFactory` and
logs and continues when its factory setup fails), no path limits depth or
size, and JSON parsing (Gson 2.8.9) has no limits at all. Parse cost is also
unmeasured: the smoke runs of `add-pdp-jmh-benchmarks` show `importXml` near
350 us per policy, and each parse creates a new `SAXParserFactory` and JAXB
`Unmarshaller`.

## What Changes

- **Compiler-like diagnostics** for XACML 2.0 and 3.0 XML and XACML JSON
  policies and requests: each problem has a severity, a stable code, a
  message, the source name, line, and column, the element or JSON path, the
  source line with a caret, and notes or "did you mean" hints. A parse
  collects up to a configurable number of errors instead of stopping at the
  first one where the input allows it (well-formedness errors still stop the
  parse). Output formats: a GCC/Clang-style text form; a machine form
  (SARIF 2.1.0 or JSON, research Q5).
- `SyntaxException` keeps its constructors and gains the list of
  diagnostics; its message starts with the first diagnostic's location.
  `Unmarshaller` gains a default method that takes parse options (source
  name, limits, optional XSD validation, maximum errors, warnings as errors);
  existing calls behave as today apart from the hardening below.
- **XML parsing security** on every XML entry point (SAX/JAXB, DOM, StAX,
  `Transformer`, schema validation): one owner of hardened factories,
  secure processing on, external DTDs, entities, schemas, stylesheets, and
  XInclude off, and fail-closed setup. **BREAKING**: a document with a
  DOCTYPE is rejected (today the SAX path accepts an internal DTD subset; no
  OASIS or RSA conformance fixture has a DOCTYPE, checked 2026-10-27).
  Configurable limits with safe defaults on every path, including callers'
  own readers and DOM nodes: document size, element depth, element count,
  attributes per element, attribute and text length, name length.
- **XSD validation is optional and off by default.** When enabled, the
  bundled OASIS schemas are compiled once, resolved only from the class path,
  and schema errors become diagnostics with positions.
- **JSON parsing**: the same limits (nesting depth, string, number, and name
  length, document size), duplicate member names rejected, and positions in
  diagnostics. A framework bake-off decides the JSON provider: Gson 2.15.0 in
  place, Jackson 2.22.4 streaming, Jackson 3.2.4, and candidates from the
  discovery swarm, judged on JMH time and allocation, diagnostics quality,
  limits support, and maintenance. A chosen new provider ships as a new
  module next to `xacml-json`; the Gson module is deprecated after parity,
  not removed in this change.
- **Parse performance**: new JMH parse benchmarks in `xacml-jmh`, a recorded
  baseline, and optimization cycles until the stop rule holds (no own-code
  frame or allocation site above 5% that can be removed without changing a
  parse result, or two rounds under 5% each). **Every optimization cycle has
  a JMH before/after pair** in the change's ledger (`-prof gc`, C2, back to
  back). Candidates: reused parser factories and unmarshallers, schema reuse,
  fewer intermediate objects in the mappers, and a direct StAX-to-model
  parser that bypasses JAXB, if measurement favors it.
- Optional, pending approval: a command-line checker (`xacml-check`) that
  prints diagnostics like a compiler and exits non-zero on errors (Open
  Question 1 of the design).

## Capabilities

### New Capabilities

- `parse-diagnostics`: positioned, coded, multi-error diagnostics for policy
  and request parsing, with text and machine formats.
- `parse-security`: hardened XML and JSON parsing with limits on every entry
  point; optional XSD validation.
- `parse-benchmarks`: JMH benchmarks of policy and request parsing, and the
  before/after rule for optimization cycles.

### Modified Capabilities

None (no main spec under `openspec/specs/` covers parsing).

## Research Questions

- Q1. JDK 21 XML processing limits (`jdk.xml.*`), their defaults, and which
  factory properties enforce them for SAX, DOM, StAX, `Transformer`, and
  `SchemaFactory`; changes in later JDKs (JDK XML configuration file).
- Q2. OWASP and JDK guidance for each factory type; which settings remain
  the caller's when a caller passes its own `XMLReader`, StAX reader, or DOM.
- Q3. Source positions for JAXB objects: XJC `-Xlocator` with
  `maven-jaxb2-plugin` 0.15.3 and JAXB RI 2.3.x, and its cost; versus a
  direct StAX parser.
- Q4. JSON limits and positions in Gson 2.15.0, Jackson 2.22.4
  (`StreamReadConstraints`, duplicate detection, `JsonLocation`), and
  Jackson 3.2.4; techniques that open-source projects use for fast Jackson
  parsing (streaming over databind, factory reuse, field-name matching,
  Blackbird); maintenance state of DSL-JSON 2.0.2 and simdjson-java 0.4.0.
- Q5. Diagnostic formats: GCC/Clang and rustc text conventions, SARIF 2.1.0
  (OASIS) for tools.
- Q6. Which conformance fixtures validate against the bundled OASIS XSDs
  (characterization, read-only), so the XSD option's tests match reality.
- Q7. Limits and diagnostics in other open-source XACML engines (for
  example AuthzForce, AT&T XACML, Balana), and published XML/JSON parsing
  techniques (arXiv and vendor docs), from the discovery swarm.

## Verification

- Unit tests per requirement, including a catalog of broken policies and
  requests with golden diagnostic output, and an attack corpus (XXE through
  file and URL, entity expansion, deep nesting, oversized attributes and
  text, many attributes, external schema and XInclude, caller-supplied
  readers).
- Full reactor `clean test` on JDK 21; OASIS XACML 2.0/3.0 and RSA 2008
  conformance unchanged (937 run, 12 skipped); JSON conformance parity for a
  new JSON provider.
- JMH: a parse baseline, and a before/after pair for every optimization
  cycle and for every group that changes the parse path, recorded in
  `evidence/benchmarks/jmh/LEDGER.md`.

## Impact

- Modules: `xacml-core` (diagnostics model, `SyntaxException`,
  `Unmarshaller` default method, `DOMUtil`, hardened factories),
  `xacml-xml` (parsers, mappers, optional XSD), `xacml-json` (limits,
  positions), `xacml-jmh` (parse benchmarks), possibly a new JSON provider
  module and a checker module (approval).
- Public API: additions only, apart from the **BREAKING** DOCTYPE
  rejection, the unknown-root `SyntaxException` (was
  `IllegalArgumentException`), and `SyntaxException` messages that now start
  with a location.
- Dependencies needing the maintainer's approval: a JSON framework (Jackson
  2.22.4 or 3.2.4) if the bake-off picks it; Woodstox 7.3.0 only if the
  StAX parser needs it. Gson's own upgrade belongs to change
  `upgrade-dependencies`.
- Generated code: an XJC option (`-Xlocator`) changes the generated classes
  through the POM only; the vendored XSDs and `xacml.xjb` stay unchanged.
- Vendored files: read-only; the conformance fixtures are only read (Q6).
- Docs: `docs/ai/TESTING.md`, `docs/ai/BENCHMARKS.md`, module READMEs.

## Approval

Approved by the maintainer on 2026-10-27, including the **BREAKING** items and the
dependency decisions. Decisions given with the approval:

- The checker is kept and becomes a policy compiler front end (`xacmlc`):
  parse, link (resolve references across a policy directory), and check,
  with compiler-like diagnostics, built as a GraalVM native executable on
  JDK 25 (Oracle GraalVM 25.0.3 is installed). Group 9 of the tasks.
- The project baseline moves to JDK 25 (change `adopt-jdk25-baseline`);
  this change builds, tests, and measures on JDK 25.
- "Analyze allocations and optimize them to the limits": the parse
  optimization cycles treat every removable own-code allocation site as a
  target, not only sites above 5% (design D9).
- Plan review (task 1.2) still runs; a finding that changes scope goes back
  to the maintainer.

## Tracking

Beads epic `policy4j-uunt` (created with this proposal at the maintainer's
request); task X.Y maps to issue `<epic>.X.Y`. Approval: implementation
starts only after the maintainer approves this proposal.
