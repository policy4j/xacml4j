# rv039: bugs 4.15, 4.14, 4.21 review (Agy)

Date: 2026-10-01. Lane: `policy4j_code_review_agy` (NTM persona), Antigravity,
Gemini 3.8 Flash (High). Session `policy4j--g1`, pane 3. Read-only.

Scope: same as rv038.

Verdict: APPROVE (1 LOW).

Finding, LOW, `PermitOverrides.java:49, 82-84`: an atLeastOneIndeterminate
local flag is never set (dead code); remove it, as DenyOverrides does.

Reviewer statements: A.3.9 argument order and types (search functions
(string, anyURI); legacy uri-* keep (anyURI, string) by delegation);
substring zero-based, end exclusive, -1 to the end, out-of-range ->
FunctionInvocationException with processing-error -> Indeterminate
(StringFunctionSemanticsTest:254-265); code-point indexing. Appendix C:
hand derivations for deny- and permit-overrides match; the old
PermitOverrides returned Deny for Deny + Indeterminate{P} and
Indeterminate{DP} for Deny + Indeterminate{D}; the 3,048-row matrix is built
from static tables and failed 144 cases on the old code (run 025 red), so it
is not circular. Legacy algorithms extend BaseDecisionCombiningAlgorithm and
are unchanged. The 16 re-enabled cases execute and pass.

Not found by this lane: the AnyURI normalization defect in rv038 (Grok).
