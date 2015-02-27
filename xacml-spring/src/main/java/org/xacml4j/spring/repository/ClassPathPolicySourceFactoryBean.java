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

import java.io.IOException;
import java.io.InputStream;

import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.core.io.Resource;
import org.xacml4j.v30.spi.combine.DecisionCombiningAlgorithmProvider;
import org.xacml4j.v30.spi.combine.DecisionCombiningAlgorithmProviderBuilder;
import org.xacml4j.v30.spi.function.FunctionProvider;
import org.xacml4j.v30.spi.function.FunctionProviderBuilder;
import org.xacml4j.v30.spi.repository.ImmutablePolicySource;
import org.xacml4j.v30.spi.repository.PolicyRepository;

import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import org.xacml4j.v30.spi.repository.PolicySource;

public class ClassPathPolicySourceFactoryBean extends AbstractFactoryBean<PolicySource>
{
	private final String id;
	private Resource[] resources;
	private FunctionProvider extensionFunctions;
	private DecisionCombiningAlgorithmProvider extensionDecisionCombiningAlgorithms;

	public ClassPathPolicySourceFactoryBean(String id){
		Preconditions.checkNotNull(id);
		this.id = id;
	}

	@Override
	public Class<PolicySource> getObjectType() {
		return PolicySource.class;
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
	protected PolicySource createInstance() throws Exception
	{
		FunctionProviderBuilder functionProviderBuilder = FunctionProviderBuilder.builder()
			.defaultFunctions();
		if(extensionFunctions != null){
			functionProviderBuilder.provider(extensionFunctions);
		}
		DecisionCombiningAlgorithmProviderBuilder decisionAlgorithmProviderBuilder =
			DecisionCombiningAlgorithmProviderBuilder.builder()
			.defaultAlgorithms();
		if(extensionDecisionCombiningAlgorithms != null){
			decisionAlgorithmProviderBuilder.algorithmProvider(extensionDecisionCombiningAlgorithms);
		}
		Preconditions.checkState(resources != null, "Policy resources must be specified");
		ImmutablePolicySource.Builder builder = ImmutablePolicySource.builder(id);
		for(final Resource r : resources){
			builder.policy(new Supplier<InputStream>() {
                @Override
                public InputStream get() {
                    try {
                        return r.getInputStream();
                    } catch (IOException e) {
                        throw new IllegalArgumentException(
                                String.format("Could not import policy from resource \"%s\"", r),
                                e);
                    }
                }
            });
		}
		return builder.build();
	}
}
