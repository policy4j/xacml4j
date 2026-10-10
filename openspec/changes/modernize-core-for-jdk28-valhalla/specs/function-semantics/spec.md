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

### Requirement: Function arguments are checked when the expression is built
The number and the types of the arguments of an `<Apply>` SHALL be checked
against the function once, when the expression is built. A policy with an
`<Apply>` whose arguments do not fit SHALL still be loaded. The evaluation
of such an expression SHALL be Indeterminate with status code
`urn:oasis:names:tc:xacml:1.0:status:processing-error`; its function SHALL
NOT be invoked and its arguments SHALL NOT be evaluated. The status
message SHALL name the function and nothing else. A call without
arguments SHALL stay valid for a function that allows it. An expression
that holds an expression whose arguments do not fit, directly or through
variable references of any number, SHALL be treated in the same way, also
when its function would not evaluate that argument.

An expression whose data type is known only at evaluation SHALL have the
dynamic type: a single value, or a bag, of a data type that is not known
yet. This is the case for the bag of an `attribute-designator` or
`attribute-selector` function whose data-type argument is not a literal.
The dynamic type SHALL be consistent with every data type of the same
shape, a single value with a single value and a bag with a bag, and with
nothing else. When the expression is built, an argument of dynamic type
SHALL be accepted where its parameter needs a type that it is consistent
with. Everything else SHALL be checked as for any other argument: the
shape, the other arguments, and the number of arguments. When an argument
of dynamic type is evaluated, its value SHALL be checked against its
parameter before the function is invoked; a value that does not fit SHALL
make the expression Indeterminate with status code
`urn:oasis:names:tc:xacml:1.0:status:processing-error`. An argument that
is not evaluated is not checked. A policy or a request SHALL NOT be able
to name the dynamic type as a data type, and no value SHALL have it.

A data-type argument that is a literal and names no data type SHALL make
the expression that takes the bag a type error. A check of the arguments
that fails for another reason than a mismatch SHALL be treated as a
mismatch.

#### Scenario: Fewer arguments than the function needs
- **WHEN** a condition holds `integer-add` with the single argument 5
- **THEN** the expression is Indeterminate with status `urn:oasis:names:tc:xacml:1.0:status:processing-error`, and not the value 5

#### Scenario: More arguments than the function takes
- **WHEN** an expression is `string-equal` with three string arguments
- **THEN** the expression is Indeterminate with status `urn:oasis:names:tc:xacml:1.0:status:processing-error`

#### Scenario: An argument of a function without parameters
- **WHEN** an expression applies a function that has no parameters to one argument
- **THEN** the expression is Indeterminate with status `urn:oasis:names:tc:xacml:1.0:status:processing-error`, and the function is not invoked

#### Scenario: An argument of another type
- **WHEN** an expression is `integer-add` with an integer and a string (conformance test IIC014)
- **THEN** the policy is loaded and the expression is Indeterminate with status `urn:oasis:names:tc:xacml:1.0:status:processing-error`

#### Scenario: A bag where a value is needed
- **WHEN** an expression is `string-equal` with a string and a bag of strings (conformance test IIC003)
- **THEN** the policy is loaded and the expression is Indeterminate with status `urn:oasis:names:tc:xacml:1.0:status:processing-error`

#### Scenario: A bag of another type
- **WHEN** an expression is `integer-bag-size` with a bag of strings
- **THEN** the expression is Indeterminate with status `urn:oasis:names:tc:xacml:1.0:status:processing-error`, and not the size of the bag

#### Scenario: A rule with such a condition
- **WHEN** a Permit rule has the condition `integer-equal(integer-add(5), 5)`
- **THEN** the rule is Indeterminate{P} with status `urn:oasis:names:tc:xacml:1.0:status:processing-error`

#### Scenario: A type error behind a deciding operand
- **WHEN** a Permit rule has the condition `or(true, string-equal(a, string-bag(a)))`
- **THEN** the rule is Indeterminate{P} with status `urn:oasis:names:tc:xacml:1.0:status:processing-error`, and not Permit

