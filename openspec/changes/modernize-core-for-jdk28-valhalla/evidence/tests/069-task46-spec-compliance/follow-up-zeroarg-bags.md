# Follow-up outside task 4.6

OpenSpec: modernize-core-for-jdk28-valhalla, group 4 gate 4.7.

Opus rv078 finding 1 identifies a pre-existing registry boundary defect:
DefaultFunctionInvocation passes null for a zero-argument varargs call;
sixteen type-bag functions do not handle null. Only the double-bag boundary
was corrected in task 4.6. This is a source-review observation; runtime
reproduction of the other types has not been run in this session.

Scope: DefaultFunctionInvocation and registry-entrypoint tests covering
zero-argument calls of every standard type-bag function. Keep typed empty
bags, result type validation, and Java equality contracts intact.

Acceptance: first reproduce the failure through the production registry,
then return a typed empty bag for each standard type-bag URN and confirm
type-bag-size(type-bag()) yields integer zero. Run focused registry tests,
full JDK 21 reactor, unchanged/improved conformance identities, and
independent review. Do not mask failures with skips or rely on direct
helper invocation as proof. Evidence source: rv078/opus/report.md.
