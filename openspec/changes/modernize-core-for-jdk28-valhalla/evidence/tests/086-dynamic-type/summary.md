# Run 086: a data type that is known only at evaluation is the dynamic type (bead the.4.61)

Date: 2026-10-09. Follow-up of run 083 (bead the.4.56). JDK: Oracle
GraalVM 21.0.6+8.1 (`java version "21.0.6" 2025-01-21 LTS`). Review:
`reviews/code/rv093-dynamic-type-codex`.

## Decision basis

The maintainer asked for a `DynamicType` in place of rule 2 of run 083,
with research on how other expression evaluators do it, and approved the
plan of the session. Before that the maintainer had said that new code
should not return null (the stand-in class of run 083).

## Normative text

XACML v3.0 Related and Nested Entities Profile v1.0, Committee
Specification 03, 30 January 2024 (excerpt, URL and checksum in
`oasis-related-entities-cs03-6.1-6.2.txt`):

- Section 6.1, the attribute-designator function: "The third argument
  SHALL be of data-type http://www.w3.org/2001/XMLSchema#anyURI. The value
  of the argument specifies the value of the DataType XML attribute of the
  XACML attribute values returned by the function."
- Section 6.2, the attribute-selector function: "The value of the argument
  specifies the data-type of the attribute values returned from the
  evaluation of this function."

The data type of the returned bag is the value of an argument. The profile
does not ask for a literal. XACML 3.0 core, sections 7.4 and 7.19.2, as in
run 083.

## Defect

Run 083 made an argument whose type can not be told when the expression is
built a type error (rule 2). The return-type resolver of the two
designator functions threw for a third argument that is not a literal, so
every use of such a bag was Indeterminate with status processing-error.
That is safe, and it forbids a form the profile allows. Before run 083 the
same uses ran without a check: `integer-bag-size` counted a bag of strings.

## Research

`research.md`: how CEL, cel-java, Trino, PostgreSQL, OPA, TypeScript, C#,
Cedar, Saxon, AuthzForce, Balana and XPath 3.1 treat a type that is not
known before evaluation; 15 primary sources, each quote checked in the
fetched file. Two results carry the design:

- The unknown type is one more instance of the ordinary type abstraction,
  with a relation that is wider than equality (cel-java `SimpleType.DYN`,
  Trino `UnknownType.UNKNOWN`; consistency of Siek and Taha 2006).
- An engine whose calls are not checked at evaluation rejects what is
  known to be wrong when the expression is built and puts a check on the
  argument that was only consistent (Saxon `TypeChecker.staticTypeCheck`;
  AuthzForce `BaseFirstOrderFunctionCall`; rule CApp2 of Siek and Taha).

## Change

- `v30/types/DynamicType` (new): `enum DynamicType implements ValueType`
  with the one constant `INSTANCE`, "a data type that is known only at
  evaluation". The shape stays known: `INSTANCE` is a single value,
  `INSTANCE.bagType()` a bag. `isConsistent(expected, actual)`: equal, or
  the same shape and one side dynamic. No method returns null. The type is
  not in the registry, `ofAny` throws, and `BagOfValues.Builder` refuses
  the dynamic bag type: no value and no bag has it.
- `AttributeDesignatorFunctions.resolve`: a literal third argument gives
  the bag of its data type, and throws as before when it names none.
  Anything else, and a reference to the function, gives the dynamic bag
  type.
- `FunctionParamValueTypeSpec`, `FunctionParamValueTypeSequenceSpec`:
  `isValidParamType` is consistency.
- `policy/TypeAssertion` (new, package-private) and `Apply`: the public
  argument list is unchanged. For the invocation every argument of dynamic
  type is wrapped. The wrapper evaluates the argument and tests the type
  of the value against the parameter; a mismatch is the exception of a
  type error (processing-error, the message names the function only). A
  function that evaluates its own arguments evaluates the wrapper when it
  needs the operand.
