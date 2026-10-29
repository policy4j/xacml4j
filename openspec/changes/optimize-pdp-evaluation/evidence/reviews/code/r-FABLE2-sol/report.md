FINAL REPORT (Codex Sol, GPT-6.1)

Reviewed the merged main working tree against base `87133237` and `patch-fable-30fix-af.diff`.

**Verdicts:** FIX-30 **REJECT**; base cache provider **APPROVE**; AF bag storage and index-loop changes **APPROVE**; AF AttributeIndex memo **REJECT**.

**P2 — A configured-cache failure bypasses a valid decision snapshot.**  
[DefaultPolicyInformationPoint.java:140](`xacml-pdp/src/main/java/org/xacml4j/v30/spi/pip/DefaultPolicyInformationPoint.java:140`)

`cached()` calls `cache.getAttributes()` before checking the decision memo. For a context-key-free cacheable resolver, a complete unexpired profile may already contain the demanded peer attribute. If the configured cache then throws, resolution records a processing error instead of answering from that profile.

Fix: check `seen()` first when the key is already available; after a cache miss/lookup, check it again because that lookup may establish the dependency key. This preserves lazy dependency timing.

Test: resolve one profile member, make the configured cache unavailable, then assert that a second member still comes from the retained profile with unchanged status.

**P2 — Expired-set replacement can still return a losing profile.**  
[DefaultPolicyInformationPoint.java:209](`xacml-pdp/src/main/java/org/xacml4j/v30/spi/pip/DefaultPolicyInformationPoint.java:209`)

Two overlapping lookups can miss members of a retained set, load replacements, and both observe that set as expired. One `replace(key, kept, set)` succeeds; the other fails, but both callers return their own sets. Different demanded attributes can therefore come from different replacement profiles while a fresh authoritative set exists.

Fix: retry selection after a failed replacement and use the current unexpired winner when it contains the demanded attribute. No resolver belongs in a map callback.

Test: use a controlled clock and coordinated overlapping refreshes of an expired partial profile; assert that the returned attribute pair belongs to one winning replacement profile.

**P2 — The AttributeIndex memo has no structural bound.**  
[AttributeIndex.java:300](`xacml-core/src/main/java/org/xacml4j/v30/AttributeIndex.java:300`), [AttributeIndex.java:319](`xacml-core/src/main/java/org/xacml4j/v30/AttributeIndex.java:319`)

The map retains full designator keys, including category, while lookup deliberately ignores category. A long-lived immutable entity reused through changing policy categories can accumulate another key and bag for the same attribute/type/issuer lookup. `ConcurrentHashMap(attributes.keySet().size())` sets initial capacity, not a bound.

Fix: memoize by the actual lookup tuple `(attributeId, dataType, issuer)`, whose positive variants follow the stored attributes, or use a bounded existing cache. Preserve category-independent Entity lookup.

Keep outcome tests across categories and issuer/type variants. Establish the bound by construction; no allocation or cache-size test is needed.

**P3 — The shared-corpus description is now inaccurate.**  
[xacml-jmh/README.md:106](`xacml-jmh/README.md:106`)

Fable’s reported consequence is real: every workload resolver declares a context key. With `pipCache=off`, a new resolver context has no available key before invocation, so the shared peer is fetched again. The README still says it comes from the decision’s retained set.

Fix the description. Add a legitimate context-key-free shared case before claiming this parameter combination measures the whole-set memo.

The original findings are closed as follows:

- **Dependency timing: closed.** `isCacheKeyAvailable()` does not force dependency resolution; the empty-dependency retry remains.
- **Winning set: partly closed.** Fresh `putIfAbsent` losers return the winner; the expired replacement race above remains.
- **Base provider age/admission: closed.** Age now runs from timestamp to current time; both puts require a cacheable descriptor and an unexpired value. The inappropriate descriptor cast is removed. [BasePolicyInformationPointCacheProvider.java:57](`xacml-pdp/src/main/java/org/xacml4j/v30/spi/pip/BasePolicyInformationPointCacheProvider.java:57`), [BasePolicyInformationPointCacheProvider.java:95](`xacml-pdp/src/main/java/org/xacml4j/v30/spi/pip/BasePolicyInformationPointCacheProvider.java:95`).

AF otherwise preserves typed-storage equality, multiset hashes, duplicate counts, scans, NaN/signed-zero behavior and the persistent serialization fields. Builder snapshots remain immutable. The single index loop preserves validation before mutation. The memo’s volatile/compare-and-exchange publication and per-key winning publication are sound. Category omission is intentional; the handler first selects the request category, then resolves the designator. Positive publication and missing-attribute fallback remain intact. Existing compatibility and outcome tests are pragmatic.

Verification: source, patch and report inspection; scoped `git diff --check` and `ast-grep scan` passed. Ripwire impact output was name-based and **PARTIAL**, not semantic proof. No files changed or JVMs/tests run. Supplied green counts—core 6,744; PDP 350; conformance 938/12 skipped; JMH 202—are retained evidence. The counterexamples above are source-derived and not covered by fresh execution.
