FINAL REPORT (policy4j_code_review_codex_astra, GPT-6 Astra)

CLI: Codex. Lane: rv012.  
**optimize-pdp-evaluation: APPROVE.**  
**upgrade-build-plugins: APPROVE WITH CHANGES** for one remaining comparator gap.

- **N1 — RESOLVED for the reported inventory and comparison-error paths.** Sorted relative inventories are compared before file contents; mismatches exit 1 and failed file comparisons exit 2—checked in `openspec/changes/upgrade-build-plugins/evidence/tests/001-characterization/compare-generated.sh:14`, `:18`, and `:29`. The characterization script invokes this comparator at `characterize-v2.sh:35`. Retained evidence reports nine passing cases and 110 files with zero other changed lines—checked in `openspec/changes/upgrade-build-plugins/evidence/tests/004-jaxb-compare-v3/output.txt:2` and `:14`.

- **B1 — RESOLVED.** The proposal and specification now agree on identical file inventories, the two permitted XJC header differences, and opt-in header updates—checked in `openspec/changes/upgrade-build-plugins/proposal.md:31`, `:61`, and `specs/build-plugin-versions/spec.md:20`. D4 and the research answers reflect that contract—checked in `openspec/changes/upgrade-build-plugins/design.md:47` and `research/README.md:37`.

- **N2 — RESOLVED.** The auditor receives a synchronized list. Assertions run after all futures complete and require 2,000 audit records with 2,000 distinct values—checked in `xacml-pdp/src/test/java/org/xacml4j/v30/pdp/CorrelationIdTest.java:146`, `:162`, and `:171`. Retained Surefire output reports seven passing tests—checked in `xacml-pdp/target/surefire-reports/org.xacml4j.v30.pdp.CorrelationIdTest.txt:4`.

**MINOR — N3: Binary differences still produce a false pass.**  
Checked in `openspec/changes/upgrade-build-plugins/evidence/tests/001-characterization/compare-generated.sh:27` and `:33`. For matching filenames with differing binary content, `diff` returns 1 and prints `Binary files … differ` rather than `<`/`>` lines; this behavior is documented in `/usr/share/man/man1/diff.1:404` and `:725`. The filter discards that diagnostic, counts zero prohibited lines, and the final check at line 39 succeeds. A corrupted generated file containing a NUL can therefore escape comparison.

**Fix:** When `diff` returns 1, reject output containing no recognized changed-text lines before applying the header allowance. Add a same-filename binary-difference fixture expecting failure. This finding is statically traced; the reproducer was **not run**.

Verification consisted of source, documentation, and retained-output inspection plus scoped SHA-256 comparisons; all eight re-review targets remained unchanged during inspection. No files were written, and no tests, Maven, benchmarks, comparison scripts, or Git/br writes were run. Fresh execution remains **UNVERIFIED**.
