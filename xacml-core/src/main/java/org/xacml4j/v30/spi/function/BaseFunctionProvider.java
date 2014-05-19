package org.xacml4j.v30.spi.function;

/*
 * #%L
 * Artagon XACML 3.0 Core Engine Implementation
 * %%
 * Copyright (C) 2009 - 2014 Artagon
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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.xacml4j.v30.pdp.FunctionSpec;

import com.google.common.base.Preconditions;

class BaseFunctionProvider implements FunctionProvider
{
	private ConcurrentMap<String, FunctionSpec> functions;

	protected BaseFunctionProvider(){
		this.functions = new ConcurrentHashMap<String, FunctionSpec>();
	}

	/**
	 * Adds given {@link FunctionSpec} to this factory
	 *
	 * @param spec a function specification
	 */
	protected final void add(FunctionSpec spec)
	{
		Preconditions.checkNotNull(spec);
		FunctionSpec other = functions.putIfAbsent(spec.getId(), spec);
		Preconditions.checkState(other == null,
					"This factory already contains " +
					"function=\"%s\" with a given identifier=\"%s\"",
					spec, spec.getId());
		if(spec.getLegacyId() != null){
			other = functions.putIfAbsent(spec.getLegacyId(), spec);
			Preconditions.checkState(other == null,
					"This factory already contains " +
					"function=\"%s\" with a given identifier=\"%s\"",
					spec, spec.getId());
		}
	}



	@Override
	public FunctionSpec remove(String functionId) {
		return functions.remove(functionId);
	}

	@Override
	public final FunctionSpec getFunction(String functionId) {
		return functions.get(functionId);
	}

	@Override
	public final Iterable<String> getProvidedFunctions() {
		return functions.keySet();
	}

	@Override
	public final boolean isFunctionProvided(String functionId) {
		return functions.containsKey(functionId);
	}

}
