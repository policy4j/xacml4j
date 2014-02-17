package org.xacml4j.v30.marshal.jaxb;

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
import org.xacml4j.v30.AttributeCategories;
import org.xacml4j.v30.AttributeExp;
import org.xacml4j.v30.Attributes;
import org.xacml4j.v30.Decision;
import org.xacml4j.v30.Effect;
import org.xacml4j.v30.Obligation;
import org.xacml4j.v30.ResponseContext;
import org.xacml4j.v30.Result;
import org.xacml4j.v30.Status;
import org.xacml4j.v30.marshal.ResponseMarshaller;
import org.xacml4j.v30.types.TypeToString;
import org.xacml4j.v30.types.TypeToXacml30;
import org.xacml4j.v30.types.Types;
import org.xacml4j.v30.types.XPathExpType;

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

	public Xacml20ResponseContextMarshaller(Types types){
		super(JAXBContextUtil.getInstance());
		this.mapper = new Mapper(types);
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

		private final static Map<Decision, DecisionType> v30ToV20DecisionMapping = ImmutableMap.<Decision, DecisionType>builder()
				.put(Decision.DENY, DecisionType.DENY)
				.put(Decision.PERMIT, DecisionType.PERMIT)
				.put(Decision.NOT_APPLICABLE, DecisionType.NOT_APPLICABLE)
				.put(Decision.INDETERMINATE, DecisionType.INDETERMINATE)
				.put(Decision.INDETERMINATE_D, DecisionType.INDETERMINATE)
				.put(Decision.INDETERMINATE_P, DecisionType.INDETERMINATE)
				.put(Decision.INDETERMINATE_DP, DecisionType.INDETERMINATE)
				.build();

		private final static Map<Effect, EffectType> v30ToV20EffectMapping = ImmutableMap.of(
			Effect.DENY, EffectType.DENY,
			Effect.PERMIT, EffectType.PERMIT);
		
		private Types types;
		
		public Mapper(Types types){
			Preconditions.checkNotNull(types);
			this.types = types;
		}
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
			r.setDecision(v30ToV20DecisionMapping.get(result.getDecision()));
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
			Attributes resource = result.getAttribute(AttributeCategories.RESOURCE);
			if(resource == null){
				return null;
			}
			Collection<Attribute> attrs = resource.getAttributes(RESOURCE_ID);
			if(attrs.size() == 1){
				Attribute resourceId = Iterables.getOnlyElement(attrs);
				AttributeExp v =  Iterables.getOnlyElement(resourceId.getValues());
				TypeToString toString = types.getCapability(v.getType(), TypeToString.class);
				return toString.toString();
			}
			Collection<AttributeExp> values =  resource.getAttributeValues(
					CONTENT_SELECTOR, XPathExpType.XPATHEXPRESSION);
			if(values.isEmpty() ||
					values.size() > 1){
				return null;
			}
			AttributeExp v =  Iterables.getOnlyElement(values);
			TypeToString toString = types.getCapability(v.getType(), TypeToString.class);
			return toString.toString();
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
			// Map advice to XACML 2.0 obligations
			for(Advice a : advices){
				obligationsv2.getObligation().add(create(a));
			}
			return obligationsv2;
		}

		private ObligationType create(Advice advice)
		{
			ObligationType obligation = new ObligationType();
			obligation.setObligationId(advice.getId());
			obligation.setFulfillOn(v30ToV20EffectMapping.get(advice.getFulfillOn()));
			for(AttributeAssignment a : advice.getAttributes()){
				obligation.getAttributeAssignment().add(create(a));
			}
			return obligation;
		}

		private ObligationType create(Obligation o)
		{
			ObligationType obligation = new ObligationType();
			obligation.setObligationId(o.getId());
			obligation.setFulfillOn(v30ToV20EffectMapping.get(o.getFulfillOn()));
			for(AttributeAssignment a : o.getAttributes()){
				obligation.getAttributeAssignment().add(create(a));
			}
			return obligation;
		}

		private AttributeAssignmentType create(AttributeAssignment a)
		{
			TypeToXacml30 toXacml30 = types.getCapability(a.getAttribute().getType(), TypeToXacml30.class);
			AttributeValueType v30 = toXacml30.toXacml30(types, a.getAttribute());
			AttributeAssignmentType attr = new AttributeAssignmentType();
			attr.setDataType(v30.getDataType());
			attr.setAttributeId(a.getAttributeId());
			attr.getContent().addAll(v30.getContent());
			attr.getOtherAttributes().putAll(v30.getOtherAttributes());
			return attr;
		}
	}
}
