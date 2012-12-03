package com.artagon.xacml.v30;

import java.util.Collection;

import com.artagon.xacml.v30.pdp.RequestSyntaxException;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;

public class RequestReference
{
	private Collection<AttributesReference> references;

	private RequestReference(Builder b){
		this.references = b.refs.build();
	}

	/**
	 * Gets all referenced attributes
	 *
	 * @return collection of referenced attributes
	 */
	public Collection<AttributesReference> getReferencedAttributes(){
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
		for(AttributesReference ref : references){
			Attributes a = context.getReferencedAttributes(ref);
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
		private ImmutableList.Builder<AttributesReference> refs = ImmutableList.builder();

		public Builder reference(String ... attributeIds){
			for(String attributeId : attributeIds){
				refs.add(AttributesReference.builder().id(attributeId).build());
			}
			return this;
		}

		public Builder reference(Attributes ... attrs){
			for(Attributes a : attrs){
				this.refs.add(AttributesReference.builder().from(a).build());
			}
			return this;
		}

		public Builder reference(AttributesReference ... refs){
			for(AttributesReference r : refs){
				this.refs.add(r);
			}
			return this;
		}

		public Builder reference(Iterable<AttributesReference> refs){
			for(AttributesReference r : refs){
				this.refs.add(r);
			}
			return this;
		}

		public RequestReference build(){
			return new RequestReference(this);
		}
	}

}
