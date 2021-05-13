package org.xacml4j.v30.spi.function;

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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xacml4j.v30.pdp.FunctionSpec;

import com.google.common.base.Preconditions;

/**
 * An implementation of {@link FunctionProvider} which
 * aggregates instances of {@link FunctionProvider}
 *
 * @author Giedrius Trumpickas
 */
public class AggregatingFunctionProvider
	implements FunctionProvider
{
	private final static Logger log = LoggerFactory.getLogger(AggregatingFunctionProvider.class);

	private Map<String, FunctionProvider> functions;

	public AggregatingFunctionProvider(){
		this.functions = new ConcurrentHashMap<String, FunctionProvider>();
	}

	public AggregatingFunctionProvider(FunctionProvider ...providers){
		this(Arrays.asList(providers));
	}

	/**
	 * Creates aggregating function provider with a given providers
	 *
	 * @param providers a collection  of {@link FunctionProvider} instances
	 */
	public AggregatingFunctionProvider(Collection<FunctionProvider> providers){
		this.functions = new ConcurrentHashMap<String, FunctionProvider>();
		for(FunctionProvider p : providers){
			add(p);
		}
	}

	/**
	 * Adds {@link FunctionProvider} to this aggregating provider
	 *
	 * @param provider a provider instance
	 * @exception IllegalArgumentException if function exported via
	 * given provider already exported via provider previously registered
	 * with this aggregating provider
	 */
	public final void add(FunctionProvider provider)
	{
		Preconditions.checkNotNull(provider);
		for(String functionId : provider.getProvidedFunctions())
		{
			if(functions.containsKey(functionId)){
				throw new IllegalArgumentException(String.format("Function provider " +
						"already contains a function with functionId=\"%s\"",
						functionId));
			}
			FunctionSpec spec = provider.getFunction(functionId);
			if(log.isDebugEnabled()){
				log.debug("Adding function=\"{}\"", functionId);
			}
			Preconditions.checkArgument(spec != null);
			this.functions.put(functionId, provider);
		}
	}

	@Override
	public final FunctionSpec remove(String functionId) {
		FunctionProvider p = functions.remove(functionId);
		return (p != null)?p.getFunction(functionId):null;
	}

	@Override
	public final FunctionSpec getFunction(String functionId) {
		FunctionProvider provider = functions.get(functionId);
		return (provider != null)?provider.getFunction(functionId):null;
	}

	@Override
	public final Iterable<String> getProvidedFunctions() {
		return  Collections.unmodifiableCollection(functions.keySet());
	}

	@Override
	public final boolean isFunctionProvided(String functionId) {
		return functions.containsKey(functionId);
	}
}
