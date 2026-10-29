package org.xacml4j.v30.pdp.request;

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
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xacml4j.v30.Attribute;
import org.xacml4j.v30.Category;
import org.xacml4j.v30.Content;
import org.xacml4j.v30.types.Entity;
import org.xacml4j.v30.PathEvaluationException;
import org.xacml4j.v30.Result;
import org.xacml4j.v30.types.Value;
import org.xacml4j.v30.types.ValueType;
import org.xacml4j.v30.pdp.PolicyDecisionPointContext;
import org.xacml4j.v30.RequestContext;
import org.xacml4j.v30.RequestSyntaxException;
import org.xacml4j.v30.Status;
import org.xacml4j.v30.types.Path;
import org.xacml4j.v30.types.XacmlTypes;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

final class MultipleResourcesViaXPathExpressionHandler extends AbstractRequestContextHandler
{
	private final static Logger LOG = LoggerFactory.getLogger(MultipleResourcesViaXPathExpressionHandler.class);

	final static String FEATURE_ID= "urn:oasis:names:tc:xacml:3.0:profile:multiple:xpath-expression";

	final static String MULTIPLE_CONTENT_SELECTOR = "urn:oasis:names:tc:xacml:3.0:profile:multiple:content-selector";
	private final static String DRAFT_MULTIPLE_CONTENT_SELECTOR = "urn:oasis:names:tc:xacml:3.0:multiple:content-selector";
	final static String CONTENT_SELECTOR = "urn:oasis:names:tc:xacml:3.0:content-selector";

	/** Default limits (maintainer ruling 2026-10-07, run 061 D16). */
	final static int DEFAULT_MAX_PATHS_PER_CATEGORY = 100;
	final static int DEFAULT_MAX_EXPANDED_REQUESTS = 100;

	/** The status message of the result when a limit is exceeded. It names no request data. */
	final static String LIMIT_EXCEEDED_MESSAGE = "Multiple decision request expansion limit exceeded";

	private final int maxPathsPerCategory;
	private final int maxExpandedRequests;

	public MultipleResourcesViaXPathExpressionHandler(){
		this(DEFAULT_MAX_PATHS_PER_CATEGORY, DEFAULT_MAX_EXPANDED_REQUESTS);
	}

	/**
	 * @param maxPathsPerCategory the largest number of node paths that one
	 * {@code multiple:content-selector} may select in its category
	 * @param maxExpandedRequests the largest number of individual requests
	 * that one request may expand to
	 */
	MultipleResourcesViaXPathExpressionHandler(int maxPathsPerCategory, int maxExpandedRequests){
		super(FEATURE_ID);
		Preconditions.checkArgument(maxPathsPerCategory > 0,
				"maxPathsPerCategory=\"%s\" must be positive", maxPathsPerCategory);
		Preconditions.checkArgument(maxExpandedRequests > 0,
				"maxExpandedRequests=\"%s\" must be positive", maxExpandedRequests);
		this.maxPathsPerCategory = maxPathsPerCategory;
		this.maxExpandedRequests = maxExpandedRequests;
	}

