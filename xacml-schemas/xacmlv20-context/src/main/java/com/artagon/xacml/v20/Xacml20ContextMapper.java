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
import org.oasis.xacml.v20.context.StatusCodeType;
import org.oasis.xacml.v20.context.StatusType;
import org.oasis.xacml.v20.context.SubjectType;
import org.oasis.xacml.v20.policy.AttributeAssignmentType;
import org.oasis.xacml.v20.policy.ObligationType;
import org.oasis.xacml.v20.policy.ObligationsType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

import com.artagon.xacml.v3.Attribute;
import com.artagon.xacml.v3.AttributeAssignment;
import com.artagon.xacml.v3.AttributeCategoryId;
import com.artagon.xacml.v3.AttributeValue;
import com.artagon.xacml.v3.Attributes;
import com.artagon.xacml.v3.Obligation;
import com.artagon.xacml.v3.Request;
import com.artagon.xacml.v3.RequestSyntaxException;
import com.artagon.xacml.v3.Response;
import com.artagon.xacml.v3.Result;
import com.artagon.xacml.v3.Status;
import com.artagon.xacml.v3.types.XacmlDataTypes;
import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

public class Xacml20ContextMapper
{
	private final static Logger log = LoggerFactory.getLogger(Xacml20ContextMapper.class);
	
	private final static String CONTENT_SELECTOR = "urn:oasis:names:tc:xacml:3.0:content-selector";
	private final static String RESOURCE_ID = "urn:oasis:names:tc:xacml:2.0:resource:resource-id";
	
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
		
	private ResultType create(Result result)
	{
		ResultType resultv2 = new ResultType();
		resultv2.setStatus(createStatus(result.getStatus()));
		resultv2.setResourceId(getResourceId(result));
		resultv2.setObligations(getObligations(result));
		return resultv2;
	}
	
	private StatusType createStatus(Status status)
	{
		if(log.isDebugEnabled()){
			log.debug("Mapping status=\"{}\"", status);
		}
		StatusType statusType = new StatusType();
		StatusCodeType codeType = new StatusCodeType();
		statusType.setStatusCode(codeType);
		codeType.setValue(status.getStatusCode().getValue().toString());
		statusType.setStatusMessage(status.getMessage());
		return statusType;
	}
	
	/**
	 * Tries to locate resource id attribute
	 * 
	 * @param result an evaluation result
	 * @return a resource id attribute
	 */
	private String getResourceId(Result result)
	{
		Attributes resource = result.getAttribute(AttributeCategoryId.RESOURCE);
		if(resource == null){
			return null;
		}
		Collection<Attribute> attrs = resource.getAttributes(RESOURCE_ID);
		if(attrs.size() == 1){
			Attribute resourceId = Iterables.getOnlyElement(attrs);
			return Iterables.getOnlyElement(resourceId.getValues()).toXacmlString();
		}
		Collection<AttributeValue> values =  resource.getAttributeValues(
				CONTENT_SELECTOR, XacmlDataTypes.XPATHEXPRESSION.getType());
		if(values.isEmpty() ||
				values.size() > 1){
			return null;
		}
		return Iterables.getOnlyElement(values).toXacmlString();
	}
	
	public ObligationsType getObligations(Result result)
	{
		Collection<Obligation> obligations = result.getObligations();
		if(obligations.isEmpty()){
			return null;
		}
		ObligationsType obligationsv2  = new ObligationsType();
		for(Obligation o : obligations){
			obligationsv2.getObligation().add(create(o));
		}
		return obligationsv2;
	}
	
	private ObligationType create(Obligation o){
		ObligationType obligation = new ObligationType();
		for(AttributeAssignment a : o.getAttributes()){
			obligation.getAttributeAssignment().add(create(a));
		}
		return obligation;
	}
	
