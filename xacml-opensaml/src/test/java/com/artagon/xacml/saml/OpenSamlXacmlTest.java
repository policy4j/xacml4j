package com.artagon.xacml.saml;

import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.Before;
import org.junit.Test;
import org.opensaml.xacml.profile.saml.XACMLAuthzDecisionQueryType;
import org.w3c.dom.Document;

import com.artagon.xacml.opensaml.OpenSamlObjectBuilder;




public class OpenSamlXacmlTest
{
	private Document xacmlRequestDOM;
	
	@Before
	public void testInit() throws Exception
	{
		InputStream xacmlReqIn =Thread.currentThread().getContextClassLoader().getResourceAsStream("TestRequest.xml");
		assertNotNull(xacmlReqIn);
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		xacmlRequestDOM = dbf.newDocumentBuilder().parse(xacmlReqIn);
	}
	
	@Test
	public void testBuildSamlXacmlQuery() throws Exception
	{
		XACMLAuthzDecisionQueryType xacml20AuthzQuery = OpenSamlObjectBuilder.makeXacml20SamlAuthzDecisionQuery(
				"testIssuer", "https://login.comcast.net/login", false,
				OpenSamlObjectBuilder.unmarshallXacml20Request(xacmlRequestDOM.getDocumentElement()));
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		OpenSamlObjectBuilder.serialize(xacml20AuthzQuery, out, true);
		System.out.println(new String(out.toByteArray()));
	}
}
