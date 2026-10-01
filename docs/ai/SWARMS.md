# Agent Swarms (CAO and NTM)

A swarm runs several CLI agents on one OpenSpec change: implementers,
test writers, reviewers, benchmark engineers, and one conductor that fans
their results into the change. Use a swarm when lanes are truly
independent or when you want reviews from different model families. A
single agent is better for small or tightly coupled work.

## CAO or NTM

| Need | Use |
| --- | --- |
| Typed roles with enforced permissions, a conductor with callbacks, headless runs | CAO with the project profiles |
| Reviews, research, and test or benchmark design from several model families | CAO |
| Parallel implementers that each need their own git worktree | NTM `--worktrees` |
| Watching and steering agents interactively in tmux panes | NTM |
| Assigning ready beads issues to idle agents | NTM `--assign` (or a CAO conductor using `br scheduler`) |

Both run the same CLIs (Claude Code, Codex, Agy, Grok, Kimi) and both
follow the lane rules below.

## Lane rules

1. **One change, one conductor.** The conductor (a flagship model) owns
   decomposition, fan-in, and the final verdict. Workers report to it.
2. **Disjoint writers.** Never give two writers the same file. Parallel
   implementers either own disjoint file sets in the shared tree or work in
   separate worktrees. Reviewers are read-only.
3. **Task packets.** Every worker gets a packet (template below). No packet,
   no work.
4. **Different families for reviews.** A code review gate uses at least two
   reviewers from different providers (for example Claude and Codex), plus
   Grok or Kimi as the adversarial lane for XACML semantics. The author of
   the code never reviews it.
5. **Evidence or it did not happen.** Each lane's final report becomes a run
   under the change's `evidence/` (or `research/`), including failed and
   empty lanes (docs/ai/OPENSPEC.md "Evidence"). A pane transcript is not
   evidence.
6. **Verdicts come from sources, not votes.** Resolve disagreements against
   the XACML specification, live source, and test or benchmark output.
7. **A launched agent is not a finished lane.** Wait for each worker's final
   report or `BLOCKER:`; an idle pane or a queued message is not a result.
8. **Nobody commits.** Workers never stage, commit, push, merge, or switch
   branches. Merging worktrees is a maintainer decision.
9. **Clean up.** Shut down every session when its lanes are done. Idle agent
   CLIs and their MCP servers hold several GB of memory each; keep a swarm
   to 6 agents or fewer and never leave sessions idle overnight.

## Lane recipes

| Swarm | Lanes (profiles) | Output |
| --- | --- | --- |
| Plan | 1 `planner` writes the change; 1-2 `arch_review` from other families review it | change artifacts; `evidence/reviews/plan/` |
| Implement a task group | 1 `conductor`; 1-3 `implementer` lanes with disjoint files (NTM worktrees when files overlap); 1 `test_writer` when tests are separable | code and tests; `evidence/tests/` |
| Review a task group | `code_review` x2 (different families), `test_review` x1, adversarial semantics lane (`code_review_grok` or `code_review_kimi`), `arch_review` for API or type-system work | `evidence/reviews/<lane>/` |
| Test | `test_writer` per module (disjoint modules), 1 `test_review` | tests; `evidence/tests/`, `evidence/reviews/tests/` |
| Benchmark | 1 `jmh` writes and runs benchmarks, 1 `jmh` from another family reviews the method and numbers | `evidence/benchmarks/jmh/`, `evidence/reviews/performance/` |
| Research | 2-4 read-only lanes (`arch_review` or `planner`), each with one question | `research/<lane>-<topic>.md`, `research/synthesis.md` |

Benchmark runs are never parallel: one JMH run at a time, with no build or
other benchmark on the machine (docs/ai/BENCHMARKS.md).

## Task packet

Every worker prompt carries this packet. Keep it short and exact.

