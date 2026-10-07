# Decision memo: five semantic questions for task 4.6

**Change:** `openspec/changes/modernize-core-for-jdk28-valhalla`
**Lane:** `policy4j_code_review` (Grok 4.7, adversarial) acting as spec analyst
**Date:** 2026-10-02
**Audience:** maintainer
**Scope:** read-only. No Maven run. No source edit. This file is the only write.

Primary text is the XACML 3.0 Core OASIS Standard, 22 January 2013
(`https://docs.oasis-open.org/xacml/3.0/xacml-3.0-core-spec-os-en.html`),
compared with Plus Errata 01 complete
(`https://docs.oasis-open.org/xacml/3.0/errata01/os/xacml-3.0-core-spec-errata01-os-complete.html`).
Quoted Core paragraphs match between OS and Errata except where this memo
says otherwise. The Errata note under A.3.5 changes the OS words "MAY NOT"
to lowercase "may not".

Other engines are secondary evidence. Behavior that was not read is not stated.

| # | Question | Recommendation | Confidence | Deciding citation |
| --- | --- | --- | --- | --- |
| 1 | `integer-divide` / `integer-mod` | Return `xs:integer`. Keep Java toward-zero quotient and sign-of-dividend remainder. Zero divisor stays Indeterminate. Amend the delta spec: A.3.2 does not say "truncated toward zero". | High on the return type. Medium on quotient direction and remainder sign. | A.3.2, OS paragraph beginning "All of the following functions SHALL take two arguments" |
| 2 | `or` / `and` / `n-of` / higher-order errors | A later True (`or`, `any-of`, `any-of-any`) or a later False (`and`, `all-of`, `all-of-all`) wins. Stop only on that deciding value. Wire real lazy evaluation; the `evaluateArguments=false` annotation is dead. | High that a later deciding value wins. Medium that all-False with no error is False. | A.3.5 `or` / `and` / `n-of`; A.3.12 "combined with or/and" |
| 3 | Non-boolean Condition (IIC012) | Keep the load-time rejection. Mark both IIC012 rows as the special-instruction static-rejection branch. This disagrees with labeling the row an engine defect (`policy4j-the.4.23`). | High | §5.26 MUST boolean; `IIC012Special.txt` |
| 4 | NaN and signed zero | Keep `double-equal(NaN, NaN)` true and record a deviation from IEEE 754-1985 §5.7. Make `+0.0` equal `-0.0`. Do not implement the delta spec sentence "NaN SHALL NOT equal any value, including NaN" while `oasis30/IIC350` and `oasis30/IIC358` stay enabled. | High | A.3.1 plus IEEE 754-1985 §5.7, against `IIC350`/`IIC358` Permit |
| 5 | `on-permit-apply-second` | The URN is Committee Specification 01 (18 August 2014), not Core or Errata. The class does not implement §2.1. Register it only after `combine()` matches that pseudo-code. | High on document status and on the mismatch. The choice to claim the optional profile is a product decision. | Additional Combining Algorithms Profile CS01 §2.1 and §3 |

## 1. `integer-divide` and `integer-mod` (task 4.6)

### Normative text

XACML 3.0 OS §A.3.2 Arithmetic functions:

> All of the following functions SHALL take two arguments of the specified data-type, integer, or double, and SHALL return an element of integer or double data-type, respectively. However, the "add" and "multiply" functions MAY take more than two arguments. Each function evaluation operating on doubles SHALL proceed as specified by their logical counterparts in IEEE 754 [IEEE754]. For all of these functions, if any argument is "Indeterminate", then the function SHALL evaluate to "Indeterminate". In the case of the divide functions, if the divisor is zero, then the function SHALL evaluate to "Indeterminate".

> `urn:oasis:names:tc:xacml:1.0:function:integer-divide`
> The result is the first argument divided by the second argument.

> `urn:oasis:names:tc:xacml:1.0:function:integer-mod`
> The result is remainder of the first argument divided by the second argument.

The IEEE sentence in A.3.2 is limited to functions operating on doubles. The integer-divide sentence does not say truncated, toward zero, floor, or half-even. The integer-mod sentence does not say whose sign the remainder has. The zero-divisor sentence names "the divide functions". It does not name `integer-mod`.

§7.5 says XACML SHALL use IEEE 754 for "all integer and double functions" in the Extended Default Context, with the division-by-zero trap enabled and rounding set to round-half-even. That paragraph sets a floating-point context. IEEE 754-1985 does not define integer division. Applying round-half-even to the real quotient would make `3/2` equal `2` and `-3/2` equal `-2`. Toward-zero integer division makes those quotients `1` and `-1`. Floor division makes them `1` and `-2`. §7.5 is not a rule that `integer-divide(3, 2)` is `2`.

