package com.artagon.xacml.opensaml;

import org.opensaml.saml2.core.RequestAbstractType;
import org.opensaml.saml2.core.Response;

public interface OpenSamlEndpoint 
{
	Response handle(RequestAbstractType req);
}
