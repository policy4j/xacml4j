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
import java.util.Set;

import com.google.common.base.*;
import com.google.common.collect.*;

public class RequestContext
{
    private final static Predicate<Category> IS_REFERENCABLE_CATEGORY =
            new Predicate<Category>() {
        @Override
        public boolean apply(Category category) {
            return category.isReferencable();
        }
    };

    private final static Function<Category, CategoryId> TO_CATEGORY_ID =
            new Function<Category, CategoryId>() {
        @Override
        public CategoryId apply(Category category) {
            return category.getCategoryId();
        }
    };

    private final static Function<Category, String> TO_REFERENCE_ID = new Function<Category, String>() {
        @Override
        public String apply(Category category) {
            return category.getReferenceId();
        }
    };

	private final boolean returnPolicyIdList;
	private final boolean combinedDecision;
	private final ImmutableMultimap<CategoryId, Category> attributes;
	private final ImmutableMap<String, Category> attributesByXmlId;
	private final ImmutableList<RequestReference> requestReferences;
	private final RequestDefaults requestDefaults;

	private final transient int cachedHashCode;

	private RequestContext(Builder b)
	{
		this.returnPolicyIdList = b.returnPolicyIdList;
		this.requestReferences = b.reqRefs.build();
		this.requestDefaults = b.reqDefaults;
		this.combinedDecision = b.combinedDecision;
		this.attributes = Multimaps.index(b.attrBuilder.build(), TO_CATEGORY_ID);
		ImmutableMap.Builder<String, Category> attributesByXmlIdBuilder = ImmutableMap.builder();
		for(Category attr : attributes.values()){
			if(attr.getReferenceId() != null){
				attributesByXmlIdBuilder.put(attr.getReferenceId(), attr);
			}
		}
		this.attributesByXmlId = Maps.uniqueIndex(
                FluentIterable
                        .from(this.attributes.values())
                        .filter(IS_REFERENCABLE_CATEGORY),
                TO_REFERENCE_ID);
		this.cachedHashCode = Objects.hashCode(
				this.returnPolicyIdList,
				this.combinedDecision,
				this.attributes,
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
	 * where both the {@link org.xacml4j.v30.pdp.Target} matched and the {@link org.xacml4j.v30.pdp.Condition}
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
	 * decisions into a single decision. The use of this category
	 * is specified in [Multi]. If the PDP does not implement the relevant
	 * functionality in [Multi], then the PDP must return an Indeterminate
	 * with a status code "Processing Error @{link {@link StatusCode#isProcessingError()} returns
	 * {@code true} if it receives a request with this category
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
	 * @return an instance of {@link RequestDefaults}
	 */
	public RequestDefaults getRequestDefaults(){
		return requestDefaults;
	}

	/**
	 * Gets occurrence of the given category category
	 * in this request
	 *
	 * @param category a category
	 * @return a non-negative number indicating category of given
	 * category occurrence in this request
	 */
	public int getCategoryOccurrences(CategoryId category){
		return attributes.get(category).size();
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

	/**
	 * Gets all {@link Category} instances contained
	 * in this request
	 * @return a collection of {@link Category}
	 * instances
	 */
	public Collection<Category> getAttributes(){
		return attributes.values();
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
	 * Gets all category categories contained
	 * in this request
	 *
	 * @return a set of all category categories in
	 * this request
	 */
	public Set<CategoryId> getCategories(){
		return attributes.keySet();
	}

	/**
	 * Resolves category references to {@link Category}
	 *
	 * @param reference an category references
	 * @return {@link Category} or {@code null} if
	 * references can not be resolved
	 */
	public Category getReferencedCategory(CategoryReference reference){
		return reference != null?attributesByXmlId.get(reference.getReferenceId()):null;
	}

	/**
	 * Gets all {@link Category} instances
	 * from request of a given category
	 *
	 * @param categoryId an category category
	 * @return a collection of {@link Category}, if
	 * a request does not have category of a specified
	 * category an empty collection is returned
	 */
	public Collection<Category> getAttributes(CategoryId categoryId){
		Preconditions.checkNotNull(categoryId);
		return attributes.get(categoryId);
	}

	public Collection<Entity> getEntities(CategoryId c) {
		return Collections2.transform(
				getAttributes(c),
				new Function<Category, Entity>() {
					@Override
					public Entity apply(Category input) {
						return input.getEntity();
					}
				});
	}

	/**
	 * Gets only one category of the given category
	 *
	 * @param category a category identifier
	 * @return {@link Category} or {@code null}
	 * if request does not have category of given category
	 * @exception IllegalArgumentException if a request
	 * has more than one instance of {@link Category}
	 * of the requested category
	 */
	public Category getOnlyCategory(CategoryId category){
		Collection<Category> attributes = getAttributes(category);
		return Iterables.getOnlyElement(attributes, null);
	}

	public Entity getOnlyEntity(CategoryId category){
		Collection<Entity> attributes = getEntities(category);
		return Iterables.getOnlyElement(attributes, null);
	}


	/**
	 * Tests if this request has an multiple
	 * {@link Category} instances with the
	 * same {@link Category#getCategoryId()} value
	 *
	 * @return {@code true} if this request
	 * has multiple category of same category
	 */
	public boolean containsRepeatingCategories() {
		for (CategoryId category : getCategories()) {
			if (getCategoryOccurrences(category) > 1) {
				return true;
			}
		}
		return false;
	}

	public boolean containsAttributeValues(
			String attributeId, String issuer, AttributeExpType type)
	{
		for (Category a : getAttributes()) {
			Entity e = a.getEntity();
			Collection<AttributeExp> values = e.getAttributeValues(attributeId, issuer, type);
			if (!values.isEmpty()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Tests if a given request context contains category with a given
	 * identifier of the given type for any category
	 *
	 * @param attributeId an category identifier
	 * @param type an category type
	 * @return {@code true} if this request contains an at least
	 * one category with a given identifier and the given type
	 */
	public boolean containsAttributeValues(String attributeId,
			AttributeExpType type)
	{
		return containsAttributeValues(attributeId, null, type);
	}

	/**
	 * Gets category values for given category, issuer, category attributeId and data type
	 *
	 * @param categoryId an category
	 * @param attributeId an category identifier
	 * @param dataType an category data type
	 * @param issuer an category issuer
	 * @return a collection of {@link AttributeExp} instances
	 */
	public Collection<AttributeExp> getAttributeValues(CategoryId categoryId,
			String attributeId, AttributeExpType dataType, String issuer)
	{
		ImmutableList.Builder<AttributeExp> found = ImmutableList.builder();
		for(Category a : attributes.get(categoryId)){
			Entity e = a.getEntity();
			found.addAll(e.getAttributeValues(attributeId, issuer, dataType));
		}
		return found.build();
	}

	/**
	 * Gets all category values of the given category with the
	 * given identifier and data type
	 *
	 * @param categoryId an category category
	 * @param attributeId an category identifier
	 * @param dataType an category data type
	 * @return a collection of {@link AttributeExp} instances
	 */
	public Collection<AttributeExp> getAttributeValues(
			CategoryId categoryId,
			String attributeId,
			AttributeExpType dataType)
	{
		return getAttributeValues(categoryId, attributeId, dataType, null);
	}

	/**
	 * Gets a single {@link AttributeExp} from this request
	 *
	 * @param categoryId an category category identifier
	 * @param attributeId an category identifier
	 * @param dataType an category data type
	 * @return {@link AttributeExp} or {@code null}
	 */
	public AttributeExp getAttributeValue(
            CategoryId categoryId,
			String attributeId,
			AttributeExpType dataType){
		return Iterables.getOnlyElement(
				getAttributeValues(categoryId, attributeId, dataType), null);
	}

	/**
	 * Gets all {@link Category} instances
	 * containing an category with {@link Attribute#isIncludeInResult()}
	 * returning {@code true}
	 *
	 * @return a collection of {@link Category} instances
	 * containing only {@link Attribute} with
	 * {@link Attribute#isIncludeInResult()} returning {@code true}
	 */
	public Collection<Category> getIncludeInResultAttributes()
	{
		ImmutableList.Builder<Category> resultAttr = ImmutableList.builder();
		for(Category a : attributes.values()){
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
		return Objects.toStringHelper(this)
		.add("ReturnPolicyIDList", returnPolicyIdList)
		.add("CombineDecision", combinedDecision)
		.add("Attribute", attributes.values())
		.add("RequestReferences", requestReferences)
		.add("RequestDefaults", requestDefaults).toString();
	}

	@Override
	public int hashCode(){
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
				attributes.equals(attributes) &&
				requestReferences.equals(r.requestReferences) &&
				Objects.equal(requestDefaults, r.requestDefaults);
	}

	public static class Builder
	{
		private boolean returnPolicyIdList;
		private boolean combinedDecision;
		private RequestDefaults reqDefaults = new RequestDefaults();
		private ImmutableList.Builder<Category> attrBuilder = ImmutableList.builder();
		private ImmutableList.Builder<RequestReference> reqRefs = ImmutableList.builder();


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
			return references(FluentIterable.of(refs));
		}

		public Builder references(Iterable<RequestReference> refs){
			this.reqRefs.addAll(FluentIterable.from(refs).filter(Predicates.notNull()));
			return this;
		}

		/**
		 * Copies all state to this builder from
		 * a given request context
		 *
		 * @param req a request context
		 * @return {@link Builder}
		 */
		public Builder copyOf(RequestContext req){
			copyOf(req, Functions.<Category>identity());
			return this;
		}

		/**
		 * Copies all state to this builder from
		 * a given request context except category elements
		 *
		 * @param req a request context
		 * @return {@link Builder}
		 */
		public Builder copyOf(RequestContext req,
                              Iterable<Category> attributes)
		{
			combineDecision(req.isCombinedDecision());
			returnPolicyIdList(req.isReturnPolicyIdList());
			reqDefaults(req.getRequestDefaults());
			categories(attributes, Functions.<Category>identity());
			return this;
		}


        public Builder copyOf(RequestContext req,
                              Function<Category, Category> transform)
        {
            combineDecision(req.isCombinedDecision());
            returnPolicyIdList(req.isReturnPolicyIdList());
            reqDefaults(req.getRequestDefaults());
            Iterable<Category> categories = FluentIterable
                    .from(req.getAttributes())
                    .transform(transform)
                    .filter(Predicates.notNull());
            this.attrBuilder.addAll(categories);
            return this;
        }

		public Builder noCategories(){
			attrBuilder = ImmutableList.builder();
			return this;
		}

		public Builder category(Category... attrs){
            return categories(FluentIterable.of(attrs), Functions.<Category>identity());
		}

        public Builder categories(Iterable<Category> attrs){
            return categories(attrs, Functions.<Category>identity());
        }
		public Builder categories(Iterable<Category> attrs, Function<Category, Category> transform)
		{
            this.attrBuilder.addAll(FluentIterable
                    .from(attrs)
                    .transform(transform)
                    .filter(Predicates.notNull()));
            return this;
		}

		public RequestContext build(){
			return new RequestContext(this);
		}

	}
}
