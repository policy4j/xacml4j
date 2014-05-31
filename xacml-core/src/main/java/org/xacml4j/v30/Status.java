package org.xacml4j.v30;

/*
 * #%L
 * Xacml4J Core Engine Implementation
 * %%
 * Copyright (C) 2009 - 2014 Xacml4J.org
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

public final class Status
{
	private final StatusCode code;
	private final String message;
	private final StatusDetail detail;

	/**
	 * Creates status with a given status
	 * code, message and detailed message
	 *
	 * @param b Status builder
	 */
	public Status(Builder b){
		Preconditions.checkNotNull(b);
		this.code = b.code;
		this.message = b.message;
		this.detail = b.detail;
	}

	public static Builder processingError(){
		return new Builder().statusCode(StatusCode.createProcessingError());
	}

	public static Builder builder(StatusCode code){
		return new Builder().statusCode(code);
	}

	public static Builder syntaxError(){
		return new Builder().statusCode(StatusCode.createSyntaxError());
	}

	public static Builder ok(){
		return new Builder().statusCode(StatusCode.createOk());
	}

	public static Builder missingAttribute(AttributeDesignatorKey key){
		return new Builder()
		.statusCode(StatusCode.createMissingAttributeError())
		.message(key.getAttributeId());
	}

	public static Builder missingAttribute(AttributeSelectorKey key){
		return new Builder()
			.statusCode(StatusCode.createMissingAttributeError())
			.message(key.getPath());
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

	public static class Builder
	{
		private StatusCode code;
		private String message;
		private StatusDetail detail;


		public Builder ok(){
			this.code = StatusCode.createProcessingError();
			return this;
		}

		public Builder statusCode(StatusCode code){
			Preconditions.checkNotNull(code);
			this.code = code;
			return this;
		}

		public Builder message(String format, Object ...args){
			this.message = (Strings.isNullOrEmpty(format))?format:String.format(format, args);
			return this;
		}

		public Builder detail(StatusDetail detail){
			this.detail = detail;
			return this;
		}

		public Status build(){
			return new Status(this);
		}
	}
}
