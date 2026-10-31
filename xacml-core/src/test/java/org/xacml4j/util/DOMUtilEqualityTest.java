package org.xacml4j.util;

/*
 * #%L
 * Xacml4J Core Engine Implementation
 * %%
 * Copyright (C) 2009 - 2014 Xacml4J.org
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xacml4j.v30.content.XmlContent;

/**
 * Bead policy4j-the.4.60: the comparison of two DOM nodes looks at the
 * whole tree, does not change the nodes, and has a hash code that agrees
 * with it. {@link XmlContent} uses both for {@code equals} and
 * {@code hashCode}.
 */
public class DOMUtilEqualityTest
{
	@ParameterizedTest(name = "{0}")
	@MethodSource("differentDocuments")
	public void differentDocumentsAreNotEqual(String name, String first, String second)
	{
		Document a = document(first);
		Document b = document(second);

		assertFalse(DOMUtil.isEqual(a, b));
		assertFalse(DOMUtil.isEqual(b, a));
		assertFalse(DOMUtil.isEqual(a.getDocumentElement(), b.getDocumentElement()));
		assertFalse(DOMUtil.compareNodes(a, b));
		assertFalse(XmlContent.of(first).equals(XmlContent.of(second)));
	}

	static Stream<Arguments> differentDocuments()
	{
		return Stream.of(
				Arguments.of("root attribute value", "<r n=\"0\"><i>1</i></r>", "<r n=\"1\"><i>1</i></r>"),
				Arguments.of("root name", "<r/>", "<s/>"),
				Arguments.of("root namespace", "<r xmlns=\"urn:a\"/>", "<r xmlns=\"urn:b\"/>"),
				Arguments.of("child element name", "<r><a/></r>", "<r><b/></r>"),
				Arguments.of("child text", "<r><i>1</i></r>", "<r><i>2</i></r>"),
				Arguments.of("number of children", "<r><i/></r>", "<r><i/><i/></r>"),
				Arguments.of("order of children", "<r><a/><b/></r>", "<r><b/><a/></r>"),
				Arguments.of("child attribute value", "<r><i a=\"1\"/></r>", "<r><i a=\"2\"/></r>"),
				Arguments.of("child attribute name", "<r><i a=\"1\"/></r>", "<r><i b=\"1\"/></r>"),
				Arguments.of("nested text", "<r><a><b>1</b></a></r>", "<r><a><b>2</b></a></r>"),
				Arguments.of("nested element", "<r><a><b/></a></r>", "<r><a><c/></a></r>"),
				Arguments.of("text against element", "<r>x</r>", "<r><x/></r>"),
				// Only xmlns and xmlns:prefix declare a namespace.
				Arguments.of("value of an attribute whose name starts with xmlns",
						"<r xmlnsx=\"1\"/>", "<r xmlnsx=\"2\"/>"),
				Arguments.of("attribute whose name starts with xmlns", "<r xmlnsx=\"1\"/>", "<r/>"));
	}

	@ParameterizedTest(name = "{0}")
	@MethodSource("equalDocuments")
	public void equalDocumentsAreEqualAndHaveTheSameHashCode(String name, String first, String second)
	{
		Document a = document(first);
		Document b = document(second);

		assertTrue(DOMUtil.isEqual(a, b));
		assertTrue(DOMUtil.isEqual(b, a));
		assertTrue(DOMUtil.isEqual(a.getDocumentElement(), b.getDocumentElement()));
		assertEquals(DOMUtil.hashCode(a), DOMUtil.hashCode(b));
		assertEquals(DOMUtil.hashCode(a), DOMUtil.hashCode(a.getDocumentElement()));
		XmlContent x = XmlContent.of(first);
		XmlContent y = XmlContent.of(second);
		assertEquals(x, y);
		assertEquals(x.hashCode(), y.hashCode());
	}

