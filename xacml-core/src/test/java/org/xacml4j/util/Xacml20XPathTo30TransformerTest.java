package org.xacml4j.util;

import static org.junit.Assert.assertEquals;
import static org.xacml4j.util.Xacml20XPathTo30Transformer.fromXacml20String;
import static org.xacml4j.util.Xacml20XPathTo30Transformer.transform20PathTo30;

import org.junit.Ignore;
import org.junit.Test;
import org.xacml4j.v30.Categories;
import org.xacml4j.v30.types.StringExp;
import org.xacml4j.v30.types.XPathExp;


public class Xacml20XPathTo30TransformerTest
{

	@Test
	public void testXacml20StringXPathToXPathExpression()
	{
		StringExp xpath = StringExp.valueOf("//Request/Resource/ResourceContent/md:record/md:patient/md:patient-number/text()");
		assertEquals(XPathExp.valueOf("//md:record/md:patient/md:patient-number/text()", Categories.RESOURCE), fromXacml20String(xpath));
	}

	// FIXME: Implement transformation
	@Test
	@Ignore
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
