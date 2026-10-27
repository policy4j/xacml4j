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

import static com.google.common.truth.Truth.assertThat;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.AsyncAppender;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.FileAppender;

/**
 * The shared Logback test configuration (change upgrade-dependencies, spec
 * logging-configuration): includes, defaults, property overrides, and the flush of the
 * async appenders at JVM exit.
 */
public class LoggingConfigurationTest
{
	private static final int MESSAGES = 10_000;
	/** At 1 ms per event downstream: a backlog of about two seconds. */
	private static final int SLOW_MESSAGES = 2_000;

	@Test
	public void sharedConfigurationIsActiveWithTheModuleFileName()
	{
		LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
		ch.qos.logback.classic.Logger root = context.getLogger(Logger.ROOT_LOGGER_NAME);

		assertThat(root.getLevel()).isEqualTo(Level.toLevel(System.getProperty("xacml4j.log.level", "INFO")));
		assertThat(context.getLogger("org.xacml4j").getLevel())
				.isEqualTo(Level.toLevel(System.getProperty("xacml4j.log.xacml4j.level", "DEBUG")));
		AsyncAppender async = (AsyncAppender) root.getAppender("ASYNC_FILE");
		assertThat(async.isIncludeCallerData()).isFalse();
		assertThat(async.getDiscardingThreshold()).isEqualTo(0);
		assertThat(async.isNeverBlock()).isFalse();
		assertThat(async.getMaxFlushTime()).isEqualTo(0);
		FileAppender<?> file = (FileAppender<?>) async.getAppender("FILE");
		assertThat(file.getFile()).endsWith("xacml-core-test.log");
	}

	@Test
	public void propertyOverridesTheSharedLevelAndQueuedEventsReachTheFileAtExit(@TempDir Path dir) throws Exception
	{
		String output = runProbe(dir, List.of("-Dxacml4j.log.level=TRACE"), MESSAGES);

		assertThat(output).contains("root=TRACE");
		List<String> lines = Files.readAllLines(dir.resolve("xacml-core-test.log"), UTF_8);
		assertThat(count(lines, "probe message ")).isEqualTo(MESSAGES);
		assertThat(lines.get(lines.size() - 1)).endsWith("probe message " + (MESSAGES - 1));
	}

	/**
	 * A backlog of about two seconds when the probe returns: with the shared flush time (0, no
	 * limit) every queued event is written (review RVG).
	 */
	@Test
	public void sharedFlushTimeWritesABacklogLongerThanOneSecond(@TempDir Path dir) throws Exception
	{
		runProbe(dir, List.of("-Dlogback.configurationFile=logback-slow-probe.xml"), SLOW_MESSAGES);

		assertThat(count(Files.readAllLines(dir.resolve("slow.log"), UTF_8), "probe message ")).isEqualTo(SLOW_MESSAGES);
	}

	/** The control of the test above: Logback's default limit of one second loses part of the backlog. */
	@Test
	public void oneSecondFlushTimeLosesPartOfTheSameBacklog(@TempDir Path dir) throws Exception
	{
		runProbe(dir, List.of("-Dlogback.configurationFile=logback-slow-probe.xml", "-Dxacml4j.log.maxFlushTime=1000"),
				SLOW_MESSAGES);

		assertThat(count(Files.readAllLines(dir.resolve("slow.log"), UTF_8), "probe message ")).isLessThan(SLOW_MESSAGES);
	}

	/**
	 * Runs {@link LoggingProbe} in a child JVM with its output in a file, waits at most 60 s,
	 * and kills it on timeout, so that a stuck child cannot hang the test (review RVG).
	 */
	private static String runProbe(Path dir, List<String> jvmArgs, int messages) throws Exception
	{
		List<String> command = new ArrayList<>();
		command.add(Path.of(System.getProperty("java.home"), "bin", "java").toString());
		command.add("-cp");
		command.add(System.getProperty("java.class.path"));
		command.add("-Dxacml4j.log.dir=" + dir);
		command.addAll(jvmArgs);
		command.add(LoggingProbe.class.getName());
		command.add(Integer.toString(messages));
		Path output = dir.resolve("probe.out");
		Process probe = new ProcessBuilder(command).redirectErrorStream(true).redirectOutput(output.toFile()).start();
		try{
			assertThat(probe.waitFor(60, TimeUnit.SECONDS)).isTrue();
		}finally{
			if(probe.isAlive()){
				probe.destroyForcibly();
			}
		}
		assertThat(probe.exitValue()).isEqualTo(0);
		return Files.readString(output, UTF_8);
	}

	private static long count(List<String> lines, String text)
	{
		long n = 0;
		for(String line : lines){
			if(line.contains(text)){
				n++;
			}
		}
		return n;
	}
}
