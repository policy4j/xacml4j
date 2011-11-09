package com.artagon.xacml.v30.pdp.profiles;

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

import com.artagon.xacml.util.DOMUtil;
import com.artagon.xacml.v30.Attribute;
import com.artagon.xacml.v30.AttributeExp;
import com.artagon.xacml.v30.Attributes;
import com.artagon.xacml.v30.RequestContext;
import com.artagon.xacml.v30.RequestSyntaxException;
import com.artagon.xacml.v30.Result;
import com.artagon.xacml.v30.core.XPathVersion;
import com.artagon.xacml.v30.pdp.AbstractRequestContextHandler;
import com.artagon.xacml.v30.pdp.PolicyDecisionPointContext;
import com.artagon.xacml.v30.spi.xpath.XPathEvaluationException;
import com.artagon.xacml.v30.spi.xpath.XPathProvider;
import com.artagon.xacml.v30.types.XPathExpType;
import com.artagon.xacml.v30.types.XPathExp;
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
				XPathExpType.XPATHEXPRESSION)){
			if(log.isDebugEnabled()){
				log.debug("Request does not have attributeId=\"{}\" of type=\"{}\", " +
						"passing request to next handler", 
						MULTIPLE_CONTENT_SELECTOR, 
						XPathExpType.XPATHEXPRESSION);
			}
			return handleNext(request, context);
		}
		try
		{
			XPathProvider xpathProvider = context.getXPathProvider();
			List<Set<Attributes>> all = new LinkedList<Set<Attributes>>();
			for(Attributes attribute : request.getAttributes()){
				all.add(getAttributes(request, attribute, xpathProvider));
			}
			Set<List<Attributes>> cartesian = Sets.cartesianProduct(all);
			List<Result> results = new LinkedList<Result>();
			for(List<Attributes> requestAttr : cartesian)
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
					Result.createIndeterminate(e.getStatus(), 
							request.getIncludeInResultAttributes()));
		}
	}
	
	private Set<Attributes> getAttributes(RequestContext request, Attributes attribute, 
			XPathProvider xpathProvider) 
		throws RequestSyntaxException
	{
		Collection<AttributeExp> values = attribute.getAttributeValues(MULTIPLE_CONTENT_SELECTOR, 
				XPathExpType.XPATHEXPRESSION);
		if(values.isEmpty()){
			return ImmutableSet.of(attribute);
		}
		XPathExp selector = (XPathExp)Iterables.getOnlyElement(values, null);
		Node content = attribute.getContent();
		// if there is no content
		// specified ignore it and return
		if(content == null){
			throw new RequestSyntaxException("Request attributes category=\"%s\" content " +
					"for selector=\"%s\" must be specified", 
					attribute.getCategory(), selector.getValue());
		}
		try 
		{
			XPathVersion version = request.getRequestDefaults().getXPathVersion();
			NodeList nodeSet = xpathProvider.evaluateToNodeSet(version, selector.getPath(), content);
			Set<Attributes> attributes = new LinkedHashSet<Attributes>();
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
	
	private Attributes transform(String xpath, Attributes attributes) 
	{
		Collection<Attribute> newAttributes = new LinkedList<Attribute>();
		for(Attribute a : attributes.getAttributes())
		{
			if(a.getAttributeId().equals(MULTIPLE_CONTENT_SELECTOR)){
				Attribute selectorAttr = new Attribute(CONTENT_SELECTOR, 
						a.getIssuer(), 
						a.isIncludeInResult(), 
						XPathExpType.XPATHEXPRESSION.create(xpath, attributes.getCategory()));
				newAttributes.add(selectorAttr);
				continue;
			}
			newAttributes.add(a);
		}
		return new Attributes(attributes.getId(), 
				attributes.getCategory(), 
				attributes.getContent(), newAttributes);
	}
}
