package org.xacml4j.spring.pip;

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
		pip.addConstructorArgValue(element.getAttribute("id"));
		String resolvers = element.getAttribute("resolvers");
		if (StringUtils.hasText(resolvers)) {
			pip.addPropertyReference("resolvers", resolvers);
		}
		String cache = element.getAttribute("cache");
		if (StringUtils.hasText(cache)) {
			pip.addPropertyReference("cache", cache);
		}
		return pip.getBeanDefinition();
	}

}
