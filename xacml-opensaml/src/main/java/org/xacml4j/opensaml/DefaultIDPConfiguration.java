package org.xacml4j.opensaml;

import org.opensaml.saml2.metadata.AuthzService;
import org.opensaml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml2.metadata.PDPDescriptor;
import org.opensaml.saml2.metadata.provider.MetadataProvider;
import org.opensaml.saml2.metadata.provider.MetadataProviderException;
import org.opensaml.security.MetadataCredentialResolver;
import org.opensaml.xml.Configuration;
import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.security.keyinfo.KeyInfoCredentialResolver;
import org.opensaml.xml.signature.SignatureTrustEngine;
import org.opensaml.xml.signature.impl.ExplicitKeySignatureTrustEngine;
import org.opensaml.xml.util.DatatypeHelper;

import com.google.common.base.Preconditions;

public class DefaultIDPConfiguration implements IDPConfiguration
{
	private final static String SAML20_PROTOCOL = "urn:oasis:names:tc:SAML:2.0:protocol";
	
	private EntityDescriptor localEntity;
	private SignatureTrustEngine trustEngine;
	private Credential idpSigningCredential;
	
	public DefaultIDPConfiguration(String localEntityId, 
			MetadataProvider metadata,  
			Credential idpSigningCredential) 
		throws MetadataProviderException
	{
		Preconditions.checkNotNull(localEntityId);
		Preconditions.checkNotNull(metadata);
		Preconditions.checkNotNull(idpSigningCredential);
		this.localEntity = metadata.getEntityDescriptor(localEntityId);
		Preconditions.checkState(localEntity != null);
		this.trustEngine = createDefaultSignatureTrustEngine(metadata);
		this.idpSigningCredential = idpSigningCredential;
	}
	
	@Override
	public EntityDescriptor getLocalEntity() {
		return localEntity;
	}
	
	public SignatureTrustEngine getSignatureTrustEngine(){
		return trustEngine;
	}
	
	
	@Override
	public AuthzService getAuthzServiceByLocation(
			String locationURL){
		PDPDescriptor pdp = localEntity.getPDPDescriptor(SAML20_PROTOCOL);
		if(pdp == null){
			return null;
		}
		for(AuthzService s : pdp.getAuthzServices()){
			if(DatatypeHelper.safeEquals(locationURL, s.getLocation())){
				return s;
			}
		}
		return null;
	}

	@Override
	public Credential getSigningCredential()
	{
		return idpSigningCredential;
	}
	
	private static SignatureTrustEngine createDefaultSignatureTrustEngine(MetadataProvider metadata)
	{
		MetadataCredentialResolver mdCredResolver = new MetadataCredentialResolver(metadata);
		KeyInfoCredentialResolver keyInfoCredResolver = Configuration.getGlobalSecurityConfiguration().getDefaultKeyInfoCredentialResolver();
		return new ExplicitKeySignatureTrustEngine(mdCredResolver, keyInfoCredResolver);
	}
	
}
