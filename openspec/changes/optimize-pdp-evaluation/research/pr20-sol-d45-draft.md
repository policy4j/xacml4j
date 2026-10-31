FINAL REPORT (Codex Sol, GPT-6)

### D45. Shared expensive condition subexpressions within one decision

**Context.** `rules:100 repeated-conditions` repeats the same normalized-email expression 100 times; department literals differ, so whole-condition sharing misses the opportunity (`RulesCorpus.java:259`). The allocation model in `research/pr19-sol-condition-analysis.md` assigns approximately 176 B to each normalization pair. These are source estimates, not measured attribution. Regex binding belongs to `add-regex-engine-spi`, D6.

**Decision — eligibility.**

Use a reviewed **internal allow-list**, rather than a general purity declaration. Initially admit:

- Exact engine string literals and immutable string-bag literals, retaining type, value, order and multiplicity.
- Exact `AttributeDesignator` nodes: category, attribute id, type, issuer **and MustBePresent** participate in identity.
- Type-correct applies of `string-one-and-only`, `string-normalize-space` and `string-normalize-to-lower-case` over eligible operands.

The function proof must identify the actual engine method and default-provider construction, default invocation factory, engine class loader, standard signature and return-type handling, and absence of custom validators. A standard function id or `writesNoResponses()` alone proves too little. Sources: `FunctionSpecBuilder.java:305`, `DefaultFunctionInvocation.java:218`, `StringConversionFunctions.java:42,63`, `BagFunctions.java:143`.

Exclude selectors/content, variables, implicit time, randomness, custom expressions, functions, specs, factories and validators. Disable sharing while runtime parameter validation is enabled.

The first cut shares **request-backed values only**. At the demanded node, require the existing request-entity resolution contract and present singleton request bags containing exact engine string values. Probe only the request; never invoke a PIP to establish eligibility. Otherwise evaluate normally without consulting or populating the expression memo. Positive PIP-result sharing needs a stronger stable-read and side-effect contract.

**Identity and preparation.**

Prepare immutable condition sidecars in the D24/D25 generation. Keep the public AST, equality, visitors and serialized forms unchanged.

A structural key contains the actual binding/spec **by identity**, ordered child identities, typed literal contents and complete designator identity. `FunctionSpecImpl.equals()` compares function ids (`FunctionSpecBuilder.java:825`), so it must not identify bindings or canonical nodes. Condition lookup must likewise use owner identity rather than `Condition.equals()`.

Use an ordinary preparation-owned canonical map. Its domain is the prepared policy graph, and its lifetime follows that bundle/generation. Do not accumulate unmanaged policy roots in a permanent table. A global weak interner adds no useful sharing across generations and cannot release identities still referenced by active plans. Guava `Interners` is unnecessary.

Select maximal repeated expensive nodes: initially **at least four occurrences and at least two allocating string transformations**. Prefer larger qualifying subtrees; suppress nested memo entries beneath selected occurrences. This amortizes a map entry against several avoided normalization pairs. Bare designators and department `one-and-only` calls receive no entries.

**Evaluation and memo.**

Keep the original `Apply` dispatch, type-error gate, exception mapping and operand order (`Apply.java:268`). A prepared sidecar supplies memo-aware operands through that same dispatcher; it does not introduce a second function interpreter. Never rewrite operands handed to custom or unknown implementations.

Default `and`/`or` remain lazy, ordinary calls. Regex remains an ordinary call, including D6’s binding; only its eligible argument prefix may be shared.

The root owns the decision memo:

- Reuse its existing lazy snapshot-map field, with distinct expression-key and reference-key namespaces; add no root field or eager holder.
- Expression entries contain successful `ValueExp` results only. D24/D25 reference outcomes retain their existing rules.
- Create expression state only when a selected node is demanded and passes its guards.
- Evaluate outside map callbacks, then publish with `putIfAbsent` and return the successful winner. Retain the existing compare-and-exchange map publication.
- Preserve `isMissed`, reference replay and `hasMisses`: expression entries are not reference misses. Generations without sharing retain the existing reference-only fast path.

