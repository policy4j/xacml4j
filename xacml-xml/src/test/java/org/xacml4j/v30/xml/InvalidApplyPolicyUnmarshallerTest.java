package org.xacml4j.v30.xml;

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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.StatusCodeId;
import org.xacml4j.v30.SyntaxException;
import org.xacml4j.v30.policy.Apply;
import org.xacml4j.v30.policy.Policy;
import org.xacml4j.v30.types.XacmlTypes;

/**
 * Bead policy4j-the.4.56, through the XML mappers of XACML 2.0 and 3.0: a
 * policy with an {@code <Apply>} whose arguments do not fit the function
 * is loaded. The expression is marked, and its evaluation is
 * Indeterminate with status processing-error (XACML 3.0 core, section
 * 7.19.2). The conformance tests IIC003 and IIC014 need such a policy to
 * load. A function that the PDP does not have is still rejected when the
 * policy is loaded.
 */
public class InvalidApplyPolicyUnmarshallerTest
{
	private static final String NAMESPACE20 = "urn:oasis:names:tc:xacml:2.0:policy:schema:os";
	private static final String NAMESPACE30 = "urn:oasis:names:tc:xacml:3.0:core:schema:wd-17";
	private static final String F1 = "urn:oasis:names:tc:xacml:1.0:function:";
	private static final String INTEGER = "http://www.w3.org/2001/XMLSchema#integer";
	private static final String STRING = "http://www.w3.org/2001/XMLSchema#string";

	@ParameterizedTest(name = "XACML {0}: {1}")
	@CsvSource({
			"20, integer-add with one argument, integer-add, 5,",
			"30, integer-add with one argument, integer-add, 5,",
			"20, integer-add with a string, integer-add, 5, a",
			"30, integer-add with a string, integer-add, 5, a",
	})
	public void policyWithAnApplyThatIsNotTypeCorrectIsLoadedAndIndeterminate(int version, String name,
			String function, String first, String second) throws Exception
	{
		String inner = "<Apply FunctionId=\"" + F1 + function + "\">" + value(INTEGER, first)
				+ (second == null ? "" : value(STRING, second)) + "</Apply>";

		Policy policy = (Policy) new XacmlPolicyUnmarshaller(true).unmarshal(bytes(policy(version,
				"<Apply FunctionId=\"" + F1 + "integer-equal\">" + inner + value(INTEGER, "5") + "</Apply>")));

		Apply condition = (Apply) policy.getRules().get(0).getCondition().getExpression();
		// The condition holds the wrong expression, so it has a type error too.
		assertFalse(condition.isTypeCorrect());
		assertFalse(((Apply) condition.getArguments().get(0)).isTypeCorrect());
		EvaluationException e = assertThrows(EvaluationException.class,
				() -> condition.evaluate(mock(EvaluationContext.class)));
		assertEquals(StatusCodeId.STATUS_PROCESSING_ERROR, e.getStatus().getStatusCode().getValue());
	}

	@ParameterizedTest(name = "XACML {0}")
	@CsvSource({"20", "30"})
	public void policyWithATypeCorrectApplyIsEvaluated(int version) throws Exception
	{
		String inner = "<Apply FunctionId=\"" + F1 + "integer-add\">" + value(INTEGER, "2")
				+ value(INTEGER, "3") + "</Apply>";

		Policy policy = (Policy) new XacmlPolicyUnmarshaller(true).unmarshal(bytes(policy(version,
				"<Apply FunctionId=\"" + F1 + "integer-equal\">" + inner + value(INTEGER, "5") + "</Apply>")));

		Apply condition = (Apply) policy.getRules().get(0).getCondition().getExpression();
		assertTrue(condition.isTypeCorrect());
		assertTrue(((Apply) condition.getArguments().get(0)).isTypeCorrect());
		assertEquals(XacmlTypes.BOOLEAN.ofAny(true), condition.evaluate(mock(EvaluationContext.class)));
	}

	@ParameterizedTest(name = "XACML {0}")
	@CsvSource({"20", "30"})
	public void policyWithAnUnknownFunctionIsRejected(int version)
	{
		String xml = policy(version, "<Apply FunctionId=\"urn:test:no-such-function\">"
				+ value(INTEGER, "5") + "</Apply>");

		assertThrows(SyntaxException.class, () -> new XacmlPolicyUnmarshaller(true).unmarshal(bytes(xml)));
	}

	private static String value(String dataType, String text)
	{
		return "<AttributeValue DataType=\"" + dataType + "\">" + text + "</AttributeValue>";
	}

	private static String policy(int version, String condition)
	{
		return "<Policy xmlns=\"" + (version == 20 ? NAMESPACE20 : NAMESPACE30)
				+ "\" PolicyId=\"urn:test:invalid-apply-policy\" Version=\"1.0\""
				+ " RuleCombiningAlgId=\"urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:deny-overrides\">"
				+ "<Target/><Rule RuleId=\"urn:test:rule\" Effect=\"Permit\"><Condition>" + condition
				+ "</Condition></Rule></Policy>";
	}

	private static ByteArrayInputStream bytes(String xml)
	{
		return new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
	}
}
