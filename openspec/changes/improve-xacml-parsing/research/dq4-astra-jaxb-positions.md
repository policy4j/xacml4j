FINAL REPORT (DQ4 research, Codex GPT-6 Astra)

**Q3 answer:** keep JAXB for the first diagnostics implementation, but do not treat `-Xlocator` as a complete solution for precise source positions. It is the smallest implementation candidate for locating bound elements. Compare it with a parse-local SAX side table before choosing it. Treat parser-provider selection and replacement of JAXB as separate decisions: Woodstox can supply positions while JAXB and the existing mappers remain in place.

All external sources below were retrieved **2026-10-27**. Performance figures are **UNVERIFIED** unless explicitly described as arithmetic assumptions. No implementation, build, test, benchmark, or profiler ran.

**What `-Xlocator` generates and requires**

The plugin’s published POM selects **XJC 2.3.7**; the project separately manages **JAXB RI runtime 2.3.8**. These are different dependency contexts. The exact 2.3.7 locator plugin generates, on classes without a generated superclass:

- A protected `org.xml.sax.Locator locator` field.
- `@XmlLocation` and `@XmlTransient` annotations.
- Implementation of RI-specific `com.sun.xml.bind.Locatable`.
- Public `sourceLocation()` and `setSourceLocation(Locator)` methods.

