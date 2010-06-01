package com.artagon.xacml.v3.policy.jaxb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.oasis.xacml.v20.context.RequestType;

import com.artagon.xacml.v3.Attribute;
import com.artagon.xacml.v3.AttributeCategoryId;
import com.artagon.xacml.v3.Attributes;
import com.artagon.xacml.v3.Request;
import com.artagon.xacml.v3.impl.DefaultRequestFactory;
import com.artagon.xacml.v3.policy.type.DataTypes;
import com.google.common.collect.Iterables;


public class Xacml20RequestMapperTest 
{
	private static JAXBContext context;
	private Xacml20RequestMapper contextMapper;
	
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
		this.contextMapper = new Xacml20RequestMapper(new DefaultRequestFactory());
	}
	
	
	
	@SuppressWarnings({"unchecked" })
	private static RequestType getRequest(String name) throws Exception
	{
		InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(name);
		assertNotNull(stream);
		JAXBElement<RequestType> request = (JAXBElement<RequestType>)context.createUnmarshaller().unmarshal(stream);
		assertNotNull(request);
		return request.getValue();
	}
	
	@Test
	public void testRequestIIIF005Mapping() throws Exception
	{
		RequestType req = getRequest("oasis-xacml20-compat-test/IIIF005Request.xml");
		Request request = contextMapper.create(req);
		assertNotNull(request);
		Attributes subject = Iterables.getOnlyElement(request.getAttributes(AttributeCategoryId.SUBJECT_ACCESS));
		assertNotNull(subject);
	
		Attribute subjectId = Iterables.getOnlyElement(subject.getAttributes("urn:oasis:names:tc:xacml:1.0:subject:subject-id"));
		Attribute subjectSomeAttribute = Iterables.getOnlyElement(subject.getAttributes("urn:oasis:names:tc:xacml:2.0:conformance-test:some-attribute"));
		assertNotNull(subjectId);
		assertNotNull(subjectSomeAttribute);
		assertEquals(DataTypes.STRING.create("Julius Hibbert"), Iterables.getOnlyElement(subjectId.getValues()));
		assertEquals(DataTypes.STRING.create("riddle me this"), Iterables.getOnlyElement(subjectSomeAttribute.getValues()));
		
		Attributes resource = Iterables.getOnlyElement(request.getAttributes(AttributeCategoryId.RESOURCE));
		assertNotNull(resource);
		assertNotNull(resource.getContent());
		Attribute resourceId = Iterables.getOnlyElement(resource.getAttributes("urn:oasis:names:tc:xacml:1.0:resource:resource-id"));
		assertNotNull(resourceId);
		assertEquals(DataTypes.ANYURI.create("http://medico.com/record/patient/BartSimpson"), Iterables.getOnlyElement(resourceId.getValues()));
		
		
		Attributes action = Iterables.getOnlyElement(request.getAttributes(AttributeCategoryId.ACTION));
		assertNotNull(action);
		Attribute actionId = Iterables.getOnlyElement(action.getAttributes("urn:oasis:names:tc:xacml:1.0:action:action-id"));
		assertNotNull(actionId);
		assertEquals(DataTypes.STRING.create("read"), Iterables.getOnlyElement(actionId.getValues()));	
	}
	
	@Test
	public void testRequestIIB028Mapping() throws Exception
	{
		RequestType req = getRequest("oasis-xacml20-compat-test/IIB028Request.xml");
		Request request = contextMapper.create(req);
		assertNotNull(request);
		Attributes subjectAccess = Iterables.getOnlyElement(request.getAttributes(AttributeCategoryId.SUBJECT_ACCESS));
		assertNotNull(subjectAccess);
	
		Attribute subjectId = Iterables.getOnlyElement(subjectAccess.getAttributes("urn:oasis:names:tc:xacml:1.0:subject:subject-id"));
		assertNotNull(subjectId);
		assertEquals(DataTypes.STRING.create("Julius Hibbert"), Iterables.getOnlyElement(subjectId.getValues()));
		
		Attributes subjectRecepient = Iterables.getOnlyElement(request.getAttributes(AttributeCategoryId.SUBJECT_RECIPIENT));
		assertNotNull(subjectRecepient);
		
		subjectId = Iterables.getOnlyElement(subjectRecepient.getAttributes("urn:oasis:names:tc:xacml:1.0:subject:subject-id"));
		assertNotNull(subjectId);
		assertEquals(DataTypes.STRING.create("Bart Simpson"), Iterables.getOnlyElement(subjectId.getValues()));
		
		Attributes subjectCodebase = Iterables.getOnlyElement(request.getAttributes(AttributeCategoryId.SUBJECT_CODEBASE));
		assertNotNull(subjectCodebase);
		
		subjectId = Iterables.getOnlyElement(subjectCodebase.getAttributes("urn:oasis:names:tc:xacml:1.0:subject:subject-id"));
		assertNotNull(subjectId);
		assertEquals(DataTypes.ANYURI.create("http://www.medico.com/applications/PatientRecordAccess"), Iterables.getOnlyElement(subjectId.getValues()));
		
		Attributes resource = Iterables.getOnlyElement(request.getAttributes(AttributeCategoryId.RESOURCE));
		assertNotNull(resource);
		
		Attribute resourceId = Iterables.getOnlyElement(resource.getAttributes("urn:oasis:names:tc:xacml:1.0:resource:resource-id"));
		assertNotNull(resourceId);
		assertEquals(DataTypes.ANYURI.create("http://medico.com/record/patient/BartSimpson"), Iterables.getOnlyElement(resourceId.getValues()));
		
		
		Attributes action = Iterables.getOnlyElement(request.getAttributes(AttributeCategoryId.ACTION));
		assertNotNull(action);
		Attribute actionId = Iterables.getOnlyElement(action.getAttributes("urn:oasis:names:tc:xacml:1.0:action:action-id"));
		assertNotNull(actionId);
		assertEquals(DataTypes.STRING.create("read"), Iterables.getOnlyElement(actionId.getValues()));	
	}
	
	@Test
	public void testRequestIIB030Mapping() throws Exception
	{
		RequestType req = getRequest("oasis-xacml20-compat-test/IIB030Request.xml");
		Request request = contextMapper.create(req);
		assertNotNull(request);
		Attributes subjectAccess = Iterables.getOnlyElement(request.getAttributes(AttributeCategoryId.SUBJECT_ACCESS));
		assertNotNull(subjectAccess);
	
		Attribute subjectId = Iterables.getOnlyElement(subjectAccess.getAttributes("urn:oasis:names:tc:xacml:1.0:subject:subject-id"));
		assertNotNull(subjectId);
		assertEquals(DataTypes.STRING.create("Julius Hibbert"), Iterables.getOnlyElement(subjectId.getValues()));
				
		Attributes resource = Iterables.getOnlyElement(request.getAttributes(AttributeCategoryId.RESOURCE));
		assertNotNull(resource);
		
		Attribute resourceId = Iterables.getOnlyElement(resource.getAttributes("urn:oasis:names:tc:xacml:1.0:resource:resource-id"));
		assertNotNull(resourceId);
		assertEquals(DataTypes.ANYURI.create("A:BartSimpson"), Iterables.getOnlyElement(resourceId.getValues()));
		
		Attribute simpleFileName = Iterables.getOnlyElement(resource.getAttributes("urn:oasis:names:tc:xacml:1.0:resource:simple-file-name"));
		assertNotNull(simpleFileName);
		assertEquals(DataTypes.STRING.create("BartSimpson"), Iterables.getOnlyElement(simpleFileName.getValues()));
		
		
		Attributes action = Iterables.getOnlyElement(request.getAttributes(AttributeCategoryId.ACTION));
		assertNotNull(action);
		Attribute actionId = Iterables.getOnlyElement(action.getAttributes("urn:oasis:names:tc:xacml:1.0:action:action-id"));
		assertNotNull(actionId);
		assertEquals(DataTypes.STRING.create("read"), Iterables.getOnlyElement(actionId.getValues()));	
	}
	
}

