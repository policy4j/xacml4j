# Run 040: group 5-7 slice map refreshed against the group 1-4 fixes (Agy)

Date: 2026-10-02. Lane: `policy4j_code_review_agy` (NTM persona), Antigravity,
Gemini 3.8 Flash (High). Session `policy4j--g1`, pane 3. Read-only: working
tree compared with `a8a90d46`, donor `b9f114a3` read with `git show`.
Raw report: [agy-report.txt](agy-report.txt) (A overlap table, B 12
regression risks, C order).

## Result

- 38 main source files and 60 test classes overlap between the donor slices
  and the worktree changes since `a8a90d46`.
- Regression risks (B): re-applying donor hunks verbatim would undo the fixes
  for 4.15/4.14 (string argument order, five 3.0 functions), 4.21
  (permit-overrides), 4.16 (INF/NaN lexical forms), 4.11 (unzoned time),
  4.10 (fractional seconds; the donor also checks getYears() where months and
  days are meant), 4.9 (context-selector cardinality), 4.22 (policy id list),
  4.17 (missing AttributeId), 4.8 (variadic higher-order functions), the
  rv038 AnyURI repair (donor `UriVal` normalizes), and 4.25 (boolean
  lexing). The donor also has syntax errors in StringFunctions and
  HigherOrderFunctions (the design records that the donor does not compile).
- Recommended slice order (C): 5.1, 5.2, 5.3, 5.4, 6.1, 6.3, 6.4, 6.6, 6.2,
  6.5 (core model, PDP runtime, marshalling as three lanes), 7.1, 7.2, 7.3.

## Conductor notes

- Correction to B10: the worktree `AnyURI` still normalizes
  (`AnyURI.java:81`); the lexical-form repair (rv038) is queued. The cited
  worktree path `types/UriVal.java` exists only in the donor. The risk stands:
  task 5.2 must carry the rv038 repair into `UriVal`.
- B7 describes BooleanVal as already strict; that is the in-flight 4.25 work,
  not yet merged at the time of the report.
- Placement: the report folds open bugs into the slices. The change keeps the
  task order instead: group 4 fixes bugs with tests first, and every group 5-7
  slice re-applies donor hunks only while those tests stay green. The group 4
  tests are the guard against each risk in B; slice packets list the risks
  for their files.
- The slice order in C is adopted for planning groups 5-7, subject to the
  dependency notes in tasks.md.
