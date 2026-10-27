total samples: 1015

## top 30 frames by self samples

| Share | Samples | Frame |
| --- | --- | --- |
| 4.5% | 46 | `pthread_jit_write_protect_np` |
| 3.3% | 34 | `itable stub` |
| 3.2% | 32 | `ResolvedMethodTable::find_method` |
| 3.0% | 30 | `java/util/stream/AbstractPipeline.<init>` |
| 3.0% | 30 | `com/google/common/collect/ImmutableList$1.get` |
| 2.2% | 22 | `vframeStreamCommon::next` |
| 2.2% | 22 | `vframeStreamCommon::fill_from_compiled_frame` |
| 1.9% | 19 | `java/util/Collections$UnmodifiableCollection$1.hasNext` |
| 1.8% | 18 | `HandleMark::~HandleMark` |
| 1.7% | 17 | `StackWalk::fill_in_frames` |
| 1.6% | 16 | `jdk/internal/util/ArraysSupport.hashCode` |
| 1.5% | 15 | `AccessInternal::PostRuntimeDispatch<G1BarrierSet::AccessBarrier<286822ull, G1BarrierSet>, (AccessInternal::BarrierType)1, 286822ull>::oop_access_barrier` |
| 1.5% | 15 | `java/util/stream/ReferencePipeline.collect` |
| 1.4% | 14 | `JavaFrameStream::fill_frame` |
| 1.4% | 14 | `call_stub` |
| 1.3% | 13 | `java_lang_StackFrameInfo::set_method_and_bci` |
| 1.3% | 13 | `java/lang/String.equals` |
| 1.3% | 13 | `java/util/stream/ReduceOps.makeRef` |
| 1.2% | 12 | `java/util/stream/AbstractPipeline.wrapSink` |
| 1.1% | 11 | `java/lang/StackStreamFactory.isReflectionFrame` |
| 1.1% | 11 | `HandleMark::initialize` |
| 1.0% | 10 | `java/util/stream/ReferencePipeline$2.opWrapSink` |
| 1.0% | 10 | `java/util/stream/Sink$ChainedReference.<init>` |
| 0.9% | 9 | `java/lang/StackStreamFactory$StackFrameBuffer.fill` |
| 0.9% | 9 | `java/util/stream/ReferencePipeline.forEachWithCancel` |
| 0.8% | 8 | `AccessInternal::PostRuntimeDispatch<G1BarrierSet::AccessBarrier<598116ull, G1BarrierSet>, (AccessInternal::BarrierType)2, 598116ull>::oop_access_barrier` |
| 0.8% | 8 | `java/lang/invoke/DirectMethodHandle.allocateInstance` |
| 0.8% | 8 | `java/util/stream/Sink$ChainedReference.cancellationRequested` |
| 0.7% | 7 | `java/util/Arrays$ArrayList.<init>` |
| 0.7% | 7 | `org/apache/logging/log4j/util/SortedArrayStringMap.initFrom0` |

## top 30 frames by total samples

