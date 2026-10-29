# Evidence

| Requirement | Tests | Tasks |
| --- | --- | --- |
| Suite descriptors and case kinds | `SuiteDescriptorTest` | 2.2 |
| One runner | `ConformanceSuiteTest` | 2.3, 2.6 |
| Same outcomes and fields after the move | `RunnerEquivalenceTest` (frozen record, controls, override controls) | 2.1, 2.3, 2.4, 2.6, 2.7 |
| Lossless comparison | `ResponseComparatorTest` (XML, legacy modes), `JsonResponseComparatorTest` | 2.4, 2.7, 5.1 |
| Structured special handling | `ConformanceSuiteTest`, ported guard tests | 2.3 |
| Table-driven attribute resolution | `TablePipTest` | 2.3 |
| JSON vectors | `ConformanceSuiteTest` (end to end, validation controls) | 5.1, 5.2 |
| Provenance per suite | `VendoredFixtureIntegrityTest`, `SuiteDescriptorTest` | 3.1, 3.2 |
| Adoption of a new source | `VectorAdoptionCheckTest` (duplicates, provenance completeness) | 2.5, 4.1, 5.2, 6.2, 7.1 |
| Corrections and conventions kept apart | `SuiteDescriptorTest` | 4.1, 5.2, 8.1 |
| Every adopted case is reviewed against the standard | `SuiteDescriptorTest` (review records) | 3.3, 4.1, 5.2, 6.2, 7.1 |

Gates with their own evidence: the RSA 2008 header removal (3.2: baseline ledger, 34-file change check,
34-entry ledger delta, same results); the 72 deprecated cases (3.3: inventory bijection, the attempt
outcome of every one of the 72 cases, then the disabled reason and bead of each case that stays failing).

## Runs and reviews

| Path | Kind | Command | Result |
| --- | --- | --- | --- |
| `../research/` | research swarm (1.1) | NTM lanes TV1 to TV4, read-only | four reports; answers in `research/README.md` |
| `reviews/plan/rvt1-terra` | plan review (1.2), Codex GPT-6.1 Terra | NTM pane, read-only packet | REJECT: 5 P1, 6 P2, 1 P3; revised (design: RVT1 dispositions) |
| `reviews/plan/rvt2-terra` | plan re-review (1.2), Codex GPT-6.1 Terra | NTM pane, read-only packet | REJECT: 4 P1, 6 P2, 1 P3; revised (design: RVT2 dispositions) |
| `reviews/plan/rvt3-terra` | plan re-review (1.2), Codex GPT-6.1 Terra | NTM pane, read-only packet | REJECT: 2 P1, 4 P2, 1 P3; revised (design: RVT3 dispositions) |
| `reviews/plan/rvt4-terra` | plan re-review (1.2), Codex GPT-6.1 Terra | NTM pane, read-only packet | REJECT: 1 P1, 5 P2; revised (design: RVT4 dispositions) |
| `reviews/plan/rvt5-terra` | plan re-review (1.2), Codex GPT-6.1 Terra | NTM pane, read-only packet | APPROVE WITH CHANGES: 1 P2, 1 P3; applied (design: RVT5 dispositions) |
