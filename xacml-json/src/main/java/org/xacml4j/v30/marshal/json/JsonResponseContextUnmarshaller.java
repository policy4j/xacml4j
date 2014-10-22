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

import org.xacml4j.v30.*;
import org.xacml4j.v30.marshal.Unmarshaller;
import org.xacml4j.v30.pdp.PolicyIDReference;
import org.xacml4j.v30.pdp.PolicySetIDReference;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

public class JsonResponseContextUnmarshaller implements Unmarshaller<ResponseContext> {
	private final Gson json;

	public JsonResponseContextUnmarshaller() {
		json = new GsonBuilder().registerTypeAdapter(ResponseContext.class, new ResponseContextAdapter())
				.registerTypeAdapter(Result.class, new ResultAdapter())
				.registerTypeAdapter(Status.class, new StatusAdapter())
				.registerTypeAdapter(StatusCode.class, new StatusCodeAdapter())
                .registerTypeAdapter(Obligation.class, new ObligationOrAdviceAdapter())
				.registerTypeAdapter(AttributeAssignment.class, new AttributeAssignmentDeserializer())
				.registerTypeAdapter(Advice.class, new ObligationOrAdviceAdapter())
				.registerTypeAdapter(Category.class, new CategoryAdapter())
				.registerTypeAdapter(Attribute.class, new AttributeDeserializer())
				.registerTypeAdapter(PolicyIDReference.class, new IdReferenceAdapter())
				.registerTypeAdapter(PolicySetIDReference.class, new IdReferenceAdapter()).create();
	}

	@Override
	public ResponseContext unmarshal(Object source) throws XacmlSyntaxException, IOException {
		if (source instanceof Reader) {
			return json.fromJson((Reader) source, ResponseContext.class);
		}
		if (source instanceof JsonElement) {
			return json.fromJson((JsonElement) source, ResponseContext.class);
		}
		return null;
	}

}
