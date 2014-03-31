package org.xacml4j.v30.marshal.json;

import static com.google.common.base.Preconditions.checkArgument;

import static org.xacml4j.v30.marshal.json.JsonProperties.ATTRIBUTE_ID_PROPERTY;
import static org.xacml4j.v30.marshal.json.JsonProperties.DATA_TYPE_PROPERTY;
import static org.xacml4j.v30.marshal.json.JsonProperties.ISSUER_PROPERTY;
import static org.xacml4j.v30.marshal.json.JsonProperties.VALUE_PROPERTY;

import java.lang.reflect.Type;

import org.xacml4j.v30.AttributeAssignment;
import org.xacml4j.v30.Categories;
import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.AttributeExp;
import org.xacml4j.v30.AttributeExpType;
import org.xacml4j.v30.types.XacmlTypes;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

public class AttributeAssignmentDeserializer implements JsonDeserializer<AttributeAssignment> {

	
	@Override
	public AttributeAssignment deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
			throws JsonParseException {
		JsonObject o = json.getAsJsonObject();

		String attrId = GsonUtil.getAsString(o, ATTRIBUTE_ID_PROPERTY, null);
		checkArgument(attrId != null, "Property '%s' is mandatory.", ATTRIBUTE_ID_PROPERTY);

		AttributeExp value = deserializeValue(context, o);
		CategoryId category = Categories.parse(GsonUtil.getAsString(o, "Category", null));
		String issuer = GsonUtil.getAsString(o, ISSUER_PROPERTY, null);

		return AttributeAssignment
				.builder()
				.id(attrId)
				.category(category)
				.issuer(issuer)
				.value(value)
				.build();
	}

	private AttributeExp deserializeValue(JsonDeserializationContext context, JsonObject o) {
		String dataTypeId = GsonUtil.getAsString(o, DATA_TYPE_PROPERTY, null);
		if (dataTypeId == null) {
			// TODO: properly infer data type
			dataTypeId = XacmlTypes.STRING.getDataTypeId();
		}
		Optional<AttributeExpType> type = XacmlTypes.getType(dataTypeId);
		Preconditions.checkState(type.isPresent());
		JsonElement jsonValue = o.get(VALUE_PROPERTY);
		// TODO: do a proper type coersion
		AttributeExp value = deserializeValue(type.get(), jsonValue, context);

		checkArgument(value != null, "Property '%s' is mandatory.", VALUE_PROPERTY);
		return value;
	}

	private AttributeExp deserializeValue(AttributeExpType type, JsonElement jsonValue, JsonDeserializationContext ctx) {
		Optional<TypeToGSon> toGson = TypeToGSon.Types.getIndex().get(type);
		Preconditions.checkState(toGson.isPresent());
		return toGson.get().fromJson(jsonValue, ctx);
	}

}
