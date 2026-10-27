total samples: 1004

## top 30 frames by self samples

| Share | Samples | Frame |
| --- | --- | --- |
| 6.7% | 67 | `itable stub` |
| 2.1% | 21 | `pthread_jit_write_protect_np` |
| 1.9% | 19 | `java/lang/String.compareTo` |
| 1.8% | 18 | `jdk/internal/util/ArraysSupport.hashCode` |
| 1.7% | 17 | `java/util/stream/AbstractPipeline.copyInto` |
| 1.6% | 16 | `java/util/Objects.hashCode` |
| 1.5% | 15 | `ResolvedMethodTable::find_method` |
| 1.5% | 15 | `org/apache/logging/log4j/core/Logger$PrivateConfig.filter` |
| 1.3% | 13 | `java/util/stream/AbstractPipeline.<init>` |
| 1.3% | 13 | `com/google/common/collect/RegularImmutableMultiset.hashFloodingDetected` |
| 1.3% | 13 | `java/util/stream/ReduceOps.makeRef` |
| 1.3% | 13 | `vframeStreamCommon::fill_from_compiled_frame` |
| 1.2% | 12 | `org/xacml4j/v30/policy/DescendantEvaluationContext.resolve` |
| 1.2% | 12 | `com/google/common/collect/RegularImmutableMultiset.create` |
| 1.1% | 11 | `java/util/RegularEnumSet.contains` |
| 1.1% | 11 | `org/xacml4j/v30/policy/function/impl/DefaultFunctionInvocation.copyInto` |
| 1.1% | 11 | `java/util/ArrayList.add` |
| 1.0% | 10 | `AccessInternal::PostRuntimeDispatch<G1BarrierSet::AccessBarrier<1335398ull, G1BarrierSet>, (AccessInternal::BarrierType)1, 1335398ull>::oop_access_barrier` |
| 1.0% | 10 | `java_lang_StackFrameInfo::set_method_and_bci` |
| 0.9% | 9 | `java_lang_Throwable::fill_in_stack_trace` |
| 0.9% | 9 | `java/lang/String.hashCode` |
| 0.9% | 9 | `java/util/stream/ReferencePipeline$2.opWrapSink` |
| 0.9% | 9 | `com/google/common/collect/ImmutableList$Builder.addAll` |
| 0.9% | 9 | `java/util/stream/AbstractPipeline.evaluate` |
| 0.8% | 8 | `java/lang/invoke/DirectMethodHandle.allocateInstance` |
| 0.8% | 8 | `org/xacml4j/v30/policy/function/FunctionSpecBuilder$FunctionSpecImpl.evaluate` |
| 0.8% | 8 | `org/xacml4j/v30/policy/function/impl/DefaultFunctionInvocation.invoke` |
| 0.8% | 8 | `HandleMark::initialize` |
| 0.8% | 8 | `org/xacml4j/v30/policy/function/FunctionSpecBuilder$FunctionSpecImpl.normalizeMandatoryParameters` |
| 0.7% | 7 | `java/util/concurrent/ConcurrentSkipListMap.doPut` |

## top 30 frames by total samples

| Share | Samples | Frame |
| --- | --- | --- |
| 99.9% | 1003 | `java/lang/Thread.run` |
| 99.9% | 1003 | `jdk/internal/reflect/DirectMethodHandleAccessor.invokeImpl` |
| 99.9% | 1003 | `java/lang/invoke/LambdaForm$MH.0x0000007c01066000.invokeExact_MT` |
| 99.9% | 1003 | `java/lang/reflect/Method.invoke` |
| 99.9% | 1003 | `java/lang/invoke/DirectMethodHandle$Holder.invokeSpecial` |
| 99.9% | 1003 | `java/util/concurrent/ThreadPoolExecutor.runWorker` |
| 99.9% | 1003 | `java/lang/invoke/LambdaForm$MH.0x0000007c01065c00.invoke` |
| 99.9% | 1003 | `java/lang/Thread.runWith` |
| 99.9% | 1003 | `java/util/concurrent/Executors$RunnableAdapter.call` |
| 99.9% | 1003 | `java/util/concurrent/FutureTask.run` |
| 99.9% | 1003 | `org/openjdk/jmh/runner/BenchmarkHandler$BenchmarkTask.call` |
| 99.9% | 1003 | `org/xacml4j/v30/pdp/DefaultPolicyDecisionPoint.decide` |
| 99.9% | 1003 | `jdk/internal/reflect/DirectMethodHandleAccessor.invoke` |
| 99.9% | 1003 | `org/xacml4j/jmh/pdp/jmh_generated/PdpDecideBenchmark_decide_jmhTest.decide_AverageTime` |
| 99.9% | 1003 | `org/xacml4j/jmh/pdp/jmh_generated/PdpDecideBenchmark_decide_jmhTest.decide_avgt_jmhStub` |
| 99.9% | 1003 | `java/util/concurrent/ThreadPoolExecutor$Worker.run` |
| 99.9% | 1003 | `org/xacml4j/jmh/pdp/PdpDecideBenchmark.decide` |
| 99.1% | 995 | `org/xacml4j/v30/spi/pdp/RequestContextHandlerChain.handle` |
| 99.0% | 994 | `org/xacml4j/v30/pdp/request/AbstractRequestContextHandler.handleNext` |
| 99.0% | 994 | `org/xacml4j/v30/pdp/request/MultipleResourcesBudgetHandler.handle` |
| 98.9% | 993 | `org/xacml4j/v30/pdp/request/MultipleResourcesViaRequestReferencesHandler.handle` |
| 98.8% | 992 | `org/xacml4j/v30/pdp/request/MultipleResourcesViaRepeatingAttributesHandler.handle` |
| 97.5% | 979 | `org/xacml4j/v30/pdp/request/MultipleResourcesViaXPathExpressionLegacyHandler.handle` |
| 97.5% | 979 | `org/xacml4j/v30/pdp/request/MultipleResourcesBudgetHandler$Checkpoint.handle` |
| 95.1% | 955 | `org/xacml4j/v30/pdp/request/MultipleResourcesViaXPathExpressionHandler.handle` |
| 89.9% | 903 | `org/xacml4j/v30/pdp/DefaultPolicyDecisionPoint$NumberedDecisions.requestDecision` |
| 89.9% | 903 | `org/xacml4j/v30/pdp/PolicyDecisionPointContextFactory$DefaultPolicyDecisionPointContextFactory$1.requestDecision` |
| 89.9% | 903 | `org/xacml4j/v30/pdp/DefaultPolicyDecisionPoint.requestDecision` |
| 89.9% | 903 | `org/xacml4j/v30/pdp/request/MultipleResourcesBudgetHandler$Budget.requestDecision` |
| 66.1% | 664 | `org/xacml4j/v30/policy/PolicySet.evaluate` |

