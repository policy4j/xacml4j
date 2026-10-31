# rv077 lane `claude_opus_5_5`: task 4.6 architecture review

- Change `modernize-core-for-jdk28-valhalla`; task 4.6, gate 4.7; bead `policy4j-the.4.6` (held by `codex-modernization-resume`).
- Role: architecture reviewer. Requested model: `claude-opus-5-5`. Actual: provider Anthropic, model `claude-opus-5-5`, as the Claude Code session declares it.
- Date 2026-10-04. Read-only lane; this file is the only write.

Paths: FS = `specs/function-semantics/spec.md`; CR = `specs/combining-algorithm-registry/spec.md`; MAN = `xacml-conformance-tests/src/test/manifest/conformance-manifest.csv`; CT = `xacml-conformance-tests/src/test/resources/xacml3.0-ct-v.0.4/ConformanceTests.html`. Core Java files are under `xacml-core/src/{main,test}/java/org/xacml4j/v30/**`; harness files are under `xacml-conformance-tests/src/test/java/org/xacml4j/v20/`.

## Result

| Decision | Approved artifacts | Run 061 | This lane |
| --- | --- | --- | --- |
| D1 | `xs:integer`, truncated | (a) | (a) |
| D2 | IEEE: NaN ≠ NaN, `0.0` = `-0.0` | (a) NaN = NaN | (b) |
| D10 | URN resolves and matches its profile | (a) | (a), two commits |

These are suggestions. The maintainer's choice stays pending.

## Findings

### 1. HIGH. D2: option (a) contradicts the normative text and three approved artifacts

- XACML 3.0 Core OS §A.3.1: `double-equal` "SHALL perform its evaluation on doubles according to IEEE 754". §A.3.6 says the same for comparisons (OASIS HTML, fetched 2026-10-04).
- FS:29-36, `design.md:25-28`, and `tasks.md:48` already class NaN equality as a deviation to fix.
- Today's result is an accident of `Double.equals`: `EqualityPredicates.java:70-77,193-195` calls `Value.java:133-142`.
- The ordering functions already use IEEE operators (`NumericComparisonFunctions.java:83,92,101,110`; enabled rows `FunctionSafetyNet43Test.java:478-487`). With (a), NaN equals NaN but is not `>=` or `<=` NaN.
- The fixtures have low authority. CT:93 calls the suite non-normative. CT:297-301, CT:1359-1364, and CT:1407-1412 mark IIC350 and IIC358 `**EXPERIMENTAL**`, from one contributor, March 2014. Their subjects are NaN parsing and NaN arithmetic (`IIC350Policy.xml:4`, `IIC358Policy.xml:4`), not equality.
- Precedent: MAN:398, MAN:744, and MAN:861 keep the engine and disable the fixture.
- Evidence for (a) that is not a fixture: WSO2 Balana (Sun lineage) `DoubleAttribute.equals` makes two NaNs equal on purpose (secondary; fetched 2026-10-04). Option (a) matches that engine family.

Impact of (b): `double-equal(NaN, NaN)` changes from true to false wherever the function runs. `oasis30/IIC350` and `oasis30/IIC358` change from Permit to NotApplicable. A policy can still detect NaN with `not(double-equal(x, x))`.

Minimal fix: compare primitives in `EqualityPredicates.eq(DoubleVal, DoubleVal)` only. Do not change `DoubleVal.equals` or `hashCode`.

Test: replace the placeholder `row30` (`FunctionSafetyNet43Test.java:490-495`) with an assertion of false. Enable `row31` (`:497-504`). Both fail on today's source.

If the maintainer selects (a): amend FS:29-36 first, record the deviation, and define `>=` and `<=` for NaN. Option (a) still needs code for signed zero.

### 2. HIGH. D10: `combine()` contradicts CS01 §2.1 and hides errors

CS01 §2.1 normative pseudo-code (OASIS HTML, fetched 2026-10-04):

1. Fewer than 2 or more than 3 children: Indeterminate{DP}, `processing-error`.
2. Child 0 is NotApplicable, Deny, or Indeterminate{D}: NotApplicable with 2 children; otherwise the decision of child 2.
3. Child 0 is Permit: the decision of child 1.
4. Otherwise: Indeterminate{DP} with the status of child 0.

`OnPermitApplySecondPolicyCombiningAlgorithm.java`:

- `:62-65` rejects three children.
- `:70` evaluates child 1 unless child 0 is NotApplicable. CS01 evaluates it only after Permit.
- `:74-81`: child 0 Deny with child 1 Permit or Deny gives Indeterminate. CS01 gives NotApplicable.
- `:94`: child 0 Indeterminate{P} or {DP} with child 1 NotApplicable gives NotApplicable. CS01 gives Indeterminate{DP}. This hides an error.

