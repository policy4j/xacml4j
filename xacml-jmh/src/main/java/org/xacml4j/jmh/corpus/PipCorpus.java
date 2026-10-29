package org.xacml4j.jmh.corpus;

/*
 * #%L
 * Xacml4J JMH Benchmarks
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program. If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.auto.service.AutoService;
import org.xacml4j.jmh.harness.CorpusRequest;
import org.xacml4j.jmh.harness.Marshalling;
import org.xacml4j.jmh.harness.PolicyCorpus;
import org.xacml4j.jmh.harness.PolicyCorpusProvider;
import org.xacml4j.jmh.harness.PolicySource;
import org.xacml4j.v30.Advice;
import org.xacml4j.v30.Attribute;
import org.xacml4j.v30.AttributeReferenceEvaluationException;
import org.xacml4j.v30.BagOfValues;
import org.xacml4j.v30.Category;
import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.Decision;
import org.xacml4j.v30.DecisionRuleResponse;
import org.xacml4j.v30.Effect;
import org.xacml4j.v30.Obligation;
import org.xacml4j.v30.ResponseContext;
import org.xacml4j.v30.Result;
import org.xacml4j.v30.Status;
import org.xacml4j.v30.SyntaxException;
import org.xacml4j.v30.marshal.MediaType;
import org.xacml4j.v30.marshal.RequestUnmarshaller;
import org.xacml4j.v30.types.Entity;
import org.xacml4j.v30.types.Value;
import org.xacml4j.v30.types.XacmlTypes;

/** A deterministic corpus whose policy data is supplied by a PIP. */
@AutoService(PolicyCorpusProvider.class)
public final class PipCorpus implements PolicyCorpusProvider
{
	public static final String NAME = "pip";
	static final String PREFIX = "urn:xacml4j:jmh:pip:";
	static final String IDENTITY = PREFIX + "identity";
	static final String CASE = PREFIX + "case";
	static final String BULK = PREFIX + "bulk";
	static final String TENANT = PREFIX + "tenant";
	static final String ROLE = PREFIX + "role";
	static final String SHARED = PREFIX + "shared";
	static final String SHARED_PEER = PREFIX + "shared-peer";
	static final String SHARED_STATIC = PREFIX + "shared-static";
	static final String SHARED_STATIC_PEER = PREFIX + "shared-static-peer";
	static final String MISSING = PREFIX + "missing";
	static final String FAILURE = PREFIX + "failure";
	static final String FALLBACK = PREFIX + "fallback";
	static final String CONTENT = PREFIX + "content";
	static final String XML = "<profile><role>admin</role></profile>";
	private static final String ATTRIBUTE = PREFIX + "attribute:";
	private static final int DEFAULT_COUNT = 8;
	private static final String ROOT = "root.xml";
	private static final String NS = "urn:oasis:names:tc:xacml:3.0:core:schema:wd-17";
	private static final String RESOURCE = "urn:oasis:names:tc:xacml:3.0:attribute-category:resource";
	private static final String SUBJECT = "urn:oasis:names:tc:xacml:1.0:subject-category:access-subject";
	private static final String STRING = "http://www.w3.org/2001/XMLSchema#string";
	private static final String EQUAL = "urn:oasis:names:tc:xacml:1.0:function:string-equal";
	private static final String IS_IN = "urn:oasis:names:tc:xacml:1.0:function:string-is-in";
	private static final String OBLIGATION = PREFIX + "obligation";
	private static final String ADVICE = PREFIX + "advice";
	private static final List<String> CASES = List.of("independent", "parallel-attributes", "dependent", "shared",
			"shared-static", "missing-optional", "missing-required", "failure", "skipped-failure", "fallback", "selector");

	@Override
	public String name(){
		return NAME;
	}

