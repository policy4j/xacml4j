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
- **THEN** every baseline URN is unchanged and the only added URN is `urn:oasis:names:tc:xacml:3.0:policy-combining-algorithm:on-permit-apply-second`

### Requirement: Discovery works without generated descriptors in tests
Algorithm and function providers discovered through `ServiceLoader` SHALL
be registered through `@AutoService` so that `META-INF/services` files are
generated at compile time. A test SHALL verify that the default providers
are discoverable through `ServiceLoader`.

#### Scenario: ServiceLoader discovery
- **WHEN** `ServiceLoader` loads `CombiningAlgorithmProvider`, `FunctionProvider`, and `MarshallingProvider`
- **THEN** each default implementation is found

### Requirement: on-permit-apply-second follows the profile decision table
The policy algorithm SHALL implement XACML 3.0 Additional Combining Algorithms
Profile Version 1.0 CS01 section 2.1. With two or three children it SHALL evaluate
the first child once. A first Permit SHALL select the second child. A first
NotApplicable, Deny or Indeterminate{D} SHALL yield NotApplicable for two children
or select the third child for three children. A first Indeterminate{P} or
Indeterminate{DP} SHALL yield Indeterminate{DP} without another child evaluation.
The implementation SHALL additionally normalize a legacy plain Indeterminate
first result to Indeterminate{DP}. Selecting a child SHALL return its decision;
an Indeterminate result SHALL retain the complete error status, if any, reported by the deciding child.
An arity other than two or three SHALL yield Indeterminate{DP} with processing-error
without creating or evaluating any child context.

#### Scenario: Permit selects the second child
- **GIVEN** two or three children whose first result is Permit
- **WHEN** the algorithm combines their decisions
- **THEN** only the first and second children are evaluated, in that order, and the second child's decision and any Indeterminate status are returned

#### Scenario: Two-child negative gate is NotApplicable
- **GIVEN** two children whose first result is NotApplicable, Deny or Indeterminate{D}
- **WHEN** the algorithm combines their decisions
- **THEN** only the first child is evaluated and the result is NotApplicable

#### Scenario: Three-child negative gate selects the third child
- **GIVEN** three children whose first result is NotApplicable, Deny or Indeterminate{D}
- **WHEN** the algorithm combines their decisions
- **THEN** only the first and third children are evaluated, in that order, and the third child's decision and any Indeterminate status are returned

#### Scenario: Potential Permit failure prevents further evaluation
- **GIVEN** the first child yields Indeterminate{P}, Indeterminate{DP} or legacy plain Indeterminate with an error status
- **WHEN** the algorithm combines two or three children
- **THEN** the result is Indeterminate{DP} with that complete status and no other child is evaluated

#### Scenario: Invalid arity is rejected before evaluation
- **GIVEN** a child count other than two or three
- **WHEN** the algorithm combines the children
- **THEN** the result is Indeterminate{DP} with processing-error and no child context is created or evaluated

### Requirement: Nested responses follow the final decision ancestry
Under XACML 3.0 Core section 7.18, an obligation or advice from a nested rule,
policy or policy set SHALL reach the final response only when its effect and
every enclosing decision agree with the final Permit or Deny. A composite
evaluating to NotApplicable or Indeterminate MUST NOT publish its child or own
responses to an enclosing policy set. An enclosed composite SHALL publish
matching child and own responses only after its final decision and successful
evaluation of its own response expressions. An extended-Indeterminate
evaluation pass MUST NOT publish responses to an enclosing policy set or the final response.

#### Scenario: Discarded Permit branch cannot contaminate a later sibling
- **GIVEN** an enclosed on-permit-apply-second policy set whose first child permits with responses and whose second child is NotApplicable
- **WHEN** an enclosing first-applicable policy set selects a later Permit sibling
- **THEN** only the sibling's matching obligations and advice are returned

#### Scenario: Discarded Deny branch cannot contaminate a later sibling
- **GIVEN** an enclosed two-child on-permit-apply-second policy set whose first child denies with responses
- **WHEN** an enclosing first-applicable policy set selects a later Deny sibling
- **THEN** only the sibling's matching obligations and advice are returned

#### Scenario: Matching nested parent and selected child responses survive
- **GIVEN** an enclosed on-permit-apply-second policy set selects a Permit child and has its own Permit obligation and advice
- **WHEN** its enclosing policy set returns Permit
- **THEN** the selected child's and nested parent's matching responses are returned and discarded Deny responses are absent

#### Scenario: Gate responses survive a matching final decision
- **GIVEN** the gate child and selected child both return Permit, or both return Deny in the three-child negative-gate branch
- **WHEN** the on-permit-apply-second policy set and its enclosing policy sets return that same decision
- **THEN** both evaluated children's matching obligations and advice are returned

#### Scenario: Failed own obligation prevents publication
- **GIVEN** an enclosed policy's children produce Permit responses and its own obligation expression fails
- **WHEN** the enclosed policy is evaluated
- **THEN** it yields Indeterminate and publishes none of the buffered responses
