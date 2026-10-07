# rv042: bugs 3.10, 3.11, IIIE302 review, conformance comparators (Agy)

Date: 2026-10-02. Lane: `policy4j_code_review_agy` (NTM persona), Antigravity,
Gemini 3.8 Flash (High). Session `policy4j--g1`, pane 3. Read-only.

Scope: xacml-test Xacml20TestUtility, Xacml30TestUtility, XacmlPolicyTestSupport
and their tests; manifest rows changed in run 030.

Verdict: APPROVE (1 LOW).

Finding, LOW, `XacmlPolicyTestSupport.java:217-219`: assertAssociatedAdvices
matches advice by AdviceId only (assertObligations also compares the
attribute multiset); permuted advices with the same id but different
assignments can bind wrongly. Fix: match on id and the attribute multiset.

Reviewer statements: strings stay strict (case and whitespace); different
values of the same type fail; missing/extra elements fail; DataType must
match; registered types are compared by value (27.50 == 27.5), invalid
lexical forms fail, unregistered types fall back to strict string equality;
categories compared as a multiset on id, xml:id, attributes and Content
(Node.isEqualNode); the 2.0 obligation comparison reads both responses and
does not reuse matched elements; 18 + 29 + 6 new tests; no fixture changed;
IIIA030/IIIA330 fail legitimately (engine bug policy4j-the.4.24).
