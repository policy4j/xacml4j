package org.xacml4j.v30.types;

import org.xacml4j.v30.BagOfAttributeExp;

import com.google.common.base.Preconditions;


public final class IntegerExp
	extends BaseAttributeExp<Long>
{
	private static final long serialVersionUID = 6654857010399020496L;

	private IntegerExp(Long value) {
		super(XacmlTypes.INTEGER, value);
	}
	
	public static IntegerExp valueOf(Number value){
		return new IntegerExp(value.longValue());
	}
	
	public static IntegerExp valueOf(String v){
		Preconditions.checkNotNull(v);
		if ((v.length() >= 1) &&
        		(v.charAt(0) == '+')){
			v = v.substring(1);
		}
		return new IntegerExp(Long.parseLong(v));
	}
	
	public StringExp toStringExp(){
		return StringExp.valueOf(getValue().toString());
	}
	
	public static BagOfAttributeExp emptyBag(){
		return XacmlTypes.INTEGER.emptyBag();
	}
	
	public static BagOfAttributeExp.Builder bag(){
		return XacmlTypes.INTEGER.bag();
	}
	
	public IntegerExp add(IntegerExp d){
		return  new IntegerExp(getValue() + d.getValue());
	}

	public IntegerExp substract(IntegerExp d){
		return  new IntegerExp(getValue() - d.getValue());
	}

	public IntegerExp multiply(IntegerExp d){
		return  new IntegerExp(getValue() * d.getValue());
	}

	public DoubleExp divide(IntegerExp d){
		Preconditions.checkArgument(d.getValue() != null);
		return DoubleExp.valueOf(getValue() / d.getValue());
	}
	
	public IntegerExp mod(IntegerExp d){
		Preconditions.checkArgument(d.getValue() != null);
		return IntegerExp.valueOf(getValue() % d.getValue());
	}
}

