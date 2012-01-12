package com.artagon.xacml.v30.marshall.jaxb;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.artagon.xacml.v30.AttributeCategories;
import com.artagon.xacml.v30.marshall.ResponseMarshaller;
import com.artagon.xacml.v30.pdp.Advice;
import com.artagon.xacml.v30.pdp.Attribute;
import com.artagon.xacml.v30.pdp.AttributeAssignment;
import com.artagon.xacml.v30.pdp.AttributeExp;
import com.artagon.xacml.v30.pdp.Attributes;
import com.artagon.xacml.v30.pdp.Decision;
import com.artagon.xacml.v30.pdp.Effect;
import com.artagon.xacml.v30.pdp.Obligation;
import com.artagon.xacml.v30.pdp.ResponseContext;
import com.artagon.xacml.v30.pdp.Result;
import com.artagon.xacml.v30.pdp.Status;
import com.artagon.xacml.v30.types.XPathExpType;
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
	private Mapper mapper;
	private ObjectFactory factory;
	
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
				if(log.isDebugEnabled()){
					log.debug("Mapping result=\"{}\"", resultV3);
				}
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
			Attributes resource = result.getAttribute(AttributeCategories.RESOURCE);
			if(resource == null){
				return null;
			}
			Collection<Attribute> attrs = resource.getAttributes(RESOURCE_ID);
			if(attrs.size() == 1){
				Attribute resourceId = Iterables.getOnlyElement(attrs);
				return Iterables.getOnlyElement(resourceId.getValues()).toXacmlString();
			}
			Collection<AttributeExp> values =  resource.getAttributeValues(
					CONTENT_SELECTOR, XPathExpType.XPATHEXPRESSION);
			if(values.isEmpty() ||
					values.size() > 1){
				return null;
			}
			return Iterables.getOnlyElement(values).toXacmlString();
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
			obligation.setFulfillOn(v30ToV20EffectnMapping.get(advice.getFullfillOn()));
			for(AttributeAssignment a : advice.getAttributes()){
				obligation.getAttributeAssignment().add(create(a));
			}
			return obligation;
		}
		
		private ObligationType create(Obligation o)
		{
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
			com.artagon.xacml.v30.pdp.AttributeExpType t = (com.artagon.xacml.v30.pdp.AttributeExpType)(a.getAttribute().getType());
			attr.setDataType(t.getDataTypeId());
			attr.setAttributeId(a.getAttributeId());
			attr.getContent().add(a.getAttribute().toXacmlString());
			return attr;
		}
	}
}
