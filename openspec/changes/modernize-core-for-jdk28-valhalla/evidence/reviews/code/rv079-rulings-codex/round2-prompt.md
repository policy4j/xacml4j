# Adversarial code review, round 2: ruled XACML semantics (xacml4j)

You are an independent reviewer. Do not use tools. Do not edit files. Answer
in one message. Review only the diff below. It is the complete diff of this
change against the last commit.

## Project

xacml4j is a Java 21 implementation of OASIS XACML 2.0/3.0 (policy model,
PDP, PIP). `xacml-core` holds the policy model and functions; `xacml-pdp`
holds the PDP runtime and the policy information point (PIP).

## Maintainer rulings this change implements (2026-10-07)

- D3: a PIP resolver failure is Indeterminate with status
  `processing-error`, regardless of `MustBePresent` (fail closed).
- D18: an XPath evaluation error of a selector that is not a syntax error is
  Indeterminate `processing-error` whatever `MustBePresent` is;
  `missing-attribute` only for an empty selection with `MustBePresent=true`;
  a syntax error keeps `syntax-error`.
- D5: `round` and `floor` follow IEEE 754: NaN stays NaN, an infinity stays,
  a tie goes to the even integer.
- D6: `double-to-integer` of NaN or an infinity is Indeterminate
  `processing-error`.
- D8: the `ipAddress-*` set functions under the 1.0 function namespace stay
  registered as engine extensions.
- D9: `x500Name-match` returns true iff the RDNs of the first name are a
  terminal sequence of the RDNs of the second, each RDN compared as
  `x500Name-equal` compares names (not a string suffix).
- D13, D14, D15: conformance cases for hierarchical resource scope, legacy
  2.0 request XPath, and three disputed fixtures stay skipped; only the
  reason text in the manifest CSV changes.

Deliberately unchanged: cyclic resolver detection still returns "no value";
integer overflow is a later task.

## Round 1 (Codex) findings and what was done

1. HIGH, resolver exception text became visible in the response status
   message. Fixed: `forResolverFailure` now builds the status with the fixed
   message `Attribute resolution failed`; the cause text stays in the
   exception message and the status error object only. (The base
   constructor `CoreException(Status, String)` copies the exception message
   into the status, so a new private constructor uses
   `(Status, String, Throwable)`, which keeps the status message.)
2. MEDIUM, a designator PIP that throws an `EvaluationException` with
   `syntax-error` kept that status. Fixed: `resolverFailure` now maps every
   PIP failure to `forResolverFailure`. Only a `PathEvaluationException` in
   the selector path is rethrown as is (the selector's own XPath result).
3. LOW, the fallback test did not prove order. Fixed: the test records the
   call order; a content-resolver failure test was added; a test proves the
   resolution stack is clean after a failure.

## Questions

Report findings ordered by severity (HIGH, MEDIUM, LOW), each with file,
line from the diff, a concrete failing scenario, and a suggested fix.

1. Are the three round-1 findings fully fixed? Is there any remaining path
   where resolver or backend text reaches a status message?
2. Fail-closed: any path where a resolver or XPath failure still becomes an
   empty bag, NotApplicable, or missing-attribute? Any path where a
   legitimate "attribute not available" now becomes Indeterminate?
3. `x500NameMatch`: is the RDN comparison correct (order of
   `LdapName.getRdns()`, multi-valued RDNs, escaping, empty names)? Can a
   valid `X500Principal` name fail `LdapName` parsing or
   `new X500Principal(rdn.toString())`?
4. `round` with `Math.rint` and `double-to-integer`: any value where the new
   behavior is wrong for the stated rulings?
5. Tests: any assertion weakened beyond what the rulings require? Important
   cases untested?

End with a verdict line: `VERDICT: APPROVE` or `VERDICT: REQUEST_CHANGES`.

## Diff

```diff