Errata 01 repeats the same A.3.2 and §7.5 sentences.

The change delta `specs/function-semantics/spec.md` says `integer-divide` SHALL return an `xs:integer` "truncated toward zero (XACML 3.0 §A.3.2)". The return type is what A.3.2 requires. The parenthetical attributes a truncation rule to a section that does not contain one.

### What xacml4j does

`ArithmeticFunctions.java:160-168` declares

`urn:oasis:names:tc:xacml:1.0:function:integer-divide`

with `@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#double")` and returns `DoubleVal`. The body calls `IntegerVal.divide` after `Preconditions.checkArgument(b.get() != 0)`.

`IntegerVal.java:109-112` computes `get() / d.get()` with Java `long` division, which truncates toward zero, then boxes that quotient as `DoubleVal.of(...)`. `5/2` is the double `2.0`, not `2.5`.

`ArithmeticFunctions.java:170-177` declares `integer-mod` with return type `xs:integer` and calls `IntegerVal.mod`. `IntegerVal.java:114-117` returns `IntegerVal.of(get() % d.get())`. Java `%` has the sign of the dividend, so `(-5) % 3` is `-2`. There is no zero check. A zero divisor throws `ArithmeticException`.

A zero divisor on either function becomes Indeterminate with `processing-error` when the call sits under `Condition.evaluate`. `DefaultFunctionInvocation.java:136-138` wraps the unchecked exception as `FunctionInvocationException`, and `FunctionInvocationException.java:53` builds `Status.processingError()`. `Condition.java:93-100` maps that to `ConditionResult.INDETERMINATE`. The observable condition result already matches A.3.2 for divide-by-zero. The function itself still signals with `IllegalArgumentException` or `ArithmeticException`.

### Callers

Search of `**/*.{java,xml}` for `integer-divide` and `divideInteger`:

- `ArithmeticFunctions.java:160` (the function)
- `ArtimeticFunctionsTest.java:49-53`, which expects `XacmlTypes.DOUBLE.ofAny(2)` for `divideInteger(4, 2)`
- evidence copies of that test under `evidence/tests/010-core-junit5-mockito/`
- `xacml-conformance-tests/.../oasis-xacml20-compat-test/IIC020Policy.xml`
- `xacml-conformance-tests/.../xacml3.0-ct-v.0.4/IIC020Policy.xml`

Search of `**/*.java` for `.divide(` finds only `ArithmeticFunctions.java:157` (`double-divide`) and `:167` (`integer-divide`). No in-repo policy passes `integer-divide` to a `double-*` function.

`integer-mod` appears in production code and in `IIC022Policy.xml` (both suites). IIC022 compares `age mod 2` with `0`. It does not exercise a negative dividend or a zero divisor. Manifest rows `oasis20/IIC022` and `oasis30/IIC022` are `executed,true`.

### Conformance

`IIC020Policy.xml` (3.0 copy, lines 13-23) is `integer-greater-than-or-equal(integer-divide(age, 2), bart-age)`. `IIC020Request.xml` has age `45` and bart age `10`. `45/2` is `22` under toward-zero, floor, and half-even (`22.5` rounds to even `22`). Half-up would be `23`. Both `22` and `23` are `>= 10`. `IIC020Response.xml` is Permit / `status:ok`.

Manifest:

- `oasis20/IIC020` skipped, reason "integer-divide returns DoubleVal; integer-greater-than-or-equal then fails invocation type matching", bead `policy4j-the.4.6`
- `oasis30/IIC020` skipped, same reason

IIC020 fails on the return type. It does not select a rounding mode. No RSA or example policy uses `integer-divide`.

Changing the success type to `IntegerVal` makes both IIC020 rows able to Permit. It breaks `ArtimeticFunctionsTest.testDivideIntegerFunction` and the public signatures `ArithmeticFunctions.divideInteger` (`DoubleVal`) and `IntegerVal.divide` (`DoubleVal`). No in-repo policy depends on the double type. External callers of those signatures are outside this tree; `design.md` lines 236-238 ask that question and this search cannot answer it.

### Secondary evidence

AuthzForce CE `core` at `5f7ad998` (secondary):

- `NumericArithmeticOperators.DivideOperator.eval` returns `arg0.divide(arg1)` (`NumericArithmeticOperators.java`).
- `INTEGER_MOD_OPERATOR` returns `arg0.remainder(arg1)`. The body of `IntegerValue.divide` / `remainder` was not in that file and was not fetched.
- `NumericArithmeticFunctionsTest` expects `integer-divide(6, 3) = IntegerValue(2)` and `integer-divide(7, -3) = IntegerValue(-2)`. `-2` matches toward-zero and also half-even of `-2.333...`. It does not match floor (`-3`). The same test expects `integer-mod(6, 3) = 0` and `integer-mod(7, 3) = 1`. The retrieved fragment has no negative remainder, so the sign rule is not shown.

