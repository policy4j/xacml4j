# Independent Adversarial Code Review: Task 5.7 (`ValueType.apply` One-Element `Object[]`)

- **Review ID**: `rv201-value-type-single-array-argon`
- **Reviewer**: Agy Gemini 4 Argon High (`agyx_1`, actor `ntm-argon-g1`), plain review packet (`.ntm/packets/remaining-g1-20261029/argon-review-value-type.md`)
- **Beads Issue**: `policy4j-the.5.7.1` (parent task: `policy4j-the.5.7`; OpenSpec change `openspec/changes/modernize-core-for-jdk28-valhalla`, task `5.7`)
- **Author Under Review**: Codex GPT-6 Luna high (`cod_4`, base commit `759d90a6c75510d55127d8e22870f9edd985c991`)
- **Baseline Worktree (`agyx_1`) Commit**: `759d90a6c75510d55127d8e22870f9edd985c991`
- **Verdict**: **APPROVE** (source and focused/module test slice only; parent task `5.7` remains open pending conductor-owned full reactor and conformance gates)

---

## 1. Source & Evidence SHA-256 Verification (`SUPPORTED`)

All candidate, live `cod_4`, baseline (`759d90a6`), and cited source/evidence files were hashed directly from disk:

| File Path | Baseline (`759d90a6`) SHA-256 | Frozen Candidate (`value-type-candidate/`) SHA-256 | Live `cod_4` Worktree SHA-256 | Drift Status |
| --- | --- | --- | --- | --- |
| `.ntm/packets/remaining-g1-20261029/value-type-manifest.json` | N/A | `2be1109002acd89b69a402e4963eab75d3fa3ad64f6768c49bad0aaaa53615d1` | N/A | Reference manifest |
| `xacml-core/src/main/java/org/xacml4j/v30/types/ValueType.java` | `6dfecb0e5e8ccf8861918fad0de665b88ae8308b110196501d5c1435fe8a18e3` | `b142112f3fd93c8b484b34f88b598fca2ac411d971d33afa2efb432dd902e57f` | `b142112f3fd93c8b484b34f88b598fca2ac411d971d33afa2efb432dd902e57f` | Exact match with manifest (no drift) |
| `xacml-core/src/test/java/org/xacml4j/v30/types/ValueTypeApplyTest.java` |*(absent in `759d90a6`)* | `e56e11a6d908b5bb9de6083bfc01780e134cdc41e8bb7c8aa4d33965ebce1c3f` | `e56e11a6d908b5bb9de6083bfc01780e134cdc41e8bb7c8aa4d33965ebce1c3f` | Exact match with manifest (no drift) |
| `xacml-core/src/main/java/org/xacml4j/v30/types/XacmlTypes.java` | `6d0a9c3432ae00e9f0522f8471858980acad4ab51ea6d00cf89559f68ff69272` |*(unchanged)* | `6d0a9c3432ae00e9f0522f8471858980acad4ab51ea6d00cf89559f68ff69272` | Identical to baseline |
| `xacml-core/src/main/java/org/xacml4j/v30/types/StringVal.java` | `cf647acd85d425954929cc6cb3326bffbb821406f194d092ddc70d469613168f` |*(unchanged)* | `cf647acd85d425954929cc6cb3326bffbb821406f194d092ddc70d469613168f` | Identical to baseline |
| `xacml-core/src/main/java/org/xacml4j/v30/types/IntegerVal.java` | `34365fc2900edb09681798762a9ab79a628e05f9a1b4919f1c4a77d7d28a6c37` |*(unchanged)* | `34365fc2900edb09681798762a9ab79a628e05f9a1b4919f1c4a77d7d28a6c37` | Identical to baseline |
| `xacml-core/src/main/java/org/xacml4j/v30/types/BooleanVal.java` | `2be686e37d1650e9e3c24d3cd0d672d26f17a699f73b91b303f35e302c98dc10` |*(unchanged)* | `2be686e37d1650e9e3c24d3cd0d672d26f17a699f73b91b303f35e302c98dc10` | Identical to baseline |
| `xacml-core/src/main/java/org/xacml4j/v30/types/Path.java` | `6d715a3eb67915c21dc65c3825f96ffadc5019a6d23e659ee198872b5f59f486` |*(unchanged)* | `6d715a3eb67915c21dc65c3825f96ffadc5019a6d23e659ee198872b5f59f486` | Identical to baseline |
| `xacml-core/src/main/java/org/xacml4j/v30/types/DynamicType.java` | `7cbd615c9d1035f0d7967ad659c2da9f0e44e14f9195296523bfd0e02781c094` |*(unchanged)* | `7cbd615c9d1035f0d7967ad659c2da9f0e44e14f9195296523bfd0e02781c094` | Identical to baseline |
| `openspec/changes/modernize-core-for-jdk28-valhalla/design.md` | `2aa61bca46d7d562dd7719875489abeea404fad4dacd0ae8ad45207b27349d22` |*(unchanged)* | `2aa61bca46d7d562dd7719875489abeea404fad4dacd0ae8ad45207b27349d22` | Identical to baseline |
| `openspec/changes/modernize-core-for-jdk28-valhalla/tasks.md` | `db1f8eba635b10940e84e7828630ca2bdf289e67bf6308828e93fe79f1a0c7a2` |*(unchanged)* | `db1f8eba635b10940e84e7828630ca2bdf289e67bf6308828e93fe79f1a0c7a2` | Identical to baseline |

