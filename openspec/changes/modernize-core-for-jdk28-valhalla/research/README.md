# Research

Status: REQUIRED (completed 2026-09-23; raw lane reports not retained)

The change depends on external facts (XACML 2.0/3.0 semantics, JEP 401
value-class rules, JDK 28 early-access toolchain support, JUnit 5 and
Mockito on that JDK), so research was required. It ran as one read-only CAO
review swarm plus local probes. The swarm's conclusions are folded into
`proposal.md` and `design.md` (Context, D1-D11, Risks). The raw lane
reports lived in the planning session's temporary scratch space and were
not copied into this change before it was cleared, so they are not
retained. Re-run a lane (docs/ai/SWARMS.md) if a decision below needs its
original evidence.

## Questions and answers

### Q1. Can the working tree be finished in place, or must it be rebuilt?

- Answer: rebuild from `a8a90d46` and re-apply the refactor in green slices;
  keep the working tree as a donor (design D1-D3).
- Sources: local builds of exported snapshots and an error-tolerant `javac`
  pass (design Context table); Grok lane (adversarial review) and conductor
  fan-in.
- Confidence: high for the compile and test counts (reproducible); medium for
  the effort estimate (32-55 engineer-days).
- Disagreement / drift risk: lanes differed on effort only.

### Q2. Which XACML semantics did the refactor change?

- Answer: designator result shape, the category-URI branch of
  `attribute-designator`, `integer-divide`/`integer-mod`, condition
  cardinality, XPath error handling; plus pre-existing deviations
  (`on-permit-apply-second` registration, `dateTime-subtract-*` sign, NaN
  equality). See design Context and specs.
- Sources: OASIS XACML 3.0 core specification (§7.20 identifier equality,
  Appendix A functions, Appendix C combining algorithms), checked against a
  locally downloaded copy; Grok and Codex lanes.
- Confidence: high where a spec section is cited in the specs; medium for the
  entity-profile identifiers (design open question).
- Disagreement / drift risk: none recorded.

### Q3. Does the JDK 28 early-access build support the target toolchain?

- Answer: `value record` and `value class` compile and run with
  `--release 28 --enable-preview` on the pinned build
  `28-ea-jdk-fd9698357bb0`; plugin, Mockito/ByteBuddy, and JAXB plugin
  support must be verified on that build first (tasks 8.1-8.3, design D10).
- Sources: local probe (`==` compares by value, `isValue()` true); JEP 401;
  JEP 451 (dynamic agent loading); Agy lane.
- Confidence: high for the probe; low until task 8.1 for plugin support.
- Disagreement / drift risk: early-access builds change; pin and re-verify.

### Q4. How should tests move from JUnit 4 + EasyMock to JUnit 5 + Mockito?

- Answer: OpenRewrite recipes for mechanics, manual conversion for strict
  mocks, captures, and ordering; real instances instead of class mocks of
  model and value types (design D4).
- Sources: Kimi and Codex lanes; the EasyMock failure on JDK 21 (96 class-mock
  errors) reproduced locally.
- Confidence: medium until task 2.2 dry-runs the recipes.
- Disagreement / drift risk: OpenRewrite behavior on a non-compiling tree.

## Lanes

| Lane | Profile / model | Report | Status |
| --- | --- | --- | --- |
| A core design | Codex (CAO, read-only) | not retained | completed |
| B tests, downstream modules, conformance | Kimi K3 (CAO, read-only) | not retained | completed |
| C JDK 28 toolchain | Agy (CAO, read-only) | not retained | completed |
| D adversarial semantics review | Grok (CAO, read-only) | not retained | completed |
| Conductor fan-in | Codex coordinator (CAO, plan mode) | not retained | completed |

## Synthesis

The synthesis is `design.md` (Context, Decisions D1-D11, Risks). No
separate synthesis report is retained.
