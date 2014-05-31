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

public class CategoryReference
{
	private String referenceId;

	private CategoryReference(Builder b){
		Preconditions.checkNotNull(b.id);
		this.referenceId = b.id;
	}

	public static Builder builder(){
		return new Builder();
	}

	/**
	 * Gets an attribute reference
	 * identifier
	 *
	 * @return reference identifier
	 */
	public String getReferenceId(){
		return referenceId;
	}

	@Override
	public String toString(){
		return Objects.toStringHelper(this)
		.add("referenceId", referenceId)
		.toString();
	}

	@Override
	public int hashCode(){
		return referenceId.hashCode();
	}

	@Override
	public boolean equals(Object o){
		if(o == this){
			return true;
		}
		if(o == null){
			return false;
		}
		if(!(o instanceof CategoryReference)){
			return false;
		}
		CategoryReference r = (CategoryReference)o;
		return referenceId.equals(r.referenceId);
	}

	public static class Builder
	{
		private String id;

		public Builder id(String id){
			Preconditions.checkArgument(!Strings.isNullOrEmpty(id),
					"Attribute id can't be null or empty");
			this.id = id;
			return this;
		}

		public Builder from(Category a){
			return id(a.getId());
		}

		public CategoryReference build(){
			Preconditions.checkState(!Strings.isNullOrEmpty(id),
					"Attribute id can't be null or empty");
			return new CategoryReference(this);
		}
	}

}