Supplied evidence files in `cod_4` (`openspec/changes/modernize-core-for-jdk28-valhalla/evidence/tests/201-value-type-single-array/`):
- `focused-red-mvn310-clean.log`: `a15ffed385a5eea0c756eae5b5a50b320e0c7b7a98bf9f1f9c47448949549f6c`
- `focused-green-mvn310-clean.log`: `b8b940f0ac18cd2f9d6b0a9bee5e105bf9a0488f97c8c37a711718bc4e12e381`
- `core-suite-green-mvn310-clean.log`: `7ad97d032152a8653d5469c83dcd3834001e72e4986529f74f20803dba6e007d`
- `focused-red.log`: `d6011f1c1243b0fd36d73138dd831a0dad52bac535b4b09bb2e4cb7c85f336ee`
- `summary.md`: `b64e51c9f9391206e496a77200f321c44ad2a0fdd9bfbcc0613564a31ab86e61`
- `command.txt`: `eaf275fbdc9c954a38831084f71410664d0ddc2a0ccea156823a074362b36434`
- `candidate-ready.json`: `72347097487bf9a6e2658a96eab1c83538d807ae7bb6fb120c459be801f91b33`

---

## 2. Technical Evaluation Against Review Criteria (`SUPPORTED`)

### 2.1 Production Diff Minimality (`ValueType.java:149-170`)
In baseline `759d90a6`, `ValueType.apply(Object o)` (`xacml-core/src/main/java/org/xacml4j/v30/types/ValueType.java:149-170`) read:
```java
	@Override
	default Value<?> apply(Object o){
		if(o == null){
			throw SyntaxException
					.invalidAttributeValue(
							null, this);
		}
		if(o instanceof Object[] array){
			if(array.length == 0){
				throw SyntaxException
						.invalidAttributeValue(
								Arrays.toString(array), this);
			}
			if(array.length == 1){
				return ofAny(o);
			}
			return ofAny(array[0],
					Arrays.copyOfRange(array,
							1, array.length));
		}
		return ofAny(o);
	}
```
The candidate changes **only** `ValueType.java:163` from `return ofAny(o);` to `return ofAny(array[0]);` inside the `if(array.length == 1)` branch (`ValueType.java:162-164`). Indentation (tabs), license header (`ValueType.java:3-23`), imports (`ValueType.java:25-33`), and all other methods in `ValueType.java` are untouched.

