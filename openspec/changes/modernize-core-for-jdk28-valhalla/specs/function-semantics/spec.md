## ADDED Requirements

### Requirement: Integer division and modulo
`integer-divide` SHALL return an `xs:integer` (XACML 3.0 §A.3.2) equal to
the quotient truncated toward zero. The truncation direction is the
project's integer-arithmetic interpretation, not an explicit rule in §A.3.2;
the §7.5 rounding ambiguity is recorded in task 4.6 evidence. `integer-mod` SHALL return the
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

### Requirement: Integer arithmetic does not wrap
`integer-add`, `integer-subtract`, `integer-multiply`, `integer-divide`,
`integer-mod`, and `integer-abs` SHALL compute on 64-bit signed integers.
When the mathematical result is less than -2^63 or greater than 2^63 - 1,
the function SHALL evaluate to Indeterminate with status code
`urn:oasis:names:tc:xacml:1.0:status:processing-error`; it SHALL NOT return
a wrapped value. For a function with more than two arguments only the
result counts: an intermediate value outside the range SHALL NOT make the
function Indeterminate when the result is inside the range. The status
message SHALL name the function and SHALL NOT contain the operands.

#### Scenario: Sum above the largest integer
- **WHEN** `integer-add` is applied to `9223372036854775807` and `1`
- **THEN** evaluation is Indeterminate with status `urn:oasis:names:tc:xacml:1.0:status:processing-error`

#### Scenario: Absolute value of the smallest integer
- **WHEN** `integer-abs` is applied to `-9223372036854775808`
- **THEN** evaluation is Indeterminate with status `urn:oasis:names:tc:xacml:1.0:status:processing-error`

#### Scenario: Smallest integer divided by minus one
- **WHEN** `integer-divide` is applied to `-9223372036854775808` and `-1`
- **THEN** evaluation is Indeterminate with status `urn:oasis:names:tc:xacml:1.0:status:processing-error`

#### Scenario: Intermediate value outside the range
- **WHEN** `integer-add` is applied to `9223372036854775807`, `1`, and `-1`
- **THEN** the result is `9223372036854775807`

#### Scenario: Result at the edge of the range
- **WHEN** `integer-multiply` is applied to `4611686018427387904` and `-2`
- **THEN** the result is `-9223372036854775808`

### Requirement: Double equality follows IEEE 754
`double-equal` and double comparisons SHALL follow IEEE 754 (XACML 3.0
§A.3.1): NaN SHALL NOT equal any value, including NaN, and `0.0` SHALL
equal `-0.0`.

#### Scenario: NaN is not equal to NaN
- **WHEN** `double-equal` is applied to `NaN` and `NaN`
- **THEN** the result is `false`

### Requirement: Rounding and conversion of special double values
`round` SHALL round to the nearest integral double as IEEE 754
round-to-nearest does: `NaN` SHALL stay `NaN`, an infinity SHALL stay that
infinity, and a tie SHALL go to the even integer. `floor` SHALL also keep
`NaN` and the infinities. `double-to-integer` SHALL evaluate to
Indeterminate with status `processing-error` for `NaN`, for an infinity,
and for a finite value outside the `xs:integer` range of the engine.

#### Scenario: Round of NaN
- **WHEN** `round` is applied to `NaN`
- **THEN** the result is `NaN`

#### Scenario: Round of a tie
- **WHEN** `round` is applied to `0.5`, `1.5`, and `2.5`
- **THEN** the results are `0.0`, `2.0`, and `2.0`

#### Scenario: Conversion of NaN or an infinity
- **WHEN** `double-to-integer` is applied to `NaN`, `INF`, or `-INF`
- **THEN** evaluation is Indeterminate with status `processing-error`

### Requirement: x500Name-match compares RDN sequences
`x500Name-match` SHALL return `true` if and only if the relative
distinguished names (RDNs) of the first argument are a terminal sequence of
the RDNs of the second argument, with each pair of RDNs compared as
`x500Name-equal` compares names. A multi-valued RDN SHALL match only an
equal multi-valued RDN; a string suffix of the name SHALL NOT be enough.

#### Scenario: Terminal sequence
- **WHEN** `x500Name-match` is applied to `O=Medico Corp,C=US` and `cn=John Smith,o=Medico Corp,c=US`
- **THEN** the result is `true`

