package org.xacml4j.v30.marshall.jaxb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;
import org.xacml4j.v30.Decision;
import org.xacml4j.v30.Obligation;
import org.xacml4j.v30.ResponseContext;
import org.xacml4j.v30.Result;
import org.xacml4j.v30.StatusCodeIds;
import org.xacml4j.v30.marshall.ResponseUnmarshaller;
import org.xacml4j.v30.marshall.jaxb.Xacml20ResponseContextUnmarshaller;
import org.xacml4j.v30.types.StringType;

import com.google.common.collect.Iterables;

public class Xacml20ResponseContextUnmarshallerTest 
{
	private ResponseUnmarshaller unmarshaller;
	
	@Before
	public void init() throws Exception
	{
		this.unmarshaller = new Xacml20ResponseContextUnmarshaller();
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
