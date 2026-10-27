# Evidence

Layout: `tests/`, `reviews/<lane>/`, `builds/`, `final/`. One directory per
run: `<NNN>-<scope>`. See docs/ai/OPENSPEC.md "Evidence".

## Requirements to proof

| Capability / requirement / scenario | Check | Run |
| --- | --- | --- |
| build-plugin-versions / Pinned plugin versions / Effective POM | `help:effective-pom` with Maven 3.9.16 and 3.10.0, `effective_plugins.py` | `tests/001-characterization` (`effective-plugins-after-*.txt`) |
| build-plugin-versions / Pinned plugin versions / Offline build with both Maven binaries | reactor `clean test` offline, both binaries | `tests/002-gate-both-mavens` (8,013 tests each); later trees: add-pdp-jmh-benchmarks `tests/016-gate-reactor` (Maven 3.9.16) |
| build-plugin-versions / The upgrade changes no source / A default build changes no file | `git status` after the builds | `tests/002-gate-both-mavens`; add-pdp-jmh-benchmarks `tests/016-gate-reactor` |
| build-plugin-versions / The upgrade changes no source / Generated JAXB sources | `compare-generated.sh` on the 0.15.1 and 0.15.3 trees | `tests/003-characterization-v2` (generation), `tests/004-jaxb-compare-v3`, `tests/005-jaxb-compare-v4` (comparison) |
| build-plugin-versions / The upgrade changes no source / The comparison rejects other differences | `compare-generated-test.sh` (11 cases) | `tests/004-jaxb-compare-v3`, `tests/005-jaxb-compare-v4` |
| build-plugin-versions / The upgrade changes no source / Header update stays opt-in | root POM property `license.skipUpdateLicense=true`; builds without the flag change no file; forced update rewrites 1,809 (1.7) and 1,704 (2.7.1) files | `tests/002-gate-both-mavens`, `tests/003-characterization-v2` |

## Gate tasks to proof

| Task | Run(s) | Status |
| --- | --- | --- |
| 1.1 Characterization | `tests/001-characterization`, `tests/003-characterization-v2` | done |
| 1.3 Gate | `tests/002-gate-both-mavens`; reviews `code/rv001-plugins-code`, `rv002-plugins-fixes`, `rv003-plugins-recheck`, `rv004-plugins-n3-recheck`; `tests/004`, `005`; docs `docs/ai/TESTING.md` Maven note | done: final APPROVE (rv004); B1, B2, N1, N3 fixed |

## Runs

| Run | Kind | Command | Result |
| --- | --- | --- | --- |
| `tests/001-characterization` | characterization (task 1.1) | `characterize.sh`, `help:effective-pom` | six plugins bound at different versions by the two Mavens before; same after; license and JAXB differences recorded |
| `tests/002-gate-both-mavens` | gate (task 1.3) | reactor `clean test` offline, Maven 3.9.16 and 3.10.0 | PASS: 8,013 tests, 0 failures, 18 skipped each; conformance 937/12; `git status` clean of build rewrites |
| `tests/003-characterization-v2` | characterization after review rv001 B2 | `characterize-v2.sh` | forced header update: 1,809 files (1.7), 1,704 (2.7.1); JAXB generated with both versions |
| `reviews/code/rv001-plugins-code` | code review, Codex GPT-6 Astra | NTM pane, read-only packet | APPROVE WITH CHANGES: B1, B2 |
| `reviews/code/rv002-plugins-fixes` | re-review, Codex GPT-6 Astra | NTM pane, read-only packet | REJECT: B1 not resolved, new N1 (major) |
| `tests/004-jaxb-compare-v3` | comparator after N1 | `compare-generated-test.sh`, `compare-generated.sh` | 9 of 9 cases; 110 files, 0 other lines |
| `reviews/code/rv003-plugins-recheck` | re-check, Codex GPT-6 Astra | NTM pane, read-only packet | APPROVE WITH CHANGES: N1, B1 resolved; new N3 (minor) |
| `tests/005-jaxb-compare-v4` | comparator after N3 | same | binary case red on the run 004 comparator, green after; 11 of 11 cases; 110 files, 0 other lines |
| `reviews/code/rv004-plugins-n3-recheck` | re-check, Codex GPT-6 Astra | NTM pane, read-only packet | APPROVE |

## Not applicable

- 1.3 JMH: NOT APPLICABLE: build plugins do not change the produced code.
