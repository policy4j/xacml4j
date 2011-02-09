package com.artagon.xacml.saml;

import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.security.KeyStore;

import javax.xml.parsers.DocumentBuilderFactory;

import org.easymock.Capture;
import org.easymock.EasyMock;
import static org.easymock.EasyMock.*;
import org.easymock.IMocksControl;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.opensaml.DefaultBootstrap;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.RequestAbstractType;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.core.Status;
import org.opensaml.saml2.core.StatusCode;
import org.opensaml.saml2.metadata.provider.MetadataProvider;
import org.opensaml.xacml.ctx.RequestType;
import org.opensaml.xacml.ctx.ResponseType;
import org.opensaml.xacml.profile.saml.XACMLAuthzDecisionQueryType;
import org.opensaml.xml.Configuration;
import org.opensaml.xml.security.SecurityHelper;
import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.security.x509.KeyStoreX509CredentialAdapter;
import org.opensaml.xml.signature.Signature;
import org.opensaml.xml.signature.SignatureConstants;
import org.opensaml.xml.signature.Signer;
import org.w3c.dom.Document;

import com.artagon.xacml.opensaml.DefaultIDPConfiguration;
import com.artagon.xacml.opensaml.IDPConfiguration;
import com.artagon.xacml.opensaml.OpenSamlObjectBuilder;
import com.artagon.xacml.opensaml.XACMLAuthzDecisionQueryEndpoint;
import com.artagon.xacml.v30.RequestContext;
import com.artagon.xacml.v30.ResponseContext;
import com.artagon.xacml.v30.Result;
import com.artagon.xacml.v30.pdp.PolicyDecisionPoint;


public class OpenSamlXacmlTest
{
	private IDPConfiguration idpConfiguration;
	private Credential spSigningCredential;
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
		this.spSigningCredential = new KeyStoreX509CredentialAdapter(getKeyStore("PKCS12", "/test-keystore.p12", "changeme"), "ping", "changeme".toCharArray());
		

		MetadataProvider md = OpenSamlObjectBuilder.getMetadata(new File("./src/test/resources/metadata.xml"));
		this.idpConfiguration = new DefaultIDPConfiguration("https://sso.comcast.net/Comcast/IdP/sso", md, idpSigningCredential);
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
