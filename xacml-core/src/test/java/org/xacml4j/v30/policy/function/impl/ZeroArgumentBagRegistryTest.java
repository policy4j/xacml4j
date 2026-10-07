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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.xacml4j.v30.BagOfValues;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.StatusCodeId;
import org.xacml4j.v30.ValueExp;
import org.xacml4j.v30.policy.Apply;
import org.xacml4j.v30.policy.FunctionReference;
import org.xacml4j.v30.policy.FunctionSpec;
import org.xacml4j.v30.policy.function.FunctionProvider;
import org.xacml4j.v30.policy.function.FunctionProviderBuilder;
import org.xacml4j.v30.types.XacmlTypes;

/**
 * Bead policy4j-the.4.53: a {@code type-bag} function without arguments
 * returns an empty bag of its type. XACML 3.0 A.3.10: "This function SHALL
 * take any number of arguments of 'type' and return a bag of 'type' values
 * containing the values of the arguments. An application of this function
 * to zero arguments SHALL produce an empty bag of the specified data-type."
 * <p>
 * Every call goes through the function registry and
 * {@code FunctionSpec.invoke}, the path that {@code Apply} uses.
 */
public class ZeroArgumentBagRegistryTest
{
	private static final FunctionProvider FUNCTIONS =
			FunctionProviderBuilder.builder().withDefaultFunctions().build();

	private EvaluationContext context;

	@BeforeEach
	public void init()
	{
		this.context = mock(EvaluationContext.class);
	}

	@ParameterizedTest(name = "{0}")
	@ValueSource(strings = {
			"urn:oasis:names:tc:xacml:1.0:function:string-bag",
			"urn:oasis:names:tc:xacml:1.0:function:boolean-bag",
			"urn:oasis:names:tc:xacml:1.0:function:integer-bag",
			"urn:oasis:names:tc:xacml:1.0:function:double-bag",
			"urn:oasis:names:tc:xacml:1.0:function:time-bag",
			"urn:oasis:names:tc:xacml:1.0:function:date-bag",
			"urn:oasis:names:tc:xacml:1.0:function:dateTime-bag",
			"urn:oasis:names:tc:xacml:1.0:function:anyURI-bag",
			"urn:oasis:names:tc:xacml:1.0:function:hexBinary-bag",
			"urn:oasis:names:tc:xacml:1.0:function:base64Binary-bag",
			"urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-bag",
			"urn:oasis:names:tc:xacml:1.0:function:dayTimeDuration-bag",
			"urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-bag",
			"urn:oasis:names:tc:xacml:1.0:function:yearMonthDuration-bag",
			"urn:oasis:names:tc:xacml:1.0:function:x500Name-bag",
			"urn:oasis:names:tc:xacml:1.0:function:rfc822Name-bag",
			"urn:oasis:names:tc:xacml:2.0:function:ipAddress-bag",
			"urn:oasis:names:tc:xacml:2.0:function:dnsName-bag",
			"urn:oasis:names:tc:xacml:1.0:function:entity-bag"})
	public void bagFunctionWithoutArgumentsReturnsAnEmptyBagOfItsType(String functionId)
	{
		FunctionSpec bag = require(functionId);

		ValueExp result = bag.invoke(context);

		assertTrue(result instanceof BagOfValues, () -> "not a bag: " + result);
		BagOfValues empty = (BagOfValues) result;
		assertEquals(0, empty.size());
		// The declared return type of the function, not only "some empty bag".
		assertEquals(bag.resolveReturnType(List.of()), empty.getEvaluatesTo());
		// A.3.10: type-bag-size of the empty bag is integer zero.
		ValueExp zero = XacmlTypes.INTEGER.ofAny(0);
		ValueExp size = require(functionId + "-size").invoke(context, empty);
		assertEquals(zero, size);
	}

	@Test
	public void applyOfABagFunctionWithoutArgumentsReturnsAnEmptyBag()
	{
		Apply apply = Apply.builder("urn:oasis:names:tc:xacml:1.0:function:string-bag", FUNCTIONS::getFunction)
				.build();

		assertEquals(XacmlTypes.STRING.emptyBag(), apply.evaluate(context));
	}

	/** rv084: a bag with arguments is built as before. */
	@Test
	public void bagFunctionWithArgumentsKeepsItsValues()
	{
		FunctionSpec stringBag = require("urn:oasis:names:tc:xacml:1.0:function:string-bag");

		assertEquals(XacmlTypes.STRING.bagBuilder().value("a").build(),
				stringBag.invoke(context, XacmlTypes.STRING.ofAny("a")));
		assertEquals(XacmlTypes.STRING.bagBuilder().value("a").value("b").build(),
				stringBag.invoke(context, XacmlTypes.STRING.ofAny("a"), XacmlTypes.STRING.ofAny("b")));
	}

	/**
	 * Only a variadic parameter that allows zero arguments gets the empty
	 * array. {@code integer-add} needs two arguments and {@code any-of}
	 * needs one after the function: without them the call still fails, with
	 * the status it had before, and does not compute a value from nothing.
	 */
	@Test
	public void functionWithAMinimumOfVariadicArgumentsStillFailsWithoutThem()
	{
		EvaluationException add = assertThrows(EvaluationException.class,
				() -> require("urn:oasis:names:tc:xacml:1.0:function:integer-add").invoke(context));
		assertEquals(StatusCodeId.STATUS_PROCESSING_ERROR, add.getStatus().getStatusCode().getValue());

		FunctionReference predicate = new FunctionReference(
				require("urn:oasis:names:tc:xacml:1.0:function:integer-equal"));
		EvaluationException anyOf = assertThrows(EvaluationException.class,
				() -> require("urn:oasis:names:tc:xacml:3.0:function:any-of").invoke(context, predicate));
		assertEquals(StatusCodeId.SYNTAX_ERROR, anyOf.getStatus().getStatusCode().getValue());
	}

	private static FunctionSpec require(String functionId)
	{
		return FUNCTIONS.getFunction(functionId)
				.orElseThrow(() -> new AssertionError("missing function " + functionId));
	}
}
