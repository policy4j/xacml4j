package com.artagon.xacml.v3.policy.jaxb;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.oasis.xacml.v20.context.ActionType;
import org.oasis.xacml.v20.context.AttributeType;
import org.oasis.xacml.v20.context.AttributeValueType;
import org.oasis.xacml.v20.context.EnvironmentType;
import org.oasis.xacml.v20.context.RequestType;
import org.oasis.xacml.v20.context.ResourceContentType;
import org.oasis.xacml.v20.context.ResourceType;
import org.oasis.xacml.v20.context.ResponseType;
import org.oasis.xacml.v20.context.ResultType;
import org.oasis.xacml.v20.context.SubjectType;
import org.w3c.dom.Node;

import com.artagon.xacml.v3.Attribute;
import com.artagon.xacml.v3.AttributeCategoryId;
import com.artagon.xacml.v3.AttributeValue;
import com.artagon.xacml.v3.Attributes;
import com.artagon.xacml.v3.ContextFactory;
import com.artagon.xacml.v3.ContextSyntaxException;
import com.artagon.xacml.v3.Request;
import com.artagon.xacml.v3.Response;
import com.artagon.xacml.v3.Result;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;

public class Xacml20ContextMapper
{
	private final static String RESOURCE_ID_ATTRIBUTE = "urn:oasis:names:tc:xacml:1.0:resource:resource-id";
	
	private ContextFactory contextFactory;
	
	public Xacml20ContextMapper(ContextFactory factory){
		Preconditions.checkNotNull(factory);
		this.contextFactory = factory;
	}
	
	public ResponseType create(Response response)
	{
		ResponseType responseV2 = new ResponseType();
		List<ResultType> results = responseV2.getResult();
		for(Result resultV3 : response.getResults()){
			results.add(create(resultV3));
		}
		return responseV2;
	}
		
	private ResultType create(Result result){
		ResultType resultv2 = new ResultType();
		return resultv2;
	}
	
	public Request create(RequestType req) throws ContextSyntaxException
	{
		Collection<Attributes> attributes = new LinkedList<Attributes>();
		if(!req.getResource().isEmpty()){
			for(ResourceType resource : req.getResource()){
				attributes.add(createResource(resource));
			}
		}
		if(!req.getSubject().isEmpty()){
			for(SubjectType resource : req.getSubject()){
				attributes.add(createSubject(resource));
			}
		}
		if(req.getAction() != null){
			attributes.add(createAction(req.getAction()));
		}
		if(req.getEnvironment() != null){
			attributes.add(createEnviroment(req.getEnvironment()));
		}
		return contextFactory.createRequest(false, attributes);
	}
	
	private Attributes createSubject(SubjectType subject) throws ContextSyntaxException
	{
		return contextFactory.createAttributes(subject.getSubjectCategory(), 
				null, create(subject.getAttribute()));
	}
	
	private Attributes createEnviroment(EnvironmentType subject) throws ContextSyntaxException
	{
		return contextFactory.createAttributes(AttributeCategoryId.ENVIRONMENT.toString(), 
				null, create(subject.getAttribute()));
	}
	
	private Attributes createAction(ActionType subject) throws ContextSyntaxException
	{
		return contextFactory.createAttributes(AttributeCategoryId.ACTION.toString(), 
				null, create(subject.getAttribute()));
	}
	
	private Attributes createResource(ResourceType resource) throws ContextSyntaxException
	{
		return contextFactory.createAttributes(AttributeCategoryId.RESOURCE.toString(), 
				getResourceContent(resource), create(resource.getAttribute()));
	}
	
	private Node getResourceContent(ResourceType resource)
	{
		ResourceContentType content = resource.getResourceContent();
		if(content == null){
			return null;
		}
		for(Object o : content.getContent()){
			if(o instanceof Node){
				return (Node)o;
			}
		}
		return null;
	}
	
	private Collection<Attribute> create(Collection<AttributeType> contextAttributes) 
		throws ContextSyntaxException
	{
		Collection<Attribute> attributes = new LinkedList<Attribute>();
		for(AttributeType a : contextAttributes){
			attributes.add(createAttribute(a));
		}
		return attributes;
	}
	
	private Attribute createAttribute(AttributeType a) 
		throws ContextSyntaxException
	{
		Collection<AttributeValue> values = new LinkedList<AttributeValue>();
		for(AttributeValueType v : a.getAttributeValue()){
			values.add(createValue(a.getDataType(), v));
		}
		String attributeId = a.getAttributeId();
		return contextFactory.createAttribute(a.getAttributeId(), a.getIssuer(), 
				attributeId.equals(RESOURCE_ID_ATTRIBUTE), values);
	}
	
	private AttributeValue createValue(String dataTypeId, AttributeValueType value) 
		throws ContextSyntaxException
	{
		List<Object> content = value.getContent();
		if(content == null || 
				content.isEmpty()){
			throw new ContextSyntaxException("Attribute does not have content");
		}
		return contextFactory.createValue(dataTypeId, Iterables.getOnlyElement(content));
	}
}
