package org.xacml4j.v30.xml;

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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.Content;
import org.xacml4j.v30.RequestContext;
import org.xacml4j.v30.SyntaxException;
import org.xacml4j.v30.StatusCodeId;
import org.xacml4j.v30.types.Entity;

public class Xacml30RequestContentTest
{
	@Test
	public void missingAttributeIdIsReportedAsSyntaxError() throws Exception
	{
		String request = "<Request xmlns=\"urn:oasis:names:tc:xacml:3.0:core:schema:wd-17\">"
				+ "<Attributes Category=\"urn:oasis:names:tc:xacml:3.0:attribute-category:action\">"
				+ "<Attribute><AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">read</AttributeValue>"
				+ "</Attribute></Attributes></Request>";
		SyntaxException error = assertThrows(SyntaxException.class,
				() -> new Xacml30RequestContextUnmarshaller().unmarshal(
						new ByteArrayInputStream(request.getBytes(StandardCharsets.UTF_8))));
		assertEquals(StatusCodeId.SYNTAX_ERROR, error.getStatus().getStatusCode().getValue());
	}
	@Test
	public void whitespaceAroundElementRetainsXmlAndElementText() throws Exception
	{
		Entity resource = unmarshal("<Content>\n\t  <sample:record>"
				+ "<sample:name> retained </sample:name></sample:record>\n\t</Content>");
		assertTrue(resource.getContent().isPresent());
		Content content = resource.<Content>getContent().get();
		assertEquals(Content.Type.XML_UTF8, content.getType());
		assertEquals(" retained ", content.evaluateToString("//sample:name/text()").get());
	}

	@Test
	public void compactElementRetainsXml() throws Exception
	{
		Entity resource = unmarshal("<Content><sample:record>"
				+ "<sample:name>value</sample:name></sample:record></Content>");
		assertTrue(resource.getContent().isPresent());
		assertEquals("value", resource.<Content>getContent().get()
				.evaluateToString("//sample:name/text()").get());
	}

	@Test
	public void absentContentStaysAbsent() throws Exception
	{
		assertFalse(unmarshal("").getContent().isPresent());
	}

	@Test
	public void whitespaceOnlyContentStaysAbsent() throws Exception
	{
		assertFalse(unmarshal("<Content>\n\t  </Content>").getContent().isPresent());
	}

	@Test
	public void significantJsonTextRetainsExistingFallback() throws Exception
	{
		Entity resource = unmarshal("<Content>\n  {\"name\":\"value\"}\n</Content>");
		assertTrue(resource.getContent().isPresent());
		assertEquals(Content.Type.JSON_UTF8, resource.<Content>getContent().get().getType());
	}

	private Entity unmarshal(String content) throws Exception
	{
		String request = "<Request xmlns=\"urn:oasis:names:tc:xacml:3.0:core:schema:wd-17\""
				+ " xmlns:sample=\"urn:test:record\" ReturnPolicyIdList=\"false\" CombinedDecision=\"false\">"
				+ "<Attributes Category=\"urn:oasis:names:tc:xacml:3.0:attribute-category:resource\">"
				+ content + "</Attributes></Request>";
		RequestContext model = new Xacml30RequestContextUnmarshaller().unmarshal(
				new ByteArrayInputStream(request.getBytes(StandardCharsets.UTF_8)));
		return model.getEntity(CategoryId.RESOURCE).get();
	}
}