- `HigherOrderFunctions`: `all-of-any`, `any-of-all` and `all-of-all` had
  no check of their bags against the predicate, neither at build nor at
  evaluation. They get the validator of `any-of-any` and the check of the
  evaluated bags. This gap did not depend on the dynamic type:
  `all-of-all(string-equal, integer-bag(1), string-bag(a))` passed the
  check of run 083.
- `FunctionSpecBuilder`: a function without parameters accepted arguments
  in both validation methods. Found by the audit of the use sites
  (`code-intelligence.md`).
- Spec `function-semantics`: the paragraph "Every argument of an `<Apply>`
  SHALL have a type that is known" is replaced by the rules of the dynamic
  type, with six scenarios; one scenario for a function without
  parameters; a new requirement "Higher-order functions with two bags
  check their operands". Design: decision D12.

Why no function gets a value that does not fit: the check at build passes
by equality or by consistency. Consistency is used for a typed parameter,
and then the argument is wrapped, or for an operand of a higher-order
function, and each of the seven checks its evaluated operands. A function
reference of dynamic type reaches none of them: all seven refuse a
predicate that returns a bag (test
`referenceToADesignatorFunctionIsNoPredicate`).

## Proof

| Step | Result | File |
| --- | --- | --- |
| Red run 1 | 10 tests fail in xacml-core, 1 in xacml-xml, 1 in xacml-pdp | `red-surefire-summary.txt` |
| Red run 2 | the test of a function without parameters fails | `red-2-surefire-summary.txt` |
| Gate | 8,009 tests, 0 failures, 0 errors, 18 skipped; conformance 937 with 12 skipped, as in run 085 | `surefire-summary.txt` |
| Mutation check | 8 mutations, each found | `mutation-check.txt` |
| Use sites | ripwire, tgrep, ast-grep; Serena not available for this tree | `code-intelligence.md` |
| Spec | `openspec validate ... --strict`: valid | `command.txt` |
| Review | Codex: APPROVE in round 1, one LOW finding on a test, closed | `reviews/code/rv093-dynamic-type-codex` |

New and changed tests (43 more than run 085): `DynamicTypeTest` (17);
`ApplyArgumentValidationTest` (62, was 40); `InvalidApplyDecisionTest`
(16, was 13); `InvalidApplyPolicyUnmarshallerTest` (9, was 8). The test
`argumentWhoseTypeIsNotKnownWhenTheExpressionIsBuiltIsATypeError` of run
083 is replaced: its expression is type correct now.

## What changes for a policy

- The bag of a designator function with a data type that is an expression
  can be used where a bag fits, and each use is checked at evaluation.
- `all-of-any`, `any-of-all`, `all-of-all` with bags that do not fit the
  predicate are type errors.
- An argument of a function without parameters is a type error.

## Limits

- The gate is `clean test`, not `clean verify`: the jars were not built.
  The artifact repository was not reachable and the build tool had changed
  during the run (`command.txt`). Run `clean verify` when the repository
  is reachable.
- An operand of dynamic type that is not evaluated is not checked (ruling
  D11, run 074).
- A mismatch that a higher-order function finds in its evaluated bags has
  status syntax-error; one that `TypeAssertion` finds has
  processing-error. Not unified.
- `Condition` and `Match` need a type that is known when the policy is
  loaded. An expression of dynamic type is no condition.
- `QuantifiedExpression` and its subclasses are not changed. They are not
  used, have no tests and do no type checks; `QuantifiedMap` over an
  iterant of dynamic type fails at evaluation.
- `DynamicType.ofAny` throws by design. Task 6.1 carries the type over to
  `ValueExprType`.
- The policy-level reading of section 7.19.2 stays as recorded in run 083.
- JMH is not measured (no module before task 9.2). The change adds one
  pass over the arguments when an `Apply` is built, and one wrapper object
  and one type test per evaluation of an argument of dynamic type.
  `all-of-any`, `any-of-all` and `all-of-all` validate their operands at
  every invocation now, as the other four do.
