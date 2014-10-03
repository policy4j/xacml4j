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
 * A base class which represents a reference
 * to a single decision rule
 */
public class IdReference {
    private final String id;
    private Version version;
    private final int hashCode;

    protected IdReference(Builder<?> b) {
        Preconditions.checkNotNull(b.id,
                "Decision rule identifier can not be null");
        this.id = Preconditions.checkNotNull(b.id,
                "Decision rule identifier can not be null");
        this.version = Preconditions.checkNotNull(b.version,
                "Decision rule version can not be null");
        this.hashCode = Objects.hashCode(id, version);
    }

    /**
     * Gets decision rule identifier
     *
     * @return decision rule identifier
     */
    public final String getId() {
        return id;
    }

    /**
     * Gets decision rule version
     *
     * @return decision rule version
     */
    public final Version getVersion() {
        return version;
    }


    public static PolicyIdRef policyIdRef(CompositeDecisionRule rule){
        return new PolicyIdRef.Builder().id(rule.getId()).version(rule.getVersion()).build();
    }

    public static PolicySetIdRef policySetIdRef(CompositeDecisionRule rule){
        return new PolicySetIdRef.Builder().id(rule.getId()).version(rule.getVersion()).build();
    }

    public static PolicyIdRef.Builder policyIdRef(String id){
        return new PolicyIdRef.Builder().id(id);
    }

    public static PolicySetIdRef.Builder policySetIdRef(String id){
        return new PolicySetIdRef.Builder().id(id);
    }

    @Override
    public final String toString() {
        return Objects.toStringHelper(this)
                .add("id", id)
                .toString();
    }

    @Override
    public final int hashCode() {
        return hashCode;
    }

    /**
     * Base builder for {@link org.xacml4j.v30.IdReference} instances
     *
     * @param <T>
     */
    public static abstract class Builder<T extends Builder<?>> {
        private String id;
        private Version version;

        public T id(String identifier) {
            Preconditions.checkArgument(!Strings.isNullOrEmpty(identifier));
            this.id = identifier;
            return getThis();
        }

        public T version(Version version) {
            this.version = Preconditions.checkNotNull(version);
            return getThis();
        }

        public T version(String version) {
            this.version = Version.parse(version);
            return getThis();
        }

        public abstract T getThis();
    }

    public static class PolicyIdRef extends IdReference{

        private PolicyIdRef(Builder b){
            super(b);
        }

        @Override
        public boolean equals(Object o){
            if(this == o){
                return true;
            }
            if(!(o instanceof PolicyIdRef)){
                return false;
            }
            PolicyIdRef r = (PolicyIdRef)o;
            return getId().equals(r.getId());
        }

        public static class Builder extends IdReference.Builder<Builder>{
            public PolicyIdRef build(){
                return new PolicyIdRef(this);
            }

            public Builder getThis(){
                return this;
            }
        }
    }

    public static class PolicySetIdRef extends IdReference{

        private PolicySetIdRef(Builder b){
            super(b);
        }

        @Override
        public boolean equals(Object o){
            if(this == o){
                return true;
            }
            if(!(o instanceof PolicySetIdRef)){
                return false;
            }
            PolicySetIdRef r = (PolicySetIdRef)o;
            return getId().equals(r.getId());
        }

        public static class Builder extends IdReference.Builder<Builder>{
            public PolicySetIdRef build(){
                return new PolicySetIdRef(this);
            }

            public Builder getThis(){
                return this;
            }
        }
    }


}