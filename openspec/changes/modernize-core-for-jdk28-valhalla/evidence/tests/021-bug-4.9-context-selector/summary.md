# policy4j-the.4.9: ContextSelector cardinality and implicit context

## Change

`XmlContent` now applies an explicit `ContextSelectorId` XPath as a node set and requires exactly one selected node. Zero or multiple nodes throw `PathEvaluationException` with status `urn:oasis:names:tc:xacml:1.0:status:syntax-error`. With no `ContextSelectorId`, selector evaluation uses the XML Content document node; it no longer looks up the implicit `content-selector` attribute. This also fixes the `attribute-selector` function's Entity overload, which delegates to `Entity.resolve` and then `XmlContent`.

The XACML 3.0 Core specification section 7.3.7 requires Indeterminate/syntax-error for an explicit selector that returns zero or more than one node, and specifies the document node when `ContextSelectorId` is absent: [OASIS XACML 3.0 Core, Committee Specification 01](https://docs.oasis-open.org/xacml/3.0/xacml-3.0-core-spec-cos01-en.html).

## Tests and gates

The pre-fix red run of `DefaultEvaluationContextHandlerTest` failed the two cardinality cases because no `EvaluationException` was thrown (18 tests, 2 failures, 0 errors). The green focused run passed all 18 tests. `XmlContentTest` calls `AttributeDesignatorFunctions.selector` with an Entity argument and an XPath attribute present but no `ContextSelectorId`; the no-ID selector returns an empty bag from the document context rather than using that attribute.

The JDK 21 conformance reactor passed: core 763 tests, 0 failures, 0 errors, 2 skips; the conformance module 42 Surefire tests, 0 failures, 0 errors, 0 skips. Data-driven case totals were OASIS 2.0 **347**, OASIS 3.0 **347**, RSA 2008 **17**, examples **2**. No conformance case failed or changed its expected result, so no case-level failure classification was needed.

## Evidence and headers

Raw output is in `red.log`, `focused-green.log`, and `conformance-gate.log`. The focused and full conformance commands used Oracle Java 21.0.6, Maven 3.9.16, and `-Dlicense.skipUpdateLicense=true`.

The LGPL block bytes for every edited Java file were compared to the same file in the read-only `bugs2.base` copy. Blocks were identical. `ISO8601Duration.java` has no `%L` header in either copy; this task did not add or alter one.

## rv030 repair

`AttributeSelector.evaluate` now preserves a `PathEvaluationException` whose status is syntax-error before applying the MustBePresent empty-bag/missing-attribute behavior. The selector PIP catch path also rethrows syntax-error instead of returning an empty result. This keeps other exception handling unchanged. Tests cover zero and two explicit context nodes with MustBePresent both false and true, assert the exact syntax-error status ID, and verify no PIP call on the request-entity route. A separate test uses a PIP-supplied context entity and proves that its syntax-error also propagates. `MatchTest.testMatchPreservesContextSelectorSyntaxError` confirms the error becomes Indeterminate while retaining syntax-error status.

Test-first evidence: before the core fix, `rv030-red-core.log` had one failure in the Match test (NOMATCH instead of INDETERMINATE). Before the PDP catch fix, `rv030-red-pdp.log` had one failure because the `AttributeSelector.evaluate` call did not throw for the cardinality cases. With the PDP syntax-error rethrow temporarily removed, `rv030-red-pip-selector.log` had one failure because a PIP-sourced syntax-error became an empty bag. Final focused runs passed: Match/duration core set 15 tests, PDP context-handler class 19 tests before the additional PIP-route test; the final PDP gate includes all 20 context-handler tests.

The final JDK 21 gates passed: core 765 tests (0 failures/errors, 2 skips), XML 35 (0/0/0), JSON 6 (0 failures/errors, 1 existing skip), PDP 75 (0 failures/errors, 3 existing skips), test-support 20 (0/0/0), and conformance 42 (0/0/0). Data-driven totals were unchanged: OASIS 2.0 347, OASIS 3.0 347, RSA 2008 17, examples 2. No conformance case changed or failed.

Catch-path review: `AttributeReference.resolveImpl` was the core selector-path catch that converted generic errors to an empty bag or missing-attribute; it now rethrows syntax-error PathEvaluationException. `Match` and `Condition` first catch EvaluationException and preserve its status. PDP `doPipResolve` for selectors was the relevant broad catch and now preserves syntax-error; its separate designator resolver catch is not on this selector exception route. `MultipleResourcesViaXPathExpressionHandler` catches PathEvaluationException for request resource expansion and returns the category fallback; that is a separate request handler path, not AttributeSelector context-selector evaluation.

Residuals: a content-selector attribute containing multiple XPath values still resolves through the existing singular `findValue` behavior at `XmlContent.java:200`, so only its first XPath is evaluated. The expected XACML behavior for multiple XPath values has not been established here; this remains a gap. No fixture or expected response was changed.
