## Context

- XML parse path: `BaseJAXBUnmarshaller.unmarshal` (`xacml-xml/src/main/java/org/xacml4j/v30/xml/BaseJAXBUnmarshaller.java:82`)
  creates a JAXB `Unmarshaller` per call (`:86`), installs a
  `ValidationEventHandler` that only logs (`:87`), and for byte and stream
  input builds a new `SAXParserFactory` per call (`secureReader`, `:162`).
  The SAX reader rejects external entities and external DTDs but accepts an
  internal DTD subset (`startDTD`, `:181`). Caller-supplied `SAXSource`
  readers, StAX readers, and DOM nodes keep the caller's settings (`:98`,
  `:138`).
- Mapping: `Xacml30PolicyFromJaxbToObjectModelMapper`,
  `Xacml20PolicyFromJaxbToObjectModelMapper`,
  `Xacml30RequestContextFromJaxbToObjectModelMapper`, and
  `Xacml20RequestContextUnmarshaller` walk JAXB objects without positions
  and throw `SyntaxException` at the first problem (114 construction sites
  in main code). `XacmlPolicyUnmarshaller.create` throws
  `IllegalArgumentException` for an unknown root (`:95`).
- `SyntaxException` (`xacml-core/src/main/java/org/xacml4j/v30/SyntaxException.java:35`)
  carries a `Status` and a message.
- `DOMUtil` (`xacml-core/src/main/java/org/xacml4j/util/DOMUtil.java:84`):
  a hardened `DocumentBuilderFactory` (DOCTYPE disallowed) and an
  unhardened `TransformerFactory`; a setup failure is logged and ignored
  (`:110`).
- `XPathProvider` sets secure processing (`xacml-core/.../content/XPathProvider.java:209`).
- JSON: Gson 2.8.9 adapters in `xacml-json` (`JsonRequestContextUnmarshaller.java:58`),
  no limits; Gson errors carry a path and position only in their message. Found while
  writing the parse benchmarks (evidence `tests/002-parse-benchmarks-green`): `xacml-json` has
  no `MarshallingProvider` (JSON is not discoverable through the SPI);
  `JsonRequestContextUnmarshaller.unmarshal` returns `null` for any source other than a
  `Reader` or `JsonElement`; `JsonRequestContextMarshaller.marshal(source, OutputStream)`
  never flushes its `OutputStreamWriter`. Group 7 fixes all three with tests.
- JAXB: one cached `JAXBContext` (`JAXBUtils.java:40`); XJC runs through
  `maven-jaxb2-plugin` 0.15.3 over vendored XSDs.

## Goals / Non-Goals

**Goals:** positioned, coded, multi-error diagnostics; one hardened factory
owner with limits on every path; optional XSD validation; JSON limits and
positions; a JSON provider chosen by measurement; parse benchmarks and
optimization cycles with a JMH pair each.

**Non-Goals:** XPath expression cost limits in `AttributeSelector`
evaluation (evaluation, not parsing); a Jakarta XML Binding migration (change
`upgrade-dependencies` decides it); removing the Gson module; editing the
vendored XSDs or `xacml.xjb`.

## Research Basis

`research/README.md` holds Q1 to Q7; the discovery swarm (task 1.1) writes
lane reports under `research/` and `research/synthesis.md`. Until then the
limits in D6 and the JSON choice in D8 are proposals, marked as such.

## Decisions

### D1. Diagnostics model in `xacml-core`

New package `org.xacml4j.v30.marshal.diagnostics`: `Severity` (ERROR,
WARNING, NOTE), `SourcePosition` (source name, line, column, both 1-based;
optional character offset), `Diagnostic` (severity, `DiagnosticCode`,
message, position, path, notes), `DiagnosticCode` (stable ids by family:
`XML0nn` well-formedness, `XSD1nn` schema, `POL2nn` policy model, `REQ3nn`
request model, `LIM4nn` limits and security, `JSN5nn` JSON), and
`DiagnosticCollector` (maximum errors, warnings-as-errors). Alternatives:
reuse `Status` detail (no positions, no multiple entries); a third-party
diagnostics library (none fits XACML codes). Records and enums keep the model
small and value-class friendly for the JDK 28 work.

