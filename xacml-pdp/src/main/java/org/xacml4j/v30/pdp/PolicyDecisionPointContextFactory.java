package org.xacml4j.v30.pdp;

import java.time.Clock;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xacml4j.v30.CompositeDecisionRule;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.Result;
import org.xacml4j.v30.XPathVersion;
import org.xacml4j.v30.content.XPathProvider;
import org.xacml4j.v30.policy.EvaluationContextHandler;
import org.xacml4j.v30.policy.PreparedPolicySets;
import org.xacml4j.v30.policy.RootEvaluationContext;
import org.xacml4j.v30.RequestContext;
import org.xacml4j.v30.spi.audit.PolicyDecisionAuditor;
import org.xacml4j.v30.spi.observe.DecisionObservation;
import org.xacml4j.v30.spi.pdp.PolicyDecisionCache;
import org.xacml4j.v30.spi.pdp.RequestContextHandlerChain;
import org.xacml4j.v30.spi.pip.PolicyInformationPoint;
import org.xacml4j.v30.spi.repository.DefaultPolicyReferenceResolver;
import org.xacml4j.v30.spi.repository.PolicyRepository;

import com.google.common.base.Preconditions;

/*
 * #%L
 * Xacml4J Core Engine Implementation
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
@FunctionalInterface
public interface PolicyDecisionPointContextFactory
{

	/**
	 * Creates policy decision context
	 *
	 * @param pdp a PDP callback
	 * @return {@link PolicyDecisionPointContext} defaultProvider
	 */
	PolicyDecisionPointContext createContext(PolicyDecisionCallback pdp);

	public
	final class DefaultPolicyDecisionPointContextFactory
		implements PolicyDecisionPointContextFactory
	{
		/** The logger of the decision point, whose debug line prints the count of the applicable policies (design D32) */
		private static final Logger DECISIONS = LoggerFactory.getLogger(DefaultPolicyDecisionPoint.class);

		/** Fixed when the PDP is built */
		private final String correlationPrefix = Long.toHexString(System.currentTimeMillis());
		/** Numbers the decide calls of this PDP from 1, without a random number generator. */
		private final AtomicLong correlationSequence = new AtomicLong();

		private PolicyInformationPoint pip;
		private PolicyDecisionAuditor decisionAuditor;
		private PolicyDecisionCache decisionCache;
		private XPathProvider xpathProvider;
		private final RootEvaluationContext.Configuration evaluationConfiguration;
		private final Optional<PreparedPolicySets> prepared;
		private final PolicyRepository repository;
		private final DefaultPolicyReferenceResolver policyReferenceResolver;
		private final Optional<PreparedPolicySetsListener> preparedListener;
		private boolean closed;
		private CompositeDecisionRule policyDomain;
		private RequestContextHandlerChain requestHandlers;

		private boolean decisionCacheEnabled = true;
		private boolean decisionAuditEnabled = true;
		private boolean validateFuncParamsAtRuntime = false;

		DefaultPolicyDecisionPointContextFactory(
				CompositeDecisionRule policyDomain,
				PolicyRepository repository,
				PolicyDecisionAuditor auditor,
				PolicyDecisionCache cache,
				XPathProvider xpathProvider,
				PolicyInformationPoint pip,
				RequestContextHandlerChain handlerChain)
		{
			this(policyDomain, repository, auditor, cache, xpathProvider, pip, handlerChain, Optional.empty());
		}

		DefaultPolicyDecisionPointContextFactory(
				CompositeDecisionRule policyDomain,
				PolicyRepository repository,
				PolicyDecisionAuditor auditor,
				PolicyDecisionCache cache,
				XPathProvider xpathProvider,
				PolicyInformationPoint pip,
				RequestContextHandlerChain handlerChain,
				PreparedPolicySets prepared)
		{
			this(policyDomain, repository, auditor, cache, xpathProvider, pip, handlerChain, Optional.of(prepared));
		}

		private DefaultPolicyDecisionPointContextFactory(
				CompositeDecisionRule policyDomain,
				PolicyRepository repository,
				PolicyDecisionAuditor auditor,
				PolicyDecisionCache cache,
				XPathProvider xpathProvider,
				PolicyInformationPoint pip,
				RequestContextHandlerChain handlerChain,
				Optional<PreparedPolicySets> prepared)
		{
			Preconditions.checkArgument(policyDomain != null);
			Preconditions.checkArgument(repository != null);
			Preconditions.checkArgument(xpathProvider != null);
			Preconditions.checkArgument(pip != null);
			Preconditions.checkArgument(auditor != null);
			Preconditions.checkArgument(cache != null);
			this.prepared = prepared;
			this.repository = repository;
			this.policyReferenceResolver = prepared.isPresent()
					? new DefaultPolicyReferenceResolver(repository, false, 1024)
					: new DefaultPolicyReferenceResolver(repository);
			this.evaluationConfiguration = new RootEvaluationContext.Configuration(
					Clock.systemUTC(), Duration.ofSeconds(30), XPathVersion.XPATH1, policyReferenceResolver);
			this.preparedListener = prepared.map(PreparedPolicySetsListener::new);
			preparedListener.ifPresent(repository::addPolicyRepositoryListener);
			this.pip = pip;
			this.xpathProvider = xpathProvider;
			this.policyDomain = policyDomain;
			this.decisionAuditor = auditor;
			this.decisionCache = cache;
			this.requestHandlers = handlerChain;
		}

		synchronized void close(){
			if(!closed){
				preparedListener.ifPresent(repository::removePolicyRepositoryListener);
				repository.removePolicyRepositoryListener(policyReferenceResolver);
				closed = true;
			}
		}

		/** The correlation identifier of a decide call, formatted when it is read (design D34) */
		String getCorrelationId(long correlation){
			return correlationPrefix + "-" + Long.toHexString(correlation);
		}

		@Override
		public PolicyDecisionPointContext createContext(final PolicyDecisionCallback pdp)
		{
			return createContext(pdp, nextCorrelation());
		}

		long nextCorrelation(){
			return correlationSequence.incrementAndGet();
		}

		boolean canHandleDirectly(RequestContext request){
			return request != null && request.getClass() == RequestContext.class
					&& !request.requiresExpansion() && !request.isCombinedDecision()
					&& requestHandlers != null && requestHandlers.canHandleSingleRequestDirectly();
		}

		PolicyDecisionPointContext createContext(final PolicyDecisionCallback pdp, final long correlation)
		{
			return new PolicyDecisionPointContext() {

				@Override
				public boolean isDecisionCacheEnabled(){
					return decisionCacheEnabled;
				}

				@Override
				public boolean isValidateFuncParamsAtRuntime(){
					return validateFuncParamsAtRuntime;
				}

				@Override
				public boolean isDecisionAuditEnabled(){
					return decisionAuditEnabled;
				}

				@Override
				public CompositeDecisionRule getDomainPolicy() {
					return policyDomain;
				}



				@Override
				public PolicyDecisionCache getDecisionCache() {
					return decisionCache;
				}

				@Override
				public PolicyDecisionAuditor getDecisionAuditor() {
					return decisionAuditor;
				}

				@Override
				public Result requestDecision(RequestContext req) {

					return pdp.requestDecision(this, req);
				}

				@Override
				public RequestContextHandlerChain getRequestHandlers() {
					return requestHandlers;
				}

				@Override
				public String getCorrelationId() {
					return DefaultPolicyDecisionPointContextFactory.this.getCorrelationId(correlation);
				}

				@Override
				public long getCorrelation() {
					return correlation;
				}

				@Override
				public EvaluationContext createEvaluationContext(RequestContext request)
				{
					return createEvaluationContext(request, DecisionObservation.none());
				}

				@Override
				public EvaluationContext createEvaluationContext(RequestContext request, DecisionObservation observation)
				{
					Preconditions.checkNotNull(request);
					Preconditions.checkArgument(
							!request.containsRepeatingCategories(),
							"RequestContext has repeating attributes categories");
					Preconditions.checkArgument(
							!request.containsRequestReferences(),
							"RequestContext contains multiple request references");
					// The handler takes the request itself: no callback per decision (cycle AH)
					EvaluationContextHandler handler = new DefaultEvaluationContextHandler(request, pip);
					// The applicable policies are recorded only for a policy identifier list or
					// for the decision point's debug line (design D32)
					boolean record = request.isReturnPolicyIdList() || DECISIONS.isDebugEnabled();
					if(prepared.isPresent()){
						return new RootEvaluationContext(evaluationConfiguration, validateFuncParamsAtRuntime,
								handler, record, prepared.orElseThrow(), observation);
					}
					return new RootEvaluationContext(evaluationConfiguration, validateFuncParamsAtRuntime,
							handler, record, observation);
				}
			};
		}
	}
}
