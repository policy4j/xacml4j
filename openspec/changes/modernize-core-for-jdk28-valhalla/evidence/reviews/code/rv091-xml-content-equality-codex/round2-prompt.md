# Adversarial code review, round 2: comparison and hash code of XML content (xacml4j)

You are an independent reviewer. Do not use tools. Do not run commands. Do
not edit files. Answer in one message. Review only the diff below. It is
the complete diff of this change after the fixes of round 1. The full
reactor passes on it: 7,887 tests, 0 failures, 0 errors, 20 skipped. Conformance unchanged: 937 tests, 12
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
  whole tree, no mutation. A document is compared by its children (the
  document element and the comments and processing instructions beside
  it). Attributes are compared as a multiset of namespace, name and value.
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

1. Are the three findings of round 1 closed? For finding 3, is computing
   the hash code on every call a sound answer given the facts about
   `Entity`?
2. Is `isEqual` now an equivalence relation, and do equal nodes have the
   same `DOMUtil.hashCode`? Give a pair or triple that breaks it.
3. False equality beyond the kept rules.
4. Can the comparison or the hash code throw, not terminate, or use stack
   or memory out of proportion for a hostile document?
5. Any other defect in the diff. Name a missing test.

End with a verdict line: `VERDICT: APPROVE` or `VERDICT: REQUEST_CHANGES`.

## Diff

(the complete diff of the change followed here)
