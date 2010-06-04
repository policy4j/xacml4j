package com.artagon.xacml.v3.context;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.artagon.xacml.v3.XacmlObject;
import com.google.common.base.Preconditions;

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
	
	public static StatusCode createMissingAttribute(){
		return new StatusCode(StatusCodeId.MISSING_ATTRIBUTE);
	}
	
	public static StatusCode createProcessingError(){
		return new StatusCode(StatusCodeId.STATUS_PROCESSING_ERROR);
	}
	
	public static StatusCode createSyntaxError(){
		return new StatusCode(StatusCodeId.SYNTAX_ERROR);
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
