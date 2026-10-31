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
import org.xacml4j.v30.Expression;
import org.xacml4j.v30.Status;
import org.xacml4j.v30.SyntaxException;
import org.xacml4j.v30.ValueExp;
import org.xacml4j.v30.policy.BoundCall;
import org.xacml4j.v30.policy.function.XacmlEvaluationContextParam;
import org.xacml4j.v30.policy.function.XacmlFuncParam;
import org.xacml4j.v30.policy.function.XacmlFuncReturnType;
import org.xacml4j.v30.policy.function.XacmlFuncSpec;
import org.xacml4j.v30.policy.function.XacmlFunctionProvider;
import org.xacml4j.v30.policy.function.XacmlLegacyFunc;
import org.xacml4j.v30.policy.function.regex.CompiledRegex;
import org.xacml4j.v30.policy.function.regex.RegexBudget;
import org.xacml4j.v30.policy.function.regex.RegexCompileException;
import org.xacml4j.v30.policy.function.regex.RegexEngine;
import org.xacml4j.v30.policy.function.regex.RegexEngines;
import org.xacml4j.v30.policy.function.regex.RegexLimitException;
import org.xacml4j.v30.policy.function.regex.RegexLimits;
import org.xacml4j.v30.policy.function.regex.RegexProfile;
import org.xacml4j.v30.policy.function.regex.jdk.JdkRegexEngine;
import org.xacml4j.v30.types.AnyURI;
import org.xacml4j.v30.types.BooleanVal;
import org.xacml4j.v30.types.DNSName;
import org.xacml4j.v30.types.IPAddress;
import org.xacml4j.v30.types.RFC822Name;
import org.xacml4j.v30.types.StringVal;
import org.xacml4j.v30.types.TypeToString;
import org.xacml4j.v30.types.Value;
import org.xacml4j.v30.types.X500Name;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

import com.google.common.base.Preconditions;
import com.google.common.base.Suppliers;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.ExecutionError;
import com.google.common.util.concurrent.UncheckedExecutionException;

/**
 * The XACML regular-expression match functions (XACML 3.0 A.3.13: {@code fn:matches} of XF 1.0
 * with the arguments reversed). They evaluate through a regular-expression engine of the SPI in
 * {@code org.xacml4j.v30.policy.function.regex} (change add-regex-engine-spi, design D1). The
 * functions a provider registers are a {@link Matching}: the six functions on one engine, profile
 * and limits (design D3); the static methods of this class are the entry points kept for
 * compatibility (design D7), on engine {@code jdk} with the {@code legacy} profile and the limits
 * of the system properties named after this class.
 * <p>
 * The pattern, the first argument of each function, is policy content. The limits bound the
 * length of the pattern, the length of the input, the time of a match that reads its input, and
 * nested counted repetitions. They do not bound every pattern: a pattern can make the matcher
 * work without a read of the input, and that work is not stopped (bug policy4j-the.4.58,
 * accepted as a limit). A policy must therefore not take the pattern from the request, as with
 * an attribute designator or selector as the first argument: the request would then control the
 * processor time of the PDP. Nothing checks that.
 */
@XacmlFunctionProvider(description="XACML regular expression functions")
public final class RegExpFunctions
{
	private final static Logger log = LoggerFactory.getLogger(RegExpFunctions.class);

	/**
	 * The system property that names the engine of the standard functions, read when a provider is
	 * built (design D3); {@code jdk} when it is not set
	 */
	public static final String ENGINE_PROPERTY = "org.xacml4j.regex.engine";

	/**
	 * The system property that names the profile of the standard functions, read when a provider
	 * is built (design D3); {@code legacy} when it is not set
	 */
	public static final String PROFILE_PROPERTY = "org.xacml4j.regex.profile";

	/** The status message when a limit is exceeded. It names neither the pattern nor the input. */
	static final String LIMIT_EXCEEDED_MESSAGE = RegexBudget.LIMIT_EXCEEDED_MESSAGE;

