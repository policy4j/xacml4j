# xacml4j Agent Guide

xacml4j is a Java implementation of the OASIS XACML 2.0 and 3.0
specifications: the policy model, data types, functions, combining
algorithms, a policy decision point (PDP), and XML and JSON marshalling.
This file is the project contract for coding agents. `CLAUDE.md` is a
symlink to it.

## Ground Rules

- Preserve uncommitted work. Branches often carry large in-progress
  refactors. Do not revert, reformat, or bulk-rewrite files outside the task.
- Do not run formatters or organize-imports across the tree.
- Treat vendored standards files as read-only (see
  [Generated and Vendored Files](#generated-and-vendored-files)). Exclude them
  from every bulk find-and-replace.
- A search hit or index result is a candidate. Confirm it in the opened
  source, then in compiler or test output.
- Run shell commands through `rtk` (for example `rtk mvn`, `rtk git`,
  `rtk rg`). Run the code-intelligence commands directly: `ripwire`,
  `ast-grep`, `tgrep`, `mcp.sh`, and `scripts/agent-doctor.sh`.

## Modules

All Java packages are under `org.xacml4j`.

| Module | Purpose | Start here |
| --- | --- | --- |
| `xacml-core` | Policy model, value types, functions, combining algorithms, marshalling SPI | `v30.policy.PolicyDecisionPoint`, `v30.Expr`, `v30.types.Value`, `v30.policy.Policy`, `v30.policy.PolicySet`, `v30.policy.FunctionSpec` |
| `xacml-pdp` | PDP runtime and SPI: attribute resolvers (PIP), policy repositories, audit | `v30.pdp.PolicyDecisionPointBuilder`, `v30.pdp.DefaultPolicyDecisionPoint`, `v30.spi.pip.PolicyInformationPoint`, `v30.spi.repository.PolicyRepository` |
| `xacml-json` | Gson JSON request and response marshalling | `v30.marshal.json` |
| `xacml-xml` | XACML 2.0 and 3.0 XML marshalling; JAXB-to-object-model mappers | `v30.xml.Xacm30MarshallingProvider`, `v30.xml.Xacm20MarshallingProvider` |
| `xacml-xml-jaxb` | XJC-generated JAXB classes only | `src/main/resources/xsd`, `xacml.xjb` |
| `xacml-test` | Test-support library for policy tests | `v30.XacmlPolicyTestSupport`, `v20.Xacml20TestUtility` |
| `xacml-conformance-tests` | OASIS 2.0/3.0 and RSA 2008 conformance suites (tests only) | `v20.Xacml30ConformanceTest`, `v20.RSA2008InteropTest` |
| `xacml-jmh` | JMH benchmarks of PDP decisions and policy store lookups and loads; pluggable stores and corpora (package `org.xacml4j.jmh`, not `v30`) | `xacml-jmh/README.md`, `jmh.harness.PdpFixture`, `jmh.harness.PolicyStore`, `jmh.pdp.PdpDecideBenchmark` |

Module dependencies:

```text
xacml-core     <- xacml-pdp, xacml-json, xacml-xml
xacml-xml-jaxb <- xacml-xml
xacml-pdp, xacml-xml <- xacml-test <- xacml-conformance-tests
xacml-pdp, xacml-xml <- xacml-jmh
```

`xacml-jmh` also packs the vendored `rsa2008-interop/` files of
`xacml-conformance-tests/src/test/resources` as its own resources (read-only
copy at build time; no Maven dependency on that module).

`xacml-proto/` is not a Maven module. It has no `pom.xml`, is not in the
reactor, and its Java code refers to types that no longer exist. Do not add
it to the build unless asked.

## Build and Test

- Use JDK 25. Every module compiles with `<release>25</release>` and no
  preview features (change `adopt-jdk25-baseline`); the enforcer rejects an
  older JDK. The local `.java-version` file (gitignored) says `11.0`, so
  select JDK 25 for each Maven run, for example
  `JAVA_HOME="$(jenv prefix 25)" rtk mvn ...`. `jenv prefix 25` is Oracle
  GraalVM 25: its JIT is Graal unless a JVM gets `-XX:-UseJVMCICompiler`
  (the JMH runs use C2 that way).
- After changing compiler settings, build with `clean`: the compiler plugin
  does not treat a `source`/`target`/`release` change as stale and reuses old
  class files.
- CI (`.github/workflows/main.yml`) builds with Temurin JDK 25
  (`actions/setup-java@v6`) and runs `verify`.
- License headers (license-maven-plugin `update-file-header`, at
  `process-sources`) are updated only with `-Dlicense.skipUpdateLicense=false`:
  the root POM skips the update by default because it would also rewrite
  1,693 vendored conformance fixtures. The commands below still pass
  `-Dlicense.skipUpdateLicense=true`; it is harmless. Check `rtk git status`
  after each build.
- The root POM pins every plugin version (`<pluginManagement>`), so Maven 3.9
  and 3.10 build alike. Maven 3.10 records downloads under a repository id
  that Maven 3.9 does not accept offline: after 3.10 downloads a new plugin,
  run one online build with 3.9 before an offline 3.9 build.
- Surefire excludes `**/*TestCase.java` (the RSA 2008 performance suite).

| Goal | Command |
| --- | --- |
| Compile | `rtk mvn -q -Dlicense.skipUpdateLicense=true compile` |
| Test one module | `rtk mvn -q -Dlicense.skipUpdateLicense=true -pl xacml-pdp -am test` |
| Run one test class | `rtk mvn -q -Dlicense.skipUpdateLicense=true -pl xacml-pdp -am -Dtest=DefaultPolicyDecisionPointTest -Dsurefire.failIfNoSpecifiedTests=false test` |
| Package | `rtk mvn -Dlicense.skipUpdateLicense=true package` |

Tests use JUnit 5 (Jupiter, `junit-bom` 5.14.4) and Mockito 5.24.0,
with Google Truth and Hamcrest. Mockito starts as a JVM agent: the
`maven-dependency-plugin` `properties` goal supplies its JAR path to the
Surefire `argLine`. `docs/ai/TESTING.md` has the test rules, the conformance
commands, what to test per change type, and how to record results.
`docs/ai/BENCHMARKS.md` covers JMH.

## Code Conventions

- Production code is in `org.xacml4j.v30.*`. XACML 2.0 support lives in the
  `Xacml20*` classes of `org.xacml4j.v30.xml`. The `org.xacml4j.v20` package
  exists only in the test modules.
- Match the indentation of the file you edit. Legacy files use tabs. Newer
  code in `xacml-core` (`types`, `identifier`, `model`, `schemas`) uses four
  spaces.
- Every Java file has an LGPL v3 license block (`#%L` to `#L%`) after the
  `package` line. Keep it. The license plugin owns the header text and years.
- Log through SLF4J. `System.out`, `System.err`, and `printStackTrace()` are
  not allowed in `src/main` (enforced by the ast-grep rule
  `java-no-console-output`).
- Guava `Preconditions` is the usual argument check.
- Extension points are annotation driven:
  - Functions: a `@PolicyFuncProvider` class with static methods that carry
    `@FuncSpec`, `@FuncReturnType`, and `@FuncParam`. Example:
    `v30/policy/function/impl/StringFunctions.java`. The standard set is
    `XacmlDefaultFunctions`.
  - Combining algorithms: `@RuleDecisionCombineAlgo` and
    `@PolicyDecisionCombineAlgo` in `v30/policy/combine/impl`.
  - Attribute resolvers: `@PolicyAttrResolverDescr`, read by
    `AnnotatedResolverFactory`.
  - `FunctionProvider`, `CombiningAlgorithmProvider`, and
    `MarshallingProvider` load through `ServiceLoader`. Register a new
    implementation with `@AutoService`; `META-INF/services` files are
    generated at compile time.

## Generated and Vendored Files

- `xacml-xml-jaxb` generates `org.oasis.xacml.*` into `target/`. Change the
  XSDs or `xacml.xjb`, never the generated classes.
- Do not edit these vendored standards files:
  - `xacml-conformance-tests/src/test/resources/**` (OASIS and RSA
    conformance policies, requests, responses, and HTML indexes; about 13 MB)
  - `xacml-xml-jaxb/src/main/resources/xsd/**` (OASIS and W3C schemas)
- Scope searches away from the conformance fixtures unless the task needs
  them, for example `rtk rg -g '!xacml-conformance-tests/src/test/resources/**'`.

## Planning, Tracking, and Swarms

Read the procedure before you do the work:

| Work | Procedure | Tooling |
| --- | --- | --- |
| Plan, apply, verify, archive a change; record evidence | `docs/ai/OPENSPEC.md` | `openspec/` (schema `policy4j-spec-driven`, rules in `openspec/config.yaml`); `/opsx:*` or the `openspec-*` skills of your CLI |
| Find, claim, and close work | `docs/ai/BEADS.md` | `br` (issue prefix `policy4j`) |
| Multi-agent implementation, review, test, benchmark, research | `docs/ai/SWARMS.md` | CAO profiles and NTM personas `policy4j_<category>_<variant>` (`.agents/CAO_PROFILES.md`, `.ntm/personas.toml`) |
| Tests and conformance | `docs/ai/TESTING.md` | Maven, Surefire |
| Performance | `docs/ai/BENCHMARKS.md` | JMH |

Rules that hold everywhere:

- Use an OpenSpec change for new capabilities, behavior changes, public API
  or type-system changes, dependency, build, or JDK changes, and multi-step
  work. Run `openspec validate <change> --strict`, and wait for the
  maintainer's approval before implementing.
- One beads epic per change; task `X.Y` is issue `<epic>.X.Y`. Claim before
  you edit (`br update <id> --claim --actor <name>`); close with the
  evidence path in the reason; run `br sync --flush-only` at session end.
- Work one task group at a time: failing or characterization test first,
  then code, then the gate (reviews, tests, conformance, JMH decision). A
  task is done only when its proof is indexed in the change's
  `evidence/README.md`.
- Swarms: one conductor per change, disjoint files per writer, reviewers
  from other model families than the author, every lane's report stored as
  evidence, sessions shut down when done.
- Do not stage, commit, push, merge, or switch branches unless the
  maintainer asks. Do not run `br agents --add` (its template commits and
  pushes).
- CAO profiles and NTM personas are generated: edit
  `scripts/gen-cao-profiles.py` and rerun it
  (`rtk python3 scripts/gen-cao-profiles.py`); never edit a profile,
  `.ntm/personas.toml`, or `.ntm/roles/` by hand. After an OpenSpec upgrade, regenerate the CLI files with
  `openspec init --tools claude,codex,antigravity,kimi .` and check that
  `openspec/config.yaml` and `openspec/schemas/` survived.

## Research Tools

Use native MCP tools directly for external research. Research servers can
come from the active client's user configuration, so discover each lane's
callable tools before relying on this catalog. Availability differs by client.

| Need | MCP server | Tools |
| --- | --- | --- |
| Library, framework, SDK, or CLI documentation | `context7` | `resolve_library_id`, then `query_docs` |
| Public facts and standards discovery | `tavily` | `tavily_search`, `tavily_extract`, `tavily_map`, `tavily_crawl`; bounded `tavily_research` for synthesis |
| Search or fetch fallback | `duckduckgo` | `search`, `fetch_content` (paginated) |
| Independent, multi-source research | `deep-research-mcp` | `deep_research`, `research_status` where supported |
| Research papers and original source | `arxiv` | `search_papers`, `get_abstract`, `download_paper`, bounded LaTeX section tools |
| OpenAI or Claude product documentation | Provider-native docs MCP | Discover the matching `search_*_docs` and fetch/query tools |

For XACML semantics, fetch the exact official OASIS standard/profile and
section; distinguish normative requirements from experimental fixtures and
engine conventions. On quota or service failure, record it and try the next
available MCP lane once. Never retry-loop or substitute a model's memory
for a fetched source. Do not launch Context7 through a shell wrapper.
See [Research procedure](docs/ai/RESEARCH.md) for tool health, citations,
fallbacks, and swarm handoffs.

## Code Intelligence Tools

Check tool health before you rely on a tool:

```sh
scripts/agent-doctor.sh            # all tools
scripts/agent-doctor.sh ripwire    # one tool: ast-grep | ripwire | tgrep
```

A `PASS` says that the tool answers. It does not say that the answer covers
your edits. Read the `WARN` lines: the tgrep check prints one when files are
newer than its snapshot index.

| Need | Tool | How |
| --- | --- | --- |
| Orient in the codebase, rank the code for a task | ripwire | `ripwire . --for="<task>"`, or `--pack-task="<task>"` for one-call context; MCP `for`, `explore` |
| Callers, blast radius, call paths | ripwire | `--callers=SYM`, `--impact=SYM` (transitive), `--uses=SYM`, `--path=A,B`; MCP `find_referencing_symbols`, `impact`, `uses`, `path_between`, and `batch` for several queries in one call |
| Check your change before you call it done | ripwire | `ripwire . --edit-check=SYM`, `ripwire . --quality-delta`; MCP `edit_check`, `quality_delta` |
| Structural code patterns, lint rules | ast-grep | `ast-grep run --lang java --pattern '<pattern>' <path>`, `ast-grep scan`; MCP `ast-grep`: `find_code`, `find_code_by_rule`, `dump_syntax_tree`, `test_match_code_rule` |
| Literal or regex text search | `rtk rg` | Default for one-off searches |
| Repeated whole-repo regex searches | tgrep | `tgrep '<pattern>' .` from the repository root (ripgrep flags) |

No listed tool resolves Java types. For type-aware questions, read the source
and let the compiler confirm the result.

Project MCP servers `ast-grep` and `ripwire` (both stdio) are declared once per
CLI: `.mcp.json` (Claude, Kimi, Copilot), `.codex/config.toml` (Codex), and
`.grok/config.toml` (Grok). Keep the three files in step. Agy has no project
MCP file; use the CLI forms above. Serena is off in this repository; do not
start it or add it back.

Each stanza finds the checkout with `git rev-parse --show-toplevel` in the
directory where the client was started. The MCP tools serve that checkout.
When you edit another worktree from the same client:

- ripwire: pass the worktree (MCP `path`, CLI `ripwire <worktree> ...`).
- ast-grep: pass the worktree (MCP `project_folder`, CLI path argument).
- tgrep: run it inside the worktree. Each checkout has its own index.

### ripwire

- No project config. It honors `.gitignore` and keeps a warm cache in
  `$TMPDIR/ripwire`.
- Call edges are name based. Rows marked `amb=` or `prov="split"` are
  guesses; confirm them with ast-grep or in source. The `incompatible=` rows
  of `--edit-check` are name based too: a caller of another method with the
  same name can be listed.
- Every count is a floor (`counts_floor="1"`). An empty result is not proof
  of absence.
- For "is it safe to change X", use `--impact=SYM`. `--callers` is one hop
  only.
- Evidence runs keep copies of Java sources under
  `openspec/changes/*/evidence/`. ripwire lists them as callers and as
  duplicates. On the CLI, pass `--exclude=/evidence/`. The MCP tools take no
  exclude option; discard those rows.
- `--quality-delta` reports JUnit test methods as dead code, and also
  methods that nothing calls by name, such as `equals` and `toString`. Read
  each finding before you act on it.

### ast-grep

- Config: `sgconfig.yml`. Rules are in `tools/ast-grep/rules`, and their
  tests are in `tools/ast-grep/rule-tests`.
- Add a rule together with a test that has `valid` and `invalid` cases. Then
  run `ast-grep test --update-all` once to write the snapshot, and
  `ast-grep test` to confirm it passes.
- Quote patterns with single quotes so the shell does not expand `$VAR`
  metavariables.
- ast-grep parses Java syntax only. It does not resolve types or overloads.
- Test a pattern on a known match before you trust a no-match. A pattern
  that can never match gives the same empty output and exit status 1 as a
  pattern with no match in the tree. A pattern that does not parse only
  prints a warning ("Pattern contains an ERROR node"), so do not hide
  stderr. MCP `dump_syntax_tree` with `format=pattern` shows how ast-grep
  reads a pattern.

### tgrep

- Trigram index in `.tgrep/` (gitignored). Build or refresh it with
  `tgrep index .` (a few seconds).
- Search from the repository root and narrow with `-g`, for example
  `tgrep '<pattern>' -g 'xacml-core/src/main/java/**' .`. With a
  subdirectory as the path, tgrep finds no index there, prints a warning,
  and scans the files instead. The two forms can then disagree.
- The index holds the copies of Java sources in the evidence runs. Exclude
  them with `-g '!openspec/**'`.
- The index is a snapshot. Files edited or added after `tgrep index` are
  invisible to indexed searches until you re-index. `tgrep serve .` keeps the
  index live by watching the file system; `tgrep status` shows the age of
  the index, and `Server: running` while the server runs.
- Before you trust a tgrep no-match, confirm the server is running or
  re-index. Or use `tgrep --no-index` or `rtk rg`.
  `scripts/agent-doctor.sh tgrep` prints a `WARN` with the number of files
  that are newer than the index.

## Evidence Labels

State which tool produced each claim, and label its coverage:

- `SUPPORTED`: a healthy tool returned a scoped result, and the opened
  source or the build output confirmed it.
- `PARTIAL`: the tool ran with a known gap: files that do not compile, a
  stale index, name-based rows (`amb=`), or a boundary that was not
  searched. Name the gap.
- `UNAVAILABLE`: the tool or service cannot be reached, or it serves another
  checkout. Name the fallback.

"No callers" and "unused" are `SUPPORTED` only when two lanes agree: ripwire or
ast-grep references, and a text search that includes configuration and
resources. ripwire alone never supports an absence claim.

A passing compile shows only that the code compiles. It does not prove that
a caller, reference, or rename inventory is complete.
