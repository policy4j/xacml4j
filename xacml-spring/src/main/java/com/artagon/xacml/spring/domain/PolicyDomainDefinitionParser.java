package com.artagon.xacml.spring.domain;

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

public class PolicyDomainDefinitionParser extends AbstractBeanDefinitionParser
{
		
	@Override
	protected AbstractBeanDefinition parseInternal(Element element,
			ParserContext parserContext) {
		BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(PolicyDomainFactoryBean.class);
		factory.addPropertyValue("name", element.getAttribute("name"));
		List<Element> childElements = (List<Element>)DomUtils.getChildElementsByTagName(
				element, new String[]{"PolicySetIdReference", "PolicyIdReference"});
	      if (childElements != null && childElements.size() > 0) {
	         parseChildComponents(childElements, factory);
	      }
	    return factory.getBeanDefinition();
	}
	
	private static BeanDefinitionBuilder parseComponent(Element element) 
	{
		BeanDefinitionBuilder component = null;
		if(element.getLocalName().equals("PolicySetIdReference")){
			component = BeanDefinitionBuilder.rootBeanDefinition(PolicySetIDReferenceFactoryBean.class);
		}
		if(element.getLocalName().equals("PolicyIdReference")){
			component = BeanDefinitionBuilder.rootBeanDefinition(PolicyIDReferenceFactoryBean.class);
		}
		component.addPropertyValue("id", element.getTextContent());
		
		if(StringUtils.hasText(element.getAttribute("Version"))){
			component.addPropertyValue("version", element.getAttribute("Version"));
		}
		if(StringUtils.hasText(element.getAttribute("Earliest"))){
			component.addPropertyValue("earliest", element.getAttribute("Earliest"));
		}
		if(StringUtils.hasText(element.getAttribute("Latest"))){
			component.addPropertyValue("latest", element.getAttribute("Latest"));
		}
		return component;
		
	}
	
	private static void parseChildComponents(List<Element> childElements, 
			BeanDefinitionBuilder factory) 
	{
	      ManagedList<BeanDefinition> children = new ManagedList<BeanDefinition>(childElements.size());
	      for (int i = 0; i < childElements.size(); ++i) {
	         Element childElement = childElements.get(i);
	         BeanDefinitionBuilder child = parseComponent(childElement);
	         children.add(child.getBeanDefinition());
	      }
	      factory.addPropertyValue("references", children);
	   }
}

