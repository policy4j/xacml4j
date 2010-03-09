package com.artagon.xacml.v3;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.artagon.xacml.util.Preconditions;

public class StatusCode extends XacmlObject
{
	private StatusCodeId value;
	private List<StatusCode> minorStatus;
	
	public StatusCode(StatusCodeId value, 
			StatusCode ...minorStatus){
		Preconditions.checkNotNull(value);
		this.value = value;
		this.minorStatus = (minorStatus == null)?
				Collections.<StatusCode>emptyList():Arrays.asList(minorStatus);
	}
	
	public StatusCode(StatusCodeId value){
		this(value, (StatusCode[])null);
	}
	
	public StatusCodeId getValue(){
		return value;
	}
	
	public Iterable<StatusCode> getMinorStatus(){
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
