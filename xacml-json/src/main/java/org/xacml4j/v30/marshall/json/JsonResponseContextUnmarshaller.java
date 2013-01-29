package org.xacml4j.v30.marshall.json;

import java.io.IOException;
import java.io.Reader;

import org.xacml4j.v30.Attribute;
import org.xacml4j.v30.AttributeExp;
import org.xacml4j.v30.Attributes;
import org.xacml4j.v30.ResponseContext;
import org.xacml4j.v30.XacmlSyntaxException;
import org.xacml4j.v30.marshall.Unmarshaller;

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
