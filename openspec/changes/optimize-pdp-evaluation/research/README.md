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