	static Stream<Arguments> equalDocuments()
	{
		return Stream.of(
				Arguments.of("the same text", "<r n=\"0\"><i>1</i></r>", "<r n=\"0\"><i>1</i></r>"),
				Arguments.of("namespace prefix",
						"<a:r xmlns:a=\"urn:x\"><a:i a:k=\"v\">1</a:i></a:r>",
						"<b:r xmlns:b=\"urn:x\"><b:i b:k=\"v\">1</b:i></b:r>"),
				Arguments.of("order of attributes", "<r a=\"1\" b=\"2\"/>", "<r b=\"2\" a=\"1\"/>"),
				Arguments.of("white space between elements", "<r><i>1</i><j/></r>", "<r>\n  <i>1</i>\n  <j/>\n</r>"),
				Arguments.of("white space around text", "<r><i>1</i></r>", "<r><i> 1 </i></r>"),
				Arguments.of("unused namespace declaration", "<r/>", "<r xmlns:u=\"urn:unused\"/>"));
	}

	/** Before the fix a comparison removed the white-space text nodes of both documents. */
	@Test
	public void comparisonDoesNotChangeTheNodes()
	{
		Document a = document("<r>\n  <i>1</i>\n  <j/>\n</r>");
		Document b = document("<r><i>1</i><j/></r>");
		int before = a.getDocumentElement().getChildNodes().getLength();

		assertTrue(DOMUtil.isEqual(a, b));
		DOMUtil.hashCode(a);

		assertEquals(5, before);
		assertEquals(before, a.getDocumentElement().getChildNodes().getLength());
	}

	/**
	 * An XPath can select a comment or a processing instruction, so their
	 * text is part of the comparison. The parser of {@link DOMUtil} drops
	 * comments; content that comes from another DOM can have them.
	 */
	@Test
	public void commentsAndProcessingInstructionsAreCompared()
	{
		assertFalse(DOMUtil.isEqual(with(d -> d.createComment("a")), with(d -> d.createComment("b"))));
		assertFalse(DOMUtil.isEqual(with(d -> d.createProcessingInstruction("p", "a")),
				with(d -> d.createProcessingInstruction("p", "b"))));
		assertFalse(DOMUtil.isEqual(with(d -> d.createProcessingInstruction("p", "a")),
				with(d -> d.createProcessingInstruction("q", "a"))));
		assertFalse(DOMUtil.isEqual(with(d -> d.createComment("a")),
				with(d -> d.createProcessingInstruction("p", "a"))));

		assertTrue(DOMUtil.isEqual(with(d -> d.createComment("a")), with(d -> d.createComment("a"))));
		assertEquals(DOMUtil.hashCode(with(d -> d.createComment("a"))),
				DOMUtil.hashCode(with(d -> d.createComment("a"))));
		assertTrue(DOMUtil.isEqual(with(d -> d.createProcessingInstruction("p", "a")),
				with(d -> d.createProcessingInstruction("p", "a"))));
		assertEquals(DOMUtil.hashCode(with(d -> d.createProcessingInstruction("p", "a"))),
				DOMUtil.hashCode(with(d -> d.createProcessingInstruction("p", "a"))));
	}

	/**
	 * An attribute without a namespace is not the attribute of the same
	 * qualified name in a namespace. A parser that is aware of namespaces
	 * does not make the first kind with a prefix; another DOM can.
	 */
	@Test
	public void namespaceOfAnAttributeIsCompared()
	{
		Document a = document("<r/>");
		a.getDocumentElement().setAttribute("p:a", "1");
		Document b = document("<r/>");
		b.getDocumentElement().setAttributeNS("urn:x", "p:a", "1");

		assertFalse(DOMUtil.isEqual(a, b));
		assertFalse(DOMUtil.isEqual(b, a));
	}

	/**
	 * A DOM that a caller builds can hold two attributes with the same
	 * namespace and local name under different prefixes. The attributes
	 * are compared as a multiset, the same in both directions.
	 */
	@Test
	public void attributesWithTheSameExpandedNameAreComparedAsAMultiset()
	{
		Document a = withAttributes("p:a", "q:a");
		Document b = withAttributes("p:a", "q:b");
		Document c = withAttributes("q:a", "p:a");

		assertFalse(DOMUtil.isEqual(a, b));
		assertFalse(DOMUtil.isEqual(b, a));
		assertTrue(DOMUtil.isEqual(a, c));
		assertTrue(DOMUtil.isEqual(c, a));
		assertEquals(DOMUtil.hashCode(a), DOMUtil.hashCode(c));
	}