	@Override
	public PolicyCorpus load(Optional<String> argument) throws IOException, SyntaxException {
		int count = argument.map(PipCorpus::fanOut).orElse(DEFAULT_COUNT);
		RequestUnmarshaller unmarshaller = Marshalling.requests(MediaType.Type.XACML30_XML);
		List<CorpusRequest> requests = new ArrayList<>();
		for(String id : CASES){
			requests.add(new CorpusRequest(id, unmarshaller.unmarshal(new ByteArrayInputStream(
					request(id).getBytes(StandardCharsets.UTF_8))), decision(id)));
		}
		PolicySource root = new PolicySource(ROOT, MediaType.Type.XACML30_XML,
				policy(count).getBytes(StandardCharsets.UTF_8));
		return new PolicyCorpus(NAME + ":" + count, List.of(root), ROOT, requests);
	}

	public static boolean isPip(String name){
		return name.equals(NAME) || name.startsWith(NAME + ":");
	}

	/** The fan-out of a corpus name or {@code corpus} parameter: {@code pip} (the default) or {@code pip:<n>}. */
	public static int count(String corpus){
		if(corpus.equals(NAME)) return DEFAULT_COUNT;
		if(corpus.startsWith(NAME + ":")) return fanOut(corpus.substring(NAME.length() + 1));
		throw new IllegalArgumentException("Not a PIP corpus: \"" + corpus + "\"");
	}

	/** The identifier of the attribute of the given index of the parallel-attributes resolver. */
	static String attributeId(int index){
		return ATTRIBUTE + index;
	}

	private static int fanOut(String value){
		try{
			int count = Integer.parseInt(value);
			if(count >= 1 && count <= 64) return count;
		}catch(NumberFormatException ignored){
			// Report the same range for non-numeric and out-of-range values.
		}
		throw new IllegalArgumentException("PIP fan-out must be an integer from 1 to 64: \"" + value + "\"");
	}

	private static Decision decision(String id){
		return switch(id){
			case "dependent" -> Decision.DENY;
			case "missing-optional" -> Decision.NOT_APPLICABLE;
			case "missing-required", "failure" -> Decision.INDETERMINATE_P;
			case "independent", "parallel-attributes", "shared", "shared-static", "skipped-failure", "fallback", "selector" -> Decision.PERMIT;
			default -> throw new IllegalArgumentException("Unknown PIP request: " + id);
		};
	}

	private static String policy(int count){
		StringBuilder xml = new StringBuilder("""
				<?xml version="1.0" encoding="UTF-8"?>
				<Policy xmlns="%s" PolicyId="urn:xacml4j:jmh:pip:policy" Version="1.0"
				        RuleCombiningAlgId="urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:first-applicable">
				  <Target/>
				""".formatted(NS));
		xml.append(rule("independent", "Permit", match(BULK, "v" + (count - 1), true)));
		xml.append(rule("parallel-attributes", "Permit", match(attributeId(count - 1), "v" + (count - 1), true)));
		xml.append(rule("dependent", "Deny", match(ROLE, "deny", true)));
		xml.append(rule("shared", "Permit", match(SHARED, "not-allow-1", true)));
		xml.append(rule("shared", "Permit", match(SHARED, "not-allow-2", true)));
		xml.append(rule("shared", "Permit", match(SHARED, "allow", true) + match(SHARED_PEER, "allow-peer", true)));
		xml.append(rule("shared-static", "Permit", match(SHARED_STATIC, "fixed", true) + match(SHARED_STATIC_PEER, "fixed-peer", true)));
		xml.append(rule("missing-optional", "Permit", condition(MISSING, false)));
		xml.append(rule("missing-required", "Permit", condition(MISSING, true)));
		xml.append(rule("failure", "Permit", condition(FAILURE, true)));
		xml.append(rule("skipped-failure", "Permit", ""));
		xml.append(rule("skipped-failure", "Permit", condition(FAILURE, true)));
		xml.append(rule("fallback", "Permit", match(FALLBACK, "allow", true)));
		xml.append(rule("selector", "Permit", selector()));
		return xml.append("</Policy>\n").toString();
	}

