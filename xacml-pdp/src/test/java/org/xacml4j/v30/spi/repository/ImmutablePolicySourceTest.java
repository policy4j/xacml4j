package org.xacml4j.v30.spi.repository;


import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import org.xacml4j.v30.XacmlSyntaxException;
import org.xacml4j.v30.pdp.Policy;
import org.xacml4j.v30.pdp.PolicyReference;

import java.io.IOError;
import java.io.IOException;

public class ImmutablePolicySourceTest 
{
    private ImmutablePolicySource.Builder sourceBuilder;
    
    @Before
    public void init(){
        this.sourceBuilder = ImmutablePolicySource.builder("testId");
    }
    
    @Test
    public void testParseValidPolicy(){
        sourceBuilder.policyFromClasspath(getClass().getClassLoader(), "Policy1.xml");
        PolicySource source = sourceBuilder.build();
        PolicyReferenceResolver resolver = source.createResolver();
        Policy p = resolver.resolve(PolicyReference.builder("urn:oasis:names:tc:xacml:3.0:example:policyid:1").build());
        assertNotNull(p);
    }

    @Test(expected = XacmlSyntaxException.class)
    public void testParseInvalidPolicy(){
        sourceBuilder.policyFromClasspath(getClass().getClassLoader(), "media-rss.xml");
        PolicySource source = sourceBuilder.build();
        PolicyReferenceResolver resolver = source.createResolver();
        Policy p = resolver.resolve(PolicyReference.builder("urn:oasis:names:tc:xacml:3.0:example:policyid:1").build());
        assertNull(p);
    }
}
