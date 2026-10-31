# Conductor approval: task 1.3 date/time arithmetic

The conductor approved required-date-period-fix.patch and expanded the lane
to pre-existing date/time/duration arithmetic defects revealed by conformance.
The six ISO8601 date/time/duration classes, DateTimeArithmeticFunctions and
their focused core tests may be changed for that purpose. Tests must be
written and recorded red before each smallest production fix, then green.

The six existing function operations will be checked at signed/zero and
month-end boundaries. No new function is authorized. A failure outside
date/time arithmetic or XML content mapping must be reported as a proposal.
Fixtures, expected results, other POMs, task state and tracking stay untouched.

The earlier compile-scope JUnit POM and core/XML restoration approvals persist.
This records the user's decision; it is not an independently issued approval.
