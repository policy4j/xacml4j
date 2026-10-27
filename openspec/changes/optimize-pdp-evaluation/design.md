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

Decision (maintainer, 2026-10-27, Open Question 1): "yes and specializations
for primitive types to avoid allocation, each type should be able to create
its own version of bag with underlying storage, for primitive arrays we can
apply Java vector SIMD operations down the road". So cycle C keeps the
serialized form and the public API of `BagOfValues`, and the storage becomes
the value type's choice:

- Storage. The value type creates the storage of its bags (an additive
  `ValueType` default method; the default storage is an array of values). The
  built-in types specialize: `integer` in a `long[]`, `double` in a `double[]`
  (equality as `DoubleVal.equals`: NaN equal to NaN, `0.0` not equal to
  `-0.0`, as today's multiset grouping), `boolean` as two counts, and the
  other types as value arrays. Every storage keeps today's iteration order
  (equal values grouped in the order of their first occurrence), size, counts,
  and set view.
- No allocation on the hot path. `Match` and the bag functions ask the storage
  to apply a known built-in predicate to a literal (for example
  `integer-equal`, `integer-greater-than`, `double-less-than-or-equal`,
  `string-equal`) with primitive comparisons; any other predicate gets values
  one at a time, and a primitive storage creates them only on that generic
  path. The result per value, the order of evaluation, and the error handling
  (the recorded `policy4j-0972` behavior) are the same as the generic loop.
- Serialization. `values` keeps its name and type in the stream through
  `serialPersistentFields` and `writeObject` / `readObject` (the `Multiset`
  is built when a bag is written); a golden stream written by the current
  class must read back equal, and a bag written by the new class must read
  equal in the current class.
- Later, not in this cycle: the Vector API (`jdk.incubator.vector`, still
  incubating in JDK 25) for scans of `long[]` and `double[]` storage; the
  storage interface keeps the arrays private so that change stays internal.

- Alternative considered, Eclipse Collections (maintainer question,
  2026-10-27): release 13.0.0 (2025-06-23) has primitive lists, sets, bags,
  and maps (implementation jar 10.6 MB, API jar 1.9 MB, Maven Central). Its
  primitive bags are hash-based with an unspecified iteration order, so they
  cannot hold a bag whose order decides error cases (§7.6, review RVP); its
  collection factories are `ServiceLoader` services of the API jar, one
  implementation per JVM, so a library that registers its own would change
  them for the whole application. Bag storage therefore stays the value
  type's own ordered arrays (no dependency); an adapter to the Eclipse
  Collections primitive iterables (`LongIterable`, `DoubleIterable`) can be
  added on top, and Eclipse Collections stays a candidate wherever a hash
  structure keyed by primitives is a measured hot spot (the maintainer's rule
  of 2026-10-26: Guava collections give way to JDK, Eclipse Collections, or
  custom ones where measured).

Plan review RVC7 (Agy, Gemini 4 Argon, 2026-10-27; APPROVE WITH CHANGES,
`evidence/reviews/plan/rvc7-cycleC-agy`), applied:
- `boolean` storage keeps two counts and which value came first (`[false,
  true, false]` iterates `[false, false, true]`, `[true, false, false]`
  iterates `[true, false, false]`).
- No hot caller of a bag uses `values()`: `HigherOrderFunctions` and
  `DecidingOperands` (`any-of` and the other quantified functions, used by
  rsa2008), `BagFunctions.doubleIsIn`, `SetFunctions.double*`,
  `BagOfValues.single()`, `BaseDecisionRuleResponseExpression`, and the
  `Quantified*` classes move to indexed or storage methods; `values()` stays
  for callers that want the `Multiset`.
- Two equalities on `double` bags, both kept: storage grouping and the Java
  methods of `BagOfValues` (`equals`, `hashCode`, `contains`, `union`,
  `intersection`) use `Double.doubleToLongBits` equality (all NaNs equal,
  `0.0` and `-0.0` differ; the first occurrence's bits are stored); the XACML
  `double-*` bag and set functions use IEEE `==` (NaN never equal, the zeros
  equal), through separate storage methods.
- A primitive storage keeps the first value object it was built from, so
  `value()`, `single()`, and `one-and-only` allocate nothing; `IntegerVal.of`
  gets a small-value cache.
- `hashCode` is computed from the storage with Guava's `Multiset.hashCode`
  formula (sum over distinct values of `hash ^ count`), so it equals today's
  value without building the `Multiset`; the cached `hashCode` field stays in
  the serialized form (`serialPersistentFields`: `hashCode`, `type`,
  `values`).
- Eclipse Collections: the reviewer agrees (no dependency, no implementation
  of its interfaces); the arrays stay private to the storage for a later
  Vector API scan.

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

### D14. Cycle H: no empty bag built when an attribute is present

Found by the second profiling round (run 016): `AttributeReference.evaluate`
ends with `v.orElse(getDataType().emptyBag())`; `orElse` evaluates its
argument, so every attribute evaluation builds an empty bag (a
`LinkedHashMap`, a `LinkedHashMultiset`, a `BagOfValues`), 44.5% of the bytes
of a full scan. The fallback becomes lazy at every such site
(`AttributeReference.evaluate`, `AttributeDesignatorFunctions`), and
`ValueType.emptyBag()` and `BagOfValuesType.emptyBag()` return one immutable
empty bag per value type from a small concurrent cache (the TODO in
`BagOfValuesType`). Kept: the bag's type, equality, hash, and serialized
form; a type that cannot have bags (`DynamicType`) still throws and nothing is
cached for it. A shared instance is safe because a bag has no mutable state
other than its hash cache, which `hashCode` reads once per call (review RVH2:
the earlier two reads could return 0 under a race, JDK-8166842).

Implementation, round 2 (2026-10-27, after review RVH1: a static map kept
custom value types reachable and bypassed a custom `bagBuilder()`): no static
cache. `ValueType.emptyBag()` keeps its default, `bagBuilder().build()`; each
constant of the enum `XacmlTypes` caches the bag that its own `bagBuilder()`
built, in an instance field (a racy publication is safe: the bag's fields are
final; its lazily cached hash is read once per call, review RVH2); `BagOfValuesType.emptyBag()` returns that bag for a built-in type and
`builder().build()` for any other. An ast-grep rule (`java-no-eager-empty-bag`)
rejects an eager `orElse(...emptyBag())` in main code.

### D15. Cycle I: direct two-argument calls of built-in functions

Second profiling round (run 021; `research/pr1-opus-allocation.md` H2,
`pr2-fable-cpu.md` H2, `pr4-astra-function-calls.md` H1): after D8 a
two-argument predicate still allocates the argument `ArrayList`, its array,
and the reflective `Object[]` of `DefaultFunctionInvocation`, 41.8% of the
bytes of synthetic:1000 `all`, and `Method.invoke` with its wrappers. A
function whose `DefaultFunctionInvocation` comes from the default
`FunctionInvocationFactory` (trusted provenance, not the function id) for a
method with exactly two parameters, no `EvaluationContext` parameter, and no
varargs gets a typed binding at registration (`MethodHandle`, adapted by
`LambdaMetafactory` where the method is accessible; no binding otherwise).
`FunctionSpecImpl.invoke(context, arg0, arg1)` with an eager pair and no
runtime validation evaluates the operands in order into locals and calls the
binding: no list, no array, no reflection. Kept: the operand order, the
exception mapping (an `EvaluationException` keeps its status; any other
failure, also an `Error`, becomes the same `FunctionInvocationException` with
the same root cause), the argument type check (a wrong runtime type fails as
`IllegalArgumentException("argument type mismatch")`, as `Method.invoke`
does), custom factories and custom `FunctionInvocation`s (no binding), the
validation path, and lazy, optional, and variadic functions (generic path).

### D16. Cycle J: index loops on the policy tree

PR1 H4, PR2 H7, PR4 H3, PR5 H4 and H7: `Target`, `MatchAnyOf`, `MatchAllOf`,
the combining algorithms, and function-parameter normalization iterate with
iterator objects (`Iterators$9` alone is 13.8% of the full-scan bytes). The
private collection fields of the immutable policy elements become lists that
the element owns, iterated with an index loop; normalization indexes the
lists it can read exactly as their iterator would (immutable lists and the
`Arrays.asList` views of the varargs path) and copies any other caller list
once through its iterator (it reads every argument anyway; a copy-on-write
list keeps its snapshot, review RCJ2). The combining algorithms keep their iterators (review RCJ1): their
list comes from the caller of a public API, a `CopyOnWriteArrayList` is
combined as its iterator's snapshot, and a lazy list is read only as far as the
combination goes; profile 021 shows no combiner iterator among the per-policy
allocations of the full scan. Kept: order and short-circuit
(XACML 3.0 §7.7, §7.11 to §7.13), the public getters and their types, and the
`policy4j-yeu3` and `policy4j-0972` behaviors.

### D17. Cycle K: context queries in one loop, one `Optional` per attribute

PR1 H6, PR2 H3, PR5 H5: every attribute lookup and every
`isValidateFuncParamsAtRuntime` or `getCurrent*` query hops through each
`DescendantEvaluationContext` with an interface call (about 15% of the time
of rsa2008 and synthetic:1000), and each memo hit wraps the bag in a new
`Optional` (9.3% of full-scan bytes). Pure forwarders walk the `parent`
fields in one loop to the first context that is not a
`DescendantEvaluationContext` and call it once; queries that a level answers
ask that level through a package-private hook during the same walk; staging
calls (`addAdvices`, `addObligations`) keep hop-by-hop dispatch. The
designator memo stores the `Optional` that `Entity.resolve` returned, and a
hit returns it. Kept: which level answers which query, §7.18 staging, §7.8
variable scope, the public `EvaluationContext` API, and a foreign context in
the chain (the loop stops there and calls it).

As built (task 15.2):

- Walked to the first context that is not one of this package's contexts, in
  one call: `resolve` (both overloads), `resolveEntity`,
  `isValidateFuncParamsAtRuntime` and its setter, `getClock`, `getTimeZone`,
  `getCurrentDateTime`, `addEvaluationResult`, `getEvaluatedPolicies`, the
  decision cache TTL methods, `getResolvedDesignators`, `getResolvedSelectors`,
  `getMatchingObligations`, `getMatchingAdvices`, and the per-rule status
  methods. No subclass overrode any of them.
- Answered by the nearest level of the answering class, found by a type test
  per level, not a call per level: `getCurrentRule` (rule context),
  `getCurrentPolicy` (policy context), `getCurrentPolicySet` (policy set
  context), `getCurrentPolicyIDReference` and
  `getCurrentPolicySetIDReference` (reference contexts). The five classes
  give their rule, policy, or reference through a package-private final
  accessor instead of an override.
- Compatibility (review RCK1): the methods keep HEAD's modifiers (only
  `resolve(AttributeReferenceKey)` is final), because a final method breaks a
  compiled subclass that overrides it (JLS 13.4.17), and
  `Rule.RuleEvaluationContext` is public and non-final. The walks pass only
  through this package's own contexts: any other `EvaluationContext`, or a
  subclass of `Rule.RuleEvaluationContext` declared elsewhere, stops the walk
  and is called, so its overrides answer as at HEAD. `Rule.isOwnContext` tests
  the exact classes `RuleEvaluationContext` and
  `Rule.ExtendedIndeterminateRuleContext` (the former anonymous extended
  Indeterminate rule context, now named). No context class of the package
  overrides a walked method.
- Kept hop by hop: the per-level evaluation status, `addAdvices` and
  `addObligations` (staging), variables and `getXPathVersion` (answered
  conditionally by policy and policy set contexts, not on the profiles),
  `getParentContext`, and the extended Indeterminate contexts.
- `isEnclosed` keeps HEAD's `getParentContext()` walk (review RCK1: a
  subclass of the public rule context can override `getParentContext()`).