	/**
	 * The status message of a pattern that is invalid in its profile (design D5, XACML 3.0
	 * 7.19.2: syntax-error). It names neither the pattern nor the input.
	 */
	public static final String INVALID_PATTERN_MESSAGE = "Regular expression pattern is invalid";

	/**
	 * The prefix of the system properties of the limits (maintainer ruling 2026-10-07, run 061
	 * D17, design D7): {@code .maxPatternLength}, {@code .maxInputLength},
	 * {@code .maxMatchMillis}, {@code .maxRepetition}, {@code .cacheSize}. The static entry points
	 * use the class-load snapshot; providers read the properties when they are built.
	 */
	private final static String PROPERTY_PREFIX = RegExpFunctions.class.getName();

	private final static RegexLimits LIMITS = limitsFrom(System::getProperty);

	private static final Supplier<Boolean> XF_READY = Suppliers.memoize(() -> {
		// Prepare the common XF tables outside matching deadlines, including for custom engines.
		CompiledRegex probe = new JdkRegexEngine().compile("\\d", RegexProfile.XF, RegexLimits.defaults());
		probe.find("1", RegexBudget.start(1, () -> 0L));
		return true;
	});

	/**
	 * The functions of engine {@code jdk}, profile {@code legacy} and the limits of the properties:
	 * the functions of the static entry points of design D7
	 */
	private final static Matching DEFAULT = new Matching(new JdkRegexEngine(), RegexProfile.LEGACY, LIMITS);

	static{
		// Design D6 (maintainer decision 4 of 2026-10-28): compiled patterns are always reused
		if(System.getProperty(PROPERTY_PREFIX + ".cacheRegExps") != null){
			log.warn("The property {}.cacheRegExps is ignored: compiled patterns are always reused, "
					+ "{} per engine instance (the property {}.cacheSize)", PROPERTY_PREFIX, LIMITS.cacheSize(), PROPERTY_PREFIX);
		}
	}

