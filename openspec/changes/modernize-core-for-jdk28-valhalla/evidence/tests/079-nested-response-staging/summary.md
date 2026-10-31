# Run 079: nested response staging, five behavioural scenarios (bead the.4.54)

Date: 2026-10-07. Source of the bead: rv078 (Opus finding 2, Astra proof
limits): five scenarios that had not been run. JDK: Oracle GraalVM
21.0.6+8.1 (`java version "21.0.6" 2025-01-21 LTS`). Review:
`reviews/code/rv088-nested-response-staging-grok`.

Tests only. No production code changed, and no scenario showed a defect.

## Scenarios

`xacml-core/src/test/java/org/xacml4j/v30/policy/NestedResponseStagingTest.java`
(5 tests). Real `Policy`, `PolicySet`, `Rule`, combining algorithms from
`DecisionCombiningAlgorithmProvider`, and `RootEvaluationContext`. Test
doubles: one expression that throws (scenarios 1 and 2) and the
`PolicyReferenceResolver` (scenario 3).

| # | Setup | Asserted | Result |
| --- | --- | --- | --- |
| 1 | `permit-overrides` set; child policy whose target throws processing-error "target failed", with a Permit rule that has an obligation, and its own obligation and advice; sibling policy Permit | the child alone: Indeterminate, that status, and the obligation of its rule in the Permit list of the unstaged root (the rule is evaluated for the extended Indeterminate value, so the response exists); in the set: decision Permit; Permit obligations and advice are the sibling's only, each with its `origin` value; no Deny responses | pass |
| 2 | `first-applicable` set; child policy with a Permit rule (obligation and advice) and a policy obligation whose assignment expression throws processing-error "assignment failed" | decision Indeterminate; status of the root context is processing-error with that message; no obligation or advice for Permit or Deny | pass |
| 3 | `deny-overrides` set around a nested `deny-overrides` set whose children are a `PolicyIdReference` and a `PolicySetIdReference`; both targets Permit with responses | decision Permit; obligations of the referenced policy, the policy inside the referenced set, the referenced set, and the nested set, each once and each with its `origin` value; advice of the two policies; no Deny obligation or advice; each reference resolved once | pass |
| 4 | Policy with `permit-overrides` rules: a Deny rule with a Deny obligation and advice, a Permit rule with a Permit obligation; sibling policy Deny; `deny-overrides` set. Evaluated as the outermost set and again one level down | decision Deny; Deny responses are the sibling's only; as the outermost set the Permit list holds exactly the Permit obligation of the rule; one level down, no Permit response reaches the root | pass |
| 5 | Four nested `first-applicable` sets around one Permit policy; every level declares one obligation and one advice with an `origin` assignment | decision Permit; five obligations and five advice, each id once; each has one assignment whose value is the id of the level that declared it | pass |

`xacml-pdp/src/test/java/org/xacml4j/v30/pdp/NestedResponseStagingPdpTest.java`
(1 test): scenario 3 through `PolicyDecisionPoint.decide` with a real
`PolicyRepository` that holds the referenced policy and policy set. One
result: Permit with status ok and five obligations (root, nested set,
referenced policy, referenced set, policy inside it) and two advice, each
once and each with its `origin` value.

Lists of ids are compared sorted and with duplicates kept, so a response
that is published twice fails the test.

## Mutation check

`mutation-check.txt`. One line of `BaseCompositeDecisionRule` was changed
for each run and put back afterwards.

| Mutation | Failing tests |
| --- | --- |
| M1: the extended-Indeterminate evaluation of an enclosed rule is not staged | scenarios 1 and 2 |
| M2: the normal evaluation of an enclosed rule is not staged | scenarios 2 and 4 |

Scenarios 3 and 5 pass under both mutations. They lock loss, duplication
and the assignment values, not the removal of staging.

## Notes from writing the tests

- The first version of scenario 4 also required the Permit list of the
  root context to be empty when the set is the outermost one. It is not:
  the outermost set is not staged, its children publish into the root
  context by effect, and the final decision selects the list. The PDP
  returns the responses for the decision only. The assertion was moved one
  level down, where the set is staged.
- Review round 1 held that scenario 1 does not depend on staging, because
  a policy with an Indeterminate target would not evaluate its rules.
  This engine does evaluate them, for the extended Indeterminate value.
  The first attempt to assert "the child alone gives no response" failed
  for that reason (`[indeterminate-rule-obligation]` in the Permit list).
  The test now asserts that list, and mutation M1 shows that scenario 1
  fails without staging.
- The tests were not red first. The bead asks for proof of scenarios that
  were expected to hold.

## Gate

`surefire-summary.txt`, gate 3 (clean build). Full reactor on JDK 21:
7,781 tests, 0 failures, 0 errors, 22 skipped. Conformance 937 tests with
12 skipped, unchanged.

## Review

`reviews/code/rv088-nested-response-staging-grok`: Grok grok-4.7, two
rounds. Round 1 REQUEST_CHANGES (one MEDIUM, four LOW, all about
assertions that were missing); all five changed. Round 2 APPROVE, no
finding. One model family reviewed this run; the tests change no
production code.

## JMH

NOT APPLICABLE: tests only.

## Limits

- Scenario 3 in xacml-core uses a mocked resolver; the PDP test covers the
  repository path for the Permit case only.
- The Indeterminate cases use `first-applicable` and `permit-overrides`.
  Other algorithms with an Indeterminate child are not covered here.
- The PDP test passes a mock `PolicyInformationPoint` because the PDP
  builder requires one; the policies read no attribute.
- The root context of an outermost Indeterminate policy keeps the
  obligation of its rule (scenario 1, the child alone). The root lists are
  read for a final Permit or Deny only:
  `DefaultPolicyDecisionPoint.createResult` builds a NotApplicable or
  Indeterminate result without reading them (read in the source). No test
  of this run asserts that.
