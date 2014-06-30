package org.xacml4j.v30.spi.pip;

/*
 * #%L
 * Xacml4J Core Engine Implementation
 * %%
 * Copyright (C) 2009 - 2014 Xacml4J.org
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

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
		this.r = new DefaultEnvironmentAttributeResolver();
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
