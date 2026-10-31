# rv068: bug 4.26 adversarial review, duplicate ids fail fast (Grok)

Date: 2026-10-02. Lane: adversarial code reviewer, Grok Build grok-4.7
(xhigh), role `.ntm/roles/policy4j_code_review.md`, session cleared first.
Session `policy4j--g1`, pane 0. Read-only on the merged main worktree; ast-grep
`java-no-console-output` on the three production files (exit 0); ripwire
impact/quality-delta (PARTIAL; whole dirty tree).

Verdict: APPROVE (no blocking findings).

- Collisions on canonical id, legacy alias or short id throw
  IllegalArgumentException naming the id and both claimants
  (`TypeFactory.java:50-76`, `BaseFunctionProvider.java:48-135`),
  independent of order; legacy-alias vs primary collisions use the same map;
  a second class loader's XacmlDefaultFunctions fails on the duplicates.
- Idempotency is by reference (same provider instance or spec object);
  equal-but-distinct specs throw, which is the right rule because
  FunctionSpec equality compares only the id. The discovery path drops a
  second XacmlDefaultFunctions when defaults are already installed; an
  explicit second instance still throws.
- Production paths build one default provider (PolicyRepository builder,
  XACML 2.0/3.0 policy unmarshallers, MarshallingProvider); the 4.27
  combining provider unchanged; JSON resolves types through XacmlTypes. No
  concurrent registration API.
- ModelFixtures and XPathFunctionsTest re-registered default classes;
  removing that hides no production duplicate.

Residual (non-blocking): the type alias index is now case-insensitive (canonical and
short ids already were); `duplicateFunctionIdentifierFailsRegistration`
asserts one id substring that depends on HashSet order; no direct test of a
type alias colliding with another type's canonical id; `accumulate`
(`BaseFunctionProvider.java:136-140`) is now unused.
