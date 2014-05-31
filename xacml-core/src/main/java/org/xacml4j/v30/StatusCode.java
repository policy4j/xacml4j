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

public class StatusCode
{
	private StatusCodeId value;
	private StatusCode minorStatus;

	private StatusCode(Builder b){
		this.value = b.codeId;
		this.minorStatus = b.minorStatus;
	}

	public static Builder builder(StatusCodeId code){
		return new Builder(code);
	}

	public static StatusCode createProcessingError(){
		return builder(StatusCodeIds.STATUS_PROCESSING_ERROR).build();
	}

	public static StatusCode createMissingAttributeError(){
		return builder(StatusCodeIds.MISSING_ATTRIBUTE).build();
	}

	public static StatusCode createSyntaxError(){
		return builder(StatusCodeIds.SYNTAX_ERROR).build();
	}

	public static StatusCode createOk(){
		return builder(StatusCodeIds.OK).build();
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

	public static class Builder
	{
		private StatusCodeId codeId;
		private StatusCode minorStatus;

		private Builder(StatusCodeId id){
			Preconditions.checkNotNull(id);
			this.codeId = id;
		}

		public Builder minorStatus(StatusCode c){
			this.minorStatus = c;
			return this;
		}

		public Builder minorStatus(Builder b){
			this.minorStatus = b.build();
			return this;
		}

		public StatusCode build(){
			return new StatusCode(this);
		}
	}
}
