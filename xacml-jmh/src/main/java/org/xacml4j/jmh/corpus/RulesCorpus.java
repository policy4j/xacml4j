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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.xacml4j.jmh.harness.CorpusRequest;
import org.xacml4j.jmh.harness.Marshalling;
import org.xacml4j.jmh.harness.PolicyCorpus;
import org.xacml4j.jmh.harness.PolicyCorpusProvider;
import org.xacml4j.jmh.harness.PolicySource;
import org.xacml4j.v30.Advice;
import org.xacml4j.v30.Attribute;
import org.xacml4j.v30.Category;
import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.Decision;
import org.xacml4j.v30.Effect;
import org.xacml4j.v30.Obligation;
import org.xacml4j.v30.Result;
import org.xacml4j.v30.Status;
import org.xacml4j.v30.SyntaxException;
import org.xacml4j.v30.marshal.MediaType;
import org.xacml4j.v30.marshal.RequestUnmarshaller;
import org.xacml4j.v30.policy.PolicyIDReference;
import org.xacml4j.v30.policy.PolicySetIDReference;
import org.xacml4j.v30.policy.function.impl.RegExpFunctions;
import org.xacml4j.v30.types.Entity;
import org.xacml4j.v30.types.StringVal;

import com.google.auto.service.AutoService;

/**
 * Deterministic XACML 3.0 cases for the next optimizations, each with its whole expected result:
 * integer and date ranges, a condition that many rules repeat, multi-valued bags, a wide request and
 * its narrow control, obligations and advice, and regular expressions in targets, in conditions, in a
 * higher-order call, and one that does not compile. The argument is the number of rules or policies
 * of each case.
 */
@AutoService(PolicyCorpusProvider.class)
public final class RulesCorpus implements PolicyCorpusProvider
{
	public static final String NAME = "rules";

