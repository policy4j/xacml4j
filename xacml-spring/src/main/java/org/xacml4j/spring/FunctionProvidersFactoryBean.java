package org.xacml4j.spring;

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

import java.util.Collection;

import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.xacml4j.v30.spi.function.FunctionProviderBuilder;

import com.google.common.base.Preconditions;

public class FunctionProvidersFactoryBean extends AbstractFactoryBean<org.xacml4j.v30.spi.function.FunctionProvider>
{
	private FunctionProviderBuilder builder;

	public FunctionProvidersFactoryBean() throws Exception
	{
		this.builder = FunctionProviderBuilder.builder();
	}

	public void setProviders(Collection<FunctionProvider> providers){
		Preconditions.checkNotNull(providers);
		for(FunctionProvider p : providers){
			Preconditions.checkState(p.getProviderClass() != null
					|| p.getProviderInstance() != null);
			if(p.getProviderClass() != null){
				builder.fromClass(p.getProviderClass());
			}
			if(p.getProviderInstance() != null){
				builder.fromInstance(p.getProviderInstance());
			}
		}
	}

	@Override
	public Class<org.xacml4j.v30.spi.function.FunctionProvider> getObjectType() {
		return org.xacml4j.v30.spi.function.FunctionProvider.class;
	}

	@Override
	protected org.xacml4j.v30.spi.function.FunctionProvider createInstance() throws Exception {
		return builder.build();
	}
}
