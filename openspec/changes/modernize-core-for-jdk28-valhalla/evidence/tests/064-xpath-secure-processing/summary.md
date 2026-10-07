# XPath secure processing (task 4.52)

## Change

`XPathProvider` is the only production site creating an `XPathFactory` in
xacml-core, xacml-pdp, xacml-xml, and xacml-json. Its thread-local factory now
sets `XMLConstants.FEATURE_SECURE_PROCESSING` to `true`. If the JAXP provider
does not support the feature, initialization fails closed with an
`IllegalStateException`. Evaluators are created from this secured factory.
No `XPathFunctionResolver` or `XPathVariableResolver` is installed; extension
functions remain unavailable. The two other `XPathFactory.newInstance()` hits
are test-only helpers (`XPathTest` and `DOMUtilTest`). Search commands and
their scope are in `command.txt`.

`XmlContentTest` checks that a Java/Xalan namespace extension-function
expression raises the engine's XPath evaluation exception with processing
error. `MatchTest` checks that the same request-origin expression is mapped
through `Match.evaluate` to `MatchResult.INDETERMINATE` with missing-attribute
status.

## Test-first observations

Both pre-change logs are green on this JDK/provider: the runtime's default
XPath implementation already rejects the chosen Java/Xalan extension call.
Therefore these runs did not demonstrate a red-to-green transition. The
explicit secure-processing setting is still necessary to make the security
policy deliberate and fail closed instead of relying on provider defaults.
After the change, the focused tests and the full xacml-core suite pass.

## Verification

| Run | Result |
| --- | --- |
| Pre-change `XmlContentTest` (`extension-red.log`) | 16 tests, 0 failures/errors; extension rejected by the default provider |
| Pre-change `XmlContentTest,MatchTest` (`extension-indeterminate-red.log`) | 20 tests, 0 failures/errors; extension rejection mapped to Indeterminate |
| Post-change `XmlContentTest,MatchTest,XPathFunctionsTest` (`focused-green.log`) | 26 tests, 0 failures/errors/skips |
| `-pl xacml-core,xacml-pdp -am clean test` (`core-pdp-clean-test.log`) | xacml-core: 4,987 tests, 0 failures/errors, 17 skips. Reactor stopped in xacml-xml: 5 failures in `BaseJAXBUnmarshallerSecurityTest.programmaticDomAttributesStillUseStrictBooleanParsing`; xacml-pdp was not reached. |
| Full reactor `verify` (`full-reactor-verify.log`) | Same 5 xacml-xml failures stopped the reactor before conformance tests. |
| Focused conformance classes (`conformance-focused.log`) | OASIS 2.0: 375 tests, 0 failures/errors, 10 skips; OASIS 3.0: 488 tests, 0 failures/errors, 7 skips; RSA: 17 pass; examples: 2 pass. Manifest and static-rejection tests also pass (13 and 16 respectively). |

The five xacml-xml failures report `NullPointerException` from
`org.xml.sax.helpers.AttributesImpl.getValue` while
`BooleanAttributeValidationFilter.validate` runs; the test expects
`SyntaxException`. The changed files are confined to xacml-core, and no
xacml-xml source or test was edited. The complete requested core+pdp gate and
full reactor gate remain unverified because the reactor exits at those
failures. The focused conformance run is supplementary and does not replace
the full reactor gate.

## Limits

No XPath size or time limits were added. If approved later, evaluation limits
belong around the `XPathExpression.evaluate` calls in `XPathProvider` (the
shared request-content XPath boundary), with values and failure semantics
decided by maintainers. Secure processing itself does not establish a
project-specific evaluation timeout or expression/result-size budget.

## Lane readiness

Implementation and the focused conformance suites pass. The lane is not
fully gate-ready until the xacml-xml failures are resolved or independently
adjudicated and the complete reactor gates run successfully.
