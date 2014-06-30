package org.xacml4j.spring.pdp;

/*
 * #%L
 * Xacml4J Spring 3.x Support Module
 * %%
 * Copyright (C) 2009 - 2014 Xacml4J.org
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import java.util.List;

import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;
import org.xacml4j.spring.PolicyIDReferenceFactoryBean;
import org.xacml4j.spring.PolicySetIDReferenceFactoryBean;

import com.google.common.base.Preconditions;

public class PolicyDecisionPointDefinitionParser extends
		AbstractBeanDefinitionParser {

	@Override
	protected AbstractBeanDefinition parseInternal(Element element,
			ParserContext parserContext) {
		BeanDefinitionBuilder factory = BeanDefinitionBuilder
				.rootBeanDefinition(PolicyDecisionPointFactoryBean.class);
		factory.addConstructorArgValue(element.getAttribute("id"));
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
		String decisionCache = element.getAttribute("decisionCache");
		if (StringUtils.hasText(decisionCache)) {
			factory.addPropertyReference("decisionCache", decisionCache);
		}
		String decisionCacheTTL = element.getAttribute("decisionCacheTTL");
		if (StringUtils.hasText(decisionCache)) {
			factory.addPropertyValue("decisionCacheTTL", decisionCacheTTL);
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
