package org.xacml4j.v30.spi.pdp;

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
import static org.easymock.EasyMock.createStrictMock;

import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;


public class RequestContextHandlerChainTest
{
	private IMocksControl c;

	@Before
	public void init(){
		this.c = createControl();
	}

	@Test
	public void testCreateCompositeHandlerWith1Handler()
	{
		RequestContextHandler m0 = c.createMock(RequestContextHandler.class);
		c.replay();
		RequestContextHandlerChain h = new RequestContextHandlerChain(m0);
		c.verify();
		c.reset();
		RequestContextHandler m1 = c.createMock(RequestContextHandler.class);
		m0.setNext(m1);
		c.replay();
		h.setNext(m1);
		c.verify();
	}

	@Test
	public void testCreateCompositeHandlerWith4Handlers()
	{
		RequestContextHandler m0 = c.createMock(RequestContextHandler.class);
		RequestContextHandler m1 = c.createMock(RequestContextHandler.class);
		RequestContextHandler m2 = c.createMock(RequestContextHandler.class);
		RequestContextHandler m3 = c.createMock(RequestContextHandler.class);
		m0.setNext(m1);
		m1.setNext(m2);
		m2.setNext(m3);
		c.replay();
		RequestContextHandler h = new RequestContextHandlerChain(m0, m1, m2, m3);
		c.verify();
		c.reset();
		RequestContextHandler m4 = createStrictMock(RequestContextHandler.class);
		h.setNext(m4, false);
		c.replay();
		h.setNext(m4);
		c.verify();
	}
}
