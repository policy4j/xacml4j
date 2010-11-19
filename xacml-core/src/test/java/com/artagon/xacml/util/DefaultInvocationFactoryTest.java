package com.artagon.xacml.util;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.util.DefaultInvocationFactory;
import com.artagon.xacml.util.Invocation;
import com.artagon.xacml.util.InvocationFactory;

public class DefaultInvocationFactoryTest
{
	private InvocationFactory f;
	private TestObject instance;
	
	@Before
	public void init(){
		this.f = new DefaultInvocationFactory();
		this.instance = new TestObject();
	}
	
	@Test
	public void testInvoke() throws Exception
	{
		Invocation<String> invoke = f.create(instance, instance.getClass().getMethod("test", String.class));
		assertEquals("test", invoke.invoke("test"));
	}
	
	@Test
	public void testInvokeStatic() throws Exception
	{
		Invocation<String> invoke = f.create(null, TestObject.class.getMethod("testStatic", String.class));
		assertEquals("test", invoke.invoke("test"));
	}
}
