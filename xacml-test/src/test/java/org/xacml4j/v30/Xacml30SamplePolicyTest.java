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


import org.junit.Before;
import org.junit.Test;


public class Xacml30SamplePolicyTest extends XacmlPolicyTestSupport
{
	private PolicyDecisionPoint pdp;

	@Before
	public void init() throws Exception{

		this.pdp = builder(
				"urn:oasis:names:tc:xacml:2.0:conformance-test:IIC058:policy", "1.0")
				.policy(getPolicy("v30-policy-test/test-policy.xml")).build();
	}

	@Test
	public void testPermitResponse() throws Exception {
		verifyXacml30Response(pdp, "v30-policy-test/test-permit-req.xml", "v30-policy-test/test-permit-resp.xml");
	}

	@Test
	public void testDenyResponse() throws Exception {
		verifyXacml30Response(pdp, "v30-policy-test/test-deny-req.xml", "v30-policy-test/test-deny-resp.xml");
	}
}
