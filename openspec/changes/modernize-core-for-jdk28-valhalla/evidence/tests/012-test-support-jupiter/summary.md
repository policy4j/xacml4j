# Task 2.5 — xacml-test JUnit 5 migration

## Result

Migrated the xacml-test main support API and tests to Jupiter. Replaced the JUnit 4 compile dependency with compile-scope `junit-jupiter-api`; no JUnit 4 imports remain in `xacml-test/src/main` or `src/test`.

## Test counts

| Class | Before @Test methods | After @Test methods | After Surefire |
| --- | ---: | ---: | --- |
| `ExpectedAttributeResolverBuilderTest` | 2 | 2 | 2 run, 0 failures, 0 errors, 0 skipped |
| `Xacml30SamplePolicyTest` | 2 | 2 | 2 run, 0 failures, 0 errors, 0 skipped |
| `AttributeResolverTestSupportTest` | 1 | 1 | 1 run, 0 failures, 0 errors, 0 skipped |
| `Xacml30TestUtilityTest` | 14 | 14 | 14 run, 0 failures, 0 errors, 0 skipped |
| `Xacml20SamplePolicyTest` | 1 | 1 | 1 run, 0 failures, 0 errors, 0 skipped |
| **Module total** | **20** | **20** | **20 run, 0 failures, 0 errors, 0 skipped** |

The `Xacml30TestUtility` assertions, including the previously repaired comparison assertions, remain present. Its test class still contains all 14 methods.

## Mock and license review

`ExpectedAttributeResolverBuilderTest` is the only EasyMock-to-Mockito conversion in this lane. The clock stub is `when(context.getClock()).thenReturn(...)`; after the resolver call and result assertions, `verify(context).getClock()` explicitly verifies the recorded interaction. No captors, strict mocks, assertThrows lambdas containing verifications, duplicate consecutive verifications, or unverified RECORD-phase calls remain in the owned files.

Compared all LGPL header blocks in the owned Java sources with the `.base` tree: 10 blocks checked for this module, zero mismatches, zero duplicate blocks. Six removed headers were restored byte-for-byte from `.base` and placed after their package declarations.

## Verification

The JDK 21 `-pl xacml-test -am test` reactor run completed successfully. xacml-test reported 20 tests, 0 failures, 0 errors, 0 skips. The run used `-Dmaven.test.failure.ignore=true` because the not-yet-migrated xacml-core lane reports its known EasyMock class-mock errors; the xacml-test module itself was green. The log is `jdk21-test.log`.

The scoped OpenRewrite dry run and real run used plugin 6.22.1 and recipe artifact `org.openrewrite.recipe:rewrite-testing-frameworks:3.20.0`; the scoped dry-run patch listed only xacml-test files. Logs: `rewrite-dry-run-scoped.log` and `rewrite-run.log`.
