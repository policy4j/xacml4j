package com.artagon.xacml.v30.pdp;

import java.util.LinkedList;
import java.util.List;

import com.artagon.xacml.v30.CompositeDecisionRule;
import com.artagon.xacml.v30.pdp.profiles.MultipleResourcesHandler;
import com.artagon.xacml.v30.spi.audit.NoAuditPolicyDecisionPointAuditor;
import com.artagon.xacml.v30.spi.audit.PolicyDecisionAuditor;
import com.artagon.xacml.v30.spi.dcache.NoCachePolicyDecisionCache;
import com.artagon.xacml.v30.spi.dcache.PolicyDecisionCache;
import com.artagon.xacml.v30.spi.pip.PolicyInformationPoint;
import com.artagon.xacml.v30.spi.repository.PolicyRepository;
import com.artagon.xacml.v30.spi.xpath.DefaultXPathProvider;
import com.artagon.xacml.v30.spi.xpath.XPathProvider;
import com.google.common.base.Preconditions;

public class PolicyDecisionPointBuilder 
{
	private XPathProvider xpathProvider;
	private PolicyDecisionAuditor decisionAuditor;
	private PolicyDecisionCache decisionCache;
	private PolicyRepository repository;
	private PolicyInformationPoint pip;
	private CompositeDecisionRule rootPolicy;
	private List<RequestContextHandler> handlers;
	
	private PolicyDecisionPointBuilder(){
		this.xpathProvider = new DefaultXPathProvider();
		this.decisionAuditor = new NoAuditPolicyDecisionPointAuditor();
		this.decisionCache = new NoCachePolicyDecisionCache();
		this.handlers = new LinkedList<RequestContextHandler>();
	}
	
	public static PolicyDecisionPointBuilder builder(){
		return new PolicyDecisionPointBuilder();
	}
	
	public PolicyDecisionPointBuilder withDecisionCache(
			PolicyDecisionCache cache){
		Preconditions.checkNotNull(cache);
		this.decisionCache = cache;
		return this;
	}
	
	public PolicyDecisionPointBuilder withRootPolicy(CompositeDecisionRule r)
	{
		Preconditions.checkNotNull(r);
		this.rootPolicy = r;
		return this;
	}
	
	public PolicyDecisionPointBuilder withPolicyRepository(
			PolicyRepository repository){
		Preconditions.checkNotNull(repository);
		this.repository = repository;
		return this;
	}
	
	public PolicyDecisionPointBuilder withRequestHandler(
			RequestContextHandler handler){
		Preconditions.checkNotNull(handler);
		this.handlers.add(handler);
		return this;
	}
	
	public PolicyDecisionPointBuilder withDefaultRequestHandlers(){
		return withRequestHandler(new MultipleResourcesHandler());
	}
	
	public PolicyDecisionPointBuilder withPolicyInformationPoint(
			PolicyInformationPoint pip){
		Preconditions.checkNotNull(pip);
		this.pip = pip;
		return this;
	}
	
	public PolicyDecisionPointBuilder withDecisionAuditor(
			PolicyDecisionAuditor auditor){
		Preconditions.checkNotNull(auditor);
		this.decisionAuditor = auditor;
		return this;
	}
	
	public PolicyDecisionPointBuilder withXPathProvider(
			XPathProvider xpath){
		Preconditions.checkNotNull(xpath);
		this.xpathProvider = xpath;
		return this;
	}
	
	public PolicyDecisionPoint build(){
		Preconditions.checkState(repository != null);
		Preconditions.checkState(pip != null);
		Preconditions.checkState(rootPolicy != null);
		RequestContextHandlerChain chain = new RequestContextHandlerChain(handlers);
		DefaultPolicyDecisionPointContextFactory factory = new DefaultPolicyDecisionPointContextFactory(
				rootPolicy, repository, decisionAuditor,  decisionCache, xpathProvider, pip, chain);
		return new DefaultPolicyDecisionPoint(factory);
	}
}
