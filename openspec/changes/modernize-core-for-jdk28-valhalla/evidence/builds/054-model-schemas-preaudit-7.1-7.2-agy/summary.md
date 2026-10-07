# Run 054: donor pre-audit for tasks 7.1 and 7.2, model and schemas (Agy)

Date: 2026-10-02. Lane: `policy4j_code_review_agy` (NTM persona), Antigravity;
pane shows Gemini 3.8 Flash (High) (report self-label unreliable). Session
`policy4j--g1`, pane 3, about 75-80 tool calls, 22 donor and 7 worktree
files. Read-only. Raw report: [agy-report.txt](agy-report.txt) (A 8 model
defects, B 16 catalog defects, C one-catalog recommendation, D 28 tests).

Reported top defects: AttributeSchema verifies `domains.isEmpty()` the wrong
way round (cannot build a schema with domains); AttributesSchema is an
interface with a record header (the tasks.md `:101` construct); EntitySchema
`type()`/`recordSchema()` recursion; Entity equality; catalog URN errors.

## Conductor spot checks (2026-10-02)

- Confirmed: `b9f114a3:xacml-core/src/main/java/org/xacml4j/v30/schemas/Resource.java:71-78`
  spells the resource-parent and resource-ancestor ids `urn:oasis::names:...`
  (double colon), for the 1.0, 2.0 and 3.0 variants. Neither a8a90d46 nor the
  worktree contains this spelling.
- Confirmed a further compile break in `b9f114a3:.../model/EntitySchema.java`:
  `eidRef()` ends with `return recordSchema().` (incomplete expression).
- The AttributeSchema `Verify.verify` line was not located at the cited line
  in `git show` output (the shown `build()` at `:111-117` has no verify);
  unverified.

Use: 7.1 re-implements the model to the task's named fixes with the 28 test
rows; 7.2 builds one catalog checked by a parameterized test over every
Appendix B identifier, reusing the 5.1 identifier types (run 041), not a
second enum set.
