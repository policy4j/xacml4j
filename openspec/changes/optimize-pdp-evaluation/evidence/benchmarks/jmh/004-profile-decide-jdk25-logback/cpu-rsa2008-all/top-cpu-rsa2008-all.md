total samples: 10106

## top 30 frames by self samples

| Share | Samples | Frame |
| --- | --- | --- |
| 8.8% | 886 | `itable stub` |
| 2.7% | 272 | `java_lang_Throwable::fill_in_stack_trace` |
| 2.5% | 252 | `java/util/stream/AbstractPipeline.copyInto` |
| 2.3% | 234 | `java/util/stream/AbstractPipeline.<init>` |
| 2.0% | 199 | `BacktraceBuilder::push` |
| 1.9% | 189 | `jdk/internal/util/ArraysSupport.hashCode` |
| 1.9% | 187 | `com/google/common/collect/RegularImmutableMultiset.hashFloodingDetected` |
| 1.8% | 186 | `java/lang/String.compareTo` |
| 1.8% | 184 | `com/google/common/collect/RegularImmutableMultiset.create` |
| 1.5% | 151 | `java/util/Objects.hashCode` |
| 1.4% | 144 | `java/lang/String.equals` |
| 1.3% | 131 | `java/lang/String.hashCode` |
| 1.1% | 114 | `org/xacml4j/v30/policy/DescendantEvaluationContext.resolve` |
| 1.1% | 111 | `java/util/concurrent/ConcurrentSkipListMap.doPut` |
| 1.0% | 106 | `org/xacml4j/v30/policy/function/impl/DefaultFunctionInvocation.copyInto` |
| 1.0% | 105 | `org/xacml4j/v30/policy/DescendantEvaluationContext.isValidateFuncParamsAtRuntime` |
| 1.0% | 102 | `org/xacml4j/v30/policy/Rule.evaluate` |
| 0.9% | 96 | `AccessInternal::PostRuntimeDispatch<G1BarrierSet::AccessBarrier<1335398ull, G1BarrierSet>, (AccessInternal::BarrierType)1, 1335398ull>::oop_access_barrier` |
| 0.9% | 92 | `vtable stub` |
| 0.8% | 78 | `org/xacml4j/v30/policy/function/FunctionSpecBuilder$FunctionSpecImpl.evaluate` |
| 0.8% | 78 | `java/util/concurrent/CopyOnWriteArrayList.getArray` |
| 0.8% | 77 | `org/xacml4j/v30/policy/function/FunctionSpecBuilder$FunctionSpecImpl.normalize` |
| 0.8% | 76 | `pthread_jit_write_protect_np` |
| 0.8% | 76 | `java/util/stream/ReferencePipeline$2.opWrapSink` |
| 0.7% | 73 | `org/xacml4j/v30/policy/function/FunctionSpecBuilder$FunctionSpecImpl.normalizeMandatoryParameters` |
| 0.7% | 72 | `java/util/Spliterator.getExactSizeIfKnown` |
| 0.7% | 72 | `org/xacml4j/v30/policy/function/impl/HigherOrderFunctions.validateArguments` |
| 0.7% | 71 | `java/util/ArrayList.add` |
| 0.7% | 69 | `java/util/LinkedList.linkLast` |
| 0.7% | 69 | `org/xacml4j/v30/BagOfValues$Builder.attributes` |

## top 30 frames by total samples

