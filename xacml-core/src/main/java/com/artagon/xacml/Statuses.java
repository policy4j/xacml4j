package com.artagon.xacml;

public enum Statuses implements StatusId
{
	OK("urn:oasis:names:tc:xacml:1.0:status:ok"), 
	MISSING_ATTRIBUTE("urn:oasis:names:tc:xacml:1.0:status:missing-attribute"),
	SYNTAX_ERROR("urn:oasis:names:tc:xacml:1.0:status:syntax-error"),
	STATUS_PROCESSING_ERROR("urn:oasis:names:tc:xacml:1.0:status:processing-error");

	private String status;
	
	private Statuses(String status){
		this.status = status;
	}
	
	@Override
	public String toString(){
		return status;
	}
}
