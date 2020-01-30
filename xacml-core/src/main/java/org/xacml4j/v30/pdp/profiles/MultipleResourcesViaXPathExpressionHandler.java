package org.xacml4j.v30.pdp.profiles;

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
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xacml4j.v30.*;
import org.xacml4j.v30.pdp.AbstractRequestContextHandler;
import org.xacml4j.v30.pdp.PolicyDecisionPointContext;
import org.xacml4j.v30.pdp.RequestSyntaxException;
import org.xacml4j.v30.types.PathValue;
import org.xacml4j.v30.types.XacmlTypes;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

final class MultipleResourcesViaXPathExpressionHandler extends AbstractRequestContextHandler
{
	private final static Logger LOG = LoggerFactory.getLogger(MultipleResourcesViaXPathExpressionHandler.class);

	final static String FEATURE_ID= "urn:oasis:names:tc:xacml:3.0:profile:multiple:xpath-expression";

	final static String MULTIPLE_CONTENT_SELECTOR = "urn:oasis:names:tc:xacml:3.0:profile:multiple:content-selector";
	final static String CONTENT_SELECTOR = "urn:oasis:names:tc:xacml:3.0:content-selector";

	public MultipleResourcesViaXPathExpressionHandler(){
		super(FEATURE_ID);
	}

	@Override
	public Collection<Result> handle(RequestContext request, PolicyDecisionPointContext context)
	{
		if(request.containsRepeatingCategories()){
			return handleNext(request, context);
		}
		if(!request.containsAttributeValues(
				MULTIPLE_CONTENT_SELECTOR,
				XacmlTypes.XPATH)){
			if(LOG.isDebugEnabled()){
				LOG.debug("Request does not have id=\"{}\" of type=\"{}\", " +
						"passing request to next handler",
						MULTIPLE_CONTENT_SELECTOR,
						XacmlTypes.XPATH);
			}
			return handleNext(request, context);
		}
		try
		{
			List<Set<Category>> all = new LinkedList<Set<Category>>();
			for(Category c : request.getAttributes()){
				Set<Category> categories = getCategories(c);
				if(LOG.isDebugEnabled()){
					LOG.debug("Categories=\"{}\"", categories);
				}
				all.add(categories);
			}
			Set<List<Category>> cartesian = Sets.cartesianProduct(all);
			if(LOG.isDebugEnabled()){
				LOG.debug("Cartesian product=\"{}\"", cartesian.size());
			}
			List<Result> results = new LinkedList<Result>();
			for(List<Category> requestAttr : cartesian)
			{
				RequestContext req = RequestContext.builder()
						.copyOf(request, requestAttr).build();
				if(LOG.isDebugEnabled()){
					LOG.debug("Created request=\"{}\"", req);
				}
				results.addAll(handleNext(req, context));
			}
			return results;
		}catch(RequestSyntaxException e){
			return Collections.singleton(
					Result.indeterminate(e.getStatus())
					.includeInResultAttr(request.getIncludeInResultAttributes())
					.build());
		}
	}

	private Set<Category> getCategories(Category category)
		throws RequestSyntaxException
	{
		Entity entity = category.getEntity();
		Optional<AttributeValue> value = entity.stream()
				.filter(a->a.getAttributeId().equalsIgnoreCase(MULTIPLE_CONTENT_SELECTOR))
				.map(a->a.getValuesByType(XacmlTypes.XPATH, XacmlTypes.JPATH))
				.flatMap(a->a.stream()).findFirst();
		if(!value.isPresent()){
			return ImmutableSet.of(category);
		}
		PathValue path = (PathValue)value.get();
		Optional<Content> content = entity.getContent().filter(
				c->c.getType().equals(path.getContentType()));
		if(!content.isPresent()){
			throw new RequestSyntaxException("Request attributes category=\"%s\" content " +
					"for selector=\"%s\" must be specified",
					category.getCategoryId(), path.value());
		}
		try
		{
			List<String> paths =  content
					.map(c->c.evaluateToNodePathList(path.getPath()))
					.orElse(Collections.<String>emptyList());
			if(LOG.isDebugEnabled()){
				LOG.debug("Found node paths=\"{}\" for path=\"{}\"", paths, path);
			}
			return paths.stream()
					.map(p->transform(p, path.getType(), content.get(), category))
					.collect(Collectors.toSet());
		}
		catch (PathEvaluationException e){
			if(LOG.isDebugEnabled()){
				LOG.debug("Failed to evaluate xpath " + "expression", e);
			}
			return ImmutableSet.of(category);
		}
	}

	private Category transform(String path, AttributeValueType selectorType, Content content, Category attributes)
	{
		LOG.debug("Processing path=\"{}\"", path);
		Entity e =  attributes.getEntity();
		Entity.Builder builder = Entity
				.builder()
				.content(content);
		for(Attribute a : e.getAttributes())
		{
			if(a.getAttributeId().equals(MULTIPLE_CONTENT_SELECTOR)){
				Attribute selectorAttr =
						Attribute.builder(CONTENT_SELECTOR)
						.issuer(a.getIssuer())
						.includeInResult(a.isIncludeInResult())
						.value(selectorType.of(
								path, attributes.getCategoryId()))
						.build();
				if(LOG.isDebugEnabled()){
					LOG.debug("Adding attribute=\"{}\"", selectorAttr);
				}
				builder.attribute(selectorAttr);
				continue;
			}
			if(LOG.isDebugEnabled()){
				LOG.debug("Adding attribute=\"{}\"", a);
			}
			builder.attribute(a);
		}
		Entity categoryEntity = builder.build();
		if(LOG.isDebugEnabled()){
			LOG.debug("New Entity=\"{}\"", categoryEntity);
		}
		Category category = Category
				.builder(attributes.getCategoryId())
				.id(attributes.getRefId())
				.entity(categoryEntity)
				.build();
		if(LOG.isDebugEnabled()){
			LOG.debug("New Category=\"{}\"", category);
		}
		return category;
	}
}
