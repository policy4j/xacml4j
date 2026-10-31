# Run 084: the regular-expression residual is accepted as a limit (bead the.4.58)

Date: 2026-10-08. Source of the bead: review rv087, round 2, finding 2
(run 078). JDK: NOT APPLICABLE for the decision; the gate ran on JDK 21.

## Decision basis

The bead is a P1 security bug with the label `needs-decision` and four
options (a to d). The recommendation of this session was option a:
accept the residual and document it. It was carried out on the
maintainer's standing instruction to implement the recommendations
without waiting; no separate ruling exists for it. This record is a risk
acceptance made on a recommendation. The maintainer can reopen the bead
to choose option b, c or d.

## The residual

`java.util.regex` has no step budget. The limits of bead the.4.51 bound
the pattern length, the input length, the time of a match that reads its
input, and nested counted repetitions. A pattern can still make the
matcher work without a read of the input:
`"(?:(?=)|(?=))".repeat(26) + "(?!)"` on the empty input took 2,092 ms on
JDK 21.0.6 (`tests/078-regexp-limits/regex-probe.txt`, probe 4); 40 such
groups would take hours by extrapolation, and the pattern has 524
characters. That work is not stopped. A late result is discarded
(Indeterminate, processing-error); the processor time is spent.

## Why option a

- The pattern is the first argument of a `*-regexp-match` function. In a
  policy it is normally a literal, and policies come from trusted
  authors. The request controls the pattern only when a policy takes it
  from a request attribute.
- Option b (an own parser for the XPath regular-expression grammar),
  option c (a linear-time engine, without back-references) and option d
  (a worker pool that abandons threads) each change behaviour or add a
  dependency. None is needed while patterns are policy content.
- The optional part of option a, rejecting a `*-regexp-match` whose
  pattern is not a literal, was not carried out. It would make policies
  that compute a pattern Indeterminate; that is a behaviour change that
  the recommendation did not include.

## Change

- `xacml-core/.../function/impl/RegExpFunctions`: a class comment states
  the rule for policy authors: the pattern is policy content, the limits
  do not bound every pattern, and a policy must not take the pattern from
  the request. Comment only.
- Already in place since run 078: the requirement "Regular-expression
  matching is bounded" in `specs/request-limits/spec.md` ends with what
  is not bounded, and the comment of `repetitionWeight` says the same.

## Proof

Gate of `builds/085-phase4-gate` (the comment was in the tree): JDK 21
clean reactor, 7,966 tests, 0 failures, 0 errors, 18 skipped; conformance
937 with 12 skipped. No test changed for this bead: nothing in the
behaviour changed.

## Review

None. The change is a comment and a recorded decision.

## Limits

- A policy that takes a regular-expression pattern from the request lets
  the request use processor time of the PDP without bound. Nothing in the
  engine detects such a policy.
- The acceptance test of the bead (40 groups, Indeterminate within the
  time limit, no thread left running) is not met; the bead says that it
  is not required for option a.
