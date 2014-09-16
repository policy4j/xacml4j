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

import com.google.common.collect.Iterators;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathFactory;
import java.io.StringReader;
import java.util.Iterator;

/**
 *
 * @author Giedrius Trumpickas
 */
public class NamespaceContextBuilderTest {

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
    public void testTheSameNamespaceDifferentPrefix(){
       NamespaceContext ctx = NamespaceContextBuilder
                .builder()
                .bind("aaa", "ns:uri0")
                .bind("bbb", "ns:uri0")
                .build();
        assertEquals("aaa", ctx.getPrefix("ns:uri0"));
        Iterator<String> prefixes = ctx.getPrefixes("ns:uri0");
        assertEquals("aaa", prefixes.next());
        assertEquals("bbb", prefixes.next());
    }

    @Test
    public void testBuilderDefaultNs(){
        NamespaceContext ctx = NamespaceContextBuilder
                .builder()
                .bind(XMLConstants.DEFAULT_NS_PREFIX, "ns:uri0")
                .build();
        assertEquals(XMLConstants.DEFAULT_NS_PREFIX, ctx.getPrefix("ns:uri0"));

        ctx = NamespaceContextBuilder
                .builder()
                .defaultNamespace("ns:uri0")
                .build();
        assertEquals(XMLConstants.DEFAULT_NS_PREFIX, ctx.getPrefix("ns:uri0"));
    }

    @Test
    public void testBuilderDelegate(){
        NamespaceContext ctx = NamespaceContextBuilder
                .builder()
                .bind("test", "urn:example:med:schemas:record")
                .delegate(doc)
                .build();
        Iterator<String> prefixes = ctx.getPrefixes("urn:example:med:schemas:record");
        assertEquals("test", Iterators.get(prefixes, 0));
        assertEquals("md", Iterators.get(prefixes, 0));
        assertFalse(prefixes.hasNext());

    }

    @Test
    public void testBuilderDelegate1(){
        NamespaceContext ctx = NamespaceContextBuilder
                .builder()
                .delegate(doc)
                .build();
        Iterator<String> prefixes = ctx.getPrefixes("urn:example:med:schemas:record");
        assertEquals("md", Iterators.get(prefixes, 0));
        assertFalse(prefixes.hasNext());
        assertEquals("urn:example:med:schemas:record", ctx.getNamespaceURI("md"));
        assertEquals(XMLConstants.NULL_NS_URI, ctx.getNamespaceURI("maaaaa"));

    }
}
