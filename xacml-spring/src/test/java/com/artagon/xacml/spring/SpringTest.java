package com.artagon.xacml.spring;


import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import com.artagon.xacml.v30.pdp.PolicyDecisionPoint;
import com.artagon.xacml.v30.spi.pip.PolicyInformationPoint;
import com.artagon.xacml.v30.spi.pip.ResolverRegistry;

@ContextConfiguration(locations={"classpath:/spring-test.xml"})
public class SpringTest extends AbstractJUnit4SpringContextTests
{
	@Autowired
	private PolicyDecisionPoint pdp;
	
	@Autowired
	private ResolverRegistry resolverRegistry;
	
	@Autowired
	private PolicyInformationPoint pip;
	
	@Test
	public void testProviders()
	{
	}
}
