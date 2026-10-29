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

### D24/D25. Cycles R and S: prepared policy sets, a restricted target index, and one resolution snapshot per decision (Claude Opus, round 5)

**History.**
- D24 (target index) and D25 (bound references) were drafted separately and rejected in plan review (RPA, RPA2).
- Claude Fable merged them over rounds 3 and 4. Round 4 was rejected in RPA3 (Codex Astra):
  - An `Error` from an unvisited child escaped.
  - A target that does not match its reference stayed in the resolver cache.
  - One generation was not one repository view per decision.
  - The resolver constructor that took the callback would have to be public.
- Round 5 follows the maintainer's decisions of 2026-10-27: "Snapshot per decision" and "Find another wiring".

**Decisions that stay:**
- Eager preparation.
- `PreparedPolicySets.create(boolean targetIndex)` as the one public factory.
- A pinned reference evaluates its target directly.
- `targetIndex()` is opt-in and off by default.
- Algorithm eligibility is by exact built-in class.
- Key values come from the request only.
- `string-equal` provenance is proven by two `scan` probes.
- JDK and Guava only.
- Minimal public API.

Profile 026: the policy and reference contexts of the children are 95% of synthetic:1000 bytes. This design removes
them for skipped children, and removes the resolver probe and the `isReferenceTo` match for pinned children.

**Round 5a.** Plan review PR5 (Agy Gemini 4 Argon): APPROVE WITH CHANGES. These changes are applied below:
- P1-1: a pin published by another decision is honored only when this decision recorded no miss for the reference,
  and the index filters only when the decision has no misses.
- P2-1: a pinned visit builds its context from the pinned target, with no map probe.
- P2-2: a `Policy` is prepared only when the index is on.
- P3-1: the `scan` probes take the live context, and the baseline factory keeps today's resolver.

**The model.** A PDP keeps one generation per repository state. A generation holds:
- the first matching target of every reference resolved in it;
- one immutable prepared list per policy set evaluated in it, built at that set's first evaluation.

A decision holds one generation, plus its own record of every other reference outcome. With the opt-in, an index
over the children whose target is decided by one string equality on a request attribute filters the list. Built-in
combining algorithms receive the prepared list.

#### The resolution snapshot

- **Where it lives.** Every reference resolution of a decision reaches `RootEvaluationContext.resolve`. The
  descendants call `top.resolve` (`DescendantEvaluationContext.java:232-236`); the root's method is final (`:374`).
  So the snapshot is in the root, and it covers prepared, cold, nested, deferred, and revisited positions, and
  custom algorithms too.
- **Generation `targets`.** A `ConcurrentHashMap<CompositeDecisionRuleIDReference, CompositeDecisionRule>` of
  resolutions that succeeded with `ref.isReferenceTo(target)`.
  - Keys use reference equality. A `PolicyIDReference` never equals a `PolicySetIDReference`
    (`PolicyIDReference.java:123`, `PolicySetIDReference.java:132`).
- **Decision `misses`.** In the root context, created on first use. It maps a reference to its first outcome that
  is not a matching target:
  - no policy;
  - a failure, meaning a `RuntimeException` or `Error` from the resolver, including a `PolicyResolutionException`
    the resolver throws;
  - a target that does not match.
- **`resolve(ref)` when the root holds a generation:**
  1. An outcome in `misses` is replayed:
     - No policy throws a new `PolicyResolutionException(this)`, as each lookup does today (`:386-402`).
     - A failure rethrows the recorded object.
     - A non-matching target is returned, and `ref.evaluate` gives Indeterminate as today
       (`PolicyIDReference.java:88-97`).
  2. A target in `targets` is returned.
  3. Otherwise the resolver is asked once.
     - A matching target goes into `targets` with `putIfAbsent`, and the winner is returned.
     - Every other outcome goes into `misses` and is replayed as in step 1.
- **The meaning.**
  - Within a generation, the first matching target of a reference is the answer for every decision that holds that
    generation.
  - Within a decision, every other first outcome is the answer to every later resolution of that reference. That
    includes the second pass of the extended-Indeterminate evaluation, which reruns `combineDecisions`
    (`BaseCompositeDecisionRule.java:189-194`).
  - A miss or a failure is kept for its decision only; the next decision asks again.
- **A repository change during a decision.**
  - The change starts a new generation for later decisions. The running decision keeps its generation.
  - Every reference it already resolved keeps its answer.
  - A reference it resolves for the first time after the change sees the repository as it then is, and keeps that
    answer for the rest of the decision.
  - No reference has two answers within one decision. Today, one reference can resolve to two states in one
    decision.
- **Not provided: one atomic view across different references.** Two references with different version constraints
  to one policy id can see the states before and after a change that falls between their first resolutions.
  `PolicyRepository` has no snapshot or versioned read. Closing this would need a repository API, or evaluating the
  decision again, and this design adds neither.

#### Wiring without a new resolver constructor

- **The factory.** `DefaultPolicyDecisionPointContextFactory` has a package-private constructor and is built only
  by `PolicyDecisionPointBuilder.java:152`. It:
  1. builds `prepared = PreparedPolicySets.create(targetIndex)`;
  2. registers one listener of its own with the repository: a package-private class in `org.xacml4j.v30.pdp` whose
     four methods (`PolicyRepositoryListener.java:30-33`) call `prepared.invalidate()`;
  3. builds the resolver with the existing public constructor and its cache off,
     `new DefaultPolicyReferenceResolver(repository, false, 1024)` (`DefaultPolicyReferenceResolver.java:63-72`).
     The generation's `targets` replaces that cache.
- **Why this is the simplest exact choice.**
  - With the resolver cache off, the generation is the only state that outlives a decision, and one listener
    empties it.
  - There is no second cache whose cleanup must come before the invalidation. So the order of the two listeners
    does not matter on any executor; each listener is its own executor task (`AbstractPolicyRepository.java:167-193`).
  - A lookup that started before an event writes into the generation its decision holds, which the event retires.
    There is no late refill into the new generation.
  - A decision that starts between an event and the listener's task holds the old generation, but resolves against
    the changed repository. It stays consistent per reference, and the generation retires when the task runs. Today's
    resolver cache has the same window.
- **Why not the alternatives.** Each needs more:
  - A hook on the resolver: a new public constructor (rejected), or a protected method reached through a subclass in
    another package, which is also new API.
  - Two listeners ordered by the executor: not exact on a multi-threaded executor.

#### The resolver's cache rule (RPA3, P2 on the mismatch)

`DefaultPolicyReferenceResolver` keeps a looked-up target in its cache only when `ref.isReferenceTo(target)`
(`:87-90`, `:112-115`). A non-matching target is still returned, but the next lookup asks the repository again. The
PDP runs its resolver with the cache off, so this matters only to other users of the class. It is documented as a
behavior change of the class.

#### Eager preparation, simplified

- **The walk.** At a set's first evaluation in a generation, `PolicySet.combineDecisions` becomes
  `combiningAlgorithm.combine(context, Prepared.children(context, this))`. `Prepared.prepare` walks `decisionRules`
  once:
  - An inline child stays itself.
  - A reference child is resolved through the snapshot by a package-private `RootEvaluationContext.outcome(ref)`:
    steps 1 to 3 above, catching `RuntimeException` and `Error` into `misses`. It builds no context and throws
    nothing.
  - A matching target gives a `PinnedReference(ref, target)` entry. Every other position keeps the original
    reference object.
  - The list is published with `putIfAbsent`; the first preparer wins.
  - With the index on, the index is built in the same walk: a pinned position by its target's `Target`, an inline
    child by its own, and every other position as `Any`.
- **`PinnedReference`.** The generation is shared by concurrent decisions, and `misses` belongs to one decision. A
  pin that another decision published is this decision's answer only when this decision has no miss for the
  reference (PR5 P1-1). So each call first asks the root `isMissed(ref)`:
  - **Not missed:**
    - `createContext(context)` is a package-private `ref.createContext(context, target)` on `PolicyIDReference`
      and `PolicySetIDReference`. It runs today's guard, builds the reference context with its cycle check, and
      returns `target.createContext(refContext)`. There is no `resolve` call and no map probe (PR5 P2-1). It is
      the same object that `resolve` would return: `targets` keeps the first matching target per generation.
    - `evaluate` and `isMatch` call the target directly (decision 3).
  - **Missed:** `createContext`, `evaluate`, and `isMatch` delegate to `ref`, which replays the recorded outcome as
    for a position that is not pinned.
  - `getId`, `getReference`, and `accept` delegate to the reference.
- **A position that is not pinned** is visited as today through `ref.createContext`, and the snapshot replays what
  preparation recorded:
  - a miss gives the same Indeterminate;
  - a failure escapes as the same object, at the same point, after the reference context;
  - a non-matching target is the same object.
- **Later decisions in the generation** use the published list. A non-pinned position resolves at its visit. The
  decision's `misses` is empty, so the lookup is live. A success there goes into `targets`.
- `Policy.combineDecisions` uses its rules as they are when the index is off: rules are inline, so preparation
  gives nothing (PR5 P2-2). With the index on, it prepares its rules the same way, to build the index.

#### Dispatch and the index (unchanged from round 4)

- **Dispatch, preparation separate from filtering.** These cases give the original `decisionRules`:
  - a foreign root;
  - a root without a registry;
  - an algorithm that is not an exact built-in class of `org.xacml4j.v30.policy.combine.impl`.

  Otherwise the algorithm gets the prepared list. It gets the candidate sublist when the index is on, the request
  carries the key, and the decision has no misses (PR5 P1-1: a pinned position is indexed by a target that a decision
  with a miss does not see). `OnPermitApplySecondPolicyCombiningAlgorithm` always gets the list unfiltered. `getDecisions()`,
  `contains`, and `accept` keep the original children.
- **The index:**
  - exact classes: `Target`, the first `MatchAnyOf`, every `MatchAllOf`, and their first `Match` over an
    `AttributeDesignator`;
  - `MustBePresent="false"`;
  - one key per set;
  - the default `string-equal`, proven by the two `FunctionSpec.scan` probes, which take the live context
    (`FunctionSpecBuilder.java:326` reads it; with runtime validation on, `scan` gives `NONE` and nothing is indexed);
  - key values from the request only (`resolveEntity`, `Entity.resolve`);
  - candidates in ascending position.

#### Public and package-private

- **Public:**
  - `PreparedPolicySets`, with `create(boolean)` and `invalidate()`;
  - one `RootEvaluationContext` constructor taking the registry;
  - `PolicyDecisionPointBuilder.targetIndex()`.
- **Package-private:**
  - `Generation` (`targets` and the prepared lists), `PreparedList`, `PinnedReference`, `TargetIndex`, `Prepared`;
  - `RootEvaluationContext.outcome(ref)`, `isMissed(ref)`, and `generation()`;
  - `createContext(context, target)` on `PolicyIDReference` and `PolicySetIDReference`;
  - the listener class in `org.xacml4j.v30.pdp`.
- **For tests:** the factory's package-private constructor without a registry gives the unprepared baseline. Its
  root contexts are built with today's constructor, and it keeps today's resolver,
  `new DefaultPolicyReferenceResolver(repository)`, with its cache on.

#### Why it is exact (the corrected argument)

**The claim.** For a request, the response and any exception that escapes `decide` equal those of the linear
evaluation over the snapshot's answers. If the repository does not change during the decision, those answers are
the ones today's live lookups give. Two facts suffice, whatever the algorithm:

1. **A visited position has the outcome the linear visit has.** That is the same target object (pinned or replayed),
   or the same miss (a new `PolicyResolutionException` at the visit), or the same failure object rethrown at the
   same point, after the same reference context and cycle check. A pin is used only when the decision has no miss
   for its reference, so it is the snapshot's answer for that decision.
