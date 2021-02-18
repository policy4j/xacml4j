package org.xacml4j.v30.spi.pip;

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
import com.google.common.collect.ImmutableList;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xacml4j.v30.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 *
 *
 * @author Giedrius Trumpickas
 */
class DefaultResolverRegistry implements ResolverRegistry
{
	private final static Logger log = LoggerFactory.getLogger(DefaultResolverRegistry.class);

	/**
	 * Resolvers index by category and attribute identifier
	 */
	private final Map<CategoryId, Multimap<String, AttributeResolverDescriptor>> attributeResolvers;
	private final Map<String, AttributeResolverDescriptor> attributeResolversById;
	private final Map<CategoryId, ContentResolverDescriptor> contentResolvers;
	private final Map<String, ContentResolverDescriptor> contentResolversById;
	private final Map<String, Map<String, AttributeResolverDescriptor>> policyOrSetScopedAttributeResolvers;
	private final Map<String, Map<String, AttributeResolverDescriptor>> policyOrSetScopedContentResolvers;

	public DefaultResolverRegistry(Collection<AttributeResolverDescriptor> attributeResolvers,
								   Collection<ContentResolverDescriptor> contentResolvers)
	{
		this.attributeResolvers = new ConcurrentHashMap<>();
		this.contentResolvers = new ConcurrentHashMap<>();
		this.attributeResolversById = new ConcurrentHashMap<>();
		this.contentResolversById = new ConcurrentHashMap<>();
		this.policyOrSetScopedAttributeResolvers = new ConcurrentHashMap<>();
		this.policyOrSetScopedContentResolvers = new ConcurrentHashMap<>();
		attributeResolvers.forEach(r->addResolver(r));
		contentResolvers.forEach(c->addResolver(c));
	}

	public DefaultResolverRegistry()
	{
		this(Collections.emptyList(),Collections.emptyList());
	}

	@Override
	public Iterable<AttributeResolverDescriptor> getMatchingAttributeResolver(
			EvaluationContext context, AttributeDesignatorKey key) {
		Multimap<String, AttributeResolverDescriptor> r = attributeResolvers.get(key.getCategory());
		return Optional.ofNullable(r)
				.map(v->v.get(key.getAttributeId()))
				.orElse(Collections.emptyList());
	}

	@Override
	public Iterable<ContentResolverDescriptor> getMatchingContentResolver(EvaluationContext context,
																AttributeSelectorKey selectorKey)
	{
		ContentResolverDescriptor descriptor = contentResolvers.get(selectorKey.getCategory());
		return ImmutableList.of(descriptor);

	}

	private void addResolverForCategory(CategoryId categoryId,
										AttributeResolverDescriptor d)
	{
		if(!d.getCategory().equals(categoryId)){
			throw SyntaxException.invalidCategoryId(categoryId, "is not " +
							"contained in the resolverId=\"%s\", descriptor categories",
					d.getId());
		}
		Multimap<String, AttributeResolverDescriptor> byCategoryId = attributeResolvers.get(categoryId);
		if(byCategoryId == null){
			byCategoryId = LinkedHashMultimap.create();
		}
		for(String attributeId : d.getProvidedAttributeIds()){
			if(log.isDebugEnabled()){
				log.debug("Indexing resolver id=\"{}\" categoryId=\"{}\", id=\"{}\"",
						new Object[]{d.getId(), categoryId, attributeId});
			}
			byCategoryId.put(attributeId, d);
		}
		attributeResolversById.put(d.getId(), d);
	}

	public void addResolver(String id, AttributeResolverDescriptor resolver){
		if(!policyOrSetScopedAttributeResolvers.containsKey(id)){
			Map<String, AttributeResolverDescriptor> resolverMap = resolver.getProvidedAttributeIds()
			                                                               .stream()
			                                                               .collect(Collectors.toMap(v->v, v->resolver));
			policyOrSetScopedAttributeResolvers.putIfAbsent(id, resolverMap);
		}
	}

	public void addResolver(String id, ContentResolverDescriptor resolver){

	}

	public void addResolver(AttributeResolverDescriptor resolver)
	{
		Preconditions.checkArgument(!attributeResolversById.containsKey(resolver.getId()),
		                            "Attribute resolver with id=\"%s\" is already registered with this registry",
		                            resolver.getId());
		addResolverForCategory(resolver.getCategory(), resolver);
	}

	public void addResolver(ContentResolverDescriptor r)
	{
		Preconditions.checkArgument(r != null);
		ContentResolverDescriptor contentResolverOther = contentResolversById.putIfAbsent(r.getId(), r);
		if(contentResolverOther != null){
			throw new IllegalArgumentException(String.format(
					"contentResolverId=\"%s\" already registered - %s",
					r.getId(),
					contentResolverOther.getClass()));
		}
		contentResolvers.putIfAbsent(r.getCategory(), r);
		if(log.isDebugEnabled()){
			log.debug("Adding content contentResolverId=\"{}\" for categoryId=\"{}\"",
					r.getId(), r.getCategory());
		}
	}
}