## top 30 frames by deepest project frame (-> leaf frame or allocated type) samples

| Share | Samples | Frame |
| --- | --- | --- |
| 1.9% | 19 | `org/xacml4j/v30/policy/RootEvaluationContext.addEvaluationResult  ->  java/lang/String.compareTo` |
| 1.5% | 15 | `org/xacml4j/v30/policy/RootEvaluationContext.<init>  ->  ResolvedMethodTable::find_method` |
| 1.3% | 13 | `org/xacml4j/v30/Attribute.getValuesByType  ->  itable stub` |
| 1.3% | 13 | `org/xacml4j/v30/RequestContext.getEntity  ->  java/util/stream/AbstractPipeline.copyInto` |
| 1.3% | 13 | `org/xacml4j/v30/BagOfValues.<init>  ->  com/google/common/collect/RegularImmutableMultiset.hashFloodingDetected` |
| 1.3% | 13 | `org/xacml4j/v30/policy/RootEvaluationContext.<init>  ->  vframeStreamCommon::fill_from_compiled_frame` |
| 1.3% | 13 | `org/xacml4j/v30/AttributeIndex.findByIssuer  ->  itable stub` |
| 1.2% | 12 | `org/xacml4j/v30/policy/DescendantEvaluationContext.resolve` |
| 1.2% | 12 | `org/xacml4j/v30/BagOfValues.<init>  ->  com/google/common/collect/RegularImmutableMultiset.create` |
| 1.1% | 11 | `org/xacml4j/v30/policy/function/impl/DefaultFunctionInvocation.copyInto` |
| 1.0% | 10 | `org/xacml4j/v30/Attribute.getValuesByType  ->  java/util/stream/AbstractPipeline.<init>` |
| 1.0% | 10 | `org/xacml4j/v30/policy/RootEvaluationContext.<init>  ->  java_lang_StackFrameInfo::set_method_and_bci` |
| 1.0% | 10 | `org/xacml4j/v30/policy/RootEvaluationContext.<init>  ->  pthread_jit_write_protect_np` |
| 1.0% | 10 | `org/xacml4j/v30/AttributeIndex.findByIssuer  ->  java/util/stream/ReduceOps.makeRef` |
| 1.0% | 10 | `org/xacml4j/v30/types/Value$OtherValue.hashCode  ->  jdk/internal/util/ArraysSupport.hashCode` |
| 0.9% | 9 | `org/xacml4j/v30/policy/DescendantEvaluationContext.resolve  ->  itable stub` |
| 0.9% | 9 | `org/xacml4j/v30/policy/DescendantEvaluationContext.isValidateFuncParamsAtRuntime  ->  itable stub` |
| 0.9% | 9 | `org/xacml4j/v30/AttributeIndex.findValues  ->  com/google/common/collect/ImmutableList$Builder.addAll` |
| 0.8% | 8 | `org/xacml4j/v30/policy/function/FunctionSpecBuilder$FunctionSpecImpl.evaluate` |
| 0.8% | 8 | `org/xacml4j/v30/SemanticIdentifier.hashCode  ->  jdk/internal/util/ArraysSupport.hashCode` |
| 0.8% | 8 | `org/xacml4j/v30/policy/function/impl/DefaultFunctionInvocation.invoke` |
| 0.8% | 8 | `org/xacml4j/v30/policy/RootEvaluationContext.<init>  ->  HandleMark::initialize` |
| 0.8% | 8 | `org/xacml4j/v30/policy/function/FunctionSpecBuilder$FunctionSpecImpl.normalizeMandatoryParameters` |
| 0.8% | 8 | `org/xacml4j/v30/SemanticIdentifier.hashCode  ->  java/util/Objects.hashCode` |
| 0.8% | 8 | `org/xacml4j/v30/policy/function/FunctionSpecBuilder$FunctionSpecImpl.evaluate  ->  java/util/ArrayList.add` |
| 0.8% | 8 | `org/xacml4j/v30/Attribute.getValuesByType  ->  java/util/stream/AbstractPipeline.evaluate` |
| 0.7% | 7 | `org/xacml4j/v30/policy/RootEvaluationContext.addEvaluationResult  ->  java/util/concurrent/ConcurrentSkipListMap.doPut` |
| 0.7% | 7 | `org/xacml4j/v30/AttributeIndex.findByIssuer  ->  java/util/stream/ReferencePipeline$2.opWrapSink` |
| 0.7% | 7 | `org/xacml4j/v30/policy/RootEvaluationContext.<init>  ->  vframeStreamCommon::next` |
| 0.6% | 6 | `org/xacml4j/v30/SemanticIdentifier.hashCode  ->  java/lang/String.hashCode` |