| Share | Samples | Frame |
| --- | --- | --- |
| 98.7% | 9979 | `java/util/concurrent/ThreadPoolExecutor$Worker.run` |
| 98.7% | 9979 | `java/lang/Thread.runWith` |
| 98.7% | 9979 | `java/util/concurrent/ThreadPoolExecutor.runWorker` |
| 98.7% | 9979 | `java/lang/Thread.run` |
| 98.7% | 9978 | `java/lang/invoke/LambdaForm$MH.0x00007c0001061c00.invoke` |
| 98.7% | 9978 | `java/lang/invoke/LambdaForm$MH.0x00007c0001062000.invokeExact_MT` |
| 98.7% | 9978 | `org/xacml4j/jmh/pdp/jmh_generated/PdpDecideBenchmark_decide_jmhTest.decide_AverageTime` |
| 98.7% | 9978 | `java/util/concurrent/Executors$RunnableAdapter.call` |
| 98.7% | 9978 | `java/lang/reflect/Method.invoke` |
| 98.7% | 9978 | `jdk/internal/reflect/DirectMethodHandleAccessor.invoke` |
| 98.7% | 9978 | `java/util/concurrent/FutureTask.run` |
| 98.7% | 9978 | `org/openjdk/jmh/runner/BenchmarkHandler$BenchmarkTask.call` |
| 98.7% | 9978 | `java/lang/invoke/DirectMethodHandle$Holder.invokeSpecial` |
| 98.7% | 9978 | `jdk/internal/reflect/DirectMethodHandleAccessor.invokeImpl` |
| 98.7% | 9976 | `org/xacml4j/jmh/pdp/jmh_generated/PdpDecideBenchmark_decide_jmhTest.decide_avgt_jmhStub` |
| 98.7% | 9975 | `org/xacml4j/jmh/pdp/PdpDecideBenchmark.decide` |
| 98.7% | 9971 | `org/xacml4j/v30/pdp/DefaultPolicyDecisionPoint.decide` |
| 97.9% | 9894 | `org/xacml4j/v30/spi/pdp/RequestContextHandlerChain.handle` |
| 97.9% | 9889 | `org/xacml4j/v30/pdp/request/MultipleResourcesBudgetHandler.handle` |
| 97.8% | 9888 | `org/xacml4j/v30/pdp/request/AbstractRequestContextHandler.handleNext` |
| 97.8% | 9886 | `org/xacml4j/v30/pdp/request/MultipleResourcesViaRequestReferencesHandler.handle` |
| 97.8% | 9881 | `org/xacml4j/v30/pdp/request/MultipleResourcesViaRepeatingAttributesHandler.handle` |
| 96.7% | 9775 | `org/xacml4j/v30/pdp/request/MultipleResourcesBudgetHandler$Checkpoint.handle` |
| 96.7% | 9774 | `org/xacml4j/v30/pdp/request/MultipleResourcesViaXPathExpressionLegacyHandler.handle` |
| 94.3% | 9535 | `org/xacml4j/v30/pdp/request/MultipleResourcesViaXPathExpressionHandler.handle` |
| 89.7% | 9069 | `org/xacml4j/v30/pdp/request/MultipleResourcesBudgetHandler$Budget.requestDecision` |
| 89.7% | 9069 | `org/xacml4j/v30/pdp/PolicyDecisionPointContextFactory$DefaultPolicyDecisionPointContextFactory$1.requestDecision` |
| 89.7% | 9069 | `org/xacml4j/v30/pdp/DefaultPolicyDecisionPoint.requestDecision` |
| 89.7% | 9069 | `org/xacml4j/v30/pdp/DefaultPolicyDecisionPoint$NumberedDecisions.requestDecision` |
| 83.5% | 8438 | `org/xacml4j/v30/policy/PolicySet.evaluate` |

## top 30 frames by deepest project frame (-> leaf frame or allocated type) samples

