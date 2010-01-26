package com.artagon.xacml;

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
	
	public StatusId getCode(){
		return code;
	}
	
	public String getMessage(){
		return message;
	}
	
	public String getDetail(){
		return detail;
	}
}
