package com.artagon.xacml.v3;

import java.util.regex.Pattern;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

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
     * '1.2.3', '1.*.3', '1.2.*' and Ô1.+'.
     * 
     * @param versionMatchPattern
     */
    public VersionMatch(String versionMatchPattern)
    {
        Preconditions.checkArgument(versionMatchPattern.matches(PATTERN), 
        		"Given version match=\"%s\" should match regular expression=\"%s\"", 
        		versionMatchPattern, PATTERN);
        this.pattern = versionMatchPattern;
        this.compiledPattern = Pattern.compile(convertVersionMatchToJavaRE(versionMatchPattern));
    }

    /**
     * Matches given version to this
     * version match constraint
     * 
     * @param version a version
     * @return <code>true</code> if given
     * version matches this constraint
     * TODO: implement more efficient matching
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
     * @see {@link #VersionMatch(String)}
     * 
     * @param pattern a version match constraint
     * @return {@link VersionMatch} instance
     * @exception IllegalArgumentException if a given version
     * match constraint can not be parsed
     */
    public static VersionMatch parse(String pattern) {
    	Preconditions.checkNotNull(pattern);
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
    	return Objects.toStringHelper(this)
    	.add("pattern", pattern).toString();
    }
    
    @Override
    public int hashCode(){
    	return pattern.hashCode();
    }
}