- The handler's request memo keeps the `Optional` of `Entity.resolve`;
  information point results still come from the resolved-attributes map,
  wrapped per hit.
- Tests: `ContextChainQueryCharacterizationTest` (green before and after:
  answers per level, the staging levels in the chain, a foreign context above
  the tree and inside the chain called once per forwarded query, settings
  written at the root),
  `DefaultEvaluationContextHandlerTest.repeatedRequestDesignatorGivesTheSameOptional`
  (red before, `evidence/tests/033-cycleK-red`), and, after review RCK1,
  `ExternalRuleContextCompatibilityTest` (outside the package: HEAD's final
  set, an external rule context's overrides, staging through its
  `getParentContext()`) (`evidence/tests/035-rck1-fix`). The class-scan
  inventory test and the class-name chain assertion were removed in the
  batch-2 clean-code review (they froze private structure).

### D18. Cycle L: guarded debug logging on the per-policy path

PR2 H4: `isDebugEnabled()` runs Logback's turbo-filter check on each call;
unguarded multi-argument `debug` calls also build their arguments
(`AttributeReference.java:109` computes `getReferenceKey()`, `Entity.resolve`
a varargs array). Every debug call on the per-policy and per-predicate path
whose arguments cost more than a field read gets an `isDebugEnabled()` guard,
and a method checks the level once when it logs several lines. No line is
removed and no level is cached across calls; removing per-element lines or
snapshotting the level per decision changes what a debug log shows and is
Open Question 2.

