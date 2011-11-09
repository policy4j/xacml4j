package com.artagon.xacml.v30.pdp;

import com.google.common.base.Preconditions;

public class StatusCode extends XacmlObject
{
	private StatusCodeId value;
	private StatusCode minorStatus;
	
	public StatusCode(StatusCodeId value, 
			StatusCode minorStatus){
		Preconditions.checkNotNull(value);
		this.value = value;
		this.minorStatus = minorStatus;
	}
	
	public StatusCode(StatusCodeId value){
		this(value, null);
	}
	
	public static StatusCode createMissingAttribute(){
		return new StatusCode(StatusCodeId.MISSING_ATTRIBUTE);
	}
	
	public static StatusCode createProcessingError(){
		return new StatusCode(StatusCodeId.STATUS_PROCESSING_ERROR);
	}
	
	public static StatusCode createSyntaxError(){
		return new StatusCode(StatusCodeId.SYNTAX_ERROR);
	}
	
	public static StatusCode createOk(){
		return new StatusCode(StatusCodeId.OK);
	}
	
	/**
	 * Gets status code value
	 * 
	 * @return status code value
	 */
	public StatusCodeId getValue(){
		return value;
	}
	
	/**
	 * Gets a minor status code, 
	 * this status code qualifies its 
	 * parent status code
	 * 
	 * @return a minor status code
	 */
	public StatusCode getMinorStatus(){
		return minorStatus;
	}
	
	public boolean isOk(){
		return value == StatusCodeId.OK;
	}
	
	public boolean isFailure(){
		return value != StatusCodeId.OK;
	}
	
	public boolean isProcessingError(){
		return value == StatusCodeId.STATUS_PROCESSING_ERROR;
	}
	
	public boolean isSyntaxError(){
		return value == StatusCodeId.SYNTAX_ERROR;
	}
}
