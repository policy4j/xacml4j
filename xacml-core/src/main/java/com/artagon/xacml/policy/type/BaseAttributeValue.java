package com.artagon.xacml.policy.type;

import com.artagon.xacml.policy.AttributeValueType;
import com.artagon.xacml.policy.AttributeValue;
import com.artagon.xacml.policy.EvaluationContext;
import com.artagon.xacml.policy.EvaluationException;
import com.artagon.xacml.policy.PolicyVisitor;
import com.artagon.xacml.policy.ValueType;
import com.artagon.xacml.util.Objects;
import com.artagon.xacml.util.Preconditions;

abstract class BaseAttributeValue<T>
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
	public boolean equals(Object o){
		if(o == this){
			return true;
		}
		if(!(o instanceof BaseAttributeValue<?>)){
			return false;
		}
		BaseAttributeValue<?> a = (BaseAttributeValue<?>)o;
		return value.equals(a.getValue()) 
		&& type.equals(a.getType());
	}
	
	@Override
	public String toString(){
		return String.format("%s[type=\"%s\", value=\"%s\"]", 
				getClass().getName(), getType(), getValue());
	}
	
	@Override
	public String toXacmlString() {
		return value.toString();
	}

	@Override
	public int hashCode(){
		return Objects.hashCode(value, getType());
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