### D2. Exception and options stay compatible

`SyntaxException` gains `List<Diagnostic> getDiagnostics()` and a constructor
from a collector; its message starts with the first diagnostic's text line.
`Unmarshaller<T>` gains `default T unmarshal(Object source, ParseOptions
options)`; the one-argument method calls it with defaults. `ParseOptions`
(immutable builder) holds the source name, limits (D6), XSD validation (D7,
off by default), maximum errors (100), warnings as errors (false), and the
strict caller-parser option. Alternative: a new `ParseResult<T>` return type
(breaking for implementers of `Unmarshaller`); rejected.

### D3. Source positions for XML model errors

Two separate choices (DQ4): how positions are captured and how XML is bound.
Capture candidates, decided by task 2.2 (prototype plus JMH on valid input):
(a) XJC `-Xlocator`, set through the POM (`<args>`), which gives every
generated class a `Locator` snapshot and needs `jaxb-impl` at compile scope;
(b) a parse-local side table that a SAX filter fills and a JAXB
`Unmarshaller.Listener` (`beforeUnmarshal`) keys by the target object;
(c) JAXB over a Woodstox 7.3.0 `XMLStreamReader`, which reports event-start
positions. Binding stays JAXB; a direct StAX parser to the model is a later
D9 cycle, not a position mechanism. A position has a kind: `start` (the
opening `<` of an element, or an attribute name or value, found by scanning
the kept source text from the parser position), `event` (the parser's event
position, when no source text is kept), or `unavailable` (caller DOM and JAXB
input). Element paths come from a small path stack kept by the mappers
(`/PolicySet[1]/Policy[2]/Rule[@RuleId='r1']`).

### D4. Collect instead of throw in the mappers

Mapper methods report to the collector and return an empty result for the
faulty element; the parent skips it and continues with the next sibling; the
unmarshaller throws `SyntaxException` with all diagnostics at the end when any
error was collected. Well-formedness and limit errors stop at once. The 114
sites move one mapper at a time (tasks 4.2 to 4.4), each with its catalog
documents. Unknown root: `SyntaxException` with `POL201`.

### D5. Text and machine formats

Text: `<source>:<line>:<col>: error[POL203]: unknown function "..."`, the
source line, a caret line, then `note:`/`help:` lines (GCC/Clang and rustc
conventions, research Q5). Source lines come from the bytes the parser read
(kept for byte and stream input up to the document limit; for caller readers
and DOM no excerpt is shown). Hints use Damerau-Levenshtein distance over the
function, data-type, and algorithm registries. Machine: SARIF 2.1.0 written
with the JSON provider chosen in D8, unless Q5 finds a reason against it.

### D6. One hardened factory owner and limits (defaults are proposals)

`org.xacml4j.util.SecureXml` in `xacml-core` creates SAX, DOM, StAX,
`Transformer`, `SchemaFactory`, and `Validator` instances: secure processing
on; `disallow-doctype-decl` (SAX, DOM) or `SUPPORT_DTD=false` (StAX);
external general and parameter entities off; `ACCESS_EXTERNAL_DTD`,
`ACCESS_EXTERNAL_SCHEMA`, and `ACCESS_EXTERNAL_STYLESHEET` empty; XInclude
off; JDK limit properties set from `ParseOptions` (Q1). Setup failures throw.
`DOMUtil` and `BaseJAXBUnmarshaller` use it. A limits filter (SAX filter and
StAX delegate, as the boolean filter is today) counts depth, elements,
attributes, and lengths on every path, including caller readers and DOM.
Proposed defaults: document 64 MiB, depth 256, elements 2,000,000,
attributes per element 256, attribute value 1 MiB, text node 8 MiB (request
`Content`), name 1,000 characters; JSON depth 256, string 8 MiB, number 1,000
digits, member name 1,000 characters. Q1, Q4, and Q7 confirm or change them.
**BREAKING**: DOCTYPE is rejected on every path (none of the conformance
fixtures has one; checked 2026-10-27 with `rtk rg -l '<!DOCTYPE'`, 0 files).

