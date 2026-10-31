1. **MEDIUM — A known static type error can be hidden in a skipped branch.**  
 **File:** `xacml-core/src/main/java/org/xacml4j/v30/policy/Apply.java:91`, `:224`, `:231`.

 Concrete condition:

 ```text
 or(true, string-equal("a", string-bag("a")))
 ```

 The inner `string-equal` is marked TYPE_ERROR. Its declared return type remains boolean, so the outer `or` passes validation. Evaluation returns true without evaluating the invalid child. A single Permit rule with this condition therefore gives Permit.

 This conflicts with the supplied §7.19.2 requirement: a policy containing invalid static data-types must yield Indeterminate with processing-error. The deciding-operand ruling explains lazy evaluation, but the skipped expression still contains a known static type error.

 **Suggested fix:** Retain known static errors across the expression tree and enforce them when evaluating the policy. Keep lazy handling for operational failures and arguments whose types remain unknown.

 **Missing test:** `knownStaticTypeErrorInSkippedOrBranchMakesPolicyIndeterminate`, using the condition above through the PDP, for both Permit and Deny effects.

Answers to the requested questions:

1. **Round 1’s demonstrated non-lazy cases are closed.** TYPE_ERROR blocks invocation immediately. AT_EVALUATION checks the evaluated values before invocation. The non-`SyntaxException` runtime fallback still permits invocation, but the supplied facts do not establish a concrete mismatched call through that fallback.

2. **No regression for type-correct calls is demonstrated.** Original arguments are evaluated in order. An `EvaluationException` propagates before later arguments are evaluated. Invocation receives values, which evaluate to themselves, so original argument expressions are not evaluated twice. Validation’s normalized parameters are discarded; invocation retains its existing handling of optional defaults and variadic parameters. A focused test asserting evaluation order, invocation counts, and preservation of an Indeterminate argument would strengthen this evidence.

3. **Both deferrals are reasonable within their stated bounds.** Checking arity after evaluating unknown-type arguments closes the non-lazy invocation hole. Preserving unevaluated arguments for lazy functions respects the deciding-operand ruling. Neither rationale addresses the known static error hidden in the finding above.

4. **The supplied normative text supports processing-error for invalid static types.** It does not itself prescribe construction-time caching, suppression of argument evaluation, or message contents. Those are implementation requirements. It also does not expressly classify every arity error as processing-error. The policy-level static-error obligation is broader than the current local evaluation guard.

The passing reactor, conformance, and mutation results are supplied evidence; they do not cover the skipped-branch scenario. No tools or commands were used.

VERDICT: REQUEST_CHANGES
