package org.xacml4j.util;

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

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.xacml4j.v30.policy.function.FunctionInvocationFactory;
import org.xacml4j.v30.policy.function.PolicyToPlatformFunctionInvocation;

@Ignore
public class FunctionInvocationBenchmark
{
	private TestObject instance;

	@Before
	public void init(){
		this.instance = new TestObject();
	}

	private static long performBenchmark(int num, PolicyToPlatformFunctionInvocation<?> invoke,
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
	public void testDefaultStaticInvocationPerformance() throws Exception
	{
		FunctionInvocationFactory f = FunctionInvocationFactory.defaultFactory();
		PolicyToPlatformFunctionInvocation<String> invoke = f.create(null, TestObject.class.getMethod("testStatic", String.class));
		System.out.println("Default static - " + performBenchmark(1000000, invoke, "aaa"));
	}

	@Test
	public void testDefaultInvocationPerformance() throws Exception
	{
		FunctionInvocationFactory f = FunctionInvocationFactory.defaultFactory();
		PolicyToPlatformFunctionInvocation<String> invoke = f.create(instance, TestObject.class.getMethod("test", String.class));
		System.out.println("Default defaultProvider - " + performBenchmark(1000000, invoke, "aaa"));
	}
}
