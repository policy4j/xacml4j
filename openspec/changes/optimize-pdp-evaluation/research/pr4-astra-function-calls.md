<!-- Lane PR4 of the second profiling swarm (after cycle H), Codex GPT-6 Astra, read-only; focus: the function call path; report from the NTM pane, paths made repository-relative. -->

FINAL REPORT (xacml4j JMH benchmark engineer, GPT-6) — PR4

**Prioritize a scalar, directly bound path for standard binary predicates.** Cycle H still allocates an argument list, its backing array, and a reflective argument array for each eligible comparison. These account for **41.81% of sampled allocation in synthetic:1000/all**, equivalent to approximately **56.8 KB per decide**. Custom providers need the existing fallback.

This is read-only analysis. No files were written, and no builds, tests, benchmarks, profilers, or Git/br writes were run.

Evidence paths below use these prefixes:

- `C` = `xacml-core/src/main/java/org/xacml4j/v30`
- `T` = `xacml-core/src/test/java/org/xacml4j/v30`
- `E` = `openspec/changes/optimize-pdp-evaluation`
- `P21` = `E/evidence/benchmarks/jmh/021-profile-decide-cycle-H`
- Collapsed-stack line numbers refer to decompressed text.

Recorded cycle H results used to scale the profile shares:

| Workload | Time ± JMH error, µs/decide | B/decide |
|---|---:|---:|
| R: rsa2008/all | 8.93 ± 0.290 | 18,774 |
| S: synthetic:1000/all | 100.4 ± 2.182 | 135,900 |
| F: synthetic:1000/first-deny | 1.084 ± 0.0243 | 4,077.7 |

Sources: `E/evidence/benchmarks/jmh/020-cycleH-after/summary.md:13,20,21`; `LEDGER.md:179,181,183`. Run 021 records Oracle GraalVM 25.0.4+7, C2, Apple M2 Pro, G1 and a 2 GB heap (`P21/env.txt:17`). Its profiling command uses one fork; the recorded performance pair uses three forks × five measurement iterations (`P21/command.txt:2`; run 020 summary).

**All gains below are UNVERIFIED projections.** Allocation estimates multiply sampled shares by run 020 bytes. Time estimates use an explicit assumption of recovering half the identified overhead; that fraction is a planning scenario, not a measured result.

The checked default call path is:

`Match.match → FunctionSpecImpl.invoke(pair) → doInvoke → DefaultFunctionInvocation.invoke → factory lambda → Method.invoke`.

The pair overload creates `ArrayList<>(2)`; `DefaultFunctionInvocation` creates `Object[]` and copies the arguments. `PolicyToPlatformFunctionInvocation.invoke(Object...)` receives that existing array—it does not itself require another array. The factory lambda still calls reflection. JVM `DirectMethodHandleAccessor` frames therefore do not establish that xacml4j has a typed, directly bound invocation path (`C/policy/Match.java:121`; `C/policy/function/FunctionSpecBuilder.java:378,421`; `C/policy/function/impl/DefaultFunctionInvocation.java:96,146`; `C/policy/function/FunctionInvocationFactory.java:58,73`).

**H1. Bind standard binary predicates directly and pass operands as scalars**

- **Evidence:** S allocation has 3,607 samples for the pair list’s backing array, 3,404 for the list, and 3,468 for the reflective array: **10,479/25,066 = 41.81%**. See `P21/alloc-synthetic_1000-all/top-alloc-synthetic_1000-all.md:78,80,82`; collapsed lines 56 and 73 show the `Match.match` callers. The remaining allocations are explicit in `FunctionSpecBuilder.java:382` and `DefaultFunctionInvocation.java:96`.
  
  S CPU has 406 self samples in `DefaultFunctionInvocation`, 385 reflection/MethodHandle leaf samples attributable to the function factory, and 271 samples whose deepest project frame is `evaluateInPlace`: **1,062/10,124 = 10.49%**. This excludes predicate implementation work and reflection above the JMH benchmark entry. Representative factory stack: `P21/cpu-synthetic_1000-all/collapsed-cpu.csv.gz:928`.

- **Change:** During standard-function registration, attach an internal, immutable binary binding to eligible specs. Initially bind the existing `EqualityPredicates.equals(StringVal,StringVal)` and `eq(IntegerVal,IntegerVal)` methods; extend to other measured binary predicates using their existing implementations. Select by exact implementation method and trusted factory provenance, **not function URI alone**.
  
  In `FunctionSpecImpl.invoke(context,arg0,arg1)`, evaluate operands in the existing order into local variables and invoke the binding without either container. Keep the current path for custom invocation factories/specs, optional/variadic/lazy functions, and runtime validation initially. This preserves annotation-derived metadata and policy validation. Relevant registration points: `JavaMethodToFunctionSpecConverter.java:244,251`; `FunctionProviderBuilder.java:83,98`; `impl/XacmlDefaultFunctions.java:38`.
  
  A typed method-reference adapter is the smallest candidate. Generated bindings are another implementation option, not a demonstrated additional gain.

