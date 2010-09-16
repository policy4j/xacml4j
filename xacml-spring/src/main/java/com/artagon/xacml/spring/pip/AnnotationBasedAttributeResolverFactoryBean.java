package com.artagon.xacml.spring.pip;

import org.springframework.beans.factory.config.AbstractFactoryBean;

import com.artagon.xacml.v3.sdk.AnnotatedAttributeResolver;
import com.artagon.xacml.v3.spi.pip.AttributeResolver;
import com.google.common.base.Preconditions;

public class AnnotationBasedAttributeResolverFactoryBean extends AbstractFactoryBean<AttributeResolver>
{
	private Object resolverBean;
	
	@Override
	public Class<AttributeResolver> getObjectType() {
		return AttributeResolver.class;
	}
	
	public void setResolverBean(Object resolverBean){
		this.resolverBean = resolverBean;
	}

	@Override
	protected AttributeResolver createInstance() throws Exception {
		Preconditions.checkState(resolverBean != null, "Resolver bean must be specified");
		return AnnotatedAttributeResolver.create(resolverBean);
	}
}
