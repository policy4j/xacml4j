# 008: fan-in functional smoke of the shaded jar (group 3 integration)

Date: 2026-10-26. JDK: Oracle GraalVM 21.0.6+8.1 (HotSpot). Sources: the spine
plus the lane files of Terra (benchmarks), Sol (`synthetic`, `dir` corpora),
and Astra (`snapshot` store); the tests lane was still running, so the jar was
built with `-Dmaven.test.skip=true`.

Purpose: integration, not measurement. One fork, no warmup, one 200 ms
iteration, `-foe true`. These scores are not a baseline and support no
performance claim (BENCHMARKS.md); the baseline is task 4.2.

Result: PASS. Every benchmark method with every default parameter set (20
results), and `PdpDecideBenchmark` on `synthetic:1`, `synthetic:2`, and
`synthetic:1000` with both stores (6 results), set up and ran without error.
Each setup opened a `PdpFixture`, so every store and corpus combination passed
the decision and lookup checks of design D5. All 26 scores are finite.

Signals to look at in the profiling round (single unwarmed iterations, so only
the order of magnitude means anything):

- `decide`: about 110 us per request on `rsa2008`, 380 us on `synthetic:100`,
  3 ms on `synthetic:1000`; the cost grows with the number of policy targets,
  about 3 us per target.
- `importXml`: about 4.8 ms for the nine `rsa2008` policy sets; the production
  import tool is built per source.
- `loadParsed` with `snapshot`: copy-on-write makes `synthetic:100` about 30
  times slower than `in-memory` (630 us against 19 us); this is a property of
  the store design (D10), not a defect.
- `lookup`: about 106 ns with `in-memory`, 19 ns with `snapshot`; the in-memory
  lookup filters with a stream and takes `Collections.max` on each call.

Files: `results-default.json`, `results-synthetic.json`, `score-table.txt`.
