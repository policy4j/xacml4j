#!/usr/bin/env bash
#
# Readiness check for the agent code-intelligence tools in this repository.
#
#   scripts/agent-doctor.sh [all|ast-grep|ripwire|tgrep|serena]
#
# Each tool prints PASS or FAIL. The exit status is non-zero when a selected
# check fails. The Serena check runs the mcp.sh transport doctor, then a live
# JDTLS canary (get_symbols_overview on one tracked Java file), so a PASS
# covers symbol lookup and not only the HTTP route. With an attach stanza in
# .mcp.json, the check first starts Serena on demand.

set -Eeuo pipefail
shopt -s inherit_errexit 2>/dev/null || true
IFS=$'\n\t'

ROOT="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.." && pwd -P)"
WORKDIR=""

pass() { printf 'PASS %s\n' "$*"; }
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

# Without `tgrep serve`, the index is a snapshot: edits and new files made
# after `tgrep index` are invisible to indexed searches.
check_tgrep() {
  local status mode="snapshot (refresh: tgrep index . | live: tgrep serve .)"
  need tgrep || return 1
  if [[ ! -f "${ROOT}/.tgrep/meta.json" ]]; then
    fail "tgrep: no index; run: tgrep index ${ROOT}"
    return 1
  fi
  if ! tgrep -q -F '<groupId>org.xacml4j</groupId>' "${ROOT}"; then
    fail "tgrep: indexed canary search found no match"
    return 1
  fi
  status="$(cd -- "${ROOT}" && tgrep status 2>&1 || true)"
  if [[ "${status}" =~ Server:[[:space:]]+running ]]; then
    mode="live (tgrep serve)"
  fi
  pass "tgrep: index present, canary matched, mode ${mode}"
}

# Print one compact JSON-RPC message from a streamable-HTTP response body,
# which is either plain JSON or an SSE stream of `data:` lines.
rpc_message() {
  local body="$1"
  if jq -e . "${body}" >/dev/null 2>&1; then
    jq -c . "${body}"
    return 0
  fi
  jq -Rr 'select(startswith("data:")) | sub("^data:[[:space:]]*"; "")' "${body}" \
    | jq -cs 'map(select(type == "object")) | last'
}

