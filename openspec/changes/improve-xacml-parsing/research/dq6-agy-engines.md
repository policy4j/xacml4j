<!-- Discovery lane DQ6 (task 1.1): Agy, Gemini 4 Argon (high), read-only; report taken from the Agy conversation database, local links made repository-relative. -->

FINAL REPORT (policy4j_bench_agy_argon, Agy Gemini 4 Argon):

## Q7: Limits, Error Reporting, and Parsing Techniques in Open-Source XACML Engines and Published Literature

### 1. Answer

#### 1.1 Cross-Engine Comparison Summary

| Dimension | OW2 AuthzForce (`authzforce/core`, `core-pdp-api`, `xacml-json-model`) | AT&T XACML 3.0 (`att/xacml-3.0`) | WSO2 Balana (`wso2/balana`) | `xacml4j` (Current Baseline) |
| --- | --- | --- | --- | --- |
| **XML Policy & Request Parser** | JAXB (`jakarta.xml.bind.Unmarshaller`) over a custom `XMLFilterImpl` (`SAXBasedXmlnsFilteringParser`) sharing one static `SAXParserFactory`; Saxon `XdmNode` (`DocumentBuilder`) for `<Content>` (`XmlUtils.java:70-282`). | Hand-written DOM walker (`DOMPolicyDef`, `DOMPolicy`, `DOMRequest`, `DOMUtil`) allocating a new `DocumentBuilderFactory` per call (`DOMUtil.java:460-495`). | Hand-written DOM walker (`Policy`, `FileBasedPolicyFinderModule`, `RequestCtxFactory`, `InputParser`) allocating a new `DocumentBuilderFactory` per call (`Utils.java:86-109`). | JAXB (`jakarta.xml.bind.Unmarshaller`) over a per-call `SAXParserFactory` + `BooleanAttributeValidationFilter` ([`BaseJAXBUnmarshaller`](xacml-xml/src/main/java/org/xacml4j/v30/xml/BaseJAXBUnmarshaller.java#L172-L216)) + mapper pass to `xacml-core` model; [`DOMUtil`](xacml-core/src/main/java/org/xacml4j/util/DOMUtil.java#L54-L115) for `<Content>`. |
| **JSON Request Parser** | `org.json` (`JSONTokener` / `JSONObject`) subclassed by `LimitsCheckingJSONObject` (`LimitsCheckingJSONObject.java:33-216`), mapped to model or Saxon `XdmNode` (`BaseXacmlJsonRequestPreprocessor.java`). | DOM-like JSON object tree via Jackson/Gson utilities in `xacml` / ONAP `policy-xacml-pdp`. | `org.json` / Gson request adapter (`JSONRequestCtx`) into `RequestCtx`. | Gson `JsonDeserializer` materializing intermediate `JsonElement` / `JsonObject` trees ([`JsonRequestContextUnmarshaller`](xacml-json/src/main/java/org/xacml4j/v30/marshal/json/JsonRequestContextUnmarshaller.java#L55-L120), [`RequestContextAdapter`](xacml-json/src/main/java/org/xacml4j/v30/marshal/json/RequestContextAdapter.java#L54-L98), [`AttributeDeserializer`](xacml-json/src/main/java/org/xacml4j/v30/marshal/json/AttributeDeserializer.java#L57-L114)). |
| **XSD & JSON Schema Validation** | **On by default**: JAXB `Unmarshaller` attaches pre-compiled XACML 3.0 `Schema` (`Xacml3JaxbHelper`); JSON requests validate against classpath `Request.schema.json` via `everit-json-schema` (`XacmlJsonUtils.java:43-65`, `BaseXacmlJsonRequestPreprocessor.java:191-196`). | **Off by default** during DOM parse; relies on manual DOM structural checks (`DOMPolicy.java`, `DOMRequest.java`) with a global `DOMProperties.isLenient()` flag and mutating `repair()` methods. | **Off by default** (`RequestCtxFactory.java:91-96`); optional XSD validation only when system property `com.sun.xacml.ContextSchema` is set (`InputParser.java:57-112`) or `PolicyReader` receives a schema File. | **Off at runtime** (`BaseJAXBUnmarshaller` passes no `Schema` to `Unmarshaller`); compensates with ad-hoc [`BooleanAttributeValidationFilter`](xacml-xml/src/main/java/org/xacml4j/v30/xml/BooleanAttributeValidationFilter.java#L44-L162) (`D7`). |
| **Error Reporting & Positions** | **Single-error, fail-fast**. Syntax/XSD errors carry SAX/JSONTokener line/char offset in exception text; semantic/model mapper errors have **no line/column or XPath** (no `-Xlocator`). | **Single-error, fail-fast** (or sets `STATUS_CODE_SYNTAX_ERROR`). Throws `DOMStructureException(Node, msg)` which prints `namespaceURI:localName` of the DOM node (`DOMUtil.java:122-125`), **without line, column, or full XPath**. | **Single-error, fail-fast**. `InputParser` logs `SAXParseException.getLineNumber()` (`InputParser.java:128`) and rethrows `new SAXException("invalid context document")`, **discarding the line number and cause**! | **Single-error, fail-fast**. [`SyntaxException`](xacml-core/src/main/java/org/xacml4j/v30/SyntaxException.java#L31-L65) wraps a free-form message + cause (`D1`–`D5`). |
| **XML & JSON Hardening / Limits** | **XML**: Saxon `Feature.XINCLUDE=false` (`XmlUtils.java:78`); duplicate `xmlns` prefix redeclaration rejected (`XmlUtils.java:209-216`). **JSON**: `LimitsCheckingJSONObject` enforces `maxDepth`, `maxNumOfImmediateChildren`, `maxJsonStringLength`, and rejects duplicate keys via `putOnce` (`LimitsCheckingJSONObject.java:78-194`). | **XML**: `DOMPolicyDef.load` and `DOMUtil.loadDocument` set `ACCESS_EXTERNAL_DTD=""`, `ACCESS_EXTERNAL_SCHEMA=""`, `FEATURE_SECURE_PROCESSING=true` (`DOMPolicyDef.java:81-84`), **but `DOMRequest.load(InputStream)` (`DOMRequest.java:101-104`) omits `FEATURE_SECURE_PROCESSING` and `disallow-doctype-decl`**! No explicit numeric size/depth limits. | **XML**: `Utils.getSecuredDocumentBuilderFactory()` sets `FEATURE_SECURE_PROCESSING=true`, disables external entities/DTDs, and sets Xerces `SecurityManager` `entityExpansionLimit=0` (`Utils.java:86-109`), **but catches `ParserConfigurationException`, logs, and continues** (`Utils.java:100-103`)! | **XML**: [`BaseJAXBUnmarshaller`](xacml-xml/src/main/java/org/xacml4j/v30/xml/BaseJAXBUnmarshaller.java#L172-L184) omits `disallow-doctype-decl`; [`DOMUtil`](xacml-core/src/main/java/org/xacml4j/util/DOMUtil.java#L75-L114) catches `ParserConfigurationException`, logs, and returns a partially configured factory (`D6`). **JSON**: no depth/token/duplicate-key limits (`D6`, `D8`). |

---

#### 1.2 Detailed Breakdown by Engine

##### A. OW2 AuthzForce (`authzforce/core`, `authzforce/core-pdp-api`, `authzforce/xacml-json-model`) — `SUPPORTED` (fetched primary sources)

1. **XML Policy and Request Parsing (`XmlUtils.java`, `XacmlJaxbParsingUtils.java`)**:
   - AuthzForce uses XJC-generated JAXB classes (`oasis.names.tc.xacml._3_0.core.schema.wd_17.*`) unmarshalled via `XmlUtils.XmlnsFilteringParser` (`XmlUtils.java:110-282`).
   - Instead of instantiating a new `SAXParserFactory` on every `unmarshal` call like [`BaseJAXBUnmarshaller.createUnmarshaller`](xacml-xml/src/main/java/org/xacml4j/v30/xml/BaseJAXBUnmarshaller.java#L172-L194), `SAXBasedXmlnsFilteringParser` initializes a single `private static final SAXParserFactory NS_AWARE_SAX_PARSER_FACTORY` once in a `static` block (`XmlUtils.java:186-195`).
   - When XPath / namespace filtering is needed, it wraps a `SAXParser.getXMLReader()` in an anonymous `XMLFilterImpl` (`XmlUtils.java:203-220`) that intercepts `startPrefixMapping(String prefix, String uri)` to record prefix-to-URI mappings for subsequent XPath compilation and explicitly rejects conflicting prefix redeclarations in the same document (`"ubu: prefix '" + prefix + "' already bound to " + boundUri`, `XmlUtils.java:211-215`), addressing AuthzForce issues `server#62` and `server#66`.
   - **Lazy / Skipped `<Content>` Parsing (`XacmlJaxbParsingUtils.java:65-185`, `XacmlJsonParsingUtils.java:60-170`)**: AuthzForce provides two `NamedXacmlAttributeParser` factories: `FullJaxbXACMLAttributesParserFactory` (which parses `<Content>` into a Saxon `XdmNode` via `SAXON_PROCESSOR.newDocumentBuilder()`) and `ContentSkippingXACMLAttributesParserFactory` (which ignores `<Content>` completely when XPath evaluation is disabled in the PDP configuration). When `<Content>` is parsed, AuthzForce uses a single static `Processor SAXON_PROCESSOR` configured from classpath `saxon.xml` with `Feature.XINCLUDE` verified as `false` (`XmlUtils.java:70-96`).
2. **XSD and JSON Schema Validation (`XacmlJsonUtils.java`, `BaseXacmlJsonRequestPreprocessor.java`)**:
   - AuthzForce validates XML policies and requests against the XACML 3.0 XSD during JAXB unmarshalling (`Xacml3JaxbHelper`). Notably, `authzforce/core/CHANGELOG.md` documents that turning on XACML 3.0 XSD validation in conformance tests exposed that several original OASIS conformance XML files are not strictly XSD-compliant.
   - For JSON requests (XACML 3.0 JSON Profile v1.1), `BaseXacmlJsonRequestPreprocessor.process(Map<String, String>, ...)` (`BaseXacmlJsonRequestPreprocessor.java:191-196`) runs `requestSchema.validate(json)` before mapping `JSONObject` into internal XACML attributes.
   - `XacmlJsonUtils.java:43-65` loads `Request.schema.json`, `Response.schema.json`, and `Policy.schema.json` from the classpath using `SchemaLoader.builder().schemaClient(SchemaClient.classPathAwareClient()).resolutionScope("classpath://org/ow2/authzforce/xacml/json/model/")` (`everit-json-schema`). This classpath-scoped `SchemaClient` was introduced specifically to fix offline/SSRF failure `GH-64` where `$ref` URIs previously attempted network resolution.
3. **JSON Limits and Duplicate-Key Rejection (`LimitsCheckingJSONObject.java:33-216`)**:
   - Because `org.json.JSONObject` has no built-in streaming limits and silently overwrites duplicate keys in some configurations, AuthzForce wrote `LimitsCheckingJSONObject extends JSONObject` with an inner `LimitsCheckingJSONTokener extends JSONTokener`:
     - **Max string length (`maxJsonStringLength`)**: Checked on every key and string value in `nextString(char quote)` by counting characters (`length++ > maxJsonStringLength`, throwing `IllegalArgumentException( maxStringLengthExceededMsg )` at `LimitsCheckingJSONObject.java:78-90`).
     - **Max nesting depth (`maxDepth`)**: Tracked by incrementing `depth` on `{` or `[` and decrementing on `}` or `]` inside `nextValue()` (`LimitsCheckingJSONObject.java:114-129`).
     - **Max container size (`maxNumOfImmediateChildren`)**: Checked inside `LimitsCheckingJSONArray` (`count++ > maxNumOfElements`, `LimitsCheckingJSONObject.java:155`) and `LimitsCheckingJSONObject` (`count++ > maxNumOfKeys`, `LimitsCheckingJSONObject.java:191`).
     - **Duplicate key rejection**: Uses `this.putOnce(key, x.nextValue())` (`LimitsCheckingJSONObject.java:194`), which throws `JSONException("Duplicate key \"" + key + "\"")` on any duplicate property in a JSON object.
4. **Error Reporting and Source Positions**:
   - AuthzForce fails fast on the first syntax, schema, or model error.
   - During JSON tokenization, `LimitsCheckingJSONTokener` wraps `IllegalArgumentException` via `throw syntaxError(e.getMessage(), e)` (`LimitsCheckingJSONObject.java:93, 117`), which appends `JSONTokener.toString()` (`" at <index> [character <col> line <line>]"`).
   - Once `LimitsCheckingJSONObject` or JAXB unmarshalling completes, however, the resulting `JSONObject` or JAXB tree carries **no source locations** (`-Xlocator` is not used). Any error during `BaseXacmlJsonRequestPreprocessor` or JAXB-to-PDP compilation throws `IndeterminateEvaluationException(..., XacmlStatusCode.SYNTAX_ERROR.value())` or `IllegalArgumentException` with a text description (e.g., `"Invalid Attributes/Attribute[@AttributeId='...']"`) and **no line or column number**.

##### B. AT&T XACML 3.0 (`att/xacml-3.0`) — `SUPPORTED` (fetched primary sources)

1. **XML Policy and Request Parsing (`DOMPolicyDef.java`, `DOMPolicy.java`, `DOMRequest.java`, `DOMUtil.java`)**:
   - AT&T XACML avoids JAXB in its core PDP and instead parses XML into a standard W3C DOM (`org.w3c.dom.Document`) via `DocumentBuilderFactory.newInstance()`, walking child `NodeList` elements manually in `DOMPolicyDef.newInstance(Document, ...)` (`DOMPolicyDef.java:103-140`), `DOMPolicy.newInstance(Node, ...)` (`DOMPolicy.java:56-163`), and `DOMRequest.load(Node)` (`DOMRequest.java:129-190`).
   - It allocates a new `DocumentBuilderFactory` on every `load()` call (`DOMPolicyDef.java:81`, `DOMUtil.java:460`, `DOMRequest.java:101`).
2. **XML Security Hardening Gap (`DOMPolicyDef.java:81-91` vs `DOMRequest.java:101-106`)**:
   - Because AT&T XACML duplicates `DocumentBuilderFactory.newInstance()` across multiple classes instead of routing through a single hardened factory owner (the exact problem `D6` solves in `xacml4j`):
     - `DOMPolicyDef.load(InputStream)` (`DOMPolicyDef.java:81-84`) and `DOMUtil.loadDocument(File)` (`DOMUtil.java:462-464`) set `XMLConstants.ACCESS_EXTERNAL_DTD = ""`, `XMLConstants.ACCESS_EXTERNAL_SCHEMA = ""`, and `XMLConstants.FEATURE_SECURE_PROCESSING = true`.
     - **`DOMRequest.load(InputStream)` (`DOMRequest.java:101-104`) sets `ACCESS_EXTERNAL_DTD = ""` and `ACCESS_EXTERNAL_SCHEMA = ""`, but omits `FEATURE_SECURE_PROCESSING = true`!**
     - Neither class sets `http://apache.org/xml/features/disallow-doctype-decl = true`, leaving in-line entity expansion (`<!DOCTYPE ... [ <!ENTITY ...> ]>`) governed only by default JDK limits where `FEATURE_SECURE_PROCESSING` is enabled, and unguarded in `DOMRequest.load(InputStream)`.
3. **XSD Validation and Lenient/Repair Mode**:
   - XSD validation is not used in `DOMPolicyDef.load` or `DOMRequest.load`. Instead, `DOMPolicy` and `DOMRequest` perform hand-coded structural checks and support a global `DOMProperties.isLenient()` mode plus mutating `DOMPolicy.repair(Node)` (`DOMPolicy.java:165+`) and `DOMRequest.repair(Node)` (`DOMRequest.java:192+`) methods that silently synthesize missing default attributes or delete unexpected elements in the DOM tree.
4. **Error Reporting and Source Positions (`DOMStructureException.java:14-48`, `DOMUtil.java:122-125`)**:
   - When a structural or value error occurs during the DOM walk, AT&T throws `DOMStructureException(Node nodeError, String message, Throwable cause)` (`DOMStructureException.java:14-48`), which stores a reference to the offending `org.w3c.dom.Node`.
   - However, because standard Java `DocumentBuilder` does not populate line or column metadata on `org.w3c.dom.Node` user-data, `DOMUtil.getNodeLabel(Node)` (`DOMUtil.java:122-125`) can only format `node.getNamespaceURI() + ":" + node.getLocalName()` (a bare tag name, not even a full path or index, and **no line or column**).
   - Errors are single-error fail-fast (`throw new DOMStructureException(...)`).

##### C. WSO2 Balana (`wso2/balana`) — `SUPPORTED` (fetched primary sources)

1. **XML Policy and Request Parsing (`Utils.java`, `FileBasedPolicyFinderModule.java`, `RequestCtxFactory.java`, `InputParser.java`, `Policy.java`)**:
   - Derived from SunXACML, Balana parses both policies (`FileBasedPolicyFinderModule.java:214-245`, `Policy.getInstance(Node)` at `Policy.java:320+`) and requests (`RequestCtxFactory.java:127-148`, `InputParser.java:88-119`) into `org.w3c.dom.Document` and walks the DOM.
   - In `PDP.evaluate(String)` (`PDP.java:128`), Balana runs `request.replaceAll(">\\s+<", "><")` across the raw XML string before parsing to strip inter-element whitespace because its DOM walkers fail or misbehave when whitespace text nodes appear where element children are expected.
2. **XML Security Hardening (`Utils.java:86-109`)**:
   - `Utils.getSecuredDocumentBuilderFactory()` configures `setXIncludeAware(false)`, `setExpandEntityReferences(false)`, `FEATURE_SECURE_PROCESSING = true`, `external-general-entities = false`, `external-parameter-entities = false`, `load-external-dtd = false`, and attaches an `org.apache.xerces.util.SecurityManager` with `setEntityExpansionLimit(0)` (`ENTITY_EXPANSION_LIMIT = 0`, `Utils.java:44, 105-107`).
   - **Critical Fail-Open Flaw (`Utils.java:100-103`)**: Inside `getSecuredDocumentBuilderFactory()`, `catch (ParserConfigurationException e)` logs `log.error("Failed to load XML Processor Feature ...", e)` and **returns the partially configured `DocumentBuilderFactory` anyway**! This is the exact same fail-open defect present in `xacml4j`'s [`DOMUtil.getDocumentBuilderFactory()`](xacml-core/src/main/java/org/xacml4j/util/DOMUtil.java#L112-L114) that `D6` remedies.
3. **XSD Validation (`InputParser.java:57-112`, `RequestCtxFactory.java:91-96`)**:
   - Disabled by default (`RequestCtxFactory.java:91-96`). Optional DTD/XSD validation is enabled only if the JVM system property `com.sun.xacml.ContextSchema` is set to a `File` path (`InputParser.java:57-112`) or a `File` schema is passed to `PolicyReader`.
4. **Error Reporting and Source Positions (`InputParser.java:126-149`)**:
   - `InputParser` implements `org.xml.sax.ErrorHandler`. In `warning`, `error`, and `fatalError` (`InputParser.java:126-149`), it logs `logger.error("Line " + exception.getLineNumber() + ": " + exception.getMessage())` and then executes `throw new SAXException("error parsing context")` or `throw new SAXException("invalid context document")` — **discarding the `SAXParseException` cause, the line number, and the message** from the exception returned to the caller!
   - Semantic/model errors during the DOM walk throw `ParsingException(String)` with no location or path.

---

#### 1.3 Published XML and JSON Parsing Techniques — `SUPPORTED` (arXiv primary literature)

1. **Eliminating Intermediate Tree Materialization ("On-Demand" / Single-Pass Streaming)**:
   - **Keiser & Lemire (2024)**, *"On-Demand JSON: A Better Way to Parse Documents?"*, `arXiv:2312.17149v3` (and **Langdale & Lemire (2019)**, *"Parsing Gigabytes of JSON per Second"*, `arXiv:1902.08318v7`, *VLDB Journal* 28(6)):
     - Demonstrates that traditional two-phase parsing—where phase 1 materializes a generic in-memory tree of nodes/values (such as Gson's `JsonElement`/`JsonObject` in `xacml4j`'s [`RequestContextAdapter`](xacml-json/src/main/java/org/xacml4j/v30/marshal/json/RequestContextAdapter.java#L54-L98), AuthzForce's `LimitsCheckingJSONObject`, or JAXB's `PolicyType`/`RuleType`/`JAXBElement` intermediate objects in [`BaseJAXBUnmarshaller`](xacml-xml/src/main/java/org/xacml4j/v30/xml/BaseJAXBUnmarshaller.java#L202-L216)), and phase 2 walks that tree to construct domain objects—is dominated by intermediate object allocation, pointer chasing, and duplicate hash-table lookups.
     - Single-pass / pull-based streaming directly into domain builders:
       1. Avoids allocating intermediate tree nodes (`JsonObject`, `JsonArray`, `JsonPrimitive`, `LinkedTreeMap`, or JAXB schema objects) altogether.
       2. Allows skipping unused subtrees (such as XACML `<Content>` or `Advice` when not needed) without materializing their DOM/tree representation (`arXiv:2312.17149v3`, Section 2 & 4).
       3. Preserves exact token-level cursor positions (byte offset, line, column) at the moment a domain validation rule runs, because the domain builder consumes tokens directly from the underlying stream reader (`JsonParser` or `XMLStreamReader`).
2. **Integrated XML Scanning, Validation, and Deserialization**:
   - **Kostoulas et al. (2006)**, *"XML Screamer: An Integrated Approach to High Performance XML Parsing, Validation and Deserialization"* (*WWW '06*):
     - Shows that layering separate XML scanning, SAX filter chains, schema validation, JAXB tree binding, and domain-model mapping incurs multiplicative per-element overhead and memory churn.
     - Integrating pull-based StAX (`XMLStreamReader`, e.g., Woodstox or JDK Wstx/Xerces StAX) with direct domain-model construction (and optional single-pass `Validator` over `StAXSource`) eliminates the intermediate binding layer while giving the model builder direct access to `XMLStreamReader.getLocation()` (`Location.getLineNumber()`, `Location.getColumnNumber()`) at **zero per-element allocation cost on valid inputs**.

---

### 2. Sources

| Source | URL / Path | Retrieved Date | Coverage Label |
| --- | --- | --- | --- |
| OW2 AuthzForce `XmlUtils.java` | `https://raw.githubusercontent.com/authzforce/core-pdp-api/develop/src/main/java/org/ow2/authzforce/core/pdp/api/XmlUtils.java` | 2026-10-27 | `SUPPORTED` |
| OW2 AuthzForce `XacmlJaxbParsingUtils.java` | `https://raw.githubusercontent.com/authzforce/core-pdp-api/develop/src/main/java/org/ow2/authzforce/core/pdp/api/io/XacmlJaxbParsingUtils.java` | 2026-10-27 | `SUPPORTED` |
| OW2 AuthzForce `LimitsCheckingJSONObject.java` | `https://raw.githubusercontent.com/authzforce/xacml-json-model/develop/src/main/java/org/ow2/authzforce/xacml/json/model/LimitsCheckingJSONObject.java` | 2026-10-27 | `SUPPORTED` |
| OW2 AuthzForce `XacmlJsonUtils.java` | `https://raw.githubusercontent.com/authzforce/xacml-json-model/develop/src/main/java/org/ow2/authzforce/xacml/json/model/XacmlJsonUtils.java` | 2026-10-27 | `SUPPORTED` |
| OW2 AuthzForce `BaseXacmlJsonRequestPreprocessor.java` | `https://raw.githubusercontent.com/authzforce/core/develop/pdp-io-xacml-json/src/main/java/org/ow2/authzforce/core/pdp/io/xacml/json/BaseXacmlJsonRequestPreprocessor.java` | 2026-10-27 | `SUPPORTED` |
| OW2 AuthzForce `XacmlJsonParsingUtils.java` | `https://raw.githubusercontent.com/authzforce/core/develop/pdp-io-xacml-json/src/main/java/org/ow2/authzforce/core/pdp/io/xacml/json/XacmlJsonParsingUtils.java` | 2026-10-27 | `SUPPORTED` |
| OW2 AuthzForce `CHANGELOG.md` | `https://raw.githubusercontent.com/authzforce/core/develop/CHANGELOG.md` | 2026-10-27 | `SUPPORTED` |
| AT&T XACML 3.0 `DOMUtil.java` | `https://raw.githubusercontent.com/att/xacml-3.0/master/xacml/src/main/java/com/att/research/xacml/std/dom/DOMUtil.java` | 2026-10-27 | `SUPPORTED` |
| AT&T XACML 3.0 `DOMStructureException.java` | `https://raw.githubusercontent.com/att/xacml-3.0/master/xacml/src/main/java/com/att/research/xacml/std/dom/DOMStructureException.java` | 2026-10-27 | `SUPPORTED` |
| AT&T XACML 3.0 `DOMRequest.java` | `https://raw.githubusercontent.com/att/xacml-3.0/master/xacml/src/main/java/com/att/research/xacml/std/dom/DOMRequest.java` | 2026-10-27 | `SUPPORTED` |
| AT&T XACML 3.0 `DOMPolicyDef.java` | `https://raw.githubusercontent.com/att/xacml-3.0/master/xacml-pdp/src/main/java/com/att/research/xacmlatt/pdp/policy/dom/DOMPolicyDef.java` | 2026-10-27 | `SUPPORTED` |
| AT&T XACML 3.0 `DOMPolicy.java` | `https://raw.githubusercontent.com/att/xacml-3.0/master/xacml-pdp/src/main/java/com/att/research/xacmlatt/pdp/policy/dom/DOMPolicy.java` | 2026-10-27 | `SUPPORTED` |
| WSO2 Balana `Utils.java` | `https://raw.githubusercontent.com/wso2/balana/master/modules/balana-utils/src/main/java/org/wso2/balana/utils/Utils.java` | 2026-10-27 | `SUPPORTED` |
| WSO2 Balana `RequestCtxFactory.java` | `https://raw.githubusercontent.com/wso2/balana/master/modules/balana-core/src/main/java/org/wso2/balana/ctx/RequestCtxFactory.java` | 2026-10-27 | `SUPPORTED` |
| WSO2 Balana `InputParser.java` | `https://raw.githubusercontent.com/wso2/balana/master/modules/balana-core/src/main/java/org/wso2/balana/ctx/InputParser.java` | 2026-10-27 | `SUPPORTED` |
| WSO2 Balana `FileBasedPolicyFinderModule.java` | `https://raw.githubusercontent.com/wso2/balana/master/modules/balana-core/src/main/java/org/wso2/balana/finder/impl/FileBasedPolicyFinderModule.java` | 2026-10-27 | `SUPPORTED` |
| WSO2 Balana `Policy.java` | `https://raw.githubusercontent.com/wso2/balana/master/modules/balana-core/src/main/java/org/wso2/balana/Policy.java` | 2026-10-27 | `SUPPORTED` |
| OASIS JSON Profile of XACML 3.0 Version 1.1 (OS) | `https://docs.oasis-open.org/xacml/xacml-json-http/v1.1/os/xacml-json-http-v1.1-os.html` | 2026-10-27 | `SUPPORTED` |
| Keiser & Lemire (2024), *"On-Demand JSON: A Better Way to Parse Documents?"* | `https://arxiv.org/abs/2312.17149` (`arXiv:2312.17149v3`, peer-reviewed in *Software: Practice and Experience*) | 2026-10-27 | `SUPPORTED` |
| Langdale & Lemire (2019), *"Parsing Gigabytes of JSON per Second"* | `https://arxiv.org/abs/1902.08318` (`arXiv:1902.08318v7`, peer-reviewed in *VLDB Journal* 28(6), 941–960) | 2026-10-27 | `SUPPORTED` |
| `xacml4j` XML & JSON Unmarshalling Sources | [BaseJAXBUnmarshaller.java](xacml-xml/src/main/java/org/xacml4j/v30/xml/BaseJAXBUnmarshaller.java), [BooleanAttributeValidationFilter.java](xacml-xml/src/main/java/org/xacml4j/v30/xml/BooleanAttributeValidationFilter.java), [Xacml30PolicyFromJaxbToObjectModelMapper.java](xacml-xml/src/main/java/org/xacml4j/v30/xml/Xacml30PolicyFromJaxbToObjectModelMapper.java), [Xacml30RequestContextFromJaxbToObjectModelMapper.java](xacml-xml/src/main/java/org/xacml4j/v30/xml/Xacml30RequestContextFromJaxbToObjectModelMapper.java), [DOMUtil.java](xacml-core/src/main/java/org/xacml4j/util/DOMUtil.java), [SyntaxException.java](xacml-core/src/main/java/org/xacml4j/v30/SyntaxException.java), [JsonRequestContextUnmarshaller.java](xacml-json/src/main/java/org/xacml4j/v30/marshal/json/JsonRequestContextUnmarshaller.java), [RequestContextAdapter.java](xacml-json/src/main/java/org/xacml4j/v30/marshal/json/RequestContextAdapter.java), [AttributeDeserializer.java](xacml-json/src/main/java/org/xacml4j/v30/marshal/json/AttributeDeserializer.java) | 2026-10-27 | `SUPPORTED` |

---

### 3. Confidence and Disagreement / Drift Risk

- **Confidence**: **High (`SUPPORTED`)**. Every claim about OW2 AuthzForce, AT&T XACML 3.0, and WSO2 Balana is verified directly against the upstream Java source files retrieved on `2026-10-27`, and every claim about `xacml4j` is verified in the opened repository source.
- **Disagreement or Drift Risk**:
  - **XSD Validation Default (AuthzForce vs. Balana/AT&T/`xacml4j`)**: AuthzForce enables XACML 3.0 XSD validation by default on JAXB unmarshalling and JSON Schema validation on JSON requests, whereas Balana, AT&T XACML, and `xacml4j` keep runtime XSD validation off by default. AuthzForce's own `CHANGELOG.md` confirms why: several original OASIS conformance XML fixtures violate strict XSD constraints, so turning XSD validation on by default would break existing callers and conformance fixtures unless opted in (`D7`).
  - **Lenient / Mutating Repair vs. Fail-Closed Diagnostics**: AT&T XACML includes a `DOMProperties.isLenient()` mode and `repair(Node)` methods that mutate invalid DOM trees in place (`DOMPolicy.java:165`, `DOMRequest.java:192`). For a security-critical policy engine, silent mutation of malformed access-control policies or requests is dangerous and contradicts `xacml4j`'s fail-closed security posture (`D4`, `D6`).

---

### 4. Concrete Recommendations for Design Decisions (Most Important First)

#### 1. Decision D9 (Parse Optimization Cycles) & Decision D3 (Source Positions for XML Model Errors)

- **What to change in [design.md](openspec/changes/improve-xacml-parsing/design.md#L120-L132) (`D3`) and [design.md](openspec/changes/improve-xacml-parsing/design.md#L194-L202) (`D9`)**:
  1. **Evaluate `D3(b)` (Direct StAX `XMLStreamReader` to Domain Model) as the primary target for `D3` + `D9`, or stage `D3(a)` -> `D3(b)` explicitly**:
     - None of AuthzForce, AT&T XACML, or WSO2 Balana achieves line/column source positions on semantic/model-level errors (`AT&T` only attaches an unpositioned `org.w3c.dom.Node`; `AuthzForce` and `Balana` lose positions after unmarshalling). Thus `xacml4j`'s positioned diagnostics (`D1`/`D3`) and `xacmlc` (`D10`) will be first-in-class among open-source XACML engines.
     - With the maintainer's approval in [proposal.md](openspec/changes/improve-xacml-parsing/proposal.md#L141-L151) adding the **`xacmlc` GraalVM 25 native executable** (`D10`) and directing `D9` to **"Analyze allocations and optimize them to the limits"**, compare `D3(a)` (`jaxb2-basics-runtime` `-Xlocator` on JAXB classes) vs. `D3(b)` (direct StAX `XMLStreamReader` parser):
       - **Allocation limit (`D9`)**: JAXB + `-Xlocator` (`D3(a)`) allocates an intermediate JAXB object (`PolicyType`, `RuleType`, `ApplyType`, `AttributeDesignatorType`, `JAXBElement<?>`) **plus** a `org.xml.sax.helpers.LocatorImpl` instance on **every single XML element**, even when the input is 100% valid, and then copies everything a second time in [`Xacml30PolicyFromJaxbToObjectModelMapper`](xacml-xml/src/main/java/org/xacml4j/v30/xml/Xacml30PolicyFromJaxbToObjectModelMapper.java#L106-L150) / [`Xacml30RequestContextFromJaxbToObjectModelMapper`](xacml-xml/src/main/java/org/xacml4j/v30/xml/Xacml30RequestContextFromJaxbToObjectModelMapper.java#L75-L162). By contrast, a direct StAX `XMLStreamReader` parser (`D3(b)`) constructs `xacml-core` domain objects in **one pass** (per Keiser & Lemire `arXiv:2312.17149v3` and *XML Screamer*), allocates **zero intermediate JAXB objects**, and reads `reader.getLocation()` **only on the cold error path** (**zero location-tracking allocation on valid input**).
       - **GraalVM 25 native-image (`D10`)**: `D3(b)` uses zero reflection and zero JAXB runtime metadata, eliminating GraalVM `native-image` reflection/proxy configuration overhead for `xacmlc`.
       - **Multi-error recovery (`D4`)**: In a hand-written StAX pull parser (`D3(b)`), skipping to the matching `END_ELEMENT` of a broken `<Rule>`, `<Condition>`, `<ObligationExpression>`, or `<Attributes>` when `maxErrors > 1` is a deterministic depth-counted loop, whereas JAXB event handlers often abort or cascade when child subtrees fail binding.
  2. **Adopt AuthzForce's Static Factory Reuse + Lazy/Skipped `<Content>` Handling (`D9`)**:
     - If JAXB/SAX remains on any path, replace per-call `SAXParserFactory.newInstance()` in [`BaseJAXBUnmarshaller.createUnmarshaller`](xacml-xml/src/main/java/org/xacml4j/v30/xml/BaseJAXBUnmarshaller.java#L172-L194) with a single shared hardened factory (as AuthzForce does in `XmlUtils.java:186`) plus pooled/thread-local `XMLReader` / `XMLStreamReader`.
     - In [`Xacml30RequestContextFromJaxbToObjectModelMapper.createCategory`](xacml-xml/src/main/java/org/xacml4j/v30/xml/Xacml30RequestContextFromJaxbToObjectModelMapper.java#L95-L120) and [`CategoryAdapter.deserialize`](xacml-json/src/main/java/org/xacml4j/v30/marshal/json/CategoryAdapter.java#L54-L90), avoid calling `DOMUtil.copyNode(Node)` when `<Content>` is absent or adopt lazy `<Content>` DOM materialization (inspired by AuthzForce's `ContentSkippingXACMLAttributesParserFactory` in `XacmlJaxbParsingUtils.java:120-165`).
     - Replace `new LinkedList<>()` and stream allocations in [`Xacml30PolicyFromJaxbToObjectModelMapper`](xacml-xml/src/main/java/org/xacml4j/v30/xml/Xacml30PolicyFromJaxbToObjectModelMapper.java#L387-L611), [`Xacml20PolicyFromJaxbToObjectModelMapper`](xacml-xml/src/main/java/org/xacml4j/v30/xml/Xacml20PolicyFromJaxbToObjectModelMapper.java#L417-L702), and [`Xacml30RequestContextFromJaxbToObjectModelMapper`](xacml-xml/src/main/java/org/xacml4j/v30/xml/Xacml30RequestContextFromJaxbToObjectModelMapper.java#L85-L152) with pre-sized `ArrayList` or `ImmutableList.builderWithExpectedSize(...)`.

#### 2. Decision D8 (JSON Provider and Streaming Architecture)

- **What to change in [design.md](openspec/changes/improve-xacml-parsing/design.md#L180-L192) (`D8`)**:
  1. **Eliminate Two-Pass `JsonElement` Tree Materialization in `xacml-json` via Single-Pass Streaming (`Jackson JsonParser` or `Gson JsonReader`)**:
     - Today, [`RequestContextAdapter.deserialize`](xacml-json/src/main/java/org/xacml4j/v30/marshal/json/RequestContextAdapter.java#L54-L98), [`CategoryAdapter.deserialize`](xacml-json/src/main/java/org/xacml4j/v30/marshal/json/CategoryAdapter.java#L54-L90), and [`AttributeDeserializer.deserialize`](xacml-json/src/main/java/org/xacml4j/v30/marshal/json/AttributeDeserializer.java#L57-L114) implement Gson's `JsonDeserializer<T>`, which forces Gson to parse the entire JSON stream into an intermediate `JsonElement` / `JsonObject` / `LinkedTreeMap` DOM tree before any XACML validation or model mapping runs. AuthzForce's `LimitsCheckingJSONObject` + `everit-json-schema` (`BaseXacmlJsonRequestPreprocessor.java:191-196`) suffers from the same two-pass tree materialization overhead.
     - Per Keiser & Lemire (`arXiv:2312.17149v3`) and Langdale & Lemire (`arXiv:1902.08318v7`), switching `xacml-json` to a **single-pass streaming pull reader** (`com.fasterxml.jackson.core.JsonParser` from `jackson-core`, or Gson `JsonReader` if keeping Gson) that builds `RequestContext`, `Category`, `Entity`, and `Attribute` directly from tokens:
       - Eliminates 100% of intermediate `JsonObject` / `JsonArray` / `JsonPrimitive` / `LinkedTreeMap` allocations.
       - Reads UTF-8 `InputStream` / `byte[]` directly in `jackson-core` (`UTF8StreamJsonParser`) without an intermediate `InputStreamReader` char-buffer copy.
       - Provides exact `JsonLocation` (`getLineNr()`, `getColumnNr()`, `getByteOffset()`) on every token for `ParseDiagnostic` (`D1`), including value conversion errors (`TypeToGSon`) and missing required fields.
       - Enforces `StreamReadFeature.STRICT_DUPLICATE_DETECTION` natively in `jackson-core` (matching AuthzForce's `LimitsCheckingJSONObject.putOnce` at `LimitsCheckingJSONObject.java:194` without custom hash sets).

#### 3. Decision D6 (Hardened Factory Owner and Resource Limits)

- **What to change in [design.md](openspec/changes/improve-xacml-parsing/design.md#L156-L169) (`D6`) and [specs/parse-security/spec.md](openspec/changes/improve-xacml-parsing/specs/parse-security/spec.md)**:
  1. **Add `maxContainerSize` (max keys per JSON object / max elements per JSON array / max child elements per XML element) to `ParseOptions`**:
     - AuthzForce's `LimitsCheckingJSONObject` (`LimitsCheckingJSONObject.java:155, 191`) specifically bounds `maxNumOfImmediateChildren` (keys per JSON object and items per JSON array) in addition to `maxDepth` and `maxJsonStringLength`, because a flat JSON request with 1,000,000 array items at depth 2 bypasses `maxDepth` and `maxStringLength` while still causing heap exhaustion (`DoS`). Adding `maxContainerSize` (default e.g. `10_000`) to `ParseOptions` closes this vector for both JSON and XML.
  2. **Centralize All XML Creation in One Fail-Closed Factory Owner**:
     - Avoid AT&T XACML's mistake (`DOMRequest.java:101-104` forgetting `FEATURE_SECURE_PROCESSING` while `DOMPolicyDef.java:81-84` sets it) and Balana's mistake (`Utils.java:100-103` catching `ParserConfigurationException`, logging, and continuing — identical to [`DOMUtil.java:112-114`](xacml-core/src/main/java/org/xacml4j/util/DOMUtil.java#L112-L114)).
     - Every XML entry point (`xacml-xml` policy/request/response unmarshallers, `DOMUtil`, and `xacmlc`) MUST route through the single fail-closed factory owner, with `http://apache.org/xml/features/disallow-doctype-decl = true` set unconditionally (fixing [`BaseJAXBUnmarshaller.java:172-184`](xacml-xml/src/main/java/org/xacml4j/v30/xml/BaseJAXBUnmarshaller.java#L172-L184)).
  3. **Reject Conflicting XML Namespace Prefix Redeclarations (`xmlns`)**:
     - Following AuthzForce's `SAXBasedXmlnsFilteringParser` (`XmlUtils.java:209-216`), consider recording or validating namespace prefix mappings when `<AttributeSelector>` / `xpathExpression` values are present so XPath expressions have deterministic namespace bindings.

#### 4. Decision D7 (Optional XSD Validation)

- **What to change in [design.md](openspec/changes/improve-xacml-parsing/design.md#L171-L178) (`D7`)**:
  1. **Keep `validateSchema` Off by Default in Library Mode, On by Default (or `--schema`) in `xacmlc` (`D10`)**:
     - AuthzForce (`CHANGELOG.md`), Balana (`RequestCtxFactory.java:91`), and AT&T XACML all confirm that strict XACML XSD validation rejects a subset of legacy/conformance XML documents. Keeping `ParseOptions.defaults().validateSchema() == false` for library callers preserves backward compatibility, while `xacmlc` (the strict policy compiler CLI in `D10`) can enable schema/strict structural checks or expose `--validate-schema`.
  2. **Use a Classpath-Only `LSResourceResolver` Blocking All Network/Filesystem URI Schemes**:
     - Mirroring AuthzForce's fix for `GH-64` (`SchemaClient.classPathAwareClient()` in `XacmlJsonUtils.java:43-65`), ensure the pre-compiled `javax.xml.validation.Schema` in `xacml-xml` resolves imported XSDs (`xml.xsd`, `xacml-core-v2-schema-wd-17.xsd`, `xacml-core-v3-schema-wd-17.xsd`) strictly from `xacml-xml-jaxb` classpath resources and rejects any external `http://`, `https://`, or `file://` resolution.

---

### 5. Risks and Open Questions

- **Risks**:
  1. **JAXB + `-Xlocator` (`D3(a)`) vs. "Optimize Allocations to the Limits" (`D9`)**: Choosing `D3(a)` (`-Xlocator`) adds one `LocatorImpl` allocation per XML element on top of JAXB's existing intermediate AST allocation, moving in the opposite direction of the maintainer's D9 allocation directive and complicating GraalVM 25 `native-image` reflection metadata for `xacmlc` (`D10`).
  2. **OASIS Conformance Fixture Quirks Under Strict Limits/Validation**: As AuthzForce documented in its `CHANGELOG.md`, some vendored OASIS conformance fixtures contain non-schema-compliant constructs or legacy datatype URIs; `Q6` must inventory these before tightening default structural checks.
- **Open Questions**:
  1. Should `xacml-xml` move directly to `D3(b)` (a streaming StAX `XMLStreamReader` unmarshaller for XACML 2.0 and 3.0 policies/requests) so that `D3` (exact line/column/XPath positions), `D4` (multi-error recovery), `D9` (zero intermediate JAXB/Locator allocations), and `D10` (reflection-free GraalVM 25 `xacmlc` native image) are solved by a single architecture?
  2. Should `ParseOptions` include `maxContainerSize` (bounding JSON object keys, JSON array elements, and XML child elements per parent, e.g. default `10_000`) alongside `maxDepth`, `maxTokenCount`, `maxStringLength`, and `maxInputBytes`?
