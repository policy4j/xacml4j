# Discovery count decision

Status: PENDING conductor acceptance of the engine count difference. The literal packet threshold of at least 762 reported tests with four skips is not met by Jupiter's 760 reported cases and two skips. The test run itself is green.

`19-vintage-count-characterization-receipt.json` records the exact task-start sources replayed on JDK 21: 762 tests, 0 failures, 96 errors, 4 skips. Its testcase classnames identify both extra entries:

| Vintage report | Actual testcase class | Method name | Reason |
| --- | --- | --- | --- |
| BagFunctionsTest | TestFunctions | empty | original class-level @Ignore; no test methods |
| DefaultFunctionSpecTest | TestInstanceFunctions | empty | original class-level @Ignore; no test methods |

Both helper classes retain the equivalent bare `@Disabled` annotation. Jupiter does not discover them as test classes because they contain no test methods. The two actual disabled methods, ContentTest.testXmlBase64 and Xacml20XPathTo30TransformerTest.testXPathTransformationWithFunctions, remain disabled. All four original @Ignore annotations had no supplied reason string; that absence is preserved. The latter method still carries its existing FIXME about implementing transformation.

Source audit: 550 declarations, same method names/order per class; runtime audit: the same 760 named invocations, with the same parameterized cardinality and skip state. Both suites contain 758 executable cases. No assertion or declared test was deleted to change the count. No dummy test, artificial skip, fixture edit or exclusion was added.

Focused observations: with only the two ignored empty helpers selected, Vintage reports zero cases (run 15). With BagFunctionsTest plus the helpers selected, its report has six named tests plus one empty helper skip (run 16). The full replay (run 19) provides both actual classnames. This is reporting/discovery bookkeeping, not lost test coverage.

The async conductor question requests acceptance of the documented 760 total. No answer is assumed from elapsed time. Once accepted, run 47 is the task-2.3 test gate; until then the numeric acceptance condition remains pending.
