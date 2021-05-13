package org.xacml4j.v30.marshal.json;

/*
 * #%L
 * Xacml4J Gson Integration
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

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xacml4j.v30.Advice;
import org.xacml4j.v30.Categories;
import org.xacml4j.v30.Category;
import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.CompositeDecisionRuleIDReference;
import org.xacml4j.v30.Decision;
import org.xacml4j.v30.Obligation;
import org.xacml4j.v30.Result;
import org.xacml4j.v30.Status;
import org.xacml4j.v30.pdp.PolicyIDReference;
import org.xacml4j.v30.pdp.PolicySetIDReference;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableBiMap;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;

public class ResultAdapter implements JsonDeserializer<Result>, JsonSerializer<Result>
{
    private final static Logger log = LoggerFactory.getLogger(ResultAdapter.class);

	private static final String DECISION_PROPERTY = "Decision";
	private static final String STATUS_PROPERTY = "Status";
	private static final String OBLIGATIONS_PROPERTY = "Obligations";
	private static final String ASSOCIATED_ADVICE_PROPERTY = "AssociatedAdvice";
	private static final String CATEGORIES_PROPERTY = "Category";
	private static final String POLICY_IDENTIFIER_PROPERTY = "PolicyIdentifier";
	private static final String POLICY_ID_REFERENCE_PROPERTY = "PolicyIdReference";
	private static final String POLICY_SET_ID_REFERENCE_PROPERTY = "PolicySetIdReference";

	private static final Type OBLIGATIONS_TYPE = new TypeToken<Collection<Obligation>>() {
	}.getType();
	private static final Type ADVICE_TYPE = new TypeToken<Collection<Advice>>() {
	}.getType();
	private static final Type ATTRIBUTES_TYPE = new TypeToken<Collection<Category>>() {
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
			throw new JsonParseException(String.format(
                    "Invalid 'Decision' value: \"%s\"", decisionValue));
		}
		Status status = context.deserialize(o.get(STATUS_PROPERTY), Status.class);
		Result.Builder builder = Result.builder(decision, status);

		final Collection<Obligation> obligations = context.deserialize(o.get(OBLIGATIONS_PROPERTY), OBLIGATIONS_TYPE);
		if (obligations != null) {
			builder.obligation(obligations);
		}

		final Collection<Advice> advices = context.<List<Advice>>deserialize(o.get(ASSOCIATED_ADVICE_PROPERTY), ADVICE_TYPE);
		if (advices != null) {
			builder.advice(advices);
		}

		final Collection<Category> attributes = context.<List<Category>>deserialize(o.get(CATEGORIES_PROPERTY), ATTRIBUTES_TYPE);
		if (attributes != null) {
			builder.includeInResultAttr(attributes);
		}

        for(String catId : Categories.getCategoryShortNames()){
            JsonElement category = o.get(catId);
            if(category != null){
                CategoryId categoryId = Categories.parse(catId);
                builder.includeInResult(CategoryAdapter.deserializeCategory(categoryId, category, context));
            }
        }
		deserializePolicyIdentifiers(o, context, builder);
		return builder.build();
	}

	private void deserializePolicyIdentifiers(JsonObject o, JsonDeserializationContext context,
                                              Result.Builder builder) {
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
	public JsonElement serialize(Result src, Type typeOfSrc, JsonSerializationContext context)
    {
		JsonObject o = new JsonObject();
		o.addProperty(DECISION_PROPERTY, DECISION_VALUE_MAP.get(src.getDecision()));
        o.add(STATUS_PROPERTY, context.serialize(src.getStatus()));
        Collection<Category> customCategories = Categories.getCustomCategories(src.getIncludeInResultAttributes());
		if (customCategories != null && !customCategories.isEmpty()) {
			o.add(CATEGORIES_PROPERTY, context.serialize(customCategories));
		}
        Collection<Category> defaultCategories = Categories.getDefaultCategories(src.getIncludeInResultAttributes());
        for(Category category : defaultCategories){
            JsonElement categoryJson = CategoryAdapter.serializeDefaultCategory(category, context);
            o.add(category.getCategoryId().getShortName(), categoryJson);
        }
        Collection<Obligation> obligations = src.getObligations();
        if (obligations != null && !obligations.isEmpty()) {
            o.add(OBLIGATIONS_PROPERTY,
                    context.serialize(obligations, OBLIGATIONS_TYPE));
        }
        Collection<Advice> associatedAdvice = src.getAssociatedAdvice();
        if (associatedAdvice != null && !associatedAdvice.isEmpty()) {
            o.add(ASSOCIATED_ADVICE_PROPERTY,
                    context.serialize(associatedAdvice, ADVICE_TYPE));
        }
		serializePolicyIdentifiers(src, context, o);
		return o;
	}

	private void serializePolicyIdentifiers(Result src, JsonSerializationContext context, JsonObject o) {
		Collection<CompositeDecisionRuleIDReference> policyIdentifiers = src.getPolicyIdentifiers();
		if (policyIdentifiers == null ||
                policyIdentifiers.isEmpty()) {
            return;
        }
        JsonObject policyIdentifiersJson = new JsonObject();
        Collection<PolicyIDReference> policyIdReferences = getPolicyIdRefs(policyIdentifiers);
        Collection<PolicySetIDReference> policySetIdReferences = getPolicySetIdRefs(policyIdentifiers);
        if (!policyIdReferences.isEmpty()) {
            policyIdentifiersJson.add(POLICY_ID_REFERENCE_PROPERTY, context.serialize(policyIdReferences));
        }
        if (!policySetIdReferences.isEmpty()) {
            policyIdentifiersJson.add(POLICY_SET_ID_REFERENCE_PROPERTY, context.serialize(policySetIdReferences));
        }
        o.add(POLICY_IDENTIFIER_PROPERTY, policyIdentifiersJson);
	}

    private Collection<PolicySetIDReference> getPolicySetIdRefs(Collection<? extends CompositeDecisionRuleIDReference> ids){
        return FluentIterable.from(ids).filter(PolicySetIDReference.class).toImmutableList();
    }

    private Collection<PolicyIDReference> getPolicyIdRefs(Collection<? extends CompositeDecisionRuleIDReference> ids){
        return FluentIterable.from(ids).filter(PolicyIDReference.class).toImmutableList();
    }

	private void splitPolicyIdentifiers(Collection<CompositeDecisionRuleIDReference> policyIdentifiers,
			List<PolicyIDReference> policyIdReferences,
            List<PolicySetIDReference> policySetIdReferences) {
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
