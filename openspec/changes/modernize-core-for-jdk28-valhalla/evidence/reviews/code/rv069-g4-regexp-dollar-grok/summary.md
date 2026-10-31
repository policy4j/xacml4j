# rv069: bug 4.49 adversarial review, regexp `$` translation, lane g449 (Grok)

Date: 2026-10-02. Lane: adversarial code reviewer, Grok Build grok-4.7
(xhigh), role `.ntm/roles/policy4j_code_review.md`, session cleared first.
Session `policy4j--g1`, pane 0. Read-only: lane sources, patch, evidence 062;
XACML 3.0 OS A.3.13, XF 2007 REC 7.6.1-7.6.2, XML Schema Part 2 Appendix F,
JDK 21 `Pattern` javadoc and source.

Verdict: APPROVE (no findings).

`translateEndAnchors` (after the `\p{Is` and class-subtraction rewrites)
turns an unescaped `$` at class depth 0 into `\z`; patterns compile with no
flags, so `\z` matches XF 7.6.1 `$` (end of the whole string) and `^` stays
as is. Attacks without a defect for legal patterns: `\\$` (literal backslash
then end), `\$` literal, `[a-z-[$]]` (rewritten to `[a-z&&[^$]]`, inner `$`
literal), `$` in groups and with quantifiers, a bare `$`, surrogate pairs,
`^foo` vs "foo\n"/"\nfoo".

Residual: `[]$]` / `[^]$]` (not legal XPath classes; XF rejects them with
FORX0002) now raise PatternSyntaxException instead of a Java match; the
typed `.*$` test does not prove the rewrite (the `^foo$` case does); other
line terminators (`\r`, U+0085, U+2028/2029) not tested; the pre-existing
`calculateNestLevel` ignores escapes.
