# Adversarial code review, round 3 (verification): ruled XACML semantics (xacml4j)

You are an independent reviewer. Do not use tools. Do not edit files. Answer
in one message. Review only the diff below. It is the complete diff of this
change against the last commit. The full reactor passes on it: 7,624 tests,
0 failures, conformance unchanged.

## Project

xacml4j is a Java 21 implementation of OASIS XACML 2.0/3.0. `xacml-core`
holds the policy model and functions; `xacml-pdp` holds the PDP runtime and
the policy information point (PIP).

## Maintainer rulings this change implements (2026-10-07)

- D3: a PIP resolver failure is Indeterminate `processing-error`,
  regardless of `MustBePresent` (fail closed).
- D18: an XPath evaluation error of a selector that is not a syntax error is
  Indeterminate `processing-error` whatever `MustBePresent` is;
  `missing-attribute` only for an empty selection with `MustBePresent=true`;
  a syntax error keeps `syntax-error`.
- D5: `round` and `floor` follow IEEE 754 (NaN stays, an infinity stays, a
  tie goes to the even integer). D6: `double-to-integer` of NaN or an
  infinity is Indeterminate `processing-error`. D8: the `ipAddress-*` set
  functions stay registered as extensions.
- D9: `x500Name-match` is true iff the RDNs of the first name are a terminal
  sequence of the RDNs of the second, each RDN compared as `x500Name-equal`
  compares names.
- D13, D14, D15: only reason text changes in the conformance manifest CSV.

Facts you cannot see in the diff: `X500Principal.equals` compares canonical
names; `getName(CANONICAL)` joins RDNs with `,` without spaces, escapes a
comma in a value as `\,`, sorts the values of a multi-valued RDN, and keeps
`#hex` for non-printable string encodings. `AttributeResolverDescriptor`
wraps a non-`EvaluationException` thrown by a resolver function into an
`EvaluationException` before the PIP sees it. `CoreException(Status, String)`
copies the exception message into the status message;
`CoreException(Status, String, Throwable)` keeps the message of the status.
A status message is marshalled into the response.

## Round 2 findings (Codex and Grok) and what was done

1. A content resolver could throw a `PathEvaluationException` (any status,
   any text) that the handler rethrew unchanged. Fixed at the boundary:
   `DefaultPolicyInformationPoint` now maps every exception from the
   resolver boundary (context creation, cache, resolver function) to
   `forResolverFailure`. The selector path is evaluated on the supplied
   content outside that `try`. The handler also maps a
   `PathEvaluationException` with status missing-attribute to a failure.
2. The "already mapped" shortcut trusted class plus status code. Fixed:
   `forResolverFailure` is idempotent only for an exception that the factory
   itself made (`isResolverFailure()`); everything else is wrapped, so the
   status message is always the fixed text.
3. XPath engine error text could reach the response through a
   `PathEvaluationException` with status processing-error. Fixed in
   `AttributeReference.resolveImpl`: missing-attribute follows
   `MustBePresent`; syntax-error is rethrown; every other
   `EvaluationException` becomes `forResolverFailure` (fixed message).
4. `x500Name-match` rebuilt RDNs from RFC 2253 and could change equality or
   fail to parse. Fixed: it now splits the canonical form of each name at
   unescaped commas and compares the lists, so it uses exactly the
   comparison of `x500Name-equal` and cannot fail on a valid name.
5. Tests added: resolver-chosen exception types and statuses for attribute
   and content resolvers; selector response message; path error on supplied
   content; consistency of `x500Name-match` with `x500Name-equal`; reversed
   multi-valued RDN; escaped comma; BMPString encoding; empty name;
   `round(-1.5)` and `round(-0.5)`.

## Questions

Report findings ordered by severity (HIGH, MEDIUM, LOW), each with file,
line from the diff, a concrete failing scenario, and a suggested fix. Report
only defects you can show from the diff and the facts above.

1. Are the round-2 findings fixed? Is there any remaining path where
   resolver, back-end, or XPath engine text reaches a status message, or
   where a resolver can choose the status?
2. Is there any path where a failure still becomes an empty bag,
   NotApplicable, or missing-attribute, or where a legitimate "attribute not
   available" becomes Indeterminate?
3. `canonicalRdns`: is the split at unescaped commas correct for the
   canonical form (escape handling, empty name, trailing backslash)?
4. Any assertion weakened beyond what the rulings require?

End with a verdict line: `VERDICT: APPROVE` or `VERDICT: REQUEST_CHANGES`.

## Diff

```diff
