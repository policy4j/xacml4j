package com.artagon.xacml.v3.spi.pip;

import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.v3.BagOfAttributeValues;

public class DefaultEnviromentAttributesResolverTest 
{
	private AttributeResolver r;
	private PolicyInformationPointContext context;
	
	@Before
	public void init(){
		this.r = new DefaultEnviromentAttributeResolver();
		this.context = createStrictMock(PolicyInformationPointContext.class);
	}
	
	@Test
	public void testResolve() throws Exception
	{
		Calendar now = Calendar.getInstance();
		expect(context.getCurrentDateTime()).andReturn(now);
		replay(context);
		Map<String, BagOfAttributeValues> a = r.resolve(context);
		assertNotNull(a);
		assertEquals(3, a.size());
		assertTrue(a.containsKey("urn:oasis:names:tc:xacml:1.0:environment:current-time"));
		assertTrue(a.containsKey("urn:oasis:names:tc:xacml:1.0:environment:current-date"));
		assertTrue(a.containsKey("urn:oasis:names:tc:xacml:1.0:environment:current-dateTime"));
		verify(context);
	}
}
