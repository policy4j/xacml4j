# 002: spine test with the harness (task 2.3)

Date: 2026-10-26. JDK: Oracle GraalVM 21.0.6+8.1. Maven 3.9.16, offline.
Sources: the harness API (`org.xacml4j.jmh.harness`), `InMemoryPolicyStore`,
`Rsa2008Corpus`, and `log4j2.xml`, on top of run 001.

Result: GREEN. `PdpFixtureTest`: 2 tests, 0 failures, 0 errors.

- `inMemoryStoreDecidesEveryRsa2008RequestAsExpected`: the fixture opens (it
  checks every lookup and decision itself); 9 policies, 17 requests, each with
  one result equal to the vendored response's decision; every lookup query
  finds its rule.
- `requestSelection`: `all` gives 17 requests, `01-01` one, `99-99` fails with
  a message that names it.

The test asserts exact equality of decisions; the RSA expectations are Permit
or Deny only, so the Indeterminate matching rule is not exercised here (the
tests lane covers it).
