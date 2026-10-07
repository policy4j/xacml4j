# rv002: group 1 review, xacml-xml repair and fixtures (Grok, adversarial)

Date: 2026-10-01. Lane: adversarial code reviewer, Grok Build, grok-4.7
(effort xhigh), role prompt `.ntm/roles/policy4j_code_review.md` sent with the
packet (Grok has no NTM persona). Session `policy4j--g1`, pane 0. Read-only.

Scope: same as rv001 (tasks 1.3 xacml-xml only, 1.4, 1.5).

Verdict: APPROVE. No medium or high findings.

Findings:
- LOW: `xacml-xml/.../ExpressionTypeBuilder.java:96` indentation did not
  match the neighboring lines. Resolution: confirmed (base line used
  `" \t \t\t\t"`, the edit used `"\t\t\t\t"`); the conductor restored the
  original leading whitespace, so the line now differs from `a8a90d46` only
  by the `getValueType()` API change.

Reviewer statements (adversarial checks):
- XML adaptations preserve data-type URIs, issuers, IncludeInResult,
  duplicate values, the 2.0 resource XPath category, and MustBePresent.
- The `TypeToXacml30` entity fix (`return b.build()`) is required because
  `Entity.ofAny` drops an Entity argument.
- All 17 fixture blobs match their add commits; policy.json has no source
  reference.
- Evidence counts and the JDK 11.0.32.1 version match the logs.
- Note: the new characterization tests round-trip JAXB lists, not XML text.
- Maven was not re-run by the reviewer; the full reactor gate was still
  blocked by the PDP `PathValue` import.

Grok asked for approval for each command; the conductor approved read-only
`rtk git`, `rtk rg`, `ast-grep scan`, and a git-only blob comparison script.
Raw pane output is not retained (machine paths).
