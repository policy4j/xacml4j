# rv077 lane claude_fable_5_1: test review and red/green plan for task 4.6

Date: 2026-10-04. Role: test reviewer. Requested model: `claude-fable-5-1`. Actual: `claude-fable-5-1` (Anthropic, Claude Code; self-reported from session metadata, same as `model-receipts.json`).

Method: file reads and `rtk rg` only. No build, test, Git, or Beads command ran. No tool approval blocked a read. Decisions D1, D2, D7, D10 (run 061) stay open; nothing below assumes one.

Paths: FSN = `xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/FunctionSafetyNet43Test.java`; AFT = `ArtimeticFunctionsTest.java` (same directory); REG = `xacml-core/src/test/java/org/xacml4j/v30/policy/combine/DecisionCombiningAlgorithmRegistryTest.java`; MAN = `xacml-conformance-tests/src/test/manifest/conformance-manifest.csv`; `main/` = `xacml-core/src/main/java/org/xacml4j/v30/`.

## Findings

1. HIGH. `on-permit-apply-second` contradicts CS01 §2.1 and no test would notice. `main/policy/combine/impl/OnPermitApplySecondPolicyCombiningAlgorithm.java:62-65` rejects three children; `:70` evaluates child 1 whenever child 0 is not NotApplicable; `:74-93` builds the result from child 1 with a new processing-error. `OnPermitApplySecondPolicyTest.java:59-157` has four cases (no children; Permit then NotApplicable, Permit, Deny); all four agree with CS01. REG`:392-398` asserts only non-null. Enabling it plus one registration line would publish a wrong algorithm on a green build.
2. HIGH. FSN rows 37 and 38 (`:552-569`) do not pin truncation. `5/2=2` and `7/-3=-2` also hold for round-half-even (run 032, option 2). They exclude only floor.
3. HIGH. The safety net cannot reproduce the IIC020 failure. FSN`:82` mocks `EvaluationContext`, so `isValidateFuncParamsAtRuntime()` is false and `main/policy/function/FunctionSpecBuilder.java:367-377` never validates. MAN`:95,474` say IIC020 fails in that type check.
4. HIGH. One Java `equals` backs three XACML behaviors. `double-equal` is `a.equals(b)` (`main/policy/function/impl/EqualityPredicates.java:70-77,193-195`; `main/types/Value.java:133-142`, so `Double.equals`). `double-is-in` (`BagFunctions.java:93`) and the set functions (`main/BagOfValues.java:141-207`) use the same `equals` and `hashCode`. No test covers NaN or signed zero in `is-in`, `subset`, `set-equals`, `intersection`, or `at-least-one-member-of`. IEEE NaN inequality cannot go into `equals` (reflexivity, hash collections). D2(b) therefore needs a function-level rule and a ruling for bags and sets (`specs/function-semantics/spec.md:39-41`).
5. MEDIUM. FSN row 48 (`:646-655`) compares bags with `BagOfValues.equals`, the equality under repair. With a function-level fix the result depends on which zero survives.
6. MEDIUM. D2(b) conflicts with the gate. MAN`:706,714` execute IIC350 and IIC358, which Permit only when NaN equals NaN (`IIC350Policy.xml:12-17`, `IIC358Policy.xml:12-19`). `tasks.md:8-10,49` forbid losing executed cases. The written spec (`spec.md:29-36`) requires (b); run 061 recommends (a). Spec, manifest, or gate must change first.
7. MEDIUM. AFT`:48-54` pins the defect (`DOUBLE.ofAny(2)` for `4/2`) and calls the static method. That bypasses the registry and the exception mapping: `ArithmeticFunctions.java:166` throws `IllegalArgumentException`; only `DefaultFunctionInvocation.java:137-138` makes it processing-error.
8. MEDIUM. Registry tests check membership, not the set. No test calls `getSupportedPolicyAlgorithms()` or `getSupportedRuleAlgorithms()`. The "Registry completeness" and "URN unchanged" scenarios (`specs/combining-algorithm-registry/spec.md:13-24`) are unproven.
9. LOW, UNVERIFIED. Obligations accumulate flat per decision (`main/policy/RootEvaluationContext.java:218-249`). A Deny "condition" child may leave Deny obligations queued when the set returns NotApplicable. Status is last-write-wins (`:134-137`); CS01 requires the status of child 0.

## Plan (one commit per slice, tests first)

### A. `integer-divide` (needs D1)

Red:

