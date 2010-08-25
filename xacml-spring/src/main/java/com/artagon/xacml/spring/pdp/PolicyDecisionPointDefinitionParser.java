package com.artagon.xacml.spring.pdp;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

public class PolicyDecisionPointDefinitionParser extends AbstractSingleBeanDefinitionParser { 

	   protected Class<PolicyDecisionPointFactoryBean> getBeanClass(Element element) {
	      return PolicyDecisionPointFactoryBean.class; 
	   }

	   protected void doParse(Element element, BeanDefinitionBuilder bean) 
	   {
	      String pipRef = element.getAttribute("pip");
	      bean.addPropertyReference("policyInformationPoint", pipRef);
	      String domainRef = element.getAttribute("domain");
	      bean.addPropertyReference("policyDomain", domainRef);
	      String repositoryRef = element.getAttribute("repository");
	      bean.addPropertyReference("policyRepository", repositoryRef);
	      String handlers = element.getAttribute("handlers");
	      if (StringUtils.hasText(handlers)) {
	    	  bean.addPropertyReference("handlers", handlers);
	      }
	   }
	}

