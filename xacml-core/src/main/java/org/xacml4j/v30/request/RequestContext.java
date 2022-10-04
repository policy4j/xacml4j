package org.xacml4j.v30.request;

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

import com.google.common.base.Function;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.*;

import java.util.*;
import java.util.stream.Stream;

import org.xacml4j.v30.Attribute;
import org.xacml4j.v30.Category;
import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.CategoryReference;
import org.xacml4j.v30.Decision;
import org.xacml4j.v30.Effect;
import org.xacml4j.v30.types.Entity;
import org.xacml4j.v30.StatusCode;
import org.xacml4j.v30.Value;
import org.xacml4j.v30.ValueType;
import org.xacml4j.v30.policy.Condition;
import org.xacml4j.v30.policy.Target;

/**
 * A XACML Request Context Abstraction
 *
 * @author Giedrius Trumpickas
 */
public class RequestContext
{
	private final boolean returnPolicyIdList;
	private final boolean combinedDecision;
	private final Multimap<CategoryId, Category> entities;
	private final Map<String, Category> attributesByXmlId;
	private final List<RequestReference> requestReferences;
	private final RequestDefaults requestDefaults;

	private transient int cachedHashCode = 0;

	private RequestContext(Builder b)
	{
		this.returnPolicyIdList = b.returnPolicyIdList;
		this.requestReferences = b.reqRefs.build();
		this.requestDefaults = b.reqDefaults;
		this.combinedDecision = b.combinedDecision;
		this.entities = b.attrBuilder.build();
		ImmutableMap.Builder<String, Category> attributesByXmlIdBuilder = ImmutableMap.builder();
		for(Category attr : entities.values()){
			if(attr.getReferenceId() != null){
				attributesByXmlIdBuilder.put(attr.getReferenceId(), attr);
			}
		}
		this.attributesByXmlId = attributesByXmlIdBuilder.build();
		this.cachedHashCode = Objects.hashCode(
				this.returnPolicyIdList,
				this.combinedDecision,
				this.entities,
				this.requestReferences,
				this.requestDefaults);
	}

	public static Builder builder(){
		return new Builder();
	}

	/**
	 * If the function returns {@code true}, a PDP that implements this optional
	 * feature MUST return a list of all policies which were
	 * found to be fully applicable. That is, all policies
	 * where both the {@link Target} matched and the {@link Condition}
	 * evaluated to {@code true}, whether or not the {@link Effect}
	 *  was the same or different from the {@link Decision}}
	 *
	 * @return boolean value
	 */
	public boolean isReturnPolicyIdList(){
		return returnPolicyIdList;
	}

	/**
	 * Gets a flag used to request that the PDP combines multiple
	 * decisions into a single decision. The use of this attribute
	 * is specified in [Multi]. If the PDP does not implement the relevant
	 * functionality in [Multi], then the PDP must return an Indeterminate
	 * with a status code "Processing Error @{link {@link StatusCode#isProcessingError()} returns
	 * {@code true} if it receives a request with this attribute
	 * set to {@code true}
	 *
	 * @return {@code true} if the decision is combined; returns {@code false} otherwise
	 */
	public boolean isCombinedDecision(){
		return combinedDecision;
	}

	/**
	 * Gets request defaults
	 *
	 * @return an defaultProvider of {@link RequestDefaults}
	 */
	public RequestDefaults getRequestDefaults(){
		return requestDefaults;
	}

	/**
	 * Gets occurrence of the given category attributes
	 * in this request
	 *
	 * @param category a category
	 * @return a non-negative number indicating attributes of given
	 * category occurrence in this request
	 */
	public int getCategoryOccurrences(CategoryId category){
		return entities.get(category).size();
	}

	/**
	 * Gets request references contained
	 * in this request context
	 *
	 * @return a collection of {@link RequestReference}
	 * instances
	 */
	public Collection<RequestReference> getRequestReferences(){
		return requestReferences;
	}

	public Collection<Category> getCategories(){
		return entities.values();
	}

	/**
	 * Gets all {@link Category} instances contained
	 * in this request
	 * @return a collection of {@link Category}
	 * instances
	 */
	public Collection<Category> getCategory(){
		return entities.values();
	}

	/**
	 * Tests if this request has multiple
	 * individual XACML requests
	 *
	 * @return {@code true} if this
	 * request has multiple XACML individual
	 * requests
	 */
	public boolean containsRequestReferences(){
		return !requestReferences.isEmpty();
	}


