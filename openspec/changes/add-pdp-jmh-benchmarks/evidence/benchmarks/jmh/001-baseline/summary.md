# 001: JMH baseline on JDK 21 (task 4.2)

Date: 2026-10-27, 05:36 to 05:59 UTC. Oracle GraalVM 21.0.6 with the C2 JIT
(`-XX:-UseJVMCICompiler`; `env.txt` shows `UseJVMCICompiler = false`), forks
with `-Xms2g -Xmx2g -XX:+UseG1GC`, JMH 1.37, 3 forks x 5 iterations, `-t 1`,
`-prof gc`, `-foe true`. Apple M2 Pro (8 performance, 4 efficiency cores), AC
power, no agent session of this project alive. Load average at start 7.5:
endpoint-security processes (outside the project) were busy; the per-fork
spread below stays within a few percent.

Completeness (check script): default 20 of 20 sets, scaling 14 of 14, every
score and error finite, 15 samples each. Full tables: `tables.md`; raw:
`results-*.json`, `jmh-*.log`.

## Decide (us/op; allocation per decision)

| Corpus | in-memory | snapshot | B/op |
| --- | --- | --- | --- |
| rsa2008, `request=all` | 19.97 ± 0.45 | 19.89 ± 0.95 | about 45,700 |
| synthetic:100, `request=all` | 44.92 ± 2.1 | 43.92 ± 1.4 | about 181,000 |

Per request, store `in-memory`:

| Request | synthetic:10 us | B/op | synthetic:1000 us | B/op |
| --- | --- | --- | --- | --- |
| first-deny (stops at policy 0) | 6.86 ± 0.25 | 16,499 | 7.21 ± 0.12 | 16,725 |
| middle-deny | 9.43 ± 0.26 | 26,915 | 260.8 ± 7.8 | 1,136,100 |
| last-permit (full scan) | 11.66 ± 0.45 | 35,542 | 509.2 ± 25 | 2,247,700 |
| unknown-notapplicable (full scan) | 10.06 ± 0.32 | 30,734 | 492 ± 21 | 2,189,400 |

Reading: the fixed cost of one decision is about 7 us and 16.5 KB
(first-deny is flat from 10 to 1,000 policies). Each policy whose target is
checked and does not match adds about 0.48 us and 2.2 KB
((492 - 7.2) us / 1,000 and (2,189,400 - 16,725) B / 1,000). The allocation
per non-matching target is the first profiling target.

## Store lookup and load

| Benchmark | Corpus | in-memory | snapshot |
| --- | --- | --- | --- |
| `lookup` ns/op (B/op) | rsa2008 | 31.4 ± 0.95 (164) | 15.3 ± 0.61 (0) |
| `lookup` ns/op (B/op) | synthetic:100 | 39.9 ± 1.3 (187) | 18.2 ± 0.56 (0) |
| `cursor` control ns/op | both | 0.77 to 0.79 | 0.77 to 0.79 |
| `loadParsed` us/op | rsa2008 | 2.62 ± 0.05 | 2.87 ± 0.08 |
| `loadParsed` us/op | synthetic:100 | 9.84 ± 0.49 | 203.6 ± 5.8 |
| `importXml` us/op (B/op) | rsa2008 | 812 ± 30 (1.17 MB) | 819 ± 32 (1.16 MB) |
| `importXml` us/op (B/op) | synthetic:100 | 7,457 ± 210 (11.1 MB) | 7,585 ± 250 (11.6 MB) |

## What one operation includes

- `decide`: one `PolicyDecisionPoint.decide` of a pre-parsed request,
  including result building; the policy references are resolved from the
  PDP's reference cache after the first call (design D7), so store choice
  does not show here.
- `lookup`: one store call that the corpus implies; `cursor` is the cost of
  choosing the next query (subtract it).
- `loadParsed`: a store session that adds the pre-parsed policies; `importXml`
  also parses every policy document (about 75 us and 111 KB per synthetic
  policy).

## Must not claim

No comparison with another engine or machine; no store difference from
`decide`; no per-policy cost from the `request=all` mix (use the scaling
table); no multi-thread behavior (`-t 1`); no JDK 25 behavior (the JDK 25
baseline is `002-baseline-jdk25` of change `adopt-jdk25-baseline`).
