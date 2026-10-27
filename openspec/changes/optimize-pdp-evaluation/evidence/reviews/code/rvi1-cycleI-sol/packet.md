READ-ONLY CODE REVIEW (a new packet in this pane; same rules: no file writes, no Maven, tests, benchmarks,
git or br writes). Role: code reviewer from a model family other than the author's (the conductor, Claude
Opus 5.5, wrote the change). Lane tag RVI1.

Review cycle I of openspec/changes/optimize-pdp-evaluation: design.md D15, tasks.md group 13, and the
uncommitted changes compared with HEAD (21858796):
- xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/DefaultFunctionInvocation.java: a static
  create(factory, instance, method, ...) that binds a static two-parameter method with LambdaMetafactory
  (cached per declaring class in a ClassValue) when the factory is the default one, the method takes no
  EvaluationContext, is not variadic, has non-primitive parameters, returns a ValueExp, and its class has
  the same class loader as xacml-core; invokeDirect(spec, context, arg0, arg1) calls the binding, or the
  old list path when unbound or when an argument is not of the parameter type.
- .../impl/JavaMethodToFunctionSpecConverter.java: uses create(...).
- xacml-core/src/main/java/org/xacml4j/v30/policy/function/FunctionSpecBuilder.java (FunctionSpecImpl):
  invoke(context, arg0, arg1) of an eager pair with a direct invocation evaluates the operands in order and
  calls invokeDirect; with validation at runtime it takes the old validated path; failures map as in
  doInvoke; one guarded debug line per call replaces the six debug lines of the old path.
Tests: xacml-core/src/test/java/org/xacml4j/v30/policy/function/DirectPairCallTest.java (red first:
evidence/tests/024-cycleI-red, 72 bytes per call before; green after).

Look for: any input where the direct call and the reflective call differ (result, exception class, status,
cause, message; null operands; an operand of the wrong type; an Error; an exception whose cause chain
holds an EvaluationException; a method that returns null); a function whose spec says two eager mandatory
parameters while its Java method differs (function references, any-bag or any-value parameters,
generic or erased parameter types); access or class-loader problems of LambdaMetafactory (package-private
provider classes, providers from other loaders, the ClassValue cache and class unloading); thread safety;
bypass of a custom FunctionInvocationFactory or a custom FunctionInvocation; the changed debug output;
anything the old path did that the new one skips (Preconditions checks, logging that a test pins); tests
that pass for the wrong reason (the allocation test logs at INFO only during its measurement).

Report: findings most severe first (P1 to P3) with file:line, the smallest fix, and the test; then a
verdict: APPROVE, APPROVE WITH CHANGES, or REJECT.

Write your report between these two lines, each on its own line, exactly as written:
=== BEGIN RVI1 REPORT ===
=== END RVI1 REPORT ===
