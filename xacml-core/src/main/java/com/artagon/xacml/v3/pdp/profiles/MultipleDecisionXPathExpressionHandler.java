package com.artagon.xacml.v3.pdp.profiles;

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
import com.artagon.xacml.v3.Attribute;
import com.artagon.xacml.v3.AttributeValue;
import com.artagon.xacml.v3.Attributes;
import com.artagon.xacml.v3.Request;
import com.artagon.xacml.v3.RequestSyntaxException;
import com.artagon.xacml.v3.Result;
import com.artagon.xacml.v3.XPathVersion;
import com.artagon.xacml.v3.pdp.PolicyDecisionCallback;
import com.artagon.xacml.v3.spi.XPathEvaluationException;
import com.artagon.xacml.v3.spi.XPathProvider;
import com.artagon.xacml.v3.types.XPathExpressionType;
import com.artagon.xacml.v3.types.XacmlDataTypes;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

final class MultipleDecisionXPathExpressionHandler extends AbstractRequestProfileHandler
{
	private final static Logger log = LoggerFactory.getLogger(MultipleDecisionXPathExpressionHandler.class);
	
	final static String ID = "urn:oasis:names:tc:xacml:3.0:profile:multiple:xpath-expression";
	
	final static String MULTIPLE_CONTENT_SELECTOR = "urn:oasis:names:tc:xacml:3.0:profile:multiple:content-selector";
	final static String CONTENT_SELECTOR = "urn:oasis:names:tc:xacml:3.0:content-selector";
	
	private XPathProvider xpathProvider;
	
	
	public MultipleDecisionXPathExpressionHandler(XPathProvider xpathProvider){
		Preconditions.checkNotNull(xpathProvider);
		this.xpathProvider = xpathProvider;
	}
	
	@Override
	public Collection<Result> handle(Request request, PolicyDecisionCallback pdp) 
	{
		if(request.hasRepeatingCategories()){
			return handleNext(request, pdp); 
		}
		if(!request.containsAttributeValues(
				MULTIPLE_CONTENT_SELECTOR, 
				XacmlDataTypes.XPATHEXPRESSION.getType())){
			if(log.isDebugEnabled()){
				log.debug("Request does not have attributeId=\"{}\" of type=\"{}\", " +
						"passing request to next handler", 
						MULTIPLE_CONTENT_SELECTOR, XacmlDataTypes.XPATHEXPRESSION.getType());
			}
			return handleNext(request, pdp);
		}
		try
		{
			List<Set<Attributes>> all = new LinkedList<Set<Attributes>>();
			for(Attributes attribute : request.getAttributes()){
				all.add(getAttributes(request, attribute));
			}
			Set<List<Attributes>> cartesian = Sets.cartesianProduct(all);
			List<Result> results = new LinkedList<Result>();
			for(List<Attributes> requestAttr : cartesian)
			{	
				Request req = new Request(request.isReturnPolicyIdList(), 
						requestAttr, request.getRequestDefaults());
				if(log.isDebugEnabled()){
					log.debug("Created request=\"{}\"", req);
				}
				results.addAll(handleNext(req, pdp));
			}
			return results;
		}catch(RequestSyntaxException e){
			return Collections.singleton(
					Result.createIndeterminate(e.getStatus()));
		}
	}
	
	private Set<Attributes> getAttributes(Request request, Attributes attribute) 
		throws RequestSyntaxException
	{
		Collection<AttributeValue> values = attribute.getAttributeValues(MULTIPLE_CONTENT_SELECTOR, 
				XacmlDataTypes.XPATHEXPRESSION.getType());
		if(values.isEmpty()){
			return ImmutableSet.of(attribute);
		}
		XPathExpressionType.XPathExpressionValue selector = (XPathExpressionType.XPathExpressionValue)Iterables.getOnlyElement(values, null);
		Node content = attribute.getContent();
		// if there is no content
		// specified ignore it and return
		if(content == null){
			throw new RequestSyntaxException("Request attributes category=\"%s\" content " +
					"for selector=\"%s\" must be specified", 
					attribute.getCategoryId(), selector.getValue());
		}
		try 
		{
			XPathVersion version = request.getRequestDefaults().getXPathVersion();
			NodeList nodeSet = xpathProvider.evaluateToNodeSet(version, selector.getValue(), content);
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
						XacmlDataTypes.XPATHEXPRESSION.create(xpath, attributes.getCategoryId()));
				newAttributes.add(selectorAttr);
				continue;
			}
			newAttributes.add(a);
		}
		return new Attributes(attributes.getId(), attributes.getCategoryId(), attributes.getContent(), newAttributes);
	}
}
