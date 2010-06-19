package com.artagon.xacml.v3.profiles;

import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;
import static org.easymock.EasyMock.verify;

import org.junit.Test;

public class CompositeRequestProfileTest 
{		
	@Test
	public void testCreateCompositeHandlerWith1Handler()
	{
		RequestProfileHandler m0 = createStrictMock(RequestProfileHandler.class);
		replay(m0);
		RequestProfileHandlerChain h = new RequestProfileHandlerChain(m0);
		verify(m0);
		reset(m0);
		RequestProfileHandler m1 = createStrictMock(RequestProfileHandler.class);
		m0.setNext(m1);
		replay(m0, m1);
		h.setNext(m1);
		verify(m0, m1);
	}
	
	@Test
	public void testCreateCompositeHandlerWith4Handlers()
	{
		RequestProfileHandler m0 = createStrictMock(RequestProfileHandler.class);
		RequestProfileHandler m1 = createStrictMock(RequestProfileHandler.class);
		RequestProfileHandler m2 = createStrictMock(RequestProfileHandler.class);
		RequestProfileHandler m3 = createStrictMock(RequestProfileHandler.class);
		m0.setNext(m1);
		m1.setNext(m2);
		m2.setNext(m3);
		replay(m0, m1, m2, m3);
		RequestProfileHandler h = new RequestProfileHandlerChain(m0, m1, m2, m3);
		verify(m0, m1, m2);
		reset(m0, m1, m2, m3);
		RequestProfileHandler m4 = createStrictMock(RequestProfileHandler.class);
		h.setNext(m4);
		replay(m0, m1, m2, m3, m4);
		h.setNext(m4);
		verify(m0, m1, m2, m3, m4);
	}
}
