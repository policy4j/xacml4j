package com.artagon.xacml.v3.policy;


import com.artagon.xacml.util.Preconditions;
import com.artagon.xacml.v3.XacmlObject;


public class Version extends XacmlObject implements Comparable<Version> 
{
	private  static final String VERSION_PATTERN = "(\\d+\\.)*\\d+";
	
	private String value;
    private int[] version;

    private Version(String v) 
    {
    	Preconditions.checkNotNull(v);
    	this.value = v;
    	this.version = parseVersion(v);
    }
    
    public String getVersionValue() {
       return value;
    }

    public int compareTo(Version v) 
    {
        int min = Math.min(version.length, v.version.length);
        for(int i = 0; i < min; i++){
        	int r = version[i] - v.version[i];
        	if(r != 0){
        		return r > 0?1:-1;
        	}
        }
        return version.length - v.version.length;
    }
    
    private static int[] parseVersion(String version)
    {
    	Preconditions.checkArgument(version.matches(VERSION_PATTERN));
    	 String[] vc = version.split("\\.");
    	 int[] v = new int[vc.length];
    	 for(int i = 0; i < vc.length; i++){
    		 v[i] = Integer.parseInt(vc[i]);
    		 if(v[i] < 0){
    			 throw new IllegalArgumentException(Integer.toString(v[i]));
    		 }
    	 }
    	 return v;
    }

    public static Version valueOf(String version) {
        return new Version(version);
    }
    
    public static Version valueOf(int version)
    {
    	Preconditions.checkArgument(version > 0);
    	return valueOf(Integer.toString(version));
    }
}
