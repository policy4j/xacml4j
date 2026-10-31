package org.xacml4j.v30.policy.function.regex.jdk;

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

import org.xacml4j.v30.policy.function.regex.RegexBudget;

/**
 * The input of one match. {@code java.util.regex} reads the input one character at a time, also
 * while it backtracks, so the deadline of the budget is checked here, once per
 * {@value #CLOCK_INTERVAL} characters.
 */
final class DeadlineInput implements CharSequence
{
	static final int CLOCK_INTERVAL = 1024;

	private final CharSequence value;
	private final RegexBudget budget;
	private int untilClock = CLOCK_INTERVAL;

	DeadlineInput(CharSequence value, RegexBudget budget){
		this.value = value;
		this.budget = budget;
	}

	@Override
	public char charAt(int index){
		if(--untilClock <= 0){
			untilClock = CLOCK_INTERVAL;
			budget.check();
		}
		return value.charAt(index);
	}

	@Override
	public int length(){
		return value.length();
	}

	@Override
	public CharSequence subSequence(int start, int end){
		return new DeadlineInput(value.subSequence(start, end), budget);
	}

	@Override
	public String toString(){
		return value.toString();
	}
}
