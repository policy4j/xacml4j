# Adversarial code review, round 2: a deciding operand wins over a failed operand (xacml4j)

You are an independent reviewer. Do not use tools. Do not edit files. Answer
in one message. Review only the diff below. It is the complete diff of this
change. The full reactor passes on it (totals below).

## Project

xacml4j is a Java 21 implementation of OASIS XACML 2.0/3.0. Functions are
static Java methods with annotations; `JavaMethodToFunctionSpecConverter`
turns each into a `FunctionSpec`.

## Maintainer ruling this change implements (2026-10-07, D11)

For `or`, `and`, `n-of` and the higher-order bag functions a later deciding
value wins over an earlier error (XACML 3.0 A.3.5 and A.3.12). When no
value decides and an operand failed, the result is Indeterminate with the
status of the first failure.

## Normative text (XACML 3.0 Plus Errata 01, fetched 2026-10-07)

A.3.5 Logical functions
This section contains the specification for logical 
functions that operate on arguments of data-type 
“http://www.w3.org/2001/XMLSchema#boolean”.
· 
urn:oasis:names:tc:xacml:1.0:function:or
This function SHALL return "False" if it has no 
arguments and SHALL return "True" if at least one of its arguments 
evaluates to "True". The order of evaluation SHALL be from first 
argument to last. The evaluation SHALL stop with a result of "True" 
if any argument evaluates to "True", leaving the rest of the 
arguments unevaluated.
· 
urn:oasis:names:tc:xacml:1.0:function:and
This function SHALL return "True" if it has no 
arguments and SHALL return "False" if one of its arguments evaluates 
to "False". The order of evaluation SHALL be from first argument to 
last. The evaluation SHALL stop with a result of "False" if any 
argument evaluates to "False", leaving the rest of the arguments 
unevaluated.
· 
urn:oasis:names:tc:xacml:1.0:function:n-of
The first argument to this function SHALL be of data-type 
http://www.w3.org/2001/XMLSchema#integer. The remaining arguments SHALL be of 
data-type http://www.w3.org/2001/XMLSchema#boolean. The first argument 
specifies the minimum number of the remaining arguments that MUST evaluate to 
"True" for the expression to be considered "True". If the 
first argument is 0, the result SHALL be "True". If the number of 
arguments after the first one is less than the value of the first argument, 
then the expression SHALL result in "Indeterminate". The order of 
evaluation SHALL be: first evaluate the integer value, and then evaluate each 
subsequent argument. The evaluation SHALL stop and return "True" if 
the specified number of arguments evaluate to "True". The evaluation 
of arguments SHALL stop if it is determined that evaluating the remaining 
arguments will not satisfy the requirement.
· 
urn:oasis:names:tc:xacml:1.0:function:not
This function SHALL take one argument of data-type 
“http://www.w3.org/2001/XMLSchema#boolean”. If the argument evaluates to 
"True", then the result of the expression SHALL be 
"False". If the argument evaluates to "False", then the 
result of the expression SHALL be "True". 
Note: When evaluating and, or, or n-of, it may not be 
necessary to attempt a full evaluation of each argument in order to determine 
whether the evaluation of the argument would result in 
"Indeterminate". Analysis of the argument regarding the availability 
of its attributes, or other analysis regarding 
errors, such as "divide-by-zero", may render the argument error 
free. Such arguments occurring in the expression in a position after the 
evaluation is stated to stop need not be processed.

A.3.12 (summary of the fetched text): `any-of` applies the predicate to each
element of the bag and combines the results with `or`; `all-of` combines
them with `and`; `any-of-any` applies it to every tuple of the cross product
and combines with `or`; `all-of-any` is evaluated "as if the any-of function
had been applied to each value of the first bag and the whole of the second
bag ... and the results were then combined using and"; `any-of-all` is
evaluated "as if the any-of function had been applied to each value of the
second bag and the whole of the first bag ... and the results were then
combined using and"; `all-of-all` applies the predicate "between every
element of the second argument and every element of the third argument and
the results were combined using and".

## What was found and changed

1. The `evaluateArguments=false` attribute of `@XacmlFuncSpec` was never
   read (also not on the main branch). `FunctionSpec.invokeWithList`
   evaluated every argument before the call, so `or` and `and` were never
   lazy through the engine: `or(true, <error>)` was Indeterminate. The
   converter now calls `FunctionSpecBuilder.lazyArgEval()` for such
   functions. Only `or`, `and`, and now `n-of` use the attribute.
