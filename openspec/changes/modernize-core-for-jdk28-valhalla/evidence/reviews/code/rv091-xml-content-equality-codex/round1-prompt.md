# Adversarial code review: comparison and hash code of XML content (xacml4j)

You are an independent reviewer. Do not use tools. Do not edit files. Answer
in one message. Review only the diff below. It is the complete diff of this
change. The full reactor passes on it: 7,877 tests, 0 failures, 0 errors, 20 skipped. Conformance unchanged:
937 tests, 12 skipped. The reactor ran with a second, unrelated change in
the tree (a limit in the multiple-decision request handlers of another
module); the two changes share no file.

## Project

xacml4j is a Java 21 implementation of OASIS XACML 2.0/3.0. A request
category can carry XML `<Content>`. The engine holds it as `XmlContent`,
which wraps a DOM `Document`. `XmlContent.equals` and `hashCode` are part
of `Entity`, `Category` and `RequestContext` equality. The trust boundary
is the request: it comes from the caller of the PDP and is not trusted.

## Defect (bug policy4j-the.4.60, priority 1)

Found while testing another bead: five repeated categories that differed
only in `<Content>` expanded to four individual requests in one run and to
five in another.

Cause, in the removed lines of `DOMUtil.compareNodes` below:

- for two documents the result of comparing the document elements was
  dropped, so any two documents were equal;
- for two elements the results of comparing the child nodes were dropped,
  so only the root name, namespace, attributes and number of children
  counted;
- `XmlContent.hashCode` was the identity hash code of the DOM document, so
  equal contents had different hash codes;
- `isEqual` removed the white-space text nodes of both documents as a side
  effect of comparing them.

Effect: hash sets and maps of categories or requests behaved by chance.
`ImmutableSet.copyOf(categories)` in the multiple-decision handlers dropped
a category whenever two hash codes fell into the same bucket. A decision
cache keyed by the request (`PolicyDecisionCache.getDecision(RequestContext)`;
the only implementation in the code base caches nothing) could return the
decision of a request with other content.

## Change

- `DOMUtil.compareNodes(Node, Node, boolean)` is rewritten: iterative,
  whole tree, no mutation.
- New `DOMUtil.hashCode(Node)` that agrees with `isEqual`.
  `XmlContent.hashCode` uses it and keeps the value after the first call.
- The rules of the old code that are kept on purpose (the maintainer's
  design; changing them is out of scope): text nodes of white space only
  are not compared; text is compared trimmed; namespace prefixes and
  namespace declarations do not count; the order of attributes does not
  count.
- Tightened on the way:
  1. only `xmlns` and `xmlns:prefix` are namespace declarations (before:
     every attribute whose name starts with `xmlns` was skipped);
  2. comments and processing instructions are compared by name and value
     (before: any two nodes of the same kind were equal);
  3. the namespace of an attribute is compared (before: `getNamedItem`
     could match an attribute without a namespace to one in a namespace).
- `DOMUtilTest.testNodeEquals` asserted that two different fixtures are
  equal. It now asserts that they differ and that a copy is equal.

Facts you cannot see in the diff:

- The constructor of `XmlContent` copies the node it is given into a new
  `Document` (`DOMUtil.copyNode`), so `contextNode` belongs to the
  `XmlContent`. `XmlContent.toNode()` returns that document. Its two
  callers in production code add it to a JAXB content list for
  marshalling; no production code changes it.
- The parser of `DOMUtil.parseXml` is namespace aware, ignores comments,
  forbids a DOCTYPE and does not expand entity references. Content can
  also come from a DOM that another component built (JAXB `xs:any`).
- `XmlContent` evaluates XPath expressions over the document; an XPath
  can select elements, attributes, text, comments and processing
  instructions.
- The callers of `isEqual` and `compareNodes` in production code are
  `XmlContent.equals` only. `compareNodes(Node, Node)` and
  `compareNodes(Node, Node, boolean)` are public; tests call them.
- `Objects` is `java.util.Objects`. `Document`, `Element`, `Attr`, `Text`,
  `Node`, `NodeList`, `NamedNodeMap` are `org.w3c.dom`. `CDATASection`
  extends `Text`.
- Red runs. First version of `DOMUtilEqualityTest` against the old code:
  all 12 rows of "different documents" compared as equal, and
  `MultipleResourcesContentOnlyRepeatsTest` failed with
  `repeats=5 ==> expected: <5> but was: <4>`. Rows and tests added later,
  each run against the code before its fix: the two `xmlnsx` rows and
  `commentsAndProcessingInstructionsAreCompared` (3 failures),
  `namespaceOfAnAttributeIsCompared` (1 failure).

## Questions

Report findings ordered by severity (HIGH, MEDIUM, LOW), each with file,
line from the diff, a concrete failing scenario, and a suggested fix. Report
only defects you can show from the diff and the facts above.

1. Is `isEqual` an equivalence relation (reflexive, symmetric,
   transitive)? Give a pair or triple of documents that breaks it.
2. Do equal nodes always have the same `DOMUtil.hashCode`? Give a pair
   that breaks it.
3. False equality beyond the kept rules: two documents that compare equal
   and that an XPath can tell apart by something other than white space,
   a prefix or a namespace declaration.
4. Can the comparison or the hash code throw, not terminate, or use stack
   or memory out of proportion for a hostile document (depth, width,
   node kinds, null names)?
5. Is the kept hash code in `XmlContent` safe (threads, a hash code of 0,
   the document reachable through `toNode()`)?
6. Does `compareNodes(Node, Node)` still do what a caller of the old
   method could rely on?
7. Do the tests pin the behaviour? Name a missing test.

End with a verdict line: `VERDICT: APPROVE` or `VERDICT: REQUEST_CHANGES`.

## Diff

(the complete diff of the change followed here)
