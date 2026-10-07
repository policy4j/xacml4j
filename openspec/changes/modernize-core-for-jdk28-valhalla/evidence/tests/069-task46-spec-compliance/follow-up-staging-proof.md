# Nested response staging proof follow-up

OpenSpec: modernize-core-for-jdk28-valhalla, group 4 gate 4.7.
Sources: rv078/opus/final-report.md finding 2 and rv078/astra/final-report.md
proof limits. These are unrun scenarios, not demonstrated product defects.

Add tests through real production policies/contexts and, where appropriate,
the PDP or policy repository. Avoid implementation-only mocks and fake skips.

1. Enclosing permit-overrides, child with an Indeterminate target and Permit
   rule carrying an obligation, plus a Permit sibling: sibling responses only.
2. Enclosed policy whose own obligation expression fails: Indeterminate,
   complete error status preserved, no buffered responses published.
3. PolicyIDReference and PolicySetIDReference children with responses beneath
   staged nested sets: successful resolution and correct final response flow.
4. Rule responses with an effect that differs from the enclosing policy
   decision: no nonmatching responses reach a later sibling result.
5. Four-level policy-set nesting: no duplicate or discarded responses; compare
   response cardinality, assignment values and multiplicity, not only ID sets.

Run focused behavioral tests, JDK 21 full reactor and conformance identities;
obtain independent cross-provider/adversarial review. A source trace and
existing general reference-suite success do not replace these scenarios.
