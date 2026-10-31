# rv064: bug 3.9 static-rejection harness and bug 4.47 strict schema booleans, adversarial security review (Grok)

Date: 2026-10-02. Lane: adversarial code reviewer, Grok Build grok-4.7
(xhigh), role `.ntm/roles/policy4j_code_review.md`, session cleared first.
Session `policy4j--g1`, pane 0. Read-only: main worktree, run 051 patch,
JDK 21.0.6 `lib/src.zip` (JAXP), `jaxb-impl-2.3.8-sources.jar`. Maven not run.

Verdict: REQUEST_CHANGES (1 HIGH, 1 MEDIUM, 1 LOW).

1. HIGH (security, XXE regression), `BaseJAXBUnmarshaller.java:101-102`:
   every non-JAXBElement input (InputStream, byte[], InputSource, reader-less
   SAXSource) is now parsed through `TransformerFactory.newInstance()
   .newTransformer()` without FEATURE_SECURE_PROCESSING or
   ACCESS_EXTERNAL_DTD/STYLESHEET. In JDK 21 the transformer leaves external
   access "all" and its XMLReader resolves external general and parameter
   entities (file:, http:) before the boolean filter runs. The previous path
   (`Unmarshaller.unmarshal`) used JAXB's XmlFactory with secure processing,
   which set ACCESS_EXTERNAL_DTD/SCHEMA to "". A request with
   `<!ENTITY xxe SYSTEM "file:///etc/passwd">` referenced in an
   AttributeValue would now read the file; a parameter entity would fetch a
   remote DTD. DOMSource/Node/XMLStreamReader/StAXSource inputs are not
   reparsed (caller's parser decides).
   Fix: parse with a method-owned XMLReader from a SAXParserFactory with
   FEATURE_SECURE_PROCESSING=true and external-general-entities and
   external-parameter-entities false (do not set disallow-doctype-decl; an
   internal subset stays legal); chain reader -> boolean filter -> JAXB
   UnmarshallerHandler; no TransformerFactory. Tests: bytes declaring an
   external general entity and an external parameter entity aimed at a
   missing file: URL -> SyntaxException, and no value taken from the URL.
2. MEDIUM: the transformer writes ACCESS_EXTERNAL_DTD=all onto the JVM-wide
   XMLReaderManager singleton (unsynchronized setters); concurrent
   unmarshals or other transforms can observe it. Owning the XMLReader (fix
   1) removes this.
3. LOW: TransformerFactory lookup per call (as rv059).

Holds: 3.9 harness accepts only the named load-time causes (exact
SyntaxException/missing AttributeId message for IIA004; the Condition
IllegalArgumentException for IIC012) or the vendored response; 16 control
tests; IIA023, IID029, IIIG300 still skipped. 4.47 filter covers exactly the
schema's xs:boolean attributes (3.0 ReturnPolicyIdList, CombinedDecision,
IncludeInResult, MustBePresent; 2.0 MustBePresent) through BooleanVal.
Entity-expansion limits (XMLSecurityManager) and XInclude unchanged.
Residual: whitespace-only boolean text not in the lexical test.
Resolution: priority fix to the author before any further work.
