## Why

PDP decisions have no measured performance targets, and the decide path does
work that a decision does not need. The maintainer asked (2026-10-26) to
record every JMH run, find the bottlenecks and allocation problems of PDP
evaluation, and optimize until throughput gains are significant, and first to
remove random number generation from the decide path. Each `decide` call
draws from two shared static `java.util.Random` instances
(`xacml-pdp/src/main/java/org/xacml4j/v30/pdp/PolicyDecisionPointContextFactory.java:68,111`,
`xacml-pdp/src/main/java/org/xacml4j/v30/pdp/MDCSupport.java:49,56`). Every
draw is a compare-and-set loop on one shared seed, so concurrent decisions
contend on it, and the identifiers are not reproducible in tests.

## What Changes

- Group 1 (requested first): the correlation id of a `decide` call comes from
  a per-PDP sequence with a prefix taken once when the PDP is built; the
  request id of each individual decision is `<correlation id>-<n>`, numbered
  from 1 within one `decide` call. No random number generator runs on the
  decide path. An ast-grep rule keeps random sources out of the PDP package.
- **BREAKING** (pre-release 2.0.0 line): `MDCSupport.setXacmlRequestId(String,
  RequestContext)` becomes `MDCSupport.setXacmlRequestId(String, int)`. The
  old form used only the first argument plus a random suffix. No code outside
  `DefaultPolicyDecisionPoint` calls it (`rtk rg`, 2026-10-26).
- Later groups: one group per measured bottleneck, added to this change after
  each profiling round of the `xacml-jmh` harness
  (`openspec/changes/add-pdp-jmh-benchmarks`). Each later group states its
  hypothesis, its profile evidence, and its before/after JMH runs before it is
  implemented, and is validated and reviewed like group 1.
- Every JMH run of this change, including profiling runs, is recorded in its
  own evidence directory, with a running ledger of the numbers.
- Standing rules of the maintainer (2026-10-26) for the later groups: keep
  running optimization cycles until the stop rule in Verification holds; on
  the decide, load, and import paths, replace stream pipelines with plain
  loops, and replace Guava collections with JDK collections, the latest
  Eclipse Collections release, or small custom implementations where a
  profile shows their cost. The main sources hold 78 stream sites (60 in
  `xacml-core`, 10 in `xacml-pdp`, 8 in `xacml-xml`) and 101 imports of
  `com.google.common.collect` (66, 18, 12, and 5 in `xacml-json`)
  (`rtk rg`, 2026-10-26). A replacement keeps the public API types unless the
  group marks a **BREAKING** change; Eclipse Collections is added as a
  dependency only by the first group that measures a gain from it.

No XACML decision changes: the OASIS and RSA conformance results must stay the
same.

## Capabilities

### New Capabilities

- `pdp-decision-correlation`: how the PDP names a `decide` call and its
  individual decisions in the logging context, without random numbers.

### Modified Capabilities

None. Later groups add their own capability or requirement deltas.

## Research Questions

None for group 1. Later groups record their questions in
`research/README.md` before their design is written, for example: target
indexing and decision-diagram evaluation from the XACML engine literature;
the current Eclipse Collections release, its license, and its JDK 21
support.

## Verification

- Group 1: `CorrelationIdTest` in `xacml-pdp` (fails before the change);
  the ast-grep rule `java-no-random-on-decide-path` with its rule test; the
  full reactor `clean test` on JDK 21 (8,009 tests, 0 failures, 18 skipped
  before) with conformance results unchanged; a before/after
  `PdpDecideBenchmark` run once the harness module is built (baseline built
  from the unchanged sources, back to back on one machine, `-prof gc`).
- Target for the whole change (2026-10-26): decide time at least 25% lower
  (about 1.33 times the throughput) on the `rsa2008` and `synthetic:1000`
  corpora with non-overlapping error intervals, and no increase of
  allocation per decision. The maintainer asked to continue past the target
  "till everything is optimized", so the cycles stop only when the profile
  of `PdpDecideBenchmark` has no frame of the project's own code above 5% of
  time that a change can remove without changing a decision. Tightened on
  2026-10-27:
  - Allocation "to the limits": every own-code allocation site on the decide
    path, at any share, is a target; the cycles stop for allocation only
    when each remaining allocation is required by the public API (the
    returned `ResponseContext`, `Result`, and their contents) or a
    measurement shows that removing it costs time.
  - "Do not stop till everything is optimized, if no progress run research
    swarms for potential optimization hypotheses": a round that gains less
    than 5% does not end the change. It triggers a research swarm (NTM, the
    model lanes of the profiling swarm, with arXiv, other XACML engines, and
    JVM performance sources) that proposes new hypotheses with expected
    gains; the cycles continue with those. The change ends only when a
    research swarm finds no hypothesis that a measurement has not already
    refuted.
  - Every optimization cycle has a JMH before/after pair (maintainer,
    2026-10-27). From change `adopt-jdk25-baseline` on, the pairs run on JDK
    25 (Oracle GraalVM 25 with C2).
  The final report names what is left and why.

## Impact

- Code: `xacml-pdp` (`PolicyDecisionPointContextFactory`,
  `DefaultPolicyDecisionPoint`, `MDCSupport`) for group 1; later groups name
  their files.
- Logs: the `XacmlRequestId` MDC value changes form from two random hex
  numbers to `<prefix>-<sequence>-<n>`.
- Tooling: one ast-grep rule and its test in `tools/ast-grep/`.
- Depends on `add-pdp-jmh-benchmarks` for every JMH run.
- Approval: the maintainer asked for this work on 2026-10-26 ("optimize code
  base till significant throughput results are achieved"; "Remove randoms
  from main code path of decide").

## Tracking

Beads epic: see `tasks.md` header. Task X.Y maps to issue `<epic>.X.Y`.
