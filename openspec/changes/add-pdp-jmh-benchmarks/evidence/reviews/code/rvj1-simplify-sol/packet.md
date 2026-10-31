READ-ONLY CODE REVIEW (a new packet in this pane; same rules: no file writes, no Maven, tests, benchmarks,
git or br writes). Role: code reviewer from a model family other than the author's (the conductor, Claude
Opus 5.5, wrote the change). Lane tag RVJ1.

The maintainer asked for a simpler xacml-jmh harness: "too many classes and other nonsense, API is not
clean ... too many stupid abstractions". Review that refactor: openspec/changes/add-pdp-jmh-benchmarks
design.md D15 (and D3/D4, which it supersedes), the spec delta, tasks.md group 6, and every change under
xacml-jmh/ compared with the last commit (git diff HEAD -- xacml-jmh; deleted files are in HEAD).

What changed:
- Removed Selection, SelectedStore, PolicyStores, PolicyCorpora, StoreSession, PolicyStoreContext,
  StoreCapability, VersionPatterns. PolicyStore is now name(), open(id, argument, functions, algorithms)
  returning a PolicyRepository, default close(repository), static named() and names().
  PolicyCorpusProvider got static corpus(parameter), named(), names(). Providers became the package-private
  Services (lookup plus the name:argument split). Rules is package-private.
- PdpFixture: open(String store, String corpus), open(String store, PolicyCorpus), static load(repository,
  policies), newRepository(), release(repository); no newContext().
- PolicyStoreLoadBenchmark: each operation opens, loads or imports, and releases one repository with
  try(AutoCloseable release = () -> fixture.release(repository)). The LOAD and CONCURRENT_READS checks
  are gone; with more threads, verifyConcurrent decides.
- Tests: 13 fake stores and 3 fake corpora folded into TestStores and TestCorpora (nested classes,
  registered in src/test/resources/META-INF/services); SelectionTest cases moved to PolicyStoreTest
  (Services.split); the VersionPatterns cases moved to LookupQueryTest; AddRefusingStore became
  ImportRefusing (direct add works, the import tool's adds are refused).

Look for: a repository that is opened and not closed on some path (fixture open failure, setup preflight,
measured methods, close failures masking the primary failure); behavior that a requirement in the spec
delta still names but no test covers after the deletions; a behavior change in the name:argument split or
in the unknown/duplicate messages; test stores whose static state leaks between tests; a fake that no
longer tests what its test says (ImportRefusing, ThreadConfined, CloseFailing); measured code that now
allocates or does work it did not do before (compare loadParsed and importXml with HEAD); any abstraction
that is still not needed, and anything the simplification made worse.

Report: findings most severe first (P1 to P3) with file:line, the smallest fix, and the test; then a
verdict: APPROVE, APPROVE WITH CHANGES, or REJECT.

Write your report between these two lines, each on its own line, exactly as written:
=== BEGIN RVJ1 REPORT ===
=== END RVJ1 REPORT ===
