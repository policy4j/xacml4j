total samples: 10176

## top 30 frames by self samples

| Share | Samples | Frame |
| --- | --- | --- |
| 8.7% | 886 | `itable stub` |
| 4.4% | 450 | `java_lang_Throwable::fill_in_stack_trace` |
| 2.9% | 291 | `BacktraceBuilder::push` |
| 2.1% | 211 | `org/xacml4j/v30/policy/Rule.evaluate` |
| 2.0% | 203 | `java/lang/String.equals` |
| 2.0% | 201 | `com/google/common/collect/RegularImmutableMultiset.create` |
| 1.9% | 197 | `jdk/internal/util/ArraysSupport.hashCode` |
| 1.7% | 173 | `org/xacml4j/v30/policy/DescendantEvaluationContext.isValidateFuncParamsAtRuntime` |
| 1.5% | 149 | `java/lang/String.hashCode` |
| 1.5% | 148 | `AccessInternal::PostRuntimeDispatch<G1BarrierSet::AccessBarrier<1335398ull, G1BarrierSet>, (AccessInternal::BarrierType)1, 1335398ull>::oop_access_barrier` |
| 1.4% | 146 | `org/xacml4j/v30/policy/DescendantEvaluationContext.resolve` |
| 1.4% | 145 | `java/util/ArrayList$Itr.next` |
| 1.3% | 137 | `org/xacml4j/v30/types/Value$OtherValue.hashCode` |
| 1.3% | 130 | `com/google/common/collect/RegularImmutableMultiset.hashFloodingDetected` |
| 1.2% | 127 | `org/xacml4j/v30/policy/function/impl/DefaultFunctionInvocation.copyInto` |
| 1.2% | 126 | `java/util/Objects.hashCode` |
| 1.2% | 123 | `pthread_jit_write_protect_np` |
| 1.1% | 115 | `java/util/ArrayList.add` |
| 1.1% | 111 | `java/util/concurrent/ConcurrentHashMap.putVal` |
| 1.0% | 105 | `org/xacml4j/v30/policy/DescendantEvaluationContext.getCurrentPolicy` |
| 1.0% | 105 | `java/util/concurrent/CopyOnWriteArrayList.getArray` |
| 1.0% | 103 | `org/xacml4j/v30/policy/Target.match` |
| 0.9% | 95 | `org/xacml4j/v30/policy/function/impl/HigherOrderFunctions.validateArguments` |
| 0.9% | 95 | `vtable stub` |
| 0.8% | 86 | `java/util/concurrent/ConcurrentHashMap.get` |
| 0.8% | 85 | `org/xacml4j/v30/policy/DescendantEvaluationContext.getCurrentPolicySetIDReference` |
| 0.8% | 84 | `org/xacml4j/v30/policy/function/impl/DefaultFunctionInvocation.invoke` |
| 0.8% | 80 | `java/lang/Enum.hashCode` |
| 0.7% | 74 | `java/util/ArrayList.<init>` |
| 0.7% | 69 | `org/xacml4j/v30/policy/DescendantEvaluationContext.addEvaluationResult` |

## top 30 frames by total samples

| Share | Samples | Frame |
| --- | --- | --- |
| 98.1% | 9984 | `java/lang/Thread.runWith` |
| 98.1% | 9984 | `java/util/concurrent/ThreadPoolExecutor.runWorker` |
| 98.1% | 9984 | `java/lang/Thread.run` |
| 98.1% | 9984 | `java/util/concurrent/ThreadPoolExecutor$Worker.run` |
| 98.1% | 9983 | `java/util/concurrent/Executors$RunnableAdapter.call` |
| 98.1% | 9983 | `org/openjdk/jmh/runner/BenchmarkHandler$BenchmarkTask.call` |
| 98.1% | 9983 | `java/util/concurrent/FutureTask.run` |
| 98.1% | 9982 | `java/lang/invoke/LambdaForm$MH.0x0000000c01061c00.invoke` |
| 98.1% | 9982 | `java/lang/reflect/Method.invoke` |
| 98.1% | 9982 | `java/lang/invoke/LambdaForm$MH.0x0000000c01062000.invokeExact_MT` |
| 98.1% | 9982 | `org/xacml4j/jmh/pdp/jmh_generated/PdpDecideBenchmark_decide_jmhTest.decide_AverageTime` |
| 98.1% | 9982 | `jdk/internal/reflect/DirectMethodHandleAccessor.invokeImpl` |
| 98.1% | 9982 | `java/lang/invoke/DirectMethodHandle$Holder.invokeSpecial` |
| 98.1% | 9982 | `jdk/internal/reflect/DirectMethodHandleAccessor.invoke` |
| 98.1% | 9980 | `org/xacml4j/jmh/pdp/jmh_generated/PdpDecideBenchmark_decide_jmhTest.decide_avgt_jmhStub` |
| 98.1% | 9979 | `org/xacml4j/jmh/pdp/PdpDecideBenchmark.decide` |
| 98.0% | 9977 | `org/xacml4j/v30/pdp/DefaultPolicyDecisionPoint.decide` |
| 97.3% | 9906 | `org/xacml4j/v30/spi/pdp/RequestContextHandlerChain.handle` |
| 97.3% | 9897 | `org/xacml4j/v30/pdp/request/MultipleResourcesBudgetHandler.handle` |
| 97.2% | 9896 | `org/xacml4j/v30/pdp/request/AbstractRequestContextHandler.handleNext` |
| 97.2% | 9890 | `org/xacml4j/v30/pdp/request/MultipleResourcesViaRequestReferencesHandler.handle` |
| 97.1% | 9882 | `org/xacml4j/v30/pdp/request/MultipleResourcesViaRepeatingAttributesHandler.handle` |
| 97.0% | 9871 | `org/xacml4j/v30/pdp/request/MultipleResourcesBudgetHandler$Checkpoint.handle` |
| 96.8% | 9848 | `org/xacml4j/v30/pdp/request/MultipleResourcesViaXPathExpressionLegacyHandler.handle` |
| 96.3% | 9804 | `org/xacml4j/v30/pdp/request/MultipleResourcesViaXPathExpressionHandler.handle` |
| 94.4% | 9603 | `org/xacml4j/v30/pdp/request/MultipleResourcesBudgetHandler$Budget.requestDecision` |
| 94.3% | 9598 | `org/xacml4j/v30/pdp/DefaultPolicyDecisionPoint$NumberedDecisions.requestDecision` |
| 94.3% | 9598 | `org/xacml4j/v30/pdp/PolicyDecisionPointContextFactory$DefaultPolicyDecisionPointContextFactory$1.requestDecision` |
| 94.3% | 9597 | `org/xacml4j/v30/pdp/DefaultPolicyDecisionPoint.requestDecision` |
| 89.3% | 9085 | `org/xacml4j/v30/policy/PolicySet.evaluate` |

