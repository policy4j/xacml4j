package org.xacml4j.springws;

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.springframework.ws.server.endpoint.AbstractStaxStreamPayloadEndpoint;
import org.xacml4j.v30.RequestContext;
import org.xacml4j.v30.ResponseContext;
import org.xacml4j.v30.marshall.RequestUnmarshaller;
import org.xacml4j.v30.marshall.ResponseMarshaller;
import org.xacml4j.v30.marshall.jaxb.Xacml20RequestContextUnmarshaller;
import org.xacml4j.v30.marshall.jaxb.Xacml20ResponseContextMarshaller;
import org.xacml4j.v30.pdp.PolicyDecisionPoint;


public class StaxXacmlEndpoint extends AbstractStaxStreamPayloadEndpoint
{
	private PolicyDecisionPoint pdp;
	private RequestUnmarshaller requestUnmarshaller;
	private ResponseMarshaller responseMarshaller;

	public StaxXacmlEndpoint(
			PolicyDecisionPoint pdp) {
		this.pdp = pdp;
		this.requestUnmarshaller = new Xacml20RequestContextUnmarshaller();
		this.responseMarshaller = new Xacml20ResponseContextMarshaller();
	}

	@Override
	protected void invokeInternal(XMLStreamReader streamReader,
			XMLStreamWriter streamWriter) throws Exception
	{
		RequestContext request = requestUnmarshaller.unmarshal(streamReader);
		ResponseContext response = pdp.decide(request);
		responseMarshaller.marshal(response, streamWriter);
	}
}


