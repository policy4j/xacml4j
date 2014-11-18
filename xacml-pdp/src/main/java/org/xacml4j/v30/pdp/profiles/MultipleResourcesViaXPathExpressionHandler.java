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

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.google.common.base.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xacml4j.util.DOMUtil;
import org.xacml4j.v30.Attribute;
import org.xacml4j.v30.AttributeExp;
import org.xacml4j.v30.Category;
import org.xacml4j.v30.Entity;
import org.xacml4j.v30.RequestContext;
import org.xacml4j.v30.Result;
import org.xacml4j.v30.pdp.AbstractRequestContextHandler;
import org.xacml4j.v30.pdp.PolicyDecisionPointContext;
import org.xacml4j.v30.RequestSyntaxException;
import org.xacml4j.v30.xpath.XPathEvaluationException;
import org.xacml4j.v30.xpath.XPathProvider;
import org.xacml4j.v30.types.XPathExp;
import org.xacml4j.v30.types.XacmlTypes;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

final class MultipleResourcesViaXPathExpressionHandler extends AbstractRequestContextHandler
{
	private final static Logger log = LoggerFactory.getLogger(MultipleResourcesViaXPathExpressionHandler.class);

	final static String FEATURE_ID= "urn:oasis:names:tc:xacml:3.0:profile:multiple:xpath-expression";

    /**
     * Multiple content selector attribute identifier
     */
	final static String MULTIPLE_CONTENT_SELECTOR = "urn:oasis:names:tc:xacml:3.0:profile:multiple:content-selector";

    /**
     * Content selector attribute identifier
     */
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
			if(log.isDebugEnabled()){
				log.debug("Request does not have attributeId=\"{}\" of type=\"{}\", " +
						"passing request to next handler",
						MULTIPLE_CONTENT_SELECTOR,
						XacmlTypes.XPATH);
			}
			return handleNext(request, context);
		}
		try
		{
			XPathProvider xpathProvider = context.getXPathProvider();
			List<Set<Category>> all = new LinkedList<Set<Category>>();
			for(Category attribute : request.getAttributes()){
				all.add(getCategories(request, attribute, xpathProvider));
			}
			Set<List<Category>> cartesian = Sets.cartesianProduct(all);
			List<Result> results = new LinkedList<Result>();
			for(List<Category> categories : cartesian)
			{
				RequestContext req = RequestContext.builder().copyOf(request, categories).build();
                if(log.isDebugEnabled()){
                    log.debug("XACML request=\"{}\"", req);
                }
				results.addAll(handleNext(req, context));
			}
			return results;
		}catch(RequestSyntaxException e){
			return Collections.singleton(
					Result.indeterminate(e.getStatus())
					.includeInResultAttributes(request.getIncludeInResultAttributes())
					.build());
		}
	}

	private Set<Category> getCategories(RequestContext request,
                                        Category attributes,
                                        XPathProvider xpathProvider) throws RequestSyntaxException
	{
		Entity entity = attributes.getEntity();
		Collection<AttributeExp> values = entity.getAttributeValues(MULTIPLE_CONTENT_SELECTOR,
				XacmlTypes.XPATH);
		if(values.isEmpty()){
			return ImmutableSet.of(attributes);
		}
		XPathExp selector = (XPathExp)Iterables.getOnlyElement(values, null);
        if(log.isDebugEnabled()){
            log.debug("Found multiple content " +
                    "selector attribute=\"{}\"", selector);
        }
		Node content = attributes.getEntity().getContent();
		// if there is no content
		// specified ignore it and return
		if(content == null){
			throw new RequestSyntaxException("Request category category=\"%s\" content " +
					"for selector=\"%s\" must be specified",
					attributes.getCategoryId(), selector.getValue());
		}
		try
		{
			NodeList nodeSet = xpathProvider.evaluateToNodeSet(selector.getPath(), content);
			Set<Category> categories = new LinkedHashSet<Category>();
			for(int i = 0; i < nodeSet.getLength(); i++){
				String xpath = DOMUtil.getXPath(nodeSet.item(i));
                if(log.isDebugEnabled()){
                    log.debug("XPath=\"{}\"", xpath);
                }
				categories.add(transform(xpath, attributes));
			}
			return categories;
		}
		catch (XPathEvaluationException e){
			if(log.isDebugEnabled()){
				log.debug("Failed to evaluate xpath " +
						"expression", e);
			}
			return ImmutableSet.of(attributes);
		}
	}

    /**
     * Replaces {@link #MULTIPLE_CONTENT_SELECTOR}
     * attribute with {@link #CONTENT_SELECTOR} in the given
     * category object
     *
     * @param xpath an XPATH
     * @param category an category object
     * @return transformed category object
     */
	private Category transform(final String xpath,
                               final Category category){
        Category.Builder b = Category.builder();
        b.copyOf(category, new Function<Attribute, Attribute>() {
            @Override
            public Attribute apply(Attribute attribute) {
                if(!attribute.getAttributeId().equals(
                        MULTIPLE_CONTENT_SELECTOR)){
                    return attribute;
                }
                return Attribute.builder()
                                .issuer(attribute.getIssuer())
                                .attributeId(CONTENT_SELECTOR)
                                .noValues()
                                .value(XPathExp.of(xpath, category.getCategoryId()))
                                .build();
            }
        });
        Category category1 = b.build();
        log.debug("Test=\"{}\"", category1);
        return category1;
	}
}
