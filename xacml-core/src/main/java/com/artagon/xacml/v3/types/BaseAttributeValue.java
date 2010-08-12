package com.artagon.xacml.v3.types;

import com.artagon.xacml.v3.AttributeValue;
import com.artagon.xacml.v3.AttributeValueType;
import com.artagon.xacml.v3.EvaluationContext;
import com.artagon.xacml.v3.EvaluationException;
import com.artagon.xacml.v3.PolicyVisitor;
import com.artagon.xacml.v3.ValueType;
import com.artagon.xacml.v3.XacmlObject;
import com.google.common.base.Objects;
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
	
	public AttributeValueType getDataType(){
		return type;
	}
	
	@Override
	public String toXacmlString() {
		return value.toString();
	}
		
	@Override
	public String toString() {
		return Objects.toStringHelper(this).
		add("Value", value).
		add("Type", type).toString();
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
