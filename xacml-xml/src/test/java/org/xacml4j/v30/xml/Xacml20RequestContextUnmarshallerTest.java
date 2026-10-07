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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.xacml4j.v30.types.XacmlTypes.STRING;

import java.util.Collection;
import java.util.Iterator;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xacml4j.v30.Attribute;
import org.xacml4j.v30.Category;
import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.Decision;
import org.xacml4j.v30.ResponseContext;
import org.xacml4j.v30.Result;
import org.xacml4j.v30.SyntaxException;
import org.xacml4j.v30.StatusCodeId;
import org.xacml4j.v30.types.Entity;
import org.xacml4j.v30.marshal.Unmarshaller;
import org.xacml4j.v30.RequestContext;
import org.xacml4j.v30.types.Path;
import org.xacml4j.v30.types.XacmlTypes;

import com.google.common.collect.Iterables;


public class Xacml20RequestContextUnmarshallerTest
{
	private Unmarshaller<RequestContext> unmarshaller;

	@BeforeEach
	public void init() throws Exception
	{
		this.unmarshaller = new Xacml20RequestContextUnmarshaller();
	}

	@Test
	public void missingAttributeIdIsReportedAsSyntaxError() throws Exception
	{
		String request = "<Request xmlns=\"urn:oasis:names:tc:xacml:2.0:context:schema:os\">"
				+ "<Action><Attribute DataType=\"http://www.w3.org/2001/XMLSchema#string\">"
				+ "<AttributeValue>read</AttributeValue></Attribute></Action></Request>";
		SyntaxException error = assertThrows(SyntaxException.class,
				() -> unmarshaller.unmarshal(new java.io.ByteArrayInputStream(
						request.getBytes(java.nio.charset.StandardCharsets.UTF_8))));
		assertEquals(StatusCodeId.SYNTAX_ERROR, error.getStatus().getStatusCode().getValue());
	}

	@Test
	public void testRequestIIIF005Mapping() throws Exception
	{
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		RequestContext request = unmarshaller.unmarshal(cl.getResourceAsStream("IIIF005Request.xml"));
		
		
		assertNotNull(request);
		
		Entity subject = request.getEntity(CategoryId.SUBJECT_ACCESS).get();
		assertNotNull(subject);

		Attribute subjectId = Iterables.getOnlyElement(subject.find("urn:oasis:names:tc:xacml:1.0:subject:subject-id"));
		Attribute subjectSomeAttribute = Iterables.getOnlyElement(subject.find("urn:oasis:names:tc:xacml:2.0:conformance-test:some-attribute"));
		assertNotNull(subjectId);
		assertNotNull(subjectSomeAttribute);
		assertEquals(STRING.ofAny("Julius Hibbert"), Iterables.getOnlyElement(subjectId.values()));
		assertEquals(STRING.ofAny("riddle me this"), Iterables.getOnlyElement(subjectSomeAttribute.values()));

		Entity resource = request.getEntity(CategoryId.RESOURCE).get();
		assertNotNull(resource);
		assertNotNull(resource.getContent());

		Attribute resourceId = Iterables.getOnlyElement(resource.find("urn:oasis:names:tc:xacml:1.0:resource:resource-id"));
		assertNotNull(resourceId);
		assertEquals(XacmlTypes.ANYURI.ofAny("http://medico.com/record/patient/BartSimpson"), Iterables.getOnlyElement(resourceId.values()));


		Entity action = request.getEntity(CategoryId.ACTION).get();
		assertNotNull(action);
		Attribute actionId = Iterables.getOnlyElement(action.find("urn:oasis:names:tc:xacml:1.0:action:action-id"));
		assertNotNull(actionId);
		assertEquals(STRING.ofAny("read"), Iterables.getOnlyElement(actionId.values()));
	}

	@Test
	public void testRequestIIA13Mapping() throws Exception
	{
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		RequestContext request = unmarshaller.unmarshal(cl.getResourceAsStream("IIA013Request.xml"));
		assertNotNull(request);
		assertEquals(2, request.getAttributeValues(CategoryId.SUBJECT_ACCESS,
				"urn:oasis:names:tc:xacml:2.0:conformance-test:age", XacmlTypes.INTEGER, null).size());
		Attribute resourceId = Iterables.getOnlyElement(request.getEntity(CategoryId.RESOURCE).get()
				.find("urn:oasis:names:tc:xacml:1.0:resource:resource-id"));
		assertFalse(resourceId.isIncludeInResult());
	}

