The limit checks hold. No request can make this handler build or iterate more than `maxExpandedRequests` individual requests, or more than `maxPathsPerCategory` paths for one selector. The round-1 empty-selection bug stays fixed.

No findings.

**1. Arithmetic.** A request cannot pass either cap. `getCategories` throws when `paths.size() > maxPathsPerCategory`, so one selector never contributes more than that many paths. The constructor rejects a limit below 1, so neither cap is zero or negative. After every category set is in hand, an empty set returns no result and does not build a product. Otherwise the running product is a `long`, multiplied once per category and compared with `>` after each multiply. A value that has already passed the check is at most `maxExpandedRequests` (`<= Integer.MAX_VALUE`). The next factor is at most `maxPathsPerCategory` (`<= Integer.MAX_VALUE`). That product is at most about 2^62, which still fits in a `long`, and it is then rejected when it is greater than the cap. The product that reaches `Sets.cartesianProduct` is therefore an `int` in range and at most `maxExpandedRequests`. Guava's size is that same product. At the cap is allowed; one above is rejected.

**2. Work before the check.** XPath evaluation and the per-category sets are built for every category before the product is checked. That cost tracks the request content, which is the bound given for `evaluateToNodePathList`, and each set is already capped by the path limit. The cross product is built only after both checks. The empty-set return happens before `Sets.cartesianProduct`, including when a later empty set would make Guava throw while multiplying earlier large sizes. No PDP call runs on either failure path.

**3. The Indeterminate result.** The client result is one `INDETERMINATE` with `processing-error` and the fixed message `Multiple decision request expansion limit exceeded`. Category id and counts stay in the WARN log, in `ExpansionLimitExceededException`'s message. `includeInResultAttr(request.getIncludeInResultAttributes())` is the same call the syntax-error path uses.

**4. Tests.** The new tests pin both defaults, 100 paths, 101 paths, product 10×10, product 11×11, 100^5 against `Integer.MAX_VALUE` (the wrap that would pass an `int` check), empty selection in both orders and beside 100^5, path limit beside an empty selection, configured limits, zero and the public chain, zero PDP interactions, and `IncludeInResult` attribute equality. A missing test: `new MultipleResourcesHandler()` with one selector of 101 nodes. The no-arg chain is what existing callers construct; nothing fails if that constructor stops passing the defaults.

**5. Repeated categories.** `MultipleResourcesViaRepeatingAttributesHandler` is a separate hole. This change does not widen it. The ruling and the new requirement are the XPath content-selector, and the spec text matches that scope.

VERDICT: APPROVE
grok exit=0
