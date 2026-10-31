# Run 086 research: how other evaluators treat a type that is known only at evaluation

Asked for by the maintainer ("do a research how other expression
evaluators do it").

## Method

- The deep-research MCP server gave no report on 2026-10-08: the first call
  hit the 300 s tool limit, the second was aborted after 1,131 s without
  progress. The next lane, Tavily research, was aborted after 631 s, and
  the Tavily gateway then failed with DNS errors.
- The sources below are primary sources, fetched directly on 2026-10-09.
  Each quote was searched for in the fetched file and found. The SHA-256
  is of the file as fetched.
- Two papers are peer reviewed (Scheme workshop 2006, POPL 2016). The rest
  is product documentation and source code of the default branch on the
  fetch date; no release is pinned.

## How the unknown type is represented

| System | Quote or code | Source |
| --- | --- | --- |
| CEL | "The type checker also introduces the `dyn` type, which is the union of all other types." | S1 |
| cel-java | `public static final CelType DYN = create(CelKind.DYN, "dyn");` and `isAssignableFrom` begins with `kind().isDyn()` | S2 |
| Trino | `public final class UnknownType`, `public static final UnknownType UNKNOWN = new UnknownType();` | S3 |
| PostgreSQL | "`unknown` literals are assumed to be convertible to anything for this purpose." | S4 |
| OPA / Rego | "A represents the superset of all types." "Any represents a dynamic type." | S5 |
| TypeScript | `unknown` "is similar to the any type, but is safer because it's not legal to do anything with an unknown value" | S6 |
| Gradual typing | "We axiomatize the consistency relation ~ on types": `T ~ T`, `T ~ ?`, `? ~ T`, and function types by their parts. "The type consistency relation is reflexive and symmetric but not transitive" | S7 |

Finding: the Java engines model the unknown type as one more instance of
their ordinary type abstraction, a singleton, with a relation that is
wider than equality. None uses a null, an `Optional`, or a second
hierarchy. `DynamicType.INSTANCE` and `DynamicType.isConsistent` follow
that.

Apache Calcite was looked at and is not used as support: `ANY` and
`UNKNOWN` are constants of `SqlTypeName` without a description in the
Javadoc (S14).

## What is checked, and when

| System | When the expression is built | At evaluation | Source |
| --- | --- | --- | --- |
| Gradual typing | an argument whose type is consistent with the parameter type is accepted | "the argument type differs from the parameter type, but is consistent. In this case the argument is cast to the parameter type" | S7 |
| Saxon | "There are three possible outcomes. If the static type of the expression is a subtype of the required type, the method returns the expression unchanged. If the static type of the expression is incompatible with the required type [...] the method throws an exception (this results in a compile-time type error being reported)." | "If the static type is a supertype of the required type, then a new expression is constructed that evaluates the original expression and checks the dynamic type of the result" | S8 (Saxon 9.3 Javadoc) |
| AuthzForce (XACML) | the constructor checks the arguments that are known: "type of arg #... not valid" | "Some of the arguments (expressions) may not be known in advance, but only at evaluation time"; `evaluate(context, mdpContext, checkRemainingArgTypes, remainingArgs)` | S9 |
| WSO2 Balana (XACML) | `function.checkInputs(xprs);` "throws IllegalArgumentException if the input expressions don't match the signature of the function" | no check found | S10 |
| OPA / Rego | "OPA type checks what it knows statically" | "and leaves the unknown parts to be type checked at runtime" | S11 |
| CEL | optional type check | `no_matching_overload`: "this function has no overload for the types of the arguments" | S1 |
| C# `dynamic` | "an object of type dynamic bypasses static type checking" | a wrong call "causes a run-time exception" | S12 |
| Cedar | validated policies "are sure not to exhibit most errors that could arise during request evaluation" | - | S13 |
| XPath 3.1 | the Static Typing Feature can be in effect or not | "If an operand of an expression is found to have a dynamic type that is not appropriate for that operand, a type error is raised [err:XPTY0004]." | S15 |

Finding: an engine whose calls are not checked at evaluation (Saxon;
AuthzForce for XACML) rejects what is known to be wrong when the
expression is built, and puts a check on the argument where the static
type was not precise enough. `TypeAssertion` is that check. Balana
rejects at parse; conformance cases IIC003 and IIC014 rule that out here
(run 083, "Design A").

## Operands that are not evaluated

- XPath 3.1, section 2.3.3: "If an expression can validly return a value
  or raise a dynamic error, the implementation may choose to return the
  value or raise the dynamic error". Section 3.1.5.1: "a function need not
  evaluate an argument if the function can evaluate its body without
  evaluating that argument." (S15)
- CEL: "In the boolean operators `&&` and `||`: if any of their operands
  uniquely determines the result [...] the other operand may or may not be
  evaluated, and if that evaluation produces a runtime error, it will be
  ignored." (S1)
- This change: ruling D11 (run 074).

## Sources

| Id | URL | SHA-256 |
| --- | --- | --- |
| S1 | https://raw.githubusercontent.com/cel-expr/cel-spec/master/doc/langdef.md | `d39dcc9fe403b221ccf9cb3d1cd61fab1a55c4b89a6289d41053db9f7698321c` |
| S2 | https://raw.githubusercontent.com/google/cel-java/main/common/src/main/java/dev/cel/common/types/SimpleType.java | `b33aebae475be15c48b51e05b16d3f94b21a84a0a97fef40c4228fb8b3513922` |
| S3 | https://raw.githubusercontent.com/trinodb/trino/master/core/trino-main/src/main/java/io/trino/type/UnknownType.java | `e0cb6c43ceadce0e793135f1511d7fd6efd660b4c1d7fd953f1ecc6d12de400a` |
| S4 | https://www.postgresql.org/docs/current/typeconv-func.html | `58c7115be36b1518c5211816c662fec4b5854b96b4ff180f885fc50b002bab76` |
| S5 | https://pkg.go.dev/github.com/open-policy-agent/opa/v1/types | `3255e416114ebc959ee5c3af4717d8beff61dbbf9188acaba04d84ed77fb605d` |
| S6 | https://www.typescriptlang.org/docs/handbook/2/functions.html | `54261e293276fe307f55063e24fe195bf1b87de75aeed0afb8d430493fb73962` |
| S7 | Siek and Taha, "Gradual Typing for Functional Languages", Scheme and Functional Programming Workshop 2006, http://scheme2006.cs.uchicago.edu/13-siek.pdf | `8da662cee42e2bf9c549c81db23a8309bc8a429e726613ab4a3e3b7ad0b61e70` |
| S8 | https://www.saxonica.com/documentation9.3/javadoc/net/sf/saxon/expr/TypeChecker.html | `ade19963fb2f9239e85ba91ccd9794839aaed806258680be64513b949bab4e09` |
| S9 | https://raw.githubusercontent.com/authzforce/core-pdp-api/develop/src/main/java/org/ow2/authzforce/core/pdp/api/func/BaseFirstOrderFunctionCall.java | `85333b5888ad7a6a39e28fb826e713ef7618172a79667e2bb1c41b06dd78358c` |
| S10 | https://raw.githubusercontent.com/wso2/balana/master/modules/balana-core/src/main/java/org/wso2/balana/cond/Apply.java | `437fa05772fcbc1560797d3c4bb1543308dc7db39182eb5121920e7de624ebc1` |
| S11 | https://www.openpolicyagent.org/docs/policy-language | `1638f03b3b8cd9277662394636723728527c5d95739cdc60efe9512231670d4c` |
| S12 | https://learn.microsoft.com/en-us/dotnet/csharp/advanced-topics/interop/using-type-dynamic | `eb7bb0f12e55118b254337808016dcf3f0b627a12e5b1864d35c19b11a4ea5fa` |
| S13 | https://docs.cedarpolicy.com/policies/validation.html | `03fb9ca5488082a05fa00c0c3713c7bafc2ef13ee91391e40082063b78ecf276` |
| S14 | https://calcite.apache.org/javadocAggregate/org/apache/calcite/sql/type/SqlTypeName.html | `1cebd00f1aff5b0c1340b0bc55015c857ca9bf93e5c734f974d184443c92738c` |
| S15 | https://www.w3.org/TR/xpath-31/ | `ca1f8b00f9626e3e73589a371d702acf6bf0f4708c96a2cb0e87ca7a5ad3b88c` |

## Not verified, not used

- The current Saxon Javadoc. The quote is from the 9.3 documentation.
- Garcia, Clark and Tanter, "Abstracting Gradual Typing" (POPL 2016), was
  fetched (https://www.cs.ubc.ca/~rxg/agt.pdf, SHA-256
  `851ec46540552fc565496ee89e00a1023cf4e060b04b210d66490870e4aa4738`). The
  plan quoted the consistency rules from it; the evidence quotes S7, the
  original, instead.
