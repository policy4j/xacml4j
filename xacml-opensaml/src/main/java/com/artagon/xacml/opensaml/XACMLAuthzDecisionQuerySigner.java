package com.artagon.xacml.opensaml;

import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyStore;

import javax.xml.parsers.DocumentBuilderFactory;

import org.opensaml.DefaultBootstrap;
import org.opensaml.saml2.core.RequestAbstractType;
import org.opensaml.xacml.profile.saml.XACMLAuthzDecisionQueryType;
import org.opensaml.xml.Configuration;
import org.opensaml.xml.security.SecurityHelper;
import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.security.x509.KeyStoreX509CredentialAdapter;
import org.opensaml.xml.signature.Signature;
import org.opensaml.xml.signature.SignatureConstants;
import org.opensaml.xml.signature.Signer;
import org.w3c.dom.Document;

public class XACMLAuthzDecisionQuerySigner 
{
	private Credential credential;
	
	public XACMLAuthzDecisionQuerySigner(KeyStore ks, 
			String signingKeyName, 
			String signingKeyPassword) 
			throws Exception
	{
		DefaultBootstrap.bootstrap();
		this.credential = new KeyStoreX509CredentialAdapter(ks, 
				signingKeyName, 
				signingKeyPassword.toCharArray());
	}
	
	public void signRequest(InputStream request, OutputStream signedRequest) throws Exception
	{
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
	    Document doc = dbf.newDocumentBuilder().parse(request);
	    XACMLAuthzDecisionQueryType xacmlSamlQuery = OpenSamlObjectBuilder.unmarshallXacml20AuthzDecisionQuery(doc.getDocumentElement());
	    signRequest(xacmlSamlQuery);
	    OpenSamlObjectBuilder.serialize(xacmlSamlQuery, signedRequest);
	}
	
	public void signRequest(RequestAbstractType response) throws Exception
	{
				
		Signature dsig = (Signature) Configuration.getBuilderFactory()
        .getBuilder(Signature.DEFAULT_ELEMENT_NAME)
        .buildObject(Signature.DEFAULT_ELEMENT_NAME);

		dsig.setSigningCredential(credential);
		dsig.setSignatureAlgorithm(SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA1);
		dsig.setCanonicalizationAlgorithm(SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);
		response.setSignature(dsig);
		SecurityHelper.prepareSignatureParams(dsig, credential, null, null);
		
		Configuration.getMarshallerFactory().getMarshaller(response).marshall(response);
		Signer.signObject(dsig);
	}
}
