package com.artagon.xacml.v3.policy;

import java.util.HashMap;
import java.util.Map;

import javax.xml.xpath.XPath;

import org.w3c.dom.Node;

import com.artagon.xacml.util.Preconditions;
import com.artagon.xacml.v3.AttributeCategoryId;

final class CachingAttributeResolverDecorator implements AttributeResolver
{
	private AttributeResolver resolver;
	private Map<AttributeCategoryId, Node> contentCache;
	
	public CachingAttributeResolverDecorator(AttributeResolver resolver){
		Preconditions.checkNotNull(resolver);
		this.resolver = resolver;
		this.contentCache = new HashMap<AttributeCategoryId, Node>(16);
		
	}
	
	@Override
	public Node getContent(AttributeCategoryId categoryId) {
		Node content = contentCache.get(categoryId);
		if(content == null){
			content = resolver.getContent(categoryId);
			if(content != null){
				contentCache.put(categoryId, content);
			}
		}
		return content;
	}

	@Override
	public BagOfAttributeValues<AttributeValue> resolve(
			AttributeCategoryId category, String attributeId,
			AttributeValueType dataType, String issuer) {
		return null;
	}

	@Override
	public BagOfAttributeValues<AttributeValue> resolve(
			AttributeCategoryId category, XPath location,
			AttributeValueType dataType) {
		return null;
	}
	
}
