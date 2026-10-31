=== BEGIN REPORT R-UX-opus ===
FINAL REPORT (policy4j_implementer, Claude Opus 5.5)

Lane cc_1, R-UX-opus: review of cycles U (D30) and X (D31) by Codex Astra, as merged in the main checkout. The
review was read-only. As the packet says, the patch's `JsonContent*` and `DescendantEvaluationContext` hunks were
ignored.

## Verdict

**APPROVE WITH CHANGES.**
- There is no P1. Every value-level behavior the packet lists is exact: hashes, grouping, order, multiset hash and
  equality, serialized form, `DoubleVal`, Annex C order and early exits.
- There is one P2: U2's index loop is never taken for policy sets in a PDP from the builder, so the saving that D30
  claims for policy combining does not happen.
- There are three P3s, on simplicity and consistency.

## Findings

### [P2-1] U2 misses the prepared children of every policy set in a default PDP
- **Where:**
  - the guard at `DenyOverrides.java:77` (and the same guard in the other nine algorithms);
  - `PreparedPolicySets.java:65-69` (`record PreparedList`, `children = List.copyOf(children)`);
  - `PolicySet.java:193` (`combine(context, Prepared.children(context, this))`);
  - `PolicyDecisionPointBuilder.java:161` (`PreparedPolicySets.create(false)` is always passed).
- **Issue:**
  - The guard is `instanceof RandomAccess && instanceof ImmutableList` (Guava).
  - D24/D25 preparation now runs for every PDP from the builder. It hands the eligible built-in algorithms the
    prepared list, which is a JDK `List.copyOf` list, not a Guava `ImmutableList`. The algorithms then take the
    iterator path.
  - Only `Policy` rule lists (`Policy.java:72`, Guava builder) and unprepared `PolicySet` children
    (`PolicySet.java:82`) get the index loop.
  - Decisions are unchanged, so no test sees it. But the policy-combining part of D30's 2.8% / 2.5% does not apply
    to the default configuration, and the batch measurement would credit U2 for less than it claims.
- **Fix:** `PreparedList`'s compact constructor copies with Guava, `children = ImmutableList.copyOf(children)`.
  - The list stays immutable and random-access.
  - Iteration order is the same.
  - No test changes: `CombiningListTypesCharacterizationTest` already compares Guava lists with the others.
  - The index candidate lists of 23.4 should also be Guava lists, or should pass the same guard.

### [P3-1] The guard has a redundant test and is copied ten times
- **Where:** the ten `Iterator<…> iterator = … instanceof RandomAccess && … instanceof ImmutableList<?> ? null :
  ….iterator();` lines: `DenyOverrides.java:77`, `PermitOverrides.java:57`, `FirstApplicable.java:56`,
  `DenyUnlessPermit.java:56`, `PermitUnlessDeny.java:54`, `OnlyOneApplicablePolicyCombiningAlgorithm.java:55`,
  `LegacyDenyOverridesPolicyCombineAlgorithm.java:61`, `LegacyPermitOverridesPolicyCombineAlgorithm.java:65`,
  `LegacyDenyOverridesRuleCombineAlgorithm.java:71`, `LegacyPermitOverridesRuleCombineAlgorithm.java:62`.
- **Issue:**
  - Guava's `ImmutableList` always implements `RandomAccess`, so the first test never decides anything.
  - The reason for the narrow guard (a caller's `CopyOnWriteArrayList` keeps its snapshot, a lazy list keeps its
    per-element laziness up to an early exit) is written nowhere in the code.
  - The merged loop (`iterator == null ? i < size : iterator.hasNext()`) is exact, but it is ten copies of a
    non-obvious idiom.
- **Fix:** drop `instanceof RandomAccess`. Put the test in one documented static helper of the combine package,
  next to `evaluateIfMatch` (`DecisionCombiningAlgorithms`), named for its reason: the engine's immutable lists are
  read by index, and a caller's list keeps its iterator. The ten loops call it.

### [P3-2] U3 uses a different guard from U2 for the same question, and D30's text does not match the code
- **Where:** `BagOfValues.java:406` (`values instanceof List && list instanceof RandomAccess`); `design.md:938-945`
  (D30 says U2 is "guarded by `RandomAccess`").
- **Issue:**
  - U3 indexes any random-access list, including a caller's `CopyOnWriteArrayList`.
  - Without concurrent change, the result is the same as before.
  - With a concurrent shrink, `get(i)` can throw `IndexOutOfBoundsException`, where the iterator worked on its
    snapshot. (Two passes already read two snapshots before, so type checking was never atomic.)
  - U2 avoids exactly this, so the two cycles disagree about one rule. D30 states a third rule.
- **Fix:**
  - Use the P3-1 helper in U3 too: the engine's lists come from unmarshallers and the builders, which are Guava or
    `ArrayList`, so decide whether `ArrayList` is in the rule.
  - Or keep U3 as is and say why bags accept it.
  - Either way, update D30 to the rule the code follows.

