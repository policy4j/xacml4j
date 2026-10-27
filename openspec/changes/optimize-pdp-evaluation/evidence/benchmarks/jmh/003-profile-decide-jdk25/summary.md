# 003: CPU and allocation profiles of decide on JDK 25 (task 2.1, first round)

Date: 2026-10-27 06:29 to 06:31 UTC. Oracle GraalVM 25.0.4 with C2, one fork, 5 x 1 s warmup,
5 x 2 s measurement, one thread, store in-memory. Logging backend in the benchmark jar:
Log4j 2.14.0 at WARN (the jar was built before the Logback change of `upgrade-dependencies`).
Profiles are for stacks, not scores; the scores in `results.json` are not compared.

Per directory: `collapsed-<event>.csv.gz` (async-profiler collapsed stacks; read with
`gzip -dc`), `flame-<event>-forward.html` and `-reverse.html`, `top-<name>.md` (shares by self
frame, by total, and by the deepest project frame with its leaf frame or allocated type),
`results.json`, `jmh.log`.

CPU sampling used async-profiler's default interval: about 1,000 samples per profile, enough
for frames above about 2%; later rounds sample every 1 ms (`interval=1000000`).

First reading (conductor; the swarm analysis is task 2.1's next step):

- `RootEvaluationContext.<init>` creates its SLF4J logger per instance (an instance field,
  `RootEvaluationContext.java:66`), so every request calls `LoggerFactory.getLogger`; under
  Log4j's adapter that walks the stack (`StackFrameInfo`, `StackWalk::fill_in_frames`): 8.7% +
  1.7% of the bytes of `first-deny` and the largest group of CPU frames in that profile.
- Bag building (`BagOfValues$Builder.<init>`, `.attributes`, `BagOfValues.<init>`):
  `LinkedHashMap`, `HashMap$Node[]`, Guava `LinkedHashMultiset` and `ImmutableMultiset`, about
  19% of the bytes of the full scan (`alloc-synthetic_1000-all`).
- Stream pipelines on every attribute lookup (`AttributeIndex.findByIssuer`,
  `Attribute.getValuesByType`, `RequestContext.getEntity`): about 25% of the full-scan bytes.
- `Objects.hash` varargs arrays in `Value$OtherValue.hashCode` and
  `SemanticIdentifier.hashCode`; `LinkedList`s in `FunctionSpecImpl.normalize*`; a `HashMap`
  per policy context (`Policy$PolicyDescendantEvaluationContext.<init>`).
- CPU of the full scan: `Match.match` through Guava `ImmutableList` access (7.9%), stream
  pipelines, Guava `LocalCache` in `DefaultPolicyReferenceResolver.resolve`, and a Log4j
  filter frame under the context's `getEntity` (debug guard).

The logging frames change with the Logback switch; profile run 004 repeats this set on the
Logback build for the swarm.

Flame graphs (`flame-*.html`) are kept local and not committed: they are generated views of the committed `collapsed-*.csv.gz` stacks and can be rebuilt with async-profiler's converter.
