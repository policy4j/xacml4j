## ADDED Requirements

### Requirement: Condition value follows its expression
A rule's `<Condition>` SHALL be True when it is absent or its expression
evaluates to True, False when its expression evaluates to False, and
Indeterminate when its expression evaluates to Indeterminate (XACML 3.0
§7.9).

#### Scenario: Absent condition
- **WHEN** a rule has no condition and its target matches
- **THEN** the rule's condition value is True

#### Scenario: Indeterminate expression
- **WHEN** the condition's expression evaluates to Indeterminate
- **THEN** the condition value is Indeterminate and the status of the expression is kept

### Requirement: A condition result is exactly one boolean
A condition expression SHALL be declared to return a single
`http://www.w3.org/2001/XMLSchema#boolean`; a policy with any other declared
type SHALL fail to load. If at runtime the expression yields zero values,
more than one value, or a non-boolean value, the condition value SHALL be
Indeterminate with status `processing-error`. The first value of a
multi-valued result SHALL NOT be used.

#### Scenario: Non-boolean declared type
- **WHEN** a policy's condition applies a function that returns a string
- **THEN** policy loading fails with a syntax error

#### Scenario: Empty runtime result
- **WHEN** a condition expression yields no value at runtime
- **THEN** the condition value is Indeterminate with status `processing-error`, not False

#### Scenario: Multi-valued runtime result
- **WHEN** a condition expression yields the values `true` and `false`
- **THEN** the condition value is Indeterminate with status `processing-error`

### Requirement: Single-value extraction is explicit
Code that needs exactly one value from an expression result SHALL use an
operation that checks the count (zero, one, or more) and fails for zero or
more than one. An operation that returns the first value SHALL NOT be used
where the XACML specification requires a single value.

#### Scenario: Exactly-one check
- **WHEN** the exactly-one operation is applied to a result with two values
- **THEN** it fails with an evaluation error instead of returning the first value
