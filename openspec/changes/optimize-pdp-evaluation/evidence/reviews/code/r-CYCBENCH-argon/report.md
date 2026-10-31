# Code Review `R-CYCBENCH-argon`: `FIX-CYCLE` (Terra) and `RX-BENCH` (Opus)

- **Reviewer**: Gemini Argon (`agyx_1`)
- **Saved Report**: [REPORT-R-CYCBENCH-argon.md](`.ntm/worktrees/xacml4j--batch2/agyx_1/REPORT-R-CYCBENCH-argon.md`)
- **Verdicts**:
  - **`FIX-CYCLE`**: **APPROVE WITH CHANGES** (`1 P1`, `1 P2` — redo before the batch measurement)
  - **`RX-BENCH`**: **APPROVE** (`0 P1`, `0 P2`, `0 P3`)

---

## Part 1: `FIX-CYCLE` (Codex Terra)

### Verdict: **APPROVE WITH CHANGES** (Redo before the batch measurement)

### Point-by-Point Check

1. **Correctness**:
   - **What holds (`SUPPORTED`)**:
     - Runtime `<PolicySetIdReference>` cycles between referenced policy sets (`a -> b -> a`) return `Decision.INDETERMINATE` with `urn:oasis:names:tc:xacml:1.0:status:processing-error` per OASIS XACML 3.0 §7.15 ([`PolicySetIDReference.cyclicContext`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/PolicySetIDReference.java`)).
     - Under `deny-overrides` ([`DenyOverrides.doCombine`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/combine/impl/DenyOverrides.java`)), `Decision.INDETERMINATE` sets `atLeastOneIndeterminateDP = true`, so a cyclic reference alongside a `Permit` sibling yields `Decision.INDETERMINATE_DP` (mapped to `Decision.INDETERMINATE` in the final PDP response, and propagated as `Indeterminate{DP}` to enclosing policy combining algorithms per §7.13 / §7.14).
     - Entering a `<PolicyIdReference>` while `context.getCurrentPolicy() != null` returns `Decision.INDETERMINATE` with `urn:oasis:names:tc:xacml:1.0:status:syntax-error` per §5.14 and §7.19.2 ([`PolicyIDReference.invalidContext`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/PolicyIDReference.java`)).
     - Pinned (`D24`/`D25` [`PinnedReference`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/PinnedReference.java`)) and unpinned references, observed and unobserved decisions, and Cycle AP's merged contexts behave identically on `ref -> ref` cycles.
   - **What fails (`P1-1`)**:
     - [`PolicySetIDReference.isReferenceCyclic`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/PolicySetIDReference.java`) checks only whether an equal `PolicySetIDReference` (`ref.equals(context.getCurrentPolicySetIDReference())`) is on the ancestor chain, and never checks whether the **resolved target `PolicySet`** is already on the active `PolicySet` ancestor stack (`context.getCurrentPolicySet()`).
     - As a result, when a cycle points back to a **root `PolicySet`** (`root a -> ref("a")`, as in the `"self"` test case in [`ReferenceContextPdpTest.java:450-456`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-pdp/src/test/java/org/xacml4j/v30/pdp/ReferenceContextPdpTest.java`)), an **inline `<PolicySet>`** (`a -> inline b -> ref("a")`), or a `PolicySet` entered through a `<PolicySetIdReference>` with a different version constraint (`ref("a", "1.*") -> a -> ref("a", "1.0")`, where `ref1.equals(ref2)` is `false`), the first entry to `ref("a")` is **not detected as cyclic**. Instead, `ref("a")` re-enters and evaluates `a` a **second time** before `ref("a")` is encountered again.
     - Because `FIX-CYCLE` returns `Decision.INDETERMINATE` instead of throwing an uncaught exception, unrolling `a` a second time changes the evaluation outcome whenever `a` has another child that overrides `Indeterminate` (see `P1-1` below for concrete counterexamples where `FIX-CYCLE` returns `DENY` or attaches obligations from a cyclic branch instead of treating `ref("a")` as `Indeterminate`), and fires duplicate `DecisionObserver` events on `a`.

