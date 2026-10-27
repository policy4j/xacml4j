# 004: decide profiles on JDK 25 with the Logback build (task 2.1)

Date: 2026-10-27 06:39 to 06:41 UTC. Same set and flags as run 003, on the jar built after
the Logback switch of `upgrade-dependencies` (Logback 1.3.16 stopgap, WARN, async console);
CPU sampled every 1 ms (about 10,000 samples per CPU profile; run 003 had about 1,000).
This is the profile set of the first swarm analysis.

Change against run 003: the per-request `LoggerFactory.getLogger` stack walk of the Log4j
adapter (`StackFrameInfo`, `StackWalk::fill_in_frames`) is gone from the top frames: Logback's
`getLogger` is a map lookup. The instance-field logger of `RootEvaluationContext` stays a
per-request lookup and is still a hypothesis.

Top project frames (deepest project frame -> leaf; `top-<name>.md` has 30 rows each):

- Fixed cost (`alloc-synthetic_1000-first-deny`): stream pipeline objects in
  `AttributeIndex.findByIssuer` (about 13% of the sampled bytes together), `Object[]` of
  `Objects.hash` in `SemanticIdentifier.hashCode` (3.3%), `LinkedHashMap` and `HashMap$Node[]` in
  `BagOfValues$Builder` (5.0%), `ConcurrentHashMap`s of `RootEvaluationContext.<init>` (2.7%).
- Fixed cost CPU (`cpu-synthetic_1000-first-deny`): `Match.match` through Guava
  `ImmutableList` element access (5.7%), `DefaultPolicyDecisionPoint.getResolvedAttributes`
  iteration (3.1%), stream setup in `Attribute.getValuesByType` and `RequestContext.getEntity`.
- Full scan CPU (`cpu-synthetic_1000-all`): `Match.match` (10.7%) and its
  `Arrays$ArrayList` creation (1.8%), `Attribute.getValuesByType` streams, `BagOfValues.<init>`
  Guava `ImmutableMultiset` creation (2.3%), `DescendantEvaluationContext.resolve` (2.0%), the
  Guava `LocalCache` lookup in `DefaultPolicyReferenceResolver.resolve` (2.0%).

Scores in `results.json` come from profiled runs and are not compared with anything.

Flame graphs (`flame-*.html`) are kept local and not committed: they are generated views of the committed `collapsed-*.csv.gz` stacks and can be rebuilt with async-profiler's converter.
