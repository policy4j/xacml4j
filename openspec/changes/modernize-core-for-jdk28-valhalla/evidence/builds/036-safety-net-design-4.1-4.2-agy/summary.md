# Run 036: test design for tasks 4.1 and 4.2 (Agy audit, conductor adjudication)

Date: 2026-10-02. Lane: `policy4j_code_review_agy` (NTM persona), Antigravity,
Gemini 3.8 Flash (High). Session `policy4j--g1`, pane 3. Read-only audit of
attribute resolution and conditions against XACML 3.0 (5.25, 5.29, 5.30,
5.58, 7.3.5, 7.11, 7.19.3). Raw report: [agy-report.txt](agy-report.txt)
(Table A defects, Table B 43 test cases, Table C ambiguities).

## Conductor adjudication of Table A

| # | Claim | Verdict | Evidence |
| --- | --- | --- | --- |
| 1, 2 | Resolver exceptions are swallowed; the result is an empty bag (MustBePresent=false) or missing-attribute (true) | Code confirmed (`DefaultEvaluationContextHandler.java:147-166`, `AttributeReference.java:122-145`). 7.3.5 defines MustBePresent only for missing attributes and is silent on retrieval errors, so this is a decision, not a spec defect. Security relevant (fails open). | policy4j-the.4.34 (decision) |
| 3 | AttributeSelector precedence | Confirmed, LOW: a null result throws and sets a missing-attribute status even when MustBePresent is false; the catch then returns empty | policy4j-the.4.32 |
| 4 | MissingAttributeDetail is required | Rejected. The spec lets a PDP return StatusDetail with MissingAttributeDetail or omit it (7.3.5: the attribute "MAY be listed"). Missing optional feature, not a defect. | none |
| 5 | attribute-designator function throws processing-error for an absent category before checking mustBePresent | Confirmed in code (`AttributeDesignatorFunctions.java:61-66`); expected behavior to be checked against the Related and Nested Entities profile | policy4j-the.4.33 |
| 6 | Condition must accept a boolean bag | Rejected. 5.25 requires the Condition expression's return data type to be boolean; the constructor check (`Condition.java:59-63`) is correct. Table C item 1 cites "design D7 / task 6.1" for bag unwrapping; D7 says nothing about conditions. | none |

Table C item 2 (MissingAttributeDetail) is an optional feature; item 3
duplicates finding 5.

## Use of Table B

Rows that assume findings 4 or 6 are not test requirements. Rows that fail
today are written as tests and disabled with the bug id (4.32, 4.33) or the
decision id (4.34) until fixed. Everything else is input for the 4.1 and 4.2
test packets.
