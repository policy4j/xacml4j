package org.xacml4j.spring.repository;

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
