# OpenSpec Workflow

OpenSpec holds the plan, the requirements, and the proof for every
non-trivial change. This file is the procedure. `openspec/config.yaml` holds
the compact rules that the OpenSpec CLI injects into generated instructions.

## When to use it

Use a change for new capabilities, behavior changes (XACML semantics,
marshalling, PDP), public API or type-system changes, dependency, build, or
JDK changes, and any multi-step work. Skip it for a typo, a comment, or a
local fix with no behavior change; still run the tests.

## Schema

The project schema is `policy4j-spec-driven`
(`openspec/schemas/policy4j-spec-driven/`). Artifacts, in dependency order:

| Artifact | File | Purpose |
| --- | --- | --- |
| proposal | `proposal.md` | Why, what changes, capabilities, research questions, verification, impact, beads epic |
| research | `research/README.md` | REQUIRED or NOT REQUIRED; answers with sources; swarm lane index |
| specs | `specs/<capability>/spec.md` | Normative SHALL/MUST requirements with GIVEN/WHEN/THEN scenarios |
| design | `design.md` | Decisions D1..Dn, test strategy, benchmark plan, execution plan (swarm lanes), risks |
| tasks | `tasks.md` | `- [ ] X.Y` checklist; X.Y maps to beads issue `<epic>.X.Y` |
| evidence | `evidence/README.md` | Requirement → test → run index; gate tasks → runs |

`apply` needs tasks and evidence. Check progress with
`openspec status --change <id>`; print the generated instructions for one
artifact with `openspec instructions <artifact> --change <id>`.

## Lifecycle

1. **Explore** (optional). Read AGENTS.md, `openspec list --specs`, and the
   code (`ripwire . --for="<goal>"`). No files yet.
2. **Propose.** Create the change with a verb-led kebab-case id and write
   the artifacts in order. Planners can be a single agent or a planning
   swarm (docs/ai/SWARMS.md).
3. **Validate.** `openspec validate <id> --strict` must pass.
4. **Review the plan.** At least one architecture reviewer lane that did not
   write the plan (for example `policy4j_arch_review_*`). Record the review
   under `evidence/reviews/plan/`.
5. **Approve.** The maintainer approves the change. No implementation
   before approval.
6. **Track.** Create the beads epic and one issue per task
   (docs/ai/BEADS.md). Put the epic id in `proposal.md` (Tracking) and the
   `tasks.md` header.
7. **Apply.** One task group at a time (below).
8. **Verify.** Every checked task has an indexed run; every requirement maps
   to a passing test or conformance case; `openspec validate <id> --strict`
   passes.
9. **Archive.** After the maintainer lands the work:
   `openspec archive <id> --yes`, then `openspec validate --all --strict`.
   Archiving merges the spec deltas into `openspec/specs/`.

## Commands per CLI

| CLI | How to drive OpenSpec |
| --- | --- |
| Claude Code, Grok | `/opsx:propose`, `/opsx:new`, `/opsx:continue`, `/opsx:ff`, `/opsx:apply`, `/opsx:verify`, `/opsx:archive`, `/opsx:explore` (`.claude/commands/opsx/`), or the `openspec-*` skills |
| Codex | `openspec-*` skills in `.codex/skills/`; `/opsx-*` prompts if installed globally |
| Agy | `/opsx-*` workflows in `.agent/workflows/`, or the `openspec-*` skills in `.agent/skills/` |
| Kimi | `openspec-*` skills in `.kimi/skills/` |
| Any | The `openspec` CLI: `new change`, `status`, `instructions`, `validate`, `show`, `archive` |

After an OpenSpec upgrade, regenerate the tool files with
`openspec init --tools claude,codex,antigravity,kimi .`, then check that
`openspec/config.yaml` and `openspec/schemas/` are unchanged.

## Apply loop (per task group)

1. Claim the group's first ready issue (`br ready`, then
   `br update <id> --claim --actor <agent>`).
2. Write the failing or characterization test first; record it as a test
   run.
3. Implement. Keep to the files the task names; one writer per file.
4. Run the group's gate: reviews, module tests, conformance when behavior
   can change, the JMH decision, and a docs/spec consistency check.
5. Record each run under `evidence/` and index it in `evidence/README.md`.
6. Check the task in `tasks.md`, close the beads issue with the proof
   (`br close <id> --reason "..."`), and run `br sync --flush-only`.

Stop and ask the maintainer when a conformance case fails for a reason you
cannot explain, when the task needs git history changes or a new
dependency, or when the scope grows.

## Evidence

Evidence lives in the change: `openspec/changes/<id>/evidence/`.

| Directory | Holds |
| --- | --- |
| `tests/` | Test and conformance runs |
| `reviews/<lane>/` | Review runs; lanes: `plan`, `code`, `tests`, `semantics`, `performance`, `docs`, `final` |
| `benchmarks/jmh/` | JMH runs (docs/ai/BENCHMARKS.md) |
| `builds/` | Baselines, dependency trees, JDK identity |
| `final/` | The closing audit |

Evidence is a reference that a reader can open and rerun, not a recording of
a run. Cite the proof itself:

| Proof | Cite |
| --- | --- |
| Test | Repository-relative path and test case: `xacml-core/src/test/java/org/xacml4j/v30/types/ISO8601DateTest.java#testCreateFromJanuaryCalendar` |
| Conformance case | Suite path and case id |
| JMH benchmark | Benchmark path, method, and parameters: `xacml-jmh/src/main/java/org/xacml4j/jmh/pdp/PdpDecideBenchmark.java#decide`, `corpus=rsa2008`, `request=all` |
| JCStress test | Test class path and the outcome it pins |
| Specification | Link with the section: XACML 3.0 Core, section 7.11 |
| External resource | URL, version, and fetch date |

Rules:

- One directory per run, `<NNN>-<scope>` (reviews: `rvNNN-<scope>-<lane>`).
  Number runs in order within the change; never reuse or overwrite a run.
- Each run holds:
  - `summary.md`: what it proves, the references above, the JDK, the result
    and counts, and for reviews the reviewer profile and model with a
    severity-ordered finding list;
  - `command.txt`: the exact command, with repository-relative paths;
  - for reviews, `report.md`.
- Do not commit execution output: build and test logs, Surefire reports, JMH
  logs or JSON, lane receipts, state snapshots, patches, or copies of source.
  Numbers that matter go in `summary.md` or `benchmarks/jmh/LEDGER.md`. Keep
  raw output outside the repository. Runs before 2026-10-31 keep their files.
- Do not commit `target/` trees, IDE files, or tool caches.
- A review lane that failed, timed out, or produced nothing is still a run.
  Record it with the reason. A three-lane review where two lanes died is a
  one-lane review.
- A run started by a swarm worker must land in the change before the worker
  is closed. A transcript in a tmux pane or a temp file is not evidence.
- A checked task without an indexed run is not done. Write
  `NOT APPLICABLE: <reason>` instead of inventing proof.

## Rules

- Never stage, commit, push, or change branches unless the maintainer asks.
- Use repository-relative paths in artifacts; never a home or checkout
  path, never secrets or credentials.
- Specs describe behavior, not tools or agents.
- Keep follow-up work in its own change instead of growing the current one.
