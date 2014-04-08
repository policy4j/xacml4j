package org.xacml4j.v30.marshal.json;

import java.io.IOException;
import java.io.Reader;

import org.xacml4j.v30.Attribute;
import org.xacml4j.v30.AttributeExp;
import org.xacml4j.v30.Category;
import org.xacml4j.v30.CategoryReference;
import org.xacml4j.v30.RequestContext;
import org.xacml4j.v30.RequestReference;
import org.xacml4j.v30.XacmlSyntaxException;
import org.xacml4j.v30.marshal.RequestUnmarshaller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

public class JsonRequestContextUnmarshaller implements RequestUnmarshaller {
	private final Gson json;

	public JsonRequestContextUnmarshaller()
	{
		json = new GsonBuilder().registerTypeAdapter(RequestContext.class, new RequestContextAdapter())
				.registerTypeAdapter(Category.class, new CategoryAdapter())
				.registerTypeAdapter(Attribute.class, new AttributeDeserializer())
				.registerTypeAdapter(RequestReference.class, new RequestReferenceAdapter())
				.registerTypeAdapter(CategoryReference.class, new AttributesRefererenceAdapater()).create();
	}

	@Override
	public RequestContext unmarshal(Object source) throws XacmlSyntaxException, IOException {
		if (source instanceof Reader) {
			return json.<RequestContext> fromJson((Reader) source, RequestContext.class);
		}
		if (source instanceof JsonElement) {
			return json.fromJson((JsonElement) source, RequestContext.class);
		}
		return null;
	}

}
