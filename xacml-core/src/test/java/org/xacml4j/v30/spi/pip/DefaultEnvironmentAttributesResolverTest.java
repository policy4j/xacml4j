package org.xacml4j.v30.spi.pip;

import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Calendar;

import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.xacml4j.v30.types.DateExp;
import org.xacml4j.v30.types.DateTimeExp;
import org.xacml4j.v30.types.TimeExp;

import com.google.common.base.Ticker;

public class DefaultEnvironmentAttributesResolverTest
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
		expect(context.getTicker()).andReturn(Ticker.systemTicker());
		c.replay();
		AttributeSet a = r.resolve(context);
		c.verify();

		assertThat(a.get("urn:oasis:names:tc:xacml:1.0:environment:current-dateTime"), is(DateTimeExp.valueOf(now).toBag()));
		assertThat(a.get("urn:oasis:names:tc:xacml:1.0:environment:current-date"), is(DateExp.valueOf(now).toBag()));
		assertThat(a.get("urn:oasis:names:tc:xacml:1.0:environment:current-time"), is(TimeExp.valueOf(now).toBag()));
	}
}
