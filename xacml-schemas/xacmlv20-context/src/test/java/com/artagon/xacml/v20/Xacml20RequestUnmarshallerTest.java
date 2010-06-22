package com.artagon.xacml.v20;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

import com.artagon.xacml.v3.Attribute;
import com.artagon.xacml.v3.AttributeCategoryId;
import com.artagon.xacml.v3.Attributes;
import com.artagon.xacml.v3.Request;
import com.artagon.xacml.v3.marshall.RequestUnmarshaller;
import com.artagon.xacml.v3.types.XPathExpressionType;
import com.artagon.xacml.v3.types.XacmlDataTypes;
import com.google.common.collect.Iterables;


public class Xacml20RequestUnmarshallerTest 
{
	private RequestUnmarshaller unmarshaller;
		
	@Before
	public void init() throws Exception
	{
		this.unmarshaller = new Xacml20RequestUnmarshaller();
	}
		
	@Test
	public void testRequestIIIF005Mapping() throws Exception
	{
		Request request = unmarshaller.unmarshalRequest("IIIF005Request.xml");
		assertNotNull(request);
		Attributes subject = request.getOnlyAttributes(AttributeCategoryId.SUBJECT_ACCESS);
		assertNotNull(subject);
	
		Attribute subjectId = Iterables.getOnlyElement(subject.getAttributes("urn:oasis:names:tc:xacml:1.0:subject:subject-id"));
		Attribute subjectSomeAttribute = Iterables.getOnlyElement(subject.getAttributes("urn:oasis:names:tc:xacml:2.0:conformance-test:some-attribute"));
		assertNotNull(subjectId);
		assertNotNull(subjectSomeAttribute);
		assertEquals(XacmlDataTypes.STRING.create("Julius Hibbert"), Iterables.getOnlyElement(subjectId.getValues()));
		assertEquals(XacmlDataTypes.STRING.create("riddle me this"), Iterables.getOnlyElement(subjectSomeAttribute.getValues()));
		
		Attributes resource = request.getOnlyAttributes(AttributeCategoryId.RESOURCE);
		assertNotNull(resource);
		assertNotNull(resource.getContent());
		assertEquals("record", ((Document)resource.getContent()).getDocumentElement().getLocalName());
		assertEquals("http://www.medico.com/schemas/record", ((Document)resource.getContent()).getDocumentElement().getNamespaceURI());
		
		Attribute resourceId = Iterables.getOnlyElement(resource.getAttributes("urn:oasis:names:tc:xacml:1.0:resource:resource-id"));
		assertNotNull(resourceId);
		assertEquals(XacmlDataTypes.ANYURI.create("http://medico.com/record/patient/BartSimpson"), Iterables.getOnlyElement(resourceId.getValues()));
		
		
		Attributes action = request.getOnlyAttributes(AttributeCategoryId.ACTION);
		assertNotNull(action);
		Attribute actionId = Iterables.getOnlyElement(action.getAttributes("urn:oasis:names:tc:xacml:1.0:action:action-id"));
		assertNotNull(actionId);
		assertEquals(XacmlDataTypes.STRING.create("read"), Iterables.getOnlyElement(actionId.getValues()));	
	}
	
	@Test
	public void testRequestIIA13Mapping() throws Exception
	{
		Request request = unmarshaller.unmarshalRequest("IIA013Request.xml");
		assertNotNull(request);	
		assertEquals(2, request.getAttributeValues(AttributeCategoryId.SUBJECT_ACCESS, 
				"urn:oasis:names:tc:xacml:2.0:conformance-test:age", null, XacmlDataTypes.INTEGER.getType()).size());
	}
	