WSO2 Balana `DivideFunction.java` (secondary; file header "Copyright 2003-2004 Sun Microsystems, Inc.", Sun XACML lineage, not a current Sun project):

- `integer-divide` is constructed with `IntegerAttribute.identifier` as both argument type and return type.
- `long quotient = dividend / divisor` then `new IntegerAttribute(quotient)`.
- divisor `0` is `makeProcessingError("divide by zero")`.

AT&T XACML: a GitHub code search for `integer-divide` in `repo:att/XACML` returned no usable hits (`incomplete_results` / empty). No AT&T behavior is claimed.

### Options

1. Return `xs:integer`, quotient toward zero, remainder sign of the dividend, zero divisor Indeterminate for both functions.
2. Return `xs:integer` and apply §7.5 half-even to the real quotient (`3/2 = 2`, `-3/2 = -2`), with a remainder defined to match that quotient.
3. Leave the double return type. IIC020 stays red.

### Recommendation

Take option 1. The type change is what A.3.2 requires and what IIC020 needs. Toward-zero is the quotient this code, the Sun-lineage function, and the AuthzForce `7/-3 = -2` test already compute. Pair `integer-mod` with that quotient so `(a/b)*b + (a%b) = a`, which is what `IntegerVal` does today. State both choices as implementation decisions. Delete the delta-spec claim that A.3.2 itself says "truncated toward zero".

Keep zero-divisor as Indeterminate / `processing-error` for divide (A.3.2, high) and for mod (operational error under §7.4; the A.3.2 sentence does not name mod, so that extension is medium). Replace `Preconditions.checkArgument` and the raw `ArithmeticException` with the engine's Indeterminate status so the function does not depend on the invocation wrapper.

## 2. `or`, `and`, `n-of`, and higher-order errors (`policy4j-the.4.12`)

### Normative text

§7.4 Expression evaluation:

> XACML defines an evaluation result of "Indeterminate", which is said to be the result of an invalid expression, or an operational error occurring during the evaluation of the expression.

§A.3.5, OS and Errata, same normative sentences:

> `or`: This function SHALL return "False" if it has no arguments and SHALL return "True" if at least one of its arguments evaluates to "True". The order of evaluation SHALL be from first argument to last. The evaluation SHALL stop with a result of "True" if any argument evaluates to "True", leaving the rest of the arguments unevaluated.

> `and`: This function SHALL return "True" if it has no arguments and SHALL return "False" if one of its arguments evaluates to "False". The order of evaluation SHALL be from first argument to last. The evaluation SHALL stop with a result of "False" if any argument evaluates to "False", leaving the rest of the arguments unevaluated.

> `n-of`: If the first argument is 0, the result SHALL be "True". If the number of arguments after the first one is less than the value of the first argument, then the expression SHALL result in "Indeterminate". The order of evaluation SHALL be: first evaluate the integer value, and then evaluate each subsequent argument. The evaluation SHALL stop and return "True" if the specified number of arguments evaluate to "True". The evaluation of arguments SHALL stop if it is determined that evaluating the remaining arguments will not satisfy the requirement.

The note (OS "MAY NOT", Errata "may not") says that an argument after the stated stop need not be processed to discover whether it would be Indeterminate. The only stop `or` states is True. The only stop `and` states is False. A later True therefore decides `or` even when an earlier argument is Indeterminate. A later False decides `and` even when an earlier argument is Indeterminate. An Indeterminate does not decide the function by itself.

`or` does not contain the sentence "return False if every argument is False". That result follows only when every argument has been evaluated to False and none was Indeterminate. If any evaluated argument is Indeterminate and no True was found, §7.4 makes the expression Indeterminate. Same shape for `and`, with False and True swapped. Confidence on the all-False / all-True case is medium because it is not a literal sentence.

`n-of(0, ...)` is True after the integer is evaluated, before the boolean arguments. An error in a later argument must not be evaluated. If the True count is met, evaluation stops and later errors are not evaluated. If the remaining arguments cannot meet the count, evaluation stops. If a seen argument is Indeterminate and the True count was not met, the result is Indeterminate: that argument might have been the missing True. Returning False in that case is not licensed.

§A.3.12:

- `any-of` and `any-of-any` combine predicate results with `or`. A later True wins.
- `all-of` combines with `and`. A later False wins.
- `all-of-any` is `and` of `any-of` over each element of the first bag. A later False wins.
- `any-of-all` is `and` of `any-of` applied to each element of the second bag against the whole first bag. A later False wins. The "True iff" sentence matches that quantifier (for every element of the second bag, some element of the first bag).
- `all-of-all` combines every pair with `and`. A later False wins.
- `map` builds a bag. It has no `or`/`and` rule. One element error makes the map Indeterminate.

