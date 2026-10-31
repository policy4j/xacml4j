**D52 corrected candidate: APPROVE. Date fix (task 5.8): APPROVE.** Neither has a production defect. D52 has two P3 test gaps, and neither blocks approval. The candidates combine cleanly with Terra's D47 correction. Nothing was built or run, so every result below is traced in source. As instructed, I wrote no file; this pane is the record for rv208-d52-opus.

FINAL REPORT (xacml4j Code reviewer, Opus 5.5)

## Scope

- **D52:** `W/cod_3`, base `759d90a6`. All nine SHA-256 values matched before and after the review.
- **Date fix:** `W/cod_4`, base `759d90a6`. The packet gave no hashes, so I recorded these: `DateTimeDurationUtils.java` `94e5fa9a…`, `ISO8601DateTest.java` `5b0aaeeb…`.
- **D47, for question 3:** `W/cod_1` at `Rule.java` `6ee7c549…` and `TargetBeforeContext.java` `761d116c…`. Both match Terra's `205-d47-correction-terra/candidate-ready.json`.
- **Moving file:** Terra's lane was editing `Rule.java` during this review. It briefly held a test mutant (`33cd7de3…`, `return 0`). I did not review that version.

## Findings: D52

No P1 or P2. Both findings are test gaps; the production code is correct today.

### P3, D52-OPUS-1: rv204's first counterexample has no regression test

Location: guard at `FunctionSpecBuilder.java:329`; tests at `HigherOrderMetadataCompatibilityTest.java:104-136,164-213` and `SharedConditionValuesPdpTest.java:213-253`.

- **Gap:** every custom-parameter row also has a custom operand, so `HigherOrderCall.java:76-84` refuses the plan before line 329 matters. Reverting line 329 to the frozen `DynamicType.isDynamic` check (`correction.patch:48`) would keep every test green.
- **Failure if it regresses:**
  - Predicate: `.param(descriptor)`, where `descriptor.equals(XacmlTypes.STRING)` is true at build time.
  - Operands: canonical `string` types.
  - During evaluation: the operand flips the descriptor and returns an empty bag.
  - Expected: Indeterminate{P} with syntax-error.
  - Actual: `all-of` gives Permit and `any-of` gives NotApplicable, the same wrong results as `red-pdp.log`.
- **Fix:** add a row to `metadataChangedDuringEvaluationIsStillASyntaxError` with a descriptor that equals `STRING` until the operand flips it, and canonical operands.

### P3, D52-OPUS-2: nothing pins "never evaluating them again" on the fallback

Location: `DefaultFunctionInvocation.java:213,231`; requirement at `design.md:2065-2066`.

- **Gap:** `ApplyArgumentValidationTest.java:294-352` reaches the fallback with a live plan but never counts operand evaluations. That file's only `verify` calls are at `:514,626,632`, which are other tests.
- **Failure if it regresses:** suppose the fallback becomes `spec.invokeWithList(context, arguments)`. A custom function declared `string` that returns an integer then runs twice, not once. The status stays the same, but a function with side effects or remote cost runs twice per decision.
- **Fix:** in that test's loop, call `clearInvocations(wrongBag)`, then `verify(wrongBag, times(1)).evaluate(context)` after each Apply.

## Question 1: prepared path against checked path

Outcomes are identical; the hypothesis that they differ is refuted. The plan differs from `invokeFast` in only two ways: it skips the signature walk once the value types match, and it skips `FunctionReference.evaluate`, which only returns `this` (`FunctionReference.java:161-164`).

| Aspect | Checked | Prepared | Result |
| --- | --- | --- | --- |
| Operand order and count | `FunctionSpecBuilder.java:657-660` | `HigherOrderCall.java:96,100` | Same: in order, once each |
| Signature check | `HigherOrderFunctions.java:86,99,159,172` | Skipped only after `DefaultFunctionInvocation.java:207,224-225` | Same; see below |
| Wrong type or null value | syntax-error, "Invalid higher-order function arguments" | `:213,231` run the same check on the evaluated values | Same status and message |
| Bag position | `HigherOrderFunctions.java:87-92,160-165` | `bagFirst`, `:186-187` | Same predicate argument order |
| Short-circuit, later-deciding, empty bag | `single` `:104-122`; `DecidingOperands.java:197-218` | Same two methods | Same code |
| Predicate failure | `DefaultFunctionInvocation.java:284-285`; `FunctionSpecBuilder.java:664-665` | `:210-211,228-229`; `:343-344,356-357` | Outer `any-of`/`all-of` spec, one observation |
| Operand failure | Outside the function wrapper | Outside the wrapper | Same, ends at `Apply.java:299-302` |
| Runtime validation on | `FunctionSpecBuilder.java:651-652` | `Apply.java:292` skips the plan | Same code |
| Observers | Only `functionFailed` | Only `functionFailed` | No success event can differ |

