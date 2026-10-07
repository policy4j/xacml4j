Scenario 1 does not lock staging. Scenarios 2 and 4 do. Scenarios 3 and 5 lock loss, duplication, and (scenario 5) assignment values. Sorted id lists keep duplicates, so they are real cardinality checks. Scenario 2’s status check is specific enough. Scenario 3’s `times(1)` checks are backed by a real repository in the PDP test.

`ids()` sorts and keeps duplicates. A second copy of any expected id fails the list comparison. That answers the “sorted ids are only a set” concern for every test that uses `ids()`.

| Scenario | Duplicate publication | Wrong effect | Staging removed |
| --- | --- | --- | --- |
| 1 | Caught only for the sibling | Caught only for the sibling | Still passes |
| 2 | Caught | Caught for Permit and Deny | Fails |
| 3 | Caught | Deny obligations caught; Deny advice is not asserted | Passes when each matching response is still published once |
| 4 | Caught | Deny-rule response in the Deny result is caught; staged forwarding of Permit is caught | Fails for those two leaks |
| 5 | Caught | Caught | Passes when each response is still published once, with the same origin |

## Findings

**MEDIUM** — `xacml-core/src/test/java/org/xacml4j/v30/policy/NestedResponseStagingTest.java`, lines 84–102, method `childWithAnIndeterminateTargetAddsNoResponsesToThePermitOfItsSibling`.

The tree matches the requested shape: permit-overrides, a child whose target expression throws, a Permit rule with an obligation, a Permit sibling, and assertions that the root received only `sibling-obligation` and `sibling-advice`. It does not show that the child was Indeterminate, and it does not show that staging discarded anything.

Under XACML policy evaluation, an indeterminate target returns before rules and before policy obligations and advice. `indeterminate-rule-obligation`, `indeterminate-obligation`, and `indeterminate-advice` are never produced. Permit-overrides then returns the sibling Permit with that sibling’s own responses. The same decision and the same four assertions hold when the first child is NotApplicable, and they hold if staging is removed. Nothing from the indeterminate child can be published twice or under Deny, because that child never emits a response. The sibling is a direct child of the outermost set, so its Permit responses belong on the root for the final decision either way.

Line 84 builds that target failure with `mock(Expression.class)`. `FailingExpression` in the same file already throws a processing-error `EvaluationException`. The mock’s `getEvaluatesTo()` is null, so a target type failure satisfies the same assertions.

Suggested change: build the target with `FailingExpression`. Evaluate that policy alone and assert `isIndeterminate()`, `STATUS_PROCESSING_ERROR`, and empty obligation and advice lists, so a NotApplicable target cannot pass. Keep the sibling case, and compare the sibling assignment values. Treat this method as the combining case it actually is. The “buffer was filled, then not published” oracle is scenario 2; this method does not provide one.

**LOW** — `NestedResponseStagingTest.java`, lines 173–176, method `referencedPolicyAndPolicySetPublishTheirResponsesThroughNestedSets`.

Permit obligation ids, Permit advice ids, and Deny obligations are exact, and duplicates would fail those lists. `verify(..., times(1))` on lines 177–178 matches “resolved once” for these two instances. `NestedResponseStagingPdpTest` resolves the same shape through a real `PolicyRepository`.

Deny advice is unchecked. Permit advice published under `Decision.DENY` leaves this test green. Scenario 3 is the only reference path, so an advice-only mis-file on that path has no other test.

Suggested change: assert `getMatchingAdvices(Decision.DENY)` is empty, and assert each Permit response’s `origin` value the way `assertOrigin` does.

**LOW** — `xacml-pdp/src/test/java/org/xacml4j/v30/pdp/NestedResponseStagingPdpTest.java`, lines 101–106.

The obligation and advice id lists keep duplicates and name every level once, including `root-obligation`. The loop on lines 104–106 asserts that each obligation has one `origin` attribute. It does not compare that value, and advice assignments are not read. An obligation or advice with the right id and the wrong origin still passes. The PIP on line 93 is an unused mock on a path that otherwise uses real policies, a real repository, and a real request.

Suggested change: assert `origin` equals the declaring id for every obligation and every advice. Assert `getResults()` has one element before `findFirst()`.

**LOW** — `NestedResponseStagingTest.java`, lines 206–219, inside `ruleResponseForAnotherEffectDoesNotReachTheDecisionOfALaterSibling`.

The Deny assertions on lines 203–204 and 216–217 fail if `mixed-deny-rule-obligation` or `mixed-deny-rule-advice` reaches the sibling’s Deny result. Lines 218–219 fail if a staged `enclosing` forwards Permit responses. That is the requested scenario, and it fails if that staging is removed.

The comment on lines 206–207 says the overruled policy’s Permit obligation stays behind. Lines 218–219 also pass when `mixed-permit-rule-obligation` was never issued. The first evaluation, where the design does publish that obligation to the outermost Permit bucket, never asserts it.

Suggested change: on the first root, assert the Permit bucket is exactly `mixed-permit-rule-obligation`. The outer empty Permit list then shows that same response was withheld.

**LOW** — `NestedResponseStagingTest.java`, lines 229–232, method `fourLevelsOfNestingPublishEveryResponseExactlyOnce`.

`assertOrigin` checks cardinality and the assignment value, and the id lists keep duplicates. Loss, duplication, a wrong origin, and a Deny publication fail this test.

The chain is three policy sets around one policy (`level1`–`level3` are sets, `level4` is a policy). The requested case was four levels of policy-set nesting. A defect that appears only at a fourth nested policy set is outside this tree.

Suggested change: wrap one more policy set around `level1`, give it its own obligation and advice, and extend the id and `assertOrigin` expectations.

Scenario 2 is a real staging test. The rule responses are created, the policy obligation then throws `assignment failed`, and lines 126–132 require an indeterminate decision, that processing-error message, and empty Permit and Deny lists. Publishing the buffered rule response, or publishing it under Deny, fails the test. No change needed there.

VERDICT: REQUEST_CHANGES
grok exit=0
