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
										StringType.STRING.create("obuolys")))).build());
		resultBuilder.obligation(Obligation.builder("obligation2").attributes(ImmutableList.<AttributeAssignment> of())
				.build());
		resultBuilder.advice(ImmutableList.of(
				Advice.builder("advice1").attributes(ImmutableList.<AttributeAssignment> of()).create(), Advice
						.builder("advice2").attributes(ImmutableList.<AttributeAssignment> of()).create()));

		resultBuilder.includeInResultAttr(ImmutableList.<Attributes> of());

		return ResponseContext.builder().result(resultBuilder.build()).build();
	}

}
