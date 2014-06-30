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

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;

/**
 * Base class for XACML attribute containers
 * 
 * @author Giedrius Trumpickas
 */
public class AttributeContainer
{
	protected final Multimap<String, Attribute> attributes;

	protected AttributeContainer(Builder<?> b){
		this.attributes = b.attrsBuilder.build();
	}

	/**
	 * Tests if this instance contains an
	 * attribute with a given identifier
	 *
	 * @param attributeId an attribute id
	 * @return {@code true} if contains
	 */
	public boolean containsAttribute(String attributeId){
		return attributes.containsKey(attributeId);
	}

	/**
	 * Gets all attributes with a given identifier
	 *
	 * @param attributeId an attribute identifier
	 * @return a collection of attributes if there is no
	 * attributes with a given identifier empty collection
	 * is returned
	 */
	public Collection<Attribute> getAttributes(String attributeId){
		return attributes.get(attributeId);
	}
	
	/**
	 * Gets a single {@link Attribute} instance with
	 * a given attribute identifier
	 *
	 * @param attributeId an attribute identifier
	 * @return {@link Attribute} instance or {@code null}
	 * if no attribute available with a given identifier
	 */
	public Attribute getOnlyAttribute(String attributeId){
		return Iterables.getOnlyElement(attributes.get(attributeId), null);
	}

	/**
	 * Gets all attributes with a given attribute
	 * identifier and issuer
	 *
	 * @param attributeId an attribute identifier
	 * @param issuer an attribute issuer
	 * @return a collection of attributes with a given identifier
	 * and given issuer
	 */
	public Collection<Attribute> getAttributes(final String attributeId, final String issuer){
		return Collections2.filter(attributes.get(attributeId), new Predicate<Attribute>() {
					@Override
					public boolean apply(Attribute attr) {
						return issuer == null ||
						issuer.equals(attr.getIssuer());
					}});
	}

	/**
	 * Gets all attribute {@link Attribute}
	 * instances
	 *
	 * @return immutable collection of {@link Attribute}
	 * instances
	 */
	public Collection<Attribute> getAttributes() {
		return attributes.values();
	}

	/**
	 * Finds all instance of {@link Attribute} with
	 * {@link Attribute#isIncludeInResult()} returning
	 * {@code true}
	 *
	 * @return a collection of {@link Attribute}
	 * instances
	 */
	public Collection<Attribute> getIncludeInResultAttributes(){
		return Collections2.filter(attributes.values(), new Predicate<Attribute>() {
				@Override
				public boolean apply(Attribute attr) {
					return attr.isIncludeInResult();
				}});
	}

	/**
	 * @see AttributeContainer#getAttributeValues(String, String, AttributeExpType)
	 */
	public Collection<AttributeExp> getAttributeValues(
			String attributeId,
			AttributeExpType dataType){
		return getAttributeValues(attributeId, null, dataType);
	}

	/**
	 * Gets only one {@link AttributeExp} instance of the given type
	 * from an attribute with a given identifier
	 *
	 * @param attributeId an attribute identifier
	 * @param dataType an attribute value data type
	 * @return {@link AttributeExp} of the given type or {@code null}
	 * if value matching given criteria does not exist
	 * @exception IllegalArgumentException if more than one value is found
	 * matching given criteria
	 */
	public AttributeExp getOnlyAttributeValue(String attributeId,
			AttributeExpType dataType){
		return Iterables.getOnlyElement(getAttributeValues(attributeId, dataType), null);
	}

	/**
	 * Gets all {@link AttributeExp} instances
	 * contained in this attributes instance
	 *
	 * @param attributeId an attribute id
	 * @param issuer an attribute issuer
	 * @param type an attribute value data type
	 * @return a collection of {@link AttributeExp} instances
	 */
	public Collection<AttributeExp> getAttributeValues(
			String attributeId, String issuer, final AttributeExpType type){
		Preconditions.checkNotNull(type);
		Collection<Attribute> found = getAttributes(attributeId, issuer);
		ImmutableList.Builder<AttributeExp> b = ImmutableList.builder();
		for(Attribute a : found){
			b.addAll(a.getValuesByType(type));
		}
		return b.build();
	}

	public static abstract class Builder<T extends Builder<?>>
	{
		private ImmutableListMultimap.Builder<String, Attribute> attrsBuilder = ImmutableListMultimap.builder();

		public T attribute(Attribute... attrs) {
			for(Attribute attr : attrs){
				attrsBuilder.put(attr.getAttributeId(), attr);
			}
			return getThis();
		}

		public T noAttributes() {
			this.attrsBuilder = ImmutableListMultimap.builder();
			return getThis();
		}

		public T attributes(Iterable<Attribute> attrs) {
			if(attrs == null){
				return getThis();
			}
			for(Attribute attr : attrs){
				attrsBuilder.put(attr.getAttributeId(), attr);
			}
			return getThis();
		}

		protected abstract T getThis();
	}
}
