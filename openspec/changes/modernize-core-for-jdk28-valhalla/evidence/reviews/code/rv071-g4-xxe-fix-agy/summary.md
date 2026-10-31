# rv071: rv064 XXE fix review (Agy)

Date: 2026-10-02. Lane: `policy4j_code_review_agy` (NTM persona), Antigravity;
pane shows Gemini 3.8 Flash (High) (self-label unreliable). Session
`policy4j--g1`, pane 3, 17 tool calls. Read-only: main worktree
BaseJAXBUnmarshaller and BaseJAXBUnmarshallerSecurityTest, evidence
tests/051-static-rejection-jaxb-boolean/rv064-fix.

Verdict: APPROVE.

- rv064-1 HIGH (XXE) FIXED (`BaseJAXBUnmarshaller.java:138-213`): a
  method-local SAXParserFactory/XMLReader (secure processing, external
  general/parameter entities off, external DTD off, XInclude off, external
  DTD/schema access denied) for bytes, InputStream, InputSource,
  StreamSource and reader-less SAXSource; a declaration handler rejects
  external entity declarations and external DTDs before any IO; internal
  subsets allowed; chain reader -> boolean filter -> JAXB handler.
- rv064-2 MEDIUM (shared XMLReaderManager) FIXED: no TransformerFactory or
  shared reader manager remains.
- rv064-3 LOW FIXED (no per-call TransformerFactory lookup).
- Caller-owned SAX readers keep their resolver; DOM and StAX inputs keep the
  caller's parser and are boolean-validated (DOM walk handles null
  localName, `:222`).
- Tests: BaseJAXBUnmarshallerSecurityTest, 102 tests (external general and
  parameter entities across 4 kinds x 5 input forms with no IO; real files
  never read; internal entities; expansion limit; caller-owned readers;
  StAX and DOM booleans); 60 failures before the fix. Full reactor clean
  verify 6,367 tests, 0 failures, 40 skipped; conformance 863 executed, 17
  skipped, unchanged.
