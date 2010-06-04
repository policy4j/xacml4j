package com.artagon.xacml.v3.context;

import java.util.Collection;

import org.w3c.dom.Node;

import com.artagon.xacml.v3.AttributeValue;
import com.artagon.xacml.v3.PolicyIdentifier;
import com.artagon.xacml.v3.policy.AttributesReference;

public interface ContextFactory 
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
		throws ContextSyntaxException;
	
	Request createRequest(boolean returnPolicyIDList, 
			Collection<Attributes> attributes) 
		throws ContextSyntaxException;
	
	/**
	 * Creates {@link Response} from a
	 * given collection of {@link Result}
	 * instances
	 * 
	 * @param results a collection
	 * @return
	 * @throws ContextSyntaxException
	 */
	Response createResponse(Collection<Result> results) 
		throws ContextSyntaxException;
	
	
	Result createResult(Decision decision, 
			Collection<Advice> advice, 
			Collection<Obligation> obligations, 
			Collection<Attributes> attributes, 
			Collection<PolicyIdentifier> policyIdList) 
		throws ContextSyntaxException;
	
	AttributeValue createValue(String dataTypeId, Object value) 
		throws ContextSyntaxException;
	
	Attribute createAttribute(
			String attributeId, 
			String issuer, 
			boolean includeInResult, 
			Collection<AttributeValue> values) 
		throws ContextSyntaxException;
	
	Attribute createAttribute(
			String attributeId, 
			String issuer, 
			Collection<AttributeValue> values) 
		throws ContextSyntaxException;
	
	Attribute createAttribute(
			String attributeId, 
			boolean includeInResult,
			Collection<AttributeValue> values) 
		throws ContextSyntaxException;
	
	Attribute createAttribute(String attributeId, Collection<AttributeValue> values) 
		throws ContextSyntaxException;
	
	
	Attributes createAttributes(
			String id, 
			String categoryId, 
			Node content, 
			Collection<Attribute> attributes) 
		throws ContextSyntaxException;
	
	Attributes createAttributes(
			String categoryId, 
			Node content, 
			Collection<Attribute> attributes) 
		throws ContextSyntaxException;
		
	RequestReference createRequestReference(
			Collection<AttributesReference> references) 
		throws ContextSyntaxException;
	
	AttributesReference createAttributesReference(String id) 
		throws ContextSyntaxException;
}
