# Summary: Independent Review of Task 5.7 (`ValueType.apply` One-Element `Object[]`)

- **Review ID**: `rv201-value-type-single-array-argon`
- **Change / Task**: `openspec/changes/modernize-core-for-jdk28-valhalla`, Task `5.7` (`policy4j-the.5.7`)
- **Review Bead**: `policy4j-the.5.7.1` (actor `ntm-argon-g1`, Agy Gemini 4 Argon High, plain review packet)
- **Author Under Review**: Codex GPT-6 Luna high (`cod_4`, base commit `759d90a6c75510d55127d8e22870f9edd985c991`)
- **Verdict**: **APPROVE** (for the two-file source/test slice; source approval does not close parent task `5.7`)

## Key Conclusions

1. **Hash & Scope Integrity (`SUPPORTED`)**:
   - Frozen candidate (`.ntm/packets/remaining-g1-20261029/value-type-candidate/`) and live `cod_4` sources match `.ntm/packets/remaining-g1-20261029/value-type-manifest.json` and `cod_4`'s `candidate-ready.json` byte-for-byte with zero drift:
     - `xacml-core/src/main/java/org/xacml4j/v30/types/ValueType.java`: `b142112f3fd93c8b484b34f88b598fca2ac411d971d33afa2efb432dd902e57f` (baseline `759d90a6`: `6dfecb0e5e8ccf8861918fad0de665b88ae8308b110196501d5c1435fe8a18e3`).
     - `xacml-core/src/test/java/org/xacml4j/v30/types/ValueTypeApplyTest.java`: `e56e11a6d908b5bb9de6083bfc01780e134cdc41e8bb7c8aa4d33965ebce1c3f` (new file vs `759d90a6`).
2. **Exact Production Diff & Semantics (`SUPPORTED`)**:
   - The sole change in `xacml-core/src/main/java/org/xacml4j/v30/types/ValueType.java` is line `163` inside `if(array.length == 1)` (`ValueType.java:162-164`): `return ofAny(o);` -> `return ofAny(array[0]);`.
   - Null container (`ValueType.java:151-155`), zero-length array (`ValueType.java:157-161`), multi-argument array (`ValueType.java:165-167`), and scalar input (`ValueType.java:169`) branches are unchanged.
   - Generic signatures and erasure from Design D13 / Task 5.6 (`Function<Object, Value<?>>`, `<T extends Value<?>> T ofAny(Object)`, `<T extends Value<?>> T ofAny(Object, Object...)`, `Value<?> apply(Object)`) remain unchanged and binary/source compatible.
3. **Test & Supplied Evidence Adequacy (`SUPPORTED`)**:
   - `ValueTypeApplyTest.java:33-77` covers 8 behavioral cases across `XacmlTypes.STRING`, `XacmlTypes.INTEGER`, `XacmlTypes.BOOLEAN`, and `XacmlTypes.XPATH`.
   - Supplied logs in `cod_4`'s `openspec/changes/modernize-core-for-jdk28-valhalla/evidence/tests/201-value-type-single-array/` verify the red-to-green transition (`focused-red-mvn310-clean.log`: 8 run, 4 `SyntaxException` errors on the 4 unary-array tests; `focused-green-mvn310-clean.log`: 8 run, 0 failures, 0 errors) and the `xacml-core` module suite (`core-suite-green-mvn310-clean.log`: 7,365 run, 0 failures, 0 errors, 2 skipped).
4. **Non-Blocking Observation**:
   - `cod_4`'s `openspec/changes/modernize-core-for-jdk28-valhalla/evidence/tests/201-value-type-single-array/summary.md:29` cites `ValueType.java:165` for the one-line production change; the actual modified line in `ValueType.java` is line `163` (`ValueType.java:162-164`).
5. **Unrun Gates & JMH Applicability**:
   - Reviewer executed no builds or tests.
   - Full reactor and conformance suites remain **UNRUN** (conductor-owned gate).
   - JMH is **NOT APPLICABLE (UNRUN)**: this change fixes a previously throwing input (`SyntaxException`) rather than optimizing an existing working path; comparing an exception throw against a valid conversion is not a valid performance benchmark.
