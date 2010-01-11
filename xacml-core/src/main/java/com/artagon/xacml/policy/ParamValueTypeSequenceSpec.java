package com.artagon.xacml.policy;

import java.util.ListIterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public final class ParamValueTypeSequenceSpec implements ParamSpec
{
	private final static Logger log = LoggerFactory.getLogger(ParamValueTypeSequenceSpec.class);
	
	private Integer min = 0;
	private Integer max = Integer.MAX_VALUE;
	
	private ValueType paramType;
	
	/**
	 * Constructs spec with a given minimum 
	 * and maximum number of parameters
	 * @param min a minimum number
	 * @param max a maximum number
	 * @param paramType an argument type
	 */
	public ParamValueTypeSequenceSpec(Integer min, Integer max, 
			ValueType paramType){
		this.min = min;
		this.max = max;
		this.paramType = paramType;
	}
	
	public ParamValueTypeSequenceSpec(int min, 
			ValueType paramType){
		this(min, null, paramType);
	}
	
	/**
	 * Gets parameter XACML type.
	 * 
	 * @return parameter XACML type
	 */
	public ValueType getParamType(){
		return paramType;
	}
	
	/**
	 * Gets minimum number of parameters 
	 * in this sequence.
	 * 
	 * @return a minimum number of parameters
	 */
	public Integer getMinParams(){
		return min;
	}
	
	public Integer getMaxParams(){
		return max;
	}
	
	@Override
	public boolean isValidParamType(ValueType type){
		return this.paramType.equals(type);
	}
	
	public boolean validate(ListIterator<Expression> it) {	
		int c = 0;
		boolean valid = true;
		while(it.hasNext()){
			Expression exp = it.next();
			ValueType expType = exp.getEvaluatesTo();
			if(!expType.equals(paramType)){
				log.debug("Expected type=\"{}\" but was type=\"{}\"",
						paramType, expType);
				valid = false;
				break;
			}
			c++;
		}
		log.debug("Found \"{}\" parameters", c);
		if(min != null){
			valid &= c >= min;
		}
		if(max != null){
			valid &= c <= max;
		}
		return valid;
	}	
}
