package com.artagon.xacml.policy;

import com.artagon.xacml.DataTypeId;

public interface DataTypeFactory 
{
	/**
	 * Gets data type {@link AttributeDataType}
	 * @param <T>
	 * @param typeId
	 * @return
	 */
	 <T extends AttributeDataType> T getDataType(DataTypeId typeId);
	 
	 /**
	  * Tests if this type factory provides
	  * given data type implementation
	  * 
	  * @param typeId a type identifier
	  * @return
	  */
	 boolean isProvided(DataTypeId typeId);
	 
	 /**
	  * Gets supported data type identifiers
	  * 
	  * @return iterator over supported data types
	  */
	 Iterable<DataTypeId> getSupportedTypes();
}
