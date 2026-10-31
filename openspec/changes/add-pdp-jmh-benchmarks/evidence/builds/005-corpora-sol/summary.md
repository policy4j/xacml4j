# Corpora lane: tasks 3.3 and 3.4

Lane: corpora; persona `policy4j_implementer_codex_sol`; CLI Codex; model
GPT-6.1 Sol (task-packet designation).

Result: both corpus providers are implemented and compile in the private
copy. Runtime behavior is UNVERIFIED by this lane: no test or benchmark was
executed, as required by the parallel-lane packet. The conductor owns the
shared test run, task checkboxes, issue closure, and evidence index.

## Files

- `xacml-jmh/src/main/java/org/xacml4j/jmh/corpus/SyntheticCorpus.java`
- `xacml-jmh/src/main/java/org/xacml4j/jmh/corpus/DirectoryCorpus.java`
- This run directory. Beads changes are limited to the assigned issue
  claims and notes, plus the required flush.

## Source decisions (SUPPORTED by opened source)

- `SyntheticCorpus.java:60`: a fixed table supplies XML effects and the
  independently expected decisions. No PDP is used by the oracle
  (`:174`). Root-first policy sources and numeric policy order are set in
  `:70`; XML is UTF-8. Count parsing rejects non-integers and values outside
  1 to 100,000 (`:94`). Distinct numeric slots retain their order, with the
  first/last labels taking priority for counts 1 and 2 (`:80`).
- `DirectoryCorpus.java:70`: the supplied path is retained in the corpus
  name and is passed directly to `Path.of`. An absent or empty argument is
  rejected. Manifest, root, request names, expectation coverage, and decision
  tokens are validated before request XML is parsed. File inventories use
  regular files sorted by file name (`:122`); file content is read once
  during load (`:110`, `:116`). Decision token matching is case-sensitive
  (`:151`).
- Both classes use the spine's license block and
  `@AutoService(PolicyCorpusProvider.class)`; the generated descriptor
  contains both implementations (`checks.txt`).

## Commands and results

Exact commands are in `command.txt`.

- `rtk openspec validate add-pdp-jmh-benchmarks --strict`: exit 0;
  `Change 'add-pdp-jmh-benchmarks' is valid`.
- Private-copy `test-compile` before implementation: exit 1, four missing
  symbol diagnostics for the two providers in a private compilation probe.
  This is a compilation RED, not an executed failing test. Output is in
  `red-compile.txt`; private-copy paths are replaced with `<private-copy>`.
- Same private-copy `test-compile` after implementation: exit 0 with empty
  output (`green-result.json`). The three-method private probe compiles,
  but none of its methods was executed. Maven 3.9.16, Oracle GraalVM
  Java 21.0.6, offline, license rewrite disabled (`checks.txt`).
- Header, annotation, no-null-return, no-console, and byte equality of the
  shared and private provider sources: assertions passed (`checks.txt`).
- `rtk git diff --no-index --check /dev/null <provider>` for each new file:
  exit 1 with no diagnostics; the status denotes the added file, with no
  whitespace error reported.
- Scoped `rtk git status --porcelain` for both vendored resource trees:
  exit 0, empty output.

## Tracking and residual risk

The first ordinary beads claim was rejected by a serial dependency on task
3.2. The packet and design explicitly authorize parallel lanes.
`br update --help` documents `--force` for a blocked issue; both assigned
tasks were claimed with that option, without changing dependencies.

No unresolved implementation blocker. The harness API, shared tests,
existing production modules, and vendored files were not edited by this
lane. Synthetic determinism, exact request contents, PDP outcomes, every
directory-manifest error, and maximum-size memory use remain UNVERIFIED at
runtime. The conductor must run `SyntheticCorpusTest`,
`DirectoryCorpusTest`, fixture integration, and gate 3.7. No JMH run or
performance claim was made.