- **Expected gain:**

  | Workload | Container allocation opportunity | Half-overhead time scenario |
  |---|---:|---:|
  | S | `135,900 × 10,479/25,066` = **56,814 B** | `100.4 × 1,062/10,124 × ½` = **5.27 µs** |
  | F | `4,077.7 × 3,618/66,379` = **222 B** | `1.084 × 423/10,151 × ½` = **0.0226 µs** |
  | R | `18,774 × 654/38,059` = **323 B** | `8.93 × 98/10,176 × ½` = **0.0430 µs** |

  R figures are the **whole sampled Match argument-container opportunity**; the portion covered by particular bindings remains UNVERIFIED. S’s generator uses `string-equal` throughout, so it directly exercises the first binding (`xacml-jmh/src/main/java/org/xacml4j/jmh/corpus/SyntheticCorpus.java:140`). Its `all` workload rotates seven requests, rather than performing a full scan on every operation (`SyntheticCorpus.java:80`).

- **Risk:** Preserve XACML 3.0 §7.4 type/evaluation behavior and §7.6 Match behavior. Keep current bag order and error handling; the recorded §7.6 deviation is separately tracked and must not be silently corrected here (`E/research/synthesis.md:73`; `C/policy/Match.java:120`). Invoke existing equality methods: double equality deliberately differs from general `Value.equals` for NaN and signed zero (`EqualityPredicates.java:70,193`, citing §A.3.1).
  
  Bindings must hold no mutable per-call operands. No public API change is needed. Replacing the public varargs SPI or adding required abstract methods would be **BREAKING**.
  
  Required proof: direct-versus-existing binding comparisons for every bound predicate, validation on/off, operand evaluation order, failures, custom factory interception, and concurrent/reentrant calls. Retain `FunctionInvocationCharacterizationTest.java:149,181,208,250` and `MatchTest.java:204`; add a custom spec using a standard URI to prove URI-based bypass cannot occur.

- **Checked / not checked:** Source, registration path, corpus and collapsed allocation/CPU attribution checked. Direct binding performance, exact RSA predicate coverage, JIT inlining and candidate correctness are UNVERIFIED. No tests run.

**H2. Remove redundant reflection exception wrappers while preserving the public function error**

- **Evidence:** On R, stacks with deepest project caller `FunctionInvocationFactory$Default.doInvoke` attribute **759 allocation samples** to `InvocationTargetException` construction and **692** to `RuntimeException` construction: **1,451/38,059 = 3.81%**. Corresponding CPU construction subtrees contain **387 + 354 = 741/10,176 = 7.28%**.
  
  Witnesses: `P21/alloc-rsa2008-all/collapsed-alloc.csv.gz:719,273` and `P21/cpu-rsa2008-all/collapsed-cpu.csv.gz:418,1206`. These include wrapper backtrace storage, not only exception objects. The factory catches `Throwable` and constructs `RuntimeException`; `DefaultFunctionInvocation` subsequently extracts a cause and constructs `FunctionInvocationException` (`FunctionInvocationFactory.java:73`; `DefaultFunctionInvocation.java:148,157`).

- **Change:** Introduce an internal invocation backend that can propagate the original throwable into the existing status-preserving adaptation. Direct built-in bindings, or a prelinked MethodHandle backend, can avoid constructing `InvocationTargetException`; an internal throwable-capable contract can also avoid the intermediate `RuntimeException`.
  
  Retain the public factory/SPI behavior and custom-factory fallback. Preserve the outer `FunctionInvocationException`, first-`EvaluationException` selection, and root-cause behavior. A MethodHandle backend retaining `Object[]` would test this wrapper hypothesis separately from H1’s scalar allocation hypothesis.

- **Expected gain:** R’s identified wrapper budget is:
  - Allocation: `18,774 × 1,451/38,059` = **716 B/decide**.
  - CPU: `8.93 × 741/10,176` = **0.650 µs/decide**; recovering half gives **0.325 µs**, about 3.6%.
  
  These are ceilings/scenarios for wrapper elimination across the sampled failing invocations. Restricting bindings to fewer functions reduces coverage. No wrapper-saving estimate is supported for S/F by these samples.

- **Risk:** Preserve Indeterminate and status behavior under XACML §§7.4 and 7.19.2. Those sections are retained in `openspec/changes/modernize-core-for-jdk28-valhalla/evidence/tests/083-apply-argument-check/oasis-xacml30-errata01-7.4-7.19.2.txt:8,23`.
  
  Do not disable stack traces globally or suppress original failures. Keep backend state immutable. Changing the public factory’s exception contract would be **BREAKING**; confine the optimization to internal adaptation.
  
  Required proof: `FunctionInvocationStatusTest.java:71,83,103,110,134` plus empty/multiple-value `one-and-only`, nested causes, and a target throwing `Error`. Compare status code, message, detail and retained cause—not just the decision enum.

