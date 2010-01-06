package com.artagon.xacml;

class XacmlCustomXacmlTypeId implements DataTypeId
{
	private String typeId;
	
	public XacmlCustomXacmlTypeId(String id){
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
		XacmlCustomXacmlTypeId d = (XacmlCustomXacmlTypeId)o;
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

