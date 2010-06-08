package com.artagon.xacml.v20;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.oasis.xacml.v20.context.RequestType;
import org.oasis.xacml.v20.context.ResponseType;
import org.w3c.dom.Element;

import com.artagon.xacml.v20.Xacml20ContextMapper;
import com.artagon.xacml.v3.Attribute;
import com.artagon.xacml.v3.AttributeCategoryId;
import com.artagon.xacml.v3.Attributes;
import com.artagon.xacml.v3.Request;
import com.artagon.xacml.v3.types.XacmlDataTypes;
import com.google.common.collect.Iterables;


public class Xacml20ContextMapperTest 
{
	private static JAXBContext context;
	private Xacml20ContextMapper contextMapper;
	
	@BeforeClass
	public static void init_static() throws Exception
	{
		try{
			context = JAXBContext.newInstance("org.oasis.xacml.v20.context");
		}catch(JAXBException e){
			System.err.println(e.getMessage());
		}
	}
	
	@Before
	public void init() throws Exception
	{
		this.contextMapper = new Xacml20ContextMapper();
	}
	
	
	
	@SuppressWarnings({"unchecked" })
	private static <T>  T getJAXBObject(String name) throws Exception
	{
		InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(name);
		assertNotNull(stream);
		JAXBElement<T> request = (JAXBElement<T>)context.createUnmarshaller().unmarshal(stream);
		assertNotNull(request);
		return request.getValue();
	}
	
	@Test
	public void testResponseIIIF005Mapping() throws Exception
	{
		@SuppressWarnings("unused")
		ResponseType res = getJAXBObject("oasis-xacml20-compat-test/IIIF005Response.xml");
	}
	
	@Test
	public void testRequestIIIF005Mapping() throws Exception
	{
		RequestType req = getJAXBObject("oasis-xacml20-compat-test/IIIF005Request.xml");
		Request request = contextMapper.create(req);
		assertNotNull(request);
		Attributes subject = Iterables.getOnlyElement(request.getAttributes(AttributeCategoryId.SUBJECT_ACCESS));
		assertNotNull(subject);
	
		Attribute subjectId = Iterables.getOnlyElement(subject.getAttributes("urn:oasis:names:tc:xacml:1.0:subject:subject-id"));
		Attribute subjectSomeAttribute = Iterables.getOnlyElement(subject.getAttributes("urn:oasis:names:tc:xacml:2.0:conformance-test:some-attribute"));
		assertNotNull(subjectId);
		assertNotNull(subjectSomeAttribute);
		assertEquals(XacmlDataTypes.STRING.create("Julius Hibbert"), Iterables.getOnlyElement(subjectId.getValues()));
		assertEquals(XacmlDataTypes.STRING.create("riddle me this"), Iterables.getOnlyElement(subjectSomeAttribute.getValues()));
		
		Attributes resource = Iterables.getOnlyElement(request.getAttributes(AttributeCategoryId.RESOURCE));
		assertNotNull(resource);
		assertNotNull(resource.getContent());
		assertEquals("record", ((Element)resource.getContent()).getLocalName());
		assertEquals("http://www.medico.com/schemas/record", ((Element)resource.getContent()).getNamespaceURI());
		
		Attribute resourceId = Iterables.getOnlyElement(resource.getAttributes("urn:oasis:names:tc:xacml:1.0:resource:resource-id"));
		assertNotNull(resourceId);
		assertTrue(resourceId.isIncludeInResult());
		assertEquals(XacmlDataTypes.ANYURI.create("http://medico.com/record/patient/BartSimpson"), Iterables.getOnlyElement(resourceId.getValues()));
		
		
		Attributes action = Iterables.getOnlyElement(request.getAttributes(AttributeCategoryId.ACTION));
		assertNotNull(action);
		Attribute actionId = Iterables.getOnlyElement(action.getAttributes("urn:oasis:names:tc:xacml:1.0:action:action-id"));
		assertNotNull(actionId);
		assertEquals(XacmlDataTypes.STRING.create("read"), Iterables.getOnlyElement(actionId.getValues()));	
	}
	
