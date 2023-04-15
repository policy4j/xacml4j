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

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.xacml4j.v30.types.EntityValue;
import org.xacml4j.v30.types.XacmlTypes;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;

/**
 * A XACML request context attribute, a container for {@link Value}
 * of the same type with some additional meta information about values
 *
 * @author Giedrius Trumpickas
 */
public class Attribute implements Serializable
{
	private final String attributeId;
	private final List<Value> values;
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
	 * {@link Value} instances
	 *
	 * @return collection of {@link Value}
	 * instances
	 */
	public Collection<Value> getValues(){
		return values;
	}

	/**
	 * Gets all instances of {@link Value} by type
	 *
	 * @param type an attribute type
	 * @return a collection of {@link Value} of given type
	 */
	public Collection<Value> getValuesByType(final ValueType... type) {
		if(type == null || type.length == 0){
			return Collections.emptyList();
		}
		List<ValueType> types = Arrays.asList(type);
		return values.stream().filter(a->types.contains(a.getType()))
				.collect(Collectors.toList());
	}

	@Override
	public final String toString(){
		return MoreObjects.toStringHelper(this)
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
		if (o == this) {
			return true;
		}
		if (!(o instanceof Attribute)) {
			return false;
		}
		Attribute a = (Attribute)o;
		return Objects.equal(attributeId, a.attributeId) &&
			includeInResult == a.includeInResult &&
			Objects.equal(issuer, a.issuer) &&
			values.containsAll(a.values) && a.values.containsAll(values);
	}

	public static class Builder
	{
		private String attributeId;
		private String issuer;
		private boolean includeInResult;
		private ImmutableList.Builder<Value> valueBuilder;

		private Builder(String attributeId){
			Preconditions.checkArgument(!Strings.isNullOrEmpty(attributeId));
			this.attributeId = attributeId;
			this.valueBuilder = ImmutableList.builder();
		}

		public Builder issuer(String issuer){
			this.issuer = Strings.emptyToNull(issuer);
			return this;
		}

		public Builder includeInResult(boolean include){
			this.includeInResult = include;
			return this;
		}

		public Builder empty(){
			this.valueBuilder = ImmutableList.builder();
			return this;
		}

		public Builder value(Value...values){
			Preconditions.checkNotNull(values);
			for(Value v : values){
				valueBuilder.add(v);
			}
			return this;
		}

		/**
		 * Wraps given entities to via {@link EntityValue#of(Entity)}
		 * and adds them to this entity builder
		 *
		 * @param values an array of entities
		 * @return reference to this builder
		 */
		public Builder entity(Entity ...values){
			Preconditions.checkNotNull(values);
			for(Entity v : values){
				valueBuilder.add(XacmlTypes.ENTITY.of(v));
			}
			return this;
		}

		/**
		 * Wraps given entities to via {@link EntityValue#of(Entity)}
		 * and adds them to this entity builder
		 *
		 * @param it an iterator over collection of {@link Entity}
		 * @return reference to this builder
		 */
		public Builder entities(Iterable<Entity> it){
			Preconditions.checkNotNull(it);
			for(Entity v : it){
				valueBuilder.add(XacmlTypes.ENTITY.of(v));
			}
			return this;
		}

		public Builder values(Iterable<Value> values){
			Preconditions.checkNotNull(values);
			for(Value v : values){
				valueBuilder.add(v);
			}
			return this;
		}

		public Attribute build(){
			return new Attribute(this);
		}
	}
}
