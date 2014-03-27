package org.xacml4j.v30.marshal.json;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.xacml4j.v30.Attribute;
import org.xacml4j.v30.Attributes;
import org.xacml4j.v30.AttributesReference;
import org.xacml4j.v30.RequestContext;
import org.xacml4j.v30.RequestReference;
import org.xacml4j.v30.marshal.Marshaller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;

public class JsonRequestContextMarshaller implements Marshaller<RequestContext> {

	private final Gson json;

	public JsonRequestContextMarshaller() {
		this.json = new GsonBuilder().registerTypeAdapter(RequestContext.class, new RequestContextAdapter())
				.registerTypeAdapter(Attributes.class, new AttributesAdapter())
				.registerTypeAdapter(Attribute.class, new AttributeSerializer())
				.registerTypeAdapter(RequestReference.class, new RequestReferenceAdapter())
				.registerTypeAdapter(AttributesReference.class, new AttributesRefererenceAdapater()).create();
	}

	@Override
	public Object marshal(RequestContext source) throws IOException {
		return json.toJsonTree(source);
	}

	@Override
	public void marshal(RequestContext source, Object target) throws IOException {
		if (target instanceof Writer) {
			json.toJson(source, RequestContext.class, new JsonWriter((Writer) target));
			return;
		}
		if (target instanceof OutputStream) {
			json.toJson(source, RequestContext.class, new JsonWriter(new OutputStreamWriter((OutputStream) target)));
			return;
		}
		if (target instanceof JsonObject) {
			JsonObject o = (JsonObject) target;
			o.add("Request", json.toJsonTree(source));
			return;
		}
		throw new IllegalArgumentException("Unsupported marshalling target");
	}

}