Generated subclasses inherit that support. This is not instrumentation of every XML token: simple values, individual attributes, and embedded DOM nodes do not each become generated locator-bearing beans. Sources: [plugin 0.15.3 POM](https://repo.maven.apache.org/maven2/org/jvnet/jaxb2/maven2/maven-jaxb2-plugin-project/0.15.3/maven-jaxb2-plugin-project-0.15.3.pom), [XJC 2.3.7 locator generator](https://raw.githubusercontent.com/eclipse-ee4j/jaxb-ri/2.3.7-RI/jaxb-ri/xjc/src/main/java/com/sun/tools/xjc/addon/locator/SourceLocationAddOn.java).

The proposed Maven configuration is supported through `<args><arg>-Xlocator</arg></args>`. Plugin 0.15.3 exposes `args` and defaults `extension` to true. No separate third-party locator plugin is needed. Source: [plugin configuration implementation](https://raw.githubusercontent.com/highsource/jaxb-tools/0.15.3/plugin-core/src/main/java/org/jvnet/jaxb2/maven2/AbstractXJC2Mojo.java).

**Repository consequence:** adding the XJC argument alone is insufficient. The generated imports introduce a compile-time RI dependency. `xacml-xml-jaxb/pom.xml:17` declares `jaxb-impl` without a scope override, while root dependency management gives it runtime scope. Inspection of the cached 2.3.8 implementation jar confirmed that it contains `Locatable` and `XmlLocation`. The prototype must make those classes available during compilation and document the resulting RI coupling. This is a source-derived build requirement; compilation remains unverified.

**Per-object cost**

RI 2.3.8 creates `new LocatorImpl(context.getLocator())` when instantiating a bean with an annotated locator field. It copies the mutable parser position; it does not retain the live parser locator. Source: [RI bean instantiation](https://raw.githubusercontent.com/eclipse-ee4j/jaxb-ri/2.3.8-RI/jaxb-ri/runtime/impl/src/main/java/com/sun/xml/bind/v2/runtime/ClassBeanInfoImpl.java).

For **N newly unmarshalled locator-bearing beans**, the structural cost is:

`N locator snapshots + added storage for one reference per bean + snapshot initialization`

A useful allocation model is:

`Δbytes ≈ N × sizeof(LocatorImpl) + Σ generated-bean layout growth`

For illustration only, **if** the measured snapshot layout is 32 bytes and adding the reference grows each bean by 0–8 bytes, that is approximately **32–40 bytes per bean**, or **320–400 KB for 10,000 beans**. These are conditional calculations, **not measured sizes or benchmark results**. JVM headers, reference compression, alignment, and existing field layouts must be recorded before using them as evidence.

Manually constructed beans gain the field but do not automatically allocate a snapshot. The inspected RI instantiation path contains no per-parse switch to disable the copy once the locator annotation is present.

**Position precision is the main design constraint**

SAX defines locator coordinates around the **end of the current event**, with approximate diagnostic positioning. A snapshot taken while processing a start tag therefore does not guarantee the opening `<`, the attribute name, or its value. Multiline start tags make that distinction visible. Locator data can also be unavailable. Source: [Java 25 SAX Locator contract](https://docs.oracle.com/en/java/javase/25/docs/api/java.xml/org/xml/sax/Locator.html).

Consequently, ordinary SAX plus `-Xlocator` does not by itself establish the exact line/column promised by the unknown-function example in `specs/parse-diagnostics/spec.md:13`, or exact attribute/value carets.

Standard StAX does not eliminate this issue automatically: its `Location` contract describes event-end coordinates and makes location information optional. Woodstox 7.3.0 explicitly distinguishes event-start and current locations; its source documents the difference from the JDK implementation. Sources: [Java 25 StAX Location](https://docs.oracle.com/en/java/javase/25/docs/api/java.xml/javax/xml/stream/Location.html), [Woodstox 7.3.0 location implementation](https://raw.githubusercontent.com/FasterXML/woodstox/woodstox-core-7.3.0/src/main/java/com/ctc/wstx/sr/StreamScanner.java).

A position model should distinguish **element start**, **event position**, **value position**, and **unavailable**. Do not invent column 1 or derive an attribute position by subtracting its name’s length.

**Alternatives and implementation cost here**

| Candidate | Work required here | Allocation and position implications |
| --- | --- | --- |
| JAXB plus `-Xlocator` | Smallest plumbing change: XJC arguments, compile dependency, position lookup, mapper diagnostic context. | One snapshot per bound bean; generated-class growth and RI coupling. Ordinary SAX positions remain approximate/event based. |
| JAXB plus SAX side table | Add a SAX filter, JAXB listener, parse-local identity lookup, cleanup, and mapper access to that lookup. | A naive `IdentityHashMap<Object, LocatorImpl>` still allocates snapshots and adds map storage. Savings require selective capture or packed coordinates; unmeasured. |
| JAXB over StAX/Woodstox | Adapt the owned-input path and provider configuration while retaining the mappers. The existing StAX branch is already present at `BaseJAXBUnmarshaller.java:109`. | Separates provider/location changes from removing the JAXB graph. Explicit Woodstox start positions can improve element diagnostics; attribute spans still need additional handling. |
| Direct StAX to model | Highest cost: implement XML dispatch and mapping for both versions, value conversion, references, recovery, content handling, and existing input compatibility. | Can remove JAXB intermediates, but still retains the returned model, deferred references, and required XML content. No measured speed or allocation advantage yet. |

For the side table, snapshot the SAX position during the callback, and associate it with the JAXB target through `beforeUnmarshal`. RI’s `StructureLoader` invokes that callback while processing the element. A SAX locator must not be read after parsing and assumed to identify an earlier bean. Listener callbacks cover JAXB elements and complex types, not every simple-type value. Sources: [RI StructureLoader](https://raw.githubusercontent.com/eclipse-ee4j/jaxb-ri/2.3.8-RI/jaxb-ri/runtime/impl/src/main/java/com/sun/xml/bind/v2/runtime/unmarshaller/StructureLoader.java), [JAXB Listener contract](https://docs.oracle.com/javase/8/docs/api/javax/xml/bind/Unmarshaller.Listener.html), [SAX Locator contract](https://docs.oracle.com/en/java/javase/25/docs/api/java.xml/org/xml/sax/Locator.html).

The direct-parser estimate is based on these concrete responsibilities:

- Both policy mappers collect variable expressions before resolving them. `VariableManager.java:43` copies them into maps; resolution uses a stack. A streaming replacement needs deferred resolution, not merely immediate builder calls.
- XACML 2.0 maps separate subject/resource/action/environment targets and converts selectors and values into the 3.0 model (`Xacml20PolicyFromJaxbToObjectModelMapper.java:248`, `:369`, `:680`).
- The 2.0 request mapper normalizes subjects, handles multiple resources, and converts values through temporary 3.0 JAXB objects (`Xacml20RequestContextUnmarshaller.java:111`, `:139`, `:216`, `:282`).
- The 3.0 request mapper handles embedded content and references resolved by JAXB to either objects or strings (`Xacml30RequestContextFromJaxbToObjectModelMapper.java:486`, `:506`).
- Embedded content remains relevant to the existing DOM-facing model; bypassing JAXB does not imply eliminating all DOM allocation.

**Confidence:** high for generated members, RI snapshot creation, plugin versions, and documented coordinate limitations. Medium for implementation-cost ranking, based on the opened source. Low for comparative throughput and allocation: those require the prohibited prototype and JMH work.

**Disagreement or drift risk:** Context7 returned current Jakarta API documentation and Woodstox 7.2 documentation. Version-sensitive conclusions above instead use the exact RI 2.3.7/2.3.8 and Woodstox 7.3.0 sources. Tavily extraction returned HTTP 429; subsequent retrieval used DuckDuckGo. The failed search for Woodstox 7.3.0 was not treated as evidence of absence; its tagged source was retrieved successfully.

**Recommendations, most important first**

1. **D3 — define position precision before selecting the implementation.**

   Replace the current two-way choice with independent choices for **binding architecture** and **position capture**. Prototype `-Xlocator`, a parse-local side table, and JAXB over a positioned StAX reader before considering a full parser replacement.

   Add acceptance cases for multiline start tags, repeated siblings, attributes on separate lines, CRLF, non-ASCII text, and missing locations. Caller-created DOM and `JAXBElement` inputs already enter through `BaseJAXBUnmarshaller.java:95`; their original lexical positions cannot be assumed available.

   If exact attribute/value carets remain mandatory, explicitly budget a lexical source-position index or equivalent token-span support. Neither a bean locator nor an ordinary SAX side table supplies that by itself.

2. **D4 — distinguish absent, invalid, and valid children.**

   Do not use an undifferentiated “empty result” for recovery. Existing mapper methods use `null` for genuinely absent conditions and targets (`Xacml30PolicyFromJaxbToObjectModelMapper.java:311`, `:483`). An invalid condition must invalidate its enclosing rule, not become an absent condition.

   Keep a parse-local diagnostic context; recover at explicit sibling boundaries; prevent publication of any model after an error. Map known input failures deliberately rather than catching every runtime exception.

   Variable resolution needs rollback on failure. `VariableManager.java:61` pushes state, while successful resolution pops it at line 73. Continuing after a failed resolution without restoring that state can contaminate later diagnostics. Track failed definitions explicitly and suppress repeated dependent errors.

   Finally, assign source ordinals during reading. Current variable resolution iterates a `HashMap` key set (`VariableManager.java:57`), so discovery order is not document order. Diagnostic ordering and the “first 100” cap must remain deterministic.

3. **D9 — establish comparable baselines, then change one source-supported cost at a time.**

   Measure the same valid inputs with position capture disabled, `-Xlocator`, and the side-table candidate. Keep security settings, XSD mode, diagnostic precision, parser version, and input representation equivalent. Record bytes/op and time with error; the “No cost on valid input” requirement actually requires measured overhead, not zero overhead.

   Initial optimization candidates, subject to profiles:

   - Replace sequential mapper `LinkedList` intermediates and expression streams with appropriately sized lists and loops. Examples: `Xacml30PolicyFromJaxbToObjectModelMapper.java:167`, `:234`, `:636`.
   - Remove temporary 2.0-to-3.0 JAXB value adapters by extracting reusable value conversion, preserving datatype and XPath behavior (`Xacml20RequestContextUnmarshaller.java:282`).
   - Measure factory/parser reuse separately from JAXB unmarshaller reuse. The current per-call allocations are explicit at `BaseJAXBUnmarshaller.java:86` and `:162`. Reuse must reset listeners, handlers, schema, limits, source references, and failure state.
   - Only then prototype a direct **3.0 request** parser before the larger policy parsers. Retain existing paths for differential comparison and compatibility.

   Each candidate needs its own before/after pair under the approved JDK 25/C2 configuration. No expected percentage improvement is established by this research.

4. **D9 — make Woodstox configuration explicit.**

   Woodstox 7.3.0’s `configureForSpeed()` disables location preservation and changes several other settings. Do not apply that profile wholesale to a diagnostics comparison. Set and verify the required location and security options explicitly. Source: [Woodstox 7.3.0 ReaderConfig](https://raw.githubusercontent.com/FasterXML/woodstox/woodstox-core-7.3.0/src/main/java/com/ctc/wstx/api/ReaderConfig.java).

   Use the JDK provider as a comparison candidate, not as an assumed equivalent source-position implementation. Isolate provider-specific position extraction behind a small interface.

**Risks and open questions**

- Is the required caret an exact lexical start, or is an explicitly labelled event position acceptable? D3 cannot be finalized without that distinction.
- How should unavailable locations be represented for caller-created DOM/JAXB objects? The current requirement universally promises positive coordinates.
- Which failures must remain recoverable after JAXB reports a validation event? The current handler merely logs and returns true (`BaseJAXBUnmarshaller.java:87`).
- How much allocation comes from JAXB, mapper intermediates, embedded content, and retained source excerpts respectively? No parse profile examined here answers that.
- Add original XACML 2.0 request fixtures to parser comparisons: the benchmark specification currently describes requests serialized as 3.0 XML and JSON (`specs/parse-benchmarks/spec.md:6`).
- The future native compiler adds a separate compatibility gate for RI-generated interfaces, reflection, and parser providers; native-image compatibility was not tested.

**Verification and residual risk:** read the proposal and approval, D1–D10, all three specs, research questions, requested source paths, both policy mappers, request mapping paths, cached dependency metadata, and external primary sources. No files changed. Generated output, actual object layouts, locator coordinates on this JVM, recovery behavior, and performance remain **UNVERIFIED** until the authorized prototype and benchmark lanes run.
