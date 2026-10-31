#!/usr/bin/env bash
#
# Readiness check for the agent code-intelligence tools in this repository.
#
#   scripts/agent-doctor.sh [all|ast-grep|ripwire|tgrep]
#
# Each tool prints PASS or FAIL. The exit status is non-zero when a selected
# check fails. A WARN line names a limit of a PASS and does not change the
# exit status: the tgrep check prints one when files are newer than its
# snapshot index.

set -Eeuo pipefail
shopt -s inherit_errexit 2>/dev/null || true
IFS=$'\n\t'

ROOT="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.." && pwd -P)"

pass() { printf 'PASS %s\n' "$*"; }
warn() { printf 'WARN %s\n' "$*"; }
fail() { printf 'FAIL %s\n' "$*" >&2; }

need() {
  local cmd missing=0
  for cmd in "$@"; do
    if ! command -v "${cmd}" >/dev/null 2>&1; then
      fail "${cmd} is not on PATH"
      missing=1
    fi
  done
  return "${missing}"
}

check_ast_grep() {
  local config="${ROOT}/sgconfig.yml"
  need ast-grep ast-grep-mcp-server || return 1
  if ! ast-grep test --config "${config}" >/dev/null; then
    fail "ast-grep: rule tests failed (ast-grep test --config sgconfig.yml)"
    return 1
  fi
  if ! ast-grep scan --config "${config}" "${ROOT}" >/dev/null; then
    fail "ast-grep: project scan failed (ast-grep scan --config sgconfig.yml)"
    return 1
  fi
  pass "ast-grep: rule tests and project scan"
}

check_ripwire() {
  local out
  need ripwire || return 1
  if ! out="$(ripwire "${ROOT}" --doctor)"; then
    fail "ripwire: --doctor exited non-zero"
    return 1
  fi
  if [[ ! "${out}" =~ \<doctor\ checks=\"([0-9]+)\"\ passed=\"([0-9]+)\" ]]; then
    fail "ripwire: unparseable --doctor output"
    return 1
  fi
  if [[ "${BASH_REMATCH[1]}" != "${BASH_REMATCH[2]}" ]]; then
    fail "ripwire: doctor passed ${BASH_REMATCH[2]} of ${BASH_REMATCH[1]} checks"
    return 1
  fi
  pass "ripwire: doctor ${BASH_REMATCH[2]}/${BASH_REMATCH[1]}"
}

# Count the files that git does not ignore and that changed after the tgrep
# index was written. tgrep honors .gitignore, so these are the files whose
# current content an indexed search can miss.
tgrep_newer_files() {
  local meta="$1" file count=0
  while IFS= read -r -d '' file; do
    if [[ "${ROOT}/${file}" -nt "${meta}" ]]; then
      count=$((count + 1))
    fi
  done < <(git -C "${ROOT}" ls-files -co --exclude-standard -z)
  printf '%s\n' "${count}"
}

# Without `tgrep serve`, the index is a snapshot: edits and new files made
# after `tgrep index` are invisible to indexed searches. A snapshot that is
# older than files in the tree still passes, with a WARN.
check_tgrep() {
  local status meta="${ROOT}/.tgrep/meta.json" age="" newer=0
  local mode="snapshot (refresh: tgrep index . | live: tgrep serve .)"
  need tgrep git || return 1
  if [[ ! -f "${meta}" ]]; then
    fail "tgrep: no index; run: tgrep index ${ROOT}"
    return 1
  fi
  if ! tgrep -q -F '<groupId>org.xacml4j</groupId>' "${ROOT}"; then
    fail "tgrep: indexed canary search found no match"
    return 1
  fi
  status="$(cd -- "${ROOT}" && tgrep status 2>&1 || true)"
  if [[ "${status}" =~ Updated:[[:space:]]+([0-9]+[a-z]+[[:space:]]ago) ]]; then
    age=", updated ${BASH_REMATCH[1]}"
  fi
  if [[ "${status}" =~ Server:[[:space:]]+running ]]; then
    pass "tgrep: index present${age}, canary matched, mode live (tgrep serve)"
    return 0
  fi
  pass "tgrep: index present${age}, canary matched, mode ${mode}"
  newer="$(tgrep_newer_files "${meta}")"
  if ((newer > 0)); then
    warn "tgrep: ${newer} files are newer than the snapshot index; indexed searches do not see their changes (run: tgrep index .)"
  fi
}

main() {
  local target="${1:-all}" rc=0

  case "${target}" in
    all)
      check_ast_grep || rc=1
      check_ripwire || rc=1
      check_tgrep || rc=1
      ;;
    ast-grep) check_ast_grep || rc=1 ;;
    ripwire) check_ripwire || rc=1 ;;
    tgrep) check_tgrep || rc=1 ;;
    *)
      printf 'usage: %s [all|ast-grep|ripwire|tgrep]\n' "$0" >&2
      return 2
      ;;
  esac
  return "${rc}"
}

main "$@"
