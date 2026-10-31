# Adversarial code review, round 4 (verification): ruled XACML semantics (xacml4j)

You are an independent reviewer. Do not use tools. Do not edit files. Answer
in one message. Review only the diff below. It is the complete diff of this
change against the last commit. The full reactor passes on it:
7,634 tests, 0 failures, 0 errors, 24 skipped; conformance 927 tests with 14 skipped, unchanged.

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

Facts you cannot see in the diff:

- `X500Principal.equals` compares canonical names; `getName(CANONICAL)`
  joins RDNs with `,` without spaces, escapes a comma in a value as `\,`,
  sorts the values of a multi-valued RDN, and keeps `#hex` for non-printable
  string encodings.
- `AttributeResolverDescriptor` wraps a non-`EvaluationException` thrown by
  a resolver function into an `EvaluationException` before the PIP sees it.
  It turns an empty result map into `Optional.empty()`, and a non-empty map
  into an `AttributeSet` that holds only the returned attributes.
- `CoreException(Status, String)` copies the exception message into the
  status message; `CoreException(Status, String, Throwable)` keeps the
  message of the status. A status message is marshalled into the response.
- `PathEvaluationException` is a final class with public static factories;
  anyone can make one with any status.
- The only place where the engine itself makes a `syntax-error` for a
  selector is `XmlContent`, when a `ContextSelectorId` path does not select
  exactly one node. That happens only when an entity is passed to
  `Content.resolve(key, entity)`. `Content.resolve(key)` (used by the
  default PIP on resolver content) never does it. An XPath that does not
  compile is a `PathEvaluationException` with status `processing-error`.
- `DefaultResolverRegistry` returns at most one content resolver per
  category and all attribute resolvers that declare the attribute, in
  registration order. `ResolverRegistry` is an interface, so another
  registry can return several content resolvers.
- `FunctionInvocationException` keeps the status of the first
  `EvaluationException` in its cause chain. The `attribute-selector`
  function calls `EvaluationContext.resolve(key)` directly, which delegates
  to `DefaultEvaluationContextHandler.resolve`.

## Round 3 findings and what was done

1. Codex HIGH: `isResolverFailure()` was overridable, so a resolver could
   throw a subclass that claims to be a mapped failure and keeps a status
   and message of its choice. Fixed: the mark is a private field (`origin`)
   that only the two factories set through a private constructor; the
   factories read the field directly (`madeBy`); the accessor is `final`.
2. Codex MEDIUM and Grok MEDIUM: a `PolicyInformationPoint` could throw a
   `PathEvaluationException` with status `syntax-error` and its own text,
   and both passed to the response. Fixed at the origin:
   `DefaultEvaluationContextHandler.resolveViaPip` maps every exception of
   `pip.resolve(context, selectorKey)` to `forResolverFailure`. Only the
   engine's own path evaluation (request content, or an entity resolved
   through `ContextSelectorId`) can report `syntax-error`.
   `doSelectorResolve` maps those path exceptions in the handler, so every
   caller of `resolve` gets a fixed status message: `forSyntaxError` for
   `syntax-error`, `forResolverFailure` for every other path error.
   `AttributeReference.resolveImpl` does the same for other context
   implementations; both factories are idempotent.
3. Grok HIGH: in the content loop of `DefaultPolicyInformationPoint`, content
   whose selection was empty ended the loop and dropped the failure of an
   earlier resolver. The attribute loop had the same hole for a resolver
   that returns attributes without the requested one. Fixed in both loops:
   only a present bag ends the loop; otherwise the next resolver is asked,
   and `emptyUnlessFailed(failure)` decides at the end. This is a behaviour
   change for several matching resolvers: a later resolver is now asked
   when an earlier one supplied attributes or content without a value.
4. Tests added: subclass of the mapped failure with `missing-attribute` and
   `syntax-error` (designator and selector PIP); PIP `syntax-error` with
   back-end text at handler and PDP level; path evaluation error on request
   content at handler level; fixed message for the engine's syntax error;
   failed resolver plus a resolver without the attribute; failed content
   resolver plus content that selects nothing, in both orders; a resolver
   without a value followed by one with a value, for attributes and content.

Known and not changed here: the `attribute-selector` function with an
entity argument evaluates the path on that entity directly
(`Entity.resolve`), outside the handler. That path keeps the status and
message of the `PathEvaluationException`, like any other function error.

## Questions

Report findings ordered by severity (HIGH, MEDIUM, LOW), each with file,
line from the diff, a concrete failing scenario, and a suggested fix. Report
only defects you can show from the diff and the facts above.

1. Are the round-3 findings fixed? Is there any remaining path through
   `DefaultEvaluationContextHandler.resolve` where resolver, back-end, or
   XPath engine text reaches a status message, or where a resolver or an
   information point can choose the status?
2. Is there any path where a failure still becomes an empty bag,
   NotApplicable, or missing-attribute, or where a legitimate "attribute not
   available" (no resolver failed) becomes Indeterminate?
3. Is the new loop rule in `DefaultPolicyInformationPoint` (only a present
   bag ends the loop) implemented the same way in both loops, including the
   cache writes?
4. Any assertion weakened beyond what the rulings require?

End with a verdict line: `VERDICT: APPROVE` or `VERDICT: REQUEST_CHANGES`.

## Diff

```diff
