package com.artagon.xacml.v3;

import com.google.common.base.Preconditions;

public final class Status extends XacmlObject
{
	private StatusCode code;
	private String message;
	private String detail;
	
	/**
	 * Creates status with a given status
	 * code, message and detailed message
	 * 
	 * @param code a status code
	 * @param message a message
	 * @param detail a detailed
	 * description
	 */
	public Status(StatusCode code, 
			String message, String detail){
		Preconditions.checkNotNull(code);
		this.code = code;
		this.message = message;
		this.detail = detail;
	}
	
	public Status(StatusCode code, String message){
		this(code, message, null);
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
