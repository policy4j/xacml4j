package com.artagon.xacml.v3.pdp;

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
		h.setNext(m4);
		c.replay();
		h.setNext(m4);
		c.verify();
	}
}
