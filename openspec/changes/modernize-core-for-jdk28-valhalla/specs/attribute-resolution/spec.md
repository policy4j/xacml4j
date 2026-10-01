## ADDED Requirements

### Requirement: Designators and selectors evaluate to bags
An `AttributeDesignator` and an `AttributeSelector` SHALL declare a bag
result type (the bag type of their data type) and SHALL evaluate to a bag
of values of that data type (XACML 3.0 §5.29, §5.30, §7.3.5). Function
parameter validation SHALL accept a designator or selector wherever a bag of
that data type is expected and SHALL reject it where a single value is
expected.

#### Scenario: Designator result type is a bag
- **WHEN** a designator for data type `http://www.w3.org/2001/XMLSchema#string` is built
- **THEN** its declared result type is the string bag type, not the string type

#### Scenario: Designator accepted as a bag argument
- **WHEN** a policy applies `string-one-and-only` to a string designator
- **THEN** the policy loads and the function receives the resolved bag

#### Scenario: Designator rejected as a single-value argument
- **WHEN** a policy applies `string-equal` directly to a string designator
- **THEN** policy loading fails with a syntax error naming the parameter

### Requirement: Missing attributes follow MustBePresent
When no request attribute matches a designator or selector, the result
SHALL be an empty bag of the declared data type if `MustBePresent` is false,
and SHALL be Indeterminate with status code
`urn:oasis:names:tc:xacml:1.0:status:missing-attribute` if `MustBePresent`
is true (XACML 3.0 §7.3.5, §7.19.3). Designators and selectors SHALL use the
same status code for this case.

#### Scenario: Missing attribute, MustBePresent false
- **WHEN** a designator with `MustBePresent="false"` finds no matching attribute
- **THEN** it returns an empty bag of its data type

#### Scenario: Missing attribute, MustBePresent true
- **WHEN** a designator with `MustBePresent="true"` finds no matching attribute
- **THEN** evaluation is Indeterminate with status `urn:oasis:names:tc:xacml:1.0:status:missing-attribute`

#### Scenario: Selector matches designator status behavior
- **WHEN** a selector with `MustBePresent="true"` selects no nodes
- **THEN** evaluation is Indeterminate with the same missing-attribute status code

### Requirement: Resolver failures are processing errors
A failure of an attribute resolver (PIP) or content resolver while
resolving a designator or selector SHALL make the evaluation Indeterminate
with status code `urn:oasis:names:tc:xacml:1.0:status:processing-error`,
regardless of `MustBePresent`. It SHALL NOT be converted to an empty bag.

#### Scenario: PIP throws with MustBePresent false
- **WHEN** the attribute resolver throws while resolving a designator with `MustBePresent="false"`
- **THEN** evaluation is Indeterminate with status `urn:oasis:names:tc:xacml:1.0:status:processing-error`

### Requirement: Issuer matching
A designator without an `Issuer` SHALL match request attributes from any
issuer. A designator with an `Issuer` SHALL match only request attributes
with exactly that issuer. Attributes that share an attribute id but have
different issuers SHALL be stored and resolved independently.

#### Scenario: Unspecified issuer matches every issuer
- **WHEN** the request holds attribute `role` from issuers `A` and `B` and the designator has no issuer
- **THEN** the bag contains the values from both issuers

#### Scenario: Specified issuer filters
- **WHEN** the request holds attribute `role` from issuers `A` and `B` and the designator requests issuer `A`
- **THEN** the bag contains only the values from issuer `A`

### Requirement: Attribute-designator function resolves by entity or category URI
The `attribute-designator` and `attribute-selector` functions SHALL accept
either an entity value or a category URI as the first argument. For an
entity, attributes SHALL be resolved from that entity. For a category URI,
attributes SHALL be resolved from the request category with that URI
through the evaluation context. Any other argument type SHALL make the
evaluation Indeterminate.

#### Scenario: Category URI argument
- **WHEN** `attribute-designator` is called with category URI `urn:oasis:names:tc:xacml:3.0:attribute-category:resource`, an attribute id, and a data type
- **THEN** it returns the matching values from the resource category of the request

#### Scenario: Entity argument
- **WHEN** `attribute-designator` is called with an entity value that contains the attribute
- **THEN** it returns the matching values from that entity

### Requirement: Bag multiplicity is preserved
Attribute values returned by designators and selectors SHALL keep
duplicate values; only the set functions remove duplicates.

#### Scenario: Duplicate attribute values
- **WHEN** a request attribute carries the value `"a"` twice
- **THEN** the designator returns a bag of size 2
