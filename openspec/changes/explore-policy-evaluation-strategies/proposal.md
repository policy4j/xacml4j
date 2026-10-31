## Status

BRAINSTORM, exploration stage. Not scheduled, not approved for implementation. This proposal records the
maintainer's request and the research; specs, design, and tasks follow after the maintainer's decisions on
the open questions (`research/README.md`).

## Why

The maintainer asked (2026-10-27): "Perhaps I should change how policy tree evaluation is done, right now
its recursive top down approach but its possible to applu different evaluation strategies, I want to brain
storm on this and lookup different evaluation and policy tree optimization strategies".

Today the PDP evaluates the policy tree recursively from the root: each policy set, policy reference,
policy, and rule creates an evaluation context, matches its target, evaluates its condition, and combines
its children with its combining algorithm; references are resolved during the walk; attributes are
resolved on first use. Change `optimize-pdp-evaluation` cuts the cost of this walk (contexts, allocation,
function calls) but keeps its shape.

## What Changes (candidate scope, to be decided)

- Alternative evaluation strategies for the policy tree: target indexing and policy retrieval, compilation
  of the tree (to closures, bytecode, or decision diagrams), flattening and normalization, partial
  evaluation and specialization, bottom-up or attribute-driven evaluation, batch evaluation, decision
  caching, and statistics-driven reordering, each with its conformance constraints.
- A pluggable evaluation strategy in the PDP, so that strategies can be compared with the xacml-jmh harness
  on the same policy stores and corpora.

## Impact

- Affected (candidates): `xacml-core` policy model and evaluation (`org.xacml4j.v30.policy`), `xacml-pdp`
  (`DefaultPolicyDecisionPoint`, policy repositories and reference resolution), `xacml-jmh`.
- Constraint: XACML 3.0 semantics (Section 7, Appendix C) and every conformance result stay unchanged
  unless the maintainer decides otherwise.
- Not BREAKING by itself; candidate BREAKING points are listed in `research/README.md`.
