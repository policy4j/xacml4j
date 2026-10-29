# Review R-D46-argon: Codex Terra's D46 (`RangeTargetIndex` — Ordered-Range Candidates Under the Target Index)

- **Reviewer**: Lane `agyx_1` (Gemini Argon)
- **Author**: Codex Terra (`I-D46`)
- **Verdict**: **APPROVE** (`0 P1`, `0 P2`, `0 P3`)

## Findings

None (`0 P1`, `0 P2`, `0 P3`).

---

## Verification of Review Points

### 1. Soundness Under XACML §§7.6, 7.7, 7.3.5, and 7.19 (`SUPPORTED`)

- **Separate existential witnesses on `min(B)` and `max(B)` (§7.6)**:
  - In XACML §7.6 `<Match>`, the literal is the first argument (`f(literal, v)` for `v ∈ B`). Over a nonempty bag `B` of a total-ordered type:
    - `<type>-less-than-or-equal(L, designator)` (`L <= v`) holds for at least one `v ∈ B` iff `L <= max(B)`, and is `NoMatch` iff `L > max(B)`.
    - `<type>-greater-than(U, designator)` (`U > v`) holds for at least one `v ∈ B` iff `U > min(B)`, and is `NoMatch` iff `U <= min(B)`.
  - [`RangeTargetIndex.candidates`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/RangeTargetIndex.java`) computes `min` and `max` across `bag` in one pass, then clears `lower[i].alternative()` for `i >= upperBound(lower, max)` (where `L > max(B)`) and clears `upper[i].alternative()` for `0 <= i < upperBound(upper, min)` (where `U <= min(B)`).
  - Because each `Match` in a [`MatchAllOf`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/MatchAllOf.java`) posts its own independent [`Endpoint`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/RangeTargetIndex.java`) against `max(B)` or `min(B)`, different bounds in one `MatchAllOf` can be satisfied by different witnesses in `B` (e.g., `[10, 20)` against `{5, 25}` has `min = 5 < 20` and `max = 25 >= 10`, retaining the alternative), and reversed bounds are never discarded at preparation.
- **First `AnyOf` only and total `AllOf` bounds on one key (§7.7)**:
  - [`RangeTargetIndex.Builder.inspect`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/RangeTargetIndex.java`) inspects only `first = target.getAnyOf().iterator().next()` (`:191`) and requires exact classes (`Target.class`, `MatchAnyOf.class`, `MatchAllOf.class`, `Match.class`, `AttributeDesignator.class`) and that every `Match` of every `MatchAllOf` in `first` is a certified total bound comparison on the single `targetKey`.
  - Because every `Match` in `first` is total and side-effect-free on a validated request bag `B`, no `Match` in `first` can evaluate to `Indeterminate` or record an evaluation status (`MatchAllOf.java:82-92`). When every `MatchAllOf` alternative of `first` has at least one false bound (`!alternatives.get(alt)`), `first.match(context)` is guaranteed to return `MatchResult.NOMATCH` (`MatchAnyOf.java:58-70`), and [`Target.match`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/Target.java`) short-circuits at `i = 0` without evaluating any later `MatchAnyOf`.
  - Conversely, surviving candidates are not treated as proven `Match`; they proceed through [`TargetBeforeContext`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/TargetBeforeContext.java`) and ordinary rule evaluation.
- **Unsupported siblings kept in place**:
  - [`RangeTargetIndex.Builder.add`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/RangeTargetIndex.java`) sets `any.set(position)` whenever `child.getClass() != Rule.class`, `inspect` returns `Optional.empty()`, or the rule's key differs from the plan's `key`. At query time, `selected.or(any)` (`:116`) retains all such rules in their original positions.
- **`MustBePresent=true` excluded and no error becomes `NoMatch` (§7.19)**:
  - [`RangeTargetIndex.Builder.inspect`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/RangeTargetIndex.java`) rejects `designator.isMustBePresent()`.
  - Any missing entity, absent or empty request bag, unexpected bag or value class, or runtime exception/error returns `children` (`:96-110`, `:121-123`), preserving all status codes and error paths.

### 2. Direction and Type Proof at Preparation (`SUPPORTED`)