	private static String rule(String id, String effect, String expression){
		boolean condition = expression.startsWith("<Condition>");
		String target = match(CASE, id, true) + (condition ? "" : expression);
		return """
				<Rule RuleId="%s" Effect="%s">
				  <Target><AnyOf><AllOf>%s</AllOf></AnyOf></Target>
				  %s
				  <ObligationExpressions><ObligationExpression ObligationId="%s" FulfillOn="%s"/></ObligationExpressions>
				  <AdviceExpressions><AdviceExpression AdviceId="%s" AppliesTo="%s"/></AdviceExpressions>
				</Rule>
				""".formatted(PREFIX + id + ":" + Math.abs(expression.hashCode()), effect, target,
				condition ? expression : "", OBLIGATION, effect, ADVICE, effect);
	}

	private static String match(String attribute, String value, boolean required){
		return """
				<Match MatchId="%s"><AttributeValue DataType="%s">%s</AttributeValue>
				<AttributeDesignator Category="%s" AttributeId="%s" DataType="%s" MustBePresent="%s"/></Match>
				""".formatted(EQUAL, STRING, value, RESOURCE, attribute, STRING, required);
	}

	private static String condition(String attribute, boolean required){
		return """
				<Condition><Apply FunctionId="%s"><AttributeValue DataType="%s">allow</AttributeValue>
				<AttributeDesignator Category="%s" AttributeId="%s" DataType="%s" MustBePresent="%s"/>
				</Apply></Condition>
				""".formatted(IS_IN, STRING, RESOURCE, attribute, STRING, required);
	}

	private static String selector(){
		return """
				<Match MatchId="%s"><AttributeValue DataType="%s">admin</AttributeValue>
				<AttributeSelector Category="%s" Path="./role/text()" ContextSelectorId="%s"
				                   DataType="%s" MustBePresent="true"/></Match>
				""".formatted(EQUAL, STRING, RESOURCE, CONTENT, STRING);
	}

	private static String request(String id){
		return """
				<?xml version="1.0" encoding="UTF-8"?>
				<Request xmlns="%s" ReturnPolicyIdList="false" CombinedDecision="false">
				  <Attributes Category="%s"><Attribute AttributeId="%s" IncludeInResult="false">
				    <AttributeValue DataType="%s">%s</AttributeValue>
				  </Attribute></Attributes>
				</Request>
				""".formatted(NS, SUBJECT, IDENTITY, STRING, id);
	}

	/** Checks all response fields, including the order of obligations and advice. */
	public static Optional<String> check(PolicyCorpus corpus, CorpusRequest request, ResponseContext response){
		Result expected = expected(request.id(), count(corpus.name()));
		if(response.getResults().size() != 1){
			return Optional.of("expected one result, got " + response.getResults().size());
		}
		Result actual = response.getResults().iterator().next();
		if(!actual.equals(expected) ||
				!sameResponses(actual.getObligations(), expected.getObligations()) ||
				!sameResponses(actual.getAssociatedAdvice(), expected.getAssociatedAdvice()) ||
				!List.copyOf(actual.getPolicyIdentifiers()).equals(List.copyOf(expected.getPolicyIdentifiers())) ||
				!attributes(actual.getIncludeInResultAttributes()).equals(attributes(expected.getIncludeInResultAttributes())) ||
				!attributes(actual.getResolvedAttributes()).equals(attributes(expected.getResolvedAttributes())) ||
				!actual.getStatus().getError().equals(expected.getStatus().getError())){
			return Optional.of("expected " + expected + ", got " + actual);
		}
		return Optional.empty();
	}

	private static boolean sameResponses(Collection<? extends DecisionRuleResponse> actual,
			Collection<? extends DecisionRuleResponse> expected){
		List<? extends DecisionRuleResponse> actualList = List.copyOf(actual);
		List<? extends DecisionRuleResponse> expectedList = List.copyOf(expected);
		if(actualList.size() != expectedList.size()) return false;
		for(int i = 0; i < actualList.size(); i++){
			DecisionRuleResponse a = actualList.get(i);
			DecisionRuleResponse e = expectedList.get(i);
			if(!a.equals(e) || a.getFulfillOn() != e.getFulfillOn() ||
					!List.copyOf(a.getAttributes()).equals(List.copyOf(e.getAttributes()))) return false;
		}
		return true;
	}

