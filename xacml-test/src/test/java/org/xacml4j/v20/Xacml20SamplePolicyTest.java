package org.xacml4j.v20;

import org.junit.Before;
import org.junit.Test;
import org.xacml4j.v30.XacmlPolicyTestSupport;
import org.xacml4j.v30.pdp.PolicyDecisionPoint;


public class Xacml20SamplePolicyTest extends XacmlPolicyTestSupport
{
	private PolicyDecisionPoint pdp;
	
	@Before
	public void init() throws Exception{
		
		this.pdp = builder(
				"urn:oasis:names:tc:xacml:2.0:conformance-test:IIC058:policy", "1.0")
				.withPolicy(getPolicy("v20-policy-test/test-policy.xml")).build();
	}
	
	@Test
	public void testResponse1() throws Exception {
		verifyXacml20Response(pdp, "v20-policy-test/test-req.xml", "v20-policy-test/test-res.xml");
	}
	
}