	/**
	 * Resolves attribute reference to {@link Category}
	 *
	 * @param reference an attributes reference
	 * @return {@link Category} or {@code null} if
	 * reference can not be resolved
	 */
	public Category getReferencedCategory(CategoryReference reference){
		Preconditions.checkNotNull(reference);
		return attributesByXmlId.get(reference.getReferenceId());
	}

	/**
	 * Gets all {@link Category} instances
	 * from request of a given category
	 *
	 * @param categoryId an attribute category
	 * @return a collection of {@link Category}, if
	 * a request does not have attributes of a specified
	 * category an empty collection is returned
	 */
	public Collection<Category> getCategory(CategoryId categoryId){
		Preconditions.checkNotNull(categoryId);
		return entities.get(categoryId);
	}

	public Collection<Entity> getEntities(CategoryId c) {
		return Collections2.transform(
				getCategory(c),
				new Function<Category, Entity>() {
					@Override
					public Entity apply(Category input) {
						return input.getEntity();
					}
				});
	}

	public Stream<Category> stream(){
		return entities
				.values()
				.stream();
	}

	/**
	 * Gets only one attribute of the given category
	 *
	 * @param category a category identifier
	 * @return {@link Category} or {@code null}
	 * if request does not have attributes of given category
	 * @exception IllegalArgumentException if a request
	 * has more than one defaultProvider of {@link Category}
	 * of the requested category
	 */
	public Category getOnlyAttributes(CategoryId category){
		Collection<Category> attributes = getCategory(category);
		return Iterables.getOnlyElement(attributes, null);
	}


	public Optional<Entity> getEntity(CategoryId category){
		return getEntities(category)
				.stream()
				.findFirst();
	}

	public Optional<Attribute> getAttribute(CategoryId id,
	                                        String attributeId){
		return getAttribute(id, (a)->a.getAttributeId()
				.equalsIgnoreCase(attributeId));
	}

	public Optional<Attribute> getAttribute(CategoryId id,
											java.util.function.Predicate<Attribute> filter){
		return getEntity(id)
				.map(v->v.getAttributes())
				.flatMap(v->v.stream()
						.filter(filter)
						.findFirst());
	}

	/**
	 * Tests if this request has an multiple
	 * {@link Category} instances with the
	 * same {@link Category#getCategoryId()} value
	 *
	 * @return {@code true} if this request
	 * has multiple attributes of same category
	 */
	public boolean containsRepeatingCategories() {
		for (Category category : getCategories()) {
			if (getCategoryOccurrences(category.getCategoryId()) > 1) {
				return true;
			}
		}
		return false;
	}

