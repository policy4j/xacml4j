# rv060: bug 4.26 review, duplicate type and function ids fail fast, lane g426 (Agy)

Date: 2026-10-02. Lane: `policy4j_code_review_agy` (NTM persona), Antigravity;
pane shows Gemini 3.8 Flash (High) (self-label unreliable). Session
`policy4j--g1`, pane 3, 25 tool calls, 11 files. Read-only: lane g426 patch
(author Luna/Codex) and evidence run 053.

Verdict: APPROVE (no findings).

- Types (canonical, short, alias) and functions (primary, legacy, short)
  fail fast on a collision, naming the id and both claimants; the same
  provider instance or spec object is idempotent; equal-but-distinct spec
  objects fail (design D5).
- Production paths start: PDP builder, ServiceLoader discovery (including
  the combining provider from 4.27), XacmlPolicyUnmarshaller, XacmlTypes.
- ModelFixtures and XPathFunctionsTest re-registered default functions on
  top of `builder()` defaults; removing that is correct and hides no
  production duplicate.
- TypeRegistryTest duplicate tests enabled; FunctionProviderDuplicateIdTest
  (6 tests) covers primary/legacy collisions, idempotency and combined
  providers.

## Merge (2026-10-02 03:34)

Lane g426 applied; TypeRegistryTest had changed through the g44f merge, so
the conductor merged its hunk by hand (enabled the two duplicate tests with
the lane bodies, added the idempotency test and the imports). Check lane on
the merged tree: xacml-core 4,768 tests, 0 failures, 6 skipped; xacml-pdp 98
tests, 0 failures, 7 skipped.
