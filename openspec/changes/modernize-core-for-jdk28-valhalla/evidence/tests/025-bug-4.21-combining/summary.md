# Extended Indeterminate: bug 4.21

All eight XACML 3.0 override variants pass 3,048 decision-table invocations.
OASIS 3.0 IID300 now executes and passes. The core, conformance and full
clean JDK 21 reactor gates pass, including the conductor's merged lanes.

Runtime: Java 21.0.6, Oracle Corporation / GraalVM 21; Maven 3.9.16;
macOS 26.6.1, aarch64. Every Maven command selected JDK 21 and passed
`-Dlicense.skipUpdateLicense=true`. See [command.txt](command.txt).

## Normative tables and deviations

[XACML 3.0 OASIS Standard, C.2–C.5](https://docs.oasis-open.org/xacml/3.0/xacml-3.0-core-spec-os-en.html)
defines deny-overrides in C.2, ordered-deny-overrides in C.3,
permit-overrides in C.4, and ordered-permit-overrides in C.5. The packet's
C.2/C.3 shorthand does not identify the permit pseudo-code; the final
standard's C.4/C.5 text was used. The tables below are derived from its
priority rules, not from the production implementation.

P = Permit; D = Deny; N = NotApplicable; ID = Indeterminate{D};
IP = Indeterminate{P}; DP = Indeterminate{DP}. Row and column are child
decisions. N is the identity for empty lists and for table folding.

Deny-overrides:

| First / second | P | D | N | ID | IP | DP |
| --- | --- | --- | --- | --- | --- | --- |
| P | P | D | P | DP | P | DP |
| D | D | D | D | D | D | D |
| N | P | D | N | ID | IP | DP |
| ID | DP | D | ID | ID | DP | DP |
| IP | P | D | IP | DP | IP | DP |
| DP | DP | D | DP | DP | DP | DP |

Permit-overrides:

| First / second | P | D | N | ID | IP | DP |
| --- | --- | --- | --- | --- | --- | --- |
| P | P | P | P | P | P | P |
| D | P | D | D | D | DP | DP |
| N | P | D | N | ID | IP | DP |
| ID | P | D | ID | ID | DP | DP |
| IP | P | DP | IP | DP | IP | DP |
| DP | P | DP | DP | DP | DP | DP |

Two deviations were found in the shared PermitOverrides implementation;
both affect rule/policy and ordered/unordered variants:

| Combination without Permit or an explicit DP | Before | Standard / after |
| --- | --- | --- |
| Deny and Indeterminate{P}, in either order | Deny | Indeterminate{DP} |
| Deny and Indeterminate{D}, in either order | Indeterminate{DP} | Deny |

`xacml-core/src/main/java/org/xacml4j/v30/policy/combine/impl/PermitOverrides.java:91`
now tests IP together with ID or Deny before the Deny branch. IP alone
still returns IP; Deny precedes ID alone. No deny-overrides deviation was
found and DenyOverrides was not edited. Legacy XACML 1.0 implementations
were not edited; their existing tests also pass in the full core gate.

## Test coverage and oracles

`xacml-core/src/test/java/org/xacml4j/v30/policy/combine/impl/OverrideDecisionTableTest.java:62`
holds explicit normative matrices. Each algorithm is resolved by its full
3.0 id on the default combining provider, and its returned id is asserted.
Coverage per variant is 36 ordered pairs, 216 ordered triples, all 64
presence subsets in forward and reverse order (128 invocations), and one
legacy generic-Indeterminate control: 381 invocations, 3,048 in total.
Empty lists, singletons, repeated decisions and lists containing all six
extended states are included. The generic control pins the engine's
existing conservative DP result without changing its semantics.

| Registry variant | Invocations | Final failures / errors |
| --- | ---: | --- |
| rule deny-overrides | 381 | 0 / 0 |
| rule permit-overrides | 381 | 0 / 0 |
| rule ordered-deny-overrides | 381 | 0 / 0 |
| rule ordered-permit-overrides | 381 | 0 / 0 |
| policy deny-overrides | 381 | 0 / 0 |
| policy permit-overrides | 381 | 0 / 0 |
| policy ordered-deny-overrides | 381 | 0 / 0 |
| policy ordered-permit-overrides | 381 | 0 / 0 |

Fixed DecisionRule interface fixtures supply the six decision states at
the combining boundary. No policy model or value class is mocked. Each
fixture asserts the context and counts createContext/evaluate calls. With
no dominant result every child must run exactly once. With a dominant
result no child can run twice and a visited child must supply that result.
Ordered variants additionally assert the exact create/evaluate sequence
through the first dominant result. Expected decisions are folded from
the explicit matrices, never computed by another production combiner.
The real-model OASIS suites provide the integration gate.

## Red and green receipts

| Run | Tests | Failures | Errors | Skipped | Result |
| --- | ---: | ---: | ---: | ---: | --- |
| `01-overrides-red.log`, production unchanged | 3048 | 144 | 0 | 0 | Red in the four permit variants; individual cases retained in 01-red-results.json. |
| `../024-bugs-string-functions/02-focused-green.log`, combining class | 3048 | 0 | 0 | 0 | Green after the shared fix. The separate string class in that command had four new-test data errors, documented in run 024. |
| `../024-bugs-string-functions/05-core-gate-retry.log` | 4323 | 0 | 0 | 2 | Clean core gate after the conductor merge. |

All 144 original failures come from the two deviations and their effects
on larger lists. New tests compiled and ran red before production changed.
The other four algorithms passed before and after. IID300 is re-enabled,
with its bug field cleared, and passes in the OASIS 3.0 probe, conformance
gate and final full reactor gate. Its final receipt is in
[iid300-result.json](iid300-result.json).

The first core gate overlapped the conductor's selector/duration merge
and failed outside this packet; run 024 preserves its 9 failures and
2 errors. No merged file was edited by this lane. Both subsequent module
and full gates are green.

## Scope and residual risk

This bug's production edit is limited to the PermitOverrides signaled
priority branches. Its new test covers all eight variants; no unrelated
combining behavior was edited. All 16 cases targeted by this packet pass;
the other 28 manifest-disabled cases retain their reasons and bug ids and
were not re-probed. All POMs, vendored fixture/schema files and existing
license blocks are unchanged by this lane. See
[run 024's source audit](../024-bugs-string-functions/source-scope-audit.json)
for the packet-start and stable final-gate hashes, including the
conductor's explicitly reported concurrent merge. No task or issue was
closed, and no commit or install was made. JMH was not run; no performance
claim is made.

## Final gates

The following counts come from the shared final clean reactor run and its
fresh Surefire XML, rather than historical baselines.

<!-- derived-counts -->

| Module | Tests run | Failures | Errors | Skipped |
| --- | ---: | ---: | ---: | ---: |
| xacml-core | 4323 | 0 | 0 | 2 |
| xacml-xml | 36 | 0 | 0 | 0 |
| xacml-json | 7 | 0 | 0 | 1 |
| xacml-pdp | 76 | 0 | 0 | 3 |
| xacml-test | 30 | 0 | 0 | 0 |
| xacml-conformance-tests | 901 | 0 | 0 | 28 |
| Total | 5373 | 0 | 0 | 34 |

These are Surefire invocation counts, including parameterized invocations and skips. The two new classes contribute 505 and 3,048 invocations. xacml-xml-jaxb has no tests.

| Suite | Manifest rows | Executed disposition | Special harness (passed / skipped) | Skipped disposition | Cases passed | Cases skipped |
| --- | ---: | ---: | --- | ---: | ---: | ---: |
| oasis20 | 374 | 360 | 2 / 0 | 12 | 362 | 12 |
| oasis30 | 487 | 470 | 1 / 1 | 15 | 471 | 16 |
| rsa2008 | 17 | 17 | 0 / 0 | 0 | 17 | 0 |
| example | 2 | 2 | 0 / 0 | 0 | 2 | 0 |
| Total | 880 | 849 | 3 / 1 | 27 | 852 | 28 |

The conformance module reports 901 invocations: 880 manifest cases, the two preserved testName tests, and 19 guard/import/resolver/integrity tests. Case totals exclude those 21 non-case checks. 852 cases passed, 0 failed, 28 were skipped. Skipped rows are retained, not evidence of a fresh failed evaluation.

Raw gate output: `../024-bugs-string-functions/07-conformance-gate.log` and `../024-bugs-string-functions/08-full-reactor-clean-verify.log`. Fresh module/class receipts: `../024-bugs-string-functions/08-full-reactor-results.json`; per-case receipts: `../024-bugs-string-functions/08-final-case-results.csv`. See surefire-summary.txt for exact Maven lines.
