package org.xacml4j.v30.pdp;

import java.util.LinkedList;
import java.util.List;

import javax.management.NotCompliantMBeanException;

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
import org.xacml4j.v30.spi.xpath.DefaultXPathProvider;
import org.xacml4j.v30.spi.xpath.XPathProvider;
import org.xacml4j.v30.types.XacmlTypes;

import com.google.common.base.Preconditions;

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
	private int defaultDecisionCacheTTL;

	private PolicyDecisionPointBuilder(String id){
		this();
		Preconditions.checkNotNull(id);
		this.id = id;
	}

	private PolicyDecisionPointBuilder(){
		this.xpathProvider = new DefaultXPathProvider();
		this.decisionAuditor = new NoAuditPolicyDecisionPointAuditor();
		this.decisionCache = new NoCachePolicyDecisionCache();
		this.handlers = new LinkedList<RequestContextHandler>();
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

	public PolicyDecisionPointBuilder decisionCacheTTL(
			int ttl){
		this.defaultDecisionCacheTTL = ttl;
		return this;
	}

	public PolicyDecisionPointBuilder rootPolicy(CompositeDecisionRule r)
	{
		Preconditions.checkNotNull(r);
		this.rootPolicy = r;
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

	public PolicyDecisionPoint build(){
		if(log.isDebugEnabled()){
			log.debug("Creating PDP=\"{}\"", id);
		}
		Preconditions.checkState(id != null);
		Preconditions.checkState(repository != null);
		Preconditions.checkState(pip != null);
		Preconditions.checkState(rootPolicy != null);
		Preconditions.checkState(id != null);
		RequestContextHandlerChain chain = new RequestContextHandlerChain(handlers);
		DefaultPolicyDecisionPointContextFactory factory = new DefaultPolicyDecisionPointContextFactory(
				rootPolicy, 
				repository, decisionAuditor,  decisionCache, xpathProvider, pip, chain);
		factory.setDefaultDecisionCacheTTL(defaultDecisionCacheTTL);
		try{
			return new DefaultPolicyDecisionPoint(id, factory);
		}catch(NotCompliantMBeanException e){
			throw new IllegalStateException(e);
		}
	}
}
