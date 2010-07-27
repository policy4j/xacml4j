package com.artagon.xacml.spring;


import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

@ContextConfiguration(locations={"classpath:/functionProviderFactoryBeanTest.xml"})
public class FunctionProviderFactoryBeanTest extends AbstractJUnit4SpringContextTests
{
	
	@Test
	public void testProviders()
	{
	}
}
