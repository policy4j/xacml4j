package org.xacml4j.jmh.testing;

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

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.xacml4j.jmh.harness.CorpusRequest;
import org.xacml4j.jmh.harness.Marshalling;
import org.xacml4j.jmh.harness.PolicyCorpus;
import org.xacml4j.jmh.harness.PolicyCorpusProvider;
import org.xacml4j.jmh.harness.PolicySource;
import org.xacml4j.v30.CompositeDecisionRule;
import org.xacml4j.v30.Decision;
import org.xacml4j.v30.SyntaxException;
import org.xacml4j.v30.marshal.MediaType;

/**
 * Small XACML 3.0 corpora for the harness tests. The expected decisions
 * follow from the rules as written (XACML 3.0 core §7.11 Table 4, rule
 * evaluation; Appendix C.2, deny-overrides), not from a PDP run.
 * <ul>
 * <li>Policy {@value #DOCUMENTS_ID} (deny-overrides): action {@code read}
 * permits; {@code delete} denies; {@code audit} permits only for a subject
 * clearance {@code high}, which must be present, so a request without it has
 * an Indeterminate condition (§7.3.5) and the Permit rule is
 * Indeterminate{P} (§7.11), which the response carries as Indeterminate
 * (§7.10); any other action matches no rule and is NotApplicable.</li>
 * <li>A root policy set (deny-overrides) that refers to it with a
 * {@code PolicyIdReference}.</li>
 * </ul>
 */
public final class TestCorpora
{
	public static final MediaType XACML30 = MediaType.Type.XACML30_XML;

	public static final String ROOT_ID = "urn:xacml4j:jmh:test:root";
	public static final String DOCUMENTS_ID = "urn:xacml4j:jmh:test:documents";
	public static final String VERSIONS_ROOT_ID = "urn:xacml4j:jmh:test:versions";
	public static final String VERSIONED_ID = "urn:xacml4j:jmh:test:versioned";
	public static final String SPARE_ID = "urn:xacml4j:jmh:test:spare";

	/** The manifest of {@link #writeDirectory}: three requests, one per decision kind it expects. */
	public static final String MANIFEST = """
			format=xacml30
			root=root.xml
			expect.a-read.xml=Permit
			expect.b-delete.xml=Deny
			expect.c-audit.xml=Indeterminate
			""";

	private TestCorpora(){
	}

	/**
	 * @param name the corpus name
	 * @return the documents policy, its root, and one request per decision:
	 * {@code read} Permit, {@code delete} Deny, {@code audit} Indeterminate,
	 * {@code write} NotApplicable
	 */
	public static PolicyCorpus documents(String name) throws IOException, SyntaxException {
		return new PolicyCorpus(name,
				List.of(source("documents.xml", documentsPolicy()),
						source("root.xml", rootPolicySet(ROOT_ID, policyReference(DOCUMENTS_ID)))),
				"root.xml",
				List.of(request("read", actionRequest("read"), Decision.PERMIT),
						request("delete", actionRequest("delete"), Decision.DENY),
						request("audit", actionRequest("audit"), Decision.INDETERMINATE),
						request("write", actionRequest("write"), Decision.NOT_APPLICABLE)));
	}

	/**
	 * @param name the corpus name
	 * @return versions 1.0 and 2.0 of policy {@value #VERSIONED_ID}, both
	 * permitting {@code read}; a root that refers to it without a version
	 * constraint, so that the reference resolves to 2.0; policy
	 * {@value #SPARE_ID}, which nothing refers to and no request reaches; and
	 * the requests {@code read} (Permit) and {@code write} (NotApplicable)
	 */
	public static PolicyCorpus versioned(String name) throws IOException, SyntaxException {
		return new PolicyCorpus(name,
				List.of(source("root.xml", rootPolicySet(VERSIONS_ROOT_ID, policyReference(VERSIONED_ID))),
						source("versioned-1.0.xml", readPolicy(VERSIONED_ID, "1.0", "first version")),
						source("versioned-2.0.xml", readPolicy(VERSIONED_ID, "2.0", "second version")),
						source("spare.xml", readPolicy(SPARE_ID, "1.0", "referenced by no policy"))),
				"root.xml",
				List.of(request("read", actionRequest("read"), Decision.PERMIT),
						request("write", actionRequest("write"), Decision.NOT_APPLICABLE)));
	}

