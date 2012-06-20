package com.artagon.xacml.v30.types;

import java.util.Map;

import javax.xml.namespace.QName;

import com.artagon.xacml.v30.AttributeExp;
import com.artagon.xacml.v30.AttributeExpType;
import com.artagon.xacml.v30.XacmlSyntaxException;

public interface DataTypeRegistry 
{
	/**
	 * Gets type by given type identifier
	 * 
	 * @param typeId a type identifier
	 * @return {@link AttributeExpType} a data type instance
	 */
	AttributeExpType getType(String typeId);
	AttributeExp create(String typeId, Object value) 
			throws XacmlSyntaxException;
	
	AttributeExp create(String typeId, Object value, Map<QName, String> attrs) 
			throws XacmlSyntaxException;
}