The skipped check cannot change an outcome:
- The reference type is fixed at construction (`FunctionReference.java:51,62`).
- `HigherOrderCall.java:85` proves the parameters against canonical enum types.
- At runtime the enum is the receiver of `equals` (`BagOfValuesType.java:92`, `DefaultFunctionInvocation.java:225`), so a custom `equals` cannot pass the check.

That also closes both rejections, D52-ASTRA-1 and rv204's P2-1.

## Question 2: D45 and regex binding

- **Clones still prepare.** `PreparedConditions.java:406` runs the `Apply` constructor, and `Shared` passes its type through (`:475-477`). A shared expression needs two transformations (`:339`), so it is always a `string` call and passes `HigherOrderCall.java:80`. The author's `SharedConditionValuesPdpTest.java:202-208` asserts this on all six clones.
- **Stale or wrong memo: refuted.**
  - The plan holds only metadata and the argument list (`HigherOrderCall.java:40-45`), so it stores no values.
  - It reaches the memo only through `Shared.evaluate`, the same call the checked path makes.
  - The memo belongs to one decision's root (`RootEvaluationContext.java:111,898-913`).
  - Failures are never stored (`PreparedConditions.java:492`).
  - Stored values are only exact `StringVal` (`:278`), which matches the expected type.
- **Regex binding: unchanged.** A binding needs a `Value` first (`Apply.java:103`), and a plan needs a `FunctionReference` (`HigherOrderCall.java:61`), which is not a `Value`. The binding still takes precedence (`Apply.java:289-291`).
- **Later-deciding operands: unchanged.** The plan uses the same `firstDeciding`, and a shared operand is evaluated once, before the body.

## Question 3: combination with D47

No concrete break.

- **Context:** `HigherOrderCall.evaluate` passes on the context it receives and creates none (`:94-103`).
- **`Rule.EngineOnly`:** it reads only `apply.function()`, `getArguments()` and `FunctionReference.function()` (`Rule.java:347,352,356`). D52 leaves all three unchanged.
- **Borrowed contexts:** a built-in `Apply` now scores `RUNTIME_VALUE`, and that plus a function reference is unsafe (`Rule.java:359-361`). So in a borrowed context, a prepared call has only engine literals as operands, or variables holding them. Those evaluate to themselves (`ValueExp.java:41-44`).
- **D45 in borrowed contexts:** a prepared call over a `Shared` operand cannot run in a borrowed context, because `Shared` stands for a built-in call.
- **Match targets:** the new path at `TargetBeforeContext.java:85-88` does not go through `Apply`, so the plan is not involved.

`Rule.java:361` also closes the `attribute-designator` P2 from rv206 in source. This is not a review of Terra's correction, which still needs its own.

## Question 4: reflection in the tests

**Yes, an ordinary refactor breaks them with no behaviour change.** Any of these would do it:
- renaming `callSite`, or splitting it back into two fields
- renaming `HigherOrderCall` or `PreparedConditions.Shared`
- renaming `predicates` or `preparedConditions`
- dropping the `Optional` wrapper

The affected lines are `HigherOrderMetadataCompatibilityTest.java:151-153,182,212,244,274-278` and `SharedConditionValuesPdpTest.java:192-207`. They fail loudly, so the cost is maintenance. The `assertNull` rows would also fail for a legitimate new call-site kind on those shapes.

**The bigger weakness is the opposite one:** `callSite != null` proves a plan exists, not that it runs. If `Apply.java:292` stopped calling it, or the check at `DefaultFunctionInvocation.java:207` always missed, all these tests would stay green and D52's gain would vanish. No decision would change, so this is not a finding, but only the JMH pair would catch it.

**A concrete better check:**
1. Wrap the predicate in `Mockito.spy` and call `clearInvocations` after building the `Apply`.
2. In the existing `canonicalCases`, assert `never()` on `getParamSpecAt` with validation off.
3. Assert `atLeastOnce()` with validation on, as the control that stops the first assertion going vacuous.