2. `DecidingOperands` (new) holds the three combinations. `LogicalFunctions`
   and `HigherOrderFunctions` use it.
3. `n-of` is now lazy: its first parameter is an `Expression` that is
   evaluated first. Before, every operand was evaluated before the call.
4. The invocation passes `null` for a variadic parameter without arguments,
   so `or()` and `and()` failed with a `NullPointerException`
   (Indeterminate). They now return false and true.

Facts you cannot see in the diff:

- `EvaluationException` extends `RuntimeException`. A failure of an operand
  is an exception from `Expression.evaluate(context)` or from
  `FunctionReference.invoke(context, ...)`.
- `FunctionInvocationException` (thrown by the invocation layer around every
  function) keeps the status of the first `EvaluationException` in its cause
  chain.
- A bag iterates in insertion order (Guava `ImmutableMultiset`).
- `HigherOrderFunctions.anyOf` is the public function with its own argument
  validation; `allOfAny` and `anyOfAll` called it per value before this
  change too.
- `Value.evaluate(context)` returns the value itself.
- With lazy evaluation an operand is an unevaluated `Expression` (an
  `Apply`, an attribute designator, a value).
- The context keeps the first status that an attribute reference records
  (`setEvaluationStatusIfAbsent`); the PDP reads it only when the final
  decision is Indeterminate.

## Round 1 result and what was done

Codex: REQUEST_CHANGES with one finding and one missing test row. Grok: no
report (the run produced no output in 53 minutes and was stopped).

1. Codex MEDIUM: a failed operand that a later value overrules has already
   recorded its status in the context (`and(or(M, T), E)`: `M` records
   missing-attribute, the decision is Indeterminate because of `E`), so the
   decision could report the status of `M`.
   What the code does: the code that catches the failure which escapes sets
   the status from that exception and overwrites what is there.
   `Condition.evaluate` is
   `catch(EvaluationException e){ context.setEvaluationStatus(e.getStatus()); return INDETERMINATE; }`;
   the target match code (`MatchAllOf`, `BaseDecisionRule`) does the same.
   `Rule.evaluate` then calls `setEvaluationStatusIfAbsent`, which changes
   nothing. New decision-level test `LaterDecidingOperandPdpTest`:
   `and(or(M, T), E)` gives Indeterminate{P} with processing-error;
   `or(M, T)` gives Permit with status ok; `or(M, F)` gives Indeterminate
   with missing-attribute; `and(M, F)` gives NotApplicable; `n-of(2, M, T, T)`
   gives Permit.
   Not fixed here: a caller that only uses `setEvaluationStatusIfAbsent`
   after a failure (a rule whose obligation or advice expression fails)
   can still pick up the recorded status. Tracked as bug policy4j-the.4.55.
2. Codex: missing row for the quantifier order. Added
   `mixedQuantifiersAreForEachThenThereIs` with integer-equal: both bags
   (1, 2) give true for `all-of-any` and `any-of-all`; `all-of-any((1, 2), (1))`
   and `any-of-all((1), (1, 2))` give false; the other direction gives true.

The full reactor passes with these tests: 7,709 tests, 0 failures, 0 errors,
22 skipped (the run also held a conformance-harness change that is reviewed
separately).

## Questions

Report findings ordered by severity (HIGH, MEDIUM, LOW), each with file,
line from the diff, a concrete failing scenario, and a suggested fix. Report
only defects you can show from the diff, the normative text, and the facts
above.

1. Do `or`, `and`, `n-of` match A.3.5 and the ruling for every mix of true,
   false, and failed operands, including the order of evaluation and where
   evaluation stops? Check `DecidingOperands.atLeast` for off-by-one errors.
2. Do the six higher-order functions match A.3.12 and the ruling, including
   the quantifier order of `all-of-any` and `any-of-all`? Is the reuse of
   one `parameters` array safe?
3. Is there a kind of failure that must not be deferred until the other
   operands are evaluated?
4. Does making `or`, `and`, `n-of` lazy change anything else that the diff
   shows (argument validation, variadic handling, the `null` variadic case)?
5. Are the tests enough to pin this? Name the missing row, if any.
6. Is the round-1 finding answered? Is deferring the remaining path to a
   separate bug acceptable, or does this diff make a wrong status reachable
   in a way the new tests do not show?

End with a verdict line: `VERDICT: APPROVE` or `VERDICT: REQUEST_CHANGES`.

## Diff

```diff
