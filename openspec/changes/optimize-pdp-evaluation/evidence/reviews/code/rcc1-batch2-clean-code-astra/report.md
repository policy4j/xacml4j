# rcc1-batch2-clean-code-astra (Codex Astra, NTM worktree lane, read-only)

FINAL REPORT (IJK-astra, Codex Astra)

Verdict: **APPROVE WITH CHANGES**. No new production correctness defect found by inspection.

**P2 — Remove tests and assertions that violate the maintainer’s rule.**

| Location | Finding and action |
|---|---|
| [DirectPairCallTest.java:225](xacml-core/src/test/java/org/xacml4j/v30/policy/function/DirectPairCallTest.java:225) | Remove `pairCallOfABuiltInPredicateAllocatesNothing` and its measurement helpers. It tests allocation counts. Keep the result, failure, operand-order, and extension-behavior tests. |
| [DefaultEvaluationContextHandlerTest.java:910](xacml-pdp/src/test/java/org/xacml4j/v30/pdp/DefaultEvaluationContextHandlerTest.java:910) | Remove `repeatedRequestDesignatorGivesTheSameOptional`. Its distinguishing assertion is wrapper identity. The test at line 854 already checks the bag, request lookup, PIP exclusion, and resolved attributes. |
| [DescendantContextInventoryTest.java:79](xacml-core/src/test/java/org/xacml4j/v30/policy/DescendantContextInventoryTest.java:79) | Remove this class. It scans compiled classes and freezes private implementation structure. Retain the context-query and external-subclass behavioral tests. |
| [ContextChainQueryCharacterizationTest.java:140](xacml-core/src/test/java/org/xacml4j/v30/policy/ContextChainQueryCharacterizationTest.java:140) | Remove the exact private-class-name chain assertion and its collection at line 306. Keep the decision, query answers, variable scope, and evaluation-order assertions. |
| [DirectBindingEligibilityTest.java:54](xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/DirectBindingEligibilityTest.java:54) | Remove `publicStaticTwoParameterMethodIsBound`: it only proves the optimization was selected. In the hidden-class test, retain invocation/result coverage and drop the mechanism assertions at lines 78–79. |

Other prohibited tests remain in this local tree:

- [DecisionDebugLoggingTest.java:131](xacml-pdp/src/test/java/org/xacml4j/v30/pdp/DecisionDebugLoggingTest.java:131): remove the logging test class. The reported deletion has not reached this worktree.
- [FunctionErrorCompatibilityTest.java:141](xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/FunctionErrorCompatibilityTest.java:141): remove `coreExceptionKeepsItsSerialDescriptor`—UID assertions plus a known serialization failure—and `stackTraceSettingIsReadOnceInAFreshJvm` at line 160 with its probe. Keep the decision/status test at line 104.
- [MatchTest.java:204](xacml-core/src/test/java/org/xacml4j/v30/policy/MatchTest.java:204): this pre-existing test explicitly preserves the `policy4j-0972` defect. List it for removal under the new rule; it is outside I/J/K.

**P3 — Simplify the direct-binding cache.**

At [DefaultFunctionInvocation.java:63](xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/DefaultFunctionInvocation.java:63), `ClassValue<Map<Method, …>>` is unnecessary under the existing eligibility guard: hidden classes and foreign defining loaders are excluded at lines 155–158. Named classes share their defining loader’s unloading lifetime. [JLS §12.7](https://docs.oracle.com/javase/specs/jls/se25/html/jls-12.html#jls-12.7)

Keep those guards and use:

```java
private static final Map<Method, Optional<Binary>> BINDINGS =
		new ConcurrentHashMap<>();
```

Replace the lookup at line 138 with:

```java
BINDINGS.computeIfAbsent(m, DefaultFunctionInvocation::bind).orElse(null)
```

This simplifies registration without changing the decision path.

**P3 — Reuse the existing array-view convention for fallback arguments.**

In the same file, lines 217–220 construct an `ArrayList` and add two entries. The receiving implementation only reads that list, and its existing varargs overload already uses `Arrays.asList`. Replace those four lines with:

```java
return invoke(spec, context, Arrays.asList(arg0, arg1));
```

This preserves null arguments and the wrapper-plus-array allocation shape. The bound-call path is unchanged.

**P3 — Fix local style and comment issues.**

- [BaseCompositeDecisionRule.java:222](xacml-core/src/main/java/org/xacml4j/v30/policy/BaseCompositeDecisionRule.java:222): the new switch uses spaces amid tab-indented code. Match the surrounding indentation.
- [FunctionSpecBuilder.java:578](xacml-core/src/main/java/org/xacml4j/v30/policy/function/FunctionSpecBuilder.java:578): two consecutive Javadoc blocks leave the normalization documentation detached. Move it onto `normalize`. Remove the change narration “No per-call debug line” at line 409.
- [CombiningListTypesCharacterizationTest.java:188](xacml-core/src/test/java/org/xacml4j/v30/policy/combine/impl/CombiningListTypesCharacterizationTest.java:188): the new proxy silently returns null for unexpected calls. Follow the fail-fast pattern already used at line 134:

```java
default: throw new UnsupportedOperationException(method.getName());
```

Verification: read the requested production diff and new tests; final diff and status matched the initial reads. No files edited and no builds/tests run. Runtime behavior and performance remain **UNVERIFIED**.
