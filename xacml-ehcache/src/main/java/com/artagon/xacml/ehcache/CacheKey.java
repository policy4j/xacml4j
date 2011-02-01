package com.artagon.xacml.ehcache;

import java.io.Serializable;
import java.util.List;

import com.artagon.xacml.v30.BagOfAttributeValues;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

/**
 * A key used to identify uniquely resolver
 * resolution result
 * 
 * @author Giedrius Trumpickas
 */
final class CacheKey implements Serializable
{
	private static final long serialVersionUID = -6895205924708410228L;
	
	private String resolverId;
	private List<BagOfAttributeValues> keys;
	
	public CacheKey(String resolverId, 
			List<BagOfAttributeValues> keys){
		Preconditions.checkNotNull(resolverId);
		this.resolverId = resolverId;
		this.keys = keys;
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
		if(!(o instanceof CacheKey)){
			return false;
		}
		CacheKey k = (CacheKey)o;
		return resolverId.equals(k.resolverId) && keys.equals(k.keys);
	}
	
	@Override
	public String toString(){
		return Objects.toStringHelper(this)
		.add("id", resolverId)
		.add("keys", keys.toString())
		.toString();
	}
}
