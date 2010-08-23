package com.artagon.xacml.spring.repository;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.w3c.dom.Element;

public class InMemoryPolicyRepositoryDefinitionParser extends AbstractSingleBeanDefinitionParser
{ 
	protected Class<InMemoryPolicyRepositoryFactoryBean> getBeanClass(Element element) {
	      return InMemoryPolicyRepositoryFactoryBean.class;
	}
	
	protected void doParse(Element element, BeanDefinitionBuilder bean) {
	      bean.addPropertyReference("policies", element.getAttribute("resources"));
	}
}
