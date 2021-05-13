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

import org.xacml4j.v30.types.EntityExp;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableMultiset;

/**
 * A XACML request context attribute
 *
 * @author Giedrius Trumpickas
 */
public class Attribute
{
	private final String attributeId;
	private final ImmutableMultiset<AttributeExp> values;
	private final boolean includeInResult;
	private final String issuer;

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
	 * identifier or {@code null}
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
	 * @return {@code true} if the attribute needs to be included
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
		return values;
	}

	/**
	 * Gets all instances of {@link AttributeExp} by type
	 *
	 * @param type an attribute type
	 * @return a collection of {@link AttributeExp} of given type
	 */
	public Collection<AttributeExp> getValuesByType(final AttributeExpType type) {
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
		.add("Values", values)
		.toString();
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
			includeInResult == a.includeInResult &&
			Objects.equal(issuer, a.issuer) &&
			values.equals(a.values);
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

		/**
		 * Wraps given entities to via {@link EntityExp#valueOf(Entity)}
		 * and adds them to this entity builder
		 *
		 * @param values an array of entities
		 * @return reference to this builder
		 */
		public Builder entity(Entity ...values){
			Preconditions.checkNotNull(values);
			for(Entity v : values){
				valueBuilder.add(EntityExp.valueOf(v));
			}
			return this;
		}

		/**
		 * Wraps given entities to via {@link EntityExp#valueOf(Entity)}
		 * and adds them to this entity builder
		 *
		 * @param it an iterator over collection of {@link Entity}
		 * @return reference to this builder
		 */
		public Builder entities(Iterable<Entity> it){
			Preconditions.checkNotNull(it);
			for(Entity v : it){
				valueBuilder.add(EntityExp.valueOf(v));
			}
			return this;
		}

		public Builder values(Iterable<AttributeExp> values){
			Preconditions.checkNotNull(values);
			for(AttributeExp v : values){
				valueBuilder.add(v);
			}
			return this;
		}

		public Attribute build(){
			return new Attribute(this);
		}
	}
}