	/** Private constructor for utility class */
	private RegExpFunctions() {}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:string-regexp-match")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanVal stringRegexpMatch(
			@XacmlEvaluationContextParam EvaluationContext context,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string") StringVal regexp,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string") StringVal input)
	{
		 return DEFAULT.stringRegexpMatch(context, regexp, input);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:2.0:function:anyURI-regexp-match")
	@XacmlLegacyFunc(id="urn:oasis:names:tc:xacml:1.0:function:anyURI-regexp-match")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanVal anyURIRegexpMatch(
			@XacmlEvaluationContextParam EvaluationContext context,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string") StringVal regexp,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#anyURI") AnyURI input)
	{
		 return DEFAULT.anyURIRegexpMatch(context, regexp, input);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:2.0:function:ipAddress-regexp-match")
	@XacmlLegacyFunc(id="urn:oasis:names:tc:xacml:1.0:function:ipAddress-regexp-match")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanVal ipAddressRegexpMatch(
			@XacmlEvaluationContextParam EvaluationContext context,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string") StringVal regexp,
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:2.0:data-type:ipAddress") IPAddress input)
	{
		 return DEFAULT.ipAddressRegexpMatch(context, regexp, input);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:2.0:function:dnsName-regexp-match")
	@XacmlLegacyFunc(id="urn:oasis:names:tc:xacml:1.0:function:dnsName-regexp-match")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanVal dnsNameRegexpMatch(
			@XacmlEvaluationContextParam EvaluationContext context,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string") StringVal regexp,
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:2.0:data-type:dnsName")DNSName input)
	{
		 return DEFAULT.dnsNameRegexpMatch(context, regexp, input);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:2.0:function:rfc822Name-regexp-match")
	@XacmlLegacyFunc(id="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-regexp-match")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanVal rfc822NameRegexpMatch(
			@XacmlEvaluationContextParam EvaluationContext context,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string") StringVal regexp,
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name")RFC822Name input)
	{
		 return DEFAULT.rfc822NameRegexpMatch(context, regexp, input);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:2.0:function:x500Name-regexp-match")
	@XacmlLegacyFunc(id="urn:oasis:names:tc:xacml:1.0:function:x500Name-regexp-match")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanVal x500NameRegexpMatch(
			@XacmlEvaluationContextParam EvaluationContext context,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string") StringVal regexp,
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:x500Name") X500Name input)
	{
		 return DEFAULT.x500NameRegexpMatch(context, regexp, input);
	}

	/**
	 * Tells whether any substring of a value matches an XACML pattern, with engine {@code jdk},
	 * profile {@code legacy} and the limits of the system properties
	 *
	 * @throws EvaluationException with the status of the failure (design D5): syntax-error for a
	 * pattern invalid in the profile, processing-error for a limit
	 */
	public static boolean matches(String regExp, String value)
	{
		return DEFAULT.matches(regExp, value);
	}

	static boolean matches(String regExp, String value, RegexLimits limits)
	{
		return matches(regExp, value, limits, System::nanoTime);
	}

	/**
	 * @param clock the time in nanoseconds, as {@link System#nanoTime()}
	 * gives it. It is read at the start, after the compilation of the
	 * pattern, once per 1,024 reads of the input, and after the match.
	 */
	static boolean matches(String regExp, String value, RegexLimits limits,
			LongSupplier clock)
	{
		return new Matching(DEFAULT.engine, DEFAULT.profile, limits).matches(regExp, value, clock);
	}

	/**
	 * The translation of an XACML pattern to {@code java.util.regex} syntax of the {@code legacy}
	 * profile of the {@code jdk} engine (design D7)
	 */
	public static String covertXacmlToJavaSyntax(String xpr)
	{
		return JdkRegexEngine.toLegacyJavaSyntax(xpr);
	}

	/**
	 * The functions of the engine and profile that the system properties {@link #ENGINE_PROPERTY}
	 * and {@link #PROFILE_PROPERTY} name, read now (design D3). When the properties name engine
	 * {@code jdk} and profile {@code legacy}, or are not set, those are the defaults.
	 *
	 * @return the functions
	 * @throws IllegalArgumentException when no engine or profile has the name a property gives, or
	 * the engine does not offer the profile
	 * @throws IllegalStateException when more than one provider has the engine name
	 */
	public static Matching matching()
	{
		String engine = System.getProperty(ENGINE_PROPERTY, JdkRegexEngine.NAME);
		String profile = System.getProperty(PROFILE_PROPERTY, RegexProfile.LEGACY.id());
		return matching(engine, profile);
	}

	/**
	 * The functions of an engine on the class path and a profile, by their names, with the limits
	 * of the system properties (design D3). The engine is resolved once, here.
	 *
	 * @param engine the name of an engine, such as {@code jdk}
	 * @param profile the name of a profile the engine offers, {@code legacy} or {@code xf}
	 * @return the functions
	 * @throws IllegalArgumentException when no engine or profile has the name, or the engine does
	 * not offer the profile; the message names the engine or the profile
	 * @throws IllegalStateException when more than one provider has the engine name
	 */
	public static Matching matching(String engine, String profile)
	{
		return matching(RegexEngines.engine(engine), profile(profile));
	}

	/** The functions of an engine and a profile, with the limits of the system properties (design D3) */
	public static Matching matching(RegexEngine engine, RegexProfile profile)
	{
		return new Matching(engine, profile, limitsFrom(System::getProperty));
	}

	private static RegexProfile profile(String name)
	{
		return RegexProfile.of(name).orElseThrow(() -> new IllegalArgumentException(String.format(
				"Unknown regular expression profile \"%s\"; the known profiles are %s", name,
				Arrays.stream(RegexProfile.values()).map(RegexProfile::id).toList())));
	}

	/**
	 * Reads the limits from properties named after this class. A property that is not set gives
	 * the default.
	 *
	 * @param properties gives the value of a property by its name
	 * @return the limits
	 * @throws IllegalArgumentException if a value is not a positive number
	 */
	static RegexLimits limitsFrom(Function<String, String> properties){
		return RegexLimits.builder()
				.maxPatternLength((int)number(properties, ".maxPatternLength",
						RegexLimits.DEFAULT_MAX_PATTERN_LENGTH, RegexLimits.LARGEST_MAX_PATTERN_LENGTH))
				.maxInputLength((int)number(properties, ".maxInputLength",
						RegexLimits.DEFAULT_MAX_INPUT_LENGTH, Integer.MAX_VALUE))
				.maxMatchMillis(number(properties, ".maxMatchMillis",
						RegexLimits.DEFAULT_MAX_MATCH_MILLIS, Integer.MAX_VALUE))
				.maxRepetition(number(properties, ".maxRepetition",
						RegexLimits.DEFAULT_MAX_REPETITION, Long.MAX_VALUE))
				.cacheSize(cacheSize(properties))
				.build();
	}

	/**
	 * Reads the size of the pattern cache from the property {@code .cacheSize}, as
	 * {@link #limitsFrom} reads the limits.
	 *
	 * @param properties gives the value of a property by its name
	 * @return the number of compiled patterns one engine instance keeps
	 * @throws IllegalArgumentException if the value is not a positive number
	 */
	static int cacheSize(Function<String, String> properties){
		return (int)number(properties, ".cacheSize", RegexLimits.DEFAULT_CACHE_SIZE, RegexLimits.LARGEST_CACHE_SIZE);
	}

	private static long number(Function<String, String> properties,
			String suffix, long defaultValue, long max){
		String name = PROPERTY_PREFIX + suffix;
		String value = properties.apply(name);
		if(value == null){
			return defaultValue;
		}
		long number;
		try{
			number = Long.parseLong(value.trim());
		}catch(NumberFormatException e){
			throw new IllegalArgumentException(name + " must be a number");
		}
		Preconditions.checkArgument(number > 0 && number <= max,
				"%s must be between 1 and %s", name, max);
		return number;
	}

	/**
	 * The six functions on one engine, profile and limits: the instance-based provider of design
	 * D3, which {@link org.xacml4j.v30.policy.function.FunctionProviderBuilder#withRegexEngine
	 * FunctionProviderBuilder} and {@code PolicyRepository.Builder} select. A pattern is compiled
	 * once per instance (design D6): a literal pattern at its call site, through
	 * {@link #bind}, and every other pattern through a cache of {@link RegexLimits#cacheSize()}
	 * entries, the least recently used evicted first. A compiled pattern and a deterministic
	 * failure are kept; a deadline or resource failure is not, and the next evaluation compiles
	 * again. Two instances over one engine instance keep their patterns apart, so the patterns of
	 * each profile are.
	 * <p>
	 * The XACML statuses of the failures of the engine (design D5): syntax-error with a fixed
	 * message for a pattern invalid in the profile; processing-error naming the construct and the
	 * engine for a pattern the engine cannot express; processing-error with the fixed limit
	 * message for every limit and for an {@link Error} of the engine.
	 */
	@XacmlFunctionProvider(description="XACML regular expression functions")
	public static final class Matching implements LiteralBinder
	{
		private final RegexEngine engine;
		private final RegexProfile profile;
		private final RegexLimits limits;
		private final LoadingCache<String, Compiled> compiled;

		/**
		 * @param engine the engine
		 * @param profile a profile the engine offers
		 * @param limits the limits of the patterns and of their matches, and the size of the cache
		 * @throws IllegalArgumentException when the engine does not offer the profile
		 */
		public Matching(RegexEngine engine, RegexProfile profile, RegexLimits limits){
			this.engine = Preconditions.checkNotNull(engine, "engine");
			this.profile = Preconditions.checkNotNull(profile, "profile");
			this.limits = Preconditions.checkNotNull(limits, "limits");
			Preconditions.checkArgument(engine.offers(profile),
					"engine %s does not offer profile %s", engine.name(), profile);
			if(profile == RegexProfile.XF){
				XF_READY.get();
			}
			this.compiled = CacheBuilder.newBuilder()
					.maximumSize(limits.cacheSize())
					.build(CacheLoader.from(this::load));
		}

		public RegexEngine engine(){
			return engine;
		}

		public RegexProfile profile(){
			return profile;
		}

		public RegexLimits limits(){
			return limits;
		}

		@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:string-regexp-match")
		@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
		public BooleanVal stringRegexpMatch(
				@XacmlEvaluationContextParam EvaluationContext context,
				@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string") StringVal regexp,
				@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string") StringVal input)
		{
			return matches(regexp, input);
		}

		@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:2.0:function:anyURI-regexp-match")
		@XacmlLegacyFunc(id="urn:oasis:names:tc:xacml:1.0:function:anyURI-regexp-match")
		@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
		public BooleanVal anyURIRegexpMatch(
				@XacmlEvaluationContextParam EvaluationContext context,
				@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string") StringVal regexp,
				@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#anyURI") AnyURI input)
		{
			return matches(regexp, input);
		}

		@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:2.0:function:ipAddress-regexp-match")
		@XacmlLegacyFunc(id="urn:oasis:names:tc:xacml:1.0:function:ipAddress-regexp-match")
		@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
		public BooleanVal ipAddressRegexpMatch(
				@XacmlEvaluationContextParam EvaluationContext context,
				@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string") StringVal regexp,
				@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:2.0:data-type:ipAddress") IPAddress input)
		{
			return matches(regexp, input);
		}

		@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:2.0:function:dnsName-regexp-match")
		@XacmlLegacyFunc(id="urn:oasis:names:tc:xacml:1.0:function:dnsName-regexp-match")
		@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
		public BooleanVal dnsNameRegexpMatch(
				@XacmlEvaluationContextParam EvaluationContext context,
				@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string") StringVal regexp,
				@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:2.0:data-type:dnsName") DNSName input)
		{
			return matches(regexp, input);
		}

		@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:2.0:function:rfc822Name-regexp-match")
		@XacmlLegacyFunc(id="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-regexp-match")
		@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
		public BooleanVal rfc822NameRegexpMatch(
				@XacmlEvaluationContextParam EvaluationContext context,
				@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string") StringVal regexp,
				@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name") RFC822Name input)
		{
			return matches(regexp, input);
		}

		@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:2.0:function:x500Name-regexp-match")
		@XacmlLegacyFunc(id="urn:oasis:names:tc:xacml:1.0:function:x500Name-regexp-match")
		@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
		public BooleanVal x500NameRegexpMatch(
				@XacmlEvaluationContextParam EvaluationContext context,
				@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string") StringVal regexp,
				@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:x500Name") X500Name input)
		{
			return matches(regexp, input);
		}

		/**
		 * The literal pattern of a call site, compiled on the first evaluation of the call and kept
		 * there with its deterministic failure (design D6); the input is the second argument. Every
		 * function of this instance binds a string literal; another literal is not bound.
		 */
		@Override
		public Optional<BoundCall> bind(String functionId, Value<?> literal){
			return (literal instanceof StringVal pattern)
					? Optional.of(new BoundPattern(pattern.get()))
					: Optional.empty();
		}

		/**
		 * Tells whether any substring of a value matches an XACML pattern
		 *
		 * @throws EvaluationException with the status of the failure (design D5)
		 */
		public boolean matches(String regExp, String value){
			return matches(regExp, value, System::nanoTime);
		}

		/**
		 * @param clock the time in nanoseconds, as {@link System#nanoTime()} gives it; read at the
		 * start, after the compilation of the pattern, once per 1,024 reads of the input, and after
		 * the match
		 */
		boolean matches(String regExp, String value, LongSupplier clock)
		{
			// The time for compiling the pattern counts too. It can not be interrupted, so it
			// is checked when it is over; the pattern length limit bounds it.
			RegexBudget budget = RegexBudget.start(limits.maxMatchNanos(), clock);
			try{
				if(regExp.length() > limits.maxPatternLength() || value.length() > limits.maxInputLength()){
					throw new RegexLimitException(LIMIT_EXCEEDED_MESSAGE);
				}
				return find(compile(regExp), value, budget);
			}catch(RegexCompileException e){
				throw status(e);
			}catch(RegexLimitException e){
				throw limitExceeded();
			}catch(Error e){
				// An Error of an engine, such as a stack overflow, counts as a limit (design D5)
				throw limitExceeded();
			}
		}

		private BooleanVal matches(StringVal regexp, Value<?> input){
			if(log.isDebugEnabled()){
				log.debug("Matching input=\"{}\" via regexp=\"{}\"", input, regexp);
			}
			return BooleanVal.of(matches(regexp.get(), text(input)));
		}

		/**
		 * The compiled pattern, or the deterministic failure kept for it, from the cache: a pattern
		 * that is in neither is compiled once, also when several threads miss it at once. A failure
		 * that is not deterministic is thrown and nothing is kept.
		 */
		private Compiled compile(String pattern){
			try{
				return compiled.getUnchecked(pattern);
			}catch(UncheckedExecutionException e){
				throw (RuntimeException)e.getCause();
			}catch(ExecutionError e){
				throw (Error)e.getCause();
			}
		}

		private Compiled load(String pattern){
			try{
				return Compiled.of(engine.compile(pattern, profile, limits));
			}catch(RegexCompileException e){
				if(e.isDeterministic()){
					return Compiled.failed(e);
				}
				throw e;
			}
		}

		private static boolean find(Compiled compiled, String value, RegexBudget budget){
			CompiledRegex regex = compiled.regex();
			budget.check();
			return regex.find(value, budget);
		}

		/** A literal pattern at its call site (design D6) */
		private final class BoundPattern implements BoundCall
		{
			private final String pattern;
			// the compiled pattern or its deterministic failure, from the first evaluation on
			private volatile Compiled bound;

			BoundPattern(String pattern){
				this.pattern = pattern;
			}

			@Override
			@SuppressWarnings("unchecked")
			public <T extends ValueExp> T invoke(EvaluationContext context, Expression argument){
				return (T)BooleanVal.of(matches(text((Value<?>)argument)));
			}

			boolean matches(String value){
				RegexBudget budget = RegexBudget.start(limits);
				try{
					if(pattern.length() > limits.maxPatternLength() || value.length() > limits.maxInputLength()){
						throw new RegexLimitException(LIMIT_EXCEEDED_MESSAGE);
					}
					Compiled compiled = bound;
					if(compiled == null){
						compiled = bind();
					}
					return find(compiled, value, budget);
				}catch(RegexCompileException e){
					throw status(e);
				}catch(RegexLimitException e){
					throw limitExceeded();
				}catch(Error e){
					throw limitExceeded();
				}
			}

			private synchronized Compiled bind(){
				if(bound == null){
					bound = compile(pattern);
				}
				return bound;
			}
		}
	}

	/** The outcome of a compilation that is kept: the compiled pattern, or its deterministic failure */
	private record Compiled(CompiledRegex pattern, RegexCompileException failure)
	{
		static Compiled of(CompiledRegex pattern){
			return new Compiled(pattern, null);
		}

		static Compiled failed(RegexCompileException failure){
			return new Compiled(null, failure);
		}

		/** @throws RegexCompileException the kept failure */
		CompiledRegex regex(){
			if(failure != null){
				throw failure;
			}
			return pattern;
		}
	}

	/** The string a function matches: the lexical form of the value */
	private static String text(Value<?> value){
		if(value instanceof StringVal s){
			return s.get();
		}
		Optional<TypeToString> toString = TypeToString.forType(value.getEvaluatesTo().getValueType());
		if(toString.isEmpty()){
			throw SyntaxException.invalidDataTypeId(value.getEvaluatesTo());
		}
		return toString.get().toString(value);
	}

	private static EvaluationException status(RegexCompileException e){
		return switch(e.kind()){
			case MALFORMED -> new EvaluationException(
					Status.syntaxError().message(INVALID_PATTERN_MESSAGE).build(), INVALID_PATTERN_MESSAGE, e);
			case UNSUPPORTED -> new EvaluationException(
					Status.processingError().message(e.getMessage()).build(), e.getMessage(), e);
			case LIMIT -> limitExceeded();
		};
	}

	private static EvaluationException limitExceeded(){
		return new EvaluationException(
				Status.processingError().message(LIMIT_EXCEEDED_MESSAGE).build(),
				LIMIT_EXCEEDED_MESSAGE);
	}
}
