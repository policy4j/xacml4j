1. **MEDIUM — Reference chains longer than 64 hide a known type error.**  
 File: `xacml-core/src/main/java/org/xacml4j/v30/policy/Apply.java:109` and `:119`.

 Construct `bad = string-equal("a", string-bag("a"))` through `Apply`, then wrap it in 65 successive variable references, each definition containing the preceding expression. Build `or(true, reference65)` through `Apply`.

 After 64 dereferences, `typeErrorInside` still holds a `VariableReference`, so it misses `bad`. The inner expression’s declared return type remains boolean: `Apply.getEvaluatesTo()` resolves the return type without consulting its error flag. With runtime validation disabled, the outer `or` returns true.

 **Round 2 is therefore not fully closed.** A Permit condition using this expression can still give Permit.

 **Fix:** Follow the complete acyclic reference chain, using identity-based cycle detection instead of a depth cutoff.  
 **Missing test:** Compare chains of 64 and 65 references behind `or(true, reference)`, including a PDP assertion. The OpenSpec requirement promises inheritance through variable references without this limit.

2. **MEDIUM — An unknown type suppresses independently known argument errors in lazy functions.**  
 File: `xacml-core/src/main/java/org/xacml4j/v30/policy/Apply.java:132` and `:271`.

 Build:

 ```text
 or(true, "a", stringBagOfUnknownType())
 ```

 Here `stringBagOfUnknownType()` is the expression already constructed by the new test helper. The string operand is statically known to violate `or`’s boolean parameter type. Nevertheless, the later unresolved argument makes the preliminary scan return `AT_EVALUATION`, bypassing validation of every argument.

 With runtime validation disabled, evaluation invokes the lazy function, which returns true at its first operand. The known string mismatch is never rejected.

 **Fix:** Add partial static validation in `FunctionSpec`: reject known parameter mismatches and known arity violations while deferring unresolved constraints. Preserve short-circuiting for unresolved operands.  
 **Missing test:** The expression above must be processing-error without evaluating either later operand. Allowing an unresolved operand to remain skipped does not justify skipping a separately known static violation.

Inheritance itself has no demonstrated false positive in the supplied material. A flagged child is already invalid under the stated static typing rule, and a variable reference denotes its definition. Skipping that child during evaluation does not make the containing expression statically valid.

The three policy-level exclusions are sound **relative to the requested Indeterminate-expression baseline**: an unreached rule, unmatched obligation or advice, or unused variable behaves identically if its invalid expression throws Indeterminate when reached. I cannot show a less-safe decision against that baseline. This remains narrower than the literal policy-wide wording of §7.19.2 and should retain the stated conformance limitation.

The OpenSpec text omits the reference-depth cutoff. Its unknown-type exception also needs clarification so that deferral applies to unresolved constraints, while known violations remain errors.

Review used only the supplied diff and facts. Reactor and mutation results are supplied evidence; no checks were run.

VERDICT: REQUEST_CHANGES
