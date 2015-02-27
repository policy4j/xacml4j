package org.xacml4j.v30.spi.repository;


import com.google.common.io.Closer;
import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableList;
import org.xacml4j.v30.CompositeDecisionRule;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.LinkedList;

public class ImmutablePolicySource extends AbstractPolicySource
{
    private Collection<Supplier<InputStream>> policies = new LinkedList<Supplier<InputStream>>();

    private ImmutablePolicySource(Builder b){
        super(b);
    }

    @Override
    public Iterable<? extends CompositeDecisionRule> getPolicies() throws IOException
    {
        ImmutableList.Builder<CompositeDecisionRule> b = ImmutableList.builder();
        Closer closer = Closer.create();
        try{
            for(Supplier<InputStream> p : policies){
                InputStream in =  p.get();
                closer.register(in);
                b.add(parse(in));
            }
        }catch(IOException e){
            closer.rethrow(e);
        }finally {
            closer.close();
        }
        return b.build();
    }

    public abstract static class Builder
            extends AbstractPolicySource.Builder<Builder>
    {
        private ImmutableList.Builder<Supplier<InputStream>> b = ImmutableList.builder();

        public Builder policy(Supplier<InputStream> policy){
            b.add(policy);
            return this;
        }

        protected Builder getThis(){
            return this;
        }

        public PolicySource build(){
            return new ImmutablePolicySource(this);
        }
    }
}
