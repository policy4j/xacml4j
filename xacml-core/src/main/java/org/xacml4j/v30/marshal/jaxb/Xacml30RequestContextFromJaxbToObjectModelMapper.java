package org.xacml4j.v30.marshal.jaxb;

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
import org.xacml4j.v30.*;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

class Xacml30RequestContextFromJaxbToObjectModelMapper
{
	private ObjectFactory factory;

	public Xacml30RequestContextFromJaxbToObjectModelMapper(){
		this.factory = new ObjectFactory();
	}

	public RequestContext create(RequestType req) throws XacmlSyntaxException
	{
		RequestContext.Builder b = RequestContext.builder();
		b.categories(create(req.getAttributes()));
		if(req.getMultiRequests() != null){
			for(RequestReferenceType m : req.getMultiRequests().getRequestReference()){
				b.reference(create(m));
			}
		}
		return b.returnPolicyIdList(req.isReturnPolicyIdList())
				.combineDecision(req.isCombinedDecision())
				.build();
	}
	
	public RequestType create(RequestContext req) throws XacmlSyntaxException
	{
		RequestType jaxbReq = new RequestType();
		jaxbReq.setCombinedDecision(req.isCombinedDecision());
		jaxbReq.setRequestDefaults(create(req.getRequestDefaults()));
		jaxbReq.setReturnPolicyIdList(req.isReturnPolicyIdList());
		for(Category c : req.getAttributes()){
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

	public ResponseType create(ResponseContext res) throws XacmlSyntaxException
	{
		ResponseType response = new ResponseType();
		for(Result r : res.getResults()){
			response.getResult().add(create(r));
		}
		return response;
	}

	public ResponseContext create(ResponseType response) throws XacmlSyntaxException {
		Preconditions.checkNotNull(response);
		ResponseContext.Builder b = ResponseContext.builder();
		for(ResultType result : response.getResult()){
			b.result(create(result));
		}
		return b.build();
	}

	private Result create(ResultType result) throws XacmlSyntaxException {
		return Result
				.builder(create(result.getDecision()), create(result.getStatus()))
				.advices(createAdvices(result.getAssociatedAdvice()))
				.obligation(createObligations(result.getObligations()))
				.evaluatedPolicies(create(result.getPolicyIdentifierList()))
				.includeInResultAttributes(create(result.getAttributes()))
				.build();
	}

	private Collection<IdReference> create(
			PolicyIdentifierListType policyIdentifierList) throws XacmlSyntaxException {
		if (policyIdentifierList == null) {
			return ImmutableList.of();
		}
 		Collection<IdReference> list = new LinkedList<IdReference>();
		for(JAXBElement<IdReferenceType> o: policyIdentifierList.getPolicyIdReferenceOrPolicySetIdReference()) {
			if (o.getName().getLocalPart().equals("PolicyIdReference")) {
				list.add(IdReference
                        .policyIdRef(o.getValue().getValue())
                        .version(o.getValue().getVersion())
                        .build());
			} else if(o.getName().getLocalPart().equals("PolicySetIdReference")) {
                list.add(IdReference
                        .policySetIdRef(o.getValue().getValue())
                        .version(o.getValue().getVersion())
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
		for(IdReference id : r.getPolicyIdentifiers()){
			if(id instanceof IdReference.PolicyIdRef){
				ids.getPolicyIdReferenceOrPolicySetIdReference().add(
						factory.createPolicyIdReference(create(id)));
			}
			if(id instanceof IdReference.PolicySetIdRef){
				ids.getPolicyIdReferenceOrPolicySetIdReference().add(
						factory.createPolicySetIdReference(create(id)));
			}
		}
		result.setPolicyIdentifierList(ids);
		result.setStatus(create(r.getStatus()));
		result.setDecision(create(r.getDecision()));
		return result;
	}

	private IdReferenceType create(IdReference ref)
	{
		IdReferenceType idRef = new IdReferenceType();
		idRef.setValue(ref.getId());
		idRef.setVersion(ref.getVersion().getValue());
		return idRef;
	}

	private AttributesType create(Category a)
	{
		AttributesType attributes = new AttributesType();
		attributes.setId(a.getId());
		attributes.setCategory(a.getCategoryId().toString());
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
		for(AttributeExp v : a.getValues()){
			Optional<TypeToXacml30> m = TypeToXacml30.Types.getIndex().get(v.getType());
			if(!m.isPresent()){
				throw new XacmlSyntaxException("Unsupported XACML data type=\"%s\"",  
						v.getType());
			}
			attr.getAttributeValue().add(m.get().toXacml30(v));
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
		return Advice.builder(a.getAdviceId(), null)
				.attributes(create(a.getAttributeAssignment()))
				.build();
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
		return StatusCode.builder(StatusCodeIds.parse(statusCode.getValue()))
				.minorStatus(create(statusCode.getStatusCode()))
				.build();
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
		return new StatusDetail(statusDetail.getAny());
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
		if(a.getCategory() != null){
			attr.setCategory(a.getCategory().toString());
		}
		AttributeValueType av = toJaxb(a.getAttribute());
		attr.setDataType(av.getDataType());
		attr.getContent().addAll(av.getContent());
		return attr;
	}

	private AttributeAssignment create(AttributeAssignmentType a) throws XacmlSyntaxException
	{
		AttributeExp value = create((AttributeValueType)a);
		return AttributeAssignment
				.builder()
				.id(a.getAttributeId())
				.category(a.getCategory())
				.issuer(a.getIssuer())
				.value(value)
				.build();
	}

	private Collection<Category> create(List<AttributesType> input) throws XacmlSyntaxException {
		Collection<Category> attributes = new LinkedList<Category>();
		for(AttributesType a : input){
			attributes.add(create(a));
		}
		return attributes;
	}

	private Category create(AttributesType attributes) throws XacmlSyntaxException
	{
		Collection<Attribute> attr = new LinkedList<Attribute>();
		for(AttributeType a : attributes.getAttribute()){
			attr.add(create(a));
		}
		return Category
				.builder(Categories.parse(attributes.getCategory()))
				.entity(Entity
						.builder()
						.content(getContent(attributes.getContent()))
						.attributes(attr).build())
				.build();
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
		RequestReference.Builder b = RequestReference.builder();
		for(AttributesReferenceType r : m.getAttributesReference()){
			b.reference((String)r.getReferenceId());
		}
		return b.build();
	}

	private Attribute create(AttributeType a) throws XacmlSyntaxException
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

	private AttributeValueType toJaxb(AttributeExp a)
	{
		Preconditions.checkNotNull(a);
		Optional<TypeToXacml30> toXacml30 = TypeToXacml30.Types.getIndex().get(a.getType());
		Preconditions.checkArgument(toXacml30.isPresent());
		return toXacml30.get().toXacml30(a);
	}
	
	private AttributeExp create(
			AttributeValueType value)
		throws XacmlSyntaxException
	{
		
		Optional<TypeToXacml30> toXacml30 = TypeToXacml30.Types.getIndex().get(value.getDataType());
		Preconditions.checkArgument(toXacml30.isPresent());
		return toXacml30.get().fromXacml30(value);
	}
}
