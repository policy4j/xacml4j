# Run 076: type-bag functions without arguments (bead the.4.53)

Date: 2026-10-07. Source of the bead: rv078, Opus finding 1. JDK: Oracle
GraalVM 21.0.6+8.1 (`java version "21.0.6" 2025-01-21 LTS`). Normative
text: XACML 3.0 Plus Errata 01, A.3.10, from the OASIS copy fetched for run
074 (SHA-256 of the HTML file
`d396daed751bc19eb5882fe1e83dacb9bc9f8af1dd54e2e8e4141e717eef9d44`):
"An application of this function to zero arguments SHALL produce an empty
bag of the specified data-type." Review:
`reviews/code/rv084-zero-argument-bags-codex`.

## Defect, reproduced through the registry

`FunctionSpec.invoke(context)` with no argument, for each registered bag id:

| Bag ids | Before | After |
| --- | --- | --- |
| `string`, `boolean`, `integer`, `time`, `date`, `dateTime`, `anyURI`, `hexBinary`, `base64Binary`, `x500Name`, `rfc822Name` (1.0), `dayTimeDuration`, `yearMonthDuration` (3.0 and the 1.0 legacy ids), `ipAddress`, `dnsName` (2.0), `entity` (1.0): 18 ids | `FunctionInvocationException` caused by `NullPointerException` (Indeterminate) | empty bag of the type; `type-bag-size` of it is integer 0 |
| `double` | empty bag (local `null` check from task 4.6) | unchanged |
| `Apply` of `string-bag` without arguments | `FunctionInvocationException` | empty string bag |

## Cause

`FunctionSpecImpl.invokeWithList` normalizes the argument list first. For a
variadic parameter without arguments the normalized list holds a single
`null`. `DefaultFunctionInvocation.invoke` built an array with that one
`null` and then passed `null` instead of the array:

```java
params[params.length - 1] = (varArgArray != null && varArgArray.length == 1 &&
        varArgArray[0] == null)?null:varArgArray;
```

## Change

| File | Change |
| --- | --- |
| `xacml-core/.../function/impl/DefaultFunctionInvocation.java` | When there is no variadic argument (no array, or the array with the single `null`) and the variadic parameter allows zero arguments (`spec.getParamSpecAt(last).isOptional()`, true exactly for `min=0`), the method gets `Array.newInstance(varArgComponentType, 0)`. A parameter with a minimum, or a component type that is not known (the two-argument constructor, used by tests), still gets `null`. |
| `xacml-core/src/test/.../function/impl/ZeroArgumentBagRegistryTest.java` (new, 22 tests) | 19 bag ids through the registry: the result is a bag, is empty, has the declared return type of the function, and `<id>-size` of it is integer 0. One test through `Apply`. `string-bag` with one and two arguments keeps its values. `integer-add()` and `any-of(f)` without their required arguments still fail with processing-error and syntax-error. |

The `null` checks in `BagFunctions.doubleBag` and in `LogicalFunctions`
(`or`, `and`, `n-of`, run 074) stay; they are no longer reached through the
registry.

## Tests

- Red first: `red-surefire-summary.txt` (20 tests, 19 errors; only
  `double-bag` passes).
- Gate: `surefire-summary.txt`. Full reactor on JDK 21: 7,745 tests, 0
  failures, 0 errors, 22 skipped. Conformance: 937 tests with 12 skipped,
  the counts of run 075.
- The gate tree also held run 074.

## Spec

No spec delta: the requirement already exists in A.3.10 and the change
restores it. `function-semantics` has no requirement that names the
defect.

## Review

Codex, two rounds, both APPROVE with no defect
(`reviews/code/rv084-zero-argument-bags-codex`). Between the rounds the
author narrowed the fix to variadic parameters that allow zero arguments
(see Limits). Round 1 asked for a test with one and two arguments; round 2
asked for `or()` and `and()` through the registry, which
`LaterDecidingOperandTest` of run 074 has (rows `or ""` and `and ""`).

## JMH

NOT APPLICABLE: one branch on the path of a call without variadic
arguments; calls with arguments run the same code as before.

## Limits

- A variadic function that is declared with an array parameter instead of
  `...` has no component type in `JavaMethodToFunctionSpecConverter`
  (`m.isVarArgs()` is false) and would still get `null`. A search of the
  main code of xacml-core and xacml-pdp found no function declared that
  way.
- Argument counts are not checked before the call unless runtime
  validation is on (`isValidateFuncParamsAtRuntime()`, off by default),
  and no production code calls `FunctionSpec.validateParametersAndThrow`
  when a policy is loaded. By reading the code (not run), a variadic
  function with a minimum that gets fewer arguments than its minimum but
  at least one (`integer-add(5)`) computes a value; that is not changed
  here and is tracked as bug policy4j-the.4.56. With no argument at all
  such a function still fails, because its parameter keeps `null`. The
  first version of this fix would have made `integer-add()` return 0; the
  author found that after review round 1 and narrowed the fix.
