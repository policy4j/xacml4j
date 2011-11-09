package com.artagon.xacml.v30.marshall.jaxb;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBElement;

import org.oasis.xacml.v30.jaxb.AdviceType;
import org.oasis.xacml.v30.jaxb.AssociatedAdviceType;
import org.oasis.xacml.v30.jaxb.AttributeAssignmentType;
import org.oasis.xacml.v30.jaxb.AttributeType;
import org.oasis.xacml.v30.jaxb.AttributeValueType;
import org.oasis.xacml.v30.jaxb.AttributesReferenceType;
import org.oasis.xacml.v30.jaxb.AttributesType;
import org.oasis.xacml.v30.jaxb.ContentType;
import org.oasis.xacml.v30.jaxb.DecisionType;
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

import com.artagon.xacml.v30.AttributeCategories;
import com.artagon.xacml.v30.pdp.Advice;
import com.artagon.xacml.v30.pdp.Attribute;
import com.artagon.xacml.v30.pdp.AttributeAssignment;
import com.artagon.xacml.v30.pdp.AttributeExp;
import com.artagon.xacml.v30.pdp.Attributes;
import com.artagon.xacml.v30.pdp.AttributesReference;
import com.artagon.xacml.v30.pdp.CompositeDecisionRuleIDReference;
import com.artagon.xacml.v30.pdp.Decision;
import com.artagon.xacml.v30.pdp.Obligation;
import com.artagon.xacml.v30.pdp.PolicyIDReference;
import com.artagon.xacml.v30.pdp.PolicySetIDReference;
import com.artagon.xacml.v30.pdp.RequestContext;
import com.artagon.xacml.v30.pdp.RequestReference;
import com.artagon.xacml.v30.pdp.ResponseContext;
import com.artagon.xacml.v30.pdp.Result;
import com.artagon.xacml.v30.pdp.Status;
import com.artagon.xacml.v30.pdp.StatusCode;
import com.artagon.xacml.v30.pdp.StatusCodeId;
import com.artagon.xacml.v30.pdp.StatusDetail;
import com.artagon.xacml.v30.pdp.XacmlSyntaxException;
import com.artagon.xacml.v30.types.DataTypes;
import com.google.common.base.Preconditions;

public class Xacml30RequestContextFromJaxbToObjectModelMapper
{
	private ObjectFactory factory;
	
