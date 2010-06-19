package com.artagon.xacml.util;

import static com.artagon.xacml.util.Xacml20XPathTo30Transformer.fromXacml20String;
import static com.artagon.xacml.util.Xacml20XPathTo30Transformer.transform20PathTo30;
import static org.junit.Assert.assertEquals;

import org.junit.Ignore;
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
		XPathExpressionValue xpathExp = fromXacml20String(xpath);
		assertEquals(XacmlDataTypes.XPATHEXPRESSION.create("//md:record/md:patient/md:patient-number/text()", AttributeCategoryId.RESOURCE), xpathExp);
	}
	
	@Ignore
	@Test
	public void testXPathTransformationWithFunctions()
	{
		assertEquals("//md:record/md:patient/md:patient-number/text()", 
				transform20PathTo30("//Request/Resource/ResourceContent/md:record/md:patient/md:patient-number/text()"));
		assertEquals("//*[local-name()='record'][namespace-uri()='urn:example:med:schemas:record']",
		transform20PathTo30("//*[local-name()='ResourceContent'][namespace-uri()='urn:oasis:names:tc:xacml:2.0:context:schema:os']/[local-name()='record'][namespace-uri()='urn:example:med:schemas:record']"));
	}
	
	@Test
	public void testXpathTransformation() throws Exception
	{
		assertEquals("//md:record/md:patient/md:patient-number/text()", 
				transform20PathTo30("//Request/Resource/ResourceContent/md:record/md:patient/md:patient-number/text()"));
		
		assertEquals("//md:record/md:patient/md:patient-number/text()", 
				transform20PathTo30("//Resource/ResourceContent/md:record/md:patient/md:patient-number/text()"));
		
		assertEquals("//md:record/md:patient/md:patient-number/text()", 
				transform20PathTo30("//xacml-context:Request/xacml-context:Resource/xacml-context:ResourceContent/md:record/md:patient/md:patient-number/text()"));
		assertEquals("/md:record/md:patient/md:patient-number/text()", 
				transform20PathTo30("/xacml-context:Request/xacml-context:Resource/xacml-context:ResourceContent/md:record/md:patient/md:patient-number/text()"));
		assertEquals("//md:record/md:patient/md:patient-number/text()", 
				transform20PathTo30("//xacml-context:Resource/xacml-context:ResourceContent/md:record/md:patient/md:patient-number/text()"));
		assertEquals("/md:record/md:patient/md:patient-number/text()", 
				transform20PathTo30("/xacml-context:Resource/xacml-context:ResourceContent/md:record/md:patient/md:patient-number/text()"));
		assertEquals("//md:record/md:patient/md:patient-number/text()", 
				transform20PathTo30("//xacml-context:ResourceContent/md:record/md:patient/md:patient-number/text()"));
		assertEquals("/md:record/md:patient/md:patient-number/text()", 
				transform20PathTo30("/xacml-context:ResourceContent/md:record/md:patient/md:patient-number/text()"));
		
		assertEquals("//md:record/md:patient/md:patient-number/text()", 
				transform20PathTo30("//md:record/md:patient/md:patient-number/text()"));
		assertEquals("/md:record/md:patient/md:patient-number/text()", 
				transform20PathTo30("/md:record/md:patient/md:patient-number/text()"));
		
		assertEquals("./md:record/md:patient/md:patient-number/text()", 
				transform20PathTo30("./xacml-context:Resource/xacml-context:ResourceContent/md:record/md:patient/md:patient-number/text()"));
		
		assertEquals("./md:record//md:name", 
				transform20PathTo30("./xacml-context:Resource/xacml-context:ResourceContent/md:record//md:name"));
	}
}
