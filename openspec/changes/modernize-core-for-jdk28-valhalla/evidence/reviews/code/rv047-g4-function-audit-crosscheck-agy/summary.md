# rv047: independent cross-check of run 037 findings NEW-5..NEW-15 (Agy)

Date: 2026-10-02. Lane: `policy4j_code_review_agy` (NTM persona), Antigravity;
the pane shows Gemini 3.8 Flash (High). The report's own label says
claude-3-7-sonnet; the self-reported model name is unreliable (earlier
reports said gemini-2.5-pro). Session `policy4j--g1`, pane 3. Read-only: the
cited sources and XACML 3.0 Appendix A.3.

Purpose: a second model family checks the findings the conductor did not
confirm in source (NEW-1..4 were confirmed in run 037).

| Finding | Verdict | Reason (reviewer) | Bead |
| --- | --- | --- | --- |
| NEW-5 rfc822Name-match | CONFIRMED | delegates to unanchored regex; domain pattern matches subdomains and partial strings | 4.39 |
| NEW-6 xpath-node-* errors | CONFIRMED | catches EvaluationException, returns 0/false | 4.40 |
| NEW-7 xpath-node-match | CONFIRMED | isEqualNode and attribute equality instead of identity/descendants | 4.40 |
| NEW-8 regexp alternation | CONFIRMED | unparenthesized `.*` around alternation | 4.41 |
| NEW-9 double >= / <= | CONFIRMED | Double.equals for the equal part | 4.42 |
| NEW-10 syntax-error lost | CONFIRMED | FunctionInvocationException always processing-error | 4.43 |
| NEW-11 overflow | CONFIRMED | wrapping long arithmetic (decision C3) | task 5.4 |
| NEW-12 x500Name-match | CONFIRMED | string endsWith instead of RDN sequence (decision C8) | 4.44 |
| NEW-13 lower-case locale | CONFIRMED | String.toLowerCase() without Locale.ROOT | 4.45 |
| NEW-14 round NaN/INF | PARTIAL | Math.round returns 0 for NaN, but A.3.2 does not specify NaN/INF for round | decision C4 |
| NEW-15 normalize-space | CONFIRMED | String.trim() strips U+000B | 4.46 |

Files opened: the run 037 report and summary, SpecialMatchFunctions,
XPathFunctions, RegExpFunctions, NumericComparisonFunctions,
DefaultFunctionInvocation, FunctionInvocationException, SyntaxException,
StringFunctions, Condition, ArithmeticFunctions, IntegerVal, BooleanVal,
StringConversionFunctions, StringVal, RFC822Name.