```text
Change: openspec/changes/<change-id>   Tasks: <N.M, ...>   Beads: <ids>
Role: <profile name>   Reply to: <conductor terminal id or "this pane">
Goal: <one sentence>
Files you own: <paths or globs>   Files you must not touch: everything else
Read first: AGENTS.md, docs/ai/<relevant>.md, the change's proposal,
  design (D<n>), and specs/<capability>/spec.md
Do: <steps>
Check: <exact commands, JDK, expected result>
Evidence: openspec/changes/<change-id>/evidence/<kind>/<NNN>-<scope>/
Report: profile and model, summary, changes or findings with evidence,
  commands run and results, residual risk; "BLOCKER: <reason>" if stuck.
```

## CAO procedure

Profiles: `.agents/cao-profiles/policy4j_<category>_<variant>.md`, generated
by `scripts/gen-cao-profiles.py` (matrix, models, and permission modes in
`.agents/CAO_PROFILES.md`). Categories: `planner`, `conductor`,
`implementer`, `arch_review`, `code_review`, `test_writer`, `test_review`,
`jmh`.

Preflight:

```sh
cao --version
cao session list                          # the CAO server answers
cao profile show policy4j_conductor_claude_opus
cao profile validate .agents/cao-profiles/<profile>.md
```

If `cao profile show` cannot find a `policy4j_*` profile, the local CAO
settings do not include `.agents/cao-profiles` in `agents.extra_dirs`. Ask
the maintainer to add it; do not copy profiles into the user store.

Launch a conductor, from the repository root:

```sh
cao launch --agents policy4j_conductor_claude_opus \
  --session-name policy4j-<change-id>-<group> \
  --working-directory "$(git rev-parse --show-toplevel)" \
  --headless --auto-approve --async "<conductor packet: change, task group, lanes, reply target>"
```

The conductor starts workers with the CAO `assign` tool (parallel) or
`handoff` (blocking) and receives their reports through `send_message`.
From a driving agent outside CAO, use the CAO ops MCP (`register_peer`,
`launch_session`, `receive_messages`, `ack_messages`) and give the conductor
your peer id as its reply target.

Monitor and finish:

```sh
cao session status <session> --workers --json    # every lane and its state
cao session send <session> "<message>"           # talk to the conductor
cao agent result <terminal-id>                   # a worker's last response
cao shutdown --session <session>                 # when every lane is terminal
```

Known provider issues (details in `.agents/CAO_PROFILES.md`):

- Grok stops on a directory trust prompt and CAO misreads its idle screen
  as busy; an operator answers `y` and types the task into the pane.
- Agy times out after 10 seconds on shell startup in a large dirty tree; run
  its read-only lanes from a clean snapshot or give it narrow file lists.
- Codex conductors need the explicit `mcp__cao-mcp-server__assign`,
  `__handoff`, and `__send_message` tools in the launch's allowed tools.
- Claude and Grok read `.mcp.json`; approve the project servers once so a
  headless launch does not stop on the approval prompt.
- Codex sandboxes (CAO `plan`/`acceptEdits`, NTM `workspace-write`) are
  bypassed by any exec-policy rule with `decision="allow"`: such commands run
  outside the sandbox without asking. The user rules allow bare `rtk bash`,
  `rtk git`, `rtk mvn`, `rtk gh`, and `rtk pkill`, so treat Codex read-only
  lanes as read-only by prompt until those rules change.
- NTM quotes launch values POSIX-style, which fish does not honor: a
  backslash-quote in a persona value can inject a command into the pane
  shell. Spawn only personas from trusted repositories.

## NTM procedure

NTM maps a session name to `<projects_base>/<name>`. Swarms for this
project (policy4j) run in the integration worktree
`<checkouts>/xacml4j-worktrees/policy4j` (branch
`integration/modernize-core-jdk28-valhalla`), so set the base to the
worktree directory and name sessions `policy4j`:

```sh
export NTM_PROJECTS_BASE="$(dirname "$(git rev-parse --show-toplevel)")"   # run from the worktree
```

Never set `NTM_PROJECTS_BASE` to the parent of the worktrees directory for a
`policy4j` session: a separate, unrelated repository named `policy4j` lives
there, and NTM would start the agents in it. The original checkout
(`xacml4j`, branch `feature_xacml_20`) is the donor tree; its work is
preserved on branch `wip/feature_xacml_20-donor`.

