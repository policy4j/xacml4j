package org.xacml4j.v30.types;

public final class BooleanExp extends
	BaseAttributeExp<Boolean>
{
	private static final long serialVersionUID = -421397689674188254L;

	public final static BooleanExp FALSE = new BooleanExp(Boolean.FALSE);
	public final static BooleanExp TRUE = new BooleanExp(Boolean.TRUE);
	
	private BooleanExp(Boolean value) {
		super(BooleanType.BOOLEAN, value);
	}
	
	public static BooleanExp create(boolean val){
		return val?TRUE:FALSE;
	}
}
