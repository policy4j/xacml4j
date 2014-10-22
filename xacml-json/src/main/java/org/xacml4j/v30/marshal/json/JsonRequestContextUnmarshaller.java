package org.xacml4j.v30.marshal.json;

/*
 * #%L
 * Xacml4J Gson Integration
 * %%
 * Copyright (C) 2009 - 2014 Xacml4J.org
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

import java.io.IOException;
import java.io.Reader;

import org.xacml4j.v30.Attribute;
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
				.registerTypeAdapter(Attribute.class, new AttributeDeserializer())
				.registerTypeAdapter(RequestReference.class, new RequestReferenceAdapter())
                .registerTypeAdapter(Category.class, new CategoryAdapter())
                .registerTypeAdapter(CategoryReference.class, new CategoryReferenceAdapter()).create();
	}

	@Override
	public RequestContext unmarshal(Object source) throws XacmlSyntaxException, IOException {
		if (source instanceof Reader) {
			return json.fromJson((Reader) source, RequestContext.class);
		}
		if (source instanceof JsonElement) {
			return json.fromJson((JsonElement) source, RequestContext.class);
		}
		return null;
	}

}
