package com.artagon.xacml.v3.policy.spi.type;

import com.artagon.xacml.v3.AttributeValue;
import com.artagon.xacml.v3.AttributeValueType;
import com.artagon.xacml.v3.XacmlObject;
import com.artagon.xacml.v3.policy.EvaluationContext;
import com.artagon.xacml.v3.policy.EvaluationException;
import com.artagon.xacml.v3.policy.PolicyVisitor;
import com.artagon.xacml.v3.policy.ValueType;
import com.google.common.base.Preconditions;

public abstract class BaseAttributeValue<T> extends XacmlObject
	implements AttributeValue
{
	private T value;
	private AttributeValueType type;
	
	protected BaseAttributeValue(AttributeValueType type, T value) {
		Preconditions.checkNotNull(type);
		Preconditions.checkNotNull(value);
		this.type = type;
		this.value = value;
	}
	
	public final T getValue(){
		return value;
	}
	
	@Override
	public ValueType getEvaluatesTo(){
		return type;
	}
	
	@Override
	public final AttributeValueType getType(){
		return type;
	}
	
	@Override
	public String toXacmlString() {
		return value.toString();
	}
	
	@Override
	public final AttributeValue evaluate(EvaluationContext context) throws EvaluationException {
		return this;
	}
	
	@Override
	public final void accept(PolicyVisitor v) {
		v.visitEnter(this);
		v.visitLeave(this);
	}
}
