# Run 037: function audit and test design for task 4.3 (Grok)

Date: 2026-10-02. Lane: adversarial code reviewer, Grok Build grok-4.7
(xhigh), role `.ntm/roles/policy4j_code_review.md`, session cleared first.
Session `policy4j--g1`, pane 0. Read-only: function implementations and
value types, OASIS XACML 3.0 Core OS (22 January 2013, line numbers of the
HTML), JDK 21 jshell probes. Maven not run.
Report: [grok-report.md](grok-report.md), extracted from the Grok session's
chat history (exact text).

Result: 15 new defects (A), 60 test rows covering every item of task 4.3 (B),
8 maintainer decisions (C).

## Conductor check and filing

Confirmed in source before filing: NEW-1 (`HigherOrderFunctions.java`
any-of-all loops first-bag elements with allOf over the second bag), NEW-2
(`RegExpFunctions.java:55-95` 1.0 URNs), NEW-3 (`StringFunctions.java:204-384`
ids without `:function:`), NEW-4 (`NumericConversionFunctions.java:44-57`
swapped return types). The other findings are filed from the report and are
confirmed or refuted by their red tests.

| Finding | Bead |
| --- | --- |
| NEW-1 any-of-all quantifier | policy4j-the.4.35 |
| NEW-2 typed regexp-match URNs | policy4j-the.4.36 |
| NEW-3 string-from-* ids | policy4j-the.4.37 |
| NEW-4 double/integer conversion | policy4j-the.4.38 |
| NEW-5 rfc822Name-match | policy4j-the.4.39 |
| NEW-6, NEW-7 xpath-node-* | policy4j-the.4.40 |
| NEW-8 regexp alternation | policy4j-the.4.41 |
| NEW-9 double >= and <= | policy4j-the.4.42 |
| NEW-10 syntax-error lost in invocation | policy4j-the.4.43 |
| NEW-11 integer overflow | comment on task 5.4 (planned non-wrapping arithmetic) |
| NEW-12 x500Name-match | policy4j-the.4.44 (behavior per decision C8) |
| NEW-13 lower-case locale | policy4j-the.4.45 |
| NEW-14 round NaN/INF | decision C4 |
| NEW-15 normalize-space | policy4j-the.4.46 |

## Decisions for the maintainer (C)

C1 double-equal(NaN, NaN); C2 integer divide/mod rounding; C3 integer
overflow; C4 round/floor on NaN, infinity, halfway; C5 double-to-integer of
NaN/INF; C6 integer-mod by zero; C7 ipAddress set functions registered at
1.0 although the spec has none; C8 x500Name-match on multi-valued RDNs.
C1, C2, C4, C5, C6 are noted on task 4.6; C3 on task 5.4.

## Use

Table B is the minimum test list for task 4.3. Rows marked N are written as
tests and disabled with the bead id until the fix lands; decision-dependent
rows stay disabled with the decision id.
