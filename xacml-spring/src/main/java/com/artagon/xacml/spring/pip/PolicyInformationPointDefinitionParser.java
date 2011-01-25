package com.artagon.xacml.spring.pip;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

public class PolicyInformationPointDefinitionParser extends AbstractSingleBeanDefinitionParser 
{ 
	   protected Class<PolicyInformationPointFactoryBean> getBeanClass(Element element) {
	      return PolicyInformationPointFactoryBean.class; 
	   }
	   
	   protected void doParse(Element element, BeanDefinitionBuilder bean) 
	   {
	      String attributeResolvers = element.getAttribute("attributeResolvers");
	      if (StringUtils.hasText(attributeResolvers)) {
	    	  bean.addPropertyReference("attributeResolvers", attributeResolvers);
	      }
	   }
}