package org.xacml4j.v30.marshall.jaxb;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
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
import org.xacml4j.util.Xacml20XPathTo30Transformer;
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
import org.xacml4j.v30.StatusCode;
import org.xacml4j.v30.StatusCodeIds;
import org.xacml4j.v30.StatusDetail;
import org.xacml4j.v30.XacmlSyntaxException;
import org.xacml4j.v30.marshall.ResponseUnmarshaller;
import org.xacml4j.v30.pdp.RequestSyntaxException;
import org.xacml4j.v30.types.StringType;
import org.xacml4j.v30.types.Types;
import org.xacml4j.v30.types.XPathExpType;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;

public class Xacml20ResponseContextUnmarshaller
	extends BaseJAXBUnmarshaller<ResponseContext>
implements ResponseUnmarshaller
{
	private Mapper mapper;

	public Xacml20ResponseContextUnmarshaller(Types dataTypes){
		super(JAXBContextUtil.getInstance());
		Preconditions.checkNotNull(dataTypes);
		this.mapper = new Mapper(dataTypes);
	}

	public Xacml20ResponseContextUnmarshaller(){
		this(Types.builder()
				.defaultTypes()
				.create());
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

		private final static Map<Decision, DecisionType> v30ToV20DecisionMapping = new HashMap<Decision, DecisionType>();
		private final static Map<DecisionType, Decision> v20ToV30DecisionMapping = new HashMap<DecisionType, Decision>();

		private final static Map<EffectType, Effect> v20ToV30EffectnMapping = new HashMap<EffectType, Effect>();
		private final static Map<Effect, EffectType> v30ToV20EffectnMapping = new HashMap<Effect, EffectType>();

		private Types dataTypes;

		static
		{
			v30ToV20DecisionMapping.put(Decision.DENY, DecisionType.DENY);
			v30ToV20DecisionMapping.put(Decision.PERMIT, DecisionType.PERMIT);
			v30ToV20DecisionMapping.put(Decision.NOT_APPLICABLE, DecisionType.NOT_APPLICABLE);
			v30ToV20DecisionMapping.put(Decision.INDETERMINATE, DecisionType.INDETERMINATE);

			v20ToV30DecisionMapping.put(DecisionType.DENY, Decision.DENY);
			v20ToV30DecisionMapping.put(DecisionType.PERMIT, Decision.PERMIT);
			v20ToV30DecisionMapping.put(DecisionType.NOT_APPLICABLE, Decision.NOT_APPLICABLE);
			v20ToV30DecisionMapping.put(DecisionType.INDETERMINATE, Decision.INDETERMINATE);


			v20ToV30EffectnMapping.put(EffectType.DENY, Effect.DENY);
			v20ToV30EffectnMapping.put(EffectType.PERMIT, Effect.PERMIT);

			v30ToV20EffectnMapping.put(Effect.DENY, EffectType.DENY);
			v30ToV20EffectnMapping.put(Effect.PERMIT, EffectType.PERMIT);

		}

		public Mapper(Types dataTypes){
			Preconditions.checkNotNull(dataTypes);
			this.dataTypes = dataTypes;
		}

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
			Decision d = v20ToV30DecisionMapping.get(result.getDecision());
			Status status = create(result.getStatus());
			if(d == Decision.INDETERMINATE){
				return Result.createIndeterminate(status).build();
			}
			Result.Builder b = Result
					.builder(d, create(result.getStatus()))
					.obligation(getObligations(result));
			if(result.getResourceId() != null){
				b.includeInResult(Attributes
						.builder(AttributeCategories.RESOURCE)
						.attribute(
								Attribute
								.builder(RESOURCE_ID)
								.value(StringType.STRING, result.getResourceId())
								.build())
						.build());
			}
			return b.build();
		}

		private Collection<Obligation> getObligations(ResultType r) throws XacmlSyntaxException
		{
			if(r.getObligations() == null){
				return Collections.<Obligation>emptyList();
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
						.value(createValue(a.getDataType(), a.getContent(), a.getOtherAttributes())).build());
			}
			return Obligation
					.builder(o.getObligationId(), v20ToV30EffectnMapping.get(o.getFulfillOn()))
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
			return new Status(create(status.getStatusCode()), status.getStatusMessage(), detail);
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

		private AttributeExp createValue(String dataTypeId,
				List<Object> any, Map<QName, String> other)
			throws XacmlSyntaxException
		{
			if(any == null ||
					any.isEmpty()){
				throw new RequestSyntaxException("Attribute does not have content");
			}
			org.xacml4j.v30.AttributeExpType dataType = dataTypes.getType(dataTypeId);
			if(dataType == null){
				throw new RequestSyntaxException(
						"DataTypeId=\"%s\" can be be " +
						"resolved to valid XACML type", dataTypeId);
			}
			Object o = Iterables.getOnlyElement(any);
			if(log.isDebugEnabled()){
				log.debug("Creating typeId=\"{}\" value=\"{}\"", dataType, o);
			}
			if(dataType.equals(XPathExpType.XPATHEXPRESSION)){
				String xpath = Xacml20XPathTo30Transformer.transform20PathTo30((String)o);
				return dataType.create(xpath, other);
			}
			return dataType.create(o);
		}
	}
}
