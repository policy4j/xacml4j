Round 1 findings 1 and 2 are closed. Finding 3 is closed for `XmlContent` itself: recomputing its structural hash agrees with its current equality state. It does not resolve the cached hashes of enclosing objects.

1. **MEDIUM — Live content mutation leaves enclosing hashes stale.**  
 File: `xacml-core/src/main/java/org/xacml4j/v30/content/XmlContent.java:167`, together with the supplied `Entity` caching fact.

 Build an entity `e0` containing `x = XmlContent.of("<r>0</r>")`. Change the root’s text through `x.toNode()` to `"1"`. Build `e1` with identical attributes and content `<r>1</r>`.

 Now `e0.equals(e1)` is true, but `e0` retains the hash calculated from `<r>0</r>`, while `e1` calculates its hash from `<r>1</r>`. Those content hashes differ by 31. Cached request hashes can retain the same inconsistency.

 This is a **retained aggregate defect**, not a newly introduced cache. Computing every time is sound locally, but insufficient for the enclosing equality/hash contracts.

 **Fix:** Recompute all enclosing hashes whose equality depends on mutable content, or make their owned content immutable through every exposed access path.  
 **Missing test:** Mutate content after building an `Entity` and `RequestContext`; compare each against a freshly built equal object and assert equal hashes.

2. **MEDIUM — Non-element containers silently ignore their children.**  
 File: `xacml-core/src/main/java/org/xacml4j/util/DOMUtil.java:632`; corresponding hash fallback at line 479.

 Create two ordinary `DocumentFragment` nodes. Append `<a/>` to one and `<b/>` to the other. Both fragments have type `DOCUMENT_FRAGMENT_NODE`, name `#document-fragment`, and null node value. Consequently, `isEqual` returns true without examining either child. Their hashes also ignore the children.

 This contradicts whole-tree comparison beyond the expressly retained normalization rules. Its demonstrated reach is the public utility API; fragments are not the stored `XmlContent` document root.

 **Fix:** Traverse children of supported container types, including `DocumentFragment`, in both comparison and hashing. Explicitly document and reject unsupported types rather than silently discarding their structure.  
 **Missing test:** Fragments with different descendants compare unequal; equivalent fragments have equal hashes.

3. **LOW — Direct attribute comparison ignores namespace identity.**  
 File: `xacml-core/src/main/java/org/xacml4j/util/DOMUtil.java:632`.

 Create attributes using `createAttributeNS("urn:a", "p:k")` and `createAttributeNS("urn:b", "p:k")`, assigning both value `"v"`. Direct `isEqual` returns true because their node types, qualified names and values match. Their namespaces differ.

 The correct attribute multiset logic only runs when comparing their owning elements. This finding likewise concerns the public utility API.

 **Fix:** Compare and hash direct `Attr` nodes using the same namespace/name/value tuple used for element attributes.  
 **Missing test:** Direct attributes with identical qualified names and values but different namespaces compare unequal.

For stable, conforming DOM trees, **`isEqual` is an equivalence relation, and equal nodes have equal `DOMUtil.hashCode` values**. Each branch compares a deterministic set of fields, and hashing uses matching fields and traversal. The false equalities above discard information consistently; they do not break symmetry, transitivity, or hash agreement. No breaking pair or triple for those mathematical properties follows from the supplied evidence.

The explicit traversal is iterative. Its queues, child lists and attribute multisets require at most linear additional storage relative to the physical tree, attributes and text processed. No new stack recursion or nontermination is demonstrated for a stable, finite DOM. `compareNodes` still throws on null arguments at line 605; that behavior predates this rewrite. The supplied facts do not establish DOM-accessor or multiset collision costs, so they do not prove a hostile-input running-time bound.

Verification was static review only. No files changed or tests ran; the reported reactor and conformance results are supplied evidence.

VERDICT: REQUEST_CHANGES
