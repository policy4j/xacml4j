# Adversarial code review: a dynamic type for arguments whose data type is known only at evaluation (xacml4j)

You are an independent reviewer. Do not use tools. Do not run commands. Do
not edit files. Answer in one message. Review only the diff below; it is
the complete diff of this change. All tests of the reactor pass on it:
8,009 tests, 0 failures, 0 errors, 18 skipped. Conformance unchanged: 937 tests, 12 skipped.

## Project and trust boundary

xacml4j is a Java 21 implementation of OASIS XACML 2.0/3.0. `Apply` is the
object model of `<Apply FunctionId="...">`: a function (`FunctionSpec`)
and a list of argument expressions. Policies and extension functions come
from the deployer and are trusted. Requests are not trusted: attribute
values and the content of a request can be anything.

## Normative text

XACML 3.0 core, errata 01, section 7.4: "Valid expressions SHALL be type
correct, which means that the types of each of the elements contained
within <Apply> elements SHALL agree with the respective argument types of
the function that is named by the FunctionId attribute."

Section 7.19.2: "If a policy that contains invalid static data-types is
evaluated by the XACML PDP at the time a decision request is received,
then the result of that policy SHALL be "Indeterminate" with a StatusCode
value of "urn:oasis:names:tc:xacml:1.0:status:processing-error"."

XACML v3.0 Related and Nested Entities Profile v1.0, Committee
Specification 03 (30 January 2024), section 6.1, the attribute-designator
function: "The third argument SHALL be of data-type
http://www.w3.org/2001/XMLSchema#anyURI. The value of the argument
specifies the value of the DataType XML attribute of the XACML attribute
values returned by the function." Section 6.2, the attribute-selector
function: "The value of the argument specifies the data-type of the
attribute values returned from the evaluation of this function."

So the data type of the bag that these two functions return is the value
of an argument. The argument can be an expression.

## State before this change

- `Apply` checks its arguments once, when it is built. An expression whose
  arguments do not fit is kept as a type error: its evaluation is
  Indeterminate with status processing-error and its function is not
  invoked. A policy with such an expression still loads; conformance cases
  IIC003 and IIC014 need that.
- One rule of that check: an argument whose type can not be told when the
  expression is built is a type error. The return-type resolver of the two
  designator functions threw for a third argument that is not a literal
  (`ClassCastException`), so every use of such a bag was a type error.
  That is safe, and it forbids a form the profile allows.
- Functions are Java methods called by reflection. A bag parameter has the
  Java type `BagOfValues` for every data type, so reflection does not stop
  `integer-bag-size` from getting a bag of strings.

## Change

Design: gradual typing. What is known is checked when the expression is
built. What is not known is checked when the value arrives.

- `DynamicType` (new): one more `ValueType`, a singleton enum. It means
  "a data type that is known only at evaluation". The shape stays known:
  `DynamicType.INSTANCE` is a single value, `DynamicType.INSTANCE.bagType()`
  is a bag. `isConsistent(expected, actual)`: equal, or the same shape and
  one side dynamic. It is not in the registry of data types
  (`XacmlTypes.getType(id)`), and `ofAny` throws.
- `AttributeDesignatorFunctions.resolve`: a literal third argument gives
  the bag of that data type as before, and still throws when the literal
  names no data type. Anything else gives the dynamic bag type.
- `FunctionParamValueTypeSpec` and the typed branch of
  `FunctionParamValueTypeSequenceSpec`: `isValidParamType` is consistency
  instead of equality.
- `TypeAssertion` (new, package-private) and `Apply.withTypeAssertions`:
  `Apply` keeps its public argument list and builds a second list for the
  invocation, with a `TypeAssertion` around every argument whose static
  type is dynamic. It evaluates the argument and tests the type of the
  value against the parameter; a mismatch is the same
  `FunctionInvocationException` as a type error.
- `HigherOrderFunctions`: `all-of-any`, `any-of-all` and `all-of-all` had
  no check of their bags against the predicate at all. They get the
  validator that `any-of-any` has, and the check of the evaluated bags.