	private static final MediaType XACML30 = MediaType.Type.XACML30_XML;
	private static final String NAMESPACE = "urn:oasis:names:tc:xacml:3.0:core:schema:wd-17";
	private static final String PREFIX = "urn:xacml4j:jmh:rules:";
	private static final String ROOT = "root.xml";
	private static final String SUBJECT = "urn:oasis:names:tc:xacml:1.0:subject-category:access-subject";
	private static final String RESOURCE = "urn:oasis:names:tc:xacml:3.0:attribute-category:resource";
	private static final String ACTION = "urn:oasis:names:tc:xacml:3.0:attribute-category:action";
	private static final String ENVIRONMENT = "urn:oasis:names:tc:xacml:3.0:attribute-category:environment";
	private static final List<String> CATEGORIES = List.of(SUBJECT, RESOURCE, ACTION, ENVIRONMENT);
	private static final String STRING = "http://www.w3.org/2001/XMLSchema#string";
	private static final String INTEGER = "http://www.w3.org/2001/XMLSchema#integer";
	private static final String DATE = "http://www.w3.org/2001/XMLSchema#date";
	private static final String ANY_URI = "http://www.w3.org/2001/XMLSchema#anyURI";
	private static final String FUNCTION = "urn:oasis:names:tc:xacml:1.0:function:";
	private static final String FUNCTION_20 = "urn:oasis:names:tc:xacml:2.0:function:";
	private static final String FUNCTION_30 = "urn:oasis:names:tc:xacml:3.0:function:";
	private static final String POLICY_FIRST_APPLICABLE = "urn:oasis:names:tc:xacml:1.0:policy-combining-algorithm:first-applicable";
	private static final String POLICY_DENY_OVERRIDES = "urn:oasis:names:tc:xacml:3.0:policy-combining-algorithm:deny-overrides";
	private static final String POLICY_PERMIT_OVERRIDES = "urn:oasis:names:tc:xacml:3.0:policy-combining-algorithm:permit-overrides";
	private static final String RULE_FIRST_APPLICABLE = "urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:first-applicable";
	private static final String RULE_DENY_OVERRIDES = "urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:deny-overrides";
	private static final String CASE = PREFIX + "case";
	private static final String SIZE = PREFIX + "size";
	private static final String DAY = PREFIX + "day";
	private static final String EMAIL = PREFIX + "email";
	private static final String DEPARTMENT = PREFIX + "department";
	private static final String ROLE = PREFIX + "role";
	private static final String GROUP = PREFIX + "group";
	private static final String STATUS = PREFIX + "status";
	private static final String RESOURCE_TYPE = PREFIX + "resource-type";
	private static final String OWNER = PREFIX + "owner";
	private static final String LOCATION = PREFIX + "location";
	private static final String USER = PREFIX + "user";
	private static final String COMMENT = PREFIX + "comment";
	private static final LocalDate DAY_ZERO = LocalDate.of(2024, 1, 1);
	private static final String EMAIL_PATTERN = "^[a-z0-9._]+@example\\.com$";
	/** A pattern whose character class is not closed, so that no engine compiles it */
	private static final String MALFORMED_PATTERN = "^[a-z0-9._+@example\\.com$";
	/** The pattern bag and the request values of conformance case IIC168 */
	private static final List<String> IIC168_PATTERNS = List.of("   This  is n*o*t* *IT!  ", "   This is not a match to IT!  ");
	private static final List<String> IIC168_VALUES = List.of("   This  is IT!  ", "   This  is not IT!  ");
	private static final int GROUPS = 8;
	private static final int UNREAD = 40;
	/** IncludeInResult attributes of the wide request and its narrow control, one per category */
	private static final List<Attr> SHOWN = List.of(
			new Attr(SUBJECT, "urn:oasis:names:tc:xacml:1.0:subject:subject-id", STRING, List.of("alice"), true),
			new Attr(RESOURCE, "urn:oasis:names:tc:xacml:1.0:resource:resource-id", STRING, List.of("document-1"), true),
			new Attr(ACTION, "urn:oasis:names:tc:xacml:1.0:action:action-id", STRING, List.of("read"), true),
			new Attr(ENVIRONMENT, PREFIX + "channel", STRING, List.of("web"), true));
	/*
	 * The root context keeps obligations and advice in hash maps. These ids iterate there in the
	 * order in which the responses are evaluated, which is also their sorted order.
	 */
	private static final String AUDIT = PREFIX + "obligation:audit";
	private static final String NOTIFY = PREFIX + "obligation:notify";
	private static final String RECORD = PREFIX + "obligation:record";
	private static final String BANNER = PREFIX + "advice:banner";
	private static final String NOTICE = PREFIX + "advice:notice";
	private static final String DENIED = PREFIX + "denied";
	private static final String ASSIGNED = PREFIX + "assignment:";

	@Override
	public String name(){
		return NAME;
	}

	@Override
	public PolicyCorpus load(Optional<String> argument) throws IOException, SyntaxException {
		int count = count(argument.orElse("100"));
		RequestUnmarshaller unmarshaller = Marshalling.requests(XACML30);
		List<CorpusRequest> requests = new ArrayList<>();
		for(Case c : cases(count)){
			requests.add(new CorpusRequest(c.id(), unmarshaller.unmarshal(new ByteArrayInputStream(
					c.request().getBytes(StandardCharsets.UTF_8))), c.expected()));
		}
		PolicySource root = new PolicySource(ROOT, XACML30, root(count).getBytes(StandardCharsets.UTF_8));
		return new PolicyCorpus(NAME + ":" + count, List.of(root), ROOT, requests);
	}

	private static int count(String value){
		try{
			int count = Integer.parseInt(value);
			if(count >= 1 && count <= 10000) return count;
		}catch(NumberFormatException ignored){
			// Report the same range for non-numeric and out-of-range values.
		}
		throw new IllegalArgumentException("Rules count must be an integer from 1 to 10000: \"" + value + "\"");
	}

