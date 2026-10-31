# String and URI function fixes: bugs 4.14 and 4.15

The JDK 21 core, conformance, and full clean reactor gates pass. All 15
string/URI cases from this packet now execute and pass. IID300 also passes;
its combining fix and decision tables are recorded in [run 025](../025-bug-4.21-combining/summary.md).

Runtime: Java 21.0.6, Oracle Corporation / GraalVM 21; Maven 3.9.16;
macOS 26.6.1, aarch64. Every Maven command used
`JAVA_HOME="$(jenv prefix 21)"` and `-Dlicense.skipUpdateLicense=true`.
Exact commands are in [command.txt](command.txt).

## Changes and source of the expectations

[XACML 3.0 OASIS Standard, A.3.9](https://docs.oasis-open.org/xacml/3.0/xacml-3.0-core-spec-os-en.html)
defines the first search argument as the searched-for string and the second
as the candidate. It defines zero-based substring indexes, an exclusive
end, `-1` for the remainder, and Indeterminate for invalid indexes.
Both substring functions return `string`; the anyURI version additionally
requires the resulting substring to be a syntactically valid URI. These
are paraphrases of the standard, checked in its complete section text.
The Jul/Juliet and URI controls use the vendored OASIS IIC300/IIC302 and
IIC330/IIC333 examples. The A.3.9 definition paragraphs do not contain a
separate worked truth-table example.

| File and line, relative to the repository | Fix or proof |
| --- | --- |
| `xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/StringFunctions.java:57` | Reverse the candidate/needle use in string-starts-with, string-ends-with and string-contains. |
| `StringFunctions.java:66` | Add the standard anyURI-starts-with, anyURI-ends-with and anyURI-contains identifiers with `(string, anyURI)` parameters. |
| `StringFunctions.java:75` | Retain the existing nonstandard uri-* identifiers and `(anyURI, string)` signatures; delegate to the standard methods while preserving their candidate/needle behavior. |
| `StringFunctions.java:138` | Add string-substring and anyURI-substring. Validate indexes as long values before conversion to int; count Unicode code points; validate the URI result with `URI.create`. |
| `xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/StringFunctionSemanticsTest.java:67` | Truth tables through Apply on the default function provider, with runtime validation off and on. |
| `StringFunctionSemanticsTest.java:249` | Exact syntax-error for signature rejection and processing-error for invocation failure; a real Condition produces Indeterminate and retains processing-error. |
| `xacml-conformance-tests/src/test/manifest/conformance-manifest.csv` | Enable exactly the 15 string/URI cases plus IID300; clear their bug ids. Preserve all other 864 rows. |
| `evidence/tests/023-conformance-manifest/inherited-skip-dispositions.csv` | Replace the 14 remaining BUG-TBD ids with the conductor's ids. Preserve other fields and historical receipts. |

The registration inventory finds all 11 relevant identifiers in
StringFunctions. Tests resolve and evaluate all nine search identifiers and
both substring identifiers. The three legacy uri-* entries keep their
existing public signatures; they are not presented as spec aliases with
the standard parameter order. No other function semantics were edited.

## Search truth table

Each row is evaluated for starts-with, ends-with and contains through
Apply. Each runs with runtime validation off and on. String rows also run
for anyURI and the legacy uri-* paths when the candidate is a valid URI;
the literal-space candidate `a b` runs only as a string. Search is literal
and case-sensitive; it does not decode `%20`.

| Searched-for first argument | Candidate second argument | Starts | Ends | Contains |
| --- | --- | --- | --- | --- |
| `""` | `""` | true | true | true |
| `""` | `"Juliet"` | true | true | true |
| `"Jul"` | `""` | false | false | false |
| `"Jul"` | `"Juliet"` | true | false | true |
| `"iet"` | `"Juliet"` | false | true | true |
| `"lie"` | `"Juliet"` | false | false | true |
| `"Juliet"` | `"Jul"` | false | false | false |
| `"Juliet"` | `"Juliet"` | true | true | true |
| `"jul"` | `"Juliet"` | false | false | false |
| `"Jul"` | `"juliet"` | false | false | false |
| `"IET"` | `"Juliet"` | false | false | false |
| `"x"` | `"Juliet"` | false | false | false |
| `"abc"` | `"abcabc"` | true | true | true |
| `"bc"` | `"abcabc"` | false | true | true |
| `"^.*"` | `"abc"` | false | false | false |
| `" "` | `"a b"` | false | false | true |
| `"http://medico.com/"` | `"http://medico.com/record"` | true | false | true |
| `"/record"` | `"http://medico.com/record"` | false | true | true |
| `".com/"` | `"http://medico.com/record"` | false | false | true |
| `"HTTP"` | `"http://medico.com/record"` | false | false | false |
| `"%20"` | `"foo%20bar"` | false | false | true |
| `" "` | `"foo%20bar"` | false | false | false |

## Substring truth table

Both functions and both validation modes run every row except the two
literal-space inputs, which are string-only. `A😀B` tests Unicode character
indexes across a supplementary code point. Assertions include return-type
metadata as well as the value.

| Input | Start | End | Expected string |
| --- | --- | --- | --- |
| `abcdef` | 0 | 0 | `""` |
| `abcdef` | 0 | -1 | `abcdef` |
| `abcdef` | 1 | 4 | `bcd` |
| `abcdef` | 2 | -1 | `cdef` |
| `abcdef` | 6 | -1 | `""` |
| `abcdef` | 6 | 6 | `""` |
| `abcdef` | 0 | 6 | `abcdef` |
| `abcdef` | 3 | 3 | `""` |
| `""` | 0 | -1 | `""` |
| `""` | 0 | 0 | `""` |
| `A😀B` | 1 | 2 | `😀` |
| `A😀B` | 2 | -1 | `B` |
| `This is the initial test string.` | 8 | 15 | `the ini` |
| `This is the initial test string.` | 8 | -1 | `the initial test string.` |
| `http://this/is/the/initial/uri` | 14 | 24 | `/the/initi` |
| `http://example.test/path` | 7 | -1 | `example.test/path` |

For `abcdef`, each of these index pairs must produce Indeterminate with
processing-error: `(-1,2)`, `(0,-2)`, `(7,-1)`, `(0,7)`, `(4,3)`,
`(Long.MAX_VALUE,-1)`, `(0,Long.MAX_VALUE)`, `(4294967297,-1)`,
`(0,4294967297)`, `(Long.MIN_VALUE,-1)`. Empty input with `(1,-1)` also
fails. Every pair runs for both types and both runtime validation modes.
Slices producing `http:`, `[` and `%` are legal string results but fail
URI syntax validation. Wrong arity and parameter types reject with
syntax-error for each of the 11 identifiers.

## Red and green receipts

| Run | Tests | Failures | Errors | Skipped | Result |
| --- | ---: | ---: | ---: | ---: | --- |
| `01-string-functions-red.log`, production unchanged | 505 | 50 | 241 | 0 | Red: operand direction and missing functions. |
| `02-focused-green.log`, both new classes | 3553 | 4 | 0 | 0 | Combining class green; four failures exposed a typo in the new IIC333 unit expectation. |
| `03-string-functions-green.log` | 505 | 0 | 0 | 0 | Green after correcting that new-test typo. |
| `04-core-gate.log`, conductor merge window | 4323 | 9 | 2 | 2 | Red in unowned selector/duration tests; both new classes green. |
| `05-core-gate-retry.log`, clean core after merge | 4323 | 0 | 0 | 2 | Green without editing the merged files. |
| `06-oasis30-case-probe.log` | 488 | 0 | 0 | 16 | All 16 enabled cases passed; 471 fixture cases passed plus testName. |

The new IIC333 expected slice was initially hand-counted as `the/initia`.
Independent character enumeration shows that index 14 is `/` and the
exclusive end is index 24, giving `/the/initi`. The new unit assertion was
corrected to that value; no assertion was removed and no vendored expected
result changed. [02-uri-index-oracle-correction.json](02-uri-index-oracle-correction.json)
records the independent check. The original red tests compiled and ran
before either production fix; [01-red-results.json](01-red-results.json)
preserves the individual failures.

The conductor reported a merge at 23:00–23:05 local time. Run 04 finished
within it. Its 11 failures are retained in
[04-core-merge-window-failures.json](04-core-merge-window-failures.json).
The clean rerun and final reactor run verify the completed merge. This
lane did not edit those merged selector, duration, PDP, XML or JSON files.

## Cases re-enabled

| Bug | OASIS 3.0 case ids | Final result |
| --- | --- | --- |
| policy4j-the.4.15 | IIC300, IIC310, IIC320 | 3 passed |
| policy4j-the.4.14 | IIC302, IIC303, IIC312, IIC313, IIC322, IIC323, IIC330, IIC331, IIC332, IIC333, IIC334, IIC335 | 12 passed |
| policy4j-the.4.21 | IID300 | 1 passed; see run 025 |

[05-enabled-case-probe-scope.json](05-enabled-case-probe-scope.json) records
the exact original and new rows. [08-final-case-results.csv](08-final-case-results.csv)
contains all 880 case receipts from the final clean gate; every enabled
case has a CONFORMANCE START marker and a passing Surefire invocation.
No targeted case remains failing. The other 28 disabled cases retain their
existing reasons and bug ids and were not re-probed in this packet.

## Scope and residual risk

Only StringFunctions, PermitOverrides, their two new tests, the 16 manifest
rows, the specified run-023 metadata and this lane's evidence were edited.
License blocks in existing Java files are byte-identical to the packet-start
copies; new tests use the same core LGPL block. All eight POMs and all
2,911 vendored fixture/schema files match the packet-start hashes.
[source-scope-audit.json](source-scope-audit.json) distinguishes this lane's
files from the conductor's merge and records the stable before/after hashes
of all 3,543 source/POM/metadata files covered by the final gate.

The source inventory is supported by source search and positive evaluation
tests. It is not a claim about arbitrary external provider aliases. The
28 retained conformance skips remain a coverage limit. The gate proves
behavior in this reactor, not a performance claim; JMH was not run for
these correctness fixes. No tracking state or task checkboxes were changed.

## Final gates

The machine-derived module and case counts follow. The full gate is the
conductor-requested JDK 21 `clean verify`, with all merged lanes present.

<!-- derived-counts -->

| Module | Tests run | Failures | Errors | Skipped |
| --- | ---: | ---: | ---: | ---: |
| xacml-core | 4323 | 0 | 0 | 2 |
| xacml-xml | 36 | 0 | 0 | 0 |
| xacml-json | 7 | 0 | 0 | 1 |
| xacml-pdp | 76 | 0 | 0 | 3 |
| xacml-test | 30 | 0 | 0 | 0 |
| xacml-conformance-tests | 901 | 0 | 0 | 28 |
| Total | 5373 | 0 | 0 | 34 |

These are Surefire invocation counts, including parameterized invocations and skips. The two new classes contribute 505 and 3,048 invocations. xacml-xml-jaxb has no tests.

| Suite | Manifest rows | Executed disposition | Special harness (passed / skipped) | Skipped disposition | Cases passed | Cases skipped |
| --- | ---: | ---: | --- | ---: | ---: | ---: |
| oasis20 | 374 | 360 | 2 / 0 | 12 | 362 | 12 |
| oasis30 | 487 | 470 | 1 / 1 | 15 | 471 | 16 |
| rsa2008 | 17 | 17 | 0 / 0 | 0 | 17 | 0 |
| example | 2 | 2 | 0 / 0 | 0 | 2 | 0 |
| Total | 880 | 849 | 3 / 1 | 27 | 852 | 28 |

The conformance module reports 901 invocations: 880 manifest cases, the two preserved testName tests, and 19 guard/import/resolver/integrity tests. Case totals exclude those 21 non-case checks. 852 cases passed, 0 failed, 28 were skipped. Skipped rows are retained, not evidence of a fresh failed evaluation.

Raw gate output: `../024-bugs-string-functions/07-conformance-gate.log` and `../024-bugs-string-functions/08-full-reactor-clean-verify.log`. Fresh module/class receipts: `../024-bugs-string-functions/08-full-reactor-results.json`; per-case receipts: `../024-bugs-string-functions/08-final-case-results.csv`. See surefire-summary.txt for exact Maven lines.
## rv038/rv039 fixes

The URI repair is complete. AnyURI stores URI.create(v) without normalization and overrides lexical equality/hash while keeping OtherValue and the URI getter.
Its repeated String branch now accepts StringVal, restoring the valid anyURI-from-string path in the same scoped class.
PermitOverrides' never-set atLeastOneIndeterminate flag and unreachable branch were removed; the 3,048-row override matrix passes in the final gate.
The conductor merged overridable Value equality/hash at 02:06; this lane did not edit Value.java or the merged engine/mapper files.

| Review | Change | Test / proof |
| --- | --- | --- |
| rv038 MEDIUM / rv044 finding 1 | Original URI spelling and codepoint equality/hash | AnyURILexicalTest: 34; XML and JSON AnyURIRequestLexicalTest: 3 each; Xacml30TestUtilityTest rejects both normalization/case pairs |
| rv039 LOW | Remove dead PermitOverrides flag | OverrideDecisionTableTest: 3,048 final invocations; oasis30/IID300 passes |

Run 03 is the complete pre-fix receipt: 113 tests, 29 failures, 4 errors. Run 04 is green at 113/0/0/0. Two later null-FulfillOn controls bring final focused coverage to 115/0/0/0.
Run 01's test setup/canonical-id errors and the stale comparator snapshot are explained in spec-and-caller-review.md; they are not used as fresh green evidence.

Final JDK 21 clean verify: all modules pass. Java 21.0.6 (Oracle GraalVM), Maven 3.9.16.
Raw final gate: evidence/tests/030-harness-comparators/review-rv042-rv044/11-full-reactor-clean-verify.log.
The complete conformance gates are run 08 before the merge (857 passed / 23 skipped), run 10 after the merge and manifest repair, and run 11 clean (859 / 21).
No conformance case failed or regressed. IIIA030 and IIIA330 are the only outcome changes: skipped -> passed, after the conductor's 4.24 merge.
All other case rows are byte-identical; fixtures and expected results are unchanged.

| Module | Tests run | Failures | Errors | Skipped |
| --- | --- | --- | --- | --- |
| xacml-core | 4502 | 0 | 0 | 5 |
| xacml-xml | 57 | 0 | 0 | 0 |
| xacml-json | 24 | 0 | 0 | 1 |
| xacml-pdp | 77 | 0 | 0 | 3 |
| xacml-test | 99 | 0 | 0 | 0 |
| xacml-conformance-tests | 901 | 0 | 0 | 21 |
| Total | 5660 | 0 | 0 | 30 |

| Suite | Rows | Executed disposition | Special (passed / skipped) | Skipped disposition | Cases passed | Cases skipped |
| --- | --- | --- | --- | --- | --- | --- |
| oasis20 | 374 | 360 | 2 / 0 | 12 | 362 | 12 |
| oasis30 | 487 | 477 | 1 / 1 | 8 | 478 | 9 |
| rsa2008 | 17 | 17 | 0 / 0 | 0 | 17 | 0 |
| example | 2 | 2 | 0 / 0 | 0 | 2 | 0 |

Surefire invocation totals include disabled and parameterized tests. The conformance module's 901 invocations contain 880 manifest cases and 21 non-case checks. xacml-xml-jaxb has no tests.
The final 115 focused review-test invocations pass. Original test methods and assertions remain; the nine owned Java license blocks are unchanged/template-matched.
review-rv038-rv039/source-audit.json distinguishes ten lane source/manifest changes from the conductor merge and proves final gate source stability. Poms and 2,911 vendored fixture/schema files are unchanged.
The 21 inherited case skips remain a coverage limit. Canonical string-from-anyURI lookup still needs a separate identifier fix (StringFunctions.java:276 omits :function:); conversion semantics were verified through its existing binding.
No commit, br, task tick, issue closure, install, fixture edit or expected-result edit was performed.

Detailed review receipts: [review-rv038-rv039/summary.md](review-rv038-rv039/summary.md).
