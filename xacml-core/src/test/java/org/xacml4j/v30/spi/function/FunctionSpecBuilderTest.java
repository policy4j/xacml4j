package org.xacml4j.v30.spi.function;

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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.xacml4j.v30.Expression;
import org.xacml4j.v30.XacmlSyntaxException;
import org.xacml4j.v30.pdp.FunctionSpec;
import org.xacml4j.v30.types.IntegerExp;
import org.xacml4j.v30.types.StringExp;
import org.xacml4j.v30.types.XacmlTypes;

import com.google.common.collect.ImmutableList;

public class FunctionSpecBuilderTest
{
	private FunctionSpec specSameTypeArgs;
	private FunctionSpec specDiffTypeArgs;
	private FunctionInvocation impl;
	private IMocksControl c;
	
	@Before
	public void init(){
		this.c = createControl();
		this.impl =  c.createMock(FunctionInvocation.class);
		FunctionSpecBuilder b = FunctionSpecBuilder.builder("testFunc1");
		this.specSameTypeArgs = b.param(XacmlTypes.INTEGER).param(XacmlTypes.INTEGER).build(XacmlTypes.INTEGER, impl);
		b = FunctionSpecBuilder.builder("testFunc2");
		this.specDiffTypeArgs = b.param(XacmlTypes.INTEGER).optional(XacmlTypes.STRING).build(XacmlTypes.INTEGER, impl);
	}
	
	@Test
	public void testAddOptionalParameters(){
		FunctionSpecBuilder b = FunctionSpecBuilder.builder("testFunc1");
		b.varArg(XacmlTypes.STRING, 0, 100);
	}
	
	@Test(expected=XacmlSyntaxException.class)
	public void testAddMultipleVarArg(){
		
		FunctionSpecBuilder b = FunctionSpecBuilder.builder("testFunc1");
		b.varArg(XacmlTypes.STRING, 0, 100);
		b.varArg(XacmlTypes.STRING, 0, 5);
	}
	
	public void testAddVarArgAfterOptional(){
		
		FunctionSpecBuilder b = FunctionSpecBuilder.builder("testFunc1");
		b.optional(XacmlTypes.STRING);
		b.varArg(XacmlTypes.STRING, 0, 5);
		FunctionSpec f = b.build(XacmlTypes.BOOLEAN, impl);
		assertTrue(f.isVariadic());
	}
	
	@Test(expected=XacmlSyntaxException.class)
	public void testAddVarArgAfterDefaultValue(){
		
		FunctionSpecBuilder b = FunctionSpecBuilder.builder("testFunc1");
		b.optional(XacmlTypes.STRING, StringExp.valueOf("aaa"));
		b.varArg(XacmlTypes.STRING, 0, 5);
	}
	
	@Test(expected=XacmlSyntaxException.class)
	public void testAddOptionalAfterVarArg1(){
		
		FunctionSpecBuilder b = FunctionSpecBuilder.builder("testFunc1");
		b.varArg(XacmlTypes.STRING, 0, 5);
		b.optional(XacmlTypes.STRING);
		
	}
	
	@Test(expected=XacmlSyntaxException.class)
	public void testAddOptionalAfterVarArg2(){
		
		FunctionSpecBuilder b = FunctionSpecBuilder.builder("testFunc1");
		b.varArg(XacmlTypes.STRING, 0, 5);
		b.optional(XacmlTypes.STRING, StringExp.valueOf("aaa"));
		
	}
	
	@Test(expected=XacmlSyntaxException.class)
	public void testAddParamWithDefaultValueAndOptionalFalse(){
		FunctionSpecBuilder b = FunctionSpecBuilder.builder("testFunc1");
		b.param(XacmlTypes.STRING, StringExp.valueOf("aaa"), false);
		
	}
	@Test
	public void testValidateSingleTypeParametersWithSameTypeArgs()
	{
		ImmutableList.Builder<Expression> b = ImmutableList.builder();
		c.replay();
		assertFalse(specSameTypeArgs.validateParameters(b.build()));
		assertFalse(specSameTypeArgs.validateParameters(b.add(IntegerExp.valueOf(10L)).build()));
		assertTrue(specSameTypeArgs.validateParameters(b.add(IntegerExp.valueOf(12L)).build()));
		assertFalse(specSameTypeArgs.validateParameters(b.add(IntegerExp.valueOf(13L)).build()));
		c.verify();
	}

	@Test
	public void testValidateSingeTypeParametersWithDiffTypesArgs()
	{
		ImmutableList.Builder<Expression> b = ImmutableList.builder();
		c.replay();
		assertFalse(specDiffTypeArgs.validateParameters(b.build()));
		assertTrue(specDiffTypeArgs.validateParameters(b.add(IntegerExp.valueOf(10L)).build()));
		assertFalse(specDiffTypeArgs.validateParameters(b.add(IntegerExp.valueOf(12L)).build()));
		assertFalse(specDiffTypeArgs.validateParameters(b.add(IntegerExp.valueOf(13L)).build()));
		b = ImmutableList.builder();
		List<Expression> exp1 = b.add(IntegerExp.valueOf(10)).add(StringExp.valueOf("aaa")).build();
		assertTrue(specDiffTypeArgs.validateParameters(exp1));
		List<Expression> exp2 = b
				.add(IntegerExp.valueOf(10))
				.add(IntegerExp.valueOf(11)).build();
		assertFalse(specDiffTypeArgs.validateParameters(exp2));
		b = ImmutableList.builder();
		assertFalse(specDiffTypeArgs.validateParameters(b.add(StringExp.valueOf("a")).add(IntegerExp.valueOf(10L)).build()));
		c.verify();
	}

	@Test(expected=XacmlSyntaxException.class)
	public void testParameterAfterVaragParam()
	{
		FunctionSpecBuilder b = FunctionSpecBuilder.builder("testFunc");
		c.replay();
		b.varArg(XacmlTypes.INTEGER, 1, 10).param(XacmlTypes.INTEGER);
		c.verify();
	}

	@Test(expected=IllegalArgumentException.class)
	public void testParameterVarArgMinAndMaxEquals()
	{
		FunctionSpecBuilder b = FunctionSpecBuilder.builder("testFunc");
		c.replay();
		b.varArg(XacmlTypes.INTEGER, 3, 3).param(XacmlTypes.INTEGER);
		c.verify();
	}
}
