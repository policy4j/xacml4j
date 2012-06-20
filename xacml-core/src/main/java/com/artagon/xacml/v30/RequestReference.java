package com.artagon.xacml.v30;

import java.util.Collection;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;

public class RequestReference
{
	private Collection<AttributesReference> references;

	/**
	 * Constructs request reference with given referenced attributes
	 *
	 * @param references an attribute references
	 */
	public RequestReference(Collection<AttributesReference> references){
		this.references = ImmutableList.copyOf(references);
	}

	/**
	 * Constructs request reference with given referenced attributes
	 *
	 * @param references an attribute references
	 */
	public RequestReference(AttributesReference ...references){
		this.references = ImmutableList.copyOf(references);
	}

	/**
	 * Gets all referenced attributes
	 *
	 * @return collection of referenced attributes
	 */
	public Collection<AttributesReference> getReferencedAttributes(){
		return references;
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
}
