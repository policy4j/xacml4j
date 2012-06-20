package com.artagon.xacml.saml;

import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.xml.security.utils.XMLUtils;
import org.easymock.Capture;
import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opensaml.DefaultBootstrap;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.core.StatusCode;
import org.opensaml.xacml.profile.saml.XACMLAuthzDecisionQueryType;
import org.opensaml.xml.security.x509.KeyStoreX509CredentialAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.w3c.dom.Document;

import com.artagon.xacml.opensaml.IDPConfiguration;
import com.artagon.xacml.opensaml.OpenSamlObjectBuilder;
import com.artagon.xacml.opensaml.XACMLAuthzDecisionQueryEndpoint;
import com.artagon.xacml.opensaml.XACMLAuthzDecisionQuerySigner;
import com.artagon.xacml.v30.RequestContext;
import com.artagon.xacml.v30.ResponseContext;
import com.artagon.xacml.v30.Result;
import com.artagon.xacml.v30.pdp.PolicyDecisionPoint;


@ContextConfiguration(locations={"classpath:testApplicationContext.xml"})
public class OpenSamlXacmlTest extends AbstractJUnit4SpringContextTests
{
	@Autowired
	private IDPConfiguration idpConfiguration;
	private XACMLAuthzDecisionQueryEndpoint endpoint;
	private PolicyDecisionPoint pdp;
	private IMocksControl control;

	private static PrivateKey hboPrivate;
	private static X509Certificate hboPublic;
	private static XACMLAuthzDecisionQuerySigner signer;

	@BeforeClass
	public static void init() throws Exception
	{
		DefaultBootstrap.bootstrap();
		KeyStore ks = getKeyStore("PKCS12", "/hbo-dev-cert.p12", "hbo");
		hboPublic = (X509Certificate)ks.getCertificate("1");
		assertNotNull(hboPublic);
		Certificate[] certs = new Certificate[]{hboPublic};
		hboPrivate  = (PrivateKey)ks.getKey("1", "hbo".toCharArray());
		assertNotNull(hboPrivate);

		KeyStore newKs = KeyStore.getInstance("PKCS12");
		newKs.load(null, "hbo".toCharArray());
		newKs.setEntry("hbo", new KeyStore.PrivateKeyEntry(hboPrivate,certs),  new KeyStore.PasswordProtection("hbo".toCharArray()));

		signer = new XACMLAuthzDecisionQuerySigner(newKs, "hbo", "hbo");
	}

	@Before
	public void testInit() throws Exception
	{
		this.control = EasyMock.createControl();
		this.pdp = control.createMock(PolicyDecisionPoint.class);
		new KeyStoreX509CredentialAdapter(getKeyStore("PKCS12", "/test-keystore.p12", "changeme"), "ping", "changeme".toCharArray());

		this.endpoint = new XACMLAuthzDecisionQueryEndpoint(idpConfiguration, pdp);
	}

	@Test
	public void testXACMLAuthzDecisionQuery() throws Exception
	{
		Document query = parse("XacmlSamlRequest.xml");
		XACMLAuthzDecisionQueryType xacmlSamlQuery = OpenSamlObjectBuilder.unmarshallXacml20AuthzDecisionQuery(query.getDocumentElement());
		signer.signRequest(xacmlSamlQuery);
		ByteArrayOutputStream outResponse = new ByteArrayOutputStream();
		OpenSamlObjectBuilder.serialize(xacmlSamlQuery, outResponse);
		System.out.println(" Request ----- " + new String(outResponse.toByteArray()));
		Capture<RequestContext> captureRequest = new Capture<RequestContext>();
		expect(pdp.decide(capture(captureRequest))).andReturn(ResponseContext
				.newBuilder()
				.result(Result
						.createIndeterminateProcessingError()
						.build())
				.build());
		control.replay();
		Response response = endpoint.handle(xacmlSamlQuery);
		assertNotNull(response);
		outResponse = new ByteArrayOutputStream();
		OpenSamlObjectBuilder.serialize(response, outResponse);
		System.out.println("Response ----- " + new String(outResponse.toByteArray()));
		control.verify();
	}

