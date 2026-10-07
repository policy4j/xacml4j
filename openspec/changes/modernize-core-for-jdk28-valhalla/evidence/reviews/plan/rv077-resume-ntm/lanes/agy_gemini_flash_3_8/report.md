# rv077 lane agy_gemini_flash_3_8: Audit of Run 068 Baseline and Phase 4 Acceptance

Date: 2026-10-04. Role: test and evidence reviewer.  
Requested Model: `gemini-3.8-flash-high`. Actual Model: Gemini 3.8 Flash (High) (Google Antigravity / Agy CLI).  
Lane: `agy_gemini_flash_3_8`. Change: `modernize-core-for-jdk28-valhalla`.  
Held Bead: [`policy4j-the.4.6`](openspec/changes/modernize-core-for-jdk28-valhalla/tasks.md:48) by conductor `codex-modernization-resume`.

---

## 1. Executive Summary: What Is Green vs. Unproven

### What Is Green
The [Run 068 baseline](openspec/changes/modernize-core-for-jdk28-valhalla/evidence/tests/068-resume-jdk21-baseline/summary.md:1-38) confirms reactor health on JDK 21:
- Reactor `clean verify` passed: exit 0; 7,137 tests, 0 failures, 0 errors, 39 skipped.
- Strict OpenSpec validation passed: 25/57 tasks complete through Phase 3.
- File integrity preserved: 3,753 file hashes match pre-baseline state; vendored fixtures are pristine.

### What Is Not Yet Proved
Phase 4 acceptance is **NOT** achieved. Task 4.6 is open and Gate 4.7 is blocked:
1. **Integer division**: [`ArithmeticFunctions.divideInteger`](xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/ArithmeticFunctions.java:160-168) still returns [`DoubleVal`](xacml-core/src/main/java/org/xacml4j/v30/types/DoubleVal.java).
2. **Double equality**: NaN equality conflicts between IEEE 754 and OASIS conformance suite requirements.
3. **Combining algorithm**: [`OnPermitApplySecondPolicyCombiningAlgorithm`](xacml-core/src/main/java/org/xacml4j/v30/policy/combine/impl/OnPermitApplySecondPolicyCombiningAlgorithm.java:48-97) is unregistered, violates CS01, and lacks verification.
4. **39 skipped tests**:
   - `xacml-core` (17): 12 in [`FunctionSafetyNet43Test`](xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/FunctionSafetyNet43Test.java:183), 1 in [`DecisionCombiningAlgorithmRegistryTest:393`](xacml-core/src/test/java/org/xacml4j/v30/policy/combine/DecisionCombiningAlgorithmRegistryTest.java:393), 2 in [`FunctionProviderServiceLoaderTest:143,158`](xacml-core/src/test/java/org/xacml4j/v30/policy/function/FunctionProviderServiceLoaderTest.java:143-158), 1 in [`ContentTest:103`](xacml-core/src/test/java/org/xacml4j/v30/content/ContentTest.java:103), 1 in [`Xacml20XPathTo30TransformerTest:46`](xacml-core/src/test/java/org/xacml4j/v30/types/Xacml20XPathTo30TransformerTest.java:46).
   - `xacml-pdp` (5): 2 in [`AttributeResolutionSafetyNetTest:164,171`](xacml-pdp/src/test/java/org/xacml4j/v30/pdp/AttributeResolutionSafetyNetTest.java:164-176), 2 in [`DefaultPolicyInformationPointTest:210,252`](xacml-pdp/src/test/java/org/xacml4j/v30/spi/pip/DefaultPolicyInformationPointTest.java:210-252), 1 in [`BaseAttributeResolverTest:57`](xacml-pdp/src/test/java/org/xacml4j/v30/spi/pip/BaseAttributeResolverTest.java:57).
   - `xacml-json` (1): [`JsonEntityMarshallingTest:67`](xacml-json/src/test/java/org/xacml4j/v30/marshal/json/JsonEntityMarshallingTest.java:67).
   - `xacml-conformance-tests` (16): 15 `skipped` + 1 `special-harness,false` in [conformance-manifest.csv](xacml-conformance-tests/src/test/manifest/conformance-manifest.csv).

---

## 2. Numbered Severity Findings

