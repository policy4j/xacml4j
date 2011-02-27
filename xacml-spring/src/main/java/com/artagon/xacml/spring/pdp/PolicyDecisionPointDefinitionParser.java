package com.artagon.xacml.spring.pdp;

import java.util.List;

import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import com.artagon.xacml.spring.PolicyIDReferenceFactoryBean;
import com.artagon.xacml.spring.PolicySetIDReferenceFactoryBean;
import com.google.common.base.Preconditions;

public class PolicyDecisionPointDefinitionParser extends
		AbstractBeanDefinitionParser {

	@Override
	protected AbstractBeanDefinition parseInternal(Element element,
			ParserContext parserContext) {
		BeanDefinitionBuilder factory = BeanDefinitionBuilder
				.rootBeanDefinition(PolicyDecisionPointFactoryBean.class);
		List<Element> childElements = DomUtils.getChildElementsByTagName(
				element, new String[]{"PolicyIdReference", "PolicySetIdReference"});
		if (childElements != null && childElements.size() > 0) {
			parseChildComponents(childElements, factory);
		}
		String pipRef = element.getAttribute("pip");
		factory.addPropertyReference("policyInformationPoint", pipRef);
		String repositoryRef = element.getAttribute("repository");
		factory.addPropertyReference("policyRepository", repositoryRef);
		String auditorRef = element.getAttribute("auditor");
		if(StringUtils.hasText(auditorRef)) {
			factory.addPropertyReference("decisionAuditor", auditorRef);
		}
		String handlers = element.getAttribute("handlers");
		if (StringUtils.hasText(handlers)) {
			factory.addPropertyReference("handlers", handlers);
		}
		String xpathProvider = element.getAttribute("xpath-provider");
		if (StringUtils.hasText(xpathProvider)) {
			factory.addPropertyReference("xpathProvider", xpathProvider);
		}
		return factory.getBeanDefinition();
	}

	private static BeanDefinitionBuilder parseComponent(Element element) {
		BeanDefinitionBuilder component = element.getLocalName().equals("PolicyIdReference")?
				BeanDefinitionBuilder.rootBeanDefinition(PolicyIDReferenceFactoryBean.class):
					BeanDefinitionBuilder.rootBeanDefinition(PolicySetIDReferenceFactoryBean.class);
		String id = element.getTextContent();
		if (StringUtils.hasText(id)) {
			component.addPropertyValue("id", id);
		}
		String version = element.getAttribute("version");
		if (StringUtils.hasText(version)) {
			component.addPropertyValue("version", version);
		}
		String latest = element.getAttribute("latest");
		if (StringUtils.hasText(latest)) {
			component.addPropertyValue("latest", latest);
		}
		String earliest = element.getAttribute("earliest");
		if (StringUtils.hasText(earliest)) {
			component.addPropertyValue("earliest", earliest);
		}
		return component;
	}

	private static void parseChildComponents(List<Element> childElements,
			BeanDefinitionBuilder factory) {
		Preconditions.checkArgument(childElements.size() == 1);
		factory.addPropertyValue("domainPolicy", parseComponent(childElements.get(0)).getBeanDefinition());
	}

}
