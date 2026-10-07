# Task 5.1–7.3 Donor Refactor Slice Map and Analysis
Evidence artifact for OpenSpec change `modernize-core-for-jdk28-valhalla`.
Read-only analysis mapping the donor refactor branch (`wip/feature_xacml_20-donor`, commit `b9f114a3`, tree `f5b92e71`, based on `a8a90d46`) across `xacml-core`, `xacml-pdp`, `xacml-json`, and `xacml-xml` to Tasks 5.1–7.3.
## 1. Executive Summary and Headline Counts
- **Donor commit**: `b9f114a3` (`wip/feature_xacml_20-donor`)
- **Baseline commit**: `a8a90d46` (upstream merge base)
- **Scope**: `xacml-core`, `xacml-pdp`, `xacml-json`, `xacml-xml`
- **Total donor diff**: **493 files** (+17,510 lines, -14,080 lines, net +3,430 lines)
- **Breakdown by raw Git status**: **339 Modified (M)**, **67 Added (A)**, **64 Renamed (R)**, **23 Deleted (D)**
- **Per-module inventory**:
  - `xacml-core`: 381 files (+15,836, -11,849 lines)
  - `xacml-pdp`: 65 files (+980, -1,494 lines)
  - `xacml-json`: 22 files (+243, -281 lines)
  - `xacml-xml`: 25 files (+451, -456 lines)

### Per-Slice Summary Table
| Slice | Target Task | Files | Added | Deleted | Net | M | A | R | D | Key Deliverable |
|---|---|---|---|---|---|---|---|---|---|---|
| **Task 5.1** | Task 5.1 | 16 | 1174 | 78 | +1096 | 3 | 12 | 1 | 0 | Canonical identifier records (`org.xacml4j.v30.identifier.*`), RFC 8141 URN parser, compatibility adapters |
| **Task 5.2** | Task 5.2 | 27 | 1988 | 1478 | +510 | 10 | 10 | 3 | 4 | Core `Value` / `BagOf` rename (`BagOfValues` -> `BagOf`, `ValueExp` -> `ValueExpr`, `AnyURI` -> `UriVal`), `Range` types |
| **Task 5.3** | Task 5.3 | 30 | 1533 | 1644 | -111 | 21 | 6 | 0 | 3 | `Primitives` typed facade, `ValueTypeId` registry, primitive value classes (`BooleanVal`, `DoubleVal`, `IPAddressV2`, etc.) |
| **Task 5.4** | Task 5.4 | 9 | 1568 | 137 | +1431 | 1 | 7 | 0 | 1 | Numeric semantics fix (`Int64` sole owner of `xs:integer`, non-wrapping arithmetic, strict integer lexing, product identity 1) |
| **Task 6.1** | Task 6.1 | 31 | 446 | 260 | +186 | 8 | 3 | 19 | 1 | `ValueExprType` result descriptors, `ExpressionKind`, AST node cleanup, `FunctionSpec` and parameter annotations |
| **Task 6.2** | Task 6.2 | 56 | 2520 | 2536 | -16 | 56 | 0 | 0 | 0 | Function annotations (`@FuncSpec`), function provider SPI, standard function implementations (`impl.*`) |
| **Task 6.3** | Task 6.3 | 41 | 483 | 513 | -30 | 19 | 1 | 19 | 2 | Combining algorithm renames (`*CombiningAlgorithm` -> `*Algorithm`), provider SPI, combining algorithm implementations |
| **Task 6.4** | Task 6.4 | 6 | 831 | 0 | +831 | 0 | 6 | 0 | 0 | `ValueStream` / `SeqOf` reworked into non-`Stream` query views (`Iterable` + `.stream()`, Design D9) |
| **Task 6.5** | Task 6.5 | 237 | 4475 | 5712 | -1237 | 205 | 6 | 22 | 4 | Policy evaluation integration: `Entity`, `Record`, `AttributeDesignator`, `AttributeSelector`, PDP SPI & runtime, XML & JSON marshallers |
| **Task 6.6** | Task 6.6 | 15 | 882 | 649 | +233 | 13 | 1 | 0 | 1 | Date, time, duration types (`Duration`, `ISO8601*`), reconciled with Bug 4.10 and Bug 4.11 fixes |
| **Task 7.1** | Task 7.1 | 8 | 834 | 0 | +834 | 0 | 8 | 0 | 0 | `AccessModel` and schema framework (`org.xacml4j.v30.model.*`) with donor defects fixed |
| **Task 7.2** | Task 7.2 | 7 | 742 | 0 | +742 | 0 | 7 | 0 | 0 | Standard category and attribute schemas (`org.xacml4j.v30.schemas.*`) aligned with XACML 3.0 Appendix B |
| **Task 7.3** | Task 7.3 | 7 | 4 | 1009 | -1005 | 1 | 0 | 0 | 6 | Legacy type removal (`AttributeContainer`, `AttributeIndex`, `Category`, `CategoryReference`, `WellKnownAttributeIds`) |
| **Task DROP** | Drop | 3 | 30 | 64 | -34 | 2 | 0 | 0 | 1 | Build/POM churn and already-deleted files (`pom.xml` preview args, unused deps `urnlib`/`ipaddress`, `policy.json`) |

## 2. Slice Dependency Order
The 13 slices form an explicit acyclic dependency graph:
```text
5.1 (Identifiers) ──┬──> 5.2 (Value/BagOf) ──> 5.3 (Primitives Registry) ──> 5.4 (Numeric Semantics)
                    │                               │
                    ├──> 6.4 (ValueStream/SeqOf)    ├──> 6.1 (Expression AST / ValueExprType)
                    │                               │          │
                    ├──> 6.6 (Date/Time/Duration)   │          ├──> 6.2 (Functions SPI & Impl)
                    │                               │          └──> 6.3 (Combining Algorithms)
                    │                               │                     │
                    └───────────────────────────────┴─────────────────────┼──> 6.5 (Entity/Record, PDP, XML, JSON)
                                                                          │          │
                                                                          │          ├──> 7.1 (AccessModel / Model)
                                                                          │          │          │
                                                                          │          │          └──> 7.2 (Schemas)
                                                                          │          │                 │
                                                                          └──────────┴─────────────────┴──> 7.3 (Legacy Type Deletions)
```

### Recommended Sequential Execution Order:
1. **Task 5.1 (Identifiers & URNs)**: Leaf package `identifier.*` and `URN8141`. Self-contained. Maintain root compatibility adapters (`CategoryId`, `StatusCodeId`, `UrnIdentifier`, `Issuer`). Zero compile-break risk.
2. **Task 5.2 (Value / BagOf Rename)**: Replaces `BagOfValues` -> `BagOf`, `ValueExp` -> `ValueExpr`, `AnyURI` -> `UriVal`. Introduces base value interfaces. Maintain temporary type aliases/subclasses to avoid breaking downstream callers.
3. **Task 5.3 (Primitives Facade & Registry)**: Introduces `Primitives` class and `ValueTypeId` registry (Design D5) alongside primitive value types (`BooleanVal`, `DoubleVal`, `IPAddressV2`, etc.).
4. **Task 5.4 (Numeric Semantics & Integer Cleanup)**: Establishes `Int64` as the single owner of `xs:integer`. Fixes arithmetic overflow handling, strict integer lexing, product identity `1`, and eliminates `return null;` stubs. Consolidates/drops auxiliary integer types.
5. **Task 6.1 (Expression Model & FunctionSpec)**: Introduces `ValueExprType` as declared result type everywhere, `requireExactlyOne()`, AST node updates, and function specification annotations.
6. **Task 6.3 (Combining Algorithms)**: Re-applies combining algorithm renames (`*CombiningAlgorithm` -> `*Algorithm`) and provider SPI. Completely disjoint from functions.
7. **Task 6.4 (SeqOf & ValueStream Query Views)**: Replaces single-use stream pipelines with `Iterable` + `.stream()` query views (Design D9). Disjoint from function/algorithm implementations.
8. **Task 6.6 (Date/Time/Duration Types)**: Replaces `ISO8601Duration` with `Duration`, updates `ISO8601Date`, `ISO8601DateTime`, `ISO8601Time`, reconciling with Bug 4.10 and Bug 4.11 fixes.
9. **Task 6.2 (Function Implementations)**: Updates all 56 standard function classes and tests in `policy/function/impl/` to use new `@FuncSpec` annotations and registry types. Reconciled with Bug 4.11 (`time-in-range`).
10. **Task 6.5 (Entity, Record, PDP Evaluation, XML & JSON Marshalling)**: Major integration slice (237 files). Adopts `Entity` and `Record` across policy AST, PDP runtime, and marshalling adapters. Can be parallelized internally into 3 disjoint sub-lanes (Core AST, PDP runtime, XML/JSON marshallers).
11. **Task 7.1 (AccessModel & Schema Framework)**: Introduces `org.xacml4j.v30.model.*` with donor defects fixed (`AttributesSchema:101`, `EntitySchema` mutual recursion, required-domain validation, `Entity` equals/hashCode).
12. **Task 7.2 (Standard Schemas Catalog)**: Introduces standard category and attribute schemas in `org.xacml4j.v30.schemas.*`.
13. **Task 7.3 (Legacy Type Deletions & Compatibility Cleanup)**: Deletes dead legacy types (`AttributeContainer`, `AttributeIndex`, `Category`, `CategoryReference`, `WellKnownAttributeIds`) and root compatibility adapters once all callers have migrated.

## 3. Top 10 Conflicts with Integration Branch (Since a8a90d46)
Since baseline `a8a90d46`, the integration branch (`integration/modernize-core-jdk28-valhalla`) has completed Tasks 1.1–3.6 (including full JUnit 5 + Mockito migration, compiler modernization, bug fixes 4.13, 4.11, 4.9, 4.10, 3.8, and conformance runner rewrite). In every conflict below, **the integration branch fix MUST WIN**:

### 1. Test Framework Migration: JUnit 5 & Mockito vs JUnit 4 & EasyMock (Tasks 2.1–2.7)
- **Affected Files**: `Over 100 test classes across `xacml-core`, `xacml-pdp`, `xacml-xml`, `xacml-json` (e.g. `DefaultPolicyDecisionPointTest.java`, `MultipleResourcesHandlerTest.java`, `Xacml20RequestContextUnmarshallerTest.java`, `NonNumericComparisionFunctionTest.java`).`
- **Conflict**: The donor branch still uses JUnit 4 (`import org.junit.Test;`, `@Before`, `@Rule`) and EasyMock (`import org.easymock.EasyMock;`, `IMocksControl`). The integration branch completely removed `junit:junit`, `junit-vintage-engine`, `easymock`, and `objenesis`, migrating all tests to JUnit 5 Jupiter (`org.junit.jupiter.api.*`) and Mockito 5 (`org.mockito.*`).
- **Resolution**: **Integration branch MUST WIN**. No donor test hunk may reintroduce JUnit 4 or EasyMock imports. Any donor test updates or newly introduced tests must use Jupiter assertions (`assertEquals`, `assertThrows`, `assertTrue`) and Mockito syntax.

### 2. Compiler Settings and POM Dependencies (Task 1.6 vs Donor POMs)
- **Affected Files**: ``pom.xml`, `xacml-core/pom.xml`, `xacml-pdp/pom.xml`.`
- **Conflict**: The integration branch compiles on JDK 21 using `<release>21</release>` without preview features. The donor branch introduced `<source>21</source><target>21</target><compilerArgs>--enable-preview</compilerArgs>`, downgraded Surefire and license-maven-plugin, commented out `xacml-xml` and `xacml-json` in `xacml-pdp/pom.xml`, and added unused dependencies (`de.slub-dresden:urnlib`, `org.eclipse.collections:eclipse-collections`, `com.github.seancfoley:ipaddress`) which have zero imports in Java sources.
- **Resolution**: **Integration branch MUST WIN**. Donor POM hunks must be completely DROPPED. The clean `<release>21</release>` configuration without preview must remain intact until Group 8.

### 3. ISO8601Time Formatting and Unzoned UTC Handling (Bug 4.11, Beads `policy4j-the.4.11`, Grok rv027)
- **Affected Files**: ``xacml-core/src/main/java/org/xacml4j/v30/types/ISO8601Time.java:188-204`.`
- **Conflict**: In donor, `toXacmlString()` formatted unzoned times with a UTC offset placeholder (`23:00:00` -> `23:00:00Z`). During XML/JSON roundtrips, this marked unzoned values as zoned UTC, causing `time-in-range` evaluations at non-UTC timezones (e.g. GMT-08:00) to invert decisions. The integration branch fixed this to use `ISO_LOCAL_TIME` for unzoned times and `ISO_TIME` only for zoned times.
- **Resolution**: **Integration branch MUST WIN**. The donor's time string formatting must be rejected in favor of the integration fix.

### 4. Time-in-Range Canonical ID and Timezone Range Matching (Bug 4.11, Beads `policy4j-the.4.11`)
- **Affected Files**: ``xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/NonNumericComparisonFunctions.java:112-160`.`
- **Conflict**: Donor line 112 declared `@FuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:time-in-range")`, missing the canonical XACML 2.0 identifier `urn:oasis:names:tc:xacml:2.0:function:time-in-range`, and lacked proper timezone normalization when comparing unzoned and zoned values across day boundaries. Integration branch fixed canonical ID registration, 1.0 aliasing, and day-wrapping boundary calculations.
- **Resolution**: **Integration branch MUST WIN**. NonNumericComparisonFunctions must retain the canonical 2.0 URN and robust timezone normalization.

### 5. XPath ContextSelectorId Singleton Node Requirement (Bug 4.9, Beads `policy4j-the.4.9`, XACML 3.0 §7.3.7)
- **Affected Files**: ``xacml-core/src/main/java/org/xacml4j/v30/types/XmlContent.java`, `AttributeSelector.java`, `AttributeSelectorKey.java`.`
- **Conflict**: Per XACML 3.0 §7.3.7, an explicit `ContextSelectorId` resolving to an XPath node-set MUST contain exactly one element node. Zero nodes or multiple nodes must immediately produce an `Indeterminate` result (`ProcessingErrorIndeterminateException`). Integration branch implements this strict validation; donor treated selector context node-sets loosely without validating singleton presence.
- **Resolution**: **Integration branch MUST WIN**. The singleton node requirement for `ContextSelectorId` must be preserved in all selector evaluation paths.

### 6. AbstractPolicyRepository Removal Notification Event (Bug 4.13, Beads `policy4j-the.4.13`)
- **Affected Files**: ``xacml-pdp/src/main/java/org/xacml4j/v30/spi/repository/AbstractPolicyRepository.java:188-193`.`
- **Conflict**: In donor line 191, `notifyPolicySetRemoved(PolicySet p)` erroneously called `l.policySetAdded(p)`. Integration branch fixed this to call `l.policySetRemoved(p)` and added a latch-based concurrency test (`AbstractPolicyRepositoryTest`).
- **Resolution**: **Integration branch MUST WIN**. Donor's faulty notification call must not be re-applied.

