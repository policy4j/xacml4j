# Compiler errors and fixes

Locations are the compiler's original locations, including temporary test insertions.
Repeated errors from reruns are retained in compile-diagnostics.json and raw logs.

## PDP diagnostics (14)

| Compiler file:line:column | Diagnostic | Fix |
| --- | --- | --- |
| `xacml-pdp/src/main/java/org/xacml4j/v30/pdp/request/MultipleResourcesViaXPathExpressionHandler.java:46:29` | cannot find symbol | Import and cast public Path instead of removed top-level PathValue. |
| `xacml-pdp/src/main/java/org/xacml4j/v30/pdp/request/MultipleResourcesViaXPathExpressionLegacyHandler.java:68:54` | cannot find symbol | Use Entity.findValues(id, type); preserve filtering/cardinality. |
| `xacml-pdp/src/main/java/org/xacml4j/v30/pdp/request/MultipleResourcesViaXPathExpressionLegacyHandler.java:90:56` | no suitable method found for find(no arguments) | Iterate Entity.attributes(). |
| `xacml-pdp/src/main/java/org/xacml4j/v30/pdp/DefaultEvaluationContextHandler.java:49:7` | org.xacml4j.v30.pdp.DefaultEvaluationContextHandler is not abstract and does not override abstract method getEntity(org.xacml4j.v30.CategoryId) in org.xacml4j.v30.policy.EvaluationContextHandler | Implement Optional<Entity> getEntity(CategoryId). |
| `xacml-pdp/src/main/java/org/xacml4j/v30/pdp/DefaultEvaluationContextHandler.java:131:81` | incompatible types: org.xacml4j.v30.AttributeDesignatorKey cannot be converted to org.xacml4j.v30.AttributeSelectorKey | Resolve designators by attribute id, datatype and issuer. |
| `xacml-pdp/src/main/java/org/xacml4j/v30/pdp/DefaultEvaluationContextHandler.java:132:61` | incompatible types: java.util.Optional<java.lang.Object> cannot be converted to java.util.Optional<org.xacml4j.v30.BagOfValues> | Cascading Optional inference error resolved by the designator overload fix. |
| `xacml-pdp/src/main/java/org/xacml4j/v30/pdp/DefaultEvaluationContextHandler.java:180:48` | cannot find symbol | Cast the single bag value directly to Entity. |
| `xacml-pdp/src/main/java/org/xacml4j/v30/pdp/DefaultEvaluationContextHandler.java:181:48` | cannot find symbol | Remove EntityValue.get(); Entity is the value. |
| `xacml-pdp/src/main/java/org/xacml4j/v30/pdp/DefaultEvaluationContextHandler.java:196:48` | name clash: getEntity(org.xacml4j.v30.CategoryId) in org.xacml4j.v30.pdp.DefaultEvaluationContextHandler and getEntity(org.xacml4j.v30.CategoryId) in org.xacml4j.v30.policy.EvaluationContextHandler have the same erasure, yet neither overrides the other | Delegate Optional<Entity> getEntity to the request callback. |
| `xacml-pdp/src/main/java/org/xacml4j/v30/pdp/request/MultipleResourcesViaXPathExpressionHandler.java:119:47` | cannot find symbol | Stream Entity.attributes(). |
| `xacml-pdp/src/main/java/org/xacml4j/v30/pdp/request/MultipleResourcesViaXPathExpressionHandler.java:145:81` | incompatible types: org.xacml4j.v30.ValueExpTypeInfo cannot be converted to org.xacml4j.v30.types.ValueType | Use path.getEvaluatesTo().getValueType(). |
| `xacml-pdp/src/main/java/org/xacml4j/v30/pdp/request/MultipleResourcesViaXPathExpressionHandler.java:146:49` | incompatible types: inference variable T has incompatible bounds | Cascading inference error resolved by the transform value-type fix. |
| `xacml-pdp/src/main/java/org/xacml4j/v30/pdp/request/MultipleResourcesViaXPathExpressionHandler.java:163:36` | no suitable method found for find(no arguments) | Iterate Entity.attributes(). |
| `xacml-pdp/src/test/java/org/xacml4j/v30/pdp/request/MultipleResourcesViaXacmlXPathHandlerTest.java:184:78` | cannot find symbol | Use RequestContext.getAttribute(category, id); keep value/path assertions. |

