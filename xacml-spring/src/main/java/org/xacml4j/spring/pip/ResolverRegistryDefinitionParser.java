package org.xacml4j.spring.pip;

import java.util.List;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

public class ResolverRegistryDefinitionParser extends AbstractBeanDefinitionParser
{
		
	@Override
	protected AbstractBeanDefinition parseInternal(Element element,
			ParserContext parserContext) {
		BeanDefinitionBuilder registry = BeanDefinitionBuilder.rootBeanDefinition(ResolverRegistryFactoryBean.class);
		
		parseResolvers(
	        		 DomUtils.getChildElementsByTagName(element, 
	        		 "Resolver"), registry);
		
	    return registry.getBeanDefinition();
	}
	
	private static BeanDefinitionBuilder parseResolvers(Element element) 
	{
	      BeanDefinitionBuilder component = BeanDefinitionBuilder.rootBeanDefinition(
	    		  ResolverRegistrationFactoryBean.class);
	      String policyId = element.getAttribute("policyId");
	      if(StringUtils.hasText(policyId)){
	    	  component.addPropertyValue("policyId", policyId);
	      }
	      String ref = element.getAttribute("ref");
	      if(StringUtils.hasText(ref)){
	    	  component.addPropertyReference("resolver", ref);
	      }
	      return component;
	}
	

	private static void parseResolvers(List<Element> childElements, BeanDefinitionBuilder factory) 
	{
		ManagedList<BeanDefinition> children = new ManagedList<BeanDefinition>(childElements.size());
	    for (int i = 0; i < childElements.size(); ++i) {
	         Element childElement = (Element) childElements.get(i);
	         BeanDefinitionBuilder child = parseResolvers(childElement);
	         children.add(child.getBeanDefinition());
	    }
	    factory.addPropertyValue("resolvers", children);
	}
}

