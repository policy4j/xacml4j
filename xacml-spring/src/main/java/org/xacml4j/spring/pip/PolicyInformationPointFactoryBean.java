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
import org.xacml4j.v30.spi.pip.PolicyInformationPoint;
import org.xacml4j.v30.spi.pip.PolicyInformationPointBuilder;
import org.xacml4j.v30.spi.pip.PolicyInformationPointCacheProvider;
import org.xacml4j.v30.spi.pip.ResolverRegistry;

import com.google.common.base.Preconditions;

public class PolicyInformationPointFactoryBean
	extends AbstractFactoryBean<PolicyInformationPoint>
{
	private ResolverRegistry registry;
	private PolicyInformationPointBuilder pipBuilder;

	public PolicyInformationPointFactoryBean(String id){
		this.pipBuilder = PolicyInformationPointBuilder.builder(id);
	}
	public void setCache(PolicyInformationPointCacheProvider cache){
		pipBuilder.withCacheProvider(cache);
	}

	public void setResolvers(ResolverRegistry registry){
		Preconditions.checkNotNull(registry);
		this.registry = registry;
	}

	@Override
	public Class<PolicyInformationPoint> getObjectType() {
		return PolicyInformationPoint.class;
	}

	@Override
	protected PolicyInformationPoint createInstance()
		throws Exception
	{
		Preconditions.checkState(registry != null);
		return pipBuilder.build(registry);
	}
}