Decision (maintainer, 2026-10-27, Open Question 2: "yes"): the per-element debug
lines of the per-policy and per-predicate path (`Target`, `MatchAnyOf`,
`MatchAllOf`, `Match`, `AttributeReference`, `FunctionSpecImpl`,
`DefaultFunctionInvocation`, `FunctionInvocationFactory`, `Entity.resolve`,
`PolicyIDReference`, `BaseCompositeDecisionRule`) are removed; the PDP logs
one debug line per decision (request id, decision, number of policies
evaluated); where a per-decision line needs the level, it is read once per
decision. MDC handling is unchanged.

As built (batch 2, Claude Opus and Fable; review Codex Sol): the listed classes
plus `RootEvaluationContext`, `DefaultPolicyReferenceResolver` (which keeps one
guarded line for a reference that matches nothing, its only trace), the request
callback of `PolicyDecisionPointContextFactory`, and the failure helper of
`DefaultEvaluationContextHandler` (its WARN lines for cyclic references stay).
`DefaultPolicyDecisionPoint` logs one guarded line per decision, and one for a
decision-cache hit. Failure-path debug lines went too; the result's status
still carries the error. No test pins log output (maintainer, 2026-10-27: a
logging test "does not make any sense").

### D19. Cycle M: obligation and advice staging only when there is something to stage

