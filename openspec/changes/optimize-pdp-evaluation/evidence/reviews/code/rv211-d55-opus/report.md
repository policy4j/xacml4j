**D55 candidate: APPROVE.** I found nothing at any severity, and the source refutes every failure hypothesis in the packet. Nothing was built or run, so all of this is traced in source. The approval covers the `cod_2` bytes only, because the main checkout's `AttributeIndex.java` already differs from them by one hunk. This pane is the record for rv211-d55-opus; I wrote no file.

FINAL REPORT (xacml4j Code reviewer, Opus 5.5)

## Scope

- **Candidate:** `W/cod_2`, base `759d90a6`. All six SHA-256 values matched before and after the review.
- **Ignored as instructed:** the four P-CPU2 files.
- **Paths:** unless stated otherwise, `AttributeIndex.java`, `Category.java` and `Attribute.java` are under `cod_2/xacml-core/src/main/java/org/xacml4j/v30/`.

## Findings

None.

## Question 1: does a lazy index answer exactly as a builder index?

**Holds.** The argument has three parts:

1. **List:** the lazy list has one contiguous run per id (`:133-148`), so it is already the builder's `values()` sequence.
2. **Multimap:** `multimap()` folds that same list (`:154-159`), so it gives the builder's multimap.
3. **State:** a keyed lookup only sets `attributes` and `bags`. After construction, `attributes` is read only by `multimap()` (`:152`), and `equals`, `hashCode` and `attributes()` read only the list (`:543-544`, `:549-561`, `:308`). Answers are therefore the same before and after the first lookup.

The hash sums `id.hashCode() ^ listHash` over runs (`:551-560`). That is the map hash, which `AttributeIndexTest.java:67` compares with Guava.

| Lazy input | Builder input | `equals`, both ways | `hashCode` | `attributes()` order |
| --- | --- | --- | --- | --- |
| `[a, b]`, distinct ids | `[b, a]` | equal | equal | differs, as two builder indexes do |
| `[a1, a2, b]` | `[a1, b, a2]` | equal | equal | same: the builder groups |
| `[a1, a2]`, one id | `[a2, a1]` | equal | **differs** | differs |

The last row breaks the `equals`/`hashCode` contract, but base has the same mismatch; D55 keeps it. The 204 summary names only the multiplicity case, and order within an id behaves the same way.

- **Methods that do not exist:** `containsAttributeId`, `getAttributesIds`, `size`, `contains` and `Builder.index` are not members of this class. `rtk rg` returned exit 1 for three of the names, with a positive control on the same tree, and I read the whole file for the rest. `design.md:2164-2166` is stale on this.
- **One observable change:** `attributes()` now returns an `ImmutableList` instead of Guava's `Values` view. The declared type stays `Collection<Attribute>`.
  - Every in-tree consumer iterates, streams, sizes, or calls `containsAll`.
  - Gson dispatches on the runtime class (`xacml-json/.../CategoryAdapter.java:86`, `TypeToGSon.java:248`). No `Collection` or `Iterable` adapter is registered, so both classes should take Gson's collection path; I relied on memory of Gson internals, UNVERIFIED.
  - An external caller using `equals` on two `attributes()` results may see a different answer. I did not read `Values`, so that is UNVERIFIED.
- **Source compatibility:** a bare `Entity.of(null)` is now ambiguous and will not compile. `rtk rg` found no such call in any module.

## Question 2: can a bad input take the lazy path?

**Refuted.**

- **More than eight attributes:** `:134` sends them to the builder.
- **Non-contiguous ids:** a recurring id at `i` differs from `i-1` and has an earlier copy at `j ≤ i-2`. The loop at `:137-146` reaches exactly that case.
- **Null element:** `ImmutableList.copyOf` at `:129` rejects it before the scan. Base also threw NPE (`:609`).
- **Null id:** impossible (`Attribute.java:55,165`).
- **Comparison:** the scan uses `String.equals`, the key equality of a hash-keyed multimap. The `Aa`/`BB` row (`AttributeIndexTest.java:342`) covers colliding hashes.

## Question 3: can threads see different answers or a partial map?

**Refuted.** `multimap()` reads only the final `attributeList` (`:65`) and `Attribute`'s final fields (`Attribute.java:49-52`). `Attribute` has a private constructor and no subclass.

A race builds two equal, complete maps, and each thread returns its own local reference (`:152-162`). `attributes` is no longer final, so a racily published index could show null, but the reader then rebuilds it from the final list. Nothing depends on the map's identity.

## Question 4: serialization

**The premise is refuted, and D55 changes nothing.** `attributeIndex` was never out of the stream.

- `Entity` is Serializable (`types/Value.java:41-42`).
- Its field is non-transient (`types/Entity.java:48`).
- `AttributeIndex` is not Serializable (`:51`, unchanged context in the base diff).
- `Value.java`, `Entity.java` and `Category.java` have no `writeReplace` or `writeObject`.

So `ObjectOutputStream` must throw `NotSerializableException` for every `Entity`, and for every `Category`, whose entity is mandatory (`Category.java:74`). That holds before and after D55, by JDK rule; I did not run it. No deserialized value exists to compare, and no in-tree test round-trips either type.

Fields and UIDs are unchanged (`types/Entity.java:45-48`, `Category.java:48-55`). The `transient` keywords in `AttributeIndex` have no effect.

## Question 5: `Category.includedInResult()`

**Holds:** same content and order.

