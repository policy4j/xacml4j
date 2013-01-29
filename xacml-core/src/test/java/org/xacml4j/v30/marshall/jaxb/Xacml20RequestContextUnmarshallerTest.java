package org.xacml4j.v30.marshall.jaxb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.xacml4j.v30.types.AnyURIType.ANYURI;
import static org.xacml4j.v30.types.IntegerType.INTEGER;
import static org.xacml4j.v30.types.StringType.STRING;

import java.util.Collection;
import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xacml4j.v30.Attribute;
import org.xacml4j.v30.AttributeCategories;
import org.xacml4j.v30.Attributes;
import org.xacml4j.v30.RequestContext;
import org.xacml4j.v30.marshall.RequestUnmarshaller;
import org.xacml4j.v30.marshall.jaxb.Xacml20RequestContextUnmarshaller;
import org.xacml4j.v30.types.XPathExp;

import com.google.common.collect.Iterables;




public class Xacml20RequestContextUnmarshallerTest 
{
	private RequestUnmarshaller unmarshaller;
		
	@Before
	public void init() throws Exception
	{
		this.unmarshaller = new Xacml20RequestContextUnmarshaller();
	}
		
	@Test
	public void testRequestIIIF005Mapping() throws Exception
	{
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		RequestContext request = unmarshaller.unmarshal(cl.getResourceAsStream("IIIF005Request.xml"));
		assertNotNull(request);
		Attributes subject = request.getOnlyAttributes(AttributeCategories.SUBJECT_ACCESS);
		assertNotNull(subject);
	
		Attribute subjectId = Iterables.getOnlyElement(subject.getAttributes("urn:oasis:names:tc:xacml:1.0:subject:subject-id"));
		Attribute subjectSomeAttribute = Iterables.getOnlyElement(subject.getAttributes("urn:oasis:names:tc:xacml:2.0:conformance-test:some-attribute"));
		assertNotNull(subjectId);
		assertNotNull(subjectSomeAttribute);
		assertEquals(STRING.create("Julius Hibbert"), Iterables.getOnlyElement(subjectId.getValues()));
		assertEquals(STRING.create("riddle me this"), Iterables.getOnlyElement(subjectSomeAttribute.getValues()));
		
		Attributes resource = request.getOnlyAttributes(AttributeCategories.RESOURCE);
		assertNotNull(resource);
		assertNotNull(resource.getContent());
		assertEquals("record", ((Document)resource.getContent()).getDocumentElement().getLocalName());
		assertEquals("http://www.medico.com/schemas/record", ((Document)resource.getContent()).getDocumentElement().getNamespaceURI());
		
		Attribute resourceId = Iterables.getOnlyElement(resource.getAttributes("urn:oasis:names:tc:xacml:1.0:resource:resource-id"));
		assertNotNull(resourceId);
		assertEquals(ANYURI.create("http://medico.com/record/patient/BartSimpson"), Iterables.getOnlyElement(resourceId.getValues()));
		
		
		Attributes action = request.getOnlyAttributes(AttributeCategories.ACTION);
		assertNotNull(action);
		Attribute actionId = Iterables.getOnlyElement(action.getAttributes("urn:oasis:names:tc:xacml:1.0:action:action-id"));
		assertNotNull(actionId);
		assertEquals(STRING.create("read"), Iterables.getOnlyElement(actionId.getValues()));	
	}
	
	@Test
	public void testRequestIIA13Mapping() throws Exception
	{
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		RequestContext request = unmarshaller.unmarshal(cl.getResourceAsStream("IIA013Request.xml"));
		assertNotNull(request);	
		assertEquals(2, request.getAttributeValues(AttributeCategories.SUBJECT_ACCESS, 
				"urn:oasis:names:tc:xacml:2.0:conformance-test:age", INTEGER, null).size());
	}
	
