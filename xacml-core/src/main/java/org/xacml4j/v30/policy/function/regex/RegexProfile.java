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

import java.util.Optional;

/**
 * The semantic profiles of the XACML regular-expression functions (change add-regex-engine-spi,
 * design D2). A profile names which grammar a pattern is read with and what it means.
 */
public enum RegexProfile
{
	/**
	 * The translation to {@code java.util.regex} syntax of the engine before the SPI, with the
	 * drifts from XF and the Java-only syntax it accepts; the {@code jdk} engine alone offers it,
	 * and it is the default
	 */
	LEGACY("legacy"),

	/**
	 * Strict XF 1.0 section 7.6.1 (23 January 2007), the edition XACML 3.0 A.3.13 cites: the XML
	 * Schema grammar with {@code ^}, {@code $}, reluctant quantifiers and back-references, no flags
	 * and no non-capturing groups; the categories and blocks are those of the running JDK
	 */
	XF("xf");

	private final String id;

	RegexProfile(String id){
		this.id = id;
	}

	/** The name of the profile, as a configuration gives it */
	public String id(){
		return id;
	}

	/**
	 * Finds a profile by its name
	 *
	 * @param id a profile name, {@code legacy} or {@code xf}
	 * @return the profile, or nothing for another name
	 */
	public static Optional<RegexProfile> of(String id){
		for(RegexProfile profile : values()){
			if(profile.id.equals(id)){
				return Optional.of(profile);
			}
		}
		return Optional.empty();
	}

	@Override
	public String toString(){
		return id;
	}
}
