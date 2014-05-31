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

import java.util.Collection;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

public class ResponseContext
{
	private Collection<Result> results;

	private ResponseContext(Builder builder){
		this.results = builder.b.build();
	}

	public static Builder builder(){
		return new Builder();
	}

	public Collection<Result> getResults(){
		return results;
	}

	@Override
	public int hashCode(){
		return results.hashCode();
	}

	@Override
	public String toString(){
		return Objects
				.toStringHelper(this)
				.add("results", results)
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
		if(!(o instanceof ResponseContext)){
			return false;
		}
		ResponseContext r = (ResponseContext)o;
		return results.equals(r.results);
	}

	public static class Builder
	{
		private ImmutableList.Builder<Result> b = ImmutableList.builder();

		public Builder result(Result ... r){
			Preconditions.checkNotNull(r);
			this.b.add(r);
			return this;
		}

		public Builder results(Iterable<Result> r){
			Preconditions.checkNotNull(r);
			this.b.addAll(r);
			return this;
		}

		public ResponseContext build(){
			return new ResponseContext(this);
		}
	}
}
