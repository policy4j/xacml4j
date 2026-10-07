# URI spelling and caller audit

The primary source is the [XACML 3.0 OASIS Standard](https://docs.oasis-open.org/xacml/3.0/xacml-3.0-core-spec-os-en.html), sections A.3.1, A.3.3 and A.3.9. Native Tavily search and extract checked it in this packet. A.3.3 preserves the original XML URI spelling; A.3.1 compares converted URI strings by code point; A.3.9 uses that spelling for searches and substring indexes.

AnyURILexicalTest uses literal expectations for dot segments, scheme/host case and percent-escape case. Both runtime-check settings execute through Apply on the default provider. String, URI and TypeToString factory paths agree on the lexical form and hash. XML and JSON tests start from text, marshal, then unmarshal again and check the original spelling.

| Caller | Read-only observation |
| --- | --- |
| TypeToString.java:81-88; XacmlTypes.java:44-45 | String parsing and output delegate to AnyURI. |
| StringFunctions.java:70-162, 278-291 | URI searches, substring and conversion consume toStringExp(); anyURI-from-string passes StringVal to ofAny. |
| EqualityPredicates.java:60-66 | anyURI-equal delegates to Value equality; the AnyURI override supplies lexical equality. This file was not edited by this lane. |
| BagFunctions.java:379-407; BagOfValues.java | Membership/equality/hash use domain values. The set/hash and bag tests retain distinct spellings. |
| RegExpFunctions.java:58-64 | URI regex uses the existing value-to-string conversion. |
| AttributeDesignatorFunctions.java:51-64, 120-123; CategoryId.java:129-130 | Identifiers use URI text; the getter retains its URI return type. |
| AttributeDesignatorKey.java:127-151 | Uses the existing diagnostic toString() in its AnyURI overloads; unchanged, and not an explicit normalization consumer. |
| TypeToXacml30.java:95-113; TypeToGSon.java:78-87 | XML/JSON capabilities delegate to TypeToString; the new round-trip tests check them. |

The RTK source inventory and ast-grep Java normalize-call inventory are retained in caller-audit.txt. The positive normalize matches are AnyURI's opt-in normalize() and four filesystem Path operations (repository imports and fixture/manifest paths); their source was opened to confirm the receiver types. This syntax inventory and reactor tests cover repository callers, not arbitrary external providers or reflection.

The conductor's 02:06 merge made Value.OtherValue.equals/hashCode overridable. AnyURI therefore retains its original superclass and URI getter; this lane did not edit Value.java. Its earlier AbstractValue workaround was removed before the final gates.

Two observations have separate receipts:

- The repeated String branch in AnyURI.ofAny rejected StringVal passed by anyURI-from-string. Four red errors in run 03 show the valid conversions failing; the branch now delegates StringVal to the existing string parser in the same class.
- StringFunctions.java:276 registers string-from-anyURI without the canonical :function: segment. Run 01's eight lookup errors reproduce this pre-existing identifier gap. Tests use the existing default-provider binding to verify its conversion semantics. This packet does not change StringFunctions or claim canonical lookup is fixed.

The initial run-01 comparator JSON came from retained reports after the XML test compilation stopped; it is not a fresh comparator execution. Run 03 is the complete pre-fix red receipt. XML test setup was corrected to use InputSource and a typed AnyURI access; expected values were unchanged.
