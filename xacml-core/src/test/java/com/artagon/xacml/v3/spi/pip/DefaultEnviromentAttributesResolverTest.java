package com.artagon.xacml.v3.spi.pip;

import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Calendar;

import org.junit.Before;
import org.junit.Test;

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
		AttributeSet a = r.resolve(context);
		assertNotNull(a);
		assertEquals(3, a.size());
		assertNotNull(a.get("urn:oasis:names:tc:xacml:1.0:environment:current-time"));
		assertNotNull(a.get("urn:oasis:names:tc:xacml:1.0:environment:current-date"));
		assertNotNull(a.get("urn:oasis:names:tc:xacml:1.0:environment:current-dateTime"));
		verify(context);
	}
}
