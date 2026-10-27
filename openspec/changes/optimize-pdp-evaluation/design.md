## Context

- `DefaultPolicyDecisionPointContextFactory` holds `private final static Random RND`
  and names each `decide` call `Long.toHexString(RND.nextLong())`
  (`xacml-pdp/src/main/java/org/xacml4j/v30/pdp/PolicyDecisionPointContextFactory.java:68,111`).
- `DefaultPolicyDecisionPoint.requestDecision` calls
  `MDCSupport.setXacmlRequestId(context.getCorrelationId(), request)` for each
  individual decision (`xacml-pdp/src/main/java/org/xacml4j/v30/pdp/DefaultPolicyDecisionPoint.java:119`),
  and `MDCSupport` appends a second draw of a shared static `Random`
  (`xacml-pdp/src/main/java/org/xacml4j/v30/pdp/MDCSupport.java:49,54-58`). The
  `RequestContext` argument is unused.
- `java.util.Random.nextLong` makes two compare-and-set updates of one
  `AtomicLong` seed, so concurrent `decide` calls contend on two shared seeds.
- A `PolicyDecisionPointContext` is used by the thread that handles its
  request only (`xacml-pdp/src/main/java/org/xacml4j/v30/pdp/request/MultipleResourcesBudgetHandler.java:145-151`);
  all handlers call `requestDecision` in sequence.
- No code outside `DefaultPolicyDecisionPoint` calls `setXacmlRequestId`
  (`rtk rg`, 2026-10-26).

## Goals / Non-Goals

**Goals:**

- No random number generator on the decide path; reproducible identifiers.
- Measured decide time and allocation for every optimization group, against a
  baseline built from the unchanged sources.

**Non-Goals:**

- Changes of XACML decisions, of the conformance manifest, or of the
  vendored fixtures.
- The regular-expression engine SPI (its own change, `add-regex-engine-spi`).
- Removing the MDC keys or the correlation id (a later group may make them
  cheaper; this group keeps them).

## Research Basis

`research/README.md`: NOT REQUIRED for group 1. Later groups add their
questions there.

## Decisions

### D1. Correlation id from a per-PDP sequence

`DefaultPolicyDecisionPointContextFactory` gets a `final String` prefix, set
once in its constructor to `Long.toHexString(System.currentTimeMillis())`, and
an `AtomicLong` sequence. `createContext` names the call
`prefix + "-" + Long.toHexString(sequence.incrementAndGet())`. One
`getAndAdd`-style atomic increment replaces two compare-and-set loops; two
PDPs built in the same millisecond share a prefix, and the `PpId` MDC key
tells them apart. Alternatives: `ThreadLocalRandom` (still random, not
reproducible); `UUID.randomUUID()` (random, and slower); a thread-local
counter (needs the thread id in the name to stay unique, and a thread-local
lookup per call).

### D2. Request id numbered within one `decide` call

`DefaultPolicyDecisionPoint.decide` passes a small per-call callback object
to `factory.createContext` instead of the bound method reference
`this::requestDecision`. The object counts the individual decisions of the
call (a plain `int`; one thread uses it) and passes the number to
`requestDecision`, which calls `MDCSupport.setXacmlRequestId(correlationId, n)`.
The per-call allocation stays one object, as the bound method reference
allocated one before.

### D3. `MDCSupport.setXacmlRequestId(String, int)`

The method takes the correlation id and the number and puts
`correlationId + "-" + n` under `XacmlRequestId`. The `Random` field and the
unused `RequestContext` parameter go. **BREAKING** for a caller outside the
project (none is known; pre-release 2.0.0 line).

### D4. A guard against random sources

The ast-grep rule `java-no-random-on-decide-path` (severity error) matches
`new Random($$$)`, `new SecureRandom($$$)`, `ThreadLocalRandom.current()`,
`Math.random()`, and `UUID.randomUUID()` in
`xacml-pdp/src/main/java/org/xacml4j/v30/pdp/**/*.java`, with a rule test of
valid and invalid cases (AGENTS.md, ast-grep).

### D5. Cycle A: one bag per request attribute and decision

`DefaultEvaluationContextHandler` (one per individual decision,
`PolicyDecisionPointContextFactory.java:176`) keeps a decision-scoped memo of
request-resolved designator bags, keyed by `AttributeDesignatorKey`, filled only
with bags found in the request (present results), consulted after the PIP-result
map and before the request lookup. PIP results keep their map
(`designatorCache`), so `getResolvedDesignators()` and the response's resolved
attributes do not change. No memo for exceptions or for PIP absence. Basis:
XACML 3.0 §7.3.5 (every later test of an attribute uses the bag first tested;
research `synthesis.md` cycle A). Alternatives: memo inside `Entity` (shared by
decisions of a multi-request, harder to scope); caching absent results (changes
PIP call counts). Selector memo: not in this cycle (backlog, synthesis).