| Share | Samples | Frame |
| --- | --- | --- |
| 1.9% | 196 | `org/xacml4j/v30/Attribute.getValuesByType  ->  java/util/stream/AbstractPipeline.<init>` |
| 1.9% | 187 | `org/xacml4j/v30/BagOfValues.<init>  ->  com/google/common/collect/RegularImmutableMultiset.hashFloodingDetected` |
| 1.8% | 186 | `org/xacml4j/v30/policy/RootEvaluationContext.addEvaluationResult  ->  java/lang/String.compareTo` |
| 1.8% | 184 | `org/xacml4j/v30/BagOfValues.<init>  ->  com/google/common/collect/RegularImmutableMultiset.create` |
| 1.4% | 141 | `org/xacml4j/v30/policy/function/FunctionInvocationFactory$Default.doInvoke  ->  java_lang_Throwable::fill_in_stack_trace` |
| 1.4% | 137 | `org/xacml4j/v30/AttributeIndex.findByIssuer  ->  itable stub` |
| 1.3% | 129 | `org/xacml4j/v30/policy/DescendantEvaluationContext.isValidateFuncParamsAtRuntime  ->  itable stub` |
| 1.2% | 118 | `org/xacml4j/v30/policy/DescendantEvaluationContext.resolve  ->  itable stub` |
| 1.1% | 114 | `org/xacml4j/v30/policy/DescendantEvaluationContext.resolve` |
| 1.1% | 111 | `org/xacml4j/v30/policy/RootEvaluationContext.addEvaluationResult  ->  java/util/concurrent/ConcurrentSkipListMap.doPut` |
| 1.0% | 106 | `org/xacml4j/v30/policy/function/impl/DefaultFunctionInvocation.copyInto` |
| 1.0% | 105 | `org/xacml4j/v30/policy/DescendantEvaluationContext.isValidateFuncParamsAtRuntime` |
| 1.0% | 104 | `org/xacml4j/v30/spi/pip/DefaultResolverRegistry.getMatchingAttributeResolver  ->  java/util/stream/AbstractPipeline.copyInto` |
| 1.0% | 102 | `org/xacml4j/v30/policy/Rule.evaluate` |
| 1.0% | 102 | `org/xacml4j/v30/AttributeIndex.findByIssuer  ->  java/util/stream/AbstractPipeline.copyInto` |
| 1.0% | 100 | `org/xacml4j/v30/types/Value$OtherValue.hashCode  ->  jdk/internal/util/ArraysSupport.hashCode` |
| 0.9% | 91 | `org/xacml4j/v30/Attribute.getValuesByType  ->  itable stub` |
| 0.9% | 90 | `org/xacml4j/v30/policy/function/FunctionInvocationFactory$Default.doInvoke  ->  BacktraceBuilder::push` |
| 0.8% | 78 | `org/xacml4j/v30/policy/function/FunctionSpecBuilder$FunctionSpecImpl.evaluate` |
| 0.8% | 77 | `org/xacml4j/v30/policy/function/FunctionSpecBuilder$FunctionSpecImpl.normalize` |
| 0.8% | 77 | `org/xacml4j/v30/policy/function/impl/BagFunctions.oneAndOnlyImpl  ->  java_lang_Throwable::fill_in_stack_trace` |
| 0.7% | 74 | `org/xacml4j/v30/types/Value$OtherValue.hashCode  ->  java/util/Objects.hashCode` |
| 0.7% | 73 | `org/xacml4j/v30/policy/function/FunctionSpecBuilder$FunctionSpecImpl.normalizeMandatoryParameters` |
| 0.7% | 72 | `org/xacml4j/v30/policy/function/impl/HigherOrderFunctions.validateArguments` |
| 0.7% | 69 | `org/xacml4j/v30/BagOfValues$Builder.attributes` |
| 0.6% | 65 | `org/xacml4j/v30/SemanticIdentifier.hashCode  ->  jdk/internal/util/ArraysSupport.hashCode` |
| 0.6% | 63 | `org/xacml4j/v30/pdp/DefaultPolicyDecisionPoint.getResolvedAttributes  ->  java/util/Collections$UnmodifiableCollection$1.hasNext` |
| 0.6% | 62 | `org/xacml4j/v30/policy/function/impl/BagFunctions.oneAndOnlyImpl  ->  BacktraceBuilder::push` |
| 0.6% | 61 | `org/xacml4j/v30/policy/function/impl/DefaultFunctionInvocation.invoke` |
| 0.6% | 61 | `org/xacml4j/v30/types/Value$OtherValue.hashCode  ->  java/lang/String.hashCode` |
