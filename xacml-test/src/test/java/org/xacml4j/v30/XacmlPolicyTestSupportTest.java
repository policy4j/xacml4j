package org.xacml4j.v30;

/*
 * #%L
 * Xacml4J Policy Unit Test Support
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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.xacml4j.v30.types.IntegerVal;
import org.xacml4j.v30.types.StringVal;

public class XacmlPolicyTestSupportTest
{
	@ParameterizedTest
	@EnumSource(Effect.class)
	public void omittedFulfillOnUsesTheResponseDecision(Effect effect)
	{
		Obligation omitted = Obligation.builder("A").attribute(assignment("X")).build();
		Obligation matching = Obligation.builder("A", effect).attribute(assignment("X")).build();
		Effect opposite = effect == Effect.PERMIT ? Effect.DENY : Effect.PERMIT;
		Obligation different = Obligation.builder("A", opposite).attribute(assignment("X")).build();
		ResponseContext expected = responseWithDecision(effect.toDecision(), omitted);
		ResponseContext actual = responseWithDecision(effect.toDecision(), matching);
		XacmlPolicyTestSupport.assertResponse(expected, actual);
		XacmlPolicyTestSupport.assertResponse(actual, expected);
		ResponseContext wrong = responseWithDecision(effect.toDecision(), different);
		assertThrows(AssertionError.class, () -> XacmlPolicyTestSupport.assertResponse(expected, wrong));
		assertThrows(AssertionError.class, () -> XacmlPolicyTestSupport.assertResponse(wrong, expected));
	}

	@Test
	public void duplicateObligationsCannotReuseOneActualMatch()
	{
		Obligation a = Obligation.builder("A").attribute(assignment("X")).build();
		Obligation b = Obligation.builder("B").attribute(assignment("Y")).build();
		ResponseContext expected = obligations(a, a);
		ResponseContext actual = obligations(a, b);
		assertThrows(AssertionError.class, () -> XacmlPolicyTestSupport.assertResponse(expected, actual));
		InvocationTargetException failure = assertThrows(InvocationTargetException.class,
				() -> compareObligationCollections(List.of(a, a), List.of(a, b)));
		assertInstanceOf(AssertionError.class, failure.getCause());
	}

	@Test
	public void obligationMultiplicityAndPermutationArePreserved() throws Exception
	{
		Obligation a = Obligation.builder("A").attribute(assignment("X")).build();
		Obligation b = Obligation.builder("B").attribute(assignment("Y")).build();
		XacmlPolicyTestSupport.assertResponse(obligations(a, a), obligations(a, a));
		XacmlPolicyTestSupport.assertResponse(obligations(a, b, a), obligations(b, a, a));
		compareObligationCollections(List.of(a, a), List.of(a, a));
		compareObligationCollections(List.of(a, b, a), List.of(b, a, a));
	}

	@Test
	public void differentFulfillOnIsRejected()
	{
		Obligation permit = Obligation.builder("A", Effect.PERMIT).attribute(assignment("X")).build();
		Obligation deny = Obligation.builder("A", Effect.DENY).attribute(assignment("X")).build();
		assertThrows(AssertionError.class, () -> XacmlPolicyTestSupport.assertResponse(
				obligations(permit), obligations(deny)));
		assertThrows(AssertionError.class, () -> XacmlPolicyTestSupport.assertResponse(
				obligations(deny), obligations(permit)));
		XacmlPolicyTestSupport.assertResponse(obligations(permit), obligations(permit));
	}

	@ParameterizedTest
	@ValueSource(strings = {"value", "type", "issuer", "category", "attribute-id"})
	public void responseRejectsAssignmentDifferencesUnderTheSameObligationId(String field)
	{
		AttributeAssignment expected = assignment("value");
		AttributeAssignment.Builder actual = AttributeAssignment.builder("attribute")
				.issuer("issuer").category("category").value(StringVal.of("value"));
		switch (field) {
		case "value" -> actual.value(StringVal.of("changed"));
		case "type" -> actual.value(IntegerVal.of(1));
		case "issuer" -> actual.issuer("different");
		case "category" -> actual.category("different");
		case "attribute-id" -> actual.attributeId("different");
		default -> throw new AssertionError(field);
		}
		ResponseContext a = response(expected);
		ResponseContext b = response(actual.build());
		assertThrows(AssertionError.class, () -> XacmlPolicyTestSupport.assertResponse(a, b));
	}

	@Test
	public void assignmentPermutationPreservesBothValues()
	{
		XacmlPolicyTestSupport.assertResponse(response(assignment("first"), assignment("second")),
				response(assignment("second"), assignment("first")));
		assertThrows(AssertionError.class, () -> XacmlPolicyTestSupport.assertResponse(
				response(assignment("first"), assignment("second")),
				response(assignment("changed"), assignment("second"))));
	}

	private static AttributeAssignment assignment(String value)
	{
		return AttributeAssignment.builder("attribute").issuer("issuer").category("category")
				.value(StringVal.of(value)).build();
	}

	private static ResponseContext obligations(Obligation... obligations)
	{
		Result.Builder result = Result.ok(Decision.PERMIT);
		for (Obligation obligation : obligations) {
			result.obligation(obligation);
		}
		return ResponseContext.builder().result(result.build()).build();
	}

	private static ResponseContext responseWithDecision(Decision decision, Obligation obligation)
	{
		return ResponseContext.builder().result(Result.ok(decision).obligation(obligation).build()).build();
	}

	private static void compareObligationCollections(Collection<Obligation> expected,
			Collection<Obligation> actual) throws Exception
	{
		// rv044: Result merges duplicate ids, so exercise the production collection comparator directly.
		var method = XacmlPolicyTestSupport.class.getDeclaredMethod("assertObligations", Collection.class, Collection.class);
		method.setAccessible(true);
		method.invoke(null, expected, actual);
	}

	private static ResponseContext response(AttributeAssignment... assignments)
	{
		Obligation.Builder obligation = Obligation.builder("obligation");
		for (AttributeAssignment assignment : assignments) {
			obligation.attribute(assignment);
		}
		return ResponseContext.builder().result(Result.ok(Decision.PERMIT).obligation(obligation.build()).build()).build();
	}
}
