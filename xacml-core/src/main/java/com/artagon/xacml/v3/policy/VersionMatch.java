package com.artagon.xacml.v3.policy;

import com.artagon.xacml.v3.XacmlObject;
import com.google.common.base.Preconditions;

public class VersionMatch extends XacmlObject
{
	private String javaRE;
	private String pattern;
    public static final String PATTERN = "((\\d+|\\*)\\.)*(\\d+|\\*|\\+)";

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
     * '1.2.3', '1.*.3', '1.2.*' and Ô1.+'.
     * 
     * @param pattern
     */
    public VersionMatch(String pattern)
    {
        Preconditions.checkArgument(pattern.matches(PATTERN));
        this.pattern = pattern;
        this.javaRE = convertVersionMatchToJavaRE(pattern);
    }

    /**
     * Matches given version to this
     * version match constraint
     * 
     * @param version a version
     * @return <code>true</code> if given
     * version matches this constraint
     */
    public boolean match(Version version) {
        return version.getValue().matches(javaRE);
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
     * @see {@link #VersionMatch(String)}
     * 
     * @param pattern a version match constraint
     * @return {@link VersionMatch} instance
     */
    public static VersionMatch valueOf(String pattern) {
        return new VersionMatch(pattern);
    }
}
