=== BEGIN REPORT R-AV-opus ===
FINAL REPORT (policy4j_implementer, Claude Opus 5.5): R-AV, review of cycle AV (Codex Astra)

## Verdict

**Approve.** There are no P1 or P2 findings. The three P3 findings are optional.

- **Handler:** D26, D27, task 30, the positive caches and the observed `REQUEST_MEMO` are kept. The holder and each member are published with compare-and-set, and every accessor uses the winning holder.
- **Trade:** the holder costs +8.0 B per PIP decision, measured exactly; a request-only decision saves 24 B. That is the right trade.
- **Bag scans:** the index loops visit the same values in the same order as the iterators did.
- **C4:** every precondition and message is kept for foreign and mock contexts, including one whose answer changes. `Policy` and `Rule` were rightly left alone. The time gain is close to zero (P3-3).
- **Tests:** they pin the outcomes; three mutants were killed.
- **Allocation:** on the merged tree (AU and AV), rsa2008 goes from 826.38 to 774.15 B/op (−52.23). Astra reported −52.24 (1,231.10 to 1,178.86).

## Findings

**P3-1. Some handler comments lost detail, and one is now inaccurate.**
- **Where:**
  - `DefaultEvaluationContextHandler.java:64-68`: the class comment does not mention the holder.
  - `:179-183`: the `requestDesignators` comment.
  - `:186`: the `flights` comment.
- **What changed:** the field comments moved into `ResolutionState` in a shorter form (old text: `PATCH-AV.diff:154-176`).
- **`requestDesignators`:**
  - The comment lost three points:
    - why the memo exists: the observation tells a repeated test from the first (D34);
    - why an unobserved decision needs none (cycles AF and AN);
    - that PIP bags stay in `designatorCache`, which the response's resolved attributes come from.
  - It now cites XACML 3.0 §7.3.5 for the request entity's one bag per designator. But §7.3.5 states the requirement: "every subsequent test of that attribute shall use the same bag of values that was initially tested" (OASIS core spec, fetched 2026-10-28). The entity's single bag (cycle AF) is how the engine meets it, as the old text said.
- **`flights`:** the comment now reads "Overlapping callers share an attempt". But a caller inside a resolution of this handler runs its own attempt (`:391-401`, `:415-417`). The old text kept that qualifier.
- **Fix:**
  - Put the old comments back on the holder's fields; `{@link #designatorCache}` still resolves inside `ResolutionState`.
  - Add one clause to the class comment: the state lives in one holder, created with its first member (cycle AV).

**P3-2. The static cache suppliers add a layer with nothing behind it.**
- **Where:** `DefaultEvaluationContextHandler.java:75-76`, used at `:129` and `:136`.
- **Why it is now redundant:**
  - The removed four-argument constructor was their only reason to exist.
  - `flights()` (`:143`) and `requestDesignators()` (`:154`) already call `new ConcurrentHashMap<>()` directly.
- **Fix:** do the same in `designatorCache()` and `selectorCache()`, and drop the two fields. My P-FIXED2 advice to make them static was written before that constructor was removed.

**P3-3. C4 removes the cheapest query, so its time gain is close to zero.**
- **What it skips:** the second `getCurrentPolicySet()` query. An own parent answers it at the first step of its walk (`DescendantEvaluationContext.java:253-256`).
- **Where the skip applies:** only when the parent is a policy set context (`PolicySet.java:197`): an inline child set, or a child of a merged reference context.
- **Where both queries remain:**
  - a top-level set, whose parent is the root; the root answers at once (`RootEvaluationContext.java:521-523`);
  - an observed reference entry (`PolicySetIDReference.java:145-146`).
