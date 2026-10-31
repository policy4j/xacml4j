**D47 corrected candidate: APPROVE.** I found nothing at any severity, and the source refutes every hypothesis in the packet. Nothing was built or run, so all of this is traced in source, and D47 still has to pass its JMH gate before it ships. As instructed, I wrote no file; this pane is the record for rv212-d47-recheck-opus.

FINAL REPORT (xacml4j Code reviewer, Opus 5.5)

## Scope

- **Candidate:** `W/cod_1`, base `759d90a6`. All three SHA-256 values matched before and after the review.
- **Paths:** unless a path says otherwise, it is under `cod_1/xacml-core/src/main/java/org/xacml4j/v30/policy/`.

## Findings

None.

| Earlier finding | State in source |
| --- | --- |
| r-D47-sol P2, runtime values reach a predicate | Closed: condition at `Rule.java:359-360`, target at `TargetBeforeContext.java:87` |
| rv205 P2-1 and rv206 P2, `attribute-designator` | Closed: `Rule.java:361` |
| rv205 P2-2, Entity literal | Closed: extraction is an Apply, so it scores `RUNTIME_VALUE` at `Rule.java:361` |
| rv206 P3, `computed argument` row could not fail | Closed: now uses `string-union` (`RulesInPolicyContextPdpTest.java:180-181`) |

## Question 1: can a borrowed rule's target be matched in the policy context?

No. The proof is short and local:

1. Borrowing starts only at `TargetBeforeContext.java:88`, the sole `enterRule` call.
2. Inside that scope only two lines can run the rule (`:99-100`). Line 99 has no answer, and the guard at `:87` admits it only for a rule with no Match (`Rule.java:288`). Line 100 is a proven MATCH, because NOMATCH already returned at `:82-84`.
3. `evaluatePrechecked` leaves the answer (`RootEvaluationContext.java:619`), and `createContext` returns the same context (`Rule.java:89-90`). `evaluateRule` then calls `isMatch` (`Rule.java:151`), which takes the answer first (`BaseDecisionRule.java:213`).
4. `target.match` has one call site (`BaseDecisionRule.java:225`), reached only when no answer is present (`:218`).

A lost answer would still be safe. `Match.match` runs the same scan first (`Match.java:122-127`) on the same bag (`xacml-pdp/.../DefaultEvaluationContextHandler.java:319`). `Target`, `MatchAnyOf` and `MatchAllOf` also stop where the precheck stops (`Target.java:71`, `MatchAnyOf.java:65-68`, `MatchAllOf.java:74-77`).

Each sub-hypothesis:

- **Answer taken by another rule: refuted.** The take compares identity (`RootEvaluationContext.java:607`), and no other rule runs between leave and take.
- **Answer dropped: refuted.** For a rule, only the `finally` at `:626-628` clears it, after the rule returns.
- **Pin: refuted.** A pin is never an exact `Rule` (`Policy.java:398`), and its answer serves only its composite target.
- **Policy reference: refuted.** `ReferencedPolicyContext` is a policy context (`PolicyIDReference.java:308`), so the same guard applies. An unpinned reference gets no plan (`TargetBeforeContext.java:195-197`).
- **Nested evaluation: refuted.** A composite takes its answer before it reaches its children (`BaseCompositeDecisionRule.java:116`).
- **Reentrant evaluation: refuted.** The closest shape is a rule re-entering itself: UNKNOWN at `TargetBeforeContext.java:209`, then `createContext` returns the borrowed context. That needs custom code, which a borrowed rule cannot contain.
- **`evaluateAfterPass`: no such symbol** in the design or the three `xacml-core` trees (`rtk rg`, exit 1).
- **Extended-Indeterminate pass: refuted.** A pass reruns `combineDecisions` (`BaseCompositeDecisionRule.java:301`), so each rule takes the same route with the pass's own slot (`Policy.java:451`). A pass starts outside any rule.
- **`isDefinitelyNoMatch`, then a match call: refuted.** Its only caller handles composites (`combine/impl/OnlyOneApplicablePolicyCombiningAlgorithm.java:40,58`). A MATCH left at `RootEvaluationContext.java:637-638` names a composite, which no rule can take.
- **Index path: refuted.** Both `candidates` methods only choose children; neither calls a match or an evaluate. Any non-`StringVal` value returns all children (`TargetIndex.java:116,127`). Chosen rules still go to the algorithm (`Policy.java:228-229`).

