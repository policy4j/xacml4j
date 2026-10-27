package org.xacml4j.jmh;

/*
 * #%L
 * Xacml4J JMH Benchmarks
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

import java.net.URL;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import ch.qos.logback.classic.AsyncAppender;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.status.Status;
import ch.qos.logback.core.status.StatusUtil;

/**
 * The benchmark jar's logback.xml (spec logging-configuration, "Benchmark appender never
 * blocks"), read into a context of its own: the test JVM itself runs with logback-test.xml.
 */
public class LoggingConfigurationTest
{
	@Test
	public void benchmarkConfigurationNeverBlocksAndLogsWarningsToStandardError() throws Exception
	{
		URL config = getClass().getResource("/logback.xml");
		assertThat(config).isNotNull();
		LoggerContext context = new LoggerContext();
		try{
			JoranConfigurator configurator = new JoranConfigurator();
			configurator.setContext(context);
			configurator.doConfigure(config);

			ch.qos.logback.classic.Logger root = context.getLogger(Logger.ROOT_LOGGER_NAME);
			assertThat(root.getLevel()).isEqualTo(Level.WARN);
			AsyncAppender async = (AsyncAppender) root.getAppender("ASYNC_CONSOLE");
			assertThat(async.isNeverBlock()).isTrue();
			assertThat(async.isIncludeCallerData()).isFalse();
			assertThat(async.getMaxFlushTime()).isEqualTo(0);
			ConsoleAppender<?> console = (ConsoleAppender<?>) async.getAppender("CONSOLE");
			assertThat(console.getTarget()).isEqualTo("System.err");
			assertThat(root.getAppender("ASYNC_FILE")).isNull();
			// A warning makes Logback print its whole status, with the jar's absolute path, in
			// every benchmark fork; the configuration must load without one.
			assertThat(new StatusUtil(context).getHighestLevel(0)).isLessThan(Status.WARN);
		}finally{
			context.stop();
		}
	}
}