### What xacml4j does

`XacmlFuncSpec.java:48` defines `boolean evaluateArguments() default true`. `JavaMethodToFunctionSpecConverter.createFunctionSpec` reads `funcId.id()` and never reads `evaluateArguments()`. `FunctionSpecBuilder.lazyArgEval()` (`FunctionSpecBuilder.java:167-168`) is the switch that skips pre-evaluation, and nothing in `src/main` calls it.

`FunctionSpecBuilder.java:383`:

`params = evaluateParameters ? normalizedArgs : evaluate(context, normalizedArgs)`

The default `evaluateParameters` is false, so every argument is evaluated before the Java method runs (`evaluate` at lines 592-598 walks the whole list). The first `EvaluationException` aborts the call. `and` and `or` are annotated `evaluateArguments=false` (`LogicalFunctions.java:59` and `:97`). That annotation does not reach the builder. `n-of` (`:136`) has no such annotation. All three functions pre-evaluate every operand.

The method bodies then evaluate again:

- `and` (`:67-72`) stops on False, and lets `evaluate` throw.
- `or` (`:105-110`) stops on True, and lets `evaluate` throw.
- `nof` (`:144-170`) throws `IllegalArgumentException` when `values.length < n`, returns True when `n == 0` only after the arguments have already been evaluated, and returns False when the True count is short. An Indeterminate never becomes a counted non-True; it throws during pre-evaluation.

Consequences that violate A.3.5:

- `or(Indeterminate, True)` is Indeterminate. The spec result is True.
- `and(Indeterminate, False)` is Indeterminate. The spec result is False.
- `or(True, divide-by-zero)` evaluates the division. The spec stops on the first True.
- `n-of(0, divide-by-zero)` evaluates the division. The spec result is True.
- `n-of(1, Indeterminate, True)` is Indeterminate. The spec result is True.

Higher-order (`HigherOrderFunctions.java`):

- `anyOf` (`:72-78`) returns true on the first true predicate and throws on the first `ref.invoke` error.
- `allOf` (`:96-101`) returns false on the first false and throws on the first error.
- `anyOfAny` (`:215-233`) returns true on the first true tuple and throws on the first error.
- `allOfAll` (`:167-176`) returns false on the first false pair and throws on the first error.
- `map` (`:194-197`) invokes every element and propagates the error. That matches A.3.12 for `map`.

`anyOfAll` (`:148-155`) is a separate quantifier bug. It returns true when some element of the first bag is `all-of` against the second bag (exists-forall). A.3.12 requires forall-exists: `and` of `any-of` over the second bag. Those differ. First bag `{3,5}`, second bag `{1,4}`, predicate `integer-greater-than`: the spec expression is True (`3>1` and `5>4`); the code is False (neither 3 nor 5 is greater than both 1 and 4). Fixing only error propagation does not make `any-of-all` match A.3.12.

### Conformance

Opened policies do not encode "error, then a deciding value":

- `oasis20`/`oasis30` `IIC056` is `or` of two `string-regexp-match` calls. Both rows are `executed,true`.
- `oasis20`/`oasis30` `IIC094` is `n-of(2, age>=45, subject-id = Bart, subject-id = Julius)`. Both rows are `executed,true`.
- `rsa2008` `XacmlPolicySet-02f-emergency.xml:33` is `and` of two `not`/`any-of`/`string-equal` expressions. No divide and no typed error operand.
- No RSA policy uses `integer-divide`.

No conformance row was found that expects the current fail-fast result. A row-by-row reading of every `or`/`and` policy was not done. Policies that an operator wrote to rely on "an early error hides a later True/False" would change decision. That is the compatibility impact, and it is the impact A.3.5 requires.

### Secondary evidence

AuthzForce, Sun XACML, and AT&T were not fetched for `or`/`and` short-circuit. No other-engine claim is made.

### Options

1. Make `evaluateArguments=false` actually call `lazyArgEval()` for `and`, `or`, and `n-of`. Inside the functions, catch an argument error, remember Indeterminate, and keep going until True (`or`, `n-of` count), False (`and`), or "remaining arguments cannot satisfy" (`n-of`). Do not evaluate past the stop. Apply the same rule inside `any-of`, `all-of`, `any-of-any`, `all-of-any`, `any-of-all`, and `all-of-all`, and correct the `any-of-all` quantifier.
2. Leave fail-fast. The functions stay simpler and keep violating the SHALL return / SHALL stop sentences.
3. Lazy-evaluate only `and` and `or`, and leave `n-of` and the higher-order functions on the old path.

### Recommendation