This uses no private names and proves the walk was skipped, which is D52's stated purpose (`design.md:2063-2064`). `cod_2` already has an inline-spy precedent. It is still a white-box check, so it is optional. I did not try it.

## Date fix: APPROVE (task 5.8)

**The fix is correct.** `Calendar.MONTH` is 0 to 11, so `+ 1` gives the 1 to 12 that `LocalDate.of` needs (`DateTimeDurationUtils.java:151`).

**Nothing depended on the old month:**
- The only dependent was the old test's `assertEquals(d1.getMonthValue(), now.get(Calendar.MONTH))`, which pinned the defect. The candidate removes it.
- `Calendar.MONTH` has one hit across all modules' Java sources.
- `toISO860Date` has two callers, `ISO8601Date.java:105` (XML) and `:118` (calendar). The calendar one is reached only through `ofAny`.
- The old code threw every January, and on days such as October 31, so no working caller could have relied on it.
- The PDP's `current-date` goes through `ZonedDateTime` (`ResolverRegistry.java:125-130`, `ISO8601Date.java:108-110`), so it never hit this defect. Only an extension passing a `GregorianCalendar` could reach it.

**No other path has the same defect:**

| Path | Conversion | Month arithmetic |
| --- | --- | --- |
| `ISO8601Date.of(Date)` `:98-100` | `toInstant().atOffset(UTC)`, then `toLocalDate()` | None |
| `ISO8601DateTime.ofAny(Date)` `:100-102` | `toInstant()`, then `DateTimeDurationUtils.java:155-161` | None |
| Date-time from calendar, `DateTimeDurationUtils.java:181-188` | `toZonedDateTime()` | None |
| Time from calendar, `:171-179` | `toZonedDateTime()`, no month field | None |
| XML calendars, `:106,120-122` | `getMonth()`, 1-based; every `dateTime` literal parses through `:106` | None |
| `ISO8601Duration.java:87,91` | JDK `GregorianCalendar.from` | None |

**Tests:** four fixed dates replace the clock-based test. They cover month 0 being rejected, an invalid prior-month day, and a silently wrong month (February 29 became January 29). The old test depended on the clock, which is why this surfaced on October 31.

## Verification

- `SUPPORTED`: the traces in questions 1 to 3 and the date path table, from `shasum`, `rtk git diff` and `status`, `rtk rg` and opened source.
- `ast-grep scan` on the six production files: exit 0. `rtk git diff --check`: exit 0 in `cod_3` and `cod_4`.
- `PARTIAL`: caller inventories. `rtk rg` and `ast-grep` agree on the two `toISO860Date` sites. `ripwire --callers` returned 0 for symbols with real call sites, so I did not count it. Serena is `UNAVAILABLE` for these worktrees.

## Not verified

- No build, test or JVM run. All test counts are author-supplied.
- Neither P3 mutant was executed; both are traced.
- `cod_1`, `cod_3` and `cod_4` have not been built together. Reactor, conformance and JMH are UNRUN.
- The date author's 7,368-test core run also contains `ValueType.java` and `ValueTypeApplyTest.java`, so it is not a date-only result. I did not review that change; its diff shows it touches array input only.
- `ISO8601Date.of(Date)` has no caller or test in the repository (`rtk rg`).
- JDK behaviour of `getMonth`, `toZonedDateTime` and `java.sql.Date.toInstant` comes from memory, not fetched docs.

## Residual risk

1. **The core gate is still red.** It stays red until the date fix lands. From the date files alone, I expect core to go from 7,443 with one error to 7,446 with none. That is arithmetic, not a run.
2. **D52's gain is unmeasured.** The JMH pair decides it (`design.md:2022-2024`).
3. **New public bridges skip the signature check.** `FunctionSpecBuilder.java:338,349` and `DefaultFunctionInvocation.java:203,217` take caller-supplied expected types. `design.md:2067` says direct public calls stay checked, while `:2068` allows narrow internal bridges. Only `HigherOrderCall` calls them, and no policy can reach them.
4. **Two pre-existing date notes, not findings:**
   - `toISO860Date(GregorianCalendar)` is the one calendar conversion that still reads fields, so a Julian-only date such as 1500-02-29 would throw `DateTimeException`.
   - `java.sql.Date.toInstant()` throws `UnsupportedOperationException`, so `ISO8601Date.ofAny` fails that way rather than with a syntax error.

   Both are UNVERIFIED and unrelated to this fix.

LANE_COMPLETE cc_1 D52-review