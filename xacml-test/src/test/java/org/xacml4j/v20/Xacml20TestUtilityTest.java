package org.xacml4j.v20;

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

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.oasis.xacml.v20.jaxb.context.DecisionType;
import org.oasis.xacml.v20.jaxb.context.ResponseType;
import org.oasis.xacml.v20.jaxb.context.ResultType;
import org.oasis.xacml.v20.jaxb.context.StatusCodeType;
import org.oasis.xacml.v20.jaxb.context.StatusDetailType;
import org.oasis.xacml.v20.jaxb.context.StatusType;
import org.oasis.xacml.v20.jaxb.policy.AttributeAssignmentType;
import org.oasis.xacml.v20.jaxb.policy.EffectType;
import org.oasis.xacml.v20.jaxb.policy.ObligationType;
import org.oasis.xacml.v20.jaxb.policy.ObligationsType;
import org.w3c.dom.Element;

public class Xacml20TestUtilityTest
{
	private static final String STRING = "http://www.w3.org/2001/XMLSchema#string";
	private static final String INTEGER = "http://www.w3.org/2001/XMLSchema#integer";

	@ParameterizedTest
	@ValueSource(strings = {"obligation-id", "fulfill-on", "attribute-id", "data-type", "value",
			"assignment-count", "obligation-count"})
	public void responseRejectsEveryObligationFieldDifference(String field)
	{
		ObligationType expected = obligation("obligation", "first");
		ObligationType actual = obligation("obligation", "first");
		ObligationsType actualList = obligations(actual);
		switch (field) {
		case "obligation-id" -> actual.setObligationId("different");
		case "fulfill-on" -> actual.setFulfillOn(EffectType.DENY);
		case "attribute-id" -> actual.getAttributeAssignment().get(0).setAttributeId("different");
		case "data-type" -> actual.getAttributeAssignment().get(0).setDataType(INTEGER);
		case "value" -> actual.getAttributeAssignment().get(0).getContent().set(0, "changed");
		case "assignment-count" -> actual.getAttributeAssignment().add(assignment("extra"));
		case "obligation-count" -> actualList.getObligation().add(obligation("extra", "extra"));
		default -> throw new AssertionError(field);
		}
		assertThrows(AssertionError.class, () -> Xacml20TestUtility.assertResponse(
				response(obligations(expected)), response(actualList)));
	}

	@Test
	public void missingAndUnexpectedObligationsAreRejected()
	{
		assertThrows(AssertionError.class, () -> Xacml20TestUtility.assertResponse(
				response(obligations(obligation("obligation", "value"))), response(null)));
		assertThrows(AssertionError.class, () -> Xacml20TestUtility.assertResponse(
				response(null), response(obligations(obligation("obligation", "value")))));
	}

	@Test
	public void emptyAndAbsentObligationsMatchInBothDirections()
	{
		Xacml20TestUtility.assertResponse(response(null), response(new ObligationsType()));
		Xacml20TestUtility.assertResponse(response(new ObligationsType()), response(null));
	}

	@Test
	public void duplicateIdsAndAssignmentOrderUseAllValues()
	{
		ObligationType first = obligation("same-id", "first");
		first.getAttributeAssignment().add(assignment("second"));
		ObligationType permuted = obligation("same-id", "second");
		permuted.getAttributeAssignment().add(assignment("first"));
		ObligationType changed = obligation("same-id", "changed");
		changed.getAttributeAssignment().add(assignment("second"));
		Xacml20TestUtility.assertObligation(first, permuted);
		assertThrows(AssertionError.class, () -> Xacml20TestUtility.assertObligation(first, changed));
		Xacml20TestUtility.assertResponse(response(obligations(first, obligation("same-id", "third"))),
				response(obligations(obligation("same-id", "third"), permuted)));
		assertThrows(AssertionError.class, () -> Xacml20TestUtility.assertResponse(
				response(obligations(first, obligation("same-id", "third"))),
				response(obligations(changed, obligation("same-id", "third")))));
	}

	@Test
	public void duplicateMultiplicityCannotReuseOneAssignment()
	{
		ObligationType expected = obligation("obligation", "first");
		expected.getAttributeAssignment().add(assignment("first"));
		ObligationType actual = obligation("obligation", "changed");
		actual.getAttributeAssignment().add(assignment("first"));
		assertThrows(AssertionError.class, () -> Xacml20TestUtility.assertObligation(expected, actual));
		assertThrows(AssertionError.class, () -> Xacml20TestUtility.assertResponse(
				response(obligations(obligation("same-id", "first"), obligation("same-id", "first"))),
				response(obligations(obligation("same-id", "first"), obligation("same-id", "changed")))));
	}

	@Test
	public void individualObligationAlsoChecksIdAndFulfillOn()
	{
		ObligationType expected = obligation("obligation", "value");
		ObligationType wrongId = obligation("different", "value");
		ObligationType wrongEffect = obligation("obligation", "value");
		wrongEffect.setFulfillOn(EffectType.DENY);
		assertThrows(AssertionError.class, () -> Xacml20TestUtility.assertObligation(expected, wrongId));
		assertThrows(AssertionError.class, () -> Xacml20TestUtility.assertObligation(expected, wrongEffect));
	}

