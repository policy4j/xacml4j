package com.artagon.xacml.springws;

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.springframework.ws.server.endpoint.AbstractStaxStreamPayloadEndpoint;

import com.artagon.xacml.v20.Xacml20RequestUnmarshaller;
import com.artagon.xacml.v20.Xacml20ResponseMarshaller;
import com.artagon.xacml.v3.RequestContext;
import com.artagon.xacml.v3.ResponseContext;
import com.artagon.xacml.v3.marshall.RequestUnmarshaller;
import com.artagon.xacml.v3.marshall.ResponseMarshaller;
import com.artagon.xacml.v3.pdp.PolicyDecisionPoint;

public class XacmlSpringWsStaxEndpoint extends AbstractStaxStreamPayloadEndpoint 
{
	private PolicyDecisionPoint pdp;
	private RequestUnmarshaller requestUnmarshaller;
	private ResponseMarshaller responseMarshaller;
	
	public XacmlSpringWsStaxEndpoint( 
			PolicyDecisionPoint pdp) {
		this.pdp = pdp;
		this.requestUnmarshaller = new Xacml20RequestUnmarshaller();
		this.responseMarshaller = new Xacml20ResponseMarshaller();		
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


