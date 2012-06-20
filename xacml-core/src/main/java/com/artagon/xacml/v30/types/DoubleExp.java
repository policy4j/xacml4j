package com.artagon.xacml.v30.types;

import com.artagon.xacml.v30.AttributeExpType;

public final class DoubleExp extends BaseAttributeExp<Double>
{
	private static final long serialVersionUID = -3689668541615314228L;

	DoubleExp(AttributeExpType type, Double value) {
		super(type, value);
	}
	
	public DoubleExp add(DoubleExp d){
		return  new DoubleExp(getType(), getValue() + d.getValue());
	}
	
	public DoubleExp substract(DoubleExp d){
		return  new DoubleExp(getType(), getValue() - d.getValue());
	}
	
	public DoubleExp multiply(DoubleExp d){
		return  new DoubleExp(getType(), getValue() * d.getValue());
	}
	
	public DoubleExp divide(DoubleExp d){
		return  new DoubleExp(getType(), getValue() / d.getValue());
	}
}