## JUnit main-source diagnostics (24)

| Compiler file:line:column | Diagnostic | Fix |
| --- | --- | --- |
| `xacml-test/src/main/java/org/xacml4j/v20/Xacml30TestUtility.java:25:24` | package org.junit does not exist | Apply conductor-approved junit:junit:4.13.2 compile-scope dependency; all assertion/lifecycle code retained. |
| `xacml-test/src/main/java/org/xacml4j/v20/Xacml30TestUtility.java:25:1` | static import only from classes and interfaces | Apply conductor-approved junit:junit:4.13.2 compile-scope dependency; all assertion/lifecycle code retained. |
| `xacml-test/src/main/java/org/xacml4j/v20/Xacml30TestUtility.java:26:24` | package org.junit does not exist | Apply conductor-approved junit:junit:4.13.2 compile-scope dependency; all assertion/lifecycle code retained. |
| `xacml-test/src/main/java/org/xacml4j/v20/Xacml30TestUtility.java:26:1` | static import only from classes and interfaces | Apply conductor-approved junit:junit:4.13.2 compile-scope dependency; all assertion/lifecycle code retained. |
| `xacml-test/src/main/java/org/xacml4j/v20/Xacml30TestUtility.java:27:24` | package org.junit does not exist | Apply conductor-approved junit:junit:4.13.2 compile-scope dependency; all assertion/lifecycle code retained. |
| `xacml-test/src/main/java/org/xacml4j/v20/Xacml30TestUtility.java:27:1` | static import only from classes and interfaces | Apply conductor-approved junit:junit:4.13.2 compile-scope dependency; all assertion/lifecycle code retained. |
| `xacml-test/src/main/java/org/xacml4j/v30/AttributeResolverTestSupport.java:27:24` | package org.junit does not exist | Apply conductor-approved junit:junit:4.13.2 compile-scope dependency; all assertion/lifecycle code retained. |
| `xacml-test/src/main/java/org/xacml4j/v30/AttributeResolverTestSupport.java:27:1` | static import only from classes and interfaces | Apply conductor-approved junit:junit:4.13.2 compile-scope dependency; all assertion/lifecycle code retained. |
| `xacml-test/src/main/java/org/xacml4j/v30/AttributeResolverTestSupport.java:28:24` | package org.junit does not exist | Apply conductor-approved junit:junit:4.13.2 compile-scope dependency; all assertion/lifecycle code retained. |
| `xacml-test/src/main/java/org/xacml4j/v30/AttributeResolverTestSupport.java:28:1` | static import only from classes and interfaces | Apply conductor-approved junit:junit:4.13.2 compile-scope dependency; all assertion/lifecycle code retained. |
| `xacml-test/src/main/java/org/xacml4j/v30/AttributeResolverTestSupport.java:29:24` | package org.junit does not exist | Apply conductor-approved junit:junit:4.13.2 compile-scope dependency; all assertion/lifecycle code retained. |
| `xacml-test/src/main/java/org/xacml4j/v30/AttributeResolverTestSupport.java:29:1` | static import only from classes and interfaces | Apply conductor-approved junit:junit:4.13.2 compile-scope dependency; all assertion/lifecycle code retained. |
| `xacml-test/src/main/java/org/xacml4j/v30/XacmlPolicyTestSupport.java:25:24` | package org.junit does not exist | Apply conductor-approved junit:junit:4.13.2 compile-scope dependency; all assertion/lifecycle code retained. |
| `xacml-test/src/main/java/org/xacml4j/v30/XacmlPolicyTestSupport.java:25:1` | static import only from classes and interfaces | Apply conductor-approved junit:junit:4.13.2 compile-scope dependency; all assertion/lifecycle code retained. |
| `xacml-test/src/main/java/org/xacml4j/v30/XacmlPolicyTestSupport.java:26:24` | package org.junit does not exist | Apply conductor-approved junit:junit:4.13.2 compile-scope dependency; all assertion/lifecycle code retained. |
| `xacml-test/src/main/java/org/xacml4j/v30/XacmlPolicyTestSupport.java:26:1` | static import only from classes and interfaces | Apply conductor-approved junit:junit:4.13.2 compile-scope dependency; all assertion/lifecycle code retained. |
| `xacml-test/src/main/java/org/xacml4j/v30/XacmlPolicyTestSupport.java:34:17` | package org.junit does not exist | Apply conductor-approved junit:junit:4.13.2 compile-scope dependency; all assertion/lifecycle code retained. |
| `xacml-test/src/main/java/org/xacml4j/v20/Xacml20TestUtility.java:25:24` | package org.junit does not exist | Apply conductor-approved junit:junit:4.13.2 compile-scope dependency; all assertion/lifecycle code retained. |
| `xacml-test/src/main/java/org/xacml4j/v20/Xacml20TestUtility.java:25:1` | static import only from classes and interfaces | Apply conductor-approved junit:junit:4.13.2 compile-scope dependency; all assertion/lifecycle code retained. |
| `xacml-test/src/main/java/org/xacml4j/v20/Xacml20TestUtility.java:26:24` | package org.junit does not exist | Apply conductor-approved junit:junit:4.13.2 compile-scope dependency; all assertion/lifecycle code retained. |
| `xacml-test/src/main/java/org/xacml4j/v20/Xacml20TestUtility.java:26:1` | static import only from classes and interfaces | Apply conductor-approved junit:junit:4.13.2 compile-scope dependency; all assertion/lifecycle code retained. |
| `xacml-test/src/main/java/org/xacml4j/v20/Xacml20TestUtility.java:27:24` | package org.junit does not exist | Apply conductor-approved junit:junit:4.13.2 compile-scope dependency; all assertion/lifecycle code retained. |
| `xacml-test/src/main/java/org/xacml4j/v20/Xacml20TestUtility.java:27:1` | static import only from classes and interfaces | Apply conductor-approved junit:junit:4.13.2 compile-scope dependency; all assertion/lifecycle code retained. |
| `xacml-test/src/main/java/org/xacml4j/v30/XacmlPolicyTestSupport.java:63:10` | cannot find symbol | Apply conductor-approved junit:junit:4.13.2 compile-scope dependency; all assertion/lifecycle code retained. |

