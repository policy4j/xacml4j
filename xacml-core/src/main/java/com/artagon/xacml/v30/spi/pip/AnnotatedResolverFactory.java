package com.artagon.xacml.v30.spi.pip;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

import com.artagon.xacml.util.Pair;
import com.artagon.xacml.util.Reflections;
import com.artagon.xacml.v30.AttributeCategories;
import com.artagon.xacml.v30.AttributeDesignatorKey;
import com.artagon.xacml.v30.AttributeReferenceKey;
import com.artagon.xacml.v30.AttributeSelectorKey;
import com.artagon.xacml.v30.BagOfAttributeValues;
import com.artagon.xacml.v30.XacmlSyntaxException;
import com.artagon.xacml.v30.types.DataTypes;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

class AnnotatedResolverFactory 
{
	private  final static Logger log = LoggerFactory.getLogger(AnnotatedResolverFactory.class);
	
	public Collection<ContentResolver> getContentResolvers(Object instance) 
		throws XacmlSyntaxException
	{
		Preconditions.checkNotNull(instance);
		Collection<ContentResolver> resolvers = new LinkedList<ContentResolver>();
		List<Method> methods =  Reflections.getAnnotatedMethods(instance.getClass(), 
				XacmlContentResolverDescriptor.class);
		for(Method m : methods){
			ContentResolver r = parseContentResolver(instance, m);
			if(log.isDebugEnabled()){
				log.debug("Parsing content resolver=\"{}\"", 
						r.getDescriptor().getId());
			}
			resolvers.add(r);
		}
		return resolvers;
	}
	
	public Collection<AttributeResolver> getAttributeResolvers(Object instance) 
		throws XacmlSyntaxException
	{
		Preconditions.checkNotNull(instance);
		Collection<AttributeResolver> resolvers = new LinkedList<AttributeResolver>();
		List<Method> methods =  Reflections.getAnnotatedMethods(instance.getClass(), 
				XacmlAttributeResolverDescriptor.class);
		for(Method m : methods){
			AttributeResolver r = parseAttributeResolver(instance, m);
			if(log.isDebugEnabled()){
				log.debug("Parsing attribute resolver=\"{}\"", 
						r.getDescriptor().getId());
			}
			resolvers.add(r);
		}
		return resolvers;
	}
	
	AttributeResolver parseAttributeResolver(Object instance, Method m) 
		throws XacmlSyntaxException
	{
		Preconditions.checkNotNull(instance);
		Preconditions.checkNotNull(m);
		Preconditions.checkArgument(m.getDeclaringClass().equals(instance.getClass()));
		XacmlAttributeResolverDescriptor d = m.getAnnotation(XacmlAttributeResolverDescriptor.class);
		AttributeResolverDescriptorBuilder b = AttributeResolverDescriptorBuilder.builder(
				d.id(), d.name(), 
				d.issuer(),
				AttributeCategories.parse(d.category()));
		b.cache(d.cacheTTL());
		XacmlAttributeDescriptor[] attributes = d.attributes();
		if(attributes == null || 
				attributes.length == 0){
			throw new XacmlSyntaxException("At least attribute " +
					"must be specified by the descriptor on method=\"{}\"", m.getName());
		}
		for(XacmlAttributeDescriptor attr : attributes){
			b.attribute(attr.id(), 
					DataTypes.getType(attr.dataType()));
		}
		Pair<Boolean, List<AttributeReferenceKey>> info = parseResolverMethodParams(m);
		b.keys(info.getSecond());
		if(!m.getReturnType().isAssignableFrom(Map.class)){
			throw new XacmlSyntaxException(
					"Attribute resolver method=\"%s\" " +
					"must return=\"%s\"", m.getName(), 
					Map.class.getName());
		}
		AttributeResolverDescriptor descriptor = b.build();
		return new AnnotatedAttributeResolver(descriptor, 
				new Invocation<Map<String,BagOfAttributeValues>>(instance, m, info.getFirst()));
	}
	
	ContentResolver parseContentResolver(Object instance, Method m) 
		throws XacmlSyntaxException
	{
		Preconditions.checkNotNull(instance);
		Preconditions.checkNotNull(m);
		Preconditions.checkArgument(m.getDeclaringClass().equals(instance.getClass()));
		XacmlContentResolverDescriptor d = m.getAnnotation(XacmlContentResolverDescriptor.class);
		ContentResolverDescriptorBuilder b = ContentResolverDescriptorBuilder.create(
				d.id(), d.name(), 
				AttributeCategories.parse(d.category()));
		b.cache(d.cacheTTL());
		Pair<Boolean, List<AttributeReferenceKey>> info = parseResolverMethodParams(m);
		b.keys(info.getSecond());
		if(!m.getReturnType().isAssignableFrom(Node.class)){
			throw new XacmlSyntaxException(
					"Attribute resolver method=\"%s\" " +
					"must return=\"%s\"", m.getName(), 
					Node.class.getName());
		}
		ContentResolverDescriptor descriptor = b.build();
		return new AnnotatedContentResolver(descriptor, 
				new Invocation<Node>(instance, m, info.getFirst()));
	}
	
	
	