	@Test
	public void testRequestIIB028Mapping() throws Exception
	{
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		RequestContext request = unmarshaller.unmarshal(cl.getResourceAsStream("IIB028Request.xml"));
		assertNotNull(request);
		Entity subjectAccess = request.getEntity(CategoryId.SUBJECT_ACCESS).get();
		assertNotNull(subjectAccess);

		Attribute subjectId = Iterables.getOnlyElement(subjectAccess.find("urn:oasis:names:tc:xacml:1.0:subject:subject-id"));
		assertNotNull(subjectId);
		assertEquals(XacmlTypes.STRING.ofAny("Julius Hibbert"), Iterables.getOnlyElement(subjectId.values()));

		Entity subjectRecepient = request.getEntity(CategoryId.SUBJECT_RECIPIENT).get();
		assertNotNull(subjectRecepient);

		subjectId = Iterables.getOnlyElement(subjectRecepient.find("urn:oasis:names:tc:xacml:1.0:subject:subject-id"));
		assertNotNull(subjectId);
		assertEquals(XacmlTypes.STRING.ofAny("Bart Simpson"), Iterables.getOnlyElement(subjectId.values()));

		Entity subjectCodebase = request.getEntity(CategoryId.SUBJECT_CODEBASE).get();
		assertNotNull(subjectCodebase);

		subjectId = Iterables.getOnlyElement(subjectCodebase.find("urn:oasis:names:tc:xacml:1.0:subject:subject-id"));
		assertNotNull(subjectId);
		assertEquals(XacmlTypes.ANYURI.ofAny("http://www.medico.com/applications/PatientRecordAccess"), Iterables.getOnlyElement(subjectId.values()));

		Entity resource = request.getEntity(CategoryId.RESOURCE).get();
		assertNotNull(resource);

		Attribute resourceId = Iterables.getOnlyElement(resource.find("urn:oasis:names:tc:xacml:1.0:resource:resource-id"));

		assertNotNull(resourceId);
		assertEquals(XacmlTypes.ANYURI.ofAny("http://medico.com/record/patient/BartSimpson"), Iterables.getOnlyElement(resourceId.values()));


		Entity action = request.getEntity(CategoryId.ACTION).get();
		assertNotNull(action);
		Attribute actionId = Iterables.getOnlyElement(action.find("urn:oasis:names:tc:xacml:1.0:action:action-id"));
		assertNotNull(actionId);
		assertEquals(XacmlTypes.STRING.ofAny("read"), Iterables.getOnlyElement(actionId.values()));
	}

	@Test
	public void testMultiResourceRequestMapping() throws Exception
	{
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		RequestContext request = unmarshaller.unmarshal(cl.getResourceAsStream("MultiResource-Request.xml"));
		assertNotNull(request);
		Entity subjectAccess = request.getEntity(CategoryId.SUBJECT_ACCESS).get();
		assertNotNull(subjectAccess);

		Attribute subjectId = Iterables.getOnlyElement(subjectAccess.find("urn:oasis:names:tc:xacml:1.0:subject:subject-id"));
		assertNotNull(subjectId);
		assertEquals(STRING.ofAny("Julius Hibbert"), Iterables.getOnlyElement(subjectId.values()));

		Entity subjectRecepient = request.getEntity(CategoryId.SUBJECT_RECIPIENT).get();
		assertNotNull(subjectRecepient);

		subjectId = Iterables.getOnlyElement(subjectRecepient.find("urn:oasis:names:tc:xacml:1.0:subject:subject-id"));
		assertNotNull(subjectId);
		assertEquals(STRING.ofAny("Bart Simpson"), Iterables.getOnlyElement(subjectId.values()));

		Entity subjectCodebase = request.getEntity(CategoryId.SUBJECT_CODEBASE).get();
		assertNotNull(subjectCodebase);

		subjectId = Iterables.getOnlyElement(subjectCodebase.find("urn:oasis:names:tc:xacml:1.0:subject:subject-id"));
		assertNotNull(subjectId);
		assertEquals(XacmlTypes.ANYURI.ofAny("http://www.medico.com/applications/PatientRecordAccess"), Iterables.getOnlyElement(subjectId.values()));

		Collection<Category> resource = request.getCategory(CategoryId.RESOURCE);
		assertNotNull(resource);
		Iterator<Category> it = resource.iterator();

		Entity resource0 = it.next().getEntity();
		Attribute resourceId0 = Iterables.getOnlyElement(resource0.find("urn:oasis:names:tc:xacml:1.0:resource:resource-id"));
		assertNotNull(resourceId0);
		assertTrue(resourceId0.isIncludeInResult());
		assertEquals(XacmlTypes.ANYURI.ofAny("http://medico.com/record/patient/BartSimpson"), Iterables.getOnlyElement(resourceId0.values()));

		Entity resource1 = it.next().getEntity();
		Attribute resourceId1 = Iterables.getOnlyElement(resource1.find("urn:oasis:names:tc:xacml:1.0:resource:resource-id"));
		assertNotNull(resourceId1);
		assertTrue(resourceId1.isIncludeInResult());
		assertEquals(XacmlTypes.ANYURI.ofAny("http://medico.com/record/patient/AnotherResource"), Iterables.getOnlyElement(resourceId1.values()));



		Entity action = request.getEntity(CategoryId.ACTION).get();
		assertNotNull(action);
		Attribute actionId = Iterables.getOnlyElement(action.find("urn:oasis:names:tc:xacml:1.0:action:action-id"));
		assertNotNull(actionId);
		assertEquals(STRING.ofAny("read"), Iterables.getOnlyElement(actionId.values()));
	}

