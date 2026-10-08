One MEDIUM finding: an unresolved argument type disables checks for errors that are already knowable at policy load.

1. **MEDIUM — `xacml-core/src/main/java/org/xacml4j/v30/policy/Apply.java:97`**, before validation at line 100.

 Use the dynamic `attribute-designator` from `argumentWhoseTypeIsKnownOnlyAtEvaluationIsLeftToTheInvocation`, and call `integer-bag-size(designator, 99)`. Resolving the designator’s type throws because its data-type argument is an `Apply`, rather than an `AnyURI` literal. The catch returns `null` before checking cardinality.

 Consequently, this invalid two-argument call reports `isTypeCorrect() == true` and enters `spec.invoke` at line 185. Its extra argument is statically detectable regardless of the designator’s unknown type. This contradicts the new requirement to detect invalid argument counts and avoid invocation and argument evaluation.

 **Fix:** check cardinality independently of type resolution. Validate arguments whose types are known, and defer only checks that actually require an unresolved type.

Responses to the eight questions:

1. **False positives:** none demonstrated. The supplied valid cases cover empty variadics, function references, nested expressions, and the corrected designator/selector bag types. The unseen optional-parameter implementation does not support another confirmed finding.

2. **Value-producing false negative:** use that same dynamic designator, whose data-type expression evaluates to the string type, in `integer-bag-size(designator)`. Construction skips validation. With runtime validation off, the supplied wrong-bag-size behavior remains reachable: it counts the string bag, including returning zero for the empty bag used by the existing deferred-type test. Deferral therefore leaves an invalid typed invocation capable of producing a value. Closing this case requires validation of the resolved value type during evaluation; rerunning the same static resolver is insufficient.

3. **Status:** processing-error is supported for static type mismatches by the quoted requirement and IIC003/IIC014. Those cases do not establish the classification of every cardinality or extra-validator failure. A `SyntaxException` alone does not prove that its cause is invalid syntax. No separate status defect is conclusively established by this packet.

4. **Existing tests:** the edits preserve the exception assertions and add construction-check coverage. The higher-order tests correctly change their expected path to early failure. They consequently cease exercising those invalid calls through runtime invocation; separate invocation tests would retain that coverage.

5. **Request data:** none shown. Construction and its WARN occur during policy load. The exception message can contain policy data; the response message contains the function ID and fixed text.

6. **Unknown-type fallback:** legitimate deferral is necessary. Its scope is too broad because it also suppresses independent checks. Policy authors control the expression that causes deferral. Request values can affect its later evaluation, but requests do not enter this constructor.

7. **Tests:** the supplied mutations strongly pin detection and enforcement for the covered cases. A missing test is an unresolved-type argument combined with a known arity violation, asserting rejection during evaluation and zero calls to argument `evaluate`. Context interaction assertions alone do not establish that pure arguments were never evaluated.

8. **Spec:** the stored-error evaluation path and bag-type correction match it. The unconditional argument-count requirement does not match the early return identified above.

This is a diff-only review; the execution and mutation results are supplied evidence.

VERDICT: REQUEST_CHANGES
