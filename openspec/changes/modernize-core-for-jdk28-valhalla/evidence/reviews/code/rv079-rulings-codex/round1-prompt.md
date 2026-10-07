# Adversarial code review: fail-closed attribute resolution (xacml4j)

You are an independent reviewer. Do not use tools. Do not edit files. Answer
in one message. Review only the diff below.

## Project

xacml4j is a Java 21 implementation of OASIS XACML 2.0/3.0 (policy model,
PDP, PIP). Module `xacml-core` holds the policy model; `xacml-pdp` holds the
PDP runtime and the policy information point (PIP).

## Decision this change implements

The maintainer ruled on 2026-10-07:

- D3: a PIP resolver failure is Indeterminate with status
  `processing-error`, regardless of `MustBePresent` (fail closed). Before
  this change every resolver exception became "no value": an empty bag for
  `MustBePresent=false`, `missing-attribute` for `true`. A Deny rule that
  depends on a PIP attribute silently stopped applying when the PIP failed.
- D18: an XPath evaluation error of a selector that is not a syntax error is
  Indeterminate with `processing-error`, whatever `MustBePresent` is.
  `missing-attribute` is only for an empty selection with
  `MustBePresent=true`. A syntax error keeps `syntax-error`.

Deliberately unchanged: cyclic resolver detection in
`DefaultEvaluationContextHandler` still returns "no value", and a resolver
whose key attribute is absent is still called with the key missing.

## What changed

1. `AttributeReference.resolveImpl`: only an `EvaluationException` with
   status missing-attribute follows `MustBePresent`; every other
   `EvaluationException` is rethrown; any other `RuntimeException` becomes
   `AttributeReferenceEvaluationException.forResolverFailure` (status
   processing-error).
2. `DefaultEvaluationContextHandler.doPipResolve` (designator and selector):
   an exception from the PIP is no longer swallowed. `resolverFailure` keeps
   a non-missing `EvaluationException` and wraps everything else.
3. `DefaultPolicyInformationPoint.resolve` (both overloads): a resolver
   exception no longer ends as `Optional.empty()`. The loop continues to the
   next matching resolver; if none supplies a value and at least one failed,
   the first failure is thrown.
4. Tests and the OpenSpec delta spec were updated to the ruled behavior.

## Questions

Report findings ordered by severity (HIGH, MEDIUM, LOW), each with file,
line from the diff, a concrete failing scenario, and a suggested fix.

1. Correctness: is there any path where a resolver or XPath failure still
   becomes an empty bag, NotApplicable, or missing-attribute?
2. Regressions: is there any path where a legitimate "attribute not
   available" case now becomes Indeterminate processing-error? Consider
   nested resolution (a resolver that resolves its key through the context),
   the `ContextSelectorId` branch of the selector `doPipResolve`, caches,
   and `finally` blocks (the resolution stacks).
3. The `resolverFailure` mapping: is it right to wrap an
   `EvaluationException` with status missing-attribute thrown by a PIP into
   processing-error?
4. `DefaultPolicyInformationPoint`: is "first failure wins, a later value
   still wins over a failure" sound? Any issue with throwing the original
   `RuntimeException`?
5. Information exposure: the status message now carries the exception
   message of the resolver. Is that a new leak compared with the code
   before?
6. Tests: do the changed tests weaken any earlier assertion beyond what the
   ruling requires? Are important cases untested?

End with a verdict line: `VERDICT: APPROVE` or `VERDICT: REQUEST_CHANGES`.

## Diff

```diff
