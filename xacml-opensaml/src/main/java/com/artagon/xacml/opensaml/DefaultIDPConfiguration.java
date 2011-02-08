package com.artagon.xacml.opensaml;

import org.opensaml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml2.metadata.provider.MetadataProvider;
import org.opensaml.saml2.metadata.provider.MetadataProviderException;
import org.opensaml.security.MetadataCredentialResolver;
import org.opensaml.xml.Configuration;
import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.security.keyinfo.KeyInfoCredentialResolver;
import org.opensaml.xml.signature.SignatureTrustEngine;
import org.opensaml.xml.signature.impl.ExplicitKeySignatureTrustEngine;

import com.google.common.base.Preconditions;

public class DefaultIDPConfiguration implements IDPConfiguration
{
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
	
	public Credential getLocalEntitySigningCredential()
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