Option 1. The deciding sentences are the unconditional "SHALL return True if at least one argument evaluates to True" and the matching `and` and `n-of` stops. §7.4 supplies Indeterminate only when the stop was not reached and an error was seen. Option 3 leaves `n-of(0, error)` and `any-of` wrong. `map` stays strict.

Implementers must not treat the existing `evaluateArguments=false` annotations as already doing this. They are unread. The builder flag is inverted relative to its name: `evaluateParameters == true` means "pass arguments through", and `false` means "evaluate them all first".

## 3. Non-boolean Condition (`policy4j-the.4.23`, IIC012)

### Normative text

§5.26 Element `<Condition>`:

> The `<Condition>` element is a Boolean function over attributes or functions of attributes. ... The `<Condition>` contains one `<Expression>` element, with the restriction that the `<Expression>` return data-type MUST be "http://www.w3.org/2001/XMLSchema#boolean". Evaluation of the `<Condition>` element is described in Section 7.9.

§7.9:

> The condition value SHALL be "True" if the `<Condition>` element is absent, or if it evaluates to "True". Its value SHALL be "False" if the `<Condition>` element evaluates to "False". The condition value SHALL be "Indeterminate", if the expression contained in the `<Condition>` element evaluates to "Indeterminate."

§7.9 does not say that a non-boolean value is Indeterminate. The three outcomes are absent/True, False, and Indeterminate.

§7.19.2:

> If a policy that contains invalid syntax is evaluated by the XACML PDP at the time a decision request is received, then the result of that policy SHALL be "Indeterminate" with a StatusCode value of "urn:oasis:names:tc:xacml:1.0:status:syntax-error".
> If a policy that contains invalid static data-types is evaluated by the XACML PDP at the time a decision request is received, then the result of that policy SHALL be "Indeterminate" with a StatusCode value of "urn:oasis:names:tc:xacml:1.0:status:processing-error".

Both sentences are conditional on the PDP evaluating the policy at request time. They do not require the PDP to accept the policy at load time.

`IIC012Special.txt` (same text under `oasis-xacml20-compat-test/` and `xacml3.0-ct-v.0.4/`) says the policy contains a static type error. If a PDP may ever evaluate such a policy at request time, the test must pass and the result must match the supplied response: Indeterminate with `processing-error`. The file names `IIC003Response.xml`; the response next to this case, `IIC012Response.xml`, is that decision and status. If the PDP can never evaluate such a policy, the implementation must demonstrate rejection, and the Request and Response may be ignored. The file names `IIA004Policy.xml` for that demonstration; the policy under test is `IIC012Policy.xml`. Those two filenames are the known copy-paste errors in the special instructions. IIA004 remains the syntax-error analogue (`syntax-error` under §7.19.2), not this case.

`IIC012Policy.xml` lines 11-21 put `integer-subtract` in the Condition. That is schema-valid XML and a static non-boolean type. `IIC012Response.xml` is Indeterminate / `processing-error`. Manifest rows `oasis20/IIC012` and `oasis30/IIC012` are skipped, reason "Fixture/spec issue", bead `policy4j-the.3.9`.

### What xacml4j does

`Condition.java:55-64` rejects construction unless `predicate.getEvaluatesTo()` equals `XacmlTypes.BOOLEAN` (`Preconditions.checkArgument` at line 59). The policy does not reach request evaluation. `Condition.evaluate` (`:87-101`) handles an expression that throws, or that returns a `BooleanVal`. It does not define a non-boolean success value, because the constructor already excluded that declared type.

The delta `specs/condition-evaluation/spec.md` matches this split: a declared non-boolean type fails to load; a runtime result of zero values, several values, or a non-boolean value is Indeterminate / `processing-error`. IIC012 is the static case.

### Secondary evidence

AuthzForce, Sun XACML, and AT&T were not fetched for Condition type checking. No other-engine claim is made.

### Options

1. Keep the load-time rejection. Satisfy IIC012 by the second branch of the special instructions (demonstrate rejection; ignore Request and Response).
2. Accept the policy and return Indeterminate / `processing-error` at request time, matching `IIC012Response.xml` and the first branch of the special instructions.
3. Accept the policy and treat a non-boolean value as False.

### Recommendation

Option 1. §5.26 is a MUST on the return data-type. §7.19.2 applies only if the PDP evaluates the bad policy. The special instructions explicitly allow an implementation that can never evaluate it. Option 3 has no support in §7.9. Option 2 is legal only for an implementation that chooses to evaluate static type errors; choosing it would weaken `Condition` so the harness can compare `IIC012Response.xml`.

This disagrees with a review that tags IIC012 as engine defect `policy4j-the.4.23`. The skipped rows should be described as the special-instruction static-rejection branch, not as a fixture bug and not as a PDP defect. The delta spec's load-time rule should stay.

