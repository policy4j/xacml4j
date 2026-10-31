# RVI1: review of cycle I (D15)

Reviewer: Codex GPT-6.1 Sol, NTM pane, read-only. Verdict: REJECT (two behavioral differences).

| Finding | Disposition |
| --- | --- |
| P2: evaluated operands were typed `ValueExp`; a `FunctionReference` evaluates to itself and is only an `Expression`, so the direct path failed with `ClassCastException` where the reflective path worked, and changed the failure of a reference given to `string-equal` | Fixed: operands stay `Expression` in `FunctionSpecImpl.invokeDirect` and `DefaultFunctionInvocation.invokeDirect`; the parameter-type check sends a wrong type to the reflective path. Tests `functionReferenceParameterWorksOnBothPaths`, `functionReferenceOperandOfTheWrongTypeFailsAsOnTheReflectivePath` (red, then green: `tests/026-rvi1-fixes`) |
| P2: the binding's lookup (package `impl`) could reach a public method of a non-public provider class that the reflective call (package `function`) cannot | Fixed: only public static methods of public classes with public enclosing classes are bound. Test `providerClassThatIsNotPublicFailsAsOnTheReflectivePath` with `impl/PackagePrivateFunctions` (red, then green) |
| P3: the direct path's debug output differed from the generic path | Superseded by the maintainer's decision on Open Question 2 (2026-10-27, D18): per-element debug lines are removed; the direct path's per-call success line was dropped; cycle L removes the rest consistently |

Re-check: RVI2.
