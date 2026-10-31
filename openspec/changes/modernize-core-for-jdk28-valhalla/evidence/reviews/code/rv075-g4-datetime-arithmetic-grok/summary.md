# rv075: task 4.5 adversarial review, dateTime arithmetic and zone preservation (Grok)

Date: 2026-10-02. Lane: adversarial code reviewer, Grok Build grok-4.7
(xhigh), role `.ntm/roles/policy4j_code_review.md`, session cleared first.
Session `policy4j--g1`, pane 0, about 16 minutes. Read-only: main worktree,
run 058 evidence (author Sol/Codex), rv073.

Verdict: APPROVE (no findings).

- The 4.5 production change is timezone absence on `ISO8601DateTime`: a
  `timeZoneAbsent` flag set from the lexical form and from
  `XMLGregorianCalendar.getTimezone()`, carried through all six plus/minus
  overloads, and used to omit the offset in `toXacmlString`/`toStringExp`.
  Signs, month-end pinning and instant equality were already in the
  task-start snapshot.
- Arithmetic against XML Schema Part 2 Appendix E and XPath F&O 10.8.4-10.8.10:
  one day pin after combining years and months
  (`2020-02-29 + P1Y1M = 2021-03-29`, `2004-02-29 + P1Y = 2005-02-28`);
  negative durations negate the period or duration
  (`2002-03-31 +/- P1M = 2002-02-28`); `PT1.5S`/`-PT1.5S` with borrow; date
  results stay dates and keep a present offset; fixed offsets `-05:00` and
  `+05:30` are kept; 3.0 and 1.0 URNs share the methods; the registry test runs
  736 evaluations.
- Equality: equals/hashCode/compareTo unchanged and instant based; an unzoned
  value equals its Z form (same hashCode).
- a8a90d46 claim confirmed by the historical probe CSV: subtracting PT1H from
  08:00:00Z gave 09:00:00Z; fractions dropped; `2020-02-29 + P1Y1M` gave
  `2021-03-28`; date year-month arithmetic was a no-op that dropped the offset.
- Agy (rv073) missed no correctness bug on the edge cases the conductor named;
  overflow raises DateTimeException, which becomes
  FunctionInvocationException (Indeterminate).
- `ast-grep scan` on the three files: no matches.

Residual (test gaps, not wrong results): a fractional base with a fractional
duration; `2004-01-31 + P1M = 2004-02-29`; a day-time step across a month end.
`+00:00` prints as `Z` (pre-existing). serialVersionUID unchanged (old streams
read timeZoneAbsent=false, which matches their old Z printing). Conformance
total is 864 passed (the packet's 865 was a conductor arithmetic slip; the
README row is corrected).
