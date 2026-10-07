# 042 green — FunctionSafetyNet43Test after the function fixes

JDK 21, offline, `xacml-core` with `-am`. Raw log: `surefire.log`. Command: `command.txt`.

Result: BUILD SUCCESS. Tests run: 87, Failures: 0, Errors: 0, Skipped: 12. Time 0.872 s.

The twelve skipped methods are the decision rows left disabled: C1 / policy4j-the.4.29 (rows 30 and 31), C2 / 4.6 (rows 37 and 38), C3 / 5.4 (rows 35 and 36), C4 (row 60), C5 (row 12), C6 (row 43), C7, and C8 / policy4j-the.4.44.

Two earlier attempts of this same log path failed before this success: a compile error in `NumericConversionFunctions` (primitive `intValue` and `EvaluationException(Status)`), then one Mockito `UnnecessaryStubbingException` on the xpath-node-equal throwing content. Both were corrected before this run. The red log is unchanged.
