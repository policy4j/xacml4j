# D47 runtime-value eligibility correction

- Lane: `cod_1`, packet assignment Codex GPT-6.1 Terra, max effort. The runtime did not independently expose its model or effort to this lane.
- Bead: `policy4j-cwtu.42.14`, claimed by the conductor as `ntm-terra-g1`.
- Base: `759d90a6c75510d55127d8e22870f9edd985c991`; no commit, task tick, or issue closure.
- Toolchain for the authoritative runs: Maven 3.10.0, Oracle GraalVM JDK 25.0.4+7.1, offline, license updates disabled, shared serialized runner.

## Change

`Rule.EngineOnly` tracks whether a built-in expression can produce an unchecked runtime value or a function reference. A call that combines both is ineligible for borrowing. Definitions retain their provenance when reused, including across a diamond; a cycle is conservatively ineligible. Completed built-in calls do not propagate a function-reference argument as a return value, so a literal-only higher-order call remains eligible even alongside an unrelated runtime designator.

A rule target containing a Match is also ineligible: Match can pass a request or PIP value to an otherwise built-in predicate, which can evaluate that value with the rule context. This is a normal Match path, not an XACML higher-order expression. No PIP is resolved by the scan. No public API changes.

Owned source files and SHA-256:

| File | Base | Candidate |
| --- | --- | --- |
| `xacml-core/src/main/java/org/xacml4j/v30/policy/Rule.java` | `60407c4a469e53e58f2344ebff3caaa887cb9bd1c6401a93bb713fe325d5522c` | `717be6326fecf2702afad2818a4a1f2a37fbcdfc3dcf052977e6effb8fd0c094` |
| `xacml-pdp/src/test/java/org/xacml4j/v30/pdp/RulesInPolicyContextPdpTest.java` | `a678f87dd34943ffc6d9b435b694500bfcd6400e90caba9cea54688eea73849f` | `ea5410ab8431c02a90dfb4b229743eeb2306680c92f938bbace5e2aec3b8737c` |

## Executed evidence

- `04-focused-clean-red.txt`: clean baseline, before editing `Rule.java`; 40 tests, 18 failures, 0 errors, 0 skipped. Sixteen runtime-value cases and two repeated-condition cases showed the ordinary observed Permit response versus unobserved NotApplicable. The four computed-argument cases instead preserve the ordinary Indeterminate processing-error result, and passed. The original extension fallback controls passed.
- `05-focused-clean-green.txt`: same focused clean run after the correction; 40 tests, 0 failures, 0 errors, 0 skipped.
- `06-modules-clean.txt`: `xacml-pdp` with reactor prerequisites, clean test; core 7,357 tests (2 skipped), XML 254 (0 skipped), JSON 24 (1 skipped), PDP 835 (3 skipped); total 8,470 tests, 0 failures, 0 errors, 6 skipped. JAXB and the root POM have no reported tests.
- `rtk git diff --check`: passed after the module gate. `git status` showed only the two owned tracked source files modified.

The named new test methods are `runtimeValuesEvaluatedByPredicatesSeeTheRuleContext` (20 parameterized cases: five placements, two sources, two index modes; each compares observed and unobserved responses), `unreachableHigherOrderRuleDoesNotResolveItsPipValue` (2), `missingRequiredPipValueKeepsItsDecisionAndStatus` (2), and `repeatedConditionsAndHigherOrderRulesKeepTheirResponses` (4). The last has four repeated shareable normalized expressions, a runtime-value or literal-only higher-order path, and an eligibility assertion for the literal case. That assertion checks the construction-time flag; it does not independently measure allocation or prove entry into the borrowing path. Existing custom-function, custom-predicate, custom-literal, and referenced-policy cases remain.

Earlier attempts are retained for provenance only. The initial sandboxed wrapper invocation could not open the shared lock and ran no Maven command; `00-sandbox-lock.txt` reconstructs its diagnostic with the path normalized. `01-focused-red.txt` failed offline dependency resolution under the old Maven 3.9 default. `02-focused-red.txt` used a temporary task-local settings file with Maven 3.9 and included four incorrect expected decisions in addition to real failures; it is not the authoritative red run. That temporary settings file was removed before the conductor's Maven 3.10 runs; no user settings or cached-origin metadata were changed. The runner's five `.log` outputs were renamed byte-for-byte to `.txt` because a user-level Git ignore rule hides `*.log` files.

## Limits

JMH: **UNRUN**, as assigned. The conservative fallback for all nonempty rule targets may reduce D47's prior allocation savings; the conductor must measure it. The final combined reactor, conformance, independent candidate review, and performance gates remain with the conductor. D45 interaction is covered by outcome comparison using repeated shareable expressions; actual memo activation was not separately instrumented.
