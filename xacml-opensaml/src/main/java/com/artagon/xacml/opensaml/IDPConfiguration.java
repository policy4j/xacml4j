package com.artagon.xacml.opensaml;

import org.opensaml.saml2.metadata.EntityDescriptor;
import org.opensaml.xml.signature.SignatureTrustEngine;

public interface IDPConfiguration 
{
	/**
	 * Gets an descriptor for local entity
	 * 
	 * @return {@link 
	 */
	EntityDescriptor getLocalEntity();
	
	SignatureTrustEngine getSignatureTrustEngine();
}
