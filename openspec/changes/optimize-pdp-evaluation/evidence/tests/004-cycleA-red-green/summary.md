# 004: cycle A tests, red and green (tasks 4.1, 4.2)

Date: 2026-10-27. Oracle GraalVM 25.0.4, Maven 3.9.16 offline, release 25.

Tests added to `xacml-pdp/src/test/java/org/xacml4j/v30/pdp/DefaultEvaluationContextHandlerTest.java`
(spec `pdp-attribute-resolution`):
- `repeatedRequestDesignatorIsTakenFromTheRequestOnceAndGivesTheSameBag` (new behavior)
- `attributeFromTheInformationPointIsAskedOnceAndListedAsResolved`, `absentAttributeIsNotRemembered`,
  `failedResolutionIsNotRemembered`, `separateDecisionsResolveSeparately` (characterization: they pass
  before and after)

Red, before D5: 36 tests, 1 failure: the repeated designator returned two different bag instances
("expected: BagOfValues@17d32e9b ... but was: BagOfValues@66f0548d") with two `getEntity` calls.

Change (D5): `DefaultEvaluationContextHandler` keeps `requestDesignators`, a decision-scoped map of
bags found in the request, consulted after the PIP-result map; the first bag wins
(`putIfAbsent`); absence and failures are not kept; `getResolvedDesignators()` still lists only
PIP results. The `Optional.or` lambdas of the designator path are gone with it.

Green: full reactor `clean test` BUILD SUCCESS, 8,220 tests, 0 failures, 18 skipped;
`xacml-pdp` 207 (202 plus 5); conformance 938 run, 12 skipped (unchanged).
