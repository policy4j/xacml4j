package org.xacml4j.v30.spi.pip;

/*
 * #%L
 * Xacml4J Core Engine Implementation
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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.xacml4j.util.Pair;
import org.xacml4j.util.Reflections;
import org.xacml4j.v30.AttributeDesignatorKey;
import org.xacml4j.v30.AttributeExpType;
import org.xacml4j.v30.AttributeReferenceKey;
import org.xacml4j.v30.AttributeSelectorKey;
import org.xacml4j.v30.BagOfAttributeExp;
import org.xacml4j.v30.Categories;
import org.xacml4j.v30.XacmlSyntaxException;
import org.xacml4j.v30.types.XacmlTypes;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.reflect.TypeToken;

class AnnotatedResolverFactory
{
	private  final static Logger log = LoggerFactory.getLogger(AnnotatedResolverFactory.class);

	private final static TypeToken<?> ATTR_RESOLVER_RETURN_TYPE;

	static{
		ATTR_RESOLVER_RETURN_TYPE = new TypeToken<Map<String, BagOfAttributeExp>>(){};
	}

	/**
	 * Gets all {@link ContentResolver} from given instance
	 *
	 * @param instance an instance containing annotated resolvers
	 * @return a collection of found {@link ContentResolver}
	 * @throws XacmlSyntaxException
	 */
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
				Categories.parse(d.category()));
		b.cache(d.cacheTTL());
		XacmlAttributeDescriptor[] attributes = d.attributes();
		if(attributes == null ||
				attributes.length == 0){
			throw new XacmlSyntaxException("At least attribute " +
					"must be specified by the descriptor on method=\"{}\"", m.getName());
		}
		for(XacmlAttributeDescriptor attr : attributes){
			Optional<AttributeExpType> type = XacmlTypes.getType(attr.dataType());
			if(!type.isPresent()){
				throw new XacmlSyntaxException("Unknown XACML type=\"%s\"",
						attr.dataType());
			}
			b.attribute(attr.id(), type.get());

		}
		Pair<Boolean, List<AttributeReferenceKey>> info = parseResolverMethodParams(m);
		b.keys(info.getSecond());
		TypeToken<?> returnType = TypeToken.of(m.getGenericReturnType());
		if(log.isDebugEnabled()){
			log.debug("Attribute resolver id=\"{}\" return type=\"{}\"",
					d.id(), returnType.toString());
		}
		if(!ATTR_RESOLVER_RETURN_TYPE.equals(returnType)){
			throw new XacmlSyntaxException(
					"Attribute resolver method=\"%s\" " +
					"must return=\"%s\"", m.getName(),
					ATTR_RESOLVER_RETURN_TYPE.toString());
		}
		AttributeResolverDescriptor descriptor = b.build();
		return new AnnotatedAttributeResolver(descriptor,
				new Invocation<Map<String,BagOfAttributeExp>>(instance, m, info.getFirst()));
	}

	ContentResolver parseContentResolver(Object instance, Method m)
		throws XacmlSyntaxException
	{
		Preconditions.checkNotNull(instance);
		Preconditions.checkNotNull(m);
		Preconditions.checkArgument(m.getDeclaringClass().equals(instance.getClass()));
		XacmlContentResolverDescriptor d = m.getAnnotation(XacmlContentResolverDescriptor.class);
		ContentResolverDescriptorBuilder b = ContentResolverDescriptorBuilder.builder(
				d.id(), d.name(),
				Categories.parse(d.category()));
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
							"Resolver parameter without annotation at index=\"%d\" must be of type=\"%s\"",
							i, ResolverContext.class);
				}
				needPipContext = true;
				continue;
			}
			if(p.length > 0 &&
					p[0] instanceof XacmlAttributeDesignator)
			{
				if(!(types[i].equals(BagOfAttributeExp.class))){
					throw new XacmlSyntaxException(
							"Resolver method=\"%s\" " +
							"parameter at index=\"%d\" must be of type=\"%s\"",
							m.getName(), i, BagOfAttributeExp.class.getName());
				}
				XacmlAttributeDesignator ref = (XacmlAttributeDesignator)p[0];
				Optional<AttributeExpType> type = XacmlTypes.getType(ref.dataType());
				if(!type.isPresent()){
					throw new XacmlSyntaxException("Unknown XACML type=\"%s\"",
							ref.dataType());
				}
				keys.add(AttributeDesignatorKey
						.builder()
						.category(ref.category())
						.attributeId(ref.attributeId())
						.dataType(type.get())
						.issuer(ref.issuer())
						.build());
				continue;
			}
			if(p.length > 0 &&
					p[0] instanceof XacmlAttributeSelector)
			{
				if(!(types[i].equals(BagOfAttributeExp.class))){
					throw new XacmlSyntaxException(
							"Resolver method=\"%s\" " +
							"request key parameter " +
							"at index=\"%d\" must be of type=\"%s\"",
							m.getName(), i, BagOfAttributeExp.class.getName());
				}
				XacmlAttributeSelector ref = (XacmlAttributeSelector)p[0];
				Optional<AttributeExpType> type = XacmlTypes.getType(ref.dataType());
				if(!type.isPresent()){
					throw new XacmlSyntaxException("Unknown XACML type=\"%s\"",
							ref.dataType());
				}
				keys.add(AttributeSelectorKey
						.builder()
						.category(ref.category())
						.xpath(ref.xpath())
						.dataType(type.get())
						.contextSelectorId(ref.contextAttributeId())
						.build());
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
			List<BagOfAttributeExp> keys = context.getKeys();
			if(requiresContext){
				Object[] params = new Object[keys.size() + 1];
				params[0] = context;
				System.arraycopy(keys.toArray(), 0, params, 1, keys.size());
				return (T)m.invoke(instance, params);
			}
			return (T)m.invoke(instance, keys.toArray());
		}
	}

	/**
	 * An implementation {@link AttributeResolver} for
	 * annotated resolver classes
	 *
	 * @author Giedrius Trumpickas
	 */
	private final class AnnotatedAttributeResolver
		extends BaseAttributeResolver
	{
		private Invocation<Map<String, BagOfAttributeExp>> invocation;

		public AnnotatedAttributeResolver(
				AttributeResolverDescriptor descriptor,
				Invocation<Map<String, BagOfAttributeExp>> invocation) {
			super(descriptor);
			this.invocation = invocation;
		}

		@Override
		protected Map<String, BagOfAttributeExp> doResolve(
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
