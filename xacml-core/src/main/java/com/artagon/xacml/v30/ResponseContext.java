package com.artagon.xacml.v30;

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