	@Override
	public Collection<Result> handle(RequestContext request, PolicyDecisionPointContext context)
	{
		if(request.containsRepeatingCategories()){
			return handleNext(request, context);
		}
		if(!request.containsMultipleContentSelector()){
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
			boolean emptySelection = false;
			for(Category c : request.getCategory()){
				Set<Category> categories = getCategories(c);
				if(LOG.isDebugEnabled()){
					LOG.debug("Categories=\"{}\"", categories);
				}
				emptySelection |= categories.isEmpty();
				all.add(categories);
			}
			if(emptySelection){
				// A selector that selects no node gives no individual request,
				// whatever the other categories select. Returned here, because
				// the cross product of large sets can not be built even when
				// one of them is empty.
				return new LinkedList<Result>();
			}
			// The number of individual requests is the product of the sizes. It is
			// checked after every category, before the product is built, so it
			// stays below maxExpandedRequests * maxPathsPerCategory and does not
			// overflow.
			long expandedRequests = 1;
			for(Set<Category> categories : all){
				expandedRequests *= categories.size();
				if(expandedRequests > maxExpandedRequests){
					throw new ExpansionLimitExceededException(String.format(
							"more than maxExpandedRequests=\"%d\" individual requests", maxExpandedRequests));
				}
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
		}catch(ExpansionLimitExceededException e){
			// The request is well-formed; the PDP declines to process it. The
			// status message is a fixed text: which limit and by how much is
			// logged, not returned.
			LOG.warn("Multiple decision request rejected: {}", e.getMessage());
			return Collections.singleton(
					Result.indeterminate(Status.processingError()
							.message(LIMIT_EXCEEDED_MESSAGE)
							.build())
					.includeInResultAttr(request.getIncludeInResultAttributes())
					.build());
		}
	}

	/** A limit of the expansion was exceeded. */
	@SuppressWarnings("serial")
	private static final class ExpansionLimitExceededException extends Exception
	{
		ExpansionLimitExceededException(String message){
			super(message);
		}
	}

	private String normalizeMultipleContentSelectorId(String attributeId)
	{
		return DRAFT_MULTIPLE_CONTENT_SELECTOR.equalsIgnoreCase(attributeId)
				? MULTIPLE_CONTENT_SELECTOR : attributeId;
	}

	private Set<Category> getCategories(Category category)
		throws RequestSyntaxException, ExpansionLimitExceededException
	{
		Entity entity = category.getEntity();
		Optional<Value<?>> value = entity.attributes().stream()
					.filter(a->normalizeMultipleContentSelectorId(a.getAttributeId())
							.equalsIgnoreCase(MULTIPLE_CONTENT_SELECTOR))
					.map(a->a.getValuesByType(XacmlTypes.XPATH, XacmlTypes.JPATH))
					.flatMap(a->a.stream())
					.findFirst();
		if(!value.isPresent()){
			return ImmutableSet.of(category);
		}
		Path path = (Path)value.get();
		LOG.debug("Found multiple content selector path value={}", value.get());
		Optional<Content> content = entity.getContent().filter(
				c->c.getType().equals(path.getContentType()));
		if(!content.isPresent()){
			throw new RequestSyntaxException(String.format("Request attributes category=\"%s\" content " +
					"for selector=\"%s\" must be specified",
					category.getCategoryId(), path.get()));
		}
		try
		{
			List<String> paths =  content
					.map(c->c.evaluateToNodePathList(path.getPath()))
					.orElse(Collections.<String>emptyList());
			if(paths.size() > maxPathsPerCategory){
				throw new ExpansionLimitExceededException(String.format(
						"category=\"%s\" selects %d node paths, more than maxPathsPerCategory=\"%d\"",
						category.getCategoryId(), paths.size(), maxPathsPerCategory));
			}
			if(LOG.isDebugEnabled()){
				LOG.debug("Found node paths=\"{}\" for path=\"{}\"", paths, path);
			}
			// Preserve the XPath node order in the response result sequence.
			return paths.stream()
					.map(p->transform(p, path.getEvaluatesTo().getValueType(), content.get(), category))
					.collect(Collectors.toCollection(LinkedHashSet::new));
		}
		catch (PathEvaluationException e){
			if(LOG.isDebugEnabled()){
				LOG.debug("Failed to evaluate xpath " + "expression", e);
			}
			return ImmutableSet.of(category);
		}
	}

	private Category transform(String path, ValueType selectorType, Content content, Category attributes)
	{
		LOG.debug("Processing path=\"{}\"", path);
		Entity e =  attributes.getEntity();
		Entity.Builder builder = Entity
				.builder()
				.content(content);
		for(Attribute a : e.attributes())
		{
			if(normalizeMultipleContentSelectorId(a.getAttributeId())
					.equalsIgnoreCase(MULTIPLE_CONTENT_SELECTOR)){
				Attribute selectorAttr =
						Attribute.builder(CONTENT_SELECTOR)
						.issuer(a.getIssuer())
						.includeInResult(a.isIncludeInResult())
						.value(selectorType.ofAny(
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
				.id(attributes.getReferenceId().orElse(null))
				.entity(categoryEntity)
				.build();
		if(LOG.isDebugEnabled()){
			LOG.debug("New Category=\"{}\"", category);
		}
		return category;
	}
}
