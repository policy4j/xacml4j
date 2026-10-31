FINAL REPORT (Codex Sol, GPT-6)

**FIX-D45: APPROVE. Cycle AZ: APPROVE. No P1–P3 findings.**

1. **FIX-D45 P2 — closed.** `Condition.java:72` excludes subclasses from sharing. Every fallback evaluates the private predicate at `Condition.java:119`; `PreparedConditions.java:141` returns an optional prepared expression, without consulting the overridable accessor for fallback. The accessor remains compatible. `SharedConditionValuesPdpTest.java:347` pins decisions and statuses for the external subclass, including missing inputs.

2. **FIX-D45 P3 — closed.** `Policy.java:269` builds outside the publication operation and returns the compare-and-exchange winner. The final map in `PreparedConditions.java:82` is fully populated before bundle construction. Preparation reads the policy’s conditions, expressions and attached function specs (`PreparedConditions.java:159`, `:381`); it captures no generation, resolved reference or target index. The current decision’s gates still apply at evaluation. Removing `Generation.conditions` (`PreparedPolicySets.java:61`) removes the reported generation-to-bundle retention path. The live-object histogram is supplied evidence, not independently rerun.

3. **AZ R1 — equivalent response construction.** `DefaultPolicyDecisionPoint.java:375` uses the same set factory and expected size as Guava 31.1’s per-key collection: default size two, delegated through `Platform` to `Sets.newHashSetWithExpectedSize`. The hash-order claim is correct for this implementation; it is not a portable ordering guarantee. Verified against the [Guava 31.1 HashMultimap source](https://github.com/google/guava/blob/v31.1/guava/src/com/google/common/collect/HashMultimap.java) and [Platform source](https://github.com/google/guava/blob/v31.1/guava/src/com/google/common/collect/Platform.java).

   Attribute construction is unchanged (`DefaultPolicyDecisionPoint.java:401`), preserving issuers, typed values, multiplicity and existing equality-based deduplication. On a second category, the prefix is discarded and the unchanged multimap path makes a complete pass (`:380`, `:388`). Concurrent publication remains append-only, with one winning bag per key (`DefaultEvaluationContextHandler.java:470`). The returned response therefore retains weakly consistent traversal semantics; it gains no snapshot guarantee.

4. **AZ current-policy shortcut — induction is sound.** The final root always answers no current policy (`RootEvaluationContext.java:72`, `:527`). Parent and top links are final, and `top` stops at foreign implementations and external rule-context subclasses (`DescendantEvaluationContext.java:75`, `:81`, `:163`). Consequently, a foreign context cannot occur inside a chain whose stored top is the engine root.

   Every engine set context, including the referenced-set variant, passes the common constructor check (`PolicySet.java:359`; `PolicySetIDReference.java:292`). The fixed, root-topped chain admitted there cannot later acquire a policy context. Other parents still receive the original query. The assertion at `PolicySet.java:372` is a useful regression tripwire; the source invariant supplies the proof.

5. **Tests — pragmatic.** They assert public outcomes: subclass decisions/statuses, resolved contents and ordering, and context creation failures and answers. The concurrent response test joins workers before assembly (`ResolvedAttributesPdpTest.java:201`), so it proves inclusion after completed concurrent resolution, not assembly overlapping publication. That latter case was reviewed from the source.

Verification: inspected `PATCH-AZ.diff` against current source at `e435e307ed0e04e95eed62ad058189a679a26edb`; scoped `ast-grep scan` and `rtk git diff --check` passed. Ripwire health passed 7/7; its impact results were PARTIAL because of name-based ambiguity and were checked against source.

No files changed and no builds or tests run. The reported reactor result—10,455 tests, zero failures/errors, 36 skipped—is supplied evidence (`REPORT-I-AZ-opus.md:181`). Performance and retention measurements were not independently reproduced.
