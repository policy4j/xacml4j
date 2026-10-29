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

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

/**
 * The limits of the regular-expression functions (change add-regex-engine-spi, design D1; the
 * defaults of maintainer ruling 2026-10-07, run 061 D17, and of the decisions of 2026-10-28).
 * The pattern length, the input length and the match time bound every profile; the repetition
 * weight bounds the {@code legacy} profile; the repeat count and the program size bound the
 * {@code xf} profile.
 */
public final class RegexLimits
{
	public static final int DEFAULT_MAX_PATTERN_LENGTH = 10_000;
	public static final int DEFAULT_MAX_INPUT_LENGTH = 100_000;
	public static final long DEFAULT_MAX_MATCH_MILLIS = 1_000;
	/** The largest product of the minimum counts of nested counted repetitions ({@code legacy}) */
	public static final long DEFAULT_MAX_REPETITION = 1_000_000;
	/** The largest count of one counted repetition ({@code xf}), as RE2 and Go bound it */
	public static final int DEFAULT_MAX_REPEAT_COUNT = 1_000;
	/** The largest estimate of the expanded program and the longest emitted pattern ({@code xf}) */
	public static final long DEFAULT_MAX_PROGRAM_SIZE = 100_000;
	public static final int DEFAULT_CACHE_SIZE = 256;

	/**
	 * The largest values that can be configured. A pattern is rewritten before it is compiled,
	 * which can make it up to four times as long, so its length limit stays far below
	 * {@link Integer#MAX_VALUE}. A cache evicts when its size is above its limit, so that limit
	 * must be below the largest size.
	 */
	public static final int LARGEST_MAX_PATTERN_LENGTH = Integer.MAX_VALUE / 8;
	public static final int LARGEST_CACHE_SIZE = Integer.MAX_VALUE - 1;

	private static final RegexLimits DEFAULTS = new Builder().build();

	private final int maxPatternLength;
	private final int maxInputLength;
	private final long maxMatchMillis;
	private final long maxRepetition;
	private final int maxRepeatCount;
	private final long maxProgramSize;
	private final int cacheSize;

	private RegexLimits(Builder b){
		Preconditions.checkArgument(b.maxPatternLength > 0 && b.maxPatternLength <= LARGEST_MAX_PATTERN_LENGTH,
				"maxPatternLength must be between 1 and %s", LARGEST_MAX_PATTERN_LENGTH);
		Preconditions.checkArgument(b.maxInputLength > 0, "maxInputLength must be positive");
		Preconditions.checkArgument(b.maxMatchMillis > 0, "maxMatchMillis must be positive");
		Preconditions.checkArgument(b.maxRepetition > 0, "maxRepetition must be positive");
		Preconditions.checkArgument(b.maxRepeatCount > 0, "maxRepeatCount must be positive");
		Preconditions.checkArgument(b.maxProgramSize > 0, "maxProgramSize must be positive");
		Preconditions.checkArgument(b.cacheSize > 0 && b.cacheSize <= LARGEST_CACHE_SIZE,
				"cacheSize must be between 1 and %s", LARGEST_CACHE_SIZE);
		this.maxPatternLength = b.maxPatternLength;
		this.maxInputLength = b.maxInputLength;
		this.maxMatchMillis = b.maxMatchMillis;
		this.maxRepetition = b.maxRepetition;
		this.maxRepeatCount = b.maxRepeatCount;
		this.maxProgramSize = b.maxProgramSize;
		this.cacheSize = b.cacheSize;
	}

	public static RegexLimits defaults(){
		return DEFAULTS;
	}

	public static Builder builder(){
		return new Builder();
	}

	public Builder toBuilder(){
		return new Builder()
				.maxPatternLength(maxPatternLength)
				.maxInputLength(maxInputLength)
				.maxMatchMillis(maxMatchMillis)
				.maxRepetition(maxRepetition)
				.maxRepeatCount(maxRepeatCount)
				.maxProgramSize(maxProgramSize)
				.cacheSize(cacheSize);
	}

