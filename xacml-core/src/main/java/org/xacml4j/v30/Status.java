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


import com.google.common.base.MoreObjects;
import com.google.common.base.Throwables;
import org.xacml4j.util.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Represents XACML status
 *
 * @see  <a href="http://docs.oasis-open.org/xacml/3.0/errata01/os/xacml-3.0-core-spec-errata01-os-complete.html#_Toc489959562"/a>
 *
 * @author Giedrius Trumpickas
 */
public final class Status
{
	private final StatusCode code;
	private final String message;
	private final StatusDetail detail;
	private final Throwable error;

	private int hashCode;

	/**
	 * Creates status with a given status
	 * code, message and detailed message
	 *
	 * @param b Status builder
	 */
	public Status(Builder b){
		Objects.requireNonNull(b);
		this.code = java.util.Objects.requireNonNull(b.code, "statusCode");
		this.message = b.message.orElse(null);
		this.detail = b.detail.orElse(null);
		this.error = b.error.orElse(null);
	}

	public static Builder processingError(){
		return new Builder().status(StatusCode.processingError());
	}

	public static Builder processingError(Throwable t){
		return new Builder()
				.status(StatusCode.processingError())
				.message(t.getMessage())
				.error(t);
	}

	public static Builder builder(StatusCode code){
		return new Builder()
				.status(java.util.Objects.requireNonNull(
				code,"statusCode"));
	}

	public static Builder from(Status status){
		return new Builder()
				.from(Optional.ofNullable(status));
	}

	public static Builder from(java.util.Optional<Status> status){
		return new Builder().from(status);
	}

	public static Builder syntaxError(){
		return new Builder().status(
				StatusCode.syntaxError());
	}

	public static Builder syntaxError(Throwable e){
		return new Builder()
				.status(StatusCode.syntaxError())
				.error(e);
	}

	public static Builder ok(){
		return new Builder()
				.status(StatusCode.ok());
	}

	public static Builder missingAttribute(AttributeDesignatorKey key){
		return new Builder()
				.status(StatusCode.missingAttributeError())
				.message(key.getAttributeId());
	}

	public static Builder missingAttribute(AttributeSelectorKey key){
		return new Builder()
				.status(StatusCode.missingAttributeError())
				.message(key.getPath());
	}

	public boolean hasDetails(){
		return detail != null;
	}

	public StatusCode getStatusCode(){
		return code;
	}

	public Optional<Throwable> getError(){
		return Optional.ofNullable(error);
	}

	public boolean isSuccess(){
		return code.isOk();
	}

	public boolean isFailure(){
		return code.isFailure();
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


	public Optional<String> getMessage(){
		return Optional.ofNullable(message);
	}

	public java.util.Optional<StatusDetail> getDetail(){
		return Optional.ofNullable(detail);
	}

	@Override
	public String toString(){
		return MoreObjects.toStringHelper(this)
				.add("code", code)
				.add("message", message)
				.add("detail", detail)
				.add("error", error)
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
		return code.equals(s.code) &&
				Objects.equals(message, s.message) &&
				Objects.equals(detail, s.detail);
	}

	@Override
	public int hashCode() {
		if (hashCode == 0) {
			this.hashCode = Objects
					.hash(code, message, detail);
		}
		return hashCode;
	}

	public static class Builder
	{
		private StatusCode code;
		private Optional<String> message = Optional.empty();
		private Optional<StatusDetail> detail = Optional.empty();
		private Optional<Throwable> error = Optional.empty();

		public Builder from(java.util.Optional<Status> status){
			this.code = status.map(v->v.getStatusCode()).orElse(null);
			this.message = status.map(v->v.getMessage()).orElse(null);
			this.detail = status.map(v->v.getDetail()).orElse(null);
			this.error = status.map(v->v.getError()).orElse(null);
			return this;
		}

		public Builder from(Status status) {
			return from(java.util.Optional.ofNullable(status));
		}

		public Builder ok(){
			this.code = StatusCode.ok();
			return this;
		}

		public Builder status(StatusCode code){
			this.code = code;
			return this;
		}

		public Builder message(String format, Object ...args){
			this.message = Optional.ofNullable(
					StringUtils.isNullOrEmpty(format) ? format : String.format(format, args));
			return this;
		}

		public Builder detail(StatusDetail detail){
			this.detail = Optional.ofNullable(detail);
			return this;
		}

		public Builder detail(Status ...details){
			return detail(new StatusDetail(details));
		}

		public Builder detail(List<Status> details){
			return detail(new StatusDetail(details));
		}

		public Builder error(Throwable t){

			return this;
		}

		public Status build(){
			return new Status(this);
		}
	}
}
