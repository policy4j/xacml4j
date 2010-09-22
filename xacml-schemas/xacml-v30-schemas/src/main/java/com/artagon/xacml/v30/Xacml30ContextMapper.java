package com.artagon.xacml.v30;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.oasis.xacml.v30.jaxb.AdviceType;
import org.oasis.xacml.v30.jaxb.AssociatedAdviceType;
import org.oasis.xacml.v30.jaxb.AttributeAssignmentType;
import org.oasis.xacml.v30.jaxb.AttributeType;
import org.oasis.xacml.v30.jaxb.AttributeValueType;
import org.oasis.xacml.v30.jaxb.AttributesReferenceType;
import org.oasis.xacml.v30.jaxb.AttributesType;
import org.oasis.xacml.v30.jaxb.ContentType;
import org.oasis.xacml.v30.jaxb.DecisionType;
import org.oasis.xacml.v30.jaxb.EffectType;
import org.oasis.xacml.v30.jaxb.IdReferenceType;
import org.oasis.xacml.v30.jaxb.ObjectFactory;
import org.oasis.xacml.v30.jaxb.ObligationType;
import org.oasis.xacml.v30.jaxb.ObligationsType;
import org.oasis.xacml.v30.jaxb.PolicyIdentifierListType;
import org.oasis.xacml.v30.jaxb.RequestReferenceType;
import org.oasis.xacml.v30.jaxb.RequestType;
import org.oasis.xacml.v30.jaxb.ResponseType;
import org.oasis.xacml.v30.jaxb.ResultType;
import org.oasis.xacml.v30.jaxb.StatusCodeType;
import org.oasis.xacml.v30.jaxb.StatusDetailType;
import org.oasis.xacml.v30.jaxb.StatusType;
import org.w3c.dom.Node;

import com.artagon.xacml.util.DOMUtil;
import com.artagon.xacml.v3.Advice;
import com.artagon.xacml.v3.Attribute;
import com.artagon.xacml.v3.AttributeAssignment;
import com.artagon.xacml.v3.AttributeCategoryId;
import com.artagon.xacml.v3.AttributeValue;
import com.artagon.xacml.v3.Attributes;
import com.artagon.xacml.v3.AttributesReference;
import com.artagon.xacml.v3.CompositeDecisionRuleIDReference;
import com.artagon.xacml.v3.Decision;
import com.artagon.xacml.v3.Effect;
import com.artagon.xacml.v3.Obligation;
import com.artagon.xacml.v3.PolicyIDReference;
import com.artagon.xacml.v3.PolicySetIDReference;
import com.artagon.xacml.v3.RequestContext;
import com.artagon.xacml.v3.RequestReference;
import com.artagon.xacml.v3.ResponseContext;
import com.artagon.xacml.v3.Result;
import com.artagon.xacml.v3.Status;
import com.artagon.xacml.v3.StatusCode;
import com.artagon.xacml.v3.StatusDetail;
import com.artagon.xacml.v3.XacmlSyntaxException;
import com.artagon.xacml.v3.marshall.PolicyUnmarshallerSupport;
import com.artagon.xacml.v3.types.XacmlDataTypes;
import com.google.common.base.Preconditions;

public class Xacml30ContextMapper extends PolicyUnmarshallerSupport
{
	private ObjectFactory factory;
	
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
		
	public Xacml30ContextMapper() throws Exception{
		this.factory = new ObjectFactory();
	}
	
	public RequestContext create(RequestType req) throws XacmlSyntaxException
	{
		Collection<Attributes> attributes = new LinkedList<Attributes>();
		for(AttributesType a : req.getAttributes()){
			attributes.add(create(a));
		}
		Collection<RequestReference> multiRequests = new LinkedList<RequestReference>();
		if(req.getMultiRequests() != null){
			for(RequestReferenceType m : req.getMultiRequests().getRequestReference()){
				multiRequests.add(create(m));
			}
		}
		return new RequestContext(req.isReturnPolicyIdList(), attributes, multiRequests, null);
	}
	
	public ResponseType create(ResponseContext res) throws XacmlSyntaxException
	{
		ResponseType response = new ResponseType();
		for(Result r : res.getResults()){
			response.getResult().add(create(r));
		}
		return response;
	}
	
	private ResultType create(Result r)
	{
		ResultType result = new ResultType();
		for(Attributes a : r.getAttributes()){
			result.getAttributes().add(create(a));
		}
		AssociatedAdviceType advice = new AssociatedAdviceType();
		for(Advice a : r.getAssociatedAdvice()){
			advice.getAdvice().add(create(a));
		}
		ObligationsType obligations = new ObligationsType();
		for(Obligation o : r.getObligations()){
			obligations.getObligation().add(create(o));
		}
		result.setAssociatedAdvice(advice);
		result.setObligations(obligations);
		PolicyIdentifierListType ids = new PolicyIdentifierListType();
		for(CompositeDecisionRuleIDReference id : r.getPolicyIdentifiers()){
			if(id instanceof PolicyIDReference){
				ids.getPolicyIdReferenceOrPolicySetIdReference().add(
						factory.createPolicyIdReference(create(id)));
			}
			if(id instanceof PolicySetIDReference){
				ids.getPolicyIdReferenceOrPolicySetIdReference().add(
						factory.createPolicySetIdReference(create(id)));
			}
		}
		result.setStatus(create(r.getStatus()));
		result.setDecision(create(r.getDecision()));
		return result;
	}
	
