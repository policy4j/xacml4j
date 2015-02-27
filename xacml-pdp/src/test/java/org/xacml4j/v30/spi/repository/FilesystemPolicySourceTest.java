package org.xacml4j.v30.spi.repository;


import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xacml4j.v30.CompositeDecisionRule;

import java.io.IOException;
import java.util.concurrent.Executor;

public class FilesystemPolicySourceTest 
{
    private PolicySource source;
    
    @Before
    public void init(){
        
        this.source = FilesystemPolicySource
                .builder("testId")
                .rootPath("src/test/resources")
                .filePatternMatch("*Policy*.xml")
                .build();
    }
    
    @After
    public void after() throws IOException{
        source.close();
    }
    
    @Test
    public void testPolicies() throws Exception{
        
        Iterable<CompositeDecisionRule> it = source.getPolicies();
    }
}
