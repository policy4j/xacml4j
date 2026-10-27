# 027: the fix of review RVI2 (cycle I)

Date: 2026-10-27. Oracle GraalVM 25.0.4, Maven 3.9.16 offline.

RVI2 (Codex GPT-6.1 Sol): a public method of a public provider whose parameter types are not accessible
to the binding's lookup is bound; the generated class casts its arguments to those types and fails with
`IllegalAccessError` where reflection works.

- Red (`cycleI-rvi2-red.log`): new test `parametersOfATypeTheBindingCannotAccessWorkAsOnTheReflectivePath`
  (a public provider whose method takes a private nested `ValueExp` class): 13 run, 1 failure, the direct
  path failed with cause `IllegalAccessError` ("failed to access class ...DirectPairCallTest$Secret"),
  the reflective path returned `true`.
- Fix: `DefaultFunctionInvocation.bind` calls `Lookup.accessClass` on every parameter type and the return
  type before it generates the lambda; a type it cannot access leaves the method unbound (reflective path).
- Green (`cycleI-rvi2-green.log`): `DirectPairCallTest` 13 of 13; the function tests of xacml-core, 241 run,
  0 failures.
- Reactor (`reactor.txt`, `mvn -B -o clean test`): 8,295 tests, 0 failures, 0 errors, 18 skipped;
  xacml-core 6,569 (2 skipped), conformance 938 (12 skipped), xacml-jmh 195.
