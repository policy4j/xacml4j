READ-ONLY PLAN REVIEW (a new packet in this pane; rules: no file writes, no Maven, tests, benchmarks, git or
br writes). Role: architecture reviewer from a model family other than the author's (the conductor, Claude
Opus 5.5, wrote the plan). Lane tag RVC7.

Review the revised design D7 (cycle C) of openspec/changes/optimize-pdp-evaluation/design.md, as the
maintainer decided it on 2026-10-27: "yes and specializations for primitive types to avoid allocation, each
type should be able to create its own version of bag with underlying storage, for primitive arrays we can
apply Java vector SIMD operations down the road". Read D7 in full (including the Eclipse Collections
alternative), tasks.md group 6, research/pr1-opus-allocation.md H1, research/pr2-fable-cpu.md H1, the
characterization tests of evidence/tests/007-cycleC-characterization, and the code:
xacml-core/src/main/java/org/xacml4j/v30/BagOfValues.java, BagOfValuesType.java, types/ValueType.java,
types/XacmlTypes.java, types/Value.java, types/IntegerVal.java, types/DoubleVal.java, types/BooleanVal.java,
policy/Match.java, policy/function/impl/BagFunctions.java, SetFunctions.java, HigherOrderFunctions.java,
EqualityPredicates.java, NumericComparisonFunctions.java.

Answer:
1. Is the storage SPI on ValueType the right seam (an additive default method), and what exactly should it
   look like: the storage interface, how BagOfValues.Builder uses it, how a custom ValueType opts in or
   out, and how iteration order (equal values grouped by first occurrence), counts, equality, hash, and
   the set view stay identical across storages?
2. The primitive fast path: which built-in predicates should a long[] / double[] / boolean storage answer
   directly in Match and in the bag and set functions, how the fast path is selected (provenance, not the
   function id), and how errors, NaN, signed zero, and the recorded policy4j-0972 Match behavior stay the
   same. Where would a generic path still box values, and is that acceptable?
3. Serialization: is serialPersistentFields plus writeObject / readObject enough to keep the stream of
   BagOfValues identical in both directions? What golden-stream tests prove it?
4. Eclipse Collections 13.0.0 (the maintainer asked whether to adopt its primitive collections and noted
   that its factories can add collection types): is the recorded recommendation right (own ordered
   arrays for bag storage; Eclipse Collections only where a primitive-keyed hash structure is a measured
   hot spot; an adapter to its primitive iterables possible)? Would implementing its API interfaces
   (ImmutableLongBag or LongIterable) for our storage be worth its size and contract?
5. The Vector API later: what in the storage design keeps a SIMD scan internal?
6. Risks, BREAKING items, and the task order you would use.

Report: findings most severe first (P1 to P3), concrete recommendations, then a verdict on the plan:
APPROVE, APPROVE WITH CHANGES, or REJECT.

Write your report between these two lines, each on its own line, exactly as written:
=== BEGIN RVC7 REPORT ===
=== END RVC7 REPORT ===
