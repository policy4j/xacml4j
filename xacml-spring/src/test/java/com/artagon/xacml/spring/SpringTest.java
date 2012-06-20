package com.artagon.xacml.spring;


import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertNotNull;

import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import com.artagon.xacml.v30.AttributeCategories;
import com.artagon.xacml.v30.AttributeDesignatorKey;
import com.artagon.xacml.v30.EvaluationContext;
import com.artagon.xacml.v30.pdp.PolicyDecisionPoint;
import com.artagon.xacml.v30.pdp.PolicySet;
import com.artagon.xacml.v30.spi.pip.PolicyInformationPoint;
import com.artagon.xacml.v30.spi.pip.ResolverRegistry;
import com.artagon.xacml.v30.types.StringType;

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
		resolverRegistry.getMatchingAttributeResolvers(policyContext, new AttributeDesignatorKey(AttributeCategories.parse("subject"), "testId1", StringType.STRING, null));
		c.verify();
	}
}