	@Test
	public void testRequestIIB030Mapping() throws Exception
	{
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		RequestContext request = unmarshaller.unmarshal(cl.getResourceAsStream("IIB030Request.xml"));
		assertNotNull(request);
		Entity subjectAccess = request.getEntity(CategoryId.SUBJECT_ACCESS).get();
		assertNotNull(subjectAccess);

		Attribute subjectId = Iterables.getOnlyElement(subjectAccess.find("urn:oasis:names:tc:xacml:1.0:subject:subject-id"));
		assertNotNull(subjectId);
		assertEquals(STRING.ofAny("Julius Hibbert"), Iterables.getOnlyElement(subjectId.values()));

		Entity resource = request.getEntity(CategoryId.RESOURCE).get();
		assertNotNull(resource);

		Attribute resourceId = Iterables.getOnlyElement(resource.find("urn:oasis:names:tc:xacml:1.0:resource:resource-id"));
		assertNotNull(resourceId);
		assertEquals(XacmlTypes.ANYURI.ofAny("A:BartSimpson"), Iterables.getOnlyElement(resourceId.values()));

		Attribute simpleFileName = Iterables.getOnlyElement(resource.find("urn:oasis:names:tc:xacml:1.0:resource:simple-file-name"));
		assertNotNull(simpleFileName);
		assertEquals(STRING.ofAny("BartSimpson"), Iterables.getOnlyElement(simpleFileName.values()));


		Entity action = request.getEntity(CategoryId.ACTION).get();
		assertNotNull(action);
		Attribute actionId = Iterables.getOnlyElement(action.find("urn:oasis:names:tc:xacml:1.0:action:action-id"));
		assertNotNull(actionId);
		assertEquals(STRING.ofAny("read"), Iterables.getOnlyElement(actionId.values()));
	}

	@Test
	public void testRequest001MultipleResourcesMapping() throws Exception
	{
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		RequestContext request = unmarshaller.unmarshal(cl.getResourceAsStream("001A-Request.xml"));
		assertNotNull(request);

		Entity resource = request.getEntity(CategoryId.RESOURCE).get();
		assertNotNull(resource);

		Attribute resourceId = Iterables.getOnlyElement(resource.find("urn:oasis:names:tc:xacml:1.0:resource:resource-id"));
		assertNotNull(resourceId);
		Path xpath = (Path)Iterables.getOnlyElement(resourceId.values());
		assertEquals(XacmlTypes.XPATH, xpath.getEvaluatesTo());
		assertEquals(CategoryId.RESOURCE, xpath.getCategory().get());
		assertEquals("//md:record/md:patient", xpath.get().getPath());
	}

	@Test
	public void testXacml20ResourceScopeIncludesResourceIdInResult() throws Exception
	{
		String request = "<Request xmlns=\"urn:oasis:names:tc:xacml:2.0:context:schema:os\">"
				+ "<Resource>"
				+ "<Attribute AttributeId=\"urn:oasis:names:tc:xacml:2.0:resource:scope\" "
				+ "DataType=\"http://www.w3.org/2001/XMLSchema#string\">"
				+ "<AttributeValue>Children</AttributeValue></Attribute>"
				+ "<Attribute AttributeId=\"urn:oasis:names:tc:xacml:1.0:resource:resource-id\" "
				+ "DataType=\"http://www.w3.org/2001/XMLSchema#anyURI\">"
				+ "<AttributeValue>urn:root</AttributeValue></Attribute>"
				+ "</Resource></Request>";
		RequestContext requestContext = unmarshaller.unmarshal(
				new ByteArrayInputStream(request.getBytes(StandardCharsets.UTF_8)));

		Attribute includedResourceId = Iterables.getOnlyElement(requestContext
				.getEntity(CategoryId.RESOURCE).orElseThrow()
				.find("urn:oasis:names:tc:xacml:1.0:resource:resource-id"));
		assertTrue(includedResourceId.isIncludeInResult());
		assertEquals(XacmlTypes.ANYURI.ofAny("urn:root"), Iterables.getOnlyElement(includedResourceId.values()));

		Result result = Result.ok(Decision.PERMIT)
				.includeInResultAttr(requestContext.getIncludeInResultAttributes())
				.build();
		StringWriter responseXml = new StringWriter();
		new Xacml20ResponseContextMarshaller().marshal(
				ResponseContext.builder().result(result).build(), responseXml);
		assertTrue(responseXml.toString().contains("ResourceId=\"urn:root\""));
	}

}
