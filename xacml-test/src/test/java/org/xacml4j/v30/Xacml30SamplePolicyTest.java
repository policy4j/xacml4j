package org.xacml4j.v30;


import org.junit.Test;
import org.xacml4j.v30.pdp.PolicyDecisionPoint;
import org.xacml4j.v30.types.BooleanExp;


public class Xacml30SamplePolicyTest extends XacmlPolicyTestSupport
{
	@Test
	public void testCPNICompliance() throws Exception {
		PolicyDecisionPoint pdp = builder("urn:cima:policy:compliance:cpni", "1.0")
				.resolver(ExpectedAttributeResolverBuilder
								.builder("test", Categories.SUBJECT_ACCESS)
									.value("urn:comcast:names:1.0:subscriber:residential:cpni-secret-compliant", BooleanExp.valueOf(true))
									.build())
					.policyFromClasspath("v30-policy-test/policyset.xml")
					.build();

		verifyXacml30Response(pdp, "v30-policy-test/test-req.xml", "v30-policy-test/test-resp.xml");
	}

	@Test
	public void testCPNIComplianceAttrsInRequest() throws Exception {
		PolicyDecisionPoint pdp = builder("urn:cima:policy:compliance:cpni", "1.0")
				.policyFromClasspath("v30-policy-test/policyset.xml")
				.build();
		verifyXacml30Response(pdp, "v30-policy-test/test-req2.xml", "v30-policy-test/test-resp2.xml");
	}
}
