package com.artagon.xacml.v30;


import java.io.InputStream;

import org.junit.Test;

import com.artagon.xacml.v30.core.AttributeCategories;
import com.artagon.xacml.v30.core.Version;
import com.artagon.xacml.v30.pdp.PolicyDecisionPoint;
import com.artagon.xacml.v30.spi.pip.AttributeResolver;
import com.artagon.xacml.v30.types.BooleanType;

public class Xacml30SamplePolicyTest extends XacmlPolicyTestSupport {
	@Test
	public void testCPNICompliance() throws Exception {
		PolicyDecisionPoint pdp = buildPDP(
			new AttributeResolver [] {
				createAttributeResolverFor(
						AttributeCategories.SUBJECT_ACCESS,
						createAttribute("urn:comcast:names:1.0:subscriber:residential:cpni-secret-compliant", BooleanType.BOOLEAN, true))
			},
			"urn:cima:policy:compliance:cpni", Version.parse("1.0"));

		verifyResponse(pdp, "v30-policy-test/test-req.xml", "v30-policy-test/test-resp.xml");
	}

	
	@Override
	protected InputStream[] getDefaultTestPolicies() throws Exception {
		return new InputStream [] {
				getPolicy("v30-policy-test/policyset.xml")
			};
	}

	@Test
	public void testCPNIComplianceAttrsInRequest() throws Exception {
		PolicyDecisionPoint pdp = buildPDP("urn:cima:policy:compliance:cpni", Version.parse("1.0"));

		verifyResponse(pdp, "v30-policy-test/test-req2.xml", "v30-policy-test/test-resp2.xml");
	}
}
