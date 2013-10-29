package org.xacml4j.v30.spi.xpath;

import javax.xml.xpath.XPathFactory;

import org.junit.Test;

public class DefaultXPathFactoryTest
{

	@Test
	public void testCreateXPathFactory() throws Exception
	{
		XPathFactory.newInstance();
	}
}
