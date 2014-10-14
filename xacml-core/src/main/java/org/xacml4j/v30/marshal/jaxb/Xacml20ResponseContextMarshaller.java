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

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.oasis.xacml.v20.jaxb.context.DecisionType;
import org.oasis.xacml.v20.jaxb.context.ObjectFactory;
import org.oasis.xacml.v20.jaxb.context.ResponseType;
import org.oasis.xacml.v20.jaxb.context.ResultType;
import org.oasis.xacml.v20.jaxb.context.StatusCodeType;
import org.oasis.xacml.v20.jaxb.context.StatusType;
import org.oasis.xacml.v20.jaxb.policy.AttributeAssignmentType;
import org.oasis.xacml.v20.jaxb.policy.EffectType;
import org.oasis.xacml.v20.jaxb.policy.ObligationType;
import org.oasis.xacml.v20.jaxb.policy.ObligationsType;
import org.oasis.xacml.v30.jaxb.AttributeValueType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xacml4j.v30.Advice;
import org.xacml4j.v30.Attribute;
import org.xacml4j.v30.AttributeAssignment;
import org.xacml4j.v30.AttributeExp;
import org.xacml4j.v30.Categories;
import org.xacml4j.v30.Category;
import org.xacml4j.v30.Decision;
import org.xacml4j.v30.Effect;
import org.xacml4j.v30.Obligation;
import org.xacml4j.v30.ResponseContext;
import org.xacml4j.v30.Result;
import org.xacml4j.v30.Status;
import org.xacml4j.v30.marshal.ResponseMarshaller;
import org.xacml4j.v30.types.TypeToString;
import org.xacml4j.v30.types.XacmlTypes;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;


/**
 * Marshals XACML 3.0 {@link ResponseContext} to the XACML 2.0 response
 *
 * @author Giedrius Trumpickas
 */
public class Xacml20ResponseContextMarshaller
	extends BaseJAXBMarshaller<ResponseContext>
	implements ResponseMarshaller
{
	private final Mapper mapper;
	private final ObjectFactory factory;

	public Xacml20ResponseContextMarshaller(){
		super(JAXBContextUtil.getInstance());
		this.mapper = new Mapper();
		this.factory = new ObjectFactory();
	}

	@Override
	public Object marshal(ResponseContext source) throws IOException {
		ResponseType response = mapper.create(source);
		return factory.createResponse(response);
	}

	static class Mapper
	{
		private final static Logger log = LoggerFactory.getLogger(Mapper.class);

		private final static String CONTENT_SELECTOR = "urn:oasis:names:tc:xacml:3.0:content-selector";
		private final static String RESOURCE_ID = "urn:oasis:names:tc:xacml:1.0:resource:resource-id";

		private final static Map<Decision, DecisionType> V30_TO_V20_DECISION_MAPPING = ImmutableMap.<Decision, DecisionType>builder()
				.put(Decision.DENY, DecisionType.DENY)
				.put(Decision.PERMIT, DecisionType.PERMIT)
				.put(Decision.NOT_APPLICABLE, DecisionType.NOT_APPLICABLE)
				.put(Decision.INDETERMINATE, DecisionType.INDETERMINATE)
				.put(Decision.INDETERMINATE_D, DecisionType.INDETERMINATE)
				.put(Decision.INDETERMINATE_P, DecisionType.INDETERMINATE)
				.put(Decision.INDETERMINATE_DP, DecisionType.INDETERMINATE)
				.build();

		private final static Map<Effect, EffectType> V30_TO_V20_EFFECT_MAPPING = ImmutableMap.of(
			Effect.DENY, EffectType.DENY,
			Effect.PERMIT, EffectType.PERMIT);


		public ResponseType create(ResponseContext response)
		{
			if(log.isDebugEnabled()){
				log.debug("Mapping response=\"{}\"", response);
			}
			ResponseType responseV2 = new ResponseType();
			List<ResultType> results = responseV2.getResult();
			for(Result resultV3 : response.getResults()){
				if(log.isDebugEnabled()){
					log.debug("Mapping result=\"{}\"", resultV3);
				}
				results.add(create(resultV3));
			}
			return responseV2;
		}


		private ResultType create(Result result)
		{
			ResultType r = new ResultType();
			r.setStatus(createStatus(result.getStatus()));
			r.setResourceId(getResourceId(result));
			r.setObligations(getObligations(result));
			r.setDecision(V30_TO_V20_DECISION_MAPPING.get(result.getDecision()));
			return r;
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
			Category resource = result.getAttribute(Categories.RESOURCE);
			if(resource == null){
				return null;
			}
			Collection<Attribute> attrs = resource.getEntity().getAttributes(RESOURCE_ID);
			if(attrs.size() == 1){
				Attribute resourceId = Iterables.getOnlyElement(attrs);
				AttributeExp v =  Iterables.getOnlyElement(resourceId.getValues());
				Optional<TypeToString> toString = TypeToString.Types.getIndex().get(v.getType());
				Preconditions.checkState(toString.isPresent());
				return toString.get().toString(v);
			}
			Collection<AttributeExp> values =  resource.getEntity().getAttributeValues(
					CONTENT_SELECTOR, XacmlTypes.XPATH);
			if(values.isEmpty() ||
					values.size() > 1){
				return null;
			}
			AttributeExp v =  Iterables.getOnlyElement(values);
			Optional<TypeToString> toString = TypeToString.Types.getIndex().get(v.getType());
			Preconditions.checkState(toString.isPresent());
			return toString.get().toString(v);
		}

		private ObligationsType getObligations(Result result)
		{
			Collection<Obligation> obligations = result.getObligations();
			Collection<Advice> advices = result.getAssociatedAdvice();
			if(obligations.isEmpty() &&
					advices.isEmpty()){
				return null;
			}
			ObligationsType obligationsv2  = new ObligationsType();
			for(Obligation o : obligations){
				obligationsv2.getObligation().add(create(o));
			}
			// Map advices to XACML 2.0 obligations
			for(Advice a : advices){
				obligationsv2.getObligation().add(create(a));
			}
			return obligationsv2;
		}

		private ObligationType create(Advice advice)
		{
			ObligationType obligation = new ObligationType();
			obligation.setObligationId(advice.getId());
			obligation.setFulfillOn(V30_TO_V20_EFFECT_MAPPING.get(advice.getFulfillOn()));
			for(AttributeAssignment a : advice.getAttributes()){
				obligation.getAttributeAssignment().add(create(a));
			}
			return obligation;
		}

		private ObligationType create(Obligation o)
		{
			ObligationType obligation = new ObligationType();
			obligation.setObligationId(o.getId());
			obligation.setFulfillOn(V30_TO_V20_EFFECT_MAPPING.get(o.getFulfillOn()));
			for(AttributeAssignment a : o.getAttributes()){
				obligation.getAttributeAssignment().add(create(a));
			}
			return obligation;
		}

		private AttributeAssignmentType create(AttributeAssignment a)
		{
			Optional<TypeToXacml30> toXacml30 = TypeToXacml30.Types.getIndex().get(a.getAttribute().getType());
			Preconditions.checkState(toXacml30.isPresent());
			AttributeValueType v30 = toXacml30.get().toXacml30(a.getAttribute());
			AttributeAssignmentType attr = new AttributeAssignmentType();
			attr.setDataType(v30.getDataType());
			attr.setAttributeId(a.getAttributeId());
			attr.getContent().addAll(v30.getContent());
			attr.getOtherAttributes().putAll(v30.getOtherAttributes());
			return attr;
		}
	}
}
