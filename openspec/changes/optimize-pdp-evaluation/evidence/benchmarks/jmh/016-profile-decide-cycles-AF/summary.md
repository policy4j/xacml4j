# 016: second profiling round after cycles A to F (task 11.1)

Session 4, 2026-10-27 09:54 to 10:16 UTC: Oracle GraalVM 25.0.4 with C2 (`-XX:-UseJVMCICompiler`), `-Xms2g -Xmx2g -XX:+UseG1GC`, 3 forks x 5 iterations, `-prof gc`; no agent session of this project alive; load average 7 to 11 on 12 cores from other processes. Jars from frozen tree copies (sha256 in `env.txt`). A difference is claimed only when the 99.9% intervals are disjoint.

Same set as run 004: cpu and alloc for rsa2008 (all requests), synthetic:1000 (all), synthetic:1000
(first-deny), on the final tree. Files per set: `collapsed-*.csv.gz`, forward and reverse flame graphs,
`top-*.md` (30 frames by self and by total samples), `results.json`, `jmh.log`.

## Allocation of a full scan (synthetic:1000, all), by leaf class

`LinkedHashMap` 21.1%, `Object[]` 15.6%, `LinkedHashMultiset` 10.5%, `Policy$PolicyDescendantEvaluationContext`
10.4%, `BagOfValues` 7.9%, `Iterators$9` 7.8%, `ArrayList` 7.7%, `PolicyIDReference$PolicyIDReferenceEvaluationContext`
7.6%, `AbstractMultiset$EntrySet` 5.2%, `Optional` 5.2%.

By project caller (collapsed stacks):

- `ValueType.emptyBag()` from `AttributeReference.evaluate`: 44.5% of all sampled bytes. `evaluate` ends with
  `v.orElse(getDataType().emptyBag())`; `orElse` evaluates its argument, so a new empty bag (a `LinkedHashMap`,
  a `LinkedHashMultiset`, a `BagOfValues`) is built on every evaluation, also when the attribute is present.
  `BagOfValuesType.emptyBag()` carries a TODO to cache it. New cycle H (design D14).
- The two-argument call path: `FunctionSpecImpl.invoke` 7.8% (the two-element list) and
  `DefaultFunctionInvocation.invoke` 7.6% (the `Object[]` of the reflective call).
- Per-policy contexts: `PolicyDescendantEvaluationContext` 10.4% and `PolicyIDReferenceEvaluationContext` 7.6%
  (design D10: their chain is public; a separate decision).
- `Iterators$9` and `AbstractMultiset$EntrySet`: iteration of the multiset bag in `Match.match` (cycle C).

Flame graphs (`flame-*.html`) are kept local and not committed: they are generated views of the committed `collapsed-*.csv.gz` stacks and can be rebuilt with async-profiler's converter.
