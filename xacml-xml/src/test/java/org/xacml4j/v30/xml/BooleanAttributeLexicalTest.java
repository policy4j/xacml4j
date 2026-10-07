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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stax.StAXSource;
import javax.xml.transform.stream.StreamSource;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.w3c.dom.Document;
import org.xacml4j.v30.RequestContext;
import org.xacml4j.v30.StatusCodeId;
import org.xacml4j.v30.SyntaxException;
import org.xacml4j.v30.policy.Apply;
import org.xacml4j.v30.policy.AttributeReference;
import org.xacml4j.v30.policy.Policy;
import org.xml.sax.InputSource;

public class BooleanAttributeLexicalTest
{
	private static final String NAMESPACE30 = "urn:oasis:names:tc:xacml:3.0:core:schema:wd-17";
	private static final String BOOLEAN = "http://www.w3.org/2001/XMLSchema#boolean";
	private static final String ONE_AND_ONLY = "urn:oasis:names:tc:xacml:1.0:function:boolean-one-and-only";
	private static final String[] LOCATIONS = {"ReturnPolicyIdList", "CombinedDecision", "IncludeInResult",
			"20-designator", "20-selector", "30-designator", "30-selector"};

	// XML Schema Part 2 sections 3.2.2 and 4.3.6: exactly true/false/1/0, after XML whitespace collapse.
	@ParameterizedTest(name = "{0} accepts [{1}] as {2}")
	@MethodSource("validAttributes")
	public void acceptsValidLexicalForms(String location, String lexical, boolean expected) throws Exception
	{
		assertEquals(expected, parse(location, lexical));
	}

	@ParameterizedTest(name = "{0} rejects [{1}]")
	@MethodSource("invalidAttributes")
	public void rejectsInvalidLexicalFormsWithSyntaxError(String location, String lexical)
	{
		SyntaxException failure = assertThrows(SyntaxException.class, () -> parse(location, lexical));
		assertEquals(StatusCodeId.SYNTAX_ERROR, failure.getStatus().getStatusCode().getValue());
		String attribute = location.contains("-") ? "MustBePresent" : location;
		assertTrue(failure.getMessage().contains(attribute), failure::getMessage);
	}

	@ParameterizedTest
	@ValueSource(strings = {"input-source", "stream-source", "sax-source", "dom-source", "stax-source",
			"stream-reader", "document", "element", "bytes"})
	public void rejectsInvalidAttributesForEveryXmlInputForm(String format) throws Exception
	{
		Object source = source(format, request("ReturnPolicyIdList", "yes"));
		SyntaxException failure = assertThrows(SyntaxException.class,
				() -> new Xacml30RequestContextUnmarshaller().unmarshal(source));
		assertEquals(StatusCodeId.SYNTAX_ERROR, failure.getStatus().getStatusCode().getValue());
		assertTrue(failure.getMessage().contains("ReturnPolicyIdList"), failure::getMessage);
	}

	@ParameterizedTest
	@ValueSource(strings = {"input-source", "stream-source", "sax-source", "dom-source", "stax-source",
			"stream-reader", "document", "element", "bytes"})
	public void preservesValidAttributesForEveryXmlInputForm(String format) throws Exception
	{
		RequestContext result = new Xacml30RequestContextUnmarshaller().unmarshal(source(format,
				request("ReturnPolicyIdList", " true ")));
		assertTrue(result.isReturnPolicyIdList());
	}

	@ParameterizedTest
	@ValueSource(ints = {20, 30})
	public void missingDesignatorAttributeIdIsAnIdentifiedSyntaxError(int version)
	{
		String xml = policy(version, false, "false").replace(" AttributeId=\"urn:test:boolean\"", "");
		SyntaxException failure = assertThrows(SyntaxException.class,
				() -> new XacmlPolicyUnmarshaller(true).unmarshal(bytes(xml)));
		assertEquals(StatusCodeId.SYNTAX_ERROR, failure.getStatus().getStatusCode().getValue());
		assertEquals("Policy AttributeDesignator is missing AttributeId", failure.getMessage());
	}

	@ParameterizedTest
	@ValueSource(strings = {"20-designator", "20-selector"})
	public void optionalXacml20MustBePresentKeepsItsFalseDefault(String location) throws Exception
	{
		String xml = policy(20, location.endsWith("selector"), "false").replace(" MustBePresent=\"false\"", "");
		Policy result = (Policy) new XacmlPolicyUnmarshaller(true).unmarshal(bytes(xml));
		assertEquals(false, reference(result).isMustBePresent());
	}

