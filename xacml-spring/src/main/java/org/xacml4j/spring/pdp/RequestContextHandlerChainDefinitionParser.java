package org.xacml4j.spring.pdp;

import java.util.List;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

public class RequestContextHandlerChainDefinitionParser	extends
		AbstractBeanDefinitionParser {

	@Override
	protected AbstractBeanDefinition parseInternal(Element element,
			ParserContext parserContext) {
		BeanDefinitionBuilder chain = BeanDefinitionBuilder
				.rootBeanDefinition(RequestContextHandlerChainFactoryBean.class);
		parseHandlers(
       		 DomUtils.getChildElementsByTagName(element,
       		 "RequestContextHandler"), chain);

		return chain.getBeanDefinition();
	}

	private static void parseHandlers(List<Element> childElements, BeanDefinitionBuilder chain)
	{
		ManagedList<BeanDefinition> handlers = new ManagedList<BeanDefinition>(childElements.size());
	    for (int i = 0; i < childElements.size(); ++i) {
	         Element childElement = (Element) childElements.get(i);
	         BeanDefinitionBuilder handler = BeanDefinitionBuilder.rootBeanDefinition(RequestContexctHandlerFactoryBean.class);
	         handler.addPropertyReference("ref", childElement.getAttribute("ref"));
	         handlers.add(handler.getBeanDefinition());
	    }
	    chain.addPropertyValue("handlers", handlers);
	}
}

