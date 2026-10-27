package org.xacml4j.logging;

/*
 * #%L
 * Xacml4J Core Engine Implementation
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;

/**
 * Child-JVM probe of {@link LoggingConfigurationTest}: logs the given number of INFO
 * messages, prints the effective root level, and returns from {@code main} at once, so the
 * messages still queued in the async appender reach the file only through the shutdown
 * hook of the shared configuration.
 */
public final class LoggingProbe
{
	private LoggingProbe(){
	}

	public static void main(String[] args)
	{
		int count = Integer.parseInt(args[0]);
		Logger log = LoggerFactory.getLogger("org.xacml4j.logging.probe");
		for(int i = 0; i < count; i++){
			log.info("probe message {}", i);
		}
		LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
		System.out.println("root=" + context.getLogger(Logger.ROOT_LOGGER_NAME).getLevel());
	}
}
