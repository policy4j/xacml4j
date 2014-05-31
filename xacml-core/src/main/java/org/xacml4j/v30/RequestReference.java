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

import org.xacml4j.v30.pdp.RequestSyntaxException;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;

public class RequestReference
{
	private Collection<CategoryReference> references;

	private RequestReference(Builder b){
		this.references = b.refs.build();
	}

	/**
	 * Gets all referenced attributes
	 *
	 * @return collection of referenced attributes
	 */
	public Collection<CategoryReference> getReferencedCategories(){
		return references;
	}

	public static Builder builder(){
		return new Builder();
	}

	@Override
	public int hashCode(){
		return references.hashCode();
	}

	@Override
	public String toString(){
		return Objects
				.toStringHelper(this)
				.add("references", references)
				.toString();
	}

	/**
	 * Resolves this references in the context of the given request
	 *
	 * @param context a request context
	 * @return {@link RequestContext}
	 */
	public RequestContext resolve(RequestContext context)
			throws RequestSyntaxException
	{
		RequestContext.Builder b = RequestContext
				.builder()
				.combineDecision(context.isCombinedDecision())
				.returnPolicyIdList(context.isCombinedDecision())
				.reqDefaults(context.getRequestDefaults());
		for(CategoryReference ref : references){
			Category a = context.getReferencedCategory(ref);
			if(a == null){
				throw new RequestSyntaxException(
						"Failed to resolve attribute reference",
						ref.getReferenceId());
			}
			b.attributes();
		}
		return b.build();
	}

	@Override
	public boolean equals(Object o){
		if(o == this){
			return true;
		}
		if(o == null){
			return false;
		}
		if(!(o instanceof RequestReference)){
			return false;
		}
		RequestReference r = (RequestReference)o;
		return references.equals(r.references);
	}

	public static class Builder
	{
		private ImmutableList.Builder<CategoryReference> refs = ImmutableList.builder();

		public Builder reference(String ... attributeIds){
			for(String attributeId : attributeIds){
				refs.add(CategoryReference.builder().id(attributeId).build());
			}
			return this;
		}

		public Builder reference(Category ... attrs){
			for(Category a : attrs){
				this.refs.add(CategoryReference.builder().from(a).build());
			}
			return this;
		}

		public Builder reference(CategoryReference ... refs){
			for(CategoryReference r : refs){
				this.refs.add(r);
			}
			return this;
		}

		public Builder reference(Iterable<CategoryReference> refs){
			for(CategoryReference r : refs){
				this.refs.add(r);
			}
			return this;
		}

		public RequestReference build(){
			return new RequestReference(this);
		}
	}

}
