package org.xacml4j.jmh.corpus;

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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.xacml4j.jmh.harness.CorpusRequest;
import org.xacml4j.jmh.harness.Marshalling;
import org.xacml4j.jmh.harness.PolicyCorpus;
import org.xacml4j.jmh.harness.PolicyCorpusProvider;
import org.xacml4j.jmh.harness.PolicySource;
import org.xacml4j.v30.Decision;
import org.xacml4j.v30.SyntaxException;
import org.xacml4j.v30.marshal.MediaType;
import org.xacml4j.v30.marshal.RequestUnmarshaller;

import com.google.auto.service.AutoService;

/** A directory of policies and requests described by {@code corpus.properties}. */
@AutoService(PolicyCorpusProvider.class)
public final class DirectoryCorpus implements PolicyCorpusProvider
{
	public static final String NAME = "dir";

	private static final String EXPECT_PREFIX = "expect.";
	private static final Pattern REQUEST_FILE_NAME = Pattern.compile("[A-Za-z0-9._-]+");

	@Override
	public String name(){
		return NAME;
	}

	@Override
	public PolicyCorpus load(Optional<String> argument) throws IOException, SyntaxException {
		String path = argument.filter(value -> !value.isEmpty())
				.orElseThrow(() -> new IllegalArgumentException("Corpus \"" + NAME + "\" requires a directory path"));
		Path directory = Path.of(path);
		Path manifestFile = directory.resolve("corpus.properties");
		if(!Files.isRegularFile(manifestFile)){
			throw new IllegalArgumentException("Missing corpus manifest " + manifestFile);
		}
		Properties manifest = new Properties();
		try(InputStream in = Files.newInputStream(manifestFile)){
			manifest.load(in);
		}
		// A misspelled key would otherwise be ignored without a word (review rv007).
		for(String key : new TreeSet<>(manifest.stringPropertyNames())){
			if(!key.equals("format") && !key.equals("root") && !key.startsWith(EXPECT_PREFIX)){
				throw new IllegalArgumentException("Unknown manifest key \"" + key + "\" in " + manifestFile
						+ "; known: format, root, expect.<request file name>");
			}
		}
		if(!manifest.containsKey("root")){
			throw new IllegalArgumentException("Manifest " + manifestFile + " has no root key; set root=<file name in policies/>");
		}
		String format = manifest.getProperty("format", "");
		MediaType mediaType = switch(format){
			case "xacml30" -> MediaType.Type.XACML30_XML;
			case "xacml20" -> MediaType.Type.XACML20_XML;
			default -> throw new IllegalArgumentException("Unknown corpus format \"" + format + "\" in " + manifestFile);
		};
		String root = manifest.getProperty("root", "");
		List<Path> policyFiles = files(directory.resolve("policies"));
		if(policyFiles.stream().noneMatch(file -> file.getFileName().toString().equals(root))){
			throw new IllegalArgumentException("Missing root file \"" + root + "\" in " + directory.resolve("policies"));
		}
		List<Path> requestFiles = files(directory.resolve("requests"));
		Set<String> requestNames = new HashSet<>();
		for(Path file : requestFiles){
			String name = file.getFileName().toString();
			checkRequestName(name);
			requestNames.add(name);
		}
		Map<String, Decision> expectations = expectations(manifest, requestNames);
		for(Path file : requestFiles){
			String name = file.getFileName().toString();
			if(!expectations.containsKey(name)){
				throw new IllegalArgumentException("Request file \"" + name + "\" has no expectation in " + manifestFile);
			}
		}

		List<PolicySource> policies = new ArrayList<>(policyFiles.size());
		for(Path file : policyFiles){
			policies.add(new PolicySource(file.getFileName().toString(), mediaType, Files.readAllBytes(file)));
		}
		RequestUnmarshaller unmarshaller = Marshalling.requests(mediaType);
		List<CorpusRequest> requests = new ArrayList<>(requestFiles.size());
		for(Path file : requestFiles){
			String name = file.getFileName().toString();
			requests.add(new CorpusRequest(name, unmarshaller.unmarshal(new ByteArrayInputStream(Files.readAllBytes(file))),
					expectations.get(name)));
		}
		return new PolicyCorpus(NAME + ":" + path, policies, root, requests);
	}

	private static List<Path> files(Path directory) throws IOException {
		if(!Files.isDirectory(directory)){
			throw new IllegalArgumentException("Missing corpus directory " + directory);
		}
		// Hidden files (.DS_Store and the like) are not corpus files (review rv004).
		try(Stream<Path> files = Files.list(directory)){
			return files.filter(Files::isRegularFile)
					.filter(file -> !file.getFileName().toString().startsWith("."))
					.sorted(Comparator.comparing(file -> file.getFileName().toString())).toList();
		}
	}

	private static void checkRequestName(String name){
		if(!REQUEST_FILE_NAME.matcher(name).matches()){
			throw new IllegalArgumentException("Invalid request file name \"" + name + "\"; use only letters, digits, '.', '_', and '-'");
		}
	}

	private static Map<String, Decision> expectations(Properties manifest, Set<String> requestNames){
		Map<String, Decision> expectations = new HashMap<>();
		for(String key : new TreeSet<>(manifest.stringPropertyNames())){
			if(!key.startsWith(EXPECT_PREFIX)){
				continue;
			}
			String name = key.substring(EXPECT_PREFIX.length());
			checkRequestName(name);
			if(!requestNames.contains(name)){
				throw new IllegalArgumentException("Expectation \"" + key + "\" has no request file \"" + name + "\"");
			}
			String value = manifest.getProperty(key, "");
			Decision decision = switch(value){
				case "Permit" -> Decision.PERMIT;
				case "Deny" -> Decision.DENY;
				case "NotApplicable" -> Decision.NOT_APPLICABLE;
				case "Indeterminate" -> Decision.INDETERMINATE;
				default -> throw new IllegalArgumentException("Unknown decision \"" + value + "\" for expectation \"" + key + "\"");
			};
			expectations.put(name, decision);
		}
		return expectations;
	}
}
