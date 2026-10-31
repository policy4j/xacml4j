# ValueType one-element Object array

## Focused red (before production change)

- JDK: 25, enforced by the Maven enforcer; Maven: `/opt/homebrew/bin/mvn` (3.10.0).
- Command: clean focused test class, offline, license updates disabled; see `command.txt`.
- Result: **8 tests run, 4 errors, 0 failures, 0 skipped**.
- Semantic errors: `oneElementStringArrayConvertsItsContainedValue`, `oneElementIntegerArrayConvertsItsContainedValue`, `oneElementBooleanArrayConvertsItsContainedValue`, and `oneElementArrayConvertsAContainedValueInstance`. Each threw `SyntaxException.invalidAttributeValue` because `ValueType.apply` passed the one-element `Object[]` itself to the factory.
- Passed controls: `scalarInputKeepsTheFactoryConversion`, `emptyArrayRemainsInvalid`, `nullContainerRemainsInvalid`, and `multiArgumentArrayStillForwardsFactoryParameters`.
- The semantic red was produced by the new tests after both production and test compilation succeeded. Raw normalized output: `focused-red-mvn310-clean.log`.

## Focused green

- Same clean, offline JDK 25 / Maven 3.10.0 path, after the one-line production correction.
- Result: **8 tests run, 0 failures, 0 errors, 0 skipped**; `ValueTypeApplyTest` passed. Raw normalized output: `focused-green-mvn310-clean.log`.

## Previous runner attempt

An earlier attempt using the runner's former default Maven 3.9 path stopped in dependency resolution because cached Guava 33.7.2 metadata named an unavailable repository id. It ran no tests and is not the red result; its raw log is retained as `focused-red.log`.

## xacml-core module gate

- Clean, offline JDK 25 / Maven 3.10.0 `-pl xacml-core clean test`.
- Compilation: 287 production and 175 core test source files.
- Result: **7,365 tests run, 0 failures, 0 errors, 2 skipped**. `ValueTypeApplyTest` ran all 8 tests. Raw normalized output: `core-suite-green-mvn310-clean.log`.

## Scope and residual gates

The production change is limited to `xacml-core/src/main/java/org/xacml4j/v30/types/ValueType.java:165`; focused tests are in `xacml-core/src/test/java/org/xacml4j/v30/types/ValueTypeApplyTest.java`. `git diff --check` passed; license updates were skipped and `git status` showed no license-header churn.

The full reactor and conformance gate are owned by the conductor and remain unrun here. JMH is **UNRUN / not applicable**: the one-element array input previously failed, so there is no successful before-case to compare; this patch restores conversion correctness and makes no speedup claim. No POM, API, factory, or unrelated branch changed. The older untracked optimize-pdp-evaluation review evidence from the prior packet remains untouched.