- **What still costs:** the constructor's `getCurrentPolicy() == null` check (`PolicySet.java:370-371`). It walks each set context's chain up to the top (`DescendantEvaluationContext.java:215-222`). That walk is the larger part of the cost that P-FIXED2 listed under C4.
- **Possible follow-up** (computed, not measured; profile first): for an own parent whose top is a `RootEvaluationContext`, that walk is redundant too.
  - The root always answers null (`RootEvaluationContext.java:492-494`).
  - Every set context is created only after the same check, on a chain that never changes.
- **Premise not stated in the code:**
  - The skip relies on every `PolicySetDescendantEvaluationContext` answering `getCurrentPolicySet()` from its final outer reference (`PolicySet.java:387-389`).
  - That includes the final subclass `ReferencedPolicySetContext` (`PolicySetIDReference.java:290-302`). Neither class overrides the method.
  - The comment at `PolicySet.java:196` states only the foreign case. One more line there would stop a later override from silently breaking the skip.

## Checks

### 1. The handler

**Layout** (default headers, compressed references):
- Before: 9 references, 48 B (`PATCH-AV.diff:143-176`).
- After: the handler has 3 references, 24 B (`:85`, `:88`, `:90`); the holder has 5 references, 32 B (`:174-191`).
- The histograms in the trade table below confirm both sizes exactly.

**When the holder is created:**
- **Not created:**
  - `resolve` reads `state` without creating it (`:249-274`).
  - An unobserved request bag returns first (`:310-312`).
  - A key that no resolver supplies returns `missing` first (`:324-326`, `:344-346`).
  - `hasResolvedDesignators` does not create it (`:206-211`), and the result reads the designators only after that test (`DefaultPolicyDecisionPoint.java:354-359`).
- **Created:**
  - the first PIP attempt (`flights()`, `:412`);
  - the first observed request bag (`:314`);
  - `getResolvedDesignators` and `getResolvedSelectors` (`:198-204`).
- **Matches the B/op:** rsa2008's −52.23 is about −24 for the handler plus −28 for the iterators (27.5 B/op in P-FIXED2). So rsa2008 decisions create no holder.

**Publication:**
- `state()` returns the compare-and-set winner (`:119-123`).
- Each member accessor calls `state()`, then compare-and-sets the member in that holder (`:125-162`). All six fields are volatile.
- `published` calls `state()` only after `flights()` has created the holder (`:412`, `:441`, `:451-456`).
- A stale null read at the top of `resolve` only skips the memo checks. The flight then re-checks the published bag (`:441-445`). The old code gave the same outcome for a read just before the publication.

**D27 single flight:**
- The flight is registered and removed in the same map (`:412-413`, `:430`).
- It already was before. At `:430`, `flights` named the field, which is set once and never cleared (`PATCH-AV.diff:170`). The new local only replaces a field that no longer exists.
- Astra's report reads as if this were a fix; it changes no behavior.

**D26 parallel resolution:**
- Concurrent callers race only on creating the holder and on the member compare-and-sets that already existed.
- `ParallelAttributeResolverTest` (8 tests) passes.

**Task 30 session:**
- `session()` now lives in the holder (`:157-162`) and is still created inside the first attempt.
- `theAttributesOfOneResolverReadServeTheDecisionTogether` passes, and fails under mutant m2.

**Positive caches and rv008:**
- `getResolvedDesignators` is a view of the winning holder's cache.
- So a map kept before the first bag sees later bags (`DefaultEvaluationContextHandlerTest.java:985-1006`).

**Observed `REQUEST_MEMO`:** unchanged. `DecisionObserverPdpTest.java:142` and `:217` pin the REQUEST, then REQUEST_MEMO events.

**The trade, measured exactly** (Epsilon GC class histograms around 10,000 decisions after warm-up):

| `pip:8` case | Handler before | Handler after | Holder after | Net per decision |
| --- | ---: | ---: | ---: | ---: |
| `independent` | 48.0 B | 24.0 B | 32.0 B | +8.0 B |
| `shared-static` | 48.0 B | 24.0 B | 32.0 B | +8.0 B |

