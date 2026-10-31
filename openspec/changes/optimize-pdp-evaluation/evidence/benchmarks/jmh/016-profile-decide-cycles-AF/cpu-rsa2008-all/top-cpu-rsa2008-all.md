total samples: 10102

## top 30 frames by self samples

| Share | Samples | Frame |
| --- | --- | --- |
| 8.5% | 860 | `itable stub` |
| 3.7% | 375 | `java_lang_Throwable::fill_in_stack_trace` |
| 2.9% | 293 | `BacktraceBuilder::push` |
| 2.1% | 209 | `org/xacml4j/v30/policy/DescendantEvaluationContext.resolve` |
| 2.0% | 204 | `com/google/common/collect/RegularImmutableMultiset.create` |
| 2.0% | 197 | `java/lang/String.equals` |
| 1.7% | 169 | `org/xacml4j/v30/policy/DescendantEvaluationContext.isValidateFuncParamsAtRuntime` |
| 1.5% | 151 | `java/util/Objects.hashCode` |
| 1.5% | 150 | `jdk/internal/util/ArraysSupport.hashCode` |
| 1.5% | 147 | `java/lang/String.hashCode` |
| 1.4% | 142 | `AccessInternal::PostRuntimeDispatch<G1BarrierSet::AccessBarrier<1335398ull, G1BarrierSet>, (AccessInternal::BarrierType)1, 1335398ull>::oop_access_barrier` |
| 1.3% | 135 | `org/xacml4j/v30/policy/function/impl/DefaultFunctionInvocation.copyInto` |
| 1.3% | 135 | `java/util/ArrayList.add` |
| 1.3% | 135 | `org/xacml4j/v30/policy/Rule.evaluate` |
| 1.3% | 129 | `com/google/common/collect/RegularImmutableMultiset.hashFloodingDetected` |
| 1.2% | 126 | `pthread_jit_write_protect_np` |
| 1.1% | 109 | `org/xacml4j/v30/policy/function/impl/HigherOrderFunctions.validateArguments` |
| 1.0% | 106 | `java/util/ArrayList$Itr.next` |
| 1.0% | 105 | `org/xacml4j/v30/policy/function/impl/DefaultFunctionInvocation.invoke` |
| 1.0% | 100 | `java/util/concurrent/ConcurrentHashMap.putVal` |
| 1.0% | 97 | `java/util/concurrent/ConcurrentHashMap.get` |
| 0.9% | 94 | `vtable stub` |
| 0.9% | 91 | `java/util/concurrent/CopyOnWriteArrayList.getArray` |
| 0.9% | 88 | `org/xacml4j/v30/policy/function/FunctionSpecBuilder$FunctionSpecImpl.doInvoke` |
| 0.9% | 88 | `org/xacml4j/v30/types/Value$OtherValue.hashCode` |
| 0.8% | 79 | `org/xacml4j/v30/policy/BaseCompositeDecisionRule$StagedResponses.addObligations` |
| 0.8% | 79 | `org/xacml4j/v30/policy/Target.match` |
| 0.8% | 78 | `org/xacml4j/v30/policy/DescendantEvaluationContext.getCurrentPolicy` |
| 0.8% | 78 | `org/xacml4j/v30/policy/DescendantEvaluationContext.getCurrentPolicySetIDReference` |
| 0.7% | 75 | `java/util/Collections$UnmodifiableMap.isEmpty` |

## top 30 frames by total samples

