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
import com.artagon.xacml.v3.Result;
import com.artagon.xacml.v3.Status;
import com.artagon.xacml.v3.StatusCode;
import com.artagon.xacml.v3.pdp.PolicyDecisionCallback;
import com.artagon.xacml.v3.policy.spi.XPathEvaluationException;
import com.artagon.xacml.v3.policy.spi.XPathProvider;
import com.artagon.xacml.v3.types.XPathExpressionType;
import com.artagon.xacml.v3.types.XacmlDataTypes;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;

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
		Collection<Result> results = new LinkedList<Result>();
		for(AttributeCategoryId category : request.getCategories())
		{
			Attributes attr = Iterables.getOnlyElement(request.getAttributes(category));
			Collection<AttributeValue> values = attr.getAttributeValues(MULTIPLE_CONTENT_SELECTOR, 
					XacmlDataTypes.XPATHEXPRESSION.getType());
			if(values == null || 
					values.isEmpty()){
				continue;
			}
			if(values.size() > 1){
				results.add(new Result(new Status(
						StatusCode.createSyntaxError(), String.format(
								"Request category=\"%s\" contains multiple attribute values wth id=\"%s\"", 
								category, MULTIPLE_CONTENT_SELECTOR))
				));
				continue;
			}
			XPathExpressionType.XPathExpressionValue selector = (XPathExpressionType.XPathExpressionValue)Iterables.getOnlyElement(values);
			Node content = attr.getContent();
			try 
			{
				NodeList nodeSet = xpathProvider.evaluateToNodeSet(
						request.getRequestDefaults().getXPathVersion(),
						selector.getValue(), 
						content);
				Collection<String> individualResources = new LinkedList<String>();
				for(int i = 0; i < nodeSet.getLength(); i++){	
					String xpath = DOMUtil.getXPath(nodeSet.item(i));
					log.debug("Individual resource xpath=\"{}\"", xpath);
					individualResources.add(xpath);
				}
				
			}catch (XPathEvaluationException e) {
				results.add(new Result(new Status(
						StatusCode.createProcessingError(), String.format(
								"Failed to evaluate xpath=\"%s\" expression", 
								selector.getValue()))
				));
			}
			
		}
		return Collections.emptyList();
	}
}
