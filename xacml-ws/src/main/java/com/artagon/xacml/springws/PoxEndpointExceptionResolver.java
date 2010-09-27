package com.artagon.xacml.springws;

import java.io.IOException;

import org.springframework.ws.context.MessageContext;
import org.springframework.ws.pox.PoxMessage;
import org.springframework.ws.server.endpoint.AbstractEndpointExceptionResolver;
import org.springframework.ws.transport.context.TransportContext;
import org.springframework.ws.transport.context.TransportContextHolder;
import org.springframework.ws.transport.http.HttpServletConnection;

public class PoxEndpointExceptionResolver extends AbstractEndpointExceptionResolver
{
	@Override
	protected boolean resolveExceptionInternal(MessageContext context,
			Object endpoint, Exception ex) 
	{
		if(!(context.getRequest() instanceof PoxMessage)){
			return false;
		}
		TransportContext httpContext = TransportContextHolder.getTransportContext();
		HttpServletConnection connection = (HttpServletConnection)httpContext.getConnection();
		try{
			connection.setFault(true);
		}catch(IOException e){
		}
		return true;
	}
	
}
