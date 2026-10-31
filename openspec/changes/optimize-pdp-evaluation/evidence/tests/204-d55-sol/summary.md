# D55 response attribute indexes

Candidate ready for conductor integration and independent review. Task `42.21`, bead
`policy4j-cwtu.42.21`, actor `ntm-sol-g1`, implementer `cod_2`. Assigned model/effort:
Codex GPT-6.1 Sol / max; the runtime model was not independently surfaced. Base:
`759d90a6c75510d55127d8e22870f9edd985c991`.

## Changes

- `xacml-core/src/main/java/org/xacml4j/v30/AttributeIndex.java`: the new public
  `of(Collection<Attribute>)` snapshots its input. An allocation-free scan checks at most eight
  attributes for contiguous equal-id runs. Interleaved ids and larger inputs use the existing
  builder. Empty input shares `EMPTY`. The builder constructor stays eager. Factory-built grouped
  indexes retain the immutable list; keyed reads and `toString()` build an immutable multimap,
  safely published through a volatile field with the harmless race used by D48. Unkeyed reads,
  include-in-result access, equality, and hash read the list. Hash folds the per-id list runs to
  the multimap's exact map hash; equality preserves the existing mutual-containment comparison,
  including its existing treatment of repeated attributes.
- `xacml-core/src/main/java/org/xacml4j/v30/types/Entity.java` and
  `xacml-core/src/main/java/org/xacml4j/v30/Category.java`: add the approved public factories.
  `Category.includedInResult()` uses them for filtered categories. The serialized fields and
  serialVersionUIDs are unchanged: Entity retains `content`, `attributeIndex`, and UID
  `6188174758603655643L`; Category retains `refId`, `cachedRef`, `categoryId`, `entity`, and UID
  `1764671001452130122L`. No existing member is removed or narrowed.
- `xacml-pdp/src/main/java/org/xacml4j/v30/pdp/DefaultPolicyDecisionPoint.java`: packages resolved
  attributes with `Category.of(c, Entity.of(attributes))`.
- `xacml-core/src/test/java/org/xacml4j/v30/AttributeIndexTest.java`: outcome parity for empty,
  one, two, shared-id issuers/types, interleaved ids, duplicates, eight, nine, and colliding-id-hash
  inputs. Checks sequence, equality in both directions, hash, string form, inclusion filtering,
  lookup results, bags, mutable input snapshots, and immutable output. Entity and Category
  factories are compared with their builders. The final tests also compare hash directly with
  Guava `asMap().hashCode()` and call `resolve` on fresh indexes.
- `xacml-pdp/src/test/java/org/xacml4j/v30/pdp/ResolvedAttributesPdpTest.java`: expands resolved
  order coverage to one through ten attributes, shared ids with distinct issuers/types, and
  multiple categories. Pins filtered result order, category reference ids, content exclusion,
  and order after keyed lookup across repeated decisions.

## Tests first

Before production edits, the new outcome tests ran against the existing builders: **30 passed**
(core 23, PDP 7). This is a passing characterization, not a red result. The retained
`characterization-tests.patch` records those exact test additions. The four construction helpers
then switched to the new factories; the outcome assertions were kept, with independent Guava
hash and cold-resolution assertions added. No allocation-count, timing, or log assertions were
added.

`before-state.json` and `characterization-ready-state.json` confirm that all four D55 production
files were unchanged for the characterization. `candidate-state.json` and `final-state.json`
pin the final source bytes used by the focused and module gates. The earlier
`focused-ready-state.json` predates only the extra Guava hash and cold-resolution assertions.

## Executed results

Every Maven run used the shared packet runner, `/opt/homebrew/bin/mvn`, `-o`,
`-Dlicense.skipUpdateLicense=true`, and `clean test`. The runner reports Maven 3.10.0 and
Oracle JDK 25.0.4, release 25, macOS aarch64. Exact commands are in `command.txt`.

| Raw log | Tests | Failures | Errors | Skipped | Result |
| --- | ---: | ---: | ---: | ---: | --- |
| `baseline-characterization-maven310-clean.log` | 30 | 0 | 0 | 0 | BUILD SUCCESS; core 23, PDP 7 |
| `focused-factories-maven310-clean.log` | 30 | 0 | 0 | 0 | BUILD SUCCESS; core 23, PDP 7 |
| `module-gate-maven310-clean.log` | 7,379 | 0 | 1 | 2 | Exit 1 in core; downstream modules did not run |
| `module-gate-known-date-error-maven310-clean.log` | 8,473 | 0 | 1 | 6 | Exit 0 with the authorized failure-ignore flag; the error remains |

