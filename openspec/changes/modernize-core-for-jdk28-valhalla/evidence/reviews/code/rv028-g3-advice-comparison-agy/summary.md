# rv028: bug policy4j-the.3.8 review, AssociatedAdvice comparison (Agy)

Date: 2026-10-01. Lane: `policy4j_code_review_agy` (NTM persona), Antigravity,
Gemini 3.8 Flash (High). Session `policy4j--g1`, pane 3. Read-only.

Scope: 211-line patch from the private advice lane (xacml-test
Xacml30TestUtility and Xacml30TestUtilityTest).

Verdict: REQUEST_CHANGES.

Findings:
1. MEDIUM, `Xacml30TestUtility.java:203-208` (and pre-existing `:217-224`,
   `:256-264` for obligations): toAttributeAssignmentMap keys assignments by
   AttributeId, so multi-valued assignments with the same AttributeId
   (XACML 3.0 5.34, 7.18; e.g. fixture IIIA301Response.xml:28-36 has
   assignment2 three times) collapse to the last one. Wrong earlier values
   pass (false negative); a different valid order fails (false positive).
   Fix: compare assignments as an unordered multiset matching all fields
   (AttributeId, Category, Issuer, DataType, content); apply the same helper
   to obligations.
2. MINOR, `:198`: assertAdvice is private while assertObligation is public;
   make the visibility consistent.
3. MINOR, `Xacml30TestUtilityTest.java:146-195`: no tests for multi-valued
   assignments with the same AttributeId (matching, differing, permuted) or
   for duplicate AdviceIds.

Checked (reviewer): advice compared as a set by AdviceId in both directions,
order independent; absent and empty normalize to empty.
Resolution: repair packet to the implementer lane (advice lane).
