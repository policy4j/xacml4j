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

/**
 * The compilation of a pattern failed (change add-regex-engine-spi, design D1): the pattern is
 * malformed in its profile, the engine cannot express it, or a structural limit stops it. The
 * detail of a failure names neither the pattern nor the input.
 */
public final class RegexCompileException extends RegexException
{
	private static final long serialVersionUID = 1L;
	private final boolean deterministic;

	private RegexCompileException(Kind kind, String message, Throwable cause){
		this(kind, message, cause, true);
	}

	private RegexCompileException(Kind kind, String message, Throwable cause, boolean deterministic){
		super(kind, message, cause);
		this.deterministic = deterministic;
	}

	/** A pattern that is invalid in the grammar of its profile */
	public static RegexCompileException malformed(String detail){
		return new RegexCompileException(Kind.MALFORMED, detail, null);
	}

	/** A pattern that the engine rejected as invalid */
	public static RegexCompileException malformed(String detail, Throwable cause){
		return new RegexCompileException(Kind.MALFORMED, detail, cause);
	}

	/**
	 * A pattern that is valid in its profile but uses a construct the engine cannot express; the
	 * message names the construct and the engine
	 */
	public static RegexCompileException unsupported(String construct, String engine){
		return new RegexCompileException(Kind.UNSUPPORTED,
				String.format("Regular expression construct %s is not supported by engine %s", construct, engine), null);
	}

	/** A pattern that a limit of its profile or of the engine stops before or during compilation */
	public static RegexCompileException limit(String detail){
		return new RegexCompileException(Kind.LIMIT, detail, null);
	}

	/** An {@link Error} the engine threw while compiling, such as a stack overflow */
	public static RegexCompileException limit(Error cause){
		return resourceLimit(cause);
	}

	/** A transient failure of the engine while compiling. */
	public static RegexCompileException resourceLimit(Throwable cause){
		return new RegexCompileException(Kind.LIMIT, RegexBudget.LIMIT_EXCEEDED_MESSAGE, cause, false);
	}

	/**
	 * Whether the same pattern fails the same way on every compilation (design D6): a malformed
	 * pattern, an unsupported construct and a structural limit do, so the failure is kept with
	 * the pattern; a resource failure of the engine does not
	 */
	public boolean isDeterministic(){
		return deterministic;
	}
}
