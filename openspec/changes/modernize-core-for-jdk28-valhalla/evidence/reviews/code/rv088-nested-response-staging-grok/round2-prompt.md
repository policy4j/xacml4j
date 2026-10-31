# Adversarial test review, round 2: nested response staging scenarios (xacml4j)

You are an independent reviewer. Do not use tools. Do not edit files. Answer
in one message. Review only the diff below: two new test classes, no
production code. The full reactor passes with them: 7,781 tests, 0 failures, 0 errors, 22 skipped.

## Project

xacml4j is a Java 21 implementation of OASIS XACML 2.0/3.0. A policy or
policy set that is evaluated inside another one buffers the obligations and
advice of its children ("staging") and publishes to its parent only those
for its own final decision. The outermost rule is not staged: its children
publish into the root context by effect, and the PDP returns the list for
the final decision.

## Task

Five scenarios were named by an earlier review as not yet run:

1. Enclosing permit-overrides; a child with an Indeterminate target and a
   Permit rule that carries an obligation; a Permit sibling: the sibling's
   responses only.
2. An enclosed policy whose own obligation expression fails:
   Indeterminate, the error status kept, no buffered response published.
3. `PolicyIdReference` and `PolicySetIdReference` children with responses
   below staged nested sets: resolution and correct response flow.
4. Rule responses with an effect that differs from the decision of the
   enclosing policy: no such response reaches the result of a later
   sibling.
5. Four levels of policy-set nesting: no duplicate and no lost response;
   compare cardinality and assignment values, not only id sets.

The tests must use real policies and contexts, not implementation-only
mocks, and must not rely on skips.

Facts you cannot see in the diff:

- `ModelFixtures.target(expression)` builds a real `Target` whose match
  evaluates the given expression; `ModelFixtures.evaluationError()` is an
  `EvaluationException` with processing-error; `ModelFixtures.context(false)`
  is a real `RootEvaluationContext` whose resolver and attribute handler
  fail the test when called.
- `root.getMatchingObligations(decision)` and `getMatchingAdvices(decision)`
  return what was published to the root context for that decision.
- All five tests of `NestedResponseStagingTest` and the PDP test passed
  when they were first complete, except that the first version of
  scenario 4 asserted an empty Permit list on the outermost set, which the
  design above does not promise; that assertion moved one level down.

## Round 1 result and what was done

Grok: REQUEST_CHANGES with one MEDIUM and four LOW findings. All five
suggested changes are in the diff below, with one correction of a fact.

1. MEDIUM, scenario 1 ("does not lock staging; an indeterminate target
   returns before rules, the child never emits a response"). The target
   now fails through `FailingExpression` (processing-error, message
   "target failed"), and the child is first evaluated alone: Indeterminate
   with that status. The stated fact is not what this engine does: with an
   Indeterminate target the rules are still evaluated, to find the
   extended Indeterminate value (XACML 3.0, section 7.12), and the Permit
   rule creates its obligation. Evaluated alone (outermost, not staged)
   that obligation is in the root context's Permit list; the test asserts
   exactly that, as proof that the response exists. Inside the policy set
   it is staged and not published. So scenario 1 does depend on staging;
   see the mutation check below.
2. LOW, scenario 3: Deny advice is now asserted empty; every Permit
   obligation and advice is checked for its `origin` value.
3. LOW, PDP test: the result list must have one element; every obligation
   and every advice is checked for its `origin` value. The PIP mock stays:
   the PDP builder requires a PIP (`checkState(pip != null)`); the
   policies read no attribute.
4. LOW, scenario 4: the first evaluation now asserts that the Permit list
   of the outermost root is exactly `mixed-permit-rule-obligation`.
5. LOW, scenario 5: four policy sets (`level1` to `level4`) around the
   policy `leaf`.

`assertOrigin` now takes the declaring id from the response id without its
`-obligation` or `-advice` suffix.

Mutation check (production code changed for the run only, then restored;
`git diff` of the production file is empty):

| Mutation of `BaseCompositeDecisionRule` | Tests of `NestedResponseStagingTest` that fail |
| --- | --- |
| M1: the extended-Indeterminate evaluation of an enclosed rule is not staged | scenario 1 (`[indeterminate-rule-obligation, sibling-obligation]` instead of `[sibling-obligation]`), scenario 2 |
| M2: the normal evaluation of an enclosed rule is not staged | scenario 2 (`[failing-rule-obligation]` instead of `[]`), scenario 4 (`[mixed-deny-rule-obligation, sibling-obligation]`) |

Scenarios 3 and 5 pass under both mutations: they lock loss, duplication
and the assignment values, not the removal of staging, as round 1 said.

## Questions

Report findings ordered by severity (HIGH, MEDIUM, LOW), each with file,
line from the diff, what the test fails to show, and a suggested change.

1. Are the five findings of round 1 closed? If you still hold that
   scenario 1 does not lock staging, say what in the mutation result is
   wrong.
2. Is a new assertion wrong, or does it lock an implementation detail that
   the design above does not promise (for example the Permit list of the
   outermost root in scenarios 1 and 4)?
3. Is an assertion still too weak for its scenario?

End with a verdict line: `VERDICT: APPROVE` or `VERDICT: REQUEST_CHANGES`.

## Diff

```diff
