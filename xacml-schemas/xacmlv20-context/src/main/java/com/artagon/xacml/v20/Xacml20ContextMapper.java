package com.artagon.xacml.v20;

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
import com.artagon.xacml.v3.Request;
import com.artagon.xacml.v3.RequestSyntaxException;
import com.artagon.xacml.v3.Response;
import com.artagon.xacml.v3.Result;
import com.artagon.xacml.v3.types.XacmlDataTypes;
import com.google.common.collect.Iterables;

public class Xacml20ContextMapper
{
	private final static String RESOURCE_ID_ATTRIBUTE = "urn:oasis:names:tc:xacml:1.0:resource:resource-id";
	
	
	public Xacml20ContextMapper(){
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
	
	public Request create(RequestType req) throws RequestSyntaxException
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
		return new Request(false, attributes);
	}
	
	private Attributes createSubject(SubjectType subject) throws RequestSyntaxException
	{
		AttributeCategoryId category = getCategoryId(subject.getSubjectCategory());
		return new Attributes(category, create(subject.getAttribute()));
	}
	
	private AttributeCategoryId getCategoryId(String id) throws RequestSyntaxException
	{
		AttributeCategoryId category = AttributeCategoryId.parse(id);
		if(category == null){
			throw new RequestSyntaxException("Unknown attrobute category=\"%s\"", id);
		}
		return category;
	}
	
	private Attributes createEnviroment(EnvironmentType subject) throws RequestSyntaxException
	{
		return new Attributes(AttributeCategoryId.ENVIRONMENT, 
				null, create(subject.getAttribute()));
	}
	
	private Attributes createAction(ActionType subject) throws RequestSyntaxException
	{
		return new Attributes(AttributeCategoryId.ACTION, 
				null, create(subject.getAttribute()));
	}
	
	private Attributes createResource(ResourceType resource) throws RequestSyntaxException
	{
		return new Attributes(AttributeCategoryId.RESOURCE, 
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
		throws RequestSyntaxException
	{
		Collection<Attribute> attributes = new LinkedList<Attribute>();
		for(AttributeType a : contextAttributes){
			attributes.add(createAttribute(a));
		}
		return attributes;
	}
	
	private Attribute createAttribute(AttributeType a) 
		throws RequestSyntaxException
	{
		Collection<AttributeValue> values = new LinkedList<AttributeValue>();
		for(AttributeValueType v : a.getAttributeValue()){
			values.add(createValue(a.getDataType(), v));
		}
		String attributeId = a.getAttributeId();
		return new Attribute(a.getAttributeId(), a.getIssuer(), 
				attributeId.equals(RESOURCE_ID_ATTRIBUTE), values);
	}
	
	private AttributeValue createValue(String dataTypeId, AttributeValueType value) 
		throws RequestSyntaxException
	{
		List<Object> content = value.getContent();
		if(content == null || 
				content.isEmpty()){
			throw new RequestSyntaxException("Attribute does not have content");
		}
		com.artagon.xacml.v3.AttributeValueType dataType = XacmlDataTypes.getByTypeId(dataTypeId);
		if(dataType == null){
			throw new RequestSyntaxException(
					"DataTypeId=\"%s\" can be be resolved to valid XACML type", dataTypeId);
		}
		return dataType.create(Iterables.getOnlyElement(content));
	}
}