2. **An unvisited position contributes nothing.** Preparation builds no context, evaluates nothing, writes no status,
   adds no policy id, and throws nothing: it records `RuntimeException` and `Error`.

**How the algorithms stop.** Each built-in algorithm decides from the outcomes of the positions it visits, in order,
and stops when those outcomes say so. By induction it visits the same positions in both paths.

| Algorithm | Stops at |
| --- | --- |
| deny-overrides (`DenyOverrides.java:73-77`) | the first Deny |
| permit-overrides, the ordered variants | the first Permit, or the first Deny for deny-overrides |
| deny-unless-permit | the first Permit |
| permit-unless-deny | the first Deny |
| first-applicable | the first decision that is not NotApplicable |
| only-one-applicable (`OnlyOneApplicablePolicyCombiningAlgorithm.java:51-66`) | the first Indeterminate target (`:55-56`) or the second match (`:58-60`), so it does not visit every child; round 4's claim that it did was wrong |
| on-permit-apply-second (`OnPermitApplySecondPolicyCombiningAlgorithm.java:63-85`) | position 0, then at most one of 1 and 2; the list is unfiltered |
| the legacy variants | as their modern counterparts |

**Consequences.**
- In each algorithm, a position after the stop may hold a failing reference. Fact 2 makes it harmless, whether the
  failure is an exception or an `Error`. In RPA3's example, first-applicable over a Permit child and then a lookup
  that throws `AssertionError` stays Permit.
- The index removes only positions whose target is NotApplicable for the request, with no side effects (the approved
  D24 conditions). Each built-in algorithm passes a NotApplicable child with no effect. only-one-applicable does not
  count it, and that target cannot be Indeterminate with `MustBePresent="false"` and a request-only key.
  on-permit-apply-second is never filtered.

**What changes:**
- The policy store's view: when and how often it is asked, including unvisited references once per set per
  generation. None of this is visible in a response.
- One answer per reference per decision, with a repository change during the decision seen per reference, as above.
- The PDP's resolver cache is replaced by the generation's map, which any event empties as a whole.
- A `VirtualMachineError` (for example `OutOfMemoryError`) during the preparation of a child that is never visited
  is recorded, not thrown. The linear path never makes that lookup.

**Kept:**
- §7.3.5.
- §7.6 and §7.7 as evaluated today (`policy4j-yeu3`, `policy4j-0972`).
- §7.11 to §7.14, and Annex C order and short-circuits.
- §7.15 per decision.
- §7.18 staging.
- Cyclic references as the engine reports them; the cycle check stays at the visit.
- No information-point call, resolved attribute, failure, or status that the linear evaluation would not produce.
- `getDecisions()`.
- Custom targets, functions, algorithms, and contexts get the original list.
- The resolver's public constructors.

**Changed and documented:**
- A set's references are resolved once at its first evaluation in a generation.
- One answer per reference per decision.
- Misses and failures are kept for their decision only.
- The resolver caches only matching targets.
- Any repository event empties the PDP's whole resolution state.

**Expected (profile 026, not measured):** on synthetic:1000, the per-child contexts of skipped children (95% of the
bytes), and the resolver probes and the `isReferenceTo` matches of pinned children. On rsa2008, small to none.

**If staging is wanted, the smallest exact subset** is the snapshot and the wiring alone:
- the generation's `targets`, the decision's `misses`, one listener, and the resolver cache off;
- it is exact, gives "Snapshot per decision", and replaces the resolver cache;
- it does not yet remove the contexts of skipped children. Preparation with pinned entries comes next, then the
  index.

### D29. Cycle T: the context root once per context, scan parameters once per binding (profile 026)

