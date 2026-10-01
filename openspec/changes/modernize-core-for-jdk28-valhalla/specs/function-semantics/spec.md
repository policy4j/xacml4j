## ADDED Requirements

### Requirement: Integer division and modulo
`integer-divide` SHALL return an `xs:integer` equal to the quotient
truncated toward zero (XACML 3.0 §A.3.2). `integer-mod` SHALL return the
remainder of that truncating division, with the sign of the dividend. A
zero divisor SHALL make either function evaluate to Indeterminate.

#### Scenario: Truncating quotient
- **WHEN** `integer-divide` is applied to `5` and `2`
- **THEN** the result is the integer `2`

#### Scenario: Negative remainder
- **WHEN** `integer-mod` is applied to `-5` and `3`
- **THEN** the result is `-2`

#### Scenario: Division by zero
- **WHEN** `integer-divide` or `integer-mod` is applied with divisor `0`
- **THEN** evaluation is Indeterminate

### Requirement: Multiplication identity
`integer-multiply` and `double-multiply` SHALL accept two or more arguments
and SHALL compute the product of all of them.

#### Scenario: Three-argument product
- **WHEN** `integer-multiply` is applied to `2`, `3`, and `4`
- **THEN** the result is `24`

### Requirement: Double equality follows IEEE 754
`double-equal` and double comparisons SHALL follow IEEE 754 (XACML 3.0
§A.3.1): NaN SHALL NOT equal any value, including NaN, and `0.0` SHALL
equal `-0.0`.

#### Scenario: NaN is not equal to NaN
- **WHEN** `double-equal` is applied to `NaN` and `NaN`
- **THEN** the result is `false`

### Requirement: Set functions remove duplicates
`type-union` (two or more bags) and `type-intersection` SHALL return bags
that contain no duplicates as determined by `type-equal` (XACML 3.0
§A.3.11). `type-subset` and `type-set-equals` SHALL ignore duplicates.
Bag functions that are not set functions SHALL preserve duplicates.

#### Scenario: Union removes duplicates
- **WHEN** `string-union` is applied to bags `["a","a","b"]` and `["b","c"]`
- **THEN** the result contains exactly `a`, `b`, and `c`

#### Scenario: Union of three bags
- **WHEN** `string-union` is applied to three bags
- **THEN** the result contains every distinct value of all three bags

#### Scenario: Bag function keeps duplicates
- **WHEN** `string-bag-size` is applied to `["a","a"]`
- **THEN** the result is `2`

### Requirement: One-and-only requires exactly one value
`type-one-and-only` SHALL return the single value of its bag argument and
SHALL evaluate to Indeterminate when the bag has zero or more than one
value (XACML 3.0 §A.3.10).

#### Scenario: Empty bag
- **WHEN** `string-one-and-only` is applied to an empty bag
- **THEN** evaluation is Indeterminate

#### Scenario: Two values
- **WHEN** `string-one-and-only` is applied to `["a","b"]`
- **THEN** evaluation is Indeterminate

### Requirement: Mixed-type bag arguments are errors
When a bag or set function receives bags whose element data types differ
from the function's declared type, the evaluation SHALL be Indeterminate
with status `processing-error`; it SHALL NOT return `false`.

#### Scenario: Mismatched bag types
- **WHEN** a set function declared for strings receives a bag of integers at runtime
- **THEN** evaluation is Indeterminate with status `processing-error`

### Requirement: Date and time duration arithmetic
`dateTime-subtract-dayTimeDuration` and
`dateTime-subtract-yearMonthDuration` SHALL subtract a positive duration and
SHALL add the magnitude of a negative duration (XACML 3.0 §A.3.7);
`dateTime-add-*` SHALL do the reverse.

#### Scenario: Subtract a positive duration
- **WHEN** `dateTime-subtract-dayTimeDuration` is applied to `2002-03-22T08:23:47-05:00` and `P5DT2H0M0S`
- **THEN** the result is `2002-03-17T06:23:47-05:00`

#### Scenario: Subtract a negative duration
- **WHEN** `dateTime-subtract-dayTimeDuration` is applied to `2002-03-22T08:23:47-05:00` and `-P5DT2H0M0S`
- **THEN** the result is `2002-03-27T10:23:47-05:00`

### Requirement: XPath function errors are Indeterminate
When the content or XPath expression of `xpath-node-count`,
`xpath-node-equal`, or `xpath-node-match` cannot be resolved or evaluated,
the function SHALL evaluate to Indeterminate; it SHALL NOT return `0` or
`false`.

#### Scenario: Invalid XPath expression
- **WHEN** `xpath-node-count` is applied to a syntactically invalid XPath expression
- **THEN** evaluation is Indeterminate

#### Scenario: Missing content
- **WHEN** `xpath-node-match` refers to a category with no `Content`
- **THEN** evaluation is Indeterminate
