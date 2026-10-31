# Boolean text parser audit

The task-start search covered `xacml-core/src`, `xacml-xml/src` and
`xacml-json/src`, including main and tests. The commands are in `command.txt`.
The final literal search output is in `parser-search.txt`. Each candidate was
opened and classified; an empty search alone is not used as proof of absence.

| Source | Input | Disposition and proof |
| --- | --- | --- |
| `xacml-core/src/main/java/org/xacml4j/v30/types/BooleanVal.java:47` | Factory `String` and `StringVal` values | Both delegate to the same strict lexical parser. `BooleanLexicalTest` tests both branches and the `TypeToString.fromString` route for all four forms, whitespace, invalid forms and exact syntax-error status. |
| `xacml-json/src/main/java/org/xacml4j/v30/marshal/json/GsonUtil.java:38` | JSON text flags | Text goes through `BooleanVal.ofAny`. Native booleans and absent defaults retain their existing path. `BooleanRequestLexicalTest` tests text and native flags, all three flag properties and syntax-error rejection. |
| `xacml-json/src/main/java/org/xacml4j/v30/marshal/json/RequestContextAdapter.java:55` | ReturnPolicyIdList and CombinedDecision | Uses GsonUtil; no edit needed. Request-level tests traverse this deserializer. |
| `xacml-json/src/main/java/org/xacml4j/v30/marshal/json/AttributeDeserializer.java:64` | IncludeInResult | Uses GsonUtil; no edit needed. Request-level tests assert included attributes and rejection of invalid text. |
| `xacml-core/src/main/java/org/xacml4j/v30/WellKnownAttributeIds.java:128` | JVM `.ALWAYS_USE_SUPPLIER` property | Remains Java configuration parsing. The opened source reads `System.getProperty`, not an XACML attribute. |
| `xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/RegExpFunctions.java:122` | JVM `.cacheRegExps` property | Remains Java configuration parsing. The opened source reads `System.getProperty`, not an XACML attribute. |
| `xacml-core/src/main/java/org/xacml4j/v30/types/TypeToString.java:93` | Boolean lexical capability | Delegates to BooleanVal; read only. The capability route is tested explicitly. |
| `xacml-xml/src/main/java/org/xacml4j/v30/xml/TypeToXacml30.java:129` | XML AttributeValue text | Delegates to the Boolean TypeToString capability; read only. Both 2.0 and 3.0 XML requests are tested through their unmarshallers. |
| `xacml-json/src/main/java/org/xacml4j/v30/marshal/json/TypeToGSon.java:108` | Declared JSON boolean AttributeValue | Delegates to the Boolean TypeToString capability; read only. Request tests cover text and native JSON boolean values. |
| `xacml-test/src/main/java/org/xacml4j/v20/Xacml30TestUtility.java:144` | Expected/actual typed lexical comparison | Removed the temporary boolean switch; use the type factory for both values. All 77 comparator tests, including boolean controls and negative cases, still pass. |

No `Boolean.valueOf` candidate appeared in the scoped searches. The expanded
search also checked Boolean constructors, BooleanUtils and toBoolean names;
the BooleanVal singleton/array hits were model construction, not text parsing.
Coverage is SUPPORTED for the literal inventory and the exercised request
paths. This is not a whole-program semantic index.

This lane did not edit XML mappers, TypeToString or DoubleVal. TypeToString and
DoubleVal retain their task-start bytes. The conductor merged XML mapper and
RequestContext changes after run 06; their separate hashes are recorded in
`08-conductor-xml-merge-changes.json` and `scope-and-header-audit.json`.
