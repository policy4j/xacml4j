package org.xacml4j.jmh.testing;

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

import java.util.Collection;
import java.util.Optional;

import org.xacml4j.jmh.harness.CorpusRequest;
import org.xacml4j.v30.CompositeDecisionRule;
import org.xacml4j.v30.PolicyDecisionPoint;
import org.xacml4j.v30.Result;
import org.xacml4j.v30.pdp.PolicyDecisionPointBuilder;
import org.xacml4j.v30.spi.pip.PolicyInformationPoint;
import org.xacml4j.v30.spi.repository.PolicyRepository;

/**
 * PDPs outside a fixture, with the setup of design D6, for tests that
 * compare a fixture with what the PDP does on its own.
 */
public final class TestPdps
{
	private TestPdps(){
	}

	/**
	 * @param repository the repository the PDP resolves references in; the
	 * PDP gets a new reference resolver, so its reference cache starts empty
	 * @param root the root policy
	 * @return a PDP without a decision cache and without an auditor (D6)
	 */
	public static PolicyDecisionPoint build(PolicyRepository repository, CompositeDecisionRule root){
		return PolicyDecisionPointBuilder.builder("jmh-test")
				.policyRepository(repository)
				.pip(PolicyInformationPoint.builder("jmh-test").withDefaultRegistry().noCacheProvider().build())
				.defaultRequestHandlers()
				.rootPolicy(root)
				.build();
	}

	/**
	 * What a fixture must report for a request: nothing when the PDP gives
	 * the expected decision, otherwise the class of the exception or the
	 * decision that the PDP gave.
	 *
	 * @param pdp the PDP
	 * @param request the request and its expected decision
	 * @return the outcome
	 */
	public static Outcome decide(PolicyDecisionPoint pdp, CorpusRequest request){
		try{
			Collection<Result> results = pdp.decide(request.request()).getResults();
			if(results.size() != 1){
				return new Outcome(request.id(), Optional.of(String.valueOf(results.size())), Optional.empty());
			}
			Result result = results.iterator().next();
			return request.matches(result.getDecision()) ? new Outcome(request.id(), Optional.empty(), Optional.empty())
					: new Outcome(request.id(), Optional.of(String.valueOf(result.getDecision())), Optional.empty());
		}catch(RuntimeException e){
			return new Outcome(request.id(), Optional.empty(), Optional.of(e.getClass()));
		}
	}

	/**
	 * @param id the request id
	 * @param wrongResult the decision, or the number of results when it is not one, where it differs from the expectation
	 * @param thrown the class of the exception that {@code decide} threw
	 */
	public record Outcome(String id, Optional<String> wrongResult, Optional<Class<? extends RuntimeException>> thrown)
	{
		/** @return whether a fixture must name this request */
		public boolean failed(){
			return wrongResult.isPresent() || thrown.isPresent();
		}
	}
}