Impact: the class is public and unregistered (`DefaultXacml30DecisionCombiningAlgorithms.java:47-56`), so the rewrite changes decisions only for direct Java users. Registration adds one URN (23 to 24) and reaches the conformance harness (`ConformanceCaseRunner.java:64`). Policies that name the URN start to resolve. No other URN changes.

Option (b) is not neutral: it contradicts CR:4-15. Registration of the current body contradicts CR:17-20.

Minimal fix: commit 1 rewrites `combine()`. Commit 2 registers it and enables `DecisionCombiningAlgorithmRegistryTest.java:392-398`.

Test: a decision table. Arity 0 to 4; child 0 across all six decisions; evaluation counts for children 1 and 2; status. The four decisions in `OnPermitApplySecondPolicyTest.java:59-145` agree with CS01.

### 3. MEDIUM. D1: (a) is correct; the defect is the type, not the value

- `IntegerVal.java:109-112` divides two `long` values and then widens. `5/2` is already `2.0`. `ArithmeticFunctions.java:160-168` declares `xs:double`.
- §A.3.2 (fetched) requires an "integer ... data-type" result. It gives no rounding direction, but FS:4-5 cites it for truncation. Amend that citation. Truncation is a project decision.
- Run 061 offers floor as the alternative. The alternative with text support is half-even from §7.5 (`evidence/builds/032-semantic-decisions-grok/summary.md:43`), where `7/2` is `4`. No engine in the evidence does that.

| Consumer | Today | After (a) |
| --- | --- | --- |
| Integer function over `integer-divide` (IIC020; MAN:95, MAN:474) | Indeterminate | Decided; IIC020 Permit |
| Double function over `integer-divide` | Works | Indeterminate at run time (`FunctionSpecBuilder.java:367-377`; `Apply.java:192-194` does not validate) |
| `ArithmeticFunctions.divideInteger`, `IntegerVal.divide` | Return `DoubleVal` | Return `IntegerVal`: a binary and source break |
| Quotient above 2^53 | Rounded | Exact |

Callers: `ArtimeticFunctionsTest.java:49-54` only. `rtk rg` found none in `xacml-pdp`, `xacml-xml`, `xacml-json`, or `xacml-test` (SUPPORTED, literal search). External policies are unknown; `design.md:236-238` stays open.

`Long.MIN_VALUE / -1` wraps without an error. Leave it to D4 and task 5.4; add a row with that tag.

Test: enable `row37` and `row38` (`FunctionSafetyNet43Test.java:552-569`). Update `ArtimeticFunctionsTest.java:51`.

### 4. MEDIUM. Signed zero and bags stay on `Double.equals` under each D2 option

`double-equal(-0.0, 0.0)` is false today, so "true either way" in run 061 is a target. `BagOfValues.java:63,141,204-206`, `BagFunctions.java:93`, and `SetFunctions.java:181-223` compare with `Value.equals`: NaN is in a bag of NaN, and `0.0` is not in a bag of `-0.0`. FS:39-41 defines duplicates by `type-equal`. Do not repair this in `Value.equals`: hash collections and JEP 401 state equality (`design.md:185-194`) need a reflexive `equals`. Open a separate bead and decision. Add characterization rows now.

### 5. MEDIUM. The gate text blocks (b)

`tasks.md:6-10` and `:49` allow no lost executed case. D1(a) with D2(b) keeps the count (+2 IIC020, −2 IIC350 and IIC358), but two cases leave. The harness always compares with the vendored response (`ConformanceCaseRunner.java:117-124,145-151,161`). Recommend `skipped` with a reason and bug id (`ConformanceManifest.java:99-107`), a written maintainer waiver, and core tests for NaN parsing and `double-add(NaN, 1.0)`.

### 6. LOW. A registry scenario contradicts its requirement

CR:22-24 requires the URN set of `a8a90d46`. CR:4-7 and `tasks.md:63` require one more URN. Amend the scenario.

### 7. LOW. `integer-mod` by zero is already decided

FS:6-7 and FS:17-19 require Indeterminate. `FunctionSafetyNet43Test.java:603-610` is still disabled, and `IntegerVal.java:114-117` checks only for null. Enable the row and add an explicit zero check.

## Recommendations

1. D1 (a). D2 (b), in the function body only. D10 (a), rewrite before registration.
2. Amend the specs before code: the truncation citation, the registry scenario, and the D2 requirement if (a) wins.
3. One commit per fix (`tasks.md:48`). Write the failing test first.

## Uncertainty and unrun gates

- Not run: Maven, Surefire, conformance, `openspec validate`, JMH. Each "fails today" statement comes from source reading.
- OASIS and Balana text came through Tavily extraction. IEEE 754, §7.5, and XPath `idiv` were not fetched. AuthzForce and AT&T NaN behavior was not read.
- IIC351 to IIC357 and IIC359 were not opened.
- D10 obligation and advice propagation was not audited.
- No tool approval blocked a read.

DONE_CLAUDE_OPUS_5_5_20261004