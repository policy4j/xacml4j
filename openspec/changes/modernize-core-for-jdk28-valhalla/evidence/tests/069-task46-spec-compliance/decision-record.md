# Task 4.6 decision record, 2026-10-04

The maintainer's latest instruction is **Objective spec compliance**.
It supersedes run 061's compatibility recommendation for NaN equality.
The conductor has authorized tests-first implementation in NTM lanes;
task closure still requires red/green evidence and independent review.

1. IEEE double-equal: NaN is unequal to every value, including itself;
   signed zeros compare equal. Apply the same policy equality to double
   bag membership and set functions. Preserve Java Value.equals/hashCode
   and bag multiplicity.
2. integer-divide: return xs:integer with exact integer arithmetic.
   Preserve IntegerVal.divide as a legacy Java helper so no unrelated
   public helper changes. The registered function's corrected Java return
   type is an intentional compatibility change with a typed Apply test.
   Keep the approved project's toward-zero quotient/remainder semantics.
   A.3.2 establishes the result type, not an explicit truncation rule.
   Core7.5's general half-even wording and A.3.2's double-only IEEE wording
   are inconsistent; Appendix E records removal of IEEE references for
   integer arithmetic. Do not claim the truncation direction is expressly
   required by A.3.2. This ambiguity is retained as a standards limitation,
   rather than inventing a different rounding behavior in this task.
3. on-permit-apply-second: implement CS01 section2.1 before publishing its
   URN. Test both two- and three-child tables, short-circuit order,
   Indeterminate subtypes, status and obligation/advice isolation.
4. Keep IIC350 and IIC358 enabled. Their experimental Permit fixtures
   disagree with IEEE equality; add a narrowly guarded special harness
   expected NotApplicable response and retain the original vendored bytes.
   Restore both IIC020 cases. Compare every previously enabled case ID,
   not just total passing test counts.
5. Gate4.7 stays pending while disabled PIP, XPath and other semantics
   remain. No claim of whole-engine XACML compliance follows from task4.6.

Sources fetched through native MCP:
- [XACML3 Core OS](https://docs.oasis-open.org/xacml/3.0/xacml-3.0-core-spec-os-en.html):
  A.3.1, A.3.2, A.3.10/A.3.11, 7.5, 7.18 and AppendixE.
  Tavily succeeded earlier; DuckDuckGo fetched the full text after HTTP429.
- [Additional Combining Algorithms CS01](https://docs.oasis-open.org/xacml/xacml-3.0-combalgs/v1.0/cs01/xacml-3.0-combalgs-v1.0-cs01.html):
  section2.1; fetched through Tavily and DuckDuckGo.
- Requested Deep Research cross-check failed with token rate limiting.
  It supplies no accepted independent conclusion.

## XPath correction for gate 4.7
Native DuckDuckGo fetched the official 2013 OASIS Standard core text. Section A.3.15 explicitly distinguishes missing category Content from evaluation failure: xpath-node-count yields zero, xpath-node-equal and xpath-node-match yield false. Updated the local function-semantics delta accordingly. No XPath production change is part of task 4.6; the subsequent gate must test missing content separately from malformed XPath/provider failure.

## Tests-first scope expansion
Combining RED reproduced discardedFirstChildDoesNotAddObligationsToALaterSiblingDecision: expected only sibling-obligation, actual gate-obligation plus sibling-obligation. Authorized the Fable writer to edit BaseCompositeDecisionRule.java in addition to its algorithm/provider/test ownership, using private composite-boundary buffering and committing only the final matching Permit/Deny effect. RootEvaluationContext, DescendantEvaluationContext and public EvaluationContext remain outside writer ownership. Expanded code requires focused and full reactor gates plus cross-provider/adversarial review.

## JMH decision
APPLICABLE: numeric policy bag/set scans and composite evaluation change hot decision paths. No speedup or performance acceptance is claimed. Controlled JMH remains required for group 4 gate 4.7; the project has no JMH module yet, and task 9.2 owns module/tooling work. Do not label JMH NOT APPLICABLE or close gate 4.7 on structural/tests-only proof.

## Numeric test scope
Initial RED included a nonstandard short-ID lookup assertion. Source shows shortId is diagnostic metadata, not registered aliases. Removed two alias invocations on conductor instruction; kept all numeric behavior assertions and original RED receipts. Standard-URN numeric GREEN is 250 tests, zero failures/errors, seven unrelated safety-net skips. Empty zero-argument double-bag is normative and fixed in the owned BagFunctions boundary.