The normal module gate encountered only the supplied existing month defect:
`ISO8601DateTest.testCreateFromCalendar:59`, `DateTimeException: Invalid date 'SEPTEMBER 31'`.
It is outside this lane's ownership and was not edited. The authorized
`-Dmaven.test.failure.ignore=true` rerun reached all downstream modules:

| Module | Tests | Failures | Errors | Skipped |
| --- | ---: | ---: | ---: | ---: |
| xacml-core | 7,379 | 0 | 1 | 2 |
| xacml-xml | 254 | 0 | 0 | 0 |
| xacml-json | 24 | 0 | 0 | 1 |
| xacml-pdp | 816 | 0 | 0 | 3 |
| Total | 8,473 | 0 | 1 | 6 |

JAXB has no tests. There were no other failing or erroneous tests. The continuation's BUILD
SUCCESS is not a claim that the core suite passed. `surefire-summary.txt` extracts the module
totals from all four raw logs.

Additional checks: scoped `ast-grep scan` on all six owned Java files passed; strict OpenSpec
validation passed; `rtk git diff --check` passed. Ripwire doctor passed 7/7. Its orientation result
is PARTIAL (name-based, low-confidence ranking); live source and the clean builds are the
confirmation for the actual edits. No semantic reference-completeness claim is made.

## Scope and limits

Only the six assigned Java files and this evidence directory were changed for D55. All other
tracked bytes and the prior `202-resume-variable-dag-sol` evidence remain identical to the
start snapshot. In particular, ResponseWriters.java, Policy.java, PolicySet.java, and
NestedResponseStagingPdpTest.java remain unchanged from the candidate under review. The final
status includes their four pre-existing modifications, this lane's six modifications, and the
two lane evidence directories. `scope-verification.json` records these checks.

The old equality/hash behavior is preserved, including the existing mismatch when duplicate
multiplicity differs; this task does not change that public behavior. The new overloads are the
public API additions approved in D55. No serialized fields were added to Entity or Category.

Byte figures, exact histograms, JMH, and the controlled pair: **UNRUN**, reserved for the
conductor. The Guava 31.1 estimates in the research note are not measurements of this candidate.
Independent review, combined integration checks after the separate date fix, and conformance
remain conductor gates. No task, design, evidence index, Beads, branch, stage, or commit was
changed. No additional file is needed for the implementation.

Source and raw-log fingerprints follow; the full manifest is `candidate-ready.json`.

| Owned source/test | SHA-256 |
| --- | --- |
| `xacml-core/src/main/java/org/xacml4j/v30/AttributeIndex.java` | `ece4207a6747adf90eb6b78c10cb30fa08994f5a96256afcf50460fbd61f3ab8` |
| `xacml-core/src/main/java/org/xacml4j/v30/types/Entity.java` | `fbd98895aaae24453460e5d185b92e32be609c1e76833cc218793fb3d711e301` |
| `xacml-core/src/main/java/org/xacml4j/v30/Category.java` | `a572cf0483fd26bb1b8397bcb34ee1c77fb51597dd6dd8a4662a14970b8cf619` |
| `xacml-pdp/src/main/java/org/xacml4j/v30/pdp/DefaultPolicyDecisionPoint.java` | `91161e91325ebe3ba54a4d657c7cafe6855d9fbfd9cb12606c4f4fd7f2d4c99d` |
| `xacml-core/src/test/java/org/xacml4j/v30/AttributeIndexTest.java` | `1a2df4ad3f1451f738a465bf3f4a5d445043f6c6dd86b555c3cb83a0f30e5403` |
| `xacml-pdp/src/test/java/org/xacml4j/v30/pdp/ResolvedAttributesPdpTest.java` | `a82dff216c98e383d4147ea3e7fc127111fa959e52c72a07b151311fd0a3631f` |

| Raw log | SHA-256 |
| --- | --- |
| `baseline-characterization-maven310-clean.log` | `67960ae4b759529cfc2b7a26c1a09222dbeb91db251a74fc9f64f7a98e266d2d` |
| `focused-factories-maven310-clean.log` | `44b89dcdad4b7d573f4a8a1628865fbd409848d2095b47df2d80f61d0c5851ae` |
| `module-gate-maven310-clean.log` | `79a94f3687418ba69da1b4c9a0e07754519a4df304d228e265a743fd3aa398de` |
| `module-gate-known-date-error-maven310-clean.log` | `c85e839c7f747aa2cc23ea916fff4cefdb8ef9c78a59e0c190a96054be50a536` |
