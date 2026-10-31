#!/bin/bash
# The reviewed landing procedure (evidence/builds/087-single-branch-landing-gate/
# landing-procedure.md) as it ran on 2026-10-26 (paths replaced by placeholders).
# Usage: landing-run.sh <step>. Each step runs in a subshell with set -eu;
# stop() ends it with exit 1.
C="<checkouts>"
M="$C/xacml4j"
W="$C/xacml4j-worktrees/policy4j"
B="<backup>/xacml4j-landing-2026-10-24"
OLD=a0ec46838420052332f4e0495b10f54d437633f3
TAG=archive/feature_xacml_20-2026-10-24
R=xacml-core/src/main/java/org/xacml4j/v30/types/RFC822Name.java
stop() { echo "STOP: $*" >&2; exit 1; }

step1() ( set -eu
  (cd "$W" && mcp.sh -m serena stop) || true
  (cd "$M" && mcp.sh -m serena stop) || true
  if tmux list-panes -a -F '#{pane_current_path}' 2>/dev/null | grep -qF -e "$M" -e "$W"; then stop "a tmux pane still runs in a checkout"; fi
  if pgrep -x br >/dev/null; then stop "br is running"; fi
  test "$(git -C "$M" symbolic-ref HEAD)" = refs/heads/feature_xacml_20 || stop "main checkout is not on feature_xacml_20"
  test "$(git -C "$M" rev-parse HEAD)" = "$OLD" || stop "feature_xacml_20 moved"
  echo "step 1 ok" )

step2() ( set -eu
  mkdir -p "$B"
  git -C "$M" bundle create "$B/xacml4j.bundle" --all
  git -C "$M" bundle verify "$B/xacml4j.bundle" >/dev/null
  rsync -a "$M/.beads/" "$B/beads-before-flush/"
  (cd "$M" && git status --porcelain --untracked-files=all | cut -c4- > "$B/main-dirty.txt")
  (cd "$M" && rsync -a --files-from="$B/main-dirty.txt" ./ "$B/main-dirty/")
  (cd "$M" && br sync --flush-only)
  (cd "$M" && git status --porcelain --untracked-files=all | cut -c4- > "$B/main-dirty.txt")
  (cd "$W" && git ls-files --others --ignored --exclude-standard -- openspec | grep '\.log$' > "$B/worktree-logs.txt")
  test "$(grep -c . "$B/worktree-logs.txt")" -eq 146 || stop "expected 146 ignored logs in the worktree"
  echo "step 2 ok: $(du -sh "$B" | cut -f1) in backup" )

step3a() ( set -eu
  cd "$W"
  git add -- .gitignore .agents/CAO_PROFILES.md .agents/skills/desktop-deep-research .beads \
    docs/ai/SWARMS.md docs/ai/TESTING.md scripts/gen-cao-profiles.py openspec/changes/add-desktop-deep-research \
    openspec/changes/modernize-core-for-jdk28-valhalla/design.md \
    openspec/changes/modernize-core-for-jdk28-valhalla/tasks.md \
    openspec/changes/modernize-core-for-jdk28-valhalla/evidence
  test -z "$(git diff --cached --name-only -- 'xacml-*' pom.xml .serena)" || stop "product or Serena file staged"
  git commit -q -m "Land the integration line as feature_xacml_20 (D1, task 10.1)"
  echo "step 3a ok: $(git rev-parse --short HEAD)" )

step3b() ( set -eu
  cd "$W"
  git add -- xacml-core/src/main/java/org/xacml4j/v30/TypeCapability.java \
    xacml-core/src/main/java/org/xacml4j/v30/TypeCapabilityFactory.java \
    xacml-core/src/main/java/org/xacml4j/v30/types/DynamicType.java \
    xacml-core/src/main/java/org/xacml4j/v30/types/TypeFactory.java \
    xacml-core/src/main/java/org/xacml4j/v30/types/TypeToString.java \
    xacml-core/src/main/java/org/xacml4j/v30/types/ValueType.java \
    xacml-json/src/main/java/org/xacml4j/v30/marshal/json/TypeToGSon.java \
    xacml-xml/src/main/java/org/xacml4j/v30/xml/TypeToXacml30.java
  git commit -q -m "Tighten the generic bounds of ValueType.ofAny and TypeCapability (policy4j-the.5.6)"
  echo "step 3b ok: $(git rev-parse --short HEAD)" )