	public boolean containsAttributeValues(
			String attributeId, String issuer, ValueType type)
	{
		for (Category a : entities.values()) {
			Entity e = a.getEntity();
			Collection<Value> values = e.getAttributeValues(attributeId, issuer, type);
			if (!values.isEmpty()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Tests if a given request context contains attributes with a given
	 * identifier of the given type for any category
	 *
	 * @param attributeId an attribute identifier
	 * @param type an attribute type
	 * @return {@code true} if this request contains an at least
	 * one attribute with a given identifier and the given type
	 */
	public boolean containsAttributeValues(String attributeId,
			ValueType type)
	{
		return containsAttributeValues(attributeId, null, type);
	}

	/**
	 * Gets attribute values for given category, issuer, attribute id and data type
	 *
	 * @param categoryId an category
	 * @param attributeId an attribute identifier
	 * @param dataType an attribute data type
	 * @param issuer an attribute issuer
	 * @return a collection of {@link Value} instances
	 */
	public Collection<Value> getAttributeValues(CategoryId categoryId,
	                                            String attributeId, ValueType dataType, String issuer)
	{
		ImmutableList.Builder<Value> found = ImmutableList.builder();
		for(Category a : entities.get(categoryId)){
			Entity e = a.getEntity();
			found.addAll(e.getAttributeValues(attributeId, issuer, dataType));
		}
		return found.build();
	}

	/**
	 * Gets all attribute values of the given category with the
	 * given identifier and data type
	 *
	 * @param categoryId an attribute category
	 * @param attributeId an attribute identifier
	 * @param dataType an attribute data type
	 * @return a collection of {@link Value} instances
	 */
	public Collection<Value> getAttributeValues(
			CategoryId categoryId,
			String attributeId,
			ValueType dataType)
	{
		return getAttributeValues(categoryId, attributeId, dataType, null);
	}

	/**
	 * Gets a single {@link Value} from this request
	 *
	 * @param categoryId an attribute category identifier
	 * @param attributeId an attribute identifier
	 * @param dataType an attribute data type
	 * @return {@link Value} or {@code null}
	 */
	public Value getAttributeValue(CategoryId categoryId,
	                               String attributeId,
	                               ValueType dataType){
		return Iterables.getOnlyElement(
				getAttributeValues(categoryId, attributeId, dataType), null);
	}

	/**
	 * Gets all {@link Category} instances
	 * containing an attributes with {@link Attribute#isIncludeInResult()}
	 * returning {@code true}
	 *
	 * @return a collection of {@link Category} instances
	 * containing only {@link Attribute} with
	 * {@link Attribute#isIncludeInResult()} returning {@code true}
	 */
	public Collection<Category> getIncludeInResultAttributes()
	{
		ImmutableList.Builder<Category> resultAttr = ImmutableList.builder();
		for(Category a : entities.values()){
			Entity e = a.getEntity();
			Collection<Attribute> includeInResult = e.getIncludeInResultAttributes();
			if(!includeInResult.isEmpty()){
				resultAttr.add(Category
						.builder(a.getCategoryId())
						.entity(Entity.builder().attributes(e.getIncludeInResultAttributes()).build())
						.build());
			}
		}
		return resultAttr.build();
	}

	@Override
	public String toString(){
		return MoreObjects.toStringHelper(this)
		.add("ReturnPolicyIDList", returnPolicyIdList)
		.add("CombineDecision", combinedDecision)
		.addValue(entities.values())
		.add("RequestReferences", requestReferences)
		.add("RequestDefaults", requestDefaults).toString();
	}

	@Override
	public int hashCode(){
		 if(cachedHashCode == 0){
		 	this.cachedHashCode = Objects.hashCode(
					this.returnPolicyIdList,
					this.combinedDecision,
					this.entities,
					this.requestReferences,
					this.requestDefaults);
		 }
		 return cachedHashCode;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof RequestContext)) {
			return false;
		}
		RequestContext r = (RequestContext)o;
		return returnPolicyIdList == r.returnPolicyIdList &&
				combinedDecision == r.combinedDecision &&
				entities.equals(entities) &&
				requestReferences.equals(r.requestReferences) &&
				Objects.equal(requestDefaults, r.requestDefaults);
	}

	public static class Builder
	{
		private boolean returnPolicyIdList;
		private boolean combinedDecision;
		private RequestDefaults reqDefaults = new RequestDefaults();
		private ImmutableListMultimap.Builder<CategoryId, Category> attrBuilder = ImmutableListMultimap.builder();
		private ImmutableList.Builder<RequestReference> reqRefs = ImmutableList.builder();

		public Builder returnPolicyIdList(){
			this.returnPolicyIdList = true;
			return this;
		}

		public Builder reqDefaults(RequestDefaults defaults){
			this.reqDefaults = defaults;
			return this;
		}

		public Builder combineDecision(boolean combine){
			this.combinedDecision = combine;
			return this;
		}

		public Builder returnPolicyIdList(boolean returnPolicyIdList){
			this.returnPolicyIdList = returnPolicyIdList;
			return this;
		}

		public Builder reference(RequestReference ...refs){
			this.reqRefs.add(refs);
			return this;
		}

		public Builder reference(Iterable<RequestReference> refs){
			this.reqRefs.addAll(refs);
			return this;
		}

		/**
		 * Copies all state to this builder from
		 * a given request context
		 *
		 * @param req a request context
		 * @return {@link Builder}
		 */
		public Builder copyOf(RequestContext req)
		{
			combineDecision(req.isCombinedDecision());
			returnPolicyIdList(req.isReturnPolicyIdList());
			reqDefaults(req.getRequestDefaults());
			attributes(req.getCategories());
			reference(req.getRequestReferences());
			return this;
		}

		/**
		 * Copies all state to this builder from
		 * a given request context except attributes
		 *
		 * @param req a request context
		 * @return {@link Builder}
		 */
		public Builder copyOf(RequestContext req, Iterable<Category> attributes)
		{
			combineDecision(req.isCombinedDecision());
			returnPolicyIdList(req.isReturnPolicyIdList());
			reqDefaults(req.getRequestDefaults());
			attributes(attributes);
			return this;
		}

		public Builder noAttributes(){
			attrBuilder = ImmutableListMultimap.builder();
			return this;
		}

		public Builder attributes(Category ... attrs)
		{
			Preconditions.checkNotNull(attrs);
			for(Category a : attrs){
				this.attrBuilder.put(a.getCategoryId(), a);
			}
			return this;
		}

		public Builder attributes(Iterable<Category> attrs)
		{
			Preconditions.checkNotNull(attrs);
			for(Category a : attrs){
				this.attrBuilder.put(a.getCategoryId(), a);
			}
			return this;
		}

		public RequestContext build(){
			return new RequestContext(this);
		}

	}
}
