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

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

/**
 * A file appender that takes {@code delayMillis} per event, so that an async appender in
 * front of it still holds a backlog when the JVM starts to exit (review RVG of change
 * upgrade-dependencies). Test use only.
 */
public class SlowFileAppender extends AppenderBase<ILoggingEvent>
{
	private String file;
	private long delayMillis = 1;
	private PrintWriter out;

	public void setFile(String file){
		this.file = file;
	}

	public void setDelayMillis(long delayMillis){
		this.delayMillis = delayMillis;
	}

	@Override
	public void start(){
		try{
			out = new PrintWriter(new FileWriter(file, StandardCharsets.UTF_8, false), true);
		}catch(IOException e){
			addError("Cannot open " + file, e);
			return;
		}
		super.start();
	}

	@Override
	protected void append(ILoggingEvent event){
		// A busy wait, not Thread.sleep: at shutdown the async appender interrupts its worker,
		// and slow output (a disk or a socket) keeps writing; a sleep would skip every event.
		long until = System.nanoTime() + delayMillis * 1_000_000L;
		while(System.nanoTime() < until){
			Thread.onSpinWait();
		}
		out.println(event.getFormattedMessage());
	}

	@Override
	public void stop(){
		if(out != null){
			out.close();
		}
		super.stop();
	}
}