	private final static Map<Decision, DecisionType> v30ToV20DecisionMapping = new HashMap<Decision, DecisionType>();
	private final static Map<DecisionType, Decision> v20ToV30DecisionMapping = new HashMap<DecisionType, Decision>();
	
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
	}
		
	public Xacml30RequestContextFromJaxbToObjectModelMapper(){
		this.factory = new ObjectFactory();
	}
	
	public RequestContext create(RequestType req) throws XacmlSyntaxException
	{
		Collection<Attributes> attributes = create(req.getAttributes());
		Collection<RequestReference> multiRequests = new LinkedList<RequestReference>();
		if(req.getMultiRequests() != null){
			for(RequestReferenceType m : req.getMultiRequests().getRequestReference()){
				multiRequests.add(create(m));
			}
		}
		return new RequestContext(req.isReturnPolicyIdList(), 
				req.isCombinedDecision(), attributes, multiRequests, null);
	}
	
	public ResponseType create(ResponseContext res) throws XacmlSyntaxException
	{
		ResponseType response = new ResponseType();
		for(Result r : res.getResults()){
			response.getResult().add(create(r));
		}
		return response;
	}

	public ResponseContext create(ResponseType response) throws XacmlSyntaxException {
		Collection<Result> results = new LinkedList<Result>();
		for(ResultType result : response.getResult()){
			results.add(create(result));
		}

		ResponseContext responseCtx = new ResponseContext(results);
		return responseCtx;
	}

	private Result create(ResultType result) throws XacmlSyntaxException {
		Decision decision = create(result.getDecision());
		return new Result(
				decision,
				create(result.getStatus()),
				createAdvices(result.getAssociatedAdvice()),
				createObligations(result.getObligations()),
				create(result.getAttributes()),
				Collections.<Attributes>emptyList(),
				create(result.getPolicyIdentifierList()));
	}

	private Collection<CompositeDecisionRuleIDReference> create(
			PolicyIdentifierListType policyIdentifierList) throws XacmlSyntaxException {
		if (policyIdentifierList == null) {
			return Collections.emptyList();
		}
 		Collection<CompositeDecisionRuleIDReference> list = new LinkedList<CompositeDecisionRuleIDReference>();
		for(JAXBElement<IdReferenceType> o: policyIdentifierList.getPolicyIdReferenceOrPolicySetIdReference()) {
			if (o.getName().getLocalPart().equals("PolicyIdReference")) {
				PolicyIDReference ref = PolicyIDReference.create(
						o.getValue().getValue(),
						o.getValue().getVersion(),
						o.getValue().getEarliestVersion(),
						o.getValue().getLatestVersion());
				list.add(ref);
			} else if(o.getName().getLocalPart().equals("PolicySetIdReference")) {
				PolicySetIDReference ref = PolicySetIDReference.create(
						o.getValue().getValue(),
						o.getValue().getVersion(),
						o.getValue().getEarliestVersion(),
						o.getValue().getLatestVersion());
				list.add(ref);
			}
		}
		return list;
	}

	private ResultType create(Result r)
	{
		ResultType result = new ResultType();
		for(Attributes a : r.getIncludeInResultAttributes()){
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
		idRef.setVersion(ref.getVersion().toString());
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

	private Decision create(DecisionType jaxbD) {
		Decision d = v20ToV30DecisionMapping.get(jaxbD);
		Preconditions.checkState(d != null);
		return d;
	}

	private Collection<Advice> createAdvices(AssociatedAdviceType associatedAdviceType) throws XacmlSyntaxException {
		if (associatedAdviceType == null) {
			return Collections.emptyList();
		}
		Collection<Advice> advices = new LinkedList<Advice>();
		for(AdviceType a: associatedAdviceType.getAdvice()) {
			advices.add(create(a));
		}
		return advices;
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

	private Advice create(AdviceType a) throws XacmlSyntaxException {
		return new Advice(a.getAdviceId(), null, create(a.getAttributeAssignment()));
	}

	private Collection<Obligation> createObligations(ObligationsType obligationsType) throws XacmlSyntaxException {
		if (obligationsType == null) {
			return Collections.emptyList();
		}

		Collection<Obligation> obligations = new LinkedList<Obligation>();
		for(ObligationType a: obligationsType.getObligation()) {
			obligations.add(create(a));
		}
		return obligations;
	}

	private ObligationType create(Obligation a){
		ObligationType obligation = new ObligationType();
		obligation.setObligationId(a.getId());
		for(AttributeAssignment attr : a.getAttributes()){
			obligation.getAttributeAssignment().add(create(attr));
		}
		return obligation;
	}

	private Obligation create(ObligationType o) throws XacmlSyntaxException {
		return new Obligation(o.getObligationId(), null, create(o.getAttributeAssignment()));
	}

	private Status create(StatusType status) {
		return new Status(create(status.getStatusCode()), status.getStatusMessage(), create(status.getStatusDetail()));
	}

	private StatusCode create(StatusCodeType statusCode) {
		if (statusCode == null) {
			return null;
		}
		StatusCode detailedCode = create(statusCode.getStatusCode());
		StatusCodeId codeId = StatusCodeId.parse(statusCode.getValue());
		Preconditions.checkArgument(codeId != null);
		return new StatusCode(codeId, detailedCode);
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

	private StatusDetail create(StatusDetailType statusDetail) {
		if (statusDetail == null) {
			return null;
		}
		StatusDetail d = new StatusDetail();
		for(Object o : statusDetail.getAny()) {
			d.addStatusDetail(o);
		}
		return d;
	}
	
	private Collection<AttributeAssignment> create(
			Collection<AttributeAssignmentType> attributeAssignment) throws XacmlSyntaxException {
		Collection<AttributeAssignment> attrs = new LinkedList<AttributeAssignment>();
		for(AttributeAssignmentType a: attributeAssignment) {
			attrs.add(create(a));
		}
		return attrs;
	}

	private AttributeAssignmentType create(AttributeAssignment a)
	{
		AttributeAssignmentType attr = new AttributeAssignmentType();
		attr.setAttributeId(a.getAttributeId());
		attr.setIssuer(a.getIssuer());
		attr.setCategory(a.getCategory().toString());
		AttributeExp v = a.getAttribute();
		attr.setDataType(v.getType().getDataTypeId());
		attr.getContent().add(v.toXacmlString());
		return attr;
	}

	private AttributeAssignment create(AttributeAssignmentType a) throws XacmlSyntaxException {
		AttributeExp value = create((AttributeValueType)a);
		
		return new AttributeAssignment(
				a.getAttributeId(),
				AttributeCategories.parse(a.getCategory()),
				a.getIssuer(),
				value);
	}
	
	private Collection<Attributes> create(List<AttributesType> input) throws XacmlSyntaxException {
		Collection<Attributes> attributes = new LinkedList<Attributes>();
		for(AttributesType a : input){
			attributes.add(create(a));
		}
		return attributes;
	}

	private Attributes create(AttributesType attributes) throws XacmlSyntaxException
	{
		Collection<Attribute> attr = new LinkedList<Attribute>();
		for(AttributeType a : attributes.getAttribute()){
			attr.add(create(a));
		}
		return new Attributes(AttributeCategories.parse(attributes.getCategory()), 
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
		return (Node)o.iterator().next();
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
		Collection<AttributeExp> values = new LinkedList<AttributeExp>();
		for(AttributeValueType v : a.getAttributeValue()){
			values.add(create(v));
		}
		return new Attribute(a.getAttributeId(), 
				a.getIssuer(), a.isIncludeInResult(), values);
	}
		
	private AttributeExp create(
			AttributeValueType value) 
		throws XacmlSyntaxException
	{
		List<Object> content = value.getContent();
		if(content == null || 
				content.isEmpty()){
			throw new XacmlSyntaxException(
					"Attribute does not have content");
		}
		return DataTypes.createAttributeValue(value.getDataType(), 
				content.iterator().next(), value.getOtherAttributes());
	}
}