### Finding 1: HIGH — `on-permit-apply-second` Violates CS01 and Is Unregistered
- [`OnPermitApplySecondPolicyCombiningAlgorithm.java:62-65`](xacml-core/src/main/java/org/xacml4j/v30/policy/combine/impl/OnPermitApplySecondPolicyCombiningAlgorithm.java:62-65) rejects policies where `policies.size() != 2`. CS01 §2.1 permits 2 or 3 policies; valid 3-child policies incorrectly return `INDETERMINATE_DP`.
- [`OnPermitApplySecondPolicyCombiningAlgorithm.java:66-70`](xacml-core/src/main/java/org/xacml4j/v30/policy/combine/impl/OnPermitApplySecondPolicyCombiningAlgorithm.java:66-70) evaluates child 1 whenever child 0 != `NOT_APPLICABLE`. For 2 children with child 0 `DENY`, evaluating child 1 violates CS01 short-circuit semantics.
- Lines 75–93 overwrite child status with a generic `PROCESSING_ERROR`, losing deciding status codes.
- [`DefaultXacml30DecisionCombiningAlgorithms.java:30-72`](xacml-core/src/main/java/org/xacml4j/v30/policy/combine/impl/DefaultXacml30DecisionCombiningAlgorithms.java:30-72) omits registering the algorithm instance.
- [`DecisionCombiningAlgorithmRegistryTest.java:393`](xacml-core/src/test/java/org/xacml4j/v30/policy/combine/DecisionCombiningAlgorithmRegistryTest.java:393) is disabled; [`OnPermitApplySecondPolicyTest.java:59-157`](xacml-core/src/test/java/org/xacml4j/v30/policy/combine/impl/OnPermitApplySecondPolicyTest.java:59-157) has only 4 trivial cases.

### Finding 2: HIGH — `integer-divide` Returns `DoubleVal`, Causing Conformance Failure IIC020
- [`ArithmeticFunctions.java:160-168`](xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/ArithmeticFunctions.java:160-168) annotates return type `XMLSchema#double` and returns `DoubleVal`.
- [`IntegerVal.java:109-112`](xacml-core/src/main/java/org/xacml4j/v30/types/IntegerVal.java:109-112) defines `divide` returning `DoubleVal.of(get() / d.get())`.
- [`ArtimeticFunctionsTest.java:51-53`](xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/ArtimeticFunctionsTest.java:51-53) asserts `XacmlTypes.DOUBLE.ofAny(2)`.
- Violates [spec.md:3-12](openspec/changes/modernize-core-for-jdk28-valhalla/specs/function-semantics/spec.md:3-12) (XACML 3.0 §A.3.2) and forces skips of `IIC020` in [conformance-manifest.csv:95,474](xacml-conformance-tests/src/test/manifest/conformance-manifest.csv:95,474) and disabled rows 37–38 in [`FunctionSafetyNet43Test.java:552-569`](xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/FunctionSafetyNet43Test.java:552-569).

### Finding 3: HIGH — Specification Contradicts Conformance Suite on `double-equal(NaN, NaN)` (D2)
- [specs/function-semantics/spec.md:29-37](openspec/changes/modernize-core-for-jdk28-valhalla/specs/function-semantics/spec.md:29-37) requires IEEE 754: `double-equal(NaN, NaN) == false`.
- OASIS cases `IIC350` ([IIC350Policy.xml:12-18](xacml-conformance-tests/src/test/resources/xacml3.0-ct-v.0.4/IIC350Policy.xml:12-18)) and `IIC358` expect `Permit` when comparing `NaN` to `NaN`.
- [conformance-manifest.csv:706,714](xacml-conformance-tests/src/test/manifest/conformance-manifest.csv:706,714) executes both cases. Enforcing `spec.md` breaks these cases, violating [`tasks.md:9`](openspec/changes/modernize-core-for-jdk28-valhalla/tasks.md:9).
- [`EqualityPredicates.java:70-76,193-195`](xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/EqualityPredicates.java:70-76,193-195) delegates to `DoubleVal.equals` / `Double.equals` (returns `true` for `NaN==NaN`, but `false` for `-0.0==0.0`). Row 31 in [`FunctionSafetyNet43Test.java:497`](xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/FunctionSafetyNet43Test.java:497) is disabled because `-0.0 == 0.0` fails under `Double.equals`.

### Finding 4: MEDIUM — Attribute Resolver (PIP) Failures Fail Open (D3)
- [specs/attribute-resolution/spec.md:43-52](openspec/changes/modernize-core-for-jdk28-valhalla/specs/attribute-resolution/spec.md:43-52) requires Indeterminate `processing-error` when a resolver throws.
- PDP currently fails open. [`AttributeResolutionSafetyNetTest.java:164-176`](xacml-pdp/src/test/java/org/xacml4j/v30/pdp/AttributeResolutionSafetyNetTest.java:164-176) has two disabled tests pending D3 under bead `policy4j-the.4.34`.

