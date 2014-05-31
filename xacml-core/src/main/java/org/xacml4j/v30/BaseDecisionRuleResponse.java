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

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;


public abstract class BaseDecisionRuleResponse
{
	protected final String id;
	protected final Multimap<String, AttributeAssignment> attributes;

	/**
	 * For compatibility with XACML 2.0
	 * response context
	 */
	private final Effect fulfillOn;

	private final transient int hashCode;

	protected BaseDecisionRuleResponse(
			Builder<?> b)
	{
		this.id = b.id;
		this.attributes = ImmutableSetMultimap.copyOf(b.attributes);
		this.hashCode = Objects.hashCode(id, attributes);
		this.fulfillOn = b.fullFillOn;
	}

	public final String getId(){
		return id;
	}

	/**
	 * For compatibility with XACML 2.0 response
	 * context
	 *
	 * @return {@link Effect}
	 */
	public Effect getFulfillOn(){
		return fulfillOn;
	}

	public final Collection<AttributeAssignment> getAttributes(){
		return attributes.values();
	}

	public final Collection<AttributeAssignment> getAttribute(String attributeId){
		return this.attributes.get(attributeId);
	}

	@Override
	public final int hashCode(){
		return hashCode;
	}

	protected boolean equalsTo(BaseDecisionRuleResponse r) {
		return id.equals(r.id) &&
				attributes.equals(r.attributes);
	}

	@Override
	public final String toString(){
		return Objects.toStringHelper(this)
		.add("id", id)
		.add("attributes", attributes)
		.add("fullFillOn", fulfillOn)
		.toString();
	}

	public abstract static class Builder<T extends Builder<?>>
	{
		protected String id;
		protected Effect fullFillOn;
		protected Multimap<String, AttributeAssignment> attributes = LinkedHashMultimap.create();

		protected Builder(String id, Effect fullFillOn){
			Preconditions.checkNotNull(id);
			this.id = id;
			this.fullFillOn = fullFillOn;
		}

		public final T attribute(AttributeAssignment attr){
			Preconditions.checkNotNull(attr);
			this.attributes.put(attr.getAttributeId(), attr);
			return getThis();
		}

		public final T from(BaseDecisionRuleResponse r){
			this.id = r.id;
			this.fullFillOn = r.fulfillOn;
			this.attributes.putAll(r.attributes);
			return getThis();
		}

		public final T attributes(Iterable<AttributeAssignment> attributes){
			for(AttributeAssignment attr : attributes){
				this.attributes.put(attr.getAttributeId(), attr);
			}
			return getThis();
		}

		public final T attribute(
				String id, AttributeExp ... values)
		{
			return attribute(id, null, null, values);
		}

		public final T attribute(
				String id,
				CategoryId category,
				String issuer,
				AttributeExp ... values)
		{
			if(values == null ||
					values.length == 0){
				return getThis();
			}
			for(AttributeExp v : values){
				attribute(AttributeAssignment
						.builder()
						.id(id)
						.category(category)
						.issuer(issuer)
						.value(v)
						.build());
			}
			return getThis();
		}

		protected abstract T getThis();
	}
}