The narrow catalogue supplies the safety proof: singleton guards establish positive inputs, and every admitted function propagates operand errors. A successful memoized expression therefore neither consumes absence nor masks failure. No general taint tracker is needed.

On absence, wrong cardinality, unknown provenance, failure or `Error`, use the ordinary path and keep no expression result. Later occurrences retry normally. D26 retrieval and D27 flights remain unchanged. Concurrent successful computations may duplicate pure work; introduce no expression flight, waiting or resolver execution inside a map callback.

**Kept / soundness.**

- One stable positive request bag; no speculative attribute demand — §7.3.5.
- Existing type checks, operand order and errors — §§7.4, 7.19.
- Existing variable scope and caching — §7.8; variables remain outside D45.
- Existing lazy Boolean evaluation, string functions and bag/cardinality rules — A.3.5, A.3.10, A.3.13.
- Existing decisions, statuses, obligations and advice — §7.18.

A memo lookup occurs only where ordinary evaluation reaches that operand. Errors masked by surrounding `and`/`or` are evaluated and masked again normally; those enclosing expressions are never memoized. [XACML 3.0 specification](https://docs.oasis-open.org/xacml/3.0/errata01/os/xacml-3.0-core-spec-errata01-os-complete.html).

**Observation.** Disable preparation use and memo access whenever D34 observation is enabled. Preserve today’s callback sequence. No shared-value event is required in this cut.

**Alternatives rejected.** AST interning alone saves no evaluation; memoizing every node adds overhead; caching failures or empty-dependent successes suppresses retries; broad purity inference requires substantially more proof. General condition compilation is unnecessary for this scope.

**Costs and risks.** Require **zero additional B/op** on rsa2008 and synthetic when no candidates qualify: no additional root field, memo, per-call keys, arrays or captured lambdas. Preparation memory is separate.

For repeated-conditions, avoiding 99 normalization pairs removes approximately 17,424 B before memo overhead: **about 17 KB/decision expected, unmeasured**, subject to escape analysis and lookup costs. No defensible time forecast exists yet. Main risks are provenance mistakes, altered lazy dispatch, preparation retention, and accidentally treating expression entries as reference misses.

**Files / ownership.** Under `xacml-core`:

- New `policy/PreparedConditions.java`; `Condition.java:87`, `Apply.java:268`: sidecars and existing-dispatch integration.
- `PreparedPolicySets.java:61` and policy preparation hooks: coordinate with **Terra**; no `TargetIndex` edit proposed.
- `RootEvaluationContext.java:709`: memo namespace and reference-state preservation; coordinate with **Fable’s FIX-AN**.
- `FunctionSpecBuilder`, `FunctionProviderBuilder`, `DefaultFunctionInvocation`: provenance proof. Coordinate `Apply` changes with **Fable’s regex D6**.

**Test plan.** Pin decisions, statuses and response contents for repeated positive/negative strings; absent and multivalued inputs; transient absence/failure followed by success; lazy error masking; distinct MustBePresent/bindings; custom factories/validators; separate decisions and generations; and populated expression state followed by reference failure. Compare observed and unobserved outcomes and retain D34 sequence tests. No allocation, identity-token or call-count tests.

**Benchmark plan.** After regex D6 stabilizes, compare paired builds on `rules:1,100 repeated-conditions`, rsa2008 `all`, and synthetic:100/1000, index off/on. Use GC B/op, byte-weighted allocation and CPU profiles. Verify the no-candidate allocation requirement and report preparation cost separately. Gates: independent-family review, reactor/conformance, then the batch measurement.

Draft verified against source only. No files changed, builds, tests or measurements run.

**Open questions for the maintainer.**

1. Approve a preparation-only public provenance bridge, proposed `FunctionSpecBuilder.isShareableBuiltin(FunctionSpec)`, across the current package boundary? No general custom purity opt-in is proposed.
2. If sharing is later enabled with D34, should observers receive a shared-value callback or retain logical evaluation events?
3. Should a later extension admit positive PIP results under an explicit stable-read and side-effect contract?
