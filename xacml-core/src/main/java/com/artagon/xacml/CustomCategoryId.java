package com.artagon.xacml;

class CustomCategoryId implements CategoryId
{
	private String typeId;
	
	public CustomCategoryId(String id){
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
		CustomCategoryId d = (CustomCategoryId)o;
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
