FINAL REPORT (policy4j_code_review_codex_astra, GPT-6 Astra)

CLI: Codex. Lane: rv010.  
**optimize-pdp-evaluation: APPROVE WITH CHANGES.**  
**upgrade-build-plugins: REJECT** pending the comparison-gate correction below.

Original findings:

- **A1 — RESOLVED.** Qualified `Math.random()` and receiver-less calls are covered—checked in `tools/ast-grep/rules/java-no-random-on-decide-path.yml:30` and `:35`. Regression cases cover both original bypasses—checked in `tools/ast-grep/rule-tests/java-no-random-on-decide-path-test.yml:43` and `:49`. The additional RNG constructors/factories are covered at rule lines 18–28.
- **B1 — NOT RESOLVED.** The specification now separates default builds, generator comments, and opt-in updates—checked in `openspec/changes/upgrade-build-plugins/specs/build-plugin-versions/spec.md:20`. However, the proposal still promises byte-identical sources and headers at `proposal.md:31`, no header changes at `:58`, and identical JAXB sources at `:60`. The specification also permits only generator-version differences, while the helper additionally permits timestamps. **Fix:** Reconcile these promises and permitted differences across the proposal, specification, and comparator.
- **B2 — RESOLVED for the original version-selection and skipped-update defects.** The replacement forces header updates, sets each JAXB version, and checks the resulting POM—checked in `openspec/changes/upgrade-build-plugins/evidence/tests/001-characterization/characterize-v2.sh:19`, `:28`, and `:29`. Retained run output records both generations and nonzero header differences—checked in `openspec/changes/upgrade-build-plugins/evidence/tests/003-characterization-v2/output.txt:2`. Its replacement comparator introduces N1 below.

New findings, most severe first:

1. **MAJOR — N1: JAXB comparison accepts added/deleted files and comparison errors.**  
   Checked in `openspec/changes/upgrade-build-plugins/evidence/tests/001-characterization/characterize-v2.sh:33`. `diff -r` failures are swallowed, and line 34 examines only lines beginning `< ` or `> `. If one generator adds or removes `Foo.java`, `diff` reports `Only in …: Foo.java`; that line is discarded, `other` remains zero, and line 36 succeeds. Comparison errors can follow the same acceptance path. Thus the recorded zero does not establish equivalent generated trees.  
   **Fix:** Compare relative file inventories first, reject comparison errors, then compare corresponding files after narrowly defined comment normalization. Retain the raw comparison evidence. **Regression checks:** Added file, missing file, unreadable input, and changed Java declaration must fail; permitted comment-only differences must pass. These triggering cases were **not run** here.

2. **MINOR — N2: The concurrent test shares an unsynchronized audit list.**  
   Checked in `xacml-pdp/src/test/java/org/xacml4j/v30/pdp/CorrelationIdTest.java:145`. Eight workers share the PDP, whose auditor appends to that `ArrayList` at line 86. The production path invokes the auditor during evaluation—checked in `xacml-pdp/src/main/java/org/xacml4j/v30/pdp/DefaultPolicyDecisionPoint.java:160`. Concurrent additions race, permitting lost records and intermittent exceptions during growth, independently of correlation-counter correctness.  
   **Fix:** Pass a synchronized list or use a no-op auditor for this test. **Regression check:** Retain the 2,000-call uniqueness assertion; if recording audits, also assert 2,000 audit records after joining every worker. Runtime reproduction was **not run**.

The hexadecimal and evaluation-exception coverage gaps are resolved in test source: calls 10/15/16/17 are asserted at `CorrelationIdTest.java:126`, and both MDC keys are checked after the exception at `:183`. Retained Surefire output reports seven passing tests—checked in `xacml-pdp/target/surefire-reports/org.xacml4j.v30.pdp.CorrelationIdTest.txt:4`. This is retained evidence, not fresh execution.

Verification used file reads, scoped searches, Git inspection, and SHA-256 comparisons. The re-review target files remained unchanged during inspection; a concurrent AGENTS.md documentation update was inspected. No files were written, and no Maven, tests, benchmarks, characterization scripts, Git writes, or br writes were run. Fresh runtime validation and benchmark acceptance remain **UNVERIFIED**.