	/** The oracle follows only the generated policies and requests; it never runs a PDP. */
	private static List<Case> cases(int count){
		int last = count - 1;
		List<String> roles = new ArrayList<>();
		for(int i = Math.max(0, count - GROUPS); i < count; i++) roles.add("role-" + i);
		roles.add("role-unknown");
		List<String> groups = new ArrayList<>();
		for(int i = 0; i < GROUPS; i++) groups.add("group-" + i);
		List<Attr> narrow = new ArrayList<>(List.of(caseOf("narrow"), new Attr(RESOURCE, STATUS, STRING, List.of("active"), false)));
		narrow.addAll(SHOWN);
		List<Attr> wide = new ArrayList<>(narrow);
		wide.set(0, caseOf("wide"));
		for(int i = 0; i < UNREAD; i++){
			wide.add(new Attr(CATEGORIES.get(i % CATEGORIES.size()), PREFIX + "unread:" + i,
					i % 2 == 0 ? STRING : INTEGER, List.of(i % 2 == 0 ? "value-" + i : Integer.toString(i)), false));
		}
		return List.of(
				new Case("integer-ranges", request(false, caseOf("integer-ranges"),
						new Attr(RESOURCE, SIZE, INTEGER, List.of(Long.toString(10L * last + 2)), false)),
						Result.ok(effect(Math.max(0, count - 2))).build()),
				new Case("date-ranges", request(false, caseOf("date-ranges"),
						new Attr(RESOURCE, DAY, DATE, List.of(DAY_ZERO.plusDays(10L * last + 2).toString()), false)),
						Result.ok(count > 1 ? Decision.DENY : Decision.PERMIT).build()),
				new Case("repeated-conditions", request(false, caseOf("repeated-conditions"),
						new Attr(SUBJECT, EMAIL, STRING, List.of("  Alice.Smith@Example.COM  "), false),
						new Attr(SUBJECT, DEPARTMENT, STRING, List.of("department-" + last), false)),
						Result.ok(Decision.PERMIT).build()),
				new Case("multi-valued", request(false, caseOf("multi-valued"),
						new Attr(SUBJECT, ROLE, STRING, roles, false), new Attr(SUBJECT, GROUP, STRING, groups, false)),
						Result.ok(Decision.DENY).build()),
				new Case("narrow", request(false, narrow.toArray(Attr[]::new)),
						Result.ok(Decision.PERMIT).includeInResultAttr(shown()).build()),
				new Case("wide", request(false, wide.toArray(Attr[]::new)),
						Result.ok(Decision.PERMIT).includeInResultAttr(shown()).build()),
				new Case("responses", request(true, caseOf("responses"),
						new Attr(RESOURCE, RESOURCE_TYPE, STRING, List.of("document"), false),
						new Attr(RESOURCE, OWNER, STRING, List.of("alice", "bob"), false)),
						permitWithResponses()),
				new Case("regex-targets", request(false, caseOf("regex-targets"),
						new Attr(RESOURCE, LOCATION, ANY_URI, List.of("https://records.example.com/" + last + "/report.pdf"), false)),
						Result.ok(effect(last)).build()),
				new Case("regex-conditions", request(false, caseOf("regex-conditions"),
						new Attr(SUBJECT, USER, STRING, List.of("user-" + last + "@mail.example.com"), false)),
						Result.ok(effect(last)).build()),
				new Case("regex-any-of-all", request(false, caseOf("regex-any-of-all"),
						new Attr(SUBJECT, COMMENT, STRING, IIC168_VALUES, false)),
						Result.ok(Decision.PERMIT).build()),
				new Case("regex-malformed", request(false, caseOf("regex-malformed"),
						new Attr(SUBJECT, USER, STRING, List.of("user-0@mail.example.com"), false)),
						Result.builder(Decision.INDETERMINATE_P,
								Status.syntaxError().message(RegExpFunctions.INVALID_PATTERN_MESSAGE).build()).build()));
	}

	/** Even rules permit and odd rules deny */
	private static Decision effect(int rule){
		return rule % 2 == 0 ? Decision.PERMIT : Decision.DENY;
	}

