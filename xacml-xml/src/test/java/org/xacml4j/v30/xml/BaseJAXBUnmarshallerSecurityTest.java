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
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.bind.JAXBElement;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stax.StAXSource;
import javax.xml.transform.stream.StreamSource;

import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.w3c.dom.Document;
import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.RequestContext;
import org.xacml4j.v30.StatusCodeId;
import org.xacml4j.v30.SyntaxException;
import org.xacml4j.v30.policy.Apply;
import org.xacml4j.v30.policy.Policy;
import org.xacml4j.v30.types.StringVal;
import org.xacml4j.v30.types.XacmlTypes;
import org.xml.sax.InputSource;
import org.xml.sax.EntityResolver;
import org.xml.sax.XMLReader;

public class BaseJAXBUnmarshallerSecurityTest
{
	private static final String STRING = "http://www.w3.org/2001/XMLSchema#string";
	private static final String ATTRIBUTE_ID = "urn:test:entity";
	private static final String[] INPUT_FORMS = {"bytes", "stream", "input-source", "stream-source", "sax-source"};

	@TempDir
	Path directory;

	@ParameterizedTest(name = "{0}, {1}: external {2} entity")
	@MethodSource("externalEntities")
	public void rejectsExternalEntitiesBeforeAttemptingIo(Kind kind, String format, String entity)
	{
		String declaration = entity.equals("general")
				? "<!ENTITY xxe SYSTEM \"file:///nonexistent/x\">"
				: "<!ENTITY % d SYSTEM \"file:///nonexistent/d.dtd\"> %d;";
		String value = entity.equals("general") ? "&xxe;" : "x";
		SyntaxException failure = assertThrows(SyntaxException.class,
				() -> unmarshal(kind, source(format, document(kind, declaration, value))));
		assertSyntaxError(failure);
		for (Throwable cause = failure; cause != null; cause = cause.getCause()) {
			assertFalse(cause instanceof IOException, "External entities must be blocked before filesystem or network IO");
		}
	}

	@ParameterizedTest(name = "{0}, {1}: existing external entity is not consumed")
	@MethodSource("documentsAndInputForms")
	public void neverReturnsAnExternalEntityValue(Kind kind, String format) throws Exception
	{
		Path secret = directory.resolve("external-entity.txt");
		Files.writeString(secret, "external-test-sentinel", StandardCharsets.UTF_8);
		String declaration = "<!ENTITY xxe SYSTEM \"" + secret.toUri() + "\">";
		SyntaxException failure = assertThrows(SyntaxException.class,
				() -> unmarshal(kind, source(format, document(kind, declaration, "&xxe;"))));
		assertSyntaxError(failure);
	}

	@ParameterizedTest(name = "{0}, {1}: internal subset remains legal")
	@MethodSource("documentsAndInputForms")
	public void internalSubsetEntityRemainsSupported(Kind kind, String format) throws Exception
	{
		assertLexicalValue(unmarshal(kind, source(format, document(kind, "<!ENTITY a \"x\">", "&a;"))), "x");
	}

	@ParameterizedTest
	@EnumSource(Kind.class)
	public void internalEntityAlsoWorksThroughTheHistoricalJaxbEntryPoint(Kind kind) throws Exception
	{
		// a8a90d46 BaseJAXBUnmarshaller called Unmarshaller.unmarshal(InputStream) directly.
		String xml = document(kind, "<!ENTITY a \"x\">", "&a;");
		JAXBElement<?> parsed = (JAXBElement<?>) JAXBUtils.getInstance().createUnmarshaller()
				.unmarshal(bytes(xml));
		assertLexicalValue(unmarshal(kind, parsed), "x");
	}

	@ParameterizedTest
	@EnumSource(Kind.class)
	public void entityExpansionLimitRemainsASyntaxError(Kind kind)
	{
		StringBuilder declaration = new StringBuilder("<!ENTITY a0 \"x\">");
		for (int level = 1; level <= 9; level++) {
			declaration.append("<!ENTITY a").append(level).append(" \"");
			for (int repeat = 0; repeat < 10; repeat++) {
				declaration.append("&a").append(level - 1).append(";");
			}
			declaration.append("\">");
		}
		SyntaxException failure = assertThrows(SyntaxException.class,
				() -> unmarshal(kind, bytes(document(kind, declaration.toString(), "&a9;"))));
		assertSyntaxError(failure);
	}

