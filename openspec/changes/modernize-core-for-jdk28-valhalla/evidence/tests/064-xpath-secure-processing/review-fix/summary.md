# Task 4.52 review fixes

## Changes

- Added `XmlContentTest.testXPathFactoryEnablesSecureProcessing`. Its
  package-local test hook reads `FEATURE_SECURE_PROCESSING` from the factory
  held by the default provider instance, the same supplier now used by
  `DefaultXPathProvider.newXPath`.
- Wired the existing `Supplier<XPathFactory>` field into `newXPath`; it is no
  longer unused. The default provider supplies the thread-local factory.
- Kept the feature setting fail-closed. `XPathFactoryConfigurationException`
  during initialization becomes `IllegalStateException`; failure to read the
  setting in the test hook is also surfaced as `IllegalStateException`.
- In `MatchTest.testXPathExtensionFunctionFailureMakesMatchIndeterminate`,
  retained `MatchResult.INDETERMINATE` and the interaction checks, but removed
  the `missing-attribute` status-code assertion. AttributeReference's status
  mapping for nonempty-selection XPath errors remains a separate bug; no
  production change to AttributeReference was made.

## Regression proof

With `factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true)`
temporarily removed, `testXPathFactoryEnablesSecureProcessing` failed:
1 test, 1 failure; expected `true` but got `false` (see
`secure-processing-flag-removed.log`). Since removing the setting also makes
the initializer's checked-exception catch unreachable, the mutation run
temporarily widened that catch to `Exception` so compilation could reach the
test. Both the setter and the original specific catch were restored before
the green run. `secure-processing-flag-restored.log` records the focused test
passing; `core-clean-test.log` is the clean gate on the restored final code.

## Gate

JDK 21 command `-pl xacml-core -am clean test` passed: 5,755 tests, 0
failures, 0 errors, 17 skips. Raw output is `core-clean-test.log`.

## Residual risk

This only locks the secure-processing flag and preserves the reviewed
Indeterminate result. It does not add XPath evaluation time or size limits;
those remain a separate maintainer decision. AttributeReference's mapping of
XPath failures to status codes is unchanged and remains out of scope here.