| Share | Samples | Frame |
| --- | --- | --- |
| 98.5% | 1000 | `java/lang/Thread.run` |
| 98.5% | 1000 | `java/util/concurrent/ThreadPoolExecutor.runWorker` |
| 98.5% | 1000 | `java/util/concurrent/ThreadPoolExecutor$Worker.run` |
| 98.5% | 1000 | `java/util/concurrent/Executors$RunnableAdapter.call` |
| 98.5% | 1000 | `java/lang/Thread.runWith` |
| 98.5% | 1000 | `java/util/concurrent/FutureTask.run` |
| 98.5% | 1000 | `org/openjdk/jmh/runner/BenchmarkHandler$BenchmarkTask.call` |
| 98.4% | 999 | `java/lang/reflect/Method.invoke` |
| 98.4% | 999 | `jdk/internal/reflect/DirectMethodHandleAccessor.invokeImpl` |
| 98.4% | 999 | `org/xacml4j/jmh/pdp/PdpDecideBenchmark.decide` |
| 98.4% | 999 | `org/xacml4j/jmh/pdp/jmh_generated/PdpDecideBenchmark_decide_jmhTest.decide_AverageTime` |
| 98.4% | 999 | `org/xacml4j/v30/pdp/DefaultPolicyDecisionPoint.decide` |
| 98.4% | 999 | `java/lang/invoke/DirectMethodHandle$Holder.invokeSpecial` |
| 98.4% | 999 | `java/lang/invoke/LambdaForm$MH.0x00000ff001066000.invokeExact_MT` |
| 98.4% | 999 | `java/lang/invoke/LambdaForm$MH.0x00000ff001065c00.invoke` |
| 98.4% | 999 | `org/xacml4j/jmh/pdp/jmh_generated/PdpDecideBenchmark_decide_jmhTest.decide_avgt_jmhStub` |
| 98.4% | 999 | `jdk/internal/reflect/DirectMethodHandleAccessor.invoke` |
| 96.7% | 982 | `org/xacml4j/v30/spi/pdp/RequestContextHandlerChain.handle` |
| 96.7% | 981 | `org/xacml4j/v30/pdp/request/AbstractRequestContextHandler.handleNext` |
| 96.7% | 981 | `org/xacml4j/v30/pdp/request/MultipleResourcesBudgetHandler.handle` |
| 96.7% | 981 | `org/xacml4j/v30/pdp/request/MultipleResourcesViaRequestReferencesHandler.handle` |
| 96.7% | 981 | `org/xacml4j/v30/pdp/request/MultipleResourcesViaRepeatingAttributesHandler.handle` |
| 95.4% | 968 | `org/xacml4j/v30/pdp/request/MultipleResourcesBudgetHandler$Checkpoint.handle` |
| 95.4% | 968 | `org/xacml4j/v30/pdp/request/MultipleResourcesViaXPathExpressionLegacyHandler.handle` |
| 90.7% | 921 | `org/xacml4j/v30/pdp/request/MultipleResourcesViaXPathExpressionHandler.handle` |
| 85.2% | 865 | `org/xacml4j/v30/pdp/PolicyDecisionPointContextFactory$DefaultPolicyDecisionPointContextFactory$1.requestDecision` |
| 85.2% | 865 | `org/xacml4j/v30/pdp/DefaultPolicyDecisionPoint$NumberedDecisions.requestDecision` |
| 85.2% | 865 | `org/xacml4j/v30/pdp/request/MultipleResourcesBudgetHandler$Budget.requestDecision` |
| 85.2% | 865 | `org/xacml4j/v30/pdp/DefaultPolicyDecisionPoint.requestDecision` |
| 45.5% | 462 | `org/xacml4j/v30/pdp/PolicyDecisionPointContextFactory$DefaultPolicyDecisionPointContextFactory$1.createEvaluationContext` |

## top 30 frames by deepest project frame (-> leaf frame or allocated type) samples

