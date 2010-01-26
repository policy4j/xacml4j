package com.artagon.xacml.v30.policy.function;

import java.util.ListIterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.artagon.xacml.util.Preconditions;
import com.artagon.xacml.v30.policy.Expression;
import com.artagon.xacml.v30.policy.ParamSpec;
import com.artagon.xacml.v30.policy.ValueType;

public final class ParamValueTypeSpec implements ParamSpec
{
	private final static Logger log = LoggerFactory.getLogger(ParamValueTypeSpec.class);
	
	private ValueType type;
	
	public ParamValueTypeSpec(ValueType type){
		Preconditions.checkNotNull(type);
		this.type = type;
	}
	
	public ValueType getParamType(){
		return type;
	}
	
	
	public boolean validate(ListIterator<Expression> it) {
		if(!it.hasNext()){
			log.debug("Parameter iterator at index=\"{}\" does not " +
					"have any elements left to iterate", it.previousIndex());
			return false;
		}
		Expression exp = it.next();
		log.debug("Validating expression=\"{}\" " +
				"against paramSpec=\"{}\"", exp, this);
		ValueType expType = exp.getEvaluatesTo();
		boolean valid = type.equals(expType);
		if(log.isDebugEnabled()){
			log.debug("Validating parameter at index=\"{}\" " +
					"is valid=\"{}\"", it.previousIndex(), valid);
			log.debug("Expecting parameter of type=\"{}\" found=\"{}\"", 
					type, expType);
		}
		return valid;
	}
	
	@Override
	public boolean isValidParamType(ValueType type) {
		return this.type.equals(type);
	}

}
