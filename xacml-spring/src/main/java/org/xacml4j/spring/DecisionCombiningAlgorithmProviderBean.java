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

import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.xacml4j.v30.spi.combine.DecisionCombiningAlgorithmProvider;

import com.google.common.base.Preconditions;

public class DecisionCombiningAlgorithmProviderBean
	extends AbstractFactoryBean<DecisionCombiningAlgorithmProvider>
{
	private Class<?> providerClass;
	private DecisionCombiningAlgorithmProvider providerInstance;

	public void setClass(Class<?> clazz){
		Preconditions.checkNotNull(clazz);
		Preconditions.checkState(providerInstance == null,
				"Either provider instance or class can be specified, NOT both");
		this.providerClass = clazz;
	}

	public void setRef(DecisionCombiningAlgorithmProvider provider){
		Preconditions.checkNotNull(provider);
		Preconditions.checkState(providerClass == null,
				"Either provider instance or class can be specified, NOT both");
		this.providerInstance = provider;
	}

	@Override
	protected DecisionCombiningAlgorithmProvider createInstance()
			throws Exception {
		Preconditions.checkState(providerInstance != null);
		return providerInstance;
	}

	public Class<?> getProviderClass(){
		return providerClass;
	}

	@Override
	public Class<?> getObjectType() {
		return DecisionCombiningAlgorithmProvider.class;
	}
}
