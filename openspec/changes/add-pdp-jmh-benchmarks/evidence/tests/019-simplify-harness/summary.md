# Task 6.1: simplified harness API (D15)

Date: 2026-10-27. Maintainer request: "I hate design of xacml-jmh too many classes and other nonsense,
API is not clean ... too many stupid abstractions".

## Result

- Module tests: 194 run, 0 failures, 0 errors, 0 skipped (`org.xacml4j.jmh.**`, including
  `BenchmarkSmokeTest`). Before: 186 (`tests/018-review-fix-red-green`).
- `ast-grep scan`: no findings.
- Compile of main and test code: clean.

## Size, compared with HEAD (9855935a)

| | Files | Lines |
| --- | --- | --- |
| `xacml-jmh/src/main/java` | 30 -> 22 | 3,163 -> 2,836 |
| `xacml-jmh/src/test/java` | 45 -> 27 | 5,337 -> 4,597 |
| Public types in `org.xacml4j.jmh.harness` | 18 -> 8 | |

Removed main types: `Selection`, `SelectedStore`, `PolicyStores`, `PolicyCorpora`, `StoreSession`,
`PolicyStoreContext`, `StoreCapability`, `VersionPatterns`. `Providers` became the package-private
`Services`; `Rules` is package-private.

Tests: 13 fake stores and 3 fake corpora became nested classes of `TestStores` and `TestCorpora`;
`LosingPolicyRepository`, `StaleLatestPolicyRepository`, and `LostPolicyException` moved into
`TestStores`. `PolicyStoresTest` and `PolicyCorporaTest` became `PolicyStoreTest` and
`PolicyCorpusProviderTest`; the `SelectionTest` cases test `Services.split` in `PolicyStoreTest`; the
version-pattern cases moved to `LookupQueryTest`. `AddRefusingStore` became `ImportRefusing`: a direct
`add` works and the import tool's adds are refused, so the rv007 regression still tests the import
check. Removed with `StoreCapability`: the test that the load benchmark rejects a store without `LOAD`.

## Tools and gaps

- Serena: UNAVAILABLE. It was restarted to import the new module, did not finish the import, and the
  client lost the connection. Fallback: deletions with `git rm`, references by `rtk rg` over
  `xacml-jmh`, `docs`, `openspec`, and the README, then the compiler. The compile proves only that no
  reference to a removed type is left in compiled code; the text search covers docs and resources.
- Process: bead `policy4j-ba4s.6.1` was claimed after the edits, not before (the rule says before).
- No JMH run for this task: the maintainer asked to measure once after the batch of changes.
