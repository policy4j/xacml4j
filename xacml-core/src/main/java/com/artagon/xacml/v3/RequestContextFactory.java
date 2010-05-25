package com.artagon.xacml.v3;

import java.util.Collection;

import org.w3c.dom.Node;

public interface RequestContextFactory 
{
	/**
	 * Creates {@link Request} instance
	 * 
	 * @param returnPolicyIDList a flag
	 * @param attributes a collection of
	 * {@link Attributes} instances
	 * @param references a collection of
	 * request references
	 * @return an instance of {@link Request}
	 */
	Request createRequest(boolean returnPolicyIDList, 
			Collection<Attributes> attributes, 
			Collection<RequestReference> references);
	
	Request createRequest(boolean returnPolicyIDList, 
			Collection<Attributes> attributes);
		
	Response createResponse(Collection<Result> results);
	
	
	Result createResult(Decision decision, 
			Collection<Advice> advice, 
			Collection<Obligation> obligations, 
			Collection<Attributes> attributes, 
			Collection<PolicyIdentifier> policyIdList);
	
	/**
	 * Creates {@link Attributes} instance
	 * 
	 * @param id an unique identifier
	 * @param categoryId an category identifier
	 * @param content an XML content
	 * @param attributes a collection of attributes
	 * @return {@link Attributes} instance
	 */
	Attributes createAttributes(
			String id, 
			String categoryId, 
			Node content, 
			Collection<Attribute> attributes);
	
	Attribute createAttribute(String attributeId, Collection<AttributeValue> values);
	
	RequestReference createRequestReference(
			Collection<AttributesReference> references);
	
	AttributesReference createAttributesReference(String id);
}