### 7. AssociatedAdvice Multiset Comparison in Conformance Harness (Bug 3.8, Beads `policy4j-the.3.8`)
- **Affected Files**: ``xacml-test/src/main/java/org/xacml4j/v30/Xacml30TestUtility.java:120-165`.`
- **Conflict**: Integration branch added unordered multiset comparison for `AssociatedAdvice` by `AdviceId`, and element-wise assignment comparison by `AttributeId`, `Category`, `Issuer`, and value equality. Donor lacked advice assertion entirely, allowing missing or extraneous advice to pass conformance undetected.
- **Resolution**: **Integration branch MUST WIN**. The robust advice comparison in `Xacml30TestUtility` must be preserved.

### 8. Conformance Test Harness, Manifest Runner, and Off-by-One Fix (Tasks 3.1–3.6)
- **Affected Files**: ``xacml-conformance-tests/src/test/java/org/xacml4j/v20/` (`ConformanceManifest.java`, `Xacml20ConformanceTest.java`, `Xacml30ConformanceTest.java`, `VendoredFixtureIntegrityTest.java`).`
- **Conflict**: Integration branch eliminated legacy hardcoded loop bounds that skipped the final case of every test group (off-by-one bug), replaced them with a manifest-driven enumeration of 880 cases across 2.0, 3.0, and RSA suites, made policy import failures fail tests, and added Git blob SHA-1 integrity checks against recorded baselines. Donor branch retained legacy hardcoded loops.
- **Resolution**: **Integration branch MUST WIN**. The entire manifest-driven conformance suite and integrity test harness must be kept intact.

### 9. AttributeDesignator and Selector Bag-Type Enforcement (Task 4.1 / Design D7)
- **Affected Files**: ``xacml-core/src/main/java/org/xacml4j/v30/policy/AttributeDesignator.java`, `AttributeSelector.java`, `ValueExpr.java`.`
- **Conflict**: Design D7 mandates that designators and selectors always return `ValueExprType` of bag kind (`valueType().bagType()`), with `requireExactlyOne()` used only at call sites where single scalar values are strictly mandated, and `first()` renamed from `findFirst()`. Donor occasionally bypassed bag return types or used `findFirst()` as an unsafe scalar unwrapper.
- **Resolution**: **Integration branch MUST WIN**. Bag return types must be preserved everywhere in designators and selectors.

### 10. Fractional Seconds and Nanosecond Precision in Temporal Types (Bug 4.10, Beads `policy4j-the.4.10`)
- **Affected Files**: ``xacml-core/src/main/java/org/xacml4j/v30/types/ISO8601DateTime.java`, `ISO8601Date.java`, `DateTimeDurationUtils.java`.`
- **Conflict**: Integration branch handles arbitrary fractional second precision up to nanoseconds and zero duration edge cases in temporal parsing and arithmetic. Donor temporal types truncated or mishandled sub-second fractions during string conversions.
- **Resolution**: **Integration branch MUST WIN**. High-precision temporal handling from Bug 4.10 must be retained.

## 4. Donor Defects Analysis (Tasks 5.4 & 7.1)
Tasks 5.4 and 7.1 explicitly call out several severe donor defects that must be corrected before or during adoption. Below is the line-by-line inventory with exact file and line references in `wip/feature_xacml_20-donor`:

### Task 5.4: Multiple Owners of xs:integer Datatype ID
- **Donor Location**: 
```text
`xacml-core/src/main/java/org/xacml4j/v30/types/Int64.java:40`
`xacml-core/src/main/java/org/xacml4j/v30/types/UInt32.java:43`
`xacml-core/src/main/java/org/xacml4j/v30/types/UInt64.java:46`
`xacml-core/src/main/java/org/xacml4j/v30/types/Primitives.java:115-119`
```
- **Defect Description**: Three separate classes (`Int64`, `UInt32`, `UInt64`) declare `@DataType(id = "http://www.w3.org/2001/XMLSchema#integer")`. In addition, `Primitives.java:115-119` defines `Int32` with `return org.xacml4j.v30.types.UInt32.type();`. This causes non-deterministic registry collisions and violates XACML 3.0 data type ownership.
- **Required Fix**: `Int64` must be the sole owner of `xs:integer`. Drop or retire `UInt32` and `UInt64` from standard XACML type registration.

### Task 5.4: Primitive Wrapping Arithmetic in Int64 and IntegerArithmetics
- **Donor Location**: 
```text
`xacml-core/src/main/java/org/xacml4j/v30/types/Int64.java:154`
`xacml-core/src/main/java/org/xacml4j/v30/IntegerArithmetics.java:48-52, 59, 163-169`
```
- **Defect Description**: `Int64.add()` uses primitive wrapping `value() + d.value()`, silently wrapping on overflow rather than throwing `ArithmeticException`. In `IntegerArithmetics.java:48-52`, `addSignedExact` checks `r <= Byte.MIN_VALUE`, incorrectly throwing out-of-range on valid `Byte.MIN_VALUE`. Line 59 uses `Short.toUnsignedInt` on signed shorts, corrupting negative values. Lines 163-169 use `Math.floorDiv`/`floorMod` instead of truncating toward zero as required by XACML §A.3.1 (`integer-divide`).
- **Required Fix**: Use `Math.addExact` in `Int64.add()`. Fix `IntegerArithmetics` range checks or drop it in favor of standard Java `Math.*Exact` functions. Ensure `integer-divide` truncates toward zero.

### Task 5.4: Lax / Broken Integer String Lexing
- **Donor Location**: 
```text
`xacml-core/src/main/java/org/xacml4j/v30/types/Int64.java:105-110`
```
- **Defect Description**: `Int64.of(String v)` searches for `+` with `v.trim().indexOf('+')` and substrings after `+` without validating character position. This erroneously accepts invalid strings such as `"foo+123"` (parsed as `123`) and `"12+34"` (parsed as `34`).
- **Required Fix**: Implement strict XACML/XML Schema integer lexing (`[+-]?[0-9]+`), validating leading sign and disallowing internal signs or non-digit characters.

### Task 5.4: Broken Product Identity (0 instead of 1)
- **Donor Location**: 
```text
`xacml-core/src/main/java/org/xacml4j/v30/types/Int64.java:166`
`xacml-core/src/main/java/org/xacml4j/v30/types/UInt32.java:165`
```
- **Defect Description**: `Int64.mul()` implements multi-argument multiplication via `.reduce(0L, Math::multiplyExact)`. The reduction identity for multiplication is `1L`, not `0L`! Using `0L` causes any reduction to collapse to `0`.
- **Required Fix**: Change multiplication reduction identity to `1L` (`.reduce(1L, Math::multiplyExact)`).

### Task 5.4: Null Method Stubs in Numeric Value Types
- **Donor Location**: 
```text
`xacml-core/src/main/java/org/xacml4j/v30/types/Int64.java:125-147`
`xacml-core/src/main/java/org/xacml4j/v30/types/Int32.java:226-241`
```
- **Defect Description**: Methods `toUInt32()`, `toUInt64()`, `toInt16()`, `toInt32()`, `toInt64()` contain literal `return null;` stubs in `Int64` and `Int32`, leading to unexpected `NullPointerException` at runtime.
- **Required Fix**: Eliminate null stubs. Implement valid conversions or throw `UnsupportedOperationException`/`ArithmeticException` if conversion is out of range or unsupported.

### Task 7.1: Invalid Interface Syntax and Empty Permits in AttributesSchema
- **Donor Location**: 
```text
`xacml-core/src/main/java/org/xacml4j/v30/model/AttributesSchema.java:101-104`
`xacml-core/src/main/java/org/xacml4j/v30/model/AttributeSchema.java:126`
```
- **Defect Description**: Line 101 contains illegal Java syntax: `sealed interface WithAttributes(EntityTypeQualifier typeQualifier, Map<AttributeId, AttributeSchema> attributes) extends AttributesSchema permits {`. Interfaces cannot have record parameters, and the `permits` clause is empty. In addition, `AttributeSchema.java:126` contains `Verify.verify(domains.isEmpty(), ...)` which inverts the check, throwing `VerifyException` when domains are present.
- **Required Fix**: Rewrite `WithAttributes` as a proper interface or record implementing `AttributesSchema`. Fix the inverted check in `AttributeSchema.Builder.OfTypes` to verify `!domains.isEmpty()`.

### Task 7.1: Mutual Infinite Recursion in EntitySchema
- **Donor Location**: 
```text
`xacml-core/src/main/java/org/xacml4j/v30/model/EntitySchema.java:54-59`
`xacml-core/src/main/java/org/xacml4j/v30/model/EntitySchema.java:51-53`
```
- **Defect Description**: Default method `type()` calls `recordSchema().typeQualifier()`, while default method `recordSchema()` calls `AttributesSchema.noAttributes(type())`. Invoking either method on an implementation inheriting both defaults causes unbounded mutual recursion resulting in `StackOverflowError`. Furthermore, line 52 contains incomplete syntax: `return recordSchema().` with a dangling dot that fails compilation.
- **Required Fix**: Decouple `type()` and `recordSchema()` defaults. Remove the incomplete method syntax and define clear record accessors in `EntitySchema.Category` and `EntitySchema.Nested`.

### Task 7.1: Missing Required-Domain Validation and Rogue Import
- **Donor Location**: 
```text
`xacml-core/src/main/java/org/xacml4j/v30/model/ValueDomain.java:43`
`xacml-core/src/main/java/org/xacml4j/v30/model/AttributesSchema.java:45-48`
`xacml-core/src/main/java/org/xacml4j/v30/model/ValueDomain.java:27`
```
- **Defect Description**: `ValueDomain` declares `boolean required()`, but `AttributesSchema.isValid(Record)` only iterates over attributes actually present in the record (`record.attributes().entrySet().stream().allMatch(...)`). Missing attributes marked as `required = true` are never detected, causing incomplete records to pass validation. In addition, line 27 contains a rogue compiler internal import: `import com.sun.source.doctree.ValueTree;`.
- **Required Fix**: Update `AttributesSchema.isValid(Record)` to verify that all schemas marked `required` have corresponding attribute values present in the record. Remove the rogue `ValueTree` import.

### Task 7.1: Broken Entity equals() and hashCode() Contract
- **Donor Location**: 
```text
`xacml-core/src/main/java/org/xacml4j/v30/types/Entity.java:139-150`
```
- **Defect Description**: In `Entity.equals()` (lines 139-145), if `eid != null`, equality checks *only* `Objects.equal(eid, entity.eid)` and ignores `valueType`, `record`, and `content`. In contrast, `Entity.hashCode()` (lines 148-150) computes a hash over all five fields: `Objects.hashCode(valueType, record, content, eid, parentId)`. Two entities with the same `eid` but different attributes will evaluate `equals() == true` while having different hash codes, violating the Java Object contract and corrupting hash maps and sets.
- **Required Fix**: Align `equals()` and `hashCode()` to hash and compare identical field sets consistently (either purely by canonical `eid`, or across all fields including `record` and `content`).

## 5. Proposed Swarm Lane Split for Groups 5–7
To allow multiple subagents or implementers to work concurrently without merge conflicts, each slice or lane must operate on **strictly disjoint file sets**.

### Swarm Lane Partitioning
| Lane | Assigned Tasks | Scope & Directory | File Count | Lines (+/-) | Concurrency Constraints |
|---|---|---|---|---|---|
| **Lane A** | Task 5.1 | `org.xacml4j.v30.identifier.*`, `URN8141.java` | 16 | +1174, -78 | Completely independent leaf. Can run immediately. |
| **Lane B** | Task 5.2, 5.3, 5.4 | `org.xacml4j.v30.types.*` (Value, Primitives, Int64) | 66 | +5089, -3259 | Core value system. Depends on Lane A for `ValueTypeId`. |
| **Lane C** | Task 6.4 | `ValueStream.java`, `SeqOf.java`, `util/Streams.java` | 6 | +831, -0 | Independent query views. Disjoint from AST and functions. |
| **Lane D** | Task 6.6 | `ISO8601*.java`, `Duration.java`, `DateTimeDurationUtils.java` | 15 | +882, -649 | Date/Time types. Reconciled with Bugs 4.10/4.11. Disjoint files. |
| **Lane E** | Task 6.1, 6.2 | `policy/function/*`, `ExpressionKind`, `FunctionSpec` | 87 | +2966, -2796 | Function specifications and standard implementations. |
| **Lane F** | Task 6.3 | `policy/combine/*` | 41 | +483, -513 | Combining algorithms and SPI. Completely disjoint from functions. |
| **Lane G1** | Task 6.5a | `xacml-core`: `Record`, `Entity`, Policy AST nodes, EvaluationContext | 127 | +3263, -4065 | Core policy model and evaluation engine. |
| **Lane G2** | Task 6.5b | `xacml-pdp`: PDP runtime, PIP, MultiResource handlers | 64 | +968, -1482 | PDP SPI and evaluation context handlers. Separate module. |
| **Lane G3** | Task 6.5c | `xacml-json`, `xacml-xml`: Marshalling providers and adapters | 46 | +694, -737 | External format marshalling. Disjoint modules. |
| **Lane H** | Task 7.1, 7.2 | `org.xacml4j.v30.model.*`, `org.xacml4j.v30.schemas.*` | 15 | +1576, -0 | Schema catalogs and AccessModel. New packages. |
| **Lane I** | Task 7.3 | Legacy type cleanup (`AttributeContainer`, etc.) | 7 | +4, -1009 | Final cleanup lane once all references are migrated. |

### Compile-Break Surface Analysis (Slice N without N+1)
Applying slices incrementally carries specific compile-break risks if boundary interfaces are modified before callers are updated:

