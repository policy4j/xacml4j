package org.xacml4j.v30.marshal.json;

import java.lang.reflect.Type;
import java.util.Collection;

import org.xacml4j.v30.Attribute;
import org.xacml4j.v30.AttributeExp;

import com.google.common.base.Preconditions;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;

class AttributeAdapter implements JsonDeserializer<Attribute>, JsonSerializer<Attribute> {

	private static final String ATTRIBUTE_ID_PROPERTY = "AttributeId";
	private static final String VALUE_PROPERTY = "Value";
	private static final String DATA_TYPE_PROPERTY = "DataType";
	private static final String ISSUER_PROPERTY = "Issuer";
	private static final String INCLUDE_IN_RESULT_PROPERTY = "IncludeInResult";

	@Override
	public Attribute deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
			throws JsonParseException {
		JsonObject o = json.getAsJsonObject();
		String attrId = GsonUtil.getAsString(o, ATTRIBUTE_ID_PROPERTY, null);
		// TODO: deserialize DataType
		String issuer = GsonUtil.getAsString(o, ISSUER_PROPERTY, null);
		boolean inclInRes = GsonUtil.getAsBoolean(o, INCLUDE_IN_RESULT_PROPERTY, false);
		Collection<AttributeExp> values = context.deserialize(o.get(VALUE_PROPERTY),
				new TypeToken<Collection<AttributeExp>>() {
				}.getType());
		Preconditions.checkState(values != null && !values.isEmpty());
		return Attribute.builder(attrId).issuer(issuer).includeInResult(inclInRes).values(values).build();
	}

	@Override
	public JsonElement serialize(Attribute src, Type typeOfSrc, JsonSerializationContext context) {
		JsonObject o = new JsonObject();
		o.addProperty(ATTRIBUTE_ID_PROPERTY, src.getAttributeId());
		o.add(VALUE_PROPERTY, context.serialize(src.getValues(), new TypeToken<Collection<AttributeExp>>() {
		}.getType()));
		// TODO: serialize DataType
		if (src.getIssuer() != null) {
			o.addProperty(ISSUER_PROPERTY, src.getIssuer());
		}
		o.addProperty(INCLUDE_IN_RESULT_PROPERTY, src.isIncludeInResult());
		return o;
	}

}
