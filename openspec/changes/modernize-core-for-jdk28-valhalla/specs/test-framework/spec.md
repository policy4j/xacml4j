## ADDED Requirements

### Requirement: JUnit 5 and Mockito are the only test stack
All tests in all modules SHALL use JUnit 5 (Jupiter) and, where test doubles
are needed, Mockito. The build SHALL NOT depend on `junit:junit`,
`junit-vintage-engine`, or EasyMock, and no source file SHALL import
`org.junit.Test`, `org.junit.Assert`, `org.junit.Before*`,
`org.junit.After*`, `org.junit.Rule`, `org.junit.Ignore`,
`junit.framework`, or `org.easymock`.

#### Scenario: Legacy import check
- **WHEN** the repository is searched for JUnit 4 or EasyMock imports
- **THEN** there are no matches

#### Scenario: Dependency tree check
- **WHEN** the reactor dependency tree is listed
- **THEN** it contains no `junit:junit`, `junit-vintage-engine`, or `org.easymock` artifact

### Requirement: Test-support API uses Jupiter
The `xacml-test` module's public support classes SHALL expose JUnit 5
(Jupiter) lifecycle annotations and assertions, and SHALL declare
`junit-jupiter-api` as a compile-scope dependency so that consumers compile
against it.

#### Scenario: Inherited lifecycle
- **WHEN** a test class extends `XacmlPolicyTestSupport`
- **THEN** the support class's `@BeforeEach` setup runs before each test

### Requirement: Value and model objects are not mocked
Tests SHALL use real instances of value types, identifiers, records, bags,
and policy model classes (for example `Match`, `Target`, `Condition`,
`Policy`, `Rule`). Mocks SHALL be used only for interfaces at collaboration
boundaries (for example evaluation context, attribute resolvers, policy
repositories, content providers).

#### Scenario: No mocks of value types
- **WHEN** test sources are searched for `mock(` calls whose type is a record, a sealed value type, or a policy model class
- **THEN** there are no matches

### Requirement: Migration does not weaken tests
Converting a test SHALL keep every assertion and every verified
interaction. Ordered EasyMock expectations SHALL become Mockito `InOrder`
verification where the order is under test, and EasyMock captures SHALL
become `ArgumentCaptor` checks on the captured values.

#### Scenario: Strict mock conversion
- **WHEN** a test that used an EasyMock strict mock is converted
- **THEN** the converted test verifies the same calls, in order where the original test relied on order

#### Scenario: Capture conversion
- **WHEN** a test asserted on an EasyMock `Capture` value
- **THEN** the converted test asserts on the corresponding `ArgumentCaptor` value

### Requirement: Test discovery parity
After migration, each module SHALL run at least as many test methods as it
ran before migration on the same code, and no previously enabled test SHALL
be disabled without a written reason.

#### Scenario: Count comparison
- **WHEN** Surefire reports before and after migration are compared per module
- **THEN** the number of executed test methods has not decreased
