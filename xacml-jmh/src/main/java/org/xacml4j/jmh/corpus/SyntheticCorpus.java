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
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.xacml4j.jmh.harness.CorpusRequest;
import org.xacml4j.jmh.harness.Marshalling;
import org.xacml4j.jmh.harness.PolicyCorpus;
import org.xacml4j.jmh.harness.PolicyCorpusProvider;
import org.xacml4j.jmh.harness.PolicySource;
import org.xacml4j.v30.Decision;
import org.xacml4j.v30.SyntaxException;
import org.xacml4j.v30.marshal.MediaType;
import org.xacml4j.v30.marshal.RequestUnmarshaller;

import com.google.auto.service.AutoService;

/** Deterministic XACML 3.0 policies and requests with a table-based oracle. */
@AutoService(PolicyCorpusProvider.class)
public final class SyntheticCorpus implements PolicyCorpusProvider
{
	public static final String NAME = "synthetic";

	private static final MediaType XACML30 = MediaType.Type.XACML30_XML;
	private static final String NAMESPACE = "urn:oasis:names:tc:xacml:3.0:core:schema:wd-17";
	private static final String POLICY_ID_PREFIX = "urn:xacml4j:jmh:synthetic:policy:";
	private static final String ROOT = "root.xml";
	private static final String RESOURCE_CATEGORY = "urn:oasis:names:tc:xacml:3.0:attribute-category:resource";
	private static final String RESOURCE_ID = "urn:oasis:names:tc:xacml:1.0:resource:resource-id";
	private static final String ACTION_CATEGORY = "urn:oasis:names:tc:xacml:3.0:attribute-category:action";
	private static final String ACTION_ID = "urn:oasis:names:tc:xacml:1.0:action:action-id";
	private static final List<ActionRule> RULES = List.of(
			new ActionRule("read", "Permit", Decision.PERMIT),
			new ActionRule("delete", "Deny", Decision.DENY));

	@Override
	public String name(){
		return NAME;
	}

	@Override
	public PolicyCorpus load(Optional<String> argument) throws IOException, SyntaxException {
		int count = count(argument.orElse("100"));
		List<PolicySource> policies = new ArrayList<>(count + 1);
		policies.add(source(ROOT, root(count)));
		for(int i = 0; i < count; i++){
			policies.add(source("policy-" + i + ".xml", policy(i)));
		}

		RequestUnmarshaller unmarshaller = Marshalling.requests(XACML30);
		List<CorpusRequest> requests = new ArrayList<>(7);
		for(int slot : new LinkedHashSet<>(List.of(0, count / 2, count - 1))){
			String label = slot == 0 ? "first" : slot == count - 1 ? "last" : "middle";
			for(ActionRule rule : RULES){
				String id = label + "-" + rule.effect().toLowerCase(Locale.ROOT);
				requests.add(new CorpusRequest(id, unmarshaller.unmarshal(new ByteArrayInputStream(
						request("resource-" + slot, rule.action()).getBytes(StandardCharsets.UTF_8))),
						expected(count, slot, rule.action())));
			}
		}
		requests.add(new CorpusRequest("unknown-notapplicable", unmarshaller.unmarshal(new ByteArrayInputStream(
				request("resource-unknown", "read").getBytes(StandardCharsets.UTF_8))), expected(count, -1, "read")));
		return new PolicyCorpus(NAME + ":" + count, policies, ROOT, requests);
	}

	private static int count(String value){
		int count;
		try{
			count = Integer.parseInt(value);
		}catch(NumberFormatException e){
			throw new IllegalArgumentException("Synthetic policy count must be an integer from 1 to 100000: \"" + value + "\"", e);
		}
		if(count < 1 || count > 100000){
			throw new IllegalArgumentException("Synthetic policy count must be an integer from 1 to 100000: \"" + value + "\"");
		}
		return count;
	}

	private static PolicySource source(String name, String xml){
		return new PolicySource(name, XACML30, xml.getBytes(StandardCharsets.UTF_8));
	}

	private static String root(int count){
		StringBuilder xml = new StringBuilder("""
				<?xml version="1.0" encoding="UTF-8"?>
				<PolicySet xmlns="%s" PolicySetId="urn:xacml4j:jmh:synthetic:root" Version="1.0"
						PolicyCombiningAlgId="urn:oasis:names:tc:xacml:3.0:policy-combining-algorithm:deny-overrides">
					<Target/>
				""".formatted(NAMESPACE));
		for(int i = 0; i < count; i++){
			xml.append("\t<PolicyIdReference>").append(POLICY_ID_PREFIX).append(i).append("</PolicyIdReference>\n");
		}
		return xml.append("</PolicySet>\n").toString();
	}

	private static String policy(int slot){
		StringBuilder xml = new StringBuilder("""
				<?xml version="1.0" encoding="UTF-8"?>
				<Policy xmlns="%s" PolicyId="%s" Version="1.0"
						RuleCombiningAlgId="urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:deny-overrides">
				""".formatted(NAMESPACE, POLICY_ID_PREFIX + slot));
		xml.append(target(RESOURCE_CATEGORY, RESOURCE_ID, "resource-" + slot));
		for(ActionRule rule : RULES){
			xml.append("\t<Rule RuleId=\"").append(POLICY_ID_PREFIX).append(slot).append(":").append(rule.action())
					.append("\" Effect=\"").append(rule.effect()).append("\">\n")
					.append(target(ACTION_CATEGORY, ACTION_ID, rule.action())).append("\t</Rule>\n");
		}
		return xml.append("</Policy>\n").toString();
	}

	private static String target(String category, String attribute, String value){
		return """
				<Target>
					<AnyOf>
						<AllOf>
							<Match MatchId="urn:oasis:names:tc:xacml:1.0:function:string-equal">
								<AttributeValue DataType="http://www.w3.org/2001/XMLSchema#string">%s</AttributeValue>
								<AttributeDesignator Category="%s" AttributeId="%s"
										DataType="http://www.w3.org/2001/XMLSchema#string" MustBePresent="false"/>
							</Match>
						</AllOf>
					</AnyOf>
				</Target>
				""".formatted(value, category, attribute);
	}

	private static String request(String resource, String action){
		return """
				<?xml version="1.0" encoding="UTF-8"?>
				<Request xmlns="%s" ReturnPolicyIdList="false" CombinedDecision="false">
					<Attributes Category="%s">
						<Attribute AttributeId="%s" IncludeInResult="false">
							<AttributeValue DataType="http://www.w3.org/2001/XMLSchema#string">%s</AttributeValue>
						</Attribute>
					</Attributes>
					<Attributes Category="%s">
						<Attribute AttributeId="%s" IncludeInResult="false">
							<AttributeValue DataType="http://www.w3.org/2001/XMLSchema#string">%s</AttributeValue>
						</Attribute>
					</Attributes>
				</Request>
				""".formatted(NAMESPACE, RESOURCE_CATEGORY, RESOURCE_ID, resource, ACTION_CATEGORY, ACTION_ID, action);
	}

	/** The oracle follows only the generated targets and rule table; it never runs a PDP. */
	private static Decision expected(int count, int slot, String action){
		if(slot < 0 || slot >= count){
			return Decision.NOT_APPLICABLE;
		}
		return RULES.stream().filter(rule -> rule.action().equals(action)).map(ActionRule::expected)
				.findFirst().orElse(Decision.NOT_APPLICABLE);
	}

	private record ActionRule(String action, String effect, Decision expected) {}
}
