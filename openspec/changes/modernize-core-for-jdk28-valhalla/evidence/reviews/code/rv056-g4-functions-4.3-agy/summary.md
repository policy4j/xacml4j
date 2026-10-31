# rv056: task 4.3 and function bugs 4.35-4.43, 4.45, 4.46 review, lane g43 (Agy)

Date: 2026-10-02. Lane: `policy4j_code_review_agy` (NTM persona), Antigravity;
pane shows Gemini 3.8 Flash (High). Session `policy4j--g1`, pane 3, 21 tool
calls. Read-only: lane g43 patch (1,431 lines, 17 files; author Grok), lane
sources and baseline, run 042 evidence, run 037 design input.

Verdict: REQUEST_CHANGES (1 MEDIUM, 1 LOW).

PASS per reviewer: 4.35 any-of-all (for each second-bag element, any
first-bag element; call count in HigherOrderFunctionsTest 8 -> 7); 4.36 2.0
typed regexp-match ids with the 1.0 ids kept as legacy aliases (accepted);
4.37 eleven string-from-* ids (no old-id callers or fixtures; the
AnyURILexicalTest change is only the corrected id); 4.38 return types swapped
back, range checks, NaN/INF disabled per C5; 4.40 errors propagate, node
identity, absent Content still 0/false; 4.41 grouping, empty pattern -> ".*"
(fn:matches with an empty pattern is true); 4.42 primitive IEEE comparisons;
4.45 Locale.ROOT; 4.46 XML whitespace only. Table B: 48 rows active, 12
disabled, each with a decision or bead id (C1-C8, 4.6, 4.29, 4.44, task 5.4).
No production change outside the beads; LGPL headers; tabs.

1. MEDIUM (4.43), `FunctionInvocationException.java:25-38, 50-58`,
   `DefaultFunctionInvocation.java:66-76`: only `SyntaxException` keeps its
   status; any other `EvaluationException` in the cause chain (for example
   missing-attribute from the attribute-designator function) still becomes
   processing-error, so the disabled safety-net test
   `attributeDesignatorFunctionRequiredMissingAttributeIsIndeterminateForCategoryAndEntity`
   cannot be enabled; the 4-argument constructor does not use the new status
   logic. Fix: preserve the status of the first `EvaluationException` in the
   chain; route every constructor through it; enable that test.
2. LOW (4.39), `RFC822Name.java:182-197`: an empty match pattern throws
   `StringIndexOutOfBoundsException` (`charAt(0)`) instead of evaluating to
   false. Fix: guard the empty pattern; test.