	private static Stream<Arguments> validAttributes()
	{
		return Stream.of(LOCATIONS).flatMap(location -> Stream.of("true", "false", "1", "0", " true ")
				.map(lexical -> Arguments.of(location, lexical, "true".equals(lexical.trim()) || "1".equals(lexical))));
	}

	private static Stream<Arguments> invalidAttributes()
	{
		return Stream.of(LOCATIONS).flatMap(location -> Stream.of("yes", "TRUE", "10", "")
				.map(lexical -> Arguments.of(location, lexical)));
	}

	private static boolean parse(String location, String lexical) throws Exception
	{
		if (location.contains("-")) {
			Policy result = (Policy) new XacmlPolicyUnmarshaller(true).unmarshal(bytes(
					policy(Integer.parseInt(location.substring(0, 2)), location.endsWith("selector"), lexical)));
			return reference(result).isMustBePresent();
		}
		RequestContext result = new Xacml30RequestContextUnmarshaller().unmarshal(bytes(request(location, lexical)));
		return switch (location) {
			case "ReturnPolicyIdList" -> result.isReturnPolicyIdList();
			case "CombinedDecision" -> result.isCombinedDecision();
			case "IncludeInResult" -> !result.getIncludeInResultAttributes().isEmpty();
			default -> throw new IllegalArgumentException(location);
		};
	}

	private static AttributeReference reference(Policy policy)
	{
		return (AttributeReference) ((Apply) policy.getRules().get(0).getCondition().getExpression()).getArguments().get(0);
	}

	private static String request(String attribute, String lexical)
	{
		return ("<Request xmlns=\"" + NAMESPACE30 + "\" ReturnPolicyIdList=\"false\" CombinedDecision=\"false\">"
				+ "<Attributes Category=\"urn:oasis:names:tc:xacml:3.0:attribute-category:resource\">"
				+ "<Attribute AttributeId=\"urn:test:boolean\" IncludeInResult=\"false\">"
				+ "<AttributeValue DataType=\"" + BOOLEAN + "\">true</AttributeValue>"
				+ "</Attribute></Attributes></Request>").replace(attribute + "=\"false\"", attribute + "=\"" + lexical + "\"");
	}

	private static String policy(int version, boolean selector, String lexical)
	{
		String reference = selector
				? "<AttributeSelector " + (version == 20 ? "RequestContextPath=\"//test:flag/text()\""
						: "Category=\"urn:oasis:names:tc:xacml:3.0:attribute-category:resource\" Path=\"/test:flag/text()\"")
				: version == 20 ? "<SubjectAttributeDesignator AttributeId=\"urn:test:boolean\""
						: "<AttributeDesignator Category=\"urn:oasis:names:tc:xacml:1.0:subject-category:access-subject\""
								+ " AttributeId=\"urn:test:boolean\"";
		reference += " DataType=\"" + BOOLEAN + "\" MustBePresent=\"" + lexical + "\"/>";
		String condition = "<Condition><Apply FunctionId=\"" + ONE_AND_ONLY + "\">" + reference + "</Apply></Condition>";
		return "<Policy xmlns=\"" + (version == 20 ? "urn:oasis:names:tc:xacml:2.0:policy:schema:os" : NAMESPACE30)
				+ "\" xmlns:test=\"urn:test:content\" PolicyId=\"urn:test:boolean-policy\" Version=\"1.0\""
				+ " RuleCombiningAlgId=\"urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:deny-overrides\">"
				+ "<Target/><Rule RuleId=\"urn:test:rule\" Effect=\"Permit\">" + condition + "</Rule></Policy>";
	}

	private static Object source(String format, String xml) throws Exception
	{
		return switch (format) {
			case "input-source" -> new InputSource(new StringReader(xml));
			case "stream-source" -> new StreamSource(new StringReader(xml));
			case "sax-source" -> new SAXSource(new InputSource(new StringReader(xml)));
			case "stax-source" -> new StAXSource(XMLInputFactory.newFactory().createXMLStreamReader(new StringReader(xml)));
			case "stream-reader" -> XMLInputFactory.newFactory().createXMLStreamReader(new StringReader(xml));
			case "dom-source" -> new DOMSource(document(xml));
			case "document" -> document(xml);
			case "element" -> document(xml).getDocumentElement();
			case "bytes" -> xml.getBytes(StandardCharsets.UTF_8);
			default -> throw new IllegalArgumentException(format);
		};
	}

	private static Document document(String xml) throws Exception
	{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		return factory.newDocumentBuilder().parse(bytes(xml));
	}

	private static ByteArrayInputStream bytes(String xml)
	{
		return new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
	}
}