	@Test
	public void testRequestIIB028Mapping() throws Exception
	{
		RequestType req = getJAXBObject("oasis-xacml20-compat-test/IIB028Request.xml");
		Request request = contextMapper.create(req);
		assertNotNull(request);
		Attributes subjectAccess = Iterables.getOnlyElement(request.getAttributes(AttributeCategoryId.SUBJECT_ACCESS));
		assertNotNull(subjectAccess);
	
		Attribute subjectId = Iterables.getOnlyElement(subjectAccess.getAttributes("urn:oasis:names:tc:xacml:1.0:subject:subject-id"));
		assertNotNull(subjectId);
		assertEquals(XacmlDataTypes.STRING.create("Julius Hibbert"), Iterables.getOnlyElement(subjectId.getValues()));
		
		Attributes subjectRecepient = Iterables.getOnlyElement(request.getAttributes(AttributeCategoryId.SUBJECT_RECIPIENT));
		assertNotNull(subjectRecepient);
		
		subjectId = Iterables.getOnlyElement(subjectRecepient.getAttributes("urn:oasis:names:tc:xacml:1.0:subject:subject-id"));
		assertNotNull(subjectId);
		assertEquals(XacmlDataTypes.STRING.create("Bart Simpson"), Iterables.getOnlyElement(subjectId.getValues()));
		
		Attributes subjectCodebase = Iterables.getOnlyElement(request.getAttributes(AttributeCategoryId.SUBJECT_CODEBASE));
		assertNotNull(subjectCodebase);
		
		subjectId = Iterables.getOnlyElement(subjectCodebase.getAttributes("urn:oasis:names:tc:xacml:1.0:subject:subject-id"));
		assertNotNull(subjectId);
		assertEquals(XacmlDataTypes.ANYURI.create("http://www.medico.com/applications/PatientRecordAccess"), Iterables.getOnlyElement(subjectId.getValues()));
		
		Attributes resource = Iterables.getOnlyElement(request.getAttributes(AttributeCategoryId.RESOURCE));
		assertNotNull(resource);
		
		Attribute resourceId = Iterables.getOnlyElement(resource.getAttributes("urn:oasis:names:tc:xacml:1.0:resource:resource-id"));
		
		assertNotNull(resourceId);
		assertTrue(resourceId.isIncludeInResult());
		assertEquals(XacmlDataTypes.ANYURI.create("http://medico.com/record/patient/BartSimpson"), Iterables.getOnlyElement(resourceId.getValues()));
		
		
		Attributes action = Iterables.getOnlyElement(request.getAttributes(AttributeCategoryId.ACTION));
		assertNotNull(action);
		Attribute actionId = Iterables.getOnlyElement(action.getAttributes("urn:oasis:names:tc:xacml:1.0:action:action-id"));
		assertNotNull(actionId);
		assertEquals(XacmlDataTypes.STRING.create("read"), Iterables.getOnlyElement(actionId.getValues()));	
	}
	
	@Test
	public void testRequestIIB030Mapping() throws Exception
	{
		RequestType req = getJAXBObject("oasis-xacml20-compat-test/IIB030Request.xml");
		Request request = contextMapper.create(req);
		assertNotNull(request);
		Attributes subjectAccess = Iterables.getOnlyElement(request.getAttributes(AttributeCategoryId.SUBJECT_ACCESS));
		assertNotNull(subjectAccess);
	
		Attribute subjectId = Iterables.getOnlyElement(subjectAccess.getAttributes("urn:oasis:names:tc:xacml:1.0:subject:subject-id"));
		assertNotNull(subjectId);
		assertEquals(XacmlDataTypes.STRING.create("Julius Hibbert"), Iterables.getOnlyElement(subjectId.getValues()));
				
		Attributes resource = Iterables.getOnlyElement(request.getAttributes(AttributeCategoryId.RESOURCE));
		assertNotNull(resource);
		
		Attribute resourceId = Iterables.getOnlyElement(resource.getAttributes("urn:oasis:names:tc:xacml:1.0:resource:resource-id"));
		assertNotNull(resourceId);
		assertTrue(resourceId.isIncludeInResult());
		assertEquals(XacmlDataTypes.ANYURI.create("A:BartSimpson"), Iterables.getOnlyElement(resourceId.getValues()));
		
		Attribute simpleFileName = Iterables.getOnlyElement(resource.getAttributes("urn:oasis:names:tc:xacml:1.0:resource:simple-file-name"));
		assertNotNull(simpleFileName);
		assertEquals(XacmlDataTypes.STRING.create("BartSimpson"), Iterables.getOnlyElement(simpleFileName.getValues()));
		
		
		Attributes action = Iterables.getOnlyElement(request.getAttributes(AttributeCategoryId.ACTION));
		assertNotNull(action);
		Attribute actionId = Iterables.getOnlyElement(action.getAttributes("urn:oasis:names:tc:xacml:1.0:action:action-id"));
		assertNotNull(actionId);
		assertEquals(XacmlDataTypes.STRING.create("read"), Iterables.getOnlyElement(actionId.getValues()));	
	}
	
}

