package org.xacml4j.v30.policy.function.impl;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.Status;
import org.xacml4j.v30.SyntaxException;
import org.xacml4j.v30.policy.function.*;
import org.xacml4j.v30.types.*;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import com.google.common.base.Preconditions;

/**
 * The XACML regular-expression match functions.
 * <p>
 * The pattern, the first argument of each function, is policy content.
 * The limits of this class bound the length of the pattern, the length of
 * the input, the time of a match that reads its input, and nested counted
 * repetitions. They do not bound every pattern: a pattern can make the
 * matcher work without a read of the input, and that work is not stopped
 * (bug policy4j-the.4.58, accepted as a limit; see
 * {@link #repetitionWeight(java.lang.String)}). A policy must therefore
 * not take the pattern from the request, as with an attribute designator
 * or selector as the first argument: the request would then control the
 * processor time of the PDP. Nothing checks that.
 */
@XacmlFunctionProvider(description="XACML regular expression functions")
public final class RegExpFunctions
{
	private final static Logger log = LoggerFactory.getLogger(RegExpFunctions.class);

	/** Private constructor for utility class */
	private RegExpFunctions() {}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:string-regexp-match")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanVal stringRegexpMatch(
			@XacmlEvaluationContextParam EvaluationContext context,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string") StringVal regexp,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string") StringVal input)
	{
		 return matches(context, regexp, input);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:2.0:function:anyURI-regexp-match")
	@XacmlLegacyFunc(id="urn:oasis:names:tc:xacml:1.0:function:anyURI-regexp-match")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanVal anyURIRegexpMatch(
			@XacmlEvaluationContextParam EvaluationContext context,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string") StringVal regexp,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#anyURI") AnyURI input)
	{
		 return matches(context, regexp, input);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:2.0:function:ipAddress-regexp-match")
	@XacmlLegacyFunc(id="urn:oasis:names:tc:xacml:1.0:function:ipAddress-regexp-match")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanVal ipAddressRegexpMatch(
			@XacmlEvaluationContextParam EvaluationContext context,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string") StringVal regexp,
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:2.0:data-type:ipAddress") IPAddress input)
	{
		 return matches(context, regexp, input);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:2.0:function:dnsName-regexp-match")
	@XacmlLegacyFunc(id="urn:oasis:names:tc:xacml:1.0:function:dnsName-regexp-match")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanVal dnsNameRegexpMatch(
			@XacmlEvaluationContextParam EvaluationContext context,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string") StringVal regexp,
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:2.0:data-type:dnsName")DNSName input)
	{
		 return matches(context, regexp, input);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:2.0:function:rfc822Name-regexp-match")
	@XacmlLegacyFunc(id="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-regexp-match")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanVal rfc822NameRegexpMatch(
			@XacmlEvaluationContextParam EvaluationContext context,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string") StringVal regexp,
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name")RFC822Name input)
	{
		 return matches(context, regexp, input);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:2.0:function:x500Name-regexp-match")
	@XacmlLegacyFunc(id="urn:oasis:names:tc:xacml:1.0:function:x500Name-regexp-match")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanVal x500NameRegexpMatch(
			@XacmlEvaluationContextParam EvaluationContext context,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string") StringVal regexp,
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:x500Name") X500Name input)
	{
		 return matches(context, regexp, input);
	}

	private static BooleanVal matches(EvaluationContext context, StringVal regexp, Value value){
		if(log.isDebugEnabled()){
			log.debug("Matching input=\"{}\" via regexp=\"{}\"", value, regexp);
		}
		Optional<TypeToString> toString = TypeToString.forType(value.getEvaluatesTo()
				.getValueType());
		return BooleanVal.of(
				toString.map(v->v.toString(value))
				.map(v->matches(regexp.get(), v))
				.orElseThrow(
						()-> SyntaxException
								.invalidDataTypeId(value.getEvaluatesTo())));

	}

	/**
	 * Default limits of one match (maintainer ruling 2026-10-07, run 061
	 * D17). Each can be set with a system property named after this class:
	 * {@code .maxPatternLength}, {@code .maxInputLength},
	 * {@code .maxMatchMillis}, {@code .maxRepetition}, {@code .cacheSize}.
	 */
	static final int DEFAULT_MAX_PATTERN_LENGTH = 10_000;
	static final int DEFAULT_MAX_INPUT_LENGTH = 100_000;
	static final long DEFAULT_MAX_MATCH_MILLIS = 1_000;
	static final long DEFAULT_MAX_REPETITION = 1_000_000;
	static final int DEFAULT_CACHE_SIZE = 256;

	/**
	 * The largest values that can be configured. A pattern is rewritten
	 * before it is compiled, which can make it up to four times as long,
	 * so its length limit stays far below {@link Integer#MAX_VALUE}. The
	 * cache evicts when its size is above its limit, so that limit must be
	 * below the largest size.
	 */
	static final int LARGEST_MAX_PATTERN_LENGTH = java.lang.Integer.MAX_VALUE / 8;
	static final int LARGEST_CACHE_SIZE = java.lang.Integer.MAX_VALUE - 1;

	/** The status message when a limit is exceeded. It names neither the pattern nor the input. */
	static final java.lang.String LIMIT_EXCEEDED_MESSAGE = "Regular expression evaluation limit exceeded";

	private final static java.lang.String PROPERTY_PREFIX = RegExpFunctions.class.getName();

	private final static Limits LIMITS = Limits.from(System::getProperty);

	private final static int CACHE_SIZE = cacheSize(System::getProperty);

	private final static ThreadLocal<PatternCache> REGEXP_CACHE =
			ThreadLocal.withInitial(()->new PatternCache(CACHE_SIZE));

	private final static java.lang.Boolean CACHE_REGXP = java.lang.Boolean.parseBoolean(
					System.getProperty(PROPERTY_PREFIX + ".cacheRegExps"));

	/**
	 * Reads the size of the pattern cache from the property
	 * {@code .cacheSize}, as {@link Limits#from} reads the limits.
	 *
	 * @param properties gives the value of a property by its name
	 * @return the number of patterns that one thread caches
	 * @throws IllegalArgumentException if the value is not a positive number
	 */
	static int cacheSize(java.util.function.Function<java.lang.String, java.lang.String> properties){
		return (int)Limits.number(properties, ".cacheSize", DEFAULT_CACHE_SIZE, LARGEST_CACHE_SIZE);
	}

	/** Limits of one regular expression match. */
	static final class Limits
	{
		private final int maxPatternLength;
		private final int maxInputLength;
		private final long maxMatchNanos;
		private final long maxRepetition;

		Limits(int maxPatternLength, int maxInputLength, long maxMatchMillis){
			this(maxPatternLength, maxInputLength, maxMatchMillis, DEFAULT_MAX_REPETITION);
		}

		/**
		 * @param maxPatternLength the largest pattern, in characters
		 * @param maxInputLength the largest input, in characters
		 * @param maxMatchMillis the time for compiling the pattern and for
		 * one match
		 * @param maxRepetition the largest product of the minimum counts
		 * of nested counted repetitions ({@code {n}}, {@code {n,}},
		 * {@code {n,m}}) in a pattern
		 */
		Limits(int maxPatternLength, int maxInputLength, long maxMatchMillis, long maxRepetition){
			Preconditions.checkArgument(maxPatternLength > 0 && maxPatternLength <= LARGEST_MAX_PATTERN_LENGTH,
					"maxPatternLength must be between 1 and %s", LARGEST_MAX_PATTERN_LENGTH);
			Preconditions.checkArgument(maxInputLength > 0, "maxInputLength must be positive");
			Preconditions.checkArgument(maxMatchMillis > 0, "maxMatchMillis must be positive");
			Preconditions.checkArgument(maxRepetition > 0, "maxRepetition must be positive");
			this.maxPatternLength = maxPatternLength;
			this.maxInputLength = maxInputLength;
			this.maxMatchNanos = TimeUnit.MILLISECONDS.toNanos(maxMatchMillis);
			this.maxRepetition = maxRepetition;
		}

		/**
		 * Reads the limits from properties named after this class. A
		 * property that is not set gives the default.
		 *
		 * @param properties gives the value of a property by its name
		 * @return the limits
		 * @throws IllegalArgumentException if a value is not a positive number
		 */
		static Limits from(java.util.function.Function<java.lang.String, java.lang.String> properties){
			return new Limits(
					(int)number(properties, ".maxPatternLength", DEFAULT_MAX_PATTERN_LENGTH, LARGEST_MAX_PATTERN_LENGTH),
					(int)number(properties, ".maxInputLength", DEFAULT_MAX_INPUT_LENGTH, java.lang.Integer.MAX_VALUE),
					number(properties, ".maxMatchMillis", DEFAULT_MAX_MATCH_MILLIS, java.lang.Integer.MAX_VALUE),
					number(properties, ".maxRepetition", DEFAULT_MAX_REPETITION, java.lang.Long.MAX_VALUE));
		}

		private static long number(java.util.function.Function<java.lang.String, java.lang.String> properties,
				java.lang.String suffix, long defaultValue, long max){
			java.lang.String name = PROPERTY_PREFIX + suffix;
			java.lang.String value = properties.apply(name);
			if(value == null){
				return defaultValue;
			}
			long number;
			try{
				number = java.lang.Long.parseLong(value.trim());
			}catch(NumberFormatException e){
				throw new IllegalArgumentException(name + " must be a number");
			}
			Preconditions.checkArgument(number > 0 && number <= max,
					"%s must be between 1 and %s", name, max);
			return number;
		}
	}

	/**
	 * Compiled patterns by their Java syntax, least recently used first.
	 * One instance per thread; it holds at most {@code maxSize} patterns.
	 */
	static final class PatternCache
	{
		private final LinkedHashMap<java.lang.String, Pattern> patterns;

		PatternCache(final int maxSize){
			Preconditions.checkArgument(maxSize > 0 && maxSize <= LARGEST_CACHE_SIZE,
					"maxSize must be between 1 and %s", LARGEST_CACHE_SIZE);
			this.patterns = new LinkedHashMap<java.lang.String, Pattern>(16, 0.75f, true){
				private static final long serialVersionUID = 1L;

				@Override
				protected boolean removeEldestEntry(Map.Entry<java.lang.String, Pattern> eldest){
					return size() > maxSize;
				}
			};
		}

		Pattern get(java.lang.String regexp){
			Pattern p = patterns.get(regexp);
			if(p == null){
				p = Pattern.compile(regexp);
				patterns.put(regexp, p);
			}
			return p;
		}

		/** The cached pattern without making it the most recently used one. */
		Pattern peek(java.lang.String regexp){
			for(Map.Entry<java.lang.String, Pattern> e : patterns.entrySet()){
				if(e.getKey().equals(regexp)){
					return e.getValue();
				}
			}
			return null;
		}

		int size(){
			return patterns.size();
		}
	}

	/**
	 * The input of one match. {@code java.util.regex} reads the input one
	 * character at a time, also while it backtracks, so the deadline is
	 * checked here. The clock is read once per {@value #CLOCK_INTERVAL}
	 * characters.
	 */
	private static final class DeadlineCharSequence implements CharSequence
	{
		private static final int CLOCK_INTERVAL = 1024;

		private final CharSequence value;
		private final long deadlineNanos;
		private final java.util.function.LongSupplier clock;
		private int untilClock = CLOCK_INTERVAL;

		DeadlineCharSequence(CharSequence value, long deadlineNanos, java.util.function.LongSupplier clock){
			this.value = value;
			this.deadlineNanos = deadlineNanos;
			this.clock = clock;
		}

		@Override
		public char charAt(int index){
			if(--untilClock <= 0){
				untilClock = CLOCK_INTERVAL;
				if(clock.getAsLong() - deadlineNanos > 0){
					throw new MatchDeadlineExceededException();
				}
			}
			return value.charAt(index);
		}

		@Override
		public int length(){
			return value.length();
		}

		@Override
		public CharSequence subSequence(int start, int end){
			return new DeadlineCharSequence(value.subSequence(start, end), deadlineNanos, clock);
		}

		@Override
		public java.lang.String toString(){
			return value.toString();
		}
	}

	@SuppressWarnings("serial")
	private static final class MatchDeadlineExceededException extends RuntimeException
	{
		MatchDeadlineExceededException(){
			super(null, null, false, false);
		}
	}

	public static boolean matches(java.lang.String regExp, java.lang.String value)
	{
		return matches(regExp, value, LIMITS);
	}

	static boolean matches(java.lang.String regExp, java.lang.String value, Limits limits)
	{
		return matches(regExp, value, limits, System::nanoTime);
	}

	/**
	 * @param clock the time in nanoseconds, as {@link System#nanoTime()}
	 * gives it. It is read at the start, after the compilation of the
	 * pattern, once per {@value DeadlineCharSequence#CLOCK_INTERVAL} reads
	 * of the input, and after the match.
	 */
	static boolean matches(java.lang.String regExp, java.lang.String value, Limits limits,
			java.util.function.LongSupplier clock)
	{
		if(regExp.length() > limits.maxPatternLength ||
				value.length() > limits.maxInputLength){
			throw limitExceeded();
		}
		// The time for rewriting and compiling the pattern counts too. It can
		// not be interrupted, so it is checked when it is over; the pattern
		// length limit bounds it.
		long deadlineNanos = clock.getAsLong() + limits.maxMatchNanos;
		java.lang.String jdk = covertXacmlToJavaSyntax(regExp);
		// The largest weight stands for "does not fit" and "can not be scanned";
		// it is beyond every limit, also beyond a limit of Long.MAX_VALUE.
		long weight = repetitionWeight(jdk);
		if(weight == java.lang.Long.MAX_VALUE || weight > limits.maxRepetition){
			throw limitExceeded();
		}
		Pattern p = CACHE_REGXP?REGEXP_CACHE.get().get(jdk):Pattern.compile(jdk);
		if(clock.getAsLong() - deadlineNanos > 0){
			throw limitExceeded();
		}
		boolean found;
		try{
			// XF 7.6.2 fn:matches succeeds when any substring matches.
			found = p.matcher(new DeadlineCharSequence(value, deadlineNanos, clock)).find();
		}catch(MatchDeadlineExceededException e){
			throw limitExceeded();
		}
		// The deadline is seen when the matcher reads the input. A match that
		// worked without a read was not stopped; its late result is not used.
		if(clock.getAsLong() - deadlineNanos > 0){
			throw limitExceeded();
		}
		return found;
	}

	/**
	 * The largest product of the minimum counts of counted repetitions
	 * that are nested in each other. {@code (?:(?:){1000}){1000}} gives
	 * 1,000,000. A group that can match the empty string is repeated its
	 * minimum count without a read of the input, so the deadline of
	 * {@link DeadlineCharSequence} does not see that work; this number
	 * bounds it. Repetitions with {@code *}, {@code +} and {@code ?} have a
	 * minimum of 0 or 1 and do not count.
	 * <p>
	 * This bounds counted repetitions only. A pattern can also make the
	 * matcher work without a read of the input in other ways (many groups
	 * with more than one alternative that matches the empty string); that
	 * work is not bounded here.
	 *
	 * @param regexp a pattern in {@code java.util.regex} syntax
	 * @return the weight, at least 1; {@link Long#MAX_VALUE} when it is
	 * larger than a {@code long} or when the pattern can not be scanned.
	 * Such a pattern is rejected whatever the limit is
	 */
	static long repetitionWeight(java.lang.String regexp)
	{
		return new RepetitionScanner(withoutQuoting(regexp)).weight();
	}

	/**
	 * Replaces quoted text ({@code \Q..\E}) by escaped characters, in the
	 * way {@code java.util.regex} does it before it parses a pattern (its
	 * private method {@code Pattern.RemoveQEQuoting}). An empty quote
	 * disappears, so that a quantifier after it belongs to the atom before
	 * it.
	 *
	 * @param regexp a pattern in {@code java.util.regex} syntax
	 * @return the pattern without quoted text
	 */
	static java.lang.String withoutQuoting(java.lang.String regexp)
	{
		int length = regexp.length();
		int i = 0;
		while(i < length - 1 && (regexp.charAt(i) != '\\' || regexp.charAt(i + 1) != 'Q')){
			i += (regexp.charAt(i) == '\\')?2:1;
		}
		if(i >= length - 1){
			return regexp;
		}
		StringBuilder out = new StringBuilder(length).append(regexp, 0, i);
		i += 2;
		boolean inQuote = true;
		boolean beginQuote = true;
		while(i < length){
			char c = regexp.charAt(i++);
			char next = (i < length)?regexp.charAt(i):0;
			if(c > 127 || (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')){
				out.append(c);
			}else if(c >= '0' && c <= '9'){
				// A digit at the start of a quote must not join an escape before the quote.
				out.append(beginQuote?"\\x3":"").append(c);
			}else if(c != '\\'){
				out.append(inQuote?"\\":"").append(c);
			}else if(inQuote && next == 'E'){
				i++;
				inQuote = false;
			}else if(inQuote){
				out.append("\\\\");
			}else if(next == 'Q'){
				i++;
				inQuote = true;
				beginQuote = true;
				continue;
			}else{
				out.append(c);
				if(i < length){
					out.append(next);
					i++;
				}
			}
			beginQuote = false;
		}
		return out.toString();
	}

	/**
	 * Reads a pattern without quoted text far enough to find its groups,
	 * character classes and counted quantifiers. It is not a parser of the
	 * whole syntax. Where it can not follow the pattern (comments mode) it
	 * reports the largest weight, so that a quantifier can not be hidden
	 * from it.
	 */
	private static final class RepetitionScanner
	{
		private final java.lang.String regexp;
		private final int length;
		private int at;

		RepetitionScanner(java.lang.String regexp){
			this.regexp = regexp;
			this.length = regexp.length();
		}

		long weight()
		{
			// One entry per open group: the largest weight in the enclosing group so far.
			java.util.ArrayDeque<java.lang.Long> open = new java.util.ArrayDeque<>();
			long inside = 1;
			// The weight of what a counted quantifier at this place repeats: the
			// group that was just closed, or 1. It is 1 at the start of a group
			// and after a quantifier too: java.util.regex repeats an empty atom there.
			long atom = 1;
			while(at < length){
				char c = regexp.charAt(at);
				if(c == '{'){
					inside = Math.max(inside, repeated(atom));
					atom = 1;
				}else if(c == '('){
					if(turnsOnCommentsMode()){
						return java.lang.Long.MAX_VALUE;
					}
					open.push(inside);
					inside = 1;
					atom = 1;
					at++;
				}else if(c == ')'){
					atom = inside;
					inside = Math.max(atom, open.isEmpty()?1:open.pop());
					at++;
				}else{
					skipAtom(c);
					atom = 1;
				}
			}
			while(!open.isEmpty()){
				inside = Math.max(inside, open.pop());
			}
			return inside;
		}

		/**
		 * Moves past the brace that starts here.
		 *
		 * @return the weight of the repetition when the brace starts a
		 * counted quantifier, 1 when it does not
		 */
		private long repeated(long atom)
		{
			long min = countedMinimum();
			if(min < 0){
				at++;
				return 1;
			}
			at = regexp.indexOf('}', at) + 1;
			return saturatedMultiply(atom, Math.max(1, min));
		}

		private void skipAtom(char c)
		{
			if(c == '['){
				skipCharacterClass();
			}else if(c == '\\'){
				skipEscape();
			}else{
				at++;
			}
		}

		/**
		 * Tells whether the group that starts here is a flag group with
		 * {@code x}, as in {@code (?x)} or {@code (?ix:}. In comments mode
		 * a {@code #} hides the rest of a line from the pattern, which
		 * this scanner does not follow.
		 */
		private boolean turnsOnCommentsMode()
		{
			if(at + 1 >= length || regexp.charAt(at + 1) != '?'){
				return false;
			}
			for(int i = at + 2; i < length && Character.isLetter(regexp.charAt(i)); i++){
				if(regexp.charAt(i) == 'x'){
					return true;
				}
			}
			return false;
		}

		/**
		 * Moves past the escape that starts here. The character after
		 * {@code \c}, the braces of {@code \p{..}}, {@code \P{..}},
		 * {@code \x{..}}, {@code \N{..}} and the brackets of
		 * {@code \k<..>} belong to the escape.
		 */
		private void skipEscape()
		{
			char escaped = (at + 1 < length)?regexp.charAt(at + 1):0;
			at = Math.min(length, at + ((escaped == 'c')?3:2));
			char close = 0;
			if(at < length && regexp.charAt(at) == '{' && "pPxN".indexOf(escaped) >= 0){
				close = '}';
			}else if(at < length && regexp.charAt(at) == '<' && escaped == 'k'){
				close = '>';
			}
			if(close != 0){
				int end = regexp.indexOf(close, at);
				at = (end < 0)?length:end + 1;
			}
		}

		/**
		 * Moves past the character class that starts here, with the classes
		 * nested in it. As in {@code java.util.regex}, a {@code ]} that is
		 * the first character of a class (after {@code [} or {@code [^})
		 * is a literal and does not close the class.
		 */
		private void skipCharacterClass()
		{
			int depth = 0;
			boolean first = false;
			do{
				char c = regexp.charAt(at);
				if(c == '\\'){
					skipEscape();
					first = false;
				}else if(c == '['){
					depth++;
					at++;
					if(at < length && regexp.charAt(at) == '^'){
						at++;
					}
					first = true;
				}else{
					if(c == ']' && !first){
						depth--;
					}
					first = false;
					at++;
				}
			}while(depth > 0 && at < length);
		}

		/** The minimum count of the counted quantifier that starts here, or -1 when there is none. */
		private long countedMinimum()
		{
			int i = at + 1;
			long min = 0;
			int digits = 0;
			for(; i < length && isDigit(regexp.charAt(i)); i++, digits++){
				// A count above Integer.MAX_VALUE is not valid; it only has to stay large.
				min = (min > java.lang.Integer.MAX_VALUE)?min:min * 10 + (regexp.charAt(i) - '0');
			}
			if(digits == 0){
				return -1;
			}
			if(i < length && regexp.charAt(i) == ','){
				for(i++; i < length && isDigit(regexp.charAt(i)); i++){
					// the maximum count does not matter
				}
			}
			return (i < length && regexp.charAt(i) == '}')?min:-1;
		}

		private static boolean isDigit(char c)
		{
			return c >= '0' && c <= '9';
		}

		private static long saturatedMultiply(long a, long b)
		{
			return (a > java.lang.Long.MAX_VALUE / b)?java.lang.Long.MAX_VALUE:a * b;
		}
	}

	private static EvaluationException limitExceeded(){
		return new EvaluationException(
				Status.processingError().message(LIMIT_EXCEEDED_MESSAGE).build(),
				LIMIT_EXCEEDED_MESSAGE);
	}

	/*
	 *
	 * Copyright 2003-2006 Sun Microsystems, Inc. All Rights Reserved.
	 *
	 * Redistribution and use in source and binary forms, with or without
	 * modification, are permitted provided that the following conditions are met:
	 *
	 *   1. Redistribution of source code must retain the above copyright notice,
	 *      this list of conditions and the following disclaimer.
	 *
	 *   2. Redistribution in binary form must reproduce the above copyright
	 *      notice, this list of conditions and the following disclaimer in the
	 *      documentation and/or other materials provided with the distribution.
	 *
	 * Neither the name of Sun Microsystems, Inc. or the names of contributors may
	 * be used to endorse or promote products derived from this software without
	 * specific prior written permission.
	 *
	 * This software is provided "AS IS," without a warranty of any kind. ALL
	 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING
	 * ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
	 * OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN MICROSYSTEMS, INC. ("SUN")
	 * AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE
	 * AS A RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS
	 * DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR ANY LOST
	 * REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL,
	 * INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY
	 * OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THIS SOFTWARE,
	 * EVEN IF SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
	 *
	 * You acknowledge that this software is not designed or intended for use in
	 * the design, construction, operation or maintenance of any nuclear facility.
	 */
	public static java.lang.String covertXacmlToJavaSyntax(java.lang.String xpr)
	{
	       // XML Schema / XPath regular expressions differ from
	       // java.util.regex.Pattern. Substring matching is Matcher.find
	       // (XF 7.6.2). Do not wrap the pattern in ".*": that breaks
	       // branch-local ^ and $ and makes an empty pattern miss "\n".
	       // An empty pattern stays empty and matches every input.

	       StringBuffer buf = new StringBuffer(xpr);

	       // in order to handle Unicode blocks, we replace all
	       // instances of "\p{Is" with "\p{In" in the reg exp

	       int idx = -1;
	       idx = buf.indexOf("\\p{Is", 0);
	       while (idx != -1){
	           buf = buf.replace(idx, idx+5, "\\p{In");
	           idx = buf.indexOf("\\p{Is", idx);
	       }

	       // in order to handle Unicode blocks, we replace all instances
	       // of "\P{Is" with "\P{In" in the reg exp

	       idx = -1;
	       idx = buf.indexOf("\\P{Is", 0);
	       while (idx != -1){
	           buf = buf.replace(idx, idx+5, "\\P{In");
	           idx = buf.indexOf("\\P{Is", idx);
	       }

	       // converts to class subtraction
	       // in order to handle character class subtraction, we
	       // replace nested instances of "-[" with "&&[^" in the reg exp
	       idx = -1;
	       idx = buf.indexOf("-[", 0);
	       while (idx != -1){
	    	   if (calculateNestLevel(buf, idx-1) > 0) {
	    		   buf = buf.replace(idx, idx+2, "&&[^");
	    	   }
	    	   idx = buf.indexOf("-[", idx+1);
	       }

	       java.lang.String regexp = translateEndAnchors(buf.toString());
	       if(log.isDebugEnabled()){
	    	   log.debug("XACML regexp=\"{}\", " +
	    	   		"Java regexp=\"{}\"", xpr, regexp);
	       }
	       return regexp;
	  }

	private static java.lang.String translateEndAnchors(java.lang.String regexp)
	{
		StringBuilder translated = new StringBuilder(regexp.length());
		int characterClassDepth = 0;
		boolean escaped = false;
		for (int index = 0; index < regexp.length(); index++) {
			char current = regexp.charAt(index);
			if (escaped) {
				translated.append(current);
				escaped = false;
			} else if (current == '\\') {
				translated.append(current);
				escaped = true;
			} else if (current == '[') {
				characterClassDepth++;
				translated.append(current);
			} else if (current == ']') {
				if (characterClassDepth > 0) {
					characterClassDepth--;
				}
				translated.append(current);
			} else if (current == '$' && characterClassDepth == 0) {
				// XPath '$' is strict end-of-input; Java '$' also accepts before a final terminator.
				translated.append("\\z");
			} else {
				translated.append(current);
			}
		}
		return translated.toString();
	}

	private static int calculateNestLevel(
			StringBuffer buf, int idx) {
		int level = 0;
		while(idx >= 0) {
			char ch = buf.charAt(idx--);
			if (ch == '[') level++;
			if (ch == ']') level --;
		}
		return level;
	}
}
