# 042 red — FunctionSafetyNet43Test before production fixes

JDK 21, offline, `xacml-core` with `-am`. Raw log: `surefire.log`. Command: `command.txt`.

Result: BUILD FAILURE. Tests run: 87, Failures: 34, Errors: 3, Skipped: 12. Time 1.318 s.

Failures and errors match the Table B defects (NEW-1 through NEW-10, NEW-13, NEW-15). Twelve decision rows stayed skipped (C1, C2/4.6, C3/5.4, C4, C5, C6, C7, C8/4.44).

`row22XPathNodeEqualPropagatesEvaluationFailure` errored with Mockito `UnfinishedStubbing` because `throwingContent()` was called inside `when(...).thenReturn(...)`. The production swallow of `EvaluationException` is already shown by `row21` ("nothing was thrown"). The stub call was moved out of `when` after this run so the assertion can execute.

`stringRegexpMatch` empty-pattern rows errored with `FunctionInvocationException` (`StringIndexOutOfBoundsException` from `charAt(0)`), which is the red form of NEW-8.
