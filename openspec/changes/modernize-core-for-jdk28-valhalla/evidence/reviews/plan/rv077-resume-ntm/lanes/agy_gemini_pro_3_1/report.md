# Run 077: Specification Review for Pending Decisions (D1, D2, D10, D11, D18)

**Lane:** `agy_gemini_pro_3_1`
**Requested Model:** `gemini-3.1-pro-high`
**Actual Model:** Gemini 3.1 Pro (High)

## Overview
This report adjudicates the maintainer decisions D1, D2, D10, D11, and D18 from run 061 against the written specifications. The objective is to identify which decisions are already settled by the local specs versus which remain open choices for the maintainer. Note that independent normative verification against external official XACML 3.0 standard documents was not performed in this lane, as external source retrieval was not run; conclusions are strictly bounded to the files actually read within the project workspace.

## Numbered Severity Findings

### 1. [HIGH] D2: NaN equality recommendation contradicts the written specification
* **Issue:** Run 061 (memo 032) recommended option (a) `true` to match recorded deviations in conformance cases IIC350/IIC358. However, the written spec strictly forbids this.
* **Evidence:** `openspec/changes/modernize-core-for-jdk28-valhalla/specs/function-semantics/spec.md:30-32` states: "`double-equal` and double comparisons SHALL follow IEEE 754 (XACML 3.0 §A.3.1): NaN SHALL NOT equal any value, including NaN..."
* **Minimal Fix/Test:** Reject option (a) from the prior memo. Implement option (b) `false` to comply with the written spec and IEEE 754. Retag conformance cases IIC350 and IIC358 as deviations in the manifest.

### 2. [MEDIUM] D11: Logical functions with Indeterminate are absent from written specs
* **Issue:** The behavior of `or`/`and`/`n-of` with `Indeterminate` results is not codified in `function-semantics/spec.md`, leaving it ambiguous in the project specs.
* **Evidence:** [UNVERIFIED] The prior run memo claims that official XACML 3.0 §A.3.5 dictates that a later deciding value (e.g., `True` for `or`, `False` for `and`) wins over an earlier `Indeterminate`. (Note: Source retrieval for the official OASIS text was not run in this lane; this claim relies on the prior memo and remains unverified here).
* **Minimal Fix/Test:** If the maintainer confirms the A.3.5 claim, implement option (a) and update `function-semantics/spec.md` to explicitly include this requirement, ensuring test coverage for first-error vs. later-deciding-value.

### 3. [LOW] D1: Integer division and rounding is already settled by the spec
* **Issue:** Listed as a pending decision, but the written spec leaves no room for maintainer choice.
* **Evidence:** `openspec/changes/modernize-core-for-jdk28-valhalla/specs/function-semantics/spec.md:4-5` explicitly requires that `integer-divide` "SHALL return an `xs:integer` equal to the quotient truncated toward zero".
* **Minimal Fix/Test:** Ratify option (a) because it is already a strict requirement in the specification.

### 4. [LOW] D10: `on-permit-apply-second` registration is already settled by the spec
* **Issue:** Listed as a pending decision, but the registration of this CS01 profile algorithm is mandated by the written spec.
* **Evidence:** `openspec/changes/modernize-core-for-jdk28-valhalla/specs/combining-algorithm-registry/spec.md:4-7` explicitly lists `urn:oasis:names:tc:xacml:3.0:policy-combining-algorithm:on-permit-apply-second` as a URN that the default provider SHALL resolve.
* **Minimal Fix/Test:** Ratify option (a) because the spec mandates its resolution.

### 5. [LOW] D18: XPath failure status is already settled by the spec
* **Issue:** Listed as a pending decision, but the handling of content/attribute resolver failures is already defined.
* **Evidence:** `openspec/changes/modernize-core-for-jdk28-valhalla/specs/attribute-resolution/spec.md:44-47` states that a failure of an attribute or content resolver "SHALL make the evaluation Indeterminate with status code urn:oasis:names:tc:xacml:1.0:status:processing-error, regardless of MustBePresent."
* **Minimal Fix/Test:** Ratify option (a) because the written spec dictates `processing-error` over `missing-attribute`.

## Recommendations
* **Adopt Settled Specs:** The maintainer does not need to treat D1, D10, or D18 as open decisions; they are fully codified in the OpenSpec `.md` files.
* **Enforce D2:** Do not ratify the prior swarm memo's recommendation for D2. The written spec overrides the flawed conformance cases. The maintainer must either implement D2(b) or halt to amend `function-semantics/spec.md`.
* **Amend D11:** D11(a) is claimed to be dictated by the official XACML 3.0 specification but is missing from the local OpenSpec. If confirmed, the maintainer should add it to the written spec to codify the choice.

## Uncertainty and Unrun Gates
* **Uncertainty:** It is unclear if modifying the conformance manifest to retag IIC350/IIC358 (for D2) requires a separate specification amendment, given the strictness of the conformance coverage goals. External source retrieval for the official OASIS texts was not run in this lane, meaning claims like A.3.5 (D11) remain unverified against the primary source.
* **Unrun Gates:** As an independent semantic review lane, no code was executed, no builds or tests were run, and no source files were mutated. Verification gates remain unrun.

DONE_AGY_GEMINI_PRO_3_1_20261004
