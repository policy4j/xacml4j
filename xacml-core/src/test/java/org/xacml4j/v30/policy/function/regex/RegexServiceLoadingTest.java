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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program. If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.xacml4j.v30.policy.function.regex.jdk.JdkRegexEngine;

public class RegexServiceLoadingTest
{
	@TempDir Path temporary;

	@Test
	void coldCategoryAndWordEscapesCanBeCombined() throws Exception {
		try(URLClassLoader loader = isolatedLoader()){
			String pattern = "[\\p{Lu}\\p{Ll}\\p{Lt}\\p{Lm}\\p{Lo}\\p{Mn}\\p{Mc}"
					+ "\\p{Me}\\p{Nd}\\p{Nl}\\p{No}\\w]";
			Class<?> parser = loader.loadClass(XacmlPattern.class.getName());
			Object parsed = parser.getMethod("parse", String.class).invoke(null, pattern);
			Object sequence = parser.getMethod("root").invoke(parsed);
			List<?> pieces = (List<?>)sequence.getClass().getMethod("pieces").invoke(sequence);
			Object set = pieces.get(0).getClass().getMethod("codePoints").invoke(pieces.get(0));
			assertTrue((Boolean)set.getClass().getMethod("contains", int.class).invoke(set, (int)'A'));
		}
	}

	@Test
	void anInvalidDefaultDoesNotPreventMarshallingDiscovery() throws Exception {
		String property = "org.xacml4j.regex.profile";
		String before = System.getProperty(property);
		try(URLClassLoader loader = isolatedLoader()){
			System.setProperty(property, "invalid");
			Class<?> marshalling = Class.forName("org.xacml4j.v30.marshal.MarshallingProvider", true, loader);
			assertInstanceOf(Map.class, marshalling.getMethod("discoverProviders").invoke(null));
		}finally{
			restore(property, before);
		}
	}

	@Test
	void duplicateJdkProvidersFailDefaultPropertyAndExplicitSelection() throws Exception {
		Path services = temporary.resolve("META-INF/services");
		Files.createDirectories(services);
		Files.writeString(services.resolve(RegexEngineProvider.class.getName()), DuplicateJdk.class.getName() + "\n");
		String engine = System.getProperty("org.xacml4j.regex.engine");
		String profile = System.getProperty("org.xacml4j.regex.profile");
		try(URLClassLoader loader = isolatedLoader()){
			System.clearProperty("org.xacml4j.regex.engine");
			System.clearProperty("org.xacml4j.regex.profile");
			assertDuplicate(loader, false);
			System.setProperty("org.xacml4j.regex.engine", "jdk");
			System.setProperty("org.xacml4j.regex.profile", "legacy");
			assertDuplicate(loader, false);
			assertDuplicate(loader, true);
		}finally{
			restore("org.xacml4j.regex.engine", engine);
			restore("org.xacml4j.regex.profile", profile);
		}
	}

	private URLClassLoader isolatedLoader() throws Exception {
		List<URL> urls = new ArrayList<>();
		urls.add(temporary.toUri().toURL());
		String classPath = System.getProperty("surefire.test.class.path", System.getProperty("java.class.path"));
		for(String path : classPath.split(java.util.regex.Pattern.quote(File.pathSeparator))){
			urls.add(Path.of(path).toUri().toURL());
		}
		return new URLClassLoader(urls.toArray(URL[]::new), ClassLoader.getPlatformClassLoader());
	}

	private static void assertDuplicate(ClassLoader loader, boolean explicit) throws Exception {
		Class<?> provider = loader.loadClass("org.xacml4j.v30.policy.function.FunctionProvider");
		Object builder = provider.getMethod("builder").invoke(null);
		Class<?> type = builder.getClass();
		type.getMethod("withDefaultFunctions").invoke(builder);
		if(explicit) type.getMethod("withRegexEngine", String.class, String.class).invoke(builder, "jdk", "legacy");
		InvocationTargetException failure = assertThrows(InvocationTargetException.class,
				() -> type.getMethod("build").invoke(builder));
		IllegalStateException cause = assertInstanceOf(IllegalStateException.class, failure.getCause());
		assertTrue(cause.getMessage().contains("jdk"));
	}

	private static void restore(String name, String value){
		if(value == null) System.clearProperty(name);
		else System.setProperty(name, value);
	}

	public static final class DuplicateJdk implements RegexEngineProvider
	{
		@Override public String name(){ return "jdk"; }
		@Override public RegexEngine create(){ return new JdkRegexEngine(); }
	}
}
