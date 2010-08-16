package com.artagon.xacml.spring;

import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.factory.config.AbstractFactoryBean;

import com.artagon.xacml.v3.DefaultEvaluationContextFactory;
import com.artagon.xacml.v3.EvaluationContextFactory;
import com.artagon.xacml.v3.pdp.DefaultPolicyDecisionPoint;
import com.artagon.xacml.v3.pdp.PolicyDecisionPoint;
import com.artagon.xacml.v3.pdp.RequestProfileHandler;
import com.artagon.xacml.v3.pdp.profiles.MultipleDecisionProfileHandler;
import com.artagon.xacml.v3.spi.PolicyInformationPoint;
import com.artagon.xacml.v3.spi.PolicyDomain;
import com.artagon.xacml.v3.spi.XPathProvider;
import com.artagon.xacml.v3.spi.xpath.DefaultXPathProvider;
import com.google.common.base.Preconditions;

public class PolicyDecisionPointFactoryBean extends AbstractFactoryBean
{
	private PolicyInformationPoint pip;
	private PolicyDomain policyStore;
	private XPathProvider xpathProvider;
	private List<RequestProfileHandler> handlers;
	
	public PolicyDecisionPointFactoryBean(){
		this.xpathProvider = new DefaultXPathProvider();
		this.handlers = new LinkedList<RequestProfileHandler>();
		this.handlers.add(new MultipleDecisionProfileHandler(xpathProvider));
	}
	
	@Override
	public Class<PolicyDecisionPoint> getObjectType() {
		return PolicyDecisionPoint.class;
	}
	
	public void setPolicyInformationPoint(PolicyInformationPoint pip){
		this.pip = pip;
	}
	
	public void setPolicyStore(PolicyDomain policyStore){
		this.policyStore = policyStore;
	}
	
	public void setRequestProcessingHandlers(List<RequestProfileHandler> handlers){
		this.handlers.addAll(handlers);
	}
	
	@Override
	protected Object createInstance() throws Exception 
	{
		Preconditions.checkState(pip != null);
		Preconditions.checkState(policyStore != null);
		EvaluationContextFactory evalCtxFactory = new DefaultEvaluationContextFactory(policyStore, xpathProvider, pip);
		return new DefaultPolicyDecisionPoint(handlers, evalCtxFactory, policyStore);
	}
}