	/**
	 * Writes the directory corpus of the spec scenario "Valid directory": a
	 * manifest, two policies, and three requests. The files are written out
	 * of name order, so that the corpus has to sort them.
	 *
	 * @param dir an empty directory
	 * @return {@code dir}
	 */
	public static Path writeDirectory(Path dir) throws IOException {
		Path policies = Files.createDirectories(dir.resolve("policies"));
		Path requests = Files.createDirectories(dir.resolve("requests"));
		Files.writeString(policies.resolve("root.xml"), rootPolicySet(ROOT_ID, policyReference(DOCUMENTS_ID)), UTF_8);
		Files.writeString(policies.resolve("documents.xml"), documentsPolicy(), UTF_8);
		Files.writeString(requests.resolve("c-audit.xml"), actionRequest("audit"), UTF_8);
		Files.writeString(requests.resolve("b-delete.xml"), actionRequest("delete"), UTF_8);
		Files.writeString(requests.resolve("a-read.xml"), actionRequest("read"), UTF_8);
		writeManifest(dir, MANIFEST);
		return dir;
	}

	/** Replaces the manifest of a directory corpus. */
	public static void writeManifest(Path dir, String text) throws IOException {
		Files.writeString(dir.resolve("corpus.properties"), text, UTF_8);
	}

	/** @return a policy source of XACML 3.0 text */
	public static PolicySource source(String name, String xml){
		return new PolicySource(name, XACML30, xml.getBytes(UTF_8));
	}

	/** @return a corpus request parsed from XACML 3.0 text */
	public static CorpusRequest request(String id, String xml, Decision expected) throws IOException, SyntaxException {
		return new CorpusRequest(id, Marshalling.requests(XACML30).unmarshal(new ByteArrayInputStream(xml.getBytes(UTF_8))),
				expected);
	}

	/** @return a policy or policy set parsed from XACML 3.0 text */
	public static CompositeDecisionRule rule(String xml) throws IOException, SyntaxException {
		return Marshalling.policies(XACML30).unmarshal(new ByteArrayInputStream(xml.getBytes(UTF_8)));
	}

	/**
	 * @param id the policy set id, version 1.0
	 * @param references reference elements, from {@link #policyReference}
	 * @return a policy set with the deny-overrides policy-combining algorithm
	 */
	public static String rootPolicySet(String id, String... references){
		return """
				<PolicySet xmlns="urn:oasis:names:tc:xacml:3.0:core:schema:wd-17"
						PolicySetId="%s" Version="1.0"
						PolicyCombiningAlgId="urn:oasis:names:tc:xacml:3.0:policy-combining-algorithm:deny-overrides">
					<Target/>
					%s
				</PolicySet>
				""".formatted(id, String.join("\n", references));
	}

	/** @return a {@code PolicyIdReference} without version constraints */
	public static String policyReference(String id){
		return "<PolicyIdReference>" + id + "</PolicyIdReference>";
	}

	/** @return a {@code PolicyIdReference} with a {@code Version} constraint */
	public static String policyReference(String id, String version){
		return "<PolicyIdReference Version=\"" + version + "\">" + id + "</PolicyIdReference>";
	}

	/** @return policy {@value #DOCUMENTS_ID}; see the class comment */
	public static String documentsPolicy(){
		return """
				<Policy xmlns="urn:oasis:names:tc:xacml:3.0:core:schema:wd-17"
						PolicyId="%s" Version="1.0"
						RuleCombiningAlgId="urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:deny-overrides">
					<Target/>
					<Rule RuleId="read" Effect="Permit">
						%s
					</Rule>
					<Rule RuleId="delete" Effect="Deny">
						%s
					</Rule>
					<Rule RuleId="audit" Effect="Permit">
						%s
						<Condition>
							<Apply FunctionId="urn:oasis:names:tc:xacml:1.0:function:string-equal">
								<Apply FunctionId="urn:oasis:names:tc:xacml:1.0:function:string-one-and-only">
									<AttributeDesignator
											Category="urn:oasis:names:tc:xacml:1.0:subject-category:access-subject"
											AttributeId="urn:xacml4j:jmh:test:clearance"
											DataType="http://www.w3.org/2001/XMLSchema#string"
											MustBePresent="true"/>
								</Apply>
								<AttributeValue DataType="http://www.w3.org/2001/XMLSchema#string">high</AttributeValue>
							</Apply>
						</Condition>
					</Rule>
				</Policy>
				""".formatted(DOCUMENTS_ID, actionTarget("read"), actionTarget("delete"), actionTarget("audit"));
	}

