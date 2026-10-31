# Resume and standards adjudication
Date: 2026-10-04. Scope: task 4.6 and preparation for gate 4.7. Maintainer selected objective specification compliance. No commits authorized.

## Research and model coverage
All eight requested NTM review lanes produced retained reports under lanes/. Native session metadata or model footers recorded the requested configurations in model-receipts.json; these are not independent backend attestation. The Astra lane self-reported Sol despite an Astra turn_context pin; preserve this conflict and do not represent it as an attested Astra backend result. Wave 1 and wave 2 terminal receipts are retained, and those sessions were shut down after completion.

Tavily first retrieved primary OASIS text, then returned HTTP 429. Native DuckDuckGo fetched the official core and Additional Combining Algorithms Profile CS01 in full. Deep Research hit a provider token rate limit and supplied no accepted independent report. A focused DuckDuckGo rounding search returned no useful result. No absence claim follows from that search.

## Dispositions
1. Adopt primitive IEEE double-equal: NaN is unequal even to itself; signed zeroes compare equal. Keep Java Value.equals/hashCode and multiset equality reflexive. Apply policy equality consistently to double bag membership and set functions. Sol/Fable findings are supported by source and fresh numeric RED in tests/069-task46-spec-compliance.
2. integer-divide must return xs:integer. Keep the legacy IntegerVal.divide helper; implement exact integer arithmetic at the registered function boundary. Project truncation remains the selected interpretation; §A.3.2 specifies the return type but not an explicit truncation rule, while §7.5 creates rounding ambiguity. Do not claim the convention is an unambiguous standards mandate.
3. Adopt CS01 §2.1 decision table and policy-only registration for on-permit-apply-second. Preserve all 23 baseline URNs and add only its policy URN. Invalid arity evaluates no children and returns Indeterminate{DP}/processing-error. Child0 Deny is NotApplicable with two children, or child2 with three; it does not return Deny automatically. Grok 4.7 corrected its initial wrong proposed assertion. Nested obligations/advice require a real reproducer before any broader context changes.
4. Preserve every prior enabled conformance identity. Restore IIC020 in both suites. Keep IIC350/IIC358 enabled with narrowly adjudicated NotApplicable expectations in the harness; preserve vendored Permit XML and full-response comparison. Gemini Flash corrected its erroneous 901/882 counts: baseline is 880 rows, 864 enabled; target is 866 enabled.
5. Reject the proposed treatment of missing XPath Content as an error. Core §A.3.15 explicitly requires node-count=0 and node-equal/node-match=false. Corrected the local delta spec. Invalid XPath/evaluation failures remain Indeterminate; gate 4.7 must test the distinction.
6. PIP, eager logical evaluation, resource limits, and other disabled cases remain separate gate work. Report options by observed behavior, not historical option labels. The current reactor pass does not establish complete XACML compliance.

## Sources
- [XACML 3.0 OASIS Standard, 22 January 2013](https://docs.oasis-open.org/xacml/3.0/xacml-3.0-core-spec-os-en.html): §§7.5, 7.18, A.3.1, A.3.2, A.3.10, A.3.11, A.3.15, Appendix E.
- [Additional Combining Algorithms Profile 1.0 CS01](https://docs.oasis-open.org/xacml/xacml-3.0-combalgs/v1.0/cs01/xacml-3.0-combalgs-v1.0-cs01.html): §2.1.
Source retrieval is supported; inferred runtime behavior is separately tested. This planning/review run does not replace the task's implementation and green gates.

## Guidance
AGENTS.md and docs/ai/RESEARCH.md updated in donor and integration; symlinked CLAUDE.md preserved. guidance-review.md and guidance-summary.md record the adversarial review. Focused checks pass. Repository-wide guidance path gate failed on existing generated/source/evidence/vendored absolute references, not the new research guidance; retained guidance-path-gate.log. No MCP configuration changes.

