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

import java.util.Map;
import java.util.function.Function;

import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.xacml4j.v30.BagOfValues;
import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.types.XacmlTypes;


public class BaseAttributeResolverTest
{
	private AttributeResolverDescriptor d;
	private ResolverContext context;
	private IMocksControl c;
	private Function<ResolverContext, Map<String, BagOfValues>> r;

	@Before
	public void init(){
		this.c = createControl();
		this.context = c.createMock(ResolverContext.class);
		this.r =  c.createMock(Function.class);
		this.d = AttributeResolverDescriptor
				.builder("test", "Test", CategoryId.SUBJECT_ACCESS)
				.attribute("testId1", XacmlTypes.STRING)
				.attribute("testId2", XacmlTypes.INTEGER)
				.build(r);
	}

	@Test
	@Ignore
	public void testMBeanInvocationStats() throws Exception
	{
		expect(context.getDescriptor()).andReturn(d);
		c.replay();
		r.apply(context);
		c.verify();
	}
}
