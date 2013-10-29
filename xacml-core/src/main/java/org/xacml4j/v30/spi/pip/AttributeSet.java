package org.xacml4j.v30.spi.pip;

import java.util.Map;
import java.util.Map.Entry;

import org.xacml4j.v30.AttributeDesignatorKey;
import org.xacml4j.v30.BagOfAttributeExp;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Ticker;
import com.google.common.collect.ImmutableMap;

public final class AttributeSet
{
	private long timestamp;
	private ImmutableMap<String, BagOfAttributeExp> values;
	private AttributeResolverDescriptor d;

	private AttributeSet(Builder b){
		this.d = b.d;
		this.values = b.mapBuilder.build();
		this.timestamp = b.ticker.read();
	}

	public static Builder builder(AttributeResolverDescriptor d){
		return new Builder(d);
	}

	public static Builder builder(AttributeResolver r){
		return new Builder(r.getDescriptor());
	}

	/**
	 * Gets attribute resolver descriptor
	 *
	 * @return {@link AttributeResolverDescriptor}
	 */
	public AttributeResolverDescriptor getDescriptor(){
		return d;
	}

	/**
	 * Gets a time when this
	 * attribute set was created
	 *
	 * @return a time stamp in nanoseconds
	 */
	public long getTimestamp(){
		return timestamp;
	}

	/**
	 * Gets issuer for attributes in this set
	 *
	 * @return an issuer for attributes in this set
	 */
	public String getIssuer(){
		return d.getIssuer();
	}

	public Iterable<AttributeDesignatorKey> getAttributeKeys(){
		return d.getAttributesByKey().keySet();
	}

	/**
	 * Gets an attribute value for a given designator
	 *
	 * @param key an attribute designator
	 * @return {@link BagOfAttributeExp}
	 * @exception IllegalArgumentException
	 */
	public BagOfAttributeExp get(AttributeDesignatorKey key)
	{
		Preconditions.checkArgument(d.canResolve(key));
		AttributeDescriptor ad = d.getAttribute(key.getAttributeId());
		Preconditions.checkArgument(ad != null);
		BagOfAttributeExp v = values.get(key.getAttributeId());
		return (v != null)?v:ad.getDataType().emptyBag();
	}

	/**
	 * Gets attribute values from this set by the
	 * attribute identifier
	 *
	 * @param attributeId an attribute identifier
	 * @return {@link BagOfAttributeExp}
	 */
	public BagOfAttributeExp get(String attributeId)
	{
		BagOfAttributeExp v = values.get(attributeId);
		AttributeDescriptor ad = d.getAttribute(attributeId);
		Preconditions.checkState(ad != null);
		return (v != null)?v:ad.getDataType().emptyBag();
	}

	public Iterable<String> getAttributeIds(){
		return d.getProvidedAttributeIds();
	}

	public boolean isEmpty(){
		for(BagOfAttributeExp v : values.values()){
			if(v.isEmpty()){
				continue;
			}
			return false;
		}
		return true;
	}

	public int size(){
		return d.getAttributesCount();
	}

	public Map<String, BagOfAttributeExp> toMap(){
		return values;
	}

	@Override
	public String toString(){
		return Objects.toStringHelper(this)
		.add("id", d.getId())
		.add("issuer", d.getIssuer())
		.add("values", values)
		.toString();
	}

	@Override
	public int hashCode(){
		return Objects.hashCode(d, values);
	}

	public static class Builder
	{
		private Ticker ticker = Ticker.systemTicker();
		private AttributeResolverDescriptor d;
		private ImmutableMap.Builder<String, BagOfAttributeExp> mapBuilder = ImmutableMap.builder();

		Builder(AttributeResolverDescriptor d){
			Preconditions.checkNotNull(d);
			this.d = d;
		}

		public Builder resolver(AttributeResolverDescriptor d){
			Preconditions.checkNotNull(d);
			this.d = d;
			return this;
		}

		public Builder ticker(Ticker t){
			this.ticker = t;
			return this;
		}

		public Builder resolver(AttributeResolver r){
			return resolver(r.getDescriptor());
		}

		public Builder attribute(String id, BagOfAttributeExp value){
			Preconditions.checkNotNull(id);
			Preconditions.checkNotNull(value);
			AttributeDescriptor attrDesc = d.getAttribute(id);

			Preconditions.checkArgument(!(attrDesc == null),
					"Attribute=\"%s\" is not defined by resolver=\"%s\"",
					id, d.getId());
			Preconditions.checkArgument(attrDesc.getDataType().equals(value.getDataType()),
					"Given attribute=\"%s\" value has wrong type=\"%s\", type=\"%s\" is expected",
					id, value.getDataType(), attrDesc.getDataType());
			this.mapBuilder.put(id, value);
			return this;
		}

		public Builder attributes(Map<String, BagOfAttributeExp> attributes){
			if(attributes == null){
				return this;
			}
			for(Entry<String, BagOfAttributeExp> e : attributes.entrySet()){
				attribute(e.getKey(), e.getValue());
			}
			return this;
		}

		public AttributeSet build(){
			return new AttributeSet(this);
		}
	}
}
