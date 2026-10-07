1. **MEDIUM — A later empty selection causes an incorrect limit rejection.**  
   File: `xacml-pdp/src/main/java/org/xacml4j/v30/pdp/request/MultipleResourcesViaXPathExpressionHandler.java`, line **124**.

   With defaults, suppose categories are visited with selection sizes **10, 11, 0**. Every selector satisfies the path limit, and the complete Cartesian product contains **zero** individual requests. However, the running product reaches 110 before the empty category is inspected, producing a limit-exceeded Indeterminate. Visiting the empty category first avoids that rejection.

   **Fix:** Collect the per-category sets while enforcing their path limits. Detect any empty factor before checking the product; otherwise check the nonempty product incrementally before constructing the Cartesian view.  
   **Missing regression test:** Exercise both traversal orders, asserting zero PDP calls and no limit-exceeded result.

The remaining checks:

- **Arithmetic and bounds:** No demonstrated bypass. Before each multiplication, the running value is at most `maxExpandedRequests`, or zero. Both operands are bounded by positive `int` values, so their product fits in `long`. Zero remains zero. The path-count check precedes returning the expanded category set, and the product check precedes Cartesian construction and iteration.
- **Work before checking:** Lines **209–211** produce the complete XPath path list before the path limit at **212**. Also, `getCategories` completes at **119** before the product check. These per-category costs precede rejection; the supplied facts explicitly accept the initial XPath evaluation cost. Cartesian iteration and individual PDP calls occur after validation.
- **Response data:** The status message is fixed. The rejection copies the same `IncludeInResult` attributes as the syntax-error path; no dropped field is visible. The WARN message does contain the request-supplied category ID at **215**, so the fixed-message guarantee applies to the response, not the log.
- **Tests:** They cover 100/101 paths, products of 100/121, an above-`Integer.MAX_VALUE` product, configuration, positive constructor arguments, and zero PDP interactions on rejection. They omit the empty-factor case above. The preservation test checks attribute count only; comparing actual attributes would provide stronger evidence.
- **Repeated categories:** Their unbounded expansion is a separate, unchanged defect. This change establishes a bound for the XPath handler; it does not establish a bound across every handler in the public chain.

Verification is limited to the supplied diff and facts. The supplied reactor pass does not resolve the missing empty-factor case.

VERDICT: REQUEST_CHANGES
codex exit=0