- **Counts:** one handler and one holder per decision. The maps, flights, frames and futures are the same in both histograms.
- **Other rows that move:** they are PIP lambdas, `Optional` and iterators that the JIT eliminates differently from run to run. AV does not touch that code.
- **Size of the cost:** +8 B is about 0.1% of a PIP decision (6.2 to 6.5 KB/op). A request-only decision saves 24 B, 3 to 9% on the merged tree (rsa2008 826, `narrow` 312, `synthetic` 278 B/op).
- **JMH cannot resolve 8 B on `pip:8`:**
  - Within one run, its fork means differ by 136 to 554 B.
  - The fork means of `independent` fall into modes. Two modes moved by exactly +8.0 (6,176.3 to 6,184.3, and 6,088.3 to 6,096.3); one did not move (6,232.3).
- **Verdict:** the right trade.

### 2. The bag scans

**Same sequence:**
- `BagStorage.iterator()` is `valueAt(index++)` over `[0, size())` (`BagStorage.java:100-112`).
- No storage in the tree overrides it, including the custom storage in `BagStorageCompatibilityTest.java:176-190`.
- `valueAt` is O(1) in every storage (`BagStorages.java:159`, `:180-183`, `:241`, `:257-260`, `:294-297`, `:368-373`).
- So `Match.java:127-132` and `BagOfValues.java:271-277` visit the same values in the same order.

**What is kept:**
- **Literal first:** `predicate.invoke(context, value, attributes.valueAt(i))` (`Match.java:128`).
- **Duplicates:** every occurrence is visited, in grouped order.
- **Short-circuits:** the first match returns (`Match.java:129-131`), and so does the first missing value (`BagOfValues.java:274`).
- **Failure path:** the `EvaluationException` path to the status and Indeterminate is unchanged (`Match.java:134-138`).

**Pinned by existing tests:**
- `MatchTest.java:78` pins the argument order, the short-circuit, and that no extra calls are made.
- `MatchTest.java:204` pins a failure on a grouped duplicate before a later match.
- Mutant m3 (reversed loop) fails both.
- For `containsAll`, the order of the `contains` calls cannot be observed.

### 3. C4

**Foreign and mock contexts.** The flag is false (`PolicySet.java:197`), so the constructor runs as before (`:363-372`):
1. It checks `getCurrentPolicySet() != this`.
2. It checks `getCurrentPolicy() == null`.

The calls, their order and the exceptions are unchanged:
- `IllegalArgumentException` with a null message;
- `NullPointerException` with a null message, from `checkNotNull` (`:192`).

A context that first answers null and then this set is still rejected.

**Own contexts:**
- Their answer cannot change: `policySet()` is final and returns the outer instance (`:387-389`). So the skipped check can never fail for them.
- `ReferencedPolicySetContext` is built through the one-argument constructor (`PolicySetIDReference.java:292-294`), which keeps both checks (`PolicySet.java:358-361`).

**`Policy` and `Rule` were correctly left alone:** they have no duplicate query.
- `Policy.createContext` asks `getCurrentPolicy()` once (`Policy.java:191-201`); its constructor asks nothing (`:325-331`).
- `Rule.createContext` asks only `isEvaluationContextValid` (`Rule.java:70-81`); its constructor asks nothing (`:172-174`).
- Neither file is in `PATCH-AV.diff`.

### 4. Tests

**The new `PolicySetTest` cases** (`:141`, `:162`) pin:
- the two exception messages;
- a changing foreign context, which is the one outcome the C4 flag could change;
- the reuse of a foreign context that already names the set.

Mutant m1 (the skip for every context) fails `contextCreationRejectsNullAndForeignPolicyContexts`.

**The handler needs no new tests.** Mutant m2 (the holder is never published) fails 16 of 64 tests, covering:
- single flight and the published bag;
- rv008 and `hasResolvedDesignators`;
- the session and the selector cache;
- the observer's `REQUEST_MEMO` sequences.

