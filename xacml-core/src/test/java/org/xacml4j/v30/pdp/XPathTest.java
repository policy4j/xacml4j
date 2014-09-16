package org.xacml4j.v30.pdp;

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

import static org.junit.Assert.assertNotNull;

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xacml4j.util.NamespaceContextBuilder;
import org.xacml4j.util.NodeNamespaceContext;
import org.xml.sax.InputSource;


public class XPathTest
{
	private String testXml = "<md:record xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
	"xmlns:md=\"urn:example:med:schemas:record\">" +
	"<md:patient>" +
	"<md:patientDoB>1992-03-21</md:patientDoB>" +
	"<md:patient-number>555555</md:patient-number>" +
	"</md:patient>" +
	"</md:record>";

	private Document doc;
	private XPathFactory xpf;

	@Before
	public void init() throws Exception
	{
		this.xpf = XPathFactory.newInstance();

		DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
		f.setNamespaceAware(true);
		DocumentBuilder builder = f.newDocumentBuilder();
		this.doc = builder.parse(new InputSource(new StringReader(testXml)));
	}

	@Test
	public void testXPath() throws Exception
	{
		XPath p0 = xpf.newXPath();
		p0.setNamespaceContext(new NodeNamespaceContext(doc));
		Node ctx = (Node)p0.evaluate("md:record/md:patient/md:patientDoB", doc,
				XPathConstants.NODE);
		assertNotNull(ctx);
		XPath p1 = xpf.newXPath();
		p1.setNamespaceContext(new NodeNamespaceContext(ctx));
		Object r = p1.evaluate("count(ancestor-or-self::md:record)", ctx);
		System.out.println(r);
		assertNotNull(r);
	}

    @Test
    public void testXPath1() throws Exception
    {
        XPath p0 = xpf.newXPath();
        p0.setNamespaceContext(NamespaceContextBuilder.builder().delegate(new NodeNamespaceContext(doc)).build());
        Node ctx = (Node)p0.evaluate("md:record/md:patient/md:patientDoB", doc,
                XPathConstants.NODE);
        assertNotNull(ctx);
        XPath p1 = xpf.newXPath();
        p1.setNamespaceContext(new NodeNamespaceContext(ctx));
        Object r = p1.evaluate("count(ancestor-or-self::md:record)", ctx);
        System.out.println(r);
        assertNotNull(r);
    }
}
