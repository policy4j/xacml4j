package com.artagon.xacml.springws;



import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.springframework.ws.server.endpoint.AbstractDomPayloadEndpoint;

import com.artagon.xacml.v20.Xacml20RequestUnmarshaller;
import com.artagon.xacml.v20.Xacml20ResponseMarshaller;
import com.artagon.xacml.v3.RequestContext;
import com.artagon.xacml.v3.ResponseContext;
import com.artagon.xacml.v3.marshall.RequestUnmarshaller;
import com.artagon.xacml.v3.marshall.ResponseMarshaller;
import com.artagon.xacml.v3.pdp.PolicyDecisionPoint;

public class DomXacmlEndpoint extends AbstractDomPayloadEndpoint 
{
	private PolicyDecisionPoint pdp;
	private RequestUnmarshaller requestUnmarshaller;
	private ResponseMarshaller responseMarshaller;
	
	public DomXacmlEndpoint( 
			PolicyDecisionPoint pdp) {
		this.pdp = pdp;
		this.requestUnmarshaller = new Xacml20RequestUnmarshaller();
		this.responseMarshaller = new Xacml20ResponseMarshaller();		
	}
	
	
	
	@Override
	protected Element invokeInternal(Element requestElement,
			Document responseDocument) throws Exception {
		RequestContext request = requestUnmarshaller.unmarshal(requestElement);
		ResponseContext response = pdp.decide(request);
		responseMarshaller.marshal(response, responseDocument);
		return responseDocument.getDocumentElement();
	}

}

