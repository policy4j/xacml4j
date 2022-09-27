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

import com.google.common.base.Preconditions;
import com.google.common.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xacml4j.util.Pair;
import org.xacml4j.util.Reflections;
import org.xacml4j.v30.*;
import org.xacml4j.v30.types.XacmlTypes;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

class AnnotatedResolverFactory
{
	private  final static Logger LOG = LoggerFactory.getLogger(AnnotatedResolverFactory.class);

	private final static TypeToken<?> ATTR_RESOLVER_RETURN_TYPE;

	static{
		ATTR_RESOLVER_RETURN_TYPE = new TypeToken<Map<String, BagOfAttributeValues>>(){};
	}

	/**
	 * Gets all {@link Resolver<ContentRef>} from given defaultProvider
	 *
	 * @param instance an defaultProvider containing annotated resolvers
	 * @return a collection of found {@link Resolver<ContentRef>}
	 * @throws SyntaxException
	 */
	public Collection<ContentResolverDescriptor> getContentResolvers(Object instance)
		throws SyntaxException
	{
		Preconditions.checkNotNull(instance);
		Collection<ContentResolverDescriptor> resolvers = new LinkedList<>();
		List<Method> methods = Reflections.getAnnotatedMethods(instance.getClass(),
				XacmlContentResolverDescriptor.class);
		for(Method m : methods){
			ContentResolverDescriptor r = parseContentResolver(instance, m);
			if(LOG.isDebugEnabled()){
				LOG.debug("Parsing content resolver=\"{}\"",
				          r.getId());
			}
			resolvers.add(r);
		}
		return resolvers;
	}

	public Collection<AttributeResolverDescriptor> getAttributeResolvers(Object instance)
		throws SyntaxException
	{
		Preconditions.checkNotNull(instance);
		Collection<AttributeResolverDescriptor> resolvers = new LinkedList<>();
		List<Method> methods =  Reflections.getAnnotatedMethods(instance.getClass(),
				XacmlAttributeResolverDescriptor.class);
		for(Method m : methods){
			AttributeResolverDescriptor r = parseAttributeResolver(instance, m);
			if(LOG.isDebugEnabled()){
				LOG.debug("Parsing attribute resolver=\"{}\"",
				          r.getId());
			}
			resolvers.add(r);
		}
		return resolvers;
	}

	AttributeResolverDescriptor parseAttributeResolver(Object instance, Method m)
		throws SyntaxException
	{
		Preconditions.checkNotNull(instance);
		Preconditions.checkNotNull(m);
		Preconditions.checkArgument(m.getDeclaringClass().equals(instance.getClass()));
		XacmlAttributeResolverDescriptor d = m.getAnnotation(XacmlAttributeResolverDescriptor.class);
		AttributeResolverDescriptor.Builder b =
				AttributeResolverDescriptor.builder(
				d.id(), d.name(),
				d.issuer(),
				CategoryId.parse(d.category()).get());
		b.cache(d.cacheTTL());
		XacmlAttributeDescriptor[] attributes = d.attributes();
		if(attributes == null ||
				attributes.length == 0){
			throw SyntaxException.invalidResolverMethod(m,
					"At least attribute " +
							"must be specified by the descriptor");
		}
		for(XacmlAttributeDescriptor attr : attributes){
			LOG.debug("Adding attribute={}", attr);
			XacmlTypes.getType(attr.dataType())
							.map(t->b.attribute(attr.id(), t, attr.aliases()))
					.orElseThrow(
							()-> SyntaxException
									.invalidDataTypeId(attr.dataType()));
		}
		Pair<Boolean, List<AttributeReferenceKey>> info = parseResolverMethodParams(m);
		info.second()
				.forEach((r)->b.contextRef(r));
		TypeToken<?> returnType = TypeToken.of(m.getGenericReturnType());
		if(LOG.isDebugEnabled()){
			LOG.debug("Attribute resolver id=\"{}\" return type=\"{}\"",
			          d.id(), returnType.toString());
		}
		if(!ATTR_RESOLVER_RETURN_TYPE.equals(returnType)){
			throw SyntaxException.invalidResolverMethod(m,
					String.format("Invalid method return type, correct return type=\"%s\"",
					ATTR_RESOLVER_RETURN_TYPE.toString()));
		}
		final Invocation<Map<String, BagOfAttributeValues>> invocation = new Invocation(instance, m, info.first());
		return b.build(resolverContext -> invocation.invoke(resolverContext));
	}

