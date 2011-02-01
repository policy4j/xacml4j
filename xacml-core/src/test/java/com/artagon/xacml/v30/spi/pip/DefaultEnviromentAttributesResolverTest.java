package com.artagon.xacml.v30.spi.pip;

import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;

import java.util.Calendar;

import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.v30.types.DateTimeType;
import com.artagon.xacml.v30.types.DateType;
import com.artagon.xacml.v30.types.TimeType;

public class DefaultEnviromentAttributesResolverTest 
{
	private AttributeResolver r;
	private ResolverContext context;
	private IMocksControl c;
	
	@Before
	public void init(){
		this.c = createControl();
		this.r = new DefaultEnviromentAttributeResolver();
		this.context = c.createMock(ResolverContext.class);
	}
	
	@Test
	public void testResolve() throws Exception
	{
		Calendar now = Calendar.getInstance();
		expect(context.getDescriptor()).andReturn(r.getDescriptor());
		expect(context.getCurrentDateTime()).andReturn(now);
		c.replay();
		AttributeSet a = r.resolve(context);
		assertEquals(DateTimeType.DATETIME.bagOf(DateTimeType.DATETIME.create(now)), a.get("urn:oasis:names:tc:xacml:1.0:environment:current-dateTime"));
		assertEquals(DateType.DATE.bagOf(DateType.DATE.create(now)), a.get("urn:oasis:names:tc:xacml:1.0:environment:current-date"));
		assertEquals(TimeType.TIME.bagOf(TimeType.TIME.create(now)), a.get("urn:oasis:names:tc:xacml:1.0:environment:current-time"));
		c.verify();
	}
}
