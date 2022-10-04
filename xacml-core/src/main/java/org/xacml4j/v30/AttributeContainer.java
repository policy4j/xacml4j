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

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Base class for XACML attribute containers
 *
 * @author Giedrius Trumpickas
 */
public class AttributeContainer
{
	private final static Logger LOG = LoggerFactory.getLogger(AttributeContainer.class);

	protected final Multimap<String, Attribute> attributes;

	protected AttributeContainer(Builder<?> b){
		this.attributes = b.attrsBuilder.build();
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
	 * Gets a single {@link Attribute} defaultProvider with
	 * a given attribute identifier
	 *
	 * @param attributeId an attribute identifier
	 * @return {@link Attribute} defaultProvider or {@code null}
	 * if no attribute available with a given identifier
	 */
	public Optional<Attribute> getOnlyAttribute(String attributeId){
		return attributes.get(attributeId)
				.stream()
				.findFirst();
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
		return Collections2.filter(
				attributes.get(attributeId),
				a -> Objects.equals(issuer, a.getIssuer()));
	}

	public Map<String, Attribute> getAttributes(Predicate<Attribute> attributePredicate){
		return stream().filter(attributePredicate)
				.collect(Collectors.toMap(a->a.getAttributeId(), a->a));
	}

	public Stream<Attribute> stream(){
		return attributes.values()
				.stream();
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
	 * Finds all defaultProvider of {@link Attribute} with
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
	 * @see AttributeContainer#getAttributeValues(String, String, ValueType)
	 */
	public Collection<Value> getAttributeValues(
			String attributeId,
			ValueType dataType){
		return getAttributeValues(attributeId, null, dataType);
	}

	/**
	 * Gets all {@link Value} instances
	 * contained in this attributes defaultProvider
	 *
	 * @param attributeId an attribute id
	 * @param issuer an attribute issuer
	 * @param type an attribute value data type
	 * @return a collection of {@link Value} instances
	 */
	public Collection<Value> getAttributeValues(
			String attributeId, String issuer, final ValueType type){
		Preconditions.checkNotNull(type);
		Collection<Attribute> found = getAttributes(attributeId, issuer);
		ImmutableList.Builder<Value> b = ImmutableList.builder();
		for(Attribute a : found){
			b.addAll(a.getValuesByType(type));
		}
		return b.build();
	}

	public static abstract class Builder<T extends Builder<?>>
	{
		private ImmutableListMultimap.Builder<String, Attribute> attrsBuilder = ImmutableListMultimap.builder();

		public T attribute(Attribute... attrs) {
			Objects.requireNonNull(attrs,
					"At least one attribute must be specified");
			for(Attribute a : attrs){
				Objects.requireNonNull(a, "Attribute");
				attrsBuilder.put(a.getAttributeId(), a);
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
			if(LOG.isDebugEnabled()){
				LOG.debug("Adding attributes=\"{}\"",
						Iterables.toString(attrs));
			}
			for(Attribute attr : attrs){
				attrsBuilder.put(attr.getAttributeId(), attr);
			}
			return getThis();
		}

		protected abstract T getThis();
	}
}
