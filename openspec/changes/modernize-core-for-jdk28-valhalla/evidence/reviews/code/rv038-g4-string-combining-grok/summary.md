# rv038: bugs 4.15, 4.14, 4.21 review (Grok, adversarial)

Date: 2026-10-01. Lane: adversarial code reviewer, Grok Build grok-4.7
(xhigh), role `.ntm/roles/policy4j_code_review.md`, session cleared first.
Session `policy4j--g1`, pane 0. Read-only; XACML 3.0 OS A.3.3, A.3.9,
C.1-C.5, C.12; jshell for java.net.URI. Maven not run.

Scope: StringFunctions (4.15 argument order, 4.14 new 3.0 functions),
PermitOverrides (4.21), OverrideDecisionTableTest, StringFunctionSemanticsTest,
16 re-enabled manifest rows; evidence runs 024 and 025.

Verdict: REQUEST_CHANGES (1 MEDIUM).

Finding, MEDIUM, `StringFunctions.java:72, 99, 126, 162`;
`AnyURI.java:81, 113`: anyURI-starts-with/ends-with/contains/substring use
AnyURI.toStringExp(), which returns the normalized java.net.URI
(AnyURI.of(String) stores URI.create(v).normalize()). A.3.9 requires the
string-from-anyURI conversion, and A.3.3 says it SHALL be the URI "in the form
it was originally represented in XML form". "http://example.com/a/../b"
becomes "http://example.com/b", so anyURI-contains("a/../b", v) is false and
anyURI-substring indexes the shorter string. The new test builds values with
AnyURI.of(URI), which does not normalize, so it misses this; the re-enabled
OASIS URIs have no "." or ".." segments. Fix: keep the original lexical
string in AnyURI (stop normalizing in of(String)); add anyURI-contains and
anyURI-substring cases built from the lexical form "http://example.com/a/../b".

Checked, not findings: 4.15 matches A.3.9 (second argument begins/ends/
contains the first; empty needle true; case-sensitive; no %-decoding); types
(string, anyURI) for the 3.0 search functions, (string|anyURI, integer,
integer) -> string for substring; legacy uri-starts-with/ends-with/contains
keep their old (anyURI, string) order; substring uses code points, end
exclusive, -1 to the end, out-of-range -> Indeterminate processing-error
(IIC333 slice "/the/initi" correct). 4.21: PermitOverrides post-loop order
matches C.4; DenyOverrides matches C.2; ordered variants share combine();
the 3,048-row matrix is folded from hard-coded 6x6 tables recomputed from
C.2/C.4 (not circular); legacy 1.0/2.0 algorithms unchanged and match C.12.
The 16 re-enabled rows execute and compare responses; IID300 now gives
Indeterminate as expected.

Residual (reviewer): obligation/advice aggregation (7.18) not in the
decision matrix; substring indexes code points without NFC normalization;
java.net.URI accepts an empty slice; PermitOverrides has an unused
atLeastOneIndeterminate flag (`:49`, `:82-84`).