### Finding 5: MEDIUM — Combining Algorithm Registry Completeness Is Unchecked
- [specs/combining-algorithm-registry/spec.md:13-25](openspec/changes/modernize-core-for-jdk28-valhalla/specs/combining-algorithm-registry/spec.md:13-25) requires testing that every algorithm class resolves by URN.
- [`DecisionCombiningAlgorithmRegistryTest.java:401-409`](xacml-core/src/test/java/org/xacml4j/v30/policy/combine/DecisionCombiningAlgorithmRegistryTest.java:401-409) tests only `ServiceLoader` presence and `deny-overrides`, not set completeness.

---

## 3. Pending Decisions Audit (D1–D18)

- **D1** (`integer-divide`): **BLOCKS 4.6 & 4.7**. Requires xs:integer truncated toward zero (Rec a). Enables IIC020, FSN rows 37–38.
- **D2** (`double-equal` NaN): **BLOCKS 4.6 & 4.7**. Rec (a) (deviation: true, keep IIC350/358 executed; fix -0.0==0.0). Reconcile `spec.md:29-37`.
- **D3** (PIP failure): Post-4.7 (`the.4.34`). PDP currently fails open. Disables 2 PDP safety net tests.
- **D4** (Integer overflow): Task 5.4 (`C3`). Disables FSN rows 34–35 (`:527,539`).
- **D5** (Round/floor NaN/INF): Task 5.4 / post-4.7 (`C4`). Disables FSN row 60 (`:779`).
- **D6** (`double-to-integer` NaN/INF): Task 5.4 / post-4.7 (`C5`). Disables FSN row 12 (`:183`).
- **D7** (`integer-mod` by zero): Rec (a). Already evaluates to processing-error. Enable FSN row 43 (`:603`).
- **D8** (`ipAddress` set functions at 1.0): Extension functions. Disables FSN row 57 (`:734`).
- **D9** (`x500Name-match` multi-valued RDN): Bead `the.4.44`. Disables FSN row 56 (`:727`).
- **D10** (`on-permit-apply-second` CS01): **BLOCKS 4.6 & 4.7**. Rec (a). Rewrite `combine()`, isolate obligations, register in provider.
- **D11** (`or`/`and`/`n-of` Indeterminate): Bead `the.4.12`. Deciding value wins over earlier error.
- **D12** (Request syntax error responses): Bead `the.4.17`. Harness mapping for IIA005 (stays skipped in manifest).
- **D13** (Hierarchical / Legacy 2.0 XPath): Beads `the.4.18`, `4.20`. Unsupported; skips IIIC002/003, IIIG001–005.
- **D14** (IIIG300 Errata 01 vs Fixture): Retain engine behavior; manifest skips IIIG300.
- **D15** (IIA023 / IID029 fixture defects): Keep skipped with documented adjudications.
- **D16** (XPath expansion bounds): Security hardening (bead `the.4.50`).
- **D17** (ReDoS / regex hardening): Security hardening (bead `the.4.51`).
- **D18** (XPath failure status code): Bead `the.11`. Processing error vs missing attribute.

---

## 4. Minimal Fix & Test Guidance

1. **`integer-divide` (Task 4.6 Slice 1)**:
   - In [`ArithmeticFunctions.java:160-168`](xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/ArithmeticFunctions.java:160-168), set `@XacmlFuncReturnType` to `XMLSchema#integer` and method return to `IntegerVal`.
   - In [`IntegerVal.java:109-112`](xacml-core/src/main/java/org/xacml4j/v30/types/IntegerVal.java:109-112), update `divide` to return `IntegerVal.of(get() / d.get())`.
   - Update [`ArtimeticFunctionsTest.java:51`](xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/ArtimeticFunctionsTest.java:51) to assert `XacmlTypes.INTEGER.ofAny(2)`.
   - Enable rows 37–38 in [`FunctionSafetyNet43Test.java:552,562`](xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/FunctionSafetyNet43Test.java:552,562); update [conformance-manifest.csv:95,474](xacml-conformance-tests/src/test/manifest/conformance-manifest.csv:95,474) `IIC020` to `executed,true`.

