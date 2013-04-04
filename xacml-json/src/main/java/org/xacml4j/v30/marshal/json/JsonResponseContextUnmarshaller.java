package org.xacml4j.v30.marshal.json;

import java.io.IOException;
import java.io.Reader;

import org.xacml4j.v30.Advice;
import org.xacml4j.v30.Attribute;
import org.xacml4j.v30.AttributeAssignment;
import org.xacml4j.v30.AttributeExp;
import org.xacml4j.v30.Attributes;
import org.xacml4j.v30.Obligation;
import org.xacml4j.v30.ResponseContext;
import org.xacml4j.v30.Result;
import org.xacml4j.v30.Status;
import org.xacml4j.v30.StatusCode;
import org.xacml4j.v30.XacmlSyntaxException;
import org.xacml4j.v30.marshall.Unmarshaller;
import org.xacml4j.v30.pdp.PolicyIDReference;
import org.xacml4j.v30.pdp.PolicySetIDReference;
import org.xacml4j.v30.types.Types;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

public class JsonResponseContextUnmarshaller implements Unmarshaller<ResponseContext> {
	private final Gson json;

	public JsonResponseContextUnmarshaller(Types typesRegistry) {
		json = new GsonBuilder().registerTypeAdapter(ResponseContext.class, new ResponseContextAdapter())
				.registerTypeAdapter(Result.class, new ResultAdapter())
				.registerTypeAdapter(Status.class, new StatusAdapter())
				.registerTypeAdapter(StatusCode.class, new StatusCodeAdapter())
				.registerTypeAdapter(Obligation.class, new ObligationOrAdviceAdapter())
				.registerTypeAdapter(AttributeAssignment.class, new AttributeAssignmentDeserializer(typesRegistry))
				.registerTypeAdapter(Advice.class, new ObligationOrAdviceAdapter())
				.registerTypeAdapter(Attributes.class, new AttributesAdapter())
				.registerTypeAdapter(Attribute.class, new AttributeDeserializer(typesRegistry))
				.registerTypeAdapter(AttributeExp.class, new AttributeExpDeserializer(typesRegistry))
				.registerTypeAdapter(PolicyIDReference.class, new IdReferenceAdapter())
				.registerTypeAdapter(PolicySetIDReference.class, new IdReferenceAdapter()).create();
	}

	@Override
	public ResponseContext unmarshal(Object source) throws XacmlSyntaxException, IOException {
		if (source instanceof Reader) {
			return json.<ResponseContext> fromJson((Reader) source, ResponseContext.class);
		}
		if (source instanceof JsonElement) {
			return json.<ResponseContext> fromJson((JsonElement) source, ResponseContext.class);
		}
		return null;
	}

}
