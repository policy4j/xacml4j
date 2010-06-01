package com.artagon.xacml.v3;

import java.util.Collection;

import org.w3c.dom.Node;

public interface RequestFactory 
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
			Collection<RequestReference> references) 
		throws RequestSyntaxException;
	
	Request createRequest(boolean returnPolicyIDList, 
			Collection<Attributes> attributes) 
		throws RequestSyntaxException;
	
	/**
	 * Creates {@link Response} from a
	 * given collection of {@link Result}
	 * instances
	 * 
	 * @param results a collection
	 * @return
	 * @throws RequestSyntaxException
	 */
	Response createResponse(Collection<Result> results) 
		throws RequestSyntaxException;
	
	
	Result createResult(Decision decision, 
			Collection<Advice> advice, 
			Collection<Obligation> obligations, 
			Collection<Attributes> attributes, 
			Collection<PolicyIdentifier> policyIdList) 
		throws RequestSyntaxException;
	
	AttributeValue createValue(String dataTypeId, Object value) 
		throws RequestSyntaxException;
	
	Attribute createAttribute(
			String attributeId, 
			String issuer, 
			boolean includeInResult, 
			Collection<AttributeValue> values) 
		throws RequestSyntaxException;
	
	Attribute createAttribute(
			String attributeId, 
			String issuer, 
			Collection<AttributeValue> values) 
		throws RequestSyntaxException;
	
	Attribute createAttribute(
			String attributeId, 
			boolean includeInResult,
			Collection<AttributeValue> values) 
		throws RequestSyntaxException;
	
	Attribute createAttribute(String attributeId, Collection<AttributeValue> values) 
		throws RequestSyntaxException;
	
	
	Attributes createAttributes(
			String id, 
			String categoryId, 
			Node content, 
			Collection<Attribute> attributes) 
		throws RequestSyntaxException;
	
	Attributes createAttributes(
			String categoryId, 
			Node content, 
			Collection<Attribute> attributes) 
		throws RequestSyntaxException;
		
	RequestReference createRequestReference(
			Collection<AttributesReference> references) 
		throws RequestSyntaxException;
	
	AttributesReference createAttributesReference(String id) 
		throws RequestSyntaxException;
}
