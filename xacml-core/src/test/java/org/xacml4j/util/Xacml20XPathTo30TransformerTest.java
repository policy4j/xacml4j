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