	private static Result expected(String id, int count){
		Decision decision = decision(id);
		Status status = switch(id){
			case "missing-required" -> Status.missingAttribute(MISSING).message(
					"AttributeId=\"%s\", CategoryId=\"%s\", DataType=\"%s\"",
					MISSING, CategoryId.RESOURCE, XacmlTypes.STRING.getShortTypeId()).build();
			case "failure" -> Status.processingError().message(AttributeReferenceEvaluationException.RESOLUTION_FAILED_MESSAGE).build();
			default -> Status.ok().build();
		};
		Entity.Builder values = Entity.builder().attribute(attribute(CASE, XacmlTypes.STRING.bagOf(XacmlTypes.STRING.ofAny(id))));
		switch(id){
			case "independent" -> {
				BagOfValues.Builder bag = XacmlTypes.STRING.bagBuilder();
				for(int i = 0; i < count; i++) bag.attribute(XacmlTypes.STRING.ofAny("v" + i));
				values.attribute(attribute(BULK, bag.build()));
			}
			case "parallel-attributes" -> values.attribute(attribute(attributeId(count - 1),
					XacmlTypes.STRING.bagOf(XacmlTypes.STRING.ofAny("v" + (count - 1)))));
			case "dependent" -> values.attribute(attribute(TENANT, XacmlTypes.STRING.bagOf(XacmlTypes.STRING.ofAny("tenant-dependent"))),
					attribute(ROLE, XacmlTypes.STRING.bagOf(XacmlTypes.STRING.ofAny("deny"))));
			case "shared" -> values.attribute(attribute(SHARED, XacmlTypes.STRING.bagOf(XacmlTypes.STRING.ofAny("allow"))),
					attribute(SHARED_PEER, XacmlTypes.STRING.bagOf(XacmlTypes.STRING.ofAny("allow-peer"))));
			case "shared-static" -> values.attribute(attribute(SHARED_STATIC, XacmlTypes.STRING.bagOf(XacmlTypes.STRING.ofAny("fixed"))),
					attribute(SHARED_STATIC_PEER, XacmlTypes.STRING.bagOf(XacmlTypes.STRING.ofAny("fixed-peer"))));
			case "fallback" -> values.attribute(attribute(FALLBACK, XacmlTypes.STRING.bagOf(XacmlTypes.STRING.ofAny("allow"))));
			case "selector" -> values.attribute(attribute(CONTENT, XacmlTypes.ENTITY.bagOf(contentEntity())));
			default -> { }
		}
		Result.Builder result = Result.builder(decision, status)
				.resolvedAttr(List.of(Category.builder(CategoryId.RESOURCE).entity(values.build()).build()));
		if(!decision.isIndeterminate() && decision != Decision.NOT_APPLICABLE){
			Effect effect = decision == Decision.PERMIT ? Effect.PERMIT : Effect.DENY;
			result.obligation(Obligation.builder(OBLIGATION, effect).build())
					.advice(Advice.builder(ADVICE, effect).build());
		}
		return result.build();
	}

	static Entity contentEntity(){
		return Entity.builder().xmlContent(XML)
				.attribute(Attribute.builder(CONTENT).value(XacmlTypes.XPATH.ofAny("/profile", CategoryId.RESOURCE)).build())
				.build();
	}

	private static Attribute attribute(String id, BagOfValues bag){
		return Attribute.builder(id).values(bag).build();
	}

	private static Map<CategoryId, Map<String, List<Value<?>>>> attributes(Collection<Category> categories){
		Map<CategoryId, Map<String, List<Value<?>>>> result = new LinkedHashMap<>();
		for(Category category : categories){
			Map<String, List<Value<?>>> values = new LinkedHashMap<>();
			for(Attribute attribute : category.getEntity().attributes()){
				values.put(attribute.getAttributeId(), List.copyOf(attribute.values()));
			}
			result.put(category.getCategoryId(), values);
		}
		return result;
	}
}
