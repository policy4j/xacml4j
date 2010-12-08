package com.artagon.xacml.v3.pdp;

import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;
import static org.easymock.EasyMock.verify;

import org.junit.Test;



public class RequestContextHandlerChainTest 
{		
	@Test
	public void testCreateCompositeHandlerWith1Handler()
	{
		RequestContextHandler m0 = createStrictMock(RequestContextHandler.class);
		replay(m0);
		RequestContextHandlerChain h = new RequestContextHandlerChain(m0);
		verify(m0);
		reset(m0);
		RequestContextHandler m1 = createStrictMock(RequestContextHandler.class);
		m0.setNext(m1);
		replay(m0, m1);
		h.setNext(m1);
		verify(m0, m1);
	}
	
	@Test
	public void testCreateCompositeHandlerWith4Handlers()
	{
		RequestContextHandler m0 = createStrictMock(RequestContextHandler.class);
		RequestContextHandler m1 = createStrictMock(RequestContextHandler.class);
		RequestContextHandler m2 = createStrictMock(RequestContextHandler.class);
		RequestContextHandler m3 = createStrictMock(RequestContextHandler.class);
		m0.setNext(m1);
		m1.setNext(m2);
		m2.setNext(m3);
		replay(m0, m1, m2, m3);
		RequestContextHandler h = new RequestContextHandlerChain(m0, m1, m2, m3);
		verify(m0, m1, m2);
		reset(m0, m1, m2, m3);
		RequestContextHandler m4 = createStrictMock(RequestContextHandler.class);
		h.setNext(m4);
		replay(m0, m1, m2, m3, m4);
		h.setNext(m4);
		verify(m0, m1, m2, m3, m4);
	}
}
