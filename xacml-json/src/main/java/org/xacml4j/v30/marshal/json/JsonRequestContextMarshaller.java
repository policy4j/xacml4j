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
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.xacml4j.v30.Attribute;
import org.xacml4j.v30.Category;
import org.xacml4j.v30.CategoryReference;
import org.xacml4j.v30.RequestContext;
import org.xacml4j.v30.RequestReference;
import org.xacml4j.v30.marshal.Marshaller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonWriter;

public class JsonRequestContextMarshaller implements Marshaller<RequestContext> {

	private final Gson json;

	public JsonRequestContextMarshaller() {
		this.json = new GsonBuilder()
				.registerTypeAdapter(RequestContext.class, new RequestContextAdapter())
				.registerTypeAdapter(Attribute.class, new AttributeSerializer())
				.registerTypeAdapter(RequestReference.class, new RequestReferenceAdapter())
				.registerTypeAdapter(Category.class, new CategoryAdapter())
                .registerTypeAdapter(CategoryReference.class, new CategoryReferenceAdapter())
                .create();
	}

	@Override
	public Object marshal(RequestContext source) throws IOException {
		return json.toJsonTree(source);
	}

	@Override
	public void marshal(RequestContext source, Object target) throws IOException {
		if (target instanceof Writer) {
			json.toJson(source, RequestContext.class,
                    new JsonWriter((Writer) target));
			return;
		}
		if (target instanceof OutputStream) {
			json.toJson(source, RequestContext.class,
                    new JsonWriter(new OutputStreamWriter((OutputStream) target)));
			return;
		}
		throw new IllegalArgumentException("Unsupported marshalling target");
	}

}