| Share | Samples | Frame |
| --- | --- | --- |
| 98.9% | 9990 | `java/util/concurrent/ThreadPoolExecutor$Worker.run` |
| 98.9% | 9990 | `java/util/concurrent/ThreadPoolExecutor.runWorker` |
| 98.9% | 9990 | `java/lang/Thread.run` |
| 98.9% | 9990 | `java/util/concurrent/FutureTask.run` |
| 98.9% | 9990 | `java/util/concurrent/Executors$RunnableAdapter.call` |
| 98.9% | 9990 | `java/lang/Thread.runWith` |
| 98.9% | 9989 | `java/lang/invoke/DirectMethodHandle$Holder.invokeSpecial` |
| 98.9% | 9989 | `java/lang/reflect/Method.invoke` |
| 98.9% | 9989 | `org/openjdk/jmh/runner/BenchmarkHandler$BenchmarkTask.call` |
| 98.9% | 9989 | `org/xacml4j/jmh/pdp/jmh_generated/PdpDecideBenchmark_decide_jmhTest.decide_AverageTime` |
| 98.9% | 9989 | `java/lang/invoke/LambdaForm$MH.0x0000000c01062000.invokeExact_MT` |
| 98.9% | 9989 | `jdk/internal/reflect/DirectMethodHandleAccessor.invoke` |
| 98.9% | 9989 | `java/lang/invoke/LambdaForm$MH.0x0000000c01061c00.invoke` |
| 98.9% | 9989 | `jdk/internal/reflect/DirectMethodHandleAccessor.invokeImpl` |
| 98.9% | 9988 | `org/xacml4j/jmh/pdp/jmh_generated/PdpDecideBenchmark_decide_jmhTest.decide_avgt_jmhStub` |
| 98.9% | 9987 | `org/xacml4j/jmh/pdp/PdpDecideBenchmark.decide` |
| 98.8% | 9985 | `org/xacml4j/v30/pdp/DefaultPolicyDecisionPoint.decide` |
| 98.1% | 9913 | `org/xacml4j/v30/spi/pdp/RequestContextHandlerChain.handle` |
| 98.1% | 9908 | `org/xacml4j/v30/pdp/request/AbstractRequestContextHandler.handleNext` |
| 98.1% | 9908 | `org/xacml4j/v30/pdp/request/MultipleResourcesBudgetHandler.handle` |
| 98.0% | 9901 | `org/xacml4j/v30/pdp/request/MultipleResourcesViaRequestReferencesHandler.handle` |
| 97.9% | 9890 | `org/xacml4j/v30/pdp/request/MultipleResourcesViaRepeatingAttributesHandler.handle` |
| 97.8% | 9880 | `org/xacml4j/v30/pdp/request/MultipleResourcesBudgetHandler$Checkpoint.handle` |
| 97.7% | 9873 | `org/xacml4j/v30/pdp/request/MultipleResourcesViaXPathExpressionLegacyHandler.handle` |
| 97.3% | 9834 | `org/xacml4j/v30/pdp/request/MultipleResourcesViaXPathExpressionHandler.handle` |
| 95.3% | 9627 | `org/xacml4j/v30/pdp/request/MultipleResourcesBudgetHandler$Budget.requestDecision` |
| 95.3% | 9626 | `org/xacml4j/v30/pdp/DefaultPolicyDecisionPoint.requestDecision` |
| 95.3% | 9626 | `org/xacml4j/v30/pdp/DefaultPolicyDecisionPoint$NumberedDecisions.requestDecision` |
| 95.3% | 9626 | `org/xacml4j/v30/pdp/PolicyDecisionPointContextFactory$DefaultPolicyDecisionPointContextFactory$1.requestDecision` |
| 90.8% | 9176 | `org/xacml4j/v30/policy/PolicySet.evaluate` |

## top 30 frames by deepest project frame (-> leaf frame or allocated type) samples