PR1 H5, PR5 H6: each composite rule creates `StagedResponses` with four
`ArrayList`s, and `evaluateObligations` and `evaluateAdvices` build a
`LinkedList` on every call, about 13% of rsa2008 bytes. Staging lists are
created on the first add, empty collections are not forwarded, and an
expression list with no applicable expression returns an empty immutable list.
Kept: what is published, in which order, at which level (§7.18), and the
staging behavior pinned by `evidence/tests/009-cycleE-characterization-green`.
A custom `EvaluationContext` no longer receives calls with empty collections
(recorded in the review packet).

### D20. Cycle N: cheap exceptions on the function error path

PR1 H3, PR2 H8, PR4 H2: a failing built-in call on rsa2008 creates up to four
throwables with full stack traces (`Throwable.<init>` 14.6% of rsa2008 CPU,
throwables 7% of its bytes). Maintainer decision (2026-10-27, Open Question 3:
"optimize"): (a) the reflective factory rethrows a `RuntimeException` cause of
`InvocationTargetException` without wrapping it (errors and checked exceptions
are wrapped as today; bound calls of D15 already have no wrapper); (b) the
`FunctionInvocationException` that `DefaultFunctionInvocation` creates has no
stack trace; (c) engine exceptions (`CoreException` and its subclasses) fill
no stack trace unless the system property `xacml4j.exceptions.stackTrace` is
`true`; `CoreException` pins its current default `serialVersionUID` so the
override keeps its serialized form; (d) `one-and-only` and the other bag
functions report a wrong bag size with the same status and message without a
`Preconditions` exception. Kept: decisions, status codes, status messages,
the class, message, and cause chain of `Status.getError()` (only its stack
trace is empty by default), and XACML A.3.10 and §7.19.3.

