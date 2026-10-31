# Adversarial code review, round 3: comparison and hash code of XML content (xacml4j)

You are an independent reviewer. Do not use tools. Do not run commands. Do
not edit files. Answer in one message. Review only the diff below. It is
the complete diff of this change after the fixes of rounds 1 and 2. The full
reactor passes on it: 7,889 tests, 0 failures, 0 errors, 20 skipped. Conformance unchanged: 937 tests, 12
skipped. The reactor ran with a second, unrelated change in the tree (a
limit in the multiple-decision request handlers of another module); the
two changes share no file.

## Project

xacml4j is a Java 21 implementation of OASIS XACML 2.0/3.0. A request
category can carry XML `<Content>`. The engine holds it as `XmlContent`,
which wraps a DOM `Document`. `XmlContent.equals` and `hashCode` are part
of `Entity`, `Category` and `RequestContext` equality. The trust boundary
is the request: it comes from the caller of the PDP and is not trusted.

## Defect (bug policy4j-the.4.60, priority 1)

In the removed lines of `DOMUtil.compareNodes` below: the result of
comparing the document elements was dropped, so any two documents were
equal; the results of comparing child nodes were dropped;
`XmlContent.hashCode` was the identity hash code of the DOM document;
`isEqual` removed the white-space text nodes of both documents as a side
effect.

## Change (final)

- `DOMUtil.compareNodes(Node, Node, boolean)` is rewritten: iterative,
  whole tree, no mutation. Every node other than a text node and an
  attribute node is compared by its children too: an element, a document
  (its document element and the comments and processing instructions
  beside it), a document fragment. The attributes of an element are
  compared as a multiset of namespace, name and value; an attribute node
  by the same three.
- New `DOMUtil.hashCode(Node)` that agrees with `isEqual`.
  `XmlContent.hashCode` uses it and computes it on every call.
- Rules of the old code that are kept on purpose (the maintainer's design;
  changing them is out of scope): text nodes of white space only are not
  compared by `isEqual`; text is compared trimmed; namespace prefixes and
  namespace declarations do not count; the order of attributes does not
  count.
- Tightened: only `xmlns` and `xmlns:prefix` are namespace declarations;
  comments and processing instructions are compared by name and value.
- `DOMUtilTest.testNodeEquals` asserted that two different fixtures are
  equal. It now asserts that they differ and that a copy is equal.

## Round 1 result (Codex: REQUEST_CHANGES) and what was done

1. HIGH, attribute matching was not symmetric and disagreed with the hash
   code for two attributes with the same namespace and local name under
   different prefixes: the attributes of an element are now one multiset
   (`HashMultiset` of `Arrays.asList(namespace, name, value)`), used by
   the comparison and by the hash code. The lookup methods
   `sameAttributes` and `countNonNamespaceAttribures` are removed. Test
   `attributesWithTheSameExpandedNameAreComparedAsAMultiset`, red before
   the fix.
2. MEDIUM, processing instructions beside the document element were not
   compared: a document is now compared and hashed by its children. Test
   `nodesBesideTheDocumentElementAreCompared`, red before the fix.
3. MEDIUM, the kept hash code of `XmlContent` could disagree with `equals`
   after a change of the document through `toNode()`: the hash code is no
   longer kept. Test `hashCodeOfXmlContentFollowsTheDocument`, red before
   the fix (the two hash codes differed by 31). Not changed: `toNode()`
   and the XPath methods of `Content` still return live nodes; `Entity`
   and `RequestContext` keep their own hash codes, as before this change
   (see the facts).
4. Missing tests: `compareNodesWithTwoArgumentsComparesWhiteSpace`
   (`<r/>` and `<r> </r>`) and `deepDocumentsAreCompared` (100,000
   nested elements) added. Both passed at once; the deep test was not run
   against the old, recursive code.

## Round 2 result (Codex: REQUEST_CHANGES) and what was done

1. MEDIUM, a change of the document through a live node leaves the hash
   codes that `Entity` and `RequestContext` keep stale: no code change.
   Reason given by the author: these objects are used as keys. A key
   whose content is changed after it was put into a hash collection is
   lost there whether its hash code is kept or computed again, so
   computing the enclosing hash codes on every call would not make such
   a change safe; it would only move the failure. The XPath functions of
   the engine need the live nodes (node identity and ancestors), so the
   nodes cannot be copies. The contract is now written on the class
   `XmlContent`: the nodes are for reading and MUST NOT be changed. No
   production code changes them. Recorded as a limit of the run. Before
   this change the same objects kept hash codes built on the identity
   hash code of the DOM document.
2. MEDIUM, a document fragment was compared without its children: fixed.
   The comparison and the hash code walk the children of every node other
   than a text node and an attribute node. Test
   `documentFragmentsAreComparedByTheirChildren`, red before the fix.
3. LOW, two attribute nodes with the same qualified name and value in
   different namespaces were equal: fixed. An attribute node is compared
   and hashed by namespace, name and value. Test
   `attributeNodesAreComparedByNamespaceNameAndValue`, red before the fix.

Facts you cannot see in the diff:

- The constructor of `XmlContent` copies the document element of the node
  it is given into a new `Document` (`DOMUtil.copyNode(Element)`:
  `importNode(source, true)` into a new document). Nodes beside the
  document element of the source are not copied.
- `Entity.hashCode()` computes `Objects.hashCode(attributeIndex, content)`
  once and keeps it; `RequestContext` keeps its hash code too. Both did so
  before this change. `Content.evaluateToNodeSet(path)` and
  `evaluateToNode(path)` return nodes of the document.
- The parser of `DOMUtil.parseXml` is namespace aware, ignores comments,
  forbids a DOCTYPE and does not expand entity references. Content can
  also come from a DOM that another component built (JAXB `xs:any`).
- The callers of `isEqual` and `compareNodes` in production code are
  `XmlContent.equals` only. `compareNodes(Node, Node)` and
  `compareNodes(Node, Node, boolean)` are public.
- `Objects` is `java.util.Objects`; `HashMultiset` and `Multiset` are
  Guava. `Multiset.equals` compares elements and counts;
  `Multiset.hashCode` is the sum over the entries of
  `element.hashCode() ^ count`. `Arrays.asList` allows null elements;
  its `equals` and `hashCode` are those of `List`.
- `CDATASection` extends `Text`.

## Questions

Report findings ordered by severity (HIGH, MEDIUM, LOW), each with file,
line from the diff, a concrete failing scenario, and a suggested fix. Report
only defects you can show from the diff and the facts above.

1. Are findings 2 and 3 of round 2 closed?
2. Finding 1 of round 2: is the author's reason sound? If you still hold
   that this change must alter `Entity` or `RequestContext`, say what
   that would make safe that is not safe with the contract.
3. Is `isEqual` an equivalence relation, and do equal nodes have the same
   `DOMUtil.hashCode`, with the new uniform walk? Give a pair or triple
   that breaks it.
4. Any other defect in the diff. Name a missing test.

End with a verdict line: `VERDICT: APPROVE` or `VERDICT: REQUEST_CHANGES`.

## Diff

(the complete diff of the change followed here)
