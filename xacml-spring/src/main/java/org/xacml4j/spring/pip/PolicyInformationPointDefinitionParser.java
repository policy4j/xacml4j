package org.xacml4j.spring.pip;

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
