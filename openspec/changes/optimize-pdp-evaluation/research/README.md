# Research

Status: REQUIRED. The optimization cycles come from recorded profiles and a six-lane swarm
analysis (task 2.1); JVM options and external engines need primary sources.

## Questions and answers

Each optimization question is answered by a profile, a lane report, and the cycle's own JMH
pair; the consensus and the cycle order are in `synthesis.md`.

## Lanes

| Lane | Profile / model | Report | Status |
| --- | --- | --- | --- |
| PF1 | `policy4j_jmh`, Claude Opus 5.5 | `pf1-opus-allocation.md` | done |
| PF2 | `policy4j_jmh_claude_fable`, Claude Fable 5.1 | `pf2-fable-fixed-cost.md` | done |
| PF3 | `policy4j_jmh_codex_sol`, Codex GPT-6.1 Sol | `pf3-sol-cpu.md` | done after one follow-up |
| PF4 | `policy4j_jmh_codex_astra`, Codex GPT-6 Astra | `pf4-astra-collections.md` | done |
| PF5 | Codex GPT-6.1 Terra (`--cod=1:gpt-6.1-terra:max`) | `pf5-terra-algorithms.md` | done |
| PF6 | Agy, Gemini 4 Argon (`--agyx=1:gemini-4-argon-high`) | `pf6-agy-engines.md` | done (two figures corrected in `synthesis.md`) |

## Second profiling swarm (after cycle H, profile run 021)

| Lane | Profile / model | Focus | Report | Status |
| --- | --- | --- | --- | --- |
| PR1 | `policy4j_jmh`, Claude Opus 5.5 | allocation | `pr1-opus-allocation.md` | done |
| PR2 | `policy4j_jmh_claude_fable`, Claude Fable 5.1 | CPU | `pr2-fable-cpu.md` | done |
| PR3 | Codex GPT-6.1 Sol | bags | | not sent; the lane reviewed cycle H and the harness instead |
| PR4 | `policy4j_jmh_codex_astra`, Codex GPT-6 Astra | function call path | `pr4-astra-function-calls.md` | done after one follow-up |
| PR5 | Codex GPT-6.1 Terra | per-policy structure | `pr5-terra-policy-structure.md` | done after one follow-up |
| PR6 | Agy, Gemini 4 Argon | target indexing, literature | `pr6-agy-target-indexing.md` | done; report taken from the Agy conversation database |

Consensus without a BREAKING change (each in two or more reports): direct two-argument invokers with no
argument list and no reflective `Object[]` (PR1 H2, PR2 H2, PR4 H1); index loops instead of iterators on the
policy tree (PR1 H4, PR2 H7, PR4 H3, PR5 H4, H7); context forwarding as one loop and no `Optional` per lookup
(PR1 H6, PR2 H3, PR5 H5); array-backed bags that keep the serialized form (PR1 H1, PR2 H1; settles D7 Open
Question 1 only with the maintainer). Needs the maintainer: exceptions without stack traces (PR1 H3, PR2 H8,
PR4 H2) and one context object per referenced policy (PR1 H10, D10).

## Synthesis

`synthesis.md`; plan review: `evidence/reviews/plan/`.

<!-- Earlier content of this file, kept for history: -->
<!-- # Research -->

<!-- Status: NOT REQUIRED (group 1) -->

<!-- Group 1 replaces random identifiers with sequence numbers; the live source and -->
<!-- tests answer every question. Later groups set this file to REQUIRED and add -->
<!-- their questions before their design is written, for example about target -->
<!-- indexing (XEngine) or decision-diagram evaluation from the XACML engine -->
<!-- literature. -->

<!-- ## Questions and answers -->

<!-- None yet. -->

<!-- ## Lanes -->

<!-- | Lane | Profile / model | Report | Status | -->
<!-- | --- | --- | --- | --- | -->

<!-- ## Synthesis -->

<!-- n/a -->
