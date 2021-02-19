package org.xacml4j.v30.xml;

import org.junit.Before;
import org.junit.Test;
import org.xacml4j.util.DOMUtil;
import org.xacml4j.v30.AttributeSelectorKey;
import org.xacml4j.v30.BagOfAttributeValues;
import org.xacml4j.v30.Content;
import org.xacml4j.v30.XmlContent;
import org.xacml4j.v30.XPathEvaluationException;
import org.xacml4j.v30.types.XacmlTypes;

import java.util.Optional;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

public class XmlContentTest
{
    private String testXml = "<md:record xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
            "xmlns:md=\"urn:example:med:schemas:record\">" +
            "<md:patient>" +
            "<md:patientDoB>1992-03-21</md:patientDoB>" +
            "<md:patient-number>555555</md:patient-number>" +
            "</md:patient>" +
            "</md:record>";

    private Content content;

    @Before
    public void init(){
        this.content = XmlContent.of(testXml);
    }

    @Test
    public void testEntityXPathCorrectType(){
        Optional<BagOfAttributeValues> values = content.resolve(
                AttributeSelectorKey
                        .builder()
                        .xpath("/md:record/md:patient/md:patient-number/text()")
                        .dataType(XacmlTypes.INTEGER)
                        .build());
        assertTrue(values.get().contains(XacmlTypes.INTEGER.of(555555)));
    }

    @Test(expected= XPathEvaluationException.class)
    public void testXPathReturnUnsupportedNodeType()
    {
        Optional<BagOfAttributeValues> values = content.resolve(
                AttributeSelectorKey
                        .builder()
                        .xpath("/md:record/md:patient")
                        .dataType(XacmlTypes.INTEGER)
                        .build());
        assertFalse(values.isPresent());
    }
}

