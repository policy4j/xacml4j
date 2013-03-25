package org.xacml4j.v30.marshal.json;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.InputStreamReader;
import java.io.Reader;

import org.junit.Test;
import org.xacml4j.v30.Decision;
import org.xacml4j.v30.ResponseContext;
import org.xacml4j.v30.Result;

public class JsonResponseContextUnmarshallerTest {

	@Test
	public void testUnmarshal() throws Exception {
		Reader input = new InputStreamReader(Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("xacml30-response.json"));
		JsonResponseContextUnmarshaller unmarshaller = new JsonResponseContextUnmarshaller();
		ResponseContext response = unmarshaller.unmarshal(input);
		assertThat(response, is(createExpectedResponse()));
	}

	private ResponseContext createExpectedResponse() {
		return ResponseContext.builder().result(Result.createOk(Decision.PERMIT).build())
				.build();
	}

}
