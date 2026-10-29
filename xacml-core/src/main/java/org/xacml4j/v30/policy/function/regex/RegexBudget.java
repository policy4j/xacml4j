package org.xacml4j.v30.policy.function.regex;

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

import java.util.function.LongSupplier;

import com.google.common.base.Preconditions;

/**
 * The time of one match (change add-regex-engine-spi, design D1): a deadline that the engine
 * checks while it reads the input and the caller checks after the steps it cannot interrupt. A
 * budget belongs to one call and is never kept in a compiled pattern.
 */
public final class RegexBudget
{
	/** The status message when a limit is exceeded. It names neither the pattern nor the input. */
	public static final String LIMIT_EXCEEDED_MESSAGE = "Regular expression evaluation limit exceeded";

	private final long deadlineNanos;
	private final LongSupplier clock;

	private RegexBudget(long deadlineNanos, LongSupplier clock){
		this.deadlineNanos = deadlineNanos;
		this.clock = clock;
	}

	/**
	 * Starts a budget that ends after the match time of the limits, on the clock of
	 * {@link System#nanoTime()}
	 */
	public static RegexBudget start(RegexLimits limits){
		return start(limits.maxMatchNanos(), System::nanoTime);
	}

	/**
	 * Starts a budget, reading the clock once
	 *
	 * @param nanos the time of the budget, in nanoseconds
	 * @param clock the time in nanoseconds, as {@link System#nanoTime()} gives it
	 */
	public static RegexBudget start(long nanos, LongSupplier clock){
		Preconditions.checkArgument(nanos > 0, "nanos must be positive");
		Preconditions.checkNotNull(clock, "clock");
		return new RegexBudget(clock.getAsLong() + nanos, clock);
	}

	/** Whether the deadline has passed; reads the clock */
	public boolean expired(){
		return clock.getAsLong() - deadlineNanos > 0;
	}

	/**
	 * Fails when the deadline has passed; reads the clock
	 *
	 * @throws RegexLimitException when the deadline has passed
	 */
	public void check(){
		if(expired()){
			throw new RegexLimitException(LIMIT_EXCEEDED_MESSAGE);
		}
	}
}