	private static List<Category> shown(){
		List<Category> categories = new ArrayList<>();
		for(Attr shown : SHOWN){
			categories.add(Category.builder(CategoryId.of(shown.category())).entity(Entity.builder()
					.attribute(Attribute.builder(shown.id()).includeInResult(true)
							.value(StringVal.of(shown.values().get(0))).build())
					.build()).build());
		}
		return categories;
	}

	/**
	 * Permit, from the permit policy after the deny policy: the advice and obligations of the rule,
	 * the policy, and the policy set that apply to Permit. Every policy and policy set that decided
	 * is listed, in id order.
	 */
	private static Result permitWithResponses(){
		return Result.ok(Decision.PERMIT)
				.obligation(
						Obligation.builder(AUDIT, Effect.PERMIT)
								.attribute(ASSIGNED + "owner", StringVal.of("alice"), StringVal.of("bob"))
								.attribute(ASSIGNED + "level", StringVal.of("info")).build(),
						Obligation.builder(NOTIFY, Effect.PERMIT).attribute(ASSIGNED + "channel", StringVal.of("email")).build(),
						Obligation.builder(RECORD, Effect.PERMIT).attribute(ASSIGNED + "case", StringVal.of("responses")).build())
				.advice(
						Advice.builder(BANNER, Effect.PERMIT).attribute(ASSIGNED + "text", StringVal.of("shared document")).build(),
						Advice.builder(NOTICE, Effect.PERMIT).attribute(ASSIGNED + "text", StringVal.of("retained")).build())
				.referencedPolicy(
						PolicySetIDReference.builder(PREFIX + "responses").versionAsString("1.0").build(),
						PolicyIDReference.builder(PREFIX + "responses:deny").versionAsString("1.0").build(),
						PolicyIDReference.builder(PREFIX + "responses:permit").versionAsString("1.0").build(),
						PolicySetIDReference.builder(PREFIX + "root").versionAsString("1.0").build())
				.build();
	}

	private static String root(int count){
		return """
				<?xml version="1.0" encoding="UTF-8"?>
				<PolicySet xmlns="%s" PolicySetId="%s" Version="1.0" PolicyCombiningAlgId="%s">
				<Target/>
				""".formatted(NAMESPACE, PREFIX + "root", POLICY_FIRST_APPLICABLE)
				+ integerRanges(count) + dateRanges(count) + repeatedConditions(count) + multiValued(count)
				+ wide() + responses(count) + regexTargets(count) + regexConditions(count) + regexAnyOfAll(count)
				+ regexMalformed(count) + "</PolicySet>\n";
	}

	/** Overlapping ranges [10i, 10i + 15) of one integer; the first match decides. */
	private static String integerRanges(int count){
		StringBuilder rules = new StringBuilder();
		for(int i = 0; i < count; i++){
			rules.append(rule("integer-ranges:" + i, effectOf(i), target(allOf(
					match("integer-less-than-or-equal", INTEGER, Integer.toString(10 * i), RESOURCE, SIZE),
					match("integer-greater-than", INTEGER, Integer.toString(10 * i + 15), RESOURCE, SIZE)))));
		}
		return policy("integer-ranges", RULE_FIRST_APPLICABLE, caseTarget("integer-ranges"), rules.toString());
	}

	/** Overlapping ranges of days [10i, 10i + 15) after the first day; a deny overrides. */
	private static String dateRanges(int count){
		StringBuilder rules = new StringBuilder();
		for(int i = 0; i < count; i++){
			rules.append(rule("date-ranges:" + i, effectOf(i), target(allOf(
					match("date-less-than-or-equal", DATE, DAY_ZERO.plusDays(10L * i).toString(), RESOURCE, DAY),
					match("date-greater-than", DATE, DAY_ZERO.plusDays(10L * i + 15).toString(), RESOURCE, DAY)))));
		}
		return policy("date-ranges", RULE_DENY_OVERRIDES, caseTarget("date-ranges"), rules.toString());
	}

