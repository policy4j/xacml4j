# 029: cycle J characterization before the change (task 14.1)

Date: 2026-10-27. Oracle GraalVM 25.0.4, Maven 3.9.16 offline, tree with cycle I.

- `CombiningListTypesCharacterizationTest` (new), 3 tests: every combining algorithm (deny-overrides,
  permit-overrides, deny-unless-permit, permit-unless-deny, first-applicable, only-one-applicable, the four
  legacy policy and four legacy rule algorithms) over every sequence of up to three decisions (400
  sequences of the 7 decisions) gives the same decision and evaluates the same elements in the same order
  whether its list is an `ArrayList`, a `LinkedList`, or `List.of`.
- `FunctionInvocationCharacterizationTest.linkedListArgumentsNormalizeAsArrayListArguments` (new):
  normalization of optional and variadic calls gives the same arguments for a `LinkedList`.
- Existing `TargetTest`, `MatchAnyOfTest`, `MatchAllOfTest` pin order and short-circuit of the target.

Result: 33 run, 0 failures, before D16 (characterization). Output: `output.txt`.
