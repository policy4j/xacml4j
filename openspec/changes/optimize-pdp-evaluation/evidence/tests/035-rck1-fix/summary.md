# 035: the fix of review RCK1 (cycle K)

Date: 2026-10-27. Oracle GraalVM 25.0.4, Maven 3.9.16 offline.

RCK1 (Codex GPT-6.1 Sol, REJECT, two P2): final walked methods break compiled subclasses of the public
`Rule.RuleEvaluationContext`; `isEnclosed` on parent fields bypasses an overridden `getParentContext()`.

Fix (design D17 updated): HEAD's final set restored (only `resolve(AttributeReferenceKey)`); the walks pass only
through the package's own contexts (`isOwn`, `Rule.isOwnContext` with the named
`Rule.ExtendedIndeterminateRuleContext`) and call any other context; `isEnclosed` restored to HEAD.

- Focused (`cycleK-rck1-fix.log`): `ExternalRuleContextCompatibilityTest` 3, `DescendantContextInventoryTest` 1,
  `ContextChainQueryCharacterizationTest` 4, `DefaultEvaluationContextHandlerTest` 40: all pass. The log also
  holds the first run of the next cycle's `DecisionDebugLoggingTest` (2 of 2 red, as intended; see 036).
  The new compatibility tests were written after the fix: against the reviewed version the external subclass
  does not compile (final `getClock`), so no red run exists.
- Reactor (`reactor.txt`, `clean test`, with the cycle L test parked outside the tree): 8,313 tests, 0
  failures, 0 errors, 18 skipped; conformance 938 (12 skipped), unchanged.
