package com.artagon.xacml.util;

import static org.junit.Assert.assertEquals;

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class DOMUtilTest 
{
	private String testXml = "<md:record xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
	"xmlns:md=\"urn:example:med:schemas:record\">" +
	"<md:patient>" +
	"<md:patientDoB md:attrn1=\"test\" attrn2=\"v\" >1992-03-21</md:patientDoB>" +
	"<md:patient-number>555555</md:patient-number>" +
	"</md:patient>" +
	"<md:patient>" +
	"<md:patientDoB md:attrn1=\"test1\" attrn2=\"v1\">1991-01-11</md:patientDoB>" +
	"<md:patient-number>11111</md:patient-number>" +
	"</md:patient>" +
	"</md:record>";
	
	private Node content;
	private XPathFactory xpf;
	private XPath xpath;
	
	@Before
	public void init() throws Exception
	{
		DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
		f.setNamespaceAware(true);
		DocumentBuilder builder = f.newDocumentBuilder();
		this.content = builder.parse(new InputSource(new StringReader(testXml)));
		this.xpf = XPathFactory.newInstance();
		this.xpath = xpf.newXPath();
		xpath.setNamespaceContext(new NodeNamespaceContext(content));
	
	}
	
	@Test
	public void testNodeXPathElementNodes1() throws Exception{
		NodeList nodes = (NodeList)xpath.evaluate("//md:record/md:patient", content, XPathConstants.NODESET);
		assertEquals("//md:record[1]/md:patient[1]", DOMUtil.getXPath(nodes.item(0)));
		assertEquals("//md:record[1]/md:patient[2]", DOMUtil.getXPath(nodes.item(1)));
	}
	
	@Test
	public void testNodeXPathElementTextNodes() throws Exception{
		NodeList nodes = (NodeList)xpath.evaluate("//md:record/md:patient/md:patientDoB/text()", content, XPathConstants.NODESET);
		assertEquals("//md:record[1]/md:patient[1]/md:patientDoB[1]/text()", DOMUtil.getXPath(nodes.item(0)));
		assertEquals("//md:record[1]/md:patient[2]/md:patientDoB[1]/text()", DOMUtil.getXPath(nodes.item(1)));
	}
	
	@Test
	public void testNodeXPathAttributeNodes() throws Exception{
		NodeList nodes = (NodeList)xpath.evaluate("//md:record/md:patient/md:patientDoB/@md:attrn1", content, XPathConstants.NODESET);
		assertEquals("//md:record[1]/md:patient[1]/md:patientDoB[1]/@md:attrn1", DOMUtil.getXPath(nodes.item(0)));
		assertEquals("//md:record[1]/md:patient[2]/md:patientDoB[1]/@md:attrn1", DOMUtil.getXPath(nodes.item(1)));
	}
	
	@Test
	public void testNodeXPathElementNodes2() throws Exception{
		NodeList nodes = (NodeList)xpath.evaluate("//md:record/md:patient/md:patientDoB", content, XPathConstants.NODESET);
		assertEquals("//md:record[1]/md:patient[1]/md:patientDoB[1]", DOMUtil.getXPath(nodes.item(0)));
		assertEquals("//md:record[1]/md:patient[2]/md:patientDoB[1]", DOMUtil.getXPath(nodes.item(1)));
	}
}
