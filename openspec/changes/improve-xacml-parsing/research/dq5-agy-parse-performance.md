<!-- Discovery lane DQ5 (task 1.1): Agy, Gemini 4 Argon (high), read-only (assigned to Agy instead of Terra so no lane idled); parse performance and literature, design D7 and D9. -->

FINAL REPORT (policy4j_bench_agy_argon, Agy Gemini 4 Argon):

## Q7 (Performance & Literature Focus): Parse Performance, JAXB/JSON Costs, Factory & Schema Reuse, Interning, XSD Validation Cost, and JMH Benchmark Design

### 1. Answer

#### 1.1 Published XML and JSON Parsing Techniques with Measured Results — `SUPPORTED`

| Study / Benchmark | Technique Compared | Measured Result | Relevance to `xacml4j` (`D3`, `D7`, `D8`, `D9`) |
| --- | --- | --- | --- |
| **Keiser & Lemire (2024)**, *"On-Demand JSON: A Better Way to Parse Documents?"*, `arXiv:2312.17149v3` (*Softw. Pract. Exper.* 54(6)) | Single-pass streaming pull iteration ("On-Demand") vs. two-pass intermediate DOM tree materialization (`simdjson DOM`, `yyjson`, `RapidJSON`, `nlohmann/json`) on Xeon Gold 6338 (3.2 GHz, GCC 12 `-O3`). | Geometric mean throughput: **3.3–3.6 GiB/s** (On-Demand) vs. **1.9–2.1 GiB/s** (`simdjson DOM`, **1.70x faster**, **40% fewer retired CPU instructions/byte**), **0.84–1.4 GiB/s** (`yyjson`, **>2.5x faster**, **50% fewer instructions**), **0.37–0.46 GiB/s** (`RapidJSON`, **~8x faster**), and **0.069–0.075 GiB/s** (`nlohmann/json`, **~48x faster**). On partial extraction (`partial tweets`, `find tweet`), streaming is **1.7x–2.6x faster** than DOM. | Directly indicts `xacml4j`'s two-pass tree materialization in both `xacml-json` (Gson `JsonDeserializer<T>` materializing `JsonElement`/`LinkedTreeMap` trees before [`RequestContextAdapter`](xacml-json/src/main/java/org/xacml4j/v30/marshal/json/RequestContextAdapter.java#L54-L98) runs) and `xacml-xml` (JAXB materializing `PolicyType`/`RuleType` trees before [`Xacml30PolicyFromJaxbToObjectModelMapper`](xacml-xml/src/main/java/org/xacml4j/v30/xml/Xacml30PolicyFromJaxbToObjectModelMapper.java#L106-L150) runs). |
| **Langdale & Lemire (2019)**, *"Parsing Gigabytes of JSON per Second"*, `arXiv:1902.08318v7` (*VLDB J.* 28(6), 941–960) | Direct UTF-8 byte-stream structural scanning vs. char-transcoding state machines (`RapidJSON`, `sajson`). | Parses `twitter.json` (631 KB) at **>2.2 GB/s** using **<25% of the CPU instructions** of `RapidJSON` by operating directly on UTF-8 bytes and avoiding per-token heap allocation. | Justifies parsing `InputStream` / `byte[]` directly in UTF-8 (e.g., Jackson's `UTF8StreamJsonParser` over `InputStream`) instead of wrapping `InputStream` in `new InputStreamReader(is, UTF_8)` as [`JsonRequestContextUnmarshaller.java:90`](xacml-json/src/main/java/org/xacml4j/v30/marshal/json/JsonRequestContextUnmarshaller.java#L90) currently does. |
| **Java JSON Benchmark on JDK 25** (`fabienrenaud/java-json-benchmark`, run `2026-02-28`, OpenJDK 25.0.2, JMH 1.35, `Users` deserialization, 1 KB & 10 KB) | JMH throughput (`ops/s`) across Java JSON libraries (`stream` vs `databind`) on 1 KB payloads (typical XACML JSON request size) and 10 KB payloads. | **1 KB Deserialization**: `jackson/stream` (`JsonParser`): **1,091,952 ± 9,375 ops/s**; `jackson/databind`: **910,772 ± 12,084 ops/s**; `gson/stream` (`JsonReader`): **671,022 ± 4,698 ops/s**; `gson/databind`: **669,697 ± 6,113 ops/s**; `orgjson/stream` (`org.json`, used by AuthzForce): **121,310 ± 2,366 ops/s**. `jackson/stream` is **1.63x faster than `gson/stream` and `gson/databind`** (and **9.00x faster than `org.json`**). At **10 KB**: `jackson/stream` (**128,763 ops/s**) is **1.54x faster** than `gson/stream` (**83,723 ops/s**). | Provides direct JDK 25 JMH evidence for `D8` and `D9`: a single-pass `jackson-core` `JsonParser` streaming unmarshaller outperforms Gson streaming by **+54% to +63%** and avoids the extra `JsonElement` DOM tree penalty of `xacml4j`'s current Gson `JsonDeserializer<T>` adapters. |
| **Nicola & John (2003)**, *"XML Parsing: A Threat to Database Performance"* (*ACM CIKM '03*, IBM Silicon Valley & Toronto Labs) | Instruction-level profiling of SAX/DOM XML parsing with and without XSD Schema validation on 2 KB–16 KB documents. | Plain XML parsing executes **515,705** (2 KB) to **3,816,107** (16 KB) CPU instructions per document (2x–3x database CPU cost). **Enabling XSD Schema validation increases total CPU consumption to 4x–12x (a 2x–4x multiplier on parse CPU, or 50%–75% of total parse time)**. | Directly quantifies `D7`: even when XSD validation runs in-process, finite-state automaton transitions and simple-type checking double or triple XML parse CPU cost. XSD validation MUST be off by default (`D7`). |
| **Kostoulas et al. (2006)**, *"XML Screamer: An Integrated Approach to High Performance XML Parsing, Validation and Deserialization"* (*WWW '06*, IBM Research) | Single-pass integrated scanning + validation + business-object construction vs. layered SAX/DOM + XSD validator + binding framework. | Single-pass deserialization achieves **2.3x to 5.7x higher throughput** than layered parsing + binding, running at **22% to 44% of raw byte-scanning speed**. | Supports `D3(b)` + `D9`: replacing the two-pass `SAX -> JAXB AST -> ObjectModelMapper` pipeline with a single-pass StAX `XMLStreamReader -> xacml-core builder` pipeline eliminates the intermediate binding tree. |
| **Local `xacml4j` JDK 25 JMH Baseline** (`002-baseline-jdk25`, [tables.md](openspec/changes/add-pdp-jmh-benchmarks/evidence/benchmarks/jmh/002-baseline-jdk25/tables.md#L7-L14), Oracle GraalVM 25.0.4 C2) | `PolicyStoreLoadBenchmark.importXml` minus `loadParsed` (`store=in-memory`). | **`synthetic:100`** (101 XML docs, ~140 KB total XML, ~1.38 KB/policy): `6,709 - 9.57 = 6,699.4 us` and `10,641 - 68.6 = 10,572.4 KB` (**66.3 us and 104.7 KB per 1.38 KB policy — 75.8x heap allocation amplification**). **`rsa2008`** (9 XACML 2.0 policy sets, ~71 KB total XML): `777.2 - 2.38 = 774.8 us` and `1,120.5 - 23.5 = 1,097.0 KB` (**86.1 us and 121.9 KB per policy set**). | Establishes the exact baseline numbers on the target JDK 25 machine for `D9`. |

---

#### 1.2 JAXB Unmarshalling Costs in `xacml4j` and How Projects Avoid Them — `SUPPORTED`

Tracing [`PolicyImportTool.importPolicy`](xacml-pdp/src/main/java/org/xacml4j/v30/spi/repository/PolicyImportTool.java#L75-L80) -> [`BaseJAXBUnmarshaller.doUnmarshal`](xacml-xml/src/main/java/org/xacml4j/v30/xml/BaseJAXBUnmarshaller.java#L172-L216) -> [`Xacml30PolicyFromJaxbToObjectModelMapper`](xacml-xml/src/main/java/org/xacml4j/v30/xml/Xacml30PolicyFromJaxbToObjectModelMapper.java#L106-L150) explains why parsing a **1.38 KB** synthetic policy currently allocates **104.7 KB** and takes **66.3 us**:

1. **Per-Call JAXP Factory Discovery & Xerces Parser Allocation (~50–60 KB and ~30–40 us per document)**:
   - [`BaseJAXBUnmarshaller.createUnmarshaller()`](xacml-xml/src/main/java/org/xacml4j/v30/xml/BaseJAXBUnmarshaller.java#L172-L194) is called on **every single `unmarshal()` invocation**.
   - Line 172 calls `SAXParserFactory.newInstance()`, triggering JAXP `FactoryFinder` lookup (system property check, `jaxp.properties` check, `ServiceLoader` scan).
   - Line 184 calls `spf.newSAXParser().getXMLReader()`, which constructs a brand-new JDK Xerces `SAXParserImpl`, `XIncludeAwareParserConfiguration`, `XMLNSDocumentScannerImpl`, a 2,039-entry `SymbolTable`, and multiple `8 KB` internal byte and char buffers (**~45–55 KB of heap allocated and discarded per document**).
   - Line 186 allocates `new BooleanAttributeValidationFilter(xmlReader)`.
   - Line 189 calls `context.createUnmarshaller()`, allocating a new GlassFish/Eclipse JAXB `UnmarshallerImpl`, `SAXUnmarshallerHandlerImpl`, and internal namespace/element state stacks (**~5–8 KB**).
2. **Per-Call `PolicyUnmarshaller` and Mapper Allocation ([`PolicyImportTool.java:75-78`](xacml-pdp/src/main/java/org/xacml4j/v30/spi/repository/PolicyImportTool.java#L75-L78), [`XacmlPolicyUnmarshaller.java:54-74`](xacml-xml/src/main/java/org/xacml4j/v30/xml/XacmlPolicyUnmarshaller.java#L54-L74))**:
   - `PolicyImportTool.importPolicy` calls `p.newPolicyUnmarshaller()` on every policy, which constructs `new XacmlPolicyUnmarshaller(...)` and **both** `new Xacml30PolicyFromJaxbToObjectModelMapper(...)` and `new Xacml20PolicyFromJaxbToObjectModelMapper(...)` per document.
3. **Intermediate JAXB Tree Materialization (~20–25 KB per synthetic policy)**:
   - JAXB reflection/bytecode injectors instantiate generated `org.oasis.xacml.*` classes (`PolicyType`, `TargetType`, `AnyOfType`, `AllOfType`, `MatchType`, `AttributeValueType`, `AttributeDesignatorType`, `RuleType`, `JAXBElement<?>`, and default-sized `ArrayList(10)` instances for child lists) only for `Xacml30PolicyFromJaxbToObjectModelMapper` to traverse them once and make them immediate garbage.
   - Note: If `D3(a)` (`jaxb2-basics-runtime` `-Xlocator`) is enabled on the JAXB classes, JAXB will additionally allocate one `org.xml.sax.helpers.LocatorImpl` object per XML element (~16–20 extra objects per synthetic policy, hundreds per RSA 2008 policy set) on **every valid parse**.
4. **Mapper Collection Churn (`LinkedList` -> `ImmutableList.copyOf` + `DOMUtil.copyNode`)**:
   - [`Xacml30PolicyFromJaxbToObjectModelMapper`](xacml-xml/src/main/java/org/xacml4j/v30/xml/Xacml30PolicyFromJaxbToObjectModelMapper.java#L387-L611) (lines 387, 463, 479, 494, 559, 611), [`Xacml20PolicyFromJaxbToObjectModelMapper`](xacml-xml/src/main/java/org/xacml4j/v30/xml/Xacml20PolicyFromJaxbToObjectModelMapper.java#L417-L702), and [`Xacml30RequestContextFromJaxbToObjectModelMapper`](xacml-xml/src/main/java/org/xacml4j/v30/xml/Xacml30RequestContextFromJaxbToObjectModelMapper.java#L85-L152) (lines 85, 102, 134, 152) accumulate mapped children into `new LinkedList<>()` (allocating a `Node` wrapper per element) before the domain builders copy them into `ImmutableList.copyOf(...)`.

**How Projects Avoid JAXB Unmarshalling Costs**:
- **Tier 1 (Immediate, Low-Risk)**: Reuse a single static hardened `SAXParserFactory` (or `XMLInputFactory`) and cache `SAXParser` + `Unmarshaller` (or `XMLStreamReader`) in a `ThreadLocal` (resetting via `saxParser.reset()` after each parse), eliminating the ~50–60 KB per-call parser setup cost.
- **Tier 2 (Structural, Zero Intermediate Tree — `D3(b)`)**: Replace the two-pass `SAX -> JAXB AST -> ObjectModelMapper` pipeline with a direct pull-based StAX `XMLStreamReader` parser (as used in `jackson-dataformat-xml`, Apache CXF StAX databinding, and Hibernate 6 XML mapping) that constructs `xacml-core` `Policy`, `PolicySet`, `RequestContext`, and `ResponseContext` builders directly in a single pass.

---

#### 1.3 `SAXParserFactory`, `SAXParser`, `XMLInputFactory`, and `Unmarshaller` Reuse and Thread Safety — `SUPPORTED`

| Component | Thread-Safe? | Reusable Sequentially? | Correct Reuse Pattern & Caveats |
| --- | --- | --- | --- |
| `jakarta.xml.bind.JAXBContext` | **Yes** (spec-guaranteed) | Yes | Hold one `static final JAXBContext` (already done in [`JAXBUtils`](xacml-xml/src/main/java/org/xacml4j/v30/xml/JAXBUtils.java#L36-L52)). |
| `javax.xml.parsers.SAXParserFactory` / `DocumentBuilderFactory` | **No** (JAXP Javadoc explicitly states not guaranteed thread-safe) | Yes | Create once in the hardened factory owner (`D6`), never mutate after initialization, and **synchronize `factory.newSAXParser()` / `factory.newDocumentBuilder()`** when initializing a per-thread parser. |
| `javax.xml.parsers.SAXParser` / `org.xml.sax.XMLReader` | **No** (throws `SAXException: FWK005 parse may not be called while parsing` on concurrent use) | **Yes** (`saxParser.reset()`) | Cache in a `ThreadLocal` (or bounded pool) per `ParseOptions` security profile. Always call `saxParser.reset()` in `finally` and clear `ContentHandler`/`ErrorHandler`/`DTDHandler` references so input state is never retained. **Caveat**: Xerces's internal `SymbolTable` grows across resets as new XML element/attribute names are seen; to prevent unbounded `SymbolTable` growth on untrusted non-schema-validated XML, replace the `ThreadLocal` `SAXParser` after a fixed reuse count (e.g., every `1,024` parses). |
| `jakarta.xml.bind.Unmarshaller` | **No** (Jakarta XML Binding spec §8) | **Yes** | Cache in the same `ThreadLocal` holder alongside `SAXParser`; clear any custom `ValidationEventHandler` and `Schema` in `finally`. |
| `javax.xml.stream.XMLInputFactory` | **Config: No; `createXMLStreamReader`: Yes in Woodstox, No in standard JAXP doc** | Yes | Configure once at startup in the hardened factory owner (`SUPPORT_DTD=false`, `IS_SUPPORTING_EXTERNAL_ENTITIES=false`); create lightweight `XMLStreamReader` per call (Woodstox and JDK Xerces StAX recycle internal char buffers and symbol tables at the factory level). |

---

#### 1.4 `Schema` and `Validator` / `ValidatorHandler` Reuse and Thread Safety (`D7`) — `SUPPORTED`

1. **`javax.xml.validation.Schema`**:
   - **Immutable and thread-safe** per JAXP `javax.xml.validation.Schema` Javadoc (*"A `Schema` object is thread-safe and applications are encouraged to share it across many parsers in many threads"*).
   - Compiling the XACML 2.0 and 3.0 XSDs via `SchemaFactory.newSchema(...)` takes **tens of milliseconds and several megabytes of grammar allocations**. It MUST be compiled **at most once per JVM** (lazily via a static holder class on the first call with `ParseOptions.validateSchema() == true`), using a classpath-only `LSResourceResolver` that resolves `xml.xsd` and XACML schemas from `xacml-xml-jaxb` resources and blocks all network/file URIs (`D7`).
2. **`javax.xml.validation.Validator` and `ValidatorHandler`**:
   - **NOT thread-safe**, stateful during a parse, **reusable sequentially** via `reset()`.
   - When `validateSchema == true`, either attach the shared `Schema` to the thread-local `Unmarshaller` (`unmarshaller.setSchema(schema)`, clearing `unmarshaller.setSchema(null)` when `validateSchema == false`) or wire a thread-local `ValidatorHandler` (`schema.newValidatorHandler()`) into the SAX filter chain (`XMLReader -> BooleanAttributeValidationFilter -> ValidatorHandler -> UnmarshallerHandler`). Reusing the `ValidatorHandler` / `Unmarshaller` avoids allocating a new `XMLSchemaValidator` (~30–40 KB) on every validated parse.

---

#### 1.5 Interning in XML and JSON Parsers and Domain Mappers — `SUPPORTED`

1. **What XML Parsers Intern (and What They Do NOT)**:
   - In JDK Xerces SAX (`http://xml.org/sax/features/string-interning`, default `true`) and StAX (`javax.xml.stream.isInterning` / Woodstox `P_INTERN_NAMES` & `P_INTERN_NS_URIS`), the parser interns **only XML element local names, QNames, prefixes, and namespace URIs** via its internal `SymbolTable`.
   - **XML attribute values** (`DataType="http://www.w3.org/2001/XMLSchema#string"`, `Category="urn:oasis:names:tc:xacml:3.0:attribute-category:resource"`, `AttributeId="urn:oasis:names:tc:xacml:1.0:resource:resource-id"`, `MatchId="..."`, `RuleCombiningAlgId="..."`) and **element text content** (`<AttributeValue>...`) are **NEVER interned by SAX or StAX**. Every attribute value in every `<Match>`, `<AttributeDesignator>`, and `<Attribute>` allocates a brand-new `java.lang.String` on the heap!
2. **What JSON Parsers Intern (and What They Do NOT)**:
   - In `jackson-core` (`/fasterxml/jackson-core`), `JsonFactory.Feature.CANONICALIZE_FIELD_NAMES` (default `true`) deduplicates JSON **object property names** (`"Request"`, `"Category"`, `"Attribute"`, `"AttributeId"`, `"Value"`, `"DataType"`) directly from UTF-8 bytes via `ByteQuadsCanonicalizer` (allocated on the shared `JsonFactory`, borrowed per `JsonParser` via `makeChild()`, and merged back on `close()` via `mergeChild()`), with built-in hash-collision DoS protection (`FAIL_ON_SYMBOL_HASH_OVERFLOW = true`).
   - Note from Jackson 3.x architecture docs (`/fasterxml/jackson-core` `CLAUDE.md`): **disable `JsonFactory.Feature.INTERN_FIELD_NAMES`** while **keeping `CANONICALIZE_FIELD_NAMES` enabled**! That way, property names are deduplicated in Jackson's bounded `ByteQuadsCanonicalizer` without calling `String.intern()` on untrusted field names.
   - JSON **string values** (the right-hand side of `"AttributeId": "..."`, `"DataType": "..."`, `"Category": "..."`) are not canonicalized by `JsonFactory` (`TextBuffer.contentsAsString()` allocates a fresh `String` per value).
3. **Domain-Level Canonicalization in `xacml4j` (`D9`)**:
   - **Already canonicalized**: `DataType` strings resolve to `XacmlTypes` enum singletons ([`XacmlTypes.java:188-205`](xacml-core/src/main/java/org/xacml4j/v30/types/XacmlTypes.java#L188-L205)); standard `Category` strings resolve to `CategoryId` static constants ([`CategoryId.java:43-59`](xacml-core/src/main/java/org/xacml4j/v30/CategoryId.java#L43-L59)).
   - **Hidden allocation in [`CategoryId.getById(String)`](xacml-core/src/main/java/org/xacml4j/v30/CategoryId.java#L136-L143)**: `Optional.ofNullable(BY_ID.get(v)).or(() -> Optional.ofNullable(BY_ABBREVIATED_ID.get(v))).or(...)` allocates `Optional` wrappers on every call. Replacing it with a direct two-map lookup (`CategoryId id = BY_ID.get(v); if (id == null) id = BY_ABBREVIATED_ID.get(v);`) makes standard category resolution zero-allocation.
   - **Missing canonicalization (`AttributeId` and `AttributeDesignatorKey`)**:
     - Every `<AttributeDesignator>` in a policy and every `<Attribute>` in a request keeps the freshly allocated `attributeId` `String` from the XML/JSON parser, and every `<Match>` / `<AttributeDesignator>` builds a new [`AttributeDesignatorKey`](xacml-core/src/main/java/org/xacml4j/v30/AttributeDesignatorKey.java#L45-L50) via `new AttributeDesignatorKey.Builder()`.
     - Canonicalizing standard XACML `AttributeId` URIs via an immutable static lookup table (and deduplicating `AttributeDesignatorKey` instances within a policy unmarshal session, never calling `String.intern()` on arbitrary request values) eliminates duplicate key allocations and allows `AttributeDesignatorKey.equals` (`attributeId.equals(s.attributeId)`) to short-circuit on reference equality (`this == anObject`) during PDP evaluation!

---

#### 1.6 Cost of XSD Validation in the Same Pass (`D7`) — `SUPPORTED`

1. **Same-Pass (`ValidatorHandler` / `unmarshaller.setSchema(schema)`) vs. Two-Pass (`Validator.validate` + `unmarshal`)**:
   - Two-pass validation buffers the entire input into a `byte[]` and runs the XML scanner, UTF-8 decoder, and namespace resolver **twice** (2x baseline scan cost before XSD automaton work even begins).
   - Same-pass validation hooks `Schema.newValidatorHandler()` directly into the SAX event pipeline (`XMLReader -> ValidatorHandler -> UnmarshallerHandler`), scanning UTF-8 bytes once.
2. **Cost of Same-Pass XSD Validation vs. Validation Off**:
   - Even in a single SAX pass, Xerces's `XMLSchemaValidator` evaluates DFA/NFA content models on every `startElement`/`endElement`, normalizes whitespace and validates lexical facets (`xs:anyURI`, `xs:boolean`, `xs:integer`, `xs:dateTime`) on every attribute and text node, and checks `xs:ID`/`xs:IDREF` tables.
   - Per Nicola & John (*CIKM '03*, Fig. 2), XSD validation adds **+100% to +300% CPU overhead (2x–4x total parse time)** over non-validating parsing. In Java JAXP with a reused `ValidatorHandler`, expect **+50% to +150% (1.5x–2.5x) latency** and **+20% to +40% allocation** per document when `validateSchema=true` vs. **0% overhead** when `validateSchema=false`.

---

### 2. Sources

| Source | URL / Path | Retrieved Date | Coverage Label |
| --- | --- | --- | --- |
| Keiser & Lemire (2024), *"On-Demand JSON: A Better Way to Parse Documents?"* (`arXiv:2312.17149v3`, Section 4, Table 1) | `https://arxiv.org/abs/2312.17149` | 2026-10-27 | `SUPPORTED` |
| Langdale & Lemire (2019), *"Parsing Gigabytes of JSON per Second"* (`arXiv:1902.08318v7`) | `https://arxiv.org/abs/1902.08318` | 2026-10-27 | `SUPPORTED` |
| Nicola & John (2003), *"XML Parsing: A Threat to Database Performance"* (*ACM CIKM '03*, Fig. 2) | `https://lists.w3.org/Archives/Public/www-ws/2004Oct/att-0032/MNicola_CIKM_2003_1_.pdf` | 2026-10-27 | `SUPPORTED` |
| Kostoulas et al. (2006), *"XML Screamer: An Integrated Approach to High Performance XML Parsing, Validation and Deserialization"* (*WWW '06*) | `http://www.cs.toronto.edu/~aheifets/papers/5011.pdf` | 2026-10-27 | `SUPPORTED` |
| `fabienrenaud/java-json-benchmark` JDK 25 JMH Results (`2026-02-28`, OpenJDK 25.0.2) | `https://raw.githubusercontent.com/fabienrenaud/java-json-benchmark/master/archive/raw-results-2026-02-28.md` | 2026-10-27 | `SUPPORTED` |
| FasterXML `jackson-core` Documentation (`JsonFactory` Features, `StreamReadConstraints`, `ByteQuadsCanonicalizer`) | `https://github.com/fasterxml/jackson-core/wiki/JsonFactory-Features`, `https://github.com/fasterxml/jackson-core/wiki/StreamReadConstraints`, `https://github.com/fasterxml/jackson-core/blob/3.x/CLAUDE.md` (via Context7 `/fasterxml/jackson-core`) | 2026-10-27 | `SUPPORTED` |
| JAXP `javax.xml.validation` Package & `Schema` Thread-Safety Specification | `https://xerces.apache.org/xerces2-j/javadocs/api/javax/xml/validation/package-summary.html` | 2026-10-27 | `SUPPORTED` |
| Jakarta XML Binding `Unmarshaller` Specification & Thread-Safety | `https://eclipse.dev/eclipselink/api/2.4/javax/xml/bind/Unmarshaller.html` | 2026-10-27 | `SUPPORTED` |
| `xacml4j` JDK 25 JMH Baseline (`002-baseline-jdk25`) | [tables.md](openspec/changes/add-pdp-jmh-benchmarks/evidence/benchmarks/jmh/002-baseline-jdk25/tables.md), [summary.md](openspec/changes/add-pdp-jmh-benchmarks/evidence/benchmarks/jmh/001-baseline/summary.md) | 2026-10-27 | `SUPPORTED` |
| `xacml4j` JMH Harness & Benchmarks | [BENCHMARKS.md](docs/ai/BENCHMARKS.md), [xacml-jmh/README.md](xacml-jmh/README.md), [PolicyStoreLoadBenchmark.java](xacml-jmh/src/main/java/org/xacml4j/jmh/pdp/PolicyStoreLoadBenchmark.java), [PdpDecideBenchmark.java](xacml-jmh/src/main/java/org/xacml4j/jmh/pdp/PdpDecideBenchmark.java), [SyntheticCorpus.java](xacml-jmh/src/main/java/org/xacml4j/jmh/corpus/SyntheticCorpus.java), [Rsa2008Corpus.java](xacml-jmh/src/main/java/org/xacml4j/jmh/corpus/Rsa2008Corpus.java), [Marshalling.java](xacml-jmh/src/main/java/org/xacml4j/jmh/harness/Marshalling.java) | 2026-10-27 | `SUPPORTED` |
| `xacml4j` XML & JSON Unmarshalling Sources | [BaseJAXBUnmarshaller.java](xacml-xml/src/main/java/org/xacml4j/v30/xml/BaseJAXBUnmarshaller.java), [PolicyImportTool.java](xacml-pdp/src/main/java/org/xacml4j/v30/spi/repository/PolicyImportTool.java), [Xacml30PolicyFromJaxbToObjectModelMapper.java](xacml-xml/src/main/java/org/xacml4j/v30/xml/Xacml30PolicyFromJaxbToObjectModelMapper.java), [Xacml30RequestContextFromJaxbToObjectModelMapper.java](xacml-xml/src/main/java/org/xacml4j/v30/xml/Xacml30RequestContextFromJaxbToObjectModelMapper.java), [CategoryId.java](xacml-core/src/main/java/org/xacml4j/v30/CategoryId.java), [XacmlTypes.java](xacml-core/src/main/java/org/xacml4j/v30/types/XacmlTypes.java), [AttributeDesignatorKey.java](xacml-core/src/main/java/org/xacml4j/v30/AttributeDesignatorKey.java), [JsonRequestContextUnmarshaller.java](xacml-json/src/main/java/org/xacml4j/v30/marshal/json/JsonRequestContextUnmarshaller.java), [RequestContextAdapter.java](xacml-json/src/main/java/org/xacml4j/v30/marshal/json/RequestContextAdapter.java) | 2026-10-27 | `SUPPORTED` |

---

### 3. Confidence and Disagreement / Drift Risk

- **Confidence**: **High (`SUPPORTED`)**. Baseline parse costs (`66.3 us` and `104.7 KB` per `synthetic:100` policy; `86.1 us` and `121.9 KB` per `rsa2008` policy set) are already measured in [`002-baseline-jdk25/tables.md`](openspec/changes/add-pdp-jmh-benchmarks/evidence/benchmarks/jmh/002-baseline-jdk25/tables.md#L7-L14) on the project's JDK 25 baseline, and the external literature (`arXiv:2312.17149v3`, `arXiv:1902.08318v7`, *CIKM '03*, *WWW '06*, `java-json-benchmark` on JDK 25) is directly verified.
- **Disagreement or Drift Risk**:
  - **JAXB Reuse vs. Direct StAX (`D3(a)` vs. `D3(b)`)**: While `ThreadLocal` reuse of `SAXParser` + `Unmarshaller` captures the largest fixed per-document overhead (~55–65 KB/doc), it cannot eliminate the intermediate JAXB AST (~20–25 KB/doc) or `-Xlocator`'s per-element `LocatorImpl` allocation. Only `D3(b)` (direct StAX `XMLStreamReader` to domain builders) eliminates the intermediate tree.
  - **Xerces `SymbolTable` Retention in `ThreadLocal<SAXParser>`**: Reusing a `SAXParser` indefinitely via `reset()` retains its internal `SymbolTable`. On untrusted XML without XSD validation, bounding reuse (e.g., recreating the per-thread `SAXParser` every `1,024` calls) prevents unbounded symbol retention while preserving >99.9% of the reuse speedup.

---

### 4. Concrete Recommendations for D9 and D7 (Most Important First, with Justified Expected Gains)

#### 1. Decision D9 (Parse Optimization Cycles — "Analyze allocations and optimize them to the limits")

Update [design.md](openspec/changes/improve-xacml-parsing/design.md#L194-L202) (`D9`) to execute the following four measured optimization cycles in order of expected gain:

1. **Cycle D9.1 — Eliminate Per-Call `SAXParserFactory` / `SAXParser` / `Unmarshaller` / `DocumentBuilderFactory` / `PolicyUnmarshaller` Instantiation**:
   - **Change**:
     - In the single hardened XML factory owner (`D6`) and [`BaseJAXBUnmarshaller`](xacml-xml/src/main/java/org/xacml4j/v30/xml/BaseJAXBUnmarshaller.java#L172-L194), initialize one static hardened `SAXParserFactory` and `DocumentBuilderFactory` (synchronizing `newSAXParser()` / `newDocumentBuilder()`), and cache a per-thread `{SAXParser, BooleanAttributeValidationFilter, Unmarshaller}` holder (resetting via `saxParser.reset()`, clearing handlers in `finally`, and rotating every `1,024` parses).
     - In [`PolicyImportTool`](xacml-pdp/src/main/java/org/xacml4j/v30/spi/repository/PolicyImportTool.java#L53-L80), cache the `PolicyUnmarshaller` per `MediaType` on the `PolicyImportTool` instance instead of calling `p.newPolicyUnmarshaller()` (which allocates both XACML 2.0 and 3.0 mapper objects) on every `importPolicy` call.
   - **Justified Expected Gain**:
     - Baseline `synthetic:100` (`importXml - loadParsed`) is **6,699.4 us** (**66.3 us/policy**) and **10,572.4 KB** (**104.7 KB/policy**). A single Xerces `SAXParser` + `Unmarshaller` + `XacmlPolicyUnmarshaller` instantiation accounts for **~55–65 KB** and **~35–45 us** of that fixed per-document cost.
     - **Expected gain on `synthetic:100` (`PolicyStoreLoadBenchmark.importXml` & `PolicyParseBenchmark`)**: **-50% to -65% latency** (`6,709 us` -> **~2,400–3,300 us**, or **~24–33 us/policy**) and **-55% to -65% allocation** (`10.64 MB` -> **~3.7–4.8 MB**, or **~37–48 KB/policy**).
     - **Expected gain on `rsa2008` (9 larger policy sets)**: **-35% to -50% latency and allocation** (`777 us` -> **~400–500 us**; `1.12 MB` -> **~550–700 KB**).
     - **Expected gain on XML `RequestParseBenchmark` (~0.6 KB request)**: **-60% to -75% latency and allocation** (because fixed parser setup dominates small requests even more than policies).

2. **Cycle D9.2 — Single-Pass Streaming JSON Request Unmarshaller (`D8` + `D9`)**:
   - **Change**:
     - Replace Gson's two-pass `JsonDeserializer<T>` (`JsonElement` / `JsonObject` / `LinkedTreeMap` intermediate tree materialization in [`RequestContextAdapter`](xacml-json/src/main/java/org/xacml4j/v30/marshal/json/RequestContextAdapter.java#L54-L98), [`CategoryAdapter`](xacml-json/src/main/java/org/xacml4j/v30/marshal/json/CategoryAdapter.java#L54-L90), and [`AttributeDeserializer`](xacml-json/src/main/java/org/xacml4j/v30/marshal/json/AttributeDeserializer.java#L57-L114)) and `new InputStreamReader(is, UTF_8)` ([`JsonRequestContextUnmarshaller.java:90`](xacml-json/src/main/java/org/xacml4j/v30/marshal/json/JsonRequestContextUnmarshaller.java#L90)) with a single shared `com.fasterxml.jackson.core.JsonFactory` (configured with `CANONICALIZE_FIELD_NAMES = true`, `INTERN_FIELD_NAMES = false`, `FAIL_ON_SYMBOL_HASH_OVERFLOW = true`, `STRICT_DUPLICATE_DETECTION = true`, and `StreamReadConstraints` from `ParseOptions`) feeding a single-pass `JsonParser` directly over the `InputStream` / `byte[]`.
   - **Justified Expected Gain**:
     - Per Keiser & Lemire (`arXiv:2312.17149v3`, 1.7x–2.5x speedup over DOM tree materialization) and `java-json-benchmark` on JDK 25 (`jackson/stream` is **1.63x faster** than `gson/databind` at 1 KB and eliminates `InputStreamReader`'s 8 KB char buffer + all intermediate `JsonObject`/`LinkedTreeMap` nodes):
     - **Expected gain on `RequestParseBenchmark` (`format=json`)**: **1.8x to 3.0x speedup (-45% to -65% latency)** and **-50% to -70% allocation per request**.

3. **Cycle D9.3 — Eliminate Mapper Collection Churn (`LinkedList`), `CategoryId.getById` `Optional` Churn, and Deduplicate `AttributeDesignatorKey` / Standard `AttributeId`s**:
   - **Change**:
     - Replace all `new LinkedList<>()` and stream collector allocations in [`Xacml30PolicyFromJaxbToObjectModelMapper`](xacml-xml/src/main/java/org/xacml4j/v30/xml/Xacml30PolicyFromJaxbToObjectModelMapper.java#L387-L611), [`Xacml20PolicyFromJaxbToObjectModelMapper`](xacml-xml/src/main/java/org/xacml4j/v30/xml/Xacml20PolicyFromJaxbToObjectModelMapper.java#L417-L702), and [`Xacml30RequestContextFromJaxbToObjectModelMapper`](xacml-xml/src/main/java/org/xacml4j/v30/xml/Xacml30RequestContextFromJaxbToObjectModelMapper.java#L85-L152) with pre-sized `ArrayList<>(sourceList.size())` or `ImmutableList.builderWithExpectedSize(sourceList.size())`.
     - Replace the 3-stage `Optional.or(...)` chain in [`CategoryId.getById(String)`](xacml-core/src/main/java/org/xacml4j/v30/CategoryId.java#L136-L143) with a direct map lookup (`BY_ID.get(v)` then `BY_ABBREVIATED_ID.get(v)`).
     - Canonicalize standard XACML `AttributeId` strings via a static immutable map and deduplicate [`AttributeDesignatorKey`](xacml-core/src/main/java/org/xacml4j/v30/AttributeDesignatorKey.java#L45-L50) instances within a policy parse.
   - **Justified Expected Gain**:
     - Saves **~5–10 KB per synthetic policy** (~15–25% of post-Cycle-1 allocation) and **~8–12% parse time**, while also shrinking retained `Policy` footprint and accelerating `AttributeDesignatorKey.equals` on the PDP `decide` path.

4. **Cycle D9.4 — Direct StAX `XMLStreamReader` Unmarshaller (`D3(b)`), Eliminating the Intermediate JAXB Tree and `-Xlocator` Overhead**:
   - **Change**:
     - If `D3(b)` is selected (or staged after `D9.1`–`D9.3`), parse XML policies and requests directly from a hardened StAX `XMLStreamReader` into `xacml-core` builders in one pass, querying `reader.getLocation()` only when emitting a `ParseDiagnostic` (`D1`).
   - **Justified Expected Gain**:
     - Eliminates 100% of intermediate JAXB beans (`PolicyType`, `RuleType`, `MatchType`, `JAXBElement`) and avoids `-Xlocator`'s per-element `LocatorImpl` allocation: per Kostoulas et al. (*WWW '06*), reduces post-Cycle-1 XML parse allocation by a further **-40% to -55%** (down to **~15–20 KB per synthetic policy**, an **>80% reduction vs. the 104.7 KB baseline**) and latency by an additional **-30% to -45%**.

---

#### 2. Decision D7 (Optional XSD Validation in the Same Pass)

Update [design.md](openspec/changes/improve-xacml-parsing/design.md#L171-L178) (`D7`) with the following concrete design rules and expected costs:

1. **Lazy Static `Schema` Holder + Per-Thread `ValidatorHandler` / `Unmarshaller` Reuse**:
   - Compile XACML 2.0 and XACML 3.0 `javax.xml.validation.Schema` instances **at most once per JVM** inside a lazy holder class (triggered only when `ParseOptions.validateSchema()` is `true`), using a classpath-only `LSResourceResolver` that resolves `xml.xsd` and XACML schemas from `xacml-xml-jaxb` classpath resources and throws `SAXException` on any external URI.
   - Reuse the `ValidatorHandler` (or `Unmarshaller` with `setSchema(schema)`) inside the `ThreadLocal` parser holder so `new XMLSchemaValidator()` (~30–40 KB) is not allocated on every validated parse.
2. **Expected Cost of `validateSchema=true` vs. `validateSchema=false`**:
   - **`validateSchema=false` (default)**: **0 ns and 0 B overhead** (the `Schema` holder is not even initialized, and no `ValidatorHandler` is wired into the SAX/StAX pipeline).
   - **`validateSchema=true` (in-pass)**: Based on Nicola & John (*CIKM '03*) and JAXP `XMLSchemaValidator` DFA/simple-type costs, expect **+50% to +150% (1.5x–2.5x) parse time** and **+20% to +40% allocation** relative to the optimized non-validating path (compared to **+200% to +300%** if done in a separate pass or without `ValidatorHandler` reuse).

---

### 5. Recommended JMH Parameters and Design for the Parse Benchmarks (`specs/parse-benchmarks/spec.md`)

To make `PolicyParseBenchmark` and `RequestParseBenchmark` in [specs/parse-benchmarks/spec.md](openspec/changes/improve-xacml-parsing/specs/parse-benchmarks/spec.md) diagnostic across `D7`, `D8`, and `D9` while following [docs/ai/BENCHMARKS.md](docs/ai/BENCHMARKS.md):

| Benchmark Class (`org.xacml4j.jmh.parse`) | Recommended `@Param`s | Purpose & What It Isolates |
| --- | --- | --- |
| **`PolicyParseBenchmark`** | 1. `@Param({"rsa2008", "synthetic:100"}) public String corpus;`<br>2. `@Param({"false", "true"}) public boolean validateSchema;` | - **`corpus`**: Measures both XACML 2.0 multi-policy sets (`rsa2008`, 9 docs, ~7.9 KB avg) and XACML 3.0 small policies (`synthetic:100`, 101 docs, ~1.38 KB avg).<br>- **`validateSchema`**: Directly measures the in-pass XSD validation delta (`D7`) and proves zero regression when `validateSchema=false`.<br>- **Two `@Benchmark` methods**: `parseOne(Cursor cursor)` (parses the next `PolicySource` in round-robin order via a `@State(Scope.Thread)` `Cursor`, reporting per-policy `us/op` and `B/op` directly) and `parseCorpus()` (parses all policies in the corpus in one operation, directly comparable to `PolicyStoreLoadBenchmark.importXml`). |
| **`RequestParseBenchmark`** | 1. `@Param({"xml", "json"}) public String format;`<br>2. `@Param({"rsa2008", "synthetic:100"}) public String corpus;`<br>3. `@Param({"false", "true"}) public boolean validateSchema;`<br>4. `@Param({"all"}) public String request;` | - **`format`**: Compares XML (`Xacml20`/`Xacml30RequestContextUnmarshaller`) vs. JSON (`JsonRequestContextUnmarshaller`, `D8`) on the exact same semantic requests.<br>- **`validateSchema`**: Measures XSD validation cost on XML requests (`D7`); for `format=json` with `validateSchema=true`, either skip or measure strict schema/constraint checking.<br>- **`request`**: Mirrors [`PdpDecideBenchmark`](xacml-jmh/src/main/java/org/xacml4j/jmh/pdp/PdpDecideBenchmark.java#L63-L64) (`all` round-robins across all corpus requests via `@State(Scope.Thread) Cursor`, or selects one specific request id). |

Additional JMH hygiene rules for both benchmarks:
1. **Pre-serialized `byte[]` in `@Setup(Level.Trial)` with Decision-Equivalence Check**:
   - In `RequestParseBenchmark.setUp()`, marshal every `CorpusRequest.request()` into a `byte[]` for `format=xml` and `format=json`, unmarshal each `byte[]` once during `@Setup(Level.Trial)`, and verify that `fixture.pdp().decide(reparsed)` produces `CorpusRequest.expected()` (and repeat in `@TearDown(Level.Trial)`), matching [`PdpDecideBenchmark`](xacml-jmh/src/main/java/org/xacml4j/jmh/pdp/PdpDecideBenchmark.java#L69-L133) and [`PolicyStoreLoadBenchmark`](xacml-jmh/src/main/java/org/xacml4j/jmh/pdp/PolicyStoreLoadBenchmark.java#L68-L95).
2. **Shared Unmarshaller Instance Across Threads (`-t 1` and `-t 4`)**:
   - Store the configured `PolicyUnmarshaller` / `RequestUnmarshaller` on the `@State(Scope.Benchmark)` instance so running with `-t 4` tests concurrent unmarshalling and verifies that `ThreadLocal` parser reuse (`D9`) and shared `Schema` (`D7`) are thread-safe and contention-free.
3. **Standard Fork & Profiler Flags ([docs/ai/BENCHMARKS.md](docs/ai/BENCHMARKS.md#L52-L88))**:
   - `@BenchmarkMode(Mode.AverageTime)`, `@OutputTimeUnit(TimeUnit.MICROSECONDS)`, `@Fork(value = 3, jvmArgsAppend = {"-Xms2g", "-Xmx2g", "-XX:+UseG1GC"})`, `@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)`, `@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)`, run with `-jvmArgs -XX:-UseJVMCICompiler -prof gc -foe true`.

---

### 6. Risks and Open Questions

- **Risks**:
  1. **`ThreadLocal` Memory Retention in Application Thread Pools**: Holding a `SAXParser` + `Unmarshaller` (~60 KB) in a `ThreadLocal` on threads of a large web-server pool could retain ~60 KB per thread if not bounded or backed by weak/soft references or a small concurrent ring buffer (like Jackson's `RecyclerPool` / `ThreadLocalPool`). Using a bounded lock-free pool (e.g., `ArrayBlockingQueue` or Jackson-style `RecyclerPool` of 16–32 parsers) or rotating `SoftReference` `ThreadLocal` entries avoids permanent thread-local retention while keeping zero-allocation reuse on hot threads.
  2. **XACML 2.0 `rsa2008` Requests in `RequestParseBenchmark` with `format=json`**: `Rsa2008Corpus` requests are XACML 2.0 XML, which [`Xacml20RequestContextUnmarshaller`](xacml-xml/src/main/java/org/xacml4j/v30/xml/Xacml20RequestContextUnmarshaller.java) maps into the unified `org.xacml4j.v30.RequestContext` model during `Rsa2008Corpus.load()`. Therefore, marshalling those `RequestContext` objects to XACML 3.0 JSON via `Marshalling.requestMarshaller(MediaType.Type.XACML_JSON)` in `@Setup(Level.Trial)` works seamlessly, **provided** round-trip decision equivalence is verified in `@Setup` (as recommended above).
- **Open Questions**:
  1. Should `xacml-xml` use a bounded concurrent parser pool (e.g. capacity 32) or a `ThreadLocal` with `SoftReference` + parse-count rotation (`1,024` parses) for `SAXParser` + `Unmarshaller` reuse (`D9.1`) so virtual threads (JDK 25) cannot accumulate unbounded `ThreadLocal` parser instances?
  2. In `RequestParseBenchmark`, when `format=json` and `validateSchema=true`, should the benchmark skip that combination (throw an explicit assumption or omit `validateSchema` from `RequestParseBenchmark` in favor of `PolicyParseBenchmark`) since JSON structural validation is always active in the streaming JSON parser (`D8`)?
