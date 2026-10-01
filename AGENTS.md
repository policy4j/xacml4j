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
  `rtk rg`).

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

Module dependencies:

```text
xacml-core     <- xacml-pdp, xacml-json, xacml-xml
xacml-xml-jaxb <- xacml-xml
xacml-pdp, xacml-xml <- xacml-test <- xacml-conformance-tests
```

`xacml-proto/` is not a Maven module. It has no `pom.xml`, is not in the
reactor, and its Java code refers to types that no longer exist. Do not add
it to the build unless asked.

## Build and Test

- Use JDK 21. The poms compile at level 21, and `xacml-core` adds
  `--enable-preview`. The local `.java-version` file (gitignored) says `11.0`,
  so select JDK 21 for each Maven run, for example
  `JAVA_HOME="$(jenv prefix 21)" rtk mvn ...`.
- CI (`.github/workflows/main.yml`) still builds with JDK 8. It fails at
  level 21 until someone updates it.
- Maven rewrites license headers during `process-sources`
  (license-maven-plugin `update-file-header`). Pass
  `-Dlicense.skipUpdateLicense=true` unless you intend to update headers.
  Check `rtk git status` after each build.
- Surefire excludes `**/*TestCase.java` (the RSA 2008 performance suite).

| Goal | Command |
| --- | --- |
| Compile | `rtk mvn -q -Dlicense.skipUpdateLicense=true compile` |
| Test one module | `rtk mvn -q -Dlicense.skipUpdateLicense=true -pl xacml-pdp -am test` |
| Run one test class | `rtk mvn -q -Dlicense.skipUpdateLicense=true -pl xacml-pdp -am -Dtest=DefaultPolicyDecisionPointTest -Dsurefire.failIfNoSpecifiedTests=false test` |
| Package | `rtk mvn -Dlicense.skipUpdateLicense=true package` |

Tests use the JUnit 4 API (`org.junit.Test`) on the JUnit Platform through
`junit-vintage-engine`, plus EasyMock, Google Truth, and Hamcrest, until the
JUnit 5 + Mockito migration lands. `docs/ai/TESTING.md` has the test rules,
the conformance commands, what to test per change type, and how to record
results. `docs/ai/BENCHMARKS.md` covers JMH.

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

## Code Intelligence Tools

Check tool health before you rely on a tool:

```sh
scripts/agent-doctor.sh            # all tools
scripts/agent-doctor.sh serena     # one tool: ast-grep | ripwire | tgrep | serena
```

| Need | Tool | How |
| --- | --- | --- |
| Orient in the codebase, rank the code for a task | ripwire | `ripwire . --for="<task>"`, or `--pack-task="<task>"` for one-call context |
| Callers, blast radius, call paths | ripwire | `--callers=SYM`, `--impact=SYM` (transitive), `--uses=SYM`, `--path=A,B` |
| Check your change before you call it done | ripwire | `ripwire . --quality-delta` |
| Java symbols, references, hierarchy (type-aware) | Serena | MCP `serena`: `find_symbol`, `find_referencing_symbols`, `get_symbols_overview` |
| Structural code patterns, lint rules | ast-grep | `ast-grep run --lang java --pattern '<pattern>' <path>`, `ast-grep scan`, MCP `ast-grep` |
| Literal or regex text search | `rtk rg` | Default for one-off searches |
| Repeated whole-repo regex searches | tgrep | `tgrep <pattern> .` (ripgrep flags) |

Project MCP servers are declared in `.mcp.json`: `serena` (HTTP), `ast-grep`
(stdio), and `ripwire` (stdio).

### ripwire

- No project config. It honors `.gitignore` and keeps a warm cache in
  `$TMPDIR/ripwire`.
- Call edges are name based. Rows marked `amb=` or `prov="split"` are
  guesses; confirm them with Serena or in source.
- For "is it safe to change X", use `--impact=SYM`. `--callers` is one hop
  only.

### Serena

- Semantic Java lane backed by Eclipse JDTLS. JDTLS imports the Maven
  reactor through m2e.
- Config: `.serena/project.yml`. Other files in `.serena/` are local
  (gitignored).
- Runs as a project-scoped daemon at `http://127.0.0.1:9951/mcp`, managed by
  the `mcp.sh` controller with launchd label
  `com.artagon.serena-xacml4j-0adfb4b7`.
- Restart after changes to a `pom.xml`, the JDK, or `.serena/project.yml`:
  `mcp.sh -m serena restart`.
- File edits and shell commands go through the agent's native tools. Serena's
  editing, shell, and memory tools are disabled for this project.
- JDTLS results can be incomplete when the working tree has compile errors.
  Then label Serena results `PARTIAL` and confirm them in source. An empty
  result is not proof of absence.

### ast-grep

- Config: `sgconfig.yml`. Rules are in `tools/ast-grep/rules`, and their
  tests are in `tools/ast-grep/rule-tests`.
- Add a rule together with a test that has `valid` and `invalid` cases. Then
  run `ast-grep test --update-all` once to write the snapshot, and
  `ast-grep test` to confirm it passes.
- Quote patterns with single quotes so the shell does not expand `$VAR`
  metavariables.
- ast-grep parses Java syntax only. It does not resolve types or overloads.

### tgrep

- Trigram index in `.tgrep/` (gitignored). Build or refresh it with
  `tgrep index .` (about 2 s).
- The index is a snapshot. Files edited or added after `tgrep index` are
  invisible to indexed searches until you re-index. `tgrep serve .` keeps the
  index live by watching the file system; `tgrep status` shows
  `Server: running` while it runs.
- Before you trust a tgrep no-match, confirm the server is running or
  re-index. Or use `tgrep --no-index` or `rtk rg`.

## Evidence Labels

State which tool produced each claim, and label its coverage:

- `SUPPORTED`: the tool represents the syntax and returned a scoped result.
- `PARTIAL`: the tool is healthy, but its model is incomplete (for example,
  JDTLS on files that do not compile).
- `UNAVAILABLE`: the tool or service cannot be reached.

A passing compile shows only that the code compiles. It does not prove that
a caller, reference, or rename inventory is complete.