- **Slice 5.1 applied alone**: **None**, provided that root compatibility adapters (`CategoryId`, `StatusCodeId`, `UrnIdentifier`, `Issuer`) are kept in `org.xacml4j.v30` delegating to `org.xacml4j.v30.identifier.*` records. If adapters are omitted, hundreds of files in `xacml-core` and `xacml-pdp` referencing `org.xacml4j.v30.CategoryId` fail compilation.
- **Slice 5.2 applied without 5.3 / 6.1**: **High break surface**: Renaming `BagOfValues` -> `BagOf`, `ValueExp` -> `ValueExpr`, and `AnyURI` -> `UriVal` will break all evaluation, function, and combine signatures across the repository. **Mitigation**: Maintain temporary subclass/typealias wrappers (`@Deprecated public class BagOfValues extends BagOf`, etc.) during Group 5 so that existing code compiles while new types are introduced.
- **Slice 5.3 applied without 5.4 / 6.2**: **Moderate break surface**: Deleting `XacmlTypes` and `TypeFactory` breaks code calling `XacmlTypes.STRING` or `XacmlTypes.INTEGER`. **Mitigation**: Keep deprecated delegate fields in `XacmlTypes` forwarding to `Primitives.*.type()` until Task 6.2/6.5 callers are updated.
- **Slice 5.4 applied without 6.2**: **Moderate break surface**: Deleting `IntegerVal` in favor of `Int64` breaks callers of `IntegerVal.of(...)`. **Mitigation**: Provide `@Deprecated class IntegerVal extends Int64` or alias constructor during transition.
- **Slice 6.1 applied without 6.2**: **High break surface**: Updating `FunctionSpec.getReturnType()` to return `ValueExprType` and changing `ParamSpec` breaks all 56 function classes in `policy/function/impl/` until Slice 6.2 updates their signatures.
- **Slice 6.3 applied alone**: **Low break surface**: Renaming `*CombiningAlgorithm` to `*Algorithm` only affects policy builders referencing the concrete algorithm class names rather than URN identifiers. URN resolution through the registry remains stable.
- **Slice 6.5 applied without 7.1**: **Moderate break surface**: `Entity.builder(EntityType)` needs `EntityType` and `EntityId`. If `model` schemas are not yet present, `Entity` should default to `AttributesSchema.noAttributes()`.
- **Slice 7.3 applied prematurely**: **Critical break surface**: Deleting `AttributeContainer`, `AttributeIndex`, or compatibility adapters while any caller still references them causes immediate build failure. Task 7.3 must be gated on `rtk rg` confirming zero callers remain.

## 6. Complete Inventory of All 493 Donor Files
Below is the complete file-by-file mapping of all 493 files in the donor diff, grouped by assigned slice:

### Slice 5.1 (16 files)
| Status | File Path | Added | Deleted | Net | Purpose / Action |
|---|---|---|---|---|---|
| A | `xacml-core/src/main/java/org/xacml4j/util/URN8141.java` | 146 | 0 | +146 | Identifier package canonical equality and URN records |
| M | `xacml-core/src/main/java/org/xacml4j/v30/CategoryId.java` | 29 | 27 | +2 | Temporary compatibility adapters forwarding to new identifier records |
| M | `xacml-core/src/main/java/org/xacml4j/v30/StatusCodeId.java` | 3 | 3 | +0 | Temporary compatibility adapters forwarding to new identifier records |
| R060 | `xacml-core/src/main/java/org/xacml4j/v30/SemanticIdentifier.java` -> `xacml-core/src/main/java/org/xacml4j/v30/UrnIdentifier.java` | 53 | 39 | +14 | Temporary compatibility adapters forwarding to new identifier records |
| A | `xacml-core/src/main/java/org/xacml4j/v30/identifier/AttributeId.java` | 74 | 0 | +74 | Identifier package canonical equality and URN records |
| A | `xacml-core/src/main/java/org/xacml4j/v30/identifier/CategoryId.java` | 117 | 0 | +117 | Identifier package canonical equality and URN records |
| A | `xacml-core/src/main/java/org/xacml4j/v30/identifier/Either.java` | 144 | 0 | +144 | Identifier package canonical equality and URN records |
| A | `xacml-core/src/main/java/org/xacml4j/v30/identifier/EntityTypeId.java` | 91 | 0 | +91 | Identifier package canonical equality and URN records |
| A | `xacml-core/src/main/java/org/xacml4j/v30/identifier/Identifier.java` | 179 | 0 | +179 | Identifier package canonical equality and URN records |
| A | `xacml-core/src/main/java/org/xacml4j/v30/identifier/Issuer.java` | 69 | 0 | +69 | Identifier package canonical equality and URN records |
| A | `xacml-core/src/main/java/org/xacml4j/v30/identifier/ParamId.java` | 42 | 0 | +42 | Identifier package canonical equality and URN records |
| A | `xacml-core/src/main/java/org/xacml4j/v30/identifier/StatusCodeId.java` | 43 | 0 | +43 | Identifier package canonical equality and URN records |
| A | `xacml-core/src/main/java/org/xacml4j/v30/identifier/URI.java` | 81 | 0 | +81 | Identifier package canonical equality and URN records |
| A | `xacml-core/src/main/java/org/xacml4j/v30/identifier/ValueTypeId.java` | 58 | 0 | +58 | Identifier package canonical equality and URN records |
| M | `xacml-core/src/test/java/org/xacml4j/v30/CategoryIdTest.java` | 9 | 9 | +0 | CategoryId identifier test |
| A | `xacml-core/src/test/java/org/xacml4j/v30/identifier/URITest.java` | 36 | 0 | +36 | Identifier package canonical equality and URN records |

### Slice 5.2 (27 files)
| Status | File Path | Added | Deleted | Net | Purpose / Action |
|---|---|---|---|---|---|
| D | `xacml-core/src/main/java/org/xacml4j/v30/BagOfValues.java` | 0 | 324 | -324 | Value and BagOf rename, range types, value type core |
| D | `xacml-core/src/main/java/org/xacml4j/v30/BagOfValuesType.java` | 0 | 105 | -105 | Value and BagOf rename, range types, value type core |
| M | `xacml-core/src/main/java/org/xacml4j/v30/TypeCapability.java` | 30 | 34 | -4 | Value and BagOf rename, range types, value type core |
| M | `xacml-core/src/main/java/org/xacml4j/v30/TypeCapabilityFactory.java` | 12 | 5 | +7 | Value and BagOf rename, range types, value type core |
| D | `xacml-core/src/main/java/org/xacml4j/v30/ValueExp.java` | 0 | 45 | -45 | Value and BagOf rename, range types, value type core |
| R092 | `xacml-core/src/main/java/org/xacml4j/v30/policy/function/XacmlFuncParamAnyValue.java` -> `xacml-core/src/main/java/org/xacml4j/v30/annotations/FuncParamAnyValue.java` | 2 | 2 | +0 | Value and BagOf rename, range types, value type core |
| A | `xacml-core/src/main/java/org/xacml4j/v30/annotations/RangeType.java` | 41 | 0 | +41 | Value and BagOf rename, range types, value type core |
| D | `xacml-core/src/main/java/org/xacml4j/v30/types/AnyURI.java` | 0 | 134 | -134 | Value and BagOf rename, range types, value type core |
| A | `xacml-core/src/main/java/org/xacml4j/v30/types/BagOf.java` | 211 | 0 | +211 | Value and BagOf rename, range types, value type core |
| A | `xacml-core/src/main/java/org/xacml4j/v30/types/BagOfType.java` | 153 | 0 | +153 | Value and BagOf rename, range types, value type core |
| A | `xacml-core/src/main/java/org/xacml4j/v30/types/ConstructableType.java` | 77 | 0 | +77 | Value and BagOf rename, range types, value type core |
| M | `xacml-core/src/main/java/org/xacml4j/v30/types/PortRange.java` | 233 | 256 | -23 | Value and BagOf rename, range types, value type core |
| A | `xacml-core/src/main/java/org/xacml4j/v30/types/PrimitiveType.java` | 79 | 0 | +79 | Value and BagOf rename, range types, value type core |
| A | `xacml-core/src/main/java/org/xacml4j/v30/types/Range.java` | 152 | 0 | +152 | Value and BagOf rename, range types, value type core |
| A | `xacml-core/src/main/java/org/xacml4j/v30/types/RangeType.java` | 239 | 0 | +239 | Value and BagOf rename, range types, value type core |
| A | `xacml-core/src/main/java/org/xacml4j/v30/types/Ranges.java` | 78 | 0 | +78 | Value and BagOf rename, range types, value type core |
| M | `xacml-core/src/main/java/org/xacml4j/v30/types/TypeToString.java` | 256 | 256 | +0 | Value and BagOf rename, range types, value type core |
| M | `xacml-core/src/main/java/org/xacml4j/v30/types/TypeToStringFactory.java` | 3 | 4 | -1 | Value and BagOf rename, range types, value type core |
| A | `xacml-core/src/main/java/org/xacml4j/v30/types/UriVal.java` | 149 | 0 | +149 | Value and BagOf rename, range types, value type core |
| M | `xacml-core/src/main/java/org/xacml4j/v30/types/Value.java` | 64 | 128 | -64 | Value and BagOf rename, range types, value type core |
| A | `xacml-core/src/main/java/org/xacml4j/v30/types/ValueExpr.java` | 90 | 0 | +90 | Value and BagOf rename, range types, value type core |
| M | `xacml-core/src/main/java/org/xacml4j/v30/types/ValueType.java` | 83 | 133 | -50 | Value and BagOf rename, range types, value type core |
| R052 | `xacml-core/src/main/java/org/xacml4j/v30/ValueExpTypeInfo.java` -> `xacml-core/src/main/java/org/xacml4j/v30/types/ValueTypeQualifier.java` | 15 | 32 | -17 | Value and BagOf rename, range types, value type core |
| M | `xacml-core/src/test/java/org/xacml4j/v30/types/TypeCapabilityTest.java` | 1 | 1 | +0 | Value and BagOf rename, range types, value type core |
| M | `xacml-core/src/test/java/org/xacml4j/v30/types/TypeToStringCapabilityTest.java` | 2 | 2 | +0 | Value and BagOf rename, range types, value type core |
| M | `xacml-core/src/test/java/org/xacml4j/v30/types/TypeToStringTest.java` | 2 | 2 | +0 | Value and BagOf rename, range types, value type core |
| R059 | `xacml-core/src/test/java/org/xacml4j/v30/types/AnyURITest.java` -> `xacml-core/src/test/java/org/xacml4j/v30/types/UriValTest.java` | 16 | 15 | +1 | Value and BagOf rename, range types, value type core |

### Slice 5.3 (30 files)
| Status | File Path | Added | Deleted | Net | Purpose / Action |
|---|---|---|---|---|---|
| M | `xacml-core/src/main/java/org/xacml4j/util/Base64.java` | 2 | 2 | +0 | Primitives facade, ValueTypeId registry, primitive data types |
| A | `xacml-core/src/main/java/org/xacml4j/v30/annotations/DataType.java` | 36 | 0 | +36 | Data type annotations |
| A | `xacml-core/src/main/java/org/xacml4j/v30/annotations/TypeFactoryMethod.java` | 37 | 0 | +37 | Data type annotations |
| A | `xacml-core/src/main/java/org/xacml4j/v30/annotations/Version.java` | 60 | 0 | +60 | Data type annotations |
| M | `xacml-core/src/main/java/org/xacml4j/v30/types/Base64Binary.java` | 66 | 27 | +39 | Primitives facade, ValueTypeId registry, primitive data types |
| M | `xacml-core/src/main/java/org/xacml4j/v30/types/Binary.java` | 106 | 220 | -114 | Primitives facade, ValueTypeId registry, primitive data types |
| M | `xacml-core/src/main/java/org/xacml4j/v30/types/BooleanVal.java` | 61 | 39 | +22 | Primitives facade, ValueTypeId registry, primitive data types |
| M | `xacml-core/src/main/java/org/xacml4j/v30/types/DNSName.java` | 113 | 152 | -39 | Primitives facade, ValueTypeId registry, primitive data types |
| M | `xacml-core/src/main/java/org/xacml4j/v30/types/DoubleVal.java` | 108 | 50 | +58 | Primitives facade, ValueTypeId registry, primitive data types |
| M | `xacml-core/src/main/java/org/xacml4j/v30/types/HexBinary.java` | 55 | 28 | +27 | Primitives facade, ValueTypeId registry, primitive data types |
| D | `xacml-core/src/main/java/org/xacml4j/v30/types/IPAddress.java` | 0 | 290 | -290 | Primitives facade, ValueTypeId registry, primitive data types |
| A | `xacml-core/src/main/java/org/xacml4j/v30/types/IPAddressV2.java` | 209 | 0 | +209 | Primitives facade, ValueTypeId registry, primitive data types |
| A | `xacml-core/src/main/java/org/xacml4j/v30/types/Primitives.java` | 136 | 0 | +136 | Primitives facade, ValueTypeId registry, primitive data types |
| M | `xacml-core/src/main/java/org/xacml4j/v30/types/RFC822Name.java` | 114 | 253 | -139 | Primitives facade, ValueTypeId registry, primitive data types |
| A | `xacml-core/src/main/java/org/xacml4j/v30/types/Rfc822NameValidator.java` | 175 | 0 | +175 | Primitives facade, ValueTypeId registry, primitive data types |
| M | `xacml-core/src/main/java/org/xacml4j/v30/types/StringVal.java` | 64 | 54 | +10 | Primitives facade, ValueTypeId registry, primitive data types |
| D | `xacml-core/src/main/java/org/xacml4j/v30/types/TypeFactory.java` | 0 | 93 | -93 | Primitives facade, ValueTypeId registry, primitive data types |
| M | `xacml-core/src/main/java/org/xacml4j/v30/types/X500Name.java` | 50 | 37 | +13 | Primitives facade, ValueTypeId registry, primitive data types |
| D | `xacml-core/src/main/java/org/xacml4j/v30/types/XacmlTypes.java` | 0 | 255 | -255 | Primitives facade, ValueTypeId registry, primitive data types |
| M | `xacml-core/src/test/java/org/xacml4j/v30/types/Base64BinaryTest.java` | 9 | 10 | -1 | Primitives facade, ValueTypeId registry, primitive data types |
| M | `xacml-core/src/test/java/org/xacml4j/v30/types/BinaryTest.java` | 4 | 4 | +0 | Primitives facade, ValueTypeId registry, primitive data types |
| M | `xacml-core/src/test/java/org/xacml4j/v30/types/BooleanValTest.java` | 18 | 19 | -1 | Primitives facade, ValueTypeId registry, primitive data types |
| M | `xacml-core/src/test/java/org/xacml4j/v30/types/DNSNameTest.java` | 12 | 12 | +0 | Primitives facade, ValueTypeId registry, primitive data types |
| M | `xacml-core/src/test/java/org/xacml4j/v30/types/DoubleValTest.java` | 10 | 10 | +0 | Primitives facade, ValueTypeId registry, primitive data types |
| M | `xacml-core/src/test/java/org/xacml4j/v30/types/HexTypeTest.java` | 6 | 7 | -1 | Primitives facade, ValueTypeId registry, primitive data types |
| M | `xacml-core/src/test/java/org/xacml4j/v30/types/IPAddressTest.java` | 37 | 37 | +0 | Primitives facade, ValueTypeId registry, primitive data types |
| M | `xacml-core/src/test/java/org/xacml4j/v30/types/PortRangeTest.java` | 28 | 28 | +0 | Primitives facade, ValueTypeId registry, primitive data types |
| M | `xacml-core/src/test/java/org/xacml4j/v30/types/RFC822NameTest.java` | 6 | 6 | +0 | Primitives facade, ValueTypeId registry, primitive data types |
| M | `xacml-core/src/test/java/org/xacml4j/v30/types/StringValTest.java` | 3 | 3 | +0 | Primitives facade, ValueTypeId registry, primitive data types |
| M | `xacml-core/src/test/java/org/xacml4j/v30/types/XacmlTypesTest.java` | 8 | 8 | +0 | Primitives facade, ValueTypeId registry, primitive data types |