	/**
	 * @param id the policy id
	 * @param version the policy version
	 * @param description a description, so that two policies with one id and version can differ
	 * @return a policy whose only rule permits action {@code read}
	 */
	public static String readPolicy(String id, String version, String description){
		return """
				<Policy xmlns="urn:oasis:names:tc:xacml:3.0:core:schema:wd-17"
						PolicyId="%s" Version="%s"
						RuleCombiningAlgId="urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:deny-overrides">
					<Description>%s</Description>
					<Target/>
					<Rule RuleId="read" Effect="Permit">
						%s
					</Rule>
				</Policy>
				""".formatted(id, version, description, actionTarget("read"));
	}

	/** @return a request with one action id */
	public static String actionRequest(String action){
		return """
				<Request xmlns="urn:oasis:names:tc:xacml:3.0:core:schema:wd-17"
						ReturnPolicyIdList="false" CombinedDecision="false">
					%s
				</Request>
				""".formatted(actionAttributes(action));
	}

	/**
	 * @return a request for action {@code read} on two resources: two
	 * {@code Attributes} elements of the resource category, so the PDP
	 * decides each resource and returns two results (XACML 3.0 Multiple
	 * Decision Profile, repeated attribute categories)
	 */
	public static String twoResourceRequest(){
		return """
				<Request xmlns="urn:oasis:names:tc:xacml:3.0:core:schema:wd-17"
						ReturnPolicyIdList="false" CombinedDecision="false">
					%s
					%s
					%s
				</Request>
				""".formatted(actionAttributes("read"), resourceAttributes("doc-1"), resourceAttributes("doc-2"));
	}

	private static String actionTarget(String action){
		return """
				<Target>
					<AnyOf>
						<AllOf>
							<Match MatchId="urn:oasis:names:tc:xacml:1.0:function:string-equal">
								<AttributeValue DataType="http://www.w3.org/2001/XMLSchema#string">%s</AttributeValue>
								<AttributeDesignator
										Category="urn:oasis:names:tc:xacml:3.0:attribute-category:action"
										AttributeId="urn:oasis:names:tc:xacml:1.0:action:action-id"
										DataType="http://www.w3.org/2001/XMLSchema#string"
										MustBePresent="false"/>
							</Match>
						</AllOf>
					</AnyOf>
				</Target>
				""".formatted(action);
	}

	private static String actionAttributes(String action){
		return """
				<Attributes Category="urn:oasis:names:tc:xacml:3.0:attribute-category:action">
					<Attribute AttributeId="urn:oasis:names:tc:xacml:1.0:action:action-id" IncludeInResult="false">
						<AttributeValue DataType="http://www.w3.org/2001/XMLSchema#string">%s</AttributeValue>
					</Attribute>
				</Attributes>
				""".formatted(action);
	}

	private static String resourceAttributes(String resource){
		return """
				<Attributes Category="urn:oasis:names:tc:xacml:3.0:attribute-category:resource">
					<Attribute AttributeId="urn:oasis:names:tc:xacml:1.0:resource:resource-id" IncludeInResult="false">
						<AttributeValue DataType="http://www.w3.org/2001/XMLSchema#string">%s</AttributeValue>
					</Attribute>
				</Attributes>
				""".formatted(resource);
	}

	/**
	 * Corpus {@code probe}: the {@link #documents} corpus, with a record of the
	 * argument of every load. Registered in {@code META-INF/services} of the test classes.
	 */
	public static final class Probe implements PolicyCorpusProvider
	{
		public static final String NAME = "probe";
		private static final List<Optional<String>> ARGUMENTS = Collections.synchronizedList(new ArrayList<>());

		@Override
		public String name(){
			return NAME;
		}

		@Override
		public PolicyCorpus load(Optional<String> argument) throws IOException, SyntaxException {
			ARGUMENTS.add(argument);
			return documents(NAME + argument.map(a -> ":" + a).orElse(""));
		}

		/** @return the arguments of the loads since the last reset, in order */
		public static List<Optional<String>> arguments(){
			synchronized(ARGUMENTS){
				return List.copyOf(ARGUMENTS);
			}
		}

		public static void reset(){
			ARGUMENTS.clear();
		}
	}

	/** One of two corpora named {@code twin}; a lookup of that name must fail. */
	public static final class FirstTwin implements PolicyCorpusProvider
	{
		public static final String NAME = "twin";

		@Override
		public String name(){
			return NAME;
		}

		@Override
		public PolicyCorpus load(Optional<String> argument){
			throw new UnsupportedOperationException("A twin corpus is never selected");
		}
	}

	/** The other corpus named {@code twin}. */
	public static final class SecondTwin implements PolicyCorpusProvider
	{
		@Override
		public String name(){
			return FirstTwin.NAME;
		}

		@Override
		public PolicyCorpus load(Optional<String> argument){
			throw new UnsupportedOperationException("A twin corpus is never selected");
		}
	}
}
