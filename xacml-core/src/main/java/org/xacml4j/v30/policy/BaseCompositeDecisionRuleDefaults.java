package org.xacml4j.v30.policy;

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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.util.Map;

import org.xacml4j.v30.PolicyElement;
import org.xacml4j.v30.XPathVersion;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

public abstract class BaseCompositeDecisionRuleDefaults
		implements PolicyElement
{
	public static final String XPATH_VERSION = "XPathVersion";

	protected final Map<String, Object> values;

	protected BaseCompositeDecisionRuleDefaults(
			Builder<?> b){
		Preconditions.checkNotNull(b);
		this.values = b.values.build();
	}

	public final XPathVersion getXPathVersion(){
		return (XPathVersion)values.get(XPATH_VERSION);
	}

	@SuppressWarnings("unchecked")
	public final <T> T getValue(String name){
		return (T)values.get(name);
	}

	@Override
	public String toString(){
		return MoreObjects
				.toStringHelper(this)
				.add("values", values)
				.toString();
	}

	@Override
	public int hashCode(){
		return values.hashCode();
	}

	public static abstract class Builder<T extends Builder<?>>
	{
		private static final ImmutableSet<Class<?>> IMMUTABLE_VALUE_TYPES = ImmutableSet.of(
				XPathVersion.class, String.class, Boolean.class, Character.class,
				Byte.class, Short.class, Integer.class, Long.class, Float.class, Double.class,
				BigInteger.class, BigDecimal.class, URI.class);

		private ImmutableMap.Builder<String, Object> values = ImmutableMap.builder();

		public T xpathVersion(String xpathVersion){
			XPathVersion v = null;
			if(xpathVersion != null){
				v = XPathVersion.parse(xpathVersion);
			}
			values.put(XPATH_VERSION, v == null?XPathVersion.XPATH1:v);
			return getThis();
		}

		/**
		 * Adds an immutable default: {@link XPathVersion}, {@link String}, a boxed primitive,
		 * {@link BigInteger}, {@link BigDecimal}, or {@link URI}. Subclasses are not accepted.
		 *
		 * @throws IllegalArgumentException if the value has any other type
		 */
		public T value(String name, Object v){
			Preconditions.checkNotNull(v);
			Preconditions.checkArgument(IMMUTABLE_VALUE_TYPES.contains(v.getClass()),
					"Default value \"%s\" must be an immutable scalar", name);
			this.values.put(name, v);
			return getThis();
		}

		protected abstract T getThis();
	}
}
