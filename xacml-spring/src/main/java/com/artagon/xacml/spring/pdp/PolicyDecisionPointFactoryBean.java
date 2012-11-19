package com.artagon.xacml.spring.pdp;

import java.util.List;

import org.springframework.beans.factory.config.AbstractFactoryBean;

import com.artagon.xacml.v30.CompositeDecisionRule;
import com.artagon.xacml.v30.pdp.PolicyDecisionPoint;
import com.artagon.xacml.v30.pdp.PolicyDecisionPointBuilder;
import com.artagon.xacml.v30.spi.audit.PolicyDecisionAuditor;
import com.artagon.xacml.v30.spi.pdp.PolicyDecisionCache;
import com.artagon.xacml.v30.spi.pdp.RequestContextHandler;
import com.artagon.xacml.v30.spi.pip.PolicyInformationPoint;
import com.artagon.xacml.v30.spi.repository.PolicyRepository;
import com.artagon.xacml.v30.spi.xpath.XPathProvider;

public class PolicyDecisionPointFactoryBean extends
	AbstractFactoryBean<PolicyDecisionPoint>
{
	private PolicyDecisionPointBuilder pdpBuilder;

	public PolicyDecisionPointFactoryBean(String id){
		this.pdpBuilder = PolicyDecisionPointBuilder.builder(id);
	}

	@Override
	public Class<PolicyDecisionPoint> getObjectType() {
		return PolicyDecisionPoint.class;
	}

	public void setXPathProvider(XPathProvider xpath){
		this.pdpBuilder.xpathProvider(xpath);
	}

	public void setDecisionAuditor(PolicyDecisionAuditor auditor){
		this.pdpBuilder.decisionAuditor(auditor);
	}

	public void setDecisionCache(PolicyDecisionCache cache){
		this.pdpBuilder.decisionCache(cache);
	}

	public void setDecisionCacheTTL(int decisionCacheTTL){
		this.pdpBuilder.decisionCacheTTL(decisionCacheTTL);
	}

	public void setPolicyInformationPoint(PolicyInformationPoint pip){
		this.pdpBuilder.pip(pip);
	}

	public void setDomainPolicy(CompositeDecisionRule policyStore){
		this.pdpBuilder.rootPolicy(policyStore);
	}

	public void setPolicyRepository(PolicyRepository policyRepository)
	{
		this.pdpBuilder.policyRepository(policyRepository);
	}

	public void setHandlers(List<RequestContextHandler> handlers){
		for(RequestContextHandler handler : handlers){
			pdpBuilder.requestHandler(handler);
		}
	}

	@Override
	protected PolicyDecisionPoint createInstance() throws Exception
	{
		return pdpBuilder.build();
	}
}
