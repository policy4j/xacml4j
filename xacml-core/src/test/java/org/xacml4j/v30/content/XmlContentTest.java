package org.xacml4j.v30.content;

/*
 * #%L
 * Xacml4J Core Engine Implementation
 * %%
 * Copyright (C) 2009 - 2019 Xacml4J.org
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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xacml4j.util.DOMUtil;
import org.xacml4j.util.JSONUtil;
import org.xacml4j.v30.AttributeSelectorKey;
import org.xacml4j.v30.BagOfValues;
import org.xacml4j.v30.PathEvaluationException;
import org.xacml4j.v30.types.XacmlTypes;

public class XmlContentTest
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

    private XmlContent xml1;
    private XmlContent xml2;
    private XmlContent xml3;
    private XmlContent xml4;

    @Before
    public void setUp(){
        this.xml1 = XmlContent.of(
                XmlContent.fromString(testXml1));
        this.xml2 = XmlContent.of(
                XmlContent.fromString(testXml2));
        Optional<Document> xml = DOMUtil.parseXml(Thread.currentThread()
                               .getContextClassLoader()
                               .getResourceAsStream("./TestXpathContent.xml"))
                                        .map(Document.class::cast);
        Node e = xml.get().getElementsByTagName("md:record").item(0);

        this.xml4 = XmlContent.of(e);
        this.xml3 = XmlContent.of(
                XmlContent
                        .fromStream(
                                Thread.currentThread()
                                        .getContextClassLoader()
                                        .getResourceAsStream("./testContentXPathFunctions.xml")));
    }

    @Test
    public void testEquals(){
        XmlContent xml1 = XmlContent.of(testXml1);
        XmlContent xml2 = XmlContent.of(testXml1);
        XmlContent xml3 = XmlContent.of(testXml2);
        XmlContent xml4 = XmlContent.of(testXml2);
        assertEquals(xml1, xml2);
        assertEquals(xml3, xml4);
    }

    @Test
    public void testTestXml1(){
        assertEquals("1992-03-21", xml1.evaluateToString("//md:record/md:patient[1]/md:patientDoB[1]").get());
        assertTrue(xml1.evaluateToNodePathList("//md:record/md:patient[1]/md:patientDoB[1]").contains("//md:record/md:patient[1]/md:patientDoB[1]"));
    }

    @Test
    public void testEvaluateToNodePath(){
        Optional<String> path = xml1.evaluateToNodePath("//md:record/md:patient");
        assertEquals(path.get(), "//md:record/md:patient[1]");
    }

    @Test
    public void testEvaluateToNodePathList(){
        List<String> paths = xml1.evaluateToNodePathList("//md:record/md:patient");
        assertTrue(paths.contains("//md:record/md:patient[1]"));
        assertTrue(paths.contains("//md:record/md:patient[2]"));

        paths = xml1.evaluateToNodePathList("//md:record/md:pati");
        assertNotNull(paths);
        assertTrue(paths.isEmpty());
    }


    @Test
    public void testXpathEvaluation() throws Exception
    {
        List<Node> result = xml3.evaluateToNodeSet("//md:record");
        List<Node> result1 = xml1.evaluateToNodeSet("//md:record/md:patient");
        List<Node> result2 = xml1.evaluateToNodeSet("//*[local-name()='record'][namespace-uri()='urn:example:med:schemas:record']");
        assertEquals(1, result.size());
        assertEquals(2, result1.size());
        assertEquals(1, result2.size());
    }

    @Test
    public void testEntityXPathCorrectType(){
        Optional<BagOfValues> values = xml1.resolve(
                AttributeSelectorKey
                        .builder()
                        .xpath("/md:record/md:patient/md:patient-number/text()")
                        .dataType(XacmlTypes.INTEGER)
                        .build());
        assertTrue(values.get().contains(XacmlTypes.INTEGER.of(555555)));
    }

    @Test
    public void testEntityXPathCorrectTypeString(){
        Optional<BagOfValues> values = xml1.resolve(
                AttributeSelectorKey
                        .builder()
                        .xpath("/md:record/md:patient/md:patient-number/text()")
                        .dataType(XacmlTypes.STRING)
                        .build());
        assertTrue(values.get().contains(XacmlTypes.STRING.of("555555")));
    }

    @Test
    public void testEntityXPathCorrectTypeString4(){
        Optional<BagOfValues> values = xml4.resolve(
                AttributeSelectorKey
                        .builder()
                        .xpath("//md:record/md:patient_info/md:health_insurance/text()")
                        .dataType(XacmlTypes.STRING)
                        .build());
        assertTrue(values.get().contains(XacmlTypes.STRING.of("123456")));
        values = xml4.resolve(
                AttributeSelectorKey
                        .builder()
                        .xpath("./md:record/md:patient_info/md:health_insurance/text()")
                        .dataType(XacmlTypes.STRING)
                        .build());
        assertTrue(values.get().contains(XacmlTypes.STRING.of("123456")));
    }

    @Test(expected = PathEvaluationException.class)
    public void testXPathReturnUnsupportedNodeType()
    {
        Optional<BagOfValues> values = xml1.resolve(
                AttributeSelectorKey
                        .builder()
                        .xpath("/md:record/")
                        .dataType(XacmlTypes.INTEGER)
                        .build());
        assertFalse(values.isPresent());
    }

    @Test
    public void testWithContentEscaping(){
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><security>\n<through obscurity=\"true\"/></security>";
        XmlContent xmlContent1 = XmlContent.of(xml);
        XmlContent xmlContent2 = XmlContent.of(xmlContent1.asString());
        assertEquals(xmlContent1, xmlContent2);
    }

}
