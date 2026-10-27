# 010: cycle F tests before and after (tasks 9.1, 9.2)

Date: 2026-10-27. Oracle GraalVM 25.0.4, Maven 3.9.16 offline, release 25.

Before-tree: the cycle E snapshot with the two new test files, so only cycle F differs.

- `PolicyVariableContextCharacterizationTest` (2): a policy without variables returns no result and rejects
  every id; a policy with variables keeps its own ids' results per context, rejects an undefined id, and an
  extended-indeterminate child context starts empty.
- `DefaultPolicyReferenceResolverCacheTest` (3): repeated resolutions of a policy and a policy set reference
  ask the repository once; a matching `policyAdded` or `policySetAdded` empties the entry; a disabled cache
  asks every time; 8 threads resolving first at the same time all get the policy.

Existing coverage kept: `VariableDefinitionTest`, `VariableReferenceTest`, `DefaultPolicyReferenceResolverTest`;
cyclic references, versions, and nested policy sets are in the conformance suites (reactor run).

Before D10: 5 of 5 pass (characterization). After D10: the same.

Output: `output.txt` (test lines of the before and after runs). The reactor and conformance run of the gate is separate.