- **Operator provenance and direction certification**:
  - [`RangeTargetIndex.Builder.inspect`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/RangeTargetIndex.java`) requires `predicate.getClass() == BUILT_IN_SPEC` (`FunctionSpecBuilder.FunctionSpecImpl.class`), `predicate.hasDirectScan(literal)`, the `<type>-less-than-or-equal` or `<type>-greater-than` identifier, and [`Builder.certified`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/RangeTargetIndex.java`).
  - `certified` probes `predicate.scan(context, literal, before/equal/after)` on `(-1, 0, 1)` for `INTEGER` and `(2024-06-14Z, 2024-06-15Z, 2024-06-16Z)` for `DATE`:
    - `lower == true` requires `(NO_MATCH, MATCH, MATCH)`, which uniquely selects `BagStorage.Comparison.LESS_OR_EQUAL` (`literal <= v`).
    - `lower == false` requires `(MATCH, NO_MATCH, NO_MATCH)`, which uniquely selects `BagStorage.Comparison.GREATER_THAN` (`literal > v`).
- **Exact engine comparisons**:
  - [`RangeTargetIndex.isValue`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/RangeTargetIndex.java`) requires `key.getDataType() == XacmlTypes.INTEGER && value.getClass() == IntegerVal.class` or `key.getDataType() == XacmlTypes.DATE && value.getClass() == ISO8601Date.class` (and `:205` checks `literal.getEvaluatesTo() == ref.getDataType()`).
  - [`RangeTargetIndex.compare`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/RangeTargetIndex.java`) uses `Long.compare(((IntegerVal)left).get(), ((IntegerVal)right).get())` (with no off-by-one boundary arithmetic) and `((ISO8601Date)left).compareTo((ISO8601Date)right)` (which compares `epochSecond()` using [`ISO8601Date`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/types/ISO8601Date.java`)'s fixed `ZoneOffset.UTC` default when a timezone is absent, matching [`NonNumericComparisonFunctions`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/NonNumericComparisonFunctions.java`) and [`BagStorages.compareTemporal`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/BagStorages.java`)).

### 3. Query Guards (`SUPPORTED`)

- [`Policy.Prepared.children`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/Policy.java`) guards on exact `BUILT_IN_ALGORITHMS`, `top instanceof RootEvaluationContext decision`, `decision.generation().isPresent()`, `generation.indexes.isPresent()`, `policy.rules.size() >= 2`, `decision.hasRequestAttributeResolution()`, `!decision.hasMisses()`, and `!context.isValidateFuncParamsAtRuntime()`.
- [`RangeTargetIndex.candidates`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/RangeTargetIndex.java`) guards on:
  - `top instanceof RootEvaluationContext root`
  - `root.isDecidingThread()`
  - `root.hasRequestAttributeResolution()`
  - `!root.getDecisionObservation().isEnabled()`
  - `!context.isValidateFuncParamsAtRuntime()`
  - `context.getCurrentPolicy() != null && context.getCurrentRule() == null`
- It performs one `context.resolveEntity(key.getCategory())` call (`:96`) and one `entity.orElseThrow().resolve(key)` call (`:98`), never calling `EvaluationContext.resolve`, the PIP, or a selector.
- Any missing entity, missing or empty bag, mismatched bag value type, non-exact value class, or thrown `RuntimeException | Error` returns `children`.

### 4. Publication and Lifetime (`SUPPORTED`)

- [`TargetIndex`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/TargetIndex.java`) holds `private final Optional<RangeTargetIndex<T>> ranges;`, created through [`TargetIndex.createRules`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/TargetIndex.java`) and published once per `Policy` per `Generation` via `indexes.putIfAbsent(policy, built)` in [`Policy.Prepared.children`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/Policy.java`).
- [`RangeTargetIndex`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/RangeTargetIndex.java`) is completely immutable after construction (`any` is cloned and only read via `selected.or(any)`; `childForAlternative`, `lower`, and `upper` are private arrays never mutated after the constructor sort).
- `alternatives` (`:111`) and `selected` (`:115`) are decision-local `BitSet` instances allocated per query.

### 5. Order, Duplicates, Equality Precedence, and Pre-Sizing (`SUPPORTED`)

- **Equality plan precedence**: [`TargetIndex.createRules`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/TargetIndex.java`) runs `create(context, rules)` first and returns immediately when `index.key.isPresent()`.
- **Order and duplicates**: Each rule position `0 .. children.size() - 1` is tracked by index in `any` or `childForAlternative`, and [`TargetIndex.candidatesFor`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/TargetIndex.java`) materializes surviving positions in ascending index order (`selected.nextSetBit`), preserving declaration order and duplicate rules; when `size == children.size()`, it returns `children` without copying.
- **P-FIXED2 item 9 pre-sizing**: [`TargetIndex.candidates`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/TargetIndex.java`) and [`TargetIndex.candidatesFor`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/TargetIndex.java`) pre-size `new BitSet(children.size())` before `selected.or(any)` and use `ImmutableList.builderWithExpectedSize(size)`, avoiding `BitSet` `long[]` growth and `ImmutableList.Builder` array resizing on `multi-valued` (`1,031.1 -> 871.5 B/op` in Terra's smoke).

### 6. Allocation with the Index Off (`SUPPORTED`)

- **No code on the `targetIndex=off` path changed**:
  - When `targetIndex` is `false`, [`Generation`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/PreparedPolicySets.java`) sets `indexes = Optional.empty()`.
  - [`Policy.Prepared.children`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/Policy.java`) returns `policy.rules` at `if (generation.indexes.isEmpty() ...)` before line 218. Neither `TargetIndex` nor `RangeTargetIndex` is called or instantiated when the index is off.
- **Cause of index-off B/op differences in Terra's smoke**:
  - It is fork-to-fork C2 scalar-replacement / iteration variance in the 1-fork, 1-warmup, 1-second smoke (as previously documented in [`pr21-astra-range2-profile.md`](`.ntm/worktrees/xacml4j--batch2/agyx_1/openspec/changes/optimize-pdp-evaluation/research/pr21-astra-range2-profile.md`) and [`pr22-opus-fixed2-profile.md`](`.ntm/worktrees/xacml4j--batch2/agyx_1/openspec/changes/optimize-pdp-evaluation/research/pr22-opus-fixed2-profile.md`), where `Result.Builder` scalar replacement differs by ~48 B/op across forks—matching the `296.3 -> 344.3 B/op` delta on `rules:10 date-ranges` `off`, while `rules:1000 integer-ranges` `off` moved in the opposite direction `1729.4 -> 1647.1 B/op`).

### 7. Tests (`SUPPORTED`)

- [`TargetIndexPdpTest`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-pdp/src/test/java/org/xacml4j/v30/pdp/TargetIndexPdpTest.java`) compares full responses via [`assertRangeModes`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-pdp/src/test/java/org/xacml4j/v30/pdp/TargetIndexPdpTest.java`) and [`assertResponse`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-pdp/src/test/java/org/xacml4j/v30/pdp/TargetIndexPdpTest.java`) (decision, status, error chain, ordered obligations and advice with attributes, and policy identifiers) with the index off and on (plus `observed-index` and `validateFuncParamsAtRuntime=true`) across:
  - lower/equal/upper integer boundaries, separate witnesses (`{5, 25}`), duplicate bag values, and `Long.MIN_VALUE`/`Long.MAX_VALUE` (`:299-317`);
  - multiple `MatchAllOf` alternatives, overlapping ranges, duplicate rule positions, and reversed bounds (`[20, 10)`) (`:320-346`);
  - explicit and absent date timezones (`2024-06-15` UTC default vs `+02:00` and `Z`) (`:349-361`);
  - absent optional, absent `MustBePresent=true` (`MISSING_ATTRIBUTE`), and PIP-supplied attributes (`:364-380`);
  - repository policy replacement (`:383-406`);
  - earlier `Indeterminate` before a non-matching range rule, a missing required attribute before a failing bound in the same `MatchAllOf`, and a custom `Value.OtherValue<Long>` subclass in the request bag (`:409-429`).

## Verification Run

- `JAVA_HOME="$(jenv prefix 25)" rtk mvn -o -q -Dlicense.skipUpdateLicense=true -pl xacml-pdp -am -Dtest=TargetIndexPdpTest -Dsurefire.failIfNoSpecifiedTests=false test`: **PASS** (exit 0).
- `ast-grep scan xacml-core/src/main/java/org/xacml4j/v30/policy/RangeTargetIndex.java xacml-core/src/main/java/org/xacml4j/v30/policy/TargetIndex.java xacml-core/src/main/java/org/xacml4j/v30/policy/Policy.java`: **PASS** (exit 0, 0 diagnostics).
