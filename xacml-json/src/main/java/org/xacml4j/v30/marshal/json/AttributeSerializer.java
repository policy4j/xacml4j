package org.xacml4j.v30.marshal.json;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static org.xacml4j.v30.marshal.json.JsonProperties.ATTRIBUTE_ID_PROPERTY;
import static org.xacml4j.v30.marshal.json.JsonProperties.DATA_TYPE_PROPERTY;
import static org.xacml4j.v30.marshal.json.JsonProperties.INCLUDE_IN_RESULT_PROPERTY;
import static org.xacml4j.v30.marshal.json.JsonProperties.ISSUER_PROPERTY;
import static org.xacml4j.v30.marshal.json.JsonProperties.VALUE_PROPERTY;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Iterator;

import org.xacml4j.v30.Attribute;
import org.xacml4j.v30.AttributeExp;

import com.google.common.base.Optional;
import com.google.common.collect.Iterables;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

class AttributeSerializer implements JsonSerializer<Attribute> 
{
	@Override
	public JsonElement serialize(Attribute src, Type typeOfSrc, 
			JsonSerializationContext context) {
		JsonObject o = new JsonObject();
		o.addProperty(ATTRIBUTE_ID_PROPERTY, src.getAttributeId());
		if (src.getIssuer() != null) {
			o.addProperty(ISSUER_PROPERTY, src.getIssuer());
		}
		Collection<AttributeExp> values = src.getValues();
		serializeValue(context, o, values);
		// OMIT property if value is "false"
		if(src.isIncludeInResult()){
			o.addProperty(INCLUDE_IN_RESULT_PROPERTY, src.isIncludeInResult());
		}
		return o;
	}

	private void serializeValue(JsonSerializationContext context, JsonObject o, 
			Collection<AttributeExp> values) {
		checkArgument(values != null && !values.isEmpty(), "Attribute value is mandatory.");
		AttributeExp firstValue = Iterables.getFirst(values, null);
		o.addProperty(DATA_TYPE_PROPERTY, firstValue.getType().getShortDataTypeId());
		Optional<TypeToGSon> toGson = TypeToGSon.Types.getIndex().get(firstValue.getType());
		checkState(toGson.isPresent());
		if(values.size() == 1){
			o.add(VALUE_PROPERTY, toGson.get().toJson(firstValue, context));
			return;
		}
		JsonArray array = new JsonArray();
		for(AttributeExp a : values){
			checkArgument(firstValue.getType().equals(a.getType()));
			array.add(toGson.get().toJson(a, context));
		}
		o.add(VALUE_PROPERTY, array);
	}
}