## top 30 frames by deepest project frame (-> leaf frame or allocated type) samples

| Share | Samples | Frame |
| --- | --- | --- |
| 2.1% | 211 | `org/xacml4j/v30/policy/Rule.evaluate` |
| 2.0% | 207 | `org/xacml4j/v30/policy/function/FunctionInvocationFactory$Default.doInvoke  ->  java_lang_Throwable::fill_in_stack_trace` |
| 2.0% | 201 | `org/xacml4j/v30/BagOfValues.<init>  ->  com/google/common/collect/RegularImmutableMultiset.create` |
| 1.9% | 190 | `org/xacml4j/v30/policy/DescendantEvaluationContext.isValidateFuncParamsAtRuntime  ->  itable stub` |
| 1.7% | 173 | `org/xacml4j/v30/policy/DescendantEvaluationContext.isValidateFuncParamsAtRuntime` |
| 1.5% | 156 | `org/xacml4j/v30/types/Value$OtherValue.hashCode  ->  jdk/internal/util/ArraysSupport.hashCode` |
| 1.5% | 154 | `org/xacml4j/v30/policy/function/FunctionInvocationFactory$Default.doInvoke  ->  BacktraceBuilder::push` |
| 1.4% | 146 | `org/xacml4j/v30/policy/DescendantEvaluationContext.resolve` |
| 1.4% | 141 | `org/xacml4j/v30/policy/DescendantEvaluationContext.resolve  ->  itable stub` |
| 1.3% | 137 | `org/xacml4j/v30/types/Value$OtherValue.hashCode` |
| 1.3% | 130 | `org/xacml4j/v30/BagOfValues.<init>  ->  com/google/common/collect/RegularImmutableMultiset.hashFloodingDetected` |
| 1.2% | 127 | `org/xacml4j/v30/policy/function/impl/DefaultFunctionInvocation.copyInto` |
| 1.2% | 122 | `org/xacml4j/v30/CoreException.<init>  ->  java_lang_Throwable::fill_in_stack_trace` |
| 1.2% | 121 | `org/xacml4j/v30/policy/function/impl/BagFunctions.oneAndOnlyImpl  ->  java_lang_Throwable::fill_in_stack_trace` |
| 1.2% | 120 | `org/xacml4j/v30/types/Value$OtherValue.hashCode  ->  java/lang/String.hashCode` |
| 1.1% | 109 | `org/xacml4j/v30/policy/function/FunctionSpecBuilder$FunctionSpecImpl.normalizeMandatoryParameters  ->  java/util/ArrayList$Itr.next` |
| 1.0% | 105 | `org/xacml4j/v30/policy/DescendantEvaluationContext.getCurrentPolicy` |
| 1.0% | 103 | `org/xacml4j/v30/policy/Target.match` |
| 1.0% | 103 | `org/xacml4j/v30/types/Value$OtherValue.equals  ->  java/lang/String.equals` |
| 0.9% | 95 | `org/xacml4j/v30/policy/function/impl/HigherOrderFunctions.validateArguments` |
| 0.9% | 87 | `org/xacml4j/v30/policy/DescendantEvaluationContext.getCurrentPolicy  ->  itable stub` |
| 0.8% | 85 | `org/xacml4j/v30/policy/DescendantEvaluationContext.getCurrentPolicySetIDReference` |
| 0.8% | 84 | `org/xacml4j/v30/policy/function/impl/DefaultFunctionInvocation.invoke` |
| 0.8% | 79 | `org/xacml4j/v30/types/Value$OtherValue.hashCode  ->  java/util/Objects.hashCode` |
| 0.8% | 78 | `org/xacml4j/v30/pdp/DefaultEvaluationContextHandler.resolve  ->  java/util/concurrent/ConcurrentHashMap.get` |
| 0.8% | 77 | `org/xacml4j/v30/policy/DescendantEvaluationContext.addEvaluationResult  ->  itable stub` |
| 0.7% | 70 | `org/xacml4j/v30/types/Value$OtherValue.hashCode  ->  java/lang/Enum.hashCode` |
| 0.7% | 70 | `org/xacml4j/v30/CoreException.<init>  ->  BacktraceBuilder::push` |
| 0.7% | 69 | `org/xacml4j/v30/policy/function/FunctionInvocationFactory$Default.doInvoke  ->  AccessInternal::PostRuntimeDispatch<G1BarrierSet::AccessBarrier<1335398ull, G1BarrierSet>, (AccessInternal::BarrierType)1, 1335398ull>::oop_access_barrier` |
| 0.7% | 69 | `org/xacml4j/v30/policy/DescendantEvaluationContext.addEvaluationResult` |
