package org.xacml4j.v30.policy.function;

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
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.xacml4j.v30.BagStorage;

/**
 * Declares that a two-argument predicate has the same result as this
 * comparison with the first argument as the literal. The predicate must be
 * side-effect-free and total for valid values. IEEE mode uses primitive double
 * comparison. Temporal ordering uses the value's typed comparison, including
 * the fixed UTC default of dates and dateTimes. Time ordering without an
 * explicit timezone, unsupported values and temporal comparison failures
 * leave the bag to ordinary evaluation. Only directly bound methods use this declaration.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface XacmlFuncBagScan
{
	BagStorage.Comparison value() default BagStorage.Comparison.EQUAL;
	boolean ieee() default false;
}