2. **The `evaluating` flags and simpler zero-mutable-state design**:
   - **Why `evaluating` was needed**:
     Because `PolicySetIDReference.createContext` and `PolicyIDReference.createContext` return the target `PolicySet`/`Policy`'s own evaluation context (`ctx`), `ctx` is used first as the reference's context (`ref.isMatch(ctx)`, `ref.evaluate(ctx)`) and then passed unchanged into `target.combineDecisions(ctx)` as the parent context for `target`'s children. Without distinguishing those two phases, `context.getCurrentPolicy*IDReference() == this` at the start of `createContext` matches both idempotent pre-evaluation reuse (`ref.createContext(ref.createContext(parent))`) and same-instance recursion from inside `target`.
   - **Why static cycle detection at D24/D25 pin time cannot replace runtime detection**:
     OASIS XACML 3.0 §7.15 specifies *"Circular references, if detected at evaluation time, SHALL return Indeterminate..."*. If a policy set precedes a cyclic reference with a rule/policy that short-circuits (`first-applicable`, `deny-overrides`, `permit-overrides`), or if a policy set containing a cycle has a `<Target>` that evaluates to `NoMatch` for the current request, the cycle is not reached at evaluation time and must not make the decision `Indeterminate`.
   - **How to eliminate `evaluating` from the merged contexts (and optionally everywhere)**:
     Cycle AP's merged contexts ([`ReferencedPolicySetContext`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/PolicySetIDReference.java`) and [`ReferencedPolicyContext`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/PolicyIDReference.java`)) are package-private fast-path contexts created **only** inside an unobserved `RootEvaluationContext` decision (`DescendantEvaluationContext.isUnobserved(context)`). In a `RootEvaluationContext` decision, the engine (`DecisionCombiningAlgorithms`, `OnlyOneApplicablePolicyCombiningAlgorithm`, `TargetBeforeContext`, `RootEvaluationContext`) **never** calls `ref.createContext` twice on the same reference context; any call to `ref.createContext(ctx)` where `ctx` is a `ReferencedPolicySetContext` or `ReferencedPolicyContext` is **always** a child inside `target` re-entering `ref`. Excluding `ReferencedPolicySetContext` and `ReferencedPolicyContext` from `isReferenceContext(context)` removes `private boolean evaluating` from both merged context classes and makes `setEvaluating` a no-op on the unobserved fast path while keeping every test green.

3. **Cost (Object layout and parent-chain walks)**:
   - **Object size growth (`P2-1`)**:
     - Under default 64-bit HotSpot object headers (12-byte header with compressed class pointers, used by the standard JMH benchmark runs):
       - `DescendantEvaluationContext` = $12\text{ B (header)} + 3 \times 4\text{ B (`parent`, `top`, `evaluationStatus`)} = 24\text{ B}$.
       - `PolicySet.PolicySetDescendantEvaluationContext` adds synthetic `final PolicySet this$0` ($4\text{ B}$) = $28\text{ B}$ (4 bytes of padding).
       - `PolicySetIDReference.ReferencedPolicySetContext` adds synthetic `final PolicySetIDReference this$0` ($4\text{ B}$) = **32 B before `FIX-CYCLE`** (0 bytes of padding left).
       - Adding `private boolean evaluating` ($1\text{ B}$) in [`PolicySetIDReference.java:277`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/PolicySetIDReference.java`) pushes every `ReferencedPolicySetContext` from **32 B to 40 B (+8 B per `PolicySetIDReference` evaluation)**, undoing 25% of Cycle AP's 32 B savings on `PolicySetIDReference`.
     - Under compact object headers (`-XX:+UseCompactObjectHeaders`, 8-byte header):
       - `Policy.PolicyDescendantEvaluationContext` is $8 + 12 + 4\text{ (`this$0`)} + 4\text{ (`varDefEvalResults`)} = 28\text{ B}$, and `PolicyIDReference.ReferencedPolicyContext` (+4 B `this$0`) was **32 B before `FIX-CYCLE`**.
       - Adding `private boolean evaluating` ($1\text{ B}$) in [`PolicyIDReference.java:284`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/PolicyIDReference.java`) pushes `ReferencedPolicyContext` from **32 B to 40 B (+8 B per `PolicyIDReference` evaluation)** under compact headers (though under default 12-byte headers it was already 36 B $\to$ 40 B).
   - **Estimated cost on `rsa2008` and `synthetic:100`**:
     - `synthetic:100`: **0 B/op**, **0 ns/op** (no policy or policy-set references).
     - `rsa2008` (~3.8 `PolicyIDReference`s/decision, 0 `PolicySetIDReference`s): **0 B/op** under default 12-byte headers (**+30.4 B/op** under compact 8-byte headers). In CPU work, `isReferenceContext` short-circuits on `context.getCurrentPolicyIDReference() == this` (`null == this` is `false`, so `isEvaluating` is not called), and `setEvaluating(true/false)` matches `context instanceof ReferencedPolicyContext` on the first iteration (`c = context`) without walking parents, adding ~2 `instanceof` checks and 2 boolean stores per reference (~7.6 stores/decision).
   - **Recommendation**: **Redo before the batch measurement** to fix `P1-1` (first-back-edge cycle detection for root/inline/cross-version `PolicySet`s) and `P2-1` (remove `evaluating` from `ReferencedPolicySetContext` and `ReferencedPolicyContext` so `ReferencedPolicySetContext` returns to 32 B).

