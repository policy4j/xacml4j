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

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * A XACML function to java method invocation factory
 *
 * @author Giedrius Trumpiclas
 */
public interface FunctionInvocationFactory
{
	<T> PolicyToPlatformFunctionInvocation<T> create(Object instance, Method m);

	static FunctionInvocationFactory defaultFactory(){
	    return Default.INSTANCE;
    }

    /**
     * Default implementation of {@link FunctionInvocationFactory}
     */
    final class Default implements FunctionInvocationFactory
    {
        private final static FunctionInvocationFactory INSTANCE = new Default();

        private Default(){}

        @Override
        public <T> PolicyToPlatformFunctionInvocation<T> create(final Object instance, final Method m) {
            return (p)->doInvoke(instance,
                    Objects.requireNonNull(m, "method"), p);
        }

        private static final <T> T doInvoke(
                final Object instance,
                final Method m,
                final Object ...params){
            try{
                return (T)m.invoke(instance, params);
            }catch(Throwable t){
                throw new RuntimeException(t);
            }
        }
    }
}
