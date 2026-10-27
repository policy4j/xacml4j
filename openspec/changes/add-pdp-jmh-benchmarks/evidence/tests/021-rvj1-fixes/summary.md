# 021: fixes of review RVJ1

Date: 2026-10-27. JDK 25 (Oracle GraalVM 25.0.4), Maven 3.9.16 offline.

- Module tests after the fixes: 195 run, 0 failures (194 before, plus the new test).
- Mutation check: with the setup catch narrowed to `RuntimeException | Error`, `ReviewGateFixesTest`
  ran 10 with 1 failure, the new test: the closed ids miss the fixture's own repository (`missing (1)`).
  So the test fails when the cleanup of a checked import failure is removed. The file was restored
  from a copy; `git diff` of it shows only the intended changes.
