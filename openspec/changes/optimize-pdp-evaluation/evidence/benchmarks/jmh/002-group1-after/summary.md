# 002: group 1 after (pair with 001)

Date: 2026-10-27 05:36 UTC, directly after 001, same machine, JDK, and flags.
Jar: the working tree (class references `AtomicLong`, not `Random`).

| Corpus (in-memory, `request=all`) | Before us/op | After us/op | Change | Intervals | B/op before | B/op after |
| --- | --- | --- | --- | --- | --- | --- |
| rsa2008 | 19.63 ± 0.72 | 20.38 ± 1.6 | +3.8% | overlap: no claim | 45,358 ± 590 | 45,789 ± 130 |
| synthetic:100 | 45.57 ± 2.1 | 43.87 ± 1.4 | -3.7% | overlap: no claim | 182,580 ± 2,700 | 180,900 ± 31 |

Result: no measurable time or allocation change. Group 1 removed the random
sources for determinism and contention, not for speed; the gate needs no
gain, only no measurable loss.
