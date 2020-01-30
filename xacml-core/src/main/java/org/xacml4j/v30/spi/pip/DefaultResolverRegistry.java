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

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;


import com.google.common.collect.Iterables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xacml4j.v30.*;

import com.google.common.base.Preconditions;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

/**
 * TODO: Implement support for resolver with the same attributes but different issuer
 *
 * @author Giedrius Trumpickas
 */
class DefaultResolverRegistry implements ResolverRegistry
{
	private final static Logger log = LoggerFactory.getLogger(DefaultResolverRegistry.class);

	/**
	 * Resolvers index by category and attribute identifier
	 */
	private final Map<CategoryId, Multimap<String, Resolver<AttributeSet>>> attributeResolvers;
	private final Map<String, Resolver<AttributeSet>> attributeResolversById;
	private final Map<CategoryId, Resolver<ContentRef>> contentResolvers;
	private final Map<String, Resolver<ContentRef>> contentResolversById;

	public DefaultResolverRegistry(Collection<Resolver<AttributeSet>> attributeResolvers,
								   Collection<Resolver<ContentRef>> contentResolvers)
	{
		this.attributeResolvers = new LinkedHashMap<>();
		this.contentResolvers = new ConcurrentHashMap<>();
		this.attributeResolversById = new HashMap<>();
		this.contentResolversById = new HashMap<>();
		attributeResolvers.forEach(r->addAttributeResolver(r));
		contentResolvers.forEach(c->addContentResolver(c));
	}

	@Override
	public Iterable<Resolver<AttributeSet>> getMatchingAttributeResolver(
			EvaluationContext context, AttributeDesignatorKey key) {
		Multimap<String, Resolver<AttributeSet>> r = attributeResolvers.get(key.getCategory());
		return Optional.ofNullable(r)
				.map(v->v.get(key.getAttributeId()))
				.orElse(Collections.emptyList());
	}

	@Override
	public Iterable<Resolver<ContentRef>> getMatchingContentResolver(EvaluationContext context,
																AttributeSelectorKey selectorKey)
	{
		return Optional.ofNullable(contentResolvers.get(selectorKey.getCategory()))
				.filter(r->r.canResolve(selectorKey))
				.map(v->Arrays.asList(v))
				.orElse(Collections.emptyList());

	}

	private void addAttributeResolver(Resolver<AttributeSet> resolver)
	{
		ResolverDescriptor d = resolver.getDescriptor();
		Preconditions.checkArgument(!attributeResolversById.containsKey(d.getId()),
				"Attribute resolver with id=\"%s\" is already registered with this registry", d.getId());
		for (CategoryId id : resolver.getDescriptor().getSupportedCategories()){
				addResolverForCategory(id, resolver);
		}
	}

	private void addResolverForCategory(CategoryId categoryId,
										Resolver<AttributeSet> resolver)
	{
		AttributeResolverDescriptor d = resolver.getDescriptor();
		if(!resolver.isCategorySupported(categoryId)){
			throw XacmlSyntaxException.invalidCategoryId(categoryId, "is not " +
							"contained in the resolverId=\"%s\", descriptor categories",
					resolver.getId());
		}
		Multimap<String, Resolver<AttributeSet>> byCategoryId = attributeResolvers.get(categoryId);
		if(byCategoryId == null){
			byCategoryId = LinkedHashMultimap.create();
		}
		for(String attributeId : d.getProvidedAttributeIds()){
			if(log.isDebugEnabled()){
				log.debug("Indexing resolver id=\"{}\" category=\"{}\", issuer=\"{}\" id=\"{}\"",
						new Object[]{d.getId(), categoryId, d.getIssuer(), attributeId});
			}
			byCategoryId.put(attributeId, resolver);
		}
		attributeResolversById.put(d.getId(), resolver);

	}


	private void addContentResolver(Resolver<ContentRef> r)
	{
		Preconditions.checkArgument(r != null);
		Resolver<ContentRef> contentResolverOther = contentResolversById.putIfAbsent(r.getId(), r);
		if(contentResolverOther != null){
			throw new IllegalArgumentException(String.format(
					"ContentResolverId=\"%s\" already registered - %s",
					r.getDescriptor().getId(),
					contentResolverOther.getClass()));
		}
		ResolverDescriptor d = r.getDescriptor();
		for (CategoryId categoryId : r.getDescriptor().getSupportedCategories()){
			contentResolvers.putIfAbsent(categoryId, r);
			if(log.isDebugEnabled()){
				log.debug("Adding content ContentResolverId=\"{}\" for category=\"{}\"",
						d.getId(), categoryId);
			}
		}
	}
}