- Enable FSN rows 37 and 38. Add, by URN and by short id `integer-divide` (`BaseFunctionProvider.java:143-148`): `7/2=3`, `-7/2=-3`, `-7/3=-2`, `-7/-3=2`, `0/5=0`, `0/-5=0`. The first two separate truncation from floor and from half-even.
- `integer-mod`: `5 mod -3=2`, `-5 mod -3=-2`, `0 mod 5=0`; the identity `(a/b)*b + (a mod b) = a` for four sign pairs.
- Zero divisor: `0/0` is Indeterminate processing-error (row 39 has `1/0`); enable row 43 (`:603`, D7).
- Composed row: `integer-greater-than-or-equal(integer-divide(45, 2), 10)` through `Apply` with `ModelFixtures.context(true)`; expect true.
- AFT: the expected value becomes `INTEGER.ofAny(2)`. Record it as a correction to the A.3.2 value, not a weakened assertion.
- MAN`:95,474` become `executed,true`.
- Add, disabled under "C3 task 5.4": `Long.MIN_VALUE / -1`.

Green: `ArithmeticFunctions.java:161-162`, `IntegerVal.java:109-112`.

### B. Double equality (needs D2)

Both options: enable row 31. Rewrite row 48 to assert size 1 and `double-equal(element, 0.0)`. Add signed-zero rows for `double-is-in`, `-subset`, `-set-equals`, `-intersection`, `-at-least-one-member-of`, and `double-bag-size([0.0, -0.0])=2`. Add a `DoubleVal` contract test: equal values have equal hash codes; a NaN value equals itself. Keep `TypeRegistryTest.java:169-179` green: do not normalize the stored zero.

- (a): row 30 asserts true. Add NaN against `1.0` and `INF` (false) and `double-is-in(NaN, [NaN])` (true). Rows 27-29 (`:478-487`) stay false, so the spec must state that `equal` and `>=` disagree on NaN.
- (b): row 30 asserts false. The NaN bag and set rows need a ruling. MAN`:706,714` become skipped with an adjudicated reason.

### C. `on-permit-apply-second` (needs D10)

(a) Red: a table test with recording children (pattern: `OverrideDecisionTableTest.java:184-221`); take the algorithm by URN from the default provider.

| Children | Child 0 | Result | Evaluated |
| --- | --- | --- | --- |
| 0, 1, 4 | none | Indeterminate{DP}, processing-error | none |
| 2 | NotApplicable, Deny, Indeterminate{D} | NotApplicable | 0 |
| 3 | the same three | decision of child 2 (six values) | 0, 2 |
| 2, 3 | Permit | decision of child 1 (six values) | 0, 1 |
| 2, 3 | Indeterminate{P}, Indeterminate{DP} | Indeterminate{DP}, status of child 0 | 0 |

Then enable REG`:392` and strengthen it: `getId()`, `isPolicyAlgorithmProvided` (used by `AbstractPolicyRepository.java:305-309`), `getRuleAlgorithm` null, `withDiscoveredAlgorithms()`, and exact sets (11 rule, 13 policy). Add a nested `PolicySet` test for finding 9. Green: rewrite `combine()`; register at `DefaultXacml30DecisionCombiningAlgorithms.java:53-56`.

(b) Assert that the URN does not resolve; amend the spec; exact sets are 11 and 12.

## Enable after approval

- FSN`:552,562` (D1), with MAN`:95,474`.
- FSN`:603` (D7; expected green without a code change).
- FSN`:497,646` (D2, either option); `:490` (D2; replace the `fail()` body).
- REG`:392` (D10a, after the table is green).
- Leave disabled: FSN`:183,527,539,727,734,779` and the PDP 4.34 tests.

## Uncertainty and unrun gates

- Unrun: Maven verify, conformance, `openspec validate`, JMH. Every red or green prediction comes from reading source.
- Labels: `rtk rg` and file reads are SUPPORTED for literal references. Caller inventories are PARTIAL (no Serena or ripwire run). `gemini-search` was UNAVAILABLE.
- CS01 §2.1 was read from the OASIS CS01 HTML through one extraction. Array indices were stripped, so the child mapping comes from the prose list. No second source.
- CS01 does not list plain `Decision.INDETERMINATE` (`Decision.java:35`) for child 0. This needs a ruling.
- Not read: `xacml-xml` mappers (out of scope), the `Apply` builder signature, external callers of `divideInteger`.
- `FunctionProviderServiceLoaderTest.java:143,158` look stale against FSN`:108-157`. Check under 4.7.
- JMH decision: required if `DoubleVal.equals` or `hashCode` changes.

DONE_CLAUDE_FABLE_5_1_20261004