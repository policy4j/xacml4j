package org.xacml4j.logging;

/*
 * #%L
 * Xacml4J Conformance Tests
 * %%
 * Copyright (C) 2009 - 2026 Xacml4J.org
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

import static com.google.common.truth.Truth.assertThat;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.AsyncAppender;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.FileAppender;

/**
 * A module override on top of the shared configuration (spec logging-configuration,
 * "Module override"): this module's logback-test.xml, outside the vendored fixture tree,
 * lowers the project's loggers to INFO; the rest stays at the shared default.
 */
public class LoggingConfigurationTest
{
	@Test
	public void moduleOverrideLowersTheProjectLoggersAndKeepsTheSharedRest()
	{
		LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
		ch.qos.logback.classic.Logger root = context.getLogger(Logger.ROOT_LOGGER_NAME);

		assertThat(context.getLogger("org.xacml4j").getLevel())
				.isEqualTo(Level.toLevel(System.getProperty("xacml4j.log.xacml4j.level", "INFO")));
		assertThat(root.getLevel()).isEqualTo(Level.toLevel(System.getProperty("xacml4j.log.level", "INFO")));
		FileAppender<?> file = (FileAppender<?>) ((AsyncAppender) root.getAppender("ASYNC_FILE")).getAppender("FILE");
		assertThat(file.getFile()).endsWith("xacml-conformance-tests-test.log");
	}
}
