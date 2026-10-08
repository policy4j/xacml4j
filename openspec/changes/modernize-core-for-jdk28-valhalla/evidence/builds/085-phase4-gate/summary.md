# Run 085: phase 4 gate, safety net green on JDK 21 (task 4.7, bead the.4.7)

Date: 2026-10-08. JDK: Oracle GraalVM 21.0.6+8.1
(`java version "21.0.6" 2025-01-21 LTS`). Branch:
`integration/modernize-core-jdk28-valhalla`.

Task 4.7: "Gate: all safety-net tests pass on JDK 21; conformance
manifest unchanged or improved". Header gate of `tasks.md`: `verify`
passes on JDK 21, no license header churn, the manifest loses no executed
case.

## Result

| Condition | Result | Proof |
| --- | --- | --- |
| `clean verify` on JDK 21 | PASS: 7,966 tests, 0 failures, 0 errors, 18 skipped | `surefire-summary.txt` |
| Safety-net tests | PASS: none is disabled (see "Skipped tests") | below |
| Conformance | PASS: 937 tests, 0 failures, 12 skipped; the 12 are the rows of the manifest that are not enabled | `surefire-summary.txt`, manifest |
| Manifest unchanged or improved | PASS: 868 of 880 rows enabled; 866 at task 4.6 (run 069). The two more are IIA005 of both suites (run 075). No row was lost | manifest counts below |
| License header churn | none: after the build `git status` shows the two files of this run only | `command.txt` |
| Open beads of phase 4 | none besides the gate itself | `br list` |
| JMH | not measured; decision below | - |

## Skipped tests

| Tests | Count | Why |
| --- | --- | --- |
| `Xacml20ConformanceTest`, `Xacml30ConformanceTest` | 7 and 5 | manifest rows with `enabled=false`: unsupported features and adjudicated fixtures, each with a reason (runs 073, 075) |
| `ContentTest.testXmlBase64`, `Xacml20XPathTo30TransformerTest.testXPathTransformationWithFunctions`, `JsonEntityMarshallingTest.testEntityMarshall`, `BaseAttributeResolverTest.testMBeanInvocationStats`, two tests of `DefaultPolicyInformationPointTest` | 6 | `@Ignore` on the baseline `a8a90d46`, carried over as `@Disabled` by the JUnit 5 migration (group 2). None is a test of phase 4. No reason is written on them |

Enabled in this run: two tests of `FunctionProviderServiceLoaderTest`
were still `@Disabled` with the reasons "policy4j-the.4.37 ... are being
repaired" and "policy4j-the.4.36 ... are being repaired". Both beads were
closed on 2026-10-02 (run 042). The annotations are removed; the 16 rows
(11 `string-from-*` ids, 5 XACML 2.0 `*-regexp-match` ids) pass. Before:
7,952 tests with 20 skipped; now 7,966 with 18.

## Conformance manifest

`xacml-conformance-tests/src/test/manifest/conformance-manifest.csv`,
880 rows, last changed by run 075 (`ea852c58`):

| Suite | executed | special-harness | skipped or not enabled |
| --- | --- | --- | --- |
| oasis20 | 362 | 5 | 7 |
| oasis30 | 476 | 6 | 5 (4 skipped, 1 special-harness not enabled) |
| rsa2008 | 17 | 0 | 0 |
| example | 2 | 0 | 0 |

## Beads closed since the last gate note (2026-10-07)

| Bead | Commit | Run |
| --- | --- | --- |
| the.4.54 nested response staging tests | `e2b60578` | 079 |
| the.4.51 regular-expression limits | `d0ee4272`, `55a2a84d` | 078 |
| the.4.59 integer arithmetic does not wrap (ruling D4) | `2de18916` | 080 |
| the.4.57 one limit for the multiple-decision expansion | `9d6d12bc` | 081 |
| the.4.60 XML content comparison | `8baa9d42` | 082 |
| the.4.56 argument check of Apply | `dc7140f4` | 083 |
| the.4.58 regular-expression residual, accepted as a limit | `67bfd13d` | 084 |

The.4.56, the.4.57 and the.4.58 waited for a maintainer decision. They
were carried out on the recommendations of the session under the
maintainer's standing instruction; each run says so under "Decision
basis".

## JMH decision

No JMH module exists before task 9.2. The runs that touch a hot path
record that they are unmeasured: 072 (x500Name-match), 074 (lazy logical
and higher-order functions), 078 (regular expressions), 080 (integer
arithmetic), 081 (one wrapper object and two counters per request), 082
(`XmlContent.hashCode` walks the document), 083 (one field read per
`Apply` evaluation). The gate is closed on tests and conformance. No
performance acceptance follows from it; the measurements belong to group
9.

## What this gate does not say

- A green reactor and 18 named skips are not a proof of whole-engine
  compliance (note of 2026-10-04 on the bead).
- The six inherited ignored tests were not examined.
- The default limits of runs 077, 078 and 081 (100 node paths, 100
  individual requests, the regular-expression limits) are proposals of
  the session.
- Open by decision, not by defect: the regular-expression residual (run
  084); the policy-level reading of section 7.19.2 (run 083, limits);
  white-space leniency of the XML content comparison (run 082, limits).
- The conversions and the lexing of `xs:integer` stay with task 5.4.