### Slice 5.4 (9 files)
| Status | File Path | Added | Deleted | Net | Purpose / Action |
|---|---|---|---|---|---|
| A | `xacml-core/src/main/java/org/xacml4j/v30/IntegerArithmetics.java` | 175 | 0 | +175 | Numeric semantics (xs:integer owner, exact arithmetic, product identity) |
| A | `xacml-core/src/main/java/org/xacml4j/v30/types/Int32.java` | 272 | 0 | +272 | Numeric semantics (xs:integer owner, exact arithmetic, product identity) |
| A | `xacml-core/src/main/java/org/xacml4j/v30/types/Int64.java` | 247 | 0 | +247 | Numeric semantics (xs:integer owner, exact arithmetic, product identity) |
| D | `xacml-core/src/main/java/org/xacml4j/v30/types/IntegerVal.java` | 0 | 122 | -122 | Numeric semantics (xs:integer owner, exact arithmetic, product identity) |
| A | `xacml-core/src/main/java/org/xacml4j/v30/types/Number.java` | 180 | 0 | +180 | Numeric semantics (xs:integer owner, exact arithmetic, product identity) |
| A | `xacml-core/src/main/java/org/xacml4j/v30/types/UInt32.java` | 202 | 0 | +202 | Numeric semantics (xs:integer owner, exact arithmetic, product identity) |
| A | `xacml-core/src/main/java/org/xacml4j/v30/types/UInt64.java` | 210 | 0 | +210 | Numeric semantics (xs:integer owner, exact arithmetic, product identity) |
| A | `xacml-core/src/main/java/org/xacml4j/v30/types/UnsignedInteger.java` | 268 | 0 | +268 | Numeric semantics (xs:integer owner, exact arithmetic, product identity) |
| M | `xacml-core/src/test/java/org/xacml4j/v30/types/IntegerTypeTest.java` | 14 | 15 | -1 | Numeric semantics (xs:integer owner, exact arithmetic, product identity) |

### Slice 6.1 (31 files)
| Status | File Path | Added | Deleted | Net | Purpose / Action |
|---|---|---|---|---|---|
| R068 | `xacml-core/src/main/java/org/xacml4j/v30/Expression.java` -> `xacml-core/src/main/java/org/xacml4j/v30/Expr.java` | 22 | 12 | +10 | Expression AST nodes, ValueExprType result descriptors, and function specifications |
| A | `xacml-core/src/main/java/org/xacml4j/v30/ExpressionKind.java` | 107 | 0 | +107 | Expression AST nodes, ValueExprType result descriptors, and function specifications |
| M | `xacml-core/src/main/java/org/xacml4j/v30/ExpressionVisitor.java` | 26 | 2 | +24 | Expression AST nodes, ValueExprType result descriptors, and function specifications |
| R089 | `xacml-pdp/src/main/java/org/xacml4j/v30/spi/pip/XacmlAttributeDescriptor.java` -> `xacml-core/src/main/java/org/xacml4j/v30/annotations/Attribute.java` | 5 | 4 | +1 | Function specification and parameter annotations |
| R091 | `xacml-pdp/src/main/java/org/xacml4j/v30/spi/pip/XacmlAttributeDesignator.java` -> `xacml-core/src/main/java/org/xacml4j/v30/annotations/AttributeDesig.java` | 3 | 3 | +0 | Function specification and parameter annotations |
| R087 | `xacml-pdp/src/main/java/org/xacml4j/v30/spi/pip/XacmlAttributeSelector.java` -> `xacml-core/src/main/java/org/xacml4j/v30/annotations/AttributeSelect.java` | 8 | 2 | +6 | Function specification and parameter annotations |
| R092 | `xacml-core/src/main/java/org/xacml4j/v30/policy/function/XacmlFuncParam.java` -> `xacml-core/src/main/java/org/xacml4j/v30/annotations/FuncParam.java` | 3 | 3 | +0 | Function specification and parameter annotations |
| R092 | `xacml-core/src/main/java/org/xacml4j/v30/policy/function/XacmlFuncParamAnyBag.java` -> `xacml-core/src/main/java/org/xacml4j/v30/annotations/FuncParamAnyBag.java` | 2 | 2 | +0 | Function specification and parameter annotations |
| R092 | `xacml-core/src/main/java/org/xacml4j/v30/policy/function/XacmlEvaluationContextParam.java` -> `xacml-core/src/main/java/org/xacml4j/v30/annotations/FuncParamEvalCtx.java` | 2 | 2 | +0 | Function specification and parameter annotations |
| R091 | `xacml-core/src/main/java/org/xacml4j/v30/policy/function/XacmlFuncParamFunctionReference.java` -> `xacml-core/src/main/java/org/xacml4j/v30/annotations/FuncParamFunctionRef.java` | 2 | 2 | +0 | Function specification and parameter annotations |
| R091 | `xacml-core/src/main/java/org/xacml4j/v30/policy/function/XacmlFuncParamOptional.java` -> `xacml-core/src/main/java/org/xacml4j/v30/annotations/FuncParamOptional.java` | 3 | 3 | +0 | Function specification and parameter annotations |
| R088 | `xacml-core/src/main/java/org/xacml4j/v30/policy/function/XacmlFuncParamValidator.java` -> `xacml-core/src/main/java/org/xacml4j/v30/annotations/FuncParamValidator.java` | 4 | 2 | +2 | Function specification and parameter annotations |
| R091 | `xacml-core/src/main/java/org/xacml4j/v30/policy/function/XacmlFuncParamVarArg.java` -> `xacml-core/src/main/java/org/xacml4j/v30/annotations/FuncParamVarArg.java` | 4 | 3 | +1 | Function specification and parameter annotations |
| R090 | `xacml-core/src/main/java/org/xacml4j/v30/policy/function/XacmlFuncReturnType.java` -> `xacml-core/src/main/java/org/xacml4j/v30/annotations/FuncReturnType.java` | 3 | 3 | +0 | Function specification and parameter annotations |
| R088 | `xacml-core/src/main/java/org/xacml4j/v30/policy/function/XacmlFuncReturnTypeResolver.java` -> `xacml-core/src/main/java/org/xacml4j/v30/annotations/FuncReturnTypeResolver.java` | 4 | 2 | +2 | Function specification and parameter annotations |
| R092 | `xacml-core/src/main/java/org/xacml4j/v30/policy/function/XacmlFuncSpec.java` -> `xacml-core/src/main/java/org/xacml4j/v30/annotations/FuncSpec.java` | 3 | 2 | +1 | Function specification and parameter annotations |
| R090 | `xacml-pdp/src/main/java/org/xacml4j/v30/spi/pip/XacmlAttributeResolverDescriptor.java` -> `xacml-core/src/main/java/org/xacml4j/v30/annotations/PolicyAttrResolverDescr.java` | 3 | 3 | +0 | Function specification and parameter annotations |
| R091 | `xacml-core/src/main/java/org/xacml4j/v30/policy/combine/XacmlRuleDecisionCombiningAlgorithm.java` -> `xacml-core/src/main/java/org/xacml4j/v30/annotations/PolicyDecisionCombineAlgo.java` | 2 | 2 | +0 | Function specification and parameter annotations |
| R093 | `xacml-core/src/main/java/org/xacml4j/v30/policy/function/XacmlFunctionProvider.java` -> `xacml-core/src/main/java/org/xacml4j/v30/annotations/PolicyFuncProvider.java` | 2 | 2 | +0 | Function specification and parameter annotations |
| R093 | `xacml-core/src/main/java/org/xacml4j/v30/policy/function/XacmlLegacyFunc.java` -> `xacml-core/src/main/java/org/xacml4j/v30/annotations/PolicyLegacyFunc.java` | 2 | 2 | +0 | Function specification and parameter annotations |
| R091 | `xacml-core/src/main/java/org/xacml4j/v30/policy/combine/XacmlPolicyDecisionCombiningAlgorithm.java` -> `xacml-core/src/main/java/org/xacml4j/v30/annotations/RuleDecisionCombineAlgo.java` | 2 | 2 | +0 | Function specification and parameter annotations |
| M | `xacml-core/src/main/java/org/xacml4j/v30/policy/AdviceExpression.java` | 2 | 5 | -3 | Expression AST nodes, ValueExprType result descriptors, and function specifications |
| M | `xacml-core/src/main/java/org/xacml4j/v30/policy/AttributeAssignmentExpression.java` | 13 | 19 | -6 | Expression AST nodes, ValueExprType result descriptors, and function specifications |
| M | `xacml-core/src/main/java/org/xacml4j/v30/policy/BaseDecisionRuleResponseExpression.java` | 12 | 29 | -17 | Expression AST nodes, ValueExprType result descriptors, and function specifications |
| M | `xacml-core/src/main/java/org/xacml4j/v30/policy/FunctionParamSpec.java` | 15 | 9 | +6 | Expression AST nodes, ValueExprType result descriptors, and function specifications |
| M | `xacml-core/src/main/java/org/xacml4j/v30/policy/FunctionSpec.java` | 20 | 18 | +2 | Expression AST nodes, ValueExprType result descriptors, and function specifications |
| M | `xacml-core/src/main/java/org/xacml4j/v30/policy/ObligationExpression.java` | 0 | 5 | -5 | Expression AST nodes, ValueExprType result descriptors, and function specifications |
| A | `xacml-core/src/main/java/org/xacml4j/v30/policy/QuantifiedExp.java` | 140 | 0 | +140 | Expression AST nodes, ValueExprType result descriptors, and function specifications |
| D | `xacml-core/src/main/java/org/xacml4j/v30/policy/QuantifiedExpression.java` | 0 | 113 | -113 | Expression AST nodes, ValueExprType result descriptors, and function specifications |
| A | `xacml-core/src/main/java/org/xacml4j/v30/types/ValueExprType.java` | 28 | 0 | +28 | Expression AST nodes, ValueExprType result descriptors, and function specifications |
| M | `xacml-pdp/src/main/java/org/xacml4j/v30/pdp/DefaultExpressionVisitor.java` | 4 | 4 | +0 | Expression AST nodes, ValueExprType result descriptors, and function specifications |

### Slice 6.2 (56 files)
| Status | File Path | Added | Deleted | Net | Purpose / Action |
|---|---|---|---|---|---|
| M | `xacml-core/src/main/java/org/xacml4j/util/Reflections.java` | 11 | 9 | +2 | Function annotations, implementations, and function provider SPI |
| M | `xacml-core/src/main/java/org/xacml4j/v30/policy/function/FunctionInvocation.java` | 9 | 11 | -2 | Function annotations, implementations, and function provider SPI |
| M | `xacml-core/src/main/java/org/xacml4j/v30/policy/function/FunctionInvocationFactory.java` | 5 | 5 | +0 | Function annotations, implementations, and function provider SPI |
| M | `xacml-core/src/main/java/org/xacml4j/v30/policy/function/FunctionParamAnyBagSpec.java` | 24 | 19 | +5 | Function annotations, implementations, and function provider SPI |
| M | `xacml-core/src/main/java/org/xacml4j/v30/policy/function/FunctionParamAnyValueSpec.java` | 15 | 14 | +1 | Function annotations, implementations, and function provider SPI |
| M | `xacml-core/src/main/java/org/xacml4j/v30/policy/function/FunctionParamFuncReferenceSpec.java` | 11 | 10 | +1 | Function annotations, implementations, and function provider SPI |
| M | `xacml-core/src/main/java/org/xacml4j/v30/policy/function/FunctionParamValueTypeSequenceSpec.java` | 22 | 21 | +1 | Function annotations, implementations, and function provider SPI |
| M | `xacml-core/src/main/java/org/xacml4j/v30/policy/function/FunctionParamValueTypeSpec.java` | 20 | 20 | +0 | Function annotations, implementations, and function provider SPI |
| M | `xacml-core/src/main/java/org/xacml4j/v30/policy/function/FunctionParametersValidator.java` | 4 | 4 | +0 | Function annotations, implementations, and function provider SPI |
| M | `xacml-core/src/main/java/org/xacml4j/v30/policy/function/FunctionProvider.java` | 2 | 2 | +0 | Function annotations, implementations, and function provider SPI |
| M | `xacml-core/src/main/java/org/xacml4j/v30/policy/function/FunctionProviderBuilder.java` | 10 | 16 | -6 | Function annotations, implementations, and function provider SPI |
| M | `xacml-core/src/main/java/org/xacml4j/v30/policy/function/FunctionReturnTypeResolver.java` | 7 | 6 | +1 | Function annotations, implementations, and function provider SPI |
| M | `xacml-core/src/main/java/org/xacml4j/v30/policy/function/FunctionSpecBuilder.java` | 49 | 61 | -12 | Function annotations, implementations, and function provider SPI |
| M | `xacml-core/src/main/java/org/xacml4j/v30/policy/function/StaticFunctionReturnTypeResolver.java` | 8 | 9 | -1 | Function annotations, implementations, and function provider SPI |
| M | `xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/AnnotationBasedFunctionProvider.java` | 4 | 4 | +0 | Function annotations, implementations, and function provider SPI |
| M | `xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/ArithmeticFunctions.java` | 64 | 65 | -1 | Function annotations, implementations, and function provider SPI |
| M | `xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/AttributeDesignatorFunctions.java` | 53 | 46 | +7 | Function annotations, implementations, and function provider SPI |
| M | `xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/BagFunctions.java` | 402 | 401 | +1 | Function annotations, implementations, and function provider SPI |
| M | `xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/BaseFunctionParamSpec.java` | 6 | 6 | +0 | Function annotations, implementations, and function provider SPI |
| M | `xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/DateTimeArithmeticFunctions.java` | 32 | 32 | +0 | Function annotations, implementations, and function provider SPI |
| M | `xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/DefaultFunctionInvocation.java` | 11 | 13 | -2 | Function annotations, implementations, and function provider SPI |
| M | `xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/EqualityPredicates.java` | 72 | 72 | +0 | Function annotations, implementations, and function provider SPI |
| M | `xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/HigherOrderFunctions.java` | 121 | 129 | -8 | Function annotations, implementations, and function provider SPI |
| M | `xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/JavaMethodToFunctionSpecConverter.java` | 51 | 48 | +3 | Function annotations, implementations, and function provider SPI |
| M | `xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/LogicalFunctions.java` | 33 | 32 | +1 | Function annotations, implementations, and function provider SPI |
| M | `xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/NonNumericComparisonFunctions.java` | 87 | 86 | +1 | Function annotations, implementations, and function provider SPI |
| M | `xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/NumericComparisonFunctions.java` | 47 | 47 | +0 | Function annotations, implementations, and function provider SPI |
| M | `xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/NumericConversionFunctions.java` | 15 | 15 | +0 | Function annotations, implementations, and function provider SPI |
| M | `xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/RegExpFunctions.java` | 40 | 40 | +0 | Function annotations, implementations, and function provider SPI |
| M | `xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/SetFunctions.java` | 382 | 382 | +0 | Function annotations, implementations, and function provider SPI |
| M | `xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/SpecialMatchFunctions.java` | 15 | 15 | +0 | Function annotations, implementations, and function provider SPI |
| M | `xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/StringConversionFunctions.java` | 14 | 14 | +0 | Function annotations, implementations, and function provider SPI |
| M | `xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/StringFunctions.java` | 157 | 156 | +1 | Function annotations, implementations, and function provider SPI |
| M | `xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/XPathFunctions.java` | 54 | 59 | -5 | Function annotations, implementations, and function provider SPI |
| M | `xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/XacmlDefaultFunctions.java` | 2 | 2 | +0 | Function annotations, implementations, and function provider SPI |
| M | `xacml-core/src/test/java/org/xacml4j/v30/policy/function/FunctionSpecBuilderTest.java` | 32 | 32 | +0 | Function annotations, implementations, and function provider SPI |
| M | `xacml-core/src/test/java/org/xacml4j/v30/policy/function/ParamSingleTypeSpecTest.java` | 8 | 8 | +0 | Function annotations, implementations, and function provider SPI |
| M | `xacml-core/src/test/java/org/xacml4j/v30/policy/function/ParamTypeSequenceTest.java` | 53 | 53 | +0 | Function annotations, implementations, and function provider SPI |
| M | `xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/AnnotationBasedFunctionFactoryTest.java` | 20 | 20 | +0 | Function annotations, implementations, and function provider SPI |
| M | `xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/ArtimeticFunctionsTest.java` | 10 | 10 | +0 | Function annotations, implementations, and function provider SPI |
| M | `xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/AttributeDesignatorFunctionTest.java` | 21 | 27 | -6 | Function annotations, implementations, and function provider SPI |
| M | `xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/BagFunctionsTest.java` | 46 | 44 | +2 | Function annotations, implementations, and function provider SPI |
| M | `xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/DefaultFunctionFunctionInvocationTest.java` | 53 | 53 | +0 | Function annotations, implementations, and function provider SPI |
| M | `xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/DefaultFunctionSpecTest.java` | 60 | 60 | +0 | Function annotations, implementations, and function provider SPI |
| M | `xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/HigherOrderFunctionsTest.java` | 48 | 48 | +0 | Function annotations, implementations, and function provider SPI |
| M | `xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/JavaMethodToFunctionSpecTest.java` | 2 | 3 | -1 | Function annotations, implementations, and function provider SPI |
| M | `xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/JavaMethodToFunctionSpecTestFunctions.java` | 16 | 17 | -1 | Function annotations, implementations, and function provider SPI |
| M | `xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/LogicalFunctionsTest.java` | 65 | 65 | +0 | Function annotations, implementations, and function provider SPI |
| M | `xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/NonNumericComparisionFunctionTest.java` | 31 | 31 | +0 | Function annotations, implementations, and function provider SPI |
| M | `xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/NumericComparisonFunctionTest.java` | 38 | 38 | +0 | Function annotations, implementations, and function provider SPI |
| M | `xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/RegularExpressionFunctionsTest.java` | 23 | 23 | +0 | Function annotations, implementations, and function provider SPI |
| M | `xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/SetFunctionTest.java` | 18 | 18 | +0 | Function annotations, implementations, and function provider SPI |
| M | `xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/SpecialMatchFunctionsTest.java` | 16 | 16 | +0 | Function annotations, implementations, and function provider SPI |
| M | `xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/TestFunctions.java` | 63 | 62 | +1 | Function annotations, implementations, and function provider SPI |
| M | `xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/TestInstanceFunctions.java` | 18 | 18 | +0 | Function annotations, implementations, and function provider SPI |
| M | `xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/XPathFunctionsTest.java` | 20 | 19 | +1 | Function annotations, implementations, and function provider SPI |

