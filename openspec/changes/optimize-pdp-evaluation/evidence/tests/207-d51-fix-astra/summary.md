# D51 correction — rv210-ba-argon P2-1

**Implemented and ready for independent review.** Focused red reproduced the default-cardinality defect; focused green passes. The requested clean core/PDP gate has exactly the expected `ISO8601DateTest` error and no other failures or errors. **JMH UNRUN.**

- Change `optimize-pdp-evaluation`, task `42.18`, cycle BA; author `cod_3`, Codex GPT-6 Astra, max effort.
- Base `759d90a6c75510d55127d8e22870f9edd985c991`. This checkout also contains the earlier, disjoint D52 correction.
- All seven BA overlay hashes matched both the protected `.ntm/worktrees/xacml4j--batch2/cod_3` candidate and the hashes in `evidence/reviews/code/rv210-ba-argon/report.md` before edits. See [pre-correction-baseline.json](pre-correction-baseline.json).
- Only `BagOfValues.java`, `BagStorages.java`, and `BagStorageCompatibilityTest.java` changed during this task, plus this evidence directory. [correction.patch](correction.patch) is the delta from the reviewed BA candidate. [candidate-ready.json](candidate-ready.json) records all seven final source hashes.

## Verified defect and correction

The reviewed candidate's `BagOfValues.size()` accepted the JVM default `cardinality == 0` as an empty bag before reading storage. Its constructor and `readObject` assigned storage before cardinality. The prior storage-based implementation read a constant for `Single`, final `Grouped.size` for `Values`/`Integers`/`Doubles`, or final boolean counts for `Booleans`. A default cache read could therefore introduce a silent zero for a non-empty engine bag. This source analysis supports Argon's finding; the deterministic regression below demonstrates the wrong result without attempting to provoke a race.

The corrected encoding is **0 = delegate to storage; −1 = engine Empty; positive = engine cardinality**:

- `xacml-core/src/main/java/org/xacml4j/v30/BagStorages.java:68`: the helper recognizes only the private final engine storage classes. Custom storage receives zero without calling its `size()`.
- `xacml-core/src/main/java/org/xacml4j/v30/BagOfValues.java:85` and `:338`: construction and deserialization write cardinality before publishing storage.
- `BagOfValues.java:180`: zero calls `storage.size()`; other encodings answer directly. `isEmpty()` at `:191` uses that result. Public signatures, `serialVersionUID`, and the explicit persistent-field list are unchanged.

If storage itself is still unseen, the fallback preserves the prior possible null failure instead of manufacturing an empty result. This does not claim general safe publication for the object.

## Regression and executed verification

`xacml-core/src/test/java/org/xacml4j/v30/BagStorageCompatibilityTest.java:142` adds 56 parameterized cases: five existing string/integer/double/boolean seeds at sizes 0, 1, 2, 8, 9, plus custom storage at sizes 0, 1, 3; each is tested after construction and after deserialization. These cover Empty, Single, Values, Integers, Doubles, and Booleans, including duplicates and both boolean insertion orders.

Each case checks `size()` and `isEmpty()`, resets cardinality to zero via reflection, then checks those outcomes again. The initial nonzero engine encoding is also pinned. The existing throwing custom-storage control at `:188` checks that size is still requested only on use, including after the reset. The test source is identical between red and green. No allocation or timing assertions were added.

All runs used the shared serialized runner, `--maven /opt/homebrew/bin/mvn` (Maven 3.10.0), offline mode, and `-Dlicense.skipUpdateLicense=true`. The runner selected JDK 25: Oracle `25.0.4`, VM `25.0.4+7-LTS-jvmci-b01`. Commands are in [command.txt](command.txt); logs have paths normalized by the runner.

