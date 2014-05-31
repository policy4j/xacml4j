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
import java.util.Map;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import org.oasis.xacml.v20.jaxb.context.DecisionType;
import org.oasis.xacml.v20.jaxb.context.ResponseType;
import org.oasis.xacml.v20.jaxb.context.ResultType;
import org.oasis.xacml.v20.jaxb.context.StatusCodeType;
import org.oasis.xacml.v20.jaxb.context.StatusType;
import org.oasis.xacml.v20.jaxb.policy.AttributeAssignmentType;
import org.oasis.xacml.v20.jaxb.policy.EffectType;
import org.oasis.xacml.v20.jaxb.policy.ObligationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xacml4j.v30.Attribute;
import org.xacml4j.v30.AttributeAssignment;
import org.xacml4j.v30.AttributeExp;
import org.xacml4j.v30.Categories;
import org.xacml4j.v30.Category;
import org.xacml4j.v30.Decision;
import org.xacml4j.v30.Effect;
import org.xacml4j.v30.Entity;
import org.xacml4j.v30.Obligation;
import org.xacml4j.v30.ResponseContext;
import org.xacml4j.v30.Result;
import org.xacml4j.v30.Status;
import org.xacml4j.v30.StatusCode;
import org.xacml4j.v30.StatusCodeIds;
import org.xacml4j.v30.StatusDetail;
import org.xacml4j.v30.XacmlSyntaxException;
import org.xacml4j.v30.marshal.ResponseUnmarshaller;
import org.xacml4j.v30.types.StringExp;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

public class Xacml20ResponseContextUnmarshaller
	extends BaseJAXBUnmarshaller<ResponseContext>
implements ResponseUnmarshaller
{
	private Mapper mapper;

	public Xacml20ResponseContextUnmarshaller(){
		super(JAXBContextUtil.getInstance());
		this.mapper = new Mapper();
	}

	@Override
	protected ResponseContext create(JAXBElement<?> jaxbInstance)
			throws XacmlSyntaxException {
		return mapper.create(
				(ResponseType)jaxbInstance.getValue());
	}

	static class Mapper
	{
		private final static Logger log = LoggerFactory.getLogger(Mapper.class);

		private final static String RESOURCE_ID = "urn:oasis:names:tc:xacml:1.0:resource:resource-id";

		private final static Map<DecisionType, Decision> V20_TO_V30_DECISION_MAPPING = ImmutableMap.of(
				DecisionType.DENY, Decision.DENY,
				DecisionType.PERMIT, Decision.PERMIT,
				DecisionType.NOT_APPLICABLE, Decision.NOT_APPLICABLE,
				DecisionType.INDETERMINATE, Decision.INDETERMINATE);

		private final static Map<EffectType, Effect> V20_TO_V30_EFFECT_MAPPING = ImmutableMap.of(
				EffectType.DENY, Effect.DENY,
				EffectType.PERMIT, Effect.PERMIT);

		public ResponseContext create(ResponseType response) throws XacmlSyntaxException
		{
			Preconditions.checkNotNull(response);
			ResponseContext.Builder b = ResponseContext.builder();
			for(ResultType result : response.getResult()){
				b.result(create(result));
			}
			return b.build();
		}

		private Result create(ResultType result) throws XacmlSyntaxException
		{
			Preconditions.checkArgument(result.getDecision() != null);
			Decision d = V20_TO_V30_DECISION_MAPPING.get(result.getDecision());
			Status status = create(result.getStatus());
			if(d == Decision.INDETERMINATE){
				return Result.indeterminate(status).build();
			}
			Result.Builder b = Result
					.builder(d, create(result.getStatus()))
					.obligation(getObligations(result));
			if(result.getResourceId() != null){
				b.includeInResult(Category
						.builder(Categories.RESOURCE)
						.entity(Entity
								.builder()
								.attribute(
								Attribute
								.builder(RESOURCE_ID)
								.value(StringExp.valueOf(result.getResourceId()))
								.build()).build())
						.build());
			}
			return b.build();
		}

		private Collection<Obligation> getObligations(ResultType r) throws XacmlSyntaxException
		{
			if(r.getObligations() == null){
				return Collections.emptyList();
			}
			Collection<Obligation> obligations = new LinkedList<Obligation>();
			for(ObligationType o  : r.getObligations().getObligation()){
				if(log.isDebugEnabled()){
					log.debug("Unmarshalling obligationId=\"{}\"", o.getObligationId());
				}
				obligations.add(create(o));
			}
			return obligations;
		}

		private Obligation create(ObligationType o) throws XacmlSyntaxException
		{
			Collection<AttributeAssignment> attrs = new LinkedList<AttributeAssignment>();
			for(AttributeAssignmentType a : o.getAttributeAssignment()){
				attrs.add(
						AttributeAssignment.builder()
						.id(a.getAttributeId())
						.value(createValue(a.getDataType(), a.getOtherAttributes(), a.getContent())).build());
			}
			return Obligation
					.builder(o.getObligationId(), V20_TO_V30_EFFECT_MAPPING.get(o.getFulfillOn()))
					.attributes(attrs)
					.build();
		}

		private Status create(StatusType status) throws XacmlSyntaxException
		{
			StatusDetail detail = null;
			if(status.getStatusDetail() != null &&
					!status.getStatusDetail().getAny().isEmpty()){
				detail =  new StatusDetail(status.getStatusDetail().getAny());
			}
			return Status
					.builder(create(status.getStatusCode()))
					.message(status.getStatusMessage())
					.detail(detail)
					.build();
		}

		private StatusCode create(StatusCodeType code) throws XacmlSyntaxException
		{
			if(code == null){
				return null;
			}
			return StatusCode.builder(StatusCodeIds.parse(code.getValue()))
					.minorStatus(create(code.getStatusCode()))
					.build();
		}

		private AttributeExp createValue(String dataType, Map<QName, String> attr, List<Object> content)
		{
			org.oasis.xacml.v30.jaxb.AttributeValueType va = new org.oasis.xacml.v30.jaxb.AttributeValueType();
			va.setDataType(dataType);
			va.getOtherAttributes().putAll(attr);
			va.getContent().addAll(content);
			Optional<TypeToXacml30> toXacml30 = TypeToXacml30.Types.getIndex().get(dataType);
			Preconditions.checkState(toXacml30.isPresent());
			return toXacml30.get().fromXacml30(va);
		}
	}
}
