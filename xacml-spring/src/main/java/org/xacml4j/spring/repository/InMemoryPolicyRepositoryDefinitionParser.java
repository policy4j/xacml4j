package org.xacml4j.spring.repository;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

public class InMemoryPolicyRepositoryDefinitionParser extends AbstractSingleBeanDefinitionParser {

	protected Class<InMemoryPolicyRepositoryFactoryBean> getBeanClass(Element element) {
		return InMemoryPolicyRepositoryFactoryBean.class;
	}

	protected void doParse(Element element, BeanDefinitionBuilder bean) {
		bean.addConstructorArgValue(element.getAttribute("id"));
		bean.addPropertyReference("policies", element.getAttribute("policies"));
		if (StringUtils.hasText(element.getAttribute("extensionFunctions"))) {
			bean.addPropertyReference("extensionFunctions",
					element.getAttribute("extensionFunctions"));
		}
		if (StringUtils.hasText(element.getAttribute("extensionCombiningAlgorithms"))) {
			bean.addPropertyReference("extensionCombiningAlgorithms",
					element.getAttribute("extensionCombiningAlgorithms"));
		}
	}
}
