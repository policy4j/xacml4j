# rv079: adversarial review of the ruled semantics, runs 071 to 073 (Codex)

Date: 2026-10-07. Lane: adversarial code reviewer, Codex CLI, one shot per
round (`codex exec -s read-only --skip-git-repo-check`), no tools, no
repository access. Input per round: `roundN-prompt.md` followed by the
complete `git diff` of the change against the last commit, with the home
path removed. Scope: runs 071 (beads the.4.34, the.11), 072 (bead the.4.44
and decisions C4, C5, C7), 073 (manifest reasons).

Final verdict: APPROVE in round 6.

| Round | Verdict | Findings | Result |
| --- | --- | --- | --- |
| 1 | REQUEST_CHANGES | HIGH: the resolver's exception text became the status message of the response. MEDIUM: a resolver exception with another status kept that status. LOW: the test for "a later resolver has the value" did not prove the order. | Fixed: fixed status message, every resolver failure is `processing-error`, the test records the call order. |
| 2 | REQUEST_CHANGES | HIGH: a content resolver could throw a `PathEvaluationException` with its own status and text. HIGH: the "already mapped" shortcut trusted class and status code. MEDIUM: `x500Name-match` rebuilt RDNs from RFC 2253 text. | Fixed: resolver boundary in the PIP, origin mark set only by the factory, RDNs taken from the canonical name. |
| 3 | REQUEST_CHANGES | HIGH: `isResolverFailure()` was overridable. MEDIUM: a `PolicyInformationPoint` could report `syntax-error` with its own text. | Fixed: private `origin` field, `final` accessor, `resolveViaPip` maps every exception, the handler maps path errors. |
| 4 | REQUEST_CHANGES | HIGH: the boundary called `getMessage()` of the failure before it mapped it; a `getMessage()` that throws escaped the mapping. | Fixed: the failure is mapped before anything is logged; red run `tests/071-resolver-failure-fail-closed/round4-red-surefire-summary.txt`. |
| 5 | REQUEST_CHANGES | MEDIUM: a checked exception thrown from `getMessage()` still escaped. | Fixed: `getMessage()` of the cause is never called; red run `round5-red-surefire-summary.txt`. |
| 6 | APPROVE | none | - |

No round reported a finding on `round`, `double-to-integer`, the
`ipAddress-*` functions, or the manifest reasons. `canonicalRdns` was
checked in round 3 with no finding.

The diff that round 6 approved also showed the `function-semantics`
requirement of bead the.4.12; its code was not in the diff and is reviewed
in rv081 and rv082.

Limits: the reviewer saw the diff and the facts in the prompt only. It did
not run the build; the test totals in the prompts were supplied.
