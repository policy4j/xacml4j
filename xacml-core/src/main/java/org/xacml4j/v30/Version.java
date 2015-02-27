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

import java.lang.reflect.Array;
import java.util.*;
import java.util.Objects;
import java.util.regex.Pattern;

import com.google.common.base.*;
import com.google.common.collect.Iterators;

/**
 * A XACML version  is expressed as a sequence of decimal numbers,
 * each separated by a period (.).
 *
 * @author Giedrius Trumpickas
 */
public final class Version implements Comparable<Version>
{
	private final String value;
    private final Integer[] version;
    private final int hashCode;

    private Version(String v) {
    	this.version = parseVersion(v);
    	this.hashCode = Objects.hashCode(version);
        this.value = v;
    }

    Version(Integer[] v) {
        this.version = v;
        this.hashCode = Objects.hashCode(v);
        StringBuilder b = new StringBuilder();
        for(int i = 0; i < v.length; i++){
            b.append(v[i]);
            if(i < v.length - 1){
                b.append('.');
            }
        }
        this.value = b.toString();
    }

    public Iterator<Integer> iterator(){
        return Iterators.forArray(version);
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

    /**
     * Gets version component at given index
     *
     * @param index a version component index
     * @return version component at given index
     */
    public Integer getComponent(int index){
        return version[index];
    }

    /**
     * Gets version length
     *
     * @return
     */
    public int getLength(){
        return version.length;
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
    	return MoreObjects.toStringHelper(this).
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
        if (checkAllZeros(version, min) &&
                checkAllZeros(v.version, min)) {
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
    private boolean checkAllZeros(Integer[] versions, int startIdx)
    {
		for (int i = versions.length - 1; i >= startIdx; i--) {
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
	private static Integer[] parseVersion(
			String version)
    {
        List<String> components = Splitter.on('.').splitToList(version);
        Integer[] v = new Integer[components.size()];
        for(int i = 0; i < components.size(); i++){
            String component = components.get(i);
            if(!CharMatcher.DIGIT.matchesAllOf(component)){
                throw new IllegalArgumentException(String.format("Version=\"%s\" component=\"%s\" " +
                        "at index=\"%d\" is not a number", version, component, i));
            }
            v[i] = Integer.parseInt(component);
    		 if(v[i] < 0){
    			 throw new IllegalArgumentException(
    					 String.format(
							     "Invalid version=\"%s\", component=\"%d\" at " +
                                         "index=\"%d\", number is negative", version, v[i], i));
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
        Preconditions.checkArgument(!Strings.isNullOrEmpty(version));
        return new Version(version);
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
