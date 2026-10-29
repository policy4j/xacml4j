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

import java.util.Set;

/**
 * A regular-expression engine of the XACML {@code *-regexp-match} functions (change
 * add-regex-engine-spi, design D1). An engine compiles an XACML pattern once, in a semantic
 * profile it offers, into a {@link CompiledRegex} that answers whether any substring of an input
 * matches. Engines are found by name through {@link RegexEngines}.
 */
public interface RegexEngine
{
	/** The name an engine is selected by, such as {@code jdk} */
	String name();

	/** The profiles this engine offers */
	Set<RegexProfile> profiles();

	default boolean offers(RegexProfile profile){
		return profiles().contains(profile);
	}

	/**
	 * Compiles a pattern in a profile, within the limits
	 *
	 * @param pattern an XACML pattern
	 * @param profile a profile this engine offers
	 * @param limits the limits of the pattern and of its matches
	 * @return the compiled pattern
	 * @throws RegexCompileException when the pattern is malformed in the profile, uses a
	 * construct this engine cannot express, or exceeds a limit
	 * @throws IllegalArgumentException when this engine does not offer the profile
	 */
	CompiledRegex compile(String pattern, RegexProfile profile, RegexLimits limits);
}
