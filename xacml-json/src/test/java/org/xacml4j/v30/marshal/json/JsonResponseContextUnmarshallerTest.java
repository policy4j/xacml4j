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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.Test;
import org.w3c.dom.Node;
import org.xacml4j.v30.*;
import org.xacml4j.v30.types.StringExp;
import org.xml.sax.InputSource;

import com.google.common.collect.ImmutableList;

public class JsonResponseContextUnmarshallerTest {

	@Test
	public void testUnmarshal() throws Exception {
		Reader input = new InputStreamReader(Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("xacml30-test-res01.json"));
		JsonResponseContextUnmarshaller unmarshaller = new JsonResponseContextUnmarshaller();
		ResponseContext response = unmarshaller.unmarshal(input);
		assertThat(response, is(createExpectedResponse()));
	}

	private ResponseContext createExpectedResponse() throws Exception {
		Result.Builder resultBuilder = Result
				.builder(Decision.PERMIT, Status.ok().message("alles kaput").build());
		resultBuilder
				.obligation(Obligation
						.builder("obligation1")
						.attributes(
								ImmutableList.<AttributeAssignment> of(
										AttributeAssignment.builder()
												.id(SubjectAttributes.SUBJECT_ID.toString())
												.category(Categories.ACTION)
												.issuer("Vytenai")
												.attribute(StringExp.of("obuolys"))
												.build(),
										AttributeAssignment.builder()
												.id(SubjectAttributes.KEY_INFO.toString())
												.category(Categories.ACTION)
												.issuer("ispanija")
												.attribute(StringExp.of("apelsinas"))
												.build()))
						.build());
		resultBuilder.obligation(Obligation
				.builder("obligation2")
				.attributes(
						ImmutableList.<AttributeAssignment> of(
								AttributeAssignment
										.builder()
										.id("custom:attribute1")
										.category("totaly:made:up:category-category1")
										.attribute(StringExp.of("same old apelsinas"))
						                .build()))
				.build());
		resultBuilder.advices(ImmutableList.of(
                Advice
                        .builder("advice1")
                        .attributes(
                                ImmutableList.<AttributeAssignment>of(
                                        AttributeAssignment
                                                .builder()
                                                .id("test:advice1")
                                                .attribute(StringExp.of("nespjauk i sulini"))
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
						ImmutableList.<Attribute> of(
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
		resultBuilder.includeInResultAttributes(ImmutableList.<Category>of(subjectAttributes));

		resultBuilder.evaluatedPolicies(ImmutableList.<IdReference.PolicyIdRef> of(
                IdReference.policyIdRef("policy1").version("1.0").build(),
                IdReference.policyIdRef("policy2").version("1.0").build()));
		resultBuilder.evaluatedPolicies(ImmutableList.<IdReference.PolicySetIdRef> of(
                IdReference.policySetIdRef("policySet3").version("1.1").build(),
                IdReference.policySetIdRef("policySet4").version("2.0").build()));

		return ResponseContext.builder().result(resultBuilder.build()).build();
	}

	private Node sampleContent1() throws Exception {
		DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

		return documentBuilder.parse(new InputSource(new StringReader(
				"<security>\n<through obscurity=\"true\"></through></security>")));
	}

}