| Share | Samples | Frame |
| --- | --- | --- |
| 2.1% | 209 | `org/xacml4j/v30/policy/DescendantEvaluationContext.resolve` |
| 2.0% | 204 | `org/xacml4j/v30/BagOfValues.<init>  ->  com/google/common/collect/RegularImmutableMultiset.create` |
| 1.8% | 182 | `org/xacml4j/v30/policy/function/FunctionInvocationFactory$Default.doInvoke  ->  java_lang_Throwable::fill_in_stack_trace` |
| 1.7% | 175 | `org/xacml4j/v30/policy/DescendantEvaluationContext.isValidateFuncParamsAtRuntime  ->  itable stub` |
| 1.7% | 170 | `org/xacml4j/v30/policy/DescendantEvaluationContext.resolve  ->  itable stub` |
| 1.7% | 169 | `org/xacml4j/v30/policy/DescendantEvaluationContext.isValidateFuncParamsAtRuntime` |
| 1.4% | 142 | `org/xacml4j/v30/policy/function/FunctionInvocationFactory$Default.doInvoke  ->  BacktraceBuilder::push` |
| 1.3% | 135 | `org/xacml4j/v30/policy/function/impl/DefaultFunctionInvocation.copyInto` |
| 1.3% | 135 | `org/xacml4j/v30/policy/Rule.evaluate` |
| 1.3% | 129 | `org/xacml4j/v30/BagOfValues.<init>  ->  com/google/common/collect/RegularImmutableMultiset.hashFloodingDetected` |
| 1.2% | 123 | `org/xacml4j/v30/types/Value$OtherValue.hashCode  ->  java/lang/String.hashCode` |
| 1.1% | 109 | `org/xacml4j/v30/types/Value$OtherValue.hashCode  ->  jdk/internal/util/ArraysSupport.hashCode` |
| 1.1% | 109 | `org/xacml4j/v30/policy/function/impl/HigherOrderFunctions.validateArguments` |
| 1.1% | 109 | `org/xacml4j/v30/types/Value$OtherValue.hashCode  ->  java/util/Objects.hashCode` |
| 1.0% | 105 | `org/xacml4j/v30/policy/function/impl/DefaultFunctionInvocation.invoke` |
| 1.0% | 105 | `org/xacml4j/v30/policy/function/impl/BagFunctions.oneAndOnlyImpl  ->  java_lang_Throwable::fill_in_stack_trace` |
| 0.9% | 89 | `org/xacml4j/v30/pdp/DefaultEvaluationContextHandler.resolve  ->  java/util/concurrent/ConcurrentHashMap.get` |
| 0.9% | 88 | `org/xacml4j/v30/policy/function/FunctionSpecBuilder$FunctionSpecImpl.doInvoke` |
| 0.9% | 88 | `org/xacml4j/v30/types/Value$OtherValue.hashCode` |
| 0.9% | 88 | `org/xacml4j/v30/CoreException.<init>  ->  java_lang_Throwable::fill_in_stack_trace` |
| 0.8% | 85 | `org/xacml4j/v30/types/Value$OtherValue.equals  ->  java/lang/String.equals` |
| 0.8% | 84 | `org/xacml4j/v30/policy/function/FunctionSpecBuilder$FunctionSpecImpl.normalizeMandatoryParameters  ->  java/util/ArrayList$Itr.next` |
| 0.8% | 83 | `org/xacml4j/v30/policy/function/impl/BagFunctions.oneAndOnlyImpl  ->  BacktraceBuilder::push` |
| 0.8% | 79 | `org/xacml4j/v30/policy/DescendantEvaluationContext.addEvaluationResult  ->  itable stub` |
| 0.8% | 79 | `org/xacml4j/v30/policy/BaseCompositeDecisionRule$StagedResponses.addObligations` |
| 0.8% | 79 | `org/xacml4j/v30/policy/Target.match` |
| 0.8% | 78 | `org/xacml4j/v30/policy/DescendantEvaluationContext.getCurrentPolicy` |
| 0.8% | 78 | `org/xacml4j/v30/policy/DescendantEvaluationContext.getCurrentPolicySetIDReference` |
| 0.8% | 78 | `org/xacml4j/v30/policy/DescendantEvaluationContext.getCurrentPolicy  ->  itable stub` |
| 0.7% | 75 | `org/xacml4j/v30/pdp/DefaultPolicyDecisionPoint.getResolvedAttributes  ->  java/util/Collections$UnmodifiableMap.isEmpty` |
