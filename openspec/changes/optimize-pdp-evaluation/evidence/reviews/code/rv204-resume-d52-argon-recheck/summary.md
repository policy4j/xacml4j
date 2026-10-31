# Summary — Code Review Recheck `rv204-resume-d52-argon-recheck` (`R-D52-argon`)

- **Change**: `optimize-pdp-evaluation`
- **Task / Beads**: `42.19` (`policy4j-cwtu.42.19.2`)
- **Design**: D52 (Prepared signatures for higher-order call sites)
- **Reviewer**: `agyx_1` (actor `ntm-argon-g1`), Agy Gemini 4 Argon High launched with the plain review packet (no separate `policy4j_reviewer_argon` profile selected)
- **Author Report Inspected**: `REPORT-I-D52-terra.md` (`I-D52-terra`; exact model version not stated in report)
- **Candidate Base**: `cd443805c3044e7412c25aae71dd02127994d418`
- **Current-Main Baseline**: `759d90a6c75510d55127d8e22870f9edd985c991`
- **Source Hash Drift (`cd443805` -> `759d90a6`)**: None (`0` files drifted among the 7 pre-existing files; `HigherOrderCall.java` is new in D52; all 8 candidate files match identically across `d52-manifest.json`, frozen `d52-candidate/`, and live candidate worktree)
- **Verdict**: **REJECT** (`0` P1, `1` P2, `0` P3)
- **Blocking Finding (`P2-1`)**:
  - `FunctionSpecBuilder.canPrepareHigherOrder` (`xacml-core/src/main/java/org/xacml4j/v30/policy/function/FunctionSpecBuilder.java:328-329`) and `HigherOrderCall.prepare` (`xacml-core/src/main/java/org/xacml4j/v30/policy/HigherOrderCall.java:74-85`) only exclude `DynamicType.isDynamic(...)` (`type.getValueType() == DynamicType.INSTANCE`), admitting arbitrary custom/mutable `ValueExpTypeInfo` and `ValueType` implementations supplied through public extension APIs (`FunctionSpecBuilder.param(ValueExpTypeInfo)`, `new BagOfValuesType(ValueType)`, `Expression.getEvaluatesTo()`).
  - Because `DefaultFunctionInvocation.invokeHigherOrderSingle` (`DefaultFunctionInvocation.java:207-209`) and `invokeHigherOrderPair` (`DefaultFunctionInvocation.java:224-227`) only compare `expectedBagType.equals(bag.getEvaluatesTo())` and `expectedScalarType.equals(scalar.getEvaluatesTo())` and never re-run `HigherOrderFunctions.validateArguments`, a mutable/custom parameter or operand type descriptor can change signature validity after `Apply` construction (or satisfy `expectedScalarType.equals(...)` asymmetrically while failing `ref.getParamSpecAt(i).isValidParamType(...)`) and evaluate the predicate where ordinary checked evaluation throws `EvaluationException` (`Status.syntaxError()`).
  - **Minimal Fix**: Require `typed.getParamType() instanceof XacmlTypes` in `FunctionSpecBuilder.canPrepareHigherOrder` (`FunctionSpecBuilder.java:328-329`), and require `type.getValueType() instanceof XacmlTypes` and `type instanceof XacmlTypes valueType` (for the scalar branch) in `HigherOrderCall.prepare` (`HigherOrderCall.java:75-82`).
- **Evidence & Gate Status**:
  - Code-intelligence & source inspection: `SUPPORTED` (`ast-grep` PASS, `ripwire` PASS, `serena` PASS; `tgrep` `UNAVAILABLE` without local index, covered by native file reads)
  - Tests in this review lane: **UNRUN** (read-only review lane). Supplied `REPORT-I-D52-terra.md:19-21` records: focused core test run (`ApplyArgumentValidationTest,LaterDecidingOperandTest,HigherOrderVariadicFunctionsTest`) `PASS` (per-class counts not recorded); full reactor `package` `PASS` with **10,591** tests, **36** skipped, **0** failures/errors (`xacml-core` 7,363/2 skipped, `xacml-pdp` 808/3, `xacml-conformance-tests` 1,818/30, `xacml-jmh` 225, `xacml-xml` 254, `xacml-json` 24/1, `xacml-test` 99).
  - Controlled JMH pair (`PdpDecideBenchmark.decide`, `-prof gc`, JDK 25 C2 `-jvmArgs -XX:-UseJVMCICompiler`) and async CPU profile gates for task `42.19`: **UNRUN** (`NOT RUN` in `REPORT-I-D52-terra.md:25`).