Concurrency contract (review RVP): one handler belongs to one individual
decision, but a PIP receives the `EvaluationContext` and may call back, also
from another thread. The memo is a `ConcurrentHashMap`; two concurrent first
resolutions of one key may both search the request, and both return the bag
stored first (`putIfAbsent`); there is no single-flight lock, so a PIP callback
cannot deadlock. The requirement is equal bags for every test of an attribute
(§7.3.5); the same instance and one request search are implementation checks
of the single-threaded case. Not covered and kept as today: a PIP absence
followed by a later value in the same decision (§7.3.5 gap, bug
`policy4j-6vhd`).

### D6. Cycle B: loops instead of streams on the attribute lookup path

`RequestContext.getEntity`, `AttributeIndex.findByIssuer` and `findValues`,
`Attribute.getValuesByType`: index loops on private or package-private paths;
the public `findByIssuer` and `getValuesByType` keep returning new modifiable
lists (no public overload is added); `findValues` returns the attribute's own
immutable list when one attribute supplies every matching value, otherwise an
`ImmutableList` as before. Same values, same order, same public collection
behavior (review RVP).

### D7. Cycle C: bags without the Guava multiset

`BagOfValues` keeps its values in an array in today's iteration order: the
order of Guava's `ImmutableMultiset`, which groups equal values in the order of
their first occurrence (`[a, b, a]` iterates as `[a, a, b]`), so `Match` and
every bag function see the same sequence (review RVP: an insertion-order array
would change decisions in error cases, §7.6). A shared empty bag per type; a
set view only for set operations; `Match.match` iterates the array through a
package-private accessor. `values()` keeps returning a `Multiset` (built on
first call and kept) so its equality and counts stay. `AttributeReference`
builds the empty bag only on the absent path. Equality, hash, and bag and set
functions keep their results (XACML 3.0 A.3.10, A.3.11; NaN and signed zero as
today). `BagOfValues` is `Serializable` with a non-transient `Multiset` field:
any change of its fields changes the serialized form, so cycle C is
**BREAKING** for Java serialization of bags unless it keeps the field;
maintainer decision required before task 6.2 (Open Questions).

### D8. Cycle D: fixed-arity predicate calls

`Match` and `FunctionSpec` call a two-argument predicate without building a
varargs list or normalizing through `LinkedList`. Eligible: functions with
exactly two eager (non-lazy) parameters, no optional or variadic parameter,
from any provider; lazy, optional, and variadic functions keep the generic
path. With runtime parameter validation, the pair path validates the two
arguments as given (normalization returns them unchanged) and evaluates them
into a new list, so a validator never sees the list change (review rv009,
final wording after rv012). Argument order,
evaluation of operands, validation, exceptions, and status codes stay (review
RVP).

Implementation (2026-10-27): `FunctionSpec` gains a default `invoke(context, arg0, arg1)` with today's behavior (additive; a two-argument call now binds to it at compile time). `FunctionSpecImpl` overrides it when both parameters are mandatory and eager: the two arguments are the normalized list, so `normalize` is skipped and the operands are evaluated into the one list the call creates; validation at runtime, logging, and exception wrapping run as in the generic path; the default method calls the variadic form, so a custom implementation that overrides it keeps its behavior; the list handed to `FunctionInvocation` is a modifiable `ArrayList`, and with validation at runtime the operands are evaluated into a new list, because a validator may keep the one it saw (reviews rv006 to rv009). The parameter and normalized lists are `ArrayList`s instead of `LinkedList`s.

### D9. Cycle E: the fixed cost of a decision

Precomputed multiple-decision flags of the immutable `RequestContext`
(`containsRepeatingCategories`, attribute-value checks),
`getIncludeInResultAttributes` computed once, lazily created maps and deques in
`RootEvaluationContext` and `DefaultEvaluationContextHandler`, no
`ConcurrentSkipListMap` for evaluated policies, an empty fast path in
`getResolvedAttributes`, a static logger in `RootEvaluationContext`, and
`hashCode` without `Objects.hash` varargs where the fields allow it. Thread
safety is checked per structure: a structure shared across threads keeps a
concurrent type, and lazy creation publishes safely. Kept as today (review
RVP): the order of evaluated policy ids that the root context exposes (sorted
by id today), duplicate ids, the merge of advice and obligations, and the
minimum of the decision cache TTL.

