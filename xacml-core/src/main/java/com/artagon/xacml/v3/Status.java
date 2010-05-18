package com.artagon.xacml.v3;

import com.google.common.base.Preconditions;

public final class Status extends XacmlObject
{
	private StatusCode code;
	private String message;
	private String detail;
	
	public Status(StatusCode code, 
			String message, String detail){
		Preconditions.checkNotNull(code);
		this.code = code;
		this.message = message;
		this.detail = detail;
	}
	
	public Status(StatusCode code){
		this(code, null, null);
	}
	
	public static Status createSuccessStatus(){
		return new Status(new StatusCode(StatusCodeId.OK), null , null);
	}
	
	public StatusCode getStatusCode(){
		return code;
	}
	
	public boolean isSuccess(){
		return code.getValue() == StatusCodeId.OK;
	}
	
	public boolean isFailure(){
		return code.getValue() != StatusCodeId.OK;
	}
	
	public String getMessage(){
		return message;
	}
	
	public String getDetail(){
		return detail;
	}
}
