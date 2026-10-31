
FINAL REPORT (Codex Astra)

**Verdict: REJECT — two P1 findings.**

1. **P1 — VariableDefinition subclasses are incorrectly classified as silent.** [ResponseWriters.java:85](`xacml-core/src/main/java/org/xacml4j/v30/policy/ResponseWriters.java:85`) follows the definition’s expression without checking the definition’s implementation class. [VariableDefinition.evaluate:115](`xacml-core/src/main/java/org/xacml4j/v30/policy/VariableDefinition.java:115`) is overridable. A subclass can retain a Boolean literal expression while its evaluation writes Permit responses. Used by a Deny rule under a Permit-overrides parent, it now skips staging and leaks those responses into the final Permit.
   
   **Fix:** treat definitions whose class differs from `VariableDefinition.class` as writers before inspecting their expression. Add a response regression using that Deny-child/Permit-parent case.

2. **P1 — Custom invocation factories inherit an unjustified silence declaration.** [JavaMethodToFunctionSpecConverter.java:110](`xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/JavaMethodToFunctionSpecConverter.java:110`) checks only the method’s declaring class. Yet [XacmlDefaultFunctions.java:59](`xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/XacmlDefaultFunctions.java:59`) accepts a custom invocation factory. A wrapper around standard `and` receives the evaluation context and can write responses without ever declaring `writesNoResponses()`. It is nevertheless marked silent, producing the same filtering failure described above.
   
   **Fix:** require `invocationFactory == FunctionInvocationFactory.defaultFactory()` before automatically setting the flag. Keep custom factories conservative. Add an outcome regression for a wrapped standard function.

Both counterexamples bypass the existing filtering by enclosing decisions required by [XACML 3.0 §7.18](https://docs.oasis-open.org/xacml/3.0/xacml-3.0-core-spec-os-en.html).

The other requested checks support the implementation:

- Own target/condition evaluation precedes staging; `canRespondToParent()` accounts for this correctly ([BaseCompositeDecisionRule.java:119](`xacml-core/src/main/java/org/xacml4j/v30/policy/BaseCompositeDecisionRule.java:119`)).
- Attribute resolution delegates to the root, bypassing staging ([DescendantEvaluationContext.java:315](`xacml-core/src/main/java/org/xacml4j/v30/policy/DescendantEvaluationContext.java:315`)).
- The new public methods are small and conservative by default; the automatic factory classification is the exception above.
- Policy, PolicySet and Rule have private constructors. No subclass guard is needed.
- The three `clearInvocations` calls precede the stimulus and preserve the decision/status assertions: `PolicyTest:184`, `PolicySetTest:181`, `RuleTest:502`.

Read-only review of the supplied patch and current main sources. No builds or tests ran during this review; the counterexamples are source-derived and unexecuted. Opus’s reported green gates remain supplied evidence.