	@Test
	public void testRequestIIB028Mapping() throws Exception
	{
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		RequestContext request = unmarshaller.unmarshal(cl.getResourceAsStream("IIB028Request.xml"));
		assertNotNull(request);
		Attributes subjectAccess = request.getOnlyAttributes(AttributeCategories.SUBJECT_ACCESS);
		assertNotNull(subjectAccess);
	
		Attribute subjectId = Iterables.getOnlyElement(subjectAccess.getAttributes("urn:oasis:names:tc:xacml:1.0:subject:subject-id"));
		assertNotNull(subjectId);
		assertEquals(STRING.create("Julius Hibbert"), Iterables.getOnlyElement(subjectId.getValues()));
		
		Attributes subjectRecepient = request.getOnlyAttributes(AttributeCategories.SUBJECT_RECIPIENT);
		assertNotNull(subjectRecepient);
		
		subjectId = Iterables.getOnlyElement(subjectRecepient.getAttributes("urn:oasis:names:tc:xacml:1.0:subject:subject-id"));
		assertNotNull(subjectId);
		assertEquals(STRING.create("Bart Simpson"), Iterables.getOnlyElement(subjectId.getValues()));
		
		Attributes subjectCodebase = request.getOnlyAttributes(AttributeCategories.SUBJECT_CODEBASE);
		assertNotNull(subjectCodebase);
		
		subjectId = Iterables.getOnlyElement(subjectCodebase.getAttributes("urn:oasis:names:tc:xacml:1.0:subject:subject-id"));
		assertNotNull(subjectId);
		assertEquals(ANYURI.create("http://www.medico.com/applications/PatientRecordAccess"), Iterables.getOnlyElement(subjectId.getValues()));
		
		Attributes resource = request.getOnlyAttributes(AttributeCategories.RESOURCE);
		assertNotNull(resource);
		
		Attribute resourceId = Iterables.getOnlyElement(resource.getAttributes("urn:oasis:names:tc:xacml:1.0:resource:resource-id"));
		
		assertNotNull(resourceId);
		assertEquals(ANYURI.create("http://medico.com/record/patient/BartSimpson"), Iterables.getOnlyElement(resourceId.getValues()));
		
		
		Attributes action = request.getOnlyAttributes(AttributeCategories.ACTION);
		assertNotNull(action);
		Attribute actionId = Iterables.getOnlyElement(action.getAttributes("urn:oasis:names:tc:xacml:1.0:action:action-id"));
		assertNotNull(actionId);
		assertEquals(STRING.create("read"), Iterables.getOnlyElement(actionId.getValues()));	
	}
	
	@Test
	public void testMultiResourceRequestMapping() throws Exception
	{
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		RequestContext request = unmarshaller.unmarshal(cl.getResourceAsStream("MultiResource-Request.xml"));
		assertNotNull(request);
		Attributes subjectAccess = request.getOnlyAttributes(AttributeCategories.SUBJECT_ACCESS);
		assertNotNull(subjectAccess);
	
		Attribute subjectId = Iterables.getOnlyElement(subjectAccess.getAttributes("urn:oasis:names:tc:xacml:1.0:subject:subject-id"));
		assertNotNull(subjectId);
		assertEquals(STRING.create("Julius Hibbert"), Iterables.getOnlyElement(subjectId.getValues()));
		
		Attributes subjectRecepient = request.getOnlyAttributes(AttributeCategories.SUBJECT_RECIPIENT);
		assertNotNull(subjectRecepient);
		
		subjectId = Iterables.getOnlyElement(subjectRecepient.getAttributes("urn:oasis:names:tc:xacml:1.0:subject:subject-id"));
		assertNotNull(subjectId);
		assertEquals(STRING.create("Bart Simpson"), Iterables.getOnlyElement(subjectId.getValues()));
		
		Attributes subjectCodebase = request.getOnlyAttributes(AttributeCategories.SUBJECT_CODEBASE);
		assertNotNull(subjectCodebase);
		
		subjectId = Iterables.getOnlyElement(subjectCodebase.getAttributes("urn:oasis:names:tc:xacml:1.0:subject:subject-id"));
		assertNotNull(subjectId);
		assertEquals(ANYURI.create("http://www.medico.com/applications/PatientRecordAccess"), Iterables.getOnlyElement(subjectId.getValues()));
		
		Collection<Attributes> resource = request.getAttributes(AttributeCategories.RESOURCE);
		assertNotNull(resource);
		Iterator<Attributes> it = resource.iterator();
		
		Attributes resource0 = it.next();
		Attribute resourceId0 = Iterables.getOnlyElement(resource0.getAttributes("urn:oasis:names:tc:xacml:1.0:resource:resource-id"));
		assertNotNull(resourceId0);
		assertTrue(resourceId0.isIncludeInResult());
		assertEquals(ANYURI.create("http://medico.com/record/patient/BartSimpson"), Iterables.getOnlyElement(resourceId0.getValues()));
		
		Attributes resource1 = it.next();
		Attribute resourceId1 = Iterables.getOnlyElement(resource1.getAttributes("urn:oasis:names:tc:xacml:1.0:resource:resource-id"));
		assertNotNull(resourceId1);
		assertTrue(resourceId1.isIncludeInResult());
		assertEquals(ANYURI.create("http://medico.com/record/patient/AnotherResource"), Iterables.getOnlyElement(resourceId1.getValues()));
		
	
		
		Attributes action = request.getOnlyAttributes(AttributeCategories.ACTION);
		assertNotNull(action);
		Attribute actionId = Iterables.getOnlyElement(action.getAttributes("urn:oasis:names:tc:xacml:1.0:action:action-id"));
		assertNotNull(actionId);
		assertEquals(STRING.create("read"), Iterables.getOnlyElement(actionId.getValues()));	
	}
	
