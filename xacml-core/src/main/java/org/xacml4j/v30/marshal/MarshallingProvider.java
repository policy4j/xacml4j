package org.xacml4j.v30.marshal;

/*
 * #%L
 * Xacml4J Core Engine Implementation
 * %%
 * Copyright (C) 2009 - 2023 Xacml4J.org
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

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.stream.Collectors;

import org.xacml4j.v30.policy.combine.DecisionCombiningAlgorithmProvider;
import org.xacml4j.v30.policy.function.FunctionProvider;


public interface MarshallingProvider
{
	MediaType getMediaType();

	default Optional<PolicyMarshaller> newPolicyMarshaller(){
		return Optional.empty();
	}
	default Optional<PolicyUnmarshaller> newPolicyUnmarshaller(FunctionProvider functionProvider,
	                                                           DecisionCombiningAlgorithmProvider algorithmProvider){
		return Optional.empty();
	}

	default Optional<PolicyUnmarshaller> newPolicyUnmarshaller(){
		return newPolicyUnmarshaller(
				FunctionProvider.builder()
				                .withDiscoveredFunctions()
				                .withDefaultFunctions()
				                .build(),
				DecisionCombiningAlgorithmProvider.builder()
				                                  .withDiscoveredAlgorithms()
				                                  .withDefaultAlgorithms()
				                                  .build());
	}
	default Optional<RequestMarshaller> newRequestMarshaller(){
		return Optional.empty();
	}
	default Optional<RequestUnmarshaller> newRequestUnmarshaller(){
		return Optional.empty();
	}

	default Optional<ResponseMarshaller> newResponseMarshaller(){
		return Optional.empty();
	}
	default Optional<ResponseUnmarshaller> newResponseUnmarshaller(){
		return Optional.empty();
	}

	static Map<MediaType, MarshallingProvider> discoverProviders(){
		return Collections.unmodifiableMap(ServiceLoader.load(MarshallingProvider.class)
		                                .stream()
		                                .map(p->p.get())
		                                .collect(Collectors.toMap(p->p.getMediaType(), p->p, (a,b)->a)));
	}

	static Optional<MarshallingProvider> getProvider(MediaType mediaType){
		return Optional.ofNullable(discoverProviders().get(mediaType));
	}

}
