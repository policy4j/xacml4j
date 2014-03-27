package org.xacml4j.v30.marshal.json;

import static com.google.common.base.Preconditions.checkArgument;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;

import org.xacml4j.v30.Advice;
import org.xacml4j.v30.Attributes;
import org.xacml4j.v30.CompositeDecisionRuleIDReference;
import org.xacml4j.v30.Decision;
import org.xacml4j.v30.Obligation;
import org.xacml4j.v30.Result;
import org.xacml4j.v30.Status;
import org.xacml4j.v30.pdp.PolicyIDReference;
import org.xacml4j.v30.pdp.PolicySetIDReference;

import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.Lists;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;

public class ResultAdapter implements JsonDeserializer<Result>, JsonSerializer<Result> {

	private static final String DECISION_PROPERTY = "Decision";
	private static final String STATUS_PROPERTY = "Status";
	private static final String OBLIGATIONS_PROPERTY = "Obligations";
	private static final String ASSOCIATED_ADVICE_PROPERTY = "AssociatedAdvice";
	private static final String ATTRIBUTES_PROPERTY = "Attributes";
	private static final String POLICY_IDENTIFIER_PROPERTY = "PolicyIdentifier";
	private static final String POLICY_ID_REFERENCE_PROPERTY = "PolicyIdReference";
	private static final String POLICY_SET_ID_REFERENCE_PROPERTY = "PolicySetIdReference";

	private static final Type OBLIGATIONS_TYPE = new TypeToken<Collection<Obligation>>() {
	}.getType();
	private static final Type ADVICE_TYPE = new TypeToken<Collection<Advice>>() {
	}.getType();
	private static final Type ATTRIBUTES_TYPE = new TypeToken<Collection<Attributes>>() {
	}.getType();
	private static final Type POLICY_ID_REFERENCES_TYPE = new TypeToken<Collection<PolicyIDReference>>() {
	}.getType();
	private static final Type POLICY_SET_ID_REFERENCES_TYPE = new TypeToken<Collection<PolicySetIDReference>>() {
	}.getType();

	private static ImmutableBiMap<Decision, String> DECISION_VALUE_MAP = ImmutableBiMap.of(
			Decision.PERMIT, "Permit",
			Decision.DENY, "Deny",
			Decision.NOT_APPLICABLE, "NotApplicable",
			Decision.INDETERMINATE, "Indeterminate");

	@Override
	public Result deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
			throws JsonParseException {
		JsonObject o = json.getAsJsonObject();
		final String decisionValue = GsonUtil.getAsString(o, DECISION_PROPERTY, null);
		Decision decision = DECISION_VALUE_MAP.inverse().get(decisionValue);
		if (decision == null) {
			throw new JsonParseException(String.format("Invalid 'Decision' value: \"%s\"", decisionValue));
		}
		Status status = context.deserialize(o.get(STATUS_PROPERTY), Status.class);

		Result.Builder builder = Result.builder(decision, status);

		Collection<Obligation> obligations = context.deserialize(o.get(OBLIGATIONS_PROPERTY), OBLIGATIONS_TYPE);
		if (obligations != null) {
			checkArgument(!obligations.isEmpty(), "At least one obligation should be specified.");
			builder.obligation(obligations);
		}

		Collection<Advice> advice = context.deserialize(o.get(ASSOCIATED_ADVICE_PROPERTY), ADVICE_TYPE);
		if (advice != null) {
			checkArgument(!advice.isEmpty(), "At least one advice should be specified.");
			builder.advice(advice);
		}

		Collection<Attributes> attributes = context.deserialize(o.get(ATTRIBUTES_PROPERTY), ATTRIBUTES_TYPE);
		if (attributes != null) {
			builder.includeInResultAttr(attributes);
			// TODO: or should we call builder.resolvedAttr?
		}

		deserializePolicyIdentifiers(o, context, builder);

		return builder.build();
	}

