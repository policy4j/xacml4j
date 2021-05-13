package org.xacml4j.v30.marshal.json;

/*
 * #%L
 * Xacml4J Gson Integration
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

import static org.junit.Assert.assertEquals;

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Node;
import org.xacml4j.v30.Advice;
import org.xacml4j.v30.Attribute;
import org.xacml4j.v30.AttributeAssignment;
import org.xacml4j.v30.Categories;
import org.xacml4j.v30.Category;
import org.xacml4j.v30.Decision;
import org.xacml4j.v30.Entity;
import org.xacml4j.v30.Obligation;
import org.xacml4j.v30.ResponseContext;
import org.xacml4j.v30.Result;
import org.xacml4j.v30.Status;
import org.xacml4j.v30.SubjectAttributes;
import org.xacml4j.v30.marshal.Marshaller;
import org.xacml4j.v30.marshal.Unmarshaller;
import org.xacml4j.v30.pdp.PolicyIDReference;
import org.xacml4j.v30.pdp.PolicySetIDReference;
import org.xacml4j.v30.types.StringExp;
import org.xml.sax.InputSource;

import com.google.common.collect.ImmutableList;

public class JsonResponseContextMarshallerTest {

	private Unmarshaller<ResponseContext> unmarshaller;
	private Marshaller<ResponseContext> marshaller;

	@Before
	public void setUp() {
		marshaller = new JsonResponseContextMarshaller();
		unmarshaller = new JsonResponseContextUnmarshaller();
	}

	@Test
	public void testMarshal() throws Exception {
		ResponseContext reqIn = createTestResponse();
		Object o = marshaller.marshal(reqIn);
		ResponseContext reqOut = unmarshaller.unmarshal(o);
		assertEquals(reqIn, reqOut);
	}

	private ResponseContext createTestResponse() throws Exception {
		Result.Builder resultBuilder = Result
				.builder(Decision.PERMIT, Status.ok().message("alles kaput").build());
		resultBuilder
				.obligation(Obligation
						.builder("obligation1")
						.attributes(
								ImmutableList.of(
										AttributeAssignment
												.builder()
												.id(SubjectAttributes.SUBJECT_ID.toString())
												.category(Categories.ACTION)
												.issuer("Vytenai")
												.value(StringExp.of("obuolys"))
												.build(),
										AttributeAssignment
												.builder()
												.id(SubjectAttributes.KEY_INFO.toString())
												.category(Categories.ACTION)
												.issuer("ispanija")
												.value(StringExp.of("apelsinas"))
												.build()))
						.build());
		resultBuilder.obligation(Obligation
				.builder("obligation2")
				.attributes(
						ImmutableList.of(
								AttributeAssignment
										.builder()
										.id("custom:attribute1")
										.category(Categories.parse("totaly:made:up:attribute-category1"))
										.value(StringExp.of("same old apelsinas"))
										.build()))
				.build());
		resultBuilder.advice(ImmutableList.of(
				Advice.builder("advice1")
				      .attributes(
						      ImmutableList.of(
								      AttributeAssignment
										      .builder()
										      .id("test:advice1")
										      .value(StringExp.of("nespjauk i sulini"))
										      .build()))
				      .build(),
				Advice.builder("advice2").build()));

		Category subjectAttributes = Category
				.builder(Categories.SUBJECT_ACCESS)
				.id("SubjectAttributes")
				.entity(Entity
						.builder()
						.content(sampleContent1())
						.attributes(
						ImmutableList.of(
								Attribute
										.builder(SubjectAttributes.SUBJECT_ID.toString())
										.includeInResult(false)
										.issuer("testIssuer")
										.value(StringExp.of(
												"VFZTAQEAABRcZ03t-NNkK__rcIbvgKcK6e5oHBD5fD0qkdPIuqviWHzzFVR6AAAAgFl8GkUGZQG8TPXg9T6cQCoMO3a_sV1FR8pJC4BPfXfXlOvWDPUt4pr0cBkGTeaSU9RjSvEiXF-kTq5GFPkBHXcYnBW7eNjhq2EB_RWHh7_0sWqY32yb4fxlPLOsh5cUR4WbYZJE-zNuVzudco5cOjHU6Zwlr2HACpHW5siAVKfW"))
										.build(),
								Attribute.builder(SubjectAttributes.SUBJECT_ID_QUALIFIER.toString())
								         .includeInResult(false).issuer("testIssuer")
								         .value(StringExp.of("TestDomain")).build())).build())
						.build();
		resultBuilder.includeInResultAttr(ImmutableList.of(subjectAttributes));

		resultBuilder.evaluatedPolicies(ImmutableList.of(
				PolicyIDReference.builder().id("policy1").versionAsString("1.0").build(),
				PolicyIDReference.builder().id("policy2").versionAsString("1.0").build()));
		resultBuilder.evaluatedPolicies(ImmutableList.of(
				PolicySetIDReference.builder().id("policySet3").versionAsString("1.1").build(),
				PolicySetIDReference.builder().id("policySet4").versionAsString("2.0").build()));

		return ResponseContext.builder().result(resultBuilder.build()).build();
	}

	private Node sampleContent1() throws Exception {
		DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

		return documentBuilder.parse(new InputSource(new StringReader(
				"<security>\n<through obscurity=\"true\"></through></security>")));
	}

}
