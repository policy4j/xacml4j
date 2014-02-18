package org.xacml4j.springws;


import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xacml4j.v30.RequestContext;
import org.xacml4j.v30.ResponseContext;
import org.xacml4j.v30.marshal.RequestUnmarshaller;
import org.xacml4j.v30.marshal.ResponseMarshaller;
import org.xacml4j.v30.marshal.jaxb.Xacml20RequestContextUnmarshaller;
import org.xacml4j.v30.marshal.jaxb.Xacml20ResponseContextMarshaller;
import org.xacml4j.v30.pdp.PolicyDecisionPoint;
import org.xacml4j.v30.types.Types;


@Endpoint
public class DomXacmlEndpoint {

	private final PolicyDecisionPoint pdp;
	private final RequestUnmarshaller xacml20RequestUnmarshaller;
	private final ResponseMarshaller xacml20ResponseMarshaller;

	public DomXacmlEndpoint(
			PolicyDecisionPoint pdp) {
		this.pdp = pdp;
		Types types = Types.builder().defaultTypes().create();
		this.xacml20RequestUnmarshaller = new Xacml20RequestContextUnmarshaller(types);
		this.xacml20ResponseMarshaller = new Xacml20ResponseContextMarshaller(types);
	}

	@PayloadRoot(namespace ="urn:oasis:names:tc:xacml:2.0:context:schema:os", localPart = "Request")
	public Element processXacml20Request(Element requestElement,
			Document responseDocument) throws Exception {
		RequestContext request = xacml20RequestUnmarshaller.unmarshal(requestElement);
		ResponseContext response = pdp.decide(request);
		xacml20ResponseMarshaller.marshal(response, responseDocument);
		return responseDocument.getDocumentElement();
	}
}

