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

import org.xacml4j.v30.policy.function.regex.RegexEngine;
import org.xacml4j.v30.policy.function.regex.RegexEngineProvider;

import com.google.auto.service.AutoService;

/** Makes the {@code jdk} engine available by name (change add-regex-engine-spi, design D1) */
@AutoService(RegexEngineProvider.class)
public final class JdkRegexEngineProvider implements RegexEngineProvider
{
	@Override
	public String name(){
		return JdkRegexEngine.NAME;
	}

	@Override
	public RegexEngine create(){
		return new JdkRegexEngine();
	}
}