As built (batch 2, Codex Astra; review Claude Opus, APPROVE WITH CHANGES applied):
only (c). `CoreException.fillInStackTrace` returns without a trace unless the JVM
starts with `-Dxacml4j.exceptions.stackTrace=true` (read once); this also covers
(b), since the wrapper is a `CoreException`. Not done: (a), because the
unwrapped cause changes the status message and error chain that
`FunctionSpecImpl` builds; (d), because the error of `Status.getError()` must
stay an `IllegalArgumentException` with its message, and that exception fills
its own stack trace. Both conflict with "Kept" above; they need a maintainer
decision to go further. `CoreException` already declared its
`serialVersionUID`, so the added method keeps the serialized form.

Decision (maintainer, 2026-10-27, batch 3): do (d), skip (a). `Status.Builder.error`
is a no-op at commit `21858796`, so `Status.getError()` is always empty and the
exception of a wrong bag size never reaches a caller; `one-and-only` and the other
bag functions throw a package-private `IllegalArgumentException` subclass with the
same message and no stack trace. (a) stays undone.

## Batch 3 (designs of 2026-10-27; swarm drafts, maintainer decisions recorded)

Sources: `research/pr7-fable-literature.md`, `research/pr8-sol-next-cycles.md`, and
profile 021 (before batch 2). Every share below is expected, not measured; batch 2
lowered the costs these shares were taken from.

### D21. Cycle O: canonical designator keys (draft Codex Sol)

PR2 H6, PR8 rank 1: `AttributeDesignatorKey.equals` is 5.72% of synthetic:1000 CPU
(3.8% in `String.equals`). `AttributeDesignatorKey.Builder.build()` validates the
candidate as today and returns the live representative of equal keys from Guava's
weak interner (`Interners.newWeakInterner()`: concurrent, no global lock, keys held
weakly; the maintainer rejected a hand-rolled `WeakHashMap` pool). The builder factories,
`toBuilder()`, and `withIssuer()` use it; the XML mappers already build keys through
the builder. The public constructor stays and may give non-canonical keys; a key
without a category bypasses the pool. `equals` keeps its identity check and full
fallback.

