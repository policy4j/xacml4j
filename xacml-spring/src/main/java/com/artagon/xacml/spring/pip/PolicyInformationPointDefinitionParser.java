package com.artagon.xacml.spring.pip;

import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

public class PolicyInformationPointDefinitionParser extends AbstractBeanDefinitionParser
{
		
	@Override
	protected AbstractBeanDefinition parseInternal(Element element,
			ParserContext parserContext) {
		BeanDefinitionBuilder pip = BeanDefinitionBuilder.rootBeanDefinition(PolicyInformationPointFactoryBean.class);
		
		String handlers = element.getAttribute("resolvers");
		if (StringUtils.hasText(handlers)) {
			pip.addPropertyReference("resolvers", handlers);
		}
		String xpathProvider = element.getAttribute("cache");
		if (StringUtils.hasText(xpathProvider)) {
			pip.addPropertyReference("cache", xpathProvider);
		}
		return pip.getBeanDefinition();
	}
	
}
