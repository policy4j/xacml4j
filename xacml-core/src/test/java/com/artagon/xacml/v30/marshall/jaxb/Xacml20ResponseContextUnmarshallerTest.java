package com.artagon.xacml.v30.marshall.jaxb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.v30.StatusCodeIds;
import com.artagon.xacml.v30.marshall.ResponseMarshaller;
import com.artagon.xacml.v30.marshall.ResponseUnmarshaller;
import com.artagon.xacml.v30.pdp.Decision;
import com.artagon.xacml.v30.pdp.Obligation;
import com.artagon.xacml.v30.pdp.ResponseContext;
import com.artagon.xacml.v30.pdp.Result;
import com.artagon.xacml.v30.types.StringType;
import com.google.common.collect.Iterables;

public class Xacml20ResponseContextUnmarshallerTest 
{
	private ResponseUnmarshaller unmarshaller;
	private ResponseMarshaller marshaller;
	
	@Before
	public void init() throws Exception
	{
		this.unmarshaller = new Xacml20ResponseContextUnmarshaller();
		this.marshaller = new Xacml20ResponseContextMarshaller();
	}
	
	@Test
	public void testResponseTest1Mapping() throws Exception
	{
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		ResponseContext res = unmarshaller.unmarshal(cl.getResourceAsStream("test-response.xml"));
		Result r = Iterables.getOnlyElement(res.getResults());
		assertEquals(Decision.PERMIT, r.getDecision());
		assertEquals(r.getStatus().getStatusCode().getValue(), StatusCodeIds.OK);
		assertNull(r.getStatus().getMessage());
		assertNull(r.getStatus().getDetail());
		assertNull(r.getStatus().getStatusCode().getMinorStatus());
		Obligation o1 = r.getObligation("urn:oasis:names:tc:xacml:2.0:conformance-test:IIIA001:obligation-1");
		assertNotNull(o1);
		assertEquals(StringType.STRING.create("assignment1"),  
				Iterables.getOnlyElement(
						o1.getAttribute(
								"urn:oasis:names:tc:xacml:2.0:conformance-test:IIIA001:assignment1")).getAttribute());
		assertEquals(StringType.STRING.create("assignment2"),  
				Iterables.getOnlyElement(
						o1.getAttribute(
								"urn:oasis:names:tc:xacml:2.0:conformance-test:IIIA001:assignment2")).getAttribute());
		Obligation o2 = r.getObligation("urn:oasis:names:tc:xacml:2.0:conformance-test:IIIA001:obligation-2");
		assertNotNull(o2);
		assertEquals(StringType.STRING.create("assignment1"),  
				Iterables.getOnlyElement(
						o2.getAttribute(
								"urn:oasis:names:tc:xacml:2.0:conformance-test:IIIA001:assignment1")).getAttribute());
		assertEquals(StringType.STRING.create("assignment2"),  
				Iterables.getOnlyElement(
						o2.getAttribute(
								"urn:oasis:names:tc:xacml:2.0:conformance-test:IIIA001:assignment2")).getAttribute());
		
	}
}