### Slice 6.3 (41 files)
| Status | File Path | Added | Deleted | Net | Purpose / Action |
|---|---|---|---|---|---|
| R071 | `xacml-core/src/main/java/org/xacml4j/v30/policy/combine/AnnotatedDecisionCombiningAlgorithmProviderFactory.java` -> `xacml-core/src/main/java/org/xacml4j/v30/policy/combine/AnnotatedCombiningAlgorithmParser.java` | 30 | 34 | -4 | Combining algorithm renames (*CombiningAlgorithm -> *Algorithm) and provider SPI |
| R060 | `xacml-core/src/main/java/org/xacml4j/v30/policy/combine/BaseDecisionCombiningAlgorithmProvider.java` -> `xacml-core/src/main/java/org/xacml4j/v30/policy/combine/BaseCombiningAlgorithmProvider.java` | 22 | 26 | -4 | Combining algorithm renames (*CombiningAlgorithm -> *Algorithm) and provider SPI |
| R080 | `xacml-core/src/main/java/org/xacml4j/v30/policy/combine/BaseDecisionCombiningAlgorithm.java` -> `xacml-core/src/main/java/org/xacml4j/v30/policy/combine/BaseDecisionAlgorithm.java` | 7 | 8 | -1 | Combining algorithm renames (*CombiningAlgorithm -> *Algorithm) and provider SPI |
| A | `xacml-core/src/main/java/org/xacml4j/v30/policy/combine/CombiningAlgorithmProvider.java` | 172 | 0 | +172 | Combining algorithm renames (*CombiningAlgorithm -> *Algorithm) and provider SPI |
| R052 | `xacml-core/src/main/java/org/xacml4j/v30/policy/combine/AggregatingDecisionCombiningAlgorithmProvider.java` -> `xacml-core/src/main/java/org/xacml4j/v30/policy/combine/CompositeCombiningAlgorithmProvider.java` | 35 | 39 | -4 | Combining algorithm renames (*CombiningAlgorithm -> *Algorithm) and provider SPI |
| D | `xacml-core/src/main/java/org/xacml4j/v30/policy/combine/DecisionCombiningAlgorithmProvider.java` | 0 | 95 | -95 | Combining algorithm renames (*CombiningAlgorithm -> *Algorithm) and provider SPI |
| D | `xacml-core/src/main/java/org/xacml4j/v30/policy/combine/DecisionCombiningAlgorithmProviderBuilder.java` | 0 | 95 | -95 | Combining algorithm renames (*CombiningAlgorithm -> *Algorithm) and provider SPI |
| M | `xacml-core/src/main/java/org/xacml4j/v30/policy/combine/DecisionCombiningAlgorithms.java` | 4 | 4 | +0 | Combining algorithm renames (*CombiningAlgorithm -> *Algorithm) and provider SPI |
| R063 | `xacml-core/src/main/java/org/xacml4j/v30/policy/combine/impl/DefaultXacml30DecisionCombiningAlgorithms.java` -> `xacml-core/src/main/java/org/xacml4j/v30/policy/combine/impl/DefaultXacml30CombiningAlgorithms.java` | 16 | 16 | +0 | Combining algorithm renames (*CombiningAlgorithm -> *Algorithm) and provider SPI |
| M | `xacml-core/src/main/java/org/xacml4j/v30/policy/combine/impl/DenyOverrides.java` | 10 | 10 | +0 | Combining algorithm renames (*CombiningAlgorithm -> *Algorithm) and provider SPI |
| R083 | `xacml-core/src/main/java/org/xacml4j/v30/policy/combine/impl/DenyOverridesPolicyCombiningAlgorithm.java` -> `xacml-core/src/main/java/org/xacml4j/v30/policy/combine/impl/DenyOverridesPolicyAlgorithm.java` | 3 | 3 | +0 | Combining algorithm renames (*CombiningAlgorithm -> *Algorithm) and provider SPI |
| R082 | `xacml-core/src/main/java/org/xacml4j/v30/policy/combine/impl/DenyOverridesPolicyOrderedCombiningAlgorithm.java` -> `xacml-core/src/main/java/org/xacml4j/v30/policy/combine/impl/DenyOverridesPolicyOrderedAlgorithm.java` | 3 | 3 | +0 | Combining algorithm renames (*CombiningAlgorithm -> *Algorithm) and provider SPI |
| R088 | `xacml-core/src/main/java/org/xacml4j/v30/policy/combine/impl/DenyOverridesRuleCombiningAlgorithm.java` -> `xacml-core/src/main/java/org/xacml4j/v30/policy/combine/impl/DenyOverridesRuleAlgorithm.java` | 2 | 2 | +0 | Combining algorithm renames (*CombiningAlgorithm -> *Algorithm) and provider SPI |
| R087 | `xacml-core/src/main/java/org/xacml4j/v30/policy/combine/impl/DenyOverridesRuleOrderedCombiningAlgorithm.java` -> `xacml-core/src/main/java/org/xacml4j/v30/policy/combine/impl/DenyOverridesRuleOrderedAlgorithm.java` | 2 | 2 | +0 | Combining algorithm renames (*CombiningAlgorithm -> *Algorithm) and provider SPI |
| M | `xacml-core/src/main/java/org/xacml4j/v30/policy/combine/impl/DenyUnlessPermit.java` | 10 | 11 | -1 | Combining algorithm renames (*CombiningAlgorithm -> *Algorithm) and provider SPI |
| R082 | `xacml-core/src/main/java/org/xacml4j/v30/policy/combine/impl/DenyUnlessPermitPolicyCombiningAlgorithm.java` -> `xacml-core/src/main/java/org/xacml4j/v30/policy/combine/impl/DenyUnlessPermitPolicyAlgorithm.java` | 3 | 3 | +0 | Combining algorithm renames (*CombiningAlgorithm -> *Algorithm) and provider SPI |
| R087 | `xacml-core/src/main/java/org/xacml4j/v30/policy/combine/impl/DenyUnlessPermitRuleCombiningAlgorithm.java` -> `xacml-core/src/main/java/org/xacml4j/v30/policy/combine/impl/DenyUnlessPermitRuleAlgorithm.java` | 2 | 2 | +0 | Combining algorithm renames (*CombiningAlgorithm -> *Algorithm) and provider SPI |
| M | `xacml-core/src/main/java/org/xacml4j/v30/policy/combine/impl/FirstApplicable.java` | 10 | 10 | +0 | Combining algorithm renames (*CombiningAlgorithm -> *Algorithm) and provider SPI |
| R082 | `xacml-core/src/main/java/org/xacml4j/v30/policy/combine/impl/FirstApplicablePolicyCombiningAlgorithm.java` -> `xacml-core/src/main/java/org/xacml4j/v30/policy/combine/impl/FirstApplicablePolicyAlgorithm.java` | 3 | 3 | +0 | Combining algorithm renames (*CombiningAlgorithm -> *Algorithm) and provider SPI |
| R088 | `xacml-core/src/main/java/org/xacml4j/v30/policy/combine/impl/FirstApplicableRuleCombiningAlgorithm.java` -> `xacml-core/src/main/java/org/xacml4j/v30/policy/combine/impl/FirstApplicableRuleAlgorithm.java` | 2 | 2 | +0 | Combining algorithm renames (*CombiningAlgorithm -> *Algorithm) and provider SPI |
| M | `xacml-core/src/main/java/org/xacml4j/v30/policy/combine/impl/LegacyDenyOverridesPolicyCombineAlgorithm.java` | 8 | 8 | +0 | Combining algorithm renames (*CombiningAlgorithm -> *Algorithm) and provider SPI |
| M | `xacml-core/src/main/java/org/xacml4j/v30/policy/combine/impl/LegacyDenyOverridesRuleCombineAlgorithm.java` | 10 | 10 | +0 | Combining algorithm renames (*CombiningAlgorithm -> *Algorithm) and provider SPI |
| M | `xacml-core/src/main/java/org/xacml4j/v30/policy/combine/impl/LegacyPermitOverridesPolicyCombineAlgorithm.java` | 8 | 8 | +0 | Combining algorithm renames (*CombiningAlgorithm -> *Algorithm) and provider SPI |
| M | `xacml-core/src/main/java/org/xacml4j/v30/policy/combine/impl/LegacyPermitOverridesRuleCombineAlgorithm.java` | 9 | 9 | +0 | Combining algorithm renames (*CombiningAlgorithm -> *Algorithm) and provider SPI |
| R086 | `xacml-core/src/main/java/org/xacml4j/v30/policy/combine/impl/OnPermitApplySecondPolicyCombiningAlgorithm.java` -> `xacml-core/src/main/java/org/xacml4j/v30/policy/combine/impl/OnPermitApplySecondPolicyAlgorithm.java` | 10 | 10 | +0 | Combining algorithm renames (*CombiningAlgorithm -> *Algorithm) and provider SPI |
| R073 | `xacml-core/src/main/java/org/xacml4j/v30/policy/combine/impl/OnlyOneApplicablePolicyCombiningAlgorithm.java` -> `xacml-core/src/main/java/org/xacml4j/v30/policy/combine/impl/OnlyOneApplicablePolicyAlgorithm.java` | 11 | 11 | +0 | Combining algorithm renames (*CombiningAlgorithm -> *Algorithm) and provider SPI |
| M | `xacml-core/src/main/java/org/xacml4j/v30/policy/combine/impl/PermitOverrides.java` | 10 | 10 | +0 | Combining algorithm renames (*CombiningAlgorithm -> *Algorithm) and provider SPI |
| M | `xacml-core/src/main/java/org/xacml4j/v30/policy/combine/impl/PermitOverridesPolicyCombineAlgorithm.java` | 1 | 1 | +0 | Combining algorithm renames (*CombiningAlgorithm -> *Algorithm) and provider SPI |
| M | `xacml-core/src/main/java/org/xacml4j/v30/policy/combine/impl/PermitOverridesPolicyOrderedCombineAlgorithm.java` | 1 | 1 | +0 | Combining algorithm renames (*CombiningAlgorithm -> *Algorithm) and provider SPI |
| R087 | `xacml-core/src/main/java/org/xacml4j/v30/policy/combine/impl/PermitOverridesRuleOrderedCombiningAlgorithm.java` -> `xacml-core/src/main/java/org/xacml4j/v30/policy/combine/impl/PermitOverridesRuleOrderedAlgorithm.java` | 2 | 2 | +0 | Combining algorithm renames (*CombiningAlgorithm -> *Algorithm) and provider SPI |
| M | `xacml-core/src/main/java/org/xacml4j/v30/policy/combine/impl/PermitUnlessDeny.java` | 10 | 10 | +0 | Combining algorithm renames (*CombiningAlgorithm -> *Algorithm) and provider SPI |
| R085 | `xacml-core/src/main/java/org/xacml4j/v30/policy/combine/impl/PermitUnlessDenyPolicyCombiningAlgorithm.java` -> `xacml-core/src/main/java/org/xacml4j/v30/policy/combine/impl/PermitUnlessDenyPolicyAlgorithm.java` | 3 | 3 | +0 | Combining algorithm renames (*CombiningAlgorithm -> *Algorithm) and provider SPI |
| R087 | `xacml-core/src/main/java/org/xacml4j/v30/policy/combine/impl/PermitUnlessDenyRuleCombiningAlgorithm.java` -> `xacml-core/src/main/java/org/xacml4j/v30/policy/combine/impl/PermitUnlessDenyRuleAlgorithm.java` | 2 | 2 | +0 | Combining algorithm renames (*CombiningAlgorithm -> *Algorithm) and provider SPI |
| R069 | `xacml-core/src/test/java/org/xacml4j/v30/policy/combine/AnnotatedDecisionCombiningAlgorithmProviderFactoryTest.java` -> `xacml-core/src/test/java/org/xacml4j/v30/policy/combine/AnnotatedCombiningAlgorithmProviderFactoryTest.java` | 19 | 17 | +2 | Combining algorithm renames (*CombiningAlgorithm -> *Algorithm) and provider SPI |
| M | `xacml-core/src/test/java/org/xacml4j/v30/policy/combine/impl/DenyOverridesTest.java` | 6 | 6 | +0 | Combining algorithm renames (*CombiningAlgorithm -> *Algorithm) and provider SPI |
| M | `xacml-core/src/test/java/org/xacml4j/v30/policy/combine/impl/DenyUnlessPermitTest.java` | 6 | 6 | +0 | Combining algorithm renames (*CombiningAlgorithm -> *Algorithm) and provider SPI |
| M | `xacml-core/src/test/java/org/xacml4j/v30/policy/combine/impl/FirstApplicableTest.java` | 6 | 6 | +0 | Combining algorithm renames (*CombiningAlgorithm -> *Algorithm) and provider SPI |
| M | `xacml-core/src/test/java/org/xacml4j/v30/policy/combine/impl/OnPermitApplySecondPolicyTest.java` | 7 | 7 | +0 | Combining algorithm renames (*CombiningAlgorithm -> *Algorithm) and provider SPI |
| M | `xacml-core/src/test/java/org/xacml4j/v30/policy/combine/impl/OnlyOneApplicablePolicyCombiningAlgorithmTest.java` | 6 | 6 | +0 | Combining algorithm renames (*CombiningAlgorithm -> *Algorithm) and provider SPI |
| M | `xacml-core/src/test/java/org/xacml4j/v30/policy/combine/impl/PermitOverridesTest.java` | 6 | 6 | +0 | Combining algorithm renames (*CombiningAlgorithm -> *Algorithm) and provider SPI |
| M | `xacml-core/src/test/java/org/xacml4j/v30/policy/combine/impl/PermitUnlessDenyTest.java` | 6 | 6 | +0 | Combining algorithm renames (*CombiningAlgorithm -> *Algorithm) and provider SPI |

