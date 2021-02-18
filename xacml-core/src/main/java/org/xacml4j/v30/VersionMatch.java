package org.xacml4j.v30;

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

import com.google.common.base.Strings;

import java.util.regex.Pattern;

public class VersionMatch
{
	private static final String PATTERN_STRING = "((\\d+|\\*)\\.)*(\\d+|\\*|\\+)";
	private static final Pattern VERSION_PATTERN = Pattern.compile(PATTERN_STRING);

	private static final Pattern AST_PATTERN = Pattern.compile("\\*");
	private static final String AST_REPLACEMENT = "\\\\d";
	private static final Pattern PLUS_PATTERN = Pattern.compile("\\.\\+");
	private static final String PLUS_REPLACEMENT = "(.\\\\d+)*";
	private static final Pattern DOT_PATTERN = Pattern.compile("\\.");
	private static final String DOT_REPLACEMENT = "\\\\.";

	private final String pattern;
    private final Pattern compiledPattern;

	/**
     * Constructs version match constraint
     * from a given string representation.
     * A version match is '.'-separated,
     * like a version string. A number represents
     * a direct numeric match. A '*'means that any
     * single number is valid. A '+' means that any number,
     * and any subsequent numbers, are valid.
     * In this manner, the following four patterns
     * would all match the version string '1.2.3':
     * '1.2.3', '1.*.3', '1.2.*' and '1.+'.
     *
     * @param versionMatchPattern version match pattern
     */
    public VersionMatch(String versionMatchPattern)
    {
    	if(Strings.isNullOrEmpty(versionMatchPattern)){
    		throw new SyntaxException("Version can't be null or empty");
    	}
    	if(!VERSION_PATTERN.matcher(versionMatchPattern).matches()){
    		throw new SyntaxException(
				    "Given version match=\"%s\" should match regular expression=\"%s\"",
        		    versionMatchPattern, PATTERN_STRING);
    	}
        this.pattern = versionMatchPattern;
        this.compiledPattern = Pattern.compile(convertVersionMatchToJavaRE(versionMatchPattern));
    }

    /**
     * Matches given version to this
     * version match constraint
     *
     * @param version a version
     * @return {@code true} if given
     * version matches this constraint
     */
    public boolean match(Version version) {
    	return compiledPattern.matcher(version.getValue()).matches();
    }

    /**
     * A helper method to convert XACML version
     * match regular expression to java regular
     * expression syntax
     *
     * @param pattern an XACML regular expression
     * @return equivalent java regular expression
     */
    private static String convertVersionMatchToJavaRE(String pattern) {
	    // replace all "*" with "\d"
		final String phase1 = AST_PATTERN.matcher(pattern).replaceAll(AST_REPLACEMENT);
	    // replace all ".+" with "(.\d+)*"
		String phase2 = PLUS_PATTERN.matcher(phase1).replaceAll(PLUS_REPLACEMENT);
	    // replace all "." with "\\.", include the "." in "(.\d+)*"
	    return DOT_PATTERN.matcher(phase2).replaceAll(DOT_REPLACEMENT);
    }

    /**
     * Gets version match constraint
     *
     * @return a version match constraint
     */
    public String getPattern() {
        return pattern;
    }

    /**
     * @see VersionMatch#VersionMatch(String)
     *
     * @param pattern a version match constraint
     * @return {@link VersionMatch} defaultProvider
     * @exception IllegalArgumentException if a given version
     * match constraint can not be parsed
     */
    public static VersionMatch parse(String pattern) {
        return new VersionMatch(pattern);
    }

    @Override
    public boolean equals(Object o){
    	if(o == this){
    		return true;
    	}
    	if(!(o instanceof VersionMatch)){
    		return false;
    	}
    	VersionMatch vm = (VersionMatch)o;
    	return pattern.equals(vm.pattern);
    }

    @Override
    public String toString() {
    	return pattern;
    }

    @Override
    public int hashCode(){
    	return pattern.hashCode();
    }
}