Amended (maintainer, 2026-10-27: "Create Interner SPI for plugable reference
intering"): interning is a pluggable SPI, public API in `org.xacml4j.v30`:
`AttributeReferenceKeyInterner`, a `@FunctionalInterface` with
`AttributeReferenceKey intern(AttributeReferenceKey key)` (thread-safe, returns the key
or an equal key of the same class), and the static factories `weak()` (Guava's weak
interner, a method reference, the default), `none()` (`key -> key`), and `get()` (the
interner in use). The interner in use is the one implementation `ServiceLoader` finds,
else `weak()`, resolved once into a package-private field of `AttributeReferenceKey`;
two registered implementations fail loudly at the first key build. The builders of
`AttributeDesignatorKey` and `AttributeSelectorKey` intern every key they build; both
classes compare categories null-safely, so a key without a category needs no bypass.
An implementation registers with `@AutoService`. As built after code review RSO
(Claude Opus): the first version's generic method, null-category bypasses, and holder
class failed with any non-default interner on category-less selector keys; selector
fields are final.

Kept: category, type, id, and issuer
matching (§7.3.4, §7.3.5, §7.20), MustBePresent and missing-attribute statuses, hash
values, serialized form, public signatures; canonical identity is not a public
guarantee. No public API change.

### D22. Cycle P: results and structural hashes without temporary arrays (draft Codex Sol)

PR1 H8, H9, PR8 rank 2. `Result.Builder` starts its obligation and advice maps as
shared immutable empty maps and creates a `LinkedHashMap` on the first element;
built results stay immutable snapshots. The varargs hashes of `Result`, `Status`,
`StatusCode`, `BaseDecisionRule`, `BaseCompositeDecisionRule`, `Rule`, `Policy`,
and `PolicySet` become explicit folds (`31 * h + Objects.hashCode(field)`, the same
seed and field order), so every hash value stays. Decision (maintainer, 2026-10-27):
`Result.ok` shares one private immutable OK `Status`; `Status.ok()` still returns a
builder. Kept: decisions, statuses (§7.17, §7.19), obligation and advice order and
merge (§7.18), hash values, serialized forms, public signatures. Expected: about
7.3% of first-deny bytes, 1.5% of rsa2008 bytes.

### D23. Cycle Q: one-operand bindings, and built-in calls without reflection (draft Claude Opus)

The maintainer asked whether MethodType and MethodHandles make sense for optimized
invocations: D15 already uses them; D23 extends them. Profile 021 (rsa2008): the
list path under `Apply` and `FunctionReference` holds 17.0% of the bytes and 13.0%
of the CPU; the reflective `InvocationTargetException` and the factory's wrapper
take 7.3% of the CPU. synthetic:1000 calls only `Match` pairs (D15).

- (a) A built-in with one parameter that meets D15's conditions gets a
  `LambdaMetafactory` adapter `Unary` beside `Binary`, in D15's cache (46 built-ins;
  rsa2008 uses `string-one-and-only` and `not`).
- (b) `FunctionSpecImpl` overrides `invoke(context, Expression...)`: one operand of an
  eager bound one-parameter function without runtime validation goes to the binding
  through a new public `DefaultFunctionInvocation.invokeDirect(FunctionSpec,
  EvaluationContext, Expression)` (decision below); two operands take D8's pair path
  with D15's binding; any other call takes the list path. `Apply` keeps its varargs call
  (code review RD23S, Codex Sol: a custom `FunctionSpec` that overrides both overloads
  must keep receiving the varargs call); `FunctionSpecImpl`'s varargs override routes
  the built-in pairs.
- (c) A default-factory static method without a binding (variadic, lazy, with the
  context, or three or more operands) is called through a spreading `MethodHandle`
  (`unreflect`, `asType`, `asSpreader`) instead of `Method.invoke` when every argument
  is null or of its parameter type; otherwise the factory's invocation runs and fails
  as before. Its success path costs what `Method.invoke` costs; a failure no longer
  builds the two reflective throwables.

Kept: results; operand evaluation once each, in order, and the lazy functions' order
and short-circuit (§7.4, A.3.5, A.3.12); exception mapping (an `EvaluationException`
keeps its status, any other failure becomes the same `FunctionInvocationException`
with the same status code and message; a wrong argument type fails as
`IllegalArgumentException("argument type mismatch")`); runtime validation; custom
factories, invocations, specs, instance providers, and other class loaders (no
binding, no handle); the interfaces `FunctionSpec`, `FunctionInvocation`,
`FunctionInvocationFactory`. Decisions (maintainer, 2026-10-27): no
`FunctionSpec.invokeUnary`; the one public method on `DefaultFunctionInvocation`, as
D15 added the two-operand one.

### D24. Cycle R: a restricted target index for policy-set children and policy rules (draft Claude Fable)

PR6 H1, PR7 1 to 3, PR8 rank 5: a policy set evaluates every child per request; on
synthetic:1000 `Target.match` is 72.7% of the CPU and the per-child contexts 32.8% of
the bytes. An opted-in PDP keeps, per policy set it evaluates, an immutable snapshot of
its prepared children (D25) with an index of the children whose target is decided by
one string equality, and gives the combining algorithm only the children that can
apply, in declaration order.

Revised after plan review RPA (Codex Astra, REJECT: key prefetch through the PIP,
re-resolution of failed references, separate listeners; all three confirmed in the
code) by Claude Fable:

