package com.artagon.xacml.saml;

import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.xml.security.Init;
import org.apache.xml.security.c14n.Canonicalizer;
import org.apache.xml.security.signature.XMLSignature;
import org.apache.xml.security.transforms.Transforms;
import org.apache.xml.security.transforms.params.InclusiveNamespaces;
import org.apache.xml.security.utils.Constants;
import org.bouncycastle.util.io.Streams;
import org.easymock.Capture;
import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opensaml.DefaultBootstrap;
import org.opensaml.saml2.core.Response;
import org.opensaml.xacml.profile.saml.XACMLAuthzDecisionQueryType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.artagon.xacml.opensaml.IDPConfiguration;
import com.artagon.xacml.opensaml.OpenSamlObjectBuilder;
import com.artagon.xacml.opensaml.XACMLAuthzDecisionQueryEndpoint;
import com.artagon.xacml.v30.RequestContext;
import com.artagon.xacml.v30.ResponseContext;
import com.artagon.xacml.v30.Result;
import com.artagon.xacml.v30.pdp.PolicyDecisionPoint;


@ContextConfiguration(locations={"classpath:testApplicationContext.xml"})
public class HBOIntegrationTest extends AbstractJUnit4SpringContextTests
{
	@Autowired
	private IDPConfiguration idpConfiguration;
	private static PrivateKey hboPrivateKey;
	private static X509Certificate hboCertificate;
	private XACMLAuthzDecisionQueryEndpoint endpoint;
	private PolicyDecisionPoint pdp;
	private IMocksControl control;
	
	@BeforeClass
	public static void init_static() throws Exception{
		Init.init();
		DefaultBootstrap.bootstrap();
		KeyStore ks = getKeyStore("PKCS12", "/hbo-dev-cert.p12", "hbo");		
		Certificate[] certs = new Certificate[]{ks.getCertificate("1")};
		hboCertificate = (X509Certificate) certs[0];
		hboPrivateKey = (PrivateKey)ks.getKey("1", "hbo".toCharArray());
		assertNotNull(hboPrivateKey);
	}
	
	@Before
	public void setup() throws Exception
	{
		this.control = EasyMock.createControl();
		this.pdp = control.createMock(PolicyDecisionPoint.class);
//		idpConfiguration = new DefaultIDPConfiguration(localEntityId, metadata, idpSigningCredential)
		this.endpoint = new XACMLAuthzDecisionQueryEndpoint(idpConfiguration, pdp);
	}
	
	@Test
	public void testHBOIntegration() throws Exception
	{
		String req = read("hbo-test-req.xml");
		String signedDoc = generateSignature(req, hboPrivateKey, hboCertificate, "#_fd4ad156-7d84-4070-bc9f-679d88a1368a");
		Document query = parseSAML(signedDoc);
		
		XACMLAuthzDecisionQueryType xacmlSamlQuery = OpenSamlObjectBuilder.unmarshallXacml20AuthzDecisionQuery(query.getDocumentElement());
		ByteArrayOutputStream outResponse = new ByteArrayOutputStream();
		OpenSamlObjectBuilder.serialize(xacmlSamlQuery, outResponse);
		System.err.println(" Request ----- " + new String(outResponse.toByteArray()));
		Capture<RequestContext> captureRequest = new Capture<RequestContext>();
		expect(pdp.decide(capture(captureRequest))).andReturn(new ResponseContext(
				Result.createIndeterminate(com.artagon.xacml.v30.Status.createProcessingError())));
		control.replay();
		Response response = endpoint.handle(xacmlSamlQuery);
		assertNotNull(response);
		outResponse = new ByteArrayOutputStream();
		OpenSamlObjectBuilder.serialize(response, outResponse);
		System.err.println("Response ----- " + new String(outResponse.toByteArray()));
		control.verify();		
	}
	

	private static KeyStore getKeyStore(String ksType, String resource, String ksPwd) throws Exception 
	{
		KeyStore ks = KeyStore.getInstance(ksType);
		ks.load(HBOIntegrationTest.class.getResourceAsStream(resource), ksPwd.toCharArray());
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

	public static String read(String resourcePath) throws Exception
	{
		InputStream in =Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcePath);
		assertNotNull(in);
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		Streams.pipeAll(in, os);
		return new String(os.toByteArray());
	}

	public Document generateSignatureSla(Document doc, PrivateKey privateKey,
			X509Certificate publicKey, String referenceUri) {
		try {
			XMLSignature sig = new XMLSignature(doc, "",
					XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA1);
			doc.normalizeDocument();
			NodeList reqNodes = doc.getElementsByTagNameNS("urn:oasis:names:tc:xacml:2.0:context:schema:os", "Request");
			reqNodes.item(0).getParentNode().insertBefore(sig.getElement(), reqNodes.item(0));
			Transforms transforms = new Transforms(doc);
			transforms.addTransform(Transforms.TRANSFORM_ENVELOPED_SIGNATURE);
			InclusiveNamespaces incNS = new InclusiveNamespaces(doc,
					"ds saml2 xacml-context xacml-samlp");
//			transforms.addTransform(
//					Transforms.TRANSFORM_C14N_EXCL_OMIT_COMMENTS,
//					incNS.getElement());
			transforms.addTransform(
					Transforms.TRANSFORM_C14N_EXCL_OMIT_COMMENTS);
			sig.addKeyInfo(publicKey);
			sig.addKeyInfo(publicKey.getPublicKey());
			sig.addDocument(referenceUri, transforms,
					Constants.ALGO_ID_DIGEST_SHA1);
			sig.sign(privateKey);

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer trans = tf.newTransformer();
			DOMResult dr = new DOMResult();
			trans.transform(new DOMSource(doc), dr);

			return (Document)dr.getNode();
//			result = baos.toString();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static String generateSignature(String xml, PrivateKey privateKey,
			X509Certificate publicKey, String referenceUri) {
		String result = null;
		try {
			Document doc = parseSAML(xml);
			Init.init();
			XMLSignature sig = new XMLSignature(doc, "http://xml-security",
					XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA1,
					Canonicalizer.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);
			Element headerElement = doc.getDocumentElement();
			doc.normalizeDocument();
			headerElement.appendChild(sig.getElement());
			Transforms transforms = new Transforms(doc);
			transforms.addTransform(Transforms.TRANSFORM_ENVELOPED_SIGNATURE);
			InclusiveNamespaces incNS = new InclusiveNamespaces(doc,
					"ds saml2 xacml-context xacml-samlp");
			transforms.addTransform(
					Transforms.TRANSFORM_C14N_EXCL_OMIT_COMMENTS,
					incNS.getElement());
			sig.addDocument(referenceUri, transforms,
					Constants.ALGO_ID_DIGEST_SHA1);
			sig.addKeyInfo(publicKey);
			sig.addKeyInfo(publicKey.getPublicKey());
			sig.sign(privateKey);

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer trans = tf.newTransformer();
			trans.transform(new DOMSource(doc), new StreamResult(baos));

			result = baos.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	public static Document parseSAML(String saml) {
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setNamespaceAware(true);
			Document doc = dbf.newDocumentBuilder().parse(
					new ByteArrayInputStream(saml.getBytes()));
			return doc;
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}
}
