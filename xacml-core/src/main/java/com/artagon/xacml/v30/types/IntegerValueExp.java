package com.artagon.xacml.v30.types;

import com.artagon.xacml.v30.AttributeExpType;

public final class IntegerValueExp 
	extends BaseAttributeExpression<Long>
{
	private static final long serialVersionUID = 6654857010399020496L;

	IntegerValueExp(AttributeExpType type, Long value) {
		super(type, value);
	}
	
	public IntegerValueExp add(IntegerValueExp d){
		return  new IntegerValueExp(getType(), getValue() + d.getValue());
	}
	
	public IntegerValueExp substract(IntegerValueExp d){
		return  new IntegerValueExp(getType(), getValue() - d.getValue());
	}
	
	public IntegerValueExp multiply(IntegerValueExp d){
		return  new IntegerValueExp(getType(), getValue() * d.getValue());
	}
	
	public IntegerValueExp divide(IntegerValueExp d){
		return  new IntegerValueExp(getType(), getValue() / d.getValue());
	}
}

