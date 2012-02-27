package com.artagon.xacml.v30.marshall.json;

import java.io.IOException;
import java.io.Reader;

import com.artagon.xacml.v30.marshall.Unmarshaller;
import com.artagon.xacml.v30.pdp.Attribute;
import com.artagon.xacml.v30.pdp.AttributeExp;
import com.artagon.xacml.v30.pdp.Attributes;
import com.artagon.xacml.v30.pdp.ResponseContext;
import com.artagon.xacml.v30.pdp.XacmlSyntaxException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

public class JsonResponseContextUnmarshaller implements Unmarshaller<ResponseContext>
{
	private Gson json;
	
	public JsonResponseContextUnmarshaller(){
		this.json = new GsonBuilder()
		.registerTypeAdapter(Attributes.class, new AttributesAdapter())
		.registerTypeAdapter(Attribute.class, new AttributeAdapter())
		.registerTypeAdapter(AttributeExp.class, new AttributeExpSerializer())
		.create();
	}
	
	@Override
	public ResponseContext unmarshal(Object source)
			throws XacmlSyntaxException, IOException {
		if(source instanceof Reader){
			return json.<ResponseContext>fromJson((Reader)source, ResponseContext.class);
		}
		if(source instanceof JsonElement){
			return json.<ResponseContext>fromJson((JsonElement)source, ResponseContext.class);
		}
		return null;
	}
}
