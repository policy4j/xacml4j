## ADDED Requirements

### Requirement: Every implemented algorithm resolves by URN
The default combining algorithm provider SHALL resolve, by URN, every rule-
and policy-combining algorithm the library implements, including the XACML
3.0 algorithms, the legacy 1.0/1.1 algorithms, and
`urn:oasis:names:tc:xacml:3.0:policy-combining-algorithm:on-permit-apply-second`.

#### Scenario: on-permit-apply-second by URN
- **WHEN** the default provider is asked for `urn:oasis:names:tc:xacml:3.0:policy-combining-algorithm:on-permit-apply-second`
- **THEN** it returns the implementation, not null

#### Scenario: Registry completeness
- **WHEN** the set of combining algorithm classes in `xacml-core` is compared with the URNs the default provider resolves
- **THEN** every class has a resolvable URN

### Requirement: Algorithm URNs are stable across renames
Renaming combining algorithm classes SHALL NOT change the URN of any
algorithm. Each algorithm's URN SHALL match the identifier defined by the
XACML specification or profile that defines it.

#### Scenario: URN unchanged after rename
- **WHEN** the URNs registered by the default provider are compared with those registered at `a8a90d46`
- **THEN** the sets are identical

### Requirement: Discovery works without generated descriptors in tests
Algorithm and function providers discovered through `ServiceLoader` SHALL
be registered through `@AutoService` so that `META-INF/services` files are
generated at compile time. A test SHALL verify that the default providers
are discoverable through `ServiceLoader`.

#### Scenario: ServiceLoader discovery
- **WHEN** `ServiceLoader` loads `CombiningAlgorithmProvider`, `FunctionProvider`, and `MarshallingProvider`
- **THEN** each default implementation is found
