# Landing procedure (maintainer only)

Replaces `feature_xacml_20` with the integration line (design D1, task 10.1).
Local only: do not push until bead `policy4j-the.12` (home paths) is closed.
Revised after the adversarial review of this procedure (rv097, Grok).

Run it in bash or zsh, from a directory outside both checkouts (step 1
stops if any tmux pane is inside one), one block at a time. Each block runs in a subshell
with `set -eu`: a failing command or a failed check prints `STOP: <reason>`
and ends the block. Do not run the next block after a STOP. Nothing before
step 6 moves a branch.

```sh
C="<checkouts>"                           # the directory that holds both checkouts
M="$C/xacml4j"                            # main checkout, branch feature_xacml_20
W="$C/xacml4j-worktrees/policy4j"         # integration worktree
B="<backup>/xacml4j-landing-2026-10-24"   # outside both checkouts
OLD=a0ec46838420052332f4e0495b10f54d437633f3
TAG=archive/feature_xacml_20-2026-10-24
R=xacml-core/src/main/java/org/xacml4j/v30/types/RFC822Name.java
stop() { echo "STOP: $*" >&2; exit 1; }
```

## 1. Quiesce

Kill every agent session that runs in `$M` or `$W` (`ntm list`, then
`ntm kill <session>`), close both checkouts in the IDE, and make sure the
session that owns `$M`'s uncommitted work is idle. Then:

```sh
( set -eu
  (cd "$W" && mcp.sh -m serena stop) || true
  (cd "$M" && mcp.sh -m serena stop) || true
  if tmux list-panes -a -F '#{pane_current_path}' 2>/dev/null | grep -qF -e "$M" -e "$W"; then stop "a tmux pane still runs in a checkout"; fi
  if pgrep -x br >/dev/null; then stop "br is running"; fi
  test "$(git -C "$M" symbolic-ref HEAD)" = refs/heads/feature_xacml_20 || stop "main checkout is not on feature_xacml_20"
  test "$(git -C "$M" rev-parse HEAD)" = "$OLD" || stop "feature_xacml_20 moved"
  echo "step 1 ok" )
```

## 2. Back up, then export the tracker

```sh
( set -eu
  mkdir -p "$B"
  git -C "$M" bundle create "$B/xacml4j.bundle" --all
  git -C "$M" bundle verify "$B/xacml4j.bundle"
  rsync -a "$M/.beads/" "$B/beads-before-flush/"      # issues.jsonl, beads.db*, .br_history
  (cd "$M" && git status --porcelain --untracked-files=all | cut -c4- > "$B/main-dirty.txt")
  (cd "$M" && rsync -a --files-from="$B/main-dirty.txt" ./ "$B/main-dirty/")
  (cd "$M" && br sync --flush-only)
  (cd "$W" && git ls-files --others --ignored --exclude-standard -- openspec | grep '\.log$' > "$B/worktree-logs.txt")
  test "$(grep -c . "$B/worktree-logs.txt")" -eq 146 || stop "expected 146 ignored logs in the worktree"
  echo "step 2 ok" )
```

## 3. Commit in the worktree

```sh
( set -eu
  cd "$W"
  git add -- .gitignore .agents/CAO_PROFILES.md .agents/skills/desktop-deep-research .beads \
    docs/ai/SWARMS.md docs/ai/TESTING.md scripts/gen-cao-profiles.py openspec/changes/add-desktop-deep-research \
    openspec/changes/modernize-core-for-jdk28-valhalla/design.md \
    openspec/changes/modernize-core-for-jdk28-valhalla/tasks.md \
    openspec/changes/modernize-core-for-jdk28-valhalla/evidence
  test -z "$(git diff --cached --name-only -- 'xacml-*' pom.xml .serena)" || stop "product or Serena file staged"
  git commit -m "Land the integration line as feature_xacml_20 (D1, task 10.1)"
  echo "step 3a ok" )
```

Then the 5.6 work as its own commit (8 files; reviewed in rv098):

```sh
( set -eu
  cd "$W"
  git add -- xacml-core/src/main/java/org/xacml4j/v30/TypeCapability.java \
    xacml-core/src/main/java/org/xacml4j/v30/TypeCapabilityFactory.java \
    xacml-core/src/main/java/org/xacml4j/v30/types/DynamicType.java \
    xacml-core/src/main/java/org/xacml4j/v30/types/TypeFactory.java \
    xacml-core/src/main/java/org/xacml4j/v30/types/TypeToString.java \
    xacml-core/src/main/java/org/xacml4j/v30/types/ValueType.java \
    xacml-json/src/main/java/org/xacml4j/v30/marshal/json/TypeToGSon.java \
    xacml-xml/src/main/java/org/xacml4j/v30/xml/TypeToXacml30.java
  git commit -m "Tighten the generic bounds of ValueType.ofAny and TypeCapability (policy4j-the.5.6)"
  echo "step 3b ok" )
```

