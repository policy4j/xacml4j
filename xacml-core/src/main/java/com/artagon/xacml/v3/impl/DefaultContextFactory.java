package com.artagon.xacml.v3.impl;

import java.util.Collection;

import org.w3c.dom.Node;

import com.artagon.xacml.v3.Advice;
import com.artagon.xacml.v3.Attribute;
import com.artagon.xacml.v3.AttributeCategoryId;
import com.artagon.xacml.v3.AttributeValue;
import com.artagon.xacml.v3.AttributeValueType;
import com.artagon.xacml.v3.Attributes;
import com.artagon.xacml.v3.AttributesReference;
import com.artagon.xacml.v3.Decision;
import com.artagon.xacml.v3.Obligation;
import com.artagon.xacml.v3.PolicyIdentifier;
import com.artagon.xacml.v3.Request;
import com.artagon.xacml.v3.RequestFactory;
import com.artagon.xacml.v3.RequestReference;
import com.artagon.xacml.v3.RequestSyntaxException;
import com.artagon.xacml.v3.Response;
import com.artagon.xacml.v3.Result;
import com.artagon.xacml.v3.policy.type.DataTypes;

public final class DefaultContextFactory implements RequestFactory
{
	
	@Override
	public Result createResult(Decision decision, 
			Collection<Advice> advice,
			Collection<Obligation> obligations,
			Collection<Attributes> attributes,
			Collection<PolicyIdentifier> policyIdList) throws RequestSyntaxException
	{
		return new Result(decision, advice, obligations, attributes, policyIdList);
	}

	@Override
	public Attribute createAttribute(String attributeId, 
			Collection<AttributeValue> values)
		throws RequestSyntaxException
	{
		return new DefaultAttribute(attributeId, values);
	}
	
	

	@Override
	public Attribute createAttribute(
			String attributeId, 
			String issuer,
			boolean includeInResult, 
			Collection<AttributeValue> values) 
		throws RequestSyntaxException
	{
		return new DefaultAttribute(attributeId, issuer, includeInResult, values);
	}
	
	@Override
	public Attribute createAttribute(
			String attributeId, 
			String issuer,
			Collection<AttributeValue> values) 
		throws RequestSyntaxException
	{
		return createAttribute(attributeId, issuer, false, values);
	}
	
	@Override
	public AttributeValue createValue(String typeId, Object value) 
		throws RequestSyntaxException
	{
		AttributeValueType type = DataTypes.getByTypeId(typeId);
		if(type == null){
			throw new RequestSyntaxException(
					"TypeId=\"%s\" can not be resolved as an XACML type",typeId);
		}
		try{
			return type.create(value);
		}catch(Exception e){
			throw new RequestSyntaxException(e);
		}
	}
	

	@Override
	public Attributes createAttributes(String id, String categoryId,
			Node content, Collection<Attribute> attributes) {
		AttributeCategoryId category = AttributeCategoryId.parse(categoryId);
		return new DefaultAttributes(id, category, content, attributes);
	}
	
	@Override
	public Attributes createAttributes(String categoryId,
			Node content, Collection<Attribute> attributes) {
		AttributeCategoryId category = AttributeCategoryId.parse(categoryId);
		return new DefaultAttributes(null, category, content, attributes);
	}

	@Override
	public AttributesReference createAttributesReference(String id) {
		return new DefaultAttributesReference(id);
	}

	@Override
	public Request createRequest(boolean returnPolicyIDList,
			Collection<Attributes> attributes,
			Collection<RequestReference> references) {
		return new DefaultRequest(returnPolicyIDList, attributes, references);
	}

	@Override
	public Request createRequest(boolean returnPolicyIDList,
			Collection<Attributes> attributes) {
		return new DefaultRequest(returnPolicyIDList, attributes);
	}

	@Override
	public RequestReference createRequestReference(
			Collection<AttributesReference> references) {
		return new DefaultRequestReference(references);
	}

	@Override
	public Response createResponse(Collection<Result> results) {
		return new DefaultResponse(results);
	}
	
}
