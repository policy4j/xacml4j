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
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.oasis.xacml.v30.jaxb.AttributesType;
import org.oasis.xacml.v30.jaxb.AttributesReferenceType;
import org.oasis.xacml.v30.jaxb.MultiRequestsType;
import org.oasis.xacml.v30.jaxb.RequestReferenceType;
import org.oasis.xacml.v30.jaxb.RequestType;
import org.xacml4j.v30.RequestContext;
import org.xacml4j.v30.SyntaxException;

class Xacml30RequestReferenceMappingTest
{
	@Test
	void keepsStringRequestReferenceCompatibility()
	{
		RequestContext request = mapRequest("legacy-id");

		assertEquals("legacy-id", request.getRequestReferences().iterator().next()
				.getReferencedCategories().iterator().next().getReferenceId());
	}

	@Test
	void resolvesAttributesTypeReferenceUsingXmlId()
	{
		AttributesType attributes = new AttributesType();
		attributes.setId("subject-1");

		RequestContext request = mapRequest(attributes);

		assertEquals("subject-1", request.getRequestReferences().iterator().next()
				.getReferencedCategories().iterator().next().getReferenceId());
	}

	@Test
	void rejectsAttributesTypeReferenceWithoutXmlIdAsSyntaxError()
	{
		assertThrows(SyntaxException.class, () -> mapRequest(new AttributesType()));
	}

	@Test
	void rejectsUnsupportedRequestReferenceValuesAsSyntaxErrors()
	{
		assertThrows(SyntaxException.class, () -> mapRequest(new Object()));
	}

	@Test
	void rejectsDuplicateAttributesXmlIdsThroughRequestUnmarshaller()
	{
		String request = "<Request xmlns=\"urn:oasis:names:tc:xacml:3.0:core:schema:wd-17\">"
				+ "<Attributes xml:id=\"dup\" Category=\"urn:oasis:names:tc:xacml:3.0:attribute-category:resource\"/>"
				+ "<Attributes xml:id=\"dup\" Category=\"urn:oasis:names:tc:xacml:3.0:attribute-category:action\"/>"
				+ "</Request>";

		SyntaxException error = assertThrows(SyntaxException.class,
				() -> new Xacml30RequestContextUnmarshaller().unmarshal(
						new ByteArrayInputStream(request.getBytes(StandardCharsets.UTF_8))));
		assertEquals(org.xacml4j.v30.StatusCodeId.SYNTAX_ERROR,
				error.getStatus().getStatusCode().getValue());
	}

	private RequestContext mapRequest(Object referenceId)
	{
		AttributesReferenceType attributesReference = new AttributesReferenceType();
		attributesReference.setReferenceId(referenceId);
		RequestReferenceType requestReference = new RequestReferenceType();
		requestReference.getAttributesReference().add(attributesReference);
		MultiRequestsType multiRequests = new MultiRequestsType();
		multiRequests.getRequestReference().add(requestReference);
		RequestType request = new RequestType();
		request.setMultiRequests(multiRequests);
		return new Xacml30RequestContextFromJaxbToObjectModelMapper().create(request);
	}
}
