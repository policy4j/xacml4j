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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class DOMUtilTest
{
	private String testXml1 = "<md:record xmlns:md=\"urn:example:med:schemas:record\" " +
	"xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" +
	"<md:patient>" +
	"<md:patientDoB attrn2=\"v\" md:attrn1=\"test\">1992-03-21</md:patientDoB>" +
	"<md:patient-number>555555</md:patient-number>" +
	"</md:patient>" +
	"<md:patient>" +
	"<md:patientDoB attrn2=\"v1\" md:attrn1=\"test1\">1991-01-11</md:patientDoB>" +
	"<md:patient-number>11111</md:patient-number>" +
	"</md:patient>" +
	"</md:record>";

	private String testXml2 = "<md:record xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
	"xmlns:md=\"urn:example:med:schemas:record\">" +
	"<md:patient>" +
	"<md:patientDoB md:attrn1=\"test\" attrn2=\"v\" >1991-03-21</md:patientDoB>" +
	"<md:patient-number>66666</md:patient-number>" +
	"</md:patient>" +
	"<md:patient>" +
	"<md:patientDoB md:attrn1=\"test1\" attrn2=\"v1\">1992-01-11</md:patientDoB>" +
	"<md:patient-number>12394</md:patient-number>" +
	"</md:patient>" +
	"</md:record>";

	private String testXml4 = "<test>aaa</test>";

	private Element content1;
	private Element content2;
	private Element content4;
	private XPathFactory xpf;
	private XPath xpath;

	@Before
	public void init() throws Exception
	{
		DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
		f.setNamespaceAware(true);
		DocumentBuilder builder = f.newDocumentBuilder();
		this.content1 = builder.parse(new InputSource(new StringReader(testXml1))).getDocumentElement();
		this.content2 = builder.parse(new InputSource(new StringReader(testXml2))).getDocumentElement();
		this.content2 = builder.parse(new InputSource(new StringReader(testXml2))).getDocumentElement();
		this.content4 = builder.parse(new InputSource(new StringReader(testXml4))).getDocumentElement();
		this.xpf = XPathFactory.newInstance();
		this.xpath = xpf.newXPath();
		xpath.setNamespaceContext(new NodeNamespaceContext(content1));

	}

	@Test
	public void testCopyNode()
	{
		Document copy  = DOMUtil.copyNode(content1);
		Element c  = copy.getDocumentElement();
		assertEquals(2, c.getChildNodes().getLength());
		assertEquals("record", c.getLocalName());
		assertEquals("urn:example:med:schemas:record", c.getNamespaceURI());
		assertEquals(2, c.getChildNodes().getLength());
		assertEquals("patient", c.getChildNodes().item(0).getLocalName());
		assertEquals("urn:example:med:schemas:record", copy.getChildNodes().item(0).getNamespaceURI());
		assertEquals("patient", c.getChildNodes().item(1).getLocalName());
		assertEquals("urn:example:med:schemas:record", c.getChildNodes().item(1).getNamespaceURI());
		assertTrue(c.isEqualNode(content1));
		assertTrue(DOMUtil.compareNodes(c, content1));

	}

	@Test
	public void testNodeXPathElementNodes1() throws Exception{
		NodeList nodes = (NodeList)xpath.evaluate("//md:record/md:patient", content1, XPathConstants.NODESET);
		assertEquals("//md:record/md:patient[1]", DOMUtil.getXPath(nodes.item(0)));
		assertEquals("//md:record/md:patient[2]", DOMUtil.getXPath(nodes.item(1)));
	}

	@Test
	public void testNodeXPathElementTextNodes() throws Exception{
		assertEquals("//md:record", DOMUtil.getXPath(content1));
		NodeList nodes = (NodeList)xpath.evaluate("//md:record/md:patient/md:patientDoB/text()", content1, XPathConstants.NODESET);
		assertEquals("//md:record/md:patient[1]/md:patientDoB[1]/text()", DOMUtil.getXPath(nodes.item(0)));
		assertEquals("//md:record/md:patient[2]/md:patientDoB[1]/text()", DOMUtil.getXPath(nodes.item(1)));
	}


	@Test
	public void testNodeListToList() throws Exception{
		assertEquals("//md:record", DOMUtil.getXPath(content1));
		NodeList nodes = (NodeList)xpath.evaluate("//md:record/md:patient/md:patientDoB/text()", content1, XPathConstants.NODESET);
		List<Node> list = DOMUtil.nodeListToList(nodes);
		assertEquals(2, list.size());
		assertEquals("//md:record/md:patient[1]/md:patientDoB[1]/text()", DOMUtil.getXPath(nodes.item(0)));
		assertEquals("//md:record/md:patient[2]/md:patientDoB[1]/text()", DOMUtil.getXPath(nodes.item(1)));
	}

	@Test
	public void testNodeXPathAttributeNodes() throws Exception{
		NodeList nodes = (NodeList)xpath.evaluate("//md:record/md:patient/md:patientDoB/@md:attrn1", content1, XPathConstants.NODESET);
		assertEquals("//md:record/md:patient[1]/md:patientDoB[1]/@md:attrn1", DOMUtil.getXPath(nodes.item(0)));
		assertEquals("//md:record/md:patient[2]/md:patientDoB[1]/@md:attrn1", DOMUtil.getXPath(nodes.item(1)));
	}

	@Test
	public void testNodeXPathElementNodes2() throws Exception{
		NodeList nodes = (NodeList)xpath.evaluate("//md:record/md:patient/md:patientDoB", content1, XPathConstants.NODESET);
		assertEquals("//md:record/md:patient[1]/md:patientDoB[1]", DOMUtil.getXPath(nodes.item(0)));
		assertEquals("//md:record/md:patient[2]/md:patientDoB[1]", DOMUtil.getXPath(nodes.item(1)));
	}

	@Test
	public void testWriteNodeToStream() throws Exception
	{
		ByteArrayOutputStream out1 = new ByteArrayOutputStream();
		DOMUtil.serializeToXml(content1, out1);
		assertEquals(testXml1, new String(out1.toByteArray()));
	}

	@Test
	public void testNodeEquals(){
		assertTrue(DOMUtil.isEqual(null, null));
		assertFalse(DOMUtil.isEqual(content1, null));
		assertFalse(DOMUtil.isEqual(null, content1));
		assertTrue(DOMUtil.isEqual(content1, content2));
		assertTrue(DOMUtil.isEqual(content2, content1));
	}

	@Test
	public void testNodeToString(){
		assertEquals("{urn:example:med:schemas:record}record", DOMUtil.toString(content1));
		assertEquals("test", DOMUtil.toString(content4));
		assertNull(DOMUtil.toString(null));
	}

}
