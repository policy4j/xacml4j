package org.xacml4j.v30.marshal.json;

import static org.xacml4j.v30.marshal.json.AttributeProperties.ATTRIBUTE_ID_PROPERTY;
import static org.xacml4j.v30.marshal.json.AttributeProperties.DATA_TYPE_PROPERTY;
import static org.xacml4j.v30.marshal.json.AttributeProperties.ISSUER_PROPERTY;
import static org.xacml4j.v30.marshal.json.AttributeProperties.VALUE_PROPERTY;

import java.lang.reflect.Type;

import org.xacml4j.v30.AttributeAssignment;
import org.xacml4j.v30.AttributeExp;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class AttributeAssignmentSerializer implements JsonSerializer<AttributeAssignment> {

	@Override
	public JsonElement serialize(AttributeAssignment src, Type typeOfSrc, JsonSerializationContext context) {
		JsonObject o = new JsonObject();
		o.addProperty(ATTRIBUTE_ID_PROPERTY, src.getAttributeId());

		AttributeExp value = src.getAttribute();
		o.add(VALUE_PROPERTY, context.serialize(value, AttributeExp.class));
		if (value.getType() != null) {
			// NOTE: we are serializing with short data type identifier
			o.addProperty(DATA_TYPE_PROPERTY, value.getType().getShortDataTypeId());
		}

		if (src.getCategory() != null) {
			o.addProperty("Category", src.getCategory().toString());
		}
		if (src.getIssuer() != null) {
			o.addProperty(ISSUER_PROPERTY, src.getIssuer());
		}

		return o;
	}

}
