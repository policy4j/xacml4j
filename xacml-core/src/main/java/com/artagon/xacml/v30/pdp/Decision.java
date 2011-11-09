package com.artagon.xacml.v30.pdp;

import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;


public enum Decision 
{
	PERMIT(false, "Permit"),
	DENY(false, "Deny"),
	NOT_APPLICABLE(false, "NotApplicable"),
	INDETERMINATE(true, "Indeterminate"),
	INDETERMINATE_D(true, "Indeterminate"),
	INDETERMINATE_P(true, "Indeterminate"),
	INDETERMINATE_DP(true, "Indeterminate");
	
	private String value;
	private boolean indeterminate;
	
	private final static Map<String, Decision> BY_VALUE = new HashMap<String, Decision>(8);
	
	static{
		BY_VALUE.put(PERMIT.toString(), PERMIT);
		BY_VALUE.put(DENY.toString(), DENY);
		BY_VALUE.put(NOT_APPLICABLE.toString(), NOT_APPLICABLE);
		BY_VALUE.put(INDETERMINATE.toString(), INDETERMINATE);
	}
	
	private Decision(boolean indeterminate, 
			String value){
		this.indeterminate = indeterminate;
		this.value = value;
	}
	
	public boolean isIndeterminate(){
		return indeterminate;
	}
	
	@Override
	public String toString(){
		return value;
	}
	
	/**
	 * Creates {@link Decision} instance
	 * from a given string value
	 * 
	 * @param v a string value
	 * @return {@link Decision}
	 * @exception IllegalArgumentException if given value
	 * can't be converted to the valid {@link Decision}
	 * instance
	 */
	public static Decision parse(String v)
	{
		Preconditions.checkArgument(!Strings.isNullOrEmpty(v), 
				"Decision value can't be null or empty");
		Decision d = BY_VALUE.get(v);
		if(d == null){
			throw new IllegalArgumentException(
					String.format(
							"Given decision value=\"%s\" " +
							"can't be converted to valid decision", v));
		}
		return d;
	}
}
