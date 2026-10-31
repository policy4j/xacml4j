# rv055: rv038/rv039/rv042/rv044 fix verification, AnyURI lexical form and harness comparators (Agy)

Date: 2026-10-02. Lane: `policy4j_code_review_agy` (NTM persona), Antigravity;
pane shows Gemini 3.8 Flash (High) (report self-label unreliable). Session
`policy4j--g1`, pane 3. Read-only: main worktree sources and run 024/030
follow-up evidence.

Verdict: APPROVE. No new issues.

- rv038 FIXED (`AnyURI.java:47-55, 86-93`): original lexical string kept;
  equals/hash on it; dot-segment and scheme/host-case pairs unequal.
- rv039 FIXED (`PermitOverrides.java:49-52, 81-98`): unused flag removed.
- rv044-1 FIXED (`Xacml30TestUtility.java:145-154`;
  `Xacml30TestUtilityTest.java:446-456`): comparator uses AnyURI lexical
  equality; both spelling pairs rejected in both directions.
- rv044-3 / rv042 FIXED (`XacmlPolicyTestSupport.java:201-207, 214-219,
  256-270`): matches consumed; FulfillOn compared, defaulting to the
  enclosing decision when a response obligation omits it.
- rv044-4 FIXED (`Xacml30TestUtility.java:148-152`): `a == b || both NaN`
  (not Double.compare), tests for signed zero, NaN, INF and different values.
- rv044-2/5: covered by the merged engine fixes 4.28 and 4.31; the comparator
  delegates to the type factory and `Objects.equals`.
