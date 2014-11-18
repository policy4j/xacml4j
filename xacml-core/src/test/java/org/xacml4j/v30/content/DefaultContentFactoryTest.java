package org.xacml4j.v30.content;

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


import com.google.common.collect.Iterables;
import com.google.common.net.MediaType;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import org.xacml4j.v30.BagOfAttributeExp;
import org.xacml4j.v30.Categories;
import org.xacml4j.v30.ResponseContext;
import org.xacml4j.v30.types.DateExp;
import org.xacml4j.v30.types.XPathExp;
import org.xacml4j.v30.types.XacmlTypes;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;

public class DefaultContentFactoryTest
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


    private ContentFactory provider;

    @Before
    public void init(){
        this.provider = new DefaultContentFactory();
    }

    @Test
    public void testCreateXmlContent() throws Exception
    {
        Content content = provider.from(MediaType.XML_UTF_8, new StringReader(testXml1));
        Iterable<Content> it = content.selectNodes(XPathExp.of("//md:record/md:patient", Categories.RESOURCE));
        Content content1 = Iterables.get(it, 0);
        Content content2 = Iterables.get(it, 1);
        BagOfAttributeExp date = content1.select(XPathExp.of("//md:patientDoB/text()"), XacmlTypes.DATE);
        assertEquals(date.first(), DateExp.of("1992-03-21"));
        XmlContentParser.XmlNode xml1 = (XmlContentParser.XmlNode)content1;
        XmlContentParser.XmlNode xml2 = (XmlContentParser.XmlNode)content2;
        assertEquals("//md:record/md:patient[1]", xml1.getPath());
        assertEquals("//md:record/md:patient[2]", xml2.getPath());
    }

    @Test
    public void testCreateJsonContent() throws Exception
    {
        InputStream stream = getClass().getClassLoader().getResourceAsStream("test-json-content.json");
        Content content = provider.from(MediaType.JSON_UTF_8, new InputStreamReader(stream));
        assertNotNull(content);
        BagOfAttributeExp price = content.select(XPathExp.of("$.store..price"), XacmlTypes.DOUBLE);
        assertFalse(price.isEmpty());

        price = content.select(XPathExp.of("$.store..price"), XacmlTypes.STRING);
    }
}
