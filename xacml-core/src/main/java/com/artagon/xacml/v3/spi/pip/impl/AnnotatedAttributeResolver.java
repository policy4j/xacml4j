package com.artagon.xacml.v3.spi.pip.impl;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.artagon.xacml.util.Reflections;
import com.artagon.xacml.v3.AttributeCategories;
import com.artagon.xacml.v3.AttributeCategory;
import com.artagon.xacml.v3.AttributeValueType;
import com.artagon.xacml.v3.BagOfAttributeValues;
import com.artagon.xacml.v3.XacmlSyntaxException;
import com.artagon.xacml.v3.marshall.XacmlDataTypesRegistry;
import com.artagon.xacml.v3.sdk.XacmlAttributeCategory;
import com.artagon.xacml.v3.sdk.XacmlAttributeDescriptor;
import com.artagon.xacml.v3.sdk.XacmlAttributeIssuer;
import com.artagon.xacml.v3.sdk.XacmlAttributesResolverDescriptor;
import com.artagon.xacml.v3.spi.pip.AttributeResolver;
import com.artagon.xacml.v3.spi.pip.AttributeResolverDescriptor;
import com.artagon.xacml.v3.spi.pip.AttributeResolverDescriptorBuilder;
import com.artagon.xacml.v3.spi.pip.BaseAttributeResolver;
import com.artagon.xacml.v3.spi.pip.PolicyInformationPointContext;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

public class AnnotatedAttributeResolver extends BaseAttributeResolver
{
	private final static Logger log = LoggerFactory.getLogger(AnnotatedAttributeResolver.class);
	private Map<AttributeCategory, Map<String, ResolverMethodInfo>> resolvers;
	
	AnnotatedAttributeResolver(
			AttributeResolverDescriptor descriptor, 
			Map<AttributeCategory, Map<String, ResolverMethodInfo>> methods, Object instance) 
	{
		super(descriptor);
		Preconditions.checkArgument(instance != null);
		this.resolvers = new HashMap<AttributeCategory, Map<String, ResolverMethodInfo>>();
		for(AttributeCategory c : methods.keySet()){
			resolvers.put(c, ImmutableMap.copyOf(methods.get(c)));
		}
	}
	
	public static AttributeResolver create(Object instance)
	{
		try{
			return build(instance);
		}catch(XacmlSyntaxException e){
			throw new IllegalArgumentException(e);
		}
	}
	
	@Override
	protected BagOfAttributeValues doResolve(
			PolicyInformationPointContext context, 
			AttributeCategory category,
			String attributeId,
			AttributeValueType dataType,
			String issuer) 
				throws Exception 
	{
		Map<String, ResolverMethodInfo> byId = resolvers.get(category);
		Preconditions.checkState(byId != null);
		ResolverMethodInfo m = byId.get(attributeId);
		Preconditions.checkState(m != null);
		try{
			return (BagOfAttributeValues)m.invoke(context);
		}catch(Exception e){
			if(log.isDebugEnabled()){
				log.debug(e.getMessage(), e);
			}
			throw e;
		}
		
	}	
	
	private static AttributeResolver build(Object instance) throws XacmlSyntaxException
	{
		Preconditions.checkArgument(instance != null);
		XacmlAttributesResolverDescriptor descriptor = instance.getClass().getAnnotation(XacmlAttributesResolverDescriptor.class);
		Preconditions.checkState(descriptor != null);
		XacmlAttributeIssuer issuer = instance.getClass().getAnnotation(XacmlAttributeIssuer.class);
		List<Method> resolvers = Reflections.getAnnotatedMethods(instance.getClass(), XacmlAttributeDescriptor.class);
		AttributeResolverDescriptorBuilder builder = AttributeResolverDescriptorBuilder.create(descriptor.name(), 
				(issuer == null)?null:issuer.value());
		Map<AttributeCategory, Map<String, ResolverMethodInfo>> methods = new HashMap<AttributeCategory, Map<String, ResolverMethodInfo>>();
		for(Method r : resolvers)
		{
			XacmlAttributeDescriptor d = r.getAnnotation(XacmlAttributeDescriptor.class);
			XacmlAttributeCategory c = r.getAnnotation(XacmlAttributeCategory.class);
			Preconditions.checkState(c != null, 
					"Class=\"%s\" method=\"%s\" does not have attribute category specified", 
					instance.getClass().getName(), r.getName());
			ResolverMethodInfo resolverMethod = new ResolverMethodInfo(instance, r, 
					RequestContextKeyInfo.getRequestKeyInfo(r));
			for(String cat : c.value())
			{
				AttributeCategory category = AttributeCategories.parse(cat);
				builder.attribute(category, d.id(), XacmlDataTypesRegistry.getType(d.typeId()));
				Map<String, ResolverMethodInfo> byId = methods.get(category);
				if(byId == null){
					byId = new HashMap<String, ResolverMethodInfo>();
					methods.put(category, byId);
				}
				r.setAccessible(true);
				byId.put(d.id(), resolverMethod);
			}
		}
		return new AnnotatedAttributeResolver(builder.build(), methods, instance);
	} 
}
