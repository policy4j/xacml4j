**Sol: APPROVE. Terra: REJECT (one P2).** The candidates don't conflict, but 42.14 stays open until Terra is corrected. I did not write `report.md`: this persona's read-only rule is binding and overrides the packet, so the block below is the record for the conductor to copy.

FINAL REPORT (xacml4j Code reviewer, Opus 5.5)

**Scope.** Base `759d90a6`. All six SHA-256 values matched before and after the review, and worktree status did not change. `Rule.java` and `RulesInPolicyContextPdpTest.java` are cited from `cod_1`; `ResponseWriters.java`, `Policy.java`, `PolicySet.java` and `NestedResponseStagingPdpTest.java` from `cod_2`. Nothing was built or run, so every result below is traced in source.

## Verdicts

- **Sol (`cod_2`): APPROVE.** No findings. Questions 1 and 2 hold.
- **Terra (`cod_1`): REJECT.** The defect from r-D47-sol remains through a second source of runtime values.
- **Combined:** no behavioural conflict found.

## Findings

### P2, Terra: `attribute-designator` hides runtime values from the eligibility scan

`xacml-core/src/main/java/org/xacml4j/v30/policy/Rule.java:340-346`

`provenance` builds an Apply's flags from its arguments only. `attribute-designator` takes only literals, so it scores 0, yet it returns a bag from the request (`AttributeDesignatorFunctions.java:61-83`). It is a trusted built-in (`XacmlDefaultFunctions.java:66`), so the scan accepts it.

Failure, using Terra's own fixtures:
- **Policy:** first-applicable, one Permit rule with no target, condition `any-of(string-equal, "policy", attribute-designator(subject-category, "urn:example:runtime", string, true))`.
- **Request:** `urn:example:runtime` holds `ParentPolicyValue` (`RulesInPolicyContextPdpTest.java:372-375`).
- **Scan:** the flags are FUNCTION_REFERENCE, 0 and 0, so `Rule.java:344` does not fire. The rule is eligible and `enterRule` borrows the policy context (`Policy.java:397-406`).
- **Evaluation:** `any-of` reaches `DecidingOperands.java:204`, `FunctionReference.java:121` and `FunctionSpecBuilder.java:662`, which calls `value.evaluate` with the policy context.
- **Expected:** Permit with the `permit` obligation, as observed.
- **Actual:** NotApplicable with no obligation when unobserved. The value answers "none".

Only the bag's source differs from the path Terra's RED run saw fail with a plain designator (`201-resume-d47-terra/summary.md:23`). The function reads the request only (`DefaultEvaluationContextHandler.java:560-561`), so there is no PIP row. `attribute-selector` has the same shape.

**Fix:** seed the flags in the Apply branch, after the `usesBuiltInInvocation` check, which proves the id belongs to the engine's function:

```java
private static final Set<String> RUNTIME_SOURCES = Set.of(
        "urn:oasis:names:tc:xacml:3.0:function:attribute-designator",
        "urn:oasis:names:tc:xacml:3.0:function:attribute-selector");

int flags = RUNTIME_SOURCES.contains(apply.getFunctionId()) ? RUNTIME_VALUE : 0;
```

A search of `function/impl` finds no other built-in that reads attributes or evaluates bag values outside a function reference.

**Test:** add a `designator function` row to `runtimeValuesEvaluatedByPredicatesSeeTheRuleContext`, request source only, index off and on, using the form at `AttributeResolutionSafetyNetTest.java:283-287`.

### P3, Terra: the `computed argument` row cannot fail

`RulesInPolicyContextPdpTest.java:180-183,204-205`; `Rule.java:346`

`string-one-and-only` rejects the custom value before the predicate runs, so the result is Indeterminate whether or not the rule borrows. These four rows passed before the fix (`summary.md:23`). No test fails if `Rule.java:346` drops RUNTIME_VALUE.

**Impact:** with that change, `any-of(string-equal, "policy", string-union(designator, literalBag))` borrows and keeps the custom value (`BagOfValues.java:213-225`). It would then give NotApplicable unobserved.

**Fix:** build the row with `string-union` and expect Permit in both modes.

## Questions

**1. A revisit returning false is sound: holds.**
- Every `true` returns straight up the walk (`ResponseWriters.java:58,87-88,106,115-117`). A revisit therefore means the first visit returned false or is still running. This is a depth-first reachability search, so every reachable definition is visited until a writer is found.
- Shared memo across a target's matches: no reachable effect. `Match` holds only an `AttributeReference` and a `Value` (`Match.java:41-42`), so a target walk never reaches a definition.
- Definition reached through a Match, then an Apply: refuted, not constructible. Target and condition also use separate walks (`BaseDecisionRule.java:80`).
- Cycles: exact classes cannot form one, because both links are final constructor fields (`VariableDefinition.java:48`, `VariableReference.java:37`). XML parsing rejects a cycle (`VariableManager.java:60-61`). A forced cycle would still end with the right answer.
- Custom classes: a custom definition returns true before the memo (`ResponseWriters.java:99-101`). A custom reference falls through to `return true` (`:111`).
- Callers: one production call site, `BaseDecisionRule.java:80`, at construction. Every result feeds the same flags as at base.

