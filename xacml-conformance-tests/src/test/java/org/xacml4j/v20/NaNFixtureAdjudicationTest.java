package org.xacml4j.v20;

/*
 * #%L
 * Xacml4J Conformance Tests
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;

import javax.xml.bind.JAXBElement;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.oasis.xacml.v30.jaxb.DecisionType;
import org.oasis.xacml.v30.jaxb.ResponseType;
import org.xacml4j.v30.Decision;
import org.xacml4j.v30.PolicyDecisionPoint;
import org.xacml4j.v30.RequestContext;
import org.xacml4j.v30.ResponseContext;
import org.xacml4j.v30.Result;
import org.xacml4j.v30.marshal.MarshallingProvider;
import org.xacml4j.v30.marshal.MediaType;
import org.xacml4j.v30.marshal.ResponseMarshaller;
import org.xacml4j.v30.types.DoubleVal;

/**
 * Characterization and adjudication safety-net tests for experimental
 * OASIS XACML 3.0 NaN conformance test cases (IIC350 and IIC358).
 *
 * <p>Under IEEE 754-1985 / XACML 3.0 &sect;A.3.1 objective specification compliance:
 * <ul>
 *   <li>{@code double-equal(NaN, NaN)} evaluates to {@code false}.</li>
 *   <li>The condition of IIC350 evaluates to {@code false}, so the rule does not apply.
 *       With an empty Target and deny-overrides algorithm, the policy evaluates to
 *       {@link Decision#NOT_APPLICABLE}.</li>
 *   <li>In IIC358, {@code double-add(NaN, 1.0)} produces {@code NaN}, which then does not
 *       equal {@code NaN} under {@code double-equal}, also yielding
 *       {@link Decision#NOT_APPLICABLE}.</li>
 *   <li>The vendored response fixtures ({@code IIC350Response.xml} and {@code IIC358Response.xml})
 *       remain immutable and expect {@code Permit} from the experimental OASIS contribution.</li>
 *   <li>Cases {@code oasis20/IIC020} and {@code oasis30/IIC020} must both be enabled as
 *       {@code executed,true} following the numeric integer-divide type fix.</li>
 * </ul>
 */
public class NaNFixtureAdjudicationTest
{
	private static final Path MANIFEST_PATH = Path.of("src/test/manifest/conformance-manifest.csv");

	private static ConformanceManifest.Case findCase(String suite, String caseId) throws Exception
	{
		return ConformanceManifest.read(MANIFEST_PATH).stream()
				.filter(c -> suite.equals(c.suite()) && caseId.equals(c.caseId()))
				.findFirst()
				.orElseThrow(() -> new AssertionError("Missing manifest case: " + suite + "/" + caseId));
	}

	@Test
	@DisplayName("IIC350 policy and PDP decide NotApplicable under IEEE double-equal")
	public void iic350PolicyAndPdpDecidesNotApplicable() throws Exception
	{
		ConformanceManifest.Case testCase = findCase("oasis30", "IIC350");
		PolicyDecisionPoint pdp = ConformanceCaseRunner.buildPdp(testCase);
		RequestContext request = Xacml30TestUtility.getRequest(testCase.requestFile());
		ResponseContext response = pdp.decide(request);

		assertNotNull(response, "IIC350 evaluation must produce a response");
		assertEquals(1, response.getResults().size(), "IIC350 must produce exactly one result");
		Result result = response.getResults().iterator().next();
		assertEquals(Decision.NOT_APPLICABLE, result.getDecision(),
				"Under IEEE 754 (§A.3.1), double-equal(NaN, NaN) is false, yielding NotApplicable");
		assertAdjudicatedResponse(testCase, response);
	}

	@Test
	@DisplayName("IIC358 policy and PDP decide NotApplicable under IEEE double-equal")
	public void iic358PolicyAndPdpDecidesNotApplicable() throws Exception
	{
		ConformanceManifest.Case testCase = findCase("oasis30", "IIC358");
		PolicyDecisionPoint pdp = ConformanceCaseRunner.buildPdp(testCase);
		RequestContext request = Xacml30TestUtility.getRequest(testCase.requestFile());
		ResponseContext response = pdp.decide(request);

		assertNotNull(response, "IIC358 evaluation must produce a response");
		assertEquals(1, response.getResults().size(), "IIC358 must produce exactly one result");
		Result result = response.getResults().iterator().next();
		assertEquals(Decision.NOT_APPLICABLE, result.getDecision(),
				"Under IEEE 754 (§A.3.1), double-equal(double-add(NaN, 1.0), NaN) is false, yielding NotApplicable");
		assertAdjudicatedResponse(testCase, response);
	}

