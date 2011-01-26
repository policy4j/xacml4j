package com.artagon.xacml.v30.types;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.artagon.xacml.v30.AttributeValueType;
import com.google.common.base.Preconditions;

public class DefaultDataTypeRegistry 
	implements DataTypeRegistry
{
	private Map<String, AttributeValueType> types;
	
	public DefaultDataTypeRegistry()
	{
		this.types = new ConcurrentHashMap<String, AttributeValueType>(32);
		addType(AnyURIType.ANYURI);
		addType(Base64BinaryType.BASE64BINARY);
		addType(BooleanType.BOOLEAN);
		addType(DateTimeType.DATETIME);
		addType(DateTimeType.DATETIME);	
	}
	
	@Override
	public AttributeValueType getType(String typeId) {
		return types.get(typeId);
	}

	@Override
	public void setTypes(Collection<AttributeValueType> types) {
		for(AttributeValueType type : types){
			addType(type);
		}
	}
	
	private void addType(AttributeValueType type)
	{
		Preconditions.checkArgument(
				this.types.put(type.getDataTypeId(), type) == null);
	}
}
