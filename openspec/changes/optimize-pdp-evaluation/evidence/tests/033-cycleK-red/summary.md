# 033: cycle K tests before the change (15.1)

Date: 2026-10-27. Oracle GraalVM 25.0.4, Maven 3.9.16 offline, `-pl xacml-pdp -am`.

- `ContextChainQueryCharacterizationTest` (new, 4 tests): green on the code before cycle K. It pins which
  context answers each query in a tree of a policy set, a policy set reference, a policy set, a policy
  reference, a policy, and a rule (probes in the conditions), the staging contexts in the chain, a foreign
  `EvaluationContext` above the tree and inside the chain (called once per forwarded query), and settings
  written at the root.
- `DefaultEvaluationContextHandlerTest.repeatedRequestDesignatorGivesTheSameOptional` (new): red, 1 of 40
  (`cycleK-red.log`): the second lookup of a request attribute returned a new `Optional`.
