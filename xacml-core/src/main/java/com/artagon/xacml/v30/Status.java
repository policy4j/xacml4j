package com.artagon.xacml.v30;

import com.artagon.xacml.util.Preconditions;

public final class Status 
{
	private StatusId code;
	private String message;
	private String detail;
	
	public Status(StatusId code, 
			String message, String detail){
		Preconditions.checkNotNull(code);
		this.code = code;
		this.message = message;
		this.detail = detail;
	}
	
	public Status(StatusId code){	
		this(code, null, null);
	}
	
	public StatusId getCode(){
		return code;
	}
	
	public boolean isOk(){
		return code == StatusId.OK;
	}
	
	public boolean isFailure(){
		return code != StatusId.OK;
	}
	
	public boolean isProcessingError(){
		return code == StatusId.STATUS_PROCESSING_ERROR;
	}
	
	public boolean isSyntaxError(){
		return code == StatusId.SYNTAX_ERROR;
	}
	
	public String getMessage(){
		return message;
	}
	
	public String getDetail(){
		return detail;
	}
}
