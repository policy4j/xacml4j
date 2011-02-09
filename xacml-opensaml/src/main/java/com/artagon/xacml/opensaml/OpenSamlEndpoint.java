package com.artagon.xacml.opensaml;

import org.opensaml.saml2.core.RequestAbstractType;
import org.opensaml.saml2.core.Response;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public interface OpenSamlEndpoint 
{
	void handle(Element request, Document responseDoc) throws Exception;
	Response handle(RequestAbstractType req);
}