	@Test
	@DisplayName("NaN lexical parsing and arithmetic are observed independently of equality predicates")
	public void nanParsingAndArithmeticObservedIndependently() throws Exception
	{
		// Production parsing of lexical "NaN" into DoubleVal
		DoubleVal parsed = DoubleVal.of("NaN");
		assertNotNull(parsed);
		assertTrue(Double.isNaN(parsed.get()), "DoubleVal.of(\"NaN\") must hold Double.NaN");
		assertEquals("NaN", parsed.toXacmlString(), "DoubleVal.toXacmlString must format NaN as 'NaN'");

		// Production DoubleVal factory from double
		DoubleVal fromDouble = DoubleVal.of(Double.NaN);
		assertTrue(Double.isNaN(fromDouble.get()), "DoubleVal.of(Double.NaN) must hold Double.NaN");

		// Production DoubleVal arithmetic: NaN + 1.0 is NaN
		DoubleVal one = DoubleVal.of(1.0);
		DoubleVal sum = parsed.add(one);
		assertTrue(Double.isNaN(sum.get()), "NaN + 1.0 must produce NaN in DoubleVal arithmetic");
		assertEquals("NaN", sum.toXacmlString(), "sum of NaN + 1.0 must serialize to 'NaN'");

		// Request attribute unmarshalling for IIC350 and IIC358
		RequestContext req350 = Xacml30TestUtility.getRequest("xacml3.0-ct-v.0.4/IIC350Request.xml");
		assertNotNull(req350, "IIC350 request must unmarshal successfully");

		RequestContext req358 = Xacml30TestUtility.getRequest("xacml3.0-ct-v.0.4/IIC358Request.xml");
		assertNotNull(req358, "IIC358 request must unmarshal successfully");
	}

	@Test
	@DisplayName("Vendored response fixtures for IIC350 and IIC358 remain immutable and expect Permit")
	public void sourceResponsesRemainPermitInVendoredFixtures() throws Exception
	{
		// Vendored response IIC350Response.xml expects Permit from experimental OASIS contribution
		ResponseType response350 = Xacml30TestUtility.getResponse("xacml3.0-ct-v.0.4/IIC350Response.xml");
		assertNotNull(response350);
		assertEquals(1, response350.getResult().size(), "Vendored IIC350Response.xml must have 1 result");
		assertEquals(DecisionType.PERMIT, response350.getResult().get(0).getDecision(),
				"Vendored IIC350 fixture expects Permit");

		// Vendored response IIC358Response.xml expects Permit from experimental OASIS contribution
		ResponseType response358 = Xacml30TestUtility.getResponse("xacml3.0-ct-v.0.4/IIC358Response.xml");
		assertNotNull(response358);
		assertEquals(1, response358.getResult().size(), "Vendored IIC358Response.xml must have 1 result");
		assertEquals(DecisionType.PERMIT, response358.getResult().get(0).getDecision(),
				"Vendored IIC358 fixture expects Permit");
	}

	@Test
	@DisplayName("IIC020 must be enabled in both oasis20 and oasis30 suites")
	public void iic020MustBeEnabledInBothSuites() throws Exception
	{
		ConformanceManifest.Case iic020Oasis20 = findCase("oasis20", "IIC020");
		assertNotNull(iic020Oasis20, "oasis20/IIC020 must exist in manifest");
		assertTrue(iic020Oasis20.enabled(), "oasis20/IIC020 must be enabled (executed,true)");
		assertEquals("executed", iic020Oasis20.disposition(), "oasis20/IIC020 disposition must be executed");

		ConformanceManifest.Case iic020Oasis30 = findCase("oasis30", "IIC020");
		assertNotNull(iic020Oasis30, "oasis30/IIC020 must exist in manifest");
		assertTrue(iic020Oasis30.enabled(), "oasis30/IIC020 must be enabled (executed,true)");
		assertEquals("executed", iic020Oasis30.disposition(), "oasis30/IIC020 disposition must be executed");
	}

	@Test
	@DisplayName("expectedResponse30 adjudicates targeted NaN fixtures to NotApplicable while preserving response structure")
	public void expectedResponse30AdjudicatesTargetedNanCases() throws Exception
	{
		ConformanceManifest.Case case350 = findCase("oasis30", "IIC350");
		assertEquals("special-harness", case350.disposition());
		assertTrue(case350.enabled());
		ResponseType adjudicated350 = ConformanceCaseRunner.expectedResponse30(case350);
		assertNotNull(adjudicated350);
		assertEquals(1, adjudicated350.getResult().size());
		assertEquals(DecisionType.NOT_APPLICABLE, adjudicated350.getResult().get(0).getDecision(),
				"IIC350 expected response must be adjudicated to NotApplicable");
		assertEquals("urn:oasis:names:tc:xacml:1.0:status:ok",
				adjudicated350.getResult().get(0).getStatus().getStatusCode().getValue(),
				"IIC350 status code must be preserved as ok");

		ConformanceManifest.Case case358 = findCase("oasis30", "IIC358");
		assertEquals("special-harness", case358.disposition());
		assertTrue(case358.enabled());
		ResponseType adjudicated358 = ConformanceCaseRunner.expectedResponse30(case358);
		assertNotNull(adjudicated358);
		assertEquals(1, adjudicated358.getResult().size());
		assertEquals(DecisionType.NOT_APPLICABLE, adjudicated358.getResult().get(0).getDecision(),
				"IIC358 expected response must be adjudicated to NotApplicable");
		assertEquals("urn:oasis:names:tc:xacml:1.0:status:ok",
				adjudicated358.getResult().get(0).getStatus().getStatusCode().getValue(),
				"IIC358 status code must be preserved as ok");
	}