### D7. Optional XSD validation

Off by default (maintainer decision 2026-10-27). When on, a `Schema` per
XACML namespace is compiled once (thread-safe `Schema`, a `Validator` per
parse), schema imports resolve through an `LSResourceResolver` over the
bundled `xsd/` resources only, and validation runs as a SAX filter in the
same pass as the unmarshal (no second parse); its `ErrorHandler` collects
schema errors as `XSD1nn` diagnostics and continues. Q6 records which
conformance fixtures are schema-valid before tests assert on them.

### D8. JSON provider by bake-off

Candidates: Gson 2.15.0 (in place), Jackson 2.22.4 streaming
(`JsonParser`, `StreamReadConstraints`, `STRICT_DUPLICATE_DETECTION`,
`JsonLocation`), Jackson 3.2.4 (`tools.jackson`), and any candidate the
discovery swarm shortlists (DSL-JSON 2.0.2 and simdjson-java 0.4.0 are
listed; both carry maintenance or preview-API risk, Q4). Criteria, in order:
correct XACML JSON profile behavior (existing `xacml-json` tests and a JSON
conformance parity run), limits and positions, JMH time and allocation per
request, dependency weight and maintenance. Open-source techniques to try
with Jackson: a hand-written streaming reader instead of databind, one shared
`JsonFactory`, pre-encoded field names (`SerializedString`), no
intermediate tree. A chosen non-Gson provider ships as module
`xacml-json-jackson` with its own `MarshallingProvider`; provider selection
for the JSON media type is explicit (a `ParseOptions` or builder setting,
default the new provider after parity), and the Gson module is marked
deprecated. Alternative: replace Gson in place (breaks users of the Gson
adapters); rejected.

### D9. Parse optimization cycles

