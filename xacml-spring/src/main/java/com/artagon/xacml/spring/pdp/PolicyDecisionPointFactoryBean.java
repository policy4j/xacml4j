package com.artagon.xacml.spring.pdp;

import java.util.List;

import org.springframework.beans.factory.config.AbstractFactoryBean;

import com.artagon.xacml.v30.CompositeDecisionRule;
import com.artagon.xacml.v30.pdp.PolicyDecisionPoint;
import com.artagon.xacml.v30.pdp.PolicyDecisionPointBuilder;
import com.artagon.xacml.v30.pdp.RequestContextHandler;
import com.artagon.xacml.v30.spi.audit.PolicyDecisionAuditor;
import com.artagon.xacml.v30.spi.dcache.PolicyDecisionCache;
import com.artagon.xacml.v30.spi.pip.PolicyInformationPoint;
import com.artagon.xacml.v30.spi.repository.PolicyRepository;
import com.artagon.xacml.v30.spi.xpath.XPathProvider;

public class PolicyDecisionPointFactoryBean extends 
	AbstractFactoryBean<PolicyDecisionPoint>
{
	
	private PolicyDecisionPointBuilder pdpBuilder;
	
	public PolicyDecisionPointFactoryBean(){
		this.pdpBuilder = PolicyDecisionPointBuilder.builder();
	}
	
	@Override
	public Class<PolicyDecisionPoint> getObjectType() {
		return PolicyDecisionPoint.class;
	}
	
	public void setXPathProvider(XPathProvider xpath){
		this.pdpBuilder.withXPathProvider(xpath);
	}
	
	public void setDecisionAuditor(PolicyDecisionAuditor auditor){
		this.pdpBuilder.withDecisionAuditor(auditor);
	}
	
	public void setDecisionCache(PolicyDecisionCache cache){
		this.pdpBuilder.withDecisionCache(cache);
	}
	
	public void setPolicyInformationPoint(PolicyInformationPoint pip){
		this.pdpBuilder.withPolicyInformationPoint(pip);
	}
		
	public void setDomainPolicy(CompositeDecisionRule policyStore){
		this.pdpBuilder.withRootPolicy(policyStore);
	}
	
	public void setPolicyRepository(PolicyRepository policyRepository)
	{
		this.pdpBuilder.withPolicyRepository(policyRepository);
	}
	
	public void setHandlers(List<RequestContextHandler> handlers){
		for(RequestContextHandler handler : handlers){
			pdpBuilder.withRequestHandler(handler);
		}
	}
	
	@Override
	protected PolicyDecisionPoint createInstance() throws Exception 
	{
		return pdpBuilder.build();
	}
}
