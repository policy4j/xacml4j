package org.xacml4j.spring.pip;

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

import com.google.common.base.Preconditions;

public class ResolverRegistrationFactoryBean
extends AbstractFactoryBean<ResolverRegistration>
{
	private String policyId;
	private Object instance;

	/**
	 * Sets optionally resolver policy
	 * or policy set identifier
	 * @param policyId a policy or policy set identifier
	 */
	public void setPolicyId(String policyId){
		this.policyId = policyId;
	}

	/**
	 * Sets actual resolver instance
	 *
	 * @param instance a resolver bean instance
	 */
	public void setResolver(Object instance){
		this.instance = instance;
	}

	@Override
	protected ResolverRegistration createInstance() throws Exception
	{
		Preconditions.checkState(instance != null,
				"Resolver instance can not be null");
		return new ResolverRegistration(policyId, instance);
	}

	@Override
	public Class<ResolverRegistration> getObjectType() {
		return ResolverRegistration.class;
	}
}


