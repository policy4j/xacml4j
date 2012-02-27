package com.artagon.xacml.v30;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

public class StatusCode
{
	private StatusCodeId value;
	private StatusCode minorStatus;
	
	public StatusCode(StatusCodeId value, 
			StatusCode minorStatus){
		Preconditions.checkNotNull(value);
		this.value = value;
		this.minorStatus = minorStatus;
	}
	
	public StatusCode(StatusCodeIds value){
		this(value, null);
	}
	
	public static StatusCode createMissingAttribute(){
		return new StatusCode(StatusCodeIds.MISSING_ATTRIBUTE);
	}
	
	public static StatusCode createProcessingError(){
		return new StatusCode(StatusCodeIds.STATUS_PROCESSING_ERROR);
	}
	
	public static StatusCode createSyntaxError(){
		return new StatusCode(StatusCodeIds.SYNTAX_ERROR);
	}
	
	public static StatusCode createOk(){
		return new StatusCode(StatusCodeIds.OK);
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
		return value.equals(StatusCodeIds.OK);
	}
	
	public boolean isFailure(){
		return !isOk();
	}
	
	public boolean isProcessingError(){
		return value.equals(StatusCodeIds.STATUS_PROCESSING_ERROR);
	}
	
	public boolean isMissingAttributeError(){
		return value.equals(StatusCodeIds.MISSING_ATTRIBUTE);
	}
	
	public boolean isSyntaxError(){
		return value.equals(StatusCodeIds.SYNTAX_ERROR);
	}
	
	@Override
	public String toString(){
		return Objects.toStringHelper(this)
				.add("code", value)
				.add("minorStatus", minorStatus)
				.toString();
	}
	
	@Override
	public int hashCode(){
		return Objects.hashCode(value, minorStatus);
	}
	
	@Override
	public boolean equals(Object o){
		if(o == null){
			return false;
		}
		if(o == this){
			return true;
		}
		if(!(o instanceof StatusCode)){
			return false;
		}
		StatusCode s = (StatusCode)o;
		return Objects.equal(value, s.value) 
				&& Objects.equal(minorStatus, s.minorStatus);
	}
}
