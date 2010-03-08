package com.artagon.xacml.util;


public class RegExpressionUtil 
{
   public static String covertXacmlToJavaSyntax(String xpr) 
   {
       // the regular expression syntax required by XACML differs
       // from the syntax supported by java.util.regex.Pattern
       // in several ways; the next several code blocks transform
       // the XACML syntax into a semantically equivalent Pattern syntax

       StringBuffer buf = new StringBuffer(xpr);
       
       // in order to handle the requirement that the string is
       // considered to match the pattern if any substring matches
       // the pattern, we prepend ".*" and append ".*" to the reg exp,
       // but only if there isn't an anchor (^ or $) in place

       if (xpr.charAt(0) != '^')
           buf = buf.insert(0, ".*");

       if (xpr.charAt(xpr.length() - 1) != '$')
           buf = buf.insert(buf.length(), ".*");

       // in order to handle Unicode blocks, we replace all 
       // instances of "\p{Is" with "\p{In" in the reg exp

       int idx = -1;
       idx = buf.indexOf("\\p{Is", 0);
       while (idx != -1){
           buf = buf.replace(idx, idx+5, "\\p{In");
           idx = buf.indexOf("\\p{Is", idx);
       }

       // in order to handle Unicode blocks, we replace all instances 
       // of "\P{Is" with "\P{In" in the reg exp

       idx = -1;
       idx = buf.indexOf("\\P{Is", 0);
       while (idx != -1){
           buf = buf.replace(idx, idx+5, "\\P{In");
           idx = buf.indexOf("\\P{Is", idx);
       }
       
       // in order to handle character class subtraction, we
       // replace all instances of "-[" with "&&[^" in the reg exp

       idx = -1;
       idx = buf.indexOf("-[", 0);
       while (idx != -1){
           buf = buf.replace(idx, idx+2, "&&[^");
           idx = buf.indexOf("-[", idx);
       }
       return buf.toString();
   }
}
