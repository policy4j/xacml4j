package com.artagon.xacml.v3.policy.function;

import java.lang.reflect.Method;

import org.junit.Before;
import org.junit.Test;

public class ReflectionTest
{
	private Method m;
	
	@Before
	public void init() throws Exception{
		this.m = ReflectionTest.class.getMethod("testVarArg", Long.class, Long.class, String[].class);
	}
	@Test
	public void testInvokeVarArg() throws Exception
	{
		Object[] varArg = new String[]{"a", "b"};
		m.invoke(null, 10L, 20L, varArg);
	}
	
	public static Long testVarArg(Long a, Long b, String ...args)
	{
		return 0L;
	}
}
