package org.xacml4j.v30.marshall.json;

import java.io.IOException;
import java.io.Reader;

import org.xacml4j.v30.Attribute;
import org.xacml4j.v30.AttributeExp;
import org.xacml4j.v30.Attributes;
import org.xacml4j.v30.AttributesReference;
import org.xacml4j.v30.RequestContext;
import org.xacml4j.v30.RequestReference;
import org.xacml4j.v30.XacmlSyntaxException;
import org.xacml4j.v30.marshall.RequestUnmarshaller;
import org.xacml4j.v30.types.DataTypeRegistry;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

public class JsonRequestContextUnmarshaller implements RequestUnmarshaller
{
	private Gson json;

	public JsonRequestContextUnmarshaller(DataTypeRegistry typesRegistry)

	{
		this.json = new GsonBuilder()
		.registerTypeAdapter(RequestContext.class, new RequestContextAdapter())
		.registerTypeAdapter(Attributes.class, new AttributesAdapter())
		.registerTypeAdapter(Attribute.class, new AttributeAdapter())
		.registerTypeAdapter(AttributeExp.class, new AttributeExpDeserializer(typesRegistry))
		.registerTypeAdapter(RequestReference.class, new RequestReferenceAdapter())
		.registerTypeAdapter(AttributesReference.class, new AttributesRefererenceAdapater())
		.create();
	}

	@Override
	public RequestContext unmarshal(Object source) throws XacmlSyntaxException,
			IOException {
		if(source instanceof Reader){
			return json.<RequestContext>fromJson((Reader)source, RequestContext.class);
		}
		if(source instanceof JsonElement){
			return json.fromJson((JsonElement)source, RequestContext.class);
		}
		return null;
	}

}