step4() ( set -eu
  NEW=$(git -C "$W" rev-parse HEAD)
  # Maintainer ruling 2026-10-26: carry every uncommitted product edit of the worktree
  # (tracked, modified) to the main checkout; anything else stops the landing.
  test -z "$(git -C "$W" status --porcelain | grep -v -e '^ M \.serena/project\.yml$' -e '^ M xacml-[a-z-]*/' || true)" \
    || stop "uncommitted files other than modified product files and .serena/project.yml"
  git -C "$W" diff --name-only -- 'xacml-*' > "$B/carry.txt"
  echo "carry: $(grep -c . "$B/carry.txt") files"
  git -C "$W" diff --quiet "$OLD" "$NEW" -- .beads .serena/project.yml openspec/changes/add-desktop-deep-research \
    || stop "a file that the main checkout has edited differs between OLD and NEW"
  git -C "$M" merge-base --is-ancestor origin/feature_xacml_20 "$NEW" || stop "a later push would not be a fast-forward"
  echo "$NEW" > "$B/NEW"
  echo "step 4 ok: NEW=$NEW" )

step5() ( set -eu
  if git -C "$M" rev-parse -q --verify "refs/tags/$TAG" >/dev/null; then
    test "$(git -C "$M" rev-parse "$TAG^{commit}")" = "$OLD" || stop "tag exists on another commit"
  else
    git -C "$M" tag -a "$TAG" "$OLD" -m "feature_xacml_20 before the D1 landing"
  fi
  echo "step 5 ok" )

step6() ( set -eu
  NEW=$(cat "$B/NEW")
  git -C "$M" reset --keep "$NEW" || stop "reset --keep refused; nothing changed"
  test "$(git -C "$M" rev-parse HEAD)" = "$NEW" || stop "HEAD is not NEW"
  (cd "$M" && git status --porcelain --untracked-files=all | cut -c4-) | diff - "$B/main-dirty.txt" >/dev/null \
    || stop "the set of dirty files in the main checkout changed"
  echo "step 6 ok" )

step7() ( set -eu
  (cd "$W" && rsync -a --files-from="$B/worktree-logs.txt" ./ "$M/")
  test "$(cd "$M" && git ls-files --others --ignored --exclude-standard -- openspec | grep -c '\.log$')" -eq 146 \
    || stop "expected 146 logs in the main checkout"
  if ! test -s "$B/carry.txt"; then
    echo "nothing to carry"
  else
    git -C "$W" diff --binary -- $(cat "$B/carry.txt") > "$B/worktree-edits.patch"
    test -z "$(git -C "$M" diff --name-only -- $(cat "$B/carry.txt"))" || stop "main checkout already edits a carried file"
    git -C "$M" apply --binary "$B/worktree-edits.patch"
    while IFS= read -r f; do
      git -C "$M" diff --quiet -- "$f" && stop "not carried: $f"
    done < "$B/carry.txt"
    echo "carried: $(grep -c . "$B/carry.txt") files"
  fi
  echo "step 7 ok" )

step8() ( set -eu
  # no edit may have changed since the patch of step 7 was taken
  test "$(git -C "$W" diff --name-only -- 'xacml-*')" = "$(cat "$B/carry.txt")" || stop "the set of edited files changed after step 7"
  git -C "$W" diff --binary -- $(cat "$B/carry.txt") | cmp -s - "$B/worktree-edits.patch" || stop "worktree edits changed after step 7"
  git -C "$W" restore -- .serena/project.yml
  if test -s "$B/carry.txt"; then
    git -C "$W" restore -- $(cat "$B/carry.txt")
  fi
  test -z "$(git -C "$W" status --porcelain)" || stop "worktree still dirty"
  git -C "$M" worktree remove "$W"
  git -C "$M" worktree list
  echo "step 8 ok" )

"step$1"
