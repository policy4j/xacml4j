package org.xacml4j.springws;


import org.springframework.ws.server.endpoint.AbstractDomPayloadEndpoint;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xacml4j.v30.RequestContext;
import org.xacml4j.v30.ResponseContext;
import org.xacml4j.v30.marshall.RequestUnmarshaller;
import org.xacml4j.v30.marshall.ResponseMarshaller;
import org.xacml4j.v30.marshall.jaxb.Xacml20RequestContextUnmarshaller;
import org.xacml4j.v30.marshall.jaxb.Xacml20ResponseContextMarshaller;
import org.xacml4j.v30.pdp.PolicyDecisionPoint;


public class DomXacmlEndpoint extends AbstractDomPayloadEndpoint
{
	private PolicyDecisionPoint pdp;
	private RequestUnmarshaller requestUnmarshaller;
	private ResponseMarshaller responseMarshaller;

	public DomXacmlEndpoint(
			PolicyDecisionPoint pdp) {
		this.pdp = pdp;
		this.requestUnmarshaller = new Xacml20RequestContextUnmarshaller();
		this.responseMarshaller = new Xacml20ResponseContextMarshaller();
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

