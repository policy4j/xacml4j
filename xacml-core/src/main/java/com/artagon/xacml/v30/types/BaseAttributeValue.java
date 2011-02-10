package com.artagon.xacml.v30.types;

import com.artagon.xacml.v30.AttributeValue;
import com.artagon.xacml.v30.AttributeValueType;
import com.artagon.xacml.v30.EvaluationContext;
import com.artagon.xacml.v30.EvaluationException;
import com.artagon.xacml.v30.ExpressionVisitor;
import com.artagon.xacml.v30.ValueType;
import com.artagon.xacml.v30.XacmlObject;
import com.google.common.base.Preconditions;

public abstract class BaseAttributeValue extends XacmlObject
	implements AttributeValue
{
	private static final long serialVersionUID = 4131180767511036271L;
	
	private AttributeValueType type;
	
	protected BaseAttributeValue(AttributeValueType type) {
		Preconditions.checkNotNull(type);
		this.type = type;
	}

	@Override
	public final ValueType getEvaluatesTo(){
		return type;
	}
	
	@Override
	public final AttributeValueType getType(){
		return type;
	}

	@Override
	public final AttributeValue evaluate(EvaluationContext context) throws EvaluationException {
		return this;
	}
	
	@Override
	public final void accept(ExpressionVisitor v) {
		v.visit(this);
	}
}
