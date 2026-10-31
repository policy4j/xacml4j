# Conductor-approved identifier repairs

The current task 1.3 decision explicitly approves
`required-duration-bag-alias-fix.patch`. It also permits repairs of missing
or wrong XACML function, type, and combining-algorithm identifiers revealed
by conformance, with identifier/alias annotations only and focused lookup
tests. The conductor requested a scan of the same function family to avoid
successive missing-name stops. Semantic changes remain outside this scope,
apart from the previously approved date/time arithmetic class of defects.

IIC150 was independently reproduced as a missing 3.0 duration bag identifier
in run 37. The family scan found all eight duration bag identifiers and ten
duration set identifiers registered only under their old 1.0 names. Tests
were written and run red before either provider changed (run 39: 36 failures).
The eight bag changes apply the exact approved patch; the ten set changes
use the same canonical 3.0 identifier plus retained 1.0 alias pattern. The
only additional production edit is the import of the alias annotation in
SetFunctions. Method bodies, parameter and return types remain unchanged.

Run 40 exposed the missing annotation import at ten SetFunctions locations;
the import was added and run 41 passed all 42 focused tests. These compiler
diagnostics are recorded, rather than omitted from the execution history.

The earlier exact junit:junit:4.13.2 compile-scope POM decision, XML mixed
content decision, and date/time arithmetic decisions remain in force.
Their files are protected by the new pre-write snapshot. No additional POM
edit, fixture/expected-result change, exclusion, loop-bound change, task tick,
tracking command or commit is authorized or made by this continuation.

References: `39-identifier-sources-before.json`,
`39-protected-before-identifiers.json`, `39-duration-identifier-red.json`,
`39-duration-identifiers-before.txt`, `40-duration-identifiers-after.txt`,
`41-duration-identifiers-after.txt`, and `identifier-audit.md`.

After the duration aliases, run 42 reached IIC164 and stopped at the missing
3.0 any-of name. Direct production import reproduced that failure in run 43.
The same-family scan of HigherOrderFunctions found four missing 3.0 names:
any-of, all-of, any-of-any and map (Appendix A.3.12). Run 44 records the new
lookup tests before production edits: 11 tests, 8 failures, 3 passing checks
of unchanged 1.0 standard names. The four repairs change only identifier and
legacy annotations, within the conductor's expanded identifier-class scope.
Run 45 passes 53 focused tests; run 46 passes the entire JDK 11 reactor.

No function semantic failure was revealed by the final gate. Full variadic
3.0 higher-order semantics were not added or certified by these name repairs.
