package com.artagon.xacml.v30.types;

import com.artagon.xacml.v30.AttributeExpType;

public final class DoubleValueExp extends BaseAttributeExpression<Double>
{
	private static final long serialVersionUID = -3689668541615314228L;

	DoubleValueExp(AttributeExpType type, Double value) {
		super(type, value);
	}
	
	public DoubleValueExp add(DoubleValueExp d){
		return  new DoubleValueExp(getType(), getValue() + d.getValue());
	}
	
	public DoubleValueExp substract(DoubleValueExp d){
		return  new DoubleValueExp(getType(), getValue() - d.getValue());
	}
	
	public DoubleValueExp multiply(DoubleValueExp d){
		return  new DoubleValueExp(getType(), getValue() * d.getValue());
	}
	
	public DoubleValueExp divide(DoubleValueExp d){
		return  new DoubleValueExp(getType(), getValue() / d.getValue());
	}
}

