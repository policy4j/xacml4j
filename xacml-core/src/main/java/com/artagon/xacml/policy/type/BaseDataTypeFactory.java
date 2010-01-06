package com.artagon.xacml.policy.type;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.artagon.xacml.DataTypeId;
import com.artagon.xacml.policy.AttributeDataType;
import com.artagon.xacml.policy.DataTypeFactory;

public class BaseDataTypeFactory implements DataTypeFactory
{
	private Map<DataTypeId, AttributeDataType> types;
	
	protected BaseDataTypeFactory(){
		this.types = new ConcurrentHashMap<DataTypeId, AttributeDataType>();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public final <T extends AttributeDataType> T getDataType(DataTypeId typeId) 
	{
		T type = (T)types.get(typeId);
		if(type == null){
			throw new IllegalArgumentException(
					String.format("DataTypeId=\"%s\" is not available", typeId));
		}
		return type;
	}
	
	@Override
	public final Iterable<DataTypeId> getSupportedTypes() {
		return types.keySet();
	}

	@Override
	public final boolean isProvided(DataTypeId typeId) {
		return types.containsKey(typeId);
	}

	protected final void add(AttributeDataType type){
		this.types.put(type.getDataTypeId(), type);
	}
}
