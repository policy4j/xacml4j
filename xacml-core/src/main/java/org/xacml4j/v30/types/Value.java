package org.xacml4j.v30.types;

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

import java.io.Serializable;
import java.util.Objects;

import com.google.common.base.MoreObjects;
import com.google.common.base.Supplier;
import org.xacml4j.v30.BagOfValues;
import org.xacml4j.v30.ExpressionVisitor;
import org.xacml4j.v30.ValueExp;
import org.xacml4j.v30.ValueExpTypeInfo;


/**
 * Represents XACML attribute value
 *
 * @author Giedrius Trumpickas
 */
public interface Value<T>
		extends ValueExp, Serializable, Supplier<T>
{
	/**
	 * Gets attribute type
	 *
	 * @return {@link ValueType}
	 */
	ValueExpTypeInfo getEvaluatesTo();

	/**
	 * Gets attribute expression value
	 *
	 * @return an attribute expression value
	 */
	T get();

	/**
	 * Creates bag with this attribute in the bag
	 *
	 * @return {@link BagOfValues}
	 */
	default BagOfValues toBag(){
		return this.getEvaluatesTo()
				   .getValueType()
		           .bagBuilder()
		           .attribute(this)
		           .build();
	}

	default void accept(ExpressionVisitor v) {
		if(!(v instanceof Value.Visitor)){
			return;
		}
		((Visitor) v).visitEnter(this);
		((Visitor) v).visitLeave(this);
	}

	interface Visitor extends ExpressionVisitor
	{
		default void visitEnter(Value<?> value){
		}
		default void visitLeave(Value<?> value){
		}
	}

	abstract class SelfValue<T>  extends AbstractValue<T>
	{
		protected SelfValue(
				ValueType attrType) {
			super(attrType);
		}

		@Override
		public final T get(){
			return (T)this;
		}

		public abstract int hashCode();
		public abstract boolean equals(Object o);
	}

	class OtherValue<T>
			implements Value<T>, Supplier<T>
	{
		private static final long serialVersionUID = 4131180767511036271L;

		protected T value;
		private ValueType type;

		protected OtherValue(
				ValueType attrType,
				T attrValue) {
			this.type = Objects.requireNonNull(attrType);
			this.value = Objects.requireNonNull(attrValue);
		}

		@Override
		public final ValueType getEvaluatesTo(){
			return type;
		}

		@Override
		public final T get(){
			return value;
		}

		protected final String formatVal(T v){
			return value.toString();
		}

		@Override
		public final int hashCode(){
			return Objects.hash(value, type);
		}

		@Override
		public final boolean equals(Object o) {
			if (o == this) {
				return true;
			}
			if (!(o instanceof Value.OtherValue)) {
				return false;
			}
			OtherValue e = (OtherValue) o;
			return type.equals(e.getEvaluatesTo()) &&
					value.equals(e.get());
		}
	}

    abstract class AbstractValue<T>
            implements Value<T>, Supplier<T>
    {
        private final ValueType type;

        protected AbstractValue(
                ValueType attrType) {
            this.type = Objects.requireNonNull(attrType);
        }

        @Override
        public final ValueType getEvaluatesTo(){
            return type;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
					.add("type", type.getShortTypeId())
                    .add("value", formatVal(get()))
                    .toString();
        }

        protected abstract String formatVal(T v);
    }
}