#### Scenario: A type error in a variable definition
- **WHEN** an expression is `or` of true and a reference to a variable whose definition is `string-equal` with a string and a bag
- **THEN** the expression is Indeterminate with status `urn:oasis:names:tc:xacml:1.0:status:processing-error`

#### Scenario: A call without arguments that the function allows
- **WHEN** an expression is `or` without arguments
- **THEN** the expression is valid and evaluates to false

#### Scenario: A dynamic bag of the right data type
- **WHEN** `string-bag-size` is applied to the bag of an `attribute-designator` function whose data-type argument is an expression that gives the string type
- **THEN** the policy is loaded and the expression evaluates to the size of the bag

#### Scenario: A dynamic bag of another data type
- **WHEN** `integer-bag-size` is applied to the bag of an `attribute-designator` function whose data-type argument is an expression that gives the string type
- **THEN** the policy is loaded, and the expression is Indeterminate with status `urn:oasis:names:tc:xacml:1.0:status:processing-error` when it is evaluated, and not the size of the bag

#### Scenario: A dynamic bag where a single value is needed
- **WHEN** `string-equal` is applied to a string and to the bag of an `attribute-designator` function whose data-type argument is an expression
- **THEN** the expression is a type error when it is built: it is Indeterminate with status `urn:oasis:names:tc:xacml:1.0:status:processing-error`, and no argument is evaluated

#### Scenario: A wrong operand next to a dynamic bag
- **WHEN** a Permit rule has the condition `or(true, any-of(string-equal, 1, bag))`, and `bag` is the bag of an `attribute-designator` function whose data-type argument is an expression
- **THEN** the rule is Indeterminate{P} with status `urn:oasis:names:tc:xacml:1.0:status:processing-error`, and not Permit

#### Scenario: A dynamic operand behind a deciding operand
- **WHEN** `or` is applied to true and to `integer-equal(integer-bag-size(bag), 0)`, and `bag` is a dynamic bag that holds strings at evaluation
- **THEN** the result is true and the second operand is not evaluated

#### Scenario: The data type of a designator function does not exist
- **WHEN** `string-is-in` is applied to a string and to the bag of an `attribute-designator` function whose data-type argument is a literal that names no data type
- **THEN** the expression is Indeterminate with status `urn:oasis:names:tc:xacml:1.0:status:processing-error`

#### Scenario: A type error behind many variable references
- **WHEN** a Permit rule has the condition `or` of true and a variable reference, and the type error is 65 variable definitions away
- **THEN** the rule is Indeterminate{P} with status `urn:oasis:names:tc:xacml:1.0:status:processing-error`

#### Scenario: A failure of the evaluation behind a deciding operand
- **WHEN** `or` is applied to true and to `boolean-one-and-only` of an empty bag, which is type correct and fails when it is evaluated
- **THEN** the result is true and the second argument is not evaluated

### Requirement: Higher-order functions with two bags check their operands
`all-of-any`, `any-of-all` and `all-of-all` SHALL check that the data types
of their two bags fit the two parameters of the function they are given and
that this function returns a boolean. The check SHALL be made when the
expression is built, as for the other higher-order functions, and again
with the evaluated bags when the function is invoked, also when a bag is
empty.

#### Scenario: A bag of another data type
- **WHEN** an expression is `all-of-all(string-equal, integer-bag(1), string-bag(a))`
- **THEN** the expression is a type error when it is built: it is Indeterminate with status `urn:oasis:names:tc:xacml:1.0:status:processing-error`

#### Scenario: A dynamic bag that is empty and of another data type
- **WHEN** `all-of-all` is applied to `string-equal`, to a dynamic bag that is an empty bag of integers at evaluation, and to a bag of strings
- **THEN** the expression is Indeterminate, and not true

#### Scenario: Bags that fit
- **WHEN** an expression is `all-of-all(string-equal, string-bag(a), string-bag(a))`
- **THEN** the expression evaluates to true
