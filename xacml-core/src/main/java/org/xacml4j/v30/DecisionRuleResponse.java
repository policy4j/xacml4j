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

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import org.xacml4j.v30.types.Value;


public abstract class DecisionRuleResponse
{
	protected final String id;
	protected final Multimap<String, AttributeAssignment> attributes;

	/**
	 * For compatibility with XACML 2.0
	 * response context
	 */
	private final Effect fulfillOn;

	private final transient int hashCode;

	protected DecisionRuleResponse(
			Builder<?> b)
	{
		this(b.id, b.fullFillOn, ImmutableSetMultimap.copyOf(b.attributes));
	}

	DecisionRuleResponse(String id, Effect fulfillOn, Collection<AttributeAssignment> assignments)
	{
		this(Preconditions.checkNotNull(id), fulfillOn, attributes(ImmutableList.copyOf(assignments)));
	}

	private DecisionRuleResponse(String id, Effect fulfillOn,
			ImmutableSetMultimap<String, AttributeAssignment> attributes)
	{
		this.id = id;
		this.attributes = attributes;
		this.hashCode = 31 * (31 + java.util.Objects.hashCode(id)) + attributes.hashCode();
		this.fulfillOn = fulfillOn;
	}

	private static ImmutableSetMultimap<String, AttributeAssignment> attributes(
			ImmutableList<AttributeAssignment> assignments)
	{
		if(assignments.isEmpty()){
			return ImmutableSetMultimap.of();
		}
		if(assignments.size() == 1){
			AttributeAssignment assignment = assignments.get(0);
			return ImmutableSetMultimap.of(assignment.getAttributeId(), assignment);
		}
		ImmutableSetMultimap.Builder<String, AttributeAssignment> attributes = ImmutableSetMultimap.builder();
		for(int i = 0; i < assignments.size(); i++){
			AttributeAssignment assignment = assignments.get(i);
			attributes.put(assignment.getAttributeId(), assignment);
		}
		return attributes.build();
	}

	public final String getId(){
		return id;
	}

	/*&
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

	/**
	 * Combines this advice attributes with a
	 * given advice attributes
	 *
	 * @param a an advice
	 * @return a new advice defaultProvider with combined attributes
	 */
	public abstract  <T extends DecisionRuleResponse> T merge(T a);

	@Override
	public final int hashCode(){
		return hashCode;
	}

	protected boolean equalsTo(DecisionRuleResponse r) {
		return id.equals(r.id) &&
				attributes.equals(r.attributes);
	}

	@Override
	public final String toString(){
		return MoreObjects.toStringHelper(this)
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

		public final T from(DecisionRuleResponse r){
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
				String id, Value... values)
		{
			return attribute(id, null, null, values);
		}

		public final T attribute(
				String id,
				CategoryId category,
				String issuer,
				Value... values)
		{
			if(values == null ||
					values.length == 0){
				return getThis();
			}
			for(Value v : values){
				attribute(AttributeAssignment
						.builder()
						.attributeId(id)
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
