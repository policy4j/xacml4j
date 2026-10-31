<!-- Discovery lane DQ3 (task 1.1): Codex GPT-6.1 Sol, read-only, after one follow-up; question Q4 and design D8. -->

FINAL REPORT (DQ3 JSON research engineer, Codex GPT-6.1 Sol)

**Q4 answer:** Shortlist Gson 2.15.0 streaming and Jackson core 2.22.4/3.2.4 streaming. Jackson offers the more complete native constraint and position APIs. Keep the provider decision pending the bake-off: no performance winner is established.

D8 needs two corrections before implementation: provider selection must happen before obtaining an unmarshaller, and parity must include the OASIS JSON Profile rather than only current Gson behavior. D6 needs explicit units, exact boundary checks, and a contract for already parsed `JsonElement` inputs.

All external sources linked below were retrieved **2026-10-27**. This lane changed no files and ran no builds, tests, benchmarks, profilers, Git writes, or beads writes.

**Q4 — Framework limits and positions**

| Candidate | Verified capabilities | Consequence for this change |
| --- | --- | --- |
| **Gson 2.15.0** | `Strictness.STRICT`; `JsonReader.setNestingLimit`, available since 2.12.0, default **255**; `getPath`/`getPreviousPath`; new **`getCharacterOffset()` in 2.15.0**. | Explicitly configure depth 256. Use streaming adapters plus application guards for the remaining limits and duplicate names. Retain source spans for model errors. |
| **Jackson 2.22.4** | `StreamReadConstraints` supports depth, document length, token count, number length, string length and name length. `STRICT_DUPLICATE_DETECTION` checks repeated object names. `JsonLocation` exposes line, column and available byte/character offsets. | Strong streaming candidate with relatively little constraint infrastructure to supply. Native limits still need normalization and boundary tests. |
| **Jackson 3.2.4** | Corresponding core APIs use `tools.jackson.core`. Its location type is **`TokenStreamLocation`**, returned by `currentTokenLocation()` and `currentLocation()`. | D8 must name the actual 3.x API. Keep Jackson types inside its provider module and translate positions into the core diagnostic model. |

