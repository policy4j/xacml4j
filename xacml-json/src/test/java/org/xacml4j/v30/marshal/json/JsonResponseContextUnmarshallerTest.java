package org.xacml4j.v30.marshal.json;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.InputStreamReader;
import java.io.Reader;

import org.junit.Test;
import org.xacml4j.v30.Advice;
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

	private ResponseContext createExpectedResponse() {
		Result.Builder resultBuilder = Result
				.builder(Decision.PERMIT, new Status(StatusCode.createOk(), "alles kaput"));
		resultBuilder
				.obligation(Obligation
						.builder("obligation1")
						.attributes(
								ImmutableList.<AttributeAssignment> of(new AttributeAssignment(
										SubjectAttributes.SUBJECT_ID.toString(), AttributeCategories.ACTION, "Vytenai",
										StringType.STRING.create("obuolys")), new AttributeAssignment(
										SubjectAttributes.KEY_INFO.toString(), AttributeCategories.ACTION, "ispanija",
										StringType.STRING.create("apelsinas")))).build());
		resultBuilder.obligation(Obligation
				.builder("obligation2")
				.attributes(
						ImmutableList.<AttributeAssignment> of(new AttributeAssignment("custom:attribute1",
								AttributeCategories.parse("totaly:made:up:attribute-category1"), null,
								StringType.STRING.create("same old apelsinas")))).build());
		resultBuilder.advice(ImmutableList.of(
				Advice.builder("advice1")
						.attributes(
								ImmutableList.<AttributeAssignment> of(new AttributeAssignment("test:advice1",
										StringType.STRING.create("nespjauk i sulini")))).create(),
				Advice.builder("advice2").create()));

		resultBuilder.includeInResultAttr(ImmutableList.<Attributes> of());

		resultBuilder.evaluatedPolicies(ImmutableList.<PolicyIDReference> of(PolicyIDReference.builder("policy1")
				.version("1.0").earliest("0.5").latest("1.5").build(), PolicyIDReference.builder("policy2").build()));
		resultBuilder.evaluatedPolicies(ImmutableList.<PolicySetIDReference> of(
				PolicySetIDReference.builder("policySet3").version("1.1").earliest("1.0").latest("1.9").build(),
				PolicySetIDReference.builder("policySet4").version("2.0").build()));

		return ResponseContext.builder().result(resultBuilder.build()).build();
	}

}