	/** The document {@code <r/>} with one attribute of value 1 in the namespace urn:x for every qualified name. */
	private static Document withAttributes(String... qualifiedNames)
	{
		Document d = document("<r/>");
		for (String name : qualifiedNames) {
			Attr attr = d.createAttributeNS("urn:x", name);
			attr.setValue("1");
			// setAttributeNode replaces by qualified name, so p:a and q:a are two attributes
			d.getDocumentElement().setAttributeNode(attr);
		}
		assertEquals(qualifiedNames.length, d.getDocumentElement().getAttributes().getLength());
		return d;
	}

	/** A processing instruction beside the document element is part of the document. */
	@Test
	public void nodesBesideTheDocumentElementAreCompared()
	{
		Document a = document("<?p a?><r/>");
		Document b = document("<?p b?><r/>");

		assertFalse(DOMUtil.isEqual(a, b));
		assertFalse(DOMUtil.isEqual(a, document("<r/>")));
		assertFalse(DOMUtil.isEqual(document("<r/>"), a));
		assertTrue(DOMUtil.isEqual(a, document("<?p a?><r/>")));
		assertEquals(DOMUtil.hashCode(a), DOMUtil.hashCode(document("<?p a?><r/>")));
		// XmlContent keeps the document element only.
		assertEquals(XmlContent.of(a), XmlContent.of(b));
		assertEquals(XmlContent.of(a).hashCode(), XmlContent.of(b).hashCode());
	}

	/** A document fragment is compared by its children, as a document is. */
	@Test
	public void documentFragmentsAreComparedByTheirChildren()
	{
		assertFalse(DOMUtil.isEqual(fragment("a"), fragment("b")));
		assertFalse(DOMUtil.isEqual(fragment("a"), fragment("a", "a")));
		assertFalse(DOMUtil.isEqual(fragment("a", "b"), fragment("b", "a")));
		assertTrue(DOMUtil.isEqual(fragment("a", "b"), fragment("a", "b")));
		assertEquals(DOMUtil.hashCode(fragment("a", "b")), DOMUtil.hashCode(fragment("a", "b")));
	}

	/** A document fragment with one empty element for every name. */
	private static DocumentFragment fragment(String... names)
	{
		Document d = document("<r/>");
		DocumentFragment fragment = d.createDocumentFragment();
		for (String name : names) {
			fragment.appendChild(d.createElementNS("urn:x", name));
		}
		return fragment;
	}

	/** Two attribute nodes are compared by namespace, name and value, as the attributes of an element are. */
	@Test
	public void attributeNodesAreComparedByNamespaceNameAndValue()
	{
		assertFalse(DOMUtil.isEqual(attribute("urn:a", "p:k", "v"), attribute("urn:b", "p:k", "v")));
		assertFalse(DOMUtil.isEqual(attribute("urn:a", "p:k", "v"), attribute("urn:a", "p:k", "w")));
		assertFalse(DOMUtil.isEqual(attribute("urn:a", "p:k", "v"), attribute("urn:a", "p:j", "v")));
		// The prefix does not count.
		assertTrue(DOMUtil.isEqual(attribute("urn:a", "p:k", "v"), attribute("urn:a", "q:k", "v")));
		assertEquals(DOMUtil.hashCode(attribute("urn:a", "p:k", "v")), DOMUtil.hashCode(attribute("urn:a", "q:k", "v")));
	}

	private static Attr attribute(String namespace, String qualifiedName, String value)
	{
		Attr attr = document("<r/>").createAttributeNS(namespace, qualifiedName);
		attr.setValue(value);
		return attr;
	}

	/**
	 * A CDATA section is text for the white-space rules. It is not equal
	 * to a text node with the same text: the kinds of the nodes differ.
	 */
	@Test
	public void cdataSections()
	{
		Document padded = document("<r><![CDATA[ x ]]></r>");
		Document plain = document("<r><![CDATA[x]]></r>");
		assertEquals(Node.CDATA_SECTION_NODE, plain.getDocumentElement().getFirstChild().getNodeType());
		assertTrue(DOMUtil.isEqual(padded, plain));
		assertEquals(DOMUtil.hashCode(padded), DOMUtil.hashCode(plain));

		Document blank = document("<r><![CDATA[ ]]></r>");
		assertTrue(DOMUtil.isEqual(blank, document("<r/>")));
		assertEquals(DOMUtil.hashCode(blank), DOMUtil.hashCode(document("<r/>")));
		assertEquals(1, blank.getDocumentElement().getChildNodes().getLength());

		assertFalse(DOMUtil.isEqual(plain, document("<r>x</r>")));
		assertFalse(DOMUtil.isEqual(document("<r>x</r>"), plain));
	}

