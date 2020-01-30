package org.xacml4j.v30.pdp;

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

import com.codahale.metrics.MetricRegistry;
import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xacml4j.v30.CompositeDecisionRule;
import org.xacml4j.v30.pdp.profiles.MultipleResourcesHandler;
import org.xacml4j.v30.spi.audit.NoAuditPolicyDecisionPointAuditor;
import org.xacml4j.v30.spi.audit.PolicyDecisionAuditor;
import org.xacml4j.v30.spi.pdp.NoCachePolicyDecisionCache;
import org.xacml4j.v30.spi.pdp.PolicyDecisionCache;
import org.xacml4j.v30.spi.pdp.RequestContextHandler;
import org.xacml4j.v30.spi.pdp.RequestContextHandlerChain;
import org.xacml4j.v30.spi.pip.PolicyInformationPoint;
import org.xacml4j.v30.spi.repository.PolicyRepository;
import org.xacml4j.v30.XPathProvider;

import java.util.LinkedList;
import java.util.List;

public final class PolicyDecisionPointBuilder
{
	private final static Logger log = LoggerFactory.getLogger(PolicyDecisionPointBuilder.class);

	private String id;
	private XPathProvider xpathProvider;
	private PolicyDecisionAuditor decisionAuditor;
	private PolicyDecisionCache decisionCache;
	private PolicyRepository repository;
	private PolicyInformationPoint pip;
	private CompositeDecisionRule rootPolicy;
	private List<RequestContextHandler> handlers;
	private MetricRegistry metricRegistry;

	private PolicyDecisionPointBuilder(String id){
		this();
		Preconditions.checkNotNull(id);
		this.id = id;
	}

	private PolicyDecisionPointBuilder(){
		this.xpathProvider = XPathProvider.defaultProvider();
		this.decisionAuditor = new NoAuditPolicyDecisionPointAuditor();
		this.decisionCache = new NoCachePolicyDecisionCache();
		this.metricRegistry = new MetricRegistry();
		this.handlers = new LinkedList<>();
	}

	public static PolicyDecisionPointBuilder builder(String id){
		return new PolicyDecisionPointBuilder(id);
	}

	public static PolicyDecisionPointBuilder builder(){
		return new PolicyDecisionPointBuilder();
	}

	public PolicyDecisionPointBuilder decisionCache(
			PolicyDecisionCache cache){
		Preconditions.checkNotNull(cache);
		this.decisionCache = cache;
		return this;
	}

	public PolicyDecisionPointBuilder rootPolicy(CompositeDecisionRule r)
	{
		Preconditions.checkNotNull(r);
		this.rootPolicy = r;
		return this;
	}

	public PolicyDecisionPointBuilder metrics(MetricRegistry r)
	{
		Preconditions.checkNotNull(r);
		this.metricRegistry = r;
		return this;
	}

	public PolicyDecisionPointBuilder policyRepository(
			PolicyRepository repository){
		Preconditions.checkNotNull(repository);
		this.repository = repository;
		return this;
	}

	public PolicyDecisionPointBuilder requestHandler(
			RequestContextHandler handler){
		Preconditions.checkNotNull(handler);
		this.handlers.add(handler);
		return this;
	}

	public PolicyDecisionPointBuilder defaultRequestHandlers(){
		return requestHandler(new MultipleResourcesHandler());
	}

	public PolicyDecisionPointBuilder pip(PolicyInformationPoint pip){
		Preconditions.checkNotNull(pip);
		this.pip = pip;
		return this;
	}

	public PolicyDecisionPointBuilder decisionAuditor(
			PolicyDecisionAuditor auditor){
		Preconditions.checkNotNull(auditor);
		this.decisionAuditor = auditor;
		return this;
	}
	
	public PolicyDecisionPointBuilder xpathProvider(
			XPathProvider xpath){
		Preconditions.checkNotNull(xpath);
		this.xpathProvider = xpath;
		return this;
	}

	public PolicyDecisionPoint build() {
		if (log.isDebugEnabled()) {
			log.debug("Creating PDP=\"{}\"", id);
		}
		Preconditions.checkState(id != null);
		Preconditions.checkState(repository != null);
		Preconditions.checkState(pip != null);
		Preconditions.checkState(rootPolicy != null);
		Preconditions.checkState(id != null);
		Preconditions.checkState(metricRegistry != null);
		return new DefaultPolicyDecisionPoint(id, metricRegistry,
				new DefaultPolicyDecisionPointContextFactory(
						rootPolicy,
						repository,
						decisionAuditor,
						decisionCache,
						xpathProvider,
						pip,
						new RequestContextHandlerChain(handlers)));
	}
}