	/** The largest pattern, in characters */
	public int maxPatternLength(){
		return maxPatternLength;
	}

	/** The largest input, in characters */
	public int maxInputLength(){
		return maxInputLength;
	}

	/** The time for compiling a pattern and for one match */
	public long maxMatchMillis(){
		return maxMatchMillis;
	}

	public long maxMatchNanos(){
		return TimeUnit.MILLISECONDS.toNanos(maxMatchMillis);
	}

	/**
	 * The largest product of the minimum counts of nested counted repetitions ({@code {n}},
	 * {@code {n,}}, {@code {n,m}}) in a pattern of the {@code legacy} profile
	 */
	public long maxRepetition(){
		return maxRepetition;
	}

	/** The largest count of one counted repetition in a pattern of the {@code xf} profile */
	public int maxRepeatCount(){
		return maxRepeatCount;
	}

	/**
	 * The largest estimate of the expanded program of a pattern of the {@code xf} profile, and
	 * the longest pattern an engine may emit for it
	 */
	public long maxProgramSize(){
		return maxProgramSize;
	}

	/** The number of compiled patterns an engine instance keeps */
	public int cacheSize(){
		return cacheSize;
	}

	@Override
	public boolean equals(Object o){
		if(o == this){
			return true;
		}
		if(!(o instanceof RegexLimits l)){
			return false;
		}
		return maxPatternLength == l.maxPatternLength && maxInputLength == l.maxInputLength
				&& maxMatchMillis == l.maxMatchMillis && maxRepetition == l.maxRepetition
				&& maxRepeatCount == l.maxRepeatCount && maxProgramSize == l.maxProgramSize
				&& cacheSize == l.cacheSize;
	}

	@Override
	public int hashCode(){
		return Objects.hash(maxPatternLength, maxInputLength, maxMatchMillis, maxRepetition,
				maxRepeatCount, maxProgramSize, cacheSize);
	}

	@Override
	public String toString(){
		return MoreObjects.toStringHelper(this)
				.add("maxPatternLength", maxPatternLength)
				.add("maxInputLength", maxInputLength)
				.add("maxMatchMillis", maxMatchMillis)
				.add("maxRepetition", maxRepetition)
				.add("maxRepeatCount", maxRepeatCount)
				.add("maxProgramSize", maxProgramSize)
				.add("cacheSize", cacheSize)
				.toString();
	}

	public static final class Builder
	{
		private int maxPatternLength = DEFAULT_MAX_PATTERN_LENGTH;
		private int maxInputLength = DEFAULT_MAX_INPUT_LENGTH;
		private long maxMatchMillis = DEFAULT_MAX_MATCH_MILLIS;
		private long maxRepetition = DEFAULT_MAX_REPETITION;
		private int maxRepeatCount = DEFAULT_MAX_REPEAT_COUNT;
		private long maxProgramSize = DEFAULT_MAX_PROGRAM_SIZE;
		private int cacheSize = DEFAULT_CACHE_SIZE;

		private Builder(){
		}

		public Builder maxPatternLength(int maxPatternLength){
			this.maxPatternLength = maxPatternLength;
			return this;
		}

		public Builder maxInputLength(int maxInputLength){
			this.maxInputLength = maxInputLength;
			return this;
		}

		public Builder maxMatchMillis(long maxMatchMillis){
			this.maxMatchMillis = maxMatchMillis;
			return this;
		}

		public Builder maxRepetition(long maxRepetition){
			this.maxRepetition = maxRepetition;
			return this;
		}

		public Builder maxRepeatCount(int maxRepeatCount){
			this.maxRepeatCount = maxRepeatCount;
			return this;
		}

		public Builder maxProgramSize(long maxProgramSize){
			this.maxProgramSize = maxProgramSize;
			return this;
		}

		public Builder cacheSize(int cacheSize){
			this.cacheSize = cacheSize;
			return this;
		}

		/**
		 * @throws IllegalArgumentException when a limit is not positive or above its largest value
		 */
		public RegexLimits build(){
			return new RegexLimits(this);
		}
	}
}
