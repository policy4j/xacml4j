package org.xacml4j.v30.marshal.json;

import static com.google.common.base.Preconditions.checkArgument;
import static org.xacml4j.v30.marshal.json.AttributeProperties.ATTRIBUTE_ID_PROPERTY;
import static org.xacml4j.v30.marshal.json.AttributeProperties.DATA_TYPE_PROPERTY;
import static org.xacml4j.v30.marshal.json.AttributeProperties.INCLUDE_IN_RESULT_PROPERTY;
import static org.xacml4j.v30.marshal.json.AttributeProperties.ISSUER_PROPERTY;
import static org.xacml4j.v30.marshal.json.AttributeProperties.VALUE_PROPERTY;

import java.lang.reflect.Type;
import java.util.Collection;

import org.xacml4j.v30.Attribute;
import org.xacml4j.v30.AttributeExp;
import org.xacml4j.v30.AttributeExpType;
import org.xacml4j.v30.types.StringType;
import org.xacml4j.v30.types.Types;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

class AttributeDeserializer extends Support implements JsonDeserializer<Attribute> 
{
	public AttributeDeserializer(Types typesRegistry) {
		super(typesRegistry);
	}

	@Override
	public Attribute deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
			throws JsonParseException {
		JsonObject o = json.getAsJsonObject();
		String attrId = GsonUtil.getAsString(o, ATTRIBUTE_ID_PROPERTY, null);
		checkArgument(attrId != null, "Property '%s' is mandatory.", ATTRIBUTE_ID_PROPERTY);

		Collection<AttributeExp> values = deserializeValue(context, o);

		String issuer = GsonUtil.getAsString(o, ISSUER_PROPERTY, null);
		boolean inclInRes = GsonUtil.getAsBoolean(o, INCLUDE_IN_RESULT_PROPERTY, false);

		return Attribute.builder(attrId).issuer(issuer).includeInResult(inclInRes).values(values).build();
	}

	private Collection<AttributeExp> deserializeValue(JsonDeserializationContext context, JsonObject o) {
		String dataTypeId = GsonUtil.getAsString(o, DATA_TYPE_PROPERTY, null);
		if (dataTypeId == null) {
			// TODO: properly infer data type
			dataTypeId = StringType.STRING.getDataTypeId();
		}
		AttributeExpType type = types.getType(dataTypeId);
		JsonElement jsonValue = o.get(VALUE_PROPERTY);
		Collection<AttributeExp> values = null;
		if (jsonValue.isJsonArray()) {
			JsonArray jsonArray = jsonValue.getAsJsonArray();
			ImmutableList.Builder<AttributeExp> valuesBuilder = ImmutableList.builder();
			for (int i = 0; i < jsonArray.size(); i++) {
				valuesBuilder.add(deserializeValue(type, jsonArray.get(i)));
			}
			values = valuesBuilder.build();
		} else {
			// TODO: do a proper type coersion
			values = ImmutableList.of(deserializeValue(type, jsonValue));
		}
		checkArgument(values != null && !values.isEmpty(), "Property '%s' is mandatory.", VALUE_PROPERTY);
		return values;
	}

	private AttributeExp deserializeValue(AttributeExpType type, JsonElement jsonValue) {
		TypeToGSon toGson = types.getCapability(type, TypeToGSon.class);
		Preconditions.checkState(toGson != null);
		return toGson.fromJson(jsonValue);
	}

}
