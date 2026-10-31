package org.xacml4j.v30.spi.repository;

/*
 * #%L
 * Xacml4J PDP
 * %%
 * Copyright (C) 2009 - 2026 Xacml4J.org
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.xacml4j.v30.Attribute;
import org.xacml4j.v30.Category;
import org.xacml4j.v30.CompositeDecisionRule;
import org.xacml4j.v30.Decision;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.PolicyDecisionPoint;
import org.xacml4j.v30.RequestContext;
import org.xacml4j.v30.Result;
import org.xacml4j.v30.StatusCodeId;
import org.xacml4j.v30.marshal.MarshallingProvider;
import org.xacml4j.v30.marshal.MediaType;
import org.xacml4j.v30.pdp.PolicyDecisionPointBuilder;
import org.xacml4j.v30.policy.FunctionSpec;
import org.xacml4j.v30.policy.function.FunctionProvider;
import org.xacml4j.v30.policy.function.impl.RegExpFunctions;
import org.xacml4j.v30.policy.function.regex.CompiledRegex;
import org.xacml4j.v30.policy.function.regex.RegexEngine;
import org.xacml4j.v30.policy.function.regex.RegexEngineProvider;
import org.xacml4j.v30.policy.function.regex.RegexEngines;
import org.xacml4j.v30.policy.function.regex.RegexLimits;
import org.xacml4j.v30.policy.function.regex.RegexProfile;
import org.xacml4j.v30.spi.pip.PolicyInformationPoint;
import org.xacml4j.v30.types.BooleanVal;
import org.xacml4j.v30.types.Entity;
import org.xacml4j.v30.types.StringVal;

/**
 * The selection of the regular-expression engine and profile per function provider and policy
 * repository (change add-regex-engine-spi, design D3, maintainer decision 1 of 2026-10-28): an
 * unknown engine or profile fails when the provider is built, two providers of one engine name
 * fail its resolution, and the engine of a repository evaluates the regexp functions of every
 * policy the repository loads, also through its import tool.
 */
class RegexEngineSelectionTest
{
	private static final String STRING_REGEXP_MATCH = "urn:oasis:names:tc:xacml:1.0:function:string-regexp-match";
	private static final String SUBJECT_ID = "urn:oasis:names:tc:xacml:1.0:subject:subject-id";
	/** U+0663 ARABIC-INDIC DIGIT THREE: a digit of category Nd that is not an ASCII digit (design D2) */
	private static final String ARABIC_THREE = "٣";

	/** A policy that permits a subject whose id holds a {@code \d}: ASCII only in legacy, every Nd in xf */
	private static final String DIGIT_POLICY = """
			<Policy xmlns="urn:oasis:names:tc:xacml:3.0:core:schema:wd-17" PolicyId="digit" Version="1.0"
			        RuleCombiningAlgId="urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:deny-overrides">
			    <Target/>
			    <Rule Effect="Permit" RuleId="digit-rule">
			        <Condition>
			            <Apply FunctionId="urn:oasis:names:tc:xacml:1.0:function:string-regexp-match">
			                <AttributeValue DataType="http://www.w3.org/2001/XMLSchema#string">\\d</AttributeValue>
			                <Apply FunctionId="urn:oasis:names:tc:xacml:1.0:function:string-one-and-only">
			                    <AttributeDesignator AttributeId="urn:oasis:names:tc:xacml:1.0:subject:subject-id"
			                            Category="urn:oasis:names:tc:xacml:1.0:subject-category:access-subject"
			                            DataType="http://www.w3.org/2001/XMLSchema#string" MustBePresent="true"/>
			                </Apply>
			            </Apply>
			        </Condition>
			    </Rule>
			</Policy>
			""";

	/**
	 * Load the XF profile before the selection scenarios.
	 */
	@BeforeAll
	static void loadTheXfProfile()
	{
		RegExpFunctions.Matching xf = new RegExpFunctions.Matching(RegexEngines.engine("jdk"), RegexProfile.XF,
				RegexLimits.defaults());
		assertTrue(xf.matches("\\d", ARABIC_THREE));
	}

	@Test
	void anUnknownEngineFailsWhenTheProviderIsBuilt()
	{
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
				() -> FunctionProvider.builder().withRegexEngine("none", "legacy").withDefaultFunctions().build());

