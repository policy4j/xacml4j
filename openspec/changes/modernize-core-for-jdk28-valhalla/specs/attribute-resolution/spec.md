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
regardless of `MustBePresent`. It SHALL NOT be converted to an empty bag or
to a missing-attribute status. When several resolvers match a reference,
they SHALL be asked in registration order until one yields a value for the
reference, and that value SHALL be used. A resolver that returns nothing,
that returns attributes without the requested attribute, or that returns
content in which the selector selects no node has not yielded a value.
When no resolver yields a value and at least one resolver fails, the
evaluation SHALL be Indeterminate with status processing-error.

An XPath evaluation error of a selector that is not a syntax error SHALL
also make the evaluation Indeterminate with status processing-error,
regardless of `MustBePresent`. An XPath syntax error that the engine finds
SHALL keep status `urn:oasis:names:tc:xacml:1.0:status:syntax-error`. Status
missing-attribute SHALL be used only when the selection or lookup succeeds
and yields no value.

A resolver or a policy information point SHALL NOT be able to choose the
status of its failure: every exception from it SHALL be reported with status
processing-error, whatever status the exception carries. The status message
of a processing-error or syntax-error caused by attribute resolution SHALL
be a fixed text of the engine. It SHALL NOT contain text from a resolver, a
back end, or the XPath engine, because the status message is returned in the
response.

#### Scenario: PIP throws with MustBePresent false
- **WHEN** the attribute resolver throws while resolving a designator with `MustBePresent="false"`
- **THEN** evaluation is Indeterminate with status `urn:oasis:names:tc:xacml:1.0:status:processing-error`

#### Scenario: PIP throws with MustBePresent true
- **WHEN** the attribute resolver throws while resolving a designator with `MustBePresent="true"`
- **THEN** evaluation is Indeterminate with status `urn:oasis:names:tc:xacml:1.0:status:processing-error`, not missing-attribute

#### Scenario: Deny rule keeps applying when its resolver fails
- **WHEN** the condition of a Deny rule uses a designator whose resolver throws
- **THEN** the rule evaluates to Indeterminate{D}, not NotApplicable

#### Scenario: A failed resolver and a resolver with a value
- **WHEN** two resolvers match a designator, one throws, and the other returns a value
- **THEN** the designator evaluates to a bag with that value

#### Scenario: A failed resolver and a resolver without a value
- **WHEN** two resolvers match a designator, one throws, and the other returns no value
- **THEN** evaluation is Indeterminate with status `urn:oasis:names:tc:xacml:1.0:status:processing-error`

#### Scenario: XPath evaluation error in a selector
- **WHEN** the XPath of a selector fails to evaluate for a reason other than its syntax
- **THEN** evaluation is Indeterminate with status `urn:oasis:names:tc:xacml:1.0:status:processing-error` for `MustBePresent` true and false

#### Scenario: A failed resolver and a resolver without the requested attribute
- **WHEN** two resolvers match a designator, one throws, and the other returns attributes that do not include the requested attribute
- **THEN** evaluation is Indeterminate with status `urn:oasis:names:tc:xacml:1.0:status:processing-error`

#### Scenario: A failed content resolver and content without the selected node
- **WHEN** two content resolvers match a selector, one throws, and the selector selects no node in the content of the other
- **THEN** evaluation is Indeterminate with status `urn:oasis:names:tc:xacml:1.0:status:processing-error`, in either order of the two resolvers

#### Scenario: A resolver chooses a status and a message
- **WHEN** a resolver or a policy information point throws an exception that carries status syntax-error or missing-attribute and a message with back-end details
- **THEN** evaluation is Indeterminate with status `urn:oasis:names:tc:xacml:1.0:status:processing-error`, and the status message in the response is the fixed text of the engine

#### Scenario: Syntax error status message
- **WHEN** the engine finds a syntax error while it evaluates the path of a selector
- **THEN** evaluation is Indeterminate with status `urn:oasis:names:tc:xacml:1.0:status:syntax-error`, and the status message in the response is the fixed text of the engine

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
