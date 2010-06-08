package com.artagon.xacml.v3.profiles;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.artagon.xacml.util.DOMUtil;
import com.artagon.xacml.v3.AttributeCategoryId;
import com.artagon.xacml.v3.AttributeValue;
import com.artagon.xacml.v3.Attributes;
import com.artagon.xacml.v3.Request;
import com.artagon.xacml.v3.RequestDefaults;
import com.artagon.xacml.v3.Result;
import com.artagon.xacml.v3.Status;
import com.artagon.xacml.v3.StatusCode;
import com.artagon.xacml.v3.pdp.PolicyDecisionCallback;
import com.artagon.xacml.v3.policy.spi.XPathEvaluationException;
import com.artagon.xacml.v3.policy.spi.XPathProvider;
import com.artagon.xacml.v3.types.XacmlDataTypes;
import com.artagon.xacml.v3.types.StringType.StringValue;
import com.artagon.xacml.v3.types.XPathExpressionType.XPathExpressionValue;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;

public class MultipleResourcesIdentifiedViaXPathExpressionHandler extends AbstractRequestProfileHandler
{
	private final static String ID = "urn:oasis:names:tc:xacml:3.0:profile:multiple:xpath-expression";
	private final static String SCOPE_ATTRIBUTE = "urn:oasis:names:tc:xacml:2.0:resource:scope";
	private final static String SCOPE_XPATH_EXPRESSION = "XPath-expression";
	private final static String RESOURCE_ID = "urn:oasis:names:tc:xacml:2.0:resource:resource-id";
	
	private XPathProvider xpathProvider;
	
	
	public MultipleResourcesIdentifiedViaXPathExpressionHandler(XPathProvider xpathProvider){
		super(ID);
		Preconditions.checkNotNull(xpathProvider);
		this.xpathProvider = xpathProvider;
	}
	
	@Override
	public Collection<Result> handle(Request request, PolicyDecisionCallback pdp) 
	{
		if(request.getCategoryOccuriences(
				AttributeCategoryId.RESOURCE) != 1){
			return handleNext(request, pdp);
		}
		Attributes resource = Iterables.getOnlyElement(request.getAttributes(AttributeCategoryId.RESOURCE));
		// check scope attribute
		Collection<AttributeValue> scopeAttrs = resource.getAttributeValues(
				SCOPE_ATTRIBUTE, null, XacmlDataTypes.STRING.getType());
		if(scopeAttrs.size() > 1){
			return Collections.singleton(
					new Result(
							new Status(StatusCode.createSyntaxError(), 
									String.format(
											"Found one than more attribute with id=\"%s\" of type=\"%s\"", 
											SCOPE_ATTRIBUTE))));
		}
		StringValue scope = (StringValue)Iterables.getOnlyElement(scopeAttrs);
		if(!scope.getValue().equals(SCOPE_XPATH_EXPRESSION)){
			return handleNext(request, pdp);
		}
		// get resource id if scope is XPATH
		Collection<AttributeValue> resourceIdAttrs = resource.getAttributeValues(
				RESOURCE_ID, null, XacmlDataTypes.XPATHEXPRESSION.getType());
		if(resourceIdAttrs.isEmpty()){
			return handleNext(request, pdp);
		}
		if(resourceIdAttrs.size() > 1){
			return Collections.singleton(
					new Result(
							new Status(StatusCode.createSyntaxError(), 
									String.format(
											"Found one than more \"%s\" resourcr attribute of type=\"%s\"", 
											RESOURCE_ID, XacmlDataTypes.XPATHEXPRESSION)
											)
							)
					);
		}
		XPathExpressionValue xpath = (XPathExpressionValue)Iterables.getOnlyElement(resourceIdAttrs); 
		Node content = resource.getContent();
		if(content == null){
			return Collections.singleton(
					new Result(
							new Status(StatusCode.createSyntaxError(), 
									"Content must be specified in the request")));
		}
		RequestDefaults requestDefaults = request.getRequestDefaults();
		Collection<String> individualResources = new LinkedList<String>();
		try
		{
			NodeList nodes = xpathProvider.evaluateToNodeSet(requestDefaults.getXPathVersion(), xpath.getValue(), content);
			for(int i  = 0; i < nodes.getLength(); i++){
				individualResources.add(DOMUtil.getXPath(nodes.item(i)));
			}
			
		}catch(XPathEvaluationException e){
			return Collections.singleton(new Result(
					new Status(
							StatusCode.createProcessingError(), 
							String.format("Failed to evaluate xpath=\"%s\" expression", xpath), 
							e.getMessage())));
		}
		return Collections.emptyList();
	}
}
