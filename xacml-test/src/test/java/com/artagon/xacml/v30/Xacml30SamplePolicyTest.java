package com.artagon.xacml.v30;


import org.junit.Test;

import com.artagon.xacml.v30.pdp.PolicyDecisionPoint;
import com.artagon.xacml.v30.types.BooleanType;

public class Xacml30SamplePolicyTest extends XacmlPolicyTestSupport {
	@Test
	public void testCPNICompliance() throws Exception {
		PolicyDecisionPoint pdp = buildPDP(
			new String [] {
				"v30-policy-test/policyset.xml"
			},
			"urn:cima:policy:compliance:cpni", Version.parse("1.0"),
			createAttributeResolverFor(
					AttributeCategories.SUBJECT_ACCESS,
					createAttribute("urn:comcast:names:1.0:subscriber:residential:cpni-secret-compliant", BooleanType.BOOLEAN, true)));

		RequestContext req = getRequest("v30-policy-test/test-req.xml");
		ResponseContext expectedResponse = getResponse("v30-policy-test/test-resp.xml");
		ResponseContext resp = pdp.decide(req);

		assertResponse(expectedResponse, resp);
	}
}