	private AttributeAssignmentType create(AttributeAssignment a)
	{
		AttributeAssignmentType attr = new AttributeAssignmentType();
		com.artagon.xacml.v3.AttributeValueType t = (com.artagon.xacml.v3.AttributeValueType)(a.getAttribute().getType());
		attr.setDataType(t.getDataTypeId());
		attr.setAttributeId(a.getAttributeId());
		attr.getContent().add(a.getAttribute().toXacmlString());
		return attr;
	}
	
	public Request create(RequestType req) throws RequestSyntaxException
	{
		Collection<Attributes> attributes = new LinkedList<Attributes>();
		if(!req.getResource().isEmpty()){
			for(ResourceType resource : req.getResource()){
				attributes.add(createResource(resource));
			}
		}
		if(!req.getSubject().isEmpty())
		{
			Multimap<AttributeCategoryId, Attributes> map = LinkedHashMultimap.create();
			for(SubjectType subject : req.getSubject()){
				Attributes attr =  createSubject(subject);
				map.put(attr.getCategoryId(), attr);
			}
			attributes.addAll(normalize(map));
		}
		if(req.getAction() != null)
		{
			attributes.add(createAction(req.getAction()));
		}
		if(req.getEnvironment() != null)
		{
			attributes.add(createEnviroment(req.getEnvironment()));
		}
		return new Request(false, attributes);
	}
	
	public Collection<Attributes> normalize(Multimap<AttributeCategoryId, Attributes> attributes)
	{
		Collection<Attributes> normalized = new LinkedList<Attributes>();
		for(AttributeCategoryId categoryId : attributes.keySet()){
			Collection<Attributes> byCategory = attributes.get(categoryId);
			Collection<Attribute> categoryAttr = new LinkedList<Attribute>();
			for(Attributes a : byCategory){
				categoryAttr.addAll(a.getAttributes());
			}
			normalized.add(new Attributes(categoryId, categoryAttr));
		}
		return normalized;
	}
	
	private Attributes createSubject(SubjectType subject) throws RequestSyntaxException
	{
		AttributeCategoryId category = getCategoryId(subject.getSubjectCategory());
		return new Attributes(category, create(subject.getAttribute(), category));
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
				null, create(subject.getAttribute(), AttributeCategoryId.ENVIRONMENT));
	}
	
	private Attributes createAction(ActionType subject) throws RequestSyntaxException
	{
		return new Attributes(AttributeCategoryId.ACTION, 
				null, create(subject.getAttribute(), AttributeCategoryId.ACTION));
	}
	
	private Attributes createResource(ResourceType resource) throws RequestSyntaxException
	{
		return new Attributes(AttributeCategoryId.RESOURCE, 
				getResourceContent(resource), create(resource.getAttribute(), AttributeCategoryId.RESOURCE));
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
	
	private Collection<Attribute> create(Collection<AttributeType> contextAttributes, AttributeCategoryId category) 
		throws RequestSyntaxException
	{
		Collection<Attribute> attributes = new LinkedList<Attribute>();
		for(AttributeType a : contextAttributes){
			attributes.add(createAttribute(a, category));
		}
		return attributes;
	}
	
	private Attribute createAttribute(AttributeType a, AttributeCategoryId category) 
		throws RequestSyntaxException
	{
		Collection<AttributeValue> values = new LinkedList<AttributeValue>();
		for(AttributeValueType v : a.getAttributeValue()){
			values.add(createValue(a.getDataType(), v, category));
		}
		return new Attribute(a.getAttributeId(), a.getIssuer(), false, values);
	}
	
	private AttributeValue createValue(String dataTypeId, 
			AttributeValueType value, 
			AttributeCategoryId categoryId) 
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
					"DataTypeId=\"%s\" can be be " +
					"resolved to valid XACML type", dataTypeId);
		}
		Object o = Iterables.getOnlyElement(content);
		if(dataType.equals(XacmlDataTypes.XPATHEXPRESSION.getType())){
			String xpath = Xacml20PolicyMapper.transform20PathTo30((String)o);
			return dataType.create(xpath, categoryId);
		}
		return dataType.create(o);
	}
}