	@ParameterizedTest
	@EnumSource(Kind.class)
	public void callerOwnedSaxReaderKeepsItsResolverAndParserPolicy(Kind kind) throws Exception
	{
		SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setNamespaceAware(true);
		XMLReader reader = factory.newSAXParser().getXMLReader();
		AtomicInteger calls = new AtomicInteger();
		EntityResolver resolver = (publicId, systemId) -> {
			assertEquals("urn:test:caller-owned-dtd", systemId);
			calls.incrementAndGet();
			return new InputSource(new StringReader("<!ENTITY a \"x\">"));
		};
		reader.setEntityResolver(resolver);
		String root = kind == Kind.REQUEST20 || kind == Kind.REQUEST30 ? "Request" : "Policy";
		String xml = document(kind, "", "&a;").replace("<!DOCTYPE " + root + " []>",
				"<!DOCTYPE " + root + " SYSTEM \"urn:test:caller-owned-dtd\">");
		assertLexicalValue(unmarshal(kind, new SAXSource(reader, new InputSource(new StringReader(xml)))), "x");
		assertEquals(1, calls.get(), "The caller's resolver must receive the DTD exactly once");
		assertSame(resolver, reader.getEntityResolver());
	}

	@ParameterizedTest
	@ValueSource(strings = {"true", "1", " true "})
	public void callerOwnedStaxEventReaderKeepsBooleanValidation(String lexical) throws Exception
	{
		String xml = document(Kind.REQUEST30, "<!ENTITY a \"x\">", "&a;")
				.replace("ReturnPolicyIdList=\"false\"", "ReturnPolicyIdList=\"" + lexical + "\"");
		StAXSource source = new StAXSource(XMLInputFactory.newFactory().createXMLEventReader(new StringReader(xml)));
		RequestContext request = (RequestContext) unmarshal(Kind.REQUEST30, source);
		assertEquals(true, request.isReturnPolicyIdList());
		assertLexicalValue(request, "x");
	}

	@ParameterizedTest
	@ValueSource(strings = {"yes", " \t\n\r "})
	public void callerOwnedStaxEventReaderRejectsInvalidBooleanAttributes(String lexical) throws Exception
	{
		String xml = document(Kind.REQUEST30, "", "x")
				.replace("ReturnPolicyIdList=\"false\"", "ReturnPolicyIdList=\"" + lexical + "\"");
		StAXSource source = new StAXSource(XMLInputFactory.newFactory().createXMLEventReader(new StringReader(xml)));
		SyntaxException failure = assertThrows(SyntaxException.class, () -> unmarshal(Kind.REQUEST30, source));
		assertSyntaxError(failure);
		assertEquals("Invalid xs:boolean attribute ReturnPolicyIdList", failure.getMessage());
	}

	@ParameterizedTest
	@ValueSource(strings = {"yes", "TRUE", "10", "", " \t\n\r "})
	public void programmaticDomAttributesStillUseStrictBooleanParsing(String lexical) throws Exception
	{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		Document parsed = factory.newDocumentBuilder().parse(bytes(document(Kind.REQUEST30, "", "x")));
		// DOM setAttribute creates an unqualified attribute whose getLocalName() is null.
		parsed.getDocumentElement().removeAttribute("ReturnPolicyIdList");
		parsed.getDocumentElement().setAttribute("ReturnPolicyIdList", lexical);
		SyntaxException failure = assertThrows(SyntaxException.class,
				() -> unmarshal(Kind.REQUEST30, new DOMSource(parsed)));
		assertSyntaxError(failure);
		assertEquals("Invalid xs:boolean attribute ReturnPolicyIdList", failure.getMessage());
	}

	private static Stream<Arguments> externalEntities()
	{
		return Stream.of(Kind.values()).flatMap(kind -> Stream.of(INPUT_FORMS)
				.flatMap(format -> Stream.of("general", "parameter").map(entity -> Arguments.of(kind, format, entity))));
	}

