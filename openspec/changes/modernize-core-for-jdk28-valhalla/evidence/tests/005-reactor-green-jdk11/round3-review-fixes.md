## Round 3 review fixes

Result: HIGH policy4j-the.4.8 and MEDIUM evaluation coverage are RESOLVED.
Work started after 1.3g: the round 2 fixes and run 53 gate were complete,
with the final audit retained as 56-final-review-evidence-audit.txt. The
conductor handles beads. No tracking, task or issue updates were performed.

Normative reference: [OASIS XACML 3.0 Standard](https://docs.oasis-open.org/xacml/3.0/xacml-3.0-core-spec-os-en.html),
A.3.12 and mandatory Table 10.2.8. any-of/all-of/map accept one or more
operands after the function reference, with exactly one bag in any position.
any-of-any permits primitive and bag operands and searches the Cartesian
product. The referenced function's primitive arity/types must match. map
returns a bag of that function's primitive return type. A.3.10/A.3.11 and
Tables 10.2.8/10.2.9 govern the duration identifiers and retained aliases.

The packet's primitive Ringo plus two bags supplies three operands to binary
string-equal; that shape is an arity error. The actual A.3.12 any-of-any
example has two bags (Ringo/Mary and John/Paul/George/Ringo). Both the
normative example and a valid mixed cross product using a three-argument
predicate are evaluated; the packet's binary three-operand form is rejected.

### Tests first and observed results

Run 58 did not reach tests: nine errors in new test bag construction were
corrected using the actual a8a90d46 ValueType API. These are listed below.
Production stayed unchanged. Run 59 is the behavior-red receipt: **155 tests,
4 failures, 24 errors, 0 skips**. 59-round3-red-tests.json records that the
higher-order and four conditional production files matched the pre-test
snapshot. The duration evaluation checks and the valid 1.0 controls already
pass in that red run; their expectations were kept.

After enabling mixed parameter metadata, run 61 isolates the invocation
failure: **3 tests, 0 failures, 1 error**. mixedValuesReachDeclaredExpressionArray
fails with ArrayStoreException when a BagOfValues is copied into a StringVal[]
allocated from the first operand. Run 61's XML/log is the red proof for the
conditional DefaultFunctionInvocation edit. Its receipt's source hashes were
captured after the implementation, and are not a pre-edit hash attestation.

Run 62 repeats the original focused command: **155/0/0/0, PASS**. Final full
run 63 repeats these tests and all modules: **914/0/0/9, BUILD SUCCESS**.
Runs 59/61/62/63 have zero compiler diagnostics. Stripped XML receipts and
per-test names are retained as 59-round3-red-*, 61-round3-packing-red-*,
62-round3-focused-* and 63-round3-green-*. No existing assertion was removed
or weakened; the identifier tests gained evaluation assertions.

| Finding / original file:line -> fix | Tests / final result |
| --- | --- |
| HIGH HigherOrderFunctions.java:54–110 -> variadic any-of/all-of/any-of-any annotations and bodies; substitute the single bag at its original position or iterate every bag's cross product. | HigherOrderVariadicFunctionsTest: anyOfSpecExample, anyOfThreeArgumentPredicate, anyOfBagFirst, anyOfPreservesEveryBagPosition, allOfSpecExample, allOfPreservesEveryBagPosition, unaryPredicateNeedsOnlyOneBag, variadicPredicateAcceptsMatchingOperandTypes, anyOfAnySpecExample, anyOfAnyMixedCrossProduct, anyOfAnyThreeBags, anyOfAnyAllowsAllPrimitiveOperands. All pass with runtime checks off/on. |
| HIGH HigherOrderFunctions.java:172–191 and :209–241 -> variadic map body, referenced primitive return type, matching operand signature and one-bag cardinality. | mapSpecExample, mapConcatenateWithPrimitive, mapBagFirstPreservesOrderAndMultiplicity, emptyBagsHaveIdentityResults; all pass off/on, including return type and duplicate bag members. |
| HIGH validation/runtime errors -> validators reject wrong bag count, arity, primitive type or referenced return type; evaluation raises EvaluationException and Condition returns INDETERMINATE. | anyOfNoBagIsSyntaxErrorAndIndeterminate, allOfNoBagIsSyntaxErrorAndIndeterminate, mapNoBagIsSyntaxErrorAndIndeterminate; corresponding TwoBags tests; missingOperandsAreSyntaxErrorAndIndeterminate, packetBinaryPredicateWithThreeOperandsIsArityError, referencedPredicateTypesAndReturnTypeAreChecked, mapRejectsBagReturningFunctionAtValidation. All pass off/on; syntax rejection and runtime Indeterminate are separately asserted. |
| Conditional parameter-spec scope: FunctionSpecBuilder.java:168–182 and FunctionParamValueTypeSequenceSpec.java:53–60, :92–121 required a single non-null element type; JavaMethodToFunctionSpecConverter.java:193–200 rejected an empty type id. -> add an explicit mixed-value sequence and recognize empty typeId on Expression varargs. Existing typed sequences retain their type validation. | HigherOrderVariadicInvocationTest.mixedValuesReachDeclaredExpressionArray and mixedSequenceStillChecksMinimumAndExcludesFunctionReferences both error in run 59 on invalid type id; metadata checks pass in run 61; run 63 all three tests pass. typedVariadicValidationIsUnchanged checks rejection of an integer in typed string varargs and successful valid invocation. |
| Conditional invocation scope: DefaultFunctionInvocation.java:102–108 and converter construction sites :231–242 -> allocate varargs with the Java method's declared component type; retain the existing two-argument constructor. | mixedValuesReachDeclaredExpressionArray: run 61 ArrayStoreException red -> runs 62/63 green, preserving argument order and both runtime modes. |
| MEDIUM DurationFunctionIdentifierTest.java:86 and HigherOrderFunctionIdentifierTest.java:81 lookup-only coverage -> add expected-result evaluation through Apply and compare each old/new alias. | Duration: 36 evaluation rows cover all 18 new 3.0 IDs and their 1.0 aliases. Higher-order: 11 evaluation rows cover four new IDs, four aliases and three unchanged 1.0-only methods. Each row runs off/on and asserts independent expected values plus identical alias results. All pass in runs 59/62/63. |

The two new classes contain 52 parameterized higher-order checks and three
invocation checks. The duration class now has 72 tests (36 lookup + 36
evaluation); the higher-order identifier class has 22 (11 + 11). All six
existing HigherOrderFunctionsTest tests pass unchanged, including strict mock
invocation counts. all-of-any, any-of-all and all-of-all remain 1.0-only and
fixed-arity; their method declarations, annotations and bodies are byte-identical
to the snapshot. The valid legacy n=2 and map n=1 cases evaluate identically.

### Duration evaluation inventory

Both dayTimeDuration (PT1S/PT2S/PT3S) and yearMonthDuration (P1M/P2M/P3M)
exercise the following operations through the default provider's Apply:

| Operation | Independent expected result |
| --- | --- |
| one-and-only | The singleton bag's value |
| bag-size | Two for {a,b} |
| is-in | True for b in {a,b} |
| bag | {a,b} from a,b |
| intersection | {b} from {a,b} and {b,c} |
| union | {a,b,c} from {a,b} and {b,c} |
| subset | True for {a} subset of {a,b} |
| at-least-one-member-of | True for {a,b} and {b,c} |
| set-equals | True for {a,b} and {b,a} |

Each operation runs for its 3.0 identifier and 1.0 alias with runtime checks
off/on. No duration bag/set method body was changed. This covers the requested
evaluation cases; it does not expand the inherited OASIS loop bounds.

### Run 58 compiler diagnostics -> test setup fixes

Every diagnostic was the same API mismatch: ValueType.bagOf requires Value[],
while the new setup passed String/Integer/Object[] values. All nine original
locations are retained in compile-diagnostics.json and 58-round3-before.txt.

| Original test file:line -> fix |
| --- |
| HigherOrderVariadicInvocationTest.java:58 -> bagOf(StringVal.of("a")) |
| HigherOrderVariadicFunctionsTest.java:347 -> STRING.bagBuilder().value(values).build() |
| HigherOrderVariadicFunctionsTest.java:352 -> INTEGER.bagBuilder().value(values).build() |
| HigherOrderFunctionIdentifierTest.java:115 -> STRING.bagBuilder().value("Hello", "World!").build() |
| HigherOrderFunctionIdentifierTest.java:116 -> STRING.bagBuilder().value("hello", "world!").build() |
| HigherOrderFunctionIdentifierTest.java:118 -> construct the four-name bag with bagBuilder().value(...) |
| HigherOrderFunctionIdentifierTest.java:121 -> construct the duplicate a/a bag with bagBuilder().value(...) |
| HigherOrderFunctionIdentifierTest.java:124, column 76 -> construct first singleton with bagBuilder().value("a") |
| HigherOrderFunctionIdentifierTest.java:124, column 106 -> construct second singleton with bagBuilder().value("a") |

### Record-only notes and scope

NonNumericComparisonFunctions.java:111 registers time-in-range under the 1.0
URN; the opened method has no legacy annotation. OASIS Table 10.2.8 lists the
2.0 URN. BaseFunctionProvider.java:83–86 merge(a,b) returns a, silently keeping
the first duplicate. Both are recorded only; their files match the snapshot.
The prior round 2 IIIE301–303 proposals and selector/fraction/advice gaps remain
open. Those additional direct diagnostics were not rerun in round 3.

Exactly nine core source/test files changed in this round:

1. policy/function/impl/HigherOrderFunctions.java
2. policy/function/FunctionSpecBuilder.java
3. policy/function/FunctionParamValueTypeSequenceSpec.java
4. policy/function/impl/JavaMethodToFunctionSpecConverter.java
5. policy/function/impl/DefaultFunctionInvocation.java
6. test policy/function/impl/HigherOrderVariadicFunctionsTest.java (new)
7. test policy/function/impl/HigherOrderVariadicInvocationTest.java (new)
8. test policy/function/impl/DurationFunctionIdentifierTest.java
9. test policy/function/impl/HigherOrderFunctionIdentifierTest.java

The four conditional classes are required by the recorded metadata/packing red
tests. No other function semantics were changed. scope-check.txt compares 3535
protected files, including fixtures, prior arithmetic/XML/selector code, POMs,
other providers, accepted run 003 and conformance suite sources. Exclusions
remain 30 per OASIS suite and all existing group bounds remain. Full configured
case counts: OASIS 2.0 347/0, OASIS 3.0 347/0, RSA 17/0, examples 2/0; no new
failed case ID. The previous global diff warning at ExpressionTypeBuilder.java:96
belongs to preserved other-lane work; the scoped round 3 diff check passes.
