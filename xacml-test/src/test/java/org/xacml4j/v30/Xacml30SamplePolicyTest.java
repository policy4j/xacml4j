package org.xacml4j.v30;

/*
 * #%L
 * Xacml4J Policy Unit Test Support
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
