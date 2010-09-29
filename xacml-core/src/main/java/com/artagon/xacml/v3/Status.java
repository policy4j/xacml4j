package com.artagon.xacml.v3;

import com.google.common.base.Preconditions;

public final class Status extends XacmlObject
{
	private StatusCode code;
	private String message;
	private StatusDetail detail;
	
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
			String message, StatusDetail detail){
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
	
	public static Status createSuccess(){
		return new Status(new StatusCode(StatusCodeId.OK), null , null);
	}
	
	public static Status createSyntaxError(String format, Object ...params){
		return new Status(StatusCode.createSyntaxError(), 
				(format == null)?null:String.format(format, params), null);
	}
	
	public static Status createProcessingError(String format, Object ...params){
		return new Status(StatusCode.createProcessingError(), 
				(format == null)?null:String.format(format, params), null);
	}
	
	public static Status createProcessingError(){
		return createProcessingError(null);
	}
	
	public static Status createMissingAttribute(String format, Object ...params){
		return new Status(StatusCode.createMissingAttribute(), 
				(format == null)?null:String.format(format, params), null);
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
	
	public StatusDetail getDetail(){
		return detail;
	}
}
