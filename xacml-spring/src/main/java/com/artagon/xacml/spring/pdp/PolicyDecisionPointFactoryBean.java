package com.artagon.xacml.spring.pdp;

import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.factory.config.AbstractFactoryBean;

import com.artagon.xacml.v30.CompositeDecisionRule;
import com.artagon.xacml.v30.pdp.DefaultPolicyDecisionPoint;
import com.artagon.xacml.v30.pdp.DefaultPolicyDecisionPointContextFactory;
import com.artagon.xacml.v30.pdp.PolicyDecisionPoint;
import com.artagon.xacml.v30.pdp.PolicyDecisionPointContextFactory;
import com.artagon.xacml.v30.pdp.RequestContextHandler;
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

public class PolicyDecisionPointFactoryBean extends 
	AbstractFactoryBean<PolicyDecisionPoint>
{
	private PolicyInformationPoint pip;
	private CompositeDecisionRule policyDomain;
	private PolicyRepository policyRepository;
	private List<RequestContextHandler> handlers;
	private XPathProvider xpathProvider;
	private PolicyDecisionAuditor decisionAuditor;
	private PolicyDecisionCache decisionCache;
	
	public PolicyDecisionPointFactoryBean(){
		this.handlers = new LinkedList<RequestContextHandler>();
		this.xpathProvider = new DefaultXPathProvider();
		this.decisionAuditor = new NoAuditPolicyDecisionPointAuditor();
		this.decisionCache = new NoCachePolicyDecisionCache();
	}
	
	@Override
	public Class<PolicyDecisionPoint> getObjectType() {
		return PolicyDecisionPoint.class;
	}
	
	public void setXPathProvider(XPathProvider xpath){
		Preconditions.checkNotNull(xpath);
		this.xpathProvider = xpath;
	}
	
	public void setDecisionAuditor(PolicyDecisionAuditor auditor){
		Preconditions.checkNotNull(auditor);
		this.decisionAuditor = auditor;
	}
	
	public void setDecisionCache(PolicyDecisionCache cache){
		Preconditions.checkNotNull(cache);
		this.decisionCache = cache;
	}
	
	public void setPolicyInformationPoint(PolicyInformationPoint pip){
		this.pip = pip;
	}
		
	public void setDomainPolicy(CompositeDecisionRule policyStore){
		this.policyDomain = policyStore;
	}
	
	public void setPolicyRepository(PolicyRepository policyRepository)
	{
		this.policyRepository = policyRepository;
	}
	
	public void setHandlers(List<RequestContextHandler> handlers){
		this.handlers.addAll(handlers);
	}
	
	@Override
	protected PolicyDecisionPoint createInstance() throws Exception 
	{
		Preconditions.checkState(pip != null);
		Preconditions.checkState(policyDomain != null);
		Preconditions.checkState(policyRepository != null);
		PolicyDecisionPointContextFactory factory = new DefaultPolicyDecisionPointContextFactory(policyDomain,
				policyRepository,
				decisionAuditor, 
				decisionCache, 
				xpathProvider, 
				pip, handlers);
		return new DefaultPolicyDecisionPoint(factory);
	}
}
