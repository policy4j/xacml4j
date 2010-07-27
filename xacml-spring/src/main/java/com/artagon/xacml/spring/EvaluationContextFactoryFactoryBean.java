package com.artagon.xacml.spring;

import org.springframework.beans.factory.config.AbstractFactoryBean;

import com.artagon.xacml.v3.DefaultEvaluationContextFactory;
import com.artagon.xacml.v3.spi.PolicyInformationPoint;
import com.artagon.xacml.v3.spi.PolicyStore;
import com.artagon.xacml.v3.spi.XPathProvider;
import com.artagon.xacml.v3.spi.xpath.DefaultXPathProvider;
import com.google.common.base.Preconditions;

public class EvaluationContextFactoryFactoryBean extends AbstractFactoryBean
{
	private XPathProvider xpathProvider;
	private PolicyStore policyStore;
	private PolicyInformationPoint pip;
	
	public EvaluationContextFactoryFactoryBean(){
		this.xpathProvider = new DefaultXPathProvider();
	}
	
	public void setPolicyStore(PolicyStore policyStore){
		this.policyStore = policyStore;
	}
	
	public void setPip(PolicyInformationPoint pip){
		this.pip = pip;
	}
	
	@Override
	public Class<EvaluationContextFactoryFactoryBean> getObjectType() {
		return EvaluationContextFactoryFactoryBean.class;
	}

	@Override
	protected Object createInstance() throws Exception {
		Preconditions.checkState(policyStore != null, "Policy Store must be specified");
		Preconditions.checkState(pip != null, "Policy Information Point must be specified");
		return new DefaultEvaluationContextFactory(policyStore, xpathProvider, pip);
	}
	
}
