package com.artagon.xacml.v30.types;

import com.artagon.xacml.v30.AttributeExpType;

public final class IntegerExp 
	extends BaseAttributeExp<Long>
{
	private static final long serialVersionUID = 6654857010399020496L;

	IntegerExp(AttributeExpType type, Long value) {
		super(type, value);
	}
	
	public IntegerExp add(IntegerExp d){
		return  new IntegerExp(getType(), getValue() + d.getValue());
	}
	
	public IntegerExp substract(IntegerExp d){
		return  new IntegerExp(getType(), getValue() - d.getValue());
	}
	
	public IntegerExp multiply(IntegerExp d){
		return  new IntegerExp(getType(), getValue() * d.getValue());
	}
	
	public IntegerExp divide(IntegerExp d){
		return  new IntegerExp(getType(), getValue() / d.getValue());
	}
}

