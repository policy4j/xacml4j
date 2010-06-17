package com.artagon.xacml.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.artagon.xacml.v3.AttributeCategoryId;
import com.artagon.xacml.v3.types.XacmlDataTypes;
import com.artagon.xacml.v3.types.StringType.StringValue;
import com.artagon.xacml.v3.types.XPathExpressionType.XPathExpressionValue;

public class Xacml20XPathTo30TransformerTest 
{
	
	@Test
	public void testXacml20StringXPathToXPathExpression()
	{
		StringValue xpath = XacmlDataTypes.STRING.create("//Request/Resource/ResourceContent/md:record/md:patient/md:patient-number/text()");
		XPathExpressionValue xpathExp = Xacml20XPathTo30Transformer.fromXacml20String(xpath);
		assertEquals(XacmlDataTypes.XPATHEXPRESSION.create("//md:record/md:patient/md:patient-number/text()", AttributeCategoryId.RESOURCE), xpathExp);
	}
	
	@Test
	public void testXpathTransformation() throws Exception
	{
		assertEquals("//md:record/md:patient/md:patient-number/text()", 
				Xacml20XPathTo30Transformer.transform20PathTo30("//Request/Resource/ResourceContent/md:record/md:patient/md:patient-number/text()"));
		
		assertEquals("//md:record/md:patient/md:patient-number/text()", 
				Xacml20XPathTo30Transformer.transform20PathTo30("//Resource/ResourceContent/md:record/md:patient/md:patient-number/text()"));
		
		assertEquals("//md:record/md:patient/md:patient-number/text()", 
				Xacml20XPathTo30Transformer.transform20PathTo30("//xacml-context:Request/xacml-context:Resource/xacml-context:ResourceContent/md:record/md:patient/md:patient-number/text()"));
		assertEquals("/md:record/md:patient/md:patient-number/text()", 
				Xacml20XPathTo30Transformer.transform20PathTo30("/xacml-context:Request/xacml-context:Resource/xacml-context:ResourceContent/md:record/md:patient/md:patient-number/text()"));
		assertEquals("//md:record/md:patient/md:patient-number/text()", 
				Xacml20XPathTo30Transformer.transform20PathTo30("//xacml-context:Resource/xacml-context:ResourceContent/md:record/md:patient/md:patient-number/text()"));
		assertEquals("/md:record/md:patient/md:patient-number/text()", 
				Xacml20XPathTo30Transformer.transform20PathTo30("/xacml-context:Resource/xacml-context:ResourceContent/md:record/md:patient/md:patient-number/text()"));
		assertEquals("//md:record/md:patient/md:patient-number/text()", 
				Xacml20XPathTo30Transformer.transform20PathTo30("//xacml-context:ResourceContent/md:record/md:patient/md:patient-number/text()"));
		assertEquals("/md:record/md:patient/md:patient-number/text()", 
				Xacml20XPathTo30Transformer.transform20PathTo30("/xacml-context:ResourceContent/md:record/md:patient/md:patient-number/text()"));
		
		assertEquals("//md:record/md:patient/md:patient-number/text()", 
				Xacml20XPathTo30Transformer.transform20PathTo30("//md:record/md:patient/md:patient-number/text()"));
		assertEquals("/md:record/md:patient/md:patient-number/text()", 
				Xacml20XPathTo30Transformer.transform20PathTo30("/md:record/md:patient/md:patient-number/text()"));
		
		assertEquals("./md:record/md:patient/md:patient-number/text()", 
				Xacml20XPathTo30Transformer.transform20PathTo30("./xacml-context:Resource/xacml-context:ResourceContent/md:record/md:patient/md:patient-number/text()"));
		
		assertEquals("./md:record//md:name", 
				Xacml20XPathTo30Transformer.transform20PathTo30("./xacml-context:Resource/xacml-context:ResourceContent/md:record//md:name"));
	}
}
