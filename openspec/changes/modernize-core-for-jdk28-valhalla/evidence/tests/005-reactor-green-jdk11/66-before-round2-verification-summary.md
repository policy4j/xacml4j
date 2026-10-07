# Task 1.3 continued: full reactor green on JDK 11

Result: **PASS for the configured full reactor** on JDK 11: **914 Surefire tests,
0 failures, 0 errors, 9 existing skips**. Round 3's HIGH and MEDIUM findings and the prior round 2 fixes pass
with recorded red/green tests. OASIS 2.0 and 3.0 each pass 347 case executions; all 17
RSA cases and both examples pass. Prior round 2 direct IIIE301–303 checks outside the unchanged bounds failed
identically before/after; proposals remain open. They were not rerun in round 3.

JDK: `openjdk version "11.0.32.1" 2026-08-18 LTS`;
Zulu11.90+205-CA, build 11.0.32.1+1-LTS. Fresh raw version line: 64-jdk-version.txt.
Fresh final-gate Surefire JVM properties: 63-jdk-properties.json; direct version: 64-jdk-version.txt.
Branch: integration/modernize-core-jdk28-valhalla; HEAD
befdd116bd11385ac8d007732fa8e4d347af3143; core API base a8a90d46.

## Final full-reactor gate

```sh
JAVA_HOME="$(jenv prefix 11)" rtk mvn -Dlicense.skipUpdateLicense=true verify -l openspec/changes/modernize-core-for-jdk28-valhalla/evidence/tests/005-reactor-green-jdk11/63-reactor-verify-round3.txt
```

| Module | Tests run | Failures | Errors | Skipped | Reactor result |
| --- | ---: | ---: | ---: | ---: | --- |
| xacml parent | 0 | 0 | 0 | 0 | SUCCESS; no tests |
| xacml-core | 740 | 0 | 0 | 4 | SUCCESS |
| xacml-xml-jaxb | 0 | 0 | 0 | 0 | SUCCESS; no tests |
| xacml-xml | 34 | 0 | 0 | 0 | SUCCESS |
| xacml-json | 5 | 0 | 0 | 1 | SUCCESS |
| xacml-pdp | 74 | 0 | 0 | 4 | SUCCESS |
| xacml-test | 20 | 0 | 0 | 0 | SUCCESS |
| xacml-conformance-tests | 41 | 0 | 0 | 0 | SUCCESS |
| Total | 914 | 0 | 0 | 9 | BUILD SUCCESS |

These are fresh run 63 Maven Results, retained in surefire-summary.txt and
63-module-counts.json. Surefire counts grouped conformance methods; individual
case counts below come from their retained production execution output.

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


## Review fixes (round 2, retained)

Both MEDIUM findings are repaired. Focused tests were written and run before
production edits. Run 48 has 97 arithmetic tests / 35 failures; run 49 has
24 selector/expansion tests / 1 failure. The unchanged production hashes were
checked when recording those red reports in 49-review-red-tests.json.
Runs 51 and 52 repeat the same commands: 97/97 and 24/24 pass.
Run 53 repeats them in the full JDK 11 reactor: 812 tests / 0 failures /
0 errors / 9 existing skips. Compiler diagnostics: zero in runs 48–53.

| Finding / original file:line -> repair | Tests written first | Red -> green |
| --- | --- | --- |
| MEDIUM context node; DefaultEvaluationContextHandler.java:127–134 before review -> branch only on a null ContextSelectorId; resolve directly against Content in that branch. Keep entity.resolve for an explicit ID. | DefaultEvaluationContextHandlerTest.testRelativePathWithoutContextSelectorUsesDocumentNode changes the requested relative-path assertion to an empty integer bag; testRelativePathWithExplicitContextSelectorUsesSelectedNode asserts 555555 with the same path and explicit ID. | No-ID test fails in run 49 (555555 instead of empty bag); explicit-ID test already passes. Both pass in runs 52/53. |
| MEDIUM fractional seconds; ISO8601DateTime.java:162–184 and DateTimeDurationUtils.java:210–228, :249–259 before review -> retain BigDecimal SECONDS during lexical parsing and XML-to-Java conversion, then apply signed whole seconds plus nanos through plus/minus(java.time.Duration). | ISO8601FractionalDateTimeArithmeticTest.addPreservesFractionalSeconds and subtractPreservesFractionalSeconds; ISO8601DurationReviewTest's four fractional preservation methods. | All 30 parameterized arithmetic checks and all four fraction-preservation methods fail in run 48; all pass in runs 51/53. |
| Conditional reviewer note, programmatic P14M; DateTimeDurationUtils.java:244 before review -> add normalized d.getMonths(), rather than the original 14 months, to the normalized year count. | ISO8601DurationReviewTest.programmaticFourteenMonthsAreCountedOnce checks raw getMonths()==14, conversion to +14/-14 total months, and dateTime add/subtract. | Run 48: expected 14, actual 26. Runs 51/53: pass. ISO8601YearMonthDuration itself is unchanged. |

