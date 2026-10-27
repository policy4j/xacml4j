# 017: one-fork smoke run of the shaded jar (task 3.7)

Date: 2026-10-27. Oracle GraalVM 21.0.6 with its default Graal JIT, one fork, no warmup, one
200 ms iteration, `-foe true`, default parameters (stores `in-memory`, `snapshot`; corpora
`rsa2008`, `synthetic:100`). Review lanes were alive (read-only); the machine was not quiet.

Purpose: integration, not measurement. Every benchmark set up, passed its decision and lookup
checks before and after the trial (setup and the new tear-down `recheck`), and produced a
score: 20 of 20 method and parameter sets, exit 0. `results.json` and `jmh.log` are the raw
output.

No score here supports a performance claim: no error interval, no allocation profiler, a
different JIT from the Benchmark Plan, and a busy machine. The baseline is task 4.2.