	@Test
	public void testRequestIIB030Mapping() throws Exception
	{
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		RequestContext request = unmarshaller.unmarshal(cl.getResourceAsStream("IIB030Request.xml"));	
		assertNotNull(request);
		Attributes subjectAccess = request.getOnlyAttributes(AttributeCategories.SUBJECT_ACCESS);
		assertNotNull(subjectAccess);
	
		Attribute subjectId = Iterables.getOnlyElement(subjectAccess.getAttributes("urn:oasis:names:tc:xacml:1.0:subject:subject-id"));
		assertNotNull(subjectId);
		assertEquals(STRING.create("Julius Hibbert"), Iterables.getOnlyElement(subjectId.getValues()));
				
		Attributes resource = request.getOnlyAttributes(AttributeCategories.RESOURCE);
		assertNotNull(resource);
		
		Attribute resourceId = Iterables.getOnlyElement(resource.getAttributes("urn:oasis:names:tc:xacml:1.0:resource:resource-id"));
		assertNotNull(resourceId);
		assertEquals(ANYURI.create("A:BartSimpson"), Iterables.getOnlyElement(resourceId.getValues()));
		
		Attribute simpleFileName = Iterables.getOnlyElement(resource.getAttributes("urn:oasis:names:tc:xacml:1.0:resource:simple-file-name"));
		assertNotNull(simpleFileName);
		assertEquals(STRING.create("BartSimpson"), Iterables.getOnlyElement(simpleFileName.getValues()));
		
		
		Attributes action = request.getOnlyAttributes(AttributeCategories.ACTION);
		assertNotNull(action);
		Attribute actionId = Iterables.getOnlyElement(action.getAttributes("urn:oasis:names:tc:xacml:1.0:action:action-id"));
		assertNotNull(actionId);
		assertEquals(STRING.create("read"), Iterables.getOnlyElement(actionId.getValues()));	
	}
	
	@Test
	public void testRequest001MultipleResourcesMapping() throws Exception
	{
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		RequestContext request = unmarshaller.unmarshal(cl.getResourceAsStream("001A-Request.xml"));
		assertNotNull(request);
		
		Attributes resource = request.getOnlyAttributes(AttributeCategories.RESOURCE);
		assertNotNull(resource);
		
		Attribute resourceId = Iterables.getOnlyElement(resource.getAttributes("urn:oasis:names:tc:xacml:1.0:resource:resource-id"));
		assertNotNull(resourceId);
		XPathExp xpath = (XPathExp)Iterables.getOnlyElement(resourceId.getValues());
		assertEquals(AttributeCategories.RESOURCE, xpath.getCategory());
		assertEquals("//md:record/md:patient", xpath.getValue().getPath());
	}
	
}

