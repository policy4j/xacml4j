# rvn1-cycleN-opus (Claude Opus 5.5, NTM worktree lane, read-only)

FINAL REPORT (policy4j_implementer as reviewer, Claude Opus 5.5) — review of cycle N (D20), patch `merge-N.diff` by Codex Astra

**Verdict: APPROVE WITH CHANGES.** No P1. The code is correct; the test is far heavier than it needs to be, and the `BagFunctions` hunk is a no-op.

**Checked and correct**
- Status, message, and error chain are unchanged:
  - `withoutStackTrace(spec, c)` computes `statusFor(c, null)` and calls `CoreException(status, c, false)`.
  - The old `new FunctionInvocationException(spec, c)` goes through `this(spec, c, null)` to `statusFor(c, null)` (`FunctionInvocationException.java:43-47`, `:57`), so both build the same status.
  - Both throw sites still pass `invocationCause(e)` (`DefaultFunctionInvocation.java:225`, `:304`).
  - `CoreException(Status, Throwable)` → `this(…, true)` → `super(msg, t, true, true)` is equivalent to the old `super(msg, t)`.
  - The new private boolean constructor creates no overload trap: `this(spec, cause, null)` still binds to the `String`-varargs constructor, because `null` can't become a `boolean`.
- Decisions are unchanged: the thrown types are the same `EvaluationException` subclasses, so `Condition` maps them as before.
- The static read: `Boolean.getBoolean` runs once at class load, which is correct and lets the JIT fold the flag. The catch is that the property must be set at JVM start; see P3-3.
- The `fillInStackTrace` override combined with `writableStackTrace`, probed on JDK 25.0.4 with the same pattern:
  - Flag off: 0 frames for both the normal and the non-writable exception.
  - Flag on: the normal exception gets 6 frames; the non-writable one stays at 0, even after an explicit `fillInStackTrace()`.
  - Probe: `scratchpad/probe/StackProbe.java`
- Subclasses outside the package: `FunctionInvocationException`, `PolicySyntaxException`, `ResponseSyntaxException`, and the test class `SubclassedFailure`.
  - None of them overrides `fillInStackTrace`.
  - No code in any module calls `fillInStackTrace`, `setStackTrace`, `getStackTrace`, or `printStackTrace`.
  - Their default serialVersionUIDs don't change, because those are computed from a class's own members only.
  - Only `EvaluationException` (new protected constructor) and `FunctionInvocationException` (new public static method) gain members, and both are now pinned. I could not check the pinned values: no compiled HEAD classes exist offline. UNVERIFIED.
- Build:
  - `git apply --check` exits 0 on my tree.
  - The patch applied to a scratch copy, and the five changed main classes type-check with `javac` (0 errors in them).
  - The POMs have no `-Xlint` or `-Werror`, so dropping `@SuppressWarnings("serial")` is harmless.

**P2-1. The serialization test pins a defect and restates constants**
- `merge-N.diff:146-162`, `coreExceptionKeepsItsSerialDescriptor`:
  - It asserts that serialization fails with `NotSerializableException: org.xacml4j.v30.Status`. `Status` is a plain `final class` (`Status.java:42`), so every `CoreException` is unserializable today. The test would break the day someone fixes that.
  - The serialVersionUID assertions restate the constants the classes declare.
- Fix: delete the method and its imports (`ByteArrayOutputStream`, `NotSerializableException`, `ObjectOutputStream`, `ObjectStreamClass`, `CoreException`). Capture "CoreException is not serializable (Status)" as a separate bead; I can't write `.beads/`.

**P2-2. The fresh-JVM test launches three JVMs for a one-line switch**
- `merge-N.diff:164-220`, `stackTraceSettingIsReadOnceInAFreshJvm` plus `main()`.
- Each case starts a JVM on the Surefire classpath with a 30 s timeout. It tests "read once", which is an implementation detail; "unset" and "false" are the same case.
- Fix: delete both, along with the `Proxy`, `ProcessBuilder`, `TempDir`, `Files`, `Path`, `TimeUnit`, and `ValueSource` imports.
- Only if the maintainer wants the default pinned, do it in-process:
  ```java
  @Test
  void engineExceptionsHaveNoStackTraceByDefault()
  {
  	// The test JVM starts without -Dxacml4j.exceptions.stackTrace (design D20)
  	assertEquals(0, new SyntaxException("syntax").getStackTrace().length);
  }
  ```
  I'd leave this out too: it's a performance property, like the allocation counts the maintainer ruled out.

**P2-3. The `BagFunctions` hunk is a no-op**
- `merge-N.diff:342-353`: `Preconditions.checkArgument(false, "Bag is empty")` already throws `new IllegalArgumentException("Bag is empty")`. The explicit throw has the same class, message, and full stack trace, and the same cost.
- D20(d)'s goal, no throwable per wrong bag size, isn't met. It can't be while D20 also keeps "the class, message, and cause chain of `Status.getError()`".
- Fix: drop the hunk. Record (d) as not done next to (a), and put that conflict to the maintainer.