	private static Stream<Arguments> documentsAndInputForms()
	{
		return Stream.of(Kind.values()).flatMap(kind -> Stream.of(INPUT_FORMS)
				.map(format -> Arguments.of(kind, format)));
	}

	private static void assertSyntaxError(SyntaxException failure)
	{
		assertEquals(StatusCodeId.SYNTAX_ERROR, failure.getStatus().getStatusCode().getValue());
	}

	private static Object unmarshal(Kind kind, Object source) throws Exception
	{
		return switch (kind) {
			case REQUEST20 -> new Xacml20RequestContextUnmarshaller().unmarshal(source);
			case REQUEST30 -> new Xacml30RequestContextUnmarshaller().unmarshal(source);
			case POLICY20, POLICY30 -> new XacmlPolicyUnmarshaller(true).unmarshal(source);
		};
	}

	private static void assertLexicalValue(Object result, String expected)
	{
		StringVal value = result instanceof RequestContext request
				? (StringVal) request.getAttributeValues(CategoryId.RESOURCE, ATTRIBUTE_ID, XacmlTypes.STRING, null)
						.iterator().next()
				: (StringVal) ((Apply) ((Policy) result).getRules().get(0).getCondition().getExpression())
						.getArguments().get(0);
		assertEquals(expected, value.get());
	}

	private static String document(Kind kind, String declaration, String value)
	{
		String namespace = kind == Kind.REQUEST30 || kind == Kind.POLICY30
				? "urn:oasis:names:tc:xacml:3.0:core:schema:wd-17"
				: kind == Kind.REQUEST20 ? "urn:oasis:names:tc:xacml:2.0:context:schema:os"
						: "urn:oasis:names:tc:xacml:2.0:policy:schema:os";
		String attributeValue = "<AttributeValue DataType=\"" + STRING + "\">" + value + "</AttributeValue>";
		String body = switch (kind) {
			case REQUEST20 -> "<Request xmlns=\"" + namespace + "\"><Subject/><Resource>"
					+ "<Attribute AttributeId=\"" + ATTRIBUTE_ID + "\" DataType=\"" + STRING + "\">"
					+ "<AttributeValue>" + value + "</AttributeValue></Attribute></Resource><Action/><Environment/></Request>";
			case REQUEST30 -> "<Request xmlns=\"" + namespace + "\" ReturnPolicyIdList=\"false\" CombinedDecision=\"false\">"
					+ "<Attributes Category=\"" + CategoryId.RESOURCE.getId() + "\"><Attribute AttributeId=\"" + ATTRIBUTE_ID
					+ "\" IncludeInResult=\"false\">" + attributeValue + "</Attribute></Attributes></Request>";
			case POLICY20, POLICY30 -> "<Policy xmlns=\"" + namespace + "\" PolicyId=\"urn:test:entity-policy\" Version=\"1.0\""
					+ " RuleCombiningAlgId=\"urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:deny-overrides\">"
					+ "<Target/><Rule RuleId=\"urn:test:entity-rule\" Effect=\"Permit\"><Condition>"
					+ "<Apply FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:string-equal\">" + attributeValue
					+ "<AttributeValue DataType=\"" + STRING + "\">x</AttributeValue>"
					+ "</Apply></Condition></Rule></Policy>";
		};
		String root = kind == Kind.REQUEST20 || kind == Kind.REQUEST30 ? "Request" : "Policy";
		return "<?xml version=\"1.0\"?><!DOCTYPE " + root + " [" + declaration + "]>" + body;
	}

	private static Object source(String format, String xml)
	{
		return switch (format) {
			case "bytes" -> xml.getBytes(StandardCharsets.UTF_8);
			case "stream" -> bytes(xml);
			case "input-source" -> new InputSource(new StringReader(xml));
			case "stream-source" -> new StreamSource(new StringReader(xml));
			case "sax-source" -> new SAXSource(new InputSource(new StringReader(xml)));
			default -> throw new IllegalArgumentException(format);
		};
	}

	private static ByteArrayInputStream bytes(String xml)
	{
		return new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
	}

	enum Kind { REQUEST20, REQUEST30, POLICY20, POLICY30 }
}