## 4. NaN and signed zero (task 4.6)

### Normative text

§A.3.1 `double-equal`:

> This function SHALL take two arguments of data-type "http://www.w3.org/2001/XMLSchema#double" and SHALL return an "http://www.w3.org/2001/XMLSchema#boolean". It SHALL perform its evaluation on doubles according to IEEE 754 [IEEE754].

§A.3.6 Numeric comparison functions:

> For doubles they SHALL comply with the rules governed by IEEE 754 [IEEE754].

Bibliography `[IEEE754]` is "IEEE Standard for Binary Floating-Point Arithmetic 1985, ISBN 1-5593-7653-8". String conversion of doubles also SHALL use IEEE 754 (OS paragraph 3529). XACML points `double-equal` and the double comparisons at IEEE 754, not at XML Schema value equality.

ANSI/IEEE Std 754-1985 §5.7, from the public copy of that standard (the IEEE product PDF was not the source of this quote):

> Four mutually exclusive relations are possible: less than, equal, greater than, and unordered. The last case arises when at least one operand is NaN. Every NaN shall compare unordered with everything, including itself. Comparisons shall ignore the sign of zero (so +0 = -0).

Unordered means the predicates `<`, `>`, `<=`, `>=`, and `=` are false when an operand is NaN. So `double-equal(NaN, NaN)` is false under the citation A.3.1 actually makes, and `+0` equals `-0`.

XML Schema Part 2, Second Edition, 28 October 2004, §3.2.5 `double` (secondary relative to A.3.1, primary as the XSD rule the suite matches):

> NaN equals itself but is incomparable with (neither greater than nor less than) any other value in the value space.
> This datatype differs from that of [IEEE 754-1985] in that there is only one NaN and only one zero.

XSD also says applications that compute with IEEE 754 may use a different equality than XSD identity, and that XACML's own function is the place that chose IEEE. The two documents disagree on `NaN == NaN`. They agree that `+0` equals `-0`.

### What xacml4j does

`DoubleVal` extends `Value.OtherValue` and does not override `equals`. `Value.java:132-142` uses `value.equals(...)`. `Double.equals` is true for two NaNs and false for `+0.0` versus `-0.0`.

`EqualityPredicates.java:68-74` implements `double-equal` as `_eq`, so `double-equal(NaN, NaN)` is true and `double-equal(-0.0, 0.0)` is false.

`NumericComparisonFunctions.java`:

- `double-greater-than` (`:83`) and `double-less-than` (`:92`) use primitive `>` and `<`. Those are false for any NaN operand, and `+0.0 > -0.0` is false. That part matches IEEE.
- `double-greater-than-or-equal` (`:101`) and `double-less-than-or-equal` (`:110`) are `Double.equals` OR the primitive relation. `NaN >= NaN` is therefore true, and `-0.0 >= 0.0` is false. Both disagree with IEEE. The NaN half agrees with XSD identity. The signed-zero half agrees with neither IEEE nor XSD.

### Conformance

NaN literals in the vendored 3.0 suite are `IIC350`, `IIC353`, `IIC354`, `IIC358`, and `IIIA340` (assignment of NaN, not a comparison). There is no `-0.0` literal under `xacml-conformance-tests/src/test/resources`.

| Case | What it compares | Expected | Manifest |
| --- | --- | --- | --- |
| `oasis30/IIC350` | `double-equal(age, NaN)` with request age NaN. Description: "Permit if the test age is NaN". | Permit / ok | `executed,true` |
| `oasis30/IIC358` | `double-equal(double-add(age, 1.0), NaN)` with request age NaN. Description: "Arithmetic on NaN returns NaN". | Permit / ok | `executed,true` |
| `oasis30/IIC353`, `IIC354` | `double-equal` of NaN to an infinity | NotApplicable (opened `IIC353` response) | `executed,true` |

`IIC350` and `IIC358` are Permit only if `double-equal(NaN, NaN)` is true. IEEE 754-1985 §5.7 makes that predicate false, the condition False, and a deny-overrides policy with an empty target NotApplicable. There is no `oasis20` IIC350 row. No opened case locks signed zero or a NaN ordering predicate (`<=`, `>=`, `<`, `>`).

The delta spec says "NaN SHALL NOT equal any value, including NaN, and `0.0` SHALL equal `-0.0`". The second half matches IEEE and XSD. The first half matches IEEE and fails `IIC350` and `IIC358`.

### Secondary evidence

AuthzForce, Sun XACML, and AT&T were not fetched for `double-equal(NaN, NaN)`. No other-engine claim is made.

### Options

