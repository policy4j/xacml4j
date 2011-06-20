package com.artagon.xacml.v30.spi.pip;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.artagon.xacml.v30.AttributeDesignatorKey;
import com.artagon.xacml.v30.BagOfAttributeValues;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

public final class AttributeSet 
{
	private Map<String, BagOfAttributeValues> values;
	private AttributeResolverDescriptor d;
	
	public AttributeSet(
			AttributeResolverDescriptor d){
		this(d, Collections.<String, BagOfAttributeValues>emptyMap());
	}
	
	public AttributeSet(
			AttributeResolverDescriptor d, 
			Map<String, BagOfAttributeValues> values){
		Preconditions.checkNotNull(d);
		Preconditions.checkNotNull(values);
		this.d = d;
		this.values = new HashMap<String, BagOfAttributeValues>(values.size());
		for(Entry<String, BagOfAttributeValues> e : values.entrySet()){
			AttributeDescriptor ad = d.getAttribute(e.getKey());
			Preconditions.checkArgument(ad != null);
			Preconditions.checkArgument(ad.getDataType().equals(e.getValue().getDataType()));
			this.values.put(e.getKey(), e.getValue());
		}
	}
	
	public AttributeResolverDescriptor getDescriptor(){
		return d;
	}
	
	public Iterable<AttributeDesignatorKey> getAttributeKeys(){
		return d.getAttributesByKey().keySet();
	}
	
	public BagOfAttributeValues get(AttributeDesignatorKey key)
	{
		Preconditions.checkState(d.canResolve(key));
		AttributeDescriptor ad = d.getAttribute(key.getAttributeId());
		Preconditions.checkState(ad != null);
		BagOfAttributeValues v = values.get(key.getAttributeId());
		return (v != null)?v:ad.getDataType().emptyBag();
	}
	
	/**
	 * Gets attribute values from this set
	 * 
	 * @param attributeId an attribute identifier
	 * @return {@link BagOfAttributeValues}
	 */
	public BagOfAttributeValues get(String attributeId)
	{
		BagOfAttributeValues v = values.get(attributeId);
		AttributeDescriptor ad = d.getAttribute(attributeId);
		Preconditions.checkState(ad != null);
		return (v != null)?v:ad.getDataType().emptyBag();
	}
	
	public Iterable<String> getAttributeIds(){
		return d.getProvidedAttributeIds();
	}
	
	public int size(){
		return d.getAttributesCount();
	}
	
	public Map<String, BagOfAttributeValues> toMap()
	{
		Map<String, BagOfAttributeValues> v = new HashMap<String, BagOfAttributeValues>(size());
		for(String attributeId : getAttributeIds()){
			v.put(attributeId, get(attributeId));
		}
		return v;
	}
	
	@Override
	public String toString(){
		return Objects.toStringHelper(this)
		.add("id", d.getId())
		.add("issuer", d.getIssuer())
		.add("attributes", values)
		.toString();
	}
	
	@Override
	public int hashCode(){
		return Objects.hashCode(d, values);
	}
}
