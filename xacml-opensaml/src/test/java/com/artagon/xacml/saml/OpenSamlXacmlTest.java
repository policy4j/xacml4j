package com.artagon.xacml.saml;

import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.security.KeyStore;

import javax.xml.parsers.DocumentBuilderFactory;

import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opensaml.DefaultBootstrap;
import org.opensaml.saml2.core.Assertion;
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
import com.artagon.xacml.v30.pdp.PolicyDecisionPoint;


public class OpenSamlXacmlTest
{
	private Document xacmlRequestDOM;
	private Document xacmlResponseDOM;
	
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
		xacmlRequestDOM = parse("TestRequest.xml");
		xacmlResponseDOM = parse("TestResponse.xml");
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
		Response response = endpoint.handle(xacmlSamlQuery);
		assertNotNull(response);
		
	}
	
	@Test
	public void testBuildSamlXacmlQuery() throws Exception
	{
		RequestType xacml20Request1 = OpenSamlObjectBuilder.unmarshallXacml20Request(xacmlRequestDOM.getDocumentElement());
		RequestType xacml20Request2 = OpenSamlObjectBuilder.unmarshallXacml20Request(xacmlRequestDOM.getDocumentElement());
		ResponseType xacml20Response2 = OpenSamlObjectBuilder.unmarshallXacml20Response(xacmlResponseDOM.getDocumentElement());
		
		
		XACMLAuthzDecisionQueryType xacml20AuthzQuery = OpenSamlObjectBuilder.makeXacml20SamlAuthzDecisionQuery(
				"https://qa.profile.hbogo.com/dev.sp.inhbogo.com/COMCAST", "https://login-qa4.comcast.net/api/xacml/saml20", false,
				xacml20Request1);
		
		Signature signature = OpenSamlObjectBuilder.makeSiganture();
		signature.setSigningCredential(spSigningCredential);
		signature.setSignatureAlgorithm(SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA1);
		signature.setCanonicalizationAlgorithm(SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);
		xacml20AuthzQuery.setSignature(signature);
		SecurityHelper.prepareSignatureParams(signature, spSigningCredential, null, null);
		Configuration.getMarshallerFactory().getMarshaller(xacml20AuthzQuery).marshall(xacml20AuthzQuery);
		
		
		Signer.signObject(signature);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		OpenSamlObjectBuilder.serialize(xacml20AuthzQuery, out, false);
		System.out.println(new String(out.toByteArray()));
		
		Status status = OpenSamlObjectBuilder.makeStatus(StatusCode.SUCCESS_URI);
		Response xacmlResponse = OpenSamlObjectBuilder.makeResponse(xacml20AuthzQuery, status);
		xacmlResponse.setIssuer(OpenSamlObjectBuilder.makeIssuer("https://sso.comcast.net/Comcast/IdP/sso"));
		xacmlResponse.setIssueInstant(new DateTime());
		
		Assertion assertion = OpenSamlObjectBuilder.makeXacml20AuthzDecisionAssertion(
				"https://sso.comcast.net/Comcast/IdP/sso", xacml20Request2, xacml20Response2);
		
		Signature assertionSignature = (Signature) Configuration.getBuilderFactory()
        .getBuilder(Signature.DEFAULT_ELEMENT_NAME)
        .buildObject(Signature.DEFAULT_ELEMENT_NAME);

		assertionSignature.setSigningCredential(idpSigningCredential);
		assertionSignature.setSignatureAlgorithm(SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA1);
		assertionSignature.setCanonicalizationAlgorithm(SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);
		assertion.setSignature(assertionSignature);
		SecurityHelper.prepareSignatureParams(assertionSignature, idpSigningCredential, null, null);
		
		Configuration.getMarshallerFactory().getMarshaller(assertion).marshall(assertion);
		Signer.signObject(assertionSignature);
	
		Response samlResponse = OpenSamlObjectBuilder.makeXacml20AuthzDecisionQueryResponse("https://sso.comcast.net/Comcast/IdP/sso", xacml20AuthzQuery, assertion);	
		ByteArrayOutputStream outResponse = new ByteArrayOutputStream();
		OpenSamlObjectBuilder.serialize(samlResponse, outResponse, false);
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
}