| Share | Samples | Frame |
| --- | --- | --- |
| 4.4% | 45 | `org/xacml4j/v30/policy/RootEvaluationContext.<init>  ->  pthread_jit_write_protect_np` |
| 3.2% | 32 | `org/xacml4j/v30/policy/RootEvaluationContext.<init>  ->  ResolvedMethodTable::find_method` |
| 3.0% | 30 | `org/xacml4j/v30/policy/Match.match  ->  com/google/common/collect/ImmutableList$1.get` |
| 2.2% | 22 | `org/xacml4j/v30/policy/RootEvaluationContext.<init>  ->  vframeStreamCommon::next` |
| 2.2% | 22 | `org/xacml4j/v30/policy/RootEvaluationContext.<init>  ->  vframeStreamCommon::fill_from_compiled_frame` |
| 1.9% | 19 | `org/xacml4j/v30/pdp/DefaultPolicyDecisionPoint.getResolvedAttributes  ->  java/util/Collections$UnmodifiableCollection$1.hasNext` |
| 1.8% | 18 | `org/xacml4j/v30/policy/RootEvaluationContext.<init>  ->  HandleMark::~HandleMark` |
| 1.8% | 18 | `org/xacml4j/v30/Attribute.getValuesByType  ->  java/util/stream/AbstractPipeline.<init>` |
| 1.7% | 17 | `org/xacml4j/v30/policy/RootEvaluationContext.<init>  ->  StackWalk::fill_in_frames` |
| 1.5% | 15 | `org/xacml4j/v30/policy/RootEvaluationContext.<init>  ->  AccessInternal::PostRuntimeDispatch<G1BarrierSet::AccessBarrier<286822ull, G1BarrierSet>, (AccessInternal::BarrierType)1, 286822ull>::oop_access_barrier` |
| 1.4% | 14 | `org/xacml4j/v30/policy/RootEvaluationContext.<init>  ->  JavaFrameStream::fill_frame` |
| 1.4% | 14 | `org/xacml4j/v30/policy/RootEvaluationContext.<init>  ->  call_stub` |
| 1.3% | 13 | `org/xacml4j/v30/policy/RootEvaluationContext.<init>  ->  java_lang_StackFrameInfo::set_method_and_bci` |
| 1.1% | 11 | `org/xacml4j/v30/policy/RootEvaluationContext.<init>  ->  java/lang/StackStreamFactory.isReflectionFrame` |
| 1.1% | 11 | `org/xacml4j/v30/policy/RootEvaluationContext.<init>  ->  HandleMark::initialize` |
| 0.9% | 9 | `org/xacml4j/v30/policy/RootEvaluationContext.<init>  ->  java/lang/StackStreamFactory$StackFrameBuffer.fill` |
| 0.8% | 8 | `org/xacml4j/v30/Attribute.getValuesByType  ->  itable stub` |
| 0.8% | 8 | `org/xacml4j/v30/AttributeIndex.findByIssuer  ->  java/util/stream/ReferencePipeline.collect` |
| 0.8% | 8 | `org/xacml4j/v30/policy/RootEvaluationContext.<init>  ->  AccessInternal::PostRuntimeDispatch<G1BarrierSet::AccessBarrier<598116ull, G1BarrierSet>, (AccessInternal::BarrierType)2, 598116ull>::oop_access_barrier` |
| 0.8% | 8 | `org/xacml4j/v30/SemanticIdentifier.hashCode  ->  jdk/internal/util/ArraysSupport.hashCode` |
| 0.8% | 8 | `org/xacml4j/v30/policy/RootEvaluationContext.<init>  ->  java/lang/invoke/DirectMethodHandle.allocateInstance` |
| 0.8% | 8 | `org/xacml4j/v30/policy/RootEvaluationContext.<init>  ->  java/util/stream/Sink$ChainedReference.cancellationRequested` |
| 0.8% | 8 | `org/xacml4j/v30/RequestContext.getEntity  ->  java/util/stream/ReferencePipeline.forEachWithCancel` |
| 0.8% | 8 | `org/xacml4j/v30/types/Value$OtherValue.hashCode  ->  jdk/internal/util/ArraysSupport.hashCode` |
| 0.7% | 7 | `org/xacml4j/v30/policy/Match.match  ->  java/util/Arrays$ArrayList.<init>` |
| 0.7% | 7 | `org/xacml4j/v30/policy/RootEvaluationContext.<init>  ->  itable stub` |
| 0.7% | 7 | `org/xacml4j/v30/AttributeIndex.findByIssuer  ->  java/util/stream/AbstractPipeline.<init>` |
| 0.7% | 7 | `org/xacml4j/v30/policy/RootEvaluationContext.<init>  ->  StackWalk::fetchFirstBatch` |
| 0.7% | 7 | `org/xacml4j/v30/AttributeIndex.find  ->  java/util/stream/ReduceOps.makeRef` |
| 0.7% | 7 | `org/xacml4j/v30/policy/RootEvaluationContext.<init>  ->  java/util/stream/WhileOps$1Op$1OpSink.accept` |
