# Run 082: comparison and hash code of XML content (bead the.4.60)

Dates: 2026-10-07 and 2026-10-08. Source of the bead: run 081 (found
while testing the.4.57). JDK: Oracle GraalVM 21.0.6+8.1
(`java version "21.0.6" 2025-01-21 LTS`). Review:
`reviews/code/rv091-xml-content-equality-codex`.

## Defect

`xacml-core/.../util/DOMUtil.compareNodes`, used by `XmlContent.equals`:

- for two documents the result of comparing the document elements was
  dropped, so any two documents were equal;
- for two elements the results of comparing the child nodes were dropped;
- `XmlContent.hashCode` was the identity hash code of the DOM document, so
  equal contents had different hash codes;
- `DOMUtil.isEqual` removed the white-space text nodes of both documents
  while it compared them.

Effect: hash sets and maps of categories and requests behaved by chance.
`ImmutableSet.copyOf(categories)` in the multiple-decision handlers
dropped a category when two hash codes fell into one bucket: five repeated
categories that differed only in `<Content>` gave four individual
requests in one run and five in another. A decision cache keyed by the
request could return the decision of a request with other content; the
only implementation in the code base (`NoCachePolicyDecisionCache`)
caches nothing.

`DOMUtilTest.testNodeEquals` asserted that two different fixtures are
equal, so it passed on the defect.

## Change

- `DOMUtil.compareNodes(Node, Node, boolean)`: rewritten. It walks the
  whole tree with an explicit stack and changes no node. Every node other
  than a text node and an attribute node is compared by its children too:
  an element, a document, a document fragment.
- `DOMUtil.hashCode(Node)`: new, agrees with `isEqual`.
  `XmlContent.hashCode` uses it and computes it on every call.
- Kept from the old code, as the maintainer's design: text nodes of white
  space only are not compared by `isEqual`, text is compared trimmed,
  prefixes, namespace declarations and the order of attributes do not
  count.
- Tightened:
  - only `xmlns` and `xmlns:prefix` are namespace declarations (before:
    every attribute whose name starts with `xmlns` was skipped);
  - comments and processing instructions are compared by name and value
    (before: by kind only);
  - the attributes of an element are one multiset of namespace, name and
    value (before: a lookup by name that could match an attribute without
    a namespace to one in a namespace, and two attributes to one); an
    attribute node is compared by the same three.
- The class comment of `XmlContent` states the contract: the content
  owns its document, and the nodes that it returns are for reading.
- `DOMUtilTest.testNodeEquals`: the two fixtures differ, and a copy is
  equal.

## Tests

- New `DOMUtilEqualityTest` (33 tests): 14 rows of different documents, 6
  rows of equal documents with equal hash codes, no mutation, hash codes
  tell documents apart, null nodes, comments and processing
  instructions, the namespace of an attribute, attributes as a multiset,
  nodes beside the document element, document fragments, attribute
  nodes, CDATA sections, the two-argument method and white space, a
  document 100,000 elements deep, the hash code of `XmlContent` after a
  change of its document.
- New `MultipleResourcesContentOnlyRepeatsTest` (1 test): 2, 5, 17 and 64
  repeats that differ only in content all expand.
- Red first: `red-surefire-summary.txt` (20 of 21 and 1 of 1 fail on the
  old code), `red-2-surefire-summary.txt` (3 failures),
  `red-3-surefire-summary.txt` (1 failure), `red-4-surefire-summary.txt`
  (3 failures, the findings of review round 1),
  `red-5-surefire-summary.txt` (2 failures, the findings of round 2).
- Not red first: `compareNodesWithTwoArgumentsComparesWhiteSpace`,
  `deepDocumentsAreCompared` and `cdataSections` were asked for by the
  reviewer and passed at once. The deep test was not run against the old,
  recursive code.
- Gate: `surefire-summary.txt`, two clean builds. The last one is the
  tree as committed. Full reactor on JDK 21: 7,890 tests, 0 failures, 0
  errors, 20 skipped. Conformance 937 tests with 12 skipped, unchanged.
  No other test of the reactor depended on the old comparison.
- Both gates had the change of bead policy4j-the.4.57 in the tree (it was
  committed before the last one). The two beads share no file. This bead
  was not built without it.

## Spec

`specs/attribute-resolution/spec.md`: new requirement "XML content
compares by structure" with four scenarios.
`openspec validate modernize-core-for-jdk28-valhalla --strict` passes.

## Review

`reviews/code/rv091-xml-content-equality-codex`: Codex, three rounds.
Round 1 REQUEST_CHANGES (one HIGH, two MEDIUM): attribute matching, nodes
beside the document element, the kept hash code. Round 2 REQUEST_CHANGES
(two MEDIUM, one LOW): document fragments and attribute nodes, fixed; the
hash codes that `Entity` and `RequestContext` keep, answered with the
contract on `XmlContent`. Round 3 APPROVE; the CDATA test it named is
added.

## JMH

Not measured. `XmlContent.hashCode` now walks the document; `Entity` and
`RequestContext` keep their hash codes, so the walk runs once per entity.
No JMH module exists yet (task 9.2).

## Limits

- White space: `<i> 1 </i>` and `<i>1</i>` are equal contents, by the
  kept rule. An XPath can tell them apart. Not changed here: it is the
  design of the old code, and a request controls its own content either
  way.
- `XmlContent.toNode()` and the XPath methods of `Content` return nodes
  of the document; the XPath functions need them (node identity,
  ancestors). The contract is that a caller does not change them. A
  caller that does gets an `Entity` or a `RequestContext` whose kept
  hash code no longer fits its content. They kept their hash codes
  before this run too. Nothing enforces the contract.
- A text node and a CDATA section with the same text are not equal.
- `compareNodes(Node, Node)` is public. It now returns false for trees
  that differ below the root; a caller that relied on the old result
  relied on the defect.