All eleven classes in `Policy.java:72-78` reach a rule through `evaluateIfMatch`:

| Algorithm | Route, under `combine/impl/` |
| --- | --- |
| first-applicable | `FirstApplicable.java:57` |
| deny-overrides and ordered | `DenyOverrides.java:80` |
| permit-overrides and ordered | `PermitOverrides.java:58` |
| deny-unless-permit | `DenyUnlessPermit.java:57`; stream form `:67` |
| permit-unless-deny | `PermitUnlessDeny.java:55` |
| legacy deny-overrides and ordered | `LegacyDenyOverridesRuleCombineAlgorithm.java:74` |
| legacy permit-overrides and ordered | `LegacyPermitOverridesRuleCombineAlgorithm.java:63` |

## Question 2: is a precheck MATCH free of `value.evaluate(context)`?

Yes. A MATCH reads the entity, the bag and the scan (`TargetBeforeContext.java:224-228`). None of those files has an `.evaluate(` site, in a search of core outside `spi`.

| Scan | What happens with a custom value | Evidence |
| --- | --- | --- |
| `equal` | An exact-class pass over the whole bag gives NONE before any comparison | `BagStorage.java:65-67` |
| Integer and double comparison | An `instanceof` pass runs first, and both classes are final | `BagStorage.java:76-78,86-88`; `types/IntegerVal.java:36`, `types/DoubleVal.java:35` |
| `Integers`, `Doubles`, `Booleans` | Built only when every value fits; otherwise `Values` | `BagStorages.java:96,133-141` |
| Temporal | An exact-class pass runs first | `BagStorages.java:405-410` |

- **Mixed bag:** `{"yes", custom}` gives NONE even though `"yes"` would match, so the precheck is UNKNOWN (`TargetBeforeContext.java:229`) and the rule keeps its own context.
- **`safeValues`:** it requires the exact literal class and a built-in designator type (`:161-186`). So `literal.equals(value)` runs only between two values of that exact class.
- **Predicate with a direct scan:** borrowing also needs a trusted built-in predicate (`Rule.java:289`).

## Question 3: status and the local slot

Holds. The source refutes both a misplaced status and a dirty slot.

- **Failing condition:** `Condition.java:101-103` writes to the borrowed context. `Policy.java:444-446` records the rule's local status, its keyed status, then the policy's. An ordinary rule context makes the same writes in the same order (`DescendantEvaluationContext.java:105-115`).
- **Generic exception:** `Condition.java:104-108` writes only the keyed status. The fallback at `Rule.java:175` then finds the local slot empty (`Policy.java:427-428`), as a fresh rule context would.
- **Next rule:** `exitRule` clears the slot in a `finally` (`TargetBeforeContext.java:91-93`, `Policy.java:412-415`), and `enterRule` clears it again (`Policy.java:403-404`).
- **Prechecked rule that throws:** the answer was already taken, before the condition ran. Both `finally` blocks still run (`RootEvaluationContext.java:626-628`, then `TargetBeforeContext.java:91-93`).
- **Inside a pass:** the keyed status is written before the guard that keeps the status which started the pass (`DescendantEvaluationContext.java:123-130`).

`matchedTargetKeepsTheFailingRulesStatus` (`RulesInPolicyContextPdpTest.java:292-313`) covers the first case against an observed decision.

## Question 4: does a Value, bag or literal carry an unchecked value at score 0?

One does, but it is not reachable. A bag value reaches `evaluate` only through a `Match` predicate or a function reference; every other `.evaluate(` site takes a policy expression. No class in `types` overrides `evaluate` (positive control: `ValueExp.java:41`).

