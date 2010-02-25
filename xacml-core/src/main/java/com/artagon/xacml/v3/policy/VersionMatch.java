package com.artagon.xacml.v3.policy;

import com.artagon.xacml.util.Preconditions;
import com.artagon.xacml.v3.XacmlObject;

public class VersionMatch extends XacmlObject
{
	private String pattern;
    public static final String PATTERN = "((\\d+|\\*)\\.)*(\\d+|\\*|\\+)";

    public VersionMatch(String pattern) {
        Preconditions.checkArgument(pattern.matches(PATTERN));
        this.pattern = convertVersionMatchToJavaRE(pattern);
    }

    public boolean match(Version version) {
        return version.getVersionValue().matches(pattern);
    }
    
    private String convertVersionMatchToJavaRE(String versionMatch) 
    {
        String plus = "\\.\\+", plusRep = "(.\\\\d+)*";
        String dot = "\\.", dotRep = "\\\\.";
        String ast = "\\*", astRep = "\\\\d";
        // replace all "*" with "\d"
        String phase1 = versionMatch.replaceAll(ast, astRep);
        // replace all ".+" with "(.\d+)*"
        String phase2 = phase1.replaceAll(plus, plusRep);
        // replace all "." with "\\.", include the "." in "(.\d+)*"
        return phase2.replaceAll(dot, dotRep);
    }

    public String getPattern() {
        return pattern;
    }
    
    public static VersionMatch valueOf(String pattern) {
        return new VersionMatch(pattern);
    }
}
