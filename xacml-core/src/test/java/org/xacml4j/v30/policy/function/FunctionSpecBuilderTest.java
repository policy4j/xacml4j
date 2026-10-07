package org.xacml4j.v30.policy.function;

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


import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xacml4j.v30.Expression;
import org.xacml4j.v30.SyntaxException;
import org.xacml4j.v30.policy.FunctionSpec;
import org.xacml4j.v30.types.XacmlTypes;

import com.google.common.collect.ImmutableList;

@ExtendWith(MockitoExtension.class)
public class FunctionSpecBuilderTest
{
	private FunctionSpec specSameTypeArgs;
	private FunctionSpec specDiffTypeArgs;
	private FunctionInvocation impl;
	
	@BeforeEach
	public void init(){
		this.impl =  mock(FunctionInvocation.class);
		FunctionSpecBuilder b = FunctionSpecBuilder.builder("testFunc1");
		this.specSameTypeArgs = b.param(XacmlTypes.INTEGER).param(XacmlTypes.INTEGER).build(XacmlTypes.INTEGER, impl);
		b = FunctionSpecBuilder.builder("testFunc2");
		this.specDiffTypeArgs = b.param(XacmlTypes.INTEGER).optional(XacmlTypes.STRING).build(XacmlTypes.INTEGER, impl);
	}

	@Test
	public void testFunctionShortId(){

		assertEquals("test", FunctionSpecBuilder.getShortId("test", null));
		assertEquals("test", FunctionSpecBuilder.getShortId("test:test", null));
		assertEquals("test", FunctionSpecBuilder.getShortId("test", null));
		assertEquals("aaaa", FunctionSpecBuilder.getShortId("test:test:aaaa", null));
		assertEquals("test:test:", FunctionSpecBuilder.getShortId("test:test:", null));
		assertEquals("xpath-node-match", FunctionSpecBuilder.getShortId("urn:oasis:names:tc:xacml:3.0:function:xpath-node-match", null));


	}
	
	@Test
	public void testAddOptionalParameters(){
		FunctionSpecBuilder b = FunctionSpecBuilder.builder("testFunc1");
		b.varArg(XacmlTypes.STRING, 0, 100);
	}
	
	@Test
	public void testAddMultipleVarArg(){
		
		FunctionSpecBuilder b = FunctionSpecBuilder.builder("testFunc1");
		b.varArg(XacmlTypes.STRING, 0, 100);
		assertThrows(SyntaxException.class, () -> b.varArg(XacmlTypes.STRING, 0, 5));
	}
	
	public void testAddVarArgAfterOptional(){
		
		FunctionSpecBuilder b = FunctionSpecBuilder.builder("testFunc1");
		b.optional(XacmlTypes.STRING);
		b.varArg(XacmlTypes.STRING, 0, 5);
		FunctionSpec f = b.build(XacmlTypes.BOOLEAN, impl);
		assertTrue(f.isVariadic());
	}
	
	@Test
	public void testAddVarArgAfterDefaultValue(){
		
		FunctionSpecBuilder b = FunctionSpecBuilder.builder("testFunc1");
		assertThrows(SyntaxException.class, () -> {
			b.optional(XacmlTypes.STRING, XacmlTypes.STRING.ofAny("aaa"));
			b.varArg(XacmlTypes.STRING, 0, 5);
		});
	}
	
	@Test
	public void testAddOptionalAfterVarArg1(){
		
		FunctionSpecBuilder b = FunctionSpecBuilder.builder("testFunc1");
		b.varArg(XacmlTypes.STRING, 0, 5);
		assertThrows(SyntaxException.class, () -> b.optional(XacmlTypes.STRING));
		
	}
	
	@Test
	public void testAddOptionalAfterVarArg2(){
		
		FunctionSpecBuilder b = FunctionSpecBuilder.builder("testFunc1");
		b.varArg(XacmlTypes.STRING, 0, 5);
		assertThrows(SyntaxException.class, () -> b.optional(XacmlTypes.STRING, XacmlTypes.STRING.ofAny("aaa")));
		
	}
	
	@Test
	public void testAddParamWithDefaultValueAndOptionalFalse(){
		FunctionSpecBuilder b = FunctionSpecBuilder.builder("testFunc1");
		assertThrows(SyntaxException.class, () -> b.param(XacmlTypes.STRING, XacmlTypes.STRING.ofAny("aaa"), false));
		
	}
	@Test
	public void testValidateSingleTypeParametersWithSameTypeArgs()
	{
		ImmutableList.Builder<Expression> b = ImmutableList.builder();
		assertFalse(specSameTypeArgs.validateParameters(b.build()));
		assertFalse(specSameTypeArgs.validateParameters(b.add(XacmlTypes.INTEGER.ofAny(10L)).build()));
		assertTrue(specSameTypeArgs.validateParameters(b.add(XacmlTypes.INTEGER.ofAny(12L)).build()));
		assertFalse(specSameTypeArgs.validateParameters(b.add(XacmlTypes.INTEGER.ofAny(13L)).build()));
		verifyNoMoreInteractions(impl);
	}

	@Test
	public void testValidateSingeTypeParametersWithDiffTypesArgs()
	{
		ImmutableList.Builder<Expression> b = ImmutableList.builder();
		assertFalse(specDiffTypeArgs.validateParameters(b.build()));
		assertTrue(specDiffTypeArgs.validateParameters(b.add(XacmlTypes.INTEGER.ofAny(10L)).build()));
		assertFalse(specDiffTypeArgs.validateParameters(b.add(XacmlTypes.INTEGER.ofAny(12L)).build()));
		assertFalse(specDiffTypeArgs.validateParameters(b.add(XacmlTypes.INTEGER.ofAny(13L)).build()));
		b = ImmutableList.builder();
		List<Expression> exp1 = b.add(XacmlTypes.INTEGER.ofAny(10)).add(XacmlTypes.STRING.ofAny("aaa")).build();
		assertTrue(specDiffTypeArgs.validateParameters(exp1));
		List<Expression> exp2 = b
				.add(XacmlTypes.INTEGER.ofAny(10))
				.add(XacmlTypes.INTEGER.ofAny(11)).build();
		assertFalse(specDiffTypeArgs.validateParameters(exp2));
		b = ImmutableList.builder();
		assertFalse(specDiffTypeArgs.validateParameters(b.add(XacmlTypes.STRING.ofAny("a")).add(XacmlTypes.INTEGER.ofAny(10L)).build()));
		verifyNoMoreInteractions(impl);
	}

	@Test
	public void testParameterAfterVaragParam()
	{
		FunctionSpecBuilder b = FunctionSpecBuilder.builder("testFunc");
		assertThrows(SyntaxException.class, () -> b.varArg(XacmlTypes.INTEGER, 1, 10).param(XacmlTypes.INTEGER));
		verifyNoMoreInteractions(impl);
	}

	@Test
	public void testParameterVarArgMinAndMaxEquals()
	{
		FunctionSpecBuilder b = FunctionSpecBuilder.builder("testFunc");
		assertThrows(IllegalArgumentException.class, () -> b.varArg(XacmlTypes.INTEGER, 3, 3).param(XacmlTypes.INTEGER));
		verifyNoMoreInteractions(impl);
	}
}
