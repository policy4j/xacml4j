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

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

/**
 * A XACML version  is expressed as a sequence of decimal numbers,
 * each separated by a period (.).
 *
 * @author Giedrius Trumpickas
 */
public final class Version implements Comparable<Version>
{
	private static final String VERSION_PATTERN = "(\\d+\\.)*\\d+";

	private final String value;
    private final int[] version;
    private final int hashCode;

    /**
     * Constructs version from
     * a given string
     *
     * @param version a version represented as string
     * @exception XacmlSyntaxException if version can not be parsed
     */
    private Version(String version) {
	    this.value = version;
    	this.version = parseVersion(version);
    	this.hashCode = value.hashCode();
    }

    /**
     * Gets version value
     *
     * @return version value as
     * a string
     */
    public String getValue() {
       return value;
    }

    @Override
    public boolean equals(Object other) {
    	if (this == other) {
    		return true;
    	}
    	if (!(other instanceof Version)) {
    		return false;
    	}
    	return compareTo((Version)other) == 0;
    }

    @Override
    public String toString(){
    	return Objects.toStringHelper(this).
    	add("version", value).toString();
    }

    @Override
    public int hashCode(){
    	return hashCode;
    }

    @Override
    public int compareTo(Version v)
    {
        int min = Math.min(version.length, v.version.length);
        for(int i = 0; i < min; i++){
        	int r = version[i] - v.version[i];
        	if(r != 0){
        		return r > 0?1:-1;
        	}
        }
        if (checkAllZeros(version, min) && checkAllZeros(v.version, min)) {
        	return 0;
        }
        return version.length - v.version.length;
    }

    /**
     * Checks if a given version vector starting at given pos is all {@code 0}
     * @param versions a version vector
     * @param startIdx a start index
     * @return {@code true} if all versions are equal to {@code 0}
     */
    private boolean checkAllZeros(int[] versions, int startIdx)
    {
		for (int i = versions.length-1; i >= startIdx; i--) {
			if (versions[i] != 0) return false;
		}
		return true;
	}

    /**
     * Parses given version string and returns
     * version as an array of non-negative integers
     *
     * @param version a version string
     * @return an array of non-negative integers
     */
	private static int[] parseVersion(
			String version)
    {
    	if(!version.matches(VERSION_PATTERN)){
    		throw new IllegalArgumentException(
    				String.format("Invalid version=\"%s\", " +
    				"does not match regular expression=\"%s\"",
    				version, VERSION_PATTERN));
    	}
    	 String[] vc = version.split("\\.");
    	 int[] v = new int[vc.length];
    	 for(int i = 0; i < vc.length; i++){
    		 v[i] = Integer.parseInt(vc[i]);
    		 if(v[i] < 0){
    			 throw new IllegalArgumentException(
    					 String.format("Invalid version=\"%s\", " +
    					 		"component=\"%s\", number is negative", version,
    					 Integer.toString(v[i])));
    		 }
    	 }
    	 return v;
    }

	/**
	 * A static factory method to
	 * build {@link Version} instances
	 * from a given string
	 *
	 * @param version a version
	 * @return {@link Version} instance
	 */
    public static Version parse(String version){
        return Strings.isNullOrEmpty(version)?
        		new Version("1.0.0"):new Version(version);
    }

    /**
	 * A static factory method to
	 * build {@link Version} instances
	 * from a given integer
	 *
	 * @param version a version
	 * @return {@link Version} instance
	 */
    public static Version valueOf(int version)
    	throws XacmlSyntaxException
    {
    	Preconditions.checkArgument(version > 0,
    			"Version must be positive integer");
    	return parse(Integer.toString(version));
    }
}
