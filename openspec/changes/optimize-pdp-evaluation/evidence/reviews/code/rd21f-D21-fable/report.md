# rd21f-D21-fable (Claude Fable 5.1, NTM worktree lane, read-only)

**Lane cc_2 (Claude Fable 5.1), code review of D21 (Cycle O, canonical designator keys, Codex Terra), read-only, 2026-10-27.** Read: `merge-D21.diff` (182 lines: `AttributeDesignatorKey.java`, new `AttributeDesignatorKeyTest.java`), the pre-change `AttributeDesignatorKey.java`, D21 (`design.md:472-485`), and every production site that builds a key. Terra's counts (xacml-core 6,631, conformance 938/12) taken as reported; no build run.

**Verdict: APPROVE WITH CHANGES.** The interner is correct and keeps hash, equality and the category-less bypass; one evaluation-path site now takes the global monitor, against D21's own sentence.

**Findings**

P2-1. The monitor is taken on the decide path. `DefaultEvaluationContextHandler.doPipResolve(AttributeSelectorKey)` builds a designator key for the context selector through the builder (`DefaultEvaluationContextHandler.java:277-283`), so with `build()` now interning (`AttributeDesignatorKey.java:179` of the patch) every XPath selector with a `ContextSelectorId` locks `INTERN_LOCK` during evaluation. D21 says "Evaluation never takes the monitor" (`design.md:482`). Not on the JMH corpora, but a contention point under `-t 4` for that profile. Fix: build that key non-canonically, `new AttributeDesignatorKey(AttributeDesignatorKey.builder()...)`, the constructor D21 keeps for that purpose; equality falls back. The other builder sites are load time: the XML mappers (`Xacml30PolicyFromJaxbToObjectModelMapper.java:583`, `Xacml20...:722`), the resolver descriptors (`AttributeResolverDescriptor.java:242,256`, `AnnotatedResolverFactory.java:205`).

P3-2. One comment. The pool has no "design D21" line; the house style wants the why where it is not obvious (why a `WeakReference` value for a key the map already holds weakly).

**Checks**

- Atomicity: lookup, dereference and insert under one monitor (`intern`, patch lines 156-169); `WeakHashMap` is only touched inside it. Correct.
- Weak reachability, no global retention: the map's key is weak and the value is a `WeakReference` to the same object, so a key referenced by nothing but the pool is collected and its entry expunged on the next access; a cleared referent (`reference.get() == null`) is replaced. Correct. Keys of a removed policy leave the pool with the policy.
- Hash and equality: `hashCode` (`Objects.hash(attributeId, issuer)`, pre-change lines 89-95) and `equals` (97-110, identity first, then category, type, id, issuer) are untouched; a canonical and a constructor-built key are equal both ways (test `builderAndConstructorPreserveValuesAndHash`). The pool hashes by the key's own hash, so keys that differ only by category or type share a bucket chain; load-time cost only.
- Category-less bypass: `equals` dereferences `category` (line 107), so a key without one cannot be a map key; `intern` returns it unpooled (patch 157-159). Correct and necessary.
- Builder paths: `build()`, `toBuilder().build()`, `withIssuer()` intern; the public constructor stays non-canonical, as D21 says. Deserialized keys are non-canonical too (no `readResolve`), consistent with "canonical identity is not a public guarantee".
- Tests, pragmatic: `builderAndConstructorPreserveValuesAndHash` (values, both-way equality, the hash formula), `eachKeyFieldSeparatesValues`, `emptyIssuerRemainsAbsent`, `keysWithoutCategoryKeepTheirValues` (the bypass keeps values and hash), `builderStillValidatesRequiredFields`. Nothing tests identity or the pool's mechanics, which is right. Keep all five.
