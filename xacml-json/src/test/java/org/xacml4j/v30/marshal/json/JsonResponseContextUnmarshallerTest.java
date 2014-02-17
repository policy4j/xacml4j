package org.xacml4j.v30.marshal.json;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.Test;
import org.w3c.dom.Node;
import org.xacml4j.v30.Advice;
import org.xacml4j.v30.Attribute;
import org.xacml4j.v30.AttributeAssignment;
import org.xacml4j.v30.AttributeCategories;
import org.xacml4j.v30.Attributes;
import org.xacml4j.v30.Decision;
import org.xacml4j.v30.Obligation;
import org.xacml4j.v30.ResponseContext;
import org.xacml4j.v30.Result;
import org.xacml4j.v30.Status;
import org.xacml4j.v30.StatusCode;
import org.xacml4j.v30.SubjectAttributes;
import org.xacml4j.v30.pdp.PolicyIDReference;
import org.xacml4j.v30.pdp.PolicySetIDReference;
import org.xacml4j.v30.types.StringType;
import org.xacml4j.v30.types.Types;
import org.xml.sax.InputSource;

import com.google.common.collect.ImmutableList;

public class JsonResponseContextUnmarshallerTest {

	@Test
	public void testUnmarshal() throws Exception {
		Reader input = new InputStreamReader(Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("xacml30-response.json"));
		Types types = Types.builder().defaultTypes().create();
		JsonResponseContextUnmarshaller unmarshaller = new JsonResponseContextUnmarshaller(types);
		ResponseContext response = unmarshaller.unmarshal(input);
		assertThat(response, is(createExpectedResponse()));
	}

	private ResponseContext createExpectedResponse() throws Exception {
		Result.Builder resultBuilder = Result
				.builder(Decision.PERMIT, new Status(StatusCode.createOk(), "alles kaput"));
		resultBuilder
				.obligation(Obligation
						.builder("obligation1")
						.attributes(
								ImmutableList.<AttributeAssignment> of(
										AttributeAssignment.builder()
												.id(SubjectAttributes.SUBJECT_ID.toString())
												.category(AttributeCategories.ACTION)
												.issuer("Vytenai")
												.value(StringType.STRING.create("obuolys"))
												.build(),
										AttributeAssignment.builder()
												.id(SubjectAttributes.KEY_INFO.toString())
												.category(AttributeCategories.ACTION)
												.issuer("ispanija")
												.value(StringType.STRING.create("apelsinas"))
												.build()))
						.build());
		resultBuilder.obligation(Obligation
				.builder("obligation2")
				.attributes(
						ImmutableList.<AttributeAssignment> of(
								AttributeAssignment
										.builder()
										.id("custom:attribute1")
										.category("totaly:made:up:attribute-category1")
										.value(StringType.STRING.create("same old apelsinas"))
						                .build()))
				.build());
		resultBuilder.advice(ImmutableList.of(
				Advice
					.builder("advice1")
					.attributes(
							ImmutableList.<AttributeAssignment> of(
								AttributeAssignment
										.builder()
										.id("test:advice1")
										.value(StringType.STRING.create("nespjauk i sulini"))
										.build()))
					.build(),
				Advice.builder("advice2").build()));

		Attributes subjectAttributes = Attributes
				.builder(AttributeCategories.SUBJECT_ACCESS)
				.id("SubjectAttributes")
				.content(sampleContent1())
				.attributes(
						ImmutableList.<Attribute> of(
								Attribute
										.builder(SubjectAttributes.SUBJECT_ID.toString())
										.includeInResult(false)
										.issuer("testIssuer")
										.value(StringType.STRING.create(
												"VFZTAQEAABRcZ03t-NNkK__rcIbvgKcK6e5oHBD5fD0qkdPIuqviWHzzFVR6AAAAgFl8GkUGZQG8TPXg9T6cQCoMO3a_sV1FR8pJC4BPfXfXlOvWDPUt4pr0cBkGTeaSU9RjSvEiXF-kTq5GFPkBHXcYnBW7eNjhq2EB_RWHh7_0sWqY32yb4fxlPLOsh5cUR4WbYZJE-zNuVzudco5cOjHU6Zwlr2HACpHW5siAVKfW"))
										.build(),
								Attribute.builder(SubjectAttributes.SUBJECT_ID_QUALIFIER.toString())
										.includeInResult(false).issuer("testIssuer")
										.value(StringType.STRING.create("TestDomain")).build())).build();
		resultBuilder.includeInResultAttr(ImmutableList.<Attributes> of(subjectAttributes));

		resultBuilder.evaluatedPolicies(ImmutableList.<PolicyIDReference> of(PolicyIDReference.builder("policy1")
				.versionAsString("1.0").earliest("0.5").latest("1.5").build(), PolicyIDReference.builder("policy2").build()));
		resultBuilder.evaluatedPolicies(ImmutableList.<PolicySetIDReference> of(
				PolicySetIDReference.builder("policySet3").versionAsString("1.1").earliest("1.0").latest("1.9").build(),
				PolicySetIDReference.builder("policySet4").versionAsString("2.0").build()));

		return ResponseContext.builder().result(resultBuilder.build()).build();
	}

	private Node sampleContent1() throws Exception {
		DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

		return documentBuilder.parse(new InputSource(new StringReader(
				"<security>\n<through obscurity=\"true\"></through></security>")));
	}

}