	/** Every rule normalizes and matches the same e-mail address; only the last department is the request's. */
	private static String repeatedConditions(int count){
		String email = apply("string-regexp-match", value(STRING, EMAIL_PATTERN),
				apply("string-normalize-to-lower-case", apply("string-normalize-space",
						apply("string-one-and-only", designator(SUBJECT, EMAIL, STRING, false)))));
		StringBuilder rules = new StringBuilder();
		for(int i = 0; i < count; i++){
			rules.append(rule("repeated-conditions:" + i, "Permit", condition(apply("and", email,
					apply("string-equal", apply("string-one-and-only", designator(SUBJECT, DEPARTMENT, STRING, false)),
							value(STRING, "department-" + i))))));
		}
		return policy("repeated-conditions", RULE_FIRST_APPLICABLE, caseTarget("repeated-conditions"), rules.toString());
	}

	/** One policy per role; the request has several roles and groups, and the last role denies. */
	private static String multiValued(int count){
		String groups = condition(apply("and",
				apply30("all-of", function(FUNCTION_30 + "string-starts-with"), value(STRING, "group-"),
						designator(SUBJECT, GROUP, STRING, false)),
				apply30("any-of", function(FUNCTION + "string-equal"), value(STRING, "group-3"),
						designator(SUBJECT, GROUP, STRING, false))));
		StringBuilder policies = new StringBuilder();
		for(int i = 0; i < count; i++){
			policies.append(policy("multi-valued:" + i, RULE_DENY_OVERRIDES,
					target(allOf(match("string-equal", STRING, "role-" + i, SUBJECT, ROLE))),
					rule("multi-valued:" + i, i == count - 1 ? "Deny" : "Permit", groups)));
		}
		return policySet("multi-valued", POLICY_DENY_OVERRIDES, caseTarget("multi-valued"), policies.toString());
	}

	/** One rule that reads one attribute, for the wide request and its narrow control */
	private static String wide(){
		return policy("wide", RULE_FIRST_APPLICABLE, caseTarget("narrow", "wide"),
				rule("wide", "Permit", target(allOf(match("string-equal", STRING, "active", RESOURCE, STATUS)))));
	}

	/**
	 * Policies that do not apply, a policy that denies with responses for Deny, and a policy that
	 * permits with responses for both decisions, under a policy set with responses of its own.
	 */
	private static String responses(int count){
		String denied = assignment("text", value(STRING, "denied"));
		StringBuilder policies = new StringBuilder();
		for(int i = 0; i < count; i++){
			policies.append(policy("responses:filler:" + i, RULE_FIRST_APPLICABLE, resourceType("type-" + i),
					rule("responses:filler:" + i, "Permit")));
		}
		policies.append(policy("responses:deny", RULE_FIRST_APPLICABLE, resourceType("document"),
				rule("responses:deny", "Deny", advices(advice(DENIED, "Deny", denied))),
				obligations(obligation(DENIED, "Deny", denied))));
		policies.append(policy("responses:permit", RULE_FIRST_APPLICABLE, resourceType("document"),
				rule("responses:permit", "Permit",
						obligations(
								obligation(AUDIT, "Permit", assignment("owner", designator(RESOURCE, OWNER, STRING, true)),
										assignment("level", value(STRING, "info"))),
								obligation(DENIED, "Deny", denied)),
						advices(advice(BANNER, "Permit", assignment("text", value(STRING, "shared document"))))),
				obligations(obligation(NOTIFY, "Permit", assignment("channel", value(STRING, "email"))))));
		return policySet("responses", POLICY_PERMIT_OVERRIDES, caseTarget("responses"), policies.toString(),
				obligations(obligation(RECORD, "Permit", assignment("case", value(STRING, "responses")))),
				advices(advice(NOTICE, "Permit", assignment("text", value(STRING, "retained"))), advice(DENIED, "Deny", denied)));
	}

