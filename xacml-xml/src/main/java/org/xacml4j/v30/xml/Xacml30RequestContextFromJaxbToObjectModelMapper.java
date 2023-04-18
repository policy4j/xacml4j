package org.xacml4j.v30.xml;

/*
 * #%L
 * Xacml4J Core Engine Implementation
 * %%
 * Copyright (C) 2009 - 2014 Xacml4J.org
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

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
import org.oasis.xacml.v30.jaxb.MultiRequestsType;
import org.oasis.xacml.v30.jaxb.ObjectFactory;
import org.oasis.xacml.v30.jaxb.ObligationType;
import org.oasis.xacml.v30.jaxb.ObligationsType;
import org.oasis.xacml.v30.jaxb.PolicyIdentifierListType;
import org.oasis.xacml.v30.jaxb.RequestDefaultsType;
import org.oasis.xacml.v30.jaxb.RequestReferenceType;
import org.oasis.xacml.v30.jaxb.RequestType;
import org.oasis.xacml.v30.jaxb.ResponseType;
import org.oasis.xacml.v30.jaxb.ResultType;
import org.oasis.xacml.v30.jaxb.StatusCodeType;
import org.oasis.xacml.v30.jaxb.StatusDetailType;
import org.oasis.xacml.v30.jaxb.StatusType;
import org.w3c.dom.Node;
import org.xacml4j.v30.Advice;
import org.xacml4j.v30.Attribute;
import org.xacml4j.v30.AttributeAssignment;
import org.xacml4j.v30.Category;
import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.CategoryReference;
import org.xacml4j.v30.CompositeDecisionRuleIDReference;
import org.xacml4j.v30.Content;
import org.xacml4j.v30.Decision;
import org.xacml4j.v30.Entity;
import org.xacml4j.v30.Obligation;
import org.xacml4j.v30.ResponseContext;
import org.xacml4j.v30.Result;
import org.xacml4j.v30.Status;
import org.xacml4j.v30.StatusCode;
import org.xacml4j.v30.StatusCodeId;
import org.xacml4j.v30.StatusDetail;
import org.xacml4j.v30.SyntaxException;
import org.xacml4j.v30.Value;
import org.xacml4j.v30.policy.PolicyIDReference;
import org.xacml4j.v30.policy.PolicySetIDReference;
import org.xacml4j.v30.RequestContext;
import org.xacml4j.v30.RequestDefaults;
import org.xacml4j.v30.RequestReference;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

public class Xacml30RequestContextFromJaxbToObjectModelMapper
{
	private ObjectFactory factory;

	public Xacml30RequestContextFromJaxbToObjectModelMapper(){
		this.factory = new ObjectFactory();
	}

	public RequestContext create(RequestType req) throws SyntaxException
	{
		RequestContext.Builder b = RequestContext.builder();
		b.attributes(create(req.getAttributes()));
		if(req.getMultiRequests() != null){
			for(RequestReferenceType m : req.getMultiRequests().getRequestReference()){
				b.reference(create(m));
			}
		}
		return b.returnPolicyIdList(req.isReturnPolicyIdList())
				.combineDecision(req.isCombinedDecision())
				.build();
	}

	/**
	 * Creates XACML 3.0 {@link RequestType} from given {@link RequestContext}
	 *
	 * @param req
	 * @return
	 * @throws SyntaxException
	 */
	public RequestType create(RequestContext req)
	{
		RequestType jaxbReq = new RequestType();
		jaxbReq.setCombinedDecision(req.isCombinedDecision());
		jaxbReq.setRequestDefaults(create(req.getRequestDefaults()));
		jaxbReq.setReturnPolicyIdList(req.isReturnPolicyIdList());
		for(Category c : req.getCategories()){
			jaxbReq.getAttributes().add(create(c));
		}
		MultiRequestsType multiReq = new MultiRequestsType();
		for(RequestReference ref : req.getRequestReferences()){
			multiReq.getRequestReference().add(create(ref));
		}
		jaxbReq.setMultiRequests(multiReq);
		return jaxbReq;
	}
	
	private RequestDefaultsType create(RequestDefaults defaults){
		RequestDefaultsType def = new RequestDefaultsType();
		if(defaults == null){
			return null;
		}
		def.setXPathVersion(defaults.getXPathVersion().toString());
		return def;
	}
	
	private RequestReferenceType create(RequestReference reqRef){
		RequestReferenceType jaxbReqRef = new RequestReferenceType();
		for(CategoryReference ref : reqRef.getReferencedCategories()){
			jaxbReqRef.getAttributesReference().add(create(ref));
		}
		return jaxbReqRef;
	}
	
	private AttributesReferenceType create(CategoryReference ref){
		AttributesReferenceType jaxbRef = new AttributesReferenceType();
		jaxbRef.setReferenceId(ref.getReferenceId());
		return jaxbRef;
	}

	public ResponseType create(ResponseContext res) throws SyntaxException
	{
		ResponseType response = new ResponseType();
		for(Result r : res.getResults()){
			response.getResult().add(create(r));
		}
		return response;
	}

	public ResponseContext create(ResponseType response) throws SyntaxException {
		Preconditions.checkNotNull(response);
		ResponseContext.Builder b = ResponseContext.builder();
		for(ResultType result : response.getResult()){
			b.result(create(result));
		}
		return b.build();
	}

	private Result create(ResultType result) throws SyntaxException {
		return Result
				.builder(create(result.getDecision()), create(result.getStatus()))
				.advice(createAdvices(result.getAssociatedAdvice()))
				.obligation(createObligations(result.getObligations()))
				.evaluatedPolicies(create(result.getPolicyIdentifierList()))
				.includeInResultAttr(create(result.getAttributes()))
				.build();
	}

	private Collection<CompositeDecisionRuleIDReference> create(
			PolicyIdentifierListType policyIdentifierList) throws SyntaxException {
		if (policyIdentifierList == null) {
			return ImmutableList.of();
		}
 		Collection<CompositeDecisionRuleIDReference> list = new LinkedList<CompositeDecisionRuleIDReference>();
		for(JAXBElement<IdReferenceType> o: policyIdentifierList.getPolicyIdReferenceOrPolicySetIdReference()) {
			if (o.getName().getLocalPart().equals("PolicyIdReference")) {
				list.add(PolicyIDReference
						.builder(o.getValue().getValue())
						.versionAsString(o.getValue().getVersion())
						.earliest(o.getValue().getEarliestVersion())
						.latest(o.getValue().getLatestVersion())
						.build());
			} else if(o.getName().getLocalPart().equals("PolicySetIdReference")) {
				list.add(PolicySetIDReference
						.builder(o.getValue().getValue())
						.versionAsString(o.getValue().getVersion())
						.earliest(o.getValue().getEarliestVersion())
						.latest(o.getValue().getLatestVersion())
						.build());
			}
		}
		return list;
	}

	private ResultType create(Result r)
	{
		ResultType result = new ResultType();
		for(Category a : r.getIncludeInResultAttributes()){
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
				ids.getPolicyIdReferenceOrPolicySetIdReference()
				   .add(factory.createPolicyIdReference(create(id)));
			}
			if(id instanceof PolicySetIDReference){
				ids.getPolicyIdReferenceOrPolicySetIdReference().add(
						factory.createPolicySetIdReference(create(id)));
			}
		}
		result.setPolicyIdentifierList(ids);
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

	private AttributesType create(Category a)
	{
		AttributesType attributes = new AttributesType();
		attributes.setId(a.getReferenceId().orElse(null));
		attributes.setCategory(a.getCategoryId().getId());
		for(Attribute attr : a.getEntity().getAttributes()){
			attributes.getAttribute().add(create(attr));
		}
		return attributes;
	}

	private AttributeType create(Attribute a){
		AttributeType attr = new AttributeType();
		attr.setAttributeId(a.getAttributeId());
		attr.setIssuer(a.getIssuer());
		attr.setIncludeInResult(a.isIncludeInResult());
		for(Value v : a.getValues()){
			TypeToXacml30 m = TypeToXacml30
					.forType(v.getEvaluatesTo())
					.orElseThrow(
							()-> SyntaxException.invalidDataTypeId(v.getEvaluatesTo()));
			attr.getAttributeValue().add(m.toXacml30(v));
		}
		return attr;
	}

	private DecisionType create(Decision d){
		if(d == Decision.PERMIT){
			return DecisionType.PERMIT;
		}
		if(d == Decision.DENY){
			return DecisionType.DENY;
		}
		if(d.isIndeterminate()){
			return DecisionType.INDETERMINATE;
		}
		return DecisionType.NOT_APPLICABLE;
	}

	private Decision create(DecisionType d) {
		if(d == DecisionType.PERMIT){
			return Decision.PERMIT;
		}
		if(d == DecisionType.DENY){
			return Decision.DENY;
		}
		if(d == DecisionType.INDETERMINATE){
			return Decision.INDETERMINATE;
		}
		return Decision.NOT_APPLICABLE;
	}

	private Collection<Advice> createAdvices(AssociatedAdviceType associatedAdviceType) throws SyntaxException {
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

	private Advice create(AdviceType a) throws SyntaxException {
		return Advice.builder(a.getAdviceId(), null)
				.attributes(create(a.getAttributeAssignment()))
				.build();
	}

	private Collection<Obligation> createObligations(ObligationsType obligationsType) throws SyntaxException {
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

	private Obligation create(ObligationType o) throws SyntaxException {

		return Obligation
				.builder(o.getObligationId())
				.attributes(create(o.getAttributeAssignment()))
				.build();
	}

	private Status create(StatusType status) {
		return Status
				.builder(create(status.getStatusCode()))
				.message(status.getStatusMessage())
				.detail(create(status.getStatusDetail()))
				.build();
	}

	private StatusCode create(StatusCodeType statusCode) {
		if (statusCode == null) {
			return null;
		}
		StatusCode.Builder b =  StatusCode
				.builder(StatusCodeId.of(statusCode.getValue())
				                     .orElseThrow(()->SyntaxException.invalidStatusCode(statusCode.getValue())));
		if(statusCode.getStatusCode() != null){
			b.minorStatus(StatusCode.builder(StatusCodeId.of(statusCode.getStatusCode().getValue())
			                          .orElseThrow(()->SyntaxException.invalidStatusCode(statusCode.getValue()))).build());
		}
		return b.build();
	}

	private StatusType create(Status status)
	{
		StatusType s = new StatusType();
		s.setStatusCode(create(status.getStatusCode()));
		s.setStatusMessage(status.getMessage().orElse(null));
		s.setStatusDetail(create(status.getDetail().orElse(null)));
		return s;
	}

	private StatusCodeType create(StatusCode c)
	{
		if(c == null){
			return null;
		}
		StatusCodeType code = new StatusCodeType();
		code.setValue(c.getValue().getId());
		code.setStatusCode(create(c.getMinorStatus()));
		return code;
	}

	private StatusDetailType create(StatusDetail d)
	{
		if(d == null){
			return null;
		}
		StatusDetailType detailType = new StatusDetailType();
		detailType.getAny().addAll(d.getDetails());
		return detailType;
	}

	private StatusDetail create(StatusDetailType statusDetail) {
		if (statusDetail == null) {
			return null;
		}
		return new StatusDetail(statusDetail.getAny());
	}

	private Collection<AttributeAssignment> create(
			Collection<AttributeAssignmentType> attributeAssignment) throws SyntaxException {
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
		a.getCategory().ifPresent(c->attr.setCategory(c.getId()));
		AttributeValueType av = toJaxb(a.getAttribute());
		attr.setDataType(av.getDataType());
		attr.getContent().addAll(av.getContent());
		return attr;
	}

	private AttributeAssignment create(AttributeAssignmentType a) throws SyntaxException
	{
		Value value = create((AttributeValueType)a);
		return AttributeAssignment
				.builder()
				.attributeId(a.getAttributeId())
				.category(a.getCategory())
				.issuer(a.getIssuer())
				.value(value)
				.build();
	}

	private Collection<Category> create(List<AttributesType> input) throws SyntaxException {
		Collection<Category> attributes = new LinkedList<Category>();
		for(AttributesType a : input){
			attributes.add(create(a));
		}
		return attributes;
	}

	private Category create(AttributesType attributes) throws SyntaxException
	{
		Collection<Attribute> attr = new LinkedList<Attribute>();
		for(AttributeType a : attributes.getAttribute()){
			attr.add(buildAttribute(a));
		}
		return CategoryId.parse(attributes.getCategory())
		.map(c->Category
				.builder(c)
				.entity(Entity
						.builder()
						.content(getContent(attributes.getContent()))
						.attributes(attr).build())
				.build()).orElseThrow(
						()-> SyntaxException.invalidCategoryId(attributes.getCategory()));
	}

	private java.util.Optional<Content> getContent(ContentType content)
	{
		if(content == null){
			return java.util.Optional.empty();
		}
		List<Object> o = content.getContent();
		if(o.isEmpty()){
			return java.util.Optional.empty();
		}
		Object v = content.getContent().get(0);
		if(v instanceof Node){
			return Content.fromNode(v, Content.Type.XML_UTF8);
		}
		return Content.fromString((String)v);
	}

	private RequestReference create(RequestReferenceType m)
	{
		RequestReference.Builder b = RequestReference.builder();
		for(AttributesReferenceType r : m.getAttributesReference()){
			b.reference((String)r.getReferenceId());
		}
		return b.build();
	}

	private Attribute buildAttribute(AttributeType a)
	{
		Preconditions.checkNotNull(a);
		Attribute.Builder b =  Attribute
				.builder(a.getAttributeId())
				.issuer(a.getIssuer())
				.includeInResult(a.isIncludeInResult());
		for(AttributeValueType v : a.getAttributeValue()){
			b.value(create(v));
		}
		return b.build();
	}

	private AttributeValueType toJaxb(Value a)
	{
		Preconditions.checkNotNull(a);
		return TypeToXacml30.forType(a.getEvaluatesTo()).map(v->v.toXacml30(a))
		                    .orElseThrow(()->SyntaxException.invalidAttributeValue(a.getEvaluatesTo()));
	}
	
	private Value create(
			AttributeValueType value)
		throws SyntaxException
	{
		TypeToXacml30 toXacml30 = TypeToXacml30.forType(value.getDataType())
		                                        .orElseThrow(
		                                        		()->SyntaxException
						                                        .invalidDataTypeId(value.getDataType()));
		return toXacml30.fromXacml30(value);
	}
}