	@Test
	@DisplayName("expectedResponse30 preserves negative-control cases without adjudication")
	public void expectedResponse30PreservesNegativeControlCases() throws Exception
	{
		// Negative control 1: oasis30/IIC353 (fixture already expects NotApplicable, disposition executed)
		ConformanceManifest.Case case353 = findCase("oasis30", "IIC353");
		assertEquals("executed", case353.disposition());
		ResponseType resp353 = ConformanceCaseRunner.expectedResponse30(case353);
		assertNotNull(resp353);
		assertEquals(1, resp353.getResult().size());
		assertEquals(DecisionType.NOT_APPLICABLE, resp353.getResult().get(0).getDecision(),
				"IIC353 already expects NotApplicable and must remain unadjudicated");

		// Negative control 2: oasis30/IIC001 (standard Permit case, disposition executed)
		ConformanceManifest.Case case001 = findCase("oasis30", "IIC001");
		assertEquals("executed", case001.disposition());
		ResponseType resp001 = ConformanceCaseRunner.expectedResponse30(case001);
		assertNotNull(resp001);
		assertEquals(1, resp001.getResult().size());
		assertEquals(DecisionType.PERMIT, resp001.getResult().get(0).getDecision(),
				"IIC001 expects Permit and must NOT be adjudicated");

		// Negative control 3: oasis30/IIC020 (newly re-enabled Permit case, disposition executed)
		ConformanceManifest.Case case020 = findCase("oasis30", "IIC020");
		assertEquals("executed", case020.disposition());
		ResponseType resp020 = ConformanceCaseRunner.expectedResponse30(case020);
		assertNotNull(resp020);
		assertEquals(1, resp020.getResult().size());
		assertEquals(DecisionType.PERMIT, resp020.getResult().get(0).getDecision(),
				"IIC020 expects Permit and must NOT be adjudicated");
	}

	@Test
	@DisplayName("expectedResponse30 guard binds adjudication to the named fixture")
	public void expectedResponse30GuardRejectsDifferentFixture() throws Exception
	{
		// oasis30/IIC353 expected response is NotApplicable, not Permit.
		// If artificially marked special-harness for IIC350, the guard should trigger.
		ConformanceManifest.Case case353 = findCase("oasis30", "IIC353");
		ConformanceManifest.Case syntheticCase = new ConformanceManifest.Case(
				"oasis30", "IIC350", case353.requestFile(), case353.policyFiles(),
				case353.expectedResponseFile(), "special-harness", true, "test", "test");

		IllegalStateException ex = assertThrows(
				IllegalStateException.class,
				() -> ConformanceCaseRunner.expectedResponse30(syntheticCase));
		assertTrue(ex.getMessage().contains("Unexpected response fixture"));
	}

	@Test
	public void eachAdjudicationConditionIndependentlyPreservesPermitWhenAbsent() throws Exception
	{
		ConformanceManifest.Case original = findCase("oasis30", "IIC350");
		String[][] controls = {{"oasis20", "IIC350", "special-harness"},
				{"oasis30", "IIC350", "executed"}, {"oasis30", "IIC001", "special-harness"}};
		for (String[] control : controls) {
			ConformanceManifest.Case synthetic = new ConformanceManifest.Case(
					control[0], control[1], original.requestFile(), original.policyFiles(),
					original.expectedResponseFile(), control[2], true, "test", "test");
			assertEquals(DecisionType.PERMIT,
					ConformanceCaseRunner.expectedResponse30(synthetic).getResult().get(0).getDecision(),
					String.join("/", control));
		}
	}

	@SuppressWarnings("unchecked")
	private static void assertAdjudicatedResponse(ConformanceManifest.Case testCase, ResponseContext response)
			throws Exception
	{
		ResponseMarshaller marshaller = MarshallingProvider.discoverProviders()
				.get(MediaType.Type.XACML30_XML).newResponseMarshaller().orElseThrow();
		ResponseType actual = ((JAXBElement<ResponseType>) marshaller.marshal(response)).getValue();
		Xacml30TestUtility.assertResponse(ConformanceCaseRunner.expectedResponse30(testCase), actual);
	}

	@Test
	public void sameDecisionInAnotherFixtureCannotBeAdjudicated() throws Exception
	{
		ConformanceManifest.Case original = findCase("oasis30", "IIC350");
		ConformanceManifest.Case other = findCase("oasis30", "IIC001");
		ConformanceManifest.Case synthetic = new ConformanceManifest.Case(
				original.suite(), original.caseId(), original.requestFile(), original.policyFiles(),
				other.expectedResponseFile(), original.disposition(), true, "test", "test");
		assertThrows(IllegalStateException.class, () -> ConformanceCaseRunner.expectedResponse30(synthetic));
	}
}
