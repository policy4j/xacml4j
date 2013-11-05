package org.xacml4j.v30.marshal.json;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

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
import org.xacml4j.v30.marshal.Marshaller;
import org.xacml4j.v30.pdp.PolicyIDReference;
import org.xacml4j.v30.pdp.PolicySetIDReference;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;

public class JsonResponseContextMarshaller implements Marshaller<ResponseContext> {

	private final Gson json;

	public JsonResponseContextMarshaller() {
		json = new GsonBuilder().registerTypeAdapter(ResponseContext.class, new ResponseContextAdapter())
				.registerTypeAdapter(Result.class, new ResultAdapter())
				.registerTypeAdapter(Status.class, new StatusAdapter())
				.registerTypeAdapter(StatusCode.class, new StatusCodeAdapter())
				.registerTypeAdapter(Obligation.class, new ObligationOrAdviceAdapter())
				.registerTypeAdapter(AttributeAssignment.class, new AttributeAssignmentSerializer())
				.registerTypeAdapter(Advice.class, new ObligationOrAdviceAdapter())
				.registerTypeAdapter(Attributes.class, new AttributesAdapter())
				.registerTypeAdapter(Attribute.class, new AttributeSerializer())
				.registerTypeAdapter(AttributeExp.class, new AttributeExpSerializer())
				.registerTypeAdapter(PolicyIDReference.class, new IdReferenceAdapter())
				.registerTypeAdapter(PolicySetIDReference.class, new IdReferenceAdapter()).create();
	}

	@Override
	public Object marshal(ResponseContext source) throws IOException {
		return json.toJsonTree(source);
	}

	@Override
	public void marshal(ResponseContext source, Object target) throws IOException {
		if (target instanceof Writer) {
			json.toJson(source, ResponseContext.class, new JsonWriter((Writer) target));
			return;
		}
		if (target instanceof OutputStream) {
			json.toJson(source, ResponseContext.class, new JsonWriter(new OutputStreamWriter((OutputStream) target)));
			return;
		}
		if (target instanceof JsonObject) {
			JsonObject o = (JsonObject) target;
			o.add("Response", json.toJsonTree(source));
			return;
		}
		throw new IllegalArgumentException("Unsupported marshalling target");
	}

}