**Left untested by design:**
- Allocation is not an outcome; it is measured here instead.
- The own-parent `getCurrentPolicy()` check after the skip needs no test: it can fail only if a foreign top context changes its answer.

## Verification

1. **Before files:**
   - With its `a/` and `b/` prefixes repaired, `PATCH-AV.diff` applies cleanly in reverse to this tree, so the tree holds exactly AV for the five files.
   - Reversing it gives "main with AU, without AV", which is the "before" side.
2. **Full reactor on this tree** (AU and AV): `mvn -o -Dlicense.skipUpdateLicense=true verify` gave BUILD SUCCESS. 10,350 tests ran: 0 failures, 0 errors, 36 skipped.

   | Module | Tests | Skipped |
   | --- | ---: | ---: |
   | core | 7,215 | 2 |
   | xml | 254 | 0 |
   | json | 24 | 1 |
   | pdp | 715 | 3 |
   | test support | 99 | 0 |
   | conformance | 1,818 | 30 |
   | jmh | 225 | 0 |

   The test counts that Astra cited match the Surefire reports: handler 50, parallel resolver 8, safety net 27, unresolvable 3.
3. **Three-fork smoke,** before and after interleaved: C2, `-prof gc`, JMH lock taken, load average 8.6 to 17.6.

   | Workload | B/op before | B/op after | µs/op before | µs/op after |
   | --- | ---: | ---: | ---: | ---: |
   | rsa2008 `all`, index off | 826.38 ± 0.04 | 774.15 ± 0.04 | 1.758 ± 0.036 | 1.673 ± 0.060 |
   | `pip:8 independent` | 6,165.66 ± 65.60 | 6,170.99 ± 62.33 | 1.827 ± 0.158 | 1.738 ± 0.059 |
   | `pip:8 shared-static` | 6,546.30 ± 238.48 | 6,501.18 ± 273.92 | 1.865 ± 0.040 | 1.842 ± 0.116 |

   All time intervals overlap. For rsa2008 they overlap only barely: 1.722 to 1.794 against 1.613 to 1.733. No time change is significant.
4. **Exact attribution:** Epsilon GC class histograms; results in the trade table of check 1.
5. **Mutants:** run in a scratch copy of the tree, never in the worktree. m1, m2 and m3 were all killed (check 4).
6. **Tree check:**
   - `git status` shows 238 entries, as at the start.
   - The five AV files are unchanged, and there is no license churn.
   - No production code was changed, and nothing is staged.

## Residual risk

1. **Times are indicative.** The load average was 8.6 to 17.6. Astra's smokes ran with the JMH lock ignored (their report). B/op is the evidence.
2. **`pip:8` B/op varies by fork,** because the JIT eliminates PIP-side allocations differently from run to run. The +8 B comes from the histograms, not from JMH.
3. **Custom bag storage:** a `BagStorage` whose `iterator()` disagreed with `valueAt` would now be scanned in `valueAt` order. The contract defines the indexes over every occurrence (`BagStorage.java:32-37`), and no such storage exists in the tree.
4. **C4's time was not measured on its own;** Astra says the same.
5. **Astra's evidence was not inspected** (UNVERIFIED): `evidence/builds/av-astra-*`, `evidence/tests/av-astra-*` and `evidence/benchmarks/jmh/av-astra` are not in this worktree. My runs reproduce their rsa2008 B/op exactly.

## Files

- **Changed:** none. This was a read-only review; no production or test file was changed.
- **Report:** `REPORT-R-AV-opus.md` at the worktree root.
- **Evidence** (gitignored) in `target/r-av/`:
  - `command.txt`, `summary.md`, `runs.txt`, `verify.log`, `jars.sha256`;
  - `{before,after}-*.json/.log`, `histo-*.txt`;
  - `mutants.txt`, `mutants/*.log`;
  - `scripts/` (`smoke.sh`, `PipHistoProbe.java`, `mutate.sh`).
=== END REPORT R-AV-opus ===
