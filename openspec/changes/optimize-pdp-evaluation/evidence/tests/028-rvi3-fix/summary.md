# 028: the fix of review RVI3 (cycle I), gate of task 13.3

Date: 2026-10-27. Oracle GraalVM 25.0.4, Maven 3.9.16 offline.

RVI3 (Codex GPT-6.1 Sol, APPROVE WITH CHANGES): a generated lambda class is strongly linked to its
defining loader, so binding a method of a hidden provider class would keep that class from unloading.

- Red (`cycleI-rvi3-red.log`): `DirectBindingEligibilityTest` (a hidden copy of `DirectBindingTemplate`
  defined without `STRONG`): 2 run, 1 failure, the hidden class's method was bound.
- Fix: `isBindable` excludes a hidden declaring class; its calls take the reflective path.
- Green (`cycleI-rvi3-green.log`): 243 function tests, 0 failures; the hidden class's method is unbound
  and still returns the right value.
- Reactor (`reactor.txt`): 8,297 tests, 0 failures, 0 errors, 18 skipped; xacml-core 6,571 (2 skipped),
  conformance 938 (12 skipped), xacml-jmh 195.

Cycle I reviews: RVI1 (REJECT, 2 P2 fixed), RVI2 (REJECT, 1 P2 fixed), RVI3 (APPROVE WITH CHANGES, fixed).
No JMH run: batch 2 is measured once (task 18.1).