The repository config has `commit.gpgsign=true` and `tag.gpgsign=true`:
steps 3 and 5 need the signing key; a signing failure stops the block before
any branch moves.

`RFC822Name.java` is a separate maintainer edit. Either fix its indentation
(tabs) and commit it now, or leave it uncommitted; step 7 carries it over.
Never commit `.serena/project.yml`: `$M` holds the same edited blob, and a
committed copy would make step 6 abort.

## 4. Check the candidate

```sh
NEW=$(git -C "$W" rev-parse HEAD)
( set -eu
  test -z "$(git -C "$W" status --porcelain | grep -v -e ' .serena/project.yml$' -e " $R\$" || true)" \
    || stop "unexpected uncommitted files in the worktree"
  git -C "$W" diff --quiet "$OLD" "$NEW" -- .beads .serena/project.yml openspec/changes/add-desktop-deep-research \
    || stop "a file that the main checkout has edited differs between OLD and NEW"
  git -C "$M" merge-base --is-ancestor origin/feature_xacml_20 "$NEW" || stop "a later push would not be a fast-forward"
  echo "step 4 ok: NEW=$NEW" )
```

## 5. Tag the old tip

```sh
( set -eu
  if git -C "$M" rev-parse -q --verify "refs/tags/$TAG" >/dev/null; then
    test "$(git -C "$M" rev-parse "$TAG^{commit}")" = "$OLD" || stop "tag exists on another commit"
  else
    git -C "$M" tag -a "$TAG" "$OLD" -m "feature_xacml_20 before the D1 landing"
  fi
  echo "step 5 ok" )
```

## 6. Move the branch in the main checkout

```sh
( set -eu
  git -C "$M" reset --keep "$NEW" || stop "reset --keep refused; nothing changed"
  test "$(git -C "$M" rev-parse HEAD)" = "$NEW" || stop "HEAD is not NEW"
  (cd "$M" && git status --porcelain --untracked-files=all | cut -c4-) | diff - "$B/main-dirty.txt" >/dev/null \
    || stop "the set of dirty files in the main checkout changed"
  echo "step 6 ok" )
```

`git branch -f` refuses (the branch is checked out in `$M`); `git update-ref`
would leave `$M`'s index on the old tree; `reset --hard` would drop `$M`'s
edits. `reset --keep` keeps them because NEW has those files unchanged.

## 7. Carry what lives only in the worktree

```sh
( set -eu
  (cd "$W" && rsync -a --files-from="$B/worktree-logs.txt" ./ "$M/")
  test "$(cd "$M" && git ls-files --others --ignored --exclude-standard -- openspec | grep -c '\.log$')" -eq 146 \
    || stop "expected 146 logs in the main checkout"
  if git -C "$W" diff --quiet -- "$R"; then
    echo "RFC822Name.java: nothing to carry"
  else
    git -C "$W" diff -- "$R" > "$B/rfc822name.patch"
    git -C "$M" apply "$B/rfc822name.patch"
    git -C "$M" diff --quiet -- "$R" && stop "RFC822Name.java patch applied nothing"
    echo "RFC822Name.java carried over"
  fi
  echo "step 7 ok" )
```

## 8. Remove the worktree

```sh
( set -eu
  git -C "$W" restore -- .serena/project.yml        # $M holds the same edited blob
  if ! git -C "$W" diff --quiet -- "$R"; then
    git -C "$M" diff --quiet -- "$R" && stop "RFC822Name.java is not in the main checkout"
    git -C "$W" restore -- "$R"
  fi
  test -z "$(git -C "$W" status --porcelain)" || stop "worktree still dirty"
  git -C "$M" worktree remove "$W"                  # never --force
  git -C "$M" worktree list
  echo "step 8 ok" )
```

## 9. After the landing

- Swarms run in `$M` as session `xacml4j` (docs/ai/SWARMS.md). Restart the
  agent clients and Serena there; refresh the tgrep index (`tgrep index .`).
- The owner of `$M`'s uncommitted work commits the tracker export and the
  `add-desktop-deep-research` runs 021-028 on `feature_xacml_20`.
- Keep `wip/feature_xacml_20-donor` and the tag until task 7.3.
  `wip/beads-desktop-research-snapshot` holds no unique content (rv094 L1,
  L5); delete it with `git branch -D` when you want.
- Push only after `policy4j-the.12`. Do not push the tag unless its commits
  are scrubbed as well.