**P3-1. (b) is mostly covered by (c)**
- With the property unset, `new FunctionInvocationException(spec, cause)` already has no stack trace (probe: 0 frames).
- These parts only matter when the property is set to true, which is exactly when the user asked for traces:
  - `withoutStackTrace`
  - `CoreException(Status, Throwable, boolean)`
  - `EvaluationException(Status, Throwable, boolean)`
  - the two serialVersionUID pins
- Simplest version: drop all of it. Both throw sites keep `new FunctionInvocationException(spec, invocationCause(e))`, `EvaluationException` and `FunctionInvocationException` stay untouched, and `CoreException` changes only by:
  ```java
  	// Read once, when the class loads: set it when the JVM starts (design D20)
  	private static final boolean STACK_TRACE = Boolean.getBoolean("xacml4j.exceptions.stackTrace");

  	/**
  	 * Engine exceptions fill no stack trace unless the JVM starts with
  	 * {@code -Dxacml4j.exceptions.stackTrace=true} (design D20).
  	 */
  	@Override
  	public Throwable fillInStackTrace(){
  		return STACK_TRACE ? super.fillInStackTrace() : this;
  	}
  ```
- If the maintainer keeps (b) unconditional, a static factory is the right shape: better than a public boolean constructor, and it mirrors `Throwable`'s own protected 4-argument constructor.

**P3-2. The parameterized test is too heavy**
- `merge-N.diff:108-144` builds a policy, a rule, a condition, a `RootEvaluationContext`, and two Mockito mocks.
- That scaffold isn't needed:
  - Indeterminate decisions for bag-size failures are already pinned: `FunctionSafetyNet43Test:660-671` at function level, `AttributeResolutionSafetyNetTest:416-443` at PDP level.
  - Pair versus list equivalence is pinned by `DirectPairCallTest:286-300`.
  - Asserting the thrown type is enough to keep the decision.
- The new value is only the status message and the full error chain. Keep that, call the function directly, and rename `reflective` to `validate` (the flag turns on runtime validation, which routes the pair call through the list path):
  ```java
  	@ParameterizedTest(name = "{0}, validate={1}")
  	@MethodSource("failures")
  	void failureKeepsItsStatusAndErrorChain(String kind, boolean validate, StatusCodeId code,
  			Optional<String> message, List<String> chain)
  	{
  		Status status = assertThrows(FunctionInvocationException.class,
  				() -> call(kind, context(validate))).getStatus();

  		assertEquals(code, status.getStatusCode().getValue());
  		assertEquals(message, status.getMessage());
  		assertEquals(chain, Stream.iterate(status.getError().orElseThrow(), Objects::nonNull, Throwable::getCause)
  				.map(e -> e.getClass().getSimpleName() + ": " + e.getMessage()).toList());
  	}

  	private static Object call(String kind, EvaluationContext context)
  	{
  		return switch(kind){
  			case "empty" -> function(ONE_AND_ONLY).invokeWithList(context, List.of(XacmlTypes.BOOLEAN.emptyBag()));
  			case "two" -> function(ONE_AND_ONLY).invokeWithList(context,
  					List.of(XacmlTypes.BOOLEAN.bagOf(BooleanVal.of(true), BooleanVal.of(false))));
  			default -> function(FAILURE).invoke(context, XacmlTypes.STRING.ofAny(kind), XacmlTypes.STRING.ofAny("unused"));
  		};
  	}
  ```
  - `context(validate)` is the patch's `Proxy` that answers only `isValidateFuncParamsAtRuntime`.
  - The `failures()` rows become, for example, `("evaluation", validate, SYNTAX_ERROR, Optional.of("evaluation status"), List.of("EvaluationException: evaluation status", "IllegalArgumentException: evaluation cause", "IOException: root"))`.
  - The `Failures` provider stays as it is.
  - The file goes from about 235 lines to about 90.

**P3-3. Document the switch, and optionally drop `synchronized`**
- `merge-N.diff:250`, `:271-274`: nothing documents the property, or that it is read once at class load. Use the javadoc in P3-1.
- `synchronized` on the override costs a monitor enter and exit per engine exception. Returning `this` touches no state, and `super.fillInStackTrace()` synchronizes on its own.

**P3-4. Record (a) as not done**
- Deferring (a) fits D20's "Kept: status messages … error chain".
- Astra's reason (the change surfaces through `FunctionSpecBuilder`'s wrapper) is UNVERIFIED by me.
- Record (a), and (d) per P2-3, in `tasks.md` and `design.md`, so the batch doesn't claim them.

**Merge note, not a finding**
- `merge-N.diff:368-404` also removes four D18 debug lines from `DefaultFunctionInvocation`. That's lane cod_3's cycle L work, and it shows up only because my tree lacks it.
- In the main tree, the only cycle N hunks in that file are the two `withoutStackTrace` lines (`:364`, `:410`).

**Not run**
- Maven (still blocked), `FunctionErrorCompatibilityTest`, the reactor, the conformance suites.
