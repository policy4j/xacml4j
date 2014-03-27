package org.xacml4j.v30.marshal.json;

import static com.google.common.base.Preconditions.checkArgument;
import static org.xacml4j.v30.marshal.json.AttributeProperties.ATTRIBUTE_ID_PROPERTY;
import static org.xacml4j.v30.marshal.json.AttributeProperties.DATA_TYPE_PROPERTY;
import static org.xacml4j.v30.marshal.json.AttributeProperties.INCLUDE_IN_RESULT_PROPERTY;
import static org.xacml4j.v30.marshal.json.AttributeProperties.ISSUER_PROPERTY;
import static org.xacml4j.v30.marshal.json.AttributeProperties.VALUE_PROPERTY;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Iterator;

import org.xacml4j.v30.Attribute;
import org.xacml4j.v30.AttributeExp;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;

class AttributeSerializer implements JsonSerializer<Attribute> {

	@Override
	public JsonElement serialize(Attribute src, Type typeOfSrc, 
			JsonSerializationContext context) {
		JsonObject o = new JsonObject();
		o.addProperty(ATTRIBUTE_ID_PROPERTY, src.getAttributeId());
		Collection<AttributeExp> values = src.getValues();
		serializeValue(context, o, values);

		if (src.getIssuer() != null) {
			o.addProperty(ISSUER_PROPERTY, src.getIssuer());
		}
		o.addProperty(INCLUDE_IN_RESULT_PROPERTY, src.isIncludeInResult());
		return o;
	}

	private void serializeValue(JsonSerializationContext context, JsonObject o, Collection<AttributeExp> values) {
		checkArgument(values != null && !values.isEmpty(), "Attribute value is mandatory.");
		Iterator<AttributeExp> valueIterator = values.iterator();
		AttributeExp firstValue = valueIterator.next();
		if (valueIterator.hasNext()) {
			o.add(VALUE_PROPERTY, context.serialize(values, new TypeToken<Collection<AttributeExp>>() {
			}.getType()));
		} else {
			o.add(VALUE_PROPERTY, context.serialize(firstValue, AttributeExp.class));
		}
		o.addProperty(DATA_TYPE_PROPERTY, firstValue.getType().getShortDataTypeId());
	}

}
