# rv070: bug 4.52 review, XPathProvider secure processing, lane g452 (Agy)

Date: 2026-10-02. Lane: `policy4j_code_review_agy` (NTM persona), Antigravity;
pane shows Gemini 3.8 Flash (High) (self-label unreliable). Session
`policy4j--g1`, pane 3, 7 tool calls. Read-only: lane patch (98 lines:
XPathProvider, XmlContentTest, MatchTest) and evidence 064.

Verdict: APPROVE.

- `XPathProvider` is the only production XPathFactory site (core, pdp, xml,
  json); it sets FEATURE_SECURE_PROCESSING; no function or variable
  resolvers are installed.
- If the provider cannot enable secure processing, the first evaluation on
  that thread throws IllegalStateException (fail closed; never happens on
  the JDK provider).
- Thread safety: the factory is held in a ThreadLocal; each call creates its
  own XPath; nothing compiled is shared.
- The new tests prove that a failed extension call maps to a processing
  error and to an Indeterminate match, but they also pass without the flag
  (the JDK provider rejects extension calls by default). Recommended
  enhancement: assert the factory's FEATURE_SECURE_PROCESSING directly.
- The lane's xacml-xml failures come from another lane's in-progress work
  and are outside this patch.

Merged into the main worktree on 2026-10-02 (core only). Residual: the
direct feature assertion is not yet a test.
