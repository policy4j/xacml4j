package com.artagon.xacml.v20;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.oasis.xacml.v20.jaxb.context.ActionType;
import org.oasis.xacml.v20.jaxb.context.AttributeType;
import org.oasis.xacml.v20.jaxb.context.AttributeValueType;
import org.oasis.xacml.v20.jaxb.context.DecisionType;
import org.oasis.xacml.v20.jaxb.context.EnvironmentType;
import org.oasis.xacml.v20.jaxb.context.RequestType;
import org.oasis.xacml.v20.jaxb.context.ResourceContentType;
import org.oasis.xacml.v20.jaxb.context.ResourceType;
import org.oasis.xacml.v20.jaxb.context.ResponseType;
import org.oasis.xacml.v20.jaxb.context.ResultType;
import org.oasis.xacml.v20.jaxb.context.StatusCodeType;
import org.oasis.xacml.v20.jaxb.context.StatusType;
import org.oasis.xacml.v20.jaxb.context.SubjectType;
import org.oasis.xacml.v20.jaxb.policy.AttributeAssignmentType;
import org.oasis.xacml.v20.jaxb.policy.EffectType;
import org.oasis.xacml.v20.jaxb.policy.ObligationType;
import org.oasis.xacml.v20.jaxb.policy.ObligationsType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.artagon.xacml.util.DOMUtil;
import com.artagon.xacml.util.Xacml20XPathTo30Transformer;
import com.artagon.xacml.v3.Attribute;
import com.artagon.xacml.v3.AttributeAssignment;
import com.artagon.xacml.v3.AttributeCategoryId;
import com.artagon.xacml.v3.AttributeValue;
import com.artagon.xacml.v3.Attributes;
import com.artagon.xacml.v3.Decision;
import com.artagon.xacml.v3.Effect;
import com.artagon.xacml.v3.Obligation;
import com.artagon.xacml.v3.RequestContext;
import com.artagon.xacml.v3.RequestSyntaxException;
import com.artagon.xacml.v3.ResponseContext;
import com.artagon.xacml.v3.Result;
import com.artagon.xacml.v3.Status;
import com.artagon.xacml.v3.XacmlSyntaxException;
import com.artagon.xacml.v3.types.XPathExpressionType;
import com.artagon.xacml.v3.types.XacmlDataTypes;
import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

class Xacml20ContextMapper
{
	private final static Logger log = LoggerFactory.getLogger(Xacml20ContextMapper.class);
	
	private final static String CONTENT_SELECTOR = "urn:oasis:names:tc:xacml:3.0:content-selector";
	private final static String RESOURCE_ID = "urn:oasis:names:tc:xacml:1.0:resource:resource-id";
	
	private final static Map<Decision, DecisionType> v30ToV20DecisionMapping = new HashMap<Decision, DecisionType>();
	private final static Map<DecisionType, Decision> v20ToV30DecisionMapping = new HashMap<DecisionType, Decision>();
	
	private final static Map<EffectType, Effect> v20ToV30EffectnMapping = new HashMap<EffectType, Effect>();
	private final static Map<Effect, EffectType> v30ToV20EffectnMapping = new HashMap<Effect, EffectType>();
	
	static
	{
		v30ToV20DecisionMapping.put(Decision.DENY, DecisionType.DENY);
		v30ToV20DecisionMapping.put(Decision.PERMIT, DecisionType.PERMIT);
		v30ToV20DecisionMapping.put(Decision.NOT_APPLICABLE, DecisionType.NOT_APPLICABLE);
		v30ToV20DecisionMapping.put(Decision.INDETERMINATE, DecisionType.INDETERMINATE);
		v30ToV20DecisionMapping.put(Decision.INDETERMINATE_D, DecisionType.INDETERMINATE);
		v30ToV20DecisionMapping.put(Decision.INDETERMINATE_P, DecisionType.INDETERMINATE);
		v30ToV20DecisionMapping.put(Decision.INDETERMINATE_DP, DecisionType.INDETERMINATE);
		
		v20ToV30DecisionMapping.put(DecisionType.DENY, Decision.DENY);
		v20ToV30DecisionMapping.put(DecisionType.PERMIT, Decision.PERMIT);
		v20ToV30DecisionMapping.put(DecisionType.NOT_APPLICABLE, Decision.NOT_APPLICABLE);
		v20ToV30DecisionMapping.put(DecisionType.INDETERMINATE, Decision.INDETERMINATE);
		
		
		v20ToV30EffectnMapping.put(EffectType.DENY, Effect.DENY);
		v20ToV30EffectnMapping.put(EffectType.PERMIT, Effect.PERMIT);
		
		v30ToV20EffectnMapping.put(Effect.DENY, EffectType.DENY);
		v30ToV20EffectnMapping.put(Effect.PERMIT, EffectType.PERMIT);
	
	}
	
	public ResponseType create(ResponseContext response)
	{
		if(log.isDebugEnabled()){
			log.debug("Mapping response=\"{}\"", response);
		}
		ResponseType responseV2 = new ResponseType();
		List<ResultType> results = responseV2.getResult();
		for(Result resultV3 : response.getResults()){
			results.add(create(resultV3));
		}
		return responseV2;
	}
	
	public ResponseContext create(ResponseType response)
	{
		Collection<Result> results = new LinkedList<Result>();
		for(ResultType result : response.getResult()){
			results.add(create(result));
		}
		return new ResponseContext(results);
	}
	
	private Result create(ResultType result)
	{
		return null;
	}
	
