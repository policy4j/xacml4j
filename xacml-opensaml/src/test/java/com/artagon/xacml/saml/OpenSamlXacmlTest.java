package com.artagon.xacml.saml;

import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.security.KeyStore;

import javax.xml.parsers.DocumentBuilderFactory;

import org.easymock.Capture;
import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opensaml.DefaultBootstrap;
import org.opensaml.saml2.core.RequestAbstractType;
import org.opensaml.saml2.core.Response;
import org.opensaml.xacml.profile.saml.XACMLAuthzDecisionQueryType;
import org.opensaml.xml.Configuration;
import org.opensaml.xml.security.SecurityHelper;
import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.security.x509.KeyStoreX509CredentialAdapter;
import org.opensaml.xml.signature.Signature;
import org.opensaml.xml.signature.SignatureConstants;
import org.opensaml.xml.signature.Signer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.w3c.dom.Document;

import com.artagon.xacml.opensaml.IDPConfiguration;
import com.artagon.xacml.opensaml.OpenSamlObjectBuilder;
import com.artagon.xacml.opensaml.XACMLAuthzDecisionQueryEndpoint;
import com.artagon.xacml.v30.RequestContext;
import com.artagon.xacml.v30.ResponseContext;
import com.artagon.xacml.v30.Result;
import com.artagon.xacml.v30.pdp.PolicyDecisionPoint;


@ContextConfiguration(locations={"classpath:testApplicationContext.xml"})
public class OpenSamlXacmlTest extends AbstractJUnit4SpringContextTests
{
	@Autowired
	private IDPConfiguration idpConfiguration;
	private Credential idpSigningCredential;
	private XACMLAuthzDecisionQueryEndpoint endpoint;
	private PolicyDecisionPoint pdp;
	private IMocksControl control;
	
	@BeforeClass
	public static void init() throws Exception{
		DefaultBootstrap.bootstrap();
	}
	
	@Before
	public void testInit() throws Exception
	{
		this.control = EasyMock.createControl();
		this.pdp = control.createMock(PolicyDecisionPoint.class);
		this.idpSigningCredential = new KeyStoreX509CredentialAdapter(getKeyStore("PKCS12", "/test-keystore.p12", "changeme"), "ping", "changeme".toCharArray());
		this.endpoint = new XACMLAuthzDecisionQueryEndpoint(idpConfiguration, pdp);
		
	}
	
	@Test
	public void testXACMLAuthzDecisionQuery() throws Exception
	{
		Document query = parse("XacmlSamlRequest.xml");
		XACMLAuthzDecisionQueryType xacmlSamlQuery = OpenSamlObjectBuilder.unmarshallXacml20AuthzDecisionQuery(query.getDocumentElement());
		signRequest(xacmlSamlQuery);
		Capture<RequestContext> captureRequest = new Capture<RequestContext>();
		expect(pdp.decide(capture(captureRequest))).andReturn(new ResponseContext(
				Result.createIndeterminate(com.artagon.xacml.v30.Status.createProcessingError())));
		control.replay();
		Response response = endpoint.handle(xacmlSamlQuery);
		assertNotNull(response);
		ByteArrayOutputStream outResponse = new ByteArrayOutputStream();
		OpenSamlObjectBuilder.serialize(response, outResponse, false);
		control.verify();
		System.out.println("--------" + new String(outResponse.toByteArray()));
		
	}
	
	@Test
	public void testUnmarshall() throws Exception
	{
		Document doc = parse("XacmlSamlRequest.xml");
		OpenSamlObjectBuilder.unmarshall(doc.getDocumentElement());
	}
	
	private KeyStore getKeyStore(String ksType, String resource, String ksPwd) throws Exception 
	{
		KeyStore ks = KeyStore.getInstance(ksType);
		ks.load(getClass().getResourceAsStream(resource), ksPwd.toCharArray());
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
	
	private void signRequest(RequestAbstractType response) throws Exception
	{
				
		Signature dsig = (Signature) Configuration.getBuilderFactory()
        .getBuilder(Signature.DEFAULT_ELEMENT_NAME)
        .buildObject(Signature.DEFAULT_ELEMENT_NAME);

		dsig.setSigningCredential(idpConfiguration.getSigningCredential());
		dsig.setSignatureAlgorithm(SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA1);
		dsig.setCanonicalizationAlgorithm(SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);
		response.setSignature(dsig);
		SecurityHelper.prepareSignatureParams(dsig, idpConfiguration.getSigningCredential(), null, null);
		
		Configuration.getMarshallerFactory().getMarshaller(response).marshall(response);
		Signer.signObject(dsig);
	}
}
