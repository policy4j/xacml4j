package org.xacml4j.jmh.harness;

/*
 * #%L
 * Xacml4J JMH Benchmarks
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

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Policies and requests that a benchmark decides.
 *
 * @param name the corpus name, as selected (for example {@code synthetic:100})
 * @param policies the policy documents
 * @param root the name of the policy document that holds the root policy
 * @param requests the requests with their expected decisions
 */
public record PolicyCorpus(String name, List<PolicySource> policies, String root, List<CorpusRequest> requests)
{
	public PolicyCorpus {
		Objects.requireNonNull(name, "name");
		Objects.requireNonNull(root, "root");
		policies = List.copyOf(policies);
		requests = List.copyOf(requests);
		if(policies.isEmpty()){
			throw new IllegalArgumentException("Corpus \"" + name + "\" has no policy");
		}
		if(requests.isEmpty()){
			throw new IllegalArgumentException("Corpus \"" + name + "\" has no request");
		}
		Set<String> names = new HashSet<>();
		for(PolicySource source : policies){
			if(!names.add(source.name())){
				throw new IllegalArgumentException("Corpus \"" + name + "\" has two policies named \"" + source.name() + "\"");
			}
		}
		if(!names.contains(root)){
			throw new IllegalArgumentException("Corpus \"" + name + "\" has no root policy \"" + root + "\"");
		}
		Set<String> ids = new HashSet<>();
		for(CorpusRequest request : requests){
			if(!ids.add(request.id())){
				throw new IllegalArgumentException("Corpus \"" + name + "\" has two requests \"" + request.id() + "\"");
			}
		}
	}

	/** @return the index of the root document in {@link #policies()} */
	public int rootIndex(){
		for(int i = 0; i < policies.size(); i++){
			if(policies.get(i).name().equals(root)){
				return i;
			}
		}
		throw new IllegalStateException("Corpus \"" + name + "\" has no root policy \"" + root + "\"");
	}
}