	@Test
	public void assignmentValuesUseTheirDataTypeAndKeepStringCase()
	{
		AttributeAssignmentType one = assignment("01");
		one.setDataType(INTEGER);
		AttributeAssignmentType same = assignment("1");
		same.setDataType(INTEGER);
		AttributeAssignmentType different = assignment("2");
		different.setDataType(INTEGER);
		Xacml20TestUtility.assertAttributeAssignment(one, same);
		assertThrows(AssertionError.class, () -> Xacml20TestUtility.assertAttributeAssignment(one, different));
		assertThrows(AssertionError.class, () -> Xacml20TestUtility.assertAttributeAssignment(
				assignment("abc"), assignment("ABC")));
	}

	@Test
	public void nestedStatusCodesMustMatch()
	{
		StatusType expected = status();
		StatusType actual = status();
		StatusCodeType child = new StatusCodeType();
		child.setValue("urn:test:nested-error");
		expected.getStatusCode().setStatusCode(child);
		assertThrows(AssertionError.class, () -> Xacml20TestUtility.assertStatus(expected, actual));
		assertThrows(AssertionError.class, () -> Xacml20TestUtility.assertStatus(actual, expected));
	}

	@ParameterizedTest
	@ValueSource(strings = {"direct", "response"})
	public void assignmentExtensionAttributesMustMatch(String path)
	{
		AttributeAssignmentType expected = assignment("value");
		AttributeAssignmentType actual = assignment("value");
		QName metadata = new QName("urn:test:metadata", "name");
		expected.getOtherAttributes().put(metadata, "first");
		actual.getOtherAttributes().put(metadata, "first");
		ObligationType expectedObligation = obligation("obligation", "value");
		expectedObligation.getAttributeAssignment().set(0, expected);
		ObligationType actualObligation = obligation("obligation", "value");
		actualObligation.getAttributeAssignment().set(0, actual);
		Runnable compare = switch (path) {
		case "direct" -> () -> Xacml20TestUtility.assertAttributeAssignment(expected, actual);
		case "response" -> () -> Xacml20TestUtility.assertResponse(
				response(obligations(expectedObligation)), response(obligations(actualObligation)));
		default -> throw new AssertionError(path);
		};
		compare.run();
		actual.getOtherAttributes().put(metadata, "changed");
		assertThrows(AssertionError.class, compare::run);
		actual.getOtherAttributes().clear();
		assertThrows(AssertionError.class, compare::run);
		actual.getOtherAttributes().put(new QName("urn:test:different", "name"), "first");
		assertThrows(AssertionError.class, compare::run);
	}

	@Test
	public void statusDetailsCompareDomFieldsRatherThanNodeIdentity() throws Exception
	{
		Element expected = DocumentBuilderFactory.newInstance().newDocumentBuilder()
				.newDocument().createElementNS("urn:test:detail", "detail");
		expected.setAttribute("AttributeId", "first");
		expected.setTextContent("value");
		Element equal = (Element) expected.cloneNode(true);
		Element different = (Element) expected.cloneNode(true);
		different.setAttribute("AttributeId", "changed");
		Xacml20TestUtility.assertStatus(statusWithDetail(expected), statusWithDetail(equal));
		assertThrows(AssertionError.class, () -> Xacml20TestUtility.assertStatus(
				statusWithDetail(expected), statusWithDetail(different)));
		assertThrows(AssertionError.class, () -> Xacml20TestUtility.assertStatus(statusWithDetail(expected), status()));
		assertThrows(AssertionError.class, () -> Xacml20TestUtility.assertStatus(status(), statusWithDetail(expected)));
	}

	@Test
	public void resourceIdAndDecisionStillMustMatch()
	{
		ResponseType expected = response(null);
		ResponseType actual = response(null);
		actual.getResult().get(0).setResourceId("different");
		assertThrows(AssertionError.class, () -> Xacml20TestUtility.assertResponse(expected, actual));
		actual.getResult().get(0).setResourceId("resource");
		actual.getResult().get(0).setDecision(DecisionType.DENY);
		assertThrows(AssertionError.class, () -> Xacml20TestUtility.assertResponse(expected, actual));
	}

	private static AttributeAssignmentType assignment(String value)
	{
		AttributeAssignmentType assignment = new AttributeAssignmentType();
		assignment.setAttributeId("attribute");
		assignment.setDataType(STRING);
		assignment.getContent().add(value);
		return assignment;
	}

	private static ObligationType obligation(String id, String value)
	{
		ObligationType obligation = new ObligationType();
		obligation.setObligationId(id);
		obligation.setFulfillOn(EffectType.PERMIT);
		obligation.getAttributeAssignment().add(assignment(value));
		return obligation;
	}

	private static ObligationsType obligations(ObligationType... values)
	{
		ObligationsType obligations = new ObligationsType();
		java.util.Collections.addAll(obligations.getObligation(), values);
		return obligations;
	}

	private static StatusType status()
	{
		StatusType status = new StatusType();
		StatusCodeType code = new StatusCodeType();
		code.setValue("urn:oasis:names:tc:xacml:1.0:status:ok");
		status.setStatusCode(code);
		return status;
	}

	private static StatusType statusWithDetail(Element element)
	{
		StatusType status = status();
		StatusDetailType detail = new StatusDetailType();
		detail.getAny().add(element);
		status.setStatusDetail(detail);
		return status;
	}

	private static ResponseType response(ObligationsType obligations)
	{
		ResultType result = new ResultType();
		result.setDecision(DecisionType.PERMIT);
		result.setResourceId("resource");
		result.setStatus(status());
		result.setObligations(obligations);
		ResponseType response = new ResponseType();
		response.getResult().add(result);
		return response;
	}
}
