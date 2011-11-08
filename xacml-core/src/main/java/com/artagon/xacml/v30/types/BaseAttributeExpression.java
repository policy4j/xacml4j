package com.artagon.xacml.v30.types;

import com.artagon.xacml.v30.AttributeExp;
import com.artagon.xacml.v30.AttributeExpType;
import com.artagon.xacml.v30.EvaluationContext;
import com.artagon.xacml.v30.EvaluationException;
import com.artagon.xacml.v30.ExpressionVisitor;
import com.artagon.xacml.v30.ValueType;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

public abstract class BaseAttributeExpression<T>
	implements AttributeExp
{
	private static final long serialVersionUID = 4131180767511036271L;
	
	private T value;
	private AttributeExpType type;
	
	protected BaseAttributeExpression(AttributeExpType type, 
			T value) {
		Preconditions.checkNotNull(type);
		Preconditions.checkNotNull(value);
		this.type = type;
		this.value = value;
	}

	@Override
	public final ValueType getEvaluatesTo(){
		return type;
	}
	
	@Override
	public final AttributeExpType getType(){
		return type;
	}

	@Override
	public final AttributeExp evaluate(
			EvaluationContext context) throws EvaluationException {
		return this;
	}
	
	@Override
	public String toXacmlString() {
		return value.toString();
	}
	
	public final T getValue(){
		return value;
	}
	
	@Override
	public String toString() {
		return Objects.toStringHelper(this).
		add("Value", value).
		add("Type", getType()).toString();
	}
	
	@Override
	public int hashCode(){
		return Objects.hashCode(
				getType(), value);
	}
	
	@Override
	public boolean equals(Object o){
		if(o == null){
			return false;
		}
		if(o == this){
			return true;
		}
		if(!(o instanceof AttributeExp)){
			return false;
		}
		AttributeExp e = (AttributeExp)o;
		return type.equals(e.getType()) && 
				value.equals(e.getValue());
	}
	
	@Override
	public final void accept(ExpressionVisitor v) {
		v.visitEnter(this);
		v.visitLeave(this);
	}
}
