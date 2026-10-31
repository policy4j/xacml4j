RV209-ASTRA-1 is corrected in the existing D55 candidate. This lane changed only
`xacml-core/src/main/java/org/xacml4j/v30/AttributeIndex.java` and
`xacml-core/src/test/java/org/xacml4j/v30/AttributeIndexTest.java`.

Agent: cod_2; actor: ntm-sol-g1; parent bead: policy4j-cwtu.42.21.
Assigned model/effort: Codex GPT-6.1 Sol, max (packet); the runtime model and effort
were not independently exposed. Base: `759d90a6c75510d55127d8e22870f9edd985c991`.
Review input: `evidence/reviews/code/rv209-gate-audit-astra/report.md`,
RV209-ASTRA-1, read from PROJECT_ROOT. All paths below are relative to this worktree
or the change's evidence directory, as stated.

Correction:

- `AttributeIndex.java:53` fixes the full-designator alias budget at 64 per index.
  This is a cache budget, independent of stored attributes or arriving categories.
  It supports a repeat working set of 64 designators; larger sets still resolve
  normally. No semantic correctness depends on the numerical budget, and no
  measurement is claimed for its selection.
- `AttributeIndex.java:430` keeps the existing alias lookup first: a cached repeat
  returns before constructing a Lookup or taking a monitor. On a miss, resolution
  still ignores category and returns the canonical Optional/bag selected by
  `Lookup`'s `putIfAbsent`. The canonical positive Lookup entries are not capped.
- Only admission of a new positive alias takes the existing bags map's monitor.
  The count is checked again and incremented there only for a new alias, so racing
  first demands cannot exceed 64. A volatile count lets overflow demands bypass
  alias admission and its monitor. There is no eviction, negative-key retention,
  category rejection, or new public API. The field and method comments now state
  the actual bound.

Tests were added before the production correction and were unchanged between red,
green, and the module gate. The private bags field is read reflectively only in
tests: values alone cannot reveal this retention defect.

- `AttributeIndexTest.java:171`: one Entity, one stored attribute and one canonical
  Lookup, the normal resource designator plus 1,024 distinct-category positive
  keys. Every result has identical contents and the same Optional and bag; retained
  aliases must stay within 64. Both cached and overflow repeats keep identity and
  map size; the overflow key is not retained.
- `AttributeIndexTest.java:200`: unknown id, mismatched type, and mismatched issuer
  across 128 categories, before and after a positive demand. No negative aliases
  or Lookups are retained.
- `AttributeIndexTest.java:218`: 64 distinct attribute/designator pairs each retain
  their alias alongside their canonical Lookup (128 entries), and repeat demands
  return the kept Optional. Canonical entries do not consume the alias budget.
- `AttributeIndexTest.java:247`: eight workers start together on an unprimed Entity,
  share the normal first designator, then request 1,024 distinct-category aliases.
  Every result is the canonical Optional and bag, and the bound holds after joining.
  Barrier/future timeouts only guard a hung test; there is no timing or allocation
  threshold and no log assertion.

Fresh execution used the shared runner, Maven 3.10.0, Oracle JDK 25.0.4, release 25,
offline Maven, disabled license updates, and `clean test` for every run. Exact
arguments and portable command paths are in `command.txt`; complete normalized
console output is retained in the three logs.

| Run | Tests | Failures | Errors | Skipped | Result |
| --- | ---: | ---: | ---: | ---: | --- |
| Focused red on uncorrected D55 | 1 | 1 | 0 | 0 | Expected assertion failure: 1,025 retained aliases |
| Focused green, all AttributeIndexTest cases | 27 | 0 | 0 | 0 | PASS |
| Module gate, core | 7,383 | 0 | 1 | 2 | Known ISO8601DateTest error only |
| Module gate, XML | 254 | 0 | 0 | 0 | PASS |
| Module gate, JSON | 24 | 0 | 0 | 1 | PASS |
| Module gate, PDP | 816 | 0 | 0 | 3 | PASS |
| Module gate total | 8,477 | 0 | 1 | 6 | No other failure or error |

The red failure is `designatorAliasesStayBoundedAcrossCategories:187`,
`retained designator aliases: 1025`, after all value and identity assertions had
passed. Compilation and dependency resolution succeeded. It is a semantic
retention regression, not an environmental or unrelated-test failure.

The module gate used the explicitly authorized `-Dmaven.test.failure.ignore=true`.
Its only error is `ISO8601DateTest.testCreateFromCalendar:59`,
`DateTimeException: Invalid date 'SEPTEMBER 31'`. Maven therefore returns 0 and
prints BUILD SUCCESS, but this is not a wholly green core suite. The error is in
protected source and remains for the separate fix lane. JAXB reports no tests.
The inherited D55 `ResolvedAttributesPdpTest` (7 cases) and P-CPU2
`NestedResponseStagingPdpTest` (20 cases) also pass in this fresh module log.

Static checks: `ast-grep scan` on the two owned Java files and `rtk git diff --check`
both returned 0 with no output. These checks and direct source inspection support
the admission invariant and fast-path structure; concurrency tests sample actual
parallel first demands rather than proving all possible schedules.

Preservation: before/red-ready/green-ready/final fingerprints establish that only
the test changed before red, only the two owned tracked files changed in this
follow-up, and both tested files stayed unchanged after green. All other tracked
bytes, all earlier evidence in 202 and 204, the other four D55 files, and all four
P-CPU2 files match their starting hashes. No staging, commit, branch, Beads, task,
design, or evidence-index action was taken.

`206-rv209-sol/candidate-ready.json` supplies the updated two source/test hashes and
the unchanged inherited D55 hashes. It supersedes only the two owned-file hashes
in the historical `204-d55-sol/candidate-ready.json`; that protected evidence is
preserved verbatim.

Limits: independent review and the combined gate after the separate date fix are
pending. Conformance, retained-byte figures/histograms, throughput/allocation
measurements, and JMH remain UNRUN. The conductor owns measurement and integration.

SHA-256, owned source and test files:

| Path | SHA-256 |
| --- | --- |
| `xacml-core/src/main/java/org/xacml4j/v30/AttributeIndex.java` | `56011bd4f9a92114948c57b96bf5dddbd6d05b8ae03c54c138ff23b1f40289ee` |
| `xacml-core/src/test/java/org/xacml4j/v30/AttributeIndexTest.java` | `477e874791c045b10232bbaa1b809809576cb57ca0cff92a03e94a04aa1d5453` |

SHA-256, raw build logs:

| Log | SHA-256 |
| --- | --- |
| `focused-red-d55-maven310-clean.log` | `b990db30cb4c8bc6e27be92931f35fa6c6c5ff3aadb06366b754555e58243753` |
| `focused-green-maven310-clean.log` | `055bd20965e1e1d81ec202e200bed3a9e306b6b3e91134eac36bf9bcad0ca6c8` |
| `module-gate-known-date-error-maven310-clean.log` | `76909d1324f3dd6990d186403674171da481714b0c5617eae1930136c87858d9` |