1. Suite equality: `NaN` equals `NaN`, `+0.0` equals `-0.0`. Record a deviation from A.3.1 / IEEE §5.7 for the NaN half. Keep `<` and `>` as primitive comparisons. Define `<=` and `>=` as that equality OR the relation, and record that `NaN >= NaN` and `NaN <= NaN` are then true.
2. Strict IEEE: `NaN` equals nothing, including NaN; `+0.0` equals `-0.0`; every `<` `>` `<=` `>=` with NaN is false. Disable or rewrite `IIC350` and `IIC358`.
3. Leave `Double.equals` in place. `IIC350` and `IIC358` stay green. `-0.0` stays unequal to `0.0`, which both IEEE and XSD reject, and no conformance case notices.

`Double.compare(a, b) == 0` is not option 1 and not option 2. It treats NaNs as equal and signed zeros as equal, and it also orders `-0.0 < +0.0` and NaN above `+Infinity`. It must not be used as the ordering predicate.

### Recommendation

Option 1 for a suite-green engine. Do not implement the delta spec's "NaN SHALL NOT equal NaN" while `oasis30/IIC350` and `oasis30/IIC358` are enabled. Amend that sentence to say the implementation follows XSD §3.2.5 identity for `double-equal`, and that this is a deviation from A.3.1's IEEE citation, justified by those two cases.

Change signed zero so `+0.0` equals `-0.0`. That is required by IEEE §5.7 and by XSD's "only one zero", and no OASIS case locks the current `Double.equals` result.

Keep primitive `<` and `>`. For `<=` and `>=`, OR the chosen equality with the relation. State in the spec that `NaN <= NaN` and `NaN >= NaN` are true under this deviation. Confidence on that ordering choice is medium: no conformance case covers it, and IEEE says those predicates are false. A maintainer who wants strict IEEE must take option 2 and retire or retag `IIC350` and `IIC358`.

## 5. `on-permit-apply-second` (task 4.6)

### Normative text

The URN is not in XACML 3.0 Core OS Appendix C and not in Errata 01 Appendix C. Appendix C ends at the legacy ordered-permit-overrides algorithm.

It is defined by *XACML 3.0 Additional Combining Algorithms Profile Version 1.0*, OASIS Committee Specification 01, 18 August 2014, edited by Erik Rissanen. This version:

`http://docs.oasis-open.org/xacml/xacml-3.0-combalgs/v1.0/cs01/xacml-3.0-combalgs-v1.0-cs01.html`

The latest-version URL serves that CS01 text. It is not an OASIS Standard. The abstract says the profile "defines new useful but optional combining algorithms". The introduction says they "have not been considered important enough to include as mandatory items in the core XACML specification".

Identifier (§2.1):

`urn:oasis:names:tc:xacml:3.0:policy-combining-algorithm:on-permit-apply-second`

Normative pseudo-code, compressed from CS01 §2.1 without changing the branches:

- If `lengthOf(children) < 2` or `> 3`, return `Indeterminate{DP}` with status `urn:oasis:names:tc:xacml:1.0:status:processing-error`.
- Evaluate child 0.
- If that decision is `NotApplicable`, `Deny`, or `Indeterminate{D}`: if there are two children return `NotApplicable`; otherwise evaluate child 2 and return it. Child 1 is not evaluated.
- If child 0 is `Permit`, evaluate child 1 and return it.
- Otherwise (child 0 is `Indeterminate{P}` or `Indeterminate{DP}`) return `Indeterminate{DP}` using the status of child 0.
- "Obligations and advice MUST be combined as described in [XACML3]."

§3 Conformance: an implementation that claims this profile MUST implement that URN as defined in §2.1. An implementation that does not claim the profile does not violate Core or Errata by omitting it.

Revision history in the same document: WD 05 (27 June 2013) changed the Indeterminate return; WD 04 had already allowed two children. The non-normative §2.2 describes a policy-level condition and a two-or-three-child if-then-else. The class comment in xacml4j retells §2.2. It does not retell the pseudo-code.

### What xacml4j does

`OnPermitApplySecondPolicyCombiningAlgorithm.java:51` holds the URN. The class has no `@XacmlPolicyDecisionCombiningAlgorithm` annotation and no `@AutoService` provider annotation. `DefaultXacml30DecisionCombiningAlgorithms.java:30-68` does not add it. `DecisionCombiningAlgorithmProviderBuilder.withDefaultAlgorithms()` (`:59-61`) adds only that default provider. `withDiscoveredAlgorithms()` (`:64-69`) loads `DecisionCombiningAlgorithmProvider` via `ServiceLoader`. This class is not a provider.

Search of `**/*.{java,xml}` for the URN finds only that one constant. No OASIS or RSA policy uses it.

`combine()` (`:60-95`) does not match CS01 §2.1:

