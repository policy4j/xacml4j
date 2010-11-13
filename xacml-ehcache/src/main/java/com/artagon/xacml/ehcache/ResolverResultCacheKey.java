package com.artagon.xacml.ehcache;

import java.io.Serializable;
import java.util.Arrays;

import com.artagon.xacml.v3.BagOfAttributeValues;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

/**
 * A key used to identify uniquely resolver
 * resolution result
 * 
 * @author Giedrius Trumpickas
 */
final class ResolverResultCacheKey implements Serializable
{
	private static final long serialVersionUID = -6895205924708410228L;
	
	private String resolverId;
	private BagOfAttributeValues[] keys;
	
	public ResolverResultCacheKey(String resolverId, 
			BagOfAttributeValues[] keys){
		Preconditions.checkNotNull(resolverId);
		this.resolverId = resolverId;
		this.keys = keys;
	}
	
	public String getResolvedId(){
		return resolverId;
	}
	
	public BagOfAttributeValues[] getKeys(){
		return keys;
	}
	
	@Override
	public int hashCode(){
		return Objects.hashCode(resolverId, keys);
	}
	
	@Override
	public boolean equals(Object o)
	{
		if(o == this){
			return true;
		}
		if(o == null){
			return false;
		}
		if(!(o instanceof ResolverResultCacheKey)){
			return false;
		}
		ResolverResultCacheKey k = (ResolverResultCacheKey)o;
		return resolverId.equals(k.resolverId) && 
		Arrays.equals(keys, k.keys);
	}
	
	@Override
	public String toString(){
		return Objects.toStringHelper(this)
		.add("id", resolverId)
		.add("keys", Arrays.toString(keys))
		.toString();
	}
}
