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

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

public class ResponseContext
{
	private static final ResponseContext PERMIT = new ResponseContext(ImmutableList.of(Result.ok(Decision.PERMIT).build()));
	private static final ResponseContext DENY = new ResponseContext(ImmutableList.of(Result.ok(Decision.DENY).build()));
	private static final ResponseContext NOT_APPLICABLE = new ResponseContext(ImmutableList.of(Result.ok(Decision.NOT_APPLICABLE).build()));

	private final ImmutableList<Result> results;

	private ResponseContext(Builder builder){
		this(builder.b != null ? builder.b.build()
				: builder.copied != null ? builder.copied : ImmutableList.of());
	}

	private ResponseContext(ImmutableList<Result> results){
		this.results = results;
	}

	/** A response with one result. Plain successful results may share an immutable response. */
	public static ResponseContext of(Result result){
		Preconditions.checkNotNull(result);
		return switch(result.getDecision()){
			case PERMIT -> single(result, PERMIT);
			case DENY -> single(result, DENY);
			case NOT_APPLICABLE -> single(result, NOT_APPLICABLE);
			default -> new ResponseContext(ImmutableList.of(result));
		};
	}

	private static ResponseContext single(Result result, ResponseContext plain){
		return result.getStatus() == plain.results.get(0).getStatus()
				&& result.getIncludeInResultAttributes().isEmpty()
				&& result.getObligations().isEmpty() && result.getAssociatedAdvice().isEmpty()
				&& result.getPolicyIdentifiers().isEmpty() && result.getResolvedAttributes().isEmpty()
				? plain : new ResponseContext(ImmutableList.of(result));
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
		return MoreObjects
				.toStringHelper(this)
				.add("results", results)
				.toString();
	}

	@Override
	public boolean equals(Object o){
		if (o == this) {
			return true;
		}
		if (!(o instanceof ResponseContext)) {
			return false;
		}
		ResponseContext r = (ResponseContext)o;
		return results.equals(r.results);
	}

	public static class Builder
	{
		// The one collection given to a fresh builder is kept as it is; a list builder is
		// created by the first add that cannot be kept that way (design D32)
		private ImmutableList<Result> copied;
		private ImmutableList.Builder<Result> b;

		public Builder result(Result ... r){
			Preconditions.checkNotNull(r);
			builder().add(r);
			return this;
		}

		public Builder results(Iterable<Result> r){
			Preconditions.checkNotNull(r);
			if(b == null && copied == null){
				copied = ImmutableList.copyOf(r);
				return this;
			}
			builder().addAll(r);
			return this;
		}

		private ImmutableList.Builder<Result> builder(){
			if(b == null){
				b = ImmutableList.builder();
				if(copied != null){
					b.addAll(copied);
					copied = null;
				}
			}
			return b;
		}

		public ResponseContext build(){
			return new ResponseContext(this);
		}
	}
}
