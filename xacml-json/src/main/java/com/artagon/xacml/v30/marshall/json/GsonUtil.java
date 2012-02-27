package com.artagon.xacml.v30.marshall.json;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.reflect.TypeToken;

class GsonUtil 
{
	public static boolean getAsBoolean(JsonObject o, String memberName, boolean defaultValue)
	{
		JsonPrimitive v = o.getAsJsonPrimitive(memberName);
		return (v != null)?v.getAsBoolean():defaultValue;
	}
	
	public static String getAsString(JsonObject o, String memberName, String defaultValue)
	{
		JsonPrimitive v = o.getAsJsonPrimitive(memberName);
		return (v != null)?v.getAsString():defaultValue;
	}
	
	public static <T> T deserialize(JsonElement e, JsonDeserializationContext context)
	{
		return (e != null)?context.<T>deserialize(e, new TypeToken<T>(){}.getType()):null;
	}
}
