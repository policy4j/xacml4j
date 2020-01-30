package org.xacml4j.util;

import com.google.common.base.MoreObjects;
import com.google.common.base.Supplier;
import org.checkerframework.checker.nullness.qual.Nullable;

public class External
{
    public class ToString
    {
        public ToString(Supplier<MoreObjects.ToStringHelper> s){
            this.helper = s.get();
        }

        public MoreObjects.ToStringHelper omitNullValues() {
            return helper.omitNullValues();
        }

        public MoreObjects.ToStringHelper add(String name, @Nullable Object value) {
            return helper.add(name, value);
        }

        public MoreObjects.ToStringHelper add(String name, boolean value) {
            return helper.add(name, value);
        }

        public MoreObjects.ToStringHelper add(String name, char value) {
            return helper.add(name, value);
        }

        public MoreObjects.ToStringHelper add(String name, double value) {
            return helper.add(name, value);
        }

        public MoreObjects.ToStringHelper add(String name, float value) {
            return helper.add(name, value);
        }

        public MoreObjects.ToStringHelper add(String name, int value) {
            return helper.add(name, value);
        }

        public MoreObjects.ToStringHelper add(String name, long value) {
            return helper.add(name, value);
        }

        public String toString() {
            return helper.toString();
        }

        private MoreObjects.ToStringHelper helper;


    }

}