| Run | Module | Tests | Failures | Errors | Skipped | Outcome |
| --- | --- | ---: | ---: | ---: | ---: | --- |
| [Focused red, clean](01-focused-red.log) | core | 154 | 57 | 0 | 0 | Expected failure, exit 1 |
| [Focused green](02-focused-green.log) | core | 154 | 0 | 0 | 0 | PASS, exit 0 |
| [Clean affected gate](03-core-pdp-clean.log) | core | 7,504 | 0 | 1 | 2 | Known error only |
| Same gate | XML | 254 | 0 | 0 | 0 | PASS |
| Same gate | JSON | 24 | 0 | 0 | 1 | PASS |
| Same gate | PDP | 810 | 0 | 0 | 3 | PASS |
| **Affected total** | | **8,592** | **0** | **1** | **6** | **Expected baseline exception only** |

The parent and XML JAXB modules also completed; neither ran tests. Counts above are the module Results totals in the logs, cross-checked against Surefire's error inventory.

The red run had 44 non-empty cases returning zero, one suppressed custom-storage exception, and 12 encoding failures. [01-focused-red-receipt.json](01-focused-red-receipt.json) records the exact failure messages and all seven source hashes at red: both production files still matched the reviewed implementation.

The sole affected-gate error is `org.xacml4j.v30.types.ISO8601DateTest.testCreateFromCalendar:59`: `java.time.DateTimeException: Invalid date 'SEPTEMBER 31'`. This is the known protected baseline error explicitly allowed by this dispatch. No other failing/error test appeared. The command used the requested `-Dmaven.test.failure.ignore=true`, so Maven's exit 0 / BUILD SUCCESS **does not mean all tests passed**.

## Performance and remaining gates

**No performance acceptance.** For an initialized engine bag, source inspection shows one volatile cardinality read plus the zero/sign checks, with no `BagStorage.size()` interface dispatch. The prior implementation used a volatile storage read and an interface call; whether the JIT can remove that dispatch matters. The custom/default fallback now reads cardinality and then storage, adding a read and branch to that route. Construction also performs the added volatile cardinality write. These costs may consume D51's expected gain; only the conductor's controlled pair can decide whether the field pays for itself.

Controlled JMH pairs for D49/D50/D51, object-layout/allocation checks, the combined reactor/conformance gate, independent review of this correction, and concurrency stress testing are **UNRUN here**. The reflected-state test proves the fallback outcome, not the frequency of unsafe-publication observations. No benchmark or allocation measurement was run.

## Preservation

All 7,247 pre-existing protected tracked/untracked files have the same aggregate SHA-256 before and after this task: `0405dfa7efc63a9923c5ed65a815cdd4f1bd7c5d52b57513d0bb1a9cbed942ab`. The digest excludes the three owned files and this evidence directory. All nine D52 source hashes still match its retained candidate manifest; its evidence is unchanged. The old BA candidate's seven hashes also remain unchanged. D49/D50 files, task/design/Beads files, and the finished gate audit were preserved. No stage, commit, branch change, or task update was made. `git diff --check` passed for the owned paths.

LANE_COMPLETE cod_3 D51

## D52 test-only follow-up — task 42.19

**D52-OPUS-1 and D52-OPUS-2 are covered.** The requested mutants failed, and the restored production candidate passed all **160 focused tests**. Bead `policy4j-cwtu.42.19.3`; lane/model/effort and base are unchanged. The D51 results above remain the record of that earlier run; the following results concern only this test follow-up.

Source inspection confirmed both gaps before editing. The previous mutable-parameter rows supplied a custom bag element type; that additional operand guard would reject the plan even if the parameter guard regressed. The fallback loops asserted syntax errors but did not count data-operand evaluations. `rv208-d52-opus` is supplied review evidence; the mutation and green results below are fresh execution.

