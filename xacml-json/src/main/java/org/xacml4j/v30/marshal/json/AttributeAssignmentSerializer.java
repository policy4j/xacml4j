package org.xacml4j.v30.marshal.json;

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