- **Entity literal:** it scores 0 (`Rule.java:333-335`) and can hold a custom value. Extraction needs an Apply, which scores `RUNTIME_VALUE`, so a higher-order call over it is UNSAFE (`Rule.java:359-361`). A reference to `attribute-designator` cannot extract inside the call: its type is a bag (`function/impl/AttributeDesignatorFunctions.java:144`), and the higher-order forms reject that (`function/impl/HigherOrderFunctions.java:436,486`).
- **Bag of Entity:** each element scores 0 (`Rule.java:336-343`) and evaluates to itself.
- **Variables:** the memo keeps each definition's flags (`Rule.java:371-377`), and a cycle stays UNSAFE.
- **`FunctionReference` through a variable:** the memo returns `FUNCTION_REFERENCE`, so `any-of(varRef(fn), "x", designator)` is UNSAFE at `:359-360`. With literals only it stays eligible, which is correct.

## Question 5: combination with `cod_2` and `cod_3`

No change to the proof. No file is changed by more than one of the three worktrees.

- **D52:** its five production files have the same hashes I reviewed for rv208. The proof reads `function()`, `getArguments()` and `FunctionReference.function()` (`Rule.java:347,352,356`), and D52 leaves all three alone. The prepared call passes on the context it receives (`cod_3/.../HigherOrderCall.java:94-103`). A `Shared` operand stands for an Apply, so a higher-order call over it is never borrowed.
- **Lazy `AttributeIndex`:** `resolve` takes no context and calls no `evaluate`. The 64-alias cap only stops caching, and `Entity.of` and `Category.of` serve response entities. The `cod_2` diffs to `Policy.java` and `PolicySet.java` are one line each and leave `enterRule` alone.
- **In-flight edits in `cod_3`:** `BagStorage.java` is unchanged, so the class passes stay. `StringVal.equals` adds an early hash check only. `RequestContext.getEntity` reads the same category order (`RequestContext.java:83`).

## Verification

- `SUPPORTED`: the traces above, from `rtk rg` and opened source. I read every rule algorithm, the D47 design, and the three earlier reports.
- `PARTIAL`: the single-call-site claims for `enterRule` and `target.match`. `rtk rg` and `ast-grep` agree, and bare `isMatch` calls were searched separately. Serena is `UNAVAILABLE` for these worktrees, and I did not use ripwire.
- `ast-grep scan` on `Rule.java` and `TargetBeforeContext.java`: exit 0, no findings. `rtk git diff --check` in `cod_1`: exit 0.

## Not verified

- No build, test or JVM run. The test counts and both mutants are the author's.
- `cod_1`, `cod_2` and `cod_3` have not been built together. Reactor, conformance and JMH are UNRUN.
- I did not review the D51 edits in `cod_3` or `206-rv209-sol` in `cod_2`, beyond what the proof relies on. `207-d51-fix-astra` has receipts but no summary.
- The XACML sections were not re-fetched, because this change alters no evaluation semantics.
- The package comparison at `Rule.java:334` assumes classes of one loader share a `Package` object. That is JDK behaviour from memory, so it is UNVERIFIED.

## Residual risk

1. **The allocation gate may still fail** (`design.md:1899-1900`). Rules with a proven Match can borrow again, so `narrow` should return to about −24 B/op if the precheck decides its targets. `any-of` over designators keeps a rule context, so `multi-valued` stays near +64 against the design's −192. Targets resolved through a PIP also keep theirs. These figures are arithmetic from `−32R + 8P`, UNVERIFIED.
2. **Design D47 has not been amended.** Two rules live only in code: every built-in Apply counts as runtime, and an undecided Match keeps a rule context. `design.md:1795-1804,1824` should record both. D53 (`design.md:2097-2101`) must keep the guard at `TargetBeforeContext.java:87`. This correction restores the D53 premise that rv206 said was lost.
3. **Existing, not from this candidate:** a custom `FunctionSpec` that overrides `hasDirectScan` and `scan` gets the policy context during the precheck (`TargetBeforeContext.java:140,228`). No such implementation exists in the repository, the Javadoc limits the method to built-ins (`FunctionSpec.java:170-172`), and that rule cannot borrow (`Rule.java:289`).
4. **Carried from rv206:** `ResponseWriters` still counts designators as silent. This candidate does not change that.

LANE_COMPLETE opus D47-recheck