#### Scenario: Part of a multi-valued RDN
- **WHEN** `x500Name-match` is applied to `OU=Engineering,C=US` and `O=Acme+OU=Engineering,C=US`
- **THEN** the result is `false`

### Requirement: ipAddress set functions are extensions
The functions `ipAddress-intersection`, `ipAddress-union`,
`ipAddress-subset`, `ipAddress-at-least-one-member-of`, and
`ipAddress-set-equals` under `urn:oasis:names:tc:xacml:1.0:function:` SHALL
stay registered as engine extensions. They are not standard XACML functions.

#### Scenario: Extension lookup
- **WHEN** a policy refers to `urn:oasis:names:tc:xacml:1.0:function:ipAddress-union`
- **THEN** the function resolves

### Requirement: A deciding argument wins over a failed argument
The `or`, `and`, and `n-of` functions SHALL evaluate their arguments one by
one, from first to last, and SHALL stop when the result is decided. `or`
SHALL return true when an argument evaluates to true, `and` SHALL return
false when an argument evaluates to false, and `n-of` SHALL return true when
the required number of arguments evaluate to true, even when an argument
before the deciding one could not be evaluated. `n-of` SHALL return false
when the required number cannot be reached even if every argument that
failed were true. When no argument decides the result and at least one
argument could not be evaluated, the result SHALL be Indeterminate with the
status of the first argument that failed. `or` without arguments SHALL
return false, and `and` without arguments SHALL return true.

The higher-order functions SHALL combine the results of their predicate in
the same way: `any-of` and `any-of-any` as `or`; `all-of` and `all-of-all`
as `and`; `all-of-any` as `and` over the `any-of` result for each value of
the first bag; `any-of-all` as `and` over the `any-of` result for each value
of the second bag.

#### Scenario: or with a failed argument before a true argument
- **WHEN** `or` is evaluated, its first argument fails, and its second argument evaluates to true
- **THEN** the result is true

#### Scenario: or stops at the first true argument
- **WHEN** the first argument of `or` evaluates to true and the second argument would fail
- **THEN** the result is true and the second argument is not evaluated

#### Scenario: and with a failed argument before a false argument
- **WHEN** `and` is evaluated, its first argument fails, and its second argument evaluates to false
- **THEN** the result is false

#### Scenario: No argument decides
- **WHEN** `or` is evaluated, one argument fails, and every other argument evaluates to false
- **THEN** the result is Indeterminate with the status of the argument that failed

#### Scenario: n-of with a failed argument
- **WHEN** `n-of` is evaluated with 2 as its first argument and three more arguments, and the first of them fails
- **THEN** the result is true when the other two are true, false when the other two are false, and Indeterminate when one is true and one is false

#### Scenario: any-of with a predicate that fails for one value
- **WHEN** `any-of` applies a predicate that fails for the first value of the bag and returns true for a later value
- **THEN** the result is true

#### Scenario: all-of-all with a predicate that fails for one pair
- **WHEN** `all-of-all` applies a predicate that fails for one pair of values and returns false for a later pair
- **THEN** the result is false

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

### Requirement: XPath evaluation errors and missing content
When all referenced categories have `Content` and an XPath expression of `xpath-node-count`, `xpath-node-equal`, or
`xpath-node-match` cannot be evaluated because of an invalid expression or
an evaluation failure, the function SHALL evaluate to Indeterminate.
Missing category `Content` SHALL instead yield `0` for `xpath-node-count`
and `false` for `xpath-node-equal` and `xpath-node-match`, as required by
XACML 3.0 §A.3.15.

#### Scenario: Invalid XPath expression
- **GIVEN** the referenced category has `Content`
- **WHEN** `xpath-node-count` is applied to a syntactically invalid XPath expression
- **THEN** evaluation is Indeterminate

#### Scenario: Missing content
- **WHEN** `xpath-node-match` refers to a category with no `Content`
- **THEN** the result is `false`

#### Scenario: Missing content takes precedence over expression evaluation
- **GIVEN** one argument of `xpath-node-equal` or `xpath-node-match` refers to a category with no `Content`
- **WHEN** the other argument has an expression that would fail evaluation
- **THEN** the result is `false` without evaluating the other expression

#### Scenario: Missing content for node count
- **WHEN** `xpath-node-count` refers to a category with no `Content`
- **THEN** the result is the integer `0`

#### Scenario: Missing content for node equality
- **WHEN** either `xpath-node-equal` argument refers to a category with no `Content`
- **THEN** the result is `false`
