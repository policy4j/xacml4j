package com.artagon.xacml.v3.spi.pip;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.artagon.xacml.util.Reflections;
import com.artagon.xacml.v3.AttributeCategoryId;
import com.artagon.xacml.v3.RequestContextAttributesCallback;
import com.google.common.base.Preconditions;

public class AnnotatedAttributeResolverBuilder 
{
	
	public AttributeResolver build(Object instance)
	{
		Preconditions.checkArgument(instance != null);
		XacmlAttributeIssuer issuer = instance.getClass().getAnnotation(XacmlAttributeIssuer.class);
		List<Method> resolvers = Reflections.getAnnotatedMethods(instance.getClass(), XacmlAttributeDescriptor.class);
		AttributeResolverDescriptorBuilder builder = AttributeResolverDescriptorBuilder.create((issuer == null)?null:issuer.value());
		Map<AttributeCategoryId, Map<String, Method>> methods = new HashMap<AttributeCategoryId, Map<String,Method>>();
		for(Method r : resolvers)
		{
			XacmlAttributeDescriptor d = r.getAnnotation(XacmlAttributeDescriptor.class);
			XacmlAttributeCategory c = r.getAnnotation(XacmlAttributeCategory.class);
			Preconditions.checkState(c != null, 
					"Class=\"%s\" method=\"%s\" does not have attribute category specified", 
					instance.getClass().getName(), r.getName());
			Preconditions.checkState(validateResolverMethod(r));
			builder.attribute(c.value(), d.id(), d.dataType());
			Map<String, Method> byId = methods.get(c.value());
			if(byId == null){
				byId = new HashMap<String, Method>();
				methods.put(c.value(), byId);
			}
			byId.put(d.id(), r);
		}
		return new AttributeResolverDescriptorAdapter(builder.build(), methods, instance);
	} 
	
	private static boolean validateResolverMethod(Method m)
	{
		Class<?>[] p = m.getParameterTypes();
		if(p == null || p.length != 2){
			return false;
		}
		return PolicyInformationPointContext.class.isAssignableFrom(p[0]) &&
		RequestContextAttributesCallback.class.isAssignableFrom(p[1]);
	}
}
