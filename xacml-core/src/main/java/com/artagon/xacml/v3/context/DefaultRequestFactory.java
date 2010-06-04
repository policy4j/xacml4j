package com.artagon.xacml.v3.context;

import java.util.Collection;

import org.w3c.dom.Node;

import com.artagon.xacml.v3.AttributeCategoryId;
import com.artagon.xacml.v3.AttributeValue;
import com.artagon.xacml.v3.AttributeValueType;
import com.artagon.xacml.v3.PolicyIdentifier;
import com.artagon.xacml.v3.policy.AttributesReference;
import com.artagon.xacml.v3.types.XacmlDataTypes;

public final class DefaultRequestFactory implements ContextFactory
{
	@Override
	public Result createResult(Decision decision, 
			Collection<Advice> advice,
			Collection<Obligation> obligations,
			Collection<Attributes> attributes,
			Collection<PolicyIdentifier> policyIdList) throws ContextSyntaxException
	{
		return new Result(decision, advice, obligations, attributes, policyIdList);
	}

	@Override
	public Attribute createAttribute(String attributeId, 
			Collection<AttributeValue> values)
		throws ContextSyntaxException
	{
		return createAttribute(attributeId, null, false, values);
	}
	
	

	@Override
	public Attribute createAttribute(String attributeId,
			boolean includeInResult, Collection<AttributeValue> values)
			throws ContextSyntaxException 
	{
		return createAttribute(attributeId, null, includeInResult, values);
	}
	
	@Override
	public Attribute createAttribute(
			String attributeId, String issuer, 
			Collection<AttributeValue> values)
			throws ContextSyntaxException 
	{
		return createAttribute(attributeId, issuer, false, values);
	}

	@Override
	public Attribute createAttribute(
			String attributeId, 
			String issuer,
			boolean includeInResult, 
			Collection<AttributeValue> values) 
		throws ContextSyntaxException
	{
		return new Attribute(attributeId, issuer, includeInResult, values);
	}
	
	
	@Override
	public AttributeValue createValue(String typeId, Object value) 
		throws ContextSyntaxException
	{
		AttributeValueType type = XacmlDataTypes.getByTypeId(typeId);
		if(type == null){
			throw new ContextSyntaxException(
					"TypeId=\"%s\" can not be resolved as an XACML type",typeId);
		}
		try{
			return type.create(value);
		}catch(Exception e){
			throw new ContextSyntaxException(e);
		}
	}
	

	@Override
	public Attributes createAttributes(String id, String categoryId,
			Node content, Collection<Attribute> attributes) {
		AttributeCategoryId category = AttributeCategoryId.parse(categoryId);
		return new Attributes(id, category, content, attributes);
	}
	
	@Override
	public Attributes createAttributes(String categoryId,
			Node content, Collection<Attribute> attributes) {
		AttributeCategoryId category = AttributeCategoryId.parse(categoryId);
		return new Attributes(null, category, content, attributes);
	}

	@Override
	public AttributesReference createAttributesReference(String id) {
		return new AttributesReference(id);
	}

	@Override
	public Request createRequest(boolean returnPolicyIDList,
			Collection<Attributes> attributes,
			Collection<RequestReference> references) {
		return new Request(returnPolicyIDList, attributes, references);
	}

	@Override
	public Request createRequest(boolean returnPolicyIDList,
			Collection<Attributes> attributes) {
		return new Request(returnPolicyIDList, attributes);
	}

	@Override
	public RequestReference createRequestReference(
			Collection<AttributesReference> references) {
		return new RequestReference(references);
	}

	@Override
	public Response createResponse(Collection<Result> results) {
		return new Response(results);
	}
	
}
