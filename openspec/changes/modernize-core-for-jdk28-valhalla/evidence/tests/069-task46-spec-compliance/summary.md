# Task 4.6 spec compliance checkpoint

Result: task 4.6 implementation and full JDK 21 reactor PASS, independently
reviewed in rv078. Task 4.6 is checked and Beads policy4j-the.4.6 is closed with these evidence paths.
Gate 4.7 and whole-engine compliance remain pending.

The maintainer resumed modernization with objective spec compliance and
the requested eight-model NTM matrix. rv077 records eight decision reports,
their model pins, terminal receipts, source reconciliation and model
attestation limits. Sol authored numeric functions/tests; Fable authored
combining and registry tests; Gemini Flash authored conformance adjudication.
The conductor applied the handed-off composite buffer and review follow-ups.
rv078 retains independent numeric/harness and final combining reviews.

## Implemented behavior

- double-equal and double bag/set policy comparisons use primitive IEEE
  equality: NaN never compares equal, signed zeros do. Java Value/DoubleVal
  equality and BagOfValues multiplicity/hash contracts are unchanged.
- The registered integer-divide returns xs:integer using exact long
  division. Legacy IntegerVal.divide remains unchanged. Toward-zero
  truncation is the recorded project convention; the Core 7.5/A.3.2
  wording ambiguity is explicitly retained, not presented as an express
  normative truncation mandate. Non-wrapping overflow remains task 5.4.
- on-permit-apply-second is registered as a policy algorithm after CS01
  table correction, with invalid-arity rejection, short-circuit call order,
  Indeterminate subtypes and status preservation.
- Nested composite responses are staged privately and published only after
  the final matching Permit/Deny and successful own response expressions.
  The RED discarded-child obligation leak is fixed. Permit and Deny sibling
  isolation plus nested-parent responses are tested through real policies.
- Both IIC020 cases are restored. IIC350/IIC358 remain enabled through an
  explicit, guarded in-memory expected-decision adjudication to NotApplicable;
  the full response comparator remains active and vendored XML stays intact.
- The XPath delta distinguishes missing Content (0/false, including
  precedence) from failed evaluation with Content present (Indeterminate).
  No XPath production file changed in this task.

## Evidence

| Gate | Result |
| --- | --- |
| Numeric initial RED | 252 tests, 63 failures, 2 errors, 7 skips; two nonstandard short-ID lookups subsequently removed with retained explanation |
| Harness initial RED | 5 tests, 3 failures, 0 errors, 0 skips |
| Numeric GREEN / combining RED | 674 total, 264 combining failures; numeric slice 250/0/0/7 |
| Focused GREEN r1 | 721/0/0/7, including policy and reference compatibility tests |
| Reactor r1 | Test compilation failed in the conductor's new full-response assertion; retained log, then corrected via production marshalling |
| Final reactor clean verify | 7,589 tests, 0 failures, 0 errors, 30 remaining skips; 178 Surefire suites |
| Strict OpenSpec validation | PASS |
| Source preservation against 068 | Before closure: 3,736/3,753 unchanged; final closure: 3,735/3,753 unchanged, with the task checkbox as the eighteenth expected changed path; all 2,911 vendored files unchanged |
| Final source freeze | All 18 reviewed v2 hashes match the reactor receipt; closure differs only by separately reviewed combining-spec traceability/wording additions; production/test bytes remain unchanged |

Per-module counts: core 6,197 (10 skips), XML 244, JSON 24 (1 skip),
PDP 98 (5 skips), test support 99, conformance 927 (14 skips).
New targeted tests include 289 decision-table invocations, 12 combining
policy tests, 108 double bag/set rows, 33 arithmetic tests and 10 NaN harness
tests. FunctionSafetyNet43 has 109 invocations with six remaining skips.

Manifest identity proof: 880 rows; enabled coverage 864 -> 866, with every
previously enabled identity retained. The reactor XML records all 866 distinct
enabled PASS markers with no missing or extra ID. Four are explicit allowed
static-rejection cases, not successful PDP decision evaluations:
oasis20/IIA004, oasis20/IIC012, oasis30/IIA004, oasis30/IIC012.

Logs, original RED XML, final reactor XML, identity comparison and
source-preservation JSON are retained in this run. Every review remains
bounded; a source review or a passing reactor does not prove complete XACML
compliance. No staging, commits, branch switching, push or merge occurred.

## Remaining work

Gate 4.7 stays open for PIP/logical/XPath/resource deviations and disabled
safety-net rows. Dedicated nested reference/failure/multiplicity proof is
tracked as policy4j-the.4.54 and blocks4.7. Other zero-argument type-bag functions are tracked as
policy4j-the.4.53 (source-observed, runtime reproduction not yet run), which
blocks 4.7. Integer overflow remains task 5.4. N-ary union, real malformed
XPath and dedicated nested-reference/failed-response-expression scenarios
remain unverified. JMH is APPLICABLE and unrun; hot-path scan/staging costs
have no performance acceptance. JDK 28/Valhalla work has not begun here.

Research tools are documented in AGENTS.md and docs/ai/RESEARCH.md in both
donor and integration. Gemini Pro reviewed that guidance; the focused
catalog/path/symlink checks pass. The full guidance path gate still reports
pre-existing absolute paths elsewhere (rv077/guidance-summary.md). Native
Tavily and DuckDuckGo retrieved official OASIS text; Tavily later returned
429, and Deep Research failed with provider token limiting. No completed
Deep Research conclusion is claimed. No MCP configuration was changed.