- **Whole category:** the "all marked, no content" branch returning `this` is unchanged (`Category.java:143`).
- **Filtered list:** it comes from an already grouped index (`:173-182`, `:102-115`), so it stays grouped. `AttributeIndex.of` then keeps it, or rebuilds it through the builder above eight attributes. Both give the same sequence.
- **Builder's direct list:** `b.included` is used only when each id has one attribute, where add order is index order.
- **Reference id:** kept (`Category.java:87-89`).
- **Content:** dropped on both paths (`types/Entity.java:184-186`).

`ResolvedAttributesPdpTest.java:176-209` pins this after a keyed lookup.

## Question 6: RV209-ASTRA-1

**RV209-ASTRA-1 is closed.**

- **Is the bound real under concurrent first demands? Yes.** The count rises only at `:448`, after a successful `putIfAbsent`, under one monitor with the check at `:443`. `kept` is the single map from the CAS (`:459-464`). No schedule can exceed 64.
- **Can an overflow demand see a different `Optional` or bag? Refuted.** `resolve(String…)` returns the `Lookup` entry's `Optional` (`:404-415`). An alias stores that same object (`:436,444`), and nothing removes entries.
- **Does a lazy index keep the bound? Yes.** Neither constructor touches `bags` or the count. The bound test uses a lazy index (`AttributeIndexTest.java:172`).
- **Is a cached hit forced through the monitor? Refuted.** A hit returns at `:431-435`, before the count or the monitor. A cached hit never reads the volatile count; only a miss does.
- **Does 64 cover a real policy tree? Yes, on the data here.** The tree's size is the wrong measure, because only positive demands are admitted and every engine demand finds the entity by the key's own category:
  - `xacml-pdp/.../DefaultEvaluationContextHandler.java:316-319`
  - `policy/TargetIndex.java:105-107`
  - `policy/RangeTargetIndex.java:96-98`
  - `policy/PreparedConditions.java:269-273`
  - `policy/TargetBeforeContext.java:224`
  - `Category.java:98-102`, which checks the category

  `attribute-designator` takes the `Lookup` path instead (`policy/function/impl/AttributeDesignatorFunctions.java:75`). RV209's category spread therefore needs a direct call to the public `Entity.resolve(AttributeDesignatorKey)`.

  So one category needs at most one key per (id, type), plus one per issuer. Across the vendored suites:

  | Measure | Result |
  | --- | --- |
  | Requests / category groups | 952 / 3,826 |
  | Groups with 8 keys or fewer | 3,822 |
  | Largest group | 54 (`xacml3.0-ct-v.0.4/IIA024Request.xml`) |
  | Largest in rsa2008 | 6 |
  | Groups above 64 | 0 |

  `RulesCorpus` has 13 demanded ids, and `rules:N` does not scale them (`xacml-jmh/.../RulesCorpus.java:91-103`).
- **Past the bound:** each demand costs one `Lookup` and two extra probes, with no error. Admission is first-come, so a long-lived `Entity` keeps its first 64 keys.
- **Hash collisions:** `AttributeDesignatorKey.hashCode` ignores category and type (`AttributeDesignatorKey.java:92`), so spread keys share one bin. The cap limits that scan to 64 entries.

## Question 7: test gaps

None with reachable impact. RV209 has a real red run: 1,025 retained aliases against the expected 64 or fewer. Two properties are not tested, and neither has a failure path:

- That the lazy path is actually taken. The histogram gate shows this.
- That a cached hit avoids the monitor. Source proves this.

## Verification

- `SUPPORTED`: the traces above, from opened source plus `rtk rg`. I read the D55 and D48 design text, pr27, RV209-ASTRA-1, both author summaries, and `ImmutableCollection.asList()` from the Guava 33.7.2 sources jar.
- `PARTIAL`: caller inventories are text search only. Serena is `UNAVAILABLE` for this worktree, and I did not use ripwire.
- The key counts come from a read-only script of mine that printed totals only. Each is an upper bound that assumes every shape is demanded.
- `ast-grep scan` on the four production files: exit 0, no findings. `rtk git diff --check`: exit 0.

## Not verified

- No build, test or JVM run. All counts and the red run are the author's.
- Reactor, conformance, histograms and JMH are UNRUN.
- `cod_1`, `cod_2` and `cod_3` have not been built together. The in-flight D51 edits touch `BagOfValues` and `RequestContext`, next to this change.
- I did not count `PipCorpus` or external `DirectoryCorpus` inputs, and I had no production request corpus.
- I did not review the other five files in the main checkout.

## Residual risk

1. **The main checkout is not the reviewed code.** Its `AttributeIndex.java` hash is `6637dde5…`. It holds D55 and the RV209 fix plus one extra hunk at `findByIssuer` (`:252`): a for-each loop with two-space indentation in a tab-indented file. Behaviour is equal, and it adds an iterator. Reconcile it before any commit.
2. **The saving is unmeasured.** −728 B/op is a Guava 31.1 estimate, and D55's gate is UNRUN.
   - `AttributeIndex` grows about 8 B by layout arithmetic, UNVERIFIED.
   - Builder-built indexes with two or more attributes now retain a second list (`values().asList()`).
   - That costs 0 B/op for prebuilt JMH requests, so the design's "0 B" holds there. Requests parsed per decision pay it, and D55 does not count that.
3. **Design text has drifted.** The accessor list at `design.md:2164-2166` names methods that do not exist. The 64 cap is recorded only in code and evidence, so "a repeat demand builds no lookup" now holds only for the first 64 keys.
4. **Existing and unchanged:** equal indexes can have unequal hashes, and no `Entity` or `Category` can be Java-serialized.
5. **The core gate stays red** until the separate date fix lands.

LANE_COMPLETE opus D55-review