# rd23s-D23-sol (Codex GPT-6.1 Sol, NTM worktree lane, read-only)

**Verdict: REJECT — one P2.** Lines refer to the patched source reconstructed in memory.

**P2 — `xacml-core/src/main/java/org/xacml4j/v30/policy/Apply.java:265`: custom specs can receive a different invocation.**

Previously, `Apply` always called the varargs overload. The new branch calls the pair overload for every custom `FunctionSpec`. A spec overriding both methods now bypasses its varargs implementation, potentially changing validation, results or statuses. The new `ApplyTest.java:119` covers only a spec inheriting the default pair method, which delegates to varargs; it misses this case.

**Smallest fix:** retain `Apply`’s original varargs call. The new `FunctionSpecImpl.invoke(context, Expression...)` already routes eager built-in pairs internally (`FunctionSpecBuilder.java:405`). This also avoids adding another public dispatch API.

**Test:** extend the custom-spec test with distinct implementations of both overloads. Assert that `Apply` preserves the legacy varargs result/status.

Static inspection found no further defect in the requested paths:

- `DefaultFunctionInvocation.java:418`: the spread guard checks physical arity and accepts null or an instance of each reference parameter type, including the whole variadic array. Primitive parameters are excluded; mismatches retain reflection. `asFixedArity()` preserves array passing. This matches the relevant [JDK invocation rules](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/lang/reflect/Method.html).
- Binding, spread and factory paths retain first-`EvaluationException`/root-cause mapping. Operand evaluation remains ordered and single; lazy arguments retain their generic path. Runtime validation is read once per invocation.
- Factory identity, static/public accessibility, hidden-class and loader checks retain provenance. The concurrent binding cache publishes stateless adapters; argument arrays remain per-call.
- The approved unary public entry point is sufficient; no additional API is needed.

Verification: reconstructed all five patch files without writing them; inspected source, tests and JDK documentation. Ripwire’s baseline query was PARTIAL and does not describe the unapplied patch.

Supplied build evidence reports **6,595 found, 6,594 passed, one skipped, zero failures**, with nine test files excluded and compile stubs used. I ran no builds or tests. PDP, reactor and conformance remain unverified.
