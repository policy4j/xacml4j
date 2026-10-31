# D37 / task 40.1 — decision-cache research survey

**Status:** Research only. This survey does not decide D37's design; task 40.2 remains for maintainer review and approval. Research fetched 2026-10-31. Local source observations are from PROJECT_ROOT at `759d90a6c75510d55127d8e22870f9edd985c991`.

## Literature and primary prior art

### ABAC attribute freshness

NIST SP 800-162, *Guide to Attribute Based Access Control (ABAC) Definition and Considerations* (January 2014; revision/update 2019), treats attribute caching as a latency optimization with a freshness/security tradeoff: a cached subject privilege can remain in force after its authoritative source changes. The Context Handler may cache attributes or retrieve them in advance, but local cached attributes require an explicit assessment of their security consequences. This supports a resolver-source freshness bound and invalidation contract; it does not prescribe a decision-cache key algorithm. [NIST SP 800-162](https://nvlpubs.nist.gov/nistpubs/specialpublications/nist.sp.800-162.pdf), fetched 2026-10-31.

### XACML decision caching and request projection

Ulltveit-Moe and Oleshchuk's peer-reviewed research article, “Decision-cache based XACML authorisation and anonymisation for XML documents,” *Computer Standards & Interfaces* 34(6), November 2012, pp. 527–534, describes an XACML obligation-service design that returns a specification to the PEP and caches finer-grained decisions for matching XML elements. It is prior art for decision reuse, but its document-element cache and obligation protocol are not a general PDP cache keyed by a static policy-read footprint. [ScienceDirect article](https://www.sciencedirect.com/science/article/pii/S0920548911001085), fetched 2026-10-31.

JBoss's archived XACML caching guide (feature available since JBoss Security 2.0.5; accessed 2026-10-31) gives an explicit manual projection example: exclude the standard `current-time` environment attribute from a decision key when policy does not use it. The guide warns that ignoring request elements can produce wrong decisions. That makes the core D37 idea established prior art, while also making automatic policy-dependency analysis the unsolved part here. [JBoss XACML Caching for Performance](https://developer.jboss.org/docs/DOC-15767), fetched 2026-10-31.

### Production XACML engines

- WSO2 Identity Server 5.9 documents separate policy, PIP-attribute, PDP-decision, and PEP-decision caches. It says decision caches need invalidation when policies, attribute caches, or global combining algorithms change; attribute invalidation is exposed for source changes and distributed across nodes. Its documentation describes a configurable decision-entry timeout and a concurrent-hash-map implementation. It does not document a key projected automatically from statically read policy attributes. [WSO2 IS 5.9 caching guide](https://is.docs.wso2.com/en/5.9.0/learn/improving-xacml-pdp-performance-with-caching-techniques), fetched 2026-10-31.
- AuthzForce's project documentation advertises a pluggable decision-cache extension for recurring XACML requests. The current README evidence establishes that a cache extension exists, but does not establish an automatic MayRead/projection key or its invalidation semantics; those details remain unverified from that source. [AuthzForce core repository](https://github.com/authzforce/core), accessed 2026-10-31 (the page is the moving repository README, not a pinned release).

### Volatile and dynamic inputs

OASIS XACML 3.0 Core, §10.2.5 and Appendix B.7, requires the context handler to supply `current-time`, `current-date`, and `current-dateTime` when the request omits them. The standard defines current time at request-context creation and requires repeated occurrences in one evaluation to use the same value. Thus excluding an *unused* environment attribute can improve reuse, but excluding a policy-read clock because it was absent from the serialized request is unsound. The generated value or a safe time interval must be represented, or the decision must bypass caching. [OASIS XACML 3.0 Core specification](https://docs.oasis-open.org/xacml/3.0/xacml-3.0-core-spec-os-en.html), fetched 2026-10-31.

`AttributeSelector` and XPath can read arbitrary content associated with an `Attributes` element, so category/id/type/issuer tuples alone do not identify selector results. Selector results can also depend on PIP-provided content. Similarly, custom functions, custom expressions, and dynamically resolved references can introduce reads that a simple walk of standard designators will miss. This is an inference from the XACML model and xacml4j's extension points, not a claim that the cited engines solve these cases. A conservative dependency analyzer therefore needs either declared read dependencies/content identity or a “not safely projectable” outcome.

The local source has a concrete dynamic-content path: `AttributeSelector` resolves an `AttributeSelectorKey` through the evaluation context (`xacml-core/src/main/java/org/xacml4j/v30/policy/AttributeSelector.java:73-84`); `Entity.resolve(AttributeSelectorKey)` delegates into its `Content` (`xacml-core/src/main/java/org/xacml4j/v30/types/Entity.java:236-238`); and `JsonContent.resolve` evaluates the selector path against JSON content, optionally rooted in the value selected by a context selector (`xacml-core/src/main/java/org/xacml4j/v30/content/JsonContent.java:147-175`). This makes request content and context-selector inputs part of the dependency problem, beyond ordinary attribute designator tuples. The general risk from custom functions remains an extension-point inference: a custom implementation can compute from data not represented by declarative designator nodes unless its contract declares those reads.

## Observations from xacml4j at the main-checkout baseline

### Current decision-cache request key

`PolicyDecisionCache` exposes `getDecision(RequestContext)` and `putDecision(RequestContext, Result, Duration)` (`xacml-pdp/src/main/java/org/xacml4j/v30/spi/pdp/PolicyDecisionCache.java:33-52`). `DefaultPolicyDecisionPoint` passes the whole request to the cache before evaluation and later stores the result with `evalContext.getDecisionCacheTTL()` (`xacml-pdp/src/main/java/org/xacml4j/v30/pdp/DefaultPolicyDecisionPoint.java:251-292`). The code does not create a projected key. The default implementation always misses (`NoCachePolicyDecisionCache.java:31-41`); any equality or projection for a custom implementation is left to that implementation.

`RequestContext` is immutable and precomputes a hash over `returnPolicyIdList`, `combinedDecision`, entities, request references, and request defaults (`xacml-core/src/main/java/org/xacml4j/v30/RequestContext.java:48-95`). It contains full request categories, not only those read by the active policy. The response also depends on request options and result attributes: `DefaultPolicyDecisionPoint` passes included attributes and the policy-id-list flag when it constructs `Result` (`.../DefaultPolicyDecisionPoint.java:274-291`). A projected key cannot omit any input that affects `Result`, even if the policy body does not read it.

### Current decision TTL path

`DefaultPolicyDecisionPoint` forwards one duration from `EvaluationContext` to `putDecision` (above). `RootEvaluationContext` initializes that duration from PDP configuration and keeps the minimum of successive values (`xacml-core/src/main/java/org/xacml4j/v30/policy/RootEvaluationContext.java:131-165,267-273,373-385`). The current resolver path does not call `setDecisionCacheTTL`; the TTL is not automatically derived from the descriptors that participated. D35's remote-result cache would add a source TTL and timestamp, but a decision cache must not restart that full nominal TTL when consuming an already-aged D35 value.

### Which D37 questions the sources answer

1. **Can irrelevant request attributes be omitted?** Yes, prior art explicitly does so manually, and the design goal is sound only when the omitted field is proven unread and does not affect returned response fields. JBoss's warning and XACML's generated current-time rule make a false omission a correctness defect.
2. **How should attribute TTL and security freshness interact?** NIST makes freshness a security policy decision; WSO2 separates resolver and decision caches and ties invalidation together. The sources support a conservative minimum remaining source TTL or bypass, but do not dictate a universal formula for this engine.
3. **What invalidation dimensions matter?** WSO2 documents policy, attribute-source/cache, and combining-algorithm invalidation. The engine also needs policy/repository generation invalidation and a connection from D35 resolver invalidation to D37 decisions.
4. **Does prior art solve selectors and dynamic reads?** The verified sources do not establish a general static projection algorithm for selectors/XPath, content, custom functions, or dynamic policy references. These remain hazards requiring conservative bypass or explicit dependency metadata.

## Survey conclusions and unresolved research limits

- A policy-read footprint is a more precise key than a whole request only if it is a conservative **MayRead** set over every expression that can affect the exact `Result`: targets, conditions, obligations/advice, selectors/content, references, and response options. A false positive lowers hit rate; a false negative can return a wrong authorization or response.
- Time should be irrelevant only when no reachable expression reads it. If current-time/date is read and not explicitly fixed in the request, it is an implicit context-handler input. A simple request-only key is insufficient.
- A D35 resolver's advertised TTL is not necessarily its remaining freshness at D37 insertion. An `AttributeSet` may already have spent part of that TTL in the resolver cache or D27 decision memo. The D37 TTL needs to use a remaining deadline, or set no decision entry; renewing the nominal duration can extend stale authorization.
- Cache invalidation is separate from expiry: policy/repository generation changes, source changes, resolver cache invalidation, and combining-configuration changes can invalidate entries earlier. A TTL bounds staleness but does not define immediate revocation behavior.
- Exact result caching must account for `ReturnPolicyIdList`, `IncludeInResult`, resolved attributes, obligations/advice, response status, and multi-decision request handling. The feature must decide whether every decision/status is eligible, especially Indeterminate and resolver failures.
- The targeted arXiv searches reached the arXiv MCP but returned no directly relevant cache-key projection paper. The primary sources above are a peer-reviewed XACML decision-cache paper, NIST ABAC guidance, the OASIS specification, and primary engine documentation/source. This survey does not claim exhaustive literature coverage.

## Questions left for task 40.2 and maintainer approval

1. Is the key a full request, a projected request, or both with a fallback? Which exact `RequestContext` options and response fields must participate?
2. How is the reachable policy tree and repository generation identified, including unpinned references and dynamic policy lookup?
3. What inputs disqualify projection: selectors/XPath, custom functions/expressions, content supplied by PIP, dynamic attributes, or extensions with undeclared reads? Can extensions declare dependencies safely?
4. How are implicit current-time/date values handled: include the request-context clock value, use a bounded time bucket, or bypass caching whenever read?
5. What is the exact decision TTL rule when no resolver participates, several resolvers participate, one is non-cacheable, or a D35 entry is already aged? Can an obligation or policy attribute cap the PDP default?
6. Which decisions, statuses, and full responses may be cached? Are Indeterminate results or any result containing resolver failures excluded? How do observer events/audit behave on a cache hit?
7. What invalidation API/event connects policy repository generation, external PIP source changes, D35 resolver entries, and D37 decisions? Is invalidation local or clustered, and what is the allowed revocation delay?
8. What is the key construction budget and measurement plan, including projected-key cost, cache hit rate, resolver source churn, response equality, and realistic changing-time requests?

No D37 design is proposed in this file; these answers belong in task 40.2 after the maintainer review.