- Key values from the request only: `EvaluationContext.resolveEntity(category)` and
  `Entity.resolve(...)`, never `resolve(key)` and never the information point. A key the
  request does not carry evaluates the original list. A key the request carries is the
  bag every `Match` of that key sees, so the index adds no PIP call, resolved
  attribute, failure, or status.
- Reference children only from D25's bindings: the index classifies the target the
  resolver returned and `isReferenceTo` accepted, and the combining algorithm evaluates
  that same bound entry. A reference whose binding fails, throws, or does not match
  stays an `Any` entry on today's `createContext` path, so §7.15 errors are per decision
  as today. A reference listed twice is two entries.
- One generation contract (with D25): one registry per PDP (`PreparedPolicySets`) owns
  the generation and immutable per-set snapshots; one listener per PDP runs, in order,
  the resolver's cache cleanup (D25's generation-checked refill), then the registry's
  increment and clear. A build reads the generation, resolves through the resolver, and
  publishes with `putIfAbsent` only while the generation is unchanged; an evaluation
  that started with a snapshot finishes with it; a bound entry of an old generation
  falls back to today's path for that call.
- Eligible child, exact classes: `Target`, its first `MatchAnyOf`, every `MatchAllOf`,
  their first `Match` over an `AttributeDesignator` are exactly those engine classes
  (a subclass goes to `Any`), `MustBePresent="false"`, one key shared by the set, and
  the predicate is the default `string-equal`: its id plus two `FunctionSpec.scan`
  probes at build time (MATCH for `"a"` in `["a"]`, NO_MATCH for `"a"` in `["A"]`),
  which only the direct default binding of `EqualityPredicates.equals(StringVal,
  StringVal)` answers; with runtime validation on, the set is not indexed. With
  today's error order (`policy4j-yeu3`, `policy4j-0972`), a child whose indexed
  equalities are all No-match is Not-applicable with no observable effect.
- Candidates: the union of the buckets of the request bag's values and `Any`, in
  ascending position, handed to the unchanged combining algorithm.
- Algorithms by implementation class: a built-in class of
  `org.xacml4j.v30.policy.combine.impl` (not a subclass) other than
  `OnPermitApplySecondPolicyCombiningAlgorithm`; anything else evaluates the original
  list.
- Plumbing: `PolicySet.combineDecisions` reaches the registry through the root context
  (the D17 walk); a foreign root, or a PDP without the opt-in, evaluates the original
  list.
- Step 2: the same selection over the rules in `Policy.combineDecisions` (no bindings).

Decisions (maintainer, 2026-10-27): opt-in `PolicyDecisionPointBuilder.targetIndex()`,
off by default; reference children through D25's bindings; the rule step in this cycle;
measured with the index on and off as a `PdpDecideBenchmark` parameter plus the batch
pair; after the revision: additive public API in xacml-core, `public final class
PreparedPolicySets` (`create()`, `invalidate()`) and one public `RootEvaluationContext`
constructor taking it (a package-private factory cannot be reached from
`org.xacml4j.v30.pdp`), the registry's internals package-private; algorithm
eligibility by implementation class; provenance by the `scan` probes, no new
`FunctionSpec` method. Kept: §7.3.5; §7.6, §7.7 as the engine evaluates them today;
§7.11 to §7.14 and Annex C order and short-circuits; §7.15 reference errors per
decision; §7.18 staging; §5.29; §5.42; no PIP call, resolved attribute, failure, or
status that linear evaluation would not produce; custom targets, functions,
algorithms, and contexts evaluate the original list. Changed and documented: the
resolver resolves a reference child once per snapshot instead of once per decision.
Proof: `TargetIndexDifferentialTest`, every conformance and rsa2008 request through two
PDPs over independent equivalent repositories and PIPs, index on and off; per result
`Result.equals` plus what it omits (the status error's class and message chain, the
iteration order of obligations, advice, policy ids, and attribute assignments).
Expected: synthetic:1000 keeps the gain (every request carries the key); rsa2008 small
to none (its targets key on attributes its requests rarely carry).

