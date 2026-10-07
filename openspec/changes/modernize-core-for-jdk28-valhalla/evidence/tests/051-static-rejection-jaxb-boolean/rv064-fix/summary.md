# rv064: secure JAXB parsing

Result: the HIGH XXE regression and MEDIUM shared XMLReaderManager mutation are fixed. The final JDK 21 clean reactor passes. Every conformance case has the same outcome as run 051/07.

JDK: Oracle Corporation, java.version 21.0.6, runtime 21.0.6+8-LTS-jvmci-23.1-b55.

## Changes and parser ownership

Only BaseJAXBUnmarshaller.java changed in production. BaseJAXBUnmarshallerSecurityTest.java is new. changes.patch records the delta from the source present when this priority packet arrived; the original and a8a90d46 versions are retained separately.

The unmarshaller now creates a method-local SAXParserFactory and XMLReader for bytes, InputStream, InputSource, StreamSource and reader-less SAXSource. The factory is namespace-aware, enables FEATURE_SECURE_PROCESSING, disables external general entities, external parameter entities, external DTD loading and XInclude. The reader denies external DTD/schema access. A declaration/lexical handler rejects external entity declarations and external DTDs before any resolver or IO runs, while allowing internal subsets. The chain is reader -> existing boolean filter -> JAXB UnmarshallerHandler.

The declaration handler prevents disabled external entities from being silently skipped. Rejecting unused external declarations is stricter than the historical parser's possible acceptance of an unused declaration; this is deliberate, and no internal-subset restriction was added.

The parser settings follow the [JDK 21 JAXP security guide](https://docs.oracle.com/en/java/javase/21/security/java-api-xml-processing-jaxp-security-guide.html). [SAX DeclHandler](https://docs.oracle.com/en/java/javase/21/docs/api/java.xml/org/xml/sax/ext/DeclHandler.html) provides the external declaration callback. Actual behavior is demonstrated by the red/green runs on the stated JDK.

Caller-owned SAX readers retain their settings and EntityResolver. DOM and StAX inputs retain the caller's parser. Their boolean attributes are checked before JAXB's permissive conversion, using a DOM walk or StAX delegates. JAXB/StAX linked exceptions retain the original SyntaxException and attribute message. Programmatically-created DOM attributes use their node name when localName is absent.

No TransformerFactory or XMLReaderManager is used by this class. Its full source, the scoped ast-grep check (no transformer call), and the positive SAXParserFactory check support that conclusion. The factory and reader are method-local; no global property or shared parser is changed.

## Tests first and receipts

| Run | Tests run | Failures | Errors | Result |
| --- | ---: | ---: | ---: | --- |
| 01 | 0 | — | — | Test compile setup error: StringVal exposes get(), corrected before any production edit |
| 02 security | 88 | 60 | 0 | Red: 40 missing-file IO attempts and 20 accepted real-file external entities |
| 02 existing boolean attributes | 85 | 0 | 0 | Existing 4.47 controls still pass |
| 03 security | 88 | 0 | 0 | External access fixed; internal entities and expansion limits pass |
| 03 existing boolean attributes | 85 | 0 | 2 | StAX-linked messages lost; repaired without weakening assertions |
| 04 security/compatibility | 97 | 2 | 0 | StAX event-reader message controls red |
| 04 existing boolean attributes | 85 | 0 | 2 | Same message regression remains red |
| 05 combined | 182 | 0 | 0 | All focused security and compatibility controls green |
| 08 programmatic DOM | 5 | 5 | 0 | Red: null local names surface NullPointerException instead of syntax-error |
| 09 new security class | 102 | 0 | 0 | Green in the clean reactor, including all five DOM controls |
| 09 existing boolean attributes | 85 | 0 | 0 | All original assertions retained |
| 09 static-rejection harness | 16 | 0 | 0 | All 3.9 positive and negative controls retained |

The 102 new checks cover both XACML request versions and both policy versions. They include external general and parameter entities, existing-file sentinels, all five parser-owned input forms, internal entities, a billion-laughs expansion limit, caller-owned SAX resolvers, StAX event readers, and DOM attributes created with setAttribute. Every rejection asserts the syntax-error status code.

The a8a90d46 source calls Unmarshaller.unmarshal(InputStream) directly. Four tests execute that same JAXB entry point, map its JAXBElement through the normal mapper, and assert the internal entity is still x. This is a characterization of the historical parsing path on the current JDK/JAXB stack, not a build of the entire historical reactor.

## Final clean reactor

Final command: JAVA_HOME="$(jenv prefix 21)" rtk mvn -Dlicense.skipUpdateLicense=true -l openspec/changes/modernize-core-for-jdk28-valhalla/evidence/tests/051-static-rejection-jaxb-boolean/rv064-fix/09-full-reactor-clean-verify.log clean verify

| Module | Tests run | Failures | Errors | Skipped |
| --- | ---: | ---: | ---: | ---: |
| xacml-core | 4985 | 0 | 0 | 17 |
| xacml-xml-jaxb | 0 | 0 | 0 | 0 |
| xacml-xml | 244 | 0 | 0 | 0 |
| xacml-json | 24 | 0 | 0 | 1 |
| xacml-pdp | 98 | 0 | 0 | 5 |
| xacml-test | 99 | 0 | 0 | 0 |
| xacml-conformance-tests | 917 | 0 | 0 | 17 |
| **Total** | **6367** | **0** | **0** | **40** |

Run 06 also passed, but ten concurrently merged core files changed during its source audit. Run 07 repeated clean verify on that current tree and passed with 6362 tests. The five additional DOM controls required the final run 09; its 548 source/POM/manifest hashes remained unchanged throughout the gate. Existing headers match exactly, and the new test copies the module header. The paused 4.5 characterization test was archived outside src/test until that task resumes.

## Conformance and scope

| Suite | Manifest cases | Passed | Skipped | Failed |
| --- | ---: | ---: | ---: | ---: |
| OASIS 2.0 | 374 | 364 | 10 | 0 |
| OASIS 3.0 | 487 | 480 | 7 | 0 |
| RSA 2008 | 17 | 17 | 0 | 0 |
| Examples | 2 | 2 | 0 | 0 |
| **Total** | **880** | **863** | **17** | **0** |

09-case-results.csv maps the actual Surefire case invocations to every manifest row. Comparison with run 051/07 produces zero changed cases. The manifest hash remains be4b25dabe9185ff213255461ba05e36a7fa93b2251b10923bba5ea1796cab08. ConformanceStaticRejectionTest, ConformanceManifestTest and VendoredFixtureIntegrityTest pass. No fixtures, expected results, schema bindings, poms or unrelated source were edited by this fix.

command.txt records the build and verification commands. surefire-summary.txt preserves Maven Results and per-class/module counts. Numbered logs replace the checkout path with <repo> and redact any home prefix. scope-audit.json and 09-gate-results.json contain machine-readable receipts.

Residual boundary: callers remain responsible for parsers they supply through SAXSource, DOM and StAX, as requested. Parsed DOM/JAXB objects cannot reveal an earlier external read. All requested parser-owned inputs are covered. No commit, br command, task tick or issue closure was performed.
