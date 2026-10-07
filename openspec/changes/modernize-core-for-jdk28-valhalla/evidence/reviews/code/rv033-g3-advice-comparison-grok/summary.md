# rv033: bug policy4j-the.3.8 review, advice/obligation comparison (Grok, adversarial)

Date: 2026-10-01. Lane: adversarial code reviewer, Grok Build grok-4.7
(xhigh), role `.ntm/roles/policy4j_code_review.md`, session cleared first.
Session `policy4j--g1`, pane 0. Read-only; re-counted the lane conformance log.

Scope: advice lane after the rv028 fix (advice-lane-v2.patch:
Xacml30TestUtility, Xacml30TestUtilityTest).

Verdict: APPROVE. Findings: none.

Reviewer statements: assertResults compares AssociatedAdvice (`:83`); absent
and empty normalize to empty (`:165-179`) like obligations (`:148-162`);
advice, obligations and assignments are occurrence-preserving multisets
(`:205-247`); assignment equality covers AttributeId, Category, Issuer,
DataType and content (`:219-225`), shared by advice and obligations;
assertAdvice is public. XACML 3.0 5.33 (advice set, order not significant),
5.35-5.36 (bags of assignments may repeat), 7.18, 7.20 (codepoint equality)
are respected; IIIA301Response.xml:28-36 (three assignment2 values) is
handled. Adversarial pairs that must differ do differ; permutations compare
equal; no differing pair compares equal. Red log: 24 tests, 4 failures (the
new multiset tests); green: 24/0. Conformance recount: OASIS 2.0 347, 3.0
347, RSA 17, examples 2; xacml-test 30/0, conformance 42/0.

Residual (reviewer): getOtherAttributes() of assignments is not compared
(OASIS responses do not use foreign attributes); content equality is lexical
(typed equivalents such as 1 and 01 differ; structured Element content
compares by identity); XacmlPolicyTestSupport.assertAssociatedAdvices is a
separate domain-object oracle, unused by conformance.
