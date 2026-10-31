# 032: the fix of review RCJ2 (cycle J)

Date: 2026-10-27. Oracle GraalVM 25.0.4, Maven 3.9.16 offline.

RCJ2 (Codex GPT-6.1 Sol, REJECT): normalization indexed every `RandomAccess` caller list; a transformed
view over a `CopyOnWriteArrayList` changed while it was read gave the changed contents (or an index out of
bounds) where the old iterator gave the snapshot.

- Red (`cycleJ-rcj2-red.log`): new test `callerListIsNormalizedAsItsSnapshot` (replacement and removal
  variants): 13 run, 1 error, `ArrayIndexOutOfBoundsException: Index 2 out of bounds for length 2`.
- Fix: `FunctionSpecImpl.normalize` indexes only lists an index loop reads as their iterator would (Guava
  `ImmutableList`, the JDK `List.of` classes, `Arrays.asList` views of the varargs path) and copies any
  other caller list once through its own iteration (D16 updated).
- Green (`cycleJ-rcj2-green.log`): 592 focused tests, 0 failures.
- Reactor (`reactor.txt`): 8,304 tests, 0 failures, 0 errors, 18 skipped; xacml-core 6,578 (2 skipped),
  conformance 938 (12 skipped), xacml-jmh 195.