	private IdReferenceType create(CompositeDecisionRuleIDReference ref)
	{
		IdReferenceType idRef = new IdReferenceType();
		idRef.setValue(ref.getId());
		idRef.setVersion(ref.getVersionMatch().toString());
		return idRef;
	}
	
	private AttributesType create(Attributes a)
	{
		AttributesType attributes = new AttributesType();
		attributes.setId(a.getId());
		attributes.setCategory(a.getCategory().toString());
		for(Attribute attr : a.getAttributes()){
			attributes.getAttribute().add(create(attr));
		}
		return attributes;
	}
	
	private AttributeType create(Attribute a){
		return null;
	}
	
	private DecisionType create(Decision d){
		DecisionType jaxbD = v30ToV20DecisionMapping.get(d);
		Preconditions.checkState(jaxbD != null);
		return jaxbD;
	}
	
	private AdviceType create(Advice a)
	{
		AdviceType advice = new AdviceType();
		advice.setAdviceId(a.getId());
		for(AttributeAssignment attr : a.getAttributes()){
			advice.getAttributeAssignment().add(create(attr));
		}
		return advice;
	}
	
	private ObligationType create(Obligation a){
		ObligationType obligation = new ObligationType();
		obligation.setObligationId(a.getId());
		for(AttributeAssignment attr : a.getAttributes()){
			obligation.getAttributeAssignment().add(create(attr));
		}
		return obligation;
	}
	
	private StatusType create(Status status)
	{
		StatusType s = new StatusType();
		s.setStatusCode(create(status.getStatusCode()));
		s.setStatusMessage(status.getMessage());
		s.setStatusDetail(create(status.getDetail()));
		return s;
	}
	
	private StatusCodeType create(StatusCode c)
	{
		if(c == null){
			return null;
		}
		StatusCodeType code = new StatusCodeType();
		code.setValue(c.getValue().toString());
		code.setStatusCode(create(c.getMinorStatus()));
		return code;
	}
	
	private StatusDetailType create(StatusDetail d)
	{
		return null;
	}
	
	private AttributeAssignmentType create(AttributeAssignment a)
	{
		AttributeAssignmentType attr = new AttributeAssignmentType();
		attr.setAttributeId(a.getAttributeId());
		attr.setIssuer(a.getIssuer());
		attr.setCategory(a.getCategory().toString());
		AttributeValue v = a.getAttribute();
		attr.setDataType(v.getDataType().getDataTypeId());
		attr.getContent().add(v.toXacmlString());
		return attr;
	}
	
	private Attributes create(AttributesType attributes) throws XacmlSyntaxException
	{
		Collection<Attribute> attr = new LinkedList<Attribute>();
		for(AttributeType a : attributes.getAttribute()){
			attr.add(create(a));
		}
		return new Attributes(AttributeCategoryId.parse(attributes.getCategory()), 
				getContent(attributes.getContent()), attr);
	}
	
	private Node getContent(ContentType content) throws XacmlSyntaxException
	{
		if(content == null){
			return null;
		}
		List<Object> o = content.getContent();
		if(o.isEmpty()){
			return null;
		}
		return DOMUtil.copyNode((Node)o.iterator().next());
	}
	
	private RequestReference create(RequestReferenceType m) throws XacmlSyntaxException
	{
		Collection<AttributesReference> references = new LinkedList<AttributesReference>();
		for(AttributesReferenceType r : m.getAttributesReference()){
			references.add(new AttributesReference(r.getReferenceId().toString()));
		}
		return new RequestReference(references);
	}
	
	private Attribute create(AttributeType a) throws XacmlSyntaxException
	{
		Collection<AttributeValue> values = new LinkedList<AttributeValue>();
		for(AttributeValueType v : a.getAttributeValue()){
			values.add(create(v));
		}
		return new Attribute(a.getAttributeId(), 
				a.getIssuer(), a.isIncludeInResult(), values);
	}
		
	private AttributeValue create(
			AttributeValueType value) 
		throws XacmlSyntaxException
	{
		List<Object> content = value.getContent();
		if(content == null || 
				content.isEmpty()){
			throw new XacmlSyntaxException(
					"Attribute does not have content");
		}
		
		return XacmlDataTypes.createAttributeValue(value.getDataType(), 
				content.iterator().next(), value.getOtherAttributes());
	}
}
