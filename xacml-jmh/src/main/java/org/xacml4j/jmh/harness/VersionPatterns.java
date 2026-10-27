package org.xacml4j.jmh.harness;

/*
 * #%L
 * Xacml4J JMH Benchmarks
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

import org.xacml4j.v30.Version;
import org.xacml4j.v30.VersionMatch;

/**
 * Version matching of XACML 3.0 §5.13 for the lookup check. The engine's
 * {@link VersionMatch#match} reads {@code *} as one digit, so {@code 1.*}
 * misses {@code 1.10} (bead policy4j-w842); the check must not copy that, or
 * it would reject a correct store and pass a store with the same defect
 * (review rv006).
 * <p>
 * Known limit: the check applies {@code EarliestVersion} and
 * {@code LatestVersion} of a reference as patterns, as the engine does; a
 * range reading of §5.11 is not modeled. No built-in corpus uses them.
 */
public final class VersionPatterns
{
	private VersionPatterns(){
	}

	/**
	 * @param pattern a version match: dot-separated numbers, {@code *} for any
	 * one number, and a final {@code +} for any further numbers
	 * @param version a version
	 * @return whether the version matches the pattern
	 */
	public static boolean matches(VersionMatch pattern, Version version){
		return matches(pattern.getPattern(), version.getValue());
	}

	/**
	 * @param pattern a version match pattern
	 * @param version a version string
	 * @return whether the version matches the pattern; a final {@code +}
	 * matches any further numbers, also none, as the engine reads it
	 */
	public static boolean matches(String pattern, String version){
		String[] want = pattern.split("\\.", -1);
		String[] have = version.split("\\.", -1);
		for(int i = 0; i < want.length; i++){
			if(want[i].equals("+") && i == want.length - 1){
				return true;
			}
			if(i >= have.length){
				return false;
			}
			if(!want[i].equals("*") && !want[i].equals(have[i])){
				return false;
			}
		}
		return want.length == have.length;
	}
}
