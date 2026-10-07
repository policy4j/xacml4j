# Task 4.6 numeric lane, 2026-10-04

Implementation is ready for the conductor's GREEN gate. Compilation and
behavior after the changes have not been run by this lane.

Role: numeric compliance writer. Requested model: `gpt-6.1-sol`.
Available identity: OpenAI Codex, GPT-6 family; exact serving variant
is not exposed and remains UNVERIFIED. Beads ownership remains with
conductor `codex-modernization-resume`, issue `policy4j-the.4.6`.

## Changes and boundaries

1. `xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/ArithmeticFunctions.java`:
   `integer-divide` now declares xs:integer, returns `IntegerVal`, and
   divides unboxed longs directly, retaining the existing zero guard.
   A.3.2 requires the integer type. Quotient toward zero is the approved
   PROJECT interpretation in `../decision-record.md`, not an express
   normative rounding requirement. The legacy `IntegerVal.divide` helper
   is unchanged. The registered method's Java return descriptor changes
   intentionally; consumers must recompile.
2. `EqualityPredicates.java`, in the same directory: scalar double-equal
   uses one package-private primitive predicate. NaN never matches,
   including the same NaN object; signed zeros match. Other equality
   overloads retain their existing behavior.
3. `BagFunctions.java`: double-is-in uses that predicate and checks the
   bag datatype. A zero-argument double-bag produces a typed empty bag
   when the current registry invocation supplies null varargs; ordinary
   bags preserve all occurrences and stored zero signs.
4. `SetFunctions.java`: double intersection, union, subset, set-equals
   and at-least-one-member-of share the same predicate via membership
   scans. Union/intersection deduplicate policy-equal numbers. Every NaN
   occurrence survives union and no NaN matches in intersection. Empty
   bags retain their double datatype. Both bag types are checked before
   scanning, including empty bags. No object-identity shortcuts or Java
   equality-based deduplication are used in these double functions.

Production edits are confined to those four files. SHA-256 checks before
and after implementation confirm `IntegerVal.java`, `Value.java`,
`DoubleVal.java`, and `BagOfValues.java` are unchanged; Java equality,
hashing and multiset storage are preserved. Other writers' work,
vendored fixtures, the harness, manifest, Beads and Git state were not
modified by this lane. No staging, commits, branch switch or publication.

## Tests and RED evidence

The tests-first handoff wrote only
`xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/ArtimeticFunctionsTest.java`,
`FunctionSafetyNet43Test.java`, and `DoubleBagSetEqualityTest.java`.
They exercise actual registry functions, composed production Apply with
runtime validation, exact integers above 2^53, project rounding ties,
zero-divisor processing-error mapping, signed zeros, distinct wrappers,
same-object NaN, empty bags, duplicates and Java hash collection contracts.
Double set results use independent size, NaN count, zero count and finite
value assertions; no policy function or bag equality supplies the oracle.

Conductor-owned `../numeric-red-receipt.json` and the three retained
`../numeric-red-TEST-*.xml` files record JDK21 RED: **252 tests, 63
failures, 2 errors, 7 skips**. These are inspected supplied receipts, not
execution by this lane. They reproduce the wrong integer result/type,
composed validation failure, IEEE policy equality defects and empty
double-bag null-varargs error. The saved RED files remain unchanged.

RED also observed missing `integer-divide` short-ID lookup. The conductor
adjudicated short names as diagnostic metadata, with no task requirement
for lookup. On explicit instruction, arithmetic tests were narrowed to
the standard URN: remove the alias declaration/error rows and alias calls
inside quotient rows. Two parameterized invocations were removed; no
numeric behavior assertion was removed. The converter and aliases remain
unchanged. The two other test files retain their pre-GO SHA-256 hashes.

## Verification and remaining coverage

Run by this lane: scoped `rtk git status --short`; focused `rtk rg` and
`rtk proxy sed` source/receipt reads; `rtk proxy shasum -a 256` preservation
checks; scoped `rtk git diff --check` and final diff inspection. These are
SUPPORTED for literal source and file preservation; runtime conclusions
remain pending the conductor's gate.

Unrun by this lane: Maven compile, numeric GREEN, core/PDP tests, full
verify, conformance, vendored-integrity tests, independent review,
`openspec validate`, and JMH/JDK28 checks. Suggested numeric gate, for the
conductor only:

```sh
JAVA_HOME="$(jenv prefix 21)" rtk mvn -q -Dlicense.skipUpdateLicense=true -pl xacml-core -am -Dtest='ArtimeticFunctionsTest,FunctionSafetyNet43Test,DoubleBagSetEqualityTest' -Dsurefire.failIfNoSpecifiedTests=false test
```

Omitted coverage: union with more than two bags, arbitrary-precision
integers and `Long.MIN_VALUE / -1` overflow (task 5.4), and performance.
The existing two-bag union signature is retained. Double policy scans
can be quadratic for large bags; no performance result is claimed.
Seven unrelated safety-net skips remain. Task closure, evidence indexing
and whole-engine acceptance belong to the conductor; gate 4.7 is pending.

DONE_NUMERIC_20261004
