package com.artagon.xacml.spring.pdp;

import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.factory.config.AbstractFactoryBean;

import com.artagon.xacml.v3.CompositeDecisionRule;
import com.artagon.xacml.v3.context.RequestContextHandler;
import com.artagon.xacml.v3.pdp.DefaultPolicyDecisionPoint;
import com.artagon.xacml.v3.pdp.DefaultPolicyDecisionPointContextFactory;
import com.artagon.xacml.v3.pdp.PolicyDecisionPoint;
import com.artagon.xacml.v3.pdp.PolicyDecisionPointContextFactory;
import com.artagon.xacml.v3.pdp.profiles.MultipleResourcesHandler;
import com.artagon.xacml.v3.spi.PolicyInformationPoint;
import com.artagon.xacml.v3.spi.PolicyRepository;
import com.google.common.base.Preconditions;

public class PolicyDecisionPointFactoryBean extends 
	AbstractFactoryBean<PolicyDecisionPoint>
{
	private PolicyInformationPoint pip;
	private CompositeDecisionRule policyDomain;
	private PolicyRepository policyRepository;
	private List<RequestContextHandler> handlers;
	
	
	public PolicyDecisionPointFactoryBean(){
		this.handlers = new LinkedList<RequestContextHandler>();
		this.handlers.add(new MultipleResourcesHandler());
	}
	
	@Override
	public Class<PolicyDecisionPoint> getObjectType() {
		return PolicyDecisionPoint.class;
	}
	
	public void setPolicyInformationPoint(PolicyInformationPoint pip){
		this.pip = pip;
	}
		
	public void setPolicyDomain(CompositeDecisionRule policyStore){
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
		PolicyDecisionPointContextFactory factory = new DefaultPolicyDecisionPointContextFactory(
				policyDomain, policyRepository, pip, handlers);
		return new DefaultPolicyDecisionPoint(factory);
	}
}