		assertTrue(e.getMessage().contains("\"none\""), e::getMessage);
		assertTrue(e.getMessage().contains("jdk"), e::getMessage);
		assertThrows(IllegalArgumentException.class,
				() -> FunctionProvider.builder().withRegexEngine("none", "legacy").build());
	}

	@Test
	void anUnknownProfileFailsWhenTheProviderIsBuilt()
	{
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
				() -> FunctionProvider.builder().withRegexEngine("jdk", "nope").withDefaultFunctions().build());

		assertTrue(e.getMessage().contains("\"nope\""), e::getMessage);
		assertTrue(e.getMessage().contains("legacy") && e.getMessage().contains("xf"), e::getMessage);
	}

	/** The spec scenario names re2j with legacy; here an engine that offers xf only stands for it */
	@Test
	void aProfileTheEngineDoesNotOfferFailsWhenTheProviderIsBuilt()
	{
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
				() -> FunctionProvider.builder().withRegexEngine(new XfOnlyEngine(), RegexProfile.LEGACY)
						.withDefaultFunctions().build());

		assertTrue(e.getMessage().contains("xf-only"), e::getMessage);
		assertTrue(e.getMessage().contains("legacy"), e::getMessage);
	}

	@Test
	void theRepositoryBuilderRejectsAnUnknownEngine()
	{
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
				() -> PolicyRepository.build("unknown-engine").withRegexEngine("none", "legacy").build());

		assertTrue(e.getMessage().contains("\"none\""), e::getMessage);
	}

	/** Two providers named {@code twin} are registered in the test resources of this module */
	@Test
	void twoProvidersOfOneEngineNameFailItsResolution()
	{
		IllegalStateException e = assertThrows(IllegalStateException.class, () -> RegexEngines.engine("twin"));

		assertTrue(e.getMessage().contains("\"twin\""), e::getMessage);
		assertTrue(e.getMessage().contains(FirstTwin.class.getName()), e::getMessage);
		assertTrue(e.getMessage().contains(SecondTwin.class.getName()), e::getMessage);
		assertThrows(IllegalStateException.class,
				() -> FunctionProvider.builder().withRegexEngine("twin", "xf").withDefaultFunctions().build());
	}

	/** The properties are read when the provider is built, so a provider built later sees a change */
	@Test
	void thePropertiesSelectTheEngineAndProfileOfTheStandardFunctions()
	{
		String property = RegExpFunctions.PROFILE_PROPERTY;
		String before = System.getProperty(property);
		try{
			System.setProperty(property, "xf");
			assertEquals(BooleanVal.of(true), stringRegexpMatch(FunctionProvider.builder().withDefaultFunctions().build(),
					"\\d", ARABIC_THREE));
			System.setProperty(property, "nope");
			assertThrows(IllegalArgumentException.class, () -> FunctionProvider.builder().withDefaultFunctions().build());
			System.clearProperty(property);
			assertEquals(BooleanVal.of(false), stringRegexpMatch(FunctionProvider.builder().withDefaultFunctions().build(),
					"\\d", ARABIC_THREE));
		}finally{
			if(before == null){
				System.clearProperty(property);
			}else{
				System.setProperty(property, before);
			}
		}
	}

	@Test
	void providersKeepTheLimitsInEffectWhenTheyWereBuilt()
	{
		String property = RegExpFunctions.class.getName() + ".maxInputLength";
		String before = System.getProperty(property);
		try{
			System.setProperty(property, "1");
			FunctionProvider first = FunctionProvider.builder().withDefaultFunctions().build();
			System.setProperty(property, "2");
			FunctionProvider second = FunctionProvider.builder().withDefaultFunctions().build();
			EvaluationException failure = assertThrows(EvaluationException.class,
					() -> stringRegexpMatch(first, "a", "aa"));
			assertEquals(StatusCodeId.STATUS_PROCESSING_ERROR, failure.getStatus().getStatusCode().getValue());
			assertEquals(BooleanVal.of(true), stringRegexpMatch(second, "a", "aa"));
			assertThrows(EvaluationException.class, () -> stringRegexpMatch(first, "a", "aa"));
		}finally{
			if(before == null) System.clearProperty(property);
			else System.setProperty(property, before);
		}
	}

	@Test
	void explicitEngineWorksWhenTheDefaultPropertyIsInvalid() throws Exception
	{
		String property = RegExpFunctions.PROFILE_PROPERTY;
		String before = System.getProperty(property);
		try{
			System.setProperty(property, "invalid");
			assertTrue(MarshallingProvider.getProvider(MediaType.Type.XACML30_XML).isPresent());
			FunctionProvider functions = FunctionProvider.builder().withDefaultFunctions()
					.withRegexEngine("jdk", "legacy").build();
			PolicyRepository repository = PolicyRepository.build("explicit").withFunctionProvider(functions).build();
			assertDecision(Decision.PERMIT, decide(repository, DIGIT_POLICY, "3"));
			assertThrows(IllegalArgumentException.class,
					() -> FunctionProvider.builder().withDefaultFunctions().build());
		}finally{
			if(before == null) System.clearProperty(property);
			else System.setProperty(property, before);
		}
	}

	/** Spec scenario "Two repositories": the policies of each repository match with its own engine and profile */
	@Test
	void twoRepositoriesEvaluateWithTheirOwnEngines() throws Exception
	{
		PolicyRepository legacy = PolicyRepository.build("legacy").withRegexEngine("jdk", "legacy").build();
		PolicyRepository xf = PolicyRepository.build("xf").withRegexEngine("jdk", "xf").build();

		assertDecision(Decision.NOT_APPLICABLE, decide(legacy, DIGIT_POLICY, ARABIC_THREE));
		assertDecision(Decision.PERMIT, decide(xf, DIGIT_POLICY, ARABIC_THREE));
		assertDecision(Decision.PERMIT, decide(legacy, DIGIT_POLICY, "3"));
	}

	/**
	 * The import tool unmarshals with the functions of its repository, not with the static default:
	 * an engine that finds every pattern permits a subject that no real pattern of the policy matches
	 */
	@Test
	void theImportToolLoadsPoliciesWithTheFunctionsOfItsRepository() throws Exception
	{
		FunctionProvider everything = FunctionProvider.builder()
				.withRegexEngine(new FindingEverything(), RegexProfile.XF, RegexLimits.defaults())
				.withDefaultFunctions()
				.build();
		PolicyRepository repository = PolicyRepository.build("finding-everything").withFunctionProvider(everything).build();

		assertDecision(Decision.PERMIT, decide(repository, DIGIT_POLICY, "no digit here"));
		assertDecision(Decision.NOT_APPLICABLE, decide(PolicyRepository.build("default").build(), DIGIT_POLICY, "no digit here"));
	}

	private static void assertDecision(Decision expected, Result result)
	{
		assertEquals(expected, result.getDecision(), () -> {
			StringBuilder text = new StringBuilder(result.getStatus().toString());
			for(Throwable error = result.getStatus().getError().orElse(null); error != null; error = error.getCause()){
				text.append("\n  caused by ").append(error);
				if(error.getCause() == error){
					break;
				}
			}
			return text.toString();
		});
	}

	@Test
	void aRepositoryTellsItsFunctionProvider()
	{
		FunctionProvider functions = FunctionProvider.builder().withDefaultFunctions().build();

		PolicyRepository repository = PolicyRepository.build("mine").withFunctionProvider(functions).build();

		assertTrue(repository.getFunctionProvider() == functions);
		assertFalse(PolicyRepository.build("own").build().getFunctionProvider() == functions);
	}

	private static Result decide(PolicyRepository repository, String policy, String subjectId) throws Exception
	{
		CompositeDecisionRule root = repository.newImportTool().importPolicy(MediaType.Type.XACML30_XML,
				() -> new ByteArrayInputStream(policy.getBytes(StandardCharsets.UTF_8)));
		RequestContext request = RequestContext.builder()
				.attributes(Category.subjectAccess().entity(Entity.builder()
						.attribute(Attribute.builder(SUBJECT_ID).value(StringVal.of(subjectId)).build()).build()).build())
				.build();
		try(PolicyDecisionPoint pdp = PolicyDecisionPointBuilder.builder("selection-" + repository.getId())
				.policyRepository(repository)
				.rootPolicy(root)
				.pip(mock(PolicyInformationPoint.class))
				.build()){
			Collection<Result> results = pdp.decide(request).getResults();
			assertEquals(1, results.size());
			return results.iterator().next();
		}
	}

	private static BooleanVal stringRegexpMatch(FunctionProvider functions, String pattern, String input)
	{
		FunctionSpec function = functions.getFunction(STRING_REGEXP_MATCH).orElseThrow();
		return function.invoke(mock(EvaluationContext.class), StringVal.of(pattern), StringVal.of(input));
	}

	/** An engine of the xf profile only, as re2j is */
	static final class XfOnlyEngine implements RegexEngine
	{
		@Override
		public String name(){
			return "xf-only";
		}

		@Override
		public Set<RegexProfile> profiles(){
			return Set.of(RegexProfile.XF);
		}

		@Override
		public CompiledRegex compile(String pattern, RegexProfile profile, RegexLimits limits){
			return (input, budget) -> false;
		}
	}

	/** An engine whose every pattern finds every input */
	static final class FindingEverything implements RegexEngine
	{
		@Override
		public String name(){
			return "finding-everything";
		}

		@Override
		public Set<RegexProfile> profiles(){
			return Set.of(RegexProfile.XF);
		}

		@Override
		public CompiledRegex compile(String pattern, RegexProfile profile, RegexLimits limits){
			return (input, budget) -> true;
		}
	}

	/** One of two providers of the engine name {@code twin}, registered through {@code META-INF/services} */
	public static final class FirstTwin implements RegexEngineProvider
	{
		@Override
		public String name(){
			return "twin";
		}

		@Override
		public RegexEngine create(){
			return new XfOnlyEngine();
		}
	}

	/** The other provider of the engine name {@code twin} */
	public static final class SecondTwin implements RegexEngineProvider
	{
		@Override
		public String name(){
			return "twin";
		}

		@Override
		public RegexEngine create(){
			return new XfOnlyEngine();
		}
	}
}
