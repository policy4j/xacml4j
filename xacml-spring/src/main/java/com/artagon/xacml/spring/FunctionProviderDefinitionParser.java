package com.artagon.xacml.spring;

import java.util.List;

import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

public class FunctionProviderDefinitionParser extends AbstractBeanDefinitionParser
{
		
	@Override
	protected AbstractBeanDefinition parseInternal(Element element,
			ParserContext parserContext) {
		BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(FunctionProvidersFactoryBean.class);
		@SuppressWarnings("unchecked")
		List<Element> childElements = (List<Element>)DomUtils.getChildElementsByTagName(
				element, "function-provider");
	      if (childElements != null && childElements.size() > 0) {
	         parseChildComponents(childElements, factory);
	      }
	    return factory.getBeanDefinition();
	}
	
	private static BeanDefinitionBuilder parseComponent(Element element) 
	{
	      BeanDefinitionBuilder component = BeanDefinitionBuilder.rootBeanDefinition(FunctionProviderFactoryBean.class);
	      String clazz = element.getAttribute("class");
	      if(StringUtils.hasText(clazz)){
	    	  component.addPropertyValue("class", clazz);
	      }
	      String ref = element.getAttribute("ref");
	      if(StringUtils.hasText(ref)){
	    	  component.addPropertyReference("ref", ref);
	      }
	      return component;
	}
	
	@SuppressWarnings("unchecked")
	private static void parseChildComponents(List<Element> childElements, BeanDefinitionBuilder factory) 
	{
	      ManagedList children = new ManagedList(childElements.size());
	      for (int i = 0; i < childElements.size(); ++i) {
	         Element childElement = (Element) childElements.get(i);
	         BeanDefinitionBuilder child = parseComponent(childElement);
	         children.add(child.getBeanDefinition());
	      }
	      factory.addPropertyValue("providers", children);
	   }
}
