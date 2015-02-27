package org.xacml4j.v30.spi.repository;


import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.base.Verify;
import com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xacml4j.v30.CompositeDecisionRule;
import org.xacml4j.v30.XacmlPolicySyntaxException;
import org.xacml4j.v30.marshal.PolicyUnmarshaller;
import org.xacml4j.v30.marshal.jaxb.DefaultXacmlPolicyUnmarshaller;
import org.xacml4j.v30.pdp.Policy;
import org.xacml4j.v30.pdp.PolicySet;
import org.xacml4j.v30.spi.combine.DecisionCombiningAlgorithmProviderBuilder;
import org.xacml4j.v30.spi.function.FunctionProviderBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.*;

/**
 * A base class for policy source implementations
 *
 * @author Giedrius Trumpickas
 */
public abstract class AbstractPolicySource implements PolicySource
{
    private final static Logger log = LoggerFactory.getLogger(AbstractPolicySource.class);
    
    private String id;
    private PolicyUnmarshaller unmarshaller;
    private List<ChangeListener> listeners;


    protected AbstractPolicySource(Builder<?> b){
        this.id = b.id;
        this.unmarshaller = b.unmarshaller;
        this.listeners = new CopyOnWriteArrayList<ChangeListener>(b.listeners.build());
    }

    @Override
    public final String getId(){
        return id;
    }

    @Override
    public PolicyReferenceResolver createResolver(){
        return DefaultPolicyReferenceResolver
                .builder()
                .source(this)
                .build();
    }

    /**
     * Creates a xacml policy from a given input stream
     * 
     * @param source a policy content source
     * @return {@link org.xacml4j.v30.CompositeDecisionRule}
     * @throws IOException if an error occurs
     * @throws XacmlPolicySyntaxException
     */
    protected CompositeDecisionRule parse(InputStream source)
            throws IOException, XacmlPolicySyntaxException{
        if(source == null){
            throw new IOException(
                    "Policy stream is null");
        }
        return unmarshaller.unmarshal(source);
    }


    @Override
    public final void addListener(ChangeListener l){
        this.listeners.add(l);
    }

    @Override
    public final void removeListener(ChangeListener l){
        listeners.remove(l);
    }


    /**
     * Notifies all listeners that given policies 
     * were added to this source
     *
     * @param executor an executor used to invoke the listeners
     * @param p an array of new policies added to the source
     */
    protected final void notifyPolicyAdded(Executor executor, final Policy ... p){
        for(final ChangeListener l : listeners){
            executor.execute(new Runnable() {
                public void run() {
                    l.policyAdded(p);
                }
            });
        }
    }

    /**
     * Notifies all listeners that given policy sets
     * were added to this source
     *
     * @param executor an executor used to invoke the listeners
     * @param p an array of new policy sets added to the source
     */
    protected final void notifyPolicySetAdded(Executor executor, final PolicySet ... p){
        for(final ChangeListener l : listeners){
            executor.execute(new Runnable() {
                public void run() {
                    l.policySetAdded(p);
                }
            });
        }
    }

    /**
     * Notifies all listeners that given polices
     * were removed from this source
     *
     * @param executor an executor used to invoke the listeners
     * @param p an array of removed policies
     */
    protected final void notifyPolicyRemoved(final Executor executor, final Policy ... p){
        for(final ChangeListener l : listeners){
            executor.execute(new Runnable() {
                public void run() {
                    l.policyRemoved(p);
                }
            });
        }
    }

    /**
     * Notifies all listeners that given policy sets
     * were removed from this source
     *
     * @param executor an executor used to invoke the listeners
     * @param p an array of removed policy sets from the source
     */
    protected final void notifyPolicySetRemoved(Executor executor, final PolicySet ... p){
        for(final ChangeListener l : listeners){
            executor.execute(new Runnable() {
                public void run() {
                    l.policySetRemoved(p);
                }
            });
        }
    }

    /**
     * An utility method to shutdown executor service
     * *
     * @param executor an executor service
     * @param timeout a termination timeout
     * @param units a timeout units
     * @return <code>true</code> if shutdown was successful
     */
    protected static boolean shutdownExecutor(ExecutorService executor, long timeout, TimeUnit units)
    {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(timeout, units)) {
                executor.shutdownNow();
                if (!executor.awaitTermination(timeout, units)) {
                    return false;
                }
                return true;
            }
        } catch (InterruptedException ie) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        return false;
    }
    
    /**
     * A base class for policy source builders
     *
     * @param <T>
     */
    protected abstract static class Builder<T extends Builder<?>>
    {
        private String id;
        private PolicyUnmarshaller unmarshaller;
        private ImmutableList.Builder<ChangeListener> listeners = ImmutableList.builder();

        public Builder(String id){
            Preconditions.checkArgument(!Strings.isNullOrEmpty(id),
                    "Policy source identifier can't be null");
            this.id = id;
            this.unmarshaller = new DefaultXacmlPolicyUnmarshaller(
                    FunctionProviderBuilder
                            .builder()
                            .defaultFunctions()
                            .build(),
                    DecisionCombiningAlgorithmProviderBuilder.builder()
                            .defaultAlgorithms().build());
        }
        
        /**
         * Sets custom policy unmarshaller for
         * unmarshalling policies, by default
         *
         * @param unmarshaller
         * @return reference to this builder
         */
        public T unmarshaller(PolicyUnmarshaller unmarshaller){
            Preconditions.checkNotNull(unmarshaller);
            this.unmarshaller = unmarshaller;
            return getThis();
        }

        /**
         * Source chance listener
         * *
         * @param listener a change listener
         * @return reference to this builder
         */
        public T listener(ChangeListener listener){
            Preconditions.checkNotNull(listener);
            this.listeners.add(listener);
            return getThis();
        }

        /**
         * Gets reference to this
         * *
         * @return reference to this
         */
        protected abstract T getThis();

        /**
         * Builds a {@link org.xacml4j.v30.spi.repository.PolicySource}
         *
         * @return a policy source instance
         */
        public abstract PolicySource build();
    }

}
