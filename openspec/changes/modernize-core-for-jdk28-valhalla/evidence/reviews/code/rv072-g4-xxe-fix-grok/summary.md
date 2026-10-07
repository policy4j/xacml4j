# rv072: rv064 XXE fix adversarial verification (Grok)

Date: 2026-10-02. Lane: adversarial code reviewer, Grok Build grok-4.7
(xhigh), role `.ntm/roles/policy4j_code_review.md`, session cleared first.
Session `policy4j--g1`, pane 0, about 28 minutes. Read-only: main worktree
sources (SHA-256 matches the gated version in
`09-pre-gate-source-hashes.json`), JDK 21.0.6 `lib/src.zip`, JAXB 2.3.8
sources, run 051 rv064-fix evidence.

Verdict: APPROVE (no open findings).

1. HIGH XXE fixed: no TransformerFactory or XMLReaderManager in
   `BaseJAXBUnmarshaller` (rg and ast-grep: no hits). Bytes, InputStream,
   InputSource, StreamSource and reader-less SAXSource share one path
   (`:117-147`): method-local `secureReader()` -> BooleanAttributeValidationFilter
   -> JAXB UnmarshallerHandler; the filter's setupParse (which would replace
   the entity resolver) never runs because the reader is parsed directly.
2. MEDIUM shared XMLReaderManager fixed: per-call SAXParserFactory and
   XMLReader; JDK SAXParserImpl creates its own XMLSecurityManager and
   property manager; ACCESS_EXTERNAL_DTD/SCHEMA "" set at API level.
3. LOW per-call TransformerFactory lookup removed.
JDK 21 call-order evidence: XMLDTDScannerImpl.scanEntityDecl reports
external general and parameter entity declarations via externalEntityDecl
before any open; URL.openConnection is reached only through
XMLEntityManager.startEntity after resolve, which returns early when
external entities are disabled; for `<!DOCTYPE root SYSTEM ...>`
LexicalHandler.startDTD runs before the external subset is read, and the
handler throws on a non-null id. Internal subsets allowed; unparsed
entities skipped without IO; secure processing installs the 64000 entity
expansion limit. Caller-owned SAX readers, DOM and StAX keep the caller's
parser; boolean checks run first; null localName handled like JAXB's
DOMScanner.
Tests: red 60 failures (IO attempted, external values accepted) and 5 NPEs
on programmatic DOM; green 102/0; reactor 6,367 tests, 0 failures.
Residual: caller-owned SAX/DOM/StAX follow the caller's entity policy; no
test with only an external DOCTYPE subset (covered by the callback order);
no direct XMLStreamReader test; the jdk.xml.entityExpansionLimit system
property can raise the limit.