serena_canary() {
  local url="$1"
  local timeout="${SERENA_DOCTOR_TIMEOUT:-180}"
  local file="" candidate line session="" proto msg payload verdict
  local -a headers=(-H 'Content-Type: application/json' -H 'Accept: application/json, text/event-stream')

  # package-info.java declares no type, so JDTLS correctly returns no symbols.
  while IFS= read -r candidate; do
    [[ "${candidate##*/}" == package-info.java ]] && continue
    file="${candidate}"
    break
  done < <(git -C "${ROOT}" ls-files -- 'xacml-core/src/main/java/*.java')
  if [[ -z "${file}" ]]; then
    fail "serena: no tracked Java file under xacml-core/src/main/java"
    return 1
  fi

  if ! curl -sS --max-time 20 -D "${WORKDIR}/init.hdr" -o "${WORKDIR}/init.body" \
      "${headers[@]}" \
      --data '{"jsonrpc":"2.0","id":1,"method":"initialize","params":{"protocolVersion":"2025-03-26","capabilities":{},"clientInfo":{"name":"agent-doctor","version":"1.0.0"}}}' \
      "${url}"; then
    fail "serena: initialize request to ${url} failed"
    return 1
  fi
  msg="$(rpc_message "${WORKDIR}/init.body" || true)"
  if ! printf '%s' "${msg}" | jq -e '.result.serverInfo' >/dev/null 2>&1; then
    fail "serena: initialize returned no MCP result"
    return 1
  fi
  proto="$(printf '%s' "${msg}" | jq -r '.result.protocolVersion // "2025-03-26"')"
  while IFS= read -r line; do
    line="${line%$'\r'}"
    if [[ "${line,,}" == mcp-session-id:* ]]; then
      session="${line#*: }"
    fi
  done <"${WORKDIR}/init.hdr"
  headers+=(-H "MCP-Protocol-Version: ${proto}")
  if [[ -n "${session}" ]]; then
    headers+=(-H "Mcp-Session-Id: ${session}")
  fi

  curl -sS --max-time 10 -o /dev/null "${headers[@]}" \
    --data '{"jsonrpc":"2.0","method":"notifications/initialized"}' "${url}" || true

  payload="$(jq -cn --arg path "${file}" \
    '{jsonrpc:"2.0",id:2,method:"tools/call",params:{name:"get_symbols_overview",arguments:{relative_path:$path}}}')"
  if ! curl -sS --max-time "${timeout}" -o "${WORKDIR}/call.body" "${headers[@]}" \
      --data "${payload}" "${url}"; then
    fail "serena: get_symbols_overview did not answer within ${timeout}s"
    return 1
  fi
  msg="$(rpc_message "${WORKDIR}/call.body" || true)"
  verdict="$(printf '%s' "${msg}" | jq -r '
    if .error then "rpc-error"
    elif (.result.isError // false) then "tool-error"
    else ([.result.content[]? | select(.type == "text") | .text] | join("")
      | (try fromjson catch null)
      | if . == null then "unparseable-payload"
        elif ((type == "object" or type == "array") and length > 0) then "ok"
        else "no-symbols" end)
    end' 2>/dev/null || printf 'unparseable-response')"

  if [[ -n "${session}" ]]; then
    curl -sS --max-time 5 -X DELETE -o /dev/null "${headers[@]}" "${url}" 2>/dev/null || true
  fi

  if [[ "${verdict}" != "ok" ]]; then
    fail "serena: JDTLS canary ${verdict} for ${file}"
    return 1
  fi
  pass "serena: JDTLS returned symbols for ${file}"
}

check_serena() {
  local url
  need curl jq git || return 1
  if ! command -v mcp.sh >/dev/null 2>&1; then
    fail "serena: mcp.sh controller is not on PATH"
    return 1
  fi
  url="$(jq -r '.mcpServers.serena.url // empty' "${ROOT}/.mcp.json" 2>/dev/null || true)"
  if [[ -z "${url}" ]]; then
    if ! jq -e '(.mcpServers.serena.args // []) | join(" ") | test("serena attach")' \
        "${ROOT}/.mcp.json" >/dev/null 2>&1; then
      fail "serena: no URL or attach stanza for mcpServers.serena in .mcp.json"
      return 1
    fi
    # Attach stanza: the controller owns the port and Serena starts on first
    # use. Start it through attach, so the idle reaper still stops it.
    mcp.sh -q -m serena attach --project "${ROOT}" </dev/null >/dev/null 2>&1 || true
    url="$(MCP_PROJECT_ROOT="${ROOT}" mcp.sh -q -m serena emit 2>/dev/null \
      | jq -r '.serena.url // empty' 2>/dev/null || true)"
    if [[ -z "${url}" ]]; then
      fail "serena: the mcp.sh controller returned no Serena URL"
      return 1
    fi
  fi
  if ! MCP_PROJECT_ROOT="${ROOT}" mcp.sh -q -m serena doctor; then
    fail "serena: transport doctor failed (start with: mcp.sh -m serena start)"
    return 1
  fi
  serena_canary "${url}"
}

main() {
  local target="${1:-all}" rc=0
  WORKDIR="$(mktemp -d "${TMPDIR:-/tmp}/agent-doctor.XXXXXX")"
  trap 'rm -rf "${WORKDIR}"' EXIT

  case "${target}" in
    all)
      check_ast_grep || rc=1
      check_ripwire || rc=1
      check_tgrep || rc=1
      check_serena || rc=1
      ;;
    ast-grep) check_ast_grep || rc=1 ;;
    ripwire) check_ripwire || rc=1 ;;
    tgrep) check_tgrep || rc=1 ;;
    serena) check_serena || rc=1 ;;
    *)
      printf 'usage: %s [all|ast-grep|ripwire|tgrep|serena]\n' "$0" >&2
      return 2
      ;;
  esac
  return "${rc}"
}

main "$@"
