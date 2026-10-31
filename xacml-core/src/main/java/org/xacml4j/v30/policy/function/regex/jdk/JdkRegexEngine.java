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

import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.xacml4j.v30.policy.function.regex.CompiledRegex;
import org.xacml4j.v30.policy.function.regex.RegexBudget;
import org.xacml4j.v30.policy.function.regex.RegexCompileException;
import org.xacml4j.v30.policy.function.regex.RegexEngine;
import org.xacml4j.v30.policy.function.regex.RegexLimitException;
import org.xacml4j.v30.policy.function.regex.RegexLimits;
import org.xacml4j.v30.policy.function.regex.RegexProfile;
import org.xacml4j.v30.policy.function.regex.XacmlPattern;

import com.google.common.base.Preconditions;

/**
 * The {@code jdk} engine: {@code java.util.regex} behind the SPI (change add-regex-engine-spi,
 * designs D1, D2, D4, D5). In the {@code legacy} profile a pattern is translated as before the
 * SPI and bounded by the repetition weight; in the {@code xf} profile it is read with
 * {@link XacmlPattern}, bounded by the repeat count and the program size, and emitted in Java
 * syntax. A match reads the input through the deadline of its budget; an {@link Error} of the
 * matcher counts as a limit.
 */
public final class JdkRegexEngine implements RegexEngine
{
	public static final String NAME = "jdk";

	private static final Set<RegexProfile> PROFILES = Set.of(RegexProfile.LEGACY, RegexProfile.XF);

	public JdkRegexEngine(){
	}

	@Override
	public String name(){
		return NAME;
	}

	@Override
	public Set<RegexProfile> profiles(){
		return PROFILES;
	}

	@Override
	public CompiledRegex compile(String pattern, RegexProfile profile, RegexLimits limits){
		Preconditions.checkNotNull(pattern, "pattern");
		Preconditions.checkNotNull(limits, "limits");
		Preconditions.checkArgument(offers(profile), "engine %s does not offer profile %s", NAME, profile);
		if(pattern.length() > limits.maxPatternLength()){
			throw RegexCompileException.limit("pattern longer than the limit");
		}
		Pattern compiled;
		try{
			compiled = (profile == RegexProfile.LEGACY) ? legacy(pattern, limits) : xf(pattern, limits);
		}catch(Error e){
			throw RegexCompileException.limit(e);
		}
		return new Compiled(compiled, limits.maxInputLength());
	}

	/** The translation of an XACML pattern to {@code java.util.regex} syntax of the {@code legacy} profile */
	public static String toLegacyJavaSyntax(String pattern){
		return LegacySyntax.toJavaSyntax(pattern);
	}

	private static Pattern legacy(String pattern, RegexLimits limits){
		String java = LegacySyntax.toJavaSyntax(pattern);
		// The largest weight stands for "does not fit" and "can not be scanned";
		// it is beyond every limit, also beyond a limit of Long.MAX_VALUE.
		long weight = LegacySyntax.repetitionWeight(java);
		if(weight == Long.MAX_VALUE || weight > limits.maxRepetition()){
			throw RegexCompileException.limit("repetition weight above the limit");
		}
		try{
			return Pattern.compile(java);
		}catch(PatternSyntaxException e){
			throw compilationFailure(e, RegexProfile.LEGACY);
		}
	}

	private static Pattern xf(String pattern, RegexLimits limits){
		XacmlPattern parsed = XacmlPattern.parse(pattern);
		if(parsed.largestCount() > limits.maxRepeatCount()){
			throw RegexCompileException.limit("repeat count above the limit");
		}
		if(parsed.sizeEstimate() > limits.maxProgramSize()){
			throw RegexCompileException.limit("program size estimate above the limit");
		}
		String java = XfSyntax.toJavaSyntax(parsed);
		if(java.length() > limits.maxProgramSize()){
			throw RegexCompileException.limit("emitted pattern longer than the limit");
		}
		try{
			return Pattern.compile(java);
		}catch(PatternSyntaxException e){
			throw compilationFailure(e, RegexProfile.XF);
		}
	}

	static RegexCompileException compilationFailure(PatternSyntaxException failure, RegexProfile profile){
		if("Stack overflow during pattern compilation".equals(failure.getDescription())){
			return RegexCompileException.resourceLimit(failure);
		}
		return profile == RegexProfile.LEGACY
				? RegexCompileException.malformed(failure.getDescription(), failure)
				: RegexCompileException.unsupported(failure.getDescription(), NAME);
	}

	/** A compiled {@link Pattern}; it is immutable and every match has a matcher of its own */
	private static final class Compiled implements CompiledRegex
	{
		private final Pattern pattern;
		private final int maxInputLength;

		Compiled(Pattern pattern, int maxInputLength){
			this.pattern = pattern;
			this.maxInputLength = maxInputLength;
		}

		@Override
		public boolean find(CharSequence input, RegexBudget budget){
			if(input.length() > maxInputLength){
				throw new RegexLimitException(RegexBudget.LIMIT_EXCEEDED_MESSAGE);
			}
			boolean found;
			try{
				// XF 7.6.2 fn:matches succeeds when any substring matches.
				found = pattern.matcher(new DeadlineInput(input, budget)).find();
			}catch(Error e){
				throw new RegexLimitException(e);
			}
			// The deadline is seen when the matcher reads the input. A match that
			// worked without a read was not stopped; its late result is not used.
			budget.check();
			return found;
		}

		@Override
		public String toString(){
			return "jdk:" + pattern.pattern();
		}
	}
}
