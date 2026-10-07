• ## Findings

  No HIGH or MEDIUM defects found. I checked the six functions against the ruling, the fallback logic against the edge classes named in question 2, and the exception flow against the stated `DefaultFunctionInvocation` wrapping behavior. What remains is LOW.

  **L1 (LOW) — `ArithmeticException` cause is dropped when building the `EvaluationException`.**
  `ArithmeticFunctions.java`, `outsideIntegerRange` (~line 125 in the diff): `new EvaluationException(Status..., message)` does not chain the caught `ArithmeticException` (`e` is unused in all four catch sites: ~lines 68, 139, 177, 242, 260). Failing scenario: `integer-add(9223372036854775807, 1)` — the log shows only "integer-add result is outside the xs:integer range"; the underlying "long overflow" / `longValueExact` detail is lost, which slows diagnosis when this fires inside a large policy. Fix: `throw outsideIntegerRange("integer-add").initCause(e)` or add a cause-aware constructor call. Note the spec forbids operands in the *status message*; the exception cause is not the status message, so chaining does not violate the ruling.

  **L2 (LOW) — `IntegerVal.add`/`multiply`/`subtract`/`abs` are public API; the "no other callers" fact is a text search, not type-checked.**
  `IntegerVal.java` ~lines 97–119, 128–135. In-repo the change is safe: the only callers (`ArithmeticFunctions` subtract/abs) catch the new `ArithmeticException`, and anything escaping inside a function invocation still becomes Indeterminate processing-error via `DefaultFunctionInvocation` (it just loses the "outside the xs:integer range" message). The residual risk is downstream/library callers of these four public methods outside any function invocation, who now get a raw unchecked `ArithmeticException` where they previously got a silently wrapped value. Failing scenario: a custom downstream function calling `IntegerVal.of(MAX).add(one)` outside `DefaultFunctionInvocation` propagates an unchecked exception with no XACML status. Fix: none required in this repo; note the behavior change in release notes, or route the remaining call sites through a checked alternative when the later type-replacement task lands.

  **L3 (LOW) — missing boundary test: the largest in-range square.**
  `IntegerOverflowTest.java`, `resultsInsideTheRange` (~lines 62–93) and `resultsOutsideTheRange` (~lines 131–156): `outside("integer-multiply", 3037000500L, 3037000500L)` pins the first out-of-range square, but the last in-range one, `integer-multiply(3037000499, 3037000499) = 9223372030926249001`, is not pinned. An off-by-one in the overflow check would pass the current table. Fix: add `inside("integer-multiply", 9223372030926249001L, 3037000499L, 3037000499L)`.

  **L4 (LOW) — no regression pin for the zero divisor next to the new try/catch.**
  `ArithmeticFunctions.java` `integer-divide` (~lines 233–244): `Preconditions.checkArgument(b.get() != 0)` sits *outside* the new `try`, which is correct — but nothing in this diff pins that `integer-divide(x, 0)` and `integer-mod(x, 0)` still evaluate to Indeterminate processing-error after the refactor (the catch is `ArithmeticException`, and a future edit reordering the precondition into the try would silently change the status path only for divide, not mod, since `checkArgument` throws `IllegalArgumentException`). If `FunctionSafetyNet43Test` already covers both zero-divisor rows, disregard; otherwise add them.

  ## Answers

  **1. Any call still wrapping, or falsely Indeterminate?** No. `add`/`subtract`/`multiply`/`divide`/`abs` use exact operators; `mod` cannot overflow (`|a % b| < |b|`, and `MIN % -1 == 0` per JLS, pinned by a test row). No false Indeterminate: the fallback recomputes the full sum/product exactly and only the final `longValueExact()` decides, so `integer-add(MAX, 1, -1) = MAX` and `integer-add(MAX, MAX, MIN) = MAX - 1` return values, as does the zero-rescue `integer-multiply(MIN, -1, 0) = 0`. For multiply, zero is the *only* rescue case (nonzero integer factors never reduce magnitude), and it is tested.

  **2. Is the BigInteger fallback right?** Yes on all four named axes: zero factors (recomputed from scratch, so a thrown fast path doesn't corrupt state), signs (BigInteger is exact), operand order (fallback restarts at index 0; addition and multiplication are commutative anyway), and the `longValueExact` `ArithmeticException` propagates out of the helper — an exception thrown inside a `catch` block is not re-caught by the same try — into the function-level catch that builds the processing-error `EvaluationException`. Cost: operands are 64-bit each, so N operands produce at most a 64N-bit result; time is quadratic in N for the pathological all-overflow case but N is bounded by the policy document itself, and only overflowing evaluations pay for the fallback. No value-driven amplification is possible.

  **3. Does a new `ArithmeticException` escape as non-Indeterminate?** Not in-repo, given the stated caller search. Both real call sites (`integer-subtract`, `integer-abs`) catch it; `IntegerVal.add`/`multiply` have no in-repo callers; and even a hypothetical missed site *inside* a function invocation is mapped to processing-error by `DefaultFunctionInvocation` (without the range message). The only exposure is external callers of the public value type — L2.

  **4. Do the tests pin the boundaries?** Mostly yes: every spec scenario has a matching row, both rescue directions for add, both zero-rescue signs for multiply, `MIN/-1` for divide, `MIN % -1 == 0` for mod, the `MIN+1` abs edge, and exact-message assertions on all 17 outside rows. Gaps: L3 (largest in-range square) and L4 (zero-divisor pins).

  **5. Does the spec say what the code does?** Yes. 64-bit domain, Indeterminate with `urn:oasis:names:tc:xacml:1.0:status:processing-error`, no wrapped value, only-the-result-counts for variadic functions, message names the function and omits operands — each clause is implemented and test-pinned, including the edge scenario `integer-multiply(4611686018427387904, -2) = MIN`.

  The red-run numbers also reconcile: 25 inside rows pass on wrapping code (wrapping is correct when the result fits), 17 outside rows + the value-type test fail, 43 total.

  VERDICT: APPROVE

kimi exit=0