**2. Prepared-child gates behave as before: holds.**
- The stored flag comes from the same final field and the same set (`Policy.java:123`, `PolicySet.java:92`).
- Policy subclass: not possible, the constructors are private (`Policy.java:115`, `PolicySet.java:83`) and nothing extends either class.
- Algorithm subclass: exact-class membership both before and after.
- Null algorithm: rejected before the flag is computed (`Policy.java:117`, `PolicySet.java:88`).
- Core and PDP tests have no test double of either class.

**3. Combination: no concrete break.**
- The changed files are disjoint, and neither candidate calls anything the other changed. `EngineOnly` does not use `ResponseWriters`.
- `evaluatesInPolicyContext`: Terra narrows it, and Sol does not touch it or the `builtInAlgorithm` read in `enterRule` (`Policy.java:398`).
- `canRespond`: Sol preserves each result. Every built-in is silent (`JavaMethodToFunctionSpecConverter.java:113-114`), so an eligible rule calls no writing function.
- Slot lifecycle: an eligible Terra rule always has an empty plan (`Rule.java:279`, `TargetBeforeContext.java:128-145`), so `known` is empty inside `TargetBeforeContext.java:86-92`. This is safe, but it removes D53's premise that a proven Match uses the slot (`design.md:2097`).
- D45: predicate selection uses `getCurrentPolicy()`, which gives the same answer in both contexts (`Condition.java:118`). `Shared` works from the root and shares single `StringVal`s only (`PreparedConditions.java:278,482`).
- Expected combined result if the sets simply add: focused classes 40 and 20, PDP 842, total 8,477, 6 skipped. This is arithmetic, not a run.

**4. Tests.**
- **Sol: no concrete gap.**
  - `customDefinitionAfterSharedVariableDag…` (`NestedResponseStagingPdpTest.java:480-512`) fails if a revisit ends the walk.
  - `customAlgorithmsKeepAllChildren…` (`:514-542`) fails if the gate goes, because the index returns one child (`TargetIndex.java:118`), the combiner denies, and the test expects Permit.
  - Only the probe (`:563-578`) fails on a revert, since the outcome tests also pass at base (`202-resume-variable-dag-sol/summary.md:37`). It is a call counter, which D47's test plan excludes (`design.md:1895-1896`).
- **Terra:** the `reused variable` row (`RulesInPolicyContextPdpTest.java:177-179`) pins retained provenance. The two gaps are the findings above.

## Verification

- `SUPPORTED`: the questions and findings above, by reading the candidate source and the author reports. I also read D47, r-D47-sol, and the Luna report and assessment.
- `PARTIAL`: the caller inventory. `rtk rg`, `ast-grep` and `ripwire --callers=canWrite` agree. Serena was not used because it cannot target these worktrees.
- `ast-grep scan` on the four production files: exit 0, no findings. `git diff --check` is clean in both worktrees.
- `ripwire --quality-delta`: exit 2 on both worktrees (output truncated mid-list). The rows I read are JUnit dead-code rows and name-based test duplication and churn, except complexity: `writes` 16 and `provenance` 24 against a bar of 15. Neither is a defect.

## Not verified

- No build, test or JVM run. All test counts are author-supplied.
- The P2 failure was not executed.
- The candidates have not been built together. Reactor, conformance and JMH are unrun.
- Custom values through `attribute-selector` were not traced.
- rsa2008 rule eligibility was read in one file only.

## Residual risk

1. **Terra's scope may fail D47's allocation gate** (`design.md:1899-1900`). Match targets and higher-order calls over designators now keep a rule context, and the extra 8 B per policy context stays. The figures below are arithmetic from `−32R + 8P` (`design.md:1877-1878`), assuming R = P. They are UNVERIFIED.

| Corpus | Rule shape | Design saving, B/op | Expected with Terra, B/op |
| --- | --- | --- | --- |
| `narrow` | Match target (`RulesCorpus.java:318`) | −24 | +8 |
| `multi-valued` | `all-of` and `any-of` over designators (`RulesCorpus.java:301-305`) | −192 | +64 |
| `responses` | no target or condition (`RulesCorpus.java:329-342`) | −48 | −48 |
| `synthetic:100` | Match targets (`SyntheticCorpus.java:134`) | −20.6 | saving lost |
| `pip:8` | Match targets (`PipCorpus.java:179`) | −24 | saving lost |
| rsa2008 | `any-of` over designators is common (`XacmlPolicySet-02d-prog-note.xml:29-38,50-61`) | −100.7 | lower, amount unknown |

   The controlled pair decides this. The design already allows declining the cycle (`design.md:1908-1910`).

2. **Existing, separate from both candidates:** `ResponseWriters` counts designators as silent (`:77-80`). A custom runtime value evaluated by a higher-order function could write responses that no staging holds. This comes from source reading only.

3. **Deep DAGs with DEBUG on:** building an `Apply` still expands the whole DAG text (`FunctionSpecBuilder.java:741-744`). Sol's tests suppress that logging (`NestedResponseStagingPdpTest.java:598-608`). No cost with DEBUG off.

BLOCKER: `report.md` not written. The persona's binding read-only rule forbids creating files, and that overrides the packet. The review is complete; copy this block to `evidence/reviews/code/rv206-d47-opus/report.md`.