## Downstream API diagnostics (3)

| Compiler file:line:column | Diagnostic | Fix |
| --- | --- | --- |
| `xacml-test/src/main/java/org/xacml4j/v30/ExpectedAttributeResolverBuilder.java:63:53` | incompatible types: org.xacml4j.v30.ValueExpTypeInfo cannot be converted to org.xacml4j.v30.types.ValueType | Use value.getEvaluatesTo().getValueType(). |
| `xacml-conformance-tests/src/test/java/org/xacml4j/v20/Xacml20ConformanceAttributeResolver.java:32:29` | cannot find symbol | Import StringVal instead of removed String. |
| `xacml-conformance-tests/src/test/java/org/xacml4j/v20/Xacml20ConformanceAttributeResolver.java:52:51` | incompatible types: org.xacml4j.v30.types.Value<capture#1 of ?> cannot be converted to org.xacml4j.v30.types.StringVal | Cast the annotation-constrained string BagOfValues.value() result to StringVal. |

## Temporary edit errors, also corrected (4)

| Compiler file:line:column | Diagnostic | Fix |
| --- | --- | --- |
| `xacml-pdp/src/main/java/org/xacml4j/v30/pdp/request/MultipleResourcesViaXPathExpressionHandler.java:145:66` | cannot find symbol | Use path.getEvaluatesTo().getValueType(); resolves subsequent inference error. |
| `xacml-pdp/src/main/java/org/xacml4j/v30/pdp/request/MultipleResourcesViaXPathExpressionHandler.java:146:49` | incompatible types: inference variable T has incompatible bounds | Use path.getEvaluatesTo().getValueType(); resolves subsequent inference error. |
| `xacml-pdp/src/test/java/org/xacml4j/v30/pdp/request/MultipleResourcesViaXacmlXPathHandlerTest.java:105:84` | cannot find symbol | New test setup uses Result.ok(Decision.PERMIT), not nonexistent Result.permit(). |
| `xacml-pdp/src/test/java/org/xacml4j/v30/pdp/request/MultipleResourcesViaXacmlXPathHandlerTest.java:106:85` | cannot find symbol | New test setup uses Result.ok(Decision.PERMIT), not nonexistent Result.permit(). |
