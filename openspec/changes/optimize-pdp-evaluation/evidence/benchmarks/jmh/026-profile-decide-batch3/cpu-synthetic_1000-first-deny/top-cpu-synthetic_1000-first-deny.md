total samples: 9867

## top 30 frames by self samples

| Share | Samples | Frame |
| --- | --- | --- |
| 4.2% | 416 | `org/xacml4j/v30/policy/DescendantEvaluationContext.isOwn` |
| 3.0% | 292 | `org/xacml4j/v30/BagStorages$Builder.add` |
| 2.3% | 228 | `java/lang/String.equals` |
| 2.2% | 220 | `java/util/Objects.hashCode` |
| 2.2% | 216 | `org/xacml4j/v30/policy/DescendantEvaluationContext.top` |
| 2.2% | 213 | `java/util/concurrent/ConcurrentHashMap.putVal` |
| 2.0% | 193 | `com/google/common/collect/RegularImmutableMap$Values.get` |
| 1.8% | 174 | `org/xacml4j/v30/BagStorages$Grouped.<init>` |
| 1.7% | 168 | `com/google/common/collect/ImmutableMultimap$2.next` |
| 1.7% | 163 | `java/util/HashMap.getNode` |
| 1.6% | 158 | `sun/reflect/annotation/AnnotationInvocationHandler.invoke` |
| 1.6% | 153 | `zero_blocks` |
| 1.3% | 133 | `org/xacml4j/v30/policy/DescendantEvaluationContext.getCurrentPolicy` |
| 1.3% | 133 | `java/lang/invoke/VarHandleReferences$FieldInstanceReadWrite.compareAndExchange` |
| 1.3% | 129 | `com/google/common/collect/AbstractIndexedListIterator.hasNext` |
| 1.2% | 122 | `org/xacml4j/v30/pdp/request/AbstractRequestContextHandler.handleNext` |
| 1.2% | 120 | `java/util/HashMap.resize` |
| 1.2% | 118 | `java/lang/Enum.hashCode` |
| 1.2% | 116 | `org/xacml4j/v30/policy/DescendantEvaluationContext.getCurrentRule` |
| 1.1% | 113 | `java/util/HashMap.merge` |
| 1.1% | 111 | `org/xacml4j/v30/policy/combine/impl/DenyOverrides.doCombine` |
| 1.1% | 109 | `com/google/common/collect/ImmutableListMultimap.get` |
| 1.1% | 105 | `org/xacml4j/v30/BagStorages$Values.<init>` |
| 1.1% | 104 | `com/google/common/collect/ImmutableMultimap$2.<init>` |
| 1.0% | 103 | `org/xacml4j/v30/BagStorage.equal` |
| 1.0% | 102 | `vtable stub` |
| 1.0% | 101 | `java/util/concurrent/ConcurrentHashMap.initTable` |
| 1.0% | 100 | `jdk/internal/util/ArraysSupport.hashCode` |
| 1.0% | 97 | `jbyte_disjoint_arraycopy` |
| 0.9% | 89 | `org/xacml4j/v30/types/Value$OtherValue.hashCode` |

## top 30 frames by inclusive samples

| Share | Samples | Frame |
| --- | --- | --- |
| 99.2% | 9784 | `java/util/concurrent/ThreadPoolExecutor.runWorker` |
| 99.2% | 9784 | `org/openjdk/jmh/runner/BenchmarkHandler$BenchmarkTask.call` |
| 99.2% | 9784 | `java/util/concurrent/Executors$RunnableAdapter.call` |
| 99.2% | 9784 | `java/lang/Thread.runWith` |
| 99.2% | 9784 | `java/lang/Thread.run` |
| 99.2% | 9784 | `java/util/concurrent/FutureTask.run` |
| 99.2% | 9784 | `java/util/concurrent/ThreadPoolExecutor$Worker.run` |
| 99.1% | 9782 | `jdk/internal/reflect/DirectMethodHandleAccessor.invoke` |
| 99.1% | 9782 | `java/lang/invoke/LambdaForm$MH.0x000000f801062000.invokeExact_MT` |
| 99.1% | 9782 | `jdk/internal/reflect/DirectMethodHandleAccessor.invokeImpl` |
| 99.1% | 9782 | `java/lang/reflect/Method.invoke` |
| 99.1% | 9782 | `java/lang/invoke/LambdaForm$MH.0x000000f801061c00.invoke` |
| 99.1% | 9782 | `java/lang/invoke/DirectMethodHandle$Holder.invokeSpecial` |
| 99.1% | 9782 | `org/xacml4j/jmh/pdp/jmh_generated/PdpDecideBenchmark_decide_jmhTest.decide_AverageTime` |
| 99.1% | 9778 | `org/xacml4j/jmh/pdp/jmh_generated/PdpDecideBenchmark_decide_jmhTest.decide_avgt_jmhStub` |
| 99.1% | 9774 | `org/xacml4j/jmh/pdp/PdpDecideBenchmark.decide` |
| 98.9% | 9755 | `org/xacml4j/v30/pdp/DefaultPolicyDecisionPoint.decide` |
| 92.5% | 9125 | `org/xacml4j/v30/spi/pdp/RequestContextHandlerChain.handle` |
| 91.9% | 9065 | `org/xacml4j/v30/pdp/request/MultipleResourcesBudgetHandler.handle` |
| 91.7% | 9046 | `org/xacml4j/v30/pdp/request/AbstractRequestContextHandler.handleNext` |
| 91.2% | 9002 | `org/xacml4j/v30/pdp/request/MultipleResourcesViaRequestReferencesHandler.handle` |
| 91.0% | 8978 | `org/xacml4j/v30/pdp/request/MultipleResourcesViaRepeatingAttributesHandler.handle` |
| 90.5% | 8934 | `org/xacml4j/v30/pdp/request/MultipleResourcesBudgetHandler$Checkpoint.handle` |
| 89.9% | 8872 | `org/xacml4j/v30/pdp/request/MultipleResourcesViaXPathExpressionLegacyHandler.handle` |
| 86.0% | 8483 | `org/xacml4j/v30/pdp/request/MultipleResourcesViaXPathExpressionHandler.handle` |
| 79.5% | 7840 | `org/xacml4j/v30/pdp/request/MultipleResourcesBudgetHandler$Budget.requestDecision` |
| 79.2% | 7817 | `org/xacml4j/v30/pdp/PolicyDecisionPointContextFactory$DefaultPolicyDecisionPointContextFactory$1.requestDecision` |
| 79.1% | 7808 | `org/xacml4j/v30/pdp/DefaultPolicyDecisionPoint$NumberedDecisions.requestDecision` |
| 79.1% | 7803 | `org/xacml4j/v30/pdp/DefaultPolicyDecisionPoint.requestDecision` |
| 60.2% | 5943 | `org/xacml4j/v30/policy/PolicySet.evaluate` |

