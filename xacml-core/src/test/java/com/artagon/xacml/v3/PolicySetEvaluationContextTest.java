package com.artagon.xacml.v3;

import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.v3.spi.PolicyReferenceResolver;
import com.artagon.xacml.v3.spi.XPathProvider;

public class PolicySetEvaluationContextTest 
{
	private EvaluationContext context;
	
	private PolicySet policySet;
	private ContextHandler resolver;
	private PolicyReferenceResolver policyResolver;
	private PolicySetDefaults defaults;
	private XPathProvider xpathProvider;
	
	@Before
	public void setup(){
		this.defaults = createStrictMock(PolicySetDefaults.class);
		this.policyResolver = createStrictMock(PolicyReferenceResolver.class);
		this.resolver = createStrictMock(ContextHandler.class);
		this.xpathProvider = createStrictMock(XPathProvider.class);
		this.policySet = createStrictMock(PolicySet.class);
		this.context = new PolicySetEvaluationContext(policySet, 
				resolver, xpathProvider, policyResolver);
	}
	
	@Test
	public void testGetXPathVersionWithPolicySetDefaultsNotSet()
	{
		expect(policySet.getDefaults()).andReturn(null);
		replay(policySet, resolver, policyResolver);
		assertEquals(XPathVersion.XPATH1, context.getXPathVersion());
		verify(policySet, resolver, policyResolver);
	}
	
	@Test
	public void testGetXPathVersionWithPolicySetDefaultsSet()
	{
		expect(policySet.getDefaults()).andReturn(defaults);
		expect(defaults.getXPathVersion()).andReturn(XPathVersion.XPATH2);
		replay(policySet, defaults);
		assertEquals(XPathVersion.XPATH2, context.getXPathVersion());
		verify(policySet, defaults);
	}
	
	@Test
	public void testCreateContext()
	{
		assertSame(policySet, context.getCurrentPolicySet());
		assertNull(context.getCurrentPolicy());
		assertNull(context.getCurrentPolicyIDReference());
		assertNull(context.getCurrentPolicySetIDReference());
	}
}
