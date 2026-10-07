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
import org.xacml4j.v30.BagOfValues;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.Expression;
import org.xacml4j.v30.ValueExp;
import org.xacml4j.v30.policy.Apply;
import org.xacml4j.v30.policy.FunctionSpec;
import org.xacml4j.v30.policy.function.FunctionProvider;
import org.xacml4j.v30.policy.function.FunctionProviderBuilder;
import org.xacml4j.v30.types.BooleanVal;
import org.xacml4j.v30.types.Value;
import org.xacml4j.v30.types.ValueType;
import org.xacml4j.v30.types.XacmlTypes;

/**
 * XACML 3.0 Appendix A.3.10 and A.3.11 define duration bag and set functions.
 * Tables 10.2.8 and 10.2.9 list their 3.0 identifiers and retained 1.0 names:
 * https://docs.oasis-open.org/xacml/3.0/xacml-3.0-core-spec-os-en.html
 */
public class DurationFunctionIdentifierTest
{
	private static final FunctionProvider FUNCTIONS =
			FunctionProviderBuilder.builder().withDefaultFunctions().build();

	private String identifier;
	private String canonical;
	private String legacy;

	public void initDurationFunctionIdentifierTest(String identifier, String canonical, String legacy)
	{
		this.identifier = identifier;
		this.canonical = canonical;
		this.legacy = legacy;
	}

	public static Collection<Object[]> identifiers()
	{
		List<Object[]> identifiers = new ArrayList<>();
		for (String type : new String[]{"dayTimeDuration", "yearMonthDuration"}) {
			for (String operation : new String[]{"one-and-only", "bag-size", "is-in", "bag",
					"intersection", "union", "subset", "at-least-one-member-of", "set-equals"}) {
				String canonical = "urn:oasis:names:tc:xacml:3.0:function:" + type + "-" + operation;
				String legacy = "urn:oasis:names:tc:xacml:1.0:function:" + type + "-" + operation;
				identifiers.add(new Object[]{canonical, canonical, legacy});
				identifiers.add(new Object[]{legacy, canonical, legacy});
			}
		}
		return identifiers;
	}

	@MethodSource("identifiers")
	@ParameterizedTest(name = "{0}")
	public void resolvesStandardAndLegacyNamesToSameFunction(String identifier, String canonical, String legacy)
	{
		initDurationFunctionIdentifierTest(identifier, canonical, legacy);
		Optional<FunctionSpec> resolved = FUNCTIONS.getFunction(identifier);
		assertTrue(resolved.isPresent(), "Missing required function " + identifier);
		assertEquals(canonical, resolved.get().getId());
		assertTrue(resolved.get().getLegacyId().isPresent(), "Missing legacy alias " + legacy);
		assertEquals(legacy, resolved.get().getLegacyId().get());
		assertSame(resolved.get(), FUNCTIONS.getFunction(canonical).get());
		assertSame(resolved.get(), FUNCTIONS.getFunction(legacy).get());
	}

	@MethodSource("identifiers")
	@ParameterizedTest(name = "{0}")
	public void evaluatesStandardAndLegacyNamesIdentically(String identifier, String canonical, String legacy) throws Exception
	{
		initDurationFunctionIdentifierTest(identifier, canonical, legacy);
		String name = canonical.substring(canonical.lastIndexOf(':') + 1);
		boolean dayTime = name.startsWith("dayTimeDuration-");
		ValueType type = dayTime ? XacmlTypes.DAYTIMEDURATION : XacmlTypes.YEARMONTHDURATION;
		String operation = name.substring(name.indexOf('-') + 1);
		Value a = type.ofAny(dayTime ? "PT1S" : "P1M");
		Value b = type.ofAny(dayTime ? "PT2S" : "P2M");
		Value c = type.ofAny(dayTime ? "PT3S" : "P3M");
		BagOfValues ab = type.bagOf(a, b);
		BagOfValues bc = type.bagOf(b, c);
		Expression[] arguments;
		ValueExp expected;
		switch (operation) {
		case "one-and-only": arguments = new Expression[]{type.bagOf(a)}; expected = a; break;
		case "bag-size": arguments = new Expression[]{ab}; expected = XacmlTypes.INTEGER.ofAny(2); break;
		case "is-in": arguments = new Expression[]{b, ab}; expected = BooleanVal.of(true); break;
		case "bag": arguments = new Expression[]{a, b}; expected = ab; break;
		case "intersection": arguments = new Expression[]{ab, bc}; expected = type.bagOf(b); break;
		case "union": arguments = new Expression[]{ab, bc}; expected = type.bagOf(a, b, c); break;
		case "subset": arguments = new Expression[]{type.bagOf(a), ab}; expected = BooleanVal.of(true); break;
		case "at-least-one-member-of": arguments = new Expression[]{ab, bc}; expected = BooleanVal.of(true); break;
		case "set-equals": arguments = new Expression[]{ab, type.bagOf(b, a)}; expected = BooleanVal.of(true); break;
		default: throw new AssertionError("Uncovered duration operation " + operation);
		}
		for (boolean validate : new boolean[]{false, true}) {
			EvaluationContext context = mock(EvaluationContext.class);
			when(context.isValidateFuncParamsAtRuntime()).thenReturn(validate);
			ValueExp actual = evaluate(identifier, arguments, context);
			assertEquals(expected, actual);
			assertEquals(actual, evaluate(canonical, arguments, context));
			assertEquals(actual, evaluate(legacy, arguments, context));
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