	@Test
	public void testXACMLAuthzDecisionQuery1() throws Exception
	{
		Document query = parse("TestXacmlSamlRequest.xml");
		XACMLAuthzDecisionQueryType xacmlSamlQuery = OpenSamlObjectBuilder.unmarshallXacml20AuthzDecisionQuery(query.getDocumentElement());
		Capture<RequestContext> captureRequest = new Capture<RequestContext>();
		expect(pdp.decide(capture(captureRequest))).andReturn(ResponseContext
				.newBuilder()
				.result(Result
						.createIndeterminateProcessingError()
						.build())
				.build());
		control.replay();
		Response response = endpoint.handle(xacmlSamlQuery);
		assertNotNull(response);
		ByteArrayOutputStream outResponse = new ByteArrayOutputStream();
		OpenSamlObjectBuilder.serialize(response, outResponse);
		System.out.println("Response ----- " + new String(outResponse.toByteArray()));
		control.verify();
	}

	@Test
	public void testXACMLAuthzDecisionQueryInvalidSignature() throws Exception
	{
		Document query = parse("TestXacmlSamlRequest-invalidSignature.xml");
		XACMLAuthzDecisionQueryType xacmlSamlQuery = OpenSamlObjectBuilder.unmarshallXacml20AuthzDecisionQuery(query.getDocumentElement());
		Capture<RequestContext> captureRequest = new Capture<RequestContext>();
		expect(pdp.decide(capture(captureRequest))).andReturn(ResponseContext
				.newBuilder()
				.result(Result
						.createIndeterminateProcessingError()
						.build())
				.build());
		control.replay();
		Response response1 = endpoint.handle(xacmlSamlQuery);

		assertNotNull(response1);
		assertEquals(StatusCode.REQUESTER_URI, response1.getStatus().getStatusCode().getValue());

		endpoint.setRequireSignatureValidation(false);
		Response response2 = endpoint.handle(xacmlSamlQuery);
		assertNotNull(response2);
		assertEquals(StatusCode.SUCCESS_URI, response2.getStatus().getStatusCode().getValue());
		control.verify();
	}

	@Test
	public void testXACMLAuthzDecisionQueryNoSignatureSignWithOtherLibrary() throws Exception
	{
		Document query = parse("TestXacmlSamlRequest-nosignature.xml");
		new ApacheXMLDsigGenerator().signSamlRequest(query.getDocumentElement(), hboPrivate, hboPublic);

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		XMLUtils.outputDOMc14nWithComments(query, bos);

		XACMLAuthzDecisionQueryType xacmlSamlQuery = OpenSamlObjectBuilder.unmarshallXacml20AuthzDecisionQuery(
				query.getDocumentElement());
		Capture<RequestContext> captureRequest = new Capture<RequestContext>();
		expect(pdp.decide(capture(captureRequest))).andReturn(ResponseContext
				.newBuilder()
				.result(Result
						.createIndeterminateProcessingError()
						.build())
				.build());
		control.replay();
		Response response1 = endpoint.handle(xacmlSamlQuery);

		assertNotNull(response1);
		assertEquals(StatusCode.SUCCESS_URI, response1.getStatus().getStatusCode().getValue());

		control.verify();
	}

	private static KeyStore getKeyStore(String ksType, String resource, String ksPwd) throws Exception
	{
		KeyStore ks = KeyStore.getInstance(ksType);
		ks.load(OpenSamlXacmlTest.class.getResourceAsStream(resource), ksPwd.toCharArray());
		return ks;
	}

	public static Document parse(String resourcePath) throws Exception
	{
		InputStream in =Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcePath);
		assertNotNull(in);
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		return dbf.newDocumentBuilder().parse(in);
	}

}
