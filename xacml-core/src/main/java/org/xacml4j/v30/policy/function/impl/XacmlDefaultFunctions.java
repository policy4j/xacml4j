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

import com.google.auto.service.AutoService;
import org.xacml4j.v30.policy.function.FunctionInvocationFactory;
import org.xacml4j.v30.policy.function.FunctionProvider;
import org.xacml4j.v30.policy.function.XacmlFunctionProvider;

@AutoService(FunctionProvider.class)
@XacmlFunctionProvider(description = "XACML 3.0 standard functions")
public final class XacmlDefaultFunctions extends BaseFunctionProvider
{
    public XacmlDefaultFunctions(){
        this(FunctionInvocationFactory.defaultFactory());
    }

    public XacmlDefaultFunctions(FunctionInvocationFactory invocationFactory){
        super(toFunctionSpec(
                AnnotationBasedFunctionProvider.toProviders(
                        invocationFactory,
                        ArithmeticFunctions.class,
                        BagFunctions.class,
                        DateTimeArithmeticFunctions.class,
                        EqualityPredicates.class,
                        LogicalFunctions.class,
                        NonNumericComparisonFunctions.class,
                        RegExpFunctions.class,
                        NumericConversionFunctions.class,
                        NumericComparisonFunctions.class,
                        SetFunctions.class,
                        SpecialMatchFunctions.class,
                        StringConversionFunctions.class,
                        StringFunctions.class,
                        XPathFunctions.class,
                        HigherOrderFunctions.class,
                        AttributeDesignatorFunctions.class)));
    }
}