- `xacml-core/src/test/java/org/xacml4j/v30/policy/function/HigherOrderMetadataCompatibilityTest.java:93` adds canonical-operand variants to the existing metadata-mutation method at `:109`. The caller-owned parameter descriptor equals `XacmlTypes.STRING` until the bag operand changes it, while both operand descriptors are canonical. The matrix retains any-of/all-of, unary and both binary bag positions, runtime validation off/on, an empty bag, exact syntax-error/message, outer function/observer attribution, operand order/count, and no predicate call.
- `xacml-core/src/test/java/org/xacml4j/v30/policy/ApplyArgumentValidationTest.java:311`, `:354`, and `:362` clear prior mock calls and verify exactly one evaluation of each data operand after each Apply. They cover the wrong-bag and wrong-scalar binary fallbacks, both bag positions, the null-bag unary fallback, and runtime validation off/on. Existing any-of-any/map checks remain controls. The `Apply.callSite` reflection is unchanged.

| Fresh run | Tests | Failures | Errors | Skipped | Evidence |
| --- | ---: | ---: | ---: | ---: | --- |
| Frozen parameter check mutant | 24 | 6 | 0 | 0 | [04-d52-param-mutant-red.log](04-d52-param-mutant-red.log) |
| Binary fallback re-evaluation mutant | 4 | 2 | 0 | 0 | [05-d52-pair-mutant-red.log](05-d52-pair-mutant-red.log) |
| Unary fallback re-evaluation mutant | 2 | 1 | 0 | 0 | [06-d52-unary-mutant-red.log](06-d52-unary-mutant-red.log) |
| Restored production, full focused classes, clean | **160** | **0** | **0** | **0** | [07-d52-focused-green.log](07-d52-focused-green.log) |

The parameter mutant temporarily replaced the canonical-type guard with the frozen `DynamicType.isDynamic` check (fully qualified to avoid an import change). Exactly the six canonical-operand, validation-off rows failed because no `FunctionInvocationException` was thrown. All 12 pre-existing custom-operand rows and the six new validation-on rows passed.

The binary and unary mutants were run separately. Each temporarily replaced its evaluated-value fallback with `spec.invokeWithList(context, arguments)`. Status/message checks still passed, then the new invocation checks reported **wanted 1, was 2**; the traces name both `HigherOrderCall.evaluate` and the second evaluation in `FunctionSpecImpl.invokeFast`. Validation-on controls passed. Each mutant was restored before the next probe; no mutant remains.

The final clean green run comprised **68 ApplyArgumentValidationTest cases and 92 HigherOrderMetadataCompatibilityTest cases**. It did not set failure-ignore. All four runs used the same shared serialized runner, JDK 25, `--maven /opt/homebrew/bin/mvn`, offline mode, disabled license updates, and distinct logs. Commands are appended in [command.txt](command.txt). [d52-tests-mutation-proof.json](d52-tests-mutation-proof.json) records failing cases, mutant/test hashes, log hashes, and restored hashes. Both test files were unchanged between the mutant runs and final green.

Final production hashes exactly match the required approved candidate:

- `FunctionSpecBuilder.java`: `bda8302758388ee43f6746c04b3b33dbd80174e11f11a0f7fe852c3921b04a12`.
- `DefaultFunctionInvocation.java`: `cadc93aa1855de78607c7e8351137fe2715276caba33dd1a43c1ea0855a993e2`.

All **7,248 protected files** matched the pre-follow-up inventory, including every production file, the seven BA files, the earlier D52 evidence, and the gate audit. Their aggregate is `d64c875ab22dca4ccaae3c6963fa2d34135cc1b5a7b5b31480b6e59e8ab7ede4`; the inventory excludes only the two owned tests and this evidence directory. Only those two test hashes changed among the nine D52 paths. `git diff --check` passed. [candidate-ready.json](candidate-ready.json) now includes all seven BA hashes and all nine current D52 hashes, with a separate D52 follow-up record.

The full core/PDP suites were **not rerun after these test-only additions**; log 03's known date error remains historical evidence. Combined reactor/conformance and controlled JMH remain **UNRUN here**. No production, task, design, Beads, staging, commit, or branch change is retained.

LANE_COMPLETE cod_3 D52-tests

