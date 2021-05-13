package org.xacml4j.spring;

/*
 * #%L
 * Xacml4J Spring 3.x Support Module
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


import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertNotNull;

import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.xacml4j.v30.Categories;
import org.xacml4j.v30.AttributeDesignatorKey;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.pdp.PolicyDecisionPoint;
import org.xacml4j.v30.pdp.PolicySet;
import org.xacml4j.v30.spi.pip.PolicyInformationPoint;
import org.xacml4j.v30.spi.pip.ResolverRegistry;
import org.xacml4j.v30.types.XacmlTypes;


@ContextConfiguration(locations={"classpath:/spring-test.xml"})
public class SpringTest extends AbstractJUnit4SpringContextTests
{
	@Autowired
	private PolicyDecisionPoint pdp;

	@Autowired
	private ResolverRegistry resolverRegistry;

	private IMocksControl c;

	@Autowired
	private PolicyInformationPoint pip;

	@Before
	public void init(){
		c = createControl();
	}

	@Test
	public void testProviders()
	{
		assertNotNull(pdp);
		assertNotNull(resolverRegistry);
		assertNotNull(pip);
	}

	@Test
	public void testPolicyBoundResolvers() throws Exception
	{
		EvaluationContext policyContext = c.createMock(EvaluationContext.class);
		EvaluationContext parentContext = c.createMock(EvaluationContext.class);
		PolicySet p = c.createMock(PolicySet.class);

		expect(policyContext.getCurrentPolicy()).andReturn(null);
		expect(policyContext.getCurrentPolicySet()).andReturn(p);
		expect(p.getId()).andReturn("testId");
		expect(policyContext.getParentContext()).andReturn(parentContext);
		expect(parentContext.getCurrentPolicy()).andReturn(null);
		expect(parentContext.getCurrentPolicySet()).andReturn(null);
		expect(parentContext.getParentContext()).andReturn(null);
		c.replay();
		resolverRegistry.getMatchingAttributeResolvers(policyContext, AttributeDesignatorKey.builder()
				.category(Categories.parse("subject"))
				.attributeId("testId1")
				.dataType(XacmlTypes.STRING).build());
		c.verify();
	}
}
