## Status

FUTURE ENHANCEMENT, exploration stage. Not scheduled, not approved for implementation. This proposal
records the maintainer's request and the brainstorm research; specs, design, and tasks follow after the
maintainer's decisions on the open questions (`research/README.md`).

## Why

The maintainer asked (2026-10-27): "add future enhancement for ALFA support for policy language and
authorization domain model metadata definition like it's done in axiomatic: with default XACML 2.0 model,
3.0 model and 4.0 model with all required and optional attributes and all profiles ... also look up
Artagon-uri project it was ValueBox design for XACML expressions and type system with much more
expressive type systems. Authorization model: entities, attributes and their types, type system and
functions on these types", and "Also add support for AWS Cedar like policies and types as and
authorization model definitions in cedar syntax". Today xacml4j has no attribute schema: policies are
checked only for function signatures and condition types, and policies can only be written in XACML XML
or JSON.

## What Changes (candidate scope, to be decided)

- An authorization model: versioned standards catalogs (XACML 2.0, XACML 3.0, each OASIS profile, and an
  experimental catalog pinned to XACML 4.0 / ACAL CSD01), kept separate from application models
  (entities, attributes and their types, actions, hierarchies, constraints).
- A richer type system and typed functions: entity, record, set, and map types, refinements, typed and
  overloaded function signatures, and a static policy type check against a model (opt-in).
- Policy-language front ends in new modules: ALFA (a clean-room grammar; no open, permissive grammar
  exists) and Cedar (policies compiled to the xacml4j policy model with a Cedar combining algorithm;
  Cedar schema syntax as a model language).
- The relation to the ValueBox module of artagon-uri (`artagon-valuebox`): a port of its type descriptors,
  a shared library, or design input only.

Every part is opt-in and leaves today's PDP decisions unchanged by default. Items that would be
**BREAKING** are listed in `research/README.md`.

## Capabilities

### New Capabilities

To be named after the decisions; candidates: `authorization-model`, `policy-type-checking`,
`alfa-policy-language`, `cedar-policy-language`.

### Modified Capabilities

None yet.

## Research Questions

Answered by the four-lane swarm (`research/`): the status of ALFA, Cedar, and XACML 4.0; grammars and
their licenses; the Axiomatics domain model; the standard attributes per version and profile; the ValueBox
module and type systems of Cedar and CEL. Open: the decisions listed in `research/README.md`.

## Verification

To be defined with the design. Constraint from the research: with every new feature disabled, decisions,
conformance results, and JMH numbers stay as they are.

## Impact

Candidate new modules (`xacml-model`, `xacml-alfa`, `xacml-cedar`), additions in `xacml-core` types and
function SPI, possibly an ANTLR 4 runtime dependency (BSD-3) in the language modules. Nothing changes
until a design is approved.

## Tracking

Beads epic: after the maintainer schedules the work.
