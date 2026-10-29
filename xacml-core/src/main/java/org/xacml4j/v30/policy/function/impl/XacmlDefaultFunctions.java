package org.xacml4j.v30.policy.function.impl;

/*
 * #%L
 * Xacml4J Core Engine Implementation
 * %%
 * Copyright (C) 2009 - 2019 Xacml4J.org
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.auto.service.AutoService;
import com.google.common.base.Preconditions;
import org.xacml4j.v30.policy.function.FunctionInvocationFactory;
import org.xacml4j.v30.policy.function.FunctionProvider;
import org.xacml4j.v30.policy.function.XacmlFunctionProvider;

/**
 * The standard XACML functions. The regexp-match functions are an instance of
 * {@link RegExpFunctions.Matching}, the six functions on one regular-expression engine, profile
 * and limits (change add-regex-engine-spi, design D3): the one given, or the one the system
 * properties {@link RegExpFunctions#ENGINE_PROPERTY} and {@link RegExpFunctions#PROFILE_PROPERTY}
 * name when this provider is built, or {@code jdk} with {@code legacy}.
 */
@AutoService(FunctionProvider.class)
@XacmlFunctionProvider(description = "XACML 3.0 standard functions")
public final class XacmlDefaultFunctions extends BaseFunctionProvider
{
    /**
     * The provider classes of the standard functions with static methods, none of which writes advice
     * or obligations (design D36); the regexp-match functions are an instance (design D3 of
     * add-regex-engine-spi)
     */
    static final List<Class<?>> PROVIDERS = List.of(
            ArithmeticFunctions.class,
            BagFunctions.class,
            DateTimeArithmeticFunctions.class,
            EqualityPredicates.class,
            LogicalFunctions.class,
            NonNumericComparisonFunctions.class,
            NumericConversionFunctions.class,
            NumericComparisonFunctions.class,
            SetFunctions.class,
            SpecialMatchFunctions.class,
            StringConversionFunctions.class,
            StringFunctions.class,
            XPathFunctions.class,
            HigherOrderFunctions.class,
            AttributeDesignatorFunctions.class);

    /**
     * Tells whether a class is one of the engine's own function providers, whose methods the default
     * invocation factory calls as they are and which write no responses (design D36): the classes of
     * {@link #PROVIDERS}, {@link RegExpFunctions} with its static entry points, and the regexp
     * functions of an engine
     *
     * @param provider a class with function methods
     * @return {@code true} for a provider class of the engine
     */
    static boolean isBuiltIn(Class<?> provider){
        return PROVIDERS.contains(provider) || provider == RegExpFunctions.class
                || provider == RegExpFunctions.Matching.class;
    }

    public XacmlDefaultFunctions(){
        this(FunctionInvocationFactory.defaultFactory());
    }

    /** The standard functions with the regexp functions of the system properties, read now (design D3) */
    public XacmlDefaultFunctions(FunctionInvocationFactory invocationFactory){
        this(invocationFactory, RegExpFunctions.matching());
    }

    /**
     * @param invocationFactory the invocation of the function methods
     * @param regexp the regexp-match functions: one engine, profile and limits (design D3)
     */
    public XacmlDefaultFunctions(FunctionInvocationFactory invocationFactory, RegExpFunctions.Matching regexp){
        super(toFunctionSpec(providers(invocationFactory, regexp)));
    }

    private static Collection<FunctionProvider> providers(FunctionInvocationFactory invocationFactory,
            RegExpFunctions.Matching regexp){
        Preconditions.checkNotNull(regexp, "regexp");
        List<FunctionProvider> providers = new ArrayList<>(AnnotationBasedFunctionProvider.toProviders(
                invocationFactory, PROVIDERS.toArray(new Class<?>[0])));
        providers.addAll(AnnotationBasedFunctionProvider.toProviders(invocationFactory, regexp));
        return providers;
    }
}