- **Checked / not checked:** Wrapper construction, cause selection and sampled wrapper subtrees checked. Whether every sampled failing method can use the internal backend, diagnostic compatibility and actual gain remain UNVERIFIED. No tests run.

**H3. Replace generic normalization iterators with guarded indexed loops**

- **Evidence:** R allocates **866 `AbstractList$ListItr` samples and 863 `ArrayList$ListItr` samples** in `FunctionSpecImpl.normalize`: **1,729/38,059 = 4.54%**. See `P21/alloc-rsa2008-all/top-alloc-rsa2008-all.md:82,83`; collapsed witnesses at lines 200, 373, 730 and 908.
  
  Normalization’s deepest-project CPU attribution is **444/10,176 = 4.36%**; a representative stack is `P21/cpu-rsa2008-all/collapsed-cpu.csv.gz:1717`. Source creates both iterators at `FunctionSpecBuilder.java:537,538` and passes them through mandatory/optional/variadic normalization.

- **Change:** Use indexed traversal for the internal parameter `ArrayList` and known `RandomAccess` argument lists. Preserve an iterator fallback for other lists. Keep the fresh normalized list, optional defaults, variadic sentinel handling, extra arguments and subsequent evaluation unchanged.
  
  This targets the generic path still reached through `Apply.evaluate` and `FunctionReference.invoke` (`C/policy/Apply.java:264`; `C/policy/FunctionReference.java:95`). It does not claim H1’s already-skipped pair normalization again.

- **Expected gain:** R’s iterator opportunity is `18,774 × 1,729/38,059` = **853 B/decide** if both sampled iterator allocations disappear. Half the normalization CPU budget is `8.93 × 444/10,176 × ½` = **0.195 µs/decide**. Coverage retained on the fallback reduces these figures. No S/F gain is estimated from the checked samples.

- **Risk:** Preserve XACML §7.4 type/evaluation behavior, lazy function behavior and existing validation contracts. Indexed access on an arbitrary linked list could increase cost; hence the guard. Do not mutate caller lists or reuse validator-visible lists. No API change is required.
  
  Required proof: characterization tests for eager order, defaults, variadic arguments and lazy evaluation (`FunctionInvocationCharacterizationTest.java:78,91,104,117,134`), retained-validator lists at line 208, and equivalent calls using `ArrayList`, `LinkedList`, empty lists and incorrect arity.

- **Checked / not checked:** Allocation sites, normalization source and generic callers checked. Candidate traversal equivalence and RandomAccess coverage are UNVERIFIED. No tests run.

**Refuted ideas**

- **“Reflection consumes approximately 100%, so replacing it removes approximately 100%.”** The near-total frames include the JMH harness invoking the benchmark. Count reflection below the function factory only. Witness: the prefix of `P21/cpu-synthetic_1000-all/collapsed-cpu.csv.gz:1`; compare the 97.1% inclusive rows in its top table with H1’s 3.80% attributable reflection leaves.
- **“Replacing `Method.invoke` alone eliminates argument allocation.”** The list and arrays are explicitly allocated upstream. A backend still accepting `Object[]` leaves those sites intact (`FunctionSpecBuilder.java:382`; `DefaultFunctionInvocation.java:96`).
- **“Use `Value.equals` for every standard equality predicate.”** Double policy equality uses primitive comparison deliberately (`EqualityPredicates.java:193`).
- **“Reuse one argument array/list per function or thread.”** Custom invocations may mutate lists, and validators may retain them. Shared buffers also require reentrancy protection. Existing tests explicitly preserve these behaviors (`FunctionInvocationCharacterizationTest.java:181,208`).
- **“The small first-deny gain justifies stopping.”** The proposal targets every own-code allocation site and requires further research after rounds below 5% (`E/proposal.md:82,87`).

**Open questions and verification limits**

The next measurement should isolate H1’s scalar binding from H2’s exception backend. Each needs its own JMH pair under the recorded JDK/C2 configuration, with allocation profiling and unchanged decision assertions. None of these projections is an acceptance result.

The RSA fraction attributable specifically to `integer-equal` and other individual Match predicates remains UNVERIFIED; the synthetic generator demonstrates string equality coverage only. Shared-wrapper allocations do not identify individual function IDs reliably.

I read the contract, proposal, design D7/D8/D14, ledger, first-round synthesis, current sources/tests, and recorded profiles 004/016/021. Commands were scoped `rtk rg`, `rtk proxy sed`, read-only `rtk git --no-optional-locks status`, and in-memory `rtk proxy python3 -B -c` decompression/aggregation. No benchmark code was added. The main residual risks are sample-to-byte extrapolation, unmeasured binding coverage, and preserving custom-provider/validation behavior.