	private void deserializePolicyIdentifiers(JsonObject o, JsonDeserializationContext context, Result.Builder builder) {
		JsonObject jsonPolicyIdentifiers = o.getAsJsonObject(POLICY_IDENTIFIER_PROPERTY);
		if (jsonPolicyIdentifiers != null) {
			Collection<PolicyIDReference> policyIdReferences = context.deserialize(
					jsonPolicyIdentifiers.get(POLICY_ID_REFERENCE_PROPERTY), POLICY_ID_REFERENCES_TYPE);
			if (policyIdReferences != null) {
				builder.evaluatedPolicies(policyIdReferences);
			}
			Collection<PolicySetIDReference> policySetIdReferences = context.deserialize(
					jsonPolicyIdentifiers.get(POLICY_SET_ID_REFERENCE_PROPERTY), POLICY_SET_ID_REFERENCES_TYPE);
			if (policySetIdReferences != null) {
				builder.evaluatedPolicies(policySetIdReferences);
			}
		}
	}

	@Override
	public JsonElement serialize(Result src, Type typeOfSrc, JsonSerializationContext context) {
		JsonObject o = new JsonObject();
		o.addProperty(DECISION_PROPERTY, DECISION_VALUE_MAP.get(src.getDecision()));

		if (src.getStatus() != null) {
			o.add(STATUS_PROPERTY, context.serialize(src.getStatus()));
		}

		Collection<Obligation> obligations = src.getObligations();
		if (obligations != null && !obligations.isEmpty()) {
			o.add(OBLIGATIONS_PROPERTY, context.serialize(obligations, OBLIGATIONS_TYPE));
		}

		Collection<Advice> associatedAdvice = src.getAssociatedAdvice();
		if (associatedAdvice != null && !associatedAdvice.isEmpty()) {
			o.add(ASSOCIATED_ADVICE_PROPERTY, context.serialize(associatedAdvice, ADVICE_TYPE));
		}
		System.out.println("Result marshall");
		Collection<Attributes> attributes = src.getIncludeInResultAttributes();
		if (attributes != null && !attributes.isEmpty()) {
			o.add(ATTRIBUTES_PROPERTY, context.serialize(attributes));
		}

		serializePolicyIdentifiers(src, context, o);

		return o;
	}

	private void serializePolicyIdentifiers(Result src, JsonSerializationContext context, JsonObject o) {
		Collection<CompositeDecisionRuleIDReference> policyIdentifiers = src.getPolicyIdentifiers();
		if (policyIdentifiers != null && !policyIdentifiers.isEmpty()) {
			JsonObject policyIdentifiersJson = new JsonObject();
			List<PolicyIDReference> policyIdReferences = Lists.newArrayList();
			List<PolicySetIDReference> policySetIdReferences = Lists.newArrayList();
			splitPolicyIdentifiers(policyIdentifiers, policyIdReferences, policySetIdReferences);
			if (!policyIdReferences.isEmpty()) {
				policyIdentifiersJson.add(POLICY_ID_REFERENCE_PROPERTY, context.serialize(policyIdReferences));
			}
			if (!policySetIdReferences.isEmpty()) {
				policyIdentifiersJson.add(POLICY_SET_ID_REFERENCE_PROPERTY, context.serialize(policySetIdReferences));
			}
			o.add(POLICY_IDENTIFIER_PROPERTY, policyIdentifiersJson);
		}
	}

	private void splitPolicyIdentifiers(Collection<CompositeDecisionRuleIDReference> policyIdentifiers,
			List<PolicyIDReference> policyIdReferences, List<PolicySetIDReference> policySetIdReferences) {
		for (CompositeDecisionRuleIDReference policyId : policyIdentifiers) {
			if (policyId instanceof PolicyIDReference) {
				policyIdReferences.add((PolicyIDReference) policyId);
			} else if (policyId instanceof PolicySetIDReference) {
				policySetIdReferences.add((PolicySetIDReference) policyId);
			} else {
				throw new IllegalArgumentException(String.format("Invalid policy ID type %s.", policyId.getClass()
						.getName()));
			}
		}
	}

}
