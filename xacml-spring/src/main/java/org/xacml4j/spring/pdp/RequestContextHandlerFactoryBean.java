package org.xacml4j.spring.pdp;

/*
 * #%L
 * Xacml4J Spring 3.x Support Module
 * %%
 * Copyright (C) 2009 - 2014 Xacml4J.org
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.xacml4j.v30.spi.pdp.RequestContextHandler;

import com.google.common.base.Preconditions;

public class RequestContextHandlerFactoryBean extends AbstractFactoryBean<RequestContextHandler>
{
	private RequestContextHandler ref;

	public void setRef(RequestContextHandler handler){
		Preconditions.checkNotNull(handler);
		this.ref = handler;
	}
	@Override
	protected RequestContextHandler createInstance() throws Exception {
		Preconditions.checkState(ref != null);
		return ref;
	}

	@Override
	public Class<?> getObjectType() {
		return RequestContextHandler.class;
	}

}
