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
import org.junit.Test;
import org.xacml4j.v30.FunctionInvocation;
import org.xacml4j.v30.FunctionInvocationFactory;

import static org.junit.Assert.assertEquals;

public class DefaultFunctionFunctionInvocationFactoryTest
{
	private FunctionInvocationFactory f;
	private TestObject instance;

	@Before
	public void init(){
		this.f = FunctionInvocationFactory.defaultFactory();
		this.instance = new TestObject();
	}

	@Test
	public void testInvoke() throws Exception
	{
		FunctionInvocation<String> invoke = f.create(instance, instance.getClass().getMethod("test", String.class));
		assertEquals("test", invoke.invoke("test"));
	}

	@Test
	public void testInvokeStatic() throws Exception
	{
		FunctionInvocation<String> invoke = f.create(null, TestObject.class.getMethod("testStatic", String.class));
		assertEquals("test", invoke.invoke("test"));
	}
}
