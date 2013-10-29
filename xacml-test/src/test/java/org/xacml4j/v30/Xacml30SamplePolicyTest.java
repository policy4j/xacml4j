package org.xacml4j.v30;


import org.junit.Test;
import org.xacml4j.v30.pdp.PolicyDecisionPoint;
import org.xacml4j.v30.types.BooleanType;


public class Xacml30SamplePolicyTest extends XacmlPolicyTestSupport
{
	@Test
	public void testCPNICompliance() throws Exception {
		PolicyDecisionPoint pdp = builder("urn:cima:policy:compliance:cpni", "1.0")
				.withResolver(ExpectedAttributeResolverBuilder
								.builder("test", AttributeCategories.SUBJECT_ACCESS)
									.withAttributeValue("urn:comcast:names:1.0:subscriber:residential:cpni-secret-compliant", BooleanType.BOOLEAN.create(true))
									.build())
					.withPolicyFromClasspath("v30-policy-test/policyset.xml")
					.build();

		verifyXacml30Response(pdp, "v30-policy-test/test-req.xml", "v30-policy-test/test-resp.xml");
	}

	@Test
	public void testCPNIComplianceAttrsInRequest() throws Exception {
		PolicyDecisionPoint pdp = builder("urn:cima:policy:compliance:cpni", "1.0")
				.withPolicyFromClasspath("v30-policy-test/policyset.xml")
				.build();
		verifyXacml30Response(pdp, "v30-policy-test/test-req2.xml", "v30-policy-test/test-resp2.xml");
	}
}
