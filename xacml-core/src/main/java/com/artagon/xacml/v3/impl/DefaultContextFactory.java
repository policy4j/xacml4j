package com.artagon.xacml.v3.impl;

import java.util.Collection;

import org.w3c.dom.Node;

import com.artagon.xacml.v3.Advice;
import com.artagon.xacml.v3.Attribute;
import com.artagon.xacml.v3.AttributeCategoryId;
import com.artagon.xacml.v3.AttributeValue;
import com.artagon.xacml.v3.Attributes;
import com.artagon.xacml.v3.AttributesReference;
import com.artagon.xacml.v3.PolicyIdentifier;
import com.artagon.xacml.v3.RequestContextFactory;
import com.artagon.xacml.v3.Decision;
import com.artagon.xacml.v3.Obligation;
import com.artagon.xacml.v3.Request;
import com.artagon.xacml.v3.RequestReference;
import com.artagon.xacml.v3.Response;
import com.artagon.xacml.v3.Result;

public final class DefaultContextFactory implements RequestContextFactory
{
	
	@Override
	public Result createResult(Decision decision, 
			Collection<Advice> advice,
			Collection<Obligation> obligations,
			Collection<Attributes> attributes,
			Collection<PolicyIdentifier> policyIdList) {
		return new Result(decision, advice, obligations, attributes, policyIdList);
	}


	@Override
	public Attribute createAttribute(String attributeId, 
			Collection<AttributeValue> values) {
		return new DefaultAttribute(attributeId, values);
	}

	@Override
	public Attributes createAttributes(String id, String categoryId,
			Node content, Collection<Attribute> attributes) {
		AttributeCategoryId category = AttributeCategoryId.parse(categoryId);
		return new DefaultAttributes(id, category, content, attributes);
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
