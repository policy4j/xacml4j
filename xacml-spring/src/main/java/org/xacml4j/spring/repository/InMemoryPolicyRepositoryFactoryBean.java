package org.xacml4j.spring.repository;

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
import org.springframework.core.io.Resource;
import org.xacml4j.v30.spi.combine.DecisionCombiningAlgorithmProvider;
import org.xacml4j.v30.spi.combine.DecisionCombiningAlgorithmProviderBuilder;
import org.xacml4j.v30.spi.function.FunctionProvider;
import org.xacml4j.v30.spi.function.FunctionProviderBuilder;
import org.xacml4j.v30.spi.repository.InMemoryPolicyRepository;
import org.xacml4j.v30.spi.repository.PolicyRepository;

import com.google.common.base.Preconditions;

public class InMemoryPolicyRepositoryFactoryBean extends AbstractFactoryBean<PolicyRepository>
{
	private final String id;
	private Resource[] resources;
	private FunctionProvider extensionFunctions;
	private DecisionCombiningAlgorithmProvider extensionDecisionCombiningAlgorithms;

	public InMemoryPolicyRepositoryFactoryBean(String id){
		Preconditions.checkNotNull(id);
		this.id = id;
	}

	@Override
	public Class<PolicyRepository> getObjectType() {
		return PolicyRepository.class;
	}

	public void setExtensionFunctions(FunctionProvider functions){
		this.extensionFunctions = functions;
	}

	public void setExtensionCombiningAlgorithms(
			DecisionCombiningAlgorithmProvider algorithms){
		this.extensionDecisionCombiningAlgorithms = algorithms;
	}

	public void setPolicies(Resource[] policies){
		this.resources = policies;
	}

	@Override
	protected PolicyRepository createInstance() throws Exception
	{
		FunctionProviderBuilder functionProviderBuilder = FunctionProviderBuilder.builder()
			.defaultFunctions();
		if(extensionFunctions != null){
			functionProviderBuilder.provider(extensionFunctions);
		}
		DecisionCombiningAlgorithmProviderBuilder decisionAlgorithmProviderBuilder =
			DecisionCombiningAlgorithmProviderBuilder.builder()
			.withDefaultAlgorithms();
		if(extensionDecisionCombiningAlgorithms != null){
			decisionAlgorithmProviderBuilder.withAlgorithmProvider(extensionDecisionCombiningAlgorithms);
		}
		Preconditions.checkState(resources != null, "Policy resources must be specified");
		InMemoryPolicyRepository repository = new InMemoryPolicyRepository(
				id, functionProviderBuilder.build(), decisionAlgorithmProviderBuilder.create());
		for(Resource r : resources){
			repository.importPolicy(r.getInputStream());
		}
		return repository;
	}
}
