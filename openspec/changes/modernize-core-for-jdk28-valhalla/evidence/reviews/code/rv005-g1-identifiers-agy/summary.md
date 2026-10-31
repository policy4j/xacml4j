# rv005: group 1 round 3 review, identifier aliases (Agy)

Date: 2026-10-01. Lane: `policy4j_code_review_agy` (NTM persona), Antigravity,
Gemini 3.8 Flash (High). Session `policy4j--g1`, pane 3. Read-only.

Scope: task 1.3 identifier part: BagFunctions (eight 3.0 duration bag ids),
SetFunctions (ten 3.0 duration set ids), HigherOrderFunctions (3.0 any-of,
all-of, any-of-any, map), 1.0 names kept as aliases; tests
DurationFunctionIdentifierTest and HigherOrderFunctionIdentifierTest.

Verdict: APPROVE, with findings.

Findings:
1. MEDIUM, `HigherOrderFunctions.java:54,73,92,172` (validator ~232-234).
   The 3.0 ids resolve to the 1.0 fixed-arity implementations. XACML 3.0
   A.3.12 makes any-of, all-of, any-of-any and map variadic. Valid 3.0 calls
   fail validation with a syntax error: any-of(f, "prefix", "suffix",
   string-bag("item1")); any-of(string-equal, string-bag("a","b"), "b");
   map(integer-add, integer-bag(1,2), integer-bag(10,20)) (expected {11, 22}).
   No silent misevaluation: the 1.0 argument shape evaluates as before, and
   before this change the 3.0 ids did not resolve at all.
   The reviewer said this is "scheduled for Phase 6"; the conductor checked
   tasks.md and design.md and found no such task. Resolution: tracked as
   beads bug `policy4j-the.4.8` under the group 4 epic; scope needs a
   maintainer decision.
2. LOW, `DurationFunctionIdentifierTest.java:82-89`,
   `HigherOrderFunctionIdentifierTest.java:83-91`. The tests prove lookup,
   canonical id, and alias equality, not evaluation. Resolution: an
   evaluation test per new id is requested from the implementer lane.

Checked (reviewer): URNs match XACML 3.0 A.3.10, A.3.11, A.3.12 and table
10.2.8; all-of-any, any-of-all, all-of-all keep their 1.0 ids (no 3.0
replacement); no 2.0 id removed; registry lookup order canonical, short id,
legacy id; Apply and FunctionReference report the canonical 3.0 id;
XacmlTypes maps the W3C and 20020816 xquery-operators duration type ids to
one value type, so 3.0 ids and 1.0 aliases accept both; ast-grep 0; no
console output; headers and tabs kept.