### D25. Cycle S: bound policy-reference resolution (draft Codex Astra)

PR8 rank 4: on profile 021 the resolver probe and the reference match of each
reference child were 6.6% and 1.7% of a non-indexed synthetic:1000 scan (nothing
comparable on rsa2008). D24 removes them for every skipped child, so D25 pays only for
the reference children D24 evaluates (index off, the `Any` bucket, other algorithm
ids). Decisions (maintainer, 2026-10-27): internal only, no public SPI; fix the
late-refill race.

As revised after plan review RPF (Claude Fable, APPROVE WITH CHANGES), one mechanism
with D24:

- D24's per-PDP registry (`PreparedPolicySets`, package-private in
  `org.xacml4j.v30.policy`, identity-keyed, with an `AtomicLong` generation) holds per
  policy set one prepared child list in declaration order (`PreparedPolicySet`: the
  children and, with the opt-in, D24's index). A reference child is a package-private
  `BoundReference` that keeps a volatile link (target and generation): its
  `createContext` binds once per generation (the context's resolver, then the reference
  match, once per bind instead of once per decision) and builds the same reference and
  policy contexts as today through a package-private factory of the reference class;
  its `evaluate` reads the target from the context, so a rebind cannot pair a context
  with another target. A bind that a generation change overlapped is used once and
  not kept. `PolicySet.combineDecisions` fetches the prepared list once per evaluation
  of the set; `getDecisions()` keeps returning the original children. The prepared list
  exists for every default PDP; only the index depends on `targetIndex()`.
- The resolver keeps its reference cache and stays the only caller of the repository.
  Race fix: each listener callback and `clearRefCache()` increment a generation first
  and then remove or clear; `resolve` reads the generation before the repository call
  and stores with `compute(ref, (k, cached) -> generation.get() == before ? found :
  cached)`. The PDP registers the registry's listener after the resolver's, and the
  repository dispatches callbacks in registration order. The test that preserves the
  race is replaced by one that pins the fix.

Kept: version selection; missing references per §7.15, and cyclic references as the
engine reports them today; §7.14 and Annex C; §7.18 staging; context ancestry and
`getReferencedCompositeDecisionRules`; policy-id reporting; custom contexts and
combining algorithms (they get the original children). Not in D25: lazy reference
contexts (PR5), which change the public parent chain (**BREAKING**, its own decision).
D25 is implemented with or after D24's registry (task 23.2).

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

Changed by the maintainer on 2026-10-27: "Dont do meassurments with each
change only after changes are complete". From cycle H on, cycles are measured
in batches: the batch's cycles are implemented, tested, and reviewed one by
one, then one JMH pair compares the commit before the batch with the batch;
the gain is credited to the batch, and a profiling round follows it.

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

1. ANSWERED 2026-10-27 (see D7). Cycle C (D7): accept a **BREAKING** change of the Java serialized form of
   `BagOfValues`, or keep the `Multiset` field for compatibility (then less
   allocation is saved)? Task 6.2 waits for the maintainer. The second
   profiling round found a third route (`research/pr1-opus-allocation.md` H1,
   `pr2-fable-cpu.md` H1): keep the `values` field, its type, and the
   `serialVersionUID`, build the `Multiset` on demand, and iterate a transient
   array in today's order; a golden-bytes test written by the current class
   would prove the stream unchanged, so it needs no BREAKING change.
2. ANSWERED 2026-10-27, yes (see D18). Cycle L (D18): may per-element debug lines on the per-policy path be
   removed and replaced by one line per decision, and may the debug level be
   read once per decision? Either changes what a debug log shows. Until
   decided, cycle L only adds guards.
3. ANSWERED 2026-10-27, optimize (see D20). Exceptions on built-in function errors (PR1 H3, PR2 H8, PR4 H2): built
   without stack traces, `Status.getError()` would lose the trace for a
   built-in function failure (about 15% of rsa2008 CPU). Removing only the
   reflective wrappers (`InvocationTargetException`, the factory's
   `RuntimeException`) keeps the trace; cycle I does that for bound calls.
