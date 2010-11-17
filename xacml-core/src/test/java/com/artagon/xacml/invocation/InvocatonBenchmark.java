package com.artagon.xacml.invocation;

import org.junit.Before;
import org.junit.Test;

public class InvocatonBenchmark 
{
	private TestObject instance;
	
	@Before
	public void init(){
		this.instance = new TestObject();
	}
	
	private static long performBenchmark(int num, Invocation<?> invoke, 
			Object ...params) throws Exception
	{
		long sum = 0;
		for(int i = 0; i < num; i++)
		{
			long start = System.nanoTime();
			invoke.invoke(params);
			long end = System.nanoTime();
			sum += (end - start);
		}
		return sum/num;
	}
	
	@Test
	public void testCglibStaticInvocationPerformance() throws Exception
	{
		InvocationFactory f = new CglibInvocationFactory();
		Invocation<String> invoke = f.create(null, TestObject.class.getMethod("testStatic", String.class));
		System.out.println("CGLIB static - " + performBenchmark(1000000, invoke, "aaa"));
	}
	
	@Test
	public void testCglibInvocationPerformance() throws Exception
	{
		InvocationFactory f = new CglibInvocationFactory();
		Invocation<String> invoke = f.create(instance, TestObject.class.getMethod("test", String.class));
		System.out.println("CGLIB instance - " + performBenchmark(1000000, invoke, "aaa"));
	}
	
	@Test
	public void testDefaultStaticInvocationPerformance() throws Exception
	{
		InvocationFactory f = new DefaultInvocationFactory();
		Invocation<String> invoke = f.create(null, TestObject.class.getMethod("testStatic", String.class));
		System.out.println("Default static - " + performBenchmark(1000000, invoke, "aaa"));
	}
	
	@Test
	public void testDefaultInvocationPerformance() throws Exception
	{
		InvocationFactory f = new DefaultInvocationFactory();
		Invocation<String> invoke = f.create(instance, TestObject.class.getMethod("test", String.class));
		System.out.println("Default instance - " + performBenchmark(1000000, invoke, "aaa"));
	}
}