Third profiling round (run 026, the batch-3 jar): `DescendantEvaluationContext.isOwn` and
`top()` are 18% of synthetic:1000 CPU and 9% of rsa2008 (D17's walk repeats per query);
`AnnotationInvocationHandler.invoke` is 2.7% of synthetic:1000 CPU, because
`DefaultFunctionInvocation` keeps the `@XacmlFuncBagScan` proxy and calls `value()` and
`ieee()` on every scan (cycle C). (a) `parent` is final, so the first context above a
context that is not one of the package's own is fixed at construction: a final `top` field
set in the constructor (`isOwn(parent) ? parent.top : parent`) replaces the loop; every
query that called `top()` reads the field. (b) The binding stores the scan's
`BagStorage.Comparison` and its IEEE flag in final fields at construction instead of the
annotation. Kept: which context answers which query (a foreign context or an external
subclass of `Rule.RuleEvaluationContext` still stops the walk), every scan result, public
API. Expected (profile 026, not measured): most of the 18% and 9%, and the 2.7%.

## Batch 4 (concurrent attribute resolution; designs of 2026-10-27)

Sources: `research/pr9-sol-concurrent-resolution-code.md`, `pr10-astra-concurrent-resolution-jdk.md`,
`pr11-fable-concurrent-resolution-literature.md` (maintainer request: "analyze concurrent attribue
resolution strategies via selector or designator and how structured tasks can be used for high
performance parallel attribute resolution with effects etc"). Agreed facts, from primary sources
fetched 2026-10-27: `StructuredTaskScope` is preview in JDK 25, 26, 27 (JEP 505, 525, 533) and
final only in JDK 28 (JEP 543), so it cannot enter this no-preview JDK 25 build; `ScopedValue` is
final in JDK 25 (JEP 506); virtual threads are final (JEP 444) and do not pin on `synchronized` since
JDK 24 (JEP 491). Execution goes behind a package-private seam that a structured-task adapter can
replace when final.

### D26. Cycle V: parallel resolver work behind an internal runner (Agy Argon's minimal shape)

Plan review RPD26 (Agy Gemini 4 Argon, REJECT of Codex Astra's four-type `CompletionStage` SPI:
`getResolverFunction()` throwing for async descriptors, forced `noCache()` plus cancelled siblings
re-running the resolver, a per-call executor whose `close()` blocks the timeout fallback, a public
SPI that `StructuredTaskScope` could not back). Decision (maintainer, 2026-10-27): "Argon's minimal
shape now". No new public types:

- `AttributeResolverDescriptor.Builder.attribute(String id, DataType type,
  Function<ResolverContext, BagOfValues> resolver)` registers one function per attribute;
  `buildParallel()` builds a descriptor whose `getResolverFunction()` still returns an
  `Optional<AttributeSet>` (the per-attribute functions run through the PIP's runner and are
  combined into one `AttributeSet`), so `ResolverDescriptor`, `Resolver`, caching, and D27's
  `forDecision()` memo work unchanged.
- `PolicyInformationPoint.Builder`: `resolverTimeout(Duration)`, `maxConcurrentResolutions(int)`,
  `resolverExecutor(Executor)` (default: a shared virtual-thread executor owned by the PIP).
- Internal `StructuredResolverRunner` in `DefaultPolicyInformationPoint`: on JDK 25 a `Semaphore`
  permit per task released in the worker's `finally`, the shared executor, a wait up to the
  timeout, `Future.cancel(true)` on timeout and immediate fallback to the next candidate (never a
  blocking `close()` on the evaluation thread); on JDK 28 its body becomes a `StructuredTaskScope`
  with no public change. No MDC capture (MDC is removed, D34); request context reaches workers
  through the `ResolverContext` that D27 confines and prepares on the deciding thread.
- Semantics identical to synchronous resolvers: an omitted attribute is missing (fallback
  continues), a present empty bag stops fallback; timeouts, cancellation, and failures map to the
  existing "Attribute resolution failed" processing error; nothing is published from workers.

Implemented after D27 (same classes); measured with D28's `pip` corpus (`pipMode`).

### D27. Cycle W: resolution state ready for concurrency (draft Codex Sol, with Agy Argon's independent draft)

Decision (maintainer, 2026-10-27): the full design now, before any parallel resolution exists.

- Single flight and publication: each handler keeps a lazily created map of active PIP resolutions
  keyed by the full designator or selector key (`ConcurrentHashMap` of `CompletableFuture` flights;
  map callbacks only install flights, so a resolver never runs inside a map callback and the JDK
  "Recursive update" failure of a nested `computeIfAbsent` on the same map, found by Argon, cannot
  occur). One caller owns an attempt; overlapping top-level callers share its outcome (as built after
  review R-D27-sol: a caller inside a resolution of the same handler never waits for another flight;
  it runs its own attempt, whose bag still loses to the published winner and whose absence or failure
  is its own, as today); on success the owner
  publishes through the existing positive cache and returns the `putIfAbsent` winner, which also
  fixes the PIP path returning its own bag today. Flights are completed and removed on every exit,
  `Error` included; an attached caller may receive the attempt's absence or failure, a later call
  retries; failures keep their mapping; a waiting caller does not cancel the attempt.
- Ancestry and cycles: the two shared `ConcurrentLinkedDeque` stacks are replaced by immutable
  resolution ancestry bound through a private `ScopedValue` (frames carry the handler identity, so a
  nested separate decision is not a cycle); an ancestor lookup returns absence as today (fallback and
  MustBePresent decide). No wait graph (as built after R-D27-sol): only callers outside every
  resolution of the handler wait, and an owner never waits, so no cycle of waits can form; a repeated
  key within one ancestry ends through the cycle check. After winning ownership, an attempt first
  reads the published bag (a caller that missed before another published gets that bag).
- `ResolverContext` stays confined to its attempt; never shared through flights. Argon found that
  `getCacheKey()` and `resolveContextRef` re-run `resolveAllContextRefs()` whenever the resolved map
  is empty; the implementation resolves once per attempt unless that changes an observable retry
  (the reviewer checks this against Sol's "keep the empty-result retry").
- `ResolverCacheKey` (bead `policy4j-3huw`): `hashCode` by content (`Arrays.deepHashCode`, matching
  `equals`); serialized form kept, a cached hash read from an old stream is discarded on
  deserialization. The default PIP puts into its cache only after a miss.
- No execution seam (removed after R-D27-sol): attempts run inline on the calling thread; worker
  threads belong to the information point (D26). No XPath or MDC changes in D27.

Kept: request precedence and one immutable positive bag per attribute (§7.3.4, §7.3.5); missing
versus failure, MustBePresent, status codes and messages, resolver fallback order (§7.19); demand
order, short-circuits, response publication (§7.18, Annex C); public signatures, lazy dependencies,
the backed resolved-attribute views. Observable today: concurrent callers of one handler coalesce and
see one bag; false cycles from the shared deques disappear; a configured PIP cache starts to hit
(equal keys hash equally).

PIP session (maintainer, 2026-10-27): an additive `PolicyInformationPoint.forDecision()` default
method (returns `this`; the default PIP returns a decision-local session) is approved as the home of
the constrained whole-result-set memo of PR11 rank 1 (only a demanded member already present in an
unexpired set is reused and published; a missing member is a memo miss; failures not memoized;
dependency timing kept); implemented after D28 can measure it.

### D28. A PIP-heavy JMH corpus and a configurable fixture (draft Codex Terra)

`pip:N` corpus through `PolicyCorpusProvider`: requests carry only an identity; attributes and
selector content come from the PIP; cases independent, dependent (a PIP context reference), shared
key, missing (optional and MustBePresent), failure and skipped failure, resolver fallback, XML
selector through the PIP entity fallback; deterministic data. Parameters `pipLatencyMicros` (0),
`pipMode` (`serial`, `parallel` backend reads inside the demanded resolver on a fixture-owned
virtual-thread executor behind a small seam), `pipCache` (`off`, `on`: a fixture-local adapter over
bounded Guava caches keyed by `ResolverCacheKey`, after `policy4j-3huw`), `pipMaxInFlight` (32). A
full-response oracle (decision, status code, message, detail, obligations and advice in order,
included and resolved attributes, policy references) at setup and recheck, never in the timed
operation. Decision (maintainer, 2026-10-27): an additive `PdpFixture.open(...)` overload accepts a
caller's `PolicyInformationPoint` (the caller owns it); the existing overloads stay. Measurement:
`-t 1 avgt` serial against parallel and at zero latency; `-t T avgt` with cache off and on;
`-bm sample` percentiles; `-bm thrpt` saturation. Expected effects unmeasured.

## Batch 5 (allocation sites of profile 026; analysis `research/pr12-opus-allocation-026.md`, Claude Opus)

Shares are allocation samples of profile 026 (rsa2008 / synthetic:1000 first-deny), expected, not
measured, overlapping. synthetic:1000 `all` is 94.8% the per-child contexts that D24/D25 remove.

### D30. Cycle U: hashing and iteration without temporary objects

U1: `Value.OtherValue.hashCode` folds instead of `Objects.hash` varargs (6.4% / 3.7%), and the same
fold in the uncached value hashes (`RFC822Name`, `ISO8601Duration`, `DNSName`, `IPAddress`,
`PortRange`, `Path`); hash values unchanged. U2: the combining algorithms loop by index over their
lists (2.8% / 2.5%), guarded by `RandomAccess` (a non-random-access caller list keeps the
iterator); Annex C order and early exits unchanged. U3: `BagOfValues.Builder.attributes` keeps its
two passes (type check, then add) as index loops over a random-access list (3.0% rsa2008).

### D31. Cycle X: small bags without a hash map

X1: `BagStorages.Builder` collects values in an array; at `build()` up to 8 values are grouped by
`equals` with a linear scan, above that through the `LinkedHashMap` as today; the private storages
take distinct values and counts (15.5% / 15.8%). Identical: grouped iteration order, multiset
hash, equality, serialized form, `DoubleVal` grouping (`Double.equals`). X2: `BagOfValues` computes
the `Optional` of its single value in `single()` instead of storing it per bag (transient field,
serialized form unchanged).

### D32. Cycle Y: per-decision state created only when it is read

Y1: `getMatchingObligations` and `getMatchingAdvices` read the maps without creating them (6.3%
first-deny). Y2: the root context records evaluated policies only when the request asks for the
policy id list (or debug logging is on); decision (maintainer, 2026-10-27): otherwise
`EvaluationContext.getEvaluatedPolicies()` returns an empty collection, documented (4.2% / 8.1%).
Y3: decision (maintainer): an additive default method `EvaluationContext.hasResolvedDesignators()`
that the handler answers without creating its designator cache (2.5% first-deny). Y4: decision
(maintainer): an additive `Entity.hasIncludeInResultAttributes()` computed when the attribute index
is built; requests skip categories without them; `Category.getIncludeInResultAttributes()`
unchanged (4.1% / 3.8%). Y5: the request keeps its categories as a list built at construction and
loops by index (3.7% / 8.4%, overstated by request reuse in the benchmark). Y6:
`ResponseContext.Builder` without an eager list builder (2.0% first-deny). Y7: `Result.Builder`
creates its remaining builders on first add (1.8% rsa2008). Y8 (logging): decision (maintainer,
2026-10-27): "Remove MDC I want to integrate tracing anyways down the road, much better is to
create observabilty events with context callback spi with zero allocations": the MDC puts and the
per-decision request id string go (up to 8.3% first-deny); what they carried moves to the
observability SPI of D34.

### D33. Cycle Z: function calls with fewer copies

Z1: `FunctionSpecImpl` evaluates the normalized argument list in place whenever no validator saw
it (the list is owned by the call), keeping the copy for validated calls (review rv009) (1.5%
rsa2008). Z2: the higher-order runtime validation (`requireValidArguments`, `validateArguments`)
works over the argument array with one exactly sized primitive view instead of growing lists; the
check itself stays (bead the.4.61); same result, status, and message (3% to 4% rsa2008).

### D34. Observability events through a context callback SPI with zero allocations

**Round 3a (plan review PR3-D34, Agy Argon: APPROVE WITH CHANGES), binding for 36.2.**
- P2-1, resolver spans in `DefaultPolicyInformationPoint` (both loops):
  - resolve the candidate's `Optional` inside its `try`, including the D26 per-attribute
    `rethrowFailure`;
  - in `catch (Exception)`, map the failure, keep the first one for the PIP's result, give
    `resolverFailed` this candidate's own mapped failure, and continue;
  - a selector path error on the resolver's content is mapped, reported through `resolverFailed`,
    and thrown at once;
  - an `Error` is reported through `resolverFailed` and rethrown;
  - `resolverFinished` comes after the candidate's `try`, for a present or an empty result, so an
    observer exception there is never a resolver failure; then a present result returns.
- P2-2, sources:
  - `resolvedFrom(INFORMATION_POINT)` is set after the attempt returns (in `resolveWith`) and after
    a shared flight's outcome returns (in `resolveOnce`), never over a memo source;
  - `attributeResolved` consumes and clears the source, and requires one, so a missed return path
    fails fast.
- P3-1: `PolicyDecisionPointContext.getCorrelation()` returns the `long` (default 0). The factory's
  context implements it; `Budget` forwards it. `ObservedDecisions` reads it without formatting.

**Sources and decisions.**
- Maintainer (2026-10-27): "Remove MDC I want to integrate tracing anyways down the road, much better is to
  create observabilty events with context callback spi with zero allocations".
- Draft by Codex Sol (`evidence/reviews/plan/d34-sol/draft.md`). Final API by Claude Opus (round 2 D34b-opus;
  round 3 D34c-opus after Agy Argon's PR-D34, APPROVE WITH CHANGES).
- Decisions (maintainer, 2026-10-27):
  - a generic token type;
  - failures propagate, with no isolating adapter;
  - `MDCSupport` is removed;
  - every event group is in the first cut;
  - no event objects, and no allocation for the default observer;
  - fluent `PolicyDecisionPointBuilder.observer(...)`.
- Round 3 decisions ("Fix and resolve all of these"):
  - the extended-Indeterminate bug is fixed in code, and the pass callbacks stay;
  - the correlation reaches observers as a primitive `long`;
  - there are per-resolver events, with a hook for D35's per-resolver caches.

**The SPI.** It lives in `xacml-core`, package `org.xacml4j.v30.spi.observe`, in tabs like the policy classes. It
has 25 callbacks.

```java
public interface DecisionObserver<S>
{
	/** The observer of a decision point that has none; the engine never calls it */
	static DecisionObserver<?> none()

	S requestStarted(PolicyDecisionPoint pdp, RequestContext request);
	default void requestFinished(S request, ResponseContext response){}
	default void requestFailed(S request, Throwable failure){}

	default S decisionStarted(S request, RequestContext individualRequest, long correlation, int number){ return request; }
	default void decisionCacheHit(S decision, Result result){}
	default void decisionFinished(S decision, Result result){}
	default void decisionFailed(S decision, Throwable failure){}

	default S evaluationStarted(S parent, EvaluationContext context, DecisionRule rule){ return parent; }
	default void evaluationFinished(S evaluation, EvaluationContext context, DecisionRule rule, Decision decision){}
	default void evaluationFailed(S evaluation, EvaluationContext context, DecisionRule rule, Throwable failure){}

	default S extendedIndeterminateStarted(S evaluation, EvaluationContext context, CompositeDecisionRule rule){ return evaluation; }
	default void extendedIndeterminateFinished(S pass, EvaluationContext context, CompositeDecisionRule rule, Decision decision){}
	default void extendedIndeterminateFailed(S pass, EvaluationContext context, CompositeDecisionRule rule, Throwable failure){}

	default S attributeRequested(S parent, EvaluationContext context, AttributeReferenceKey key){ return parent; }
	default void attributeResolved(S attribute, EvaluationContext context, AttributeReferenceKey key, AttributeSource source, Optional<BagOfValues> value){}
	default void attributeFailed(S attribute, EvaluationContext context, AttributeReferenceKey key, Throwable failure){}

	default S resolverStarted(S parent, EvaluationContext context, String resolverId, AttributeReferenceKey key){ return parent; }
	default void resolverCacheHit(S resolver, EvaluationContext context, String resolverId, AttributeReferenceKey key){}
	default void resolverFinished(S resolver, EvaluationContext context, String resolverId, AttributeReferenceKey key, Optional<BagOfValues> value){}
	default void resolverFailed(S resolver, EvaluationContext context, String resolverId, AttributeReferenceKey key, Throwable failure){}

	default void targetMatched(S current, EvaluationContext context, DecisionRule rule, MatchResult result){}
	default void conditionEvaluated(S current, EvaluationContext context, DecisionRule rule, ConditionResult result){}
	default void childrenSelected(S current, EvaluationContext context, CompositeDecisionRule rule, List<? extends DecisionRule> selected){}
	default void functionFailed(S current, EvaluationContext context, FunctionSpec function, Throwable failure){}
	default void responsesStaged(S current, EvaluationContext context, DecisionRule rule, Decision decision, List<Advice> advice, List<Obligation> obligations){}
}

public enum AttributeSource { REQUEST, REQUEST_MEMO, INFORMATION_POINT, INFORMATION_POINT_MEMO, CYCLE }
```

The SPI names only `xacml-core` types. `ConditionResult` and `FunctionSpec` are public in
`org.xacml4j.v30.policy` (`ConditionResult.java:25`). A resolver appears as its id string, because `spi.observe`
cannot name the PDP module's `ResolverDescriptor`.

**Semantics (the javadoc).**

*Contract.*
- Callbacks are synchronous, on the deciding thread, in evaluation order.
- One observer receives concurrent decisions on their threads, so it must be safe across decisions. The
  callbacks of one decision never overlap.
- Tokens are not null.
- Arguments are borrowed engine objects. An observer reads them during the call and does not change them.

*Request.*
- `requestStarted`: a decide call starts. It is the one abstract method: only the observer can make its first
  token.
- `requestFinished`: decide returns its response. The response has every individual result, including the results
  that request handlers make without a decision.
- `requestFailed`: decide throws.

*Decision.*
- `decisionStarted`: an individual request reaches the PDP, before the cache lookup. Only leaf split decisions are
  reported.
  - `correlation` is the correlation sequence of the decide call: the same for every decision of the call. The
    string form is the decision point's `getCorrelationId(correlation)` (see "The correlation").
  - `number` counts the decisions of the call from 1.
- `decisionCacheHit`: the decision cache answers, and no evaluation callbacks follow.
- `decisionFinished`: the result, after result construction, audit, and cache publication.
- `decisionFailed`: the decision fails. Each decision gets exactly one of `decisionFinished` and `decisionFailed`.

*Evaluation.*
- `evaluationStarted`, `Finished`, `Failed`: one `evaluate` call of a `Rule`, `Policy`, `PolicySet`,
  `PolicyIDReference`, or `PolicySetIDReference`.
  - A reference is reported with the reference as the rule, and the policy it references nests inside, whether the
    position is pinned or not (D24/D25).
  - `Finished` gets the decision; `Failed` gets the exception that leaves `evaluate`.
- `extendedIndeterminateStarted`, `Finished`, `Failed`: a policy or policy set evaluates its children once more for
  its extended Indeterminate. This happens after an Indeterminate target or condition, or after its advice or
  obligations fail.
  - `Finished` gets the extended value: Indeterminate{D}, {P}, {DP}, or NotApplicable.
  - The children evaluated in the pass nest in its token. A child whose own target is Indeterminate runs its own
    pass, which nests inside.
  - Advice and obligations run inside a pass, because their failure is part of the value (XACML 3.0 §7.18). Their `responsesStaged` callbacks nest in the pass token, and the pass never publishes them.
  - Only an `Error` can fail a pass, because the pass maps exceptions to Indeterminate.
  - Observers rely on these callbacks, not on `EvaluationContext.isExtendedIndeterminateEval()`.

*Attribute.*
- `attributeRequested`: the context handler starts one resolution of a designator or selector. Every call of
  `EvaluationContext.resolve` reaches it, memo hits included.
- `attributeResolved`: the outcome. `value` is the `Optional` the handler returns, and empty means absence.
  `MustBePresent` is applied later by the designator and is not part of the outcome. The source is one of:
  - `REQUEST`: the request has the attribute, at its first test;
  - `REQUEST_MEMO`: a later test gets the bag that the request gave first;
  - `INFORMATION_POINT`: an attempt of the information point, or the outcome of an attempt in flight that this
    call shared (D27);
  - `INFORMATION_POINT_MEMO`: a bag that the information point gave earlier in this decision;
  - `CYCLE`: the key is already on the way down, which gives absence.
- `attributeFailed`: the resolution throws. The callback gets the exception as the handler throws it: a mapped
  processing error, or a selector path syntax error.
- An observer that does not want a span per attribute keeps the default (the parent token) and records the
  outcome at `attributeResolved`.

*Resolver.*
- `resolverStarted`: the information point asks one matching resolver for the key. This is one candidate of its
  fallback loop, including a candidate whose D26 parallel attribute functions run. The parent is the attribute
  token.
- `resolverCacheHit`: the resolver's answer comes from the information point's cache, and the resolver is not
  called. It is reported inside the resolver's span.
- `resolverFinished`: the resolver's answer for the key.
  - For a designator: the bag of the requested attribute from its set.
  - For a selector: the bag that the selector selects from its content.
  - Empty means no value, and the next resolver is asked.
- `resolverFailed`: the resolver call fails, with the mapped resolution failure. A failed function of a D26
  parallel resolver counts when its attribute is demanded. A selector path error on the resolver's content counts
  too.
- The context keys that a resolver reads resolve on the deciding thread, so their attribute spans nest inside the
  resolver span. The functions of D26 parallel resolvers run on workers and emit nothing.

*Point events (they get the innermost open token).*
- `targetMatched`: the result of one `isMatch` call that is performed, after the engine has mapped a failure and set
  the status.
- `conditionEvaluated`: the result of a rule's condition, after the condition has mapped a failure to
  Indeterminate. There is no callback when there is no condition: a missing condition is True without evaluation.
- `childrenSelected`: a policy set (or, with D24/D25 task 23.4, a policy) gives its combining algorithm a prepared
  list or an index candidate list.
  - `selected` is that list. The original children stay `getDecisions()` or `getRules()`.
  - It is not reported when the algorithm gets the original children.
- `functionFailed`: a function call fails in its implementation, or is rejected (a type error, a dynamic type
  rejected by a type assertion, or a failed validation at runtime).
  - The failure is the exception that the call propagates, after the engine's wrapping.
  - An argument's own failure is reported where it happens: `attributeFailed`, or the argument's own
    `functionFailed`.
  - A failure that leaves a higher-order or lazy function's implementation is also that function's failure.
  - A recovered failure does not mark the decision.
- `responsesStaged`: an element's advice and obligations for its decision were evaluated and staged.
  - It is reported only when at least one advice or obligation is there.
  - Publication to an enclosing policy set is not reported again. The surviving responses are in the `Result`.

**Token nesting.** Request, then decision, then evaluation (and its pass), then attribute, then resolver; the
attributes that a resolver reads nest inside the resolver again.

| Callback | Token it receives | Returns |
| --- | --- | --- |
| `requestStarted` | none | the request token |
| `requestFinished`, `requestFailed` | the request token | |
| `decisionStarted` | the request token | the decision token (default: the request token) |
| `decisionCacheHit`, `decisionFinished`, `decisionFailed` | the decision token | |
| `evaluationStarted` | the innermost open token (an evaluation, a pass, or the decision token for the root policy) | the evaluation token (default: the parent) |
| `extendedIndeterminateStarted` | the evaluation token of its policy or policy set | the pass token |
| `attributeRequested` | the innermost open token (an evaluation, a pass, or a resolver) | the attribute token |
| `resolverStarted` | the attribute token | the resolver token |
| `resolverCacheHit` | the resolver token | |
| every `Finished` and `Failed` | the token that its start returned | |
| `targetMatched`, `conditionEvaluated`, `childrenSelected`, `functionFailed`, `responsesStaged` | the innermost open token | |

Every start that returns gets exactly one finish or failure. A start that throws gets neither.

**The correlation (round 3).**
- `decisionStarted` gets the correlation sequence of its decide call as a `long`.
- The string is the decision point's: `PolicyDecisionPoint.getCorrelationId(long correlation)`, an additive
  default method in `xacml-core` that returns `getId() + "-" + Long.toHexString(correlation)`.
  - `DefaultPolicyDecisionPoint` overrides it with its prefix, the hex of its build time.
  - It is the prefix from `PolicyDecisionPointContextFactory.java:70`. 36.2 lets the PDP format with the factory's
    prefix: both constructions pass the default factory (`PolicyDecisionPointBuilder.java:152`,
    `PreparedPolicySetsPdpTest.java:451`).
- Why not a static formatter of `DecisionObserver`:
  - the prefix belongs to one decision point and is fixed when it is built, so a static method would need it as an
    argument;
  - passing it on every callback adds a parameter that only formatting uses.
- How the ids relate:
  - **request-level id** = `pdp.getCorrelationId(correlation)`. It equals `PolicyDecisionPointContext.getCorrelationId()`
    of that call (`:41`).
  - **per-decision id** = request-level id + `"-"` + `number`. This is what the PDP's debug lines print
    (`DefaultPolicyDecisionPoint.java:148-151`, `:162-165`) and what the removed `XacmlRequestId` MDC key held
    (`MDCSupport.java:57`).
- Nothing is formatted unless someone reads it. The context keeps the `long` and formats at each read, with no memo;
  the eager string at `PolicyDecisionPointContextFactory.java:147-148` goes.
- A decide call whose individual requests are all rejected by request handlers has no decision. Its observer sees
  only the request callbacks.

**The engine side.**
`DecisionObservation` is a public final class in the same package. It is engine-facing, and **non-generic** (P3-1).
- **Fields:**
  - the observer, typed `DecisionObserver<Object>` by one unchecked cast in `start`. This is safe: the observation
    gives the observer only tokens that the observer returned.
  - `Object current`, the innermost open token, first the decision token;
  - the first observer exception;
  - the source named for the open attribute resolution;
  - the deciding thread.
- **Allocation:** one object per observed decision, with no deque, no array, and no per-callback allocation. The
  default observer allocates nothing.
- **The Java stack keeps the parents.** Each start returns the parent token, the caller keeps it in a local, and the
  matching end gets it back:
  - a start sets `current` to the token that the observer returned;
  - an end restores `current` to the parent *before* it calls the observer with the span's token.

  Balance is structural; an exception in an end callback cannot unbalance it.
- **The API, all without `S`:**
  - `none()`;
  - `start(observer, requestToken, individualRequest, correlation, number)`, which calls `decisionStarted`;
  - `isEnabled()`;
  - `decisionCacheHit`, `decisionFinished`, `decisionFailed`, `throwObserverFailure()`;
  - span pairs that return or take `Object parent`: `evaluation*`, `extendedIndeterminate*`, `attribute*`,
    `resolver*`;
  - `resolvedFrom(AttributeSource)`;
  - the five point events.
- **`isEnabled()`** is true only for a bound observation, on its deciding thread, before its first observer
  exception (P2-2):
  - after that exception, no start or point callback is made for the decision;
  - spans already open still get their finish or failure, because their ends are not guarded;
  - a call from another thread (no engine code makes one; D26 workers cannot reach the context) is not observed.

The plumbing (all additive):
- `EvaluationContext.getDecisionObservation()` is a default method that returns `none()`.
  - `RootEvaluationContext` returns its final field.
  - `DescendantEvaluationContext` returns `top.getDecisionObservation()` (D29).
  - A context of another implementation keeps the default, so nothing below it is observed.
- `RootEvaluationContext` gets both overloads (P3-2): `(…, recordEvaluatedPolicies, observation)` beside `:136`,
  and `(…, recordEvaluatedPolicies, prepared, observation)` beside `:149`.
  - The private constructor at `:162` takes the observation.
  - The existing constructors pass `none()`.
  - The observation is not part of `equals`, `hashCode`, or `toString`.
- `PolicyDecisionPointContext.createEvaluationContext(RequestContext, DecisionObservation)` is a default method that
  calls the one-argument method.
  - The factory's context overrides it, and both branches at `PolicyDecisionPointContextFactory.java:213-224` pass
    the observation.
  - `MultipleResourcesBudgetHandler.Budget` (`:151`) forwards it.
- `PolicyDecisionPointBuilder.observer(DecisionObserver<?>)`: fluent, `Preconditions.checkNotNull`, default
  `DecisionObserver.none()`. The PDP keeps a final `DecisionObserver<?>`.
- `PolicyDecisionPoint.getCorrelationId(long)`, as described in "The correlation".

The PDP captures `S` once per decide call. `ObservedDecisions<S>`, the twin of `NumberedDecisions`, makes the
observation:

```java
@Override
public ResponseContext decide(RequestContext request)
{
	return (observer == DecisionObserver.none())?
			respond(request, new NumberedDecisions()):
			decide(observer, request);
}

private <S> ResponseContext decide(DecisionObserver<S> observer, RequestContext request)
{
	S token = Preconditions.checkNotNull(observer.requestStarted(this, request), "token");
	ResponseContext response;
	try{
		response = respond(request, new ObservedDecisions<>(observer, token));
	}catch(Throwable t){
		requestFailed(observer, token, t);
		throw t;
	}
	observer.requestFinished(token, response);
	return response;
}

private Result requestDecision(PolicyDecisionPointContext context, RequestContext request, int number,
		DecisionObservation observation)
{
	Result r;
	try{
		r = decide(context, request, number, observation);
	}catch(Throwable t){
		observation.decisionFailed(t);
		throw t;
	}
	observation.decisionFinished(r);
	return r;
}
```

- `decide(context, request, number, observation)` is today's body (`DefaultPolicyDecisionPoint.java:141-180`)
  without the MDC calls:
  - the cache path calls `decisionCacheHit` before the audit;
  - the evaluated path calls `throwObserverFailure()` right after the root evaluation.
- `decisionFinished` is outside the `try` on both paths (P2-1).

An evaluation call site:

```java
@Override
public final Decision evaluate(EvaluationContext context)
{
	DecisionObservation o = context.getDecisionObservation();
	if(!o.isEnabled()){
		return evaluateRule(context);
	}
	Object parent = o.evaluationStarted(context, this);
	Decision d;
	try{
		d = evaluateRule(context);
	}catch(Throwable t){
		o.evaluationFailed(context, this, parent, t);
		throw t;
	}
	o.evaluationFinished(context, this, parent, d);
	return d;
}
```

- The attribute and resolver spans have the same shape.
- A point event is `DecisionObservation o = context.getDecisionObservation(); if(o.isEnabled()){
  o.targetMatched(context, this, r); }`.
- There are no lambdas, casts, boxing, or argument arrays at the call sites.

**Emission points.**

| Event | Where |
| --- | --- |
| request | `DefaultPolicyDecisionPoint.java:95-112`; the MDC calls at `:98` and `:110` go |
| decision start | `:123-131` (`NumberedDecisions`, `ObservedDecisions`), before the cache lookup at `:144-146` |
| cache hit | `decisionCacheHit` at `:147`, before the audit at `:152-154`; `decisionFinished` after the `try` |
| evaluated decision | `createEvaluationContext(request, observation)` at `:158`; `throwObserverFailure()` after `:160`; `decisionFinished` after the `try` around `:141-180`; `decisionFailed` in its `catch`; the MDC calls at `:138` and `:182` go |
| rule | `Rule.java:107-142` |
| policy, policy set | `BaseCompositeDecisionRule.java:116-153` |
| extended-Indeterminate pass | `BaseCompositeDecisionRule.java:189-203`, entered from `:137-138` and `:152` (after Terra's fix) |
| references | `PolicyIDReference.java:88-98`, `PolicySetIDReference.java:93-102`; `PinnedReference.java:55-57`, whose not-missed path reports the pair with `ref` around `target.evaluate` (the missed path delegates to `ref`, which reports itself) |
| target | `BaseDecisionRule.java:128-144`, once, after the result, outside the `try` at `:133-143`; a pinned `isMatch` (`PinnedReference.java:60-62`) reaches the target's |
| condition | after `condition.evaluate` at `Rule.java:121` and `BaseCompositeDecisionRule.java:123`, outside the condition's guard at `Condition.java:87-100` |
| children | `PolicySet.java:192-194`, when `Prepared.children` (`:206-227`) gives a prepared list, not `decisionRules` (`:207`, `:209`); `Policy.java:172-174` with 23.4 |
| staging | `BaseDecisionRule.java:153-182`, after the adds at `:164-169`, outside the `try` |
| attribute | the handler's `resolve` boundary at `DefaultEvaluationContextHandler.java:177-201` (requested, resolved, failed) |
| attribute sources | `resolvedFrom` at each return path: `INFORMATION_POINT_MEMO` at `:187-191`, `:196-198`, `:332-335`; `REQUEST_MEMO` at `:192-193`; `REQUEST` at `:228-235` and `:207-210`; `CYCLE` at `:304-307`; `INFORMATION_POINT` at `:311-312` and `:314-317` |
| resolver | `DefaultPolicyInformationPoint.java:77-111` (designators) and `:160-200` (selectors): one span per candidate, starting before the candidate's guard at `:79` and `:164`; `resolverCacheHit` when `cache.getAttributes` (`:83`) or `cache.getContent` (`:168`) returns a value; the finish after the guard, before `return r` (`:100`, `:194`); `resolverFailed` in the `catch` at `:108` and `:180`, with the mapped failure |
| function | `Apply.java:258-261` (type error); `TypeAssertion.java:71-79` for an eager function (`!isRequiresLazyParamEval()`, `FunctionSpecBuilder.java:366`; a lazy function's assertion runs inside its implementation call); `FunctionSpecBuilder.java:451`, `:469`, `:513` (implementation calls) and `:506` (validation rejection); `:325-328` (`scan`) |

The function rule (P2-3):
- 36.2 moves argument evaluation (`FunctionSpecBuilder.java:449-450`, `:468`, `:510-512`) out of the `try` that
  wraps the implementation call. `normalize` and the validation stay inside, as rejections of the call.
- The `catch` blocks wrap a non-evaluation exception into `FunctionInvocationException` (`:455`, `:473`, `:492`)
  and then report `functionFailed` with the wrapped exception.
- Engine argument expressions throw evaluation exceptions, so their propagation does not change. A raw runtime
  exception from a custom argument expression is then wrapped one level up, by the enclosing `Apply` (`:267-268`).

**Exception order.**
1. A callback runs outside the guard of its own element: the target, the condition, staging, the resolver
   candidate, and the function implementation. So that element's mapping does not take an observer exception for
   its own failure.
2. An observer exception propagates from its call site like an engine exception thrown there. The observation
   keeps the first one and is disabled from then on (P2-2). Open spans still unwind through their ends.
3. A failure callback that throws while an engine failure propagates:
   - its exception is added to the engine failure as suppressed, unless it is the same exception;
   - the engine failure keeps propagating, as in try-with-resources;
   - the observation keeps the exception, as in step 2.
4. An end restores `current` before it calls the observer, so balance holds when an end callback throws.
5. Some engine guards can absorb an observer exception thrown inside them:
   - `BaseDecisionRule.java:139`;
   - `BaseCompositeDecisionRule.java:194`;
   - `Condition.java:96`;
   - `Apply.java:267`;
   - `FunctionSpecBuilder.java:454`, `:472`, `:491`;
   - `DefaultFunctionInvocation`;
   - `DefaultEvaluationContextHandler.java:245`, `:284`;
   - `DefaultPolicyInformationPoint.java:108`, `:180`.

   Because of step 2, no further callback reports what such a guard makes up. The unwinding ends of open spans can
   carry outcomes that follow from it, and `decisionFailed` follows them. Right after the root evaluation returns,
   the PDP throws the kept exception (`throwObserverFailure`). This happens before the counters, the result, the
   audit, and the cache, so a possibly changed result is neither audited nor cached.
6. When the evaluation throws, its exception propagates. The kept observer exception is added to it as suppressed,
   unless it is the same exception or is already there.
7. `decisionFailed` and `requestFailed` follow step 3.
8. An end callback that throws after a successful engine step is an engine failure at that point.
   - `evaluationFinished`, `attributeResolved`, and `resolverFinished` fail their enclosing spans.
   - `decisionFinished` runs after the `try` that calls `decisionFailed`, so a decision has exactly one terminal
     callback (P2-1). decide throws the exception after audit and cache publication.
   - `requestFinished` behaves the same at the request level.

**D24/D25.**
- `childrenSelected` is reported as above. Skipped children are not evaluated and have no callbacks.
- A pinned position that is not missed reports the reference pair with the original reference, with the target
  nested inside. A missed position delegates to the reference. So the sequence does not depend on pinning.
- A failure that the snapshot replays escapes from `createContext` before any reference callback, and the enclosing
  evaluation's failure callback gets it.
- With the index on (23.4), the sequence differs only by the skipped children.

**D26.**
- Workers emit nothing:
  - they get the `PreparedContext`, whose `getEvaluationContext()` throws;
  - `isEnabled()` is false on other threads.
- The resolver span wraps the whole candidate call on the deciding thread, including the runner's wait and its
  timeout.
- The context keys of a parallel resolver resolve on the deciding thread when its `PreparedContext` is built, so
  their attribute spans nest in the resolver span.

**D27.** A caller that shares another caller's attempt reports `INFORMATION_POINT` and no resolver spans: the
resolvers ran in the owner's attempt.

**D35 (deferred).**
- The per-resolver caches report a hit with `resolverCacheHit`, from inside the resolver span, through
  `DecisionObservation.resolverCacheHit`. This is the hook.
- A hit is a value that this decision did not compute. A load that waits for another decision's load is also a hit.
- A load that runs the resolver reports no hit: the resolver's own work, and the context keys it reads, nest in the
  span.

**MDC.** `MDCSupport` (`MDCSupport.java:35`, public) and its four calls go. An application can write an MDC
observer: `requestStarted` and `decisionStarted` put the keys, and the matching ends restore them.

**Kept.**
- Decisions, statuses, and missing-attribute behavior.
- Operand order and laziness, and combining short-circuits.
- Obligation and advice filtering.
- Cache and audit order, and the counters with their classification.
- Serialized forms.
- With an observer that does not throw, the response is the same as without one.

**Order of work.** Implementation comes after D24/D25 tasks 23.2 and 23.3 (done) and after Terra's
extended-Indeterminate fix, which changes `BaseCompositeDecisionRule`. The children event of `Policy` comes with
23.4.

### D35. A TTL cache SPI for resolved attributes across decisions, with Guava, Caffeine, and cache2k implementations (todo, deferred by the maintainer: "Later", "Todo")

Maintainer (2026-10-27): "design caching SPI with default implementation for guava, caffeine and
cache2k (i dont remember name) for fast TTL based caches for resolved attributes across evaluation
sessions in one PDP".

Maintainer follow-ups (2026-10-27): "Caches needs to be per resolver" and "Assunmption resolver has
to access remote resource". So:
- **One cache per resolver.** Each resolver descriptor gets its own cache instance with its own TTL,
  size bound, and statistics. Fixed expiry per cache is then enough for all three libraries,
  including Guava's `CacheBuilder`.
- **A resolver reads a remote resource.** Remote latency dominates the cost of a miss. Concurrent
  misses of one key from different decisions of one PDP should share one remote call. A loading
  cache (compute on miss) gives that, across decisions; D27 coalesces only within one decision.
  A remote failure or a timeout (D26) is not cached.

Decisions (maintainer, 2026-10-27): "add those caffeine and cache2k as separate modules and unified
locator api"; Artagon core's locator is not used (it needs JDK 28 preview; "Don't use it"). So:
Caffeine and cache2k implementations live in separate Maven modules; one locator API in xacml4j finds
the cache implementation through the JDK `ServiceLoader` (as `AttributeReferenceKeyInterner` does),
with explicit selection on the PIP builder taking precedence.

Starting point: the PIP already has a cache SPI, `PolicyInformationPointCacheProvider` (get and put
of an `AttributeSet` or `ContentRef` per `ResolverContext`), with a no-op default and the abstract
`BasePolicyInformationPointCacheProvider`, set through `PolicyInformationPoint.Builder`. No
implementation is backed by a cache library. Each resolver descriptor has its own TTL
(`cache(int)`), so a cache must expire entries per entry, not per cache.

The design to settle:
- The SPI shape: keep and tighten `PolicyInformationPointCacheProvider`, or replace it with a smaller
  fluent SPI. Keys are `ResolverCacheKey` (content hash since D27), values are complete
  `AttributeSet`s (D26) and `ContentRef`s, and the TTL comes from the descriptor.
- One implementation each for Guava, Caffeine, and cache2k, built per resolver from the
  descriptor's TTL and bounds. Verify each library's loading, expiry, and statistics APIs from
  current documentation.
- Packaging: optional Maven modules or optional dependencies, discovered through `ServiceLoader` or
  chosen on the builder. This is a dependency change and needs the maintainer's approval.
- Bounds and memory: maximum size or weight, and statistics for observers (D34).
- How it fits with the per-decision memo (task 30), the single-flight lookups (D27), and the
  parallel resolvers (D26): misses, failures, and absence are not cached.
- Measured with D28's `pip` corpus, with `pipCache` on.

After R and S: `StagedResponses` only for subtrees that can respond (a flag computed when the policy
is built, conservative for references), after D24/D25 lands.

### D36. Cycle AA: `StagedResponses` only for subtrees that can respond (Codex Terra; classification by Claude Opus after review R-TERRA)

A response-capability flag is computed when each policy or policy set is built:
- An element's own staging holds the writes of its children, its combining algorithm, and its own advice and
  obligations. Its own target and condition run before its staging exists, so they count only for its parent
  (`canRespondToParent`).
- Targets and conditions are classified exactly:
  - silent: an expression of engine classes whose functions write no responses (the default providers' functions
    are built with `FunctionSpecBuilder.writesNoResponses()`), and an empty target;
  - able to write: a custom expression or match class, and a function of another provider unless it declares
    `writesNoResponses()`.
- Attribute resolvers write, if at all, to the root, which no staging holds.
- References and other implementations count as able to respond. Custom combining algorithms count too.
- `Policy`, `PolicySet`, and `Rule` cannot be subclassed (private constructors).
- Staging is created only for an enclosed subtree that may respond; the filtering and order of XACML 3.0 §7.18 are
  kept.
- The synthetic corpus (string-equal targets, no advice or obligations) and the silent parts of rsa2008 skip staging.
- Expected: up to 3.8% rsa2008 and 1.6% first-deny bytes (profile 026); measured with the batch.

### Fix: the extended-Indeterminate pass (bead `policy4j-cwtu.38`)

Found by Claude Opus (D34b) and Agy Argon (PR-D34 1.7); maintainer: "Fix and add tests". Corrected after review
R-TERRA (Claude Opus): the first fix skipped advice and obligations inside a pass, which failed open.

- The pass state reaches descendants: `DescendantEvaluationContext` forwards `isExtendedIndeterminateEval()`.
- A child evaluated inside a pass records no evaluated policy, so it is not in the policy identifier list.
- Advice and obligations are evaluated inside a pass. XACML 3.0 §7.18 makes a failing matching assignment turn its
  rule, policy, or policy set Indeterminate, and §7.14 sets the value of a policy with an Indeterminate target from
  the values of its children. Their responses go to the pass's staging (or, for an unenclosed root, to a root that
  an Indeterminate result does not read) and are never returned (§7.18).
- A failure inside a pass keeps the status of the failure that started the pass: the context that starts the pass
  does not pass a later status up when its parent has one. A status set without a rule (by the target or condition
  of a policy or policy set) goes up level by level like any other, so the rule holds for it too (review R-EXT2,
  Agy Argon P2-1).
- A nested policy or policy set whose target is Indeterminate still combines its own children for its extended
  value (§7.13, §7.14).
- Behavior compared with the code before the fix:
  - changed: the policy identifier list no longer holds the children of a pass; the root status is no longer
    overwritten from inside a pass;
  - unchanged: the extended values; the attributes that a pass's advice and obligations read appear in the resolved
    attributes; no response from a pass is returned.

### D38 to D42. Batch 6 (from profiles pr14, Agy Argon, and pr13, Codex Astra)

Profile pr14 (`research/pr14-argon-allocation-batch45.md`; batches 4 and 5 plus 23.1 to 23.3) against
batch 3: rsa2008 6,194 B/op (-38%), synthetic:1000 first-deny 1,480 B/op (-42%), synthetic:100
-9%; synthetic:1000 `all` +11% (51,686 B/op), because cycle T's `top` field moved the reference
context from 24 to 32 bytes on ~785 visited children, which the index (23.4) removes (~97%).
- D38, cycle AF (Argon's AA and AE; Claude Fable): one-value and empty bags without the growth
  buffer and grouping temporaries; an entity's attribute bags built once (memoized on the immutable
  `AttributeIndex`). Expected 22 to 25% rsa2008, 26 to 29% first-deny.
- D39, cycle AG (Argon's AB): direct list and binary function dispatch, and no per-call allocation in
  the higher-order runtime validation and logical operands. Expected 25 to 29% rsa2008. After D34
  (same function classes).
- D40, cycle AH (Argon's AD): a compact request-designator memo, no `Optional`/`AtomicReference`/
  lambda per decision in the root context and the factory, a single-result list without a copy.
  Expected 16 to 20% first-deny. After D34.
- D41, cycle AI (Argon's AC, rest): target before context for children that cannot match. After
  D34, D36, and 23.4.
- D42, cycle AJ (pr13, `research/pr13-astra-pip-profile.md`; Codex Astra; PIP path): resolver
  candidates without a stream pipeline (`DefaultResolverRegistry`), the bag passed directly into
  resolved-attribute assembly (`DefaultPolicyDecisionPoint`, about 13% of the uncached PIP path),
  and `AttributeSet` failure storage only on the first failure. pr13's dependency-map and key staging
  cycle waits for task 30's fix.
- D43, cycle AK (Claude Fable): `Category.resolve(AttributeDesignatorKey)` answers from the entity's
  kept bag. JDK 25 compact object headers (JEP 519, a product option in JDK 25, off by default)
  measured as an option, not a default: a one-fork smoke allocates 10 to 12.5% less per decision
  (rsa2008 4,299 to 3,769 B/op; synthetic:1000 first-deny 920 to 824 B/op), time unchanged within
  error. Documented in `docs/ai/BENCHMARKS.md` and `xacml-jmh/README.md`; D12 pairs keep the default
  headers.

### D37. Decision caching: TTL from the participating resolvers and a request key over the attributes the policy tree reads (epic after D35; research first)

Maintainer (2026-10-27): "add epic for decision caching, response cache TTL is calculated based
resolvers participating in decision lowest TTL or other policies, I need to research on how to
calculate request cache key in fast and efficient manner to filter some attributes from env etc -
irrelevant for decision caching - based on attributes used in the policy tree - I need to research
litterature and prior art add as separate epic after cache spi design".

Starting point: the PDP already has a decision cache (`DefaultPolicyDecisionPoint`, keyed by the
request). To settle, after D35 and a literature and prior-art survey:
- TTL of a cached decision: the lowest TTL among the resolvers that took part in the decision, or a
  policy-defined bound (an obligation, a policy attribute, or PDP configuration); no caching when a
  participating resolver is not cacheable.
- The request cache key: only the attributes that the policy tree can read (designators and
  selectors by category, id, type, and issuer), computed once per policy tree and per repository
  generation (D24/D25), so volatile environment attributes such as the current time do not split
  the cache; a fast, allocation-light key (hashing, interning, ordering), and exactness when a
  policy reads attributes dynamically (selectors, functions over content, references resolved
  later).
- Invalidation with repository events and resolver cache expiry; interaction with D35's per-resolver
  caches and D34's events.

### D45. Shared expensive condition subexpressions within one decision (draft Codex Sol, revised after plan review PR-D45-argon; approved by the maintainer 2026-10-28; built after `add-regex-engine-spi` D6)

**Context.** `rules:100 repeated-conditions` contains the normalized-email chain once in each of 100 rule conditions within one Policy (`RulesCorpus.java:259`). Department literals differ, so whole-condition sharing misses the opportunity. The source model in `research/pr19-sol-condition-analysis.md` estimates approximately 176 B per normalization pair; this is not measured attribution. Regex binding remains in `add-regex-engine-spi`, D6.

**Eligibility and provenance.**

Use a reviewed internal allow-list:

- Exact engine string literals and immutable string-bag literals, preserving type, order and multiplicity.
- Exact `AttributeDesignator` nodes, identified by category, attribute id, type, issuer **and MustBePresent**.
- Type-correct applies of `string-one-and-only`, `string-normalize-space` and `string-normalize-to-lower-case` over eligible operands.

Prove function eligibility in O(1): `FunctionSpecImpl.direct` must carry the same canonical unary binding as the corresponding allow-listed method in `DefaultFunctionInvocation`. Compare the underlying binding identity, not the invocation-wrapper identity. Canonical bindings originate from the default factory’s static, public, engine-loader path (`DefaultFunctionInvocation.java:74,218`). Also require the standard signature and return-type handling, eager unary evaluation, `validator == null`, and `writesNoResponses()`. Compute or cache this classification during construction/preparation; perform no reflective lookup per evaluation.

Binding provenance alone does not make arbitrary spec metadata trustworthy. Custom specs, factories, validators and return-type handling remain excluded. Function ids and `writesNoResponses()` alone are insufficient.

Exclude selectors/content, variables, time-dependent and random functions, and custom expressions. The first cut shares only successful values derived from stable, positive singleton **request bags** under the existing request-entity resolution contract. PIP-backed inputs use ordinary evaluation.

**Preparation scope, identity and threshold.**

Add one immutable `PreparedConditions` bundle per **Policy** to the D24/D25 generation, using a weak identity-keyed map like `Indexes.policies` (`PreparedPolicySets.java:75`). *Amended by FIX-D45 (review R-D45-sol P3): the Policy owns its bundle in a private lazy field, published once by compare-and-exchange; `Generation.conditions` is removed. The bundle depends only on the policy's rules, conditions, expressions, and function specs, so it is prepared once per Policy, and a custom sibling that holds its Policy forms a cycle the collector frees.*

Scan **all rule conditions of that Policy together**. Count structurally equal occurrences across rules, including multiple occurrences within a condition. Do not count each Condition in isolation or combine counts across unrelated Policies. Within the bundle, map Condition identities to prepared predicates. Without an enclosing Policy, use ordinary evaluation.

At Policy construction, compute a private final `mayShareConditions` flag. A cheap conservative scan sets it only when the rule conditions contain at least four potentially eligible occurrences containing two string transformations. This flag need not establish structural equality; preparation performs that check. A false flag makes `Condition.evaluate` skip Generation lookup entirely. Keep it outside public equality, hashing and marshalling.

Canonical keys contain binding/spec identity, ordered child identities, typed literal contents and complete designator identity. Neither `FunctionSpecImpl.equals()`—function id only (`FunctionSpecBuilder.java:825`)—nor ordinary `Apply.equals()` supplies this identity.

Use an ordinary canonical map owned by the prepared bundle. No global or weak interner is needed: the bundle already retains its active identities. The preparation lives as long as its Policy (FIX-D45). Only an exact `Condition` takes part: a subclass is not counted, and every fallback evaluates the condition's private predicate, never the overridable `getExpression()` (FIX-D45, review R-D45-sol P2).

Select maximal expensive repeated roots with **at least four occurrences and at least two allocating string transformations**. Prefer larger qualifying roots and suppress nested memo entries beneath selected occurrences. Bare designators and department `one-and-only` calls receive no entries.

**Reaching nested shared roots.**

For each affected Condition, build a prepared expression spine once:

- Replace selected roots with private memo-aware Expression wrappers retaining the original subtree.
- Clone only ancestor `Apply` nodes between the predicate root and those wrappers, using the same FunctionSpecs, argument positions and unchanged sibling expressions.
- Reuse unaffected branches. Do not cross custom or unknown expression/function/validator boundaries.

`Condition.evaluate` chooses the prepared predicate and evaluates it inside its existing exception/status handling (`Condition.java:87`). Its public predicate, visitors and serialized representation remain unchanged.

**Do not modify `Apply.evaluate` or lazy `and`/`or`.** Cloned Applies use their normal construction and evaluation paths. Consequently, a selected root is reached only when ordinary lazy evaluation reaches that operand. Regex calls remain ordinary calls; ancestor cloning must retain D6’s normal binding and deferred-error behavior.

**Runtime gates and memo access.**

Before preparation lookup, require a qualifying Policy and an engine root with a generation. Sharing additionally requires:

```text
root.isDecidingThread()
&& !root.getDecisionObservation().isEnabled()
&& !context.isValidateFuncParamsAtRuntime()
&& root.hasRequestAttributeResolution()
```

Foreign contexts retain their existing path. Memo-aware roots recheck these gates whenever invoked, so a worker or a changed validation flag falls back to the original subtree.

After those gates:

1. Look up the canonical expression token in the root memo without creating the map.
2. On a hit, return the successful value immediately. The entry already proves the request-bag guards succeeded in this decision.
3. **Only on a miss**, inspect the referenced request entities and bags. Require present singleton bags of exact engine string values. Never call a PIP to establish eligibility.
4. If a guard fails, evaluate the original subtree normally and store nothing.
5. Otherwise evaluate normally outside map callbacks; publish only its successful value with `putIfAbsent`.

No per-call keys, operand lists or captured suppliers are created.

**Root storage and reference-miss isolation.**

Reuse the root’s existing lazy snapshot-map field, widening its private key type for disjoint reference and expression namespaces. Add no root field or eager holder.

Choose an explicit singleton **`ReferenceMiss.INSTANCE` marker**:

- `remember(ref, outcome)` records the marker **before** publishing any non-matching reference outcome, including absence, failure and wrong target.
- `hasMisses()` becomes `m != null && m.containsKey(ReferenceMiss.INSTANCE)`.
- Expression insertion never writes that marker.
- `isMissed(ref)`, snapshot lookup and replay continue to address the reference directly.

This preserves O(1) checks and prevents expression entries from disabling the target index. No key scan or `!m.isEmpty()` test remains for reference-miss detection (`RootEvaluationContext.java:714,752`). The marker adds one map entry only on decisions recording a real reference miss.

Retain compare-and-exchange map publication. Expression evaluation, attribute resolution and resolver calls stay outside map callbacks. D45 is deciding-thread-only; D26 workers follow ordinary evaluation, and D27’s flights remain unchanged. Introduce no expression flight or wait mechanism.

**Retry and soundness.**

The admitted functions propagate operand failures, and singleton request guards establish positive inputs. A successful cached value therefore cannot have consumed absence or masked a failure. Keep no expression failure, empty-dependent success, timeout or `Error`. Later occurrences retry through the ordinary path.

Surrounding `and`/`or` remain uncached and preserve their existing error masking. Preparation evaluates no expressions and resolves no attributes.

**Kept:**

- Stable positive bags and demanded retrieval — §7.3.5.
- Type checks, operand order, exceptions and status mapping — §§7.4, 7.19.
- Variable scope and existing variable caching — §7.8.
- Lazy Boolean, string and bag/cardinality semantics — A.3.5, A.3.10, A.3.13.
- Decisions, obligations and advice — §7.18.

[Official XACML 3.0 specification](https://docs.oasis-open.org/xacml/3.0/errata01/os/xacml-3.0-core-spec-errata01-os-complete.html).

**Observation.** Observed decisions use original predicates and retain every D34 callback. No shared-value event is added in this cut.

**Alternatives rejected.** Per-Condition counting misses this workload. AST interning alone saves no evaluation. General compilation, broad purity inference and failure caching require more proof. A separate root map field risks enlarging every decision; shared storage with an explicit reference marker avoids that cost.

**Costs and risks.** Require zero additional B/op for rsa2008 and synthetic’s no-candidate paths: the Policy flag skips generation lookup, with no root-field growth, memo creation or per-rule preparation objects. Preparation allocations occur once per qualifying Policy (once per generation before FIX-D45) and must be reported separately.

Avoiding 99 normalization pairs removes approximately 17,424 B before memo overhead: **about 17 KB/decision expected, unmeasured**. Actual savings depend on escape analysis. No defensible time forecast exists yet. Measure reference-marker overhead separately on reference-failure cases. Principal risks are incorrect provenance, ancestor cloning, weak-key retention and mixed-map bookkeeping.

**Files and ownership.**

- New `policy/PreparedConditions.java`; `Policy.java`, `Condition.java`: flag, Policy-wide preparation and predicate selection.
- `PreparedPolicySets.java`: generation table; coordinate with **Terra**. No `TargetIndex` edit.
- `RootEvaluationContext.java`: memo and O(1) reference marker; coordinate with **Fable’s FIX-AN**.
- `FunctionSpecBuilder.java`, `DefaultFunctionInvocation.java`: binding proof.
- Coordinate prepared Apply construction with **Fable’s regex D6**; **no `Apply.evaluate` edit**.

**Test plan.** Pin outcomes for repetition across separate rules; positive/negative strings; missing and multivalued inputs; transient absence/failure retries; lazy error masking; distinct bindings and MustBePresent; custom factories/validators; observed and worker-thread fallback; separate decisions/generations; and reference failure after expression-state population. Retain D34 sequence and reference-snapshot coverage. No allocation, token-identity or call-count tests.

**Benchmark plan.** After regex D6 stabilizes, pair builds on `rules:1,100 repeated-conditions`, rsa2008 `all`, and synthetic:100/1000, index off/on. Use GC B/op, byte-weighted allocation and CPU profiles. Report cold preparation separately. Gates remain independent-family review, reactor/conformance and batch measurement.

Checked against the saved draft, Argon’s substantive findings and current main-checkout source. No files changed, builds, tests or measurements run.

**Maintainer decisions (2026-10-28).** D45 is approved for implementation after regex D6. (1) The preparation-only public bridge `FunctionSpecBuilder.isShareableBuiltin(FunctionSpec)` is allowed; bindings stay hidden and no custom purity opt-in is added. (2) Sharing stays off in observed decisions; if it is ever enabled there, an explicit shared-value observer callback (a D34 decision) is preferred over replaying events. (3) Values from a PIP stay unshared until a stable-read and side-effect contract exists.

**Open questions as drafted.**

1. **Public provenance bridge:** recommend retaining a narrow preparation-only bridge, proposed `FunctionSpecBuilder.isShareableBuiltin(FunctionSpec)`. O(1) binding comparison removes repeated discovery, **not Java’s package-access boundary**: `FunctionSpecImpl` and the unary-binding internals are inaccessible to the policy-package planner. Approve the bridge and necessary binding-comparison plumbing without exposing bindings or adding a custom purity opt-in.
2. **Future observation:** recommend keeping sharing off for observed decisions now. If later enabled, prefer an explicit shared-value callback over replaying events for work that did not run; that requires a D34 decision.
3. **Future PIP inputs:** recommend deferring them until a stable-positive-read and side-effect contract is explicit. D27 single flight alone does not establish that contract.

### D46. Ordered-range candidates under the target index (draft Codex Astra, `research/pr21-astra-range2-profile.md`; approved by the maintainer 2026-10-28)

**Why.** After AM, AO, AS, and AT, rejecting a range rule allocates nothing, but it still costs
time: on `rules:100 date-ranges` the prechecks of the 98 rules that cannot match are about 80% of
the decision (an estimated 6.6 µs of 8.2 µs per decision). A candidate filter removes that work.
It is a time optimization, not an allocation one.

**Scope.** Behind the existing target-index opt-in. First the forms present in the measured corpus:
integer and date literals in `type-less-than-or-equal(L, designator)` and `type-greater-than(U,
designator)` on the inline rules of a `Policy`. Policy-set children and references keep their
existing paths; other ordered types, multi-key targets, and referenced policies follow only with
new evidence. No new public API.

**Preparation.** Exact engine `Target`, `MatchAnyOf`, `MatchAllOf`, `Match`, designator, and value
classes; the first AnyOf only; one complete designator key and ordered type per plan. Every Match of
every AllOf alternative of that AnyOf must be a supported, total bound comparison on that key,
otherwise the child is always retained. `MustBePresent=true`, selectors, custom expressions, and
unsupported comparisons stay on the ordinary path. The comparison direction is proven from the
engine `FunctionSpec` and a direct scan for the typed literal, with lower, equal, and upper checks
at preparation, as the equality index certifies its operator; never from the function identifier
alone. Immutable sorted endpoint arrays with alternative and child positions (JDK sort and binary
search), one posting per bound, published inside the same `TargetIndex` through
`Generation.indexes.policies.putIfAbsent`; no second registry, listener, or shared mutable mask.

**Query.** For an authoritative, nonempty bag B of the ordered type: `Match(L <= x)` holds exactly
when `max(B) >= L`, and `Match(U > x)` exactly when `min(B) < U`; an AllOf is possible when both
hold. Different Matches may use different witnesses (`[10,20)` matches `{5,25}`), so neither point
stabbing nor interval intersection is used. Integers use the engine comparison with no off-by-one
adjustment; dates use `ISO8601Date.compareTo`, including its fixed UTC default for an absent zone.
The query reads the request entity once and the bag once (`Entity.resolve(AttributeDesignatorKey)`),
under the precheck's guards (deciding thread, request resolution, unobserved, no runtime
validation); it never calls `EvaluationContext.resolve`, a PIP, or a selector. A missing entity or
key, an empty or unexpected bag, an unsupported ordering, or any failure returns the original
children. A decision-local `BitSet` clears the alternatives whose bounds fail; children keep their
order and duplicates; the original list is returned when nothing is removed.

**Soundness.** The skipped set is an under-approximation of proven NoMatch (§§7.6, 7.7): a candidate
is never a proof of Match, retained children go through the existing precheck and ordinary
evaluation, and no error becomes NoMatch (§7.19). Kept: §7.3.5 bag identity, Annex C order and
short-circuits, §7.18 responses, evaluated policy identifiers, §7.15 reference handling.

**Cost and gate.** Expected 3 to 6 µs per decision on `rules:100` ranges (unmeasured); the
candidate list may cost 100 to 250 B per decision. Within the cycle, the existing
`TargetIndex.candidates` mask and list are pre-sized (`research/pr22-opus-fixed2-profile.md`,
item 9). Outcome tests at the bounds, literal order, `{5,25}`, duplicates, overlapping and reversed
bounds, alternatives, an earlier Indeterminate, absent and PIP-supplied attributes, MustBePresent,
type errors, date zones, runtime validation, observation, and a repository replacement. Gate: review
by another family, reactor and conformance, and a controlled pair with rule counts below and above
100; ship only if the pair shows the latency gain.

### D47. Rules evaluated in their policy's context (draft Codex Sol, from `research/pr22-opus-fixed2-profile.md` item 2; approved by the maintainer 2026-10-28 after a plan review; built after D45 and FIX-TSTATUS)

**Maintainer decisions (2026-10-28).** A plan review by another family first, then the build after
D45. The three recommendations below are accepted: conservative eligibility computed at
construction; the exact legacy algorithm classes and the policy-pass contexts included; +8 B per
policy context, no public option, enabled after the controlled pair.

**Plan review PR-D47-argon (APPROVE WITH CHANGES; `evidence/reviews/plan/pr-D47-argon/report.md`).**
The build follows these amendments:

- **The eligibility scan (P2-1).** Besides every `Match` predicate and `Apply` function, require
  `ref.getClass() == FunctionReference.class` and a trusted built-in `ref.function()` for every
  function reference (higher-order calls pass the rule's context to it). Reject or unwrap
  `TypeAssertion`. Require exact engine classes for `Target`, `MatchAnyOf`, `MatchAllOf`, `Match`,
  `Condition`, `Apply`, `AttributeDesignator`, `AttributeSelector`, `FunctionReference`,
  `VariableReference`, `VariableDefinition`, `ObligationExpression`, `AdviceExpression`, and
  `AttributeAssignmentExpression`, and engine `Value` classes for literals. Walk
  `VariableReference` to `VariableDefinition` with a lazily allocated identity set, so diamond
  graphs stay linear and cycles end. Quantified expressions are not on the list.
- **The parent and the algorithm set (P2-2).** Recognize the parent with
  `instanceof Policy.PolicyDescendantEvaluationContext` (package-private, so no external subclass):
  it covers the direct context, `PolicyIDReference.ReferencedPolicyContext` (every policy context
  of `synthetic`), and the pass context. Keep `canIndexRules()` (D19) on its existing seven-class
  set; D47 uses its own eleven-class set with the four legacy overrides classes.
- **Status order in a pass context (P3-1).** While a rule is active in a pass context, record the
  rule's local status, the keyed `(rule, status)`, and the context's own status before the
  `startsPass()` guard. Write the policy's keyed status and the parent's status only after the
  guard, as the former two-hop chain did.
- **Savings on the current tree (P3-2)**, from the exact P-FIXED3 histograms: rsa2008 −100.7,
  `narrow` −24, `responses` −48, `multi-valued` −192, `synthetic:100` −20.6, `pip:8` −24 B/op.
  `ReferencedPolicyContext` grows from 40 to 48 B.
- **Tests:** add a custom function reached through a `FunctionReference` of a built-in
  higher-order function, and a policy reached through `PolicyIDReference`.

**Context.** `Rule.createContext` still allocates a `RuleEvaluationContext` unless its argument
already identifies that rule (`Rule.java:70`). Its comment describes context sharing that the
normal policy path does not provide. Under the compressed-reference layout (12-byte header, 4-byte
references, 8-byte alignment): `DescendantEvaluationContext` (parent, top, status) is 24 B; a rule
context adds its Rule, 32 B; a policy context adds its Policy and variable-results map, 32 B.
Adding a rule reference and a separate rule-status reference makes the policy context 40 B. No root
field is added; AX's root stays 80 B. These are source-layout calculations, not a measurement.

**Eligibility.** Borrow the policy context only when:

- the actual parent is the engine's policy context, with an uninterrupted engine context chain to
  its `RootEvaluationContext`;
- `root.isDecidingThread()`, observation is disabled, and runtime function validation is disabled;
- the policy uses an exact class from its `BUILT_IN_ALGORITHMS` set (`Policy.java:69`):
  first-applicable, deny/permit overrides including the ordered variants, deny-unless-permit,
  permit-unless-deny, and the four legacy overrides variants;
- the child is an exact engine Rule, and the policy has no active borrowed rule;
- the rule's reachable expression paths cannot pass this context to custom evaluation code.

For the last condition, compute a conservative internal flag once, at construction, from the
target, condition, referenced variable definitions, and response assignments. Require engine
expression classes and trusted built-in invocations (`FunctionSpecBuilder.usesBuiltInInvocation`).
Unknown expressions, custom specs, factories, or context-receiving callbacks keep a real rule
context. `writesNoResponses()` alone is insufficient. This protects legitimate extension behavior
(context retention, parent-chain inspection); it is not an adversarial-code boundary. No purity
restriction is needed: D47 caches no values. Custom algorithms, foreign contexts, external
`RuleEvaluationContext` subclasses, observed decisions, worker threads, and reentrant entry keep the
existing path.

**Mechanism.** Two private fields on `PolicyDescendantEvaluationContext`: the active Rule and its
local Status. Package-private entry and exit; no new public API, scope object, captured supplier, or
per-rule holder. At the shared evaluation boundary of the built-in combining loops
(`DecisionCombiningAlgorithms.java:66`):

1. Run `TargetBeforeContext.precheck` before setting the slot; its current-rule guard rejects an
   already-active rule (`TargetBeforeContext.java:191`).
2. A proven NoMatch takes the existing skip path and sets no slot.
3. For an eligible remaining rule, install its identity with an empty local status.
4. Evaluate as today, including the prechecked-Match path; `Rule.createContext` finds a valid
   context and returns it.
5. Clear both fields in `finally`, also on exceptions and `Error`.

Traversal, candidate order, and short-circuits are unchanged. An occupied slot falls back to a
fresh rule context; no mutable stack.

**Context answers.** During the scope, `getCurrentRule()` answers the active Rule; the nearest-level
walk lets descendant engine contexts recognize this logical rule level
(`DescendantEvaluationContext.java:228`). `getCurrentDecisionRule()` follows through its default
(`EvaluationContext.java:70`). Policy, policy-set, reference, clock, and XPath answers keep their
owners. Outside the scope, the policy context answers as today. Only the deciding thread reaches the
slot.

**Status isolation.** FIX-TSTATUS is in place first; ordinary rule contexts and borrowed evaluation
use the same internal status operations. The inherited policy status and the active rule's local
status are distinct:

- entry starts with no local status, whatever earlier rules or the policy recorded;
- `getEvaluationStatus()` and `setEvaluationStatusIfAbsent` address the active rule's local state;
- a local failure records the Rule's status and propagates as the former Rule-to-Policy chain did,
  naming the Policy explicitly at the policy level (calling the inherited setter while
  `getCurrentDecisionRule()` answers the Rule would misattribute it,
  `DescendantEvaluationContext.java:105`);
- keyed setters and `getEvaluationStatus(rule)` keep FIX-TSTATUS's contract and the root's keyed
  map; a write for another rule does not touch the active rule's local state;
- exit clears the local state; it does not remove keyed statuses or the policy or root status.

A second failing rule therefore reports its own status, or its own fallback processing-error.

**Variables and preparation.** The policy's variable-results map, lazy evaluation, and
undefined-variable checks are unchanged (`Policy.java:349`); variables are not cleared between
rules. D45 keeps selecting prepared conditions by the enclosing Policy and its root memo; D47 adds no
generation entry or memo.

**Extended-Indeterminate and resolution.** The separate policy-pass context of
`Policy.createExtIndeterminateEvalContext` (`Policy.java:334`) stays. Eligible rules in a pass use
that pass's slot and local status, never the original policy context's. The boundary that keeps
the status which started a pass (`DescendantEvaluationContext.java:107`) stays. Public rule
extended-Indeterminate contexts stay separate objects; response staging, pass cleanup, and
publication are unchanged (`BaseCompositeDecisionRule.java:220`). D26 resolution goes through
`top.resolve`, so workers never see the slot; D27 flights, task-30 sessions, and publication are
unchanged.

**Soundness.** Kept: target and condition truth tables and effect-specific Indeterminate values
(§7.11); missing-attribute, syntax-error, and processing-error (§7.19); first-applicable stopping,
ordered traversal, the overrides' extended-Indeterminate distinctions, and the unless algorithms
(Annex C); policy-scoped variables, stable request bags, and effect-filtered responses (§§7.8,
7.3.5, 7.18). This changes the context representation, not the evaluation sequence.
[XACML 3.0 Plus Errata 01](https://docs.oasis-open.org/xacml/3.0/errata01/os/xacml-3.0-core-spec-errata01-os-complete.html),
fetched 2026-10-28.

**Cost and saving.** With `R` eliminated rule contexts and `P` policy contexts,
`Δ B/op = −32R + 8P`. From the P-FIXED2 attribution (unmeasured on the current tree): rsa2008
−100.6, `rules:100 multi-valued` −186.4, `responses` −48, `narrow` −24. Every policy context gains
8 B, also on fallback paths. No latency saving is claimed. The reusable-wrapper alternative (cycle
AP) removes about `32(R − P)`: about −47 B on rsa2008 and nothing where each policy evaluates one
rule, and it still needs status isolation.

**Files.** `Rule.java` (construction-time eligibility, corrected comment); `Policy.java` (slot
lifecycle, local status, policy-pass integration; after D45); `DescendantEvaluationContext.java`
(nearest-rule answer, shared status propagation); `TargetBeforeContext.java` (scope entry after the
precheck, cleanup). No `RootEvaluationContext`, `Condition`, `PreparedPolicySets`, regex, or index
edit.

**Test plan.** Reuse existing coverage first. Add only uncovered outcomes: several failing rules
with distinct statuses and a fallback processing-error (keyed statuses and the final response);
first-applicable, ordered overrides, and mixed-effect Indeterminate outcomes; policy variables
shared across rules; nested response staging and extended-Indeterminate passes (originating status,
response order); custom and foreign contexts, external subclasses, custom callbacks, and worker
fallback; D34 callback sequences, D26 outcomes, and D45 condition results. No allocation,
context-count, slot, or call-counter tests.

**Gate.** Review by another family, reactor and conformance, then a controlled pair that differs only
in D47: rsa2008 `all`, `rules:100` `multi-valued`, `responses`, `narrow`, index off and on. Ship
only with the allocation saving and no material latency regression.

**Open questions as drafted.**

1. **Extension boundary:** recommend the conservative construction-time eligibility above. Broader
   reuse would change what custom callbacks can observe.
2. **Legacy algorithms and policy passes:** recommend including the exact legacy classes and the
   policy-pass contexts, after FIX-TSTATUS and their outcome coverage.
3. **Fallback cost and enablement:** recommend accepting the 8 B policy-context increase and
   enabling D47 internally after the controlled pair, with no public tuning option; decline the
   cycle if the eligibility machinery or a measured regression outweighs the saving.

### D48. Evaluated responses keep their assignment list and build the multimap on demand (draft Gemini Argon, D-RESP, `research/pr24-argon-response-representation.md`; approved by the maintainer 2026-10-28, both open questions as recommended; built after cycle AY)

**Context.** After cycle AX, `rules:100 responses` allocates 2,336 B per decision (exact
histograms). About 1,176 B of it builds and hashes Guava's `ImmutableSetMultimap` for the one
dynamic `audit` obligation (`owner=alice`, `owner=bob`, `level=info`) in
`DecisionRuleResponse(String, Effect, Collection)`:
- builder staging: the builder, three `LinkedHashMap`s, three tables, five entries, two
  `LinkedHashSet`s, entry sets and arrays, about 864 B;
- the retained multimap graph, about 272 B;
- the eager hash at `DecisionRuleResponse.java:65`, 40 B.

The engine never reads the multimap. PDP evaluation, XML, and JSON marshalling read `getId()`,
`getFulfillOn()`, and `getAttributes()` only.

Guava 31.1 offers no cheaper equal multimap. Every `ImmutableSetMultimap.of(...)` overload goes
through the builder. There is no pre-sized builder. `ImmutableListMultimap` is never equal to a set
multimap: `asMap()` values are `List`s against `Set`s, and the hash differs.

**Mechanism.**
1. `DecisionRuleResponse` gains `private final ImmutableList<AttributeAssignment>
   assignmentList`: the distinct assignments, grouped by first-seen attribute id, in first-seen
   order. That is exactly the sequence of the multimap's `values()`.
2. The package-private collection constructor, used only by `Obligation.of` and `Advice.of`, keeps
   `ImmutableList.copyOf` (a no-op for the engine's immutable list). A bounded, allocation-free scan
   (up to eight assignments) checks that equal ids form contiguous runs and that the values within
   a run are distinct. If so, it keeps the list as is. Otherwise (duplicates, interleaved ids, more
   than eight), it builds the `ImmutableSetMultimap` as today and takes `values().asList()`.
3. The protected `Multimap<String, AttributeAssignment> attributes` keeps its name, type, and
   visibility:
   - The protected `DecisionRuleResponse(Builder<?>)`, the only constructor an external subclass
     can reach, fills it eagerly as today.
   - For factory-built responses it is built on first demand by a private `attributes()` accessor.
     Engine code reads the multimap only through that accessor. `Advice` is final, and `Obligation`
     has no public or protected constructor, so no external subclass sees a factory-built instance.
4. **Accessors.**
   - `getAttributes()` returns the list.
   - `getAttribute(id)` returns `attributes().get(id)`.
   - The hash is computed lazily over the runs, bit for bit equal to the multimap's.
   - `equals` takes a fast path on equal lists and falls back to the multimap comparison when the
     order differs.
   - `toString` uses `attributes()`.
   - `merge` and `Builder.from` read `getAttributes()`.
5. The object stays 32 B: the new reference fills the existing alignment gap.

**Soundness.** §5.34 and §5.35 define an obligation's and an advice's attributes as a set. §7.18
evaluates the expressions and passes them up by effect. The list keeps the set semantics,
grouping, order, equality, and hash of today's representation (the AX tests pin them).

**Compatibility.**
- No public or protected member is removed or narrowed.
- The builder's protected `attributes` is unchanged (R-AN P2-2).
- External subclasses of `DecisionRuleResponse` (`ExternalResponseBuilderCompatibilityTest`) and
  of the response expressions (`ExternalObligationExpressionCompatibilityTest`) keep working.
- Marshalling iterates the same elements in the same order.
- The lazy fields are derived from immutable state and published safely, as `Result.hashCode` is.

**Cost and gate.**
- `rules:100 responses`: about −1,176 B/op (2,336 to about 1,160).
- rsa2008, `narrow`, `multi-valued`, `synthetic`: 0 B. Their responses are constant (cycle AN) or
  absent.
- Gate: review by another family, the reactor and conformance, and a controlled pair on
  `responses` and rsa2008.

**Files.** `DecisionRuleResponse.java`; optionally `Obligation.java` and `Advice.java` (`merge`);
`ResponseFactoryTest.java`. It is separate from P-FIXED3 R4 (cycle AY, the assignment expressions).

**Test plan (outcomes only).**
- `of` against `builder` for 0, 1, 2, 3 or more assignments: grouped, interleaved, and duplicate
  input. Pin the same `getAttributes()` sequence, `getAttribute(id)`, `equals` in both directions,
  `hashCode`, and `toString`.
- Unknown ids give an empty unmodifiable set.
- `merge` and `Builder.from` across factory-built and builder-built responses.
- The two external compatibility tests, unchanged.

**Open questions as drafted.**
1. The builder constructor stays eager; the collection constructor is lazy: recommend yes.
2. Keep R4 separate, and leave `merge` as is (it reads `getAttributes()`, so it does not build the
   multimap): recommend yes.

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
