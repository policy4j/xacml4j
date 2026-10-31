package org.xacml4j.v30.policy.function.impl;

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

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;

import java.util.Arrays;

import org.junit.Test;
import org.xacml4j.v30.BagOfValues;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.Expression;
import org.xacml4j.v30.SyntaxException;
import org.xacml4j.v30.policy.Apply;
import org.xacml4j.v30.policy.FunctionReference;
import org.xacml4j.v30.policy.FunctionSpec;
import org.xacml4j.v30.policy.function.FunctionInvocationFactory;
import org.xacml4j.v30.policy.function.FunctionProviderBuilder;
import org.xacml4j.v30.policy.function.XacmlFuncParamVarArg;
import org.xacml4j.v30.policy.function.XacmlFuncReturnType;
import org.xacml4j.v30.policy.function.XacmlFuncSpec;
import org.xacml4j.v30.types.BooleanVal;
import org.xacml4j.v30.types.StringVal;
import org.xacml4j.v30.types.XacmlTypes;

/** Characterizes the mixed varargs metadata/packing required by XACML 3.0 A.3.12. */
public class HigherOrderVariadicInvocationTest
{
	@Test
	public void mixedValuesReachDeclaredExpressionArray() throws Exception
	{
		FunctionSpec spec = mixedSpec();
		Expression[] arguments = {StringVal.of("prefix"), XacmlTypes.STRING.bagOf(StringVal.of("a")), StringVal.of("suffix")};
		spec.validateParametersAndThrow(Arrays.asList(arguments));
		for (boolean validate : new boolean[]{false, true}) {
			assertEquals(BooleanVal.of(true), Apply.builder(spec).param(arguments).build().evaluate(context(validate)));
		}
	}

	@Test
	public void mixedSequenceStillChecksMinimumAndExcludesFunctionReferences() throws Exception
	{
		FunctionSpec spec = mixedSpec();
		assertFalse(spec.validateParameters(Arrays.asList()));
		assertThrows(SyntaxException.class, () -> spec.validateParametersAndThrow(Arrays.asList()));
		FunctionReference ref = new FunctionReference(FunctionProviderBuilder.builder().withDefaultFunctions().build()
				.getFunction("urn:oasis:names:tc:xacml:1.0:function:string-equal").get());
		assertFalse(spec.validateParameters(Arrays.asList(ref)));
	}

	@Test
	public void typedVariadicValidationIsUnchanged() throws Exception
	{
		FunctionSpec spec = converter().createFunctionSpec(Methods.class.getMethod("strings", StringVal[].class));
		assertFalse(spec.validateParameters(Arrays.asList(StringVal.of("a"), XacmlTypes.INTEGER.ofAny(1))));
		assertEquals(BooleanVal.of(true), Apply.builder(spec).param(StringVal.of("a"), StringVal.of("b"))
				.build().evaluate(context(true)));
	}

	private static FunctionSpec mixedSpec() throws Exception
	{
		return converter().createFunctionSpec(Methods.class.getMethod("mixed", Expression[].class));
	}

	private static JavaMethodToFunctionSpecConverter converter()
	{
		return new JavaMethodToFunctionSpecConverter(FunctionInvocationFactory.defaultFactory());
	}

	private static EvaluationContext context(boolean validate)
	{
		EvaluationContext context = createMock(EvaluationContext.class);
		expect(context.isValidateFuncParamsAtRuntime()).andStubReturn(validate);
		replay(context);
		return context;
	}

	public static class Methods
	{
		@XacmlFuncSpec(id="urn:xacml4j:test:mixed-values")
		@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
		public static BooleanVal mixed(@XacmlFuncParamVarArg(typeId="", min=1) Expression... values)
		{
			return BooleanVal.of(values.getClass() == Expression[].class && values.length == 3
					&& values[0].equals(StringVal.of("prefix")) && values[1] instanceof BagOfValues
					&& values[2].equals(StringVal.of("suffix")));
		}

		@XacmlFuncSpec(id="urn:xacml4j:test:typed-strings")
		@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
		public static BooleanVal strings(@XacmlFuncParamVarArg(typeId="http://www.w3.org/2001/XMLSchema#string", min=1) StringVal... values)
		{
			return BooleanVal.of(values.length == 2 && values[0].get().equals("a") && values[1].get().equals("b"));
		}
	}
}