### [P3-3] X1 adds one array copy for bags of more than 8 values (note, no change needed)
- **Where:** `BagStorages.java:50` (the builder's array) and `:90` (the `LinkedHashMap` above 8).
- **Issue:** a large bag now has the builder's array and the map, where it had the map only before. Bags of more
  than 8 values are rare in requests, and the smaller allocation of the common small bag outweighs this. D31's
  "above that through the `LinkedHashMap` as today" is correct about grouping, but not about memory.
- **Fix:** none needed. The batch JMH pair shows the net. The D31 text could say "plus the builder's array".

## The checks of the packet

| Check | Result | Evidence |
| --- | --- | --- |
| U1 hash folds equal the old values, nulls included | **Exact** | See below |
| `ValueHashCompatibilityTest` | **Adequate** | See below |
| U2 Annex C order and early exits | **Exact** | The merged loop visits `get(0..size-1)` for an `ImmutableList` and the iterator otherwise; every `return` is unchanged |
| U2 narrow guard, exact for `CopyOnWriteArrayList` and lazy lists | **Exact** | Any list other than a Guava `ImmutableList` keeps its iterator, so the COW snapshot and lazy views behave as before (pinned by `CombiningListTypesCharacterizationTest`) |
| U2 simple enough | **No** | P3-1. It also misses the prepared lists: P2-1 |
| U3 `BagOfValues.Builder` passes | **Exact** | See below |
| X1 grouping by `equals` | **Exact** | See below |
| X1 iteration order | **Exact** | `distinct` keeps first-occurrence order, like `LinkedHashMap`; `Values` fills `[offsets[i], offsets[i+1])` with `distinct[i]` |
| X1 multiset hash, equality, serialized form | **Exact** | See below |
| X1 `DoubleVal` grouping | **Exact** | `Double.equals`: NaN with NaN, 0.0 apart from -0.0; `Double.hashCode` agrees |
| X1 8 and 9 threshold tests | **Exact** | `LINEAR_LOOKUP_LIMIT = 8` (`BagStorages.java:39`); 8 takes the linear path and 9 the map |
| X2 `single()` on demand | **Exact** | See below |
| Tests pragmatic, code clean | **Yes**, except P3-1 | Hash values are pinned because `BagOfValues` serializes its hash; no mechanism or count tests |

U1 hash folds:
- `Arrays.hashCode` gives `31*(31+h(a))+h(b)` for two fields and `31*(31*(31+h(a))+h(b))+h(c)` for three, with 0
  for null. `java.util.Objects.hash` and Guava's `Objects.hashCode(Object...)` both reduce to it.
- Every fold uses the old field order: `Value.java:128-130`, `RFC822Name.java:266`, `ISO8601Duration.java:126`,
  `DNSName.java:163`, `IPAddress.java:205-209`, `PortRange.java:246`, `Path.java:165-168`.
- The unqualified single-argument `Objects.hashCode(x)` is `java.util.Objects`. The old code called
  `Objects.hash`, which exists only in `java.util.Objects`.

`ValueHashCompatibilityTest`:
- It pins every folded type against `java.util.Objects.hash` of the same fields.
- Edge cases covered: NaN, ±0.0, null port bounds, null mask and range, and a null path category.

U3 passes:
- `BagOfValues.java:406-410`: the type of every value is checked before any is added, as before.
- A lazy random-access view is read twice, as the two iterator passes did. See P3-2 for concurrent change.

X1 grouping by `equals`:
- The linear scan (`BagStorages.java:79-88`) calls `new.equals(existing)`, which is the direction of
  `HashMap.getNode`.
- It groups exactly as the map did when `equals` agrees with `hashCode` and is reflexive. I read the value types
  that define their own: `Value.OtherValue` (`:128-143`), `AnyURI`, `Binary`, `ISO8601Date`, `ISO8601Time`,
  `ISO8601DateTime`, `Entity`, `DNSName`, `IPAddress`, `PortRange`, `Path`, `RFC822Name`, and `ISO8601Duration`.
  All agree.
- Their `equals` methods start with `o == this` or are reflexive, so the map's identity shortcut changes nothing.

X1 multiset hash, equality, and serialized form:
- `Grouped` sums `distinct[i].hashCode() ^ counts[i]`, as the map's entries did.
- `serialPersistentFields` (`BagOfValues.java:66-70`) is `hashCode`, `type`, `values`. The storage is transient,
  so the form is unchanged; it is pinned by the previous-class stream test, now also at 8 and 9 values.

X2 `single()` on demand:
- `BagOfValues.java:163-164`.
- The removed field was transient and is not in `serialPersistentFields`.
- The value is the same (`valueAt(0)`).
- The callers are rare: `BagOfValues.java:343` and `DefaultEvaluationContextHandler.java:271`. So an `Optional`
  per call costs less than one per bag.

## Verification

- Read:
  - Astra's report;
  - the patch (`patch-UX.diff`, 922 lines; hunks at `:1-328`, `:418-852`, `:899-922`);
  - D30 and D31 (`design.md:938-955`).
- In the main checkout:
  - `BagStorages` (`:39`, `:50`, `:75-100`), `BagOfValues` (`:66-70`, `:163-164`, `:395-425`);
  - the ten algorithms, `PreparedPolicySets.java:60-69`, `PolicySet.java:185-227`, and
    `PolicyDecisionPointBuilder.java:161`;
  - `Policy.java:56`, `:72`, and `PolicySet.java:67`, `:82`, `:354`;
  - the `equals` and `hashCode` of the value types listed above;
  - the callers of `single()`.
- No build or test was run: the review is read-only, and the conductor reports core 6,683 and conformance 938/12
  green on this code.
- Serena was not used. Facts come from the source.

## Residual risk

- P2-1 is a performance gap, not a correctness one. Its size is UNVERIFIED until the batch JMH pair.
- Hash consistency was checked for the value types of `xacml-core`. A custom `Value` from an extension, with
  `equals` that disagrees with `hashCode`, would now group differently in bags of up to 8 values (UNVERIFIED that
  one exists).
=== END REPORT R-UX-opus ===