### 2.2 Return & Exception Behavior Across Input Shapes and Type Factories
1. **Length-one `Object[]` (`array.length == 1`, `ValueType.java:162-164`)**:
   - Previously, `ofAny(o)` passed the `Object[]` container itself to `ofAny(Object v)` (`ValueType.java:82-84`), which calls `ofAny(v, (Object[]) null)` (`ValueType.java:83`). None of the standard type factories in `XacmlTypes` (`xacml-core/src/main/java/org/xacml4j/v30/types/XacmlTypes.java:45-163`) accept an `Object[]` as the first parameter `v`, so every valid one-element `Object[]` failed with `SyntaxException.invalidAttributeValue`.
   - With `return ofAny(array[0]);` (`ValueType.java:163`), `apply(new Object[]{x})` now delegates `x` to `ofAny(x)` -> `ofAny(x, (Object[]) null)` (`ValueType.java:82-84`), matching both the task `5.7` specification (`openspec/changes/modernize-core-for-jdk28-valhalla/tasks.md:61`) and the scalar branch `return ofAny(o);` (`ValueType.java:169`).
   - Verified against the concrete factories exercised by `ValueTypeApplyTest`:
     - `XacmlTypes.STRING` (`XacmlTypes.java:112-116`) -> `StringVal.ofAny(Object v, Object... params)` (`xacml-core/src/main/java/org/xacml4j/v30/types/StringVal.java:56-66`): checks `Objects.requireNonNull(v)` (`StringVal.java:57`), converts `String` via `StringVal.of((String) v)` (`StringVal.java:58-60`), returns an existing `StringVal` instance unchanged (`StringVal.java:61-63`), and throws `SyntaxException.invalidAttributeValue(v, XacmlTypes.STRING)` (`StringVal.java:64-65`) for unsupported element types.
     - `XacmlTypes.INTEGER` (`XacmlTypes.java:93-99`) -> `IntegerVal.ofAny(Object v, Object... params)` (`xacml-core/src/main/java/org/xacml4j/v30/types/IntegerVal.java:57-72`): converts `String`, `StringVal`, `DoubleVal`, and `Number` (`IntegerVal.java:58-69`, using `SmallValues.CACHE` for `[-128, 255]` at `IntegerVal.java:81-85`), and throws `SyntaxException.invalidAttributeValue(v, XacmlTypes.INTEGER)` (`IntegerVal.java:70-71`) otherwise (including when `array[0]` is `null`).
     - `XacmlTypes.BOOLEAN` (`XacmlTypes.java:57-62`) -> `BooleanVal.ofAny(Object v, Object... params)` (`xacml-core/src/main/java/org/xacml4j/v30/types/BooleanVal.java:47-59`): converts `String` and `StringVal` via `fromLexical` (`BooleanVal.java:48-53, 61-76`), converts `Boolean` to `BooleanVal.TRUE`/`FALSE` (`BooleanVal.java:54-56`), and throws `SyntaxException.invalidAttributeValue(v, XacmlTypes.BOOLEAN)` (`BooleanVal.java:57-58`) otherwise.
     - `XacmlTypes.XPATH` (`XacmlTypes.java:136-142`) -> `Path.xpath(Object xpath, Object... params)` (`xacml-core/src/main/java/org/xacml4j/v30/types/Path.java:51-68`): when called via `ofAny(array[0])` (`params == null`), `Path.getCategory(params)` (`Path.java:89-97`) returns `Optional.empty()` (`Path.java:90-91`) and defaults to `CategoryId.RESOURCE`, identical to `XacmlTypes.XPATH.ofAny(array[0])`.
     - `DynamicType.INSTANCE` (`xacml-core/src/main/java/org/xacml4j/v30/types/DynamicType.java:51-79`): `ofAny(Object v, Object... params)` unconditionally throws `UnsupportedOperationException` (`DynamicType.java:77-79`), so `DynamicType.INSTANCE.apply(new Object[]{"a"})` continues to throw `UnsupportedOperationException`.
2. **Scalar input (`!(o instanceof Object[])`, `ValueType.java:169`)**:
   - Unchanged; delegates directly to `ofAny(o)`.
3. **Null container (`o == null`, `ValueType.java:151-155`) and zero-length array (`array.length == 0`, `ValueType.java:157-161`)**:
   - Unchanged; both throw `SyntaxException.invalidAttributeValue(..., this)` before reaching `ValueType.java:162`.
4. **Multi-argument array (`array.length > 1`, `ValueType.java:165-167`)**:
   - Unchanged; calls `ofAny(array[0], Arrays.copyOfRange(array, 1, array.length))`, forwarding additional factory parameters such as `CategoryId` in `Path.xpath` (`Path.java:51-68, 89-97`).

### 2.3 Generic Signatures, Erasure, and Public Compatibility (Design D13)
- Per `openspec/changes/modernize-core-for-jdk28-valhalla/design.md:242-270` (D13) and task `5.6` (`tasks.md:60`), `ValueType` extends `Function<Object, Value<?>>` (`ValueType.java:40`), declares `default <T extends Value<?>> T ofAny(Object v)` (`ValueType.java:82`) and `<T extends Value<?>> T ofAny(Object v, Object ...params)` (`ValueType.java:94`), and implements `@Override default Value<?> apply(Object o)` (`ValueType.java:149-150`).
- At `ValueType.java:163`, `return ofAny(array[0]);` resolves to `this.<Value<?>>ofAny(array[0])`, inferred from the return type `Value<?>` of `apply(Object)`. No generic signature, method descriptor, type bound, or bridge method changes. Public source and binary compatibility are preserved.

