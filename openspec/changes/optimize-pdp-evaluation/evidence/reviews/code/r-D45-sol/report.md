FINAL REPORT (Codex Sol, GPT-6)

**Verdict: REJECT — one P2 compatibility regression; one P3 retention issue.**

1. **P2 — Fallback changes which predicate a `Condition` subclass evaluates.**  
   `xacml-core/src/main/java/org/xacml4j/v30/policy/Condition.java:116`; `PreparedConditions.java:141` and `:151`.

   Before D45, inherited `Condition.evaluate()` evaluates its private constructor predicate. Once a Policy qualifies, the new fallback instead calls the overridable `getExpression()`. A subclass overriding that accessor can therefore change decisions or introduce an exception. This also happens when observation, runtime validation, or a foreign context disables sharing.

   Concrete case: four conditions contain true normalization predicates, while their subclass exposes a different expression through `getExpression()`. Previously the first-applicable policy permits; the new fallback evaluates the exposed expression instead.

   **Fix:** exclude Condition subclasses from the count/preparation path and preserve the private predicate for inherited evaluation and every fallback. Do not make the public accessor final.

   **Test:** an external-package Condition subclass, four qualifying rules, and assertions that its decisions and statuses remain unchanged with observation enabled and disabled.

2. **P3 — Weak Policy keys do not guarantee release of the Policy.**  
   `xacml-core/src/main/java/org/xacml4j/v30/policy/PreparedPolicySets.java:66`; `PreparedConditions.java:180` and `:400`.

   The strong value retains Condition keys, original subtrees, and reused siblings. A trusted custom sibling—or Condition subclass—holding its Policy creates this path:

   `Generation → PreparedConditions → Condition/sibling → Policy`

   Opus’s residual 5 is real. Retention lasts until the generation becomes unreachable; existing strong entries in `targets` do not establish that every affected Policy was already retained there.

   **Fix:** give the Policy ownership of its prepared bundle and use weak handles from the generation, or obtain an explicit decision accepting generation-lifetime retention. Verify ownership through retained-object inspection rather than a brittle GC unit test.

The remaining checks found no additional defect:

- **Provenance:** canonical unary-binding identity, default-factory construction, and the closed signature/resolver checks exclude ordinary custom factories, look-alikes, custom specs and public-constructor invocation wrappers (`FunctionSpecBuilder.java:298`; `DefaultFunctionInvocation.java:313`, `:411`).
- **Keys:** string contents, bag order and multiplicity, complete designator key, MustBePresent, and FunctionSpec identity are retained (`PreparedConditions.java:293`, `:303`, `:423`).
- **Selection and spine:** Policy-wide counting, four occurrences, two transformations, and maximal-root selection match D45. Ancestors use normal Apply construction; siblings remain in position. D6 binding remains deferred (`PreparedConditions.java:340`, `:385`; `Apply.java:99`). `Apply.evaluate` is unchanged.
- **Memo and gates:** deciding thread, observation, validation and request-entity gates are checked again at shared nodes. Hits precede bag inspection. Misses require one exact request StringVal; failures publish nothing. Evaluation stays outside map callbacks (`PreparedConditions.java:155`, `:269`, `:485`).
- **Storage:** expression entries never set `ReferenceMiss.INSTANCE`. Map publication remains atomic. The root’s field layout is unchanged, consistent with the supplied 80 B histogram (`RootEvaluationContext.java:111`, `:887`, `:908`, `:948`).
- **Implementation choices:** the Condition count, stored Optional, and preparation race are reasonable. Racing preparations publish one winning bundle.
- **Tests:** the decision, status, retry, multiplicity and observer-sequence cases pin useful outcomes. A mutant disabling sharing passing these tests is acceptable: functional equivalence should hold. The supplied benchmark and histogram establish that the optimization runs. Add the subclass compatibility case above.

Verification was read-only: all ten packet files matched main before and after inspection; scoped `git diff --check` and ast-grep passed. Ripwire results were **PARTIAL** because of name ambiguity and the accumulated-batch baseline. Builds, tests, mutants and measurements were not rerun; their results remain supplied evidence.

Trust boundary: in-process extensions are trusted. Finding 1 uses ordinary public subclassing.
