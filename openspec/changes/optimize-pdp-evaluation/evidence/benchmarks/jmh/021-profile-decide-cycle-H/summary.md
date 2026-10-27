# 021: profile of the cycle H tree

Date: 2026-10-27 10:38 to 10:40 UTC, directly after session 5; Oracle GraalVM 25.0.4 with C2; no agent session
alive. Same set as runs 004 and 016. Files per set: `collapsed-*.csv.gz`, `top-*.md`, `results.json`, `jmh.log`.
Flame graphs (`flame-*.html`) are kept local and not committed: they are generated views of the committed
`collapsed-*.csv.gz` stacks and can be rebuilt with async-profiler's converter.

## Allocation of a full scan (synthetic:1000, all), by leaf class

`Object[]` 28.4%, `Policy$PolicyDescendantEvaluationContext` 18.6%,
`PolicyIDReference$PolicyIDReferenceEvaluationContext` 14.2%, `Iterators$9` 13.9%, `ArrayList` 13.6%,
`Optional` 9.3%; the empty bags of run 016 (44.5%) are gone. Input of the second analysis swarm (task 11.1).