Implementation (2026-10-27): nothing is cached per `RequestContext`, because the benchmarks reuse request objects and a per-request cache would overstate the gain; the checks are made cheap instead (`containsRepeatingCategories` compares the multimap's value and key counts; include-in-result is one loop that builds only when an attribute is marked; `AttributeIndex` searches are loops that still return a new modifiable list, as the streams did). Lazily created maps, caches, and stacks are published with a `VarHandle` compare-and-set, so concurrent first writes keep one structure; a missing map reads, compares, and hashes as an empty one. `getResolvedDesignators` and `getResolvedSelectors` create their cache when asked and return its unmodifiable view, so a kept view stays backed (reviews rv006 to rv008; one map per decision, as the result reads the designators); advice and obligation getters return a view that reads the response maps once they exist; the non-empty path of `getResolvedAttributes` is unchanged (its `HashMultimap` order was already unspecified). `SemanticIdentifier` computes its hash on each call without the varargs array and with null-safe field hashes, because subclasses can change its protected fields (no cache). `Attribute` gets no new accessor: its default `serialVersionUID` counts non-private methods, and `AttributeIndex` reads `values()` instead.

### D10. Cycle F: policy-level allocation

Variable map only when a policy has variables; the reference cache compared
against a plain concurrent map with the same invalidation
(`DefaultPolicyReferenceResolver` listener). Not in this cycle (review RVP):
fewer context objects per reference, because the parent chain is public
(`EvaluationContext.getParentContext`) and carries cycle detection and §7.18
obligation and advice staging; it needs its own design and, if the chain
changes, a **BREAKING** decision. The forwarding cost (PF3 H5) is measured in
the second profiling round.

Implementation (2026-10-27): the reference caches were unbounded Guava caches (the size argument was never applied), so a `ConcurrentHashMap` with the same listener invalidation keeps their behavior. A policy without variables shares the empty map, since `setVariableEvaluationResult` rejects an undefined id before writing.

### D11. Cycle G: compact object headers, measured

A JMH pair with `-XX:+UseCompactObjectHeaders` off and on, same build: a
deployment recommendation in the docs if allocation or time improves beyond
the error intervals; no code change.

### D12. Every cycle

Characterization or failing test first; then the change; then a JMH pair on
JDK 25 with C2 (`PdpDecideBenchmark` on rsa2008 and synthetic:100 with
`request=all`, synthetic:1000 with `request=all` and each of the seven request
positions, `-prof gc`, 3 forks x 5 iterations), back to back, no agent session
alive; the two jars come from snapshots that differ only in the cycle's files
(their SHA-256 in the run summary), with the same logging backend,
dependencies, JDK, and flags; a pair measures one cycle, and bundled edits are
credited to the group only; no PIP-heavy corpus exists yet (recorded as not
covered);
reactor and conformance; review by a lane from another family. A cycle that
gains less than 5% does not end the change: research swarm per the proposal.

## Test Strategy

| Requirement | Tests |
| --- | --- |
| Correlation ids without random numbers | `CorrelationIdTest` (sequence of one PDP; two PDPs number alike), written first and failing before D1; rule test of `java-no-random-on-decide-path` plus `ast-grep scan` |
| Request ids of individual decisions | `CorrelationIdTest` (one request; a test handler that asks for two individual decisions; the key is absent after `decide`) |

The tests observe ids through a `RequestContextHandler` (it reads
`getCorrelationId()`) and a `PolicyDecisionAuditor` (it reads the MDC key
while the PDP audits each individual decision). The full reactor `clean test`
on JDK 21 and the conformance suites must give the same counts as before
(8,009 tests, 0 failures, 18 skipped; 937 conformance tests, 12 skipped).

## Benchmark Plan

JMH applies (decide path). Group 1 runs `PdpDecideBenchmark` (`rsa2008`,
`synthetic:100`, `in-memory` store, `-prof gc`, `-foe true`) twice, back to
back: baseline jar built from the sources without the group 1 edits, and the
candidate jar. Both with `-t 1` and with `-t 4`, because the contention on the
shared seeds shows only with several threads. It runs after task 3.5 of
`add-pdp-jmh-benchmarks` provides the benchmarks. (Recorded 2026-10-27: the
group 1 pair ran with `-t 1` only, on JDK 21; the `-t 4` pair is task 1.4,
on JDK 25.) Every run is recorded under
`evidence/benchmarks/jmh/` and in the ledger `evidence/benchmarks/jmh/LEDGER.md`.

## Code Intelligence Plan

- `rtk rg -n 'setXacmlRequestId|getCorrelationId'` over the repository before
  and after: only the call sites listed in Context.
- Serena `find_referencing_symbols` on `MDCSupport.setXacmlRequestId` and on
  `PolicyDecisionCallback.requestDecision`.
- `ast-grep scan` with the new rule; `ast-grep test` for its rule test.
- A `clean` build of the reactor.

## Execution Plan

Group 1 is small and touches three files and one rule; the conductor (Claude
Opus 5.5) writes it. Review lanes from other families: Codex GPT-6.1 Sol
(code) and Agy, Gemini 4 Argon (code), in the review gate of
`add-pdp-jmh-benchmarks` group 3 when sessions are running anyway. JMH before
and after: the conductor, with no agent session alive.

Later groups: each profiling round assigns the hot spots to lanes with
disjoint files (NTM, docs/ai/SWARMS.md) and reviewers from other families.

## Risks / Trade-offs

- [Log readers expect random ids] → the new form is documented in the
  proposal; ids stay unique per PDP and per call.
- [Two PDPs share a prefix] → the `PpId` MDC key separates them.
- [The JMH gain of group 1 is below noise at `-t 1`] → expected; the group is
  requested for determinism and contention, and `-t 4` shows the contention
  part. The result is reported as measured.

## Migration and Rollback

Rollback: revert the three files and the rule. No data migration.

## Open Questions

1. Cycle C (D7): accept a **BREAKING** change of the Java serialized form of
   `BagOfValues`, or keep the `Multiset` field for compatibility (then less
   allocation is saved)? Task 6.2 waits for the maintainer.