Sources: [Gson tagged JsonReader](https://github.com/google/gson/blob/gson-parent-2.15.0/gson/src/main/java/com/google/gson/stream/JsonReader.java), [Gson 2.15.0 release](https://github.com/google/gson/releases/tag/gson-parent-2.15.0), [Jackson 2.22.4 constraints](https://github.com/FasterXML/jackson-core/blob/jackson-core-2.22.4/src/main/java/com/fasterxml/jackson/core/StreamReadConstraints.java), [Jackson 2.22.4 read features](https://github.com/FasterXML/jackson-core/blob/jackson-core-2.22.4/src/main/java/com/fasterxml/jackson/core/StreamReadFeature.java), [JsonLocation](https://github.com/FasterXML/jackson-core/blob/jackson-core-2.22.4/src/main/java/com/fasterxml/jackson/core/JsonLocation.java), [Jackson 3.2.4 parser](https://github.com/FasterXML/jackson-core/blob/jackson-core-3.2.4/src/main/java/tools/jackson/core/JsonParser.java).

**Important differences:**

- Jackson 2.22.4 defaults to depth **1,000** and string length **20,000,000**; Jackson 3.2.4 defaults to **500** and **100,000,000**. Both leave document length and token count unlimited by default. Configure project limits explicitly. [2.22.4 defaults](https://github.com/FasterXML/jackson-core/blob/jackson-core-2.22.4/src/main/java/com/fasterxml/jackson/core/StreamReadConstraints.java), [3.2.4 defaults](https://github.com/FasterXML/jackson-core/blob/jackson-core-3.2.4/src/main/java/tools/jackson/core/StreamReadConstraints.java).
- Jackson’s document limit counts input units: bytes for byte input, characters for character input. Its documentation describes chunk-based document checks and approximate string/name limits. These do not alone establish an exact provider-independent boundary. [Tagged constraint contracts](https://github.com/FasterXML/jackson-core/blob/jackson-core-2.22.4/src/main/java/com/fasterxml/jackson/core/StreamReadConstraints.java).
- Distinguish token-start positions from the current cursor. Jackson exposes both; Gson’s new offset is a cursor offset. Capture locations before conversion, especially when conversion occurs after other members have been consumed. [Jackson parser](https://github.com/FasterXML/jackson-core/blob/jackson-core-3.2.4/src/main/java/tools/jackson/core/JsonParser.java), [Gson reader](https://github.com/google/gson/blob/gson-parent-2.15.0/gson/src/main/java/com/google/gson/stream/JsonReader.java).
- Do not infer duplicate rejection from strict JSON syntax. Make duplicate detection an independently tested requirement under the project’s `parse-security/spec.md:56`.

**Confidence:** High for the tagged APIs and defaults. Medium for complete enforcement across every parser/input path; runtime boundary tests were not run.

**Disagreement or drift risk:** Context7 returned mixed-version documentation. Tagged sources take precedence. The design’s statement that Gson positions exist only in exception messages is stale for 2.15.0.

**Q4 — Fast Jackson techniques**

1. **Stream directly into model builders.** Compare handwritten `JsonParser` parsing with Gson streaming, rather than comparing Jackson streaming only against the current tree adapters. FasterXML documents direct streaming and avoiding unnecessary intermediate representations. Its historic speed percentages are not evidence for this workload. [FasterXML performance guidance](https://github.com/FasterXML/jackson-docs/wiki/Presentation:-Jackson-Performance).

2. **Reuse configured factories.** Share a configured `JsonFactory`; create and close parsers per operation. For per-parse limits, use shared factories per immutable limits configuration, with a bounded configuration cache. Do not mutate one shared factory while requests run. [JsonFactory contract](https://github.com/FasterXML/jackson-core/blob/jackson-core-2.22.4/src/main/java/com/fasterxml/jackson/core/JsonFactory.java).

3. **Benchmark pre-encoded field matching.** Jackson’s UTF-8 parser uses `SerializableString.asQuotedUTF8()` in its optimized field-name path. Test reusable `SerializedString` constants against ordinary decoded-name dispatch. Preserve arbitrary member order, escaped equivalent names and buffer-boundary behavior. [UTF8StreamJsonParser](https://github.com/FasterXML/jackson-core/blob/jackson-core-2.22.4/src/main/java/com/fasterxml/jackson/core/json/UTF8StreamJsonParser.java).

4. **Avoid a complete intermediate tree.** Retain only information needed for deferred interpretation. An Attribute’s `DataType` can follow `Value`, and inference can require inspecting the complete value array. A streaming implementation therefore needs bounded deferred values and their positions, rather than an assumption about property order. [OASIS profile §§3.2.2, 3.3.2](https://docs.oasis-open.org/xacml/xacml-json-http/v1.1/os/xacml-json-http-v1.1-os.html).

5. **Treat Blackbird as a databind experiment.** It optimizes bean access through generated lambda accessors; it does not optimize a handwritten core token loop. Its documentation also warns against creating many mapper instances because generated accessors remain associated with class loaders. Include it only as a separate databind comparator if useful. [Blackbird documentation](https://github.com/FasterXML/jackson-modules-base/tree/2.x/blackbird).

Duplicate detection must remain enabled in candidate measurements. Jackson documents overhead for it, but that estimate is not a measured cost here. [StreamReadFeature](https://github.com/FasterXML/jackson-core/blob/jackson-core-2.22.4/src/main/java/com/fasterxml/jackson/core/StreamReadFeature.java).

**Confidence:** High for mechanisms; **UNVERIFIED** for workload gains. No representative third-party application implementation was confirmed in this lane; the cited implementation evidence is Jackson’s own parser and optimization module.

**Q4 — DSL-JSON and simdjson-java maintenance**

- **DSL-JSON:** The upstream README advertises 2.0.2, generated/custom converters and reusable readers/writers. Its GitHub Releases page instead labels 1.4.2 latest. That disagreement makes the Releases page unsuitable as the version authority. Current maintenance responsiveness and security-fix cadence remain **UNVERIFIED**; the evidence does not justify calling it abandoned. Keep it optional until limits, duplicates, diagnostics and release provenance are demonstrated. [Upstream README](https://github.com/ngs-doo/dsl-json), [release listing](https://github.com/ngs-doo/dsl-json/releases).
- **simdjson-java:** The 0.4.0 release notes raise the build JDK baseline to 24. Its tagged README requires Java 24, describes the pre-1.0 API as unstable, but retains older installation and benchmark instructions. It offers DOM and schema-based parsing. These are maintenance/documentation risks, not proof of abandonment. JDK 25 compatibility and required Vector API/runtime flags remain **UNVERIFIED**. [0.4.0 release](https://github.com/simdjson/simdjson-java/releases/tag/0.4.0), [tagged README](https://github.com/simdjson/simdjson-java/blob/0.4.0/README.md).

Do not transfer their published throughput results to this project: those tests use different data, operations, versions and machines. Admit either candidate only after it satisfies the same correctness, safety and diagnostic gates.

**Confidence:** Medium for release/documentation observations; low for current support responsiveness. The exact publication timestamps recorded in `research/README.md:32` were not independently reconfirmed.

**Q4 — OASIS JSON Profile 1.1 requirements**

The normative reference is the **20 June 2019 OASIS Standard**, not existing adapter behavior. [JSON Profile 1.1](https://docs.oasis-open.org/xacml/xacml-json-http/v1.1/os/xacml-json-http-v1.1-os.html).

The parser must preserve:

- **§§3.2.1–3.2.4:** exact names/case, order independence, cardinalities and prohibition of JSON null.
- **§§3.3.1–3.3.2:** full/shorthand datatype identifiers and inference over complete arrays; mixed integers/doubles promote to double, other mixed combinations fall back to strings.
- **§3.3.3:** structured XPath expressions and namespace rules.
- **§3.3.4:** prohibited special numeric values, including negative zero, produce Indeterminate/syntax-error.
- **§§4.2.1–4.2.2.2:** Request structure/defaults, category shorthand arrays and CategoryId consistency; at least one Category.
- **§4.2.3:** escaped XML or Base64 Content, carrying required namespaces.
- **§4.2.4:** mandatory AttributeId/Value and optional Issuer, DataType and IncludeInResult.
- **§§4.2.5–4.2.6:** nonempty request-reference arrays and references to Category Ids.
- **§5.2:** response/result structure, statuses, obligations, advice and identifiers.
- **§6:** `application/xacml+json` transport representation.

Two interpretation issues need explicit fixtures: Table 8 describes Value as an array while examples use scalars; request/category tables also contain `CodeBase`/`Codebase` spelling differences. Record the chosen compatibility interpretation.

**Confidence:** High for the fetched edition and section requirements; medium for those inconsistencies. Duplicate-member rejection is independently required by the project specification.

**Concrete recommendations, most important first**

1. **D8 — Correct provider selection and module boundaries.**

   Add stable provider identities and explicit selection at the factory/builder layer. `ParseOptions` alone is too late when the unmarshaller has already been selected.

   The current discovery API collapses providers into `Map<MediaType, MarshallingProvider>` and resolves collisions with `(a,b)->a`. Two JSON providers therefore cannot be selected reliably through the existing media-type-only lookup. Evidence: `xacml-core/src/main/java/org/xacml4j/v30/marshal/MarshallingProvider.java:72`.

   Keep `xacml-json` and its public Gson adapters. Put a selected replacement in the approved new module, with its own provider registration and dependencies isolated from core. During experiments, distinguish `gson`, `jackson2` and `jackson3`; preserve the old lookup through a documented deterministic default. Reconcile D8’s “default the new provider” statement with its unresolved default-provider question (`design.md:149`, `:259`).

2. **D8 — Repair the existing integration defects before comparing implementations.**

   - Provider absence is recorded at `design.md:27`; add discovery and request/response capability tests.
   - Non-Reader/non-JsonElement request inputs fall through to `null`: `xacml-json/.../JsonRequestContextUnmarshaller.java:55`.
   - OutputStream marshalling creates an unflushed writer: `xacml-json/.../JsonRequestContextMarshaller.java:65`.

   Define supported input forms, explicit UTF-8 conversion, flushing and caller-owned stream lifetime. Unsupported input must fail explicitly. Test both request and response paths; response unmarshalling also returns `null` for unsupported inputs (`JsonResponseContextUnmarshaller.java:65`).

3. **D8 — Make normative correctness the first bake-off gate.**

   Current adapters are insufficient as the sole oracle:

   - Datatype inference remains a TODO and defaults to string: `AttributeDeserializer.java:102`.
   - Request parsing requires `Category` and does not handle the shorthand category arrays: `RequestContextAdapter.java:57`.
   - Helpers accept lowercase property alternatives and string-form booleans: `GsonUtil.java:38`.

   Build independent profile fixtures, including reordered properties, inferred types, numeric precision, special values, references, Content and responses. Track compatibility changes separately from speed changes. Declare any newly rejected legacy forms **BREAKING** where applicable.

4. **D6 — Define exact units and enforcement points.**

   Retain the proposed defaults provisionally, but specify them precisely:

   | Limit | Recommended definition |
   | --- | --- |
   | Document | 64 MiB of encoded input bytes before decoding; separately define the character-input bound |
   | Depth | 256 simultaneously open objects/arrays |
   | String | 8 Mi UTF-16 code units after JSON unescaping |
   | Member name | 1,000 decoded UTF-16 code units |
   | Number | 1,000 lexical characters, including sign, decimal point and exponent |
   | Structural count | Add a configurable JSON token/model-node budget; calibrate its default against corpora |

   These are design recommendations, not established safe workload thresholds. Use native constraints plus exact project checks. Test limit−1, limit and limit+1 across byte arrays, streams and Readers. Apply guards while consuming ignored fields too.

5. **D6/D1 — Preserve positions without inventing unavailable information.**

   Normalize Jackson locations into project positions. For Gson, combine character offsets with a bounded source-line index and retained token spans. Do not calculate positions from a counting Reader’s latest read position: buffering can read ahead.

   Define byte offsets, character offsets and caret-column units separately. Include UTF-8 multibyte characters, supplementary Unicode, CRLF, escapes, chunk boundaries and deferred datatype conversion in diagnostic fixtures.

   Existing `JsonElement` input has already lost original spelling, duplicate occurrences and source positions. D1/D6 must state its supported guarantees or explicitly restrict positioned strict parsing to serialized input. Re-serializing a tree cannot restore original evidence.

6. **D6 — Make strict syntax, duplicates and model validation separate checks.**

   For Gson, configure strictness/depth and detect repeated decoded names before tree construction can discard occurrences. For Jackson, enable strict duplicate detection. Require exactly one complete top-level document and consume trailing input.

   Preserve missing versus null, number versus quoted string, and the original numeric lexeme where inference requires it. Test semantically equal escaped names as duplicates. Strict parser syntax alone does not enforce the XACML profile.

7. **D6 — Apply XML hardening to JSON Content.**

   Decoding Content must feed the same hardened XML path and decoded-content limits. A JSON document-size bound does not define the embedded XML depth/node limits. Current Category parsing forwards Content into the entity builder (`CategoryAdapter.java:62`); trace that conversion during implementation.

   Keep embedded XML errors related to the outer JSON Content position, with inner coordinates identified separately. OASIS §4.2.3 establishes the Content representations; the project’s D6 supplies the hardening policy.

8. **D8/D9 — Compare equivalent, fully checked pipelines.**

   Measure Gson tree baseline, Gson streaming, Jackson 2 streaming and Jackson 3 streaming. Include input conversion, required limits, duplicate detection, position tracking and complete model construction in comparable measurements.

   Use representative small/large requests, Content, multivalued attributes and multiple requests. Verify full model/result behavior in setup; add independent fixtures beyond inputs generated by the current marshaller. Report time ± error and B/op on the approved JDK 25/C2 baseline with controlled JMH pairs. Performance and allocation gains remain **UNVERIFIED** until then.

**Risks and open questions**

- Resolve profile inconsistencies and current permissive-adapter compatibility before declaring parity.
- Determine exact structural-count defaults and memory bounds for deferred values, duplicate-name tracking and diagnostic source retention.
- Define missing-position behavior for prebuilt trees and sources whose original bytes are unavailable.
- Verify native-image support if the selected provider is used by the approved `xacmlc` front end; no native-image experiment ran.
- Reconfirm DSL-JSON maintenance and simdjson-java’s JDK 25/runtime requirements before promoting either beyond an optional experiment.

Verification consisted of source/artifact reads, native Context7 queries, and fetched primary documentation/tagged sources. The GitHub connector returned authentication errors; public web sources supplied the fallback. Some Maven/Javadoc endpoints were inaccessible; unsupported conclusions are marked UNVERIFIED. No performance result or implementation acceptance is claimed.