### 2.4 Test Coverage Evaluation (`ValueTypeApplyTest.java:33-77`)
`xacml-core/src/test/java/org/xacml4j/v30/types/ValueTypeApplyTest.java` defines 8 focused JUnit 5 tests asserting observable outcomes:
1. `oneElementStringArrayConvertsItsContainedValue` (`ValueTypeApplyTest.java:35-38`): `XacmlTypes.STRING.apply(new Object[]{"value"})` equals `StringVal.of("value")`.
2. `oneElementIntegerArrayConvertsItsContainedValue` (`ValueTypeApplyTest.java:40-43`): `XacmlTypes.INTEGER.apply(new Object[]{42L})` equals `IntegerVal.of(42L)`.
3. `oneElementBooleanArrayConvertsItsContainedValue` (`ValueTypeApplyTest.java:45-48`): `XacmlTypes.BOOLEAN.apply(new Object[]{true})` equals `BooleanVal.of(true)`.
4. `oneElementArrayConvertsAContainedValueInstance` (`ValueTypeApplyTest.java:50-55`): `XacmlTypes.STRING.apply(new Object[]{value})` returns the same `StringVal` instance (`assertSame`).
5. `scalarInputKeepsTheFactoryConversion` (`ValueTypeApplyTest.java:57-60`): `XacmlTypes.STRING.apply("value")` equals `XacmlTypes.STRING.ofAny("value")`.
6. `emptyArrayRemainsInvalid` (`ValueTypeApplyTest.java:62-65`): `XacmlTypes.STRING.apply(new Object[0])` throws `SyntaxException`.
7. `nullContainerRemainsInvalid` (`ValueTypeApplyTest.java:67-70`): `XacmlTypes.STRING.apply(null)` throws `SyntaxException`.
8. `multiArgumentArrayStillForwardsFactoryParameters` (`ValueTypeApplyTest.java:72-76`): `XacmlTypes.XPATH.apply(new Object[]{"/test", CategoryId.SUBJECT_RECIPIENT})` equals `XacmlTypes.XPATH.ofAny("/test", CategoryId.SUBJECT_RECIPIENT)`.

No blocking defect or missing regression test was found.

### 2.5 Non-Blocking Observation on Author Evidence Prose
- In `cod_4`'s `openspec/changes/modernize-core-for-jdk28-valhalla/evidence/tests/201-value-type-single-array/summary.md:29`, the prose states:
  `The production change is limited to xacml-core/src/main/java/org/xacml4j/v30/types/ValueType.java:165`
- In the actual file (`ValueType.java:162-167`), line `163` is the modified `return ofAny(array[0]);` inside `if(array.length == 1)`, whereas line `165` is the unchanged `return ofAny(array[0], Arrays.copyOfRange(array, 1, array.length));` branch. This is a minor line-number typo in `summary.md` only and does not affect code or test correctness.

---

## 3. Supplied Test Evidence & Explicit Unrun Gates

### 3.1 Supplied Author/Conductor Test Logs (`cod_4`)
Inspected directly in `cod_4/openspec/changes/modernize-core-for-jdk28-valhalla/evidence/tests/201-value-type-single-array/` (not rerun by this reviewer):
- **Focused Red (`focused-red-mvn310-clean.log:61-85, 104`)**:
  - Executed via Maven 3.10.0 / JDK 25 (`-o -Dlicense.skipUpdateLicense=true -pl xacml-core -Dtest=ValueTypeApplyTest -Dsurefire.failIfNoSpecifiedTests=false clean test`) before the `ValueType.java:163` fix.
  - Result: `Tests run: 8, Failures: 0, Errors: 4, Skipped: 0`, `Exit: 1`. All 4 one-element `Object[]` tests threw `org.xacml4j.v30.SyntaxException: Invalid XACML type="..." attribute value="[Ljava.lang.Object;@..."`; the 4 control tests passed.
- **Focused Green (`focused-green-mvn310-clean.log:61-67, 75`)**:
  - Same command after the `ValueType.java:163` fix.
  - Result: `Tests run: 8, Failures: 0, Errors: 0, Skipped: 0`, `Exit: 0`.
- **`xacml-core` Module Suite (`core-suite-green-mvn310-clean.log:124-125, 499, 508`)**:
  - Executed via Maven 3.10.0 / JDK 25 (`-o -Dlicense.skipUpdateLicense=true -pl xacml-core clean test`).
  - Result: `Tests run: 7365, Failures: 0, Errors: 0, Skipped: 2`, `Exit: 0` (including `ValueTypeApplyTest`: 8 run, 0 failures, 0 errors).

### 3.2 Explicit Unrun Gates
- **Reviewer Test/Build Execution**: **UNRUN** (reviewer operated in read-only mode).
- **Full Reactor & Conformance Gate (`xacml-pdp`, `xacml-json`, `xacml-xml`, `xacml-test`, `xacml-conformance-tests`)**: **UNRUN** (owned by the conductor before task `5.7` can be closed).
- **Parent Task `5.7` (`policy4j-the.5.7`) Completion**: **OPEN** (this source/test approval satisfies the independent review slice `policy4j-the.5.7.1` only and does not close `policy4j-the.5.7`).

### 3.3 JMH Applicability Decision
- **NOT APPLICABLE (`UNRUN` by design)**: `ValueType.apply` previously threw `SyntaxException.invalidAttributeValue` on every length-one `Object[]`. Changing `ofAny(o)` to `ofAny(array[0])` at `ValueType.java:163` restores functional correctness for a formerly failing input and makes no performance or allocation claim. Benchmarking a thrown exception against a valid value conversion would compare two different functional outcomes rather than measuring a speedup.
