package org.xacml4j.v30.policy.function;

/*
 * #%L
 * Xacml4J Core Engine Implementation
 * %%
 * Copyright (C) 2009 - 2026 Xacml4J.org
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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.xacml4j.v30.policy.FunctionSpec;
import org.xacml4j.v30.policy.function.impl.PolicyToPlatformFunctionInvocation;
import org.xacml4j.v30.types.StringVal;
import org.xacml4j.v30.types.XacmlTypes;

/**
 * Only the engine's own string-one-and-only, string-normalize-space and string-normalize-to-lower-case
 * are functions whose values a prepared condition may share (change optimize-pdp-evaluation, design D45,
 * maintainer decision 1 of 2026-10-28): the same id, shape or invocation factory is not enough.
 */
class ShareableBuiltinTest
{
	private static final String FUNCTION = "urn:oasis:names:tc:xacml:1.0:function:";
	private static final FunctionProvider STANDARD = FunctionProviderBuilder.builder().build();

	@ParameterizedTest
	@ValueSource(strings = {"string-one-and-only", "string-normalize-space", "string-normalize-to-lower-case"})
	void theEngineBuildsTheThreeFunctionsShareable(String name){
		assertTrue(FunctionSpecBuilder.isShareableBuiltin(STANDARD.getFunction(FUNCTION + name).orElseThrow()));
	}

	@ParameterizedTest
	@ValueSource(strings = {FUNCTION + "string-equal", FUNCTION + "and", FUNCTION + "string-regexp-match",
			FUNCTION + "integer-one-and-only", FUNCTION + "string-bag-size", FUNCTION + "string-is-in",
			"urn:xacml4j:names:tc:xacml:1.0:function:string-normalize-to-upper-case"})
	void noOtherFunctionOfTheEngineIsShareable(String id){
		assertFalse(FunctionSpecBuilder.isShareableBuiltin(STANDARD.getFunction(id).orElseThrow()));
	}

	@ParameterizedTest
	@ValueSource(strings = {"string-one-and-only", "string-normalize-space", "string-normalize-to-lower-case"})
	void theThreeFunctionsOfAnotherInvocationFactoryAreNotShareable(String name){
		FunctionInvocationFactory delegating = new FunctionInvocationFactory(){
			@Override
			public <T> PolicyToPlatformFunctionInvocation<T> create(Object instance, Method m){
				return FunctionInvocationFactory.defaultFactory().create(instance, m);
			}
		};
		FunctionProvider functions = FunctionProviderBuilder.builder(delegating).build();
		assertFalse(FunctionSpecBuilder.isShareableBuiltin(functions.getFunction(FUNCTION + name).orElseThrow()));
	}

	@Test
	void aProviderMethodWithAStandardIdAndShapeIsNotShareable(){
		FunctionProvider functions = FunctionProvider.builder().fromClass(LookAlike.class).build();
		assertFalse(FunctionSpecBuilder.isShareableBuiltin(functions.getFunction(FUNCTION + "string-normalize-space").orElseThrow()));
	}

	@Test
	void aBuiltFunctionWithAStandardIdIsNotShareable(){
		FunctionSpec plain = FunctionSpecBuilder.builder(FUNCTION + "string-normalize-space").param(XacmlTypes.STRING)
				.writesNoResponses().build(XacmlTypes.STRING, mock(FunctionInvocation.class));
		FunctionSpec validated = FunctionSpecBuilder.builder(FUNCTION + "string-normalize-space").param(XacmlTypes.STRING)
				.writesNoResponses().build(XacmlTypes.STRING, (spec, arguments) -> true, mock(FunctionInvocation.class));
		assertFalse(FunctionSpecBuilder.isShareableBuiltin(plain));
		assertFalse(FunctionSpecBuilder.isShareableBuiltin(validated));
		assertFalse(FunctionSpecBuilder.isShareableBuiltin(mock(FunctionSpec.class)));
	}

	/** A provider of the application with a method of the standard id and shape */
	@XacmlFunctionProvider(description = "A provider of the application")
	public static final class LookAlike
	{
		private LookAlike() {}

		@XacmlFuncSpec(id = FUNCTION + "string-normalize-space")
		@XacmlFuncReturnType(typeId = "http://www.w3.org/2001/XMLSchema#string")
		public static StringVal normalizeSpace(
				@XacmlFuncParam(typeId = "http://www.w3.org/2001/XMLSchema#string") StringVal v)
		{
			return StringVal.of(v.get().trim());
		}
	}
}
