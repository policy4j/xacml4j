# 008: cycle D tests before and after (tasks 7.1, 7.2)

Date: 2026-10-27. Oracle GraalVM 25.0.4, Maven 3.9.16 offline, release 25.

Before-tree: the cycle D snapshot with the base `FunctionSpec.java` and `FunctionSpecBuilder.java`
(from `git show HEAD:<path>`), so only cycle D differs.

`xacml-core/src/test/java/org/xacml4j/v30/policy/function/FunctionInvocationCharacterizationTest.java`, 8 tests:
argument order of a fixed two-parameter eager function; an optional parameter's default; a variadic
parameter keeps every argument; two-argument calls of an optional and of a variadic function normalize
as before; a lazy two-parameter function gets its arguments unevaluated (the second throws if
evaluated); runtime validation rejects a wrong type before the call; an `EvaluationException` passes
through and other failures are wrapped; a wrapped failure keeps the status of a nested
`EvaluationException` (syntax-error) and is processing-error otherwise. Custom providers are covered by
the existing `AnnotationBasedFunctionFactoryTest` (static and instance providers, lazy evaluation with
the context).

Before D8: 8 of 8 pass, MatchTest 5 of 5 (characterization). After D8: the same, plus
`FunctionSpecBuilderTest` 11, `AnnotationBasedFunctionFactoryTest` 5, `ApplyTest` 3.

Output: `output.txt` (test lines of the before and after runs). The reactor and conformance run of the gate is separate.
