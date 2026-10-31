FINAL REPORT (Codex Sol, GPT-6.1)

Overall: **REJECT — one P2 remains in cache-hit attribution.** The three original P2s and the corpus P3 are closed.

| Item | Verdict | Re-check |
|---|---|---|
| Decision-set precedence | APPROVE | Known keys check the decision set before the configured cache; otherwise the post-lookup check preserves dependency timing. |
| D34 cache-hit attribution | REJECT | The callback precedes selection of the winning set. |
| Expired-set replacement | APPROVE | Failed replacement retries against the current entry; no resolver runs in a map callback. |
| AttributeIndex bound | APPROVE | Positive entries use `(id, type, issuer)`; category cannot increase the map. |
| Corpus and README | APPROVE | `shared-static` exercises the memo without a configured cache; documentation explains the dependent case. |

**P2 — A losing cache candidate still emits `resolverCacheHit`.**  
[DefaultPolicyInformationPoint.java:193](`xacml-pdp/src/main/java/org/xacml4j/v30/spi/pip/DefaultPolicyInformationPoint.java:193`)

A caller can miss the decision memo and obtain cached profile A. Before it reaches `remember()`, another caller can install fresh profile B from the resolver. `remember()` correctly returns B, but the first caller has already emitted `resolverCacheHit` for A.

The resulting bag is correct; the public callback attributes it to a cache value that did not serve. This contradicts the requested condition and the [observer contract](`xacml-core/src/main/java/org/xacml4j/v30/spi/observe/DecisionObserver.java:182`).

Smallest fix: retain the cache candidate, call `remember()` first, and emit the callback only when the selected set is that candidate.

Test: coordinate a cached read with a sibling lookup that installs a different profile. Assert the winning attribute value and the public callback sequence, including no cache-hit event for the losing candidate.

The other closures are sound:

- The [replacement loop](`xacml-pdp/src/main/java/org/xacml4j/v30/spi/pip/DefaultPolicyInformationPoint.java:254`) returns a fresh winner containing the demanded member. Failed replacements imply another map change; with finite competing updates, retries converge. Missing members remain misses.
- The [lookup memo](`xacml-core/src/main/java/org/xacml4j/v30/AttributeIndex.java:288`) admits only positive lookups. For each stored `(id, type)`, entries are limited to the wildcard issuer and issuers present in the attributes. Publication remains sound. The handler selects category before Entity lookup.
- The [shared-static resolver](`xacml-jmh/src/main/java/org/xacml4j/jmh/corpus/PipWorkload.java:165`) has no context keys and returns both members. Its oracle and warm-cache coverage are present. Existing tests pin outcomes without allocation or call-count assertions.

Verification: inspected all nine patch files and merged source at base `87133237`. Scoped `git diff --check` and `ast-grep scan` passed. Ripwire quality comparison was **PARTIAL**: its HEAD baseline includes broader batch changes, so its findings cannot be attributed to FIX-30b.

No files changed; no builds, tests or JVMs run. Supplied green counts—core 6,745; PDP 360; conformance 938/12 skipped; JMH 202—were not rerun. The remaining interleaving is source-derived.