- `policies.size() != 2` returns `INDETERMINATE_DP` with a fresh `processing-error`. Three children, which CS01 allows, are rejected. One child is also rejected, which matches CS01.
- `NotApplicable` on child 0 returns `NotApplicable` immediately. CS01 treats `Deny` and `Indeterminate{D}` the same way, and a third child is the else branch.
- The method always evaluates child 1. CS01 evaluates child 1 only when child 0 is `Permit`, and evaluates child 2 only on the else branch.
- When child 0 is not `Permit`, the method inspects child 1 and returns a processing-error Indeterminate of child 1's flavor, or `NotApplicable`. CS01, for `Indeterminate{P}` or `Indeterminate{DP}` on child 0, returns `Indeterminate{DP}` with child 0's status and does not evaluate child 1.

The body matches an earlier draft (exactly two children, and a different Indeterminate rule) better than it matches CS01. Registering this body under the CS01 URN would publish the wrong algorithm.

### Secondary evidence

AuthzForce wiki "XACML Combining Algorithms" (secondary, 2020 page on `authzforce/core`): "AuthzForce supports all the non-deprecated policy and rule combining algorithms defined in XACML 3.0 core and Additional Combining Algorithms Profile specification." The algorithm body was not read, so this memo does not claim that AuthzForce matches the CS01 branches line for line. Sun XACML predates the 2014 profile; no Sun implementation was fetched. AT&T was not searched for this URN.

### Options

1. Rewrite `combine()` to the CS01 §2.1 branches, including two or three children and the child-0 `Deny` / `Indeterminate{D}` else branch, then register that URN on the default provider.
2. Leave the class unregistered and remove the URN from `specs/combining-algorithm-registry/spec.md`. Core conformance does not require it.
3. Register the current body under the CS01 URN.

### Recommendation

Option 1 if the library is going to claim the profile. Option 3 is not available: the current `combine()` contradicts the pseudo-code on the legal three-child form, on `Deny` and `Indeterminate{D}`, and on which child is evaluated. Option 2 is compliant with Core and Errata. The change spec's requirement that the default provider resolve this URN (`combining-algorithm-registry/spec.md` lines 4-11) is a project requirement, not an OS requirement, and it is incomplete until the body matches §2.1. Missing registration today is not an OS or Errata violation.

Document the algorithm as optional profile behavior. Do not describe it as part of XACML 3.0 Core or Errata 01.

## FINAL REPORT

| Question | Recommendation | Confidence | Deciding citation |
| --- | --- | --- | --- |
| 1. `integer-divide` / `integer-mod` | Return `xs:integer`. Keep toward-zero and sign-of-dividend. Zero divisor stays Indeterminate. Do not cite A.3.2 as mandating truncation direction. | High on return type. Medium on direction and remainder sign. | XACML 3.0 OS §A.3.2: return "integer or double data-type, respectively"; integer-divide is "the first argument divided by the second argument" |
| 2. `or` / `and` / `n-of` / higher-order | A later True wins for `or` (and for `any-of` / `any-of-any`). A later False wins for `and` (and for `all-of` and the `and`-combined higher-order functions). Lazy-evaluate. The current annotation does not do that. Also fix the `any-of-all` quantifier. | High on the later deciding value. Medium on implicit all-False. | XACML 3.0 OS §A.3.5 `or` SHALL return True if any argument evaluates to True, and SHALL stop only on True; §A.3.12 combines with `or` or `and` |
| 3. Non-boolean Condition | Reject at load time. Treat IIC012 as the special-instruction static-rejection branch. | High | XACML 3.0 OS §5.26: the expression return data-type MUST be `xs:boolean`; `IIC012Special.txt` second branch |
| 4. NaN and signed zero | Keep `double-equal(NaN, NaN)` true as a recorded deviation. Make `+0.0` equal `-0.0`. Do not turn NaN equality off while IIC350 and IIC358 are enabled. | High | IEEE 754-1985 §5.7 (NaN unordered, `+0 = -0`) against `oasis30/IIC350` and `oasis30/IIC358`, which Permit on `double-equal(NaN, NaN)` |
| 5. `on-permit-apply-second` | CS01 optional profile, not Core. Do not register the current method. Register only a body that matches §2.1, or drop the URN from the change spec. | High on status and mismatch | Additional Combining Algorithms Profile CS01, 18 August 2014, §2.1 pseudo-code and §3 |

Verification: source reads and repository searches named above. OASIS HTML compared locally for OS and Errata 01. No Maven run. AuthzForce and Sun-lineage behavior is limited to the files and test fragments fetched. AT&T behavior was not found.

Residual risk: external callers of `DoubleVal divideInteger` are outside this tree. No conformance row was proved to encode Indeterminate-then-True, because not every logical policy was opened. `<=` / `>=` with NaN has no OASIS case. The AuthzForce remainder implementation and the AuthzForce combining-algorithm body were not read.
