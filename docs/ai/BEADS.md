# Beads (`br`) Work Tracking

Beads is the work queue. OpenSpec says what to build and how to prove it;
beads says what is ready, who holds it, and what it blocks. Issues live in
`.beads/` (prefix `policy4j`). `issues.jsonl`, `config.yaml`, and
`metadata.json` are tracked; the SQLite database is local.

Issues created before 2026-09-29 were renamed from the old `xacml` prefix;
each keeps its old id in `external_ref` (`br show <id>`).

`br` never runs git. Do not use `br agents --add`; its template tells agents
to commit and push every session, which this project forbids.

## Mapping to OpenSpec

| OpenSpec | Beads |
| --- | --- |
| Change `<change-id>` | One epic; the change id is in its description |
| Task group `## N.` | Child epic `<epic>.N` (optional for small changes) |
| Task `N.M` | Issue `<epic>.N.M`, child of the group epic |
| Task order | `br dep add <later> <earlier>` |
| Gate task | Issue whose close reason names the evidence run |

`--parent` gives children sequential ids (`<epic>.1`, `<epic>.2`, ...), so
create groups and tasks in `tasks.md` order and the ids match the task
numbers:

```sh
br create --type epic --priority 1 --title "<change-id>" \
  --description "OpenSpec change openspec/changes/<change-id>"
br create --type epic --parent <epic> --title "N. <group name>"
br create --type task --parent <epic>.N --priority 2 \
  --title "N.M <task summary>" --description "tasks.md N.M; check: <command>"
br dep add <epic>.N.M <epic>.N.K        # N.M waits for N.K
```

Then check the graph: `br ready` shows only the first actionable task(s),
and `br blocked` shows the rest with their blockers.

## Session start

```sh
br ready --json                    # actionable work, unblocked, not deferred
br coordination status --json      # stale or hidden in-progress claims
br show <id> --json                # details, parent, dependencies
```

A swarm conductor uses `br scheduler --format json --limit <n>` to rank
ready work for parallel lanes. Assign tasks that touch different files to
different lanes.

## Claim, work, close

```sh
br update <id> --claim --actor <agent-name>     # assignee + in_progress, atomic
br update <id> --append-notes "files: <paths>; lane: <lane>"
br close <id> --reason "Completed: <proof, e.g. evidence/tests/004-core-migration>"
br sync --flush-only                            # export the DB to issues.jsonl
```

- Claim before you edit. One claim per agent at a time.
- Record the file scope in notes when you share the tree with other agents.
- Close only when the OpenSpec task is checked and its evidence is indexed.
  Put the evidence path in the close reason.
- If blocked, leave the issue in progress, append the blocker to notes, and
  report it; do not close it.
- Found work outside the task: `br q "<title>"` (quick capture) or
  `br create`, linked to the epic, instead of expanding the current task.
- Close an epic when every child is closed: `br epic close-eligible`.

## Session end

Run `br sync --flush-only` so `issues.jsonl` is current. Do not stage,
commit, or push; the maintainer commits code and `.beads/` changes
together.

## Useful queries

```sh
br epic status                      # progress per epic
br list --status in_progress        # who holds what
br graph <id>                       # what an issue unblocks
br search "<text>"                  # titles, descriptions, comments
br stale                            # issues without recent activity
br lint                             # issues missing template sections
```
