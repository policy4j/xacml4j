package com.artagon.xacml.spring.pdp;

import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.config.AbstractFactoryBean;

import com.artagon.xacml.v30.spi.pdp.RequestContextHandler;
import com.artagon.xacml.v30.spi.pdp.RequestContextHandlerChain;
import com.google.common.base.Preconditions;

public class RequestContextHandlerChainFactoryBean extends AbstractFactoryBean<RequestContextHandlerChain>
{
	private Collection<RequestContextHandler> handlers;
	
	public void setHandlers(List<RequestContextHandler> handlers){
		Preconditions.checkNotNull(handlers);
		this.handlers = handlers;
	}

	@Override
	protected RequestContextHandlerChain createInstance() throws Exception {
		return new RequestContextHandlerChain(handlers);
	}

	@Override
	public Class<?> getObjectType() {
		return RequestContextHandlerChain.class;
	}
}
