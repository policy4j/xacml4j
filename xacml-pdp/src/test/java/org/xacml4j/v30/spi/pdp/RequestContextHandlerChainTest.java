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

import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;


public class RequestContextHandlerChainTest
{

	@BeforeEach
	public void init(){
	}

	@Test
	public void testCreateCompositeHandlerWith1Handler()
	{
		RequestContextHandler m0 = Mockito.mock(RequestContextHandler.class);
		RequestContextHandlerChain h = new RequestContextHandlerChain(m0);
		RequestContextHandler m1 = Mockito.mock(RequestContextHandler.class);
		h.setNext(m1);
		org.mockito.InOrder order = Mockito.inOrder(m0);
		order.verify(m0).setNext(m1);
		Mockito.verifyNoMoreInteractions(m0);
	}

	@Test
	public void testCreateCompositeHandlerWith4Handlers()
	{
		RequestContextHandler m0 = Mockito.mock(RequestContextHandler.class);
		RequestContextHandler m1 = Mockito.mock(RequestContextHandler.class);
		RequestContextHandler m2 = Mockito.mock(RequestContextHandler.class);
		RequestContextHandler m3 = Mockito.mock(RequestContextHandler.class);
		RequestContextHandler h = new RequestContextHandlerChain(m0, m1, m2, m3);
		RequestContextHandler m4 = mock(RequestContextHandler.class);
		h.setNext(m4, false);
		h.setNext(m4);
		org.mockito.InOrder order = Mockito.inOrder(m0, m1, m2, m3);
		order.verify(m0).setNext(m1);
		order.verify(m1).setNext(m2);
		order.verify(m2).setNext(m3);
		order.verify(m3, Mockito.times(2)).setNext(m4);
		Mockito.verifyNoMoreInteractions(m0, m1, m2, m3);
		Mockito.verifyNoInteractions(m4);
	}
}
