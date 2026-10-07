# rv062: task 4.4 follow-up and bugs 4.33, 4.27 adversarial review (Grok)

Date: 2026-10-02. Lane: adversarial code reviewer, Grok Build grok-4.7
(xhigh), role `.ntm/roles/policy4j_code_review.md`, session cleared first.
Session `policy4j--g1`, pane 0. Read-only on the merged main worktree; OASIS
XACML 3.0 OS 10.2.8 and Appendix C; XML Schema Part 2 3.2.5, 3.2.16.

Verdict: REQUEST_CHANGES (1 MEDIUM, 3 LOW).

Fixed: rv048-1 for the named rows (expected lexical literals, parse through
the resolved type); rv048-2 (alias parse-through); 236 enabled 10.2.8 ids (253
in the table minus 5 for 4.36, 11 for 4.37 and the optional access-permitted);
4.33 on the direct call and the PDP optional case; 4.27 (@AutoService plus
withDefaultAlgorithms). The conductor's negative-zero test is correct.

1. MEDIUM, `DecisionCombiningAlgorithmRegistryTest.java:95-113`: one vector
   per URN does not separate deny-unless-permit from first-applicable or
   permit-overrides, or permit-unless-deny from deny-overrides or
   first-applicable; ordered vs unordered share the decision table (not
   stated); 1.0 legacy vs 3.0 Indeterminate is not covered (the helper cannot
   build Indeterminate children). Fix: evaluate every URN on one shared set
   (NotApplicable only; Permit+Deny; Deny+Indeterminate{D};
   Permit+Indeterminate{D}) with Appendix C expectations; state the ordered
   limitation.
2. LOW, `TypeRegistryTest.java:159`: row parses "YQ" (outside xs:base64Binary
   per RFC 2045 padding) - keep "YQ==" and add a disabled rejection row for
   4.48.
3. LOW, `TypeRegistryTest.java:188-203`: the disabled AnyURI rows still
   expect normalization (`a/../b` -> `b`); the 4.15 fix keeps the original
   spelling. Expect the original spellings and enable.
4. LOW, `AttributeDesignatorFunctions.java:66-67, 77-78`: missing-attribute
   status message uses `attributeId.toString()` (debug form); use
   `toXacmlString()` and assert the message.
Residual: `scalarLexicalValueRoundTrips` still self-references (12.50 or
1.25E1 for 12.5, "1" for true would pass); Entity sample is ignored by
`Entity.ofAny`; obligation order for ordered algorithms untested.
Resolution: items queued to the author with the rv057/rv058 follow-up lane.