The fraction matrix covers PT1.5S, PT0.5S and PT0.001S, each positive and
negative, addition and subtraction, both whole-second and .250 base values.
It also covers month-end rollover, combined days/hours/minutes/seconds,
offset preservation and a one-nanosecond duration. Round-trip, decimal
equality, hash equivalence, ordering and XML-to-Java conversion checks pass.
53-review-green-tests.json and the stripped green XML reports retain every
test name. 53-review-fixes.patch contains exactly this review's six-file delta.

Full operation inventory and retained review gaps: review-fixes.md.
Additional case proposals: review-proposals.md.

## Approved identifier repairs, tests first

The conductor approved required-duration-bag-alias-fix.patch and expanded
scope to missing/wrong identifiers and aliases only. The requested family
scan found ten related duration set names and, after IIC164 was reached,
four higher-order names. Each of the 22 added 3.0 identifiers and retained
1.0 aliases is listed with its original file:line, focused test and normative
reference in identifier-audit.md and 46-identifier-audit.json. The original
1.0 annotations are confirmed at a8a90d46 in 46-identifier-baseline.txt.

| Error / original source location -> fix | Red receipt | Green receipt |
| --- | --- | --- |
| IIC150 import; BagFunctions.java:491/499/507/516/529/538/547/557 -> exact approved eight 3.0 duration bag annotations plus eight 1.0 legacy aliases. | Run 39 before either duration provider edit: 36 failures (18 missing 3.0 names, 18 canonical/legacy metadata expectations). | Run 41: all 42 focused tests pass; run 46 repeats all 36 duration lookup checks and evaluates IIC150 successfully. |
| Same-family SetFunctions.java:585/594/603/612/621/630/639/648/657/666 -> ten 3.0 duration set annotations plus ten 1.0 legacy aliases. | Same run 39. | All duration lookup checks pass in runs 41/46. Later duration-set conformance cases beyond the unchanged loop bounds remain UNVERIFIED. |
| Run 40 SetFunctions.java:586/596/606/616/626/636/646/656/666/676 -> add import for XacmlLegacyFunc; every compiler diagnostic retained in compile-diagnostics.json. | Temporary edit compile failure, ten locations. | Run 41 and final gate compile without diagnostics. |
| IIC164 import; HigherOrderFunctions.java:54/72/90/169 -> four 3.0 names (any-of, all-of, any-of-any, map), plus four 1.0 legacy aliases. | Run 43 directly reproduces unchanged IIC164 import; run 44 before production edit: 11 tests / 8 failures / 3 passes. | Run 45: all 53 focused tests pass; run 46 repeats all 11 registry checks and evaluates IIC164 successfully. |

During the prior identifier-only continuation, method bodies and type/parameter
annotations matched the captured pre-write source. Round 3's authorized higher-order
semantic and parameter adaptations are recorded above; BagFunctions/SetFunctions
bodies remain unchanged. Bag aliases match the approved patch byte for byte.
Identifier tests use the default production FunctionProvider and assert
presence, canonical/legacy metadata and object identity for each old/new name.
No assertion, expected result or fixture was changed.

