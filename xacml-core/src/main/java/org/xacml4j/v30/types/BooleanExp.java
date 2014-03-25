package org.xacml4j.v30.types;

import org.xacml4j.v30.BagOfAttributeExp;

import com.google.common.base.Preconditions;

public final class BooleanExp extends
	BaseAttributeExp<Boolean>
{
	private static final long serialVersionUID = -421397689674188254L;

	public final static BooleanExp FALSE = new BooleanExp(Boolean.FALSE);
	public final static BooleanExp TRUE = new BooleanExp(Boolean.TRUE);
	
	private BooleanExp(Boolean value) {
		super(XacmlTypes.BOOLEAN, value);
	}
	
	public static BooleanExp create(boolean val){
		return val?TRUE:FALSE;
	}
	
	public static BooleanExp valueOf(String v){
		return Boolean.parseBoolean(v)?BooleanExp.TRUE:BooleanExp.FALSE;
	}
	
	public static BooleanExp valueOf(Boolean v){
		Preconditions.checkNotNull(v);
		return v?BooleanExp.TRUE:BooleanExp.FALSE;
	}
	
	public StringExp toStringExp(){
		return StringExp.valueOf(Boolean.toString(getValue()));
	}
	
	public static BagOfAttributeExp emptyBag(){
		return XacmlTypes.BOOLEAN.emptyBag();
	}
	
	public static BagOfAttributeExp.Builder bag(){
		return XacmlTypes.BOOLEAN.bag();
	}
}
