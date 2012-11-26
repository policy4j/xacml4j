package com.artagon.xacml.v30;

import java.util.Collection;
import java.util.Collections;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.Multiset;

/**
 * A XACML request context attribute
 *
 * @author Giedrius Trumpickas
 */
public class Attribute
{
	private String attributeId;
	private Multiset<AttributeExp> values;
	private boolean includeInResult;
	private String issuer;

	private Attribute(Builder b){
		Preconditions.checkArgument(!Strings.isNullOrEmpty(b.attributeId));
		this.attributeId = b.attributeId;
		this.includeInResult = b.includeInResult;
		this.issuer = b.issuer;
		this.values = b.valueBuilder.build();
	}

	public static Builder builder(String attributeId){
		return new Builder(attributeId);
	}

	/**
	 * Gets this attribute issuer
	 *
	 * @return issuer of this attribute
	 * identifier or <code>null</code>
	 * if it's not available
	 */
	public String getIssuer(){
		return issuer;
	}

	/**
	 * Tests if this attribute needs
	 * to be included back to the
	 * evaluation result
	 *
	 * @return <code>true</code>
	 * if needs to be included
	 */
	public boolean isIncludeInResult(){
		return includeInResult;
	}

	/**
	 * Gets attribute identifier.
	 *
	 * @return attribute identifier
	 */
	public String getAttributeId(){
		return attributeId;
	}

	/**
	 * Gets attribute values as collection of
	 * {@link AttributeExp} instances
	 *
	 * @return collection of {@link AttributeExp}
	 * instances
	 */
	public Collection<AttributeExp> getValues(){
		return Collections.unmodifiableCollection(values);
	}

	/**
	 * Gets all instances of {@link AttributeExp} by type
	 *
	 * @param type an attribute type
	 * @return a collection of {@link AttributeExp} of given type
	 */
	public Collection<AttributeExp> getValuesByType(final AttributeExpType type)
	{
		return Collections2.filter(values, new Predicate<AttributeExp>() {
			@Override
			public boolean apply(AttributeExp a) {
				return a.getType().equals(type);
			}

		});
	}

	@Override
	public final String toString(){
		return Objects.toStringHelper(this)
		.add("AttributeId", attributeId)
		.add("Issuer", issuer)
		.add("IncludeInResult", includeInResult)
		.add("Values", values).toString();
	}

	@Override
	public final int hashCode(){
		return Objects.hashCode(
				attributeId, issuer, includeInResult, values);
	}

	@Override
	public final boolean equals(Object o){
		if(o == this){
			return true;
		}
		if(o == null){
			return false;
		}
		if(!(o instanceof Attribute)){
			return false;
		}
		Attribute a = (Attribute)o;
		return Objects.equal(attributeId, a.attributeId) &&
			(!(includeInResult ^ a.includeInResult)) &&
			Objects.equal(issuer, a.issuer) && values.equals(a.values);

	}

	public static class Builder
	{
		private String attributeId;
		private String issuer;
		private boolean includeInResult;
		private ImmutableMultiset.Builder<AttributeExp> valueBuilder;

		private Builder(String attributeId){
			Preconditions.checkArgument(!Strings.isNullOrEmpty(attributeId));
			this.attributeId = attributeId;
			this.valueBuilder = ImmutableMultiset.builder();
		}

		public Builder issuer(String issuer){
			this.issuer = Strings.emptyToNull(issuer);
			return this;
		}

		public Builder includeInResult(boolean include){
			this.includeInResult = include;
			return this;
		}

		public Builder noValues(){
			this.valueBuilder = ImmutableMultiset.builder();
			return this;
		}

		public Builder value(AttributeExp ...values){
			Preconditions.checkNotNull(values);
			for(AttributeExp v : values){
				valueBuilder.add(v);
			}
			return this;
		}

		public Builder value(Iterable<AttributeExp> values){
			Preconditions.checkNotNull(values);
			for(AttributeExp v : values){
				valueBuilder.add(v);
			}
			return this;
		}


		public Builder value(AttributeExpType type, Iterable<Object> values){
			Preconditions.checkNotNull(type);
			Preconditions.checkNotNull(values);
			for(Object v : values){
				valueBuilder.add(type.create(v));
			}
			return this;
		}

		public Builder value(AttributeExpType type, Object...values){
			Preconditions.checkNotNull(type);
			Preconditions.checkNotNull(values);
			for(Object v : values){
				valueBuilder.add(type.create(v));
			}
			return this;
		}

		public Attribute build(){
			return new Attribute(this);
		}

	}
}
