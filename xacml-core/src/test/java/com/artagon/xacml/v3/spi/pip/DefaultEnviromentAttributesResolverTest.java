package com.artagon.xacml.v3.spi.pip;

import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

import java.util.Calendar;

import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.v3.types.DateTimeType;
import com.artagon.xacml.v3.types.DateType;
import com.artagon.xacml.v3.types.TimeType;

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
		assertEquals(DateTimeType.DATETIME.bagOf(DateTimeType.DATETIME.create(now)), a.get("urn:oasis:names:tc:xacml:1.0:environment:current-dateTime"));
		assertEquals(DateType.DATE.bagOf(DateType.DATE.create(now)), a.get("urn:oasis:names:tc:xacml:1.0:environment:current-date"));
		assertEquals(TimeType.TIME.bagOf(TimeType.TIME.create(now)), a.get("urn:oasis:names:tc:xacml:1.0:environment:current-time"));
		verify(context);
	}
}
