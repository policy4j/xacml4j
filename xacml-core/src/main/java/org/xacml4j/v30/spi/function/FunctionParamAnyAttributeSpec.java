package org.xacml4j.v30.spi.function;

import java.util.ListIterator;

import org.xacml4j.v30.AttributeExpType;
import org.xacml4j.v30.Expression;
import org.xacml4j.v30.ValueType;
import org.xacml4j.v30.pdp.FunctionParamSpec;

import com.google.common.base.Objects;


final class FunctionParamAnyAttributeSpec implements FunctionParamSpec
{
	private boolean optional = false;
	
	public FunctionParamAnyAttributeSpec(boolean optional){
		this.optional = optional;
	}
	
	@Override
	public boolean isValidParamType(ValueType type) {
		return (type instanceof AttributeExpType);
	}

	@Override
	public boolean isVariadic() {
		return false;
	}

	@Override
	public boolean validate(ListIterator<Expression> it) {
		if(!it.hasNext()){
			return false;
		}
		Expression exp = it.next();
		if(exp == null){
			return optional;
		}
		return isValidParamType(exp.getEvaluatesTo());
	}

	public String toString(){
		return Objects.
				toStringHelper(this)
				.toString();
	}

	@Override
	public int hashCode(){
		return 0;
	}

	@Override
	public boolean equals(Object o){
		if(o == this){
			return true;
		}
		return (o instanceof FunctionParamAnyAttributeSpec);
	}

}
