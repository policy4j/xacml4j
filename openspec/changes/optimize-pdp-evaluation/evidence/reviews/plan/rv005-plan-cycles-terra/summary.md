# rv005: plan review of the optimization cycles (task 2.3)

Date: 2026-10-27. Codex GPT-6.1 Terra (another family than the author's), read-only, after one
follow-up. Report: `report.md`. Verdict: REJECT, with required changes; all applied to the
artifacts the same day (design D5 to D12, spec `pdp-attribute-resolution`, tasks 4.4, 6.1 to 9.1,
research `synthesis.md` and `README.md`).

| Finding | Disposition |
| --- | --- |
| HIGH: an insertion-order bag array changes `Match` iteration (Guava groups equal values) and so decisions in error cases | D7 keeps today's grouped iteration order; task 6.1 adds interleaved duplicates and error-before-match cases |
| HIGH: cycle A promises one lookup without a same-context concurrency contract | D5 contract (concurrent map, first bag wins, no single-flight lock); spec requires equal bags, with instance and count as single-thread checks; task 4.4 adds race, PIP callback, and multi-decision tests |
| HIGH: cycle F removes context boundaries (public parent chain, cycle detection, §7.18 staging) | D10 narrowed: no context flattening; it moves to the backlog with a BREAKING note |
| MEDIUM: PIP absence then value breaks §7.3.5 | bug `policy4j-6vhd`; cycle A keeps today's behavior |
| MEDIUM: serialized form of `BagOfValues` | D7 marks cycle C BREAKING for Java serialization unless the field stays; maintainer decision before task 6.2 (design Open Question 1) |
| MEDIUM: cycle B zero-copy against public behavior | D6: public methods unchanged; private paths only (as implemented) |
| MEDIUM: cycle D eligibility for lazy fixed-arity functions | D8 eligibility rule; task 7.1 adds a lazy custom function |
| MEDIUM: cycle E ordering and same-context state safety | D9 lists what stays; task 8.1 extended |
| MEDIUM: JMH pairs do not isolate changes or cover `request=all` | D12: synthetic:1000 `all` plus the seven positions; snapshots differing only in the cycle's files, hashes in the summary |
| MEDIUM: research handoff (selector scope, PF6 figures, README) | synthesis corrected (selector memo to backlog; PF6 figures noted); research README lists the lanes |
