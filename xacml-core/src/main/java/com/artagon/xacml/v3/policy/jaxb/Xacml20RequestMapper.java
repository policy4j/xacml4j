package com.artagon.xacml.v3.policy.jaxb;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.JAXBElement;

import org.oasis.xacml.v20.context.ActionType;
import org.oasis.xacml.v20.context.AttributeType;
import org.oasis.xacml.v20.context.AttributeValueType;
import org.oasis.xacml.v20.context.EnvironmentType;
import org.oasis.xacml.v20.context.RequestType;
import org.oasis.xacml.v20.context.ResourceContentType;
import org.oasis.xacml.v20.context.ResourceType;
import org.oasis.xacml.v20.context.SubjectType;
import org.w3c.dom.Node;

import com.artagon.xacml.v3.Attribute;
import com.artagon.xacml.v3.AttributeCategoryId;
import com.artagon.xacml.v3.AttributeValue;
import com.artagon.xacml.v3.Attributes;
import com.artagon.xacml.v3.Request;
import com.artagon.xacml.v3.RequestFactory;
import com.artagon.xacml.v3.RequestSyntaxException;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;

public class Xacml20RequestMapper
{
	private RequestFactory contextFactory;
	
	public Xacml20RequestMapper(RequestFactory factory){
		Preconditions.checkNotNull(factory);
		this.contextFactory = factory;
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
		return contextFactory.createRequest(false, attributes);
	}
	
	Attributes createSubject(SubjectType subject) throws RequestSyntaxException
	{
		return contextFactory.createAttributes(subject.getSubjectCategory(), 
				null, create(subject.getAttribute()));
	}
	
	Attributes createEnviroment(EnvironmentType subject) throws RequestSyntaxException
	{
		return contextFactory.createAttributes(AttributeCategoryId.ENVIRONMENT.toString(), 
				null, create(subject.getAttribute()));
	}
	
	Attributes createAction(ActionType subject) throws RequestSyntaxException
	{
		return contextFactory.createAttributes(AttributeCategoryId.ACTION.toString(), 
				null, create(subject.getAttribute()));
	}
	
	Attributes createResource(ResourceType resource) throws RequestSyntaxException
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
		return contextFactory.createAttribute(a.getAttributeId(), a.getIssuer(), values);
	}
	
	/**
	 * Creates {@link AttributeValue} from a given
	 * {@link JAXBElement}
	 * 
	 * @param element a JAXB element
	 * @return {@link AttributeValue}
	 * @throws RequestSyntaxException
	 */
	AttributeValue createValue(String dataTypeId, AttributeValueType value) 
		throws RequestSyntaxException
	{
		List<Object> content = value.getContent();
		if(content == null || 
				content.isEmpty()){
			throw new RequestSyntaxException("Attribute does not have content");
		}
		return contextFactory.createValue(dataTypeId, Iterables.getOnlyElement(content));
	}
}
