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

import com.google.common.base.Preconditions;

/**
 * A failure of a regular-expression engine, of one of the kinds of the status matrix (change
 * add-regex-engine-spi, design D5). The message of the failure never holds the pattern or the
 * input.
 */
public class RegexException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	/** The kinds of failure, each with its XACML status (design D5) */
	public enum Kind
	{
		/** The pattern is invalid in the grammar of its profile: status syntax-error */
		MALFORMED,
		/** The pattern is valid in its profile, but the engine cannot express it: status processing-error */
		UNSUPPORTED,
		/** A limit of the engine, of the pattern or of the match: status processing-error with the fixed limit message */
		LIMIT
	}

	private final Kind kind;

	protected RegexException(Kind kind, String message, Throwable cause){
		super(message, cause);
		this.kind = Preconditions.checkNotNull(kind, "kind");
	}

	public Kind kind(){
		return kind;
	}
}
