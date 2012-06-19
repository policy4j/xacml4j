package com.artagon.xacml.v30;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

public final class Status
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
		return new Status(new StatusCode(StatusCodeIds.OK), null , null);
	}

	public static Status createSyntaxError(String format, Object ...params){
		return new Status(StatusCode.createSyntaxError(),
				(format == null)?null:String.format(format, params), null);
	}

	public static Status createSyntaxError(){
		return createSyntaxError(null, (Object[])null);
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
		return code.isOk();
	}

	public boolean isProcessingError(){
		return code.isFailure();
	}

	public boolean isSyntaxError(){
		return code.isSyntaxError();
	}

	public boolean isMissingAttributeError(){
		return code.isMissingAttributeError();
	}

	public boolean isFailure(){
		return !isSuccess();
	}

	public String getMessage(){
		return message;
	}

	public StatusDetail getDetail(){
		return detail;
	}

	@Override
	public String toString(){
		return Objects.toStringHelper(this)
				.add("code", code)
				.add("message", message)
				.add("detail", detail)
				.toString();
	}

	@Override
	public boolean equals(Object o){
		if(o == this){
			return true;
		}
		if(o == null){
			return false;
		}
		if(!(o instanceof Status)){
			return false;
		}
		Status s = (Status)o;
		return Objects.equal(code, s.code) &&
				Objects.equal(message, s.message) &&
				Objects.equal(detail, s.detail);
	}

	@Override
	public int hashCode(){
		return Objects.hashCode(code, message, detail);
	}
}