References: [OASIS XACML 3.0 Standard](https://docs.oasis-open.org/xacml/3.0/xacml-3.0-core-spec-os-en.html),
Appendix A.3.10/A.3.11 for bag/set names, A.3.12/A.4 for higher-order names,
Tables 10.2.8/10.2.9 for the duration versioned names. No type or algorithm
identifier needed a repair; no production semantic change was made in this
identifier continuation. Decisions and receipts: identifier-approval.md.

## Conformance case totals and dispositions

| Suite | Surefire methods | Cases executed | Failed case IDs |
| --- | ---: | ---: | --- |
| OASIS 2.0 | 10 | 347 | None |
| OASIS 3.0 | 10 | 347 | None |
| RSA 2008 interop | 17 | 17 | None |
| Examples | 2 | 2 | None |
| Resolver characterization | 2 | Not conformance cases | None |

OASIS/RSA: **711 executed, 711 passed, 0 failed**. Including examples:
**713 executed, 713 passed**. Each OASIS suite invokes IIIG006 twice, as before.
63-conformance-testcases.json records every method, emitted case ID and
failure field. IIC232 is reached by both suites; IIC150 and IIC164 pass.

The 30 explicit case exclusions in each OASIS suite (60 total), inherited
group bounds, RSA performance exclusion and Surefire's 9 skips are unchanged.
The exclusion sets are extracted again and compared to run 36 in
63-existing-case-exclusions.json, compared to runs 36/53. Suite and fixture hashes are unchanged.
The passed gate certifies the currently configured coverage, not all vendored
cases. The XACML 3.0 IIC loop still stops before 233; later duration set cases
IIC340-IIC349 are outside that loop and were not added to this task's gate.

| Previously observed failure | Classification / cause | Final disposition |
| --- | --- | --- |
| IIC104, both suites | Pre-existing core day-time subtraction sign defect | PASS; earlier approved sign restoration retained. |
| IIC106/IIC107, both suites | Pre-existing date/year-month conversion defect | PASS after approved Period fix. |
| IIIF001 and both IIIG006 invocations, OASIS 3.0 | Pre-existing XML whitespace/content selection defect | PASS after approved content mapper fix. |
| IIA001/IIB001/IIC001/IID001/IIE001/IIIA006 errors, run 22 | Pre-existing harness/test-support optional collection comparison defects | PASS; owned comparator repair retains assertions. |
| IIC150, run 36 | Pre-existing core duration bag registration; unchanged policy's required 3.0 name absent | PASS after conductor-approved aliases. |
| IIC164, run 42 | Pre-existing core higher-order registration; unchanged policy's required 3.0 any-of name absent | PASS after approved identifier-class repair. |

Import failures print no execution marker: IIC149 in run 36 and IIC163 in
run 42 are the last successful markers, not the failing cases. Actual failed
IDs were directly reproduced by production imports in runs 37 and 43.
No adaptation regression is shown by the configured gate. Additional IIIE301,
IIIE302 and IIIE303 checks each fail before/after the review edits; they are
classified as fixture compatibility, pre-existing harness ordering, and a
pre-existing JAXB IDREF defect. They were not part of prior green-suite coverage.
conformance-blockers.md and review-proposals.md retain these pending proposals.

## Prior approved arithmetic checks retained

The conductor approved required-date-period-fix.patch and expanded the lane
to the date/time/duration arithmetic class of defects. The decision is
recorded in `date-arithmetic-approval.md`. The prior approved JUnit dependency
and core/XML restoration decisions persist; no further POM edits were made.

`34-date-arithmetic-before.txt` records tests written before either new
production fix: 65 tests, **20 failures**, no errors or skips. The new
50-case DateTimeArithmeticBoundaryTest has 16 nonzero date failures and
four combined year/month dateTime boundary failures. The earlier 12 signed
type tests and three function-provider tests pass.

| Original source location -> fix | Red characterization | Green verification |
| --- | --- | --- |
| `ISO8601Date.java:145–150` -> use `duration.toPeriod()` in both plus/minus, exactly the approved patch. Year/month operands were converted to day/time PT0S. | DATE_ADD_YEAR_MONTH and DATE_SUBTRACT_YEAR_MONTH: eight nonzero cases fail in each; zero cases pass. | All 18 date cases pass in run 35 and the full gate. |
| `ISO8601DateTime.java:158`, `:182` before this turn -> use the signed Period in both year/month operations. Separate year/month adjustments clipped February 29 before the final month was known. | DATETIME_ADD_YEAR_MONTH and DATETIME_SUBTRACT_YEAR_MONTH each fail both P1Y1M directions from 2020-02-29; expected March 29 / January 29, actual day 28. | All 18 dateTime year/month cases pass in run 35 and the full gate. |

The second fix is within the newly approved arithmetic class and is backed
by the requested month-end characterization. It is an additional pre-existing
calendar defect found by those tests, not a newly observed conformance case.
The prior signed subtraction behavior is retained and repeated by the review tests.

`35-date-arithmetic-after.txt`: all **65 tests pass**, no failures/errors/skips.
`36-reactor-verify-date-arithmetic.txt`: the full core module repeats all of
them successfully. No duration/time class or function-provider production
class needed a change.

## Every operation checked and its test

Each row below is tested through DateTimeArithmeticFunctions by
`DateTimeArithmeticBoundaryTest.arithmeticPreservesSignedCalendarBoundaries`.
The parameter label uniquely identifies the operation and exact operands.
`36-arithmetic-operation-tests.json` lists all 50 checks, expected values,
full test names and observed red/green results. `arithmetic-operations.md`
also lists the twelve prior type-level test names.

| Function / type operation | Test parameter | Checks | Red failures | Green |
| --- | --- | ---: | ---: | --- |
| date-add-yearMonthDuration / ISO8601Date.plus | DATE_ADD_YEAR_MONTH | 9 | 8 | 9 pass |
| date-subtract-yearMonthDuration / ISO8601Date.minus | DATE_SUBTRACT_YEAR_MONTH | 9 | 8 | 9 pass |
| dateTime-add-yearMonthDuration / ISO8601DateTime.plus | DATETIME_ADD_YEAR_MONTH | 9 | 2 | 9 pass |
| dateTime-subtract-yearMonthDuration / ISO8601DateTime.minus | DATETIME_SUBTRACT_YEAR_MONTH | 9 | 2 | 9 pass |
| dateTime-add-dayTimeDuration / ISO8601DateTime.plus | DATETIME_ADD_DAY_TIME | 7 | 0 | 7 pass |
| dateTime-subtract-dayTimeDuration / ISO8601DateTime.minus | DATETIME_SUBTRACT_DAY_TIME | 7 | 0 | 7 pass |

Every operation covers positive, negative and zero durations. Calendar
operations also cover leap/non-leap February month ends, both directions,
negative equivalents and combined periods starting at February 29. Day-time
operations cover month/leap-day rollover and retain the original offset.

The OASIS XACML 3.0 Standard Appendix A.3.7 defines these six operations;
it defines neither time duration arithmetic nor date day-time arithmetic.
The opened production function class provides the same six. Those absent
operations are not added. XACML delegates arithmetic to XML Schema Appendix
E; E.1 applies the combined calendar adjustment before clipping the day.
See `arithmetic-reference.md` and the official sources:
[OASIS XACML 3.0](https://docs.oasis-open.org/xacml/3.0/xacml-3.0-core-spec-os-en.html),
[XML Schema Part 2 Appendix E](https://www.w3.org/TR/xmlschema-2/#adding-durations-to-dateTimes).

## Changed project files in the continued lane

The continued task 1.3 lane changed **32 project files** plus run 005 evidence.
Round 3 changes nine core source/test files, listed in round3-review-fixes.md and
scope-check.txt. The prior 26-file inventory below remains part of this lane;
the four parameter/invocation source files and two new tests add six files.
Accepted XML repairs, the approved POM and original run 003 are preserved.

| File | Change |
| --- | --- |
| `xacml-core/src/main/java/org/xacml4j/v30/types/ISO8601Date.java` | Approved two-line year/month Period conversion. |
| `xacml-core/src/main/java/org/xacml4j/v30/types/ISO8601DateTime.java` | Signed combined Period for year/month; signed Duration with nanos for day-time add/subtract. |
| `xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/DateTimeArithmeticBoundaryTest.java` | New 50-case test through all six production arithmetic entrypoints. |
| `xacml-core/src/test/java/org/xacml4j/v30/types/ISO8601DateTimeArithmeticTest.java` | Prior twelve-test signed/zero truth table for four type operations. |
| `xacml-xml/src/main/java/org/xacml4j/v30/xml/Xacml30RequestContextFromJaxbToObjectModelMapper.java` | Previously approved whitespace-only mixed-content filter; unchanged this turn. |
| `xacml-xml/src/test/java/org/xacml4j/v30/xml/Xacml30RequestContentTest.java` | Prior five-test production content-unmarshaller characterization. |
| `xacml-pdp/src/main/java/org/xacml4j/v30/pdp/DefaultEvaluationContextHandler.java` | Entity API, designator overload, Optional contract; every no-ContextSelectorId path uses the Content document. |
| `xacml-pdp/src/main/java/org/xacml4j/v30/pdp/request/MultipleResourcesViaXPathExpressionHandler.java` | Path, Entity.attributes(), ValueType extraction. |
| `xacml-pdp/src/main/java/org/xacml4j/v30/pdp/request/MultipleResourcesViaXPathExpressionLegacyHandler.java` | Entity.findValues() and attributes(). |
| `xacml-pdp/src/test/java/org/xacml4j/v30/pdp/DefaultEvaluationContextHandlerTest.java` | Selector, issuer/duplicate/Entity and PIP/cache tests; review document/explicit context and two retained cardinality-gap checks. |
| `xacml-pdp/src/test/java/org/xacml4j/v30/pdp/request/MultipleResourcesViaXacmlXPathHandlerTest.java` | RequestContext.getAttribute(); metadata-preserving expansion. |
| `xacml-test/pom.xml` | Exact conductor-approved compile-scope junit:junit:4.13.2 patch; unchanged this turn. |
| `xacml-test/src/main/java/org/xacml4j/v30/ExpectedAttributeResolverBuilder.java` | getEvaluatesTo().getValueType(). |
| `xacml-test/src/test/java/org/xacml4j/v30/ExpectedAttributeResolverBuilderTest.java` | Two production resolver/multiplicity characterizations. |
| `xacml-test/src/main/java/org/xacml4j/v20/Xacml30TestUtility.java` | Normalize absent optional collections to empty; compare actual obligations. |
| `xacml-test/src/test/java/org/xacml4j/v20/Xacml30TestUtilityTest.java` | Fourteen empty/mismatch and production response-marshalling tests. |
| `xacml-conformance-tests/src/test/java/org/xacml4j/v20/Xacml20ConformanceAttributeResolver.java` | StringVal and explicit BagOfValues scalar cast; preserve subject/role behavior. |
| `xacml-conformance-tests/src/test/java/org/xacml4j/v20/Xacml20ConformanceAttributeResolverTest.java` | Two mixed-case/unmatched-subject tests. |
| `xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/BagFunctions.java` | Eight standard 3.0 duration bag names; retain eight 1.0 aliases; exact approved patch. |
| `xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/SetFunctions.java` | Ten standard 3.0 duration set names; retain ten 1.0 aliases; add annotation import. |
| `xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/HigherOrderFunctions.java` | Four standard 3.0 names and retained 1.0 aliases; round 3 adds variadic semantics and validation. |
| `xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/DurationFunctionIdentifierTest.java` | 36 registry checks plus 36 evaluation rows comparing standard/legacy results with both runtime modes. |
| `xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/HigherOrderFunctionIdentifierTest.java` | 11 registry checks plus 11 evaluation rows comparing standard/legacy results with both runtime modes. |
| `xacml-core/src/main/java/org/xacml4j/util/DateTimeDurationUtils.java` | Decimal day-time parsing/conversion; normalize P14M once. |
| `xacml-core/src/test/java/org/xacml4j/v30/types/ISO8601FractionalDateTimeArithmeticTest.java` | Thirty signed/fractional add/subtract checks through production functions. |
| `xacml-core/src/test/java/org/xacml4j/v30/types/ISO8601DurationReviewTest.java` | Five decimal round-trip/equality/conversion and P14M checks. |

The xacml-test POM change is the exact conductor-approved
junit:junit:4.13.2 compile-scope patch. No other POM edit was made. Original
continued compiler errors -> fixes follow below; all raw diagnostics remain
in compile-diagnostics.json, including the temporary missing import in run 40.

## Evidence, scope and remaining risk

command.txt retains exact commands; surefire-summary.txt includes the original
history, focused red/green Results, and all six final run 63 Maven Results.
Paths are normalized to <repo>. 46-before-review-* archives preserve the prior
green state. Original run 003's fourteen files match their protected hashes.

scope-check.txt verifies 3,535 protected hashes in round 3, including all 2,906
vendored conformance resources; zero changed. Seven edited source headers remain
unchanged; two new headers match the template. Exactly nine authorized core files
changed/appeared. The full continued lane inventory is 32 files; the prior scope
proof is archived as 57-before-round3-scope-check.txt.
No assertion was removed or weakened. The one relative-path expectation was
changed as explicitly requested; all fixture/expected-response bytes and
OASIS exclusions/bounds are unchanged. Git has no staged changes; other-lane
fixture restorations, XML work, evidence and the prior JSON deletion remain.

Residual risk: IIIE301–303 direct checks need the three concrete proposals in
review-proposals.md; they fail identically before/after. Zero/two-node context
selector syntax-error behavior is unsupported and recorded without changing
XmlContent. Other fractional truncation persists in the integer getSeconds /
isZero path and Java-to-XML duration factory; 55-fractional-duration-gaps.txt
records runtime examples. Sub-nanosecond arithmetic is rejected rather than
rounded. Advice comparison is a pre-existing harness gap; mixed Content that
starts with a comment/processing instruction is UNVERIFIED. Inherited coverage
limits and tasks 4.5/4.6 function semantic questions remain. Detailed review
tests, all checked operations and notes are in review-fixes.md.
No commit, br command, task tick, issue closure or evidence-index edit was made.

# Compiler errors and fixes

Locations are the compiler's original locations, including temporary test insertions.
Repeated errors from reruns are retained in compile-diagnostics.json and raw logs.

## PDP diagnostics (14)

| Compiler file:line:column | Diagnostic | Fix |
| --- | --- | --- |
| `xacml-pdp/src/main/java/org/xacml4j/v30/pdp/request/MultipleResourcesViaXPathExpressionHandler.java:46:29` | cannot find symbol | Import and cast public Path instead of removed top-level PathValue. |
| `xacml-pdp/src/main/java/org/xacml4j/v30/pdp/request/MultipleResourcesViaXPathExpressionLegacyHandler.java:68:54` | cannot find symbol | Use Entity.findValues(id, type); preserve filtering/cardinality. |
| `xacml-pdp/src/main/java/org/xacml4j/v30/pdp/request/MultipleResourcesViaXPathExpressionLegacyHandler.java:90:56` | no suitable method found for find(no arguments) | Iterate Entity.attributes(). |
| `xacml-pdp/src/main/java/org/xacml4j/v30/pdp/DefaultEvaluationContextHandler.java:49:7` | org.xacml4j.v30.pdp.DefaultEvaluationContextHandler is not abstract and does not override abstract method getEntity(org.xacml4j.v30.CategoryId) in org.xacml4j.v30.policy.EvaluationContextHandler | Implement Optional<Entity> getEntity(CategoryId). |
| `xacml-pdp/src/main/java/org/xacml4j/v30/pdp/DefaultEvaluationContextHandler.java:131:81` | incompatible types: org.xacml4j.v30.AttributeDesignatorKey cannot be converted to org.xacml4j.v30.AttributeSelectorKey | Resolve designators by attribute id, datatype and issuer. |
| `xacml-pdp/src/main/java/org/xacml4j/v30/pdp/DefaultEvaluationContextHandler.java:132:61` | incompatible types: java.util.Optional<java.lang.Object> cannot be converted to java.util.Optional<org.xacml4j.v30.BagOfValues> | Cascading Optional inference error resolved by the designator overload fix. |
| `xacml-pdp/src/main/java/org/xacml4j/v30/pdp/DefaultEvaluationContextHandler.java:180:48` | cannot find symbol | Cast the single bag value directly to Entity. |
| `xacml-pdp/src/main/java/org/xacml4j/v30/pdp/DefaultEvaluationContextHandler.java:181:48` | cannot find symbol | Remove EntityValue.get(); Entity is the value. |
| `xacml-pdp/src/main/java/org/xacml4j/v30/pdp/DefaultEvaluationContextHandler.java:196:48` | name clash: getEntity(org.xacml4j.v30.CategoryId) in org.xacml4j.v30.pdp.DefaultEvaluationContextHandler and getEntity(org.xacml4j.v30.CategoryId) in org.xacml4j.v30.policy.EvaluationContextHandler have the same erasure, yet neither overrides the other | Delegate Optional<Entity> getEntity to the request callback. |
| `xacml-pdp/src/main/java/org/xacml4j/v30/pdp/request/MultipleResourcesViaXPathExpressionHandler.java:119:47` | cannot find symbol | Stream Entity.attributes(). |
| `xacml-pdp/src/main/java/org/xacml4j/v30/pdp/request/MultipleResourcesViaXPathExpressionHandler.java:145:81` | incompatible types: org.xacml4j.v30.ValueExpTypeInfo cannot be converted to org.xacml4j.v30.types.ValueType | Use path.getEvaluatesTo().getValueType(). |
| `xacml-pdp/src/main/java/org/xacml4j/v30/pdp/request/MultipleResourcesViaXPathExpressionHandler.java:146:49` | incompatible types: inference variable T has incompatible bounds | Cascading inference error resolved by the transform value-type fix. |
| `xacml-pdp/src/main/java/org/xacml4j/v30/pdp/request/MultipleResourcesViaXPathExpressionHandler.java:163:36` | no suitable method found for find(no arguments) | Iterate Entity.attributes(). |
| `xacml-pdp/src/test/java/org/xacml4j/v30/pdp/request/MultipleResourcesViaXacmlXPathHandlerTest.java:184:78` | cannot find symbol | Use RequestContext.getAttribute(category, id); keep value/path assertions. |

## JUnit main-source diagnostics (24)

| Compiler file:line:column | Diagnostic | Fix |
| --- | --- | --- |
| `xacml-test/src/main/java/org/xacml4j/v20/Xacml30TestUtility.java:25:24` | package org.junit does not exist | Apply conductor-approved junit:junit:4.13.2 compile-scope dependency; all assertion/lifecycle code retained. |
| `xacml-test/src/main/java/org/xacml4j/v20/Xacml30TestUtility.java:25:1` | static import only from classes and interfaces | Apply conductor-approved junit:junit:4.13.2 compile-scope dependency; all assertion/lifecycle code retained. |
| `xacml-test/src/main/java/org/xacml4j/v20/Xacml30TestUtility.java:26:24` | package org.junit does not exist | Apply conductor-approved junit:junit:4.13.2 compile-scope dependency; all assertion/lifecycle code retained. |
| `xacml-test/src/main/java/org/xacml4j/v20/Xacml30TestUtility.java:26:1` | static import only from classes and interfaces | Apply conductor-approved junit:junit:4.13.2 compile-scope dependency; all assertion/lifecycle code retained. |
| `xacml-test/src/main/java/org/xacml4j/v20/Xacml30TestUtility.java:27:24` | package org.junit does not exist | Apply conductor-approved junit:junit:4.13.2 compile-scope dependency; all assertion/lifecycle code retained. |
| `xacml-test/src/main/java/org/xacml4j/v20/Xacml30TestUtility.java:27:1` | static import only from classes and interfaces | Apply conductor-approved junit:junit:4.13.2 compile-scope dependency; all assertion/lifecycle code retained. |
| `xacml-test/src/main/java/org/xacml4j/v30/AttributeResolverTestSupport.java:27:24` | package org.junit does not exist | Apply conductor-approved junit:junit:4.13.2 compile-scope dependency; all assertion/lifecycle code retained. |
| `xacml-test/src/main/java/org/xacml4j/v30/AttributeResolverTestSupport.java:27:1` | static import only from classes and interfaces | Apply conductor-approved junit:junit:4.13.2 compile-scope dependency; all assertion/lifecycle code retained. |
| `xacml-test/src/main/java/org/xacml4j/v30/AttributeResolverTestSupport.java:28:24` | package org.junit does not exist | Apply conductor-approved junit:junit:4.13.2 compile-scope dependency; all assertion/lifecycle code retained. |
| `xacml-test/src/main/java/org/xacml4j/v30/AttributeResolverTestSupport.java:28:1` | static import only from classes and interfaces | Apply conductor-approved junit:junit:4.13.2 compile-scope dependency; all assertion/lifecycle code retained. |
| `xacml-test/src/main/java/org/xacml4j/v30/AttributeResolverTestSupport.java:29:24` | package org.junit does not exist | Apply conductor-approved junit:junit:4.13.2 compile-scope dependency; all assertion/lifecycle code retained. |
| `xacml-test/src/main/java/org/xacml4j/v30/AttributeResolverTestSupport.java:29:1` | static import only from classes and interfaces | Apply conductor-approved junit:junit:4.13.2 compile-scope dependency; all assertion/lifecycle code retained. |
| `xacml-test/src/main/java/org/xacml4j/v30/XacmlPolicyTestSupport.java:25:24` | package org.junit does not exist | Apply conductor-approved junit:junit:4.13.2 compile-scope dependency; all assertion/lifecycle code retained. |
| `xacml-test/src/main/java/org/xacml4j/v30/XacmlPolicyTestSupport.java:25:1` | static import only from classes and interfaces | Apply conductor-approved junit:junit:4.13.2 compile-scope dependency; all assertion/lifecycle code retained. |
| `xacml-test/src/main/java/org/xacml4j/v30/XacmlPolicyTestSupport.java:26:24` | package org.junit does not exist | Apply conductor-approved junit:junit:4.13.2 compile-scope dependency; all assertion/lifecycle code retained. |
| `xacml-test/src/main/java/org/xacml4j/v30/XacmlPolicyTestSupport.java:26:1` | static import only from classes and interfaces | Apply conductor-approved junit:junit:4.13.2 compile-scope dependency; all assertion/lifecycle code retained. |
| `xacml-test/src/main/java/org/xacml4j/v30/XacmlPolicyTestSupport.java:34:17` | package org.junit does not exist | Apply conductor-approved junit:junit:4.13.2 compile-scope dependency; all assertion/lifecycle code retained. |
| `xacml-test/src/main/java/org/xacml4j/v20/Xacml20TestUtility.java:25:24` | package org.junit does not exist | Apply conductor-approved junit:junit:4.13.2 compile-scope dependency; all assertion/lifecycle code retained. |
| `xacml-test/src/main/java/org/xacml4j/v20/Xacml20TestUtility.java:25:1` | static import only from classes and interfaces | Apply conductor-approved junit:junit:4.13.2 compile-scope dependency; all assertion/lifecycle code retained. |
| `xacml-test/src/main/java/org/xacml4j/v20/Xacml20TestUtility.java:26:24` | package org.junit does not exist | Apply conductor-approved junit:junit:4.13.2 compile-scope dependency; all assertion/lifecycle code retained. |
| `xacml-test/src/main/java/org/xacml4j/v20/Xacml20TestUtility.java:26:1` | static import only from classes and interfaces | Apply conductor-approved junit:junit:4.13.2 compile-scope dependency; all assertion/lifecycle code retained. |
| `xacml-test/src/main/java/org/xacml4j/v20/Xacml20TestUtility.java:27:24` | package org.junit does not exist | Apply conductor-approved junit:junit:4.13.2 compile-scope dependency; all assertion/lifecycle code retained. |
| `xacml-test/src/main/java/org/xacml4j/v20/Xacml20TestUtility.java:27:1` | static import only from classes and interfaces | Apply conductor-approved junit:junit:4.13.2 compile-scope dependency; all assertion/lifecycle code retained. |
| `xacml-test/src/main/java/org/xacml4j/v30/XacmlPolicyTestSupport.java:63:10` | cannot find symbol | Apply conductor-approved junit:junit:4.13.2 compile-scope dependency; all assertion/lifecycle code retained. |

## Downstream API diagnostics (3)

| Compiler file:line:column | Diagnostic | Fix |
| --- | --- | --- |
| `xacml-test/src/main/java/org/xacml4j/v30/ExpectedAttributeResolverBuilder.java:63:53` | incompatible types: org.xacml4j.v30.ValueExpTypeInfo cannot be converted to org.xacml4j.v30.types.ValueType | Use value.getEvaluatesTo().getValueType(). |
| `xacml-conformance-tests/src/test/java/org/xacml4j/v20/Xacml20ConformanceAttributeResolver.java:32:29` | cannot find symbol | Import StringVal instead of removed String. |
| `xacml-conformance-tests/src/test/java/org/xacml4j/v20/Xacml20ConformanceAttributeResolver.java:52:51` | incompatible types: org.xacml4j.v30.types.Value<capture#1 of ?> cannot be converted to org.xacml4j.v30.types.StringVal | Cast the annotation-constrained string BagOfValues.value() result to StringVal. |

## Temporary edit errors, also corrected (4)

| Compiler file:line:column | Diagnostic | Fix |
| --- | --- | --- |
| `xacml-pdp/src/main/java/org/xacml4j/v30/pdp/request/MultipleResourcesViaXPathExpressionHandler.java:145:66` | cannot find symbol | Use path.getEvaluatesTo().getValueType(); resolves subsequent inference error. |
| `xacml-pdp/src/main/java/org/xacml4j/v30/pdp/request/MultipleResourcesViaXPathExpressionHandler.java:146:49` | incompatible types: inference variable T has incompatible bounds | Use path.getEvaluatesTo().getValueType(); resolves subsequent inference error. |
| `xacml-pdp/src/test/java/org/xacml4j/v30/pdp/request/MultipleResourcesViaXacmlXPathHandlerTest.java:105:84` | cannot find symbol | New test setup uses Result.ok(Decision.PERMIT), not nonexistent Result.permit(). |
| `xacml-pdp/src/test/java/org/xacml4j/v30/pdp/request/MultipleResourcesViaXacmlXPathHandlerTest.java:106:85` | cannot find symbol | New test setup uses Result.ok(Decision.PERMIT), not nonexistent Result.permit(). |


## Identifier-continuation compiler errors (run 40), corrected

SetFunctions.java:586/596/606/616/626/636/646/656/666/676 (column 10):
`cannot find symbol: class XacmlLegacyFunc` -> add the annotation import.
These ten errors have the same root cause. Runs 41, 42, 44, 45 and 46
have zero compiler diagnostics; intentional red lookup runs fail assertions.

Round 3 scope proof: 3535 protected files unchanged; exactly nine authorized core
source/test files changed, bringing the continued lane inventory to 32 project files.
Seven existing license headers are unchanged; two new headers match the template.
64-round3-fixes.patch is the delta from the round 3 snapshot. No POM, fixture,
expected-result, other-provider, XML or tracking edits occurred in round 3.
The global diff check reports pre-existing ExpressionTypeBuilder.java:96 whitespace;
that file is unchanged and the scoped check passes.