	ContentResolverDescriptor parseContentResolver(Object instance, Method m)
		throws SyntaxException
	{
		Preconditions.checkNotNull(instance);
		Preconditions.checkNotNull(m);
		Preconditions.checkArgument(m.getDeclaringClass().equals(instance.getClass()));
		XacmlContentResolverDescriptor d = m.getAnnotation(XacmlContentResolverDescriptor.class);
		ContentResolverDescriptor.Builder b = ContentResolverDescriptor
				.builder(
				d.id(), d.name(),
						CategoryId.parse(d.category()).get());
		b.cache(d.cacheTTL());
		Pair<Boolean, List<AttributeReferenceKey>> info = parseResolverMethodParams(m);
		info.second().forEach(referenceKey -> b.contextRef(referenceKey));
		LOG.debug("Resolver={} method={}", m.getName(), m);
		if(!m.getReturnType().isAssignableFrom(ContentRef.class)){
			throw SyntaxException.invalidResolverMethod(m,
					String.format("Resolver must return=\"%s\"", ContentRef.class.getName()));
		}
		final Invocation<Content> invocation = new Invocation(instance, m, info.first());
		return b.build((resolverContext -> invocation.invoke(resolverContext)));
	}

	private Pair<Boolean, List<AttributeReferenceKey>> parseResolverMethodParams(Method m)
		throws SyntaxException
	{
		List<AttributeReferenceKey> keys = new LinkedList<AttributeReferenceKey>();
		Class<?>[] types = m.getParameterTypes();
		boolean needPipContext = false;
		int  i = 0;
		for(Annotation[] p : m.getParameterAnnotations())
		{
			if(p.length == 0 && i != 0){
				throw SyntaxException.invalidResolverMethod(m,
						"Only first parameter of the resolver method can be without annotation");
			}
			if(p.length == 0 && i == 0) {
				if(!types[i].equals(ResolverContext.class)){
					throw SyntaxException.invalidResolverMethod(m,
							String.format("Resolver parameter without annotation at index=\"%d\" must be of type=\"%s\"",
							i, ResolverContext.class.getSimpleName()));
				}
				needPipContext = true;
				continue;
			}
			if(p.length > 0 &&
					p[0] instanceof XacmlAttributeDesignator)
			{
				if(!(types[i].equals(BagOfAttributeValues.class))){
					throw SyntaxException.invalidResolverMethod(m,
							String.format("Resolver method parameter at index=\"%d\" must be of type=\"%s\"",
							i, BagOfAttributeValues.class.getName()));
				}
				XacmlAttributeDesignator ref = (XacmlAttributeDesignator)p[0];
				java.util.Optional<AttributeValueType> type = XacmlTypes.getType(ref.dataType());
				LOG.debug("designator={}", ref);
				keys.add(type.map(
						t-> AttributeDesignatorKey
								.builder()
								.category(ref.category())
								.attributeId(ref.attributeId())
								.dataType(t)
								.issuer(ref.issuer())
								.build())
						.orElseThrow(
								()-> SyntaxException
										.invalidDataTypeId(ref.dataType())));
				continue;
			}
			if(p.length > 0 &&
					p[0] instanceof XacmlAttributeSelector)
			{
				if(!(types[i].equals(BagOfAttributeValues.class))){
					throw SyntaxException.invalidResolverMethod(m,
							String.format("Resolver method request context key parameter=\"%s\" " +
							"at index=\"%d\" must be of type=\"%s\"",
							m.getName(), i, BagOfAttributeValues.class.getName()));
				}
				XacmlAttributeSelector ref = (XacmlAttributeSelector)p[0];
				LOG.debug("selector={}", ref);
				keys.add(XacmlTypes.getType(ref.dataType())
						.map(t->AttributeSelectorKey
								.builder()
								.category(ref.category())
								.xpath(ref.xpath())
								.dataType(t)
								.contextSelectorId(ref.contextAttributeId())
								.build())
						.orElseThrow(
								()-> SyntaxException
										.invalidDataTypeId(ref.dataType())));

				continue;
			}
			i++;
			throw SyntaxException.invalidResolverMethod(m,
						String.format("Unknown annotation of type=\"%s\" found",
						types[0].getClass()));
		}
		return new Pair(needPipContext, keys);
	}

	public static final class Invocation <T> implements Function<ResolverContext, T>
	{
		private final Method m;
		private final Object instance;
		private final boolean requiresContext;

		public Invocation(
				Object instance,
				Method m,
				boolean requiresContext){
			this.instance = instance;
			this.m = m;
			this.requiresContext = requiresContext;
		}

		@SuppressWarnings("unchecked")
		public T invoke(ResolverContext context)
		{
			try
			{
				Map<AttributeReferenceKey, BagOfAttributeValues> keys = context.getResolvedKeys();
				if (requiresContext) {
					Object[] params = new Object[keys.size() + 1];
					params[0] = context;
					System.arraycopy(keys.values().toArray(), 0, params, 1, keys.size());
					return (T)m.invoke(instance, params);
				}
				return (T)m.invoke(instance, keys.values().toArray());
			}catch (Exception e){
				throw new RuntimeException(e);
			}
		}

		@Override
		public T apply(ResolverContext context) {
			return invoke(context);
		}
	}
}