	@Test
	public void testRequestIIB028Mapping() throws Exception
	{
		Request request = unmarshaller.unmarshalRequest("IIB028Request.xml");
		assertNotNull(request);
		Attributes subjectAccess = request.getOnlyAttributes(AttributeCategoryId.SUBJECT_ACCESS);
		assertNotNull(subjectAccess);
	
		Attribute subjectId = Iterables.getOnlyElement(subjectAccess.getAttributes("urn:oasis:names:tc:xacml:1.0:subject:subject-id"));
		assertNotNull(subjectId);
		assertEquals(XacmlDataTypes.STRING.create("Julius Hibbert"), Iterables.getOnlyElement(subjectId.getValues()));
		
		Attributes subjectRecepient = request.getOnlyAttributes(AttributeCategoryId.SUBJECT_RECIPIENT);
		assertNotNull(subjectRecepient);
		
		subjectId = Iterables.getOnlyElement(subjectRecepient.getAttributes("urn:oasis:names:tc:xacml:1.0:subject:subject-id"));
		assertNotNull(subjectId);
		assertEquals(XacmlDataTypes.STRING.create("Bart Simpson"), Iterables.getOnlyElement(subjectId.getValues()));
		
		Attributes subjectCodebase = request.getOnlyAttributes(AttributeCategoryId.SUBJECT_CODEBASE);
		assertNotNull(subjectCodebase);
		
		subjectId = Iterables.getOnlyElement(subjectCodebase.getAttributes("urn:oasis:names:tc:xacml:1.0:subject:subject-id"));
		assertNotNull(subjectId);
		assertEquals(XacmlDataTypes.ANYURI.create("http://www.medico.com/applications/PatientRecordAccess"), Iterables.getOnlyElement(subjectId.getValues()));
		
		Attributes resource = request.getOnlyAttributes(AttributeCategoryId.RESOURCE);
		assertNotNull(resource);
		
		Attribute resourceId = Iterables.getOnlyElement(resource.getAttributes("urn:oasis:names:tc:xacml:1.0:resource:resource-id"));
		
		assertNotNull(resourceId);
		assertEquals(XacmlDataTypes.ANYURI.create("http://medico.com/record/patient/BartSimpson"), Iterables.getOnlyElement(resourceId.getValues()));
		
		
		Attributes action = request.getOnlyAttributes(AttributeCategoryId.ACTION);
		assertNotNull(action);
		Attribute actionId = Iterables.getOnlyElement(action.getAttributes("urn:oasis:names:tc:xacml:1.0:action:action-id"));
		assertNotNull(actionId);
		assertEquals(XacmlDataTypes.STRING.create("read"), Iterables.getOnlyElement(actionId.getValues()));	
	}
	
	@Test
	public void testRequestIIB030Mapping() throws Exception
	{
		Request request = unmarshaller.unmarshalRequest("IIB030Request.xml");	
		assertNotNull(request);
		Attributes subjectAccess = request.getOnlyAttributes(AttributeCategoryId.SUBJECT_ACCESS);
		assertNotNull(subjectAccess);
	
		Attribute subjectId = Iterables.getOnlyElement(subjectAccess.getAttributes("urn:oasis:names:tc:xacml:1.0:subject:subject-id"));
		assertNotNull(subjectId);
		assertEquals(XacmlDataTypes.STRING.create("Julius Hibbert"), Iterables.getOnlyElement(subjectId.getValues()));
				
		Attributes resource = request.getOnlyAttributes(AttributeCategoryId.RESOURCE);
		assertNotNull(resource);
		
		Attribute resourceId = Iterables.getOnlyElement(resource.getAttributes("urn:oasis:names:tc:xacml:1.0:resource:resource-id"));
		assertNotNull(resourceId);
		assertEquals(XacmlDataTypes.ANYURI.create("A:BartSimpson"), Iterables.getOnlyElement(resourceId.getValues()));
		
		Attribute simpleFileName = Iterables.getOnlyElement(resource.getAttributes("urn:oasis:names:tc:xacml:1.0:resource:simple-file-name"));
		assertNotNull(simpleFileName);
		assertEquals(XacmlDataTypes.STRING.create("BartSimpson"), Iterables.getOnlyElement(simpleFileName.getValues()));
		
		
		Attributes action = request.getOnlyAttributes(AttributeCategoryId.ACTION);
		assertNotNull(action);
		Attribute actionId = Iterables.getOnlyElement(action.getAttributes("urn:oasis:names:tc:xacml:1.0:action:action-id"));
		assertNotNull(actionId);
		assertEquals(XacmlDataTypes.STRING.create("read"), Iterables.getOnlyElement(actionId.getValues()));	
	}
	
	@Test
	public void testRequest001MultipleResourcesMapping() throws Exception
	{
		Request request = unmarshaller.unmarshalRequest("001A-Request.xml");
		assertNotNull(request);
		
		Attributes resource = request.getOnlyAttributes(AttributeCategoryId.RESOURCE);
		assertNotNull(resource);
		
		Attribute resourceId = Iterables.getOnlyElement(resource.getAttributes("urn:oasis:names:tc:xacml:1.0:resource:resource-id"));
		assertNotNull(resourceId);
		XPathExpressionType.XPathExpressionValue xpath = (XPathExpressionType.XPathExpressionValue)Iterables.getOnlyElement(resourceId.getValues());
		assertEquals(AttributeCategoryId.RESOURCE, xpath.getAttributeCategory());
		assertEquals("//md:record/md:patient", xpath.getValue());
	}
	
}

