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

import java.util.regex.Pattern;

import com.google.common.base.Strings;

public class VersionMatch
{
	private static final String PATTERN = "((\\d+|\\*)\\.)*(\\d+|\\*|\\+)";

	private String pattern;
    private Pattern compiledPattern;

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
    		throw new XacmlSyntaxException("Version can't be null or empty");
    	}
    	if(!versionMatchPattern.matches(PATTERN)){
    		throw new XacmlSyntaxException(
    				String.format(
    						"Given version match=\"%s\" should " +
    						"match regular expression=\"%s\"",
        		versionMatchPattern, PATTERN));
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
    private String convertVersionMatchToJavaRE(String pattern)
    {
        String plus = "\\.\\+", plusRep = "(.\\\\d+)*";
        String dot = "\\.", dotRep = "\\\\.";
        String ast = "\\*", astRep = "\\\\d";
        // replace all "*" with "\d"
        String phase1 = pattern.replaceAll(ast, astRep);
        // replace all ".+" with "(.\d+)*"
        String phase2 = phase1.replaceAll(plus, plusRep);
        // replace all "." with "\\.", include the "." in "(.\d+)*"
        return phase2.replaceAll(dot, dotRep);
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
     * @return {@link VersionMatch} instance
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
    	if(o == null){
    		return false;
    	}
    	if(!(o instanceof VersionMatch)){
    		return false;
    	}
    	VersionMatch vm = (VersionMatch)o;
    	return pattern.equals(vm.pattern);
    }

    @Override
    public String toString(){
    	return pattern;
    }

    @Override
    public int hashCode(){
    	return pattern.hashCode();
    }
}
