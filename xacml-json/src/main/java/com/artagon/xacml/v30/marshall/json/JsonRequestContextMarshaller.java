package com.artagon.xacml.v30.marshall.json;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import com.artagon.xacml.v30.marshall.Marshaller;
import com.artagon.xacml.v30.pdp.Attribute;
import com.artagon.xacml.v30.pdp.AttributeExp;
import com.artagon.xacml.v30.pdp.Attributes;
import com.artagon.xacml.v30.pdp.AttributesReference;
import com.artagon.xacml.v30.pdp.RequestContext;
import com.artagon.xacml.v30.pdp.RequestReference;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonWriter;

public class JsonRequestContextMarshaller implements Marshaller<RequestContext> 
{

	private Gson json;
	
	public JsonRequestContextMarshaller()
	
	{
		this.json = new GsonBuilder()
		.registerTypeAdapter(RequestContext.class, new RequestContextAdapter())
		.registerTypeAdapter(Attributes.class, new AttributesAdapter())
		.registerTypeAdapter(Attribute.class, new AttributeAdapter())
		.registerTypeAdapter(AttributeExp.class, new AttributeExpSerializer())
		.registerTypeAdapter(RequestReference.class, new RequestReferenceAdapter())
		.registerTypeAdapter(AttributesReference.class, new AttributesRefererenceAdapater())
		.build();
	}
	
	@Override
	public Object marshal(RequestContext source) throws IOException 
	{
		return json.toJsonTree(source);
	}

	@Override
	public void marshal(RequestContext object, Object source)
			throws IOException {
		if(source instanceof Writer){
			json.toJson(object, new TypeToken<RequestContext>(){}.getType(), new JsonWriter((Writer)source));
			return;
		}
		if(source instanceof OutputStream){
			json.toJson(object, new TypeToken<RequestContext>(){}.getType(), 
					new JsonWriter(new OutputStreamWriter((OutputStream)source)));
			return;
		}
		if(source instanceof JsonObject){
			JsonObject o = (JsonObject)source;
			o.add("xacmlRequest", json.toJsonTree(object));
			return;
		}
		throw new IllegalArgumentException("Unsupported marshalling source");
	}
}
