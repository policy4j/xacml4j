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

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.Expression;
import org.xacml4j.v30.ValueExp;
import org.xacml4j.v30.policy.Apply;
import org.xacml4j.v30.policy.FunctionReference;
import org.xacml4j.v30.policy.FunctionSpec;
import org.xacml4j.v30.policy.function.FunctionProvider;
import org.xacml4j.v30.policy.function.FunctionProviderBuilder;
import org.xacml4j.v30.types.BooleanVal;
import org.xacml4j.v30.types.XacmlTypes;

/**
 * XACML 3.0 Appendix A.3.12 specifies four 3.0 names and three unchanged
 * 1.0 names; A.4 and Table 10.2.9 retain the four older 1.0 names:
 * https://docs.oasis-open.org/xacml/3.0/xacml-3.0-core-spec-os-en.html
 */
public class HigherOrderFunctionIdentifierTest
{
	private static final FunctionProvider FUNCTIONS =
			FunctionProviderBuilder.builder().withDefaultFunctions().build();

	private String identifier;
	private String canonical;
	private String legacy;

	public void initHigherOrderFunctionIdentifierTest(String identifier, String canonical, String legacy)
	{
		this.identifier = identifier;
		this.canonical = canonical;
		this.legacy = legacy;
	}

	public static Collection<Object[]> identifiers()
	{
		List<Object[]> identifiers = new ArrayList<>();
		for (String operation : new String[]{"any-of", "all-of", "any-of-any", "map"}) {
			String canonical = "urn:oasis:names:tc:xacml:3.0:function:" + operation;
			String legacy = "urn:oasis:names:tc:xacml:1.0:function:" + operation;
			identifiers.add(new Object[]{canonical, canonical, legacy});
			identifiers.add(new Object[]{legacy, canonical, legacy});
		}
		for (String operation : new String[]{"all-of-any", "any-of-all", "all-of-all"}) {
			String identifier = "urn:oasis:names:tc:xacml:1.0:function:" + operation;
			identifiers.add(new Object[]{identifier, identifier, null});
		}
		return identifiers;
	}

	@MethodSource("identifiers")
	@ParameterizedTest(name = "{0}")
	public void resolvesEveryStandardHigherOrderName(String identifier, String canonical, String legacy)
	{
		initHigherOrderFunctionIdentifierTest(identifier, canonical, legacy);
		Optional<FunctionSpec> resolved = FUNCTIONS.getFunction(identifier);
		assertTrue(resolved.isPresent(), "Missing required function " + identifier);
		assertEquals(canonical, resolved.get().getId());
		if (legacy != null) {
			assertTrue(resolved.get().getLegacyId().isPresent(), "Missing legacy alias " + legacy);
			assertEquals(legacy, resolved.get().getLegacyId().get());
			assertSame(resolved.get(), FUNCTIONS.getFunction(canonical).get());
			assertSame(resolved.get(), FUNCTIONS.getFunction(legacy).get());
		}
	}

	@MethodSource("identifiers")
	@ParameterizedTest(name = "{0}")
	public void evaluatesStandardAndLegacyNamesIdentically(String identifier, String canonical, String legacy) throws Exception
	{
		initHigherOrderFunctionIdentifierTest(identifier, canonical, legacy);
		String operation = canonical.substring(canonical.lastIndexOf(':') + 1);
		boolean map = operation.equals("map");
		FunctionReference ref = new FunctionReference(FUNCTIONS.getFunction("urn:oasis:names:tc:xacml:1.0:function:"
				+ (map ? "string-normalize-to-lower-case" : "string-equal")).get());
		Expression[] arguments;
		ValueExp expected;
		if (map) {
			arguments = new Expression[]{ref, XacmlTypes.STRING.bagBuilder().value("Hello", "World!").build()};
			expected = XacmlTypes.STRING.bagBuilder().value("hello", "world!").build();
		} else if (operation.equals("any-of")) {
			arguments = new Expression[]{ref, XacmlTypes.STRING.ofAny("Paul"), XacmlTypes.STRING.bagBuilder().value("John", "Paul", "George", "Ringo").build()};
			expected = BooleanVal.of(true);
		} else if (operation.equals("all-of")) {
			arguments = new Expression[]{ref, XacmlTypes.STRING.ofAny("a"), XacmlTypes.STRING.bagBuilder().value("a", "a").build()};
			expected = BooleanVal.of(true);
		} else {
			arguments = new Expression[]{ref, XacmlTypes.STRING.bagBuilder().value("a").build(), XacmlTypes.STRING.bagBuilder().value("a").build()};
			expected = BooleanVal.of(true);
		}
		for (boolean validate : new boolean[]{false, true}) {
			EvaluationContext context = mock(EvaluationContext.class);
			when(context.isValidateFuncParamsAtRuntime()).thenReturn(validate);
			ValueExp actual = evaluate(identifier, arguments, context);
			assertEquals(expected, actual);
			assertEquals(actual, evaluate(canonical, arguments, context));
			if (legacy != null) {
				assertEquals(actual, evaluate(legacy, arguments, context));
			}
			verify(context, atLeastOnce()).isValidateFuncParamsAtRuntime();
			verifyNoMoreInteractions(context);
		}
	}

	private static ValueExp evaluate(String identifier, Expression[] arguments, EvaluationContext context) throws Exception
	{
		FunctionSpec spec = FUNCTIONS.getFunction(identifier).get();
		spec.validateParametersAndThrow(java.util.Arrays.asList(arguments));
		return Apply.builder(spec).param(arguments).build().evaluate(context);
	}
}
