# Run 086: where the changed code is used

Asked for by the maintainer during the run ("use serena and other mcp
tools", "ripwire and tgrep"). Each claim names its tool and its label
(`AGENTS.md`, "Evidence Labels").

| Question | Tool | Result | Label |
| --- | --- | --- | --- |
| Who calls `isValidParamType`? | ripwire `uses` | `Match` (2 sites), `TypeAssertion.evaluate`, and the `validate` method of the four parameter specifications | SUPPORTED: each site read in source |
| Does `Match` see a dynamic type? | source | no: its two types are the type of a literal value and the data type of an attribute designator or selector element, both from the registry | SUPPORTED |
| Who calls `resolveReturnType`? | ripwire `uses` | `Apply.getEvaluatesTo` and the constructor of `FunctionReference`; three tests | SUPPORTED: read in source |
| Who reads `Apply.getArguments`? | ripwire callers | `ExpressionTypeBuilder.from` (XML marshalling) and tests. It gets the arguments without the assertions | SUPPORTED: read in source |
| Who uses `TypeAssertion`? | ripwire `uses` | `Apply.withTypeAssertions` only | SUPPORTED |
| How are arguments bound to parameters? | ripwire `fetch_body`, source | `FunctionSpecBuilder.normalize`: by position; arguments beyond the last parameter are appended. This is the binding that `Math.min(i, last)` assumes | SUPPORTED |
| Which functions take a function reference? | tgrep, `@XacmlFuncParamFunctionReference` | the seven of `HigherOrderFunctions` | PARTIAL: main sources of the reactor; an extension function is outside the tree |
| Which functions evaluate their own arguments? | tgrep, `evaluateArguments=false` | `and`, `or`, `n-of` | PARTIAL: as above |
| Is a `ValueType` cast to `XacmlTypes` anywhere? | tgrep (fresh index) | no match for the cast or for `instanceof XacmlTypes`; implementers of `ValueType`: `XacmlTypes`, `DynamicType` | PARTIAL: text search, one lane |
| Where is a bag built from the static type of an expression? | tgrep, ripwire `uses` of `bagBuilder` | `QuantifiedMap.evaluate` and `HigherOrderFunctions.map`. `map` refuses a predicate that returns a bag, so its type is not dynamic. `QuantifiedMap` is not reachable | SUPPORTED for `map` (test); PARTIAL for the rest |
| Are the quantified expression classes used? | tgrep over all Java files | the five files name each other only; no test, no mapper | PARTIAL: text search, one lane |
| Is there a function without parameters? | ast-grep, tgrep | none in the main sources; `FunctionSpecBuilder` allows one | PARTIAL: an empty ast-grep result is not proof; the builder was read |
| Did a signature change? | ripwire `edit_check` | `unchanged` for `isValidParamType` (both), `resolve`, `validateParametersAndThrow`, `allOfAll`, `BagOfValues.Builder`. Two rows marked incompatible for `resolve` are calls of `Entity.resolve` and `EvaluationContext.resolve` (name match) | SUPPORTED: the two rows read in source |
| What did the working tree make worse? | ripwire `quality_delta` | no finding in main code that needs a change: `equals` of the two parameter specifications reported as unused (they are called through collections), small clones in constructors. One clone in the test was removed (`assertDoesNotFit`) | PARTIAL: heuristic |
| Lint rules | `ast-grep scan` | no finding | SUPPORTED |
| Type-aware references | Serena | not used for this tree: the session's Serena serves the primary checkout, which holds another code line (its `isValidParamType` has two parameters). The Serena of this worktree passed its health check and was not reachable as an MCP tool from the session | UNAVAILABLE; fallback: ripwire and source |

What the audit changed in the run:

- A function without parameters accepted arguments in the check
  (`FunctionSpecBuilder`, both validation methods). With a dynamic
  argument the new code would have asked for the parameter at index -1.
  Fixed, with a test that was red first (`red-2-surefire-summary.txt`).
- Tests for the invariants that the soundness argument rests on: a
  reference to a designator function is no predicate for any of the seven
  functions; a dynamic bag behind a variable reference; an argument of the
  variadic parameter; the validation at invocation with the assertions in
  the argument list; a single value of dynamic type.