	/** The method with two arguments compares the text nodes of white space too, as before. */
	@Test
	public void compareNodesWithTwoArgumentsComparesWhiteSpace()
	{
		Document a = document("<r/>");
		Document b = document("<r> </r>");

		assertTrue(DOMUtil.isEqual(a, b));
		assertTrue(DOMUtil.compareNodes(a, b, true));
		assertEquals(DOMUtil.hashCode(a), DOMUtil.hashCode(b));
		assertFalse(DOMUtil.compareNodes(a, b));
		assertFalse(DOMUtil.compareNodes(a, b, false));
	}

	/**
	 * The comparison and the hash code do not recurse, so a deep document
	 * does not overflow the stack. Added with the fix; it was not run
	 * against the old, recursive code.
	 */
	@Test
	public void deepDocumentsAreCompared()
	{
		Document a = deep(100_000, "1");
		Document b = deep(100_000, "1");
		Document c = deep(100_000, "2");

		assertTrue(DOMUtil.isEqual(a, b));
		assertEquals(DOMUtil.hashCode(a), DOMUtil.hashCode(b));
		assertFalse(DOMUtil.isEqual(a, c));
	}

	/** {@code depth} elements, one inside the other, with {@code text} in the innermost. */
	private static Document deep(int depth, String text)
	{
		Document d = document("<r/>");
		// From the inside out: appendChild walks the ancestors of the parent.
		Element inner = d.createElement("e");
		inner.setTextContent(text);
		for (int i = 1; i < depth; i++) {
			Element outer = d.createElement("e");
			outer.appendChild(inner);
			inner = outer;
		}
		d.getDocumentElement().appendChild(inner);
		return d;
	}

	/**
	 * {@link XmlContent} does not keep its hash code: the document can be
	 * reached through {@code toNode()}, and {@code hashCode} has to agree
	 * with {@code equals} for the document as it is.
	 */
	@Test
	public void hashCodeOfXmlContentFollowsTheDocument()
	{
		XmlContent x = XmlContent.of("<r n=\"0\"/>");
		int before = x.hashCode();

		((Document) x.toNode()).getDocumentElement().setAttribute("n", "1");

		XmlContent y = XmlContent.of("<r n=\"1\"/>");
		assertEquals(x, y);
		assertEquals(x.hashCode(), y.hashCode());
		assertFalse(before == x.hashCode());
	}

	/** The document {@code <r><i>1</i></r>} with one more child of the root, made by {@code child}. */
	private static Document with(Function<Document, Node> child)
	{
		Document d = document("<r><i>1</i></r>");
		d.getDocumentElement().appendChild(child.apply(d));
		return d;
	}

	@Test
	public void hashCodeTellsDifferentDocumentsApart()
	{
		Set<Integer> hashes = new HashSet<>();
		Set<XmlContent> contents = new HashSet<>();
		for (int i = 0; i < 50; i++) {
			hashes.add(DOMUtil.hashCode(document("<r n=\"" + i + "\"><i>" + i + "</i></r>")));
			contents.add(XmlContent.of("<r><i>" + i + "</i></r>"));
			// The same content again must not add an element.
			contents.add(XmlContent.of("<r><i>" + i + "</i></r>"));
		}

		// Equal hash codes for different documents are allowed, but not as the rule.
		assertTrue(hashes.size() > 40, () -> "distinct hash codes: " + hashes.size());
		assertEquals(50, contents.size());
	}

	@Test
	public void nullNodes()
	{
		Node a = document("<r/>");

		assertTrue(DOMUtil.isEqual(null, null));
		assertFalse(DOMUtil.isEqual(a, null));
		assertFalse(DOMUtil.isEqual(null, a));
	}

	private static Document document(String xml)
	{
		return (Document) DOMUtil.parseXml(xml).orElseThrow();
	}
}
