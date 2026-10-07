# rv063: task 4.3 and function bugs, lane g43 (Sol, Codex reviewer)

Date: 2026-10-02. Lane: `policy4j_code_review_codex_sol` (Codex GPT-6.1-Sol;
the report header says GPT-6). Session `policy4j--g1`, pane 1. Read-only:
all 21 files of the regenerated g43 patch (1,842 lines, SHA-256
e9895ded05a1693cae4399fdc486a86e205802b8fae5ced5b2aecb5d9df3607f) and
baselines, supporting sources, runs 037/042, rv047, rv056. Report text taken
from the Codex session transcript.

Verdict: REQUEST_CHANGES (3 MEDIUM).

1. MEDIUM (4.38), `NumericConversionFunctions.java:70`: integer-to-double
   exactness check accepts `Long.MAX_VALUE`: (double) 9223372036854775807 is
   2^63; casting back saturates to Long.MAX_VALUE (JLS 5.1.3), so a changed
   value passes. Fix: reject converted values >= 2^63 before the cast-back
   check; tests for Long.MAX_VALUE (processing-error) and Long.MIN_VALUE
   (exact).
2. MEDIUM (4.41), `RegExpFunctions.java:186, 195-205`: wildcard padding is
   still wrong: an empty pattern becomes `.*`, which fails whole-input
   matching on "\n"; branch-local anchors break (`^foo|bar` vs "xbar",
   `foo|bar$` vs "foox" return false). XF 7.6.2 fn:matches is a substring
   match. Fix: translate the XML Schema/XPath regex and use find() instead of
   padding; tests for these cases and anchored negative controls. (Qualifies
   rv056's acceptance of the empty-pattern handling.)
3. MEDIUM (4.40), `XPathFunctions.java:86, 125-129`: both XPath expressions
   are evaluated before checking Content presence; if one category has no
   Content and the other XPath fails, the result is Indeterminate, but A.3.15
   requires false when either Content is absent. Fix: check both Content
   objects before evaluating; tests in both argument orders for both
   functions, plus a both-present failure control.

Confirmed: rv056 fixes present (first EvaluationException status kept; empty
rfc822Name pattern false); Table B 50 active rows, 10 disabled plus two
decision tests (C1-C8, 4.6, 4.29, 4.44, 5.4); legacy 1.0 regexp aliases;
no old string-from-* ids in callers or fixtures; AnyURILexicalTest only the
id change; LGPL headers; the lane's 4.33 block identical to main.
Not reproduced at runtime (read-only review).