Project personas live in `.ntm/personas.toml`, generated with the CAO
profiles by `scripts/gen-cao-profiles.py` (matrix and limits in
`.agents/CAO_PROFILES.md`, "NTM personas"). Each category has a base
persona `policy4j_<category>` (Claude Opus) and variants
`policy4j_<category>_<variant>` for Claude Fable, Codex Astra, Luna, and
Terra, Codex Sol 6.1, and Agy. Grok and Kimi have no persona. The same role prompts live in
`.ntm/roles/policy4j_<category>.md`. List them with `ntm personas list`.

Spawn one labeled session per change and group. Session names come out as
`policy4j--<label>`:

```sh
# Implementers in isolated worktrees (branch ntm/<session>/<agent> each)
ntm spawn policy4j --label <change>-g<N> --profile-set policy4j-implement-team --worktrees --verify-boot

# Review gate in the shared tree, plus Grok and Kimi lanes without personas
ntm spawn policy4j --label <change>-g<N>-review --profile-set policy4j-review-team \
  --grok=1:grok-4.7:xhigh --kimi=1:kimi-code/k3 --verify-boot

# Any mix of personas
ntm spawn policy4j --label <label> --persona=policy4j_test_writer_claude_opus \
  --persona=policy4j_test_review_codex_terra --verify-boot
```

Team presets: `policy4j-plan-team`, `policy4j-implement-team`,
`policy4j-review-team`, `policy4j-test-team`, `policy4j-bench-pair`.

Send packets from files, one per pane. Claude and Codex panes load their
persona prompt at launch; put the role prompt in front for Agy, Grok, and
Kimi panes. (Codex needs the NTM Codex launch template that passes
`-c developer_instructions`; see `.agents/CAO_PROFILES.md`, "NTM personas".)
Then wait:

```sh
ntm send policy4j--<label> --pane=1 --file <packet-1.md>        # Claude or Codex pane
ntm send policy4j--<label> --pane=3 --base-prompt-file .ntm/roles/policy4j_code_review.md --file <packet-3.md>   # Agy, Grok, Kimi
ntm wait policy4j--<label> --until=idle --timeout=60m --exit-on-error
ntm status policy4j--<label>
ntm save policy4j--<label> --all -o "${TMPDIR:-/tmp}/ntm-<label>"   # raw pane dumps for fan-in
```

- A first Agy launch in this directory stops on Antigravity's "Do you
  trust the contents of this project?" prompt; answer it once in the pane.
- NTM runs its Agy agent on Gemini 3.8 Flash (High) only. For Gemini 3.1 Pro
  (the CAO Agy model) spawn `--agyx=1:gemini-3.1-pro-high` and send the role
  with `--base-prompt-file`.
- NTM keeps runtime state in `.ntm/` (pids, rate limits, spawn prompts);
  `.ntm/.gitignore` versions only the generated files.
- `ntm save` writes to `./outputs` by default; always pass `-o` outside
  the repository. Pane dumps are raw input for the conductor, not
  evidence: copy only each lane's final report into the change.
- `ntm conflicts policy4j--<label>` shows files that several agents
  touched; stop and reassign if it lists anything.
- `ntm spawn ... --assign` hands ready beads issues to agents; use it only
  when the ready issues touch disjoint files.
- Worktree results come back only through the maintainer:
  `ntm worktrees list`, review, then `ntm worktrees merge <agent>` when the
  maintainer asks. Merging is a git write.
- Do not run `ntm init` in this repository without the maintainer's
  approval; it installs git hooks.
- Finish with `ntm kill policy4j--<label>` and `ntm worktrees clean` once the
  maintainer has taken what they want from the worktrees.

## Conductor checklist

1. Change validated and approved; `br ready` shows the group's tasks.
2. Lanes chosen from the design's Execution Plan; file ownership written
   into each packet.
3. Every lane reached a final report or `BLOCKER:`.
4. Each report is stored as a run and indexed in `evidence/README.md`,
   failed lanes included.
5. Contradictions resolved with sources; open findings listed.
6. Gate result reported to the maintainer; sessions shut down.
