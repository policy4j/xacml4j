# Synthesis: first profiling swarm (task 2.2)

Inputs: profiles `evidence/benchmarks/jmh/003-profile-decide-jdk25` (Log4j build) and
`004-profile-decide-jdk25-logback` (Logback build, the analysed set); JMH baselines
add-pdp-jmh-benchmarks `002-baseline-jdk25` (JDK 25) and `001-baseline` (JDK 21). Lane reports,
read-only, 2026-10-27:

| Lane | Model | Focus | Report |
| --- | --- | --- | --- |
| PF1 | Claude Opus 5.5 | allocation per non-matching policy | `pf1-opus-allocation.md` |
| PF2 | Claude Fable 5.1 | fixed cost per request | `pf2-fable-fixed-cost.md` |
| PF3 | Codex GPT-6.1 Sol | CPU hot frames, streams, call sites | `pf3-sol-cpu.md` (after one follow-up) |
| PF4 | Codex GPT-6 Astra | collections and data structures | `pf4-astra-collections.md` |
| PF5 | Codex GPT-6.1 Terra | algorithms, literature, JVM options | `pf5-terra-algorithms.md` |
| PF6 | Agy Gemini 4 Argon | other XACML engines | `pf6-agy-engines.md` |

Every gain below is a lane estimate from sampled profile shares, not a measurement; each cycle
proves its own gain with a JMH pair. The conductor checked each claim used here in source.

## Where the cost is (conductor arithmetic, JDK 25 baseline)

- Fixed cost per decision (synthetic:1000, first-deny): 6.50 µs and 15.8 KB.
- Each non-matching policy of a full scan: (479.1 − 6.50) µs / 1,000 = 0.47 µs and
  (2,127,619 − 15,816) B / 1,000 = 2.1 KB.
- PF1 inventory of those 2.1 KB (sample shares match object layouts, for example `HashMap`
  48 B against 48.6 B): 76% is the request attribute `resource-id` resolved again for every
  policy: entity lookup stream (`RequestContext.java:220-224`), issuer filter stream
  (`AttributeIndex.java:105-109`), type filter stream with varargs (`Attribute.java:114-122`),
  and a Guava multiset bag (`Entity.java:214`); 6.3% an eager empty bag
  (`AttributeReference.java:116-118`); 6.8% the function call path of `Match`
  (`FunctionSpec.java:145`, `FunctionSpecBuilder.java:507,603`); 4.1% multiset iteration in
  `Match.match`; 5% reference and policy contexts and the eager variable map
  (`PolicyIDReference.java:80`, `Policy.java:167,252`).
- PF2 split of the fixed cost: request preparation in the multiple-decision handler chain
  (22% of bytes, 24% of CPU; `containsRepeatingCategories` called four times), attribute
  resolution (38%, 32%), `getIncludeInResultAttributes` (7%), root context and handler
  construction (7%), result and response building (4%), `getResolvedAttributes` (2%).

## Consensus hypotheses and the cycle order