	private Pair<Boolean, List<AttributeReferenceKey>> parseResolverMethodParams(Method m) 
		throws XacmlSyntaxException
	{		
		List<AttributeReferenceKey> keys = new LinkedList<AttributeReferenceKey>();
		Class<?>[] types = m.getParameterTypes();
		boolean needPipContext = false;
		int  i = 0;
		for(Annotation[] p : m.getParameterAnnotations())
		{
			if(p.length == 0 && i != 0){
				throw new XacmlSyntaxException(
						"Only first parameter of the resolver method=\"%s\" " +
						"can be without annotation", m.getName());
			}
			if(p.length == 0 && i == 0) {
				if(!types[i].equals(ResolverContext.class)){
					throw new XacmlSyntaxException(
							"Resolver parameter without annotiation at index=\"%d\" must be of type=\"%s\"", 
							i, ResolverContext.class);
				}
				needPipContext = true;
				continue;
			}
			if(p.length > 0 && 
					p[0] instanceof XacmlAttributeDesignator)
			{
				if(!(types[i].equals(BagOfAttributeValues.class))){
					throw new XacmlSyntaxException(
							"Resolver method=\"%s\" " +
							"parameter at index=\"%d\" must be of type=\"%s\"", 
							m.getName(), i, BagOfAttributeValues.class.getName());
				}
				XacmlAttributeDesignator ref = (XacmlAttributeDesignator)p[0];
				keys.add(new AttributeDesignatorKey(
							AttributeCategories.parse(ref.category()), 
							ref.attributeId(), 
							DataTypes.getType(ref.dataType()), 
							Strings.emptyToNull(ref.issuer())));
				continue;
			}
			if(p.length > 0 && 
					p[0] instanceof XacmlAttributeSelector)
			{
				if(!(types[i].equals(BagOfAttributeValues.class))){
					throw new XacmlSyntaxException(
							"Resolver method=\"%s\" " +
							"request key parameter " +
							"at index=\"%d\" must be of type=\"%s\"", 
							m.getName(), i, BagOfAttributeValues.class.getName());
				}
				XacmlAttributeSelector ref = (XacmlAttributeSelector)p[0];
				keys.add(new AttributeSelectorKey(
						AttributeCategories.parse(ref.category()), 
						ref.xpath(), 
						DataTypes.getType(ref.dataType()), 
						Strings.emptyToNull(ref.contextAttributeId())));
				continue;
			}
			i++;
			throw new XacmlSyntaxException(
						"Unknown annotation of type=\"%s\" found", 
						types[0].getClass());
		}
		return new Pair<Boolean, List<AttributeReferenceKey>>(needPipContext, keys);
	}
	
	
	public final class Invocation <T>
	{
		private Method m;
		private Object instance;
		private boolean requiresContext;
		
		public Invocation(
				Object instance,
				Method m, 
				boolean requiresContext){
			this.instance = instance;
			this.m = m;
			this.requiresContext = requiresContext;
		}
		
		@SuppressWarnings("unchecked")
		public T invoke(ResolverContext context) throws Exception
		{
			List<BagOfAttributeValues> keys = context.getKeys();
			if(requiresContext){
				Object[] params = new Object[keys.size() + 1];
				params[0] = context;
				System.arraycopy(keys.toArray(), 0, params, 1, keys.size());
				return (T)m.invoke(instance, params);
			}
			return (T)m.invoke(instance, keys.toArray());
		}
	}
	
	private final class AnnotatedAttributeResolver 
		extends BaseAttributeResolver
	{
		private Invocation<Map<String, BagOfAttributeValues>> invocation;
		
		public AnnotatedAttributeResolver(
				AttributeResolverDescriptor descriptor, 
				Invocation<Map<String, BagOfAttributeValues>> invocation) {
			super(descriptor);
			this.invocation = invocation;
		}

		@Override
		protected Map<String, BagOfAttributeValues> doResolve(
				ResolverContext context) throws Exception {
			return invocation.invoke(context);
		}
	}
	
	private final class AnnotatedContentResolver 
		extends BaseContentResolver
	{
		private Invocation<Node> invocation;
		
		public AnnotatedContentResolver(
				ContentResolverDescriptor descriptor, 
				Invocation<Node> invocation) {
			super(descriptor);
			this.invocation = invocation;
		}
		
		@Override
		protected Node doResolve(ResolverContext context)
				throws Exception {
			return invocation.invoke(context);
		}
	}
}
