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

import static org.xacml4j.v30.marshal.json.JsonProperties.ATTRIBUTE_ID_PROPERTY;
import static org.xacml4j.v30.marshal.json.JsonProperties.DATA_TYPE_PROPERTY;
import static org.xacml4j.v30.marshal.json.JsonProperties.ISSUER_PROPERTY;
import static org.xacml4j.v30.marshal.json.JsonProperties.VALUE_PROPERTY;

import java.lang.reflect.Type;

import org.xacml4j.v30.AttributeAssignment;
import org.xacml4j.v30.AttributeExp;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class AttributeAssignmentSerializer implements JsonSerializer<AttributeAssignment> 
{

	@Override
	public JsonElement serialize(AttributeAssignment src, Type typeOfSrc, JsonSerializationContext context) 
	{
		JsonObject o = new JsonObject();
		o.addProperty(ATTRIBUTE_ID_PROPERTY, src.getAttributeId());
		AttributeExp value = src.getAttribute();
		Optional<TypeToGSon> toGson = TypeToGSon.Types.getIndex().get(value.getType());
		Preconditions.checkState(toGson.isPresent());
		o.add(VALUE_PROPERTY, toGson.get().toJson(value, context));
		o.addProperty(DATA_TYPE_PROPERTY, value.getType().getShortDataTypeId());
		o.addProperty(ISSUER_PROPERTY, src.getIssuer());
		if (src.getCategory() != null) {
			o.addProperty("Category", 
					src.getCategory().toString());
		}
		return o;
	}
}
