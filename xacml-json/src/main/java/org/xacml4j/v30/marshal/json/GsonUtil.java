package org.xacml4j.v30.marshal.json;

/*
 * #%L
 * Artagon XACML 3.0 Gson integration
 * %%
 * Copyright (C) 2009 - 2014 Artagon
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.reflect.TypeToken;

class GsonUtil
{
	/** Private constructor for utility class */
	private GsonUtil() {}

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