	/** Each rule's target matches a URI with a literal pattern of its own; only the last rule's pattern finds the request's URI. */
	private static String regexTargets(int count){
		StringBuilder rules = new StringBuilder();
		for(int i = 0; i < count; i++){
			rules.append(rule("regex-targets:" + i, effectOf(i), target(allOf("<Match MatchId=\"" + FUNCTION_20
					+ "anyURI-regexp-match\">" + value(STRING, "^https://records\\.example\\.com/" + i + "/[a-z]+\\.pdf$")
					+ designator(RESOURCE, LOCATION, ANY_URI, false) + "</Match>"))));
		}
		return policy("regex-targets", RULE_FIRST_APPLICABLE, caseTarget("regex-targets"), rules.toString());
	}

	/** Each rule's condition matches a user with a literal pattern of its own; only the last rule's pattern finds the request's user. */
	private static String regexConditions(int count){
		StringBuilder rules = new StringBuilder();
		for(int i = 0; i < count; i++){
			rules.append(rule("regex-conditions:" + i, effectOf(i), condition(apply("string-regexp-match",
					value(STRING, "^(user|admin)-" + i + "@[a-z]{2,16}\\.example\\.(com|org)$"),
					apply("string-one-and-only", designator(SUBJECT, USER, STRING, false))))));
		}
		return policy("regex-conditions", RULE_FIRST_APPLICABLE, caseTarget("regex-conditions"), rules.toString());
	}

	/** Permit rules with the condition of conformance case IIC168, all evaluated under deny-overrides */
	private static String regexAnyOfAll(int count){
		String iic168 = condition(apply("any-of-all", function(FUNCTION + "string-regexp-match"),
				apply("string-bag", value(STRING, IIC168_PATTERNS.get(0)), value(STRING, IIC168_PATTERNS.get(1))),
				designator(SUBJECT, COMMENT, STRING, false)));
		StringBuilder rules = new StringBuilder();
		for(int i = 0; i < count; i++){
			rules.append(rule("regex-any-of-all:" + i, "Permit", iic168));
		}
		return policy("regex-any-of-all", RULE_DENY_OVERRIDES, caseTarget("regex-any-of-all"), rules.toString());
	}

	/** Permit rules whose condition has a pattern that does not compile, all evaluated under deny-overrides */
	private static String regexMalformed(int count){
		String malformed = condition(apply("string-regexp-match", value(STRING, MALFORMED_PATTERN),
				apply("string-one-and-only", designator(SUBJECT, USER, STRING, false))));
		StringBuilder rules = new StringBuilder();
		for(int i = 0; i < count; i++){
			rules.append(rule("regex-malformed:" + i, "Permit", malformed));
		}
		return policy("regex-malformed", RULE_DENY_OVERRIDES, caseTarget("regex-malformed"), rules.toString());
	}

	private static String effectOf(int rule){
		return effect(rule) == Decision.PERMIT ? "Permit" : "Deny";
	}

	private static String policySet(String id, String algorithm, String target, String... content){
		return "<PolicySet PolicySetId=\"" + PREFIX + id + "\" Version=\"1.0\" PolicyCombiningAlgId=\"" + algorithm + "\">\n"
				+ target + String.join("", content) + "</PolicySet>\n";
	}

	private static String policy(String id, String algorithm, String target, String... content){
		return "<Policy PolicyId=\"" + PREFIX + id + "\" Version=\"1.0\" RuleCombiningAlgId=\"" + algorithm + "\">\n"
				+ target + String.join("", content) + "</Policy>\n";
	}

	private static String rule(String id, String effect, String... content){
		return "<Rule RuleId=\"" + PREFIX + id + ":rule\" Effect=\"" + effect + "\">\n" + String.join("", content) + "</Rule>\n";
	}

	private static String caseTarget(String... cases){
		String[] allOfs = new String[cases.length];
		for(int i = 0; i < cases.length; i++){
			allOfs[i] = allOf(match("string-equal", STRING, cases[i], ENVIRONMENT, CASE));
		}
		return target(allOfs);
	}

	private static String resourceType(String type){
		return target(allOf(match("string-equal", STRING, type, RESOURCE, RESOURCE_TYPE)));
	}

