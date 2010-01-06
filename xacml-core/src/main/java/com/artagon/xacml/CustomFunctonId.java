package com.artagon.xacml;

/**
 * An implementation of {@link FunctionId}
 * for an extension function not defined in the
 * XACML specification
 * 
 * @author Giedrius Trumpickas
 */
class CustomFunctionId implements FunctionId
{
	private String typeId;
	
	public CustomFunctionId(String id){
		this.typeId = id;
	}
	
	@Override
	public boolean equals(Object o){
		if(o == this){
			return true;
		}
		if(!getClass().isInstance(o)){
			return false;
		}
		CustomFunctionId d = (CustomFunctionId)o;
		return typeId.equals(d.typeId);
	}
	
	@Override
	public int hashCode() {
		return typeId.hashCode();
	}
	
	@Override
	public String toString() {
		return typeId.toString();
	}
}
