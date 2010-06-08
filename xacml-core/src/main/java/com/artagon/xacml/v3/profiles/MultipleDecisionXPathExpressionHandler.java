package com.artagon.xacml.v3.profiles;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
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
import com.artagon.xacml.v3.Result;
import com.artagon.xacml.v3.Status;
import com.artagon.xacml.v3.StatusCode;
import com.artagon.xacml.v3.XPathVersion;
import com.artagon.xacml.v3.pdp.PolicyDecisionCallback;
import com.artagon.xacml.v3.policy.spi.XPathEvaluationException;
import com.artagon.xacml.v3.policy.spi.XPathProvider;
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
	final static String RESOURCE_ID = "urn:oasis:names:tc:xacml:2.0:resource:resource-id";
	
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
		if(request.hasRepeatingCategories())
		{
			if(log.isDebugEnabled()){
				log.debug("Request=\"{}\" has repeating categories", request);
			}
			return Collections.singleton(new Result(new Status(
					StatusCode.createSyntaxError(), 
					"Request contains repeating attributes in the same category"))); 
		}
		List<Set<Attributes>> all = new LinkedList<Set<Attributes>>();
		for(Attributes attribute : request.getAttributes()){
			all.add(getAttributes(request, attribute));
		}
		Set<List<Attributes>> cartesian = Sets.cartesianProduct(all);
		List<Result> results = new LinkedList<Result>();
		for(List<Attributes> requestAttr : cartesian)
		{	
			Request req = new Request(request.isReturnPolicyIdList(),  requestAttr, request.getRequestDefaults());
			if(log.isDebugEnabled()){
				log.debug("Created request=\"{}\"", req);
			}
			results.addAll(handleNext(req, pdp));
		}
		return results;
	}
	
	private Set<Attributes> getAttributes(Request request, Attributes attribute)
	{
		Collection<AttributeValue> values = attribute.getAttributeValues(MULTIPLE_CONTENT_SELECTOR, 
				XacmlDataTypes.XPATHEXPRESSION.getType());
		// if we found syntax error
		// in multiple decision xpath syntax
		// ignore attribute and return
		if(values == null || 
				values.isEmpty()){
			log.debug("No values found");
			return ImmutableSet.of(attribute);
		}
		// if we found syntax error
		// in multiple decision xpath syntax
		// ignore attribute and return
		if(values.size() > 1){
			return ImmutableSet.of(attribute);
		}
		XPathExpressionType.XPathExpressionValue selector = (XPathExpressionType.XPathExpressionValue)Iterables.getOnlyElement(values);
		Node content = attribute.getContent();
		// if there is no content
		// specified ignore it and return
		if(content == null){
			return ImmutableSet.of(attribute);
		}
		try 
		{
			XPathVersion version = request.getRequestDefaults().getXPathVersion();
			NodeList nodeSet = xpathProvider.evaluateToNodeSet(version, selector.getValue(), content);
			Set<Attributes> attributes = new LinkedHashSet<Attributes>();
			for(int i = 0; i < nodeSet.getLength(); i++){	
				String xpath = DOMUtil.getXPath(nodeSet.item(i));
				log.debug("Xpath=\"{}\" category=\"{}\"", xpath, attribute.getCategoryId());
				attributes.add(transform(xpath, attribute));
			}
			return attributes;
		}
		catch (XPathEvaluationException e){
			return ImmutableSet.of(attribute);
		}
	}
	
	private Attributes transform(String xpath, Attributes attributes)
	{
		Collection<Attribute> newAttributes = new LinkedList<Attribute>();
		for(Attribute a : attributes.getAttributes())
		{
			if(a.getAttributeId().equals(MULTIPLE_CONTENT_SELECTOR)){
				Attribute selectorAttr = new Attribute(CONTENT_SELECTOR, a.getIssuer(), a.isIncludeInResult(), 
						XacmlDataTypes.XPATHEXPRESSION.create(xpath, attributes.getCategoryId()));
				newAttributes.add(selectorAttr);
				continue;
			}
			newAttributes.add(a);
		}
		return new Attributes(attributes.getId(), attributes.getCategoryId(), attributes.getContent(), newAttributes);
	}
}