### Slice 6.4 (6 files)
| Status | File Path | Added | Deleted | Net | Purpose / Action |
|---|---|---|---|---|---|
| A | `xacml-core/src/main/java/org/xacml4j/util/Streams.java` | 65 | 0 | +65 | ValueStream and SeqOf non-Stream query views (Design D9) |
| A | `xacml-core/src/main/java/org/xacml4j/util/Strings.java` | 44 | 0 | +44 | ValueStream and SeqOf non-Stream query views (Design D9) |
| A | `xacml-core/src/main/java/org/xacml4j/v30/AttributeStream.java` | 103 | 0 | +103 | ValueStream and SeqOf non-Stream query views (Design D9) |
| A | `xacml-core/src/main/java/org/xacml4j/v30/ValueStream.java` | 255 | 0 | +255 | ValueStream and SeqOf non-Stream query views (Design D9) |
| A | `xacml-core/src/main/java/org/xacml4j/v30/ValueStreamOfType.java` | 291 | 0 | +291 | ValueStream and SeqOf non-Stream query views (Design D9) |
| A | `xacml-core/src/main/java/org/xacml4j/v30/types/SeqOf.java` | 73 | 0 | +73 | ValueStream and SeqOf non-Stream query views (Design D9) |

### Slice 6.5 (237 files)
| Status | File Path | Added | Deleted | Net | Purpose / Action |
|---|---|---|---|---|---|
| M | `xacml-core/src/main/java/org/xacml4j/util/DOMUtil.java` | 1 | 1 | +0 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| R088 | `xacml-core/src/main/java/org/xacml4j/v30/types/Xacml20XPathTo30Transformer.java` -> `xacml-core/src/main/java/org/xacml4j/util/Xacml20XPathTo30Transformer.java` | 8 | 5 | +3 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-core/src/main/java/org/xacml4j/v30/Advice.java` | 3 | 0 | +3 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-core/src/main/java/org/xacml4j/v30/Attribute.java` | 75 | 130 | -55 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-core/src/main/java/org/xacml4j/v30/AttributeAssignment.java` | 34 | 153 | -119 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-core/src/main/java/org/xacml4j/v30/AttributeDesignatorKey.java` | 19 | 125 | -106 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-core/src/main/java/org/xacml4j/v30/AttributeReferenceEvaluationException.java` | 7 | 6 | +1 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-core/src/main/java/org/xacml4j/v30/AttributeReferenceKey.java` | 24 | 62 | -38 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-core/src/main/java/org/xacml4j/v30/AttributeSelectorKey.java` | 16 | 149 | -133 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-core/src/main/java/org/xacml4j/v30/Content.java` | 127 | 84 | +43 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-core/src/main/java/org/xacml4j/v30/CoreException.java` | 13 | 8 | +5 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| R062 | `xacml-core/src/main/java/org/xacml4j/v30/RequestContext.java` -> `xacml-core/src/main/java/org/xacml4j/v30/MultiRequestContext.java` | 95 | 119 | -24 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-core/src/main/java/org/xacml4j/v30/PathEvaluationException.java` | 14 | 12 | +2 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-core/src/main/java/org/xacml4j/v30/ProcessingErrorException.java` | 10 | 8 | +2 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-core/src/main/java/org/xacml4j/v30/RequestReference.java` | 29 | 33 | -4 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-core/src/main/java/org/xacml4j/v30/RequestSyntaxException.java` | 0 | 2 | -2 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-core/src/main/java/org/xacml4j/v30/ResourceAttributes.java` | 5 | 4 | +1 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-core/src/main/java/org/xacml4j/v30/ResponseContext.java` | 3 | 3 | +0 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-core/src/main/java/org/xacml4j/v30/Result.java` | 35 | 33 | +2 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-core/src/main/java/org/xacml4j/v30/Status.java` | 48 | 101 | -53 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-core/src/main/java/org/xacml4j/v30/StatusCode.java` | 15 | 42 | -27 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-core/src/main/java/org/xacml4j/v30/StatusDetail.java` | 3 | 3 | +0 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-core/src/main/java/org/xacml4j/v30/SubjectAttributes.java` | 4 | 4 | +0 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-core/src/main/java/org/xacml4j/v30/SyntaxException.java` | 98 | 25 | +73 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-core/src/main/java/org/xacml4j/v30/Version.java` | 60 | 39 | +21 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-core/src/main/java/org/xacml4j/v30/VersionMatch.java` | 3 | 3 | +0 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-core/src/main/java/org/xacml4j/v30/content/JsonContent.java` | 20 | 25 | -5 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-core/src/main/java/org/xacml4j/v30/content/XPathProvider.java` | 6 | 12 | -6 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-core/src/main/java/org/xacml4j/v30/content/XmlContent.java` | 42 | 69 | -27 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-core/src/main/java/org/xacml4j/v30/marshal/MarshallingProvider.java` | 31 | 23 | +8 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-core/src/main/java/org/xacml4j/v30/marshal/MediaType.java` | 7 | 9 | -2 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| R096 | `xacml-core/src/main/java/org/xacml4j/v30/marshal/Marshaller.java` -> `xacml-core/src/main/java/org/xacml4j/v30/marshal/PolicyDomainWriter.java` | 1 | 1 | +0 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-core/src/main/java/org/xacml4j/v30/marshal/PolicyMarshaller.java` | 2 | 2 | +0 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-core/src/main/java/org/xacml4j/v30/marshal/PolicyUnmarshaller.java` | 1 | 1 | +0 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-core/src/main/java/org/xacml4j/v30/marshal/PolicyUnmarshallerSupport.java` | 14 | 19 | -5 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-core/src/main/java/org/xacml4j/v30/marshal/RequestMarshaller.java` | 2 | 2 | +0 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-core/src/main/java/org/xacml4j/v30/marshal/RequestUnmarshaller.java` | 2 | 2 | +0 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-core/src/main/java/org/xacml4j/v30/marshal/ResponseUnmarshaller.java` | 2 | 2 | +0 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| R091 | `xacml-core/src/main/java/org/xacml4j/v30/marshal/ResponseMarshaller.java` -> `xacml-core/src/main/java/org/xacml4j/v30/marshal/ResponseWriter.java` | 2 | 2 | +0 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-core/src/main/java/org/xacml4j/v30/marshal/Unmarshaller.java` | 2 | 2 | +0 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-core/src/main/java/org/xacml4j/v30/policy/Apply.java` | 31 | 86 | -55 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-core/src/main/java/org/xacml4j/v30/policy/AttributeDesignator.java` | 26 | 70 | -44 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-core/src/main/java/org/xacml4j/v30/policy/AttributeReference.java` | 33 | 89 | -56 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-core/src/main/java/org/xacml4j/v30/policy/AttributeSelector.java` | 20 | 62 | -42 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-core/src/main/java/org/xacml4j/v30/policy/BaseCompositeDecisionRule.java` | 28 | 30 | -2 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-core/src/main/java/org/xacml4j/v30/policy/BaseCompositeDecisionRuleDefaults.java` | 4 | 6 | -2 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-core/src/main/java/org/xacml4j/v30/policy/BaseCompositeDecisionRuleIDReference.java` | 3 | 5 | -2 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-core/src/main/java/org/xacml4j/v30/policy/BaseDecisionRule.java` | 7 | 13 | -6 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| R055 | `xacml-core/src/main/java/org/xacml4j/v30/policy/CombinerParameter.java` -> `xacml-core/src/main/java/org/xacml4j/v30/policy/CombinerParam.java` | 17 | 49 | -32 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| R089 | `xacml-core/src/main/java/org/xacml4j/v30/CompositeDecisionRule.java` -> `xacml-core/src/main/java/org/xacml4j/v30/policy/CompositeDecisionRule.java` | 7 | 1 | +6 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| R095 | `xacml-core/src/main/java/org/xacml4j/v30/CompositeDecisionRuleIDReference.java` -> `xacml-core/src/main/java/org/xacml4j/v30/policy/CompositeDecisionRuleIDReference.java` | 3 | 1 | +2 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-core/src/main/java/org/xacml4j/v30/policy/Condition.java` | 17 | 19 | -2 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| R097 | `xacml-core/src/main/java/org/xacml4j/v30/Decision.java` -> `xacml-core/src/main/java/org/xacml4j/v30/policy/Decision.java` | 1 | 1 | +0 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| R088 | `xacml-core/src/main/java/org/xacml4j/v30/policy/DecisionCombiningAlgorithm.java` -> `xacml-core/src/main/java/org/xacml4j/v30/policy/DecisionAlgorithm.java` | 1 | 5 | -4 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| R097 | `xacml-core/src/main/java/org/xacml4j/v30/DecisionRule.java` -> `xacml-core/src/main/java/org/xacml4j/v30/policy/DecisionRule.java` | 3 | 1 | +2 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| R097 | `xacml-core/src/main/java/org/xacml4j/v30/DecisionRuleResponse.java` -> `xacml-core/src/main/java/org/xacml4j/v30/policy/DecisionRuleResponse.java` | 6 | 4 | +2 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-core/src/main/java/org/xacml4j/v30/policy/DescendantEvaluationContext.java` | 125 | 102 | +23 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| R097 | `xacml-core/src/main/java/org/xacml4j/v30/Effect.java` -> `xacml-core/src/main/java/org/xacml4j/v30/policy/Effect.java` | 1 | 1 | +0 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| R078 | `xacml-core/src/main/java/org/xacml4j/v30/EvaluationContext.java` -> `xacml-core/src/main/java/org/xacml4j/v30/policy/EvaluationContext.java` | 64 | 41 | +23 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-core/src/main/java/org/xacml4j/v30/policy/EvaluationContextHandler.java` | 8 | 14 | -6 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| R068 | `xacml-core/src/main/java/org/xacml4j/v30/EvaluationException.java` -> `xacml-core/src/main/java/org/xacml4j/v30/policy/EvaluationException.java` | 28 | 3 | +25 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-core/src/main/java/org/xacml4j/v30/policy/FunctionInvocationException.java` | 2 | 3 | -1 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-core/src/main/java/org/xacml4j/v30/policy/FunctionReference.java` | 25 | 35 | -10 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-core/src/main/java/org/xacml4j/v30/policy/Match.java` | 15 | 19 | -4 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-core/src/main/java/org/xacml4j/v30/policy/MatchAllOf.java` | 4 | 8 | -4 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-core/src/main/java/org/xacml4j/v30/policy/MatchAnyOf.java` | 4 | 7 | -3 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-core/src/main/java/org/xacml4j/v30/policy/Matchable.java` | 0 | 2 | -2 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| R098 | `xacml-core/src/main/java/org/xacml4j/v30/Obligation.java` -> `xacml-core/src/main/java/org/xacml4j/v30/policy/Obligation.java` | 1 | 1 | +0 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-core/src/main/java/org/xacml4j/v30/policy/Policy.java` | 34 | 88 | -54 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| R081 | `xacml-core/src/main/java/org/xacml4j/v30/PolicyDecisionPoint.java` -> `xacml-core/src/main/java/org/xacml4j/v30/policy/PolicyDecisionPoint.java` | 7 | 3 | +4 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| R092 | `xacml-core/src/main/java/org/xacml4j/v30/PolicyElement.java` -> `xacml-core/src/main/java/org/xacml4j/v30/policy/PolicyElement.java` | 1 | 3 | -2 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-core/src/main/java/org/xacml4j/v30/policy/PolicyIDReference.java` | 1 | 8 | -7 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-core/src/main/java/org/xacml4j/v30/policy/PolicyReferenceResolver.java` | 0 | 2 | -2 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| R089 | `xacml-core/src/main/java/org/xacml4j/v30/PolicyResolutionException.java` -> `xacml-core/src/main/java/org/xacml4j/v30/policy/PolicyResolutionException.java` | 6 | 2 | +4 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-core/src/main/java/org/xacml4j/v30/policy/PolicySet.java` | 45 | 52 | -7 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-core/src/main/java/org/xacml4j/v30/policy/PolicySetIDReference.java` | 1 | 8 | -7 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-core/src/main/java/org/xacml4j/v30/policy/PolicySyntaxException.java` | 20 | 20 | +0 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-core/src/main/java/org/xacml4j/v30/policy/PolicyTreeVisitor.java` | 2 | 2 | +0 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-core/src/main/java/org/xacml4j/v30/policy/PolicyVisitorSupport.java` | 2 | 2 | +0 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-core/src/main/java/org/xacml4j/v30/policy/QuantifiedForAll.java` | 25 | 23 | +2 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-core/src/main/java/org/xacml4j/v30/policy/QuantifiedForAny.java` | 23 | 23 | +0 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-core/src/main/java/org/xacml4j/v30/policy/QuantifiedMap.java` | 16 | 19 | -3 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-core/src/main/java/org/xacml4j/v30/policy/QuantifiedSelect.java` | 18 | 26 | -8 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-core/src/main/java/org/xacml4j/v30/policy/RootEvaluationContext.java` | 37 | 61 | -24 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-core/src/main/java/org/xacml4j/v30/policy/Rule.java` | 2 | 9 | -7 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-core/src/main/java/org/xacml4j/v30/policy/Target.java` | 3 | 6 | -3 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-core/src/main/java/org/xacml4j/v30/policy/VariableDefinition.java` | 32 | 21 | +11 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-core/src/main/java/org/xacml4j/v30/policy/VariableReference.java` | 10 | 13 | -3 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-core/src/main/java/org/xacml4j/v30/types/Entity.java` | 120 | 290 | -170 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| A | `xacml-core/src/main/java/org/xacml4j/v30/types/EntityId.java` | 61 | 0 | +61 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| A | `xacml-core/src/main/java/org/xacml4j/v30/types/EntityType.java` | 72 | 0 | +72 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| A | `xacml-core/src/main/java/org/xacml4j/v30/types/JsonPath.java` | 72 | 0 | +72 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-core/src/main/java/org/xacml4j/v30/types/Path.java` | 15 | 147 | -132 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| A | `xacml-core/src/main/java/org/xacml4j/v30/types/Record.java` | 68 | 0 | +68 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| A | `xacml-core/src/main/java/org/xacml4j/v30/types/XPath.java` | 92 | 0 | +92 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-core/src/test/java/org/xacml4j/v30/AttributeTest.java` | 34 | 34 | +0 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-core/src/test/java/org/xacml4j/v30/EntityTest.java` | 47 | 47 | +0 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-core/src/test/java/org/xacml4j/v30/ObligationTest.java` | 8 | 7 | +1 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| A | `xacml-core/src/test/java/org/xacml4j/v30/RecordTest.java` | 51 | 0 | +51 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-core/src/test/java/org/xacml4j/v30/content/JsonContentTest.java` | 8 | 8 | +0 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-core/src/test/java/org/xacml4j/v30/content/XmlContentTest.java` | 37 | 37 | +0 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-core/src/test/java/org/xacml4j/v30/policy/AdviceExpressionTest.java` | 9 | 15 | -6 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-core/src/test/java/org/xacml4j/v30/policy/AdviceTest.java` | 41 | 42 | -1 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-core/src/test/java/org/xacml4j/v30/policy/ApplyTest.java` | 5 | 7 | -2 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-core/src/test/java/org/xacml4j/v30/policy/AttributeAssignmentTest.java` | 6 | 6 | +0 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-core/src/test/java/org/xacml4j/v30/policy/AttributeDesignatorTest.java` | 24 | 30 | -6 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-core/src/test/java/org/xacml4j/v30/policy/AttributeSelectorTest.java` | 43 | 48 | -5 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-core/src/test/java/org/xacml4j/v30/policy/AttributesTest.java` | 48 | 44 | +4 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-core/src/test/java/org/xacml4j/v30/policy/BagOfValuesTest.java` | 92 | 95 | -3 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-core/src/test/java/org/xacml4j/v30/policy/CombinerParameterTest.java` | 3 | 3 | +0 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-core/src/test/java/org/xacml4j/v30/policy/ConditionTest.java` | 13 | 14 | -1 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-core/src/test/java/org/xacml4j/v30/policy/DecisionTest.java` | 0 | 1 | -1 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-core/src/test/java/org/xacml4j/v30/policy/MatchAllOfTest.java` | 0 | 1 | -1 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-core/src/test/java/org/xacml4j/v30/policy/MatchAnyOfTest.java` | 0 | 1 | -1 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-core/src/test/java/org/xacml4j/v30/policy/MatchTest.java` | 17 | 24 | -7 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-core/src/test/java/org/xacml4j/v30/policy/ObligationExpressionTest.java` | 9 | 17 | -8 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-core/src/test/java/org/xacml4j/v30/policy/PolicyIDReferenceTest.java` | 2 | 6 | -4 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-core/src/test/java/org/xacml4j/v30/policy/PolicySetIDReferenceTest.java` | 5 | 9 | -4 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-core/src/test/java/org/xacml4j/v30/policy/PolicySetTest.java` | 15 | 21 | -6 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-core/src/test/java/org/xacml4j/v30/policy/PolicyTest.java` | 15 | 20 | -5 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-core/src/test/java/org/xacml4j/v30/policy/RequestTest.java` | 47 | 48 | -1 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-core/src/test/java/org/xacml4j/v30/policy/ResultTest.java` | 6 | 10 | -4 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-core/src/test/java/org/xacml4j/v30/policy/RootEvaluationContextTest.java` | 4 | 6 | -2 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-core/src/test/java/org/xacml4j/v30/policy/RuleTest.java` | 31 | 42 | -11 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-core/src/test/java/org/xacml4j/v30/policy/StatusCodeIdTest.java` | 3 | 3 | +0 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-core/src/test/java/org/xacml4j/v30/policy/TargetTest.java` | 0 | 1 | -1 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-core/src/test/java/org/xacml4j/v30/policy/VariableDefinitionTest.java` | 10 | 12 | -2 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-core/src/test/java/org/xacml4j/v30/policy/VariableReferenceTest.java` | 10 | 12 | -2 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-core/src/test/java/org/xacml4j/v30/policy/VersionMatchTest.java` | 15 | 15 | +0 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-core/src/test/java/org/xacml4j/v30/policy/VersionTest.java` | 15 | 15 | +0 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-core/src/test/java/org/xacml4j/v30/types/PathTest.java` | 4 | 4 | +0 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-core/src/test/java/org/xacml4j/v30/types/Xacml20XPathTo30TransformerTest.java` | 7 | 7 | +0 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-json/src/main/java/org/xacml4j/v30/marshal/json/AttributeAssignmentDeserializer.java` | 1 | 1 | +0 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-json/src/main/java/org/xacml4j/v30/marshal/json/AttributeAssignmentSerializer.java` | 3 | 3 | +0 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-json/src/main/java/org/xacml4j/v30/marshal/json/AttributeDeserializer.java` | 9 | 9 | +0 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-json/src/main/java/org/xacml4j/v30/marshal/json/AttributeSerializer.java` | 8 | 8 | +0 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-json/src/main/java/org/xacml4j/v30/marshal/json/AttributesReferenceAdapter.java` | 7 | 7 | +0 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-json/src/main/java/org/xacml4j/v30/marshal/json/CategoryAdapter.java` | 13 | 12 | +1 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-json/src/main/java/org/xacml4j/v30/marshal/json/JsonRequestContextMarshaller.java` | 12 | 12 | +0 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-json/src/main/java/org/xacml4j/v30/marshal/json/JsonRequestContextUnmarshaller.java` | 9 | 9 | +0 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-json/src/main/java/org/xacml4j/v30/marshal/json/JsonResponseContextMarshaller.java` | 5 | 5 | +0 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-json/src/main/java/org/xacml4j/v30/marshal/json/JsonResponseContextUnmarshaller.java` | 3 | 3 | +0 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-json/src/main/java/org/xacml4j/v30/marshal/json/ObligationOrAdviceAdapter.java` | 2 | 2 | +0 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-json/src/main/java/org/xacml4j/v30/marshal/json/RequestContextAdapter.java` | 8 | 8 | +0 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-json/src/main/java/org/xacml4j/v30/marshal/json/RequestReferenceAdapter.java` | 4 | 4 | +0 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-json/src/main/java/org/xacml4j/v30/marshal/json/ResultAdapter.java` | 8 | 8 | +0 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-json/src/main/java/org/xacml4j/v30/marshal/json/StatusAdapter.java` | 1 | 1 | +0 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-json/src/main/java/org/xacml4j/v30/marshal/json/StatusCodeAdapter.java` | 2 | 2 | +0 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-json/src/main/java/org/xacml4j/v30/marshal/json/TypeToGSon.java` | 37 | 39 | -2 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-json/src/test/java/org/xacml4j/v30/marshal/json/JsonEntityMarshallingTest.java` | 31 | 31 | +0 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-json/src/test/java/org/xacml4j/v30/marshal/json/JsonRequestUnmarshallerTest.java` | 33 | 34 | -1 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-json/src/test/java/org/xacml4j/v30/marshal/json/JsonResponseContextMarshallerTest.java` | 27 | 28 | -1 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-json/src/test/java/org/xacml4j/v30/marshal/json/JsonResponseContextUnmarshallerTest.java` | 20 | 21 | -1 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-pdp/src/main/java/org/xacml4j/v30/pdp/DefaultEvaluationContextHandler.java` | 23 | 23 | +0 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-pdp/src/main/java/org/xacml4j/v30/pdp/DefaultPolicyDecisionPoint.java` | 50 | 60 | -10 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| D | `xacml-pdp/src/main/java/org/xacml4j/v30/pdp/MDCSupport.java` | 0 | 119 | -119 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-pdp/src/main/java/org/xacml4j/v30/pdp/PolicyDecisionCallback.java` | 3 | 3 | +0 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-pdp/src/main/java/org/xacml4j/v30/pdp/PolicyDecisionPointBuilder.java` | 2 | 2 | +0 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-pdp/src/main/java/org/xacml4j/v30/pdp/PolicyDecisionPointContext.java` | 6 | 6 | +0 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-pdp/src/main/java/org/xacml4j/v30/pdp/PolicyDecisionPointContextFactory.java` | 10 | 10 | +0 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-pdp/src/main/java/org/xacml4j/v30/pdp/RequestContextCallback.java` | 3 | 3 | +0 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-pdp/src/main/java/org/xacml4j/v30/pdp/request/AbstractRequestContextHandler.java` | 2 | 2 | +0 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-pdp/src/main/java/org/xacml4j/v30/pdp/request/MultipleResourcesHandler.java` | 3 | 3 | +0 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-pdp/src/main/java/org/xacml4j/v30/pdp/request/MultipleResourcesViaRepeatingAttributesHandler.java` | 9 | 9 | +0 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-pdp/src/main/java/org/xacml4j/v30/pdp/request/MultipleResourcesViaRequestReferencesHandler.java` | 12 | 12 | +0 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-pdp/src/main/java/org/xacml4j/v30/pdp/request/MultipleResourcesViaXPathExpressionHandler.java` | 40 | 40 | +0 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-pdp/src/main/java/org/xacml4j/v30/pdp/request/MultipleResourcesViaXPathExpressionLegacyHandler.java` | 20 | 20 | +0 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-pdp/src/main/java/org/xacml4j/v30/spi/audit/BasePolicyDecisionAuditor.java` | 11 | 11 | +0 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-pdp/src/main/java/org/xacml4j/v30/spi/audit/NoAuditPolicyDecisionPointAuditor.java` | 3 | 3 | +0 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-pdp/src/main/java/org/xacml4j/v30/spi/audit/PolicyDecisionAuditor.java` | 4 | 4 | +0 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-pdp/src/main/java/org/xacml4j/v30/spi/pdp/NoCachePolicyDecisionCache.java` | 3 | 3 | +0 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-pdp/src/main/java/org/xacml4j/v30/spi/pdp/PolicyDecisionCache.java` | 3 | 3 | +0 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-pdp/src/main/java/org/xacml4j/v30/spi/pdp/RequestContextHandler.java` | 2 | 2 | +0 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-pdp/src/main/java/org/xacml4j/v30/spi/pdp/RequestContextHandlerChain.java` | 5 | 5 | +0 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-pdp/src/main/java/org/xacml4j/v30/spi/pip/AnnotatedResolverFactory.java` | 27 | 71 | -44 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-pdp/src/main/java/org/xacml4j/v30/spi/pip/AttributeDescriptor.java` | 38 | 9 | +29 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-pdp/src/main/java/org/xacml4j/v30/spi/pip/AttributeResolverDescriptor.java` | 33 | 48 | -15 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-pdp/src/main/java/org/xacml4j/v30/spi/pip/AttributeSet.java` | 17 | 17 | +0 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-pdp/src/main/java/org/xacml4j/v30/spi/pip/BasePolicyInformationPointCacheProvider.java` | 0 | 36 | -36 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-pdp/src/main/java/org/xacml4j/v30/spi/pip/BaseResolverDescriptor.java` | 1 | 1 | +0 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| D | `xacml-pdp/src/main/java/org/xacml4j/v30/spi/pip/ContentRef.java` | 0 | 100 | -100 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| D | `xacml-pdp/src/main/java/org/xacml4j/v30/spi/pip/ContentResolverDescriptor.java` | 0 | 88 | -88 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-pdp/src/main/java/org/xacml4j/v30/spi/pip/DefaultPolicyInformationPoint.java` | 8 | 37 | -29 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-pdp/src/main/java/org/xacml4j/v30/spi/pip/DefaultResolverRegistry.java` | 28 | 44 | -16 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-pdp/src/main/java/org/xacml4j/v30/spi/pip/PolicyInformationPoint.java` | 25 | 18 | +7 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-pdp/src/main/java/org/xacml4j/v30/spi/pip/PolicyInformationPointCacheProvider.java` | 0 | 23 | -23 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-pdp/src/main/java/org/xacml4j/v30/spi/pip/ResolverCacheKey.java` | 18 | 14 | +4 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-pdp/src/main/java/org/xacml4j/v30/spi/pip/ResolverContext.java` | 11 | 11 | +0 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-pdp/src/main/java/org/xacml4j/v30/spi/pip/ResolverDescriptor.java` | 8 | 8 | +0 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-pdp/src/main/java/org/xacml4j/v30/spi/pip/ResolverRegistry.java` | 42 | 70 | -28 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| D | `xacml-pdp/src/main/java/org/xacml4j/v30/spi/pip/XacmlContentResolverDescriptor.java` | 0 | 40 | -40 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-pdp/src/main/java/org/xacml4j/v30/spi/repository/AbstractPolicyRepository.java` | 10 | 10 | +0 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-pdp/src/main/java/org/xacml4j/v30/spi/repository/DefaultPolicyReferenceResolver.java` | 1 | 1 | +0 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-pdp/src/main/java/org/xacml4j/v30/spi/repository/InMemoryPolicyRepository.java` | 3 | 3 | +0 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-pdp/src/main/java/org/xacml4j/v30/spi/repository/PolicyImportTool.java` | 2 | 2 | +0 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-pdp/src/main/java/org/xacml4j/v30/spi/repository/PolicyRepository.java` | 4 | 4 | +0 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-pdp/src/test/java/org/xacml4j/v30/pdp/DefaultEvaluationContextHandlerTest.java` | 46 | 46 | +0 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-pdp/src/test/java/org/xacml4j/v30/pdp/DefaultPolicyDecisionPointTest.java` | 8 | 8 | +0 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-pdp/src/test/java/org/xacml4j/v30/pdp/request/MultipleResourcesHandlerTest.java` | 53 | 54 | -1 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-pdp/src/test/java/org/xacml4j/v30/pdp/request/MultipleResourcesViaRepeatingAttributesHandlerTest.java` | 37 | 38 | -1 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-pdp/src/test/java/org/xacml4j/v30/pdp/request/MultipleResourcesViaRequestReferencesHandlerTest.java` | 48 | 49 | -1 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-pdp/src/test/java/org/xacml4j/v30/pdp/request/MultipleResourcesViaXacmlXPathHandlerTest.java` | 76 | 76 | +0 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| R097 | `xacml-pdp/src/test/java/org/xacml4j/v30/spi/pdp/RequestContextHandlerChainTest.java` -> `xacml-pdp/src/test/java/org/xacml4j/v30/spi/pdp/MultiRequestContextHandlerChainTest.java` | 1 | 1 | +0 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-pdp/src/test/java/org/xacml4j/v30/spi/pip/AnnotatedResolverFactoryTest.java` | 60 | 70 | -10 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-pdp/src/test/java/org/xacml4j/v30/spi/pip/AttributeResolverDescriptorTest.java` | 24 | 24 | +0 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-pdp/src/test/java/org/xacml4j/v30/spi/pip/AttributeSetTest.java` | 18 | 18 | +0 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-pdp/src/test/java/org/xacml4j/v30/spi/pip/BaseAttributeResolverTest.java` | 5 | 5 | +0 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-pdp/src/test/java/org/xacml4j/v30/spi/pip/ContentResolverDescriptorTest.java` | 3 | 3 | +0 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-pdp/src/test/java/org/xacml4j/v30/spi/pip/DefaultPolicyInformationPointTest.java` | 36 | 36 | +0 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-pdp/src/test/java/org/xacml4j/v30/spi/pip/DefaultResolverRegistryTest.java` | 15 | 15 | +0 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-pdp/src/test/java/org/xacml4j/v30/spi/repository/DefaultPolicyReferenceResolverTest.java` | 3 | 3 | +0 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-pdp/src/test/java/org/xacml4j/v30/spi/repository/InMemoryPolicyRepositoryTest.java` | 22 | 22 | +0 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| R092 | `xacml-xml/src/main/java/org/xacml4j/v30/xml/BaseJAXBMarshaller.java` -> `xacml-xml/src/main/java/org/xacml4j/v30/xml/BaseJAXBPolicyDomainWriter.java` | 3 | 3 | +0 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-xml/src/main/java/org/xacml4j/v30/xml/ExpressionTypeBuilder.java` | 27 | 27 | +0 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-xml/src/main/java/org/xacml4j/v30/xml/TypeToXacml30.java` | 72 | 72 | +0 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-xml/src/main/java/org/xacml4j/v30/xml/VariableManager.java` | 2 | 2 | +0 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-xml/src/main/java/org/xacml4j/v30/xml/Xacm20MarshallingProvider.java` | 8 | 8 | +0 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-xml/src/main/java/org/xacml4j/v30/xml/Xacm30MarshallingProvider.java` | 9 | 9 | +0 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-xml/src/main/java/org/xacml4j/v30/xml/Xacml20PolicyFromJaxbToObjectModelMapper.java` | 15 | 15 | +0 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-xml/src/main/java/org/xacml4j/v30/xml/Xacml20RequestContextUnmarshaller.java` | 44 | 44 | +0 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-xml/src/main/java/org/xacml4j/v30/xml/Xacml20ResponseContextUnmarshaller.java` | 9 | 10 | -1 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| R088 | `xacml-xml/src/main/java/org/xacml4j/v30/xml/Xacml20ResponseContextMarshaller.java` -> `xacml-xml/src/main/java/org/xacml4j/v30/xml/Xacml20ResponseContextWriter.java` | 19 | 19 | +0 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-xml/src/main/java/org/xacml4j/v30/xml/Xacml30PolicyFromJaxbToObjectModelMapper.java` | 22 | 22 | +0 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-xml/src/main/java/org/xacml4j/v30/xml/Xacml30PolicyFromObjectModelToJaxbMapper.java` | 14 | 14 | +0 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-xml/src/main/java/org/xacml4j/v30/xml/Xacml30PolicyUnmarshaller.java` | 4 | 4 | +0 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-xml/src/main/java/org/xacml4j/v30/xml/Xacml30RequestContextFromJaxbToObjectModelMapper.java` | 39 | 39 | +0 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-xml/src/main/java/org/xacml4j/v30/xml/Xacml30RequestContextMarshaller.java` | 3 | 3 | +0 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-xml/src/main/java/org/xacml4j/v30/xml/Xacml30RequestContextUnmarshaller.java` | 3 | 3 | +0 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| R086 | `xacml-xml/src/main/java/org/xacml4j/v30/xml/Xacml30ResponseContextMarshaller.java` -> `xacml-xml/src/main/java/org/xacml4j/v30/xml/Xacml30ResponseContextWriter.java` | 5 | 5 | +0 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-xml/src/main/java/org/xacml4j/v30/xml/XacmlPolicyMarshaller.java` | 2 | 2 | +0 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-xml/src/main/java/org/xacml4j/v30/xml/XacmlPolicyUnmarshaller.java` | 5 | 5 | +0 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-xml/src/test/java/org/xacml4j/v30/xml/ExpressionBuilderTest.java` | 1 | 2 | -1 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-xml/src/test/java/org/xacml4j/v30/xml/Xacml20RequestContextUnmarshallerTest.java` | 75 | 75 | +0 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-xml/src/test/java/org/xacml4j/v30/xml/Xacml20ResponseContextMarshallerTest.java` | 10 | 10 | +0 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-xml/src/test/java/org/xacml4j/v30/xml/Xacml20ResponseContextUnmarshallerTest.java` | 8 | 9 | -1 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-xml/src/test/java/org/xacml4j/v30/xml/Xacml30ResponseContextUnmarshallerTest.java` | 10 | 11 | -1 | Entity and Record policy model integration, PDP evaluation, and marshalling |
| M | `xacml-xml/src/test/java/org/xacml4j/v30/xml/XacmlPolicyUnmarshallerTest.java` | 42 | 43 | -1 | Entity and Record policy model integration, PDP evaluation, and marshalling |

### Slice 6.6 (15 files)
| Status | File Path | Added | Deleted | Net | Purpose / Action |
|---|---|---|---|---|---|
| M | `xacml-core/src/main/java/org/xacml4j/util/DateTimeDurationUtils.java` | 41 | 64 | -23 | Date, time, and duration types (reconciled with bugs 4.10 & 4.11) |
| A | `xacml-core/src/main/java/org/xacml4j/v30/types/Duration.java` | 273 | 0 | +273 | Date, time, and duration types (reconciled with bugs 4.10 & 4.11) |
| M | `xacml-core/src/main/java/org/xacml4j/v30/types/ISO8601Date.java` | 108 | 83 | +25 | Date, time, and duration types (reconciled with bugs 4.10 & 4.11) |
| M | `xacml-core/src/main/java/org/xacml4j/v30/types/ISO8601DateTime.java` | 164 | 116 | +48 | Date, time, and duration types (reconciled with bugs 4.10 & 4.11) |
| M | `xacml-core/src/main/java/org/xacml4j/v30/types/ISO8601DayTimeDuration.java` | 58 | 52 | +6 | Date, time, and duration types (reconciled with bugs 4.10 & 4.11) |
| D | `xacml-core/src/main/java/org/xacml4j/v30/types/ISO8601Duration.java` | 0 | 129 | -129 | Date, time, and duration types (reconciled with bugs 4.10 & 4.11) |
| M | `xacml-core/src/main/java/org/xacml4j/v30/types/ISO8601Time.java` | 99 | 85 | +14 | Date, time, and duration types (reconciled with bugs 4.10 & 4.11) |
| M | `xacml-core/src/main/java/org/xacml4j/v30/types/ISO8601YearMonthDuration.java` | 64 | 41 | +23 | Date, time, and duration types (reconciled with bugs 4.10 & 4.11) |
| M | `xacml-core/src/test/java/org/xacml4j/util/ISO8601DurationTest.java` | 6 | 9 | -3 | Date, time, and duration types (reconciled with bugs 4.10 & 4.11) |
| M | `xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/DateTimeValueArithmeticFunctionTest.java` | 6 | 6 | +0 | Date, time, and duration types (reconciled with bugs 4.10 & 4.11) |
| M | `xacml-core/src/test/java/org/xacml4j/v30/types/ISO8601DateTest.java` | 10 | 10 | +0 | Date, time, and duration types (reconciled with bugs 4.10 & 4.11) |
| M | `xacml-core/src/test/java/org/xacml4j/v30/types/ISO8601DateTimeTest.java` | 10 | 11 | -1 | Date, time, and duration types (reconciled with bugs 4.10 & 4.11) |
| M | `xacml-core/src/test/java/org/xacml4j/v30/types/ISO8601DayTimeDurationTest.java` | 17 | 17 | +0 | Date, time, and duration types (reconciled with bugs 4.10 & 4.11) |
| M | `xacml-core/src/test/java/org/xacml4j/v30/types/ISO8601TimeTest.java` | 20 | 20 | +0 | Date, time, and duration types (reconciled with bugs 4.10 & 4.11) |
| M | `xacml-core/src/test/java/org/xacml4j/v30/types/ISO8601YearMonthDurationTest.java` | 6 | 6 | +0 | Date, time, and duration types (reconciled with bugs 4.10 & 4.11) |

### Slice 7.1 (8 files)
| Status | File Path | Added | Deleted | Net | Purpose / Action |
|---|---|---|---|---|---|
| A | `xacml-core/src/main/java/org/xacml4j/v30/model/AccessModel.java` | 116 | 0 | +116 | AccessModel and schema framework |
| A | `xacml-core/src/main/java/org/xacml4j/v30/model/AttributeSchema.java` | 136 | 0 | +136 | AccessModel and schema framework |
| A | `xacml-core/src/main/java/org/xacml4j/v30/model/AttributesSchema.java` | 133 | 0 | +133 | AccessModel and schema framework |
| A | `xacml-core/src/main/java/org/xacml4j/v30/model/Categories.java` | 66 | 0 | +66 | AccessModel and schema framework |
| A | `xacml-core/src/main/java/org/xacml4j/v30/model/ContentSchema.java` | 57 | 0 | +57 | AccessModel and schema framework |
| A | `xacml-core/src/main/java/org/xacml4j/v30/model/EntitySchema.java` | 108 | 0 | +108 | AccessModel and schema framework |
| A | `xacml-core/src/main/java/org/xacml4j/v30/model/EntityTypeQualifier.java` | 63 | 0 | +63 | AccessModel and schema framework |
| A | `xacml-core/src/main/java/org/xacml4j/v30/model/ValueDomain.java` | 155 | 0 | +155 | AccessModel and schema framework |

### Slice 7.2 (7 files)
| Status | File Path | Added | Deleted | Net | Purpose / Action |
|---|---|---|---|---|---|
| A | `xacml-core/src/main/java/org/xacml4j/v30/schemas/Action.java` | 94 | 0 | +94 | Standard category and attribute schemas |
| A | `xacml-core/src/main/java/org/xacml4j/v30/schemas/Attributes.java` | 202 | 0 | +202 | Standard category and attribute schemas |
| A | `xacml-core/src/main/java/org/xacml4j/v30/schemas/Categories.java` | 101 | 0 | +101 | Standard category and attribute schemas |
| A | `xacml-core/src/main/java/org/xacml4j/v30/schemas/Delegate.java` | 49 | 0 | +49 | Standard category and attribute schemas |
| A | `xacml-core/src/main/java/org/xacml4j/v30/schemas/Enviroment.java` | 96 | 0 | +96 | Standard category and attribute schemas |
| A | `xacml-core/src/main/java/org/xacml4j/v30/schemas/Resource.java` | 138 | 0 | +138 | Standard category and attribute schemas |
| A | `xacml-core/src/main/java/org/xacml4j/v30/schemas/Subject.java` | 62 | 0 | +62 | Standard category and attribute schemas |

### Slice 7.3 (7 files)
| Status | File Path | Added | Deleted | Net | Purpose / Action |
|---|---|---|---|---|---|
| D | `xacml-core/src/main/java/org/xacml4j/v30/AttributeContainer.java` | 0 | 194 | -194 | Legacy attribute and category type removal (replaced by Record/Entity/Model) |
| D | `xacml-core/src/main/java/org/xacml4j/v30/AttributeIndex.java` | 0 | 273 | -273 | Legacy attribute and category type removal (replaced by Record/Entity/Model) |
| D | `xacml-core/src/main/java/org/xacml4j/v30/Category.java` | 0 | 264 | -264 | Legacy attribute and category type removal (replaced by Record/Entity/Model) |
| D | `xacml-core/src/main/java/org/xacml4j/v30/CategoryReference.java` | 0 | 101 | -101 | Legacy attribute and category type removal (replaced by Record/Entity/Model) |
| D | `xacml-core/src/main/java/org/xacml4j/v30/WellKnownAttributeIds.java` | 0 | 145 | -145 | Legacy attribute and category type removal (replaced by Record/Entity/Model) |
| D | `xacml-core/src/test/java/org/xacml4j/v30/AttributeIndexTest.java` | 0 | 29 | -29 | Legacy attribute and category type removal (replaced by Record/Entity/Model) |
| M | `xacml-core/src/test/java/org/xacml4j/v30/WellKnownAttributeIdsTest.java` | 4 | 3 | +1 | Legacy attribute and category type removal (replaced by Record/Entity/Model) |

### Slice DROP (3 files)
| Status | File Path | Added | Deleted | Net | Purpose / Action |
|---|---|---|---|---|---|
| M | `xacml-core/pom.xml` | 18 | 18 | +0 | POM compiler/preview/unused dependency churn (integration release 21 & junit 5 config wins) |
| D | `xacml-json/src/test/resources/policy.json` | 0 | 34 | -34 | Already deleted in task 1.4 |
| M | `xacml-pdp/pom.xml` | 12 | 12 | +0 | POM compiler/preview/unused dependency churn (integration release 21 & junit 5 config wins) |

