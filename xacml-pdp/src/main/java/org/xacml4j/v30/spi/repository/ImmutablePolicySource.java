package org.xacml4j.v30.spi.repository;


import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Closer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xacml4j.v30.CompositeDecisionRule;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicReference;

/**
 * An immutable version of {@link org.xacml4j.v30.spi.repository.PolicySource},
 * once policies are loaded to this source from the external sources they stay
 * cached in the internal source cache
 *
 * @author Giedrius Trumpickas
 */
public class ImmutablePolicySource extends AbstractPolicySource
{
    private final static Logger log = LoggerFactory.getLogger(ImmutablePolicySource.class);

    /**
     * A collection of policy input streams
     */
    private Collection<Supplier<InputStream>> policies;
    
    private boolean cacheParsedRules = false;
    
    /**
     * Cached policies
     */
    private Collection<CompositeDecisionRule> cachedRules = null;

    private ImmutablePolicySource(Builder b){
        super(b);
        this.policies = b.suppliers.build();
        this.cacheParsedRules = b.cacheParsedRules;
    }

    @Override
    public Iterable<CompositeDecisionRule> getPolicies() throws IOException
    {
        if(cachedRules != null){
            if(log.isDebugEnabled()){
                log.debug("Returning policies " +
                        "from memory cache");
            }
            return cachedRules;
        }
        synchronized (this) {
            if(cachedRules != null){
                return cachedRules;
            }
            Closer closer = Closer.create();
            ImmutableList.Builder<CompositeDecisionRule> policyCacheBuilder = ImmutableList.builder();
            try {
                
                for (Supplier<InputStream> p : policies) {
                    InputStream in = p.get();
                    closer.register(in);
                    CompositeDecisionRule rule = parse(in);
                    if (log.isDebugEnabled()) {
                        log.debug("Importing decision rule id=\"{}\" version=\"{}\"",
                                rule.getId(), rule.getVersion());
                    }
                    policyCacheBuilder.add(rule);
                }
                if(cacheParsedRules){
                    this.cachedRules = policyCacheBuilder.build();
                    return cachedRules;
                }
            } catch (IOException e) {
                closer.rethrow(e);
            } finally {
                closer.close();
            }
            return policyCacheBuilder.build();
        }
    }

    @Override
    public void close() throws IOException {
    }

    /**
     * Creates {@link org.xacml4j.v30.spi.repository.ImmutablePolicySource.Builder}
     * instance with a given policy source identifier
     *
     * @param id a policy source identifier
     * @return {@link org.xacml4j.v30.spi.repository.ImmutablePolicySource.Builder}
     */
    public static Builder builder(String id){
        return new Builder(id);
    }

    /**
     * An immutable policy source builder
     */
    public static class Builder
            extends AbstractPolicySource.Builder<Builder>
    {
        private ImmutableList.Builder<Supplier<InputStream>> suppliers = ImmutableList.builder();

        private boolean cacheParsedRules = false;
        
        protected Builder(String id){
            super(id);
        }

        /**
         * Adds policy to this source
         * 
         * @param policy a policy input stream
         * @return reference to this builder
         */
        public Builder policy(Supplier<InputStream> ...policies){
            suppliers.add(policies);
            return this;
        }

        /**
         * Adds policy to this source
         *
         * @param policy a policy input stream
         * @return reference to this builder
         */
        public Builder policies(Iterable<Supplier<InputStream>> policies){
            suppliers.addAll(policies);
            return this;
        }
        
        public Builder cacheParsedRules(boolean cache){
            this.cacheParsedRules = cache;
            return this;
        }
        
        /**
         * Adds policy from classpath to this source
         * 
         * @param cl a class loader for policy loading
         * @param resourcePath a classpath resource path
         * @return reference to this builder*
         */
        public Builder policyFromClasspath(final ClassLoader cl, final String resourcePath){
            if(log.isDebugEnabled()){
                log.debug("Adding policy resource=\"{}\" " +
                        "classLoader=\"{}\"", resourcePath, cl);
            }
            policy(new Supplier<InputStream>() {
                @Override
                public InputStream get() {
                    return cl.getResourceAsStream(resourcePath);
                }
            });
            return this;
        }

        public Builder policyFromClasspath(final String resourcePath){
            return policyFromClasspath(Thread.currentThread().getContextClassLoader(), resourcePath);
        }

        protected Builder getThis(){
            return this;
        }

        public PolicySource build(){
            return new ImmutablePolicySource(this);
        }
    }
}
