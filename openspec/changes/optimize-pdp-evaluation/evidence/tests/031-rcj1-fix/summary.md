# 031: the fix of review RCJ1 (cycle J)

Date: 2026-10-27. Oracle GraalVM 25.0.4, Maven 3.9.16 offline.

RCJ1 (Codex GPT-6.1 Sol, REJECT): the combining API takes the caller's list. Indexing a
`CopyOnWriteArrayList` loses its iterator's snapshot (a rule that replaces a later rule turned Deny into
Permit), and copying a list without random access read a lazy list past the first terminal decision.

- Red (`cycleJ-rcj1-red.log`): new tests `copyOnWriteListIsCombinedAsItsSnapshot` (PERMIT instead of
  DENY) and `lazyListIsReadOnlyAsFarAsTheCombinationGoes` (the second element was read and threw) failed on
  the indexed combiners: 5 run, 1 failure, 1 error.
- Fix: the combining algorithms and `BaseDecisionCombiningAlgorithm` are restored to HEAD (iterator loops,
  no `indexed` helper); cycle J keeps the target classes and normalization (D16 updated).
- Green (`cycleJ-rcj1-green.log`): 589 focused tests, 0 failures.
- Reactor (`reactor.txt`): 8,303 tests, 0 failures, 0 errors, 18 skipped; xacml-core 6,577 (2 skipped),
  conformance 938 (12 skipped), xacml-jmh 195.