	private static String target(String... allOfs){
		return "<Target><AnyOf>\n" + String.join("", allOfs) + "</AnyOf></Target>\n";
	}

	private static String allOf(String... matches){
		return "<AllOf>" + String.join("", matches) + "</AllOf>\n";
	}

	private static String match(String function, String type, String literal, String category, String attribute){
		return "<Match MatchId=\"" + FUNCTION + function + "\">" + value(type, literal)
				+ designator(category, attribute, type, false) + "</Match>";
	}

	private static String condition(String expression){
		return "<Condition>" + expression + "</Condition>\n";
	}

	private static String apply(String function, String... arguments){
		return "<Apply FunctionId=\"" + FUNCTION + function + "\">" + String.join("", arguments) + "</Apply>";
	}

	private static String apply30(String function, String... arguments){
		return "<Apply FunctionId=\"" + FUNCTION_30 + function + "\">" + String.join("", arguments) + "</Apply>";
	}

	private static String function(String id){
		return "<Function FunctionId=\"" + id + "\"/>";
	}

	private static String value(String type, String value){
		return "<AttributeValue DataType=\"" + type + "\">" + value + "</AttributeValue>";
	}

	private static String designator(String category, String attribute, String type, boolean mustBePresent){
		return "<AttributeDesignator Category=\"" + category + "\" AttributeId=\"" + attribute + "\" DataType=\"" + type
				+ "\" MustBePresent=\"" + mustBePresent + "\"/>";
	}

	private static String obligations(String... expressions){
		return "<ObligationExpressions>" + String.join("", expressions) + "</ObligationExpressions>\n";
	}

	private static String obligation(String id, String fulfillOn, String... assignments){
		return "<ObligationExpression ObligationId=\"" + id + "\" FulfillOn=\"" + fulfillOn + "\">"
				+ String.join("", assignments) + "</ObligationExpression>";
	}

	private static String advices(String... expressions){
		return "<AdviceExpressions>" + String.join("", expressions) + "</AdviceExpressions>\n";
	}

	private static String advice(String id, String appliesTo, String... assignments){
		return "<AdviceExpression AdviceId=\"" + id + "\" AppliesTo=\"" + appliesTo + "\">"
				+ String.join("", assignments) + "</AdviceExpression>";
	}

	private static String assignment(String name, String expression){
		return "<AttributeAssignmentExpression AttributeId=\"" + ASSIGNED + name + "\">" + expression
				+ "</AttributeAssignmentExpression>";
	}

	private static Attr caseOf(String id){
		return new Attr(ENVIRONMENT, CASE, STRING, List.of(id), false);
	}

	private static String request(boolean returnPolicyIdList, Attr... attributes){
		StringBuilder xml = new StringBuilder("""
				<?xml version="1.0" encoding="UTF-8"?>
				<Request xmlns="%s" ReturnPolicyIdList="%s" CombinedDecision="false">
				""".formatted(NAMESPACE, returnPolicyIdList));
		for(String category : CATEGORIES){
			StringBuilder attributesOf = new StringBuilder();
			for(Attr attribute : attributes){
				if(!attribute.category().equals(category)) continue;
				attributesOf.append("\t\t<Attribute AttributeId=\"").append(attribute.id())
						.append("\" IncludeInResult=\"").append(attribute.includeInResult()).append("\">\n");
				for(String value : attribute.values()){
					attributesOf.append("\t\t\t").append(value(attribute.type(), value)).append('\n');
				}
				attributesOf.append("\t\t</Attribute>\n");
			}
			if(!attributesOf.isEmpty()){
				xml.append("\t<Attributes Category=\"").append(category).append("\">\n")
						.append(attributesOf).append("\t</Attributes>\n");
			}
		}
		return xml.append("</Request>\n").toString();
	}

	private record Attr(String category, String id, String type, List<String> values, boolean includeInResult) {}

	private record Case(String id, String request, Result expected) {}
}
