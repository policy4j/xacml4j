# 089: history scrub before the first push (bead policy4j-the.12)

Date: 2026-10-26. Run by the conductor on the maintainer's instruction ("Yes
I approve rewrite"). Scope: the 35 unpushed commits
`origin/feature_xacml_20..feature_xacml_20` (`d78d9586..c5128dab`). The
commands are in `command.txt`; the old-to-new map is `commit-map.txt`.

Result: REWRITTEN, local only. No file outside `openspec/` and `.beads/`
changed, so the gate of run 088 stays valid for the new commits.

| Ref | Before | After |
| --- | --- | --- |
| `feature_xacml_20` | `c5128dab` | `f9d99aed` (signed) |
| `integration/modernize-core-jdk28-valhalla` | `b9d3a09a` | `265be3a0` |
| `origin/feature_xacml_20` | `d78d9586` | unchanged; an ancestor of `f9d99aed`, so a push is a fast-forward |
| tag `archive/feature_xacml_20-2026-10-24`, `wip/feature_xacml_20-donor`, `wip/beads-desktop-research-snapshot` | - | unchanged; local only, they still hold the old text (206, 1, and 6 files at their tips): do not push them |
| `master`, `refs/stash` | - | unchanged; no hit at their tips |

## Found

None of these is in `origin/feature_xacml_20` (`git grep`, 0 files).

- The home directory path, which contains the account name: 202 files at
  the old tip (Surefire XML reports under `evidence/tests`, review
  receipts, `source_repo_path` in `.beads/issues.jsonl`). In 3 terminal
  receipts the path wraps across two lines.
- The account name alone: Surefire `user.name`; tracker `created_by`,
  `dependencies[].created_by`, `comments[].author`.
- The proxy exclusion lists of the corporate network (`http.nonProxyHosts`,
  `ftp.nonProxyHosts`, `socksNonProxyHosts`) in 198 Surefire reports:
  internal address ranges and company domains.
- The host name, address, and repository ids of an internal Maven mirror in
  two dependency-tree logs of run 005.
- A committed Python bytecode file,
  `evidence/tests/023-conformance-manifest/__pycache__/write-evidence.cpython-314.pyc`,
  which holds the path.

## Rules

Applied in this order to every text file version in the 35 commits:

1. The three proxy lists become `REDACTED`.
2. The home path, also when wrapped, becomes `$HOME`.
3. The account name becomes `maintainer`.
4. The mirror host becomes `artifactory.example.com`, its address
   `192.0.2.1` (a documentation address, RFC 5737), and its repository ids
   `libs-snapshots` and `mirror-snapshots`.
5. The `.pyc` file is removed; `.gitignore` now ignores `__pycache__/`.
6. A reference to a rewritten commit (7 to 40 hex characters, outside
   `*/src/`) becomes the new hash of that commit, at the same length: 115
   places in 30 files (the evidence index, runs 078, 082, 085 to 088,
   reviews rv094 to rv098, `tasks.md`, the tracker). Every one was checked
   against the map; no other hex string matched.
7. In `tests/009-dual-engine/01-before.json`, the sha256 values of the two
   rewritten dependency-tree logs become the sha256 of the new files.

The scripts are kept outside the repository with the backup: their rules
name the internal hosts.

## Commits

29 commits were made again with `git commit-tree`, each with its original
author, committer, dates, and message. Each one that was signed is signed
again with the same key. The five unsigned commits of 2023 and `befdd116`
have no changed file and keep their hashes. Signature status is as before:
30 good (one key), 5 unsigned.

## Checks

First in a scratch clone (an unsigned dry run, then the signed run), then
in the checkout:

- `git grep -i` over every commit of the new range for the account name,
  the company name, the mirror ids, and the internal addresses: 0 files.
  Commit messages and identities: 0 hits.
- `git diff` from the old tip to the new tip outside `openspec/` and
  `.beads/`: empty.
- The 198 Surefire XML files parse; `.beads/issues.jsonl` parses, with 177
  rows and the same ids in the same order.
- No reference to a rewritten old hash is left; the two sha256 receipts
  match their files.
- `git log --format=%G?`: 30 G, 5 N.

## Tracker

The tracker database is the source of every export, so it was scrubbed
too. An online backup of the database got the same rules on the exported
columns (`issues.created_by`, `source_repo_path`, `description`,
`close_reason`; `dependencies.created_by`; `comments.author`, `text`) and
replaced the live files, which were moved to the backup. The local audit
events (903) and claim records (139) are kept; `br sync --import-only --force`
was not used because it deletes them. `br sync --flush-only --force` then
writes a file byte-identical to the committed export, and
`br config set actor maintainer` keeps the account name out of new rows.

## br upgrade

br 0.7.4 wrote the absolute checkout path into `source_repo_path` of each
issue it created. From 0.7.5, br keeps that field in the database and does
not export it (upstream changelog, beads_rust #528). On the maintainer's
instruction, br was upgraded to 0.7.6 (Homebrew) and the reviewed migration
was applied (`br sync --migrate-source-repo-path`, dry run, then `--apply`
with its plan hash). It removed `source_repo_path` from all 177 rows and
changed no other field. A probe in a scratch copy showed that a new issue
and a new comment record `maintainer` and no path. `.beads/.gitignore` now
also ignores `.br-wal-index-*/`, a pattern that br 0.7.6 expects.

## Not done

- No push. The old commits stay in the backup bundle and in the reflog.