After the baseline (task 2.3), each cycle: profile the parse benchmarks
(async-profiler CPU and allocation, swarm analysis), pick one hot spot, write
the change with a test first, record the JMH before/after pair, gate. First
candidates from source reading: one `SAXParserFactory` per JVM and a
`SAXParser` per thread (today one factory per parse), an `Unmarshaller` per
thread, compiled `Schema` reuse, eager `String.format` and stream use in the
mappers, Guava builders on hot paths, interning of ids and data-type URIs,
and a direct StAX-to-model parser (Woodstox 7.3.0 or the JDK parser) as a
larger cycle that also gives native positions (D3 b). Stop rule: no own-code
frame above 5% that can be removed without changing a parse result, or two
rounds under 5% each; and for allocation (maintainer, 2026-10-27: "optimize
them to the limits") no own-code allocation site at any share that can be
removed without changing a parse result or the public API.

### D10. Optional checker CLI

A `xacml-check` main class (module decided in Open Question 1) that parses
files or directories, prints the text format, writes SARIF on request, and
exits 1 on errors, 0 otherwise.


### D13. Changes from the discovery swarm (task 1.1, reports in `research/`)

| Decision | Change | Source |
| --- | --- | --- |
| D1 | A diagnostic has a span (start and end line and column, 1-based) instead of a point, typed children (`note`, `help`), and a typed logical location (XML path or JSON Pointer); the model is `Serializable`. Families add `VAL` (value syntax), `RSP` (response parsing), and `DIA` (diagnostics engine) | DQ1 |
| D4 | JAXB validation events, today logged and dropped (`BaseJAXBUnmarshaller.java:87`), go to the collector, so a misspelled element no longer vanishes silently | DQ1 |
| D5 | Text form `<source>:<line>:<col>: error: <message> [<CODE>]`, code at the end of the line, so the GCC-style pattern that VS Code and GitHub Actions use matches; SARIF 2.1.0 written by a small built-in writer, independent of the JSON provider of D8; SAX and `-Xlocator` positions point at the end of a start tag, so the caret position scans back in the source text | DQ1 |
| D6 | Default limits (sized from the corpora): document 16 MiB, depth 100, elements 500,000, attributes per element 200, attribute value 1 MiB, text node 8 MiB, name 1,000 characters, total characters 16 Mi; JSON depth 255, string 8 MiB, number 1,000 digits, member name 1,000, document 16 MiB. Factories from `newDefaultInstance()` (a provider on the class path cannot replace them), configured once per JVM, a parser per use; JDK limit errors (`JAXP00010001` to `JAXP00010007`) mapped to `LIM4nn` with their position; a source with only a system identifier is rejected unless an option lets `SecureXml` open it under the same rules; fail fast when `com.sun.xml.bind.disableXmlSecurity` is set; `SecureXml` settings override `java.xml.config.file` and `jaxp.properties` (documented) | DQ2 |
| D7 | `Schema` compiled lazily, once per JVM, only when validation is on; a `ValidatorHandler` per thread; expected cost with validation +50% to +150% time over the optimized non-validating path (to be measured); validation off costs nothing | DQ5 |
| D8 | Providers get stable identities (`gson`, `jackson2`, `jackson3`) and are selected at the factory layer (today's discovery map keeps the first provider per media type, `MarshallingProvider.java:72`); the three known defects and the same `null` return in `JsonResponseContextUnmarshaller` are fixed before the bake-off; first bake-off gate is the XACML JSON Profile itself (datatype inference, shorthand category arrays, today missing in `AttributeDeserializer` and `RequestContextAdapter`); strict RFC 8259 mode; Gson 2.15 offers character offsets and `setNestingLimit` (default 255), Jackson offers `StreamReadConstraints` and duplicate detection | DQ2, DQ3 |
| D9 | First cycles, each with its JMH pair: (1) one hardened `SAXParserFactory` per JVM, a `SAXParser` and JAXB `Unmarshaller` per thread, and one `PolicyUnmarshaller` per `PolicyImportTool` media type; (2) a single-pass streaming JSON request reader; (3) mapper collections (`LinkedList`, streams), `CategoryId.getById` `Optional` chains, interned standard attribute ids; (4) the direct StAX parser (D3 b). The lanes' gain estimates (cycle 1: about half of the parse time and allocation) are hypotheses for the pairs | DQ5, DQ6 |

| D3 | Capture and binding are separate choices; three capture candidates (`-Xlocator`, SAX side table, JAXB over Woodstox) are prototyped and measured before one is chosen; positions carry a kind (`start`, `event`, `unavailable`); acceptance cases: multi-line start tags, repeated siblings, attributes on their own lines, CRLF, non-ASCII text, missing locations | DQ4 |
| D4 | An invalid child is not an absent child: an invalid `Condition` or `Target` makes its enclosing rule or policy invalid instead of becoming `null` (absent); variable resolution restores the `VariableManager` stack after a failure and marks the failed definition so dependent errors are not repeated; diagnostics carry a document ordinal so their order and the cap of 100 are deterministic (`VariableManager` iterates a `HashMap`); only known input failures are mapped, not every `RuntimeException` | DQ4 |
| D9 | Each capture candidate gets a pair against capture off with the same security, XSD, and provider settings; Woodstox is configured option by option (`configureForSpeed()` turns location tracking off); factory and parser reuse are measured apart from `Unmarshaller` reuse; the temporary 3.0 JAXB value objects of the 2.0 request path (`Xacml20RequestContextUnmarshaller`) are a mapper-cycle candidate; a direct parser starts with the 3.0 request, kept beside the JAXB path for a differential test | DQ4 |

## Test Strategy

- Broken-document catalog: `xacml-xml/src/test/resources/diagnostics/` and
  `xacml-json/src/test/resources/diagnostics/` (new, project-owned), one
  document per diagnostic code with a golden text file;
  `DiagnosticGoldenTest` compares output exactly.
- Unit tests named in the specs: `PolicyDiagnosticsTest`,
  `RequestDiagnosticsTest`, `JsonRequestDiagnosticsTest`,
  `DiagnosticFormatterTest`, `SyntaxExceptionTest`, `SecureXmlFactoriesTest`,
  `XmlAttackCorpusTest` (with a local HTTP server bound to 127.0.0.1 that
  records requests), `ParseLimitsTest`, `JsonParseLimitsTest`,
  `XsdValidationTest`, `XsdConformanceCharacterizationTest`,
  `ParseBenchmarkTest`.
- Characterization first: current messages and exception types for the
  catalog documents (task 3.1), and current behavior for DOCTYPE documents.
- Conformance: OASIS 2.0/3.0 and RSA 2008 counts unchanged after every
  group; JSON parity run for a new JSON provider.

## Benchmark Plan

JMH applies: parsing is on the policy-load and request paths. New benchmarks
in `xacml-jmh/src/main/java/org/xacml4j/jmh/parse/` (spec
`parse-benchmarks`), inputs from the existing corpora (policy sources as
bytes; requests written back to XML and JSON by the existing marshallers in
setup). Baseline run before any parser change (task 2.3). A before/after pair
for every group that changes the parse path (groups 4 to 8) and for every
optimization cycle; flags as in the `add-pdp-jmh-benchmarks` Benchmark Plan
(C2 through `-XX:-UseJVMCICompiler` on GraalVM, `-prof gc`, 3 forks x 5
iterations, `-foe true`, no agent session alive). Ledger:
`evidence/benchmarks/jmh/LEDGER.md`.

## Code Intelligence Plan

- `SyntaxException` sites: `ast-grep run --lang java --pattern 'new SyntaxException($$$)'`
  over `xacml-*/src/main/java`, cross-checked with Serena references to the
  constructors; every site is listed in the task that moves it.
- XML factory owners: `rtk rg` for `DocumentBuilderFactory|SAXParserFactory|XMLInputFactory|TransformerFactory|SchemaFactory`
  in main code plus an ast-grep rule `java-xml-factory-outside-securexml`
  (with valid and invalid cases) so new direct factory calls fail the scan.
- `Unmarshaller` implementers: Serena `find_implementations` on the type,
  then the overrides read in source.

## Execution Plan

- Task 1.1 discovery swarm (NTM, six lanes at most, read-only, run when no
  JMH run is active): Claude Opus 5.5 (diagnostics model and formats),
  Claude Fable 5.1 (XML security and limits), Codex GPT-6.1 Sol (JSON
  frameworks and techniques), Codex GPT-6 Astra (JAXB positions and StAX
  parser), Codex GPT-6.1 Terra (parse performance and literature), Agy
  Gemini 4 Argon (other XACML engines, standards). Plan review (task 1.2)
  by lanes from other families than the author.
- Implementation lanes later own disjoint files: diagnostics core
  (`xacml-core` marshal package), XML mappers (one lane per mapper), security
  (`SecureXml`, `DOMUtil`, `BaseJAXBUnmarshaller`), JSON (`xacml-json` and
  the new module), benchmarks (`xacml-jmh/.../parse`). Reviewers from other
  families than each author; lanes idle during JMH runs.

## Risks / Trade-offs

- [`-Xlocator` adds a field and a copy per JAXB object] → measured on valid
  input before it becomes default; the StAX cycle can replace it.
- [Collecting errors can cascade into noise] → a faulty element is skipped
  as a whole; references to it are reported once.
- [DOCTYPE rejection breaks users with internal DTDs] → **BREAKING** in the
  release notes; no option to re-enable entity expansion.
- [Limits reject large legitimate policies] → defaults sized from the
  corpora and research, all configurable, limit diagnostics name the option.
- [Two JSON providers for one media type] → explicit selection; the Gson
  module stays until removed by a later change.

## Migration and Rollback

Diagnostics and options are additions. The behavior changes (DOCTYPE,
unknown-root exception type, message prefix) are listed in the release
notes. Each group lands on its own; rolling back a group reverts its files.
The Gson provider stays available.

## Open Questions

1. Resolved 2026-10-27: kept, as the policy compiler front end `xacmlc`
   in a new module `xacml-cli`, built as a GraalVM 25 native executable
   (D10).
2. Default JSON provider after the bake-off: switch the default, or keep
   Gson as default and make the new provider opt-in for one release.
3. Severity of non-standard but accepted constructs (for example unknown
   attribute categories in requests): warning or note.