| Cycle | Hypothesis | Lanes | Evidence (checked in source) |
| --- | --- | --- | --- |
| A | One bag per request attribute and decision: memo of request-resolved designator bags in the per-decision `DefaultEvaluationContextHandler`; PIP results keep their own map (selector memo: backlog) | PF1 H1, PF2 H3, PF3 H1, PF4 H1, PF5 H3, PF6 H1 (all six lanes) | handler caches only PIP results (`DefaultEvaluationContextHandler.java:108-111,169`); handler created per decision (`PolicyDecisionPointContextFactory.java:176`); XACML 3.0 §7.3.5: every later test of an attribute uses the bag first tested |
| B | Loops instead of streams on the lookup path; zero-copy returns | PF1 H9, PF2 H1, PF3 H2, PF4 H2, PF5 H6, PF6 H1 | `AttributeIndex.findByIssuer` stream and collector; `Attribute.getValuesByType` varargs and stream; `RequestContext.getEntity` stream |
| C | Bags without the Guava multiset: array-backed values, shared empty bags, no eager empty bag, indexed loop in `Match` | PF1 H2, H4; PF2 H4; PF4 H3, H5; PF5 H4, H8; PF6 H2 | `BagOfValues` builds `LinkedHashMultiset` and `ImmutableMultiset`; `orElse(emptyBag())` evaluates eagerly |
| D | Fixed-arity predicate call without list normalization | PF1 H3, PF4 H6, PF5 H7, PF6 H3 | `FunctionSpec.invoke` varargs to list; `LinkedList` normalization per call |
| E | Fixed cost: precomputed multiple-decision flags of the immutable request, `getIncludeInResultAttributes` once, lazy maps and deques in the root context and handler, no skip list, `getResolvedAttributes` early return, static logger, `Objects.hash` without varargs | PF2 H2, H5-H9, H12; PF4 H8-H12; PF6 H5-H7; PF1 H8 | `RequestContext.java:144-146,274-281`; `RootEvaluationContext.java:66,107-116`; `DefaultEvaluationContextHandler.java:76-79`; `SemanticIdentifier.hashCode` |
| F | Policy level: variable map only when a policy has variables; one context object per reference; a simpler reference cache than Guava `LocalCache` | PF1 H5-H7, PF4 H4, H7, PF5 H5, PF6 H10 | `Policy.java:252`; `DefaultPolicyReferenceResolver` |
| G | JVM option, measured only: `-XX:+UseCompactObjectHeaders` (JEP 519, product in JDK 25, off by default) | PF5 H10 | flag present, default false (`java -XX:+PrintFlagsFinal`, 2026-10-27) |
| F2 | Shorter forwarding through the internal evaluation contexts (descendant contexts delegate several levels per call) | PF3 H5 | measured within cycle F |
| R | Research, not a cycle yet: target index or compiled decision plan; reflection-free function invocation (`MethodHandle`, prelinked built-ins) | PF1 H10, PF3 H7, PF5 H1-H2, PF6 H4, H9 | needs a design that keeps Indeterminate, missing-attribute, and PIP semantics |

Order follows PF1's dependency argument: A first (it removes most per-policy repetition and
makes B and C apply mainly to the first lookup), then B, C, D, E, F, then G as a flag study.
Each cycle has its own JMH pair; gains are not added up in advance.

## Backlog after the cycles (review RVP)

Unrefuted hypotheses that no cycle covers yet; the second profiling round (task 11.1) decides
each: selector memo (PF1, PF2); fewer context objects per reference and shorter forwarding
(PF1 H7, PF3 H5; needs a design for the public parent chain); staged obligation and advice
lists (PF4 H12); prelinked or `MethodHandle` function invocation (PF3 H7, PF6 H9); target
index or compiled plan (PF1 H10, PF5 H1-H2, PF6 H4).

## Corrections to lane reports (review RVP)

PF6 cites 480 µs as synthetic:1000 `request=all`; the baseline's 480 µs is `last-permit`
(`002-baseline-jdk25/summary.md`). It cites 85,484 allocation samples where the profile table
of `alloc-synthetic_1000-all` has 79,945. PF6 is used here only for its cross-engine
techniques and its hypothesis list, not for these figures.

## Correctness findings (not optimizations)

PF5 found, and the conductor confirmed in source, two deviations from XACML 3.0 that change
decisions in error cases; they are bugs `policy4j-yeu3` (`Target.match` returns Indeterminate
before a later No-match `AnyOf`, §7.7) and `policy4j-0972` (`Match.match` returns Indeterminate
on the first failed comparison although a later value matches, §7.6). Review RVP adds a third: a PIP absence
followed by a later value in one decision (§7.3.5), bug `policy4j-6vhd`. They need their own
semantics change and maintainer approval; no optimization cycle may depend on fixing them, and
each cycle's tests keep today's behavior for those cases.

## Refuted or deferred (from the lanes)

- Stopping deny-overrides at a Permit: refuted (a later Deny overrides; PF5).
- Reordering policies or rules: refuted for ordered algorithms and observable effects (PF5).
- Snapshot store for `decide`: no effect after warm-up (reference cache; harness D7).
- Random-number frames: 0.08% of CPU after group 1 (PF4 H9); the skip list stays a cycle E
  item for its allocation, not for randomness.
