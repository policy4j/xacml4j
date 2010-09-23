package com.artagon.xacml.v3.sdk;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.artagon.xacml.util.Reflections;
import com.artagon.xacml.v3.AttributeCategoryId;
import com.artagon.xacml.v3.AttributeValueType;
import com.artagon.xacml.v3.BagOfAttributeValues;
import com.artagon.xacml.v3.XacmlSyntaxException;
import com.artagon.xacml.v3.marshall.XacmlDataTypesRegistry;
import com.artagon.xacml.v3.spi.pip.AttributeResolver;
import com.artagon.xacml.v3.spi.pip.AttributeResolverDescriptor;
import com.artagon.xacml.v3.spi.pip.PolicyInformationPointContext;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

public class AnnotatedAttributeResolver extends BaseAttributeResolver
{
	private final static Logger log = LoggerFactory.getLogger(AnnotatedAttributeResolver.class);
	private Object instance;
	private Map<AttributeCategoryId, Map<String, Method>> resolvers;
	
	AnnotatedAttributeResolver(
			AttributeResolverDescriptor descriptor, 
			Map<AttributeCategoryId, Map<String, Method>> methods, Object instance) 
	{
		super(descriptor);
		Preconditions.checkArgument(instance != null);
		this.instance = instance;
		this.resolvers = new HashMap<AttributeCategoryId, Map<String,Method>>();
		for(AttributeCategoryId c : methods.keySet()){
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
			AttributeCategoryId category,
			String attributeId,
			AttributeValueType dataType,
			String issuer) 
				throws Exception 
	{
		Map<String, Method> byId = resolvers.get(category);
		Preconditions.checkState(byId != null);
		Method m = byId.get(attributeId);
		Preconditions.checkState(m != null);
		if(log.isDebugEnabled()){
			log.debug("Invoking resolver=\"{}\" method=\"{}\"", 
					getDescriptor().getName(), m.getName());
		}
		try{
			return (BagOfAttributeValues)m.invoke(instance, new Object[]{context});
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
		XacmlAttributeResolverDescriptor descriptor = instance.getClass().getAnnotation(XacmlAttributeResolverDescriptor.class);
		Preconditions.checkState(descriptor != null);
		XacmlAttributeIssuer issuer = instance.getClass().getAnnotation(XacmlAttributeIssuer.class);
		List<Method> resolvers = Reflections.getAnnotatedMethods(instance.getClass(), XacmlAttributeDescriptor.class);
		AttributeResolverDescriptorBuilder builder = AttributeResolverDescriptorBuilder.create(descriptor.name(), 
				(issuer == null)?null:issuer.value());
		Map<AttributeCategoryId, Map<String, Method>> methods = new HashMap<AttributeCategoryId, Map<String,Method>>();
		for(Method r : resolvers)
		{
			XacmlAttributeDescriptor d = r.getAnnotation(XacmlAttributeDescriptor.class);
			XacmlAttributeCategory c = r.getAnnotation(XacmlAttributeCategory.class);
			Preconditions.checkState(c != null, 
					"Class=\"%s\" method=\"%s\" does not have attribute category specified", 
					instance.getClass().getName(), r.getName());
			Preconditions.checkState(validateResolverMethod(r));
			for(String cat : c.value())
			{
				AttributeCategoryId category = AttributeCategoryId.parse(cat);
				builder.attribute(category, d.id(), XacmlDataTypesRegistry.getType(d.typeId()));
				Map<String, Method> byId = methods.get(category);
				if(byId == null){
					byId = new HashMap<String, Method>();
					methods.put(category, byId);
				}
				r.setAccessible(true);
				byId.put(d.id(), r);
			}
		}
		return new AnnotatedAttributeResolver(builder.build(), methods, instance);
	} 
	
	private static boolean validateResolverMethod(Method m)
	{
		Class<?>[] p = m.getParameterTypes();
		if(p == null || p.length != 1){
			return false;
		}
		return m.getReturnType().isAssignableFrom(BagOfAttributeValues.class) && 
		PolicyInformationPointContext.class.isAssignableFrom(p[0]);
	}
}