2. **`double-equal` (Task 4.6 Slice 2)**:
   - If (a), reconcile [spec.md:29-37](openspec/changes/modernize-core-for-jdk28-valhalla/specs/function-semantics/spec.md:29-37) to record `NaN == NaN` deviation matching OASIS IIC350/IIC358.
   - In [`EqualityPredicates.java:70-77`](xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/EqualityPredicates.java:70-77), implement specialized double equality so `-0.0 == 0.0` is `true`.
   - Enable row 31 in [`FunctionSafetyNet43Test.java:497`](xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/FunctionSafetyNet43Test.java:497) and replace row 30 (`:490`).

3. **`on-permit-apply-second` (Task 4.6 Slice 3)**:
   - Rewrite [`OnPermitApplySecondPolicyCombiningAlgorithm.combine`](xacml-core/src/main/java/org/xacml4j/v30/policy/combine/impl/OnPermitApplySecondPolicyCombiningAlgorithm.java:60-95) per CS01 §2.1:
     - Arity: accept 2 or 3 policies; return `INDETERMINATE_DP` with `PROCESSING_ERROR` otherwise.
     - Child 0: if `PERMIT`, evaluate and return child 1.
     - Child 0 `DENY`, `NOT_APPLICABLE`, or `INDETERMINATE_D`: if arity == 2, return `NOT_APPLICABLE`; if arity == 3, evaluate and return child 2.
     - Child 0 `INDETERMINATE_P` or `INDETERMINATE_DP`: return `INDETERMINATE_DP` preserving child 0's status.
   - Register algorithm in [`DefaultXacml30DecisionCombiningAlgorithms.java:57`](xacml-core/src/main/java/org/xacml4j/v30/policy/combine/impl/DefaultXacml30DecisionCombiningAlgorithms.java:57).
   - Expand [`OnPermitApplySecondPolicyTest.java`](xacml-core/src/test/java/org/xacml4j/v30/policy/combine/impl/OnPermitApplySecondPolicyTest.java) with full parameterized combinations.
   - Enable [`DecisionCombiningAlgorithmRegistryTest.java:393`](xacml-core/src/test/java/org/xacml4j/v30/policy/combine/DecisionCombiningAlgorithmRegistryTest.java:393) and assert registry completeness.

---

## 5. Actionable Task Ownership & Gate Order

1. **Maintainer Action**: Confirm decisions for D1 (xs:integer truncate), D2 (deviation true vs IEEE false), and D10 (CS01 implementation).
2. **Sub-task 4.6a (`conductor`)**: Fix `integer-divide` return type, enable FSN rows 37–38, update `ArtimeticFunctionsTest`, enable `IIC020` in manifest.
3. **Sub-task 4.6b (`conductor` / `test_writer`)**: Align double equality with D2 decision, enable FSN rows 30–31, reconcile `spec.md`.
4. **Sub-task 4.6c (`conductor` / `implementer`)**: Correct `OnPermitApplySecondPolicyCombiningAlgorithm`, add CS01 tests, register in provider, enable REG:393.
5. **Gate 4.7 (`conductor`)**: Reactor verify on JDK 21 (`rtk mvn -Dlicense.skipUpdateLicense=true verify`). Require 0 failures, 0 regressions, executed count increased by 2 (IIC020), skips reduced to 37.

---

## 6. Uncertainty and Unrun Gates

- **Unrun Gates**: No Maven build, test execution, or Git mutations were performed during this audit.
- **Evidence Label**: Static inspection and literal searches are `SUPPORTED`. Transitive semantic caller traces are `PARTIAL`.
- **Obligation Isolation**: Obligation buffering across composite rules in nested policy hierarchies remains unverified at runtime.

---

## 7. Post-Review Addendum: Maintainer Decision on D2

- **Resolution**: Maintainer chose Objective spec compliance: D2 resolves to Option (b) (IEEE 754 `double-equal(NaN, NaN) == false`, `-0.0 == 0.0`). Prior memo recommendation (Option a, `NaN == NaN` true) is rejected.
- **Function Layer**: Java [`Value.equals`](xacml-core/src/main/java/org/xacml4j/v30/types/Value.java:133-143) must NOT change (preserves hashing contracts). IEEE semantics apply strictly in [`EqualityPredicates.java:70-77`](xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/EqualityPredicates.java:70-77).
- **Manifest & Tests**: OASIS `IIC350` and `IIC358` become `skipped` in [conformance-manifest.csv](xacml-conformance-tests/src/test/manifest/conformance-manifest.csv) with IEEE 754 adjudication; [`FunctionSafetyNet43Test.java:490,497`](xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/FunctionSafetyNet43Test.java:490,497) enable (row 30 asserts `false`, row 31 asserts `true`).

DONE_AGY_GEMINI_FLASH_3_8_20261004