- `FunctionSpecBuilder`: a function without parameters accepted any
  arguments in the check (the loop over the parameters found "an argument
  too many" only after a last parameter). Found while reading the callers
  of the changed code; it would make `getParamSpecAt(-1)` reachable.
- `BagOfValues.Builder`: a bag value can not have the dynamic bag type.

## Facts about code that is not in the diff (read in the source)

- `FunctionSpec.invoke` calls `invokeWithList`. It binds arguments to
  parameters by position (`normalize`: mandatory parameters, then optional
  ones with their default values, then the variadic one; arguments beyond
  the last parameter are appended). Then it evaluates every argument with
  `exp.evaluate(context)`, unless the function evaluates its own arguments.
  Only `and`, `or` and `n-of` do that. Then it calls the Java method.
- A value's type is `Value.getEvaluatesTo()`, which is final and returns
  the `ValueType` the value was made with. Values are made by
  `XacmlTypes.<TYPE>.ofAny(...)`. A bag's type is the `BagOfValuesType` of
  its builder.
- The seven functions of `HigherOrderFunctions` are the only functions
  with a function-reference parameter. Their validators all end in:

  ```java
  private static boolean validateArguments(List<Expression> arguments,
          boolean singleBag, boolean booleanResult)
  {
      if(arguments == null || arguments.size() < 2 || !(arguments.get(0) instanceof FunctionReference)){
          return false;
      }
      FunctionReference ref = (FunctionReference)arguments.get(0);
      if(ref.getEvaluatesTo().isBag() || (booleanResult && !XacmlTypes.BOOLEAN.equals(ref.getEvaluatesTo()))){
          return false;
      }
      int bags = 0;
      List<Expression> primitives = new ArrayList<>();
      for(int i = 1; i < arguments.size(); i++){
          Expression argument = arguments.get(i);
          if(argument == null || argument instanceof FunctionReference || argument.getEvaluatesTo() == null){
              return false;
          }
          ValueExpTypeInfo type = argument.getEvaluatesTo();
          if(type.getValueType() == null){
              return false;
          }
          if(type.isBag()){
              bags++;
          }
          primitives.add(new PrimitiveTypeExpression(type.getValueType()));
      }
      if(singleBag && bags != 1){
          return false;
      }
      ListIterator<Expression> it = primitives.listIterator();
      try{
          for(int i = 0; i < ref.getNumberOfParams(); i++){
              if(!ref.getParamSpecAt(i).validate(it, true)){
                  return false;
              }
          }
          return !it.hasNext();
      }catch(RuntimeException e){
          return false;
      }
  }
  ```

  `requireValidArguments(ref, arguments, singleBag, booleanResult)` calls
  it with the evaluated arguments and throws
  `EvaluationException(Status.syntaxError())` when it is false. `any-of`,
  `all-of`, `any-of-any` and `map` called it before this change.
- `Condition` needs `XacmlTypes.BOOLEAN.equals(type)`. `Match` calls
  `isValidParamType` with the type of a literal value and with the data
  type of an `<AttributeDesignator>` or `<AttributeSelector>` element;
  both come from the registry.
- In this code base only the resolver of the two designator functions
  gives the dynamic type. An extension function can declare it as its
  return type.
- `QuantifiedExpression` and its four subclasses are referenced by no
  other class, have no tests, and are not produced by the XML or JSON
  mappers. They do no type checks.

## Tests in the diff

`DynamicTypeTest` (17), `ApplyArgumentValidationTest` (rows for the
dynamic type and for the two gaps), one PDP row and one XML-mapper test.
Mutation check on the final code, each run with the focused tests:

| Mutation | Failing test rows |
| --- | --- |
| M1 consistency is plain equality | 17 |
| M2 `Apply` puts no assertion around a dynamic argument | 7 |
| M3 the three two-bag functions do not check their evaluated bags | 2 |
| M4 the resolver needs a literal data type again | 23 |
| M5 the three two-bag functions have no validator | 6 |
| M6 a function without parameters accepts arguments (check with exception) | 1 |
| M7 a dynamic argument is checked against the parameter of its own index | 4 |
| M8 a function without parameters accepts arguments (check without exception) | 1 |

## Limits that are known and stated in the evidence

- An operand of dynamic type that is not evaluated is not checked (an
  earlier ruling of this change: a deciding operand of `and`, `or`, `n-of`
  wins over an operand that would fail).
- A mismatch that a higher-order function finds in its evaluated bags has
  status syntax-error; one that `TypeAssertion` finds has processing-error.
- `Condition` and `Match` still need a type that is known when the policy
  is loaded.
- The quantified expression classes are not changed.

## What to look for

1. Can a function still be invoked with a value that does not fit a typed
   parameter? Name the expression.
2. Is there an expression that was type correct and gave the right result
   before this change and is a type error, or fails, now? Not counted:
   `all-of-any`, `any-of-all`, `all-of-all` with operands that do not fit
   the predicate, and arguments of a function without parameters.
3. Is the consistency relation right, and is it used only where a check at
   evaluation follows?
4. `TypeAssertion` and `Apply`: laziness, the parameter chosen for an
   argument, equality and hash code of `Apply`, the visitor.
5. `DynamicType`: can a method return null, can a policy or a request name
   the type, can a value or a bag have it?
6. Tests that pass for the wrong reason.

Answer format: `VERDICT: APPROVE` or `VERDICT: REQUEST_CHANGES`, then the
findings, each with severity (HIGH, MEDIUM, LOW), file, and the concrete
expression or input that shows it. Say "no finding" for a point you
checked and found sound.

## Diff

(the complete diff of the change followed here)
