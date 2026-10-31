# Conductor approval: bounded core and XML baseline restoration

The conductor explicitly approved both required-core-fix.patch and
required-xml-content-fix.patch as behavior restoration for group 1.
New source ownership is limited to ISO8601DateTime's duration sign handling
and the XML mapper's mixed-content selection, plus focused tests.
Tests must be written and run red first. All four ISO8601DateTime operations
(plus/minus with day-time/year-month durations) must be checked; additional
sign changes are allowed only if a focused failing test demonstrates them.
No other core file may be changed.

The full JDK 11 gate remains required. New conformance failures must be
classified by case ID and cause; out-of-scope fixes remain proposals.
No fixture/expected-result change, commit, br command, task tick, issue
closure or additional POM edit is authorized.

The new pre-write scope snapshot is
27-protected-before-baseline-restoration.json. The newly authorized main
sources are retained verbatim in 27-approved-source-before.json for a
bounded diff and unchanged-header check. Earlier run receipts are preserved.
