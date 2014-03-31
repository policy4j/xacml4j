package org.xacml4j.v30.pdp.profiles;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

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
import org.xacml4j.v30.pdp.RequestSyntaxException;
import org.xacml4j.v30.spi.xpath.XPathEvaluationException;
import org.xacml4j.v30.spi.xpath.XPathProvider;
import org.xacml4j.v30.types.XPathExp;
import org.xacml4j.v30.types.XacmlTypes;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

final class MultipleResourcesViaXPathExpressionHandler extends AbstractRequestContextHandler
{
	private final static Logger log = LoggerFactory.getLogger(MultipleResourcesViaXPathExpressionHandler.class);

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
				all.add(getAttributes(request, attribute, xpathProvider));
			}
			Set<List<Category>> cartesian = Sets.cartesianProduct(all);
			List<Result> results = new LinkedList<Result>();
			for(List<Category> requestAttr : cartesian)
			{
				RequestContext req = new RequestContext(request.isReturnPolicyIdList(),
						requestAttr, request.getRequestDefaults());
				if(log.isDebugEnabled()){
					log.debug("Created request=\"{}\"", req);
				}
				results.addAll(handleNext(req, context));
			}
			return results;
		}catch(RequestSyntaxException e){
			return Collections.singleton(
					Result.createIndeterminate(e.getStatus())
					.includeInResultAttr(request.getIncludeInResultAttributes())
					.build());
		}
	}

	private Set<Category> getAttributes(RequestContext request, Category attribute,
			XPathProvider xpathProvider)
		throws RequestSyntaxException
	{
		Entity entity = attribute.getEntity();
		Collection<AttributeExp> values = entity.getAttributeValues(MULTIPLE_CONTENT_SELECTOR,
				XacmlTypes.XPATH);
		if(values.isEmpty()){
			return ImmutableSet.of(attribute);
		}
		XPathExp selector = (XPathExp)Iterables.getOnlyElement(values, null);
		Node content = attribute.getEntity().getContent();
		// if there is no content
		// specified ignore it and return
		if(content == null){
			throw new RequestSyntaxException("Request attributes category=\"%s\" content " +
					"for selector=\"%s\" must be specified",
					attribute.getCategoryId(), selector.getValue());
		}
		try
		{
			NodeList nodeSet = xpathProvider.evaluateToNodeSet(selector.getPath(), content);
			Set<Category> attributes = new LinkedHashSet<Category>();
			for(int i = 0; i < nodeSet.getLength(); i++){
				String xpath = DOMUtil.getXPath(nodeSet.item(i));
				attributes.add(transform(xpath, attribute));
			}
			return attributes;
		}
		catch (XPathEvaluationException e){
			if(log.isDebugEnabled()){
				log.debug("Failed to evaluate xpath " +
						"expression", e);
			}
			return ImmutableSet.of(attribute);
		}
	}

	private Category transform(String xpath, Category attributes)
	{
		Collection<Attribute> newAttributes = new LinkedList<Attribute>();
		Entity e =  attributes.getEntity();
		for(Attribute a : e.getAttributes())
		{
			if(a.getAttributeId().equals(MULTIPLE_CONTENT_SELECTOR)){
				Attribute selectorAttr =
						Attribute.builder(CONTENT_SELECTOR)
						.issuer(a.getIssuer())
						.includeInResult(a.isIncludeInResult())
						.value(XPathExp.valueOf(xpath, attributes.getCategoryId()))
						.build();
				newAttributes.add(selectorAttr);
				continue;
			}
			newAttributes.add(a);
		}
		return Category
				.builder(attributes.getCategoryId())
				.id(attributes.getId())
				.entity(Entity.builder().content(e.getContent()).attributes(newAttributes).build())
				.build();
	}
}
