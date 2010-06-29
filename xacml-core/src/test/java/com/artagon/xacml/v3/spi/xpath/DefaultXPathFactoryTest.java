package com.artagon.xacml.v3.spi.xpath;

import javax.xml.xpath.XPathFactory;

import org.junit.Test;

import com.artagon.xacml.v3.XPathVersion;

public class DefaultXPathFactoryTest 
{
	
	@Test
	public void testCreateXPathFactory() throws Exception
	{
		XPathFactory xpf = XPathFactory.newInstance();
		xpf.getFeature(XPathVersion.XPATH1.toString());
	}
}