---

### Findings (`FIX-CYCLE`)

#### `P1-1`: Cycles back to a root, inline, or differently-versioned `PolicySet` are unrolled once before detection, corrupting decisions and observer events
- **Location**:
  - [`PolicySetIDReference.java:82`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/PolicySetIDReference.java`)
  - [`PolicySetIDReference.java:98`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/PolicySetIDReference.java`)
  - [`PolicySetIDReference.java:141-153`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/PolicySetIDReference.java`)
  - [`PolicySetIDReference.java:228-238`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/PolicySetIDReference.java`)
- **Failure**:
  `isReferenceCyclic(PolicySetIDReference ref, EvaluationContext context)` checks only `ref.equals(context.getCurrentPolicySetIDReference())` and stops as soon as `context.getCurrentPolicySetIDReference() == null`. It never checks whether the resolved target `PolicySet` is already active in `context`'s `PolicySet` ancestor chain (`context.getCurrentPolicySet()`).
  Consequently:
  1. **Root `PolicySet` self-cycle (`root a -> ref("a")`)**: On the first entry to `ref("a")`, `ctxA0.getCurrentPolicySetIDReference()` is `null`, so `isReferenceCyclic` returns `false`. `enter(ctxA0, a)` sees `ctxA0.getCurrentPolicySet() == a`, falls back to `a.createContext(new PolicySetIDReferenceEvaluationContext(ctxA0))`, and `ref("a").evaluate(...)` **evaluates `a` a second time from the top** (emitting a second `DecisionObserver` start/finish for `a` and re-evaluating `a` under `only-one-applicable` where the first `ref("a").isMatch` returns `MatchResult.MATCH` instead of `MatchResult.INDETERMINATE`).
  2. **Inline `PolicySet` cycle (`root a -> inline b -> ref("a")`)**: When `b` evaluates `ref("a")`, `ctxB.getCurrentPolicySetIDReference()` is `null`, so `ref("a")` is not detected as cyclic; it re-evaluates `a`. If `a` is `deny-overrides` with `[inline b (permit-unless-deny: [ref("a")]), Policy("p-deny")]`, `ref("a")` evaluates `a` (inner) and returns **`Decision.DENY` instead of `Decision.INDETERMINATE`**, causing `b` (`permit-unless-deny`) to return **`Decision.DENY` instead of `Decision.PERMIT`** (and similarly, if `a` is `permit-overrides` with `[inline b (first-applicable, obligation on Permit: [ref("a")]), Policy("p-permit")]`, `ref("a")` returns `PERMIT` to `b`, causing `b` to fulfill its `Permit` obligation even though `b`'s only child was a circular reference).
  3. **Cross-version reference cycle (`ref1("a", "1.*") -> a -> ref2("a", "1.0")`)**: `ref1.equals(ref2)` is `false` because their version constraints differ, even though both resolve to the same `PolicySet a`, unrolling `a` a second time before `ref1` is seen again.
- **Fix**:
  Check whether the resolved (or pinned) `CompositeDecisionRule policySet` is already active in `context`'s `PolicySet` ancestor chain (`isPolicySetCyclic(policySet, context)`) inside `enter(context, policySet)` and `canCheckTarget(context, target)`:
  ```java
  private EvaluationContext enter(EvaluationContext context, CompositeDecisionRule policySet){
      if(isPolicySetCyclic(policySet, context)){
          return cyclicContext(context);
      }
      if(policySet.getClass() == PolicySet.class && DescendantEvaluationContext.isUnobserved(context)){
          return new ReferencedPolicySetContext((PolicySet)policySet, context);
      }
      return policySet.createContext(new PolicySetIDReferenceEvaluationContext(context));
  }

  boolean canCheckTarget(EvaluationContext context, CompositeDecisionRule target){
      if(context.getCurrentPolicySetIDReference() == this || context.getCurrentPolicy() != null
              || isPolicySetCyclic(target, context)) return false;
      return !isReferenceCyclic(this, context);
  }

  private static boolean isPolicySetCyclic(
          CompositeDecisionRule policySet,
          EvaluationContext context)
  {
      CompositeDecisionRule current = context.getCurrentPolicySet();
      if(current == null){
          return false;
      }
      if(policySet.equals(current)){
          return true;
      }
      EvaluationContext parent = context.getParentContext();
      return parent != null && isPolicySetCyclic(policySet, parent);
  }
  ```
  Because `enter` already called `context.getCurrentPolicySet()` once after `context.resolve(this)`, replacing `context.getCurrentPolicySet() != policySet` with `isPolicySetCyclic(policySet, context)` keeps the exact call count and `InOrder` sequence verified by `PolicySetIDReferenceTest` while catching root, inline, and cross-version cycles immediately on the first back-edge.

#### `P2-1`: `private boolean evaluating` on `ReferencedPolicySetContext` and `ReferencedPolicyContext` grows `ReferencedPolicySetContext` from 32 B to 40 B (+8 B)
- **Location**:
  - [`PolicySetIDReference.java:102-124, 181-186, 277`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/PolicySetIDReference.java`)
  - [`PolicyIDReference.java:100-122, 185-190, 284`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/PolicyIDReference.java`)
- **Failure**:
  `ReferencedPolicySetContext` had an exact 32-byte layout under default 12-byte object headers ($12\text{ B header} + 12\text{ B } \texttt{DescendantEvaluationContext} + 4\text{ B } \texttt{PolicySet.this} + 4\text{ B } \texttt{PolicySetIDReference.this} = 32\text{ B}$). Adding `private boolean evaluating` overflows the 32-byte boundary to 33 B $\to$ **40 B (+8 B per `PolicySetIDReference`)** under default headers (and similarly pushes `ReferencedPolicyContext` from 32 B to 40 B under `-XX:+UseCompactObjectHeaders`).
- **Fix**:
  `ReferencedPolicySetContext` and `ReferencedPolicyContext` are created only in unobserved `RootEvaluationContext` decisions, where the engine never calls `ref.createContext` on an already-created reference context. Exclude `ReferencedPolicySetContext` and `ReferencedPolicyContext` from `isReferenceContext(context)` and remove `private boolean evaluating` from both classes:
  ```java
  // In PolicySetIDReference:
  private boolean isReferenceContext(EvaluationContext context){
      return !(context instanceof ReferencedPolicySetContext)
              && context.getCurrentPolicySetIDReference() == this
              && !isEvaluating(context);
  }

  private boolean isEvaluating(EvaluationContext context){
      for(EvaluationContext c = context; c instanceof DescendantEvaluationContext; c = c.getParentContext()){
          if(c instanceof PolicySetIDReferenceEvaluationContext r && r.reference() == this) return r.evaluating;
      }
      return false;
  }

  void setEvaluating(EvaluationContext context, boolean evaluating){
      if(context instanceof ReferencedPolicySetContext) return;
      for(EvaluationContext c = context; c instanceof DescendantEvaluationContext; c = c.getParentContext()){
          if(c instanceof PolicySetIDReferenceEvaluationContext r && r.reference() == this){
              r.evaluating = evaluating;
              return;
          }
      }
  }
  ```
  (And the symmetric change in `PolicyIDReference` for `ReferencedPolicyContext` / `PolicyIDReferenceEvaluationContext`.)
  This restores `ReferencedPolicySetContext` to **32 B** (and `ReferencedPolicyContext` to **32 B** under compact headers) and turns `setEvaluating` into a single `instanceof` no-op on the unobserved fast path.

---

## Part 2: `RX-BENCH` (Claude Opus)

### Verdict: **APPROVE**

### Point-by-Point Check

1. **`RegexFunctionBenchmark` ([`RegexFunctionBenchmark.java`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-jmh/src/main/java/org/xacml4j/jmh/regex/RegexFunctionBenchmark.java`)) (`SUPPORTED`)**:
   - Implements the four benchmark methods required by [`openspec/changes/add-regex-engine-spi/design.md`](`.ntm/worktrees/xacml4j--batch2/agyx_1/openspec/changes/add-regex-engine-spi/design.md`): `compile`, `match`, `call`, and `callMiss`, over `@Param({"conformance", "adversarial"}) String patterns` and `@Param({"16", "256", "4096"}) int inputLength`.
   - `CONFORMANCE` covers all 12 distinct `string-regexp-match` patterns from OASIS conformance tests (`IIB008`, `IIB009`, `IIC056`, `IIC057`, `IIC165`–`IIC169`), and `ADVERSARIAL` covers `\s+$`, `.*\.pdf$`, and `[a-z0-9._]+@example\.com`.
   - `callMiss` precomputes `rounds * inputs.items.length >= MISSED_PATTERNS` (`1,024`) distinct pattern variants (`1,032` for `conformance`, `1,026` for `adversarial`, $> 4 \times 256$) by appending a zero-repetition CJK ideograph `(char)(0x4E00 + i) + "{0}"` (`variant(pattern, i)`), which matches only the empty string in XML Schema, Java `Pattern`, and RE2J syntax.
   - The expected outcome (`item.finds()`) is derived directly from the input construction (`subject.length() <= length` for `found(length)` and `false` for `missed(length)`), never from a regex engine, and `@Setup(Level.Trial)` verifies every base pattern, every `Inputs` item (both `Pattern.matcher().find()` and `RegExpFunctions.matches`), and every `Misses` variant before measurement begins.

2. **Four new `rules:<n>` regex requests ([`RulesCorpus.java`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-jmh/src/main/java/org/xacml4j/jmh/corpus/RulesCorpus.java`)) (`SUPPORTED`)**:
   - `regex-targets`: $n$ rules under `first-applicable` matching `anyURI-regexp-match` on `https://records.example.com/<n-1>/report.pdf`; only rule $n - 1$ matches, returning `Result.ok(effect(n - 1))`.
   - `regex-conditions`: $n$ rules under `first-applicable` matching `string-regexp-match` in `<Condition>` on `user-<n-1>@mail.example.com`; only rule $n - 1$ matches, returning `Result.ok(effect(n - 1))`.
   - `regex-any-of-all`: $n$ `Permit` rules under `deny-overrides` (evaluating all $n$ rules) with the `any-of-all` + `string-regexp-match` condition and bags of OASIS `IIC168`, returning `Result.ok(Decision.PERMIT)`.
   - `regex-malformed`: one `Permit` rule under `first-applicable` with unclosed character class `^[a-z0-9._+@example\.com$`, pinning today's `Result.builder(Decision.INDETERMINATE_P, Status.processingError().build()).build()` at [`RulesCorpus.java:207`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-jmh/src/main/java/org/xacml4j/jmh/corpus/RulesCorpus.java`) (which will flip to `Status.syntaxError()` when `add-regex-engine-spi` D5 lands).
   - All four policies are placed at the end of the `first-applicable` root `PolicySet` behind distinct `urn:xacml4j:jmh:rules:case` targets, so existing requests (`integer-ranges`, `date-ranges`, `repeated-conditions`, `multi-valued`, `narrow`, `wide`, `responses`) do not evaluate or traverse them.

3. **Tests ([`RegexBenchmarkTest.java`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-jmh/src/test/java/org/xacml4j/jmh/regex/RegexBenchmarkTest.java`), [`RulesCorpusTest.java`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-jmh/src/test/java/org/xacml4j/jmh/corpus/RulesCorpusTest.java`), [`BenchmarkSmokeTest.java`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-jmh/src/test/java/org/xacml4j/jmh/pdp/BenchmarkSmokeTest.java`)) (`SUPPORTED`)**:
   - Pin observable outputs, pattern cardinality ($\ge 1,024$ distinct miss patterns), setup failure on a wrong oracle, JMH parameter matrix execution, and exact `Result` equality across all 11 `RulesCorpus` cases without coupling to internal engine classes.

### Findings (`RX-BENCH`)

None (`0 P1`, `0 P2`, `0 P3`).
