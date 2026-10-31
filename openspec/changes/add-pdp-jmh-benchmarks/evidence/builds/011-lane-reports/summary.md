# 011: implementation lane reports (group 3)

Date: 2026-10-26. NTM session `xacml4j--pdp-jmh-impl`, four lanes, each with
its packet (common part + lane part). Reports were taken from each CLI's session
record between the lane markers; home paths rewritten to `$HOME` or
repository-relative form.

| Lane | Model | Tasks | Files | Report |
| --- | --- | --- | --- | --- |
| benchmarks | Codex GPT-6.1 Terra (`--cod=1:gpt-6.1-terra:max`, role `policy4j_jmh`) | 3.5 | `jmh/pdp/*.java` | `report-terra.md` |
| corpora | Codex GPT-6.1 Sol (`policy4j_implementer_codex_sol`) | 3.3, 3.4 | `jmh/corpus/SyntheticCorpus.java`, `DirectoryCorpus.java` | `report-sol.md`; lane evidence `builds/005-corpora-sol` |
| snapshot store | Codex GPT-6 Astra (`policy4j_implementer_codex_astra`) | 3.2 | `jmh/store/Snapshot*.java` | `report-astra.md`; lane evidence `tests/006-snapshot-astra-characterization`, `builds/007-snapshot-astra-compile` (renumbered by the conductor from 005 and 001, which other runs used) |
| tests | Claude Opus 5.5 (`policy4j_test_writer`) | 3.1 | `xacml-jmh/src/test/**` | `report-opus.md` |

Lane findings carried forward: `PolicyImportTool` ignores a false `add`
(Terra; bead `policy4j-d4as`); `Rules.describe` printed `Version{...}` (Opus;
fixed by the conductor); `importXml` discovers the marshalling providers once
per source (Opus, Terra; question for rv004 and rv005).

Every lane compiled in a private copy; no lane ran Maven in the shared tree or
ran the `xacml-jmh` tests. The fan-in build, smoke, and tests are runs 008 and
009.
