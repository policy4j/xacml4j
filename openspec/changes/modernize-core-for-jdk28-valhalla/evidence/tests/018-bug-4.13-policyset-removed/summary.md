# policy4j-the.4.13: policy-set removal notification

Changed `xacml-pdp/src/main/java/org/xacml4j/v30/spi/repository/AbstractPolicyRepository.java`: `notifyPolicySetRemoved` now calls `listener.policySetRemoved(p)`.

Added `testAddRemovePolicySetNotifiesMatchingCallbacks` to `InMemoryPolicyRepositoryTest`. It adds and removes the same real `PolicySet`, waits for exactly two asynchronous listener callbacks through the existing latch bridge, verifies one `policySetAdded` and one `policySetRemoved`, then uses `verifyNoMoreInteractions`.

Red proof: `red.log` ran `InMemoryPolicyRepositoryTest` with 5 tests and failed only the new test (1 failure, 0 errors). Mockito reported two `policySetAdded` calls and no matching removal callback. After the one-line fix, `green-focused.log` reports 5 tests, 0 failures, 0 errors.

The other notification methods in `AbstractPolicyRepository` pair correctly: `notifyPolicyAdded` -> `policyAdded`, `notifyPolicySetAdded` -> `policySetAdded`, and `notifyPolicyRemoved` -> `policyRemoved` (source lines 167-185 in this lane copy).

The JDK check reports Maven 3.9.16 on Oracle Java 21.0.6. Full reactor gate `-pl xacml-pdp -am test`: core 763 tests, 0 failures, 0 errors, 2 skips; PDP 74 tests, 0 failures, 0 errors, 3 skips. Maven reports `BUILD SUCCESS`; full output is in `surefire-summary.txt`. All Maven commands included `-Dlicense.skipUpdateLicense=true`.