	private ResultType create(Result result)
	{
		ResultType resultv2 = new ResultType();
		resultv2.setStatus(createStatus(result.getStatus()));
		resultv2.setResourceId(getResourceId(result));
		resultv2.setObligations(getObligations(result));
		resultv2.setDecision(v30ToV20DecisionMapping.get(result.getDecision()));
		return resultv2;
	}
	
	private StatusType createStatus(Status status)
	{
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
		if(log.isDebugEnabled()){
			log.debug("Mapping result=\"{}\" to resourceId", result);
		}
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
				CONTENT_SELECTOR, XPathExpressionType.XPATHEXPRESSION);
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
		obligation.setObligationId(o.getId());
		obligation.setFulfillOn(v30ToV20EffectnMapping.get(o.getFullfillOn()));
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
	
	public RequestContext create(RequestType req) throws XacmlSyntaxException
	{
		Collection<Attributes> attributes = new LinkedList<Attributes>();
		if(!req.getResource().isEmpty()){
			
			for(ResourceType resource : req.getResource()){
				attributes.add(createResource(resource, req.getResource().size() > 1));
			}
		}
		if(!req.getSubject().isEmpty())
		{
			Multimap<AttributeCategoryId, Attributes> map = LinkedHashMultimap.create();
			for(SubjectType subject : req.getSubject()){
				Attributes attr =  createSubject(subject);
				map.put(attr.getCategory(), attr);
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
		return new RequestContext(false, attributes);
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
	
	private Attributes createSubject(SubjectType subject) 
		throws XacmlSyntaxException
	{
		AttributeCategoryId category = getCategoryId(subject.getSubjectCategory());
		if(log.isDebugEnabled()){
			log.debug("Processing subject category=\"{}\"", category);
		}
		return new Attributes(category, create(subject.getAttribute(), category, false));
	}
	
	private AttributeCategoryId getCategoryId(String id) 
		throws XacmlSyntaxException
	{
		AttributeCategoryId category = AttributeCategoryId.parse(id);
		if(category == null){
			throw new RequestSyntaxException("Unknown attribute category=\"%s\"", id);
		}
		return category;
	}
	
	private Attributes createEnviroment(EnvironmentType subject) 
		throws XacmlSyntaxException
	{
		return new Attributes(AttributeCategoryId.ENVIRONMENT, 
				null, create(subject.getAttribute(), AttributeCategoryId.ENVIRONMENT, false));
	}
	
	private Attributes createAction(ActionType subject) throws XacmlSyntaxException
	{
		return new Attributes(AttributeCategoryId.ACTION, 
				null, create(subject.getAttribute(), AttributeCategoryId.ACTION, false));
	}
	
	private Attributes createResource(ResourceType resource, 
			boolean multipleResources) throws XacmlSyntaxException
	{
		Node content = getResourceContent(resource);
		if(content != null){
			content = DOMUtil.copyNode(content);
		}
		return new Attributes(AttributeCategoryId.RESOURCE, 
				content, 
				create(resource.getAttribute(), 
						AttributeCategoryId.RESOURCE, multipleResources));
	}
	
	private Node getResourceContent(ResourceType resource)
	{
		ResourceContentType content = resource.getResourceContent();
		if(content == null){
			return null;
		}
		for(Object o : content.getContent())
		{
			if(o instanceof Element){
				Node node = (Node)o;
				return node;
			}
		}
		return null;
	}
	
	private Collection<Attribute> create(Collection<AttributeType> contextAttributes, 
			AttributeCategoryId category, boolean includeInResult) 
		throws XacmlSyntaxException
	{
		Collection<Attribute> attributes = new LinkedList<Attribute>();
		for(AttributeType a : contextAttributes){
			attributes.add(createAttribute(a, category, includeInResult));
		}
		return attributes;
	}
	
	private Attribute createAttribute(AttributeType a, AttributeCategoryId category, 
				boolean incudeInResultResourceId) 
		throws XacmlSyntaxException
	{
		Collection<AttributeValue> values = new LinkedList<AttributeValue>();
		for(AttributeValueType v : a.getAttributeValue()){
			AttributeValue value = createValue(a.getDataType(), v, category);
			if(log.isDebugEnabled()){
				log.debug("Found attribute value=\"{}\" in request", value);
			}
			values.add(value);
		}
		return new Attribute(a.getAttributeId(), a.getIssuer(), 
				((a.getAttributeId().equals(RESOURCE_ID))?incudeInResultResourceId:false), values);
	}
	
	private AttributeValue createValue(String dataTypeId, 
			AttributeValueType value, 
			AttributeCategoryId categoryId) 
		throws XacmlSyntaxException
	{
		List<Object> content = value.getContent();
		if(content == null || 
				content.isEmpty()){
			throw new RequestSyntaxException("Attribute does not have content");
		}
		com.artagon.xacml.v3.AttributeValueType dataType = XacmlDataTypes.getType(dataTypeId);
		if(dataType == null){
			throw new RequestSyntaxException(
					"DataTypeId=\"%s\" can be be " +
					"resolved to valid XACML type", dataTypeId);
		}
		Object o = Iterables.getOnlyElement(content);
		if(log.isDebugEnabled()){
			log.debug("Creating typeId=\"{}\" value=\"{}\"", dataType, o);
		}
		if(dataType.equals(XPathExpressionType.XPATHEXPRESSION)){
			String xpath = Xacml20XPathTo30Transformer.transform20PathTo30((String)o);
			return dataType.create(xpath, categoryId);
		}
		return dataType.create(o);
	}
